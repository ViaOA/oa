package com.viaoa.object;

import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import com.viaoa.cs.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

public class OAObjectCSDelegate {
	private static Logger LOG = Logger.getLogger(OAObjectCSDelegate.class.getName());

	/**
     * @return true if the current thread is from the OAClient.getMessage().
     */
    public static boolean isClientThread() {
        OAClient client = OAClient.getClient();
        return (client != null && client.isClientThread());
    }

    /**
    * Used to determine if this JDK is running as an OAServer or OAClient.
    * @return true if this is not a Client, either the Server or Stand alone
    */
    public static boolean isServer() {
		OAClient client = OAClient.getClient();
		return (client == null || client.isServer());
    }

    /**
    * Used to determine if this JDK is running as an OAServer or OAClient.
    * @return true if this is not a Client, either the Server or Stand alone
    */
    public static boolean isWorkstation() {
		OAClient client = OAClient.getClient();
		return (client != null && !client.isServer());
    }

    /**
    * Called by OAObjectDelegate.initialize().  If Object is being created on workstation, then it needs to be cached on server,
    * so that it wont be gc'd on server while it is still being used on client(s).
    */
    protected static void initialize(OAObject oaObj) {
	    if (oaObj == null) return;
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
	       // LOG.finer("object.class="+oaObj.getClass());
		   // CACHE_NOTE: need to have object stored on server in a cache, so that it wont be GCd on server.
	       client.initializeObject(oaObj);
 	    }
    }

    protected static void finalizeObject(OAObject oaObj) {
        if (oaObj == null) return;
        OAClient client = OAClient.getClient();
        if (!client.isServer()) {
            client.finalizeObject(oaObj);
        }
    }
    
    /**
     * If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     */
    public static void addToServerSideCache(OAObject oaObj) {
    	// CACHE_NOTE: this "note" is added to all code that needs to work with the server cache for a client
  	   	if (oaObj == null) return;
        OAClient client = OAClient.getClient();
        if (client != null && !client.isServer()) {
            // LOG.finer("object="+oaObj);
            client.addToServerSideCache(oaObj);
        }
    }

    /**
     * If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     */
    public static void removeFromServerSideCache(OAObject oaObj) {
  	   	if (oaObj == null) return;
        OAClient client = OAClient.getClient();
        if (client != null && !client.isServer()) {
            // LOG.finer("object="+oaObj);
            client.removeFromServerSideCache(oaObj);
        }
    }
    
    
    /** Create a new instance of an object.
	   If OAClient.client exists, this will create the object on the server, where the server datasource can initialize object.
	*/
	protected static Object createNewObject(Class clazz) {
        OAClient client = OAClient.getClient();
        if (client != null && !client.isServer()) {
            // LOG.finer("class="+clazz);
            OAObject obj = (OAObject) client.createNewObject(clazz);
            return obj;
        }
        return null;
	}

    /** Create a new copy of an object.
        If OAClient.client exists, this will create the object on the server.
     */
     protected static OAObject createCopy(OAObject oaObj, String[] excludeProperties) {
         OAClient client = OAClient.getClient();
         if (client != null && !client.isServer()) {
             // LOG.finer("class="+oaObj.getClass());
             OAObject obj = (OAObject) client.createCopy(oaObj, excludeProperties);
             return obj;
         }
         return null;
     }
	
	
    protected static int getServerGuid() {
    	int guid = 0;
        OAClient client = OAClient.getClient();
        if (client != null && !client.isServer()) {
            guid = client.getObjectGuid();
            // LOG.fine("server guid="+guid);
        }
        return guid;
    }

    // returns true if this was saved on server
    protected static void save(OAObject oaObj, int iCascadeRule) {
    	boolean bResult = false;
        OAClient client = OAClient.getClient();
        if (client != null && !client.isServer()) {
            LOG.fine("object="+oaObj);
            bResult = true;
            OAObjectMessage msg = new OAObjectMessage();
            msg.setType(msg.SAVE);
            msg.setObjectClass(oaObj.getClass());
            msg.setObjectKey(OAObjectKeyDelegate.getKey(oaObj));
//todo: qqqqqqqq need to add Cascade type to msg to then run on server            
            client.sendMessage(msg);
            if (msg.getNewValue() != null) throw new RuntimeException(""+msg.getNewValue());
        }
    }

    /**
	    Same as delete, without first calling canDelete.
	    @return true if delete was sent to server, false if it was not sent.
	    @see #delete()
	*/
	protected static boolean delete(OAObject oaObj) {
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return false;
        if (OAObjectInfoDelegate.getOAObjectInfo(oaObj).getLocalOnly()) return false; 

        LOG.fine("object="+oaObj);
        OAObjectMessage msg = new OAObjectMessage();
        msg.setType(OAObjectMessage.DELETE);
        msg.setObjectClass(oaObj.getClass());
        msg.setObjectKey(OAObjectKeyDelegate.getKey(oaObj) );
        client.sendMessage(msg);
        if (msg.getNewValue() != null) throw new RuntimeException((String) msg.getNewValue());
        return true;
	}

    /**
     * Remove object from each workstation.
     */
    protected static void removeObject(OAObject oaObj) {
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return;
        if (OAObjectInfoDelegate.getOAObjectInfo(oaObj).getLocalOnly()) return; 
        LOG.fine("object="+oaObj);
        // this will "tell" the HubController on other clients to remove this object
        OAObjectMessage msg = new OAObjectMessage();
        msg.setType(msg.REMOVEOBJECT);
        msg.setObjectClass(oaObj.getClass());
        msg.setObjectKey( OAObjectKeyDelegate.getKey(oaObj));
        OAClient.getClient().sendMessage(msg);
    }    
    

	protected static Object getServerObject(Class clazz, OAObjectKey key) {
		Object obj = null;
        OAClient client = OAClient.getClient();
        if (client != null && !client.isServer()) {
            LOG.fine("class="+clazz+", key="+key);
            obj = client.getServerObject(clazz, key);
        }
        return obj;
	}    
	
	// used by OAObjectReflectDelegate.getReferenceObject()
