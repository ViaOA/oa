package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAFilter;

/**
 * Listen to the OAObjectCache for objects that match filter criteria, and add to a Hub.
 * 
 * @author vvia
 */
public class OAObjectCacheFilter<T extends OAObject> implements OAFilter<T> {
    private static final long serialVersionUID = 1L;
    private Class<T> clazz;
    private WeakReference<Hub<T>> wrHub;

    private String name;

    private OAObjectCacheListener cacheListener;    
    
    // list of propPaths to listen for
    private String[] dependentPropertyPaths;
    
    
    
    // used to create a unique calc propName
    private static AtomicInteger aiUnique = new AtomicInteger();  

    private OATrigger trigger;
    
    // list of filters that must return true for the isUsed to return true.
    private ArrayList<OAFilter<T>> alFilter;

    
    /**
     * create an object cache filter, and have hub updated with all objects that match filter(s) and isUsed methods return true.
     */
    public OAObjectCacheFilter(Hub<T> hub) {
        this(hub, null);
    }
    
    /**
     * Create new cache filter.  Cached objects that are true for isUsedFromObjectCache & isUsed will be added to hub.
     * @param hub if size is equal to 0, then refresh will be called.  Otherwise refresh will not be called, since it's
     * assumed that the objects were preselected.
     */
    public OAObjectCacheFilter(Hub<T> hub, OAFilter<T> filter) {
        if (hub == null) throw new RuntimeException("hub can not be null");
        clazz = hub.getObjectClass();
        wrHub = new WeakReference<Hub<T>>(hub);
 
        if (filter != null) addFilter(filter, false);
        
        cacheListener = new OAObjectCacheListener<T>() {
            @Override
            public void afterPropertyChange(T obj, String propertyName, Object oldValue, Object newValue) {
            }
            @Override
            public void afterAdd(T obj) {
                // new object is created
                final Hub<T> hub = wrHub.get();
                if (hub == null) return;
                if (isUsed((T) obj)) {
                    hub.add((T) obj);
                }
            }
            @Override
            public void afterAdd(Hub<T> hub, T obj) {
            }
            @Override
            public void afterRemove(Hub<T> hub, T obj) {
            }
        };        
        OAObjectCacheDelegate.addListener(clazz, cacheListener);
        
        if (hub.getSize() == 0) {
            refresh();
        }  // else the hub must have been preselected
    }

    public OAObjectCacheFilter(Hub<T> hub, OAFilter<T> filter, String ... dependentPropPaths) {
        if (hub == null) throw new RuntimeException("hub can not be null");
        clazz = hub.getObjectClass();
        wrHub = new WeakReference<Hub<T>>(hub);
 
        if (dependentPropPaths != null) {
            for (String pp : dependentPropPaths) {
                addDependentProperty(pp, false);
            }
        }
        
        if (filter != null) addFilter(filter, false);
        if (hub.getSize() == 0) {
            refresh();
        }  // else the hub must have been preselected
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
        final Hub<T> hub = wrHub.get();
        if (hub == null) {
            close();
            return;
        }

        try {
            hub.setLoading(true);
            hub.clear();
            // need to check loaded objects 
            OAObjectCacheDelegate.visit(clazz, new OACallback() {
                @SuppressWarnings("unchecked")
                @Override
                public boolean updateObject(Object obj) {
                    if (isUsed((T) obj)) {
                        hub.add((T) obj);
                    }
                    return true;
                }
            });
        }
        finally {
            hub.setLoading(false);
        }
    }
    
    
    /**
     * add a property to listen to.  If the property changes, then it will be recalculated to determine if it should be 
     * added to hub, or removed from it.
     * This will recheck the object cache to see if any of the existing objects isUsed() is true and should be added to hub.
     * It will not call refresh.
     */
    public void addDependentProperty(final String prop) {
        addDependentProperty(prop, true);
    }
    public void addDependentProperty(final String prop, final boolean bRefresh) {
        if (prop == null || prop.length() == 0) return;
        
        dependentPropertyPaths = (String[]) OAArray.add(String.class, dependentPropertyPaths, prop);
        
        // need to recheck in case there was previous changes for the newly added dependentProp that was never checked.  
        final Hub<T> hub = wrHub.get();
        if (hub == null) {
            close();
            return;
        }
        
        setupTrigger();

        if (!bRefresh) return;
        OAObjectCacheDelegate.visit(clazz, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                if (isUsed((T) obj)) hub.add((T) obj);
                else hub.remove((T) obj);
                return true;
            }
        });
    }
    
    protected void setupTrigger() {
        OATriggerListener<T> triggerListener = new OATriggerListener<T>() {
            @Override
            public void onTrigger(final T rootObject, final HubEvent hubEvent, final String propertyPathFromRoot) throws Exception {
                final Hub<T> hub = wrHub.get();
                if (hub == null) {
                    return;
                }
                
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
                                hub.add((T) obj);
                            }
                            else {
                                hub.remove((T) obj);
                            }
                            return true;
                        }
                    });
                }
                else {
                    if (isUsed((T) rootObject)) hub.add((T) rootObject);
                    else hub.remove((T) rootObject);
                }
            }
        };
        
        if (trigger != null) {
            OATriggerDelegate.removeTrigger(trigger);
        }
        
        if (name == null) {
            name = "OAObjectCacheFilter" + (aiUnique.incrementAndGet());
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
        if (alFilter == null) {
            return false;
        }
        
        for (OAFilter<T> f : alFilter) {
            if (!f.isUsed(obj)) return false;
        }
        return true;
    }
}
