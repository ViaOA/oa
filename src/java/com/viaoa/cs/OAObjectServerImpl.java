/*
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.*;
import java.lang.reflect.Method;
import java.rmi.*;
import java.rmi.server.*;

import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.ds.cs.*;
import com.viaoa.ds.jdbc.query.ResultSetIterator;
import com.viaoa.hub.Hub;

// qqqqqqqq make this abstract and dont give oaServer, have OAServer implement abstract when creating new OAObjectServerImpl
/** 
    RMI Distributed object used by OAClient to communicate with OAServer.
    Created by OAServerImpl.
    <p>
    See OAObjectServer for complete documentation.
    @see OAObjectServer 
*/
public class OAObjectServerImpl extends UnicastRemoteObject implements OAObjectServer, Unreferenced {

    private static Logger LOG = Logger.getLogger(OAObjectServerImpl.class.getName());

    OAServerImpl oaServer;
    Vector vecLock = new Vector(5, 5); // used by oaServer to store locks for this user
    static private int gid;
    protected int id;
    boolean bGettingMessagesNow;
    long timeLastGetMessages;
    boolean bConnected = true;
    int status;
    OADateTime dtStart, dtEnd;
    Object user;
    int cntMsgSent; // sent to client
    int cntMsgReceived; // received from client
    int queueReset; // used by OAServer, incremented whenever the queueLoadPos is changed on server

    long queueLoadPos;

    public static final int STATUS_CONNECTED = 0;
    public static final int STATUS_CLOSED = 1;
    public static final int STATUS_DISCONNECTED = 2;
    public static final int STATUS_QUEUEOVERRUN = 3;

    public static final String[] STATUS = { "Connected", "Closed by client", "Disconnected", "Queue Overrun" };

    protected OADataSource defaultDataSource;
    private Hashtable<String, Iterator> hashIterator = new Hashtable<String, Iterator>(); // used to store DB
    private int selectCount;
    protected ConcurrentHashMap<Object, Object> hashCache = new ConcurrentHashMap<Object, Object>();
    private OAClientInfo clientInfo;

    // tracks guid for all oaObjects serialized, the Boolean: true=all references have been sent, false=object has been sent (mihgt not have all references)
    private TreeMap<Integer, Boolean> treeSerialized = new TreeMap<Integer, Boolean>();
    private ReentrantReadWriteLock rwLockTreeSerialized = new ReentrantReadWriteLock();

    public OAObjectServerImpl(OAServerImpl oaServer, int port) throws RemoteException {
        super(port);
        id = ++gid;
        dtStart = new OADateTime();
        this.oaServer = oaServer;
        LOG.config("created id=" + id);
    }

    public OAObjectServerImpl(OAServerImpl oaServer, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
        id = ++gid;
        dtStart = new OADateTime();
        this.oaServer = oaServer;
        LOG.config("created id=" + id);
    }

    /** unique identifier set by OAServer */
    public int getId() throws RemoteException {
        return id;
    }

    public int getClientId() {
        return id;
    }

    /** disconnect from OAServer */
    public void close() throws RemoteException {
        internalClose();
    }

    void disconnected() {
        if (bConnected) {
            status = STATUS_DISCONNECTED;
            internalClose();
        }
    }

    private void internalClose() {
        if (bConnected) {
            bConnected = false;
            if (status == STATUS_CONNECTED) status = STATUS_CLOSED;
            dtEnd = new OADateTime();
            for (Iterator iterator : hashIterator.values()) {
                ((ResultSetIterator) iterator).close();
            }
            oaServer.onClose(this);
        }
    }

    /** send message to OAServer and wait for message to be received by getMessage.  Then
        send message back, since it can be changed during the process.
        @param bFrontOfQueue if this message is in response to a message received by OAClient
    */
    public @Override void sendMessage(final OAObjectMessage msg) throws RemoteException {
        cntMsgSent++;
        msg.objectServerId = this.id;
        process(msg);
        oaServer.sendMessage(this, msg); // async call, see getMessages(..)
    }
    int cntx;

