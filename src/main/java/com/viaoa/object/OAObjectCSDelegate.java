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
package com.viaoa.object;

import java.lang.ref.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.*;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteClientSyncInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.hub.*;
import com.viaoa.util.*;

public class OAObjectCSDelegate {
	private static Logger LOG = Logger.getLogger(OAObjectCSDelegate.class.getName());

	/**
     * @return true if the current thread is from the OAClient.getMessage().
     */
    public static boolean isRemoteThread() {
       return OARemoteThreadDelegate.isRemoteThread(); 
    }

    /**
    * Used to determine if this JDK is running as an OAServer or OAClient.
    * @return true if this is not a Client, either the Server or Stand alone
    */
    public static boolean isServer() {
		return OASyncDelegate.isServer();
    }

    /**
    * Used to determine if this JDK is running as an OAServer or OAClient.
    * @return true if this is not a Client, either the Server or Stand alone
    */
    public static boolean isWorkstation() {
        return !OASyncDelegate.isServer();
    }

    /**
    * Called by OAObjectDelegate.initialize().  If Object is being created on workstation, then it needs to be cached on server,
    * so that it wont be gc'd on server while it is still being used on client(s).
    */
    protected static void initialize(OAObject oaObj) {
	    if (oaObj == null) return;
	    if (OASyncDelegate.isServer()) return;
	    RemoteClientInterface ri = OASyncDelegate.getRemoteClientInterface();
	    if (ri != null) {
            ri.setCached(oaObj, true);
            int guid = oaObj.getObjectKey().getGuid();
            hashServerSideCache.add(guid);
 	    }
    }

    protected static void finalizeObject(OAObject oaObj) {
        if (oaObj == null) return;
        if (OASyncDelegate.isServer()) return;
        if (hashServerSideCache.remove(oaObj.getObjectKey().getGuid())) {
            RemoteClientInterface ri = OASyncDelegate.getRemoteClientInterface();
            if (ri != null) {
                ri.setCached(oaObj, false);
            }
        }
    }
    
    private static HashSet<Integer> hashServerSideCache = new HashSet<Integer>(379, .75f);
    
    /**
     * If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     */
    public static void addToServerSideCache(OAObject oaObj) {
    	// CACHE_NOTE: this "note" is added to all code that needs to work with the server cache for a client
        if (oaObj == null) return;
        if (OASyncDelegate.isSingleUser()) return;
        int guid = oaObj.getObjectKey().getGuid();
        if (hashServerSideCache.contains(guid)) return;
        RemoteClientInterface ri = OASyncDelegate.getRemoteClientInterface();
        if (ri != null) {
            ri.setCached(oaObj, true);
            hashServerSideCache.add(guid);
        }
    }

    /**
     * If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     */
    public static void removeFromServerSideCache(OAObject oaObj) {
        if (oaObj == null) return;
        if (OASyncDelegate.isSingleUser()) return;
        if (hashServerSideCache.remove(oaObj.getObjectKey().getGuid())) {
            RemoteClientInterface ri = OASyncDelegate.getRemoteClientInterface();
            if (ri != null) {
                ri.setCached(oaObj, false);
            }
        }
    }
    
    
    /** Create a new instance of an object.
	   If OAClient.client exists, this will create the object on the server, where the server datasource can initialize object.
	*/
	protected static Object createNewObject(Class clazz) {
        RemoteClientInterface ri = OASyncDelegate.getRemoteClientInterface();
        if (ri != null) {
            return ri.createNewObject(clazz);
        }
        return null;
	}

    /** Create a new copy of an object.
        If OAClient.client exists, this will create the object on the server.
     */
     protected static OAObject createCopy(OAObject oaObj, String[] excludeProperties) {
         if (oaObj == null) return null;
         RemoteClientSyncInterface ri = OASyncDelegate.getRemoteClientSyncInterface();
         if (ri != null) {
             return ri.createCopy(oaObj.getClass(), oaObj.getObjectKey(), excludeProperties);
         }
         return null;
     }
	
     protected static int getServerGuid() {
         int guid = OASyncDelegate.getObjectGuid();
         return guid;
    }

    // returns true if this was saved on server
    protected static boolean save(OAObject oaObj, int iCascadeRule) {
        if (oaObj == null) return false;
        RemoteServerInterface rs = OASyncDelegate.getRemoteServerInterface();
        if (rs != null) {
            return rs.save(oaObj.getClass(), oaObj.getObjectKey(), iCascadeRule);
        }
        return false;
    }

