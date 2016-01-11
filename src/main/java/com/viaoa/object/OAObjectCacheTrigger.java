package com.viaoa.object;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAFilter;

/**
 * This is used to listen to the OAObjectCache for objects that match filter criteria and then call the onTrigger method.
 * 
 * @author vvia
 */
public abstract class OAObjectCacheTrigger<T extends OAObject> implements OAFilter<T> {
    // Note: this code very similar to OAObjectCacheFilter

    
    private Class<T> clazz;
    
    // list of propPaths to listen for
    private String[] dependentPropertyNames;

    // this will be set when a calc property is needed for the dependent propertyPath(s)
    private String calcDependentPropertyName;
    
    // used to create a unique calc propName
    private static AtomicInteger aiUnique = new AtomicInteger();

    // object cache listener
    private OAObjectCacheListener<T> hlObjectCache;

    // if a calc prop is used, then objects will be put into a temp hub, so that they can be listened to.
    private Hub<T> hubTemp; 
    private HubListener<T> hlTemp;

    // list of filters that must return true for the isUsed to return true.
    private ArrayList<OAFilter<T>> alFilter;

    protected boolean bServerSideOnly;
    
    /**
     * Create new cache trigger.  Cached objects that are true for isUsedFromObjectCache & isUsed will then call onTrigger.
     */
    public OAObjectCacheTrigger(Class clazz) {
        this(clazz, null);
    }
    
    /**
     * Create new cache trigger.  Cached objects that are true for isUsedFromObjectCache & isUsed will then call onTrigger.
     */
    public OAObjectCacheTrigger(Class clazz, OAFilter<T> filter) {
        if (clazz == null) throw new RuntimeException("class can not be null");
        this.clazz = clazz;
 
        if (filter != null) addFilter(filter);
        setupCacheListener();
    }
    
    public OAObjectCacheTrigger(Class clazz, OAFilter<T> filter, String ... dependentPropPaths) {
        if (clazz == null) throw new RuntimeException("class can not be null");
        this.clazz = clazz;
 
        if (filter != null) addFilter(filter);
        
        if (dependentPropPaths != null) {
            for (String pp : dependentPropPaths) {
                addDependentProperty(pp);
            }
        }
        
        setupCacheListener();
    }

    /**
     * This needs to be set to true if it is only created on the server.
     * This is so that changes will be published to the clients, even if initiated on OAClientThread. 
     */
    public void setServerSideOnly(boolean b) {
        bServerSideOnly = b;
    }
    
    /**
     * Add a filter that is used to know if trigger method should be called.
     */
    public void addFilter(OAFilter<T> f) {
        if (f == null) return;
        if (alFilter == null) alFilter = new ArrayList<OAFilter<T>>();
        alFilter.add(f);
    }
    
    public void addFilter(OAFilter<T> f, String ... dependentPropPaths) {
        addFilter(f);
        if (dependentPropPaths == null) return;
        for (String pp : dependentPropPaths) {
            addDependentProperty(pp);
        }
    }