    // 20120628 processes message at the "right time".  see: sendMessage(..), getMessages(..)
    protected void process(final OAObjectMessage msg) throws RemoteException {
        if (msg == null) return;

        if (msg.type == OAObjectMessage.GETDETAIL) {
            //System.out.println("GetDetail: from="+msg.masterClass+", id="+msg.masterObjectKey+", property="+msg.property);            
            
            // get the object
            Object detailValue = getDetail(msg.masterClass, msg.masterObjectKey, msg.property);
            Object objMaster = OAObjectCacheDelegate.get(msg.masterClass, msg.masterObjectKey);
            if (oaServer.publisher != null) { // this will allow publisher to determine how reference properties are sent
                if (objMaster != null) {
                    Object objx = oaServer.publisher.getDetail(objMaster, msg.property, detailValue);
                    if (objx != null) detailValue = objx;
                }
            }

            if (msg.newValue != null && objMaster instanceof OAObject) {
                Object[] params = (Object[]) msg.newValue;
                String[] masterProps = (String[]) params[0];
                OAObjectKey[] siblingKeys = (OAObjectKey[]) params[1];
                
                OAObjectSerializer os = getSerializedDetail((OAObject)objMaster, detailValue, msg.property, masterProps, siblingKeys);
                msg.newValue = os;
            }
            else msg.newValue = detailValue;
        }
        else if (msg.type == OAObjectMessage.CREATENEWOBJECT) {
            msg.newValue = OAObjectReflectDelegate.createNewObject(msg.objectClass);

            // CACHE_NOTE: need to have OAObject.bCachedOnServer=true set by Client.
            // see: OAObjectCSDelegate.addedToCache((OAObject) msg.newValue); // flag obj to know that it is cached on server for this client.
            addToCache(msg.newValue);
        }
        else if (msg.type == OAObjectMessage.GETOBJECT) {
            msg.newValue = OAObjectReflectDelegate.getObject(msg.objectClass, msg.objectKey);
        }
        if (msg.type == OAObjectMessage.DATASOURCE) {
            msg.newValue = datasource(msg.pos, (Object[]) msg.newValue);
        }
        else if (msg.type == OAObjectMessage.GETPUBLISHEROBJECT) {
            if (oaServer.publisher == null) msg.newValue = null;
            else msg.newValue = oaServer.publisher.getObject(id, msg.objectClass, (Object[]) msg.newValue);
        }
        else if (msg.type == OAObjectMessage.REMOTEMETHODCALL) {
            msg.newValue = remoteMethodCall((String) ((Object[]) msg.newValue)[0], (String) ((Object[]) msg.newValue)[1], (Object[]) ((Object[]) msg.newValue)[2]);
        }
        else if (msg.type == OAObjectMessage.CREATECOPY) {
            OAObject obj = OAObjectCacheDelegate.getObject(msg.objectClass, msg.objectKey);
            msg.newValue = OAObjectReflectDelegate.createCopy(obj, (String[]) msg.newValue);
            addToCache(msg.newValue);
        }
        else if (msg.type == OAObjectMessage.SAVE) {
            OAObject obj = OAObjectCacheDelegate.getObject(msg.objectClass, msg.objectKey);
            if (obj != null) {
                msg.newValue = null;
                try {
                    obj.save();
                }
                catch (Exception e) {
                    msg.newValue = e.toString();
                }
            }
            else msg.newValue = "Object not found";
        }
        else if (msg.type == OAObjectMessage.DELETE) {
            OAObject obj = OAObjectCacheDelegate.getObject(msg.objectClass, msg.objectKey);
            msg.newValue = null;
            if (obj != null) {
                try {
                    obj.delete(); // delete is done on server side
                }
                catch (Exception e) {
                    msg.newValue = e.toString();
                }
            }
            else {
                msg.newValue = "Object not found";
            }
        }
        else if (msg.type == OAObjectMessage.DELETEALL) {
            Object objx = getDetail(msg.masterClass, msg.masterObjectKey, msg.property);
            if (objx instanceof Hub) {
                try {
                    ((Hub) objx).deleteAll();
                }
                catch (Exception e) {
                    msg.newValue = e.toString();
                }
            }
        }
        else if (msg.type == OAObjectMessage.CLIENTINFO) {
            this.clientInfo = (OAClientInfo) msg.newValue;
            clientInfo.lastClientUpdateReceived = new OADateTime();
            if (msg.pos != 1) msg.newValue = null;
            updateClientInfo();
        }
    }

    /** waits for OAServer to send a message and then returns it to OAClient.run().
    OAClient.run() has a loop that calls this to get the next message.  This method
    will call wait() and will be notified by addMessage() when new msg is added.
    */
    public @Override
    OAObjectMessage[] getMessages() throws RemoteException {
        // LOG.finer("called");
        
        int nextSeq = 0;
        OAObjectMessage[] msgs;
        for (;;) {
            timeLastGetMessages = System.currentTimeMillis();
            bGettingMessagesNow = true;
            try {
                msgs = oaServer.getMessages(this);
                if (msgs != null) break;
            }
            finally {
                timeLastGetMessages = System.currentTimeMillis();
                bGettingMessagesNow = false;
            }
        }
        
        cntMsgReceived += msgs.length;
        return msgs;
    }

