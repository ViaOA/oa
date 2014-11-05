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
    protected ConcurrentHashMap<OAObject, OAObject> hashCache = new ConcurrentHashMap<OAObject, OAObject>();
    protected ConcurrentHashMap<Integer, OAObject> hashCacheInt = new ConcurrentHashMap<Integer, OAObject>();
    protected ConcurrentHashMap<OAObject, OAObject> hashLock = new ConcurrentHashMap<OAObject, OAObject>();

    @Override
    public void setCached(OAObject obj, boolean bAddToCache) {
        if (bAddToCache) {
            hashCache.put(obj, obj);
            hashCacheInt.put(OAObjectDelegate.getGuid(obj), obj);
        }
        else {
            hashCache.remove(obj);
            hashCacheInt.remove(OAObjectDelegate.getGuid(obj), obj);
        }
    }

    // called by server to save any client cached objects
    public void saveCache(OACascade cascade, int iCascadeRule) {
        for (Map.Entry<OAObject, OAObject> entry : hashCache.entrySet()) {
            OAObject obj = entry.getKey();
            if (!obj.wasDeleted()) {
                OAObjectSaveDelegate.save(obj, iCascadeRule, cascade);
            }
        }
    }

    protected OAObject findInCache(int guid) {
        return hashCacheInt.get(new Integer(guid));
    }

    /**
     * GUIDs of the objects removed from oaObject cache on server.
     */
    @Override
    public abstract void removeGuids(int[] guids);
    
    
    // called by server when client is disconnected
    public void clearCache() {
        hashCache.clear();
    }

    @Override
    public boolean setLock(Class objectClass, OAObjectKey objectKey, boolean bLock) {
        OAObject obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        setLock(obj, bLock);
        return true;
    }

    public void setLock(OAObject obj, boolean bLock) {
        if (bLock) {
            hashLock.put(obj, obj);
        }
        else {
            hashLock.remove(obj);
        }
    }

    // this is used at disconnect
    public void clearLocks() {
        for (Map.Entry<OAObject, OAObject> entry : hashLock.entrySet()) {
            OAObject obj = entry.getKey();
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
