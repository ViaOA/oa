/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.sync.remote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
    private static Logger LOG = Logger.getLogger(RemoteSessionImpl.class.getName());
    protected ConcurrentHashMap<Integer, OAObject> hashCache = new ConcurrentHashMap<Integer, OAObject>();
    protected ConcurrentHashMap<OAObject, OAObject> hashLock = new ConcurrentHashMap<OAObject, OAObject>();
    protected int sessionId;
    
    public RemoteSessionImpl(int sessionId) {
        this.sessionId = sessionId;
    }
    
    @Override
    public void addToCache(OAObject obj) {
        int guid = OAObjectDelegate.getGuid(obj);
        hashCache.put(guid, obj);
        int x = hashCache.size();
        //LOG.fine("sessionId="+sessionId+", cache size="+x+", obj="+obj);
        if (x % 100 == 0) {
            LOG.fine("sessionId="+sessionId+", cache size="+x+", obj="+obj);
        }
    }
    @Override
    public void removeFromCache(int guid) {
        hashCache.remove(guid);
        int x = hashCache.size();
        //LOG.fine("sessionId="+sessionId+", cache size="+x+", guid="+guid);
        if (x>0 && x % 100 == 0) {
            LOG.fine("sessionId="+sessionId+", cache size="+x);
        }
    }

    
    // called by server to save any client cached objects
    public void saveCache(OACascade cascade, int iCascadeRule) {
        LOG.fine("sessionId="+sessionId+", cache size="+hashCache.size());
        for (Map.Entry<Integer, OAObject> entry : hashCache.entrySet()) {
            OAObject obj = entry.getValue();
            if (!obj.wasDeleted()) {
                OAObjectSaveDelegate.save(obj, iCascadeRule, cascade);
            }
        }
    }

    /**
     * GUIDs of the objects removed from oaObject cache on server.
     */
    @Override
    public abstract void removeGuids(int[] guids);
    
    
    // called by server when client is disconnected
    public void clearCache() {
        hashCache.clear();
        LOG.fine("sessionId="+sessionId+", cache size="+hashCache.size());
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
        LOG.fine("sessionId="+sessionId+", cache size="+hashLock.size()+", obj="+obj+", locked="+bLock);
    }

    // this is used at disconnect
    public void clearLocks() {
        for (Map.Entry<OAObject, OAObject> entry : hashLock.entrySet()) {
            OAObject obj = entry.getKey();
            setLock(obj, false);
        }
        LOG.fine("sessionId="+sessionId+", cache size="+hashLock.size());
    }

    @Override
    public OAObject createNewObject(Class clazz) {
        OAObject obj = (OAObject) OAObjectReflectDelegate.createNewObject(clazz);
        LOG.fine("sessionId="+sessionId+", obj="+obj);
        addToCache(obj);
        return obj;
    }
    
    @Override
    public boolean isLockedByThisClient(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        return (hashLock.get(obj) != null);
    }

    @Override
    public String ping(String msg) {
        return msg;
    }
    @Override
    public void ping2(String msg) {
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
