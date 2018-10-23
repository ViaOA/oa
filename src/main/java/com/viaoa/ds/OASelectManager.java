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
package com.viaoa.ds;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.viaoa.object.OAObject;

/**
 * Manages expired queries.
 * @author vvia
 *
 */
public class OASelectManager {
    private static Logger LOG = Logger.getLogger(OASelectManager.class.getName());
    
    private static ConcurrentHashMap<Integer, WeakReference<OASelect>> hmSelect = new ConcurrentHashMap<Integer, WeakReference<OASelect>>(23, .75f, 3);
    private static AtomicBoolean abStartThread = new AtomicBoolean(false);
    private static int timeLimitInSeconds = (5 * 60);

    public static void setTimeLimit(int seconds) {
        timeLimitInSeconds = seconds;
    }
    
    public static void add(OASelect sel) {
        if (sel == null) return;
        final int id = sel.getId();
        hmSelect.put(id, new WeakReference(sel));
        if (!abStartThread.compareAndSet(false, true)) return;
        
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        Thread.sleep(timeLimitInSeconds * 1000);
                        performCleanup();
                    }
                    catch (Exception e) {
                    }
                }
            }
        }, "OASelectManager");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

    
    public static void remove(OASelect sel) {
        final int id = sel.getId();
        WeakReference<OASelect> ref = hmSelect.remove(id);
    }    

    protected static void performCleanup() {
        LOG.finer("checking selects");
        long time = new Date().getTime();
        time -= (timeLimitInSeconds * 1000);

        int iTotal = hmSelect.size();
        Set<Map.Entry<Integer, WeakReference<OASelect>>> set = hmSelect.entrySet();
        
        for (Iterator<Map.Entry<Integer, WeakReference<OASelect>>> it = set.iterator() ; it.hasNext(); ) {
            Map.Entry<Integer, WeakReference<OASelect>> me = it.next();
            WeakReference<OASelect> ref = me.getValue();
            if (ref == null) continue;
            OASelect sel = ref.get();
            if (sel == null) {
                it.remove();
                continue;
            }
            if (sel.isCancelled()) {
                it.remove();
                continue;
            }
            
            if (!sel.hasBeenStarted()) continue;
            
            long t = sel.getLastReadTime();
            if (t == 0) continue;
            
            if (t < time) {
                if (!OAObject.getDebugMode()) {
                    LOG.warning("cancel select, after timeout.  Select="+sel.getSelectClass()+", where="+sel.getWhere());
                    sel.cancel();
                    it.remove();
                }
            }
        }
        LOG.finer("done, before="+iTotal+", after="+hmSelect.size());
    }
    
}
