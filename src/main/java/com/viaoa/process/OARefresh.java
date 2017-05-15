package com.viaoa.process;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.*;

/**
 * Used to listen to one or more hubs + propertyPaths and run a process whenever a change is made. Uses
 * a single thread to rerun a process whenever refresh is called.  
 * 
 * Note: since process is ran in another thread, it can find out if any more changes have happened since it has started.
 * 
 * @see #start() to start the thread
 * @see #refresh() to manually run process
 * @see #stop() to stop the thread.
 * @author vvia
 */
public abstract class OARefresh {
    private static Logger LOG = Logger.getLogger(OARefresh.class.getName());

    private static final AtomicInteger aiCount = new AtomicInteger();

    private final AtomicInteger aiChange = new AtomicInteger();
    private final AtomicInteger aiStartStop = new AtomicInteger();
    private final AtomicInteger aiThreadId = new AtomicInteger();
    
    private final Object lock = new Object();
    private Thread thread;
    private ArrayList<MyListener> alMyListener;
    private volatile int lastChange = -1;

    private static class MyListener {
        Hub hub;
        HubListener hl;

        public MyListener(Hub h, HubListener hl) {
            this.hub = h;
            this.hl = hl;
        }
    }

    
    /**
     * Called when it's time to process.
     * 
     * @see #isChanged() to know if refresh has been called since process started.
     */
    protected abstract void process() throws Exception;
    
    /**
     * used to have the process rerun.  Can be called manually.
     */
    public void refresh() {
        aiChange.incrementAndGet();
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void addListener(Hub hub, String... propertyPaths) {
        if (hub == null) return;
        if (propertyPaths == null) {
            addListener(hub, (String) null);
        }
        else {
            final String name = "OARefresher." + aiCount.getAndIncrement();
            HubListener hl = new HubListenerAdapter() {
                @Override
                public void afterPropertyChange(HubEvent e) {
                    if (name.equalsIgnoreCase(e.getPropertyName())) {
                        refresh();
                    }
                }
            };
            hub.addHubListener(hl, name, propertyPaths);
            MyListener ml = new MyListener(hub, hl);
            if (alMyListener == null) alMyListener = new ArrayList<MyListener>();
            alMyListener.add(ml);
        }
    }
    
    public void addListener(Hub hub, final String propertyPath) {
        if (hub == null) return;
        HubListener hl;

        if (propertyPath != null && propertyPath.indexOf(".") < 0) {
            hl = new HubListenerAdapter() {
                @Override
                public void afterPropertyChange(HubEvent e) {
                    if (propertyPath.equalsIgnoreCase(e.getPropertyName())) {
                        refresh();
                    }
                }
            };
        }
        else {
            final String name = "OARefresher." + aiCount.getAndIncrement();
            hl = new HubListenerAdapter() {
                @Override
                public void afterPropertyChange(HubEvent e) {
                    if (name.equalsIgnoreCase(e.getPropertyName())) {
                        refresh();
                    }
                }
            };
            hub.addHubListener(hl, name, new String[] { propertyPath });
        }

        MyListener ml = new MyListener(hub, hl);
        if (alMyListener == null) alMyListener = new ArrayList<MyListener>();
        alMyListener.add(ml);
    }

    @Override
    protected void finalize() throws Throwable {
        if (alMyListener != null) {
            for (MyListener ml : alMyListener) {
                ml.hub.removeHubListener(ml.hl);
            }
        }
        super.finalize();
    };

    /**
     * used to know if refresh has been called since processing.
     */
    protected boolean hasChanged() {
        int x = aiChange.get();
        return (x == lastChange);
    }
    protected boolean isChanged() {
        int x = aiChange.get();
        return (x == lastChange);
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
        thread.setName("OARefresher." + aiThreadId.incrementAndGet());
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        aiStartStop.incrementAndGet();

        synchronized (lock) {
            lock.notifyAll();
        }
        LOG.fine("stop called, aiStartStop=" + aiStartStop);
    }

    protected void runThread() {
        final int iStartStop = aiStartStop.get();
        LOG.fine("created queue processor, cntStartStop=" + iStartStop + ", thread name=" + Thread.currentThread().getName());
        for (;;) {
            try {
                if (iStartStop != aiStartStop.get()) break;
                synchronized (lock) {
                    lock.wait(60 * 1000);
                }
                if (iStartStop != aiStartStop.get()) break;

                int x = aiChange.get();
                if (x == lastChange) continue;
                lastChange = x;

                process();
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error processing from queue", e);
            }
        }
        LOG.fine("stopped OARefresher thread, cntStartStop=" + iStartStop + ", thread name=" + Thread.currentThread().getName());
    }
}
