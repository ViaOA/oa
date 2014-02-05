package com.viaoa.ds;

import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Manages expired queries.
 * @author vvia
 *
 */
public class OASelectManager {
    private static Logger LOG = Logger.getLogger(OASelectManager.class.getName());
    
    private static ConcurrentHashMap<OASelect, OASelect> hmSelect = new ConcurrentHashMap<OASelect, OASelect>(23, .75f, 3);
    private static AtomicBoolean abStartThread = new AtomicBoolean(false);
    private static int timeLimitInSeconds = (5 * 60);

    public static void setTimeLimit(int seconds) {
        timeLimitInSeconds = seconds;
    }
    
    
    public static void add(OASelect sel) {
        hmSelect.put(sel, sel);
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
        hmSelect.remove(sel);
    }    

    protected static void performCleanup() {
        LOG.fine("checking selects");
        long time = new Date().getTime();
        time -= (timeLimitInSeconds * 1000);

        int iTotal = hmSelect.size();
        Set<Map.Entry<OASelect, OASelect>> set = hmSelect.entrySet();
        
        for (Iterator<Map.Entry<OASelect, OASelect>> it = set.iterator() ; it.hasNext(); ) {
            Map.Entry<OASelect, OASelect> me = it.next();
            OASelect sel = me.getKey();

            if (sel.isCancelled()) {
                it.remove();
                continue;
            }
            
            if (!sel.hasBeenStarted()) continue;
            
            long t = sel.getLastReadTime();
            if (t == 0) continue;
            
            if (t < time) {
                sel.cancel();
                it.remove();
            }
        }
        LOG.fine("done, before="+iTotal+", after="+hmSelect.size());
    }
    
}
