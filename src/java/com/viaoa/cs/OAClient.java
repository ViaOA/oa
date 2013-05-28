/*

2004/02/29 remove EnableSend, since it is not used and is not thread specific

This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.cs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.rmi.*;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.ds.cs.OADataSourceClient;

/**
    Used to communicate from a client application to a server (OAServer), so that all Objects are kept synchronized.
    Works directly with OAObjectCacheDelegate and Hub to to send messages to OAServer.
*/
public class OAClient {
	private static Logger LOG = Logger.getLogger(OAClient.class.getName());
    
    static OAClient oaClient;

    protected String serverName;  // name of server that this client has connected with
    protected int port;
	protected OAObjectServerInterface oaObjectServer;  // rmi object used to send messages


    OAClientMessageHandler clientMessageHandler;
    OAClientMessageReader clientMessageReader;
    public static final String OAServer_BindName = "OAServer";
    
    /* keeps track of which objects (using oaobj.guid) were created locally or for the local instance,
       and will need to be stored on serverside cache whenever the object is not in a Hub,
       so that the object will not be gc'd on the server.
    */
    private HashSet<Integer> hashServerSideCache = new HashSet<Integer>(379, .75f);
    
    private MultiplexerClient multiplexerClient;
    private RemoteMultiplexerClient remoteMultiplexerClient;
    private OAServerInterface oaServer;
    
    
    /**
        Returns OAClient that is connected to OAServer.
    */
    public static OAClient getClient() {
        return oaClient;
    }

    public OAClient(String serverHostName, int port) {
        this.serverName = serverHostName;
        this.port = port;
    }
    
    /**
        Used internally by OAServer to start a client on same VM as server.  
        Note: Server must also have an OAClient running on the server, so that changes made by clients can be made on server. 
    */
    public OAClient(OAServerInterface server) throws Exception {
        LOG.config("creating for OAServer");
        OAObjectServerInterface os = server.createOAObjectServer(getClientInfo());
        setOAObjectServer(os);
        updateClientInfo();
    }
    
    
    public void start() throws Exception {
        getMultiplexerClient().start();
        oaServer = (OAServerInterface) getRemoteMultiplexerClient().lookup(OAServer_BindName);
        OAObjectServerInterface os = (OAObjectServerInterface) oaServer.createOAObjectServer(getClientInfo());
        setOAObjectServer(os);
        updateClientInfo();
    }
    public void updateClientInfo() throws Exception {
        OAClientInfo ci = getClientInfo();
        OAClientInfoDelegate.update(ci);
        if (oaServer != null) { 
            this.clientInfo = oaServer.updateClientInfo(ci); // send to server and have it updated and returned
        }
    }

    public void stop() throws Exception {
        getMultiplexerClient().close();
    }
    public void close() throws Exception {
        getMultiplexerClient().close();
    }
    
    public RemoteMultiplexerClient getRemoteMultiplexerClient() {
        if (remoteMultiplexerClient == null) {
            remoteMultiplexerClient = new RemoteMultiplexerClient(getMultiplexerClient());
        }
        return remoteMultiplexerClient;
    }
    public MultiplexerClient getMultiplexerClient() {
        if (multiplexerClient == null) {
            multiplexerClient = new MultiplexerClient(serverName, port);
        }
        return multiplexerClient;
    }


    
    /**
     * Unique identifier assigned by the server.
     */
    public int getId() {
        if (remoteMultiplexerClient == null) return 0;
        return remoteMultiplexerClient.getMultiplexerClient().getConnectionId();
    }

    /**
        Returns true if connection to OAServer currently connected.
    */
    public boolean isConnected() {
    	boolean result;
    	
        if (oaObjectServer == null) return false;
        try {
            result = remoteMultiplexerClient != null && remoteMultiplexerClient.getMultiplexerClient().isConnected();
        }
        catch (Exception e) {
        	result = false;
        }
    	//LOG.fine("returning "+result);
        return result;
    }


    /**
     * The remote object used for distributed methods from the client to the server.
     */
    protected OAObjectServerInterface getOAObjectServer() {
        return oaObjectServer;
    }
    
