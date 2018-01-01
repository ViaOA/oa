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
package com.viaoa.object;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteSessionInterface;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.ds.OASelect;
import com.viaoa.hub.*;

public class OAObjectCSDelegate {
	private static Logger LOG = Logger.getLogger(OAObjectCSDelegate.class.getName());

    /**
     * Objects that have been added ont the server.session so that it wont be GCd on the server.
     */
	private static final ConcurrentHashMap<Integer, Integer> hashServerSideCache = new ConcurrentHashMap<Integer, Integer>(31, .75f);

    /**
     * Objects that have been created on the client and have not be sent to the server.
     */
    private static final ConcurrentHashMap<Integer, Integer> hashNewObjectCache = new ConcurrentHashMap<Integer, Integer>(31, .75f);
    
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
    public static boolean isServer(OAObject obj) {
        Class c;
        if (obj == null) c = Object.class;
        else c = obj.getClass();
		return OASyncDelegate.isServer(c);
    }

    /**
    * Used to determine if this JDK is running as an OAServer or OAClient.
    * @return true if this is not a Client, either the Server or Stand alone
    */
    public static boolean isWorkstation(OAObject obj) {
        Class c;
        if (obj == null) c = Object.class;
        else c = obj.getClass();
        return !OASyncDelegate.isServer(c);
    }

    /**
    * Called by OAObjectDelegate.initialize(). 
    * If Object is being created on workstation, then it needs to be flagged that it is only on the client.
    */
    protected static void initialize(OAObject oaObj) {
	    if (oaObj == null) return;
        addToNewObjectCache(oaObj);
    }

    public static boolean isInServerSideCache(OAObject oaObj) {
        if (oaObj == null) return false;
        int guid = oaObj.getObjectKey().getGuid();
        return hashServerSideCache.contains(guid);
    }
    
    /**
     * called when an object has been removed from a client.
     * Need to remove on server side session
     */
    protected static void objectRemovedFromCache(OAObject obj, int guid) {
        if (guid < 0) return;
        
        Class c;
        if (obj == null) c = Object.class;
        else c = obj.getClass();
        
        hashServerSideCache.remove(guid);
        OASyncClient sc = OASyncDelegate.getSyncClient(c);
        if (sc != null) {
            if (guid > 0) sc.objectRemoved(guid);
        }
        hashNewObjectCache.remove(guid);
    }
    
    /**
     * If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     */
    public static void addToServerSideCache(OAObject oaObj) {
        addToServerSideCache(oaObj, true);
    }
    public static void addToServerSideCache(OAObject oaObj, boolean bSendToServer) {
        // CACHE_NOTE: this "note" is added to all code that needs to work with the server cache for a client
        if (oaObj == null) return;
        
        Class c = oaObj.getClass();
        if (!OASyncDelegate.isClient(c)) return;
        int guid = oaObj.getObjectKey().getGuid();
        if (guid < 0 || hashServerSideCache.contains(guid)) return;
    
        if (bSendToServer) {
            RemoteSessionInterface ri = OASyncDelegate.getRemoteSession(c);
            if (ri != null) {
                ri.addToCache(oaObj);
            }
        }
        hashServerSideCache.put(guid, guid);
    }

    /**
     * If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     */
    public static void removeFromServerSideCache(OAObject oaObj) {
        if (oaObj == null) return;

        Class c;
        if (oaObj == null) c = Object.class;
        else c = oaObj.getClass();
        
        if (!OASyncDelegate.isClient(c)) return;
        if (hashServerSideCache.size() == 0) return;
        int guid = oaObj.getObjectKey().getGuid();
        if (hashServerSideCache.remove(guid) != null) {
            RemoteSessionInterface ri = OASyncDelegate.getRemoteSession(c);
            if (ri != null) {
                ri.removeFromCache(oaObj.getObjectKey().getGuid());
            }
        }
    }
    
    
    /** Create a new instance of an object.
	   If OAClient.client exists, this will create the object on the server, where the server datasource can initialize object.
	*/
	protected static Object createNewObject(Class clazz) {
        if (clazz == null) return null;
        RemoteSessionInterface ri = OASyncDelegate.getRemoteSession(clazz);
        if (ri != null) {
            return ri.createNewObject(clazz);
        }
        return null;
	}

	
	private static final AtomicInteger aiNewObjectCacheSize = new AtomicInteger();
    /**
     * Objects that are so far only on this computer, and have not been sent to other computers (client or server).
     * This is called during initialization.
     * Once they are serialized (oaobject.writeObject), then it will be removed.
     */
    protected static void addToNewObjectCache(OAObject oaObj) {
        if (oaObj == null) return;
        int guid = oaObj.getObjectKey().getGuid();
        if (hashNewObjectCache.put(guid, guid) == null) {
            aiNewObjectCacheSize.incrementAndGet();
        }
    }
    protected static boolean removeFromNewObjectCache(OAObject oaObj) {
        if (oaObj == null) return false;
        if (hashNewObjectCache.size() == 0) return false;
        int guid = oaObj.getObjectKey().getGuid();
        boolean b = (hashNewObjectCache.remove(guid) != null);
        if (b) aiNewObjectCacheSize.decrementAndGet();
        return b;
    }
    public static boolean isInNewObjectCache(OAObject oaObj) {
        if (aiNewObjectCacheSize.get() == 0) return false;
        if (oaObj == null) return false;
        int guid = oaObj.getObjectKey().getGuid();
        return hashNewObjectCache.containsKey(guid);
    }
	
	
    /** Create a new copy of an object.
        If OAClient.client exists, this will create the object on the server.
     */
     protected static OAObject createCopy(OAObject oaObj, String[] excludeProperties) {
         if (oaObj == null) return null;
         RemoteClientInterface ri = OASyncDelegate.getRemoteClient(oaObj.getClass());
         if (ri != null) {
             OAObject obj = ri.createCopy(oaObj.getClass(), oaObj.getObjectKey(), excludeProperties);;
             addToServerSideCache(oaObj, true);
             return obj; 
         }
         return null;
     }
	