//qqqqqqqqqq make this protected	
	public static Object getServerReferenceObject(OAClient client, OAObject oaObj, String linkPropertyName) {
        LOG.fine("object="+oaObj+", linkProperyName="+linkPropertyName);
		Object obj = null;
        if (client != null && !client.isServer()) {
            obj = client.getDetailObject(oaObj, linkPropertyName);
        }
        else {
            LOG.warning("This should only be called from workstations, not server. Object="+oaObj+", linkPropertyName="+linkPropertyName);
        }
        return obj;
	}    

    protected static byte[] getServerReferenceBlob(OAClient client, OAObject oaObj, String linkPropertyName) {
        LOG.fine("object="+oaObj+", linkProperyName="+linkPropertyName);
        Object obj = null;
        if (client != null && !client.isServer()) {
            obj = client.getDetailObject(oaObj, linkPropertyName);
        }
        else {
            LOG.warning("This should only be called from workstations, not server. Object="+oaObj+", linkPropertyName="+linkPropertyName);
        }
        if (obj instanceof byte[]) return (byte[]) obj;
        return null;
    }    
	
	
	
	
	// used by OAObjectReflectDelegate.getReferenceHub()
	protected static Hub getServerReferenceHub(OAClient client, OAObject oaObj, String linkPropertyName) {
        LOG.fine("object="+oaObj+", linkProperyName="+linkPropertyName);
    	Hub hub = null;
        if (client != null && !client.isServer()) {
            // get hub from server
            hub = client.getDetailHub(oaObj, linkPropertyName);
            if (hub == null && client.isConnected()) {
                // server has to have the object
                throw new RuntimeException("OAObject.getDetail(\""+linkPropertyName+"\") not found on server for "+oaObj.getClass().getName());
            }
        }
        else {
            LOG.warning("This should only be called from workstations, not server. Object="+oaObj+", linkPropertyName="+linkPropertyName);
        }
		return hub;
	}
	
	// used by OAObjectReflectDelegate.getReferenceHub() to have all data loaded on server.
	protected static boolean loadReferenceHubDataOnServer(Hub hub) {
        boolean bResult;
		OAClient client = OAClient.getClient();
        if (client != null && client.isServer()) {
            LOG.finest("hub="+hub);
            bResult = true;
            // load all data without sending messages
            // even though Hub.writeObject does this, this data could be used on server application
        	try {
        		OAThreadLocalDelegate.setSuppressCSMessages(true);
	            hub.loadAllData();
        	}
        	finally {
        		OAThreadLocalDelegate.setSuppressCSMessages(false);        	
        	}
        }
        else bResult = false;
        return bResult;
	}

	protected static void lock(Object object, Object refObject, Object miscObject) {
        LOG.fine("object="+object);
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
	        client.lock(object, miscObject);
	    }		
	}
	
	protected static int getLockWaitCount(Object object) {
        LOG.fine("object="+object);
		int bResult = -1;
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
//qqqqqqqqq TODO: create this on OACLient	    	
//	       bResult = client.getLockWaitCount(object);
	    }		
	    return bResult;
	}

	protected static void unlock(Object object) {
        LOG.fine("object="+object);
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
	        client.unlock(object);
	    }		
	}

	protected static boolean isLocked(Object object) {
		boolean bResult = false;
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
	        bResult = client.isLocked(object);
	    }		
        LOG.fine("object="+object+", result="+bResult);
	    return bResult;
	}
	
	protected static OALock getLock(Object object) {
        LOG.fine("object="+object);
		OALock bResult = null;
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
	        bResult = client.getLock(object);
	    }		
	    return bResult;
	}
	
	protected static Object[] getAllLockedObjects() {
        LOG.fine("called");
	    OAClient client = OAClient.getClient();
	    if (client != null && !client.isServer()) {
	        return client.getAllLockedObjects();
	    }		
	    return null;
	}
	