    /**
        Called internally to set the OAObjectServer, which is the RMI object used to communicate with
        Server.
    */
    protected void setOAObjectServer(OAObjectServerInterface objServ) {
        LOG.config("internally setting OAObjectServer");
        oaObjectServer = objServ;
        
        LOG.config("creating ClientMessageReader");
        clientMessageReader = new OAClientMessageReader(objServ) {
            public void handleGetMessageException(OAObjectMessage msg, Exception e) {
            	// Note: clientMessageReader will be stopped before this is called 
            	super.handleGetMessageException(msg, e);
            	OAClient.this.handleGetMessageException(msg,e);
            }
        };
        LOG.config("creating ClientMessageHandler");
        clientMessageHandler = new OAClientMessageHandler(getId(), clientMessageReader, objServ) {
        	@Override
        	protected void process(OAObjectMessage msg) {
        		OAClient.this.process(msg);
        	}
        	@Override
        	public void handleSendMessageException(OAObjectMessage msg,Exception e) {
            	// Note: clientMessageHandler will be stopped before this is called 
        		super.handleSendMessageException(msg, e);
        		OAClient.this.handleSendMessageException(msg, e);
        	}
        };

        // after handler and reader have been created
        oaClient = this;

        LOG.config("starting MessageReader");
        clientMessageReader.start();
        LOG.config("starting MessageHandler");
        clientMessageHandler.start();
        
        LOG.config("completed");
    }


    /** returns true if client is running in same VM as OAServer */
    public static boolean isServer() {
        if (oaClient == null) return true;
        return oaClient.remoteMultiplexerClient == null;
    }
    /** returns true if client is running on a workstation, and not as the server. */
    public static boolean isWorkstation() {
        return !isServer();
    }

    /** Returns true if current thread is a member of OAClient thread pool.
        @see #ignoreIsClientThread
    */
    public static boolean isClientThread() {
    	boolean b = (Thread.currentThread() instanceof OAClientThread);
    	//LOG.finer("returning="+b);
        return b;
    }

    /**
        Adds a String "Total Threads: x" to Vector.
        @param vec Vector to add message to.
    */
    public void getInfo(Vector vec) {
    	vec.add("qqqqqqqqqqqq: "); //qqqqqqq
    }

    protected void clearSentMessages(OAObjectMessage msg) {
    	//LOG.fine("called");
    	if (clientMessageHandler != null) clientMessageHandler.clearSentMessages(msg);
    }

int qqq;    
    protected void process(OAObjectMessage msg) {
    	if (Thread.currentThread() instanceof OAClientThread) {
    	    ((OAClientThread) Thread.currentThread()).time = System.currentTimeMillis();
            //LOG.finer("msg="+msg+", thread="+threadClient.getName());
    	}
if ( ((++qqq) % 100) == 0 ) {    	
    // System.out.println(qqq+") "+(new OATime()).toString()+" msg="+msg);
}

    	try {
            switch(msg.type) {
                case OAObjectMessage.PROPERTY_CHANGE:
                    processPropertyChange(msg);
                    break;
                case OAObjectMessage.INSERT:
                case OAObjectMessage.ADD:
                	processAdd(msg);
                    break;
                case OAObjectMessage.REMOVE:
                	processRemove(msg);
                    break;
                case OAObjectMessage.MOVE:
                	processMove(msg);
                    break;
                case OAObjectMessage.REMOVEOBJECT:
                	processRemoveObject(msg);
                    break;
                case OAObjectMessage.SORT:
                	processSort(msg);
                    break;
/*qqqqqq                    
                case OAObjectMessage.ERROR:
                    processError(msg);
                    break;
*/                    
            }
        }
        catch (Exception e) {
        	LOG.log(OALogger.ERROR, "Exception processing msg, msg="+msg, e);
            handleProcessMessageException(msg, e);
        }
    }

    protected void processException(OAObjectMessage msg) throws Exception {
    	handleException(msg.posTo, msg.pos == 1, msg.property, (Throwable) msg.newValue);
    }

    
    
    protected void processPropertyChange(OAObjectMessage msg) throws Exception {
    	Object gobj = OAObjectCacheDelegate.get(msg.objectClass, msg.objectKey);
    	if (gobj == null) {
    	    return;  // object not on this system
    	}
    	OAObjectReflectDelegate.setProperty((OAObject)gobj, msg.property, msg.newValue, null);
    	
        // 20130318 blob value does not get sent, so clear the property so that a getXxx will retrieve it from server
        if (msg.getPos() == 77 && msg.newValue == null) {
            ((OAObject)gobj).removeProperty(msg.property);
        }
    }


    protected void processAdd(OAObjectMessage msg) throws RemoteException {
        Object object = msg.newValue;
        if (object instanceof OAObjectKey) {
        	object = OAObjectCacheDelegate.getObject(msg.getObjectClass(), object);
        	if (object == null) return;
        }
    	
    	Hub h = getHub(object.getClass(), msg, false);  // 20080625 was true
        if (h == null) {
            // 20120827
            if (object instanceof OAObject) {
                OAObjectPropertyDelegate.removeProperty((OAObject)object, msg.property, false);                
                // OAObjectHubDelegate.resetEmptyHubFlag((OAObject)object, msg.property);
            }
            // LOG.finer("hub not found");
        	return;
        }
        
        if (HubDataDelegate.getPos(h, object, false, false) < 0 ) {
            if (msg.type == OAObjectMessage.INSERT) {
                h.insert(object, msg.pos);
            }
            else h.addElement(object);
        }
        else {
        	// LOG.fine("object already in hub");
        }
    }