    protected void finalize() throws Throwable {
        super.finalize();
        // release locks
        for (; vecLock.size() > 0;) {
            Object obj = vecLock.elementAt(0);
            oaServer.unlock(this, obj);
        }
        close();
    }

    public void lock(Class clazz, Object[] objectIds, Object miscObject) throws RemoteException {
        Object obj = OAObjectCacheDelegate.get(clazz, objectIds);
        if (obj != null) {
            oaServer.lock(this, obj, miscObject); // this will add to list of locks
        }
    }

    public void unlock(Class clazz, Object[] objectIds) throws RemoteException {
        Object obj = OAObjectCacheDelegate.get(clazz, objectIds);
        if (obj != null) {
            oaServer.unlock(this, obj);
        }
    }

    public boolean isLocked(Class clazz, Object[] objectIds) throws RemoteException {
        Object obj = OAObjectCacheDelegate.get(clazz, objectIds);
        if (obj != null) {
            return oaServer.isLocked(obj);
        }
        return false;
    }

    public OALock getLock(Class clazz, Object[] objectIds) throws RemoteException {
        Object obj = OAObjectCacheDelegate.get(clazz, objectIds);
        if (obj != null) return oaServer.getLock(obj);
        return null;
    }

    public Object[] getAllLockedObjects() throws RemoteException {
        return oaServer.getAllLockedObjects();
    }

    protected OADataSource getDataSource(Class c) {
        if (c != null) {
            OADataSource ds = OADataSource.getDataSource(c);
            if (ds != null) return ds;
        }
        if (defaultDataSource == null) {
            OADataSource[] dss = OADataSource.getDataSources();
            if (dss != null && dss.length > 0) return dss[0];
        }
        return defaultDataSource;
    }

    protected OADataSource getDataSource() {
        return getDataSource(null);
    }

    /** return directly to calling program.  Can also use sendMessage(msg) and get return value
        from OAClient.getMessage().newValue, if request needs to be synchronized with queue (ex: OADataSourceClient.IT_NEXT, since it returns an object)
    */
    public Object datasource(OAObjectMessage msg) throws RemoteException {
        return datasource(msg.pos, (Object[]) msg.newValue);
    }

