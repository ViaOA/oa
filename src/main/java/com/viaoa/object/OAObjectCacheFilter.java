package com.viaoa.object;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubFilter;
import com.viaoa.util.OAFilter;

/**
 * This is used to listen to OAObjectCacheDelegate object changes and update add new objects to HubFilter.masterHub.
 * 
 * By default, the filter's isUsed(..) is called to determine if the object should be added.  
 * @author vvia
 */
public class OAObjectCacheFilter<T extends OAObject> implements OAFilter<T> {
    private HubFilter<T> filter;
    private Hub<T> hub;
    private OAObjectCacheListener<T> hlObjectCache;

    public OAObjectCacheFilter(HubFilter<T> filter) {
        if (filter == null) throw new RuntimeException("filter can not be null");
        this.hub = filter.getMasterHub();
        this.filter = filter;
        setup();
    }

    public void close() {
        if (hub != null && hlObjectCache != null) {
            Class c = hub.getObjectClass();
            OAObjectCacheDelegate.removeListener(c, hlObjectCache);
            hlObjectCache = null;
        }
    }
    
    private void setup() {
        if (hub == null || filter == null) return;
        
        hlObjectCache = new OAObjectCacheListener<T>() {
            /** HubListener interface method, used to update filter. */
            
            @Override 
            public void afterPropertyChange(T obj, String propName, Object oldValue, Object newValue) {
                if (hub == null || hub.contains(obj)) return;
                if (propName == null) return;
                
                String calcDependentPropertyName = filter.getCalcPropertyName();
                if (calcDependentPropertyName != null) {
                    if (!calcDependentPropertyName.equalsIgnoreCase(propName)) {
                        return;
                    }
                }
                else {
                    String[] dependentPropertyNames = filter.getDependentPropertyNames();
                    if (dependentPropertyNames == null) return;
                    boolean b = false;
                    for (String s : dependentPropertyNames) {
                        if (s.equalsIgnoreCase(propName)) {
                            b = true;
                        }
                    }
                    if (!b) return;
                }
                
                if (!isUsed(obj)) return;
                hub.add(obj);
            }
            
            @Override
            public void afterAdd(T obj) {
                if (hub == null || hub.contains(obj)) return;
                
                if (!isUsed(obj)) return;
                hub.add(obj);
            }
        };

        Class c = hub.getObjectClass();
        OAObjectCacheDelegate.addListener(c, hlObjectCache);
    }
    
    /**
     * Only called when a new object is added to OAObjectCache and it is not in Hub.
     * By default, this will call the filter.isUsed(..) to determine if this object should be added 
     * to the HubFilter.masterHub.  And also check to see if there is a hubFilter.calcDependentPropertyPath used.
     */
    @Override
    public boolean isUsed(T obj) {
        boolean bResult = false;
        if (filter != null) {
            bResult = filter.isUsed(obj);
            
            if (!bResult) {  // might need to listen to a propPath
                String calcDependentPropertyName = filter.getCalcPropertyName();
                if (calcDependentPropertyName != null) {
                    bResult = true;
                }                
            }
        }
        return bResult;
    }
}
