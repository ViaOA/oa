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
    private volatile int maxUsed;
    private volatile long msMaxUsed;
    
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
            throw new RuntimeException("class must define <TYPE>, or use construture that accepts 'Class clazz'");
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

    /**
     * This will make sure that the pool has at least minimum amount of objects.
     * By default, the pool will start with size zero objects.  
     */
    public void loadMinimum() {
        synchronized (alResource) {
            int x = alResource.size();
            for (int i=x; i<min; i++) {
                TYPE res = create();
                Pool p = new Pool();
                p.resource = res;
                p.used = true;
                alResource.add(p);
            }
        }
    }
    
    public TYPE get() {
        TYPE x = _get();
        return x;
    }
    
    protected TYPE _get() {
        for (int i=0;;i++) {
            synchronized (alResource) {
                if (i == 0) {
                    currentUsed++;
                    if (currentUsed > maxUsed) {
                        maxUsed = currentUsed;
                        msMaxUsed = System.currentTimeMillis();
                    }
                }
                
                for (Pool p: alResource) {
                    if (!p.used) {
                        p.used = true;
                        return p.resource;
                    }
                }
                int x = alResource.size();
                if (x < max || max == 0) {
                    TYPE res = create();
                    Pool p = new Pool();
                    p.resource = res;
                    p.used = true;
                    alResource.add(p);
                    return res;
                }
                // need to wait
                try {
                    alResource.wait();
                }
                catch (Exception e) {
                }
            }
        }
    }
    // remove from the pool
    public void remove(TYPE resource) {
        synchronized (alResource) {
            for (Pool p: alResource) {
                if (p.resource != resource) continue;
                if (p.used) currentUsed--;
                p.used = false;
                alResource.remove(p);
                removed(resource);
                alResource.notifyAll();
                break;
            }
        }        
    }
    public void release(TYPE resource) {
        synchronized (alResource) {
            
            // see if the pool can be shrunk, by removing this resource
            boolean bRelease = false;
            int x = alResource.size();
            if (x > min && (currentUsed+1) < x) {
                long msNow = System.currentTimeMillis();
                if (msNow - msMaxUsed > 800) {
                    if (x > (maxUsed+1)) bRelease = true;
                    msMaxUsed = msNow;
                    maxUsed = currentUsed;
                }
            }
            
            for (Pool p: alResource) {
                if (p.resource != resource) continue;
                if (p.used) currentUsed--;
                p.used = false;
                if (bRelease) {
                    alResource.remove(p);
                    removed(resource);
                }
                else {
                    alResource.notifyAll();
                }
                break;
            }
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