     protected static int getServerGuid(OAObject obj) {
         Class c;
         if (obj == null) c = Object.class;
         else c = obj.getClass();
         return getServerGuid(c);
     }
     protected static int getServerGuid(Class clazz) {
         if (clazz == null) clazz = Object.class;
         int guid = OASyncDelegate.getObjectGuid(clazz);
         return guid;
    }

    // returns true if this was saved on server
    protected static boolean save(OAObject oaObj, int iCascadeRule) {
        if (oaObj == null) return false;
        RemoteServerInterface rs = OASyncDelegate.getRemoteServer(oaObj.getClass());
        if (rs != null) {
            return rs.save(oaObj.getClass(), oaObj.getObjectKey(), iCascadeRule);
        }
        return false;
    }

    /**
     * 20150815 returns true if this should be deleted on this computer, false if it is done on the server. 
    */
    protected static boolean delete(OAObject obj) {
        if (obj == null) return false;
        if (OASyncDelegate.isServer(obj.getClass())) return true;  // invoke on the server
        LOG.finer("obj="+obj);
        
        if (!OARemoteThreadDelegate.shouldSendMessages()) return true;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return true;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj.getClass());
        if (oi.getLocalOnly()) return true; 
        
        RemoteClientInterface rs = OASyncDelegate.getRemoteClient(obj.getClass());
        if (rs == null) return true;
        
        rs.delete(obj.getClass(), obj.getObjectKey());
        return false;
    }
    

	protected static OAObject getServerObject(Class clazz, OAObjectKey key) {
	    if (clazz == null || key == null) return null;
        RemoteServerInterface rs = OASyncDelegate.getRemoteServer(clazz);
        OAObject result;
        if (rs != null) {
            result = rs.getObject(clazz, key);
        }       
        else result = null;
        return result;
	}    
	
    protected static byte[] getServerReferenceBlob(OAObject oaObj, String linkPropertyName) {
        LOG.finer("object="+oaObj+", linkProperyName="+linkPropertyName);
        if (oaObj == null || linkPropertyName == null) return null;
        Object obj = null;
        
        OASyncClient sc = OASyncDelegate.getSyncClient(oaObj.getClass());
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
        LOG.finer("object="+oaObj+", linkProperyName="+linkPropertyName);
        if (oaObj == null || linkPropertyName == null) return null;
        Object value = null;
        OASyncClient sc = OASyncDelegate.getSyncClient(oaObj.getClass());
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
        LOG.finer("object="+oaObj+", linkProperyName="+linkPropertyName);
        if (oaObj == null || linkPropertyName == null) return null;
    	Hub hub = null;
        OASyncClient sc = OASyncDelegate.getSyncClient(oaObj.getClass());
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
	protected static boolean loadReferenceHubDataOnServer(Hub thisHub, OASelect select) {
        if (thisHub == null) return false;
        boolean bResult;
        if (OASyncDelegate.isServer(thisHub)) {
            //LOG.finest("hub="+hub);

            // 20140328 performance improvement 
            if (thisHub.getSelect() == null && select == null) return true;
            
            
            bResult = true;
            // load all data without sending messages
            // even though Hub.writeObject does this, this data could be used on server application
        	try {
        		OAThreadLocalDelegate.setSuppressCSMessages(true);
        		HubSelectDelegate.loadAllData(thisHub, select);
        	}
        	finally {
        		OAThreadLocalDelegate.setSuppressCSMessages(false);        	
        	}
        }
        else bResult = false;
        return bResult;
	}
	
	
    protected static void fireBeforePropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
        if (obj == null) return;
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync(obj.getClass());
        if (rs == null) return;
        
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
        if (OAThreadLocalDelegate.isLoading()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        if (oi.getLocalOnly()) return;

        // LOG.finer("properyName="+propertyName+", obj="+obj+", newValue="+newValue);
        
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
        // Important NOTE: dont send, it is now using beforePropertyChange
        if (true || false) return;

        //LOG.finer("properyName="+propertyName+", obj="+obj+", newValue="+newValue);
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
        if (OAThreadLocalDelegate.isLoading()) return;
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
        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync(obj.getClass());
        if (rs != null) {
            rs.propertyChange(obj.getClass(), origKey, propertyName, newValue, bIsBlob);
        }
    }
}