    /** called by OADataSourceClient, OAClient to "talk" with OADataSource on Server */
    public Object datasource(int command, Object[] objects) throws RemoteException {
        Object obj = null;
        Class clazz, masterClass;
        OADataSource ds;
        Object objKey;
        boolean b;
        int x;
        Iterator iterator;
        Object whereObject;
        String propFromMaster;

        switch (command) {
        case OADataSourceClient.IT_NEXT:
            iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                Object[] objs = new Object[20];
                for (int i = 0; i < 20; i++) {
                    if (!iterator.hasNext()) break;
                    objs[i] = iterator.next();
                    if (objs[i] instanceof OAObject) {
                        OAObject oa = (OAObject) objs[i];
                        if (!OAObjectHubDelegate.isInHub(oa)) {
                            // CACHE_NOTE: need to have OAObject.bCachedOnServer=true set by Client.
                            // see: OAObjectCSDelegate.addedToCache((OAObject) msg.newValue); // flag obj to know that it is cached on server for this client.
                            this.addToCache(objs[i]);
                        }
                    }
                }
                obj = objs;
            }
            break;
        case OADataSourceClient.IT_HASNEXT:
            iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                b = iterator.hasNext();
                if (!b) {
                    iterator.remove();
                    hashIterator.remove(objects[0]);
                }
                obj = new Boolean(b);
            }
            break;
        case OADataSourceClient.IS_AVAILABLE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.isAvailable();
                obj = new Boolean(b);
            }
            break;
        case OADataSourceClient.ASSIGNNUMBERONCREATE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.getAssignNumberOnCreate();
                obj = new Boolean(b);
            }
            break;
        case OADataSourceClient.MAX_LENGTH:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.getMaxLength(clazz, (String) objects[1]);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.IS_CLASS_SUPPORTED:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            obj = new Boolean((ds != null));
            break;

        case OADataSourceClient.UPDATE_MANY2MANY_LINKS:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                whereObject = objects[1];
                whereObject = OAObjectCacheDelegate.get(clazz, whereObject);
                ds.updateMany2ManyLinks((OAObject) whereObject, (OAObject[]) objects[2], (OAObject[]) objects[3], (String) objects[4]);
            }
            break;

        case OADataSourceClient.INSERT:
            obj = objects[0];
            if (obj != null) {
                b = ((Boolean) objects[1]).booleanValue();
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.insert((OAObject) obj);
                obj = null;
            }
            break;

        case OADataSourceClient.UPDATE:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.update((OAObject) obj, (String[]) objects[1], (String[]) objects[2]);
                obj = null;
            }
            break;

        case OADataSourceClient.SAVE:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.save((OAObject) obj);
                obj = null;
            }
            break;

        case OADataSourceClient.DELETE:
            obj = objects[0];
            if (obj != null) {
                ds = getDataSource(obj.getClass());
                if (ds != null) ds.delete((OAObject) obj);
                obj = null;
            }
            break;
        case OADataSourceClient.COUNT:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                // 2006/09/14
                int z = objects.length - 2;
                Object[] objs = new Object[z];
                for (int y = 0; y < z; y++)
                    objs[y] = objects[2 + y];
                x = ds.count(clazz, (String) objects[1], objs);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.COUNTPASSTHRU:
            ds = getDataSource();
            if (ds != null) {
                x = ds.countPassthru((String) objects[0]);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.COUNT2:
            clazz = (Class) objects[0];
            whereObject = objects[1];
            String extraWhere = (String) objects[2];
            Object[] args = (Object[]) objects[3];
            propFromMaster = (String) objects[4];

            if (whereObject instanceof OAObjectKey) {
                whereObject = OAObjectCacheDelegate.get(clazz, whereObject);
                if (whereObject == null) System.out.println("OAObjectServer.count cant find class=" + clazz + " id=" + objects[1]);
            }

            ds = getDataSource(clazz);
            if (ds != null) {
                x = ds.count(clazz, (OAObject) whereObject, extraWhere, args, propFromMaster);
                obj = new Integer(x);
            }
            break;
        case OADataSourceClient.SUPPORTSSTORAGE:
            ds = getDataSource();
            if (ds != null) {
                b = ds.supportsStorage();
                obj = new Boolean(b);
            }
            break;
        case OADataSourceClient.EXECUTE:
            ds = getDataSource();
            if (ds != null) {
                return ds.execute((String) objects[0]);
            }
            break;
        case OADataSourceClient.IT_REMOVE:
            iterator = (Iterator) hashIterator.get(objects[0]);
            if (iterator != null) {
                iterator.remove();
                hashIterator.remove(objects[0]);
            }
            break;

        case OADataSourceClient.SELECT:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                int z = objects.length - 3;
                Object[] params = new Object[z];
                for (int y = 0; y < z; y++)
                    params[y] = objects[3 + y];

                iterator = ds.select(clazz, (String) objects[1], params, (String) objects[2]); // where, order
                obj = "client" + (selectCount++);
                if (iterator != null) hashIterator.put((String) obj, iterator);
            }
            break;
        case OADataSourceClient.SELECTPASSTHRU:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                iterator = ds.select(clazz, (String) objects[1], (String) objects[2]); // where, order
                obj = "client" + (selectCount++);
                hashIterator.put((String) obj, iterator);
            }
            break;
        case OADataSourceClient.INITIALIZEOBJECT:
            clazz = (Class) objects[0].getClass();
            ds = getDataSource(clazz);
            ds.initializeObject((OAObject) objects[0]);
            break;
        case OADataSourceClient.SELECTUSINGOBJECT:
            clazz = (Class) objects[0]; // class to select
            masterClass = (Class) objects[1];
            whereObject = objects[2];
            extraWhere = (String) objects[3];
            args = (Object[]) objects[4];
            propFromMaster = (String) objects[5];

            //System.out.println("SELECT(w/master) class:"+clazz+" masterClass:"+masterClass+" whereObj:"+whereObject+" propFromMaster:"+propFromMaster);

            if (whereObject instanceof OAObjectKey) {
                whereObject = OAObjectCacheDelegate.get(masterClass, whereObject);
                if (whereObject == null) {
                    // System.out.println("OAObjectServer.select cant find using where object masterclass="+masterClass+" id="+objects[2]+" "+propFromMaster);
                    break;
                }
            }
            ds = getDataSource(clazz);
            if (ds != null) {
                iterator = ds.select(clazz, (OAObject) whereObject, extraWhere, args, propFromMaster, (String) objects[6]);
                obj = "client" + (selectCount++);
                if (iterator != null) {
                    hashIterator.put((String) obj, iterator);
                }
            }
            break;
        case OADataSourceClient.INSERT_WO_REFERENCES:
            whereObject = objects[0];
            clazz = whereObject.getClass();
            ds = getDataSource(clazz);
            if (ds != null) {
                OAObject oa = (OAObject) whereObject;
                ds.insertWithoutReferences((OAObject) oa);
                OAObjectDelegate.setNew(oa, false);
            }
            break;
        case OADataSourceClient.GET_PROPERTY:
            clazz = (Class) objects[0];
            ds = getDataSource(clazz);
            if (ds != null) {
                objKey = (OAObjectKey) objects[1];
                whereObject = OAObjectCacheDelegate.get(clazz, objKey);
                String prop = (String) objects[2];
                obj = ds.getPropertyBlobValue((OAObject) whereObject, prop);
            }
            break;
        }
        return obj;
    }

    public int getNextFiftyObjectGuids() throws RemoteException {
        return OAObjectDelegate.getNextFiftyGuids();
    }

    private Object getDetail(Class masterClass, OAObjectKey masterObjectKey, String propertyName) {
        if (masterObjectKey == null || propertyName == null) return null;

        Object masterObject = OAObjectReflectDelegate.getObject(masterClass, masterObjectKey);
        if (masterObject == null) {
            //qqqqqqqqqqqq vavavvvvvvvvvv DEBUG         
            //masterObject = OAObjectReflectDelegate.getObject(masterClass, masterObjectKey);
            LOG.warning("cant find masterObject in cache or DS.  masterClass=" + masterClass + ", key=" + masterObjectKey + ", property=" + propertyName + ", OS.id=" + id);
            return null;
        }
        Object objx = OAObjectReflectDelegate.getProperty((OAObject) masterObject, propertyName);

        // 20120926 dont send a reference Hub that is shared, since it is a calc Hub and can
        //    be recreated on the client.  Otherwise all of the sharedHub info is sent and the
        //     client hub will not handle it correctly (masterObject, etc)
        if (objx instanceof Hub) {
            Hub h = (Hub) objx;
            if (h.getSharedHub() != null) {
                h = new Hub();
                objx = h;
            }
        }

        return objx;
    }

    /** used by OAObject to add an object to cache on Server */
    public int addToCache(Object obj) throws RemoteException {
        // NOTE: need to have OAObject.bCachedOnServer=true set by Client.
        // see: OAObjectCSDelegate.addedToCache((OAObject) msg.newValue); // flag obj to know that it is cached on server for this client.
        if (obj == null) return 0;
        hashCache.put(obj, obj);
        LOG.fine("obj=" + obj + ", cacheSize=" + hashCache.size());//qqqqqqqqqqqqqqqqqqqqq        
        return hashCache.size();
    }

    //qqqqqqqqq 20120403
    /**
     * Remove any objects from cache that are now reachable by OACascade
     */
    public void updateCache(OACascade cascade) {
        for (Map.Entry<Object, Object> entry : hashCache.entrySet()) {
            Object obj = entry.getKey();
            if (obj instanceof OAObject) {
                OAObject oa = (OAObject) obj;
                if (cascade.wasCascaded(oa, false)) {
                    hashCache.remove(obj);
                }
            }
        }
    }

    // note: this should be called after all updateCaches are called.
    public void saveCache(OACascade cascade, int iCascadeRule) {
        for (Map.Entry<Object, Object> entry : hashCache.entrySet()) {
            Object obj = entry.getKey();
            if (obj instanceof OAObject) {
                OAObject oa = (OAObject) obj;
                if (!oa.wasDeleted()) {
                    OAObjectSaveDelegate.save(oa, iCascadeRule, cascade);
                }
            }
        }
    }

    /** used by OAObject to remove an object from cache on Server */
    public int removeFromCache(Class clazz, OAObjectKey[] keys) throws RemoteException {
        for (int i = 0; keys != null && i < keys.length; i++) {
            OAObjectKey key = keys[i];
            OAObject obj = OAObjectCacheDelegate.get(clazz, key);
            if (obj != null) hashCache.remove(obj);
            LOG.fine("obj=" + obj + ", cacheSize=" + hashCache.size());//qqqqqqqqqqqqqqqqqqqqq
        }
        return hashCache.size();
    }

    // created and sent by client
    protected OAClientInfo getClientInfo() {
        return clientInfo;
    }

    protected void updateClientInfo() {
        if (clientInfo == null) return;
        clientInfo.id = this.id;
        clientInfo.created = dtStart;
        clientInfo.cacheSize = hashCache.size();
        if (timeLastGetMessages > 0) {
            clientInfo.lastGetMessageOnServer = new OADateTime(timeLastGetMessages);
        }
        clientInfo.ended = dtEnd;
        clientInfo.connectionStatus = STATUS[status];
        clientInfo.msgSent = cntMsgSent;
        clientInfo.msgReceived = cntMsgReceived;
        if (dtEnd == null) clientInfo.serverQueueSize = (int) (oaServer.getQueueLoadPos() - this.queueLoadPos);
        else clientInfo.serverQueueSize = 0;
    }

    protected String asString() {
        return getClientInfo().asString();
    }

    protected String[] asStrings() {
        if (clientInfo == null) return null;
        return getClientInfo().asStrings();
    }

    /**
     * Called by RMI when disconnected.
     * RMI callback.
     */
    @Override
    public void unreferenced() {
        if (bConnected) LOG.warning("bConnected is still true, but object is being unreferenced.  will disconnect client " + id);
        disconnected();
    }

    private static ConcurrentHashMap<String, Object> hmRemoteMethodImpl = new ConcurrentHashMap<String, Object>();

    /**
     * Works with OAClient.getProxy() to implment remote method calls.
     * @param obj the Implementation of the interface used by OAClient.getProxy(class)
     * @see OAClient#getProxy(Class) used on client to call methods on object.
     */
    public void bind(String objectName, Object obj) {
        LOG.fine(String.format("Bind object, clientId=%d, name=%s, object=%s", id, objectName, obj));
        hmRemoteMethodImpl.put(objectName, obj);
    }

    public void unbind(String objectName) {
        LOG.fine(String.format("Unbind object, clientId=%d, name=%s", id, objectName));
        hmRemoteMethodImpl.remove(objectName);
    }

    public Object remoteMethodCall(String objectName, String methodName, Object[] arguments) throws RemoteException {
        LOG.fine(String.format("clientId=%d, objectName=%s, methodName=%s", getId(), objectName, methodName));

        // make sure all changes are broadcast to clients
        OAClientDelegate.processIfServer();

        Object obj = hmRemoteMethodImpl.get(objectName);
        if (obj == null) {
            return oaServer.remoteMethodCall(this, objectName, methodName, arguments);
        }
        try {
            Method method = OAReflect.getMethod(obj.getClass(), methodName, arguments);
            method.setAccessible(true);
            Object result = method.invoke(obj, arguments);
            return result;
        }
        catch (Exception e) {
            throw new RemoteException("Remote exception for objectName=" + objectName + ", method=" + methodName, e);
        }
    }


    /** 20130213
     *  getDetail() requirements
     * load referencs for master object and detail object/hub, and one level of ownedReferences
     * serialize all first level references for master, and detail 
     * send existing references for 1 more level from master, and 2 levels from detail
     * dont send amy references that equal master or have master in the hub
     * dont send any references that have detail/hub in it
     * dont send detail if it has already been sent with all references
     * dont send a reference if it has already been sent to client, and has been added to tree
     * 
     */
    protected OAObjectSerializer getSerializedDetail(final OAObject masterObject, final Object detailObject, final String propFromMaster, final String[] masterProperties, final OAObjectKey[] siblingKeys) {
        // at this point, we know that the client does not have all of the master's references,
        // and we know that value != null, since getDetail would not have been called.
        // include the references "around" this object and master object, along with any siblings
        
        if (masterObject instanceof OAObject) {
            OAObjectReflectDelegate.loadAllReferences((OAObject) masterObject, false);
        }

        int guid = OAObjectKeyDelegate.getKey(masterObject).getGuid();
        rwLockTreeSerialized.readLock().lock();
        Object objx = treeSerialized.get(guid);
        rwLockTreeSerialized.readLock().unlock();
        boolean b = objx != null && ((Boolean) objx).booleanValue();
        final boolean bMasterWasPreviouslySent = b;

        
        Hub dHub = null;
        if (detailObject instanceof OAObject) {
            guid = OAObjectKeyDelegate.getKey((OAObject) detailObject).getGuid();
            rwLockTreeSerialized.readLock().lock();
            objx = treeSerialized.get(guid);
            rwLockTreeSerialized.readLock().unlock();
            b = objx != null && ((Boolean) objx).booleanValue();
            if (!b) {
                OAObjectReflectDelegate.loadAllReferences((OAObject) detailObject, 1, 1, false);
            }
        }
        else if (detailObject instanceof Hub) {
            dHub = (Hub) detailObject;
            if (dHub.isOAObject()) {
                int cnt = 0;
                for (Object obj : dHub) {
                    guid = OAObjectKeyDelegate.getKey((OAObject) obj).getGuid();
                    rwLockTreeSerialized.readLock().lock();
                    objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();
                    b = objx != null && ((Boolean) objx).booleanValue();
                    if (b) continue;
                    
                    b = OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false);
                    if (!b) {
                        if (++cnt < 25) {
                            OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 1, 1, false);
                        }
                        else {
                            OAObjectReflectDelegate.loadAllReferences((OAObject) obj, 0, 1, false);
                        }
                        if (cnt > 50) break;
                    }
                }
            }
        }
        final Hub detailHub = dHub;

        ArrayList<OAObject> al = null;
        if (siblingKeys != null) {
            al = new ArrayList<OAObject>(siblingKeys.length+1);
            for (OAObjectKey key : siblingKeys) {
                Class c = masterObject.getClass();
                OAObject obj = OAObjectCacheDelegate.get(c, key);
                if (obj != null) {
                    al.add(obj);
                    OAObjectReflectDelegate.getProperty(obj, propFromMaster);
                }
            }
        }
        final ArrayList<OAObject> alExtraData = al;
        
        OAObjectSerializer os = new OAObjectSerializer(detailObject, true);
        if (alExtraData != null) {
            if (detailObject == null) alExtraData.add(masterObject); // so master can go 
            os.setExtraObject(alExtraData); 
        }
        else {
            if (detailObject == null) os.setExtraObject(masterObject);  // so master can be sent to client
            else if (!(detailObject instanceof Hub)) os.setExtraObject(masterObject); 
        }

        
        OAObjectSerializerCallback callback = new OAObjectSerializerCallback() {                    
            boolean bMasterSent;
            @Override
            protected void setup(OAObject obj) {

                // parent object - will send all references
                if (obj == masterObject) {
                    if (bMasterSent) {
                        excludeAllProperties();
                        return;
                    }
                    bMasterSent = true;
                    if (bMasterWasPreviouslySent) {
                        excludeAllProperties();
                        return;
                    }
                    
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();
                    rwLockTreeSerialized.writeLock().lock();
                    treeSerialized.put(guid, true);
                    rwLockTreeSerialized.writeLock().unlock();
                    if (masterProperties == null || masterProperties.length == 0) {
                        includeAllProperties();
                    }
                    else includeProperties(masterProperties);                    
                    return;
                }

                if (obj == detailObject) {
                    int level = this.getLevelsDeep();  // obj is pushed to stack, and level is changed after setup() is called
                    if (level > 0) {
                        excludeAllProperties(); // already sent in this batch
                        return;
                    }

                    if (bMasterWasPreviouslySent) {
                        // already had all of master, this is only for a calculated prop
                        excludeAllProperties();
                        return;
                    }
                    
                    // this Object - will send all references (all have been loaded)
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                    rwLockTreeSerialized.readLock().lock();
                    Object objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();
                    boolean b = objx != null && ((Boolean) objx).booleanValue();
                    if (b) {
                        excludeAllProperties(); // already sent
                    }
                    else {
                        rwLockTreeSerialized.writeLock().lock();
                        b = OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false);
                        treeSerialized.put(guid, b);
                        rwLockTreeSerialized.writeLock().unlock();
                        includeAllProperties();
                    }
                    return;
                }

                if (detailHub != null && detailHub.contains(obj)) {
                    // this Object is a Hub - will send all references (all have been loaded)
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                    rwLockTreeSerialized.readLock().lock();
                    Object objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();
                    boolean b = objx != null && ((Boolean) objx).booleanValue();

                    if (b) {
                        excludeAllProperties();  // client has it all
                    }
                    else {
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, true);
                        rwLockTreeSerialized.writeLock().unlock();
                        includeAllProperties();
                    }
                    return;
                }

                // for siblings, only send the reference property for now
                if (alExtraData != null && alExtraData.contains(obj)) {
                    // sibling object either is not on the client or does not have all references
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();
                    rwLockTreeSerialized.writeLock().lock();
                    treeSerialized.put(guid, false); 
                    rwLockTreeSerialized.writeLock().unlock();
                    includeProperties(new String[] {propFromMaster});
                    return;
                }
                
                
                // second level object - will send all references that are already loaded
                Object objPrevious = this.getPreviousObject();
                boolean b = (objPrevious == detailObject);
                if (!b && objPrevious == masterObject) b = true; 
                if (!b) b = (detailHub != null && (objPrevious != null && detailHub.contains(objPrevious)));
                
                if (b && !bMasterWasPreviouslySent) {
                    int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                    rwLockTreeSerialized.readLock().lock();
                    Object objx = treeSerialized.get(guid);
                    rwLockTreeSerialized.readLock().unlock();

                    if (objx != null) {
                        excludeAllProperties();  // client already has it, might not be all of it
                    }
                    else {
                        // client does not have it, send whatever is loaded
                        b = OAObjectReflectDelegate.areAllReferencesLoaded((OAObject) obj, false);
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, b);
                        rwLockTreeSerialized.writeLock().unlock();
                        includeAllProperties(); // will send whatever is loaded
                    }
                    return;
                }
                
                // "leaf" reference that client does not have, only include owned references
                int guid = OAObjectKeyDelegate.getKey(obj).getGuid();

                rwLockTreeSerialized.readLock().lock();
                Object objx = treeSerialized.get(guid);
                rwLockTreeSerialized.readLock().unlock();

                if (objx == null) {  // never sent to client
                    rwLockTreeSerialized.writeLock().lock();
                    treeSerialized.put(guid, false); // flag it as: object has been sent, but not references
                    rwLockTreeSerialized.writeLock().unlock();
                }
                excludeAllProperties();
            }

            /* this is called when a reference has already been included, by the setup() method.
             * this will see if the object already exists on the client to determine if it will
             * be sent.  Otherwise, oaobject.writeObject will only send the oaKey, so that it will
             * be matched up on the client. 
             */
            @Override
            public boolean shouldSerializeReference(OAObject oaObj, String propertyName, Object obj, boolean bDefault) {
                if (!bDefault) return false;
                if (obj == null) return false;
                
                if (oaObj == masterObject) return true;
                if (oaObj == detailObject) return true;
                if (alExtraData != null && alExtraData.contains(oaObj)) {
                    // sibling object only "ask" for propertyName
                    return true; // propFromMaster.equals(propertyName);
                }
                
                if (obj instanceof Hub) {
                    Hub hub = (Hub) obj;
                    if (hub.getSize() == 0) return false;
                    
                    // dont include hubs with masterObject in it, so that it wont be sending sibling data for masterObj
                    if (hub.contains(masterObject)) {
                        int guid = OAObjectKeyDelegate.getKey(oaObj).getGuid();
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, false); // it might have been flagged as true
                        rwLockTreeSerialized.writeLock().unlock();
                        return false;  
                    }

                    // dont send other sibling data
                    if (detailObject != null && detailHub == null && hub.contains(detailObject)) {
                        int guid = OAObjectKeyDelegate.getKey(oaObj).getGuid();
                        rwLockTreeSerialized.writeLock().lock();
                        treeSerialized.put(guid, false); // it might have been flagged as true, need to unflag since this prop wont be sent
                        rwLockTreeSerialized.writeLock().unlock();
                        return false;  
                    }

                    // this will do a quick test to see if this is a Hub with any of the same objects in it.
                    if (detailHub != null) {
                        if (!detailHub.getObjectClass().equals(hub.getObjectClass())) {
                            return true;
                        }
                        Hub h1, h2;
                        if (detailHub.getSize() > hub.getSize()) {
                            h1 = hub;
                            h2 = detailHub;
                        }
                        else {
                            h1 = detailHub;
                            h2 = hub;
                        }
                        for (int i=0; i<3; i++) {
                            Object objx = h1.getAt(i);
                            if (objx == null) break;
                            if (h2.contains(objx)) {
                                int guid = OAObjectKeyDelegate.getKey(oaObj).getGuid();
                                rwLockTreeSerialized.writeLock().lock();
                                treeSerialized.put(guid, false); // it might have been flagged as true
                                rwLockTreeSerialized.writeLock().unlock();
                                return false;
                            }
                        }
                    }
                    return true;
                }

                if (!(obj instanceof OAObject)) return true;
                
                if (obj == masterObject) {
                    if (bMasterSent) return false;
                    int level = this.getLevelsDeep();
                    if (level > 1) return false; // wait for it to be saved at correct position
                    return true;
                }

                if (obj == detailObject) return false;  // only save as begin obj
                if (detailHub != null && detailHub.contains(obj)) return false; // only save as begin obj

                
                int guid = OAObjectKeyDelegate.getKey((OAObject) obj).getGuid();
                rwLockTreeSerialized.readLock().lock();
                Object objx = treeSerialized.get(guid);
                rwLockTreeSerialized.readLock().unlock();
                boolean b = objx != null && ((Boolean) objx).booleanValue();
                if (b) {
                    return false; // already sent with all refs
                }
                
                int level = this.getLevelsDeep();
                if (level < 3) return true;
                return objx == null;
            }
        };
        os.setCallback(callback);
        return os;
    }
    
}
