package com.viaoa.process;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.concurrent.OAExecutorService;
import com.viaoa.hub.*;

/**
 * Used to listen to one or more hubs + propertyPaths and run a process whenever a change is made. 
 * 
 * @author vvia
 */
public abstract class OAProcess {
    private static Logger LOG = Logger.getLogger(OAProcess.class.getName());

    private static final AtomicInteger aiCount = new AtomicInteger();
    private ArrayList<MyListener> alMyListener;
    private final OAExecutorService execService;
    
    
    /**
     * 
     * @param bUseThreadPool if false then use current thread.
     */
    public OAProcess(boolean bUseThreadPool) {
        if (bUseThreadPool) {
            execService = new OAExecutorService();
        }
        else execService = null;
    }
    

    /**
     * Called when it's time to process.
     */
    protected abstract void process(HubEvent evt) ;
    

    private void onProcess(final HubEvent evt) {
        if (execService != null) {
            execService.submit(new Runnable() {
                @Override
                public void run() {
                    OAProcess.this.process(evt);
                }
            });
        }
        else {
            process(evt);
        }
    }
    
    
    private static class MyListener {
        Hub hub;
        HubListener hl;

        public MyListener(Hub h, HubListener hl) {
            this.hub = h;
            this.hl = hl;
        }
    }

    public void addListener(Hub hub, String... propertyPaths) {
        if (hub == null) return;
        if (propertyPaths == null) {
            addListener(hub, (String) null);
        }
        else {
            final String name = "OAProcess." + aiCount.getAndIncrement();
            HubListener hl = new HubListenerAdapter() {
                @Override
                public void afterPropertyChange(HubEvent e) {
                    if (name.equalsIgnoreCase(e.getPropertyName())) {
                        onProcess(e);
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
                        onProcess(e);
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
                        onProcess(e);
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

}
