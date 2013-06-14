package com.viaoa.sync;

import java.util.Comparator;

import com.viaoa.cs.OAObjectMessage;
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
public class OASync implements OASyncInterface {

    @Override
    public void propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob) {
        Object gobj = OAObjectCacheDelegate.get(objectClass, origKey);
        if (gobj == null) {
            return;  // object not on this system
        }
        OAObjectReflectDelegate.setProperty((OAObject)gobj, propertyName, newValue, null);
        
        // blob value does not get sent, so clear the property so that a getXxx will retrieve it from server
        if (bIsBlob && newValue == null) {
            ((OAObject)gobj).removeProperty(propertyName);
        }
    }

    @Override
    public boolean removeObject(Class objectClass, OAObjectKey objectKey) {
        Object obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            OAObjectCacheDelegate.removeObject((OAObject) obj);
            bResult = true;
        }
        else bResult = false;
        return bResult;
    }

    protected Hub getHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, boolean bAutoLoad) {
        OAObject obj = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (!bAutoLoad && !OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName)) return null;
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (objx instanceof Hub) return (Hub) objx;
        return null;
    }
    
    protected Hub getHub(OAObject obj, String hubPropertyName, boolean bAutoLoad) {
        if (!bAutoLoad && !OAObjectReflectDelegate.isReferenceHubLoaded(obj, hubPropertyName)) return null;
        Object objx =  OAObjectReflectDelegate.getProperty(obj, hubPropertyName);
        if (objx instanceof Hub) return (Hub) objx;
        return null;
    }
    
    
    @Override
    public boolean addToHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName,  boolean bInsert, int pos) {
        OAObject object = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) {
            OAObjectPropertyDelegate.removeProperty((OAObject)object, msg.property, false);                
            return false;
        }
        
        if (HubDataDelegate.getPos(h, object, false, false) < 0 ) {
            if (bInsert) {
                h.insert(object, pos);
            }
            else h.addElement(object);
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
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) return false;

        OAObject objectx = OAObjectCacheDelegate.get(objectClassX, objectKeyX);
        h.remove(objectx);
        return true;
    }

    
    
    
    
//qqqqqqqqqqqqqqqqqqq

//qqqqqqq these should only go to server, and needs to be added to internal cache
//qq put into another object, a remote server class
// should be in a session object, that is created on the server and sent to client at login/connection time    
// these wont be performed usng OARemoteThread, since they need to send events 
    
    @Override
    public boolean deleteAll(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
        OAObject object = OAObjectCacheDelegate.get(objectClass, objectKey);
        if (object == null) return false;
        
        Hub h = getHub(object, hubPropertyName, false);  // 20080625 was true
        if (h == null) return false;

        h.deleteAll();
        return true;
    }

    @Override
    public boolean save(Class objectClass, OAObjectKey objectKey, int iCascadeRule) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            obj.save(iCascadeRule);
            bResult = true;
        }
        else bResult = false;
        return bResult;
    }

    @Override
    public boolean delete(Class objectClass, OAObjectKey objectKey) {
        OAObject obj = OAObjectCacheDelegate.getObject(objectClass, objectKey);
        boolean bResult;
        if (obj != null) {
            obj.delete();
            bResult = true;
        }
        else bResult = false;
        return bResult;
    }
    

    
    
  //qqqqqqq these should only go to server, and needs to be added to internal cache
  //qq put into another object, a remote server class
  // should be in a session object, that is created on the server and sent to client at login/connection time    
    
        @Override
    public Object createNewObject(Class clazz) {
//qqqqqqq need to store in client cache on server until client adds it to Hub  
        return null;
    }

    @Override
    public OAObject createCopy(OAObject oaObj, String[] excludeProperties) {
//qqqqqqq need to store in client cache on server until client adds it to Hub  
        return null;
    }

    @Override
    public Object getDetail(OAObject masterObject, String propertyName) {
// TODO Auto-generated method stub
        return null;
    }

    

    
    
}
