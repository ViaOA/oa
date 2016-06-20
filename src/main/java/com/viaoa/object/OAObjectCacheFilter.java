package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAFilter;

/**
 * Listen to the OAObjectCache for objects that match filter criteria, and add to a Hub.
 * 
 * @author vvia
 */
public class OAObjectCacheFilter<T extends OAObject> implements OAFilter<T> {

    private Class<T> clazz;
    private WeakReference<Hub<T>> wrHub;

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

    
    /**
     * create a object cache filter, and have hub updated with all objects that match filter(s) and isUsed methods return true.
     */
    public OAObjectCacheFilter(Hub<T> hub) {
        this(hub, null);
    }
    
    /**
     * Create new cache filter.  Cached objects that are true for isUsedFromObjectCache & isUsed will be added to hub.
     * @param hub is size is equal to 0, then refresh will be called.  Otherwise refresh will not be called, since it's
     * assumed that the objects were preselected.
     */
    public OAObjectCacheFilter(Hub<T> hub, OAFilter<T> filter) {
        if (hub == null) throw new RuntimeException("hub can not be null");
        clazz = hub.getObjectClass();
        wrHub = new WeakReference<Hub<T>>(hub);
 
        setupCacheListener();
        if (filter != null) addFilter(filter, false);
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
        
        setupCacheListener();
        if (filter != null) addFilter(filter, false);
        if (hub.getSize() == 0) {
            if (dependentPropPaths != null) {
                refresh();
            }
        }
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
        if (hubTemp != null) hubTemp.clear();

        final Hub<T> hub = wrHub.get();
        if (hub == null) {
            close();
            return;
        }

        try {
            hub.setLoading(true);
            hub.clear();
            // need to check loaded objects 
            OAObjectCacheDelegate.callback(clazz, new OACallback() {
                @Override
                public boolean updateObject(Object obj) {
                    if (isUsedFromObjectCache((T) obj)) {
                        if (hubTemp != null) hubTemp.add((T) obj);
                        if (isUsed((T) obj)) {
                            hub.add((T) obj);
                        }
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
                calcDependentPropertyName = "OAObjectCacheFilter" + (aiUnique.incrementAndGet());
                hubTemp = new Hub(clazz);
            }
        }
        
        if (calcDependentPropertyName != null) {
            if (hlTemp == null) setupTempHubListener();
        }
        
        // need to recheck in case there was previous changes for the newly added dependentProp that was never checked.  
        final Hub<T> hub = wrHub.get();
        if (hub == null) {
            close();
            return;
        }
        
        if (!bRefresh) return;
        
        OAObjectCacheDelegate.callback(clazz, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                if (isUsedFromObjectCache((T) obj)) {
                    if (hubTemp != null) hubTemp.add((T) obj);
                    if (isUsed((T) obj)) hub.add((T) obj);
                    else hub.remove((T) obj);
                }
                else {
                    if (hubTemp != null) hubTemp.remove((T) obj);
                    hub.remove((T) obj);
                }
                return true;
            }
        });
    }
    
    
    protected void setupCacheListener() {
        if (hlObjectCache != null) return;
        hlObjectCache = new OAObjectCacheListener<T>() {
            @Override 
            public void afterPropertyChange(T obj, String propName, Object oldValue, Object newValue) {
                if (propName == null) return;

                if (hubTemp != null) return; // hubTemp listener will get propChange for calcPropName
                
                Hub<T> hub = wrHub.get();
                if (hub == null) return;
                
                if (dependentPropertyNames == null) return;
                boolean b = false;
                for (String s : dependentPropertyNames) {
                    if (s.equalsIgnoreCase(propName)) {
                        b = true;
                    }
                }
                if (!b) return;
                
                if (isUsedFromObjectCache(obj) && isUsed(obj)) hub.add(obj);
                else hub.remove(obj);
            }
            
            @Override
            public void afterAdd(T obj) {
                Hub<T> hub = wrHub.get();
                if (hub == null) return;
                
                if (!isUsedFromObjectCache(obj)) return; // it's new so it cant be in hubs yet

                if (hubTemp != null) {
                    hubTemp.add(obj);
                }

                if (isUsed(obj)) {
                    boolean b = OAThreadLocalDelegate.isLoadingObject();
                    try {
                        if (b) OAThreadLocalDelegate.setLoadingObject(false);
                        hub.add(obj);
                    }
                    finally {
                        if (b) OAThreadLocalDelegate.setLoadingObject(true);
                    }
                }
            }

            @Override
            public void afterAdd(Hub<T> hub, T obj) {
            }
            @Override
            public void afterRemove(Hub<T> hub, T obj) {
            }
        };
        OAObjectCacheDelegate.addListener(clazz, hlObjectCache);
    }
    protected void setupTempHubListener() {
        // 20160602
        if (hlTemp != null) {
            hubTemp.removeHubListener(hlTemp);
        }
        //was: if (hlTemp != null) return;
        
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
                if (obj == null) return;
                boolean b = isUsedFromObjectCache(obj); 
                if (!b) hubTemp.remove(obj);
                        
                b = b && isUsed(obj);

                Hub<T> hub = wrHub.get();
                if (hub != null) {
                    if (b) hub.add(obj);
                    else hub.remove(obj);
                }
            }
        };
        hubTemp.addHubListener(hlTemp, calcDependentPropertyName, dependentPropertyNames);
    }

    @Override
    protected void finalize() throws Throwable {
        // 20160602 not needed, since listeners are kept in gc reachable collections
        // close();
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
}
