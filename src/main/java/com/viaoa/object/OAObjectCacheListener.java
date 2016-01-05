package com.viaoa.object;


/**
 * Used by OAObjectCacheDelegate to send out cache events.
 * @author vvia
 */
public interface OAObjectCacheListener<T extends OAObject> {
    public void afterPropertyChange(T obj, String propertyName, Object oldValue, Object newValue);

    public void afterAdd(T obj);
}
