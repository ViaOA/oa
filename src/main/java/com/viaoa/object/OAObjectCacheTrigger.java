package com.viaoa.object;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
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
    private static final long serialVersionUID = 1L;
    private Class<T> clazz;

    private String name;

    private OAObjectCacheListener cacheListener;    
    
    // list of propPaths to listen for
    private String[] dependentPropertyPaths;
    
    
    
    // used to create a unique calc propName
    private static final AtomicInteger aiUnique = new AtomicInteger();  

    private OATrigger trigger;
    
    // list of filters that must return true for the isUsed to return true.
    private ArrayList<OAFilter<T>> alFilter;

    protected boolean bServerSideOnly;
    
    
    
    /**
     * Create new cache trigger.  Cached objects that are true for isUsedFromObjectCache &amp; isUsed will then call onTrigger.
     */
    public OAObjectCacheTrigger(Class clazz) {
        this(clazz, null);
    }
    
    /**
     * Create new cache trigger.  Cached objects that are true for isUsedFromObjectCache &amp; isUsed will then call onTrigger.
     */
    public OAObjectCacheTrigger(Class clazz, OAFilter<T> filter) {
        this(clazz, filter, null);
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
        
        cacheListener = new OAObjectCacheListener<T>() {
            @Override
            public void afterPropertyChange(T obj, String propertyName, Object oldValue, Object newValue) {
            }
            @Override
            public void afterAdd(T obj) {
                if (obj.isLoading()) return;
                //removed, since this is when it is added to objCache
                //if (OAThreadLocalDelegate.isLoadingObject()) return;
                                
                // new object is created
                if (isUsed((T) obj)) {
                    callOnTrigger(obj);
                }
            }
            @Override
            public void afterAdd(Hub<T> hub, T obj) {
            }
            @Override
            public void afterRemove(Hub<T> hub, T obj) {
            }
            @Override
            public void afterLoad(T obj) {
                afterAdd(obj);
            }
        };        
        OAObjectCacheDelegate.addListener(clazz, cacheListener);
        refresh();
    }

    public OAObjectCacheTrigger(Hub<T> hub, OAFilter<T> filter, String ... dependentPropPaths) {
        if (hub == null) throw new RuntimeException("hub can not be null");
        clazz = hub.getObjectClass();
 
        if (dependentPropPaths != null) {
            for (String pp : dependentPropPaths) {
                addDependentProperty(pp);
            }
        }
        
        if (filter != null) addFilter(filter, false);
        if (hub.getSize() == 0) {
            refresh();
        }  // else the hub must have been preselected
    }

    
    /**
     * This needs to be set to true if it is only created on the server.
     * This is so that changes will be published to the clients, even if initiated on OAClientThread. 
     */
    public void setServerSideOnly(boolean b) {
        bServerSideOnly = b;
    }
    
    /**
     * Add a filter that is used to determine if an object from the cache will be added to hub.
     * This will clear and refresh hub.  
     * @param f filter to add.  By default isUsed() will return false if any of the filters.isUsed() returns false.
     * @see #addFilter(OAFilter, boolean) that has an option for refreshing.
     */
    public void addFilter(OAFilter<T> f) {
        addFilter(f, true); // filter changes what objs are selected, need to refresh
    }

    public void addFilter(OAFilter<T> f, String ... dependentPropPaths) {
        addFilter(f, true);
        if (dependentPropPaths == null) return;
        for (String pp : dependentPropPaths) {
            addDependentProperty(pp);
        }
    }
    
    /**
     * Add a filter that is used to determine if an object from the cache will be added to hub.
     * @param f filter to add.  By default isUsed() will return false if any of the filters.isUsed() returns false.
     * @param bCallRefresh if true, then call refresh.
     */
    public void addFilter(OAFilter<T> f, boolean bCallRefresh) {
        if (f == null) return;
        if (alFilter == null) alFilter = new ArrayList<OAFilter<T>>();
        alFilter.add(f);
        if (bCallRefresh) refresh();
    }

    
    /**
     * Clear hub and check all cached objects to see if they should be added to hub.
     * To be added, isUsedFromObjectCache() and isUsed() must return true.
     */
    public void refresh() {
        // need to check loaded objects 
        OAObjectCacheDelegate.visit(clazz, new OACallback() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean updateObject(Object obj) {
                if (isUsed((T) obj)) {
                    callOnTrigger((T)obj);
                }
                return true;
            }
        });
    }
    
    
    /**
     * add a property to listen to.  If the property changes, then it will be recalculated to determine if it should be 
     * added to hub, or removed from it.
     * This will recheck the object cache to see if any of the existing objects isUsed() is true and should be added to hub.
     * It will not call refresh.
     */
    public void addDependentProperty(final String prop) {
        if (prop == null || prop.length() == 0) return;
        
        dependentPropertyPaths = (String[]) OAArray.add(String.class, dependentPropertyPaths, prop);
        
        // need to recheck in case there was previous changes for the newly added dependentProp that was never checked.  
        setupTrigger();
    }
    
    protected void setupTrigger() {
        if (trigger != null) {
            OATriggerDelegate.removeTrigger(trigger);
        }

        OATriggerListener<T> triggerListener = new OATriggerListener<T>() {
            @Override
            public void onTrigger(final T rootObject, final HubEvent hubEvent, final String propertyPathFromRoot) throws Exception {
                if (rootObject == null) {
                    Hub hubx = hubEvent.getHub();
                    final OAObject masterObject = hubx == null ? null : hubx.getMasterObject();
                    
                    // the reverse property could not be used to get objRoot 
                    // - need see if any of the rootObjs + pp used the changed obj
                    final OAFinder finder = new OAFinder(propertyPathFromRoot) {
                        protected boolean isUsed(OAObject obj) {
                            if (obj == hubEvent.getObject()) return true;
                            if (masterObject == obj) return true;
                            return false;
                        }
                    };
                    finder.setUseOnlyLoadedData(false);

                    OAObjectCacheDelegate.visit(clazz, new OACallback() {
                        @SuppressWarnings("unchecked")
                        @Override
                        public boolean updateObject(Object obj) {
                            if (finder.findFirst((OAObject) obj) == null) return true;
                            
                            if (isUsed((T) obj)) {
                                callOnTrigger((T) obj);
                            }
                            return true;
                        }
                    });
                }
                else {
                    if (isUsed((T) rootObject)) {
                        callOnTrigger((T) rootObject);
                    }
                }
            }
        };
        
        if (name == null) {
            name = "OAObjectCacheTrigger" + (aiUnique.incrementAndGet());
        }
        
        trigger = new OATrigger(name, clazz, triggerListener, dependentPropertyPaths, true, false, false, true);
        OATriggerDelegate.createTrigger(trigger);
    }
    
    
    public void close() {
        if (trigger == null) {
            OATriggerDelegate.removeTrigger(trigger);
            trigger = null;
        }
        if (cacheListener == null) {
            OAObjectCacheDelegate.removeListener(clazz, cacheListener);
            cacheListener = null;
        }
    }
    
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }
    
    /**
     * Called to see if an object should be included in hub.
     * By default, this will return false if no filters have been added, or the result of the filters. 
     */
    @Override
    public boolean isUsed(T obj) {
        if (alFilter != null) {
            for (OAFilter<T> f : alFilter) {
                if (!f.isUsed(obj)) return false;
            }
        }
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
