package com.viaoa.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.util.OAString;

/**
 * Uses a single thread to rerun a process whenever refresh is called.
 * 
 * @author vvia
 */
public abstract class OARefresher {
    private static Logger LOG = Logger.getLogger(OARefresher.class.getName());

    private final AtomicInteger aiChange = new AtomicInteger();
    private final AtomicInteger aiStartStop = new AtomicInteger();
    private final AtomicInteger aiThreadId = new AtomicInteger();
    private final Object lock = new Object();
    private Thread thread;
    private volatile int lastChange;

    public void refresh() {
        aiChange.incrementAndGet();
        synchronized (lock) {
            lock.notifyAll();
        }
    }

    public void addListener(Hub hub, final String propertyPath) {
        addListener(hub, propertyPath, false);
    }

    public void addListener(Hub hub) {
        addListener(hub, null, false);
    }

    public void addListener(Hub hub, final String propertyPath, boolean bActiveObjectOnly) {
        if (OAString.isNotEmpty(propertyPath)) {
            hub.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterPropertyChange(HubEvent e) {
                    if (propertyPath == null) return;
                    String prop = e.getPropertyName();
                    if (prop != null && prop.equalsIgnoreCase(propertyPath)) {
                        refresh();
                    }
                }
            }, propertyPath, bActiveObjectOnly);
        }
        else {
            hub.addHubListener(new HubListenerAdapter() {
                @Override
                public void afterAdd(HubEvent e) {
                    refresh();
                }

                @Override
                public void afterInsert(HubEvent e) {
                    refresh();
                }

                @Override
                public void afterNewList(HubEvent e) {
                    refresh();
                }

                @Override
                public void afterRemove(HubEvent e) {
                    refresh();
                }

                @Override
                public void afterRemoveAll(HubEvent e) {
                    refresh();
                }
            });
        }
    }

    /**
     * Called when it's time to process.
     * 
     * @see #isChanged() to know if refresh has called since process started.
     */
    protected abstract void process() throws Exception;

    /**
     * used to know if refresh has been called since processing.
     */
    protected boolean hasChanged() {
        int x = aiChange.get();
        return (x != lastChange);
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
                processQueue();
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

    protected void processQueue() {
        final int cntStartStop = aiStartStop.get();
        LOG.fine("created queue processor, cntStartStop=" + cntStartStop + ", thread name=" + Thread.currentThread().getName());
        for (;;) {
            try {
                if (cntStartStop != aiStartStop.get()) break;
                synchronized (lock) {
                    lock.wait(60 * 1000);
                }
                if (cntStartStop != aiStartStop.get()) break;

                int x = aiChange.get();
                if (x == lastChange) continue;
                lastChange = x;

                process();
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error while processing from oarefresher", e);
            }
        }
        LOG.fine("stopped refresh processor, cntStartStop=" + cntStartStop + ", thread name=" + Thread.currentThread().getName());
    }
}
