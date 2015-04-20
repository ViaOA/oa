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

import java.util.Comparator;
import java.util.logging.Logger;
import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.sync.OASyncDelegate;

/**
 * Remote broadcast methods used to keep OAObjects, Hubs in sync with all computers.
 * Note: there is an instance on the server and on each client.  The server needs to always
 * try to update, even if the object is no longer in memory, then it will need to get from datasource. 
 */
public class RemoteSyncImpl implements RemoteSyncInterface {
    private static Logger LOG = Logger.getLogger(RemoteSyncImpl.class.getName());

    @Override
    public boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob) {
        OAObject obj = getObject(objectClass, origKey);
        if (obj == null) return false;
        OAObjectReflectDelegate.setProperty((OAObject)obj, propertyName, newValue, null);
        
        // blob value does not get sent, so clear the property so that a getXxx will retrieve it from server
        if (bIsBlob && newValue == null) {
            ((OAObject)obj).removeProperty(propertyName);
        }
        return true;
    }

    @Override
    public boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object objAdd) {
        OAObject obj = getObject(masterObjectClass, masterObjectKey);
        if (obj == null) return false;

        Hub h = getHub(obj, hubPropertyName);
        if (h == null) {
            OAObjectPropertyDelegate.removePropertyIfNull((OAObject)obj, hubPropertyName, false); // if hub is null (empty), then need to get from server                
            return false;
        }
        h.add(objAdd);
        return true;
    }

    @Override
    public boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object objInsert, int pos) {
        OAObject obj = getObject(masterObjectClass, masterObjectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) {
            OAObjectPropertyDelegate.removePropertyIfNull((OAObject)obj, hubPropertyName, false);                
            return false;
        }
        h.insert(objInsert, pos);
        return true;
    }

    @Override
    public boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassRemove, OAObjectKey objectKeyRemove) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) return false;

        OAObject objectRemove = getObject(objectClassRemove, objectKeyRemove);
        if (objectRemove == null) return false;

        h.remove(objectRemove);
        return true;
    }

    /* moved to RemoteClientImpl, so that it would be ran on the server
    @Override
    public boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) {
            // store null so that it can be an empty hub if needed (and wont have to get from server)
            if (!OASyncDelegate.isServer()) {
                OAObjectPropertyDelegate.setPropertyCAS(obj, hubPropertyName, null, null, true, false);                
            }
            return false;
        }
        h.deleteAll();
        return true;
    }
    */
    
    
    @Override
    public boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) {
            if (!OASyncDelegate.isServer()) {
                OAObjectPropertyDelegate.setProperty(obj, hubPropertyName, null);
            }
            return false;
        }
        h.removeAll();
        return true;
    }
    
    @Override
    public boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  int posFrom, int posTo) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) return false;

        h.move(posFrom, posTo);
        return true;
    }
    
    @Override
    public boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) return false;

        h.sort(propertyPaths, bAscending, comp);
        return true;
    }

    /** this was removed, since caching can cause GC on server
     * and it will then later refetch the object, etc 
    @Override
    public boolean removeObject(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        OAObjectCacheDelegate.removeObject((OAObject) obj);
        return true;
    }
    */

    @Override
    public boolean delete(Class objectClass, OAObjectKey objectKey) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        obj.delete();
        return true;
    }

    // on the server, if the object is not found in the cache, then it will be loaded by the datasource 
    private OAObject getObject(Class objectClass, OAObjectKey origKey) {
        OAObject obj = OAObjectCacheDelegate.get(objectClass, origKey);
        if (obj == null && OASyncDelegate.isServer()) {
            obj = (OAObject) OADataSource.getObject(objectClass, origKey);
            if (obj != null) {
                // object must have been GCd, use the original guid
                OAObjectDelegate.reassignGuid(obj, origKey);
            }
        }
        return obj;
    }
    
    // on the server, if the Hub is not found in the cache, then it will be loaded by the datasource
    private Hub getHub(OAObject obj, String hubPropertyName) {
        boolean bWasLoaded = OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName);
        if (!bWasLoaded && !OASyncDelegate.isServer()) {
            return null;
        }
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (!(objx instanceof Hub)) return null;

        // loadCachedOwners will have been done by the call to getObject(masterObj)
        return (Hub) objx;
    }

    // 20150420
    @Override
    public void clearHubChanges(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName) {
        OAObject obj = getObject(masterObjectClass, masterObjectKey);
        if (obj == null) return;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) return;

        HubDataDelegate.clearHubChanges(h);
    }
}

