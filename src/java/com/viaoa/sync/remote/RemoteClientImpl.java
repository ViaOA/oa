package com.viaoa.sync.remote;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.object.OACascade;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.object.OAObjectSaveDelegate;

// see: OAClient

public abstract class RemoteClientImpl implements RemoteClientInterface {
    protected ConcurrentHashMap<Object, Object> hashCache = new ConcurrentHashMap<Object, Object>();
    protected ConcurrentHashMap<Object, Object> hashLock = new ConcurrentHashMap<Object, Object>();
    private ClientGetDetail clientGetDetail = new ClientGetDetail(); 

    @Override
    public OAObject createNewObject(Class clazz) {
        OAObject obj = (OAObject) OAObjectReflectDelegate.createNewObject(clazz);
        hashCache.put(obj, obj);
        return obj;
    }

    @Override
    public OAObject createCopy(Class objectClass, OAObjectKey objectKey, String[] excludeProperties) {
        OAObject obj = (OAObject) OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return null;
        OAObject objx = (OAObject) OAObjectReflectDelegate.createCopy(obj, excludeProperties);
        hashCache.put(obj, obj);
        return objx;
    }

    @Override
    public boolean setCached(Class objectClass, OAObjectKey objectKey, boolean bAddToCache) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;

        if (bAddToCache) {
            hashCache.put(obj, obj);
        }
        else {
            hashCache.remove(obj);
        }
        return true;
    }

    // called by server to save any client cached objects
    public void saveCache(OACascade cascade, int iCascadeRule) {
        for (Map.Entry<Object, Object> entry : hashCache.entrySet()) {
            Object obj = entry.getKey();
            if (obj instanceof OAObject) {
                OAObject oa = (OAObject) obj;
                if (!oa.wasDeleted()) {
                    OAObjectSaveDelegate.save(oa, iCascadeRule, cascade);
                }
            }
        }
    }

    // called by server when client is disconnected, and objects are saved
    public void clearCache() {
        hashCache.clear();
    }

    @Override
    public boolean setLock(Class objectClass, OAObjectKey objectKey, boolean bLock) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        setLock(obj, bLock);
        return true;
    }

    public void setLock(Object obj, boolean bLock) {
        if (bLock) {
            hashLock.put(obj, obj);
        }
        else {
            hashLock.remove(obj);
        }
    }

    // this is used at disconnect
    public void clearLocks() {
        for (Map.Entry<Object, Object> entry : hashLock.entrySet()) {
            Object obj = entry.getKey();
            setLock(obj, false);
        }
    }

    @Override
    public abstract boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey);

    @Override
    public boolean isLockedByThisClient(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        return (hashLock.get(obj) != null);
    }

    @Override
    public Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String property, String[] masterProps, OAObjectKey[] siblingKeys) {
        Object obj = clientGetDetail.getDetail(masterClass, masterObjectKey, property, masterProps, siblingKeys);
        return obj;
    }
    
}



