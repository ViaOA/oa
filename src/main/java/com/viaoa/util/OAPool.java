/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Maintains a pool of objects, with a mini and max limits, and
 * will shrink/release if not needed or time.
 * @author vvia
 *
 * @param <TYPE> type of objects to be pooled.
 */
public abstract class OAPool<TYPE> {
    private static Logger LOG = Logger.getLogger(OAPool.class.getName());
    private Class<TYPE> classType;
    private int min;
    private int max;
    private ArrayList<Pool> alResource = new ArrayList<Pool>();
    private volatile int currentUsed;
    private volatile int highMark;
    private volatile long msHighMarkValid;  // msTime that highMark is valie
    
    class Pool {
        TYPE resource;
        boolean used;
    }
    
    public OAPool(Class clazz, int min, int max) {
        this.classType = clazz;
        this.min = min;
        this.max = max;
    }
    
    public OAPool(int min, int max) {
        this.min = min;
        this.max = max;
        Class c = getClass();
        for (; c != null;) {
            Type type = c.getGenericSuperclass();
            if (type instanceof ParameterizedType) {
                classType = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                break;
            }
            c = c.getSuperclass();
        }
        LOG.fine("classType=" + classType);
        if (classType == null) {
            throw new RuntimeException("class must define <TYPE>, or use constructure that accepts 'Class clazz'");
        }
    }
    
    public void setMinimum(int x) {
        min = x;
    }
    public int getMinimum() {
        return min;
    }
    public void setMaximum(int x) {
        max = x;
    }
    public int getMaximum() {
        return max;
    }
    public int getCurrentSize() {
        int x;
        synchronized (alResource) {
            x = alResource.size();
        }
        return x;
    }
    public int getCurrentUsed() {
        synchronized (alResource) {
            return currentUsed;
        }
    }

    /**
     * This will make sure that the pool has at least minimum amount of objects.
     * By default, the pool will start with size zero objects.  
     */
    public void loadMinimum() {
        ArrayList<Pool> al = new ArrayList<Pool>(min);
        synchronized (alResource) {
            int x = alResource.size();
            for (int i=x; i<min; i++) {
                Pool p = new Pool();
                p.used = true;
                alResource.add(p);
                al.add(p);
            }
        }
        
        for (Pool p : al) {
            if (p.resource != null) continue;
            TYPE res = create();
            synchronized (alResource) {
                p.resource = res;
                p.used = false;
            }
        }
        if (al.size() > 0) {
            synchronized (alResource) {
                alResource.notifyAll();
            }            
        }
    }
    
    public TYPE get() {
        Pool pool = null;
        synchronized (alResource) {
            for ( ;; ) {
                for (Pool p: alResource) {
                    if (!p.used && (p.resource != null)) {
                        pool = p;
                        break;
                    }
                }
                if (pool != null) break;
                
                int x = alResource.size();
                if (x < max || max == 0) {
                    pool = new Pool();
                    alResource.add(pool);
                    break;
                }

                // need to wait
                try {
                    alResource.wait();
                }
                catch (Exception e) {
                }
            }
            pool.used = true;
            
            currentUsed++;
            long msNow = System.currentTimeMillis();
            if (currentUsed >= highMark || msNow > msHighMarkValid) {
                highMark = currentUsed;
                msHighMarkValid = msNow + 2500;
            }
        }
        // needs to be create outside of sync block
        if (pool.resource == null) {
            TYPE res = create();
            pool.resource = res;
        }
        return pool.resource;
    }
    
    // remove from the pool
    public void remove(TYPE resource) {
        boolean bFound = false;
        synchronized (alResource) {
            for (Pool p: alResource) {
                if (p.resource != resource) continue;
                if (p.used) currentUsed--;
                p.used = false;
                alResource.remove(p);
                alResource.notifyAll();
                bFound = true;
                break;
            }
        }        
        if (bFound) removed(resource);
    }
    
    public void release(TYPE resource) {
        boolean bRelease = false;
        synchronized (alResource) {
            for (Pool p: alResource) {
                if (p.resource != resource) continue;
                if (p.used) currentUsed--;
                p.used = false;

                int x = alResource.size();
                if (x > min) {
                    long msNow = System.currentTimeMillis();
                    int mark = (msNow > msHighMarkValid) ? min : highMark;
                    if (mark < x) {
                        bRelease = true;
                        highMark = Math.max(min, highMark-1);
                        msHighMarkValid = msNow + 250;
                    }
                }
                if (bRelease) {
                    alResource.remove(p);
                }
                else {
                    alResource.notifyAll();
                }
                break;
            }
        }
        if (bRelease) {
            removed(resource);
        }
    }

    /**
     * @return object array of all objects that are currently in the pool.
     */
    public Object[] getAllItems() {
        synchronized (alResource) {
            int x = alResource.size();
            Object[] objs = alResource.toArray();
            return objs;
        }
    }
    
    /**
     * Callback method used to request a new object for the pool.
     */
    protected abstract TYPE create();
    
    /**
     * Callback method used when an object in the pool is no longer needed.
     */
    protected abstract void removed(TYPE resource);
    
}