    protected void processRemove(OAObjectMessage msg) {
        
        Hub h = getHub(msg,false);
        if (h != null) {
            if (Thread.currentThread() instanceof OAClientThread) {
                // if removing AO, then dont set another one, since this is the ClientThread
                HubAddRemoveDelegate.remove(h, msg.objectKey, false, true, false, false, true);        
            }
            else {
                h.remove(msg.objectKey);
            }
        }
    }

    protected void processMove(OAObjectMessage msg) {
        Hub h = getHub(msg, false); // 20080625 was true
        if (h != null) h.move(msg.pos,msg.posTo);
    }

    protected void processRemoveObject(OAObjectMessage msg) {
        Object obj = OAObjectCacheDelegate.get(msg.objectClass, msg.objectKey);
        if (obj != null) {
            OAObjectCacheDelegate.removeObject((OAObject) obj);
        }
        // else LOG.finer("hub not found");
    }

    protected void processSort(OAObjectMessage msg) {
        Hub h = getHub(msg, false);
        if (h != null) h.sort((String) msg.newValue, (msg.pos == 0));
        // else LOG.finer("hub not found");
    }

    /** used by OAClient to find the Hub that goes with a message */
    protected Hub getHub(OAObjectMessage msg, boolean bAutoLoad) {
        return getHub(msg.objectClass, msg, bAutoLoad);
    }

    protected Hub getHub(OAObjectMessage msg) {
        // find master object
        Object obj = OAObjectCacheDelegate.get(msg.masterClass, msg.masterObjectKey);
        if (obj == null) {
        	// LOG.finer("object not found in cache");
        	return null;
        }
        if (!(obj instanceof OAObject)) {
			// LOG.warning("!(obj instanceof OAObject)");
        	return null;
        }
        OAObject oaObj = (OAObject) obj;
        
        if (!OAObjectReflectDelegate.isReferenceHubLoaded(oaObj, msg.property)) {
        	return null; 
        }

        obj =  OAObjectReflectDelegate.getProperty(oaObj, msg.property);
        if (obj instanceof Hub) return (Hub) obj;
        return null;
    }

    
    protected Hub getHub(Class hubClass, OAObjectMessage msg, boolean bAutoLoad) {
        // find master object
		if (hubClass == null || msg == null) {
			LOG.warning("hubClass == null || msg == null");
			return null;
		}
		if (msg.masterClass == null) {
			LOG.warning("msg.masterClass == null");
			return null;
		}
		if (msg.masterObjectKey == null) {
			LOG.warning( "msg.masterObjectKey == null");
			return null;
		}
        Object obj = OAObjectCacheDelegate.get(msg.masterClass, msg.masterObjectKey);
        if (obj == null) {
        	// LOG.finer("object not found in cache");
        	return null;
        }
        if (!(obj instanceof OAObject)) {
			LOG.warning("!(obj instanceof OAObject)");
        	return null;
        }
        OAObject object = (OAObject) obj;
        
        // 20080823
        if (msg.property != null) {
            if (!bAutoLoad && !OAObjectReflectDelegate.isReferenceHubLoaded(object, msg.property)) return null;
            obj =  OAObjectReflectDelegate.getProperty(object, msg.property);
            if (obj instanceof Hub) return (Hub) obj;
            return null;
        }
        
        // find detail hub
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(msg.masterClass);
        ArrayList al = oi.getLinkInfos();
        Hub hub = null;
        for (int i=0; al != null && i < al.size(); i++) {
        	OALinkInfo li = (OALinkInfo) al.get(i);
            if (li.getType() != OALinkInfo.MANY || li.getName() == null) continue;
			if (li.getToClass() == null) {
				LOG.warning("li.getToClass() == null");
				continue;
			}
            if (!hubClass.equals(li.getToClass()) ) continue;
            // if (msg.property != null && !li.getName().equalsIgnoreCase(msg.property)) continue; // 04/01/2003

            if (!bAutoLoad && !OAObjectReflectDelegate.isReferenceHubLoaded(object, li.getName())) break;  // not loaded, dont load
            
            hub = (Hub) OAObjectReflectDelegate.getProperty(object, li.getName());
            break;
        }
        return hub;
    }

