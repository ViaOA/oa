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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

import java.lang.reflect.Method;
import java.rmi.*;
import java.rmi.server.*;


import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.annotation.RemoteInterface;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

/** 
     OAServer is a Distributed Object used for creating Client/Server applications that automatically
     stay synchronized, and supports distributed method calls, database access, and more.  
     <p>
     OAServer is responsible for creating and managing OAObjectServer objects for each connection.  OAServer 
     sends and receives messages from OAObjectServer.
*/
public class OAServerImpl implements OAServerInterface {
    private static Logger LOG = Logger.getLogger(OAServerImpl.class.getName());
    
    Vector<OAObjectServerInterface> vecObjectServer = new Vector<OAObjectServerInterface>();  // keeps all OAObjectServer objects created for this jvm instance.
    OAObjectPublisher publisher;

    private Object LOCKQueue = new Object();
    private static int QueueSize = 15000;
    private OAObjectMessage[] msgQueue = new OAObjectMessage[QueueSize];
    
    private volatile long queueLoadPos;
    private long queueWaitCount;
    private int queueNextSequence;
    private volatile int queueReset;  // incremented whenever the queueLoadPos is changed
    private OAObjectMessage msgErrorOverrun;  // sent to clients if they are too far behind on the queue
    private boolean bHoldSend;
    private int sendCount;

    // used by readMessages
    private int readCount;
    private int readWaitCount;
    private int readMultilpleCount;  // number of reads that were able to get multiple messages
    
    public OAServerImpl()  {
        LOG.config("queueSize="+QueueSize);
        OAObjectCacheDelegate.setDefaultAddMode(OAObjectCacheDelegate.IGNORE_DUPS);
        serverImpl = this;
    }

    private static OAServerImpl serverImpl;
    public static OAServerImpl getOASeverImpl() {
        return serverImpl;
    }
    
    
    public String test() throws RemoteException {
        LOG.fine("called");
        return "OAServerImpl.test() called";
    }

    /**
        Create a new OAObjectServer object for an OAClient.  This is automatically created by
        OAClient.
    */
    public OAObjectServerInterface getOAObjectServer() throws RemoteException {
        OAObjectServerImpl os = new OAObjectServerImpl(this);
        
        os.queueLoadPos = this.queueLoadPos;
        
        LOG.config("OAServer.getOAObjectServer() New Client Connection #"+os.getId()+" queuePos="+os.queueLoadPos);
        
        vecObjectServer.addElement(os);
        if (publisher != null) publisher.addUser(os);
        
        return os;
    }

    public void setHoldSendMessages(boolean b) {
        LOG.info("called");
        synchronized(vecObjectServer) {
            bHoldSend = b;
            vecObjectServer.notify();
        }
    }

    protected OAObjectServerImpl getOAObjectServer(int id) {
        //LOG.fine("id="+id);
        synchronized(vecObjectServer) {
            int x = vecObjectServer.size();
            for (int i=0; i<x; i++) {
                OAObjectServerImpl os = (OAObjectServerImpl) vecObjectServer.elementAt(i);
                if (os == null) continue;
                if (os.id == id) return os;
            }
        }
        return null;
    }

    /** 20120403
     * This will save all OAObjects that are in the cache, which are objects that
     * are not reachable from object graph (ServerRoot, ClientRoots)
     * @param cascade used from save
     * @param iCascadeRule used from save
     */
    public void saveCache(OACascade cascade, int iCascadeRule) {
        OAObjectServerImpl[] oos = new OAObjectServerImpl[0];
        oos = vecObjectServer.toArray(oos);
        
        for (OAObjectServerImpl os : oos) {
            os.updateCache(cascade);
        }
        for (OAObjectServerImpl os : oos) {
            os.saveCache(cascade, iCascadeRule);
        }
    }

    
    public OAClientInfo[] getClientInfos() {
        
        // disconnect user if they have not recvd msg in last 5 mintues
       
        OAClientInfo[] cis; 
        synchronized (vecObjectServer) {
            int x = vecObjectServer.size();
            cis = new OAClientInfo[x]; 
            long timeout = System.currentTimeMillis() + (5 * 60 * 1000);
            for (int i=0; i<x; i++) {
                OAObjectServerImpl os = (OAObjectServerImpl) vecObjectServer.elementAt(i);
                if (os == null) continue;

                // check for disconnects
                if (os.bConnected && !os.bGettingMessagesNow) {
                    if (os.timeLastGetMessages > 0 && os.timeLastGetMessages < timeout) {
                        try {
LOG.fine("timeout, timeLastGetMessages=" + os.timeLastGetMessages + ", timeout=" + timeout + ", will NOT disconnect user, os.id="+os.id);
//qqqq                            os.disconnected();
                        }
                        catch (Exception e) {
                        }
                    }
                }
                
                cis[i] = os.getClientInfo();
                os.updateClientInfo();
            }
        }
        return cis;
    }
    protected long getQueueLoadPos() {
        return queueLoadPos;
    }

    
    /** called by OAObjectServer.sendMessage() to send message to other clients */
    protected void sendMessage(OAObjectServerImpl objServ, OAObjectMessage msg) {
        sendCount++;

        // LOG.finer("cnt="+sendCount+", id="+objServ.id+", msg="+msg);
        for (;bHoldSend;) {
            try {
                synchronized(vecObjectServer) {
                    if (bHoldSend) vecObjectServer.wait(500);
                }
            }
            catch (InterruptedException e) {
            }
        }

        synchronized (LOCKQueue) {
            if (msg.bPrivate) {
                msg.seq = queueNextSequence;  // use last seq for private messages
            }
            else {
                msg.seq = queueNextSequence++;
                if (queueNextSequence == Integer.MAX_VALUE) queueNextSequence = 0;
            }

            int pos = (int) (queueLoadPos%QueueSize);
            msgQueue[pos] = msg;
            queueLoadPos++;
            if (queueLoadPos == Long.MAX_VALUE) {
                queueLoadPos = QueueSize + (queueLoadPos % QueueSize);
                queueReset++;
            }
            if (queueWaitCount > 0) {
                LOCKQueue.notifyAll();
            }
        }
    }