    /**
	    Same as delete, without first calling canDelete.
	    @return true if delete was sent to server, false if it was not sent.
	    @see #delete()
	*/
	protected static boolean delete(OAObject oaObj) {
	    if (oaObj == null) return false;
        RemoteServerInterface rs = OASyncDelegate.getRemoteServerInterface();
        if (rs != null) {
            return rs.delete(oaObj.getClass(), oaObj.getObjectKey());
        }       
	    return false;
	}

    /**
     * Remove object from each workstation.
     */
    protected static boolean removeObject(OAObject oaObj) {
        if (oaObj == null) return false;
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        boolean result;
        if (rs != null) {
            result = rs.removeObject(oaObj.getClass(), oaObj.getObjectKey());
        }       
        else result = false;
        return result;
    }    
    

	protected static OAObject getServerObject(Class clazz, OAObjectKey key) {
        RemoteServerInterface rs = OASyncDelegate.getRemoteServerInterface();
        OAObject result;
        if (rs != null) {
            result = rs.getObject(clazz, key);
        }       
        else result = null;
        return result;
	}    
	
    protected static byte[] getServerReferenceBlob(OAObject oaObj, String linkPropertyName) {
        LOG.fine("object="+oaObj+", linkProperyName="+linkPropertyName);
        Object obj = null;
        
        OASyncClient sc = OASyncDelegate.getSyncClient();
        if (sc != null) {
            obj = sc.getDetail(oaObj, linkPropertyName);
        }
        else {
            LOG.warning("This should only be called from workstations, not server. Object="+oaObj+", linkPropertyName="+linkPropertyName);
        }
        if (obj instanceof byte[]) return (byte[]) obj;
        return null;
    }    
	
    // used by OAObjectReflectDelegate.getReferenceHub()
    protected static Object getServerReference(OAObject oaObj, String linkPropertyName) {
        LOG.fine("object="+oaObj+", linkProperyName="+linkPropertyName);
        Object value = null;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        if (sc != null) {
            value = sc.getDetail(oaObj, linkPropertyName);
        }
        else {
            LOG.warning("This should only be called from workstations, not server. Object="+oaObj+", linkPropertyName="+linkPropertyName);
        }
        return value;
    }

    
	// used by OAObjectReflectDelegate.getReferenceHub()
	protected static Hub getServerReferenceHub(OAObject oaObj, String linkPropertyName) {
        LOG.fine("object="+oaObj+", linkProperyName="+linkPropertyName);
    	Hub hub = null;
        OASyncClient sc = OASyncDelegate.getSyncClient();
        if (sc != null) {
            Object obj = sc.getDetail(oaObj, linkPropertyName);
            if (obj instanceof Hub) hub = (Hub) obj;
            if (hub == null) {
                LOG.warning("OAObject.getDetail(\""+linkPropertyName+"\") not found on server for "+oaObj.getClass().getName());
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
        
        if (OASyncDelegate.isServer()) {
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
	
	
    protected static void fireBeforePropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        if (rs == null) return;
        
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
        if (OAThreadLocalDelegate.isSkipFirePropertyChange()) return;
        if (OAThreadLocalDelegate.isSkipObjectInitialize()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        if (oi.getLocalOnly()) return;

        LOG.finer("properyName="+propertyName+", obj="+obj+", newValue="+newValue);
        
        // 20130319 dont send out calc prop changes
        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propertyName);
        if (li != null && li.bCalculated) return;
        // LOG.finer("object="+obj+", key="+origKey+", prop="+propertyName+", newValue="+newValue+", oldValue="+oldValue);

        
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
        
        OAObjectKey key = obj.getObjectKey();
        rs.propertyChange(obj.getClass(), key, propertyName, newValue, bIsBlob);
	}
	
    protected static void fireAfterPropertyChange(OAObject obj, OAObjectKey origKey, String propertyName, Object oldValue, Object newValue) {
      //qqqqqqqqqqqqqqqqqqqqqqq dont send, it is now using beforePropertyChange
        if (true || false) return; //qqqqqqqqqqqqqqqqqqqqqq

        LOG.finer("properyName="+propertyName+", obj="+obj+", newValue="+newValue);
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
        if (OAThreadLocalDelegate.isSkipFirePropertyChange()) return;
        if (OAThreadLocalDelegate.isSkipObjectInitialize()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        if (oi.getLocalOnly()) return;

        // 20130319 dont send out calc prop changes
        OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, propertyName);
        if (li != null && li.bCalculated) return;
        // LOG.finer("object="+obj+", key="+origKey+", prop="+propertyName+", newValue="+newValue+", oldValue="+oldValue);

        
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
        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        if (rs != null) {
            rs.propertyChange(obj.getClass(), origKey, propertyName, newValue, bIsBlob);
        }
    }
}






