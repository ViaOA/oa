package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.util.OAArray;

/**
 * Creates a method that will be called when an object is added or a property is changed.
 * @author vvia
 */
public class OAObjectTrigger<T extends OAObject> {
    private Class<T> clazz;
    private WeakReference<Hub<T>> wrHub;
    private String[] dependentPropertyNames;
    private String calcDependentPropertyName;
    private Hub<T> hubTemp;
    
    private HubListener<T> hubListener;
    private OAObjectCacheListener<T> cacheListener;
    
    
    private static AtomicInteger aiUnique = new AtomicInteger();

    /**
     */
    public OAObjectTrigger(Hub<T> hub) {
        if (hub == null) throw new RuntimeException("hub can not be null");
        this.clazz = hub.getObjectClass();
        wrHub = new WeakReference(hub);
    }
    
    /**
     * This will set the trigger for all instances of a class.
     */
    public OAObjectTrigger(Class<T> clazz) {
        this.clazz = clazz;
    }
    
    /**
     * add a property to listen to.
     */
    public void addDependentProperty(String prop) {
        if (prop == null || prop.length() == 0) return;
        
        dependentPropertyNames = (String[]) OAArray.add(String.class, dependentPropertyNames, prop);
        if (calcDependentPropertyName != null || prop.indexOf(".") >= 0) {
            if (calcDependentPropertyName == null) {
                calcDependentPropertyName = "OAObjectTrigger" + (aiUnique.incrementAndGet());
                
                if (wrHub == null) {
                    hubTemp = new Hub(clazz);
                    wrHub = new WeakReference(hubTemp);
                }
            }
        }
        setup();
    }

    protected void setup() {
        Hub hub = (wrHub == null) ? null : wrHub.get();
        if (hubListener != null) {
            if (hub != null) hub.removeHubListener(hubListener);
            hubListener = null;
        }
        if (cacheListener != null) {
            OAObjectCacheDelegate.removeListener(clazz, cacheListener);
            cacheListener = null;
        }
        if (hub == null && wrHub != null) return;
        
        if (hub != null) {
            hubListener = new HubListenerAdapter<T>() {
                @Override
                public void afterAdd(HubEvent<T> e) {
                    onTrigger(e.getObject());
                }
                @Override
                public void afterInsert(HubEvent<T> e) {
                    onTrigger(e.getObject());
                }
                @Override
                public void afterPropertyChange(HubEvent<T> e) {
                    if (dependentPropertyNames == null) return;
                    String prop = e.getPropertyName();
                    if (prop == null) return;
                    
                    if (calcDependentPropertyName != null && !calcDependentPropertyName.equalsIgnoreCase(prop)) return;
                    else {
                        boolean b = false;
                        for (String s : dependentPropertyNames) {
                            if (s != null && s.equalsIgnoreCase(prop)) {
                                b = true;
                                break;
                            }
                        }
                        if (!b) return;
                    }
                    onTrigger(e.getObject());
                }
            };
            hub.addHubListener(hubListener);
        }
        
        if (hubTemp != null || hub == null) {
            cacheListener = new OAObjectCacheListener<T>() {
                @Override 
                public void afterPropertyChange(T obj, String propName, Object oldValue, Object newValue) {
                    if (hubTemp != null) return; // hubListener will be used
                    if (propName == null) return;
                    
                    if (dependentPropertyNames == null) return;
                    boolean b = false;
                    for (String s : dependentPropertyNames) {
                        if (s.equalsIgnoreCase(propName)) {
                            b = true;
                        }
                    }
                    if (!b) return;
                    onTrigger(obj);
                }
                
                @Override
                public void afterAdd(T obj) {
                    if (calcDependentPropertyName != null) return;  // hubTemp is used
                    if (!isUsedFromObjectCache(obj)) return;
                    
                    if (hubTemp != null) { // hubListener is being used
                        hubTemp.add(obj);
                    }
                    else {
                        onTrigger(obj);
                    }
                }
            };

            Class c = hub.getObjectClass();
            OAObjectCacheDelegate.addListener(c, cacheListener);
        }
    }
    

    /**
     * When using the objectCache, this will be used to determine if a new object will be used.
     */
    public boolean isUsedFromObjectCache(T obj) {
        return true;
    }

    /**
     * Called when a property is changed or added.
     */
    public void onTrigger(T obj) {
        // custom code here
    }
}