    private volatile long queueGCPos;
    public void performQueueGC() {
        long min = queueLoadPos;
        OAObjectServerImpl[] oos = new OAObjectServerImpl[0];
        synchronized(vecObjectServer) {
            oos = vecObjectServer.toArray(oos);
        }
        for (OAObjectServerImpl os : oos) {
            min = Math.min(min, os.queueLoadPos);
        }
        for ( ;queueGCPos < min; queueGCPos++) {
            msgQueue[(int) (queueGCPos % QueueSize)] = null;
        }
    }
    
    
    OAObjectMessage[] getMessages(OAObjectServerImpl os) {
        // LOG.finer("os.id="+os.id);
        if (queueReset != os.queueReset) {
            int x = (int) ((os.queueLoadPos % QueueSize) + QueueSize);
            os.queueLoadPos = x - (Long.MAX_VALUE - os.queueLoadPos);                
            os.queueReset = queueReset;
        }

        int amt = (int) (this.queueLoadPos - os.queueLoadPos);
        if (amt > QueueSize || (os.status != OAObjectServerImpl.STATUS_CONNECTED)) {
            if (msgErrorOverrun == null) {
                msgErrorOverrun = new OAObjectMessage();
                msgErrorOverrun.type = OAObjectMessage.ERROR;
                msgErrorOverrun.pos = OAObjectMessage.ERROR_ServerQueueOverrun;
            }
            if (os.status == OAObjectServerImpl.STATUS_CONNECTED) {
                os.status = OAObjectServerImpl.STATUS_QUEUEOVERRUN;
            }
            else {
                try {
                    os.close();
                }
                catch (RemoteException re) {}
            }
            return new OAObjectMessage[] {msgErrorOverrun};
        }
        
        int qpos = (int)(os.queueLoadPos%QueueSize);
        if (qpos + amt >= QueueSize) {
            amt = (QueueSize - qpos);  // dont exceed end of buffer, need to wrap back to 0 on next call
        }
        amt = Math.min(amt, 250);

        boolean bIsServerClient = OAClientDelegate.isLocal(os);
        
        ArrayList<OAObjectMessage> al = new ArrayList<OAObjectMessage>(amt);
        int pos = 0;
        for ( ; pos<amt; pos++) {
            OAObjectMessage m = msgQueue[qpos+pos];

            if (m.bPrivate) {
                if (m.objectServerId == os.id) {
                    amt = Math.min(pos + 5, amt);
                }
                else if (!bIsServerClient) {
                    continue;
                }
            }

            if (!bIsServerClient && !m.bAppliedOnServer) {
                if (al.size() > 0) break;
                synchronized (m) {
                    for ( ; !m.bAppliedOnServer; ) {
                        try {
                            m.wait();
                        }
                        catch (Exception e) {}
                    }
                }
            }
            al.add(m);
        }

        os.queueLoadPos += pos;
        OAObjectMessage[] msgs = null;
        int x = al.size();
        if (x > 0) {
            readCount += x;
            if (x > 1) readMultilpleCount++;
            msgs = new OAObjectMessage[x];
            al.toArray(msgs);
        }
        else {
            // no messages available, wait to be notified, and then return null, so that it will be retried
            synchronized (LOCKQueue) {  
                if (os.queueLoadPos == this.queueLoadPos) {
                    try {
                        readWaitCount++;  // total times wait was done
                        queueWaitCount++;
                        LOCKQueue.wait();
                    }
                    catch (Exception e) {
                    }
                    finally {
                        queueWaitCount--;
                    }
                }
            }
        }
        return msgs;
    }
    
    protected void onClose(OAObjectServerImpl objectServer) {
        LOG.config("client connection closed, connection "+objectServer.id);
        if (publisher != null) publisher.removeUser(objectServer, objectServer.user);
        vecObjectServer.removeElement(objectServer);
    }
    
    /** called by OAObjectServer only */
    protected void lock(OAObjectServerImpl objServ, Object object, Object miscObject) {
        LOG.fine("object="+object);
        OAObjectLockDelegate.lock(object,null, miscObject);
        objServ.vecLock.addElement(object);
    }
        
