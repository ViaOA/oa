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
 * This is used to listen to OAObjectCache object changes and add new objects to HubFilter.masterHub.
 * 
 * By default, the filter's isUsed(..) is called when an object is added to the cache and when a dependent property
 * has been changed.
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
    private Hub<T> hubTemp;  // if cache needs to be listened to
    private HubListener<T> hubListener;

    private ArrayList<OAFilter<T>> alFilter;

    public OAObjectCacheFilter(Hub<T> hub) {
        this(hub, null);
    }
    
    public OAObjectCacheFilter(Hub<T> hub, OAFilter filter) {
        if (hub == null) throw new RuntimeException("hub can not be null");
        clazz = hub.getObjectClass();
        wrHub = new WeakReference<Hub<T>>(hub);
        
        setupListeners();
        addFilter(filter, false);
        if (hub == null || hub.getSize() == 0) {
            refresh();
        }  // else the hub must have been preselected
    }
    public void addFilter(OAFilter<T> f) {
        addFilter(f, true);
    }
    
    public void addFilter(OAFilter<T> f, boolean bCallRefresh) {
        if (f == null) return;
        if (alFilter == null) alFilter = new ArrayList<OAFilter<T>>();
        alFilter.add(f);
        if (bCallRefresh) refresh();
    }

    public void refresh() {
        final Hub<T> hub = wrHub.get();
        if (hub != null) hub.clear();
        
        if (hubTemp != null) hubTemp.clear();

        // need to check loaded objects 
        OAObjectCacheDelegate.callback(clazz, new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                if (hub == null || !hub.contains(obj)) {
                    if (isUsedFromObjectCache((T) obj)) {
                        if (hubTemp != null) hubTemp.add((T) obj);
                        if (isUsed((T) obj)) {
                            if (hub != null) hub.add((T) obj);
                        }
                    }
                }
                return true;
            }
        });
    }
    
    /**
     * add a property to listen to.
     */
    public void addDependentProperty(String prop) {
        addDependentProperty(prop, true);
    }
    public void addDependentProperty(String prop, boolean bCallRefresh) {
        if (prop == null || prop.length() == 0) return;
        
        dependentPropertyNames = (String[]) OAArray.add(String.class, dependentPropertyNames, prop);
        if (calcDependentPropertyName != null || prop.indexOf(".") >= 0) {
            if (calcDependentPropertyName == null) {
                calcDependentPropertyName = "OAObjectTrigger" + (aiUnique.incrementAndGet());
                if (hubTemp == null) {
                    hubTemp = new Hub(clazz);
                }
            }
        }
        setupListeners();
        if (bCallRefresh) refresh();
    }
    
    public void close() {
        if (hlObjectCache != null) {
            OAObjectCacheDelegate.removeListener(clazz, hlObjectCache);
            hlObjectCache = null;
        }
        if (hubListener != null) {
            hubTemp.removeHubListener(hubListener);
            hubListener = null;
        }
    }
    
    
    
    private void setupListeners() {
        if (hlObjectCache != null) {
            OAObjectCacheDelegate.removeListener(clazz, hlObjectCache);
            hlObjectCache = null;
        }
        if (hubListener != null) {
            hubTemp.removeHubListener(hubListener);
            hubListener = null;
        }

        
        hlObjectCache = new OAObjectCacheListener<T>() {
            @Override 
            public void afterPropertyChange(T obj, String propName, Object oldValue, Object newValue) {
                Hub<T> hub = wrHub.get();
                if (hub == null) return;
                if (propName == null) return;
                
                if (calcDependentPropertyName != null) {
                    if (!calcDependentPropertyName.equalsIgnoreCase(propName)) {
                        return;
                    }
                }
                else {
                    if (dependentPropertyNames == null) return;
                    boolean b = false;
                    for (String s : dependentPropertyNames) {
                        if (s.equalsIgnoreCase(propName)) {
                            b = true;
                        }
                    }
                    if (!b) return;
                }
                
                if (!isUsed(obj)) hub.remove(obj);
                else hub.add(obj);
            }
            
            @Override
            public void afterAdd(T obj) {
                if (hubTemp != null) {
                    if (!hubTemp.contains(obj)) {
                        if (!isUsedFromObjectCache(obj)) hubTemp.add(obj);
                    }
                }

                boolean b = isUsed(obj);
                Hub<T> hub = wrHub.get();
                if (hub != null) {
                    if (b) hub.add(obj);
                    else hub.remove(obj);
                }
            }
        };
        OAObjectCacheDelegate.addListener(clazz, hlObjectCache);

        if (hubTemp != null) {
            // need to put all objects in hubTemp so that it can be listened to
            hubListener = new HubListenerAdapter<T>() {
                @Override
                public void afterPropertyChange(HubEvent<T> e) {
                    String propName = e.getPropertyName();
                    if (propName == null) return;
                    if (!calcDependentPropertyName.equalsIgnoreCase(propName)) return;
                        
                    T obj = e.getObject();
                    boolean b = isUsed(obj);
                    Hub<T> hub = wrHub.get();
                    if (hub != null) {
                        if (b) hub.add(obj);
                        else hub.remove(obj);
                    }
                        
                }
                
            };
            hubTemp.addHubListener(hubListener, calcDependentPropertyName, dependentPropertyNames);
        }
    }
    
    /**
     * Called to see if an object should be included in hub. 
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
     * Called to see if this object needs to used from the ObjectCache.
     * @return true by default, can be overwritten to be selective on which objects are being used from the cache.
     */
    public boolean isUsedFromObjectCache(T obj) {
        return true;
    }
}
