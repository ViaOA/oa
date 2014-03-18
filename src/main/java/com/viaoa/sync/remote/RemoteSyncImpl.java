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

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectPropertyDelegate;
import com.viaoa.object.OAObjectReflectDelegate;

/**
 * Broadcast methods used to keep OAObjets, Hubs in sync with all computers.
 * @author vvia
 */
public class RemoteSyncImpl implements RemoteSyncInterface {
    private static Logger LOG = Logger.getLogger(RemoteSyncImpl.class.getName());

    @Override
    public boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob) {
        OAObject gobj = OAObjectCacheDelegate.get(objectClass, origKey);
        if (gobj == null) {
            return false;  // object not on this system
        }
        OAObjectReflectDelegate.setProperty((OAObject)gobj, propertyName, newValue, null);
        
        // blob value does not get sent, so clear the property so that a getXxx will retrieve it from server
        if (bIsBlob && newValue == null) {
            ((OAObject)gobj).removeProperty(propertyName);
        }
        return true;
    }

    @Override
    public boolean removeObject(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            OAObjectCacheDelegate.removeObject((OAObject) obj);
            bResult = true;
        }
        else {
            bResult = false;
        }
        return bResult;
    }

    protected Hub getHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, boolean bAutoLoad) {
        OAObject obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (obj == null) {
            return null;
        }
        if (!bAutoLoad && !OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName)) return null;
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (objx instanceof Hub) return (Hub) objx;
        return null;
    }
    
    protected Hub getHub(OAObject obj, String hubPropertyName, boolean bAutoLoad) {
        if (!bAutoLoad) {
            if (!OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName)) return null;
        }
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (objx instanceof Hub) return (Hub) objx;
        return null;
    }

    @Override
    public boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, 
            String hubPropertyName, Object obj) {
        OAObject object = OAObjectCacheDelegate.get(masterObjectClass, masterObjectKey);
        if (object == null) {
            return false;
        }

        Hub h = getHub(object, hubPropertyName, false);
        if (h == null) {
            OAObjectPropertyDelegate.removePropertyIfNull((OAObject)object, hubPropertyName, false);                
            return false;
        }
        
        if (HubDataDelegate.getPos(h, object, false, false) < 0 ) {
            h.addElement(obj);
        }
        return true;
    }

    @Override
    public boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  int posFrom, int posTo) {
        OAObject object = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) return false;

        h.move(posFrom, posTo);
        return true;
    }
    
    @Override
    public boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp) {
        OAObject object = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) return false;

        h.sort(propertyPaths, bAscending, comp);
        return true;
    }

   
    @Override
    public boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX) {
        OAObject object = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (object == null) {
            return false;
        }
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) {
            return false;
        }

        OAObject objectx = OAObjectCacheDelegate.get(objectClassX, objectKeyX);
        if (objectx == null) {
            return false;
        }
        h.remove(objectx);
        return true;
    }

    @Override
    public boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj, int pos) {
        OAObject object = OAObjectCacheDelegate.get(masterObjectClass, masterObjectKey);
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) {
            OAObjectPropertyDelegate.removePropertyIfNull((OAObject)object, hubPropertyName, false);                
            return false;
        }
        
        if (HubDataDelegate.getPos(h, object, false, false) < 0 ) {
            h.insert(obj, pos);
        }
        return true;
    }
}
