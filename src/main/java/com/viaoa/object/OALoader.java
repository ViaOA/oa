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
    private boolean bRequiresCasade;

    private final int threadCount;
    private volatile OAExecutorService executorService;

    private final AtomicInteger aiVisitCnt = new AtomicInteger();
    private final AtomicInteger aiNotLoadedCnt = new AtomicInteger();
    
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
    
    public void load(Hub<F> hubRoot) {
        if (hubRoot == null) return;

        bStop = false;
        setup(hubRoot.getObjectClass());
        if (threadCount > 0) executorService = new OAExecutorService(threadCount, "OALoader");

        for (F obj : hubRoot) {
            _load(obj);
            if (bStop) break;
        }
    }
    
    public void load(F objectRoot) {
        if (objectRoot == null) return;

        bStop = false;
        setup(objectRoot.getClass());
        if (threadCount > 0) executorService = new OAExecutorService(threadCount, "OALoader");

        _load(objectRoot);
    }
    
    protected void _load(F object) {
        if (object == null) return;

        OACascade cascade = null;
        if (bRequiresCasade) cascade = new OACascade(true);
        
        _load(object, 0, cascade);
    }

    private void _load(final Object obj, final int pos, final OACascade cascade) {
        if (obj == null) return;
        aiVisitCnt.incrementAndGet();
        
        if (obj instanceof Hub) {
            for (Object objx : (Hub) obj) {
                _load(objx, pos, cascade);
                if (bStop) break;
            }
            return;
        }

        if (!(obj instanceof OAObject)) return;
        if (cascade != null && cascade.wasCascaded((OAObject) obj, true)) return;

        // check if recursive
        if (pos == 0) {
            if (liRecursiveRoot != null) {
                Object objx = liRecursiveRoot.getValue(obj);
                _load(objx, pos, cascade); // go up a level to then go through hub
                if (bStop) return;
            }
        }
        else if (recursiveLinkInfos != null && pos <= recursiveLinkInfos.length && (recursiveLinkInfos[pos - 1] != null)) {
            if (executorService != null && !recursiveLinkInfos[pos - 1].isLoaded(obj)) {
                aiNotLoadedCnt.incrementAndGet();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (bStop) return;
                        Object objx = recursiveLinkInfos[pos - 1].getValue(obj);
                        _load(objx, pos, cascade);
                    }
                });
            }
            else {
                Object objx = recursiveLinkInfos[pos - 1].getValue(obj);
                _load(objx, pos, cascade);
                if (bStop) return;
            }
        }

        if (linkInfos != null && pos < linkInfos.length) {
            if (executorService != null && !linkInfos[pos].isLoaded(obj)) {
                aiNotLoadedCnt.incrementAndGet();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (bStop) return;
                        Object objx = linkInfos[pos].getValue(obj);
                        _load(objx, pos+1, cascade);
                    }
                });
                return;
            }
            Object objx = linkInfos[pos].getValue(obj);
            _load(objx, pos+1, cascade);
            if (bStop) return;
        }
    }


    public void waitUntilDone() {
        for (;;) {
            if (executorService == null) break;
            int x = executorService.getExecutorService().getActiveCount();
            
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
    }
}
