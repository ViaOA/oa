package com.viaoa.sync;

import java.util.Comparator;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;

public interface OASyncInterface {

    // OAObjectCSDelegate    
    void propertyChange(Class objectClass, OAObjectKey objectKey, String propertyName, Object newValue, boolean bIsBlob);    
    boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule);
    boolean delete(Class objectClass, OAObjectKey objectKey);
    boolean removeObject(Class objectClass, OAObjectKey objectKey); // Remove object from each workstation.

    // HubCSDelegate
    boolean addToHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  boolean bInsert, int pos);
    boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  int posFrom, int posTo);
    boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp);
    boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName);    
    boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX);   

    
    // OAClient
    Object createNewObject(Class clazz);
    OAObject createCopy(OAObject oaObj, String[] excludeProperties);
    Object getDetail(OAObject masterObject, String propertyName);
    
    
    // create new interface for this
    //Object datasource(int command, Object[] objects);
    
    
    //dont use    
    // Object getServerObject(Class clazz, OAObjectKey key);
}