    /** called by OAObjectServer only */
    protected void unlock(OAObjectServerImpl objServ, Object object) {
        LOG.fine("object="+object);
        OAObjectLockDelegate.unlock(object);
        objServ.vecLock.removeElement(object);
    }
        
    /** called by OAObjectServer only */
    protected boolean isLocked(Object object) {
        LOG.fine("object="+object);
        return OAObjectLockDelegate.isLocked(object);
    }

    /** called by OAObjectServer only */
    protected OALock getLock(Object object) {
        LOG.fine("object="+object);
        return OAObjectLockDelegate.getLock(object);
    }

    /** called by OAObjectServer only */
    protected Object[] getAllLockedObjects() {
        LOG.fine("called");
        return OAObjectLockDelegate.getAllLockedObjects();
    }
    
    //============== Publisher Method ====================    
    public void setPublisher(OAObjectPublisher pub) {
        LOG.config("publisher="+pub);
        if (publisher != null) throw new RuntimeException("OAServer.setPublisher() Publisher has already been assigned");
        this.publisher = pub;
    }

    
    private static ConcurrentHashMap<String, Object> hmRemoteMethodImpl = new ConcurrentHashMap<String, Object>();
    /**
     * Works with OAClient.getProxy() to implment remote method calls.
     * @param obj the Implementation of the interface used by OAClient.getProxy(class)
     * @see OAClient#getProxy(Class) used on client to call methods on object.
     */
    public void bind(String objectName, Object obj) {
        LOG.fine(String.format("Bind object, name=%s, object=%s", objectName, obj));
        hmRemoteMethodImpl.put(objectName, obj);
    }
    public void unbind(String objectName) {
        LOG.fine(String.format("Unbind object, name=%s", objectName));
        hmRemoteMethodImpl.remove(objectName);
    }

    public Object remoteMethodCall(OAObjectServerImpl os, String objectName, String methodName, Object[] arguments) throws RemoteException {
        LOG.fine(String.format("clientId=%d, objectName=%s, methodName=%s", os.getId(), objectName, methodName));

        Object obj = hmRemoteMethodImpl.get(objectName);
        
        if (obj == null) throw new RemoteException("Remote object not found for objectName="+objectName);
        try {
            Method method = OAReflect.getMethod(obj.getClass(), methodName);
            method.setAccessible(true);
            Object result = method.invoke(obj, arguments);
            return result;
        }
        catch (Exception e) {
            throw new RemoteException("Remote exception for objectName="+objectName+", method="+methodName, e);
        }
    }
    
    
//qqqqqqqqqqqqqqqqqqq  register Impl class(es) qqqqqqqqqqqqqq etc
    public void getInfo(Vector vec, boolean bIncludeClosed) {
        //LOG.fine("called");
        int x = vecObjectServer.size();
        if (bHoldSend) vec.add("Queue is currently on HOLD");
        
        
        vec.add("Message Sends = " + OAString.format(sendCount,"#,###"));
        vec.add("Message Reads = " + OAString.format(readCount, "#,###"));
        vec.add("Message Read Multiples = " + OAString.format(readMultilpleCount,"#,###"));
        vec.add("Message Read Wait Count = " + OAString.format(readWaitCount,"#,###"));
        vec.add("Current QueueLoadPos = " + OAString.format(queueLoadPos,"#,###"));
        
        for (int i=0; i<x; i++) {
            OAObjectServerImpl os = (OAObjectServerImpl) vecObjectServer.elementAt(i);
            if (os != null && (bIncludeClosed || os.bConnected)) {
                try {
                    os.updateClientInfo();
                    String[] ss = os.asStrings();
                    if (ss == null) vec.add("User "+os.getId()+" has not been initialized from client");
                    for (int j=0; ss != null && j<ss.length; j++) {
                        vec.addElement(ss[j]);
                    }
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "Error in OAServerImpl getInfo",e);
                }
            }
        }
    }
}


/*qqqqqqqqqqqq Playback qqqqqqqqqqqqqq
 qqqqqqqqqqqqqqqqqq
    private ObjectOutputStream outStream;
    public void setRecordFile(String fname) throws Exception {
        outStream = new ObjectOutputStream(new FileOutputStream(fname));
    }

    public void playback(String fname) throws Exception {
        ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(fname));
        for (;;) {
            OAObjectMessage msg = (OAObjectMessage) inStream.readObject();
            if (msg.bPrivate) continue;
            sendMessage(null, msg);
        }
    }


    try {
        if (outStream != null) {
            boolean b = (msg.type == OAObjectMessage.CREATENEWOBJECT || msg.type == OAObjectMessage.DELETE);
            if (!b && msg.type == OAObjectMessage.PROPERTY_CHANGE) {
                b = (msg.property != null && !msg.property.equalsIgnoreCase("changed")); 
            }            
            if (b) {
                System.out.println("writing OAServerImpl cnt="+sendCount+", id="+objServ.id+", msg="+msg);
                outStream.writeObject(msg);
            }
        }
    }
    catch (Exception e) {
        System.out.println("Exception in OAServerImpl: "+e);
    }

**/
