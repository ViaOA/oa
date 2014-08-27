/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
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
