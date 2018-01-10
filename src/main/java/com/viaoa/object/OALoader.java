/* Copyright 1999-2015 Vince Via vvia@viaoa.com Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License. */
package com.viaoa.object;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.concurrent.OAExecutorService;
import com.viaoa.ds.OASelect;
import com.viaoa.hub.*;
import com.viaoa.util.*;

// 20170611
/**
 * This is used to load a property path in parallel using multiple threads.
 *
 * @param <F>
 *            type of hub or OAObject to use as the root (from)
 * @param <T>
 *            type of hub for the to class (to).
 * 
 *  example:<code>
 * 
    OALoader<Company, Employee> l = new OALoader<Company, Employee>(CompanyPP.locations.employees);
    l.load(company);
    

    // recursive
    OALoader<ItemCategory, Product> loader = new OALoader(5, ItemCategoryPP.itemCategories().items().products().pp);
    loader.load(ModelDelegate.getItemCategories());
    
    </code>
 * 
 */
public class OALoader<F extends OAObject, T extends OAObject> {
    private String strPropertyPath;
    private OAPropertyPath<T> propertyPath;

    private OALinkInfo liRecursiveRoot;

    private OALinkInfo[] linkInfos;
    private OALinkInfo[] recursiveLinkInfos;
    private Method[] methods;

    private volatile boolean bStop;
    private boolean bSetup;

    private final int threadCount;
    private volatile OAExecutorService executorService;
    private final AtomicInteger aiThreadsUsed = new AtomicInteger(); 

    private final AtomicInteger aiVisitCnt = new AtomicInteger();
    private final AtomicInteger aiNotLoadedCnt = new AtomicInteger();
    
    private Hub<F> hubFrom;
    
    private OACascade[] cascades;

    public OALoader(int threadCount, String propPath) {
        this.threadCount = Math.min(threadCount, 50);
        this.strPropertyPath = propPath;
    }

    /**
     * This is used to stop the current find that is in process. This can be used when overwriting the
     * onFound().
     */
    public void stop() {
        bStop = true;
    }

    public int getVisitCount() {
        return aiVisitCnt.get();
    }
    public int getNotLoadedCount() {
        return aiNotLoadedCnt.get();
    }

    /**
     */
    public void load(Hub<F> hubRoot) {
        if (hubRoot == null) return;

        bStop = false;
        setup(hubRoot.getObjectClass());
        if (threadCount > 0) executorService = new OAExecutorService(threadCount, "OALoader");

        this.hubFrom = hubRoot;
        
        Hub hubHold = OAThreadLocalDelegate.getGetDetailHub();
        String ppHold = OAThreadLocalDelegate.getGetDetailPropertyPath();
        try {
            OAThreadLocalDelegate.setGetDetailHub(OALoader.this.hubFrom, OALoader.this.strPropertyPath);
            for (F obj : hubRoot) {
                _load(obj);
                if (bStop) break;
            }
            this.hubFrom = null;
        }
        finally {
            OAThreadLocalDelegate.resetGetDetailHub(hubHold, ppHold);
            this.hubFrom = null;
            cascades = null;
            if (executorService != null) executorService.close();
        }
    }
    
    public void load(OASelect<F> sel) {
        if (sel == null) return;

        bStop = false;
        setup(sel.getSelectClass());
        if (threadCount > 0) executorService = new OAExecutorService(threadCount, "OALoader");
        this.hubFrom = new Hub();

        Hub hubHold = OAThreadLocalDelegate.getGetDetailHub();
        String ppHold = OAThreadLocalDelegate.getGetDetailPropertyPath();
        try {
            OAThreadLocalDelegate.setGetDetailHub(OALoader.this.hubFrom, OALoader.this.strPropertyPath);
            for ( ;!bStop && (sel.hasMore() || hubFrom.size()>0); ) {
                for ( ;sel.hasMore() && hubFrom.size() < 200; ) {
                    if (bStop) break;
                    hubFrom.add(sel.next());
                }
                Object obj = hubFrom.getAt(0);
                hubFrom.remove(0);
                _load((F) obj);
            }        
        }
        finally {
            OAThreadLocalDelegate.resetGetDetailHub(hubHold, ppHold);
            this.hubFrom = null;
            cascades = null;
            if (executorService != null) executorService.close();
        }
    }
    
    
    public void load(F objectRoot) {
        if (objectRoot == null) return;

        bStop = false;
        setup(objectRoot.getClass());
        if (threadCount > 0) executorService = new OAExecutorService(threadCount, "OALoader");

        hubFrom = new Hub(objectRoot.getClass());
        hubFrom.add(objectRoot);
        _load(objectRoot);
        hubFrom = null;
        cascades = null;
        if (executorService != null) executorService.close();
    }
    
