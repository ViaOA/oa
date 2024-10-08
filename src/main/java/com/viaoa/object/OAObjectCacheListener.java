package com.viaoa.object;

import com.viaoa.hub.Hub;


/**
 * Used by OAObjectCacheDelegate to send out object cache events.
 * @author vvia
 */
public interface OAObjectCacheListener<T extends OAObject> {
    
    /**
     * Called when there is a change to an object.
     */
    public void afterPropertyChange(T obj, String propertyName, Object oldValue, Object newValue);

    /** 
     * called when a new object is added to OAObjectCache, during the object construction. 
     */
    public void afterAdd(T obj);
    
    public void afterAdd(Hub<T> hub, T obj);
    
    public void afterRemove(Hub<T> hub, T obj);
    
    public void afterLoad(T obj);
    
}
