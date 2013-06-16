package com.viaoa.sync.remote;


import java.util.Comparator;

import com.viaoa.object.OAObjectKey;

public interface RemoteSyncInterface {

    // OAObjectCSDelegate    
    boolean propertyChange(Class objectClass, OAObjectKey objectKey, String propertyName, Object newValue, boolean bIsBlob);    
    boolean removeObject(Class objectClass, OAObjectKey objectKey); // Remove object from each workstation.

    // HubCSDelegate
    boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj);
    
    boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj, int pos);
    
    boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX);   
    
    boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  int posFrom, int posTo);
    boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp);
}


