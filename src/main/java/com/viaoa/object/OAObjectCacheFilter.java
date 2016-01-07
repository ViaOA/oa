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
 * This is used to listen to the OAObjectCache for objects that match filter criteria add new objects to a Hub.
 * 
 * @author vvia
 */
public class OAObjectCacheFilter<T extends OAObject> implements OAFilter<T> {
    private Class<T> clazz;
    private WeakReference<Hub<T>> wrHub;
    private String[] dependentPropertyNames;
    private String calcDependentPropertyName;
    private static AtomicInteger aiUnique = new AtomicInteger();

    private OAObjectCacheListener<T> hlObjectCache;
    private HubListener<T> hlTemp;
    private Hub<T> hubTemp;  // if cache needs to be listened to

    private ArrayList<OAFilter<T>> alFilter;

    public OAObjectCacheFilter(Hub<T> hub) {
        this(hub, null);
    }
    
    /**
     * Create new cache filter.  Cached objects that are true for isUsedFromObjectCache & isUsed will be added to hub.
     * @param hub is size is equal to 0, then refresh will be called.  Otherwise refresh will not be called.
     * @param filter
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
    
    /**
     * Add a filter that is used to determine if an object from the cache will be added to hub.
     * This will clear and refresh hub.  
     * @param f filter to add.  By default isUsed() will return false if any of the filters.isUsed() returns false.
     * @see #addFilter(OAFilter, boolean) to use option to refresh hub or not.
     */
    public void addFilter(OAFilter<T> f) {
        addFilter(f, true); // filter changes what objs are selected, need to refresh
    }
    
    /**
     * Add a filter that is used to determine if an object from the cache will be added to hub.
     * @param f filter to add.  By default isUsed() will return false if any of the filters.isUsed() returns false.
     * @param bCallRefresh if true, then clear and refresh hub.
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
        hub.clear();

        try {
            hub.setLoading(true);
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
    
    
    private void setupCacheListener() {
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
        };
        OAObjectCacheDelegate.addListener(clazz, hlObjectCache);
    }
    private void setupTempHubListener() {
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
}