    /**
        Used by Hub, HubController to
        send a Message to OAServer using OAObjectServer.<br>
        This will wait until OAObjectServer gets
        the message back from OAServer and gets it out of the message queue.<br>
        If needed, a new thread will be started to retrieve message from message queue.
    */
    public OAObjectMessage sendMessage(OAObjectMessage msg) {
    	// LOG.finer("msg="+msg);
    	msg.beforeSend();
        try {
        	return clientMessageHandler.sendMessage(msg);
        }
        catch (Exception ex) {
        	LOG.log(OALogger.ERROR, "Error sending msg="+msg, ex);
            handleSendMessageException(msg, ex);
        }
        return msg;
    }



    /**
        Used to get Object from OAServer.publisher
        @see OAObjectPublisher#getObject
    */
    public Object getServerObject(Class clazz, OAObjectKey key) {
    	// LOG.fine("clazz="+clazz+", key="+key.toString());
        OAObjectMessage msg = new OAObjectMessage();
        msg.type = msg.GETOBJECT;
        msg.objectClass = clazz;
        msg.objectKey = key;
        sendMessage(msg);
        return msg.newValue;
    }

    protected Object[] getIds(Object obj) {
        Object[] ids = new Object[] {obj};
        if (obj instanceof OAObject) {
            Object[] objs = OAObjectInfoDelegate.getPropertyIdValues((OAObject) obj);
            if (objs.length > 0) ids = objs;
            
        }
        return ids;
    }

    /**
        Works with OAObject.lock to lock an object across Client/Server.
    */
    public void lock(Object obj) {
    	LOG.fine("obj="+obj);
        lock(obj,null);
    }

    /**
        Works with OAObject.lock to lock an object across Client/Server.
    */
    public void lock(Object obj, Object miscObject) {
    	LOG.fine("obj="+obj+", miscObject="+miscObject);
        if (obj == null) return;
        Object[] ids = getIds(obj);
        try {
            oaObjectServer.lock(obj.getClass(), ids, miscObject);
        }
        catch (Exception ex) {
        	LOG.log(Level.WARNING, "", ex);
            throw new RuntimeException("OAClient.lock Error:", ex);
        }
    }

    /**
        Works with OAObject.lock to lock an object across Client/Server.
    */
    public void unlock(Object obj) {
    	LOG.fine("obj="+obj);
        if (obj == null) return;
        Object[] ids = getIds(obj);
        try {
            oaObjectServer.unlock(obj.getClass(), ids);
        }
        catch (Exception ex) {
        	LOG.log(Level.WARNING, "", ex);
            throw new RuntimeException("OAClient.unlock Error:", ex);
        }
    }

    /**
        Works with OAObject.lock to lock an object across Client/Server.
    */
    public boolean isLocked(Object obj) {
        if (obj == null) return false;
        Object[] ids = getIds(obj);
        try {
            boolean b = oaObjectServer.isLocked(obj.getClass(), ids);
        	LOG.fine("obj="+obj+", return="+b);
        	return b;
        }
        catch (Exception ex) {
        	LOG.log(Level.WARNING, "", ex);
            throw new RuntimeException("OAClient.unlock Error:", ex);
        }
    }

    /**
        Works with OAObject.lock to lock an object across Client/Server.
    */
    public OALock getLock(Object obj) {
    	LOG.fine("obj="+obj);
        if (obj == null) return null;
        Object[] ids = getIds(obj);
        try {
        	return oaObjectServer.getLock(obj.getClass(), ids);
        }
        catch (Exception ex) {
        	LOG.log(Level.WARNING, "", ex);
        	throw new RuntimeException("OAClient.unlock Error:", ex);
        }
    }

    /**
        Works with OAObject.lock to lock an object across Client/Server.
    */
    public Object[] getAllLockedObjects() {
    	LOG.fine("called");
        try {
            return oaObjectServer.getAllLockedObjects();
        }
        catch (Exception ex) {
        	LOG.log(Level.WARNING, "", ex);
        	throw new RuntimeException("OAClient.unlock Error:", ex);
        }
    }

int dsCnt=0;
    /** used by OADataSourceClient to "talk" to OAObjectServer and OADataSource on Server */
    public Object datasource(int command, Object[] objects)  {
//System.out.println((dsCnt++)+") ds command="+command);        
    	LOG.fine("command="+command);
        OAObjectMessage msg = new OAObjectMessage();
        msg.type = msg.DATASOURCE;
        msg.pos = command;
        msg.newValue = objects;

        if (command == OADataSourceClient.IT_NEXT) {
            sendMessage(msg);  // return object needs to be synchronized with queue
            Object[] objs = (Object[]) msg.newValue;
            for (int i=0; objs != null && i<objs.length; i++) {
            	if (objs[i] instanceof OAObject) {
            	    // object was created on server, need to add to add object to local cache
            	    //   so that it can be removed from server cache when it is in a hub (reachable)
            	    internalAddToServerSideCache((OAObject)objs[i]); 
            	}
            }
            return msg.newValue;  // returns Object[20], could have nulls
        }
        try {
            LOG.finer(Thread.currentThread().getName()+" command ="+command);//qqqqq
        	if (msg.isFastDataSourceCommand()) return oaObjectServer.datasource(msg);
        	else sendMessage(msg);
        }
        catch (Exception e) {
        	LOG.log(Level.WARNING, "", e);
            handleDataSourceException(command, e);
        }
        
        return null;
    }