//qqqqqqqq this needs to be changed to be called beforePropertyChange	
    protected static void fireAfterPropertyChange(OAObject obj, OAObjectKey origKey, String propertyName, Object oldValue, Object newValue) {
        LOG.finer("properyName="+propertyName+", obj="+obj+", newValue="+newValue);
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return;
        
        if (OAThreadLocalDelegate.isSkipFirePropertyChange()) return;
        if (OAThreadLocalDelegate.isSkipObjectInitialize()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        if (oi.getLocalOnly()) return;

        // 20130319 dont send out calc prop changes
        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propertyName);
        if (li != null && li.bCalculated) return;
        
        
        // LOG.finer("object="+obj+", key="+origKey+", prop="+propertyName+", newValue="+newValue+", oldValue="+oldValue);

        OAObjectMessage msg = null;
        msg = new OAObjectMessage();
        msg.setType(msg.PROPERTY_CHANGE);
        msg.setObjectClass(obj.getClass());
        msg.setObjectKey(origKey);  // value of key before property change was ran (and objectKey was updated)
        msg.setProperty(propertyName);
        
        // 20130318 if blob, then set a flag so that the server does not broadcast to all clients
        //     the clients (OAClient.procesPropChange) will recv the msg and know how to handle it.
        //       so that the next time the prop getXxx is called, it will then get it from the server
        boolean bIsBlob = false;
        if (newValue != null && newValue instanceof byte[]) {
            byte[] bs = (byte[]) newValue;
            if (bs.length > 400) {
                OAPropertyInfo pi = OAObjectInfoDelegate.getPropertyInfo(oi, propertyName);
                if (pi.isBlob()) {
                    bIsBlob = true;
                }
            }
        }
        msg.setPos(bIsBlob ? 77 : 0);
        msg.setNewValue(newValue);
        client.sendMessage(msg);
	}
	
}