    protected void _load(F object) {
        if (object == null) return;

        _load(object, 0);
    }

    private void _load(final Object obj, final int pos) {
        if (obj == null) return;
        aiVisitCnt.incrementAndGet();
        
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _load(objx, pos);
                if (bStop) break;
            }
            return;
        }

        if (!(obj instanceof OAObject)) return;

        if (pos > 0 && cascades != null) {
            if (cascades[pos-1].wasCascaded((OAObject) obj, true)) return;
        }
        
        // check if recursive
        if (pos == 0) {
            if (liRecursiveRoot != null) {
                Object objx = liRecursiveRoot.getValue(obj);
                _load(objx, pos); // go up a level to then go through hub
                if (bStop) return;
            }
        }
        else if (recursiveLinkInfos != null && pos <= recursiveLinkInfos.length && (recursiveLinkInfos[pos - 1] != null)) {
            if (executorService != null && !recursiveLinkInfos[pos - 1].isLoaded(obj) && aiThreadsUsed.get() < threadCount) {
                int x = aiThreadsUsed.incrementAndGet();
                if (x <= threadCount) {
                    aiNotLoadedCnt.incrementAndGet();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (bStop) return;
                            try {
                                OAThreadLocalDelegate.setGetDetailHub(OALoader.this.hubFrom, OALoader.this.strPropertyPath);
                                Object objx = recursiveLinkInfos[pos - 1].getValue(obj);
                                _load(objx, pos);
                            }
                            finally {
                                OAThreadLocalDelegate.resetGetDetailHub(null, null);
                                aiThreadsUsed.decrementAndGet();
                            }
                        }
                    });
                    return;
                }
                aiThreadsUsed.decrementAndGet();
            }
            Object objx = recursiveLinkInfos[pos - 1].getValue(obj);
            _load(objx, pos);
            if (bStop) return;
        }

        if (linkInfos != null && pos < linkInfos.length) {
            boolean b = linkInfos[pos].isLoaded(obj);
if (!b) {
    int xx = 4;
    xx++;
}
            if (executorService != null && !b && aiThreadsUsed.get() < threadCount) {
                int x = aiThreadsUsed.incrementAndGet();
                if (x <= threadCount) {
                    aiNotLoadedCnt.incrementAndGet();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (bStop) return;
                            try {
                                OAThreadLocalDelegate.setGetDetailHub(OALoader.this.hubFrom, OALoader.this.strPropertyPath);
                                Object objx = linkInfos[pos].getValue(obj);
                                _load(objx, pos+1);
                            }
                            finally {
                                OAThreadLocalDelegate.resetGetDetailHub(null, null);
                                aiThreadsUsed.decrementAndGet();
                            }
                        }
                    });
                    return;
                }
                aiThreadsUsed.decrementAndGet();
            }
            Object objx = linkInfos[pos].getValue(obj);
            _load(objx, pos+1);
            if (bStop) return;
        }
    }


    public void waitUntilDone() {
        for (;;) {
            if (executorService == null) break;
            int x = executorService.getActiveThreads();
            
            if (x < 1) break;
            try {
                Thread.sleep(250);
            }
            catch (Exception e) {}
        }
        
        if (executorService != null) {
            executorService.close();
            executorService = null;
        }
    }
    
    
    protected void setup(Class c) {
        if (bSetup) return;
        bSetup = true;
        if (propertyPath != null || c == null) return;
        propertyPath = new OAPropertyPath(c, strPropertyPath);

        linkInfos = propertyPath.getLinkInfos();
        recursiveLinkInfos = propertyPath.getRecursiveLinkInfos();
        methods = propertyPath.getMethods();

        if (linkInfos.length != methods.length) {
            // oafinder is to get from one OAObj/Hub to another, not a property/etc
            throw new RuntimeException("propertyPath " + strPropertyPath + " must end in an OAObject/Hub");
        }

        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(c);
        liRecursiveRoot = oi.getRecursiveLinkInfo(OALinkInfo.MANY);

        /*was
        bRequiresCasade = true;
        if (linkInfos != null && linkInfos.length > 0) {
            HashSet<Class> hs = new HashSet<Class>();
            for (OALinkInfo li : linkInfos) {
                if (hs.contains(li.getToClass())) {
                    bRequiresCasade = false;
                    break;
                }
                hs.add(li.getToClass());
            }
        }
        */
        
        if (linkInfos != null && linkInfos.length > 0) {
            cascades = new OACascade[linkInfos.length];
            for (int i=0; i<linkInfos.length; i++) {
                cascades[i] = new OACascade(true);  // true= use lock
                
            }
        }
    }
}