    /**
        Used by OAObject.createNewObject so that object is created on server.
        @see OAObject#createNewObject
    */
    public Object createNewObject(Class clazz) {
    	//LOG.fine("class="+clazz);
        OAObjectMessage msg = new OAObjectMessage();
        msg.type = msg.CREATENEWOBJECT;
        msg.objectClass = clazz;
        sendMessage(msg);
        internalAddToServerSideCache((OAObject)msg.newValue);  // object was created on server, need to add to add object to local cache
        return msg.newValue;
    }

    
    /**
        Used by OAObject.createCopy so that object is created on the server.
        @see OAObject#createCopy
     */
    public OAObject createCopy(OAObject oaObj, String[] excludeProperties) {
        OAObjectMessage msg = new OAObjectMessage();
        msg.type = msg.CREATECOPY;
        msg.objectClass = oaObj.getClass();;
        msg.objectKey = OAObjectKeyDelegate.getKey(oaObj);
        msg.newValue = excludeProperties;
        
        sendMessage(msg);

        internalAddToServerSideCache((OAObject)msg.newValue);  // object was created on server, need to add to add object to local cache
        return (OAObject) msg.newValue;
    }

    // 20130319
    private final static Object NextGuidLock = new Object();
    private int nextGuid;
    private int maxNextGuid;
    /**
        Used by OAObject so that object guid is created on server.
    */
    public int getObjectGuid() {
        int x;
        synchronized (NextGuidLock) {
            if (nextGuid == maxNextGuid) {
                try {
                    nextGuid = oaObjectServer.getNextFiftyObjectGuids();
                    maxNextGuid = nextGuid + 50; 
                }
                catch (Exception ex) {
                    LOG.log(Level.WARNING, "", ex);
                    throw new RuntimeException("OAClient.getObjectGuid Error:", ex);
                }
            }
            x = nextGuid++;
        }
        return x;
    }

    /**
        Used by OAObject getHub to retrieve an OAObject reference property that is a Hub.
        @see OAObject#getHub
    */
    public Hub getDetailHub(OAObject masterObject, String propertyName) {
        Object obj = getDetail(masterObject, propertyName);
        if (obj instanceof Hub) return (Hub) obj;
        return null;
    }

    /**
        Used by OAObject getHub to retrieve an OAObject reference property that is an OAObject.
        @see OAObject#getObject
    */
    public Object getDetailObject(OAObject masterObject, String propertyName) {
        return getDetail(masterObject, propertyName);
    }

    public int cntGetDetail;
    /**
        Used by OAObject getHub to retrieve an OAObject reference property.
        @see #getDetailHub
        @see #getDetailObject
        @see OAObject#getHub
        @see OAObject#getObject
    */
    public Object getDetail(OAObject masterObject, String propertyName) {
        //qqqqqqqqqqqqqqqqqqqqqqvvvvvvvvvvvvvvv        
        //System.out.println("OAClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName+", levels="+levels);
        //LOG.finer("OAClient.getDetail, masterObject="+masterObject+", propertyName="+propertyName);
        
        cntGetDetail++;        
        int xDup = OAObjectSerializeDelegate.cntDup;
        int xNew = OAObjectSerializeDelegate.cntNew;

    	// LOG.fine("masterObject="+masterObject+", propertyName="+propertyName);
        if (masterObject == null || propertyName == null) return null;
        OAObjectMessage msg = new OAObjectMessage();
        msg.type = msg.GETDETAIL;
        msg.masterClass = masterObject.getClass();
        msg.masterObjectKey = OAObjectKeyDelegate.getKey(masterObject);
        msg.property = propertyName;

        boolean bGetSibs;
        if (!(Thread.currentThread() instanceof OAClientThread)) {
            bGetSibs = true;
            // 20130216
            // send siblings to return back with same prop
            OAObjectKey[] siblingKeys = null;
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(masterObject.getClass(), propertyName);
            if (li == null || !li.getCalculated()) {
                siblingKeys = getDetailSiblings(masterObject, propertyName);
            }
            
            String[] props = OAObjectReflectDelegate.getUnloadedReferences(masterObject, false);
            msg.newValue = new Object[] {props, siblingKeys};
        }
        else bGetSibs = false;
        OAObjectMessage msgx = sendMessage(msg);
        Object obj = msgx.newValue;
        if (obj instanceof OAObjectSerializer) obj = ((OAObjectSerializer)obj).getObject();
        
        
        
        
//qqqqqqq        
        if (true || OAObjectSerializeDelegate.cntNew-xNew > 25 || cntGetDetail % 100 == 0)        
        System.out.println(String.format(
            "%,d) OAClient.getDetail() Obj=%s, prop=%s, ref=%s, getSib=%b, newCnt=%d, dupCnt=%d, totNewCnt=%d, totDupCnt=%d",
            cntGetDetail, 
            masterObject, 
            propertyName, 
            obj==null?"null":obj.getClass().getName(),
            bGetSibs,
            OAObjectSerializeDelegate.cntNew-xNew, 
            OAObjectSerializeDelegate.cntDup-xDup,
            OAObjectSerializeDelegate.cntNew, 
            OAObjectSerializeDelegate.cntDup
        ));        
        
        return obj;
    }

