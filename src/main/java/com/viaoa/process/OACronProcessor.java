package com.viaoa.process;

import java.util.ArrayList;
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
    private ArrayList<OACron> alCron;

    private Thread thread;
    private final Object lock = new Object();
    private final AtomicInteger aiStartStop = new AtomicInteger();
    private final AtomicInteger aiThreadId = new AtomicInteger();
    
    private volatile OADateTime dtLast;

    
    /**
     * 
     * @param bUseThreadPool if false then use current thread.
     */
    public OACronProcessor(OADateTime dtLast) {
        if (dtLast == null) this.dtLast = new OADateTime();
        else this.dtLast = dtLast;
        
        execService = new OAExecutorService();
        alCron = new ArrayList<OACron>();
    }
    public OACronProcessor() {
        this(null);
    }
    
    public OACron[] getCrons() {
        return (OACron[]) alCron.toArray();
    }

    public void add(OACron cron) {
        if (!alCron.contains(cron)) alCron.add(cron);
    }
    public void remove(OACron cron) {
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

    
    protected void process(final OACron cron) {
        if (cron == null) return;
        cron.process();
    }
    
    private void onProcess(final OACron cron) {
        execService.submit(new Runnable() {
            @Override
            public void run() {
                OACronProcessor.this.process(cron);
            }
        });
    }
    
    protected void runThread() {
        final int iStartStop = aiStartStop.get();
        LOG.fine("created cron processor, cntStartStop=" + iStartStop + ", thread name=" + Thread.currentThread().getName());
        for (;;) {
            try {
                if (iStartStop != aiStartStop.get()) break;
                synchronized (lock) {
                    lock.wait(60 * 1000);
                }
                if (iStartStop != aiStartStop.get()) break;
                
                OADateTime dtNow = new OADateTime();
                dtNow.setSecond(0);
                for (OACron cron : alCron) {
                    OADateTime dt = cron.findNext(dtLast);
                    if (dt.compareTo(dtNow) == 0) {
                        onProcess(cron);
                    }
                }
                dtLast = dtNow.addMinutes(1);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error processing from queue", e);
            }
        }
        LOG.fine("stopped OARefresher thread, cntStartStop=" + iStartStop + ", thread name=" + Thread.currentThread().getName());
    }
}
