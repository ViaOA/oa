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
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.sync.OASyncDelegate;

/**
 * Broadcast methods used to keep OAObjects, Hubs in sync with all computers.
 * Note: there is an instance on the server and each client.  The server needs to always
 * try to update, even if the object is no longer in memory, then it will need to get from datasource. 
 * @author vvia
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
            OAObjectPropertyDelegate.removePropertyIfNull((OAObject)obj, hubPropertyName, false);                
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
    
    @Override
    public boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) {
            // store null so that it can be an empty hub if needed (and wont have to get from server)
            OAObjectPropertyDelegate.setProperty(obj, hubPropertyName, null);                
            return false;
        }
        h.deleteAll();
        return true;
    }
    
    @Override
    public boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        
        Hub h = getHub(obj, hubPropertyName);
        if (h == null) {
            OAObjectPropertyDelegate.setProperty(obj, hubPropertyName, null);                
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

    /** remove object from OAObjectCache */
    @Override
    public boolean removeObject(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) return false;
        OAObjectCacheDelegate.removeObject((OAObject) obj);
        return true;
    }

    @Override
    public boolean delete(Class objectClass, OAObjectKey objectKey) {
        OAObject obj = getObject(objectClass, objectKey);
        if (obj == null) return false;
        obj.delete();
        return true;
    }


    private OAObject getObject(Class objectClass, OAObjectKey origKey) {
        OAObject obj = OAObjectCacheDelegate.get(objectClass, origKey);
        if (obj == null) {
            if (OASyncDelegate.isServer()) {
                obj = (OAObject) OADataSource.getObject(objectClass, origKey);
            }
        }
        return obj;
    }
    
    private Hub getHub(OAObject obj, String hubPropertyName) {
        if (!OASyncDelegate.isServer()) {
            if (!OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName)) return null;
        }
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (objx instanceof Hub) return (Hub) objx;
        return null;
    }
}