    private OAObject[] lastMasterObjects = new OAObject[10];
    private int lastMasterCnter;
    /**
     * Find any other siblings to get the same property for.
     */
    protected OAObjectKey[] getDetailSiblings(OAObject masterObject, String property) {
        Hub siblingHub = null;
        
        Hub hubThreadLocal = OAThreadLocalDelegate.getGetDetailHub();
        if (hubThreadLocal != null && hubThreadLocal.contains(masterObject)) {
            siblingHub = hubThreadLocal;
        }
        
        if (siblingHub == null) {
            WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferences(masterObject);
            
            int hits = 0;
            for (int i=0; (refs != null && i < refs.length); i++) {
                WeakReference<Hub<?>> ref = refs[i];
                if (ref == null) continue;
                Hub hub = ref.get();
                if (hub == null) continue;
                OAObject masterx = hub.getMasterObject();
                if (masterx != null || hub.getSelect() != null) {
                    if (siblingHub != null) { // more then one possible hub
                        // see if one of the previous objects can be found
                        if (hits == 0) {
                            hits++;
                            for (OAObject objz : lastMasterObjects) {
                                if (objz != null && siblingHub.contains(objz)) {
                                    hits++;
                                }
                            }
                        }
                        
                        int hits2 = 1;
                        for (OAObject objz : lastMasterObjects) {
                            if (objz != null && hub.contains(objz)) {
                                hits2++;
                                if (hits2 >= hits) {
                                    break;
                                }
                            }
                        }
                        if (hits2 >= hits) {
                            hits = hits2;
                            siblingHub = hub;
                        }
                    }
                    else {
                        siblingHub = hub;
                    }
                }
            }
        }
        lastMasterObjects[lastMasterCnter++%lastMasterObjects.length] = masterObject;
        if (siblingHub == null) {
            return null;
        }
        
        ArrayList<OAObjectKey> al = new ArrayList<OAObjectKey>();
        // load the same property for siblings
        int pos = siblingHub.getPos(masterObject)+1;
        int cnt = 0;
        for (int i=0; i<250; i++) {
            Object obj = siblingHub.getAt(i+pos);
            if (obj == null) break;
            if (obj == masterObject) continue;

            Object value = OAObjectReflectDelegate.getRawReference((OAObject)obj, property);
            if (value == null) {
                if (OAObjectPropertyDelegate.isPropertyLoaded((OAObject)obj, property)) continue;                     
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                al.add(key);
                if (++cnt == 50) break;
            }
            else if (value instanceof OAObjectKey) {
                OAObjectKey key = OAObjectKeyDelegate.getKey((OAObject)obj);
                al.add(key);
                if (++cnt == 155) break;
            }
        }

        if (al == null || al.size() == 0) return null;
        int x = al.size();
        OAObjectKey[] keys = new OAObjectKey[x];
        al.toArray(keys);
        return keys;
    }
    
    
    
    /** 20090123 
     * ServerSide Cache for objects that were specifically created for this client.
     * An object is "flagged" for severside caching if it was created on this client or for this client.
     * It needs to stay flagged, in case it is removed from Hubs, which could cause gc of the object on server, while it still exists on client.
     * ex: If Object is not in a Hub, then it could be gc'd on server, while it still exists on a client(s).
     * To keep the object from gc on server, each OAObjectServer maintains a cache to keep "unattached" objects from being gc'd.
     * 
     * The object is needed on the server specifically to handle reference changes to the object, which do not get transferred during
     * a hub.add oaobjectmessage to the server.
     */

