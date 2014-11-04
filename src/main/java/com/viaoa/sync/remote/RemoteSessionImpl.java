/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.sync.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.viaoa.object.OACascade;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.object.OAObjectSaveDelegate;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;
import com.viaoa.sync.model.ClientInfo;

// see: OAClient

public abstract class RemoteSessionImpl implements RemoteSessionInterface {
    protected ConcurrentHashMap<Object, Object> hashCache = new ConcurrentHashMap<Object, Object>();
    protected ConcurrentHashMap<Object, Object> hashLock = new ConcurrentHashMap<Object, Object>();

    @Override
    public void setCached(Class objectClass, OAObjectKey objectKey, boolean bAddToCache) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return;

        if (bAddToCache) {
            hashCache.put(obj, obj);
        }
        else {
            hashCache.remove(obj);
        }
    }
    @Override
    public void setCached(OAObject obj, boolean bAddToCache) {
        if (bAddToCache) {
            hashCache.put(obj, obj);
        }
        else {
            hashCache.remove(obj);
        }
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

    // called by server to save any client cached objects
    /*not needed
    public OAObject findInCache(int guid) {
        for (Map.Entry<Object, Object> entry : hashCache.entrySet()) {
            Object obj = entry.getKey();
            if (obj instanceof OAObject) {
                OAObject oa = (OAObject) obj;
                if (OAObjectDelegate.getGuid(oa) == guid) {
                    return oa;
                }
            }
        }
        return null;
    }
    */
    
    
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
    public OAObject createNewObject(Class clazz) {
        OAObject obj = (OAObject) OAObjectReflectDelegate.createNewObject(clazz);
        setCached(obj, true);
        return obj;
    }
    
    @Override
    public boolean isLockedByThisClient(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        return (hashLock.get(obj) != null);
    }

    @Override
    public abstract boolean isLocked(Class objectClass, OAObjectKey objectKey);

    @Override
    public abstract boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey);
    
    @Override
    public abstract void sendException(String msg, Throwable ex);
    
    @Override
    public void update(ClientInfo ci) {
    }
}