    /**
     * add a property to listen to.  If the property changes, then it will be recalculated to determine if it should be added to hub, or removed from it.
     * This will recheck the object cache to see if any of the existing objects isUsed() is true and should be added to hub.
     */
    public void addDependentProperty(final String prop) {
        if (prop == null || prop.length() == 0) return;
        
        dependentPropertyNames = (String[]) OAArray.add(String.class, dependentPropertyNames, prop);
        
        // check to see if a calc property is used, which will require a temp Hub to be set up.
        if (calcDependentPropertyName == null) {
            boolean b = (prop.indexOf(".") >= 0);
            if (!b) {
                OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(clazz);
                String[] calcProps = null;
                for (OACalcInfo ci : oi.getCalcInfos()) {
                    if (ci.getName().equalsIgnoreCase(prop)) {
                        b = true;
                    }
                }
            }
            
            if (b) {
                calcDependentPropertyName = "OAObjectCacheTrigger" + (aiUnique.incrementAndGet());
                hubTemp = new Hub(clazz);
            }
        }
        
        if (calcDependentPropertyName != null) {
            if (hlTemp == null) setupTempHubListener();
        }
    }
    
    
    protected void setupCacheListener() {
        if (hlObjectCache != null) return;
        hlObjectCache = new OAObjectCacheListener<T>() {
            @Override 
            public void afterPropertyChange(T obj, String propName, Object oldValue, Object newValue) {
                if (propName == null) return;

                if (hubTemp != null) return; // hubTemp listener will get propChange for calcPropName
                
                if (dependentPropertyNames == null) return;
                boolean b = false;
                for (String s : dependentPropertyNames) {
                    if (s.equalsIgnoreCase(propName)) {
                        b = true;
                    }
                }
                if (!b) return;
                
                if (isUsedFromObjectCache(obj) && isUsed(obj)) {
                    callOnTrigger(obj);
                }
            }
            
            @Override
            public void afterAdd(T obj) {
                if (!isUsedFromObjectCache(obj)) return; // it's new so it cant be in hubs yet

                if (hubTemp != null) {
                    hubTemp.add(obj);
                }

                if (isUsed(obj)) {
                    boolean b = OAThreadLocalDelegate.isLoadingObject();
                    try {
                        if (b) OAThreadLocalDelegate.setLoadingObject(false);
                        callOnTrigger(obj);
                    }
                    finally {
                        if (b) OAThreadLocalDelegate.setLoadingObject(true);
                    }
                }
            }
        };
        OAObjectCacheDelegate.addListener(clazz, hlObjectCache);
    }
    protected void setupTempHubListener() {
        if (hlTemp != null) return;
        if (hubTemp == null) return;
        if (calcDependentPropertyName == null) return;
        
        // only used if a calc property is being used
        // need to put all objects in hubTemp so that it can listen to prop changes
        hlTemp = new HubListenerAdapter<T>() {
            @Override
            public void afterPropertyChange(HubEvent<T> e) {
                String propName = e.getPropertyName();
                if (propName == null) return;
                if (!calcDependentPropertyName.equalsIgnoreCase(propName)) return;
                    
                T obj = e.getObject();
                boolean b = isUsedFromObjectCache(obj); 
                if (!b) hubTemp.remove(obj);
                        
                b = b && isUsed(obj);

                if (b) callOnTrigger(obj);
            }
        };
        hubTemp.addHubListener(hlTemp, calcDependentPropertyName, dependentPropertyNames);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    public void close() {
        if (hlObjectCache != null) {
            OAObjectCacheDelegate.removeListener(clazz, hlObjectCache);
            hlObjectCache = null;
        }
        if (hlTemp != null) {
            hubTemp.removeHubListener(hlTemp);
            hlTemp = null;
        }
    }
    
    /**
     * Called to see if an object should be included in hub.
     * By default, this will return true if all filters.isUsed() returns true.  If no filters, then default is to return true. 
     */
    @Override
    public boolean isUsed(T obj) {
        if (alFilter != null) {
            for (OAFilter f : alFilter) {
                if (!f.isUsed(obj)) return false;
            }
        }
        return true;
    }
    
    /**
     * Called to see if this object needs to used from the ObjectCache.  If this returns false, then it wont be considered to be added to hub.
     * @return true by default, can be overwritten to be selective on which objects are being used from the cache.
     */
    public boolean isUsedFromObjectCache(T obj) {
        return true;
    }

    private void callOnTrigger(T obj) {
        try {
            if (bServerSideOnly) { 
                OARemoteThreadDelegate.sendMessages(true);
            }
            onTrigger(obj);
        }
        finally {
            if (bServerSideOnly) {
                OARemoteThreadDelegate.sendMessages(false);
            }
        }
        
    }
    
    /**
     * Method that will be called when isUsed() returns true, and isUsedFromObjectCache() returns true.
     * @param obj
     */
    public abstract void onTrigger(T obj);
}