    // called internally when an object is created on server for this client, 
    //     and the object is already in the cache on the Server.
    //     used by: createCopy, createNewObject, datasource
    private void internalAddToServerSideCache(OAObject oaObj) {
        if (oaObj == null) return;
        
        if (OAObjectHubDelegate.isInHub(oaObj)) return;
        
 // 20120616            
//System.out.println("OAClient.serverSideCache internal ADDing>"+(++xxcnt1)+"  "+oaObj);            
LOG.finer("ADDing internal new>"+(xxcnt1)+"  "+oaObj);            
        int guid = OAObjectDelegate.getGuid(oaObj);
        synchronized (hashServerSideCache) {
            hashServerSideCache.add(guid);
        }
    }

    // called for locally created oaobjects
    public void initializeObject(OAObject oaObj) {  // called loacally to add object to serverside cache, and flag the object as being created locally
        if (oaObj == null) return;
        int guid = OAObjectDelegate.getGuid(oaObj);
        synchronized (hashServerSideCache) {
            if (hashServerSideCache.contains(guid)) {
                return;
            }
            hashServerSideCache.add(guid);
        }
// 20120616            
//System.out.println("OAClient.serverSideCache ADDing>"+(++xxcnt1)+"  "+oaObj);            
LOG.finer("ADDing new>"+(xxcnt1)+"  "+oaObj);            
        try {
            oaObjectServer.addToCache(oaObj);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    // called by oaobject finalize
    public void finalizeObject(OAObject oaObj) {
        if (oaObj == null) return;
        int guid = OAObjectDelegate.getGuid(oaObj);
        synchronized (hashServerSideCache) {
            if (!hashServerSideCache.contains(guid)) return;

// 20120616            
//System.out.println("OAClient.serverSideCache finalize Removing>"+(--xxcnt1)+"  "+oaObj);            
LOG.finer("Removing finalize>"+(xxcnt1)+"  "+oaObj);            
            
            hashServerSideCache.remove(guid); 
        }
        try {
            removeFromServerSideCache(oaObj);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    

    
    
public int xxcnt1;    
    // called whenever an object does not have any hubs attached, to have the object readded to the serverSideCache
    public void addToServerSideCache(OAObject oaObj) {
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;  // 2006/07/17
        
        if (OAThreadLocalDelegate.isHubMergerChanging()) return; // 20120616
        
        if (oaObj == null) return;

        int guid = OAObjectDelegate.getGuid(oaObj);
        synchronized (hashServerSideCache) {
            if (hashServerSideCache.contains(guid)) {
                return;
            }
        }
        
        // 20120616 only needed if the server does not have a reference to object
        if (OAObjectReflectDelegate.hasReference(oaObj)) {
            return;
        }
        
        synchronized (hashServerSideCache) {
            if (hashServerSideCache.contains(guid)) return;
            hashServerSideCache.add(guid);
        }

        try {
// 20120616            
//System.out.println("OAClient.serverSideCache ADDing>"+(++xxcnt1)+"  "+oaObj);            
LOG.finer("ADDing>"+(xxcnt1)+"  "+oaObj);            
            oaObjectServer.addToCache(oaObj);
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    ArrayList<OAObjectKey> alRemoveFromCache = new ArrayList<OAObjectKey>(100);
    // called when an object is added to a hub that would keep the object from being gc'd on the server.
    public void removeFromServerSideCache(OAObject oaObj) {
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;  // 2006/07/17
        if (oaObj == null) return;
        int guid = OAObjectDelegate.getGuid(oaObj);
        
        OAObjectKey[] keys = null;
        synchronized (hashServerSideCache) {
            if (!hashServerSideCache.contains(guid)) return;
            hashServerSideCache.remove(guid);
            
            alRemoveFromCache.add(OAObjectKeyDelegate.getKey(oaObj));
            if (alRemoveFromCache.size() == 100) {
                keys = new OAObjectKey[100];
                alRemoveFromCache.toArray(keys);
                alRemoveFromCache.clear();
            }
        }
        try {
            
// 20120616            
//System.out.println("OAClient.serverSideCache Removing>"+(--xxcnt1)+"  "+oaObj);            
LOG.finer("Removing>"+(xxcnt1)+"  "+oaObj);            
            if (keys != null) {
                oaObjectServer.removeFromCache(oaObj.getClass(), keys);
            }
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    private boolean bSendException; 
    public void sendException(String s, Throwable e, boolean bFromServer) {
        if (bSendException) return;
        bSendException = true;
    	LOG.fine("msg="+s+", exception="+e+", bFromServer="+bFromServer);
/*qqqqqqqq    	
        OAObjectMessage msg = new OAObjectMessage();
        msg.property = s;
        msg.type = OAObjectMessage.EXCEPTION;
        msg.newValue = e;
        msg.pos = bFromServer ? 1 : 0;
        try {
        	msg.posTo = oaObjectServer.getId();
        }
        catch (Exception ex) {
        }
        sendMessage(msg);
*/        
        bSendException = false;
    }

    
    // ========================= Mehtods to Overwrite ===============================
    /**
     * Works with sendException() to allow other clients (and server) to get exceptions.
    */
    public void handleException(int clientId, boolean bFromServer, String msg, Throwable e) {
    }

    /**
     * called when an ErrorMessage is received from server.
     * Default is to disconnect from server.
    */
    public void handleError(int msgErrorCode) {
    	LOG.severe("received OAObjectMessage.ERROR, code="+msgErrorCode+", closing connection");
    }

    /**
    Called when getMessage loop throws an exception.  By default, this will print error message and
    continue.
    NOTE: it is strongly recommended to close and reconnect, since sendMessage() could
    	be waiting on a return message.
	*/
	public void handleGetMessageException(OAObjectMessage msg, Exception e) {
	    if (oaClient == null) return;
	    LOG.log(Level.WARNING, "msg="+msg, e);
	}

		
    /**
    Called when sendMessage throws an exception.  By default, this will print error message.
    */
    public void handleSendMessageException(OAObjectMessage msg, Exception e) {
	    if (oaClient == null) return;
	    LOG.log(Level.WARNING, "msg="+msg, e);
    }

    /**
    Called when processing a message from OAClientMessageReader throws an exception.  By default, this will print error message.
    */
    public void handleProcessMessageException(OAObjectMessage msg, Exception e) {
	    if (oaClient == null) return;
	    LOG.log(Level.WARNING, "msg="+msg, e);
    }
    
    
    
    public void handleDataSourceException(int command, Exception e) {
	    if (oaClient == null) return;
	    LOG.log(Level.WARNING, "command="+command, e);
    }

    
    /**
     * Called when OAObjectMessage.type = ERROR.
     * The error code is stored in msg.pos
     */
    protected void processError(OAObjectMessage msg) throws Exception {
    	LOG.warning("msg="+msg);
    	handleError(msg.pos);
    }

    /** 
     * Message to only process if Server.  Any change message will be sent
     * to clients, even if this is in a OAClientThread
     * @return
     */
    public static boolean beginServerOnly() {
    	return OAClientDelegate.setProcessIfServer(true);
    }
    public static void endServerOnly() {
    	OAClientDelegate.setProcessIfServer(false);
    }
    public static void startServerSide() {
        OAClientDelegate.setProcessIfServer(true);
    }
    public static void endServerSide() {
        OAClientDelegate.setProcessIfServer(false);
    }
    
    public static boolean processIfServer() {
        return OAClientDelegate.processIfServer();
    }
    
//qqqqqqqqqqqqqq    
    /** 20120326 used by getProxy, to send method calls to server
     * 
     * @param clazz Class for interface to create a proxy instance
     * @param methodName
     * @param args
     * @return
     *
    public Object remoteMethodCall(String remoteObjectName, String methodName, Object[] args) {
        // LOG.fine("class="+clazz+", objectIds="+objectIds);
        OAObjectMessage msg = new OAObjectMessage();
        msg.type = OAObjectMessage.REMOTEMETHODCALL;
        msg.property = methodName;
        msg.newValue = new Object[] { remoteObjectName, methodName, args};
        sendMessage(msg);
        Object obj =  msg.newValue;
        return obj;
    }
*/
    private static ConcurrentHashMap<String, Object> hmProxy = new ConcurrentHashMap<String, Object>();
    /** 20120326 used by getProxy, to send method calls to server
     * 
     * @return new instance of clazz
     * @see OAServerInterface#registerForRemoteMethodCall to register the implmentation object.
     */
    public Object lookupRemote(final String remoteObjectName) throws Exception {
        Object proxy = hmProxy.get(remoteObjectName);
        if (proxy != null) return proxy;

        proxy = getRemoteMultiplexerClient().lookup(remoteObjectName);
        
        hmProxy.put(remoteObjectName, proxy);
        LOG.fine(String.format("Created proxy instance, remoteObjectName=%s, class=%s", remoteObjectName, proxy==null?"null":proxy.getClass().getName()));
        return proxy;
    }

    private OAClientInfo clientInfo;
    public OAClientInfo getClientInfo() {
        if (clientInfo == null) {
            clientInfo = new OAClientInfo();
            clientInfo.setId(getId());
            try {
                clientInfo.hostName = InetAddress.getLocalHost().getHostName();
                clientInfo.ipAddress = InetAddress.getLocalHost().getHostAddress();
                clientInfo.setUserName(System.getProperty("user.name"));
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
        return clientInfo;
    }
    
}


