package com.viaoa.hub;

/**
 * This will populate a Hub with the top N objects in another Hub.
 * @author vvia
 *
 */
public class HubSample<T> {
    protected final Hub<T> hubMaster;
    protected final Hub<T> hubSample;
    protected final int amtSample;
    protected HubListener<T> hubListener;

    /**
     * Create a hubSample instance.
     * @param hubMaster
     * @param hubSample will always be populated with hubMaster<0> .. <n-1>
     * @param sampleAmount number of objects in hubMaster to have in hubSample
     */
    public HubSample(Hub<T> hubMaster, Hub<T> hubSample, int sampleAmount) {
        this.hubMaster = hubMaster;
        this.hubSample = hubSample;
        this.amtSample = sampleAmount;
        setup();
    }
    
    protected void setup() {
        if (hubMaster == null && hubSample == null) return;
        hubListener = new HubListenerAdapter<T>() {
            @Override
            public void afterAdd(HubEvent<T> e) {
                int pos = hubMaster.getPos();
                if (e.getPos() < amtSample) refresh();
            }
            @Override
            public void afterInsert(HubEvent e) {
                if (e.getPos() < amtSample) refresh();
            }
            @Override
            public void afterNewList(HubEvent e) {
                refresh();
            }
            @Override
            public void afterRemove(HubEvent e) {
                if (e.getPos() < amtSample) refresh();
            }
            @Override
            public void afterRemoveAll(HubEvent e) {
                refresh();
            }
            @Override
            public void afterSort(HubEvent e) {
                refresh();
            }
        };
        hubMaster.addHubListener(hubListener);
        refresh();
    }
    
    protected void refresh() {
        for (int i=0; i<amtSample; i++) {
            T obj = hubMaster.getAt(i);
            if (obj == null) {
                hubSample.remove(i);
            }
            else {
                if (hubSample.getAt(i) != obj) {
                    hubSample.remove(obj);
                    hubSample.insert(obj, i);
                }
            }
        }
        for ( ; (hubSample.size() > amtSample) ; ) {
            hubSample.remove(amtSample);
        }
    }
    
    public void close() {
        if (hubListener != null) {
            hubMaster.removeListener(hubListener);
            hubListener = null;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
}
