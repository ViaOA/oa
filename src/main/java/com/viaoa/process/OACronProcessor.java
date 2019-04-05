package com.viaoa.process;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.viaoa.concurrent.OAExecutorService;
import com.viaoa.util.OADateTime;

/**
 * Used to manage OACron jobs, and uses OAExecutorService when it is time to process the cron. 
 * @author vvia
 */
public class OACronProcessor {
    private static Logger LOG = Logger.getLogger(OACronProcessor.class.getName());

    private final OAExecutorService execService;
    private CopyOnWriteArrayList<OACron> alCron;

    private Thread thread;
    private final Object lock = new Object();
    private final AtomicInteger aiStartStop = new AtomicInteger();
    private final AtomicInteger aiThreadId = new AtomicInteger();
    
    public OACronProcessor() {
        execService = new OAExecutorService();
        alCron = new CopyOnWriteArrayList<OACron>();
    }
    
    public OACron[] getCrons() {
        return (OACron[]) alCron.toArray(new OACron[0]);
    }

    public void add(OACron cron) {
        LOG.fine("cron="+cron.getName()+", "+cron.getDescription());
        if (!alCron.contains(cron)) {
            alCron.add(cron);
            synchronized (lock) {
                lock.notifyAll();
            }
        }
    }
    public void remove(OACron cron) {
        LOG.fine("cron="+cron.getName()+", "+cron.getDescription());
        alCron.remove(cron);
    }

    public boolean isRunning() {
        return (thread != null);
    }
    
    public void start() {
        aiStartStop.incrementAndGet();

        synchronized (lock) {
            lock.notifyAll();
        }

        LOG.fine("start called, aiStartStop=" + aiStartStop);
        thread = new Thread() {
            @Override
            public void run() {
                runThread();
            }
        };
        thread.setName("OACronProcessor." + aiThreadId.incrementAndGet());
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        aiStartStop.incrementAndGet();

        synchronized (lock) {
            lock.notifyAll();
            thread = null;
        }
        LOG.fine("stop called, aiStartStop=" + aiStartStop);
    }

    /*
     * called by {@link #runProcessInAnotherThread(OACron, boolean)} using an execService thread.
     */
    protected void callProcess(final OACron cron, boolean bManuallyCalled) {
        if (cron == null) return;
        cron.setLast(new OADateTime());
        LOG.fine("processing cron, name = "+cron.getName()+", description="+cron.getDescription());
        cron.process(bManuallyCalled);
    }

    
    /*
     * will use execService to then call {@link #process(OACron,boolean)}
     */
    public void callProcessInAnotherThread(final OACron cron, final boolean bManuallyCalled) {
        if (cron == null) return;
        execService.submit(new Runnable() {
            @Override
            public void run() {
                OACronProcessor.this.callProcess(cron, bManuallyCalled);
            }
        });
    }
    
    protected void runThread() {
        final int iStartStop = aiStartStop.get();
        LOG.fine("created cron processor, cntStartStop=" + iStartStop + ", thread name=" + Thread.currentThread().getName());
        
        OADateTime dtLast = null;
        final ArrayList<OACron> alLast = new ArrayList<>();
                
        
        for (;;) {
            try {
                if (iStartStop != aiStartStop.get()) break;
                
                OADateTime dtNow = new OADateTime();
                dtNow.clearSecondAndMilliSecond();
                
                OADateTime dtCompare = dtNow.addMinutes(-1);
                dtCompare.clearSecondAndMilliSecond();

                if (dtLast == null || dtLast.before(dtCompare)) {
                    alLast.clear();
                }
                dtLast = dtCompare;
                
                beforeProcess(dtNow);
                for (OACron cron : alCron) {
                    if (!cron.getEnabled()) continue;
                    if (alLast.contains(cron)) {
                        continue;
                    }
                    alLast.add(cron);

                    OADateTime dt = new OADateTime(cron.findNext(dtCompare));
                    dt.clearSecondAndMilliSecond();
                    
                    int d = dt.compareTo(dtNow);
                    if (d == 0) {
                        callProcessInAnotherThread(cron, false);
                    }
                }
                
                synchronized (lock) {
                    lock.wait(30 * 1000);
                }
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error processing from queue", e);
            }
        }
        LOG.fine("stopped OACronProcessor thread, cntStartStop=" + iStartStop + ", thread name=" + Thread.currentThread().getName());
    }
    
    protected void beforeProcess(OADateTime dtNow) {
    }
}
