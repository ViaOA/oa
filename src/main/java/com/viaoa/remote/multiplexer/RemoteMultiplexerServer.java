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
package com.viaoa.remote.multiplexer;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.comm.multiplexer.io.VirtualServerSocket;
import com.viaoa.comm.multiplexer.io.VirtualSocket;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.BindInfo;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.io.RemoteObjectInputStream;
import com.viaoa.remote.multiplexer.io.RemoteObjectOutputStream;
import com.viaoa.util.OACircularQueue;
import com.viaoa.util.OACompressWrapper;
import com.viaoa.util.OAReflect;

/*** DEBUGing 
 *  use this for debugging, so that remote methods wont timeout while debugging:  
 *      MultiplexerServer.DEBUG = true;
 */

/**
 * Server component used to allow remoting method calls with Clients. Uses a MultiplexerServer for
 * communication with clients.
 * <p>
 * <ol>
 * Different ways to create a remote object:
 * <li>Server can bind an Object so that clients can then do a lookup to get the object, and all method
 * calls will be invoked on the server.
 * <li>A method that has a remote class parameter. This can be used by client or server - where a method
 * argument is a remote object.
 * <li>A method returns a remote class. This can be used by client or server - where a method returns a
 * remote object.
 * <li>The server can create a single remote object, that will then "broadcast" to all clients that have
 * it.
 * </ol>
 *
 * Note:
 * OARemoteThread is used to process requests. 
 * 
 * @author vvia
 */
public class RemoteMultiplexerServer {
    private static Logger LOG = Logger.getLogger(RemoteMultiplexerServer.class.getName());

    private MultiplexerServer multiplexerServer;

    // internally used for lookup for Client to Server remoting 
    private VirtualServerSocket ssCtoS;
    // internally used for lookup for Server to Client remoting 
    private VirtualServerSocket ssStoC;

    // used to uniquely identify remote objects 
    private AtomicInteger aiBindCount = new AtomicInteger();

    /**
     * Used to manage all remote objects.
     */
    private ConcurrentHashMap<String, BindInfo> hmNameToBind = new ConcurrentHashMap<String, BindInfo>();
    // used to manage GC for remote objects.  See performDGC.
    private ReferenceQueue referenceQueue = new ReferenceQueue();

    // used to hold all objects from the "bind()" method, so that they will not get gc'd
    private ConcurrentHashMap<BindInfo, Object> hmBindObject = new ConcurrentHashMap<BindInfo, Object>();

    // used for queued messages 
    private ConcurrentHashMap<String, OACircularQueue<RequestInfo>> hmAsyncCircularQueue = new ConcurrentHashMap<String, OACircularQueue<RequestInfo>>();

    // track connections
    private ConcurrentHashMap<Integer, Session> hmSession = new ConcurrentHashMap<Integer, Session>();

    
    /**
     * Create a new RemoteServer using multiplexer.
     * 
     * @see MultiplexerServer#start() to have the server allow for client connections.
     * @see #start() to have this server start recieving remote calls.
     */
    public RemoteMultiplexerServer(MultiplexerServer server) {
        this.multiplexerServer = server;
    }

    public MultiplexerServer getMultiplexerServer() {
        return this.multiplexerServer;
    }
    
    /**
     * This can be called when MultiplexerServer.onClientDisconnect(..) is called. If this is not called,
     * then the next socket.IO method will throw an IOException.
     * 
     * @see MultiplexerServer#onClientDisconnect
     */
    public void removeSession(int connectionId) {
        LOG.fine("removing session, connectionId="+connectionId);
        Session s = hmSession.remove(connectionId);
        if (s != null) {
            s.onDisconnect();
        }
    }
    
    /**
     * This can be called when MultiplexerServer.onClientConnect(..) is called.
     * 
     * @see MultiplexerServer#onClientConnect
     */
    public void createSession(Socket socket, int connectionId) {
        Session session = getSession(connectionId, true);
        session.realSocket = socket;
    }

    public Session getSession(int connectionId, boolean bCreateIfNull) {
        Session session = hmSession.get(connectionId);
        if (session == null && bCreateIfNull) {
            session = new Session();
            session.connectionId = connectionId;
            hmSession.put(connectionId, session);
            LOG.fine("create session, connectionId="+connectionId);
        }
        return session;
    }
    
    
    /**
     * starts serverSockets for remote messages.
     * 
     * @see MultiplexerServer#start() to have the server allow for client connections.
     */
    public void start() throws Exception {
        startServerSocketForCtoS();
        startServerSocketForStoC();
    }

    // manages client to server messages
    protected void startServerSocketForCtoS() throws Exception {
        if (ssCtoS != null) return;
        ssCtoS = multiplexerServer.createServerSocket("CtoS");

        // accept new connections
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                    try {
                        Socket socket = ssCtoS.accept();
                        onNewConnectionForCtoS(socket);
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception on new CtoS socket", e);
                    }
                }
            }
        });
        t.setName("OAServerSocket_CtoS");
        t.setDaemon(true);
        t.start();
        //LOG.config("created Client to Server serversocket thread");
    }

    // new vsocket connection for client to server messages
    protected void onNewConnectionForCtoS(Socket socket) {
        final VirtualSocket vSocket = (VirtualSocket) socket;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processSocketCtoS(vSocket);
                }
                catch (Exception e) {
                    if (!vSocket.isClosed()) {
                        LOG.log(Level.WARNING, "error processing socket request", e);
                    }
                }
            }
        });
        t.setName("OASocket_CtoS." + vSocket.getConnectionId() + "." + vSocket.getId());
        t.setDaemon(true);
        t.start();
    }

    protected void processSocketCtoS(final VirtualSocket socket) throws Exception {
        final int socketId = socket.getId();
        final int connectionId = socket.getConnectionId();
        final Session session = getSession(connectionId, true);
        
        for (;;) {
            if (socket.isClosed()) break;

            RequestInfo ri = new RequestInfo();
            ri.socket = socket;
            ri.connectionId = ri.socket.getConnectionId();
            ri.vsocketId = socketId;
            
            boolean b = _processSocketCtoSRequest(ri, session);
            ri.nsEnd = System.nanoTime();
            
            aiReceivedMethodCallCnt.incrementAndGet();            
            if (b) {
                afterInvokeForCtoS(ri);
            }
        }
    }

    private boolean _processSocketCtoSRequest(final RequestInfo ri, final Session session) throws Exception {
        RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, session.hmClassDescInput);

        // wait for next message
        ri.type = RequestInfo.getType(ois.readByte());
        // 1:CtoS_QueuedRequest recv from client
        // 1:CtoS_QueuedRequestNoResponse
        
        ri.nsStart = System.nanoTime();
        ri.msStart = System.currentTimeMillis();

        if (ri.type == RequestInfo.Type.CtoS_GetLookupInfo) {
            // lookup, needs to return Java Interface class.
            ri.bindName = ois.readAsciiString();
            BindInfo bind = getBindInfo(ri.bindName);
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, session.hmClassDescOutput, session.aiClassDescOutput);
            if (bind != null) {
                ri.response = new Object[] { bind.interfaceClass, bind.usesQueue, bind.isBroadcast };
                if (bind.usesQueue) {
                    session.setupAsyncQueueSender(bind.asyncQueueName);
                }
                oos.writeBoolean(true); // valid response
                oos.writeObject(ri.response);
            }
            else {
                ri.exceptionMessage = "object not found";
                oos.writeBoolean(false);
                oos.writeObject(ri.exceptionMessage);
            }
            oos.flush();
            return true;
        }
        if (ri.type == RequestInfo.Type.CtoS_GetBroadcastClass) {
            ri.bindName = ois.readAsciiString();
            BindInfo bind = getBindInfo(ri.bindName);
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, session.hmClassDescOutput, session.aiClassDescOutput);
            if (bind != null) {
                if (!bind.isBroadcast) {
                    ri.exceptionMessage = "found, but not a broadcast remote object";
                    oos.writeBoolean(false);
                    oos.writeObject(ri.exceptionMessage);
                }
                else {
                    ri.response = bind.interfaceClass;
                    oos.writeBoolean(true);
                    oos.writeObject(ri.response);
                    session.setupAsyncQueueSender(bind.asyncQueueName);
                }
            }
            else {
                ri.exceptionMessage = "object not found";
                oos.writeBoolean(false);
                oos.writeObject(ri.exceptionMessage);
            }
            oos.flush();
            return true;
        }
        if (ri.type == RequestInfo.Type.CtoS_RemoveSessionBroadcastThread) {
            // remove StoC thread used for broadcast object
            ri.bindName = ois.readAsciiString();
            session.removeBindInfo(ri.bindName);
            return true;
        }

        // reading based on type
        if (ri.type == RequestInfo.Type.CtoS_SocketRequest) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
        }
        else if (ri.type == RequestInfo.Type.CtoS_SocketRequestNoResponse) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
        }
        else if (ri.type == RequestInfo.Type.CtoS_ReturnOnQueueSocket) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.messageId = ois.readInt();
        }
        else if (ri.type == RequestInfo.Type.CtoS_QueuedRequest) {
            // 2:CtoS_QueuedRequest read from client
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.messageId = ois.readInt();
        }
        else if (ri.type == RequestInfo.Type.CtoS_QueuedRequestNoResponse) {
            // 2:CtoS_QueuedRequestNoResponse
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
        }
        else if (ri.type == RequestInfo.Type.CtoS_QueuedResponse) {
            ri.messageId = ois.readInt();
            byte b = ois.readByte();
            Object objx = ois.readObject();
            if (b == 0) ri.exception = (Exception) objx;
            else if (b == 1) ri.exceptionMessage = (String) objx;
            else {
                ri.response = objx;
            }
        }
        else if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.connectionId = ois.readInt();
            ri.messageId = ois.readInt();
        }
        
        if (ri.bindName != null) {
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.bind = session.getBindInfo(ri.bindName);
            }
            if (ri.bind == null) {
                ri.exceptionMessage = "bind Object not found on server";
            }
            else {
                ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
                if (ri.methodInfo != null) ri.method = ri.methodInfo.method;
                if (ri.method == null) {
                    ri.exceptionMessage = "method not found";
                }
            }
        }

        // processing based on type
        if (ri.type == RequestInfo.Type.CtoS_SocketRequest) {
            // send back on same socket, in same thread
            if (ri.exceptionMessage == null) {
                invokeUsingRemoteThread(ri, false);
            }
            
            Object resp = null;
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, session.hmClassDescOutput, session.aiClassDescOutput);
            if (ri.exception != null) {
                if (ri.exception instanceof Serializable) {
                    resp = ri.exception;
                }
                else resp = new Exception(ri.exception.toString());
                oos.writeByte(0);
            }
            else if (ri.exceptionMessage != null) {
                resp = ri.exceptionMessage;
                oos.writeByte(1);
            }
            else if (ri.responseBindName != null) {
                oos.writeByte(2);
                resp = new Object[] { ri.responseBindName, ri.responseBindUsesQueue };
            }
            else {
                oos.writeByte(3);
                resp = ri.response;
            }
            oos.writeObject(resp);
            oos.flush();
            return false;            
        }
        
        if (ri.type == RequestInfo.Type.CtoS_SocketRequestNoResponse) {
            if (ri.exceptionMessage != null) return true;
            invokeUsingRemoteThread(ri, false);
            return false;            
        }

        // 20151129 invoke now, return result using the queue thread
        if (ri.type == RequestInfo.Type.CtoS_ReturnOnQueueSocket) {
            if (ri.exceptionMessage == null) {
                invokeUsingRemoteThread(ri, false);
            }
            if (ri.exceptionMessage != null) {
                ri.methodInvoked = true;
            }
            session.setupAsyncQueueSender(ri.bind.asyncQueueName);
            try {
                session.writeOnQueueSocket(ri);
            }
            catch (Exception e) {
                ri.exception = e;
            }
            return false;            
        }
        
        if (ri.type == RequestInfo.Type.CtoS_QueuedRequest) {
            // 3:CtoS_QueuedRequest put in queue
            // unless there is an error, then this will be invoked by the queue thread on the server
            if (ri.exceptionMessage != null) {
                ri.methodInvoked = true;
            }
            session.setupAsyncQueueSender(ri.bind.asyncQueueName);
            OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(ri.bind.asyncQueueName);
            cq.addMessageToQueue(ri);
            return false;            
        }
        
        if (ri.type == RequestInfo.Type.CtoS_QueuedRequestNoResponse) {
            // 3:CtoS_QueuedRequestNoResponse
            // this will be invoked by the queue thread on the server
            if (ri.exceptionMessage != null) return true;
            session.setupAsyncQueueSender(ri.bind.asyncQueueName);
            OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(ri.bind.asyncQueueName);
            cq.addMessageToQueue(ri);
            return false;            
        }
        
        if (ri.type == RequestInfo.Type.CtoS_QueuedResponse) {
            // received the response from a prev onInvokeForStoC, type:StoC_QueuedRequest, put response in queue
            RequestInfo rix = hmClientCallbackRequestInfo.remove(ri.messageId);
            if (rix != null) {
                rix.exception = ri.exception;
                rix.exceptionMessage = ri.exceptionMessage;
                rix.response = ri.response;
                processCtoSReturnValue(rix, session);
                rix.type = RequestInfo.Type.CtoS_QueuedResponse; 
                OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(rix.bind.asyncQueueName);
                cq.addMessageToQueue(rix);  // which will then set methodInvoked=true and notify orig thread
            }
            else {
                ri.exceptionMessage = "original message timed out waiting for response";
            }
            return false;            
        }
        
        if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
            OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(ri.bind.asyncQueueName);
//qqqqqqqqqqqqqqqqqqqq            
            int x = Math.min(250, cq.getSize() / 2);
            cq.addMessageToQueue(ri, x, session.connectionId);
            return false;
        }
        
        ri.exception = new Exception("invalid request command, it could not be processed");
        return true;
    }

    
    
    /**
     * Called after a CtoS remote method is called.
     */
    protected void afterInvokeForCtoS(RequestInfo ri) {
        if (ri == null) return;
        if (ri.exception != null || ri.exceptionMessage != null) {
            LOG.log(Level.WARNING, ri.toLogString(), ri.exception);
        }        
    }

    /**
     * VServerSocket that is used for vsockets that are used when a method is called on the server that
     * needs to be invoked on the client where the object came from.
     */
    protected void startServerSocketForStoC() throws Exception {
        if (ssStoC != null) return;
        ssStoC = multiplexerServer.createServerSocket("StoC");

        // accept new connections
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                    try {
                        Socket socket = ssStoC.accept();
                        onNewConnectionForStoC(socket);
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "Exception on new StoC socket", e);
                    }
                }
            }
        });
        t.setName("OAServerSocket_StoC");
        t.setDaemon(true);
        t.start();
        //LOG.config("created Server to Client serversocket thread");
    }

    /**
     * a client has created a new server to client (StoC) vsocket, that can be used for the server to
     * call methods on a client's remote object.
     */
    protected void onNewConnectionForStoC(Socket socket) {
        final VirtualSocket vSocket = (VirtualSocket) socket;
        int connectionId = vSocket.getConnectionId();
        Session session = getSession(connectionId, true);
        session.addSocketForStoC(vSocket);
    }

    /**
     * This will create a server side proxy instance for a remote object sent from a client.
     */
    protected Object createProxyForStoC(final Session session, Class c, final String bindName) {
        Object obj = null;
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = onInvokeForStoC(proxy, session, bindName, method, args);
                return result;
            }
        };
        obj = Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, handler);
        return obj;
    }

    
    // list of requests sent to client in queue that are waiting on a return
    private ConcurrentHashMap<Integer, RequestInfo> hmClientCallbackRequestInfo = new ConcurrentHashMap<Integer, RequestInfo>();
    private AtomicInteger aiMessageId = new AtomicInteger();
    
    protected Object onInvokeForStoC(Object proxyInstance, Session session, String bindName, Method method, Object[] args) throws Exception {
        aiMethodCallCnt.incrementAndGet();
        RequestInfo ri = new RequestInfo();
        try {
            ri.connectionId = 0;
            ri.msStart = System.currentTimeMillis();
            ri.nsStart = System.nanoTime();
            ri.object = proxyInstance;
            ri.bind = getBindInfo(bindName);
            if (ri.bind == null) ri.bind = session.getBindInfo(bindName);
            ri.bindName = bindName;
            ri.method = method;
            ri.args = args;
            ri.messageId = aiMessageId.incrementAndGet();
            ri.isRemoteThread = (Thread.currentThread() instanceof OARemoteThread);
            onInvokeForStoC(session, ri);
        }
        catch (Exception e) {
            ri.exception = e;
        }
        finally {
            ri.nsEnd = System.nanoTime();
            if (ri.socket != null) {
                session.addSocketForStoC(ri.socket);
            }
        }
        afterInvokeForStoC(ri);

        if (ri.exception != null) throw ri.exception;
        if (ri.exceptionMessage != null) {
            Exception ex = new Exception(ri.exceptionMessage + ", info: " + ri.toLogString());
            throw ex;
        }
        return ri.response;
    }

    // "dummy" object, that is used when methods are not supported in proxy interface, but are in Object class
    private final Object stuntObject = new Object();

    protected void onInvokeForStoC(Session session, RequestInfo ri) throws Exception {
        ri.bind = session.getBindInfo(ri.bindName);
        if (ri.bind == null) ri.bind = getBindInfo(ri.bindName);
        if (ri.bind == null) {
            ri.exceptionMessage = "object was removed on client (GCd)";
            return;
        }
        if (ri.bind != null) {
            ri.methodInfo = ri.bind.getMethodInfo(ri.method);
        }

        if (ri.methodInfo == null) {
            // check to see if method from Object.class is being invoked
            if (ri.method.getDeclaringClass().equals(Object.class)) {
                if ("equals".equals(ri.method.getName())) {
                    if (ri.args == null || ri.args.length != 1) {
                        ri.response = false;
                    }
                    else ri.response = (ri.args[0] == ri.object);
                }
                else {
                    try {
                        ri.response = ri.method.invoke(stuntObject, ri.args);
                    }
                    catch (InvocationTargetException e) {
                        Exception ex = e;
                        for (int i=0 ; i<10; i++) {
                            Throwable t = ex.getCause();
                            if (t == null || t == ex || !(t instanceof Exception)) { 
                                ri.exception = ex;
                                break;
                            }
                            ex = (Exception) t;
                            ri.exception = ex;
                        }
                    }
                }
            }
            else ri.exceptionMessage = "Method  not found";
            return;
        }

        
        if (ri.bind != null && ri.bind.usesQueue && (ri.methodInfo == null || !ri.methodInfo.dontUseQueue)) {
            ri.connectionId = session.connectionId;  // so that the _writeQueueMessages will send to only the client (not all clients)
  
            if (ri.methodInfo != null && ri.methodInfo.noReturnValue) {
                ri.type = RequestInfo.Type.StoC_QueuedRequestNoResponse;
                ri.response = OAReflect.getEmptyPrimitive(ri.method.getReturnType());
            }
            else {
                hmClientCallbackRequestInfo.put(ri.messageId, ri);
                ri.type = RequestInfo.Type.StoC_QueuedRequest;
            }
                
            OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(ri.bind.asyncQueueName);
            cq.addMessageToQueue(ri);
            waitForProcessedByServer(ri);

            if (ri.type == RequestInfo.Type.StoC_QueuedRequest) {
                // need to wait for return value 
                int maxSeconds = Math.max(ri.methodInfo == null ? 0 : ri.methodInfo.timeoutSeconds, 0);
                for (int i=0; ; i++) {
                    try {
                        if (waitForMethodInvoked(ri, 1)) break;  //wait for response back from client, which puts it in the queue                      
                        if (session.bDisconnected) {
                            ri.exceptionMessage = "disconnected from remote client";
                            break;
                        }
                        if (maxSeconds > 0 && i >= maxSeconds) {
                            if (!MultiplexerClient.DEBUG && !MultiplexerServer.DEBUG) {
                                ri.exceptionMessage = "timeout waiting for response";
                                break;
                            }
                        }
                    }
                    catch (Exception e) {
                        ri.exception = e;
                        break;
                    }
                }
                hmClientCallbackRequestInfo.remove(ri.messageId);
            }
        }
        else {
            if (ri.methodInfo != null && ri.methodInfo.noReturnValue) {
                ri.type = RequestInfo.Type.StoC_SocketRequestNoResponse;
                ri.response = OAReflect.getEmptyPrimitive(ri.method.getReturnType());
            }
            else {
                ri.type = RequestInfo.Type.StoC_SocketRequest;
            }
            
            processStoCArguments(ri, session);
            ri.socket = session.getSocketForStoC();
    
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, session.hmClassDescOutput, session.aiClassDescOutput);
            oos.writeByte(ri.type.ordinal());
            oos.writeAsciiString(ri.bind.name);
            oos.writeAsciiString(ri.methodInfo.methodNameSignature);
            oos.writeObject(ri.args);
            oos.flush();

            if (ri.type == RequestInfo.Type.StoC_SocketRequest) {
                RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, session.hmClassDescInput);
                byte b = ois.readByte();
                Object objx = ois.readObject();
                if (b == 0) ri.exception = (Exception) objx;
                else if (b == 1) ri.exceptionMessage = (String) objx;
                else {
                    ri.response = objx;
                    processCtoSReturnValue(ri, session);
                }
            }      
            session.releaseSocketForStoC(ri.socket);
            ri.socket = null;
        }
        processStoCReturnValue(ri, session);
        notifyMethodInvoked(ri);
    }
    
    private void processStoCArguments(final RequestInfo ri, final Session session) throws Exception {
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.compressedParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (ri.methodInfo.compressedParams[i]) {
                    ri.args[i] = new OACompressWrapper(ri.args[i]);
                }
            }
        }
        // check to see if any of the args[] are remote objects
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.remoteParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams[i] == null) continue;
                if (ri.args[i] == null) continue;

                BindInfo bindx = getBindInfo((Object) ri.args[i]);
                if (bindx == null) bindx = session.getBindInfo((Object) ri.args[i]);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    if (bindx == null) {
                        String bindNamex = "server." + aiBindCount.incrementAndGet();
                        
                        boolean b = ri.methodInfo.dontUseQueues != null && ri.methodInfo.dontUseQueues[i]; 
                        bindx = getBindInfo(ri.bind, bindNamex, ri.args[i], ri.methodInfo.remoteParams[i], b);
                    }
                    else {
                        bindx.setObject(ri.args[i], referenceQueue);
                    }
                }
                session.hmBindObject.put(bindx, ri.args[i]); // hold the remote object from getting GCd
                ri.args[i] = bindx.name;
            }
        }
    }
    private void processStoCArguments(final RequestInfo ri) throws Exception {
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.compressedParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (ri.methodInfo.compressedParams[i]) {
                    ri.args[i] = new OACompressWrapper(ri.args[i]);
                }
            }
        }
        // check to see if any of the args[] are remote objects
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.remoteParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams[i] == null) continue;
                if (ri.args[i] == null) continue;

                BindInfo bindx = getBindInfo((Object) ri.args[i]);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    if (bindx == null) {
                        String bindNamex = "server." + aiBindCount.incrementAndGet();
                        boolean b = ri.methodInfo.dontUseQueues != null && ri.methodInfo.dontUseQueues[i]; 
                        bindx = getBindInfo(ri.bind, bindNamex, ri.args[i], ri.methodInfo.remoteParams[i], b);
                    }
                    else {
                        bindx.setObject(ri.args[i], referenceQueue);
                    }
                }
                hmBindObject.put(bindx, ri.args[i]); // hold the remote object from getting GCd
                ri.args[i] = bindx.name;
            }
        }
    }
    private void processStoCReturnValue(final RequestInfo ri, final Session session) throws Exception {
        // check to see if return value is a remote object
        if (ri.methodInfo.noReturnValue) return;
        if (ri.response != null && ri.methodInfo.remoteReturn != null) {
            String bindNamex = (String) ri.response;
            BindInfo bindx = session.getBindInfo(bindNamex);
            Object objx = bindx != null ? bindx.weakRef.get() : null;
            if (bindx == null || objx == null) {
                if (bindx == null) {
                    bindx = getBindInfo(bindNamex);
                    objx = bindx != null ? bindx.weakRef.get() : null;
                    if (objx == null) bindx = null;
                }
                else bindx = null;
                if (bindx == null) {
                    Object obj = createProxyForStoC(session, ri.methodInfo.remoteReturn, bindNamex);
                    boolean b = ri.methodInfo.dontUseQueueForReturnValue; 
                    bindx = getBindInfo(ri.bind, bindNamex, obj, ri.methodInfo.remoteReturn, b);
                }
            }
            ri.response = bindx.getObject();
        }
        else if (ri.response != null && ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
            ri.response = ((OACompressWrapper) ri.response).getObject();
        }
    }
    
    /**
     * Called after a StoC remote method is called.
     */
    protected void afterInvokeForStoC(RequestInfo ri) {
        if (ri == null) return;
        if (ri.exception != null || ri.exceptionMessage != null) {
            LOG.log(Level.WARNING, ri.toLogString(), ri.exception);
        }        
    }

    // remove gc'd binding objects
    public void performDGC() {
        for (;;) {
            WeakReference ref = (WeakReference) referenceQueue.poll();
            if (ref == null) break;

            for (Map.Entry<String, BindInfo> entry : hmNameToBind.entrySet()) {
                BindInfo bindx = entry.getValue();
                if (bindx.weakRef == ref) {
                    hmNameToBind.remove(entry.getKey());
                    break;
                }
            }
        }
    }


    /**
     * Register/Bind an Object so that it can be used by clients
     * 
     * @param name
     * @param obj
     *            remote object to create
     * @param interfaceClass
     * 
     *            Important: a weakref is used to store the remote object "obj"
     */
    public void createLookup(String name, Object obj, Class interfaceClass) {
        createLookup(name, obj, interfaceClass, null, -1);
    }

    /**
     * 
     * @param name
     * @param obj
     * @param interfaceClass
     * @param queueName
     *            used to have return value use an async circular queue for responses.
     * @param queueSize
     */
    public void createLookup(String name, Object obj, Class interfaceClass, String queueName, int queueSize) {
        BindInfo bind = getBindInfo(name, obj, interfaceClass, queueName, queueSize);
        hmBindObject.put(bind, obj);
    }

    /**
     * Remove an object that was previously used for a bind.
     */
    public boolean removeLookup(String name) {
        BindInfo bind = getBindInfo(name);
        if (bind == null) return false;
        hmBindObject.remove(bind);
        hmNameToBind.remove(name);
        return true;
    }

    /**
     * Get the Bind information for the name assigned to a remote object.
     */
    protected BindInfo getBindInfo(String name) {
        if (name == null) return null;
        return hmNameToBind.get(name);
    }

    /**
     * Get the Bind information for a remote object.
     */
    protected BindInfo getBindInfo(Object obj) {
        if (obj == null) return null;
        for (BindInfo bindx : hmNameToBind.values()) {
            if (bindx.weakRef != null && bindx.weakRef.get() == obj) {
                return bindx;
            }
        }
        return null;
    }

    
    protected BindInfo getBindInfo(BindInfo biParent, String name, Object obj, Class interfaceClass, boolean bDontUseQueue) {
        return getBindInfo(biParent, name, obj, interfaceClass, false, null, 0, bDontUseQueue);
    }
    protected BindInfo getBindInfo(String name, Object obj, Class interfaceClass, String queueName, int queueSize) {
        return getBindInfo(null, name, obj, interfaceClass, false, queueName, queueSize, false);
    }
    protected BindInfo getBindInfo(BindInfo biParent, String name, Object obj, Class interfaceClass, boolean bIsBroadcast, String queueName, int queueSize, boolean bDontUseQueue) {
        if (name == null || interfaceClass == null) {
            throw new IllegalArgumentException("name and interfaceClass can not be null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("interfaceClass must be a Java interface");
        }
        
        BindInfo bind = hmNameToBind.get(name);
        if (bind != null) return bind;
        if (biParent != null) {
            queueName = biParent.asyncQueueName;  
            queueSize = biParent.asyncQueueSize;
        }
        
        bind = new BindInfo(name, obj, interfaceClass, referenceQueue, bIsBroadcast, queueName, queueSize);

        bind.loadMethodInfo();
        hmNameToBind.put(name, bind);
        
        if (bind.usesQueue && !bDontUseQueue) {
            OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(bind.asyncQueueName);
            if (cq == null) {
                cq = new OACircularQueue<RequestInfo>(bind.asyncQueueSize) {
                    @Override
                    protected boolean shouldWaitOnSlowSession(int sessionId, int msSinceLastRead) {
                        if (msSinceLastRead > 20000) return false;  // dont wait over 20 seconds
                        Session session = getSession(sessionId, false);
                        if (session == null) return false;
                        if (session.bDisconnected) return false;
                        if (session.realSocket.isClosed()) return false;
                        return true;
                    }
                    
                };
                cq.setName(queueName);
                hmAsyncCircularQueue.put(bind.asyncQueueName, cq);
            }
        }
        return bind;
    }

    public Object createBroadcast(final String bindName, Class interfaceClass, String queueName, int queueSize) {
        return createBroadcast(bindName, null, interfaceClass, queueName, queueSize);
    }

    /**
     * Allows sending messages to server and all clients.
     * 
     * @param bindName
     *            name for clients to use to lookup the object
     * @param callback
     *            object to use when receiving a broadcast from a client
     * @param interfaceClass
     * @param queueName
     *            name of circularQueue used to hold messages
     * @param queueSize
     *            size of circularQueue
     * @return proxy instance where all methods will be sent to and invoked on all clients
     * @see RemoteMultiplexerClient#createBroadcastProxy(String, Object)
     */
    public Object createBroadcast(String bindName, Object callback, Class interfaceClass, String queueName, int queueSize) {
        if (bindName == null) throw new IllegalArgumentException("bindName can not be null");
        if (interfaceClass == null) throw new IllegalArgumentException("interfaceClass can not be null");
        if (callback != null && !interfaceClass.isAssignableFrom(callback.getClass())) {
            throw new IllegalArgumentException("callback must be same class as " + interfaceClass);
        }
        if (queueSize < 100) {
            queueSize = 100;
            // throw new IllegalArgumentException("queueSize must be greater then 100");
        }

        if (queueName == null) queueName = bindName;
        final BindInfo bind = getBindInfo(null, bindName, callback, interfaceClass, true, queueName, queueSize, false);
        if (callback != null) hmBindObject.put(bind, callback); // hold from getting gc'd

        InvocationHandler handler = new InvocationHandler() {
            int errorCnt;
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RequestInfo ri = onInvokeBroadcast(bind, method, args);
                return ri.response;
            }
        };
        Object obj = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, handler);

        //20150505 need to have the server process the queue, since clients wait for server to "catch up"
        //if (callback != null) {
            // create thread to get messages from queue
            setupBroadcastQueueReader(bind.asyncQueueName, bind.name);
        //}
        return obj;
    }

    // server is calling a remote broadcast method
    protected RequestInfo onInvokeBroadcast(BindInfo bind, Method method, Object[] args) throws Exception {
        aiMethodCallCnt.incrementAndGet();
        RequestInfo ri = new RequestInfo();
        ri.connectionId = 0;
        ri.msStart = System.currentTimeMillis();
        ri.nsStart = System.nanoTime();
        ri.bindName = bind.name;
        ri.method = method;
        ri.args = args;
        ri.bind = bind;
        ri.type = RequestInfo.Type.StoC_QueuedBroadcast;
        ri.isRemoteThread = (Thread.currentThread() instanceof OARemoteThread);

        ri.methodInfo = ri.bind.getMethodInfo(ri.method);
        ri.object = ri.bind.getObject();

        if (ri.methodInfo == null) {
            // check to see if method from Object.class is being invoked
            if (ri.method.getDeclaringClass().equals(Object.class)) {
                if ("equals".equals(ri.method.getName())) {
                    if (ri.args == null || ri.args.length != 1) {
                        ri.response = false;
                    }
                    else ri.response = (ri.args[0] == ri.object);
                }
                else {
                    try {
                        OAThreadLocalDelegate.setRemoteRequestInfo(ri);
                        ri.response = ri.method.invoke(stuntObject, ri.args);
                    }
                    catch (InvocationTargetException e) {
                        Exception ex = e;
                        for (int i=0 ; i<10; i++) {
                            Throwable t = ex.getCause();
                            if (t == null || t == ex || !(t instanceof Exception)) { 
                                ri.exception = ex;
                                break;
                            }
                            ex = (Exception) t;
                            ri.exception = ex;
                        }
                    }
                    OAThreadLocalDelegate.setRemoteRequestInfo(null);
                }
            }
            else ri.exceptionMessage = "Method  not found";
            return ri;
        }

        processStoCArguments(ri);        
        if (ri.response == null) ri.response = OAReflect.getEmptyPrimitive(ri.method.getReturnType());


        Thread t = Thread.currentThread();
        RequestInfo rix;
        if (ri.isRemoteThread) {
            OARemoteThread rt = (OARemoteThread) t;
            rix = rt.requestInfo;
        }
        else rix = null;

        // put "ri" in circular queue for clients to pick up.       
        OACircularQueue<RequestInfo> cque = hmAsyncCircularQueue.get(ri.bind.asyncQueueName);
        
        int x;
        if (rix == null) {  
            // command running on the server
            x = Math.min(350, cque.getSize() / 2);
            cque.addMessageToQueue(ri, x, 0);  // this will throttle
        }
        else {
            // command running because of a client request (rix)            
            x = Math.min(175, cque.getSize() / 2);
            cque.addMessageToQueue(ri, x, rix.connectionId);  // this will throttle
        }
        
        if (rix == null) {
            waitForProcessedByServer(ri);
        }
        
        ri.nsEnd = System.nanoTime();
        notifyMethodInvoked(ri);
        afterInvokeForStoC(ri);
        
        return ri;
    }

    private ConcurrentHashMap<String, String> hmAsyncQueue = new ConcurrentHashMap<String, String>();

    protected void setupBroadcastQueueReader(final String asyncQueueName, final String bindName) {
        final OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(asyncQueueName);
        if (cq == null) throw new RuntimeException("circular queue, name="+asyncQueueName+", does not exist");
        synchronized (hmAsyncQueue) {
            if (hmAsyncQueue.get(asyncQueueName) != null) return;
            hmAsyncQueue.put(asyncQueueName, "");
        }

        final long qPos = cq.registerSession(0);

        // set up thread that will get messages from queue and send to client
        final String threadName = "Broadcast.queue." + asyncQueueName;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    try {
                        processQueueMessagesOnServer(cq, bindName, qPos);
                    }
                    catch (Exception e) {
                        String s = "processQueueMessagesOnServer thread exception, thread="+threadName+", queue=" + asyncQueueName;
                        LOG.log(Level.WARNING, s, e);
                    }
                }
            }
        });
        t.setName(threadName);
        t.setDaemon(true);
        t.start();
    }

    private void processQueueMessagesOnServer(final OACircularQueue<RequestInfo> cque, final String bindName, long qpos) throws Exception {
        if (cque == null) return;
        for (;;) {
            RequestInfo[] ris;
            ris = cque.getMessages(0, qpos, 20, 10000);
            
            if (ris == null) {
                continue;
            }
            
            qpos += ris.length;
            
            for (RequestInfo ri : ris) {
                if (ri == null) {
                    continue;
                }

                boolean bNotifyMethodInvoked = false;

                if (ri.type == RequestInfo.Type.CtoS_QueuedRequest) {
                    // 4:CtoS_QueuedRequest invoke 
                    invokeUsingRemoteThread(ri, true);
                }
                else if (ri.type == RequestInfo.Type.CtoS_QueuedRequestNoResponse) {
                    // 4:CtoS_QueuedRequestNoResponse
                    invokeUsingRemoteThread(ri, true);
                    // no clients get this
                }
                else if (ri.type == RequestInfo.Type.CtoS_QueuedResponse) {
                    bNotifyMethodInvoked = true; // waiting thread will wake up on ri.notifyAll()
                    // clients need to ignore this
                    // client is returning value for a S2C request
                }
                else if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
                    invokeUsingRemoteThread(ri, true);
                }
                else if (ri.type == RequestInfo.Type.StoC_QueuedRequest) {
                    // only one client gets this
                }
                else if (ri.type == RequestInfo.Type.StoC_QueuedRequestNoResponse) {
                    // only one client gets this
                }
                else if (ri.type == RequestInfo.Type.StoC_QueuedBroadcast) {
                }
                else if (ri.type == RequestInfo.Type.StoC_QueuedResponse) {
                    // 8:CtoS_QueuedRequest flag as processed
                }
                
                if (!ri.processedByServerQueue) {
                    notifyProcessedByServer(ri);
                }
                if (bNotifyMethodInvoked) {
                    notifyMethodInvoked(ri);
                }
            }
        }
    }
    
    
    private AtomicInteger aiRemoteClientThreadPos = new AtomicInteger();
    /**
     * This will have the currentThread wait for a RemoteThread to process the request using invokeCtoS _invokeCtoS
     */
    protected void invokeUsingRemoteThread(final RequestInfo ri, boolean bFromServerQueueThread) throws Exception {
        if (ri == null) return;
        if (ri.methodInvoked) return; 
        // sent by client, invoke method on object
        Object obj = ri.bind.getObject();
        if (obj == null) {
            if (ri.exceptionMessage != null) ri.exceptionMessage = "remote object impl is null";
            notifyMethodInvoked(ri);
            return;
        }

        // 5:CtoS_QueuedRequest remoteThread invokes the request
        // 5:CtoS_QueuedRequestNoResponse
        
        OARemoteThread remoteThread = null;
        synchronized (alRemoteClientThread) {    
            for (int cnt=0; ; cnt++) {
                int x = alRemoteClientThread.size();
                for (int i=0; i<x; i++) {
                    OARemoteThread rct = alRemoteClientThread.get( aiRemoteClientThreadPos.incrementAndGet()%x );
                    synchronized (rct.Lock) {
                        if (rct.requestInfo == null) {
                            remoteThread = rct;
                            rct.requestInfo = ri;
                            rct.Lock.notifyAll(); 
                            break;
                        }
                    }
                }
                if (remoteThread != null || x < 50 || cnt > 5) break;
                try {
                    alRemoteClientThread.wait(50);
                }
                catch (Exception e) {
                }
            }
        }

        if (remoteThread == null) {
            remoteThread = createRemoteClientThread();
            synchronized (alRemoteClientThread) {    
                alRemoteClientThread.add(remoteThread);
                synchronized (remoteThread.Lock) {
                    remoteThread.requestInfo = ri;
                    remoteThread.Lock.notifyAll(); 
                }
            }
            if (alRemoteClientThread.size() > 50) {
                LOG.warning("alRemoteClientThread.size() = " + alRemoteClientThread.size());
            }
        }
        
        int maxSeconds = Math.max(ri.methodInfo == null ? 0 : ri.methodInfo.timeoutSeconds, 0); 
        long ms1 = System.currentTimeMillis();

        // remoteThread is now processing the request
        
        if (bFromServerQueueThread) {  // if true, then need to get back to queue asap
            if (ri.bind.isOASync) {
                // note: the remoteThread.startNextThread will call notifyProcessedByServer 
                waitForProcessedByServer(ri);
            }
            // need to continue to get requests from the queue
            return;
        }
        
        for (;;) {
            if (waitForMethodInvoked(ri, maxSeconds)) break;
            if (!MultiplexerClient.DEBUG && !MultiplexerServer.DEBUG) {
                ri.exceptionMessage = "timeout waiting for response";
                break;
            }
        }
        
        long ms2 = System.currentTimeMillis();
        // this can be removed, sanity check only
        if (maxSeconds > 0 && (ms2-ms1) >= (maxSeconds * 1000)) {
            StackTraceElement[] stes = remoteThread.getStackTrace();
            Exception ex = new Exception();
            ex.setStackTrace(stes);
            LOG.log(Level.WARNING, "timeout waiting for message, will continue, this is stacktrace for the remoteThread, request="
                    + ri.toLogString(), ex);
        }
        ri.nsEnd = System.nanoTime();
    }

    protected void _invoke(final OARemoteThread rt, final RequestInfo ri, final Session session) throws Exception {
        if (ri == null) return;

        if (ri.methodInfo == null) {
            if (ri.exceptionMessage != null) ri.exceptionMessage = "method not found";
            return;
        }
        
        processCtoSArguments(ri, session);
        
        try {
            OAThreadLocalDelegate.setRemoteRequestInfo(ri);
            if (!ri.bind.isBroadcast) {
                OARemoteThreadDelegate.sendMessages(true);
            }
            ri.response = ri.method.invoke(ri.bind.getObject(), ri.args);
        }
        catch (InvocationTargetException e) {
            Exception ex = e;
            for (int i=0 ; i<10; i++) {
                Throwable t = ex.getCause();
                if (t == null || t == ex || !(t instanceof Exception)) { 
                    ri.exception = ex;
                    break;
                }
                ex = (Exception) t;
                ri.exception = ex;
            }
        }
        catch (Throwable tx) {
            ri.exception = new Exception(tx.toString(), tx);
        }
        finally {
            if (!ri.bind.isBroadcast) {
                OARemoteThreadDelegate.sendMessages(false);
            }
        }
        OAThreadLocalDelegate.setRemoteRequestInfo(null);
        processCtoSReturnValue(ri, session);
        ri.nsEnd = System.nanoTime();

        if (ri.type == RequestInfo.Type.CtoS_QueuedRequest) {
            // 7:CtoS_QueuedRequest put result back in queue
            ri.type = RequestInfo.Type.StoC_QueuedResponse;
            OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(ri.bind.asyncQueueName);
            cq.addMessageToQueue(ri);
        }
        
        notifyMethodInvoked(ri);
        afterInvokeForCtoS(ri);
    }
    
    private void processCtoSArguments(final RequestInfo ri, final Session session) throws Exception {
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.compressedParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (!ri.methodInfo.compressedParams[i]) continue;
                ri.args[i] = ((OACompressWrapper) ri.args[i]).getObject();
            }
        }

        // check to see if any of the args[] are remote objects
        if (session != null && ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.remoteParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams[i] == null) continue;
                // convert the param to real object (proxy)
                final String bindName = (String) ri.args[i];
                if (bindName == null) continue;
                BindInfo bindx = session.getBindInfo(bindName);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    if (bindx != null) {
                        bindx = getBindInfo(bindName);
                        objx = bindx != null ? bindx.weakRef.get() : null;
                        if (objx == null) { // object was gc'd
                            bindx = null;
                        }
                    }
                    else bindx = null;
                    if (bindx == null) {
                        Object obj = createProxyForStoC(session, ri.methodInfo.remoteParams[i], bindName);
                        bindx = session.createBindInfo(ri.bind, bindName, obj, ri.methodInfo.remoteParams[i]);
                    }
                }
                ri.args[i] = bindx.getObject();
            }
        }
    }
    
     
    private void processCtoSReturnValue(final RequestInfo ri, final Session session) throws Exception {
        // check the return value to see if it is a remote object, and if it needs compression
        if (ri.methodInfo.noReturnValue) return;
        
        if (session != null && ri.response != null && ri.methodInfo.remoteReturn != null) {
            BindInfo bindx = getBindInfo(ri.response);
            Object objx = bindx != null ? bindx.weakRef.get() : null; // make sure obj is not gc'd
            if (bindx == null || objx == null) {
                if (bindx == null) {
                    bindx = session.getBindInfo(ri.response);
                    objx = bindx != null ? bindx.weakRef.get() : null;
                    if (objx == null) { // object was gc'd
                        bindx = null;
                    }
                }
                if (bindx == null) {
                    // make remote
                    String bindNamex = "server." + aiBindCount.incrementAndGet(); // this will be sent to client
                    bindx = session.createBindInfo(ri.bind, bindNamex, ri.response, ri.methodInfo.remoteReturn);
                }
            }
            ri.responseBindName = bindx.name; // this will be returned to client
            ri.responseBindUsesQueue = bindx.usesQueue && !ri.methodInfo.dontUseQueueForReturnValue;
            session.hmBindObject.put(bindx, ri.response); // make sure it wont get gc'd
        }
        else if (ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
            ri.response = new OACompressWrapper(ri.response);
        }
    }

    
    
    // use OARemoteThread to process broadcast messages on the server
    private final AtomicInteger aiClientThreadCount = new AtomicInteger();
    private final ArrayList<OARemoteThread> alRemoteClientThread = new ArrayList<OARemoteThread>();

    private OARemoteThread createRemoteClientThread() {
        OARemoteThread t = new OARemoteThread() {
            @Override
            public void run() {
                boolean bReset = true;
                for ( ;!stopCalled; ) {
                    try {
                        if (shouldClose(this)) break;
                        synchronized (Lock) {
                            if (bReset) {
                                reset();
                                bReset = false;
                            }
                            if (requestInfo == null) {
                                if (alRemoteClientThread.size() > 15) {
                                    Lock.wait(1000);
                                }
                                else Lock.wait(10000);
                                if (requestInfo == null) continue;
                            }
                        }
                        bReset = true;
                        
                        Session session;
                        if (requestInfo.connectionId != 0) {
                            session = getSession(requestInfo.connectionId, false);
                        }
                        else session = null;
                        // 6:CtoS_QueuedRequest invoke
                        // 6:CtoS_QueuedRequestNoResponse
                        _invoke(this, requestInfo, session);

                        this.msLastUsed = System.currentTimeMillis();
                        synchronized (Lock) {
                            if (requestInfo != null) {
                                if (!requestInfo.processedByServerQueue) {
                                    notifyProcessedByServer(requestInfo);
                                }
                                this.requestInfo = null;
                            }
                            Lock.notifyAll();
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "error in remoteThread loop, will continue", e);
                    }
                }
            }

            @Override
            public void startNextThread() {
                if (startedNextThread) return;
                super.startNextThread();
                if (requestInfo != null) {
                    if (!requestInfo.processedByServerQueue) {
                        notifyProcessedByServer(requestInfo);
                    }
                }
            }
        };
        t.setName("OARemoteThread." + aiClientThreadCount.getAndIncrement());
        t.setDaemon(true);
        t.start();
        return t;
    }

    private boolean shouldClose(final OARemoteThread remoteThread) {
        final int x = alRemoteClientThread.size();
        if (x < 4) return false;
        int max;
        if (x > 100) max = 100;
        else if (x > 50) max = 500;
        else max = 1000;
        if (remoteThread.requestInfo != null) return false;            
        
        if (remoteThread.msLastUsed == 0 || (remoteThread.msLastUsed + max > System.currentTimeMillis()) ) return false;
        synchronized (alRemoteClientThread) {
            if (remoteThread.requestInfo != null) return false;            
            if (alRemoteClientThread.size() < 4) return false;
            
            int cntUsed = 0;
            for (OARemoteThread rt : alRemoteClientThread) {
                if (rt.requestInfo != null) cntUsed++;
            }
            if (cntUsed + 3 > x) return false;
            
            alRemoteClientThread.remove(remoteThread);
            remoteThread.stopCalled = true;
        }
        return true;
    }
    
    
    protected void onException(int connectionId, String title, String msg, Exception e, boolean bWillDisconnect) {
    }
    
    /**
     * Tracks each client connection.
     * 
     * @author vvia
     * @see #removeSession to have this session removed from collection.
     */
    class Session {
        public int connectionId;
        public Socket realSocket;
        private volatile boolean bDisconnected;

        public Session() {
            
        }
        
        // performance enhancement for ObjectSteams
        ConcurrentHashMap<String, Integer> hmClassDescOutput = new ConcurrentHashMap<String, Integer>();
        AtomicInteger aiClassDescOutput = new AtomicInteger();
        ConcurrentHashMap<Integer, ObjectStreamClass> hmClassDescInput = new ConcurrentHashMap<Integer, ObjectStreamClass>();

        // remote objects
        ConcurrentHashMap<String, BindInfo> hmNameToBind = new ConcurrentHashMap<String, BindInfo>();
        // list of vsockets used for calling methods on client
        ArrayList<VirtualSocket> alSocketFromStoC = new ArrayList<VirtualSocket>();

        // hold onto the objects that session has
        ConcurrentHashMap<BindInfo, Object> hmBindObject = new ConcurrentHashMap<BindInfo, Object>();

        // queues that this session has a thread created to send to client
        ConcurrentHashMap<String, String> hmAsyncQueue = new ConcurrentHashMap<String, String>();

        protected BindInfo getBindInfo(String name) {
            if (name == null) return null;
            return hmNameToBind.get(name);
        }

        protected BindInfo removeBindInfo(String name) {
            if (name == null) return null;
            return hmNameToBind.remove(name);
        }

        protected BindInfo getBindInfo(Object obj) {
            if (obj == null) return null;
            for (BindInfo bindx : hmNameToBind.values()) {
                if (obj.equals(bindx.weakRef.get())) {
                    return bindx;
                }
            }
            return null;
        }

        void onDisconnect() {
            synchronized (alSocketFromStoC) {
                bDisconnected = true;
                alSocketFromStoC.notifyAll();
            }
        }

        /**
         * used by server when calling methods on a remote object that was created on the client, so
         * that the server can call any of the methods on it.
         */
        public VirtualSocket getSocketForStoC() throws Exception {
            VirtualSocket socket = null;
            boolean bWaitedForFirst = false;
            for (int i = 0; socket == null; i++) {
                boolean bRequestNew = false;
                synchronized (alSocketFromStoC) {
                    if (bDisconnected) {
                        throw new Exception("closed connection/session=" + connectionId);
                    }
                    int x = alSocketFromStoC.size();
                    if (x > 0) {
                        socket = alSocketFromStoC.remove(0);
                        if (x == 1) bRequestNew = true;
                    }
                    else if (!bWaitedForFirst) {
                        alSocketFromStoC.wait(250);
                        bWaitedForFirst = true;
                    }
                    else if (i > 20) {
                        throw new Exception("no StoC sockets available for connection/session=" + connectionId);
                    }
                    else {
                        alSocketFromStoC.wait(100);
                    }
                }
                if (bRequestNew) {
                    RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket);
                    oos.writeByte(RequestInfo.Type.StoC_CreateNewStoCSocket.ordinal());
                    oos.flush();
                }
            }
            return socket;
        }

        public void releaseSocketForStoC(VirtualSocket socket) throws Exception {
            if (socket == null) return;
            if (socket.isClosed()) return;
            synchronized (alSocketFromStoC) {
                if (alSocketFromStoC.size() < 3) {
                    alSocketFromStoC.add(socket);
                    alSocketFromStoC.notifyAll();
                    return;
                }
            }
            socket.close();
        }

        public void addSocketForStoC(VirtualSocket socket) {
            if (socket == null) return;
            // LOG.fine("connectionId="+connectionId+", vid="+socket.getId());
            synchronized (alSocketFromStoC) {
                alSocketFromStoC.add(socket);
                alSocketFromStoC.notifyAll();
            }
        }

        public BindInfo createBindInfo(BindInfo biParent, String name, Object obj, Class interfaceClass) {
            if (name == null || interfaceClass == null) {
                throw new IllegalArgumentException("name and interfaceClass can not be null");
            }
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException("interfaceClass must be a Java interface");
            }
            
            BindInfo bind = new BindInfo(name, obj, interfaceClass, null, false, 
                    biParent.asyncQueueName, biParent.asyncQueueSize); 

            bind.loadMethodInfo();
            hmNameToBind.put(name, bind);
            return bind;
        }

        // 20151129
        public void writeOnQueueSocket(final RequestInfo ri) throws Exception {
            String qname = ri.bind.asyncQueueName;

            VirtualSocket vsocket = hmAsyncQueueSocket.get(qname);
            if (vsocket == null) {
                ri.exceptionMessage = "message queue does not have a virtualSocket, qname="+qname;
                return;
            }

            synchronized (vsocket) {
                RemoteObjectOutputStream oos = new RemoteObjectOutputStream(vsocket, hmClassDescOutput, aiClassDescOutput);
                oos.writeByte(ri.type.ordinal());
                
                if (ri.exception != null) {
                    oos.writeByte(0);
                    oos.writeObject(ri.exception);
                }
                else if (ri.exceptionMessage != null) {
                    oos.writeByte(1);
                    oos.writeObject(ri.exceptionMessage);
                }
                else if (ri.responseBindName != null) {
                    oos.writeByte(2);
                    oos.writeObject(new Object[] {ri.responseBindName, ri.responseBindUsesQueue} );
                }
                else {
                    oos.writeByte(3);
                    oos.writeObject(ri.response);
                }
                oos.writeInt(ri.messageId);
                oos.flush();
            }
        }
        
        private HashMap<String, VirtualSocket> hmAsyncQueueSocket = new HashMap<String, VirtualSocket>();  
        
        // start thread that will send async return values back to client
        public void setupAsyncQueueSender(final String asyncQueueName) {
            if (hmAsyncQueue.get(asyncQueueName) != null) return;
            synchronized (hmAsyncQueue) {
                if (hmAsyncQueue.get(asyncQueueName) != null) return;

                hmAsyncQueue.put(asyncQueueName, "");
                final OACircularQueue<RequestInfo> cq = hmAsyncCircularQueue.get(asyncQueueName);
                final long qPos = cq.registerSession(connectionId);
                
                // set up thread that will get messages from queue and send to client
                final String threadName = "Client." + connectionId + ".queue." + asyncQueueName;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeQueueMessages(asyncQueueName, cq, qPos);
                        }
                        catch (Exception e) {
                            if (realSocket != null && !realSocket.isClosed()) {
                                String s = "async queue thread exception, thread=" + threadName + ", thread is stopping, "
                                        + "which will stop message from being sent to this client, queue=" + asyncQueueName;
                                LOG.log(Level.WARNING, s, e);
                            }
                        }
                    }
                });
                t.setName(threadName);
                t.setDaemon(true);
                t.start();
            }
        }

        private void writeQueueMessages(final String asyncQueueName,  final OACircularQueue<RequestInfo> cque, final long startQuePos) throws Exception {
            final VirtualSocket vsocket = getSocketForStoC();
            hmAsyncQueueSocket.put(asyncQueueName, vsocket);
            
            try {
                _writeQueueMessages(cque, vsocket, startQuePos);
            }
            finally {
                cque.unregisterSession(connectionId);
                releaseSocketForStoC(vsocket);
            }
        }
        
        
        // used to send broadcast messages to client
        private void _writeQueueMessages(final OACircularQueue<RequestInfo> cque, VirtualSocket vsocket, long qpos)
                throws Exception {
            int connectionId = vsocket.getConnectionId();
            HashSet<Integer> hsQueuedRequest = new HashSet<Integer>(); 
            for (int i=0;;i++) {
                if (vsocket.isClosed()) {
                    if (realSocket != null && !realSocket.isClosed()) {
                        throw new Exception("vsocket has been closed, but real socket is still open");
                    }
                    return;
                }

                RequestInfo[] ris = null;
                try {
                    ris = cque.getMessages(connectionId, qpos, 100, 2000);
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "Message queue overrun with msg CircularQueue", e);
                    onException(connectionId, "Message queue overrun", "Message queue overrun", e, true);
                    throw e;
                }
                if (ris == null) {
                    continue;
                }

                for (RequestInfo ri : ris) {
                    cque.keepAlive(connectionId);
                    qpos++;
                    if (vsocket.isClosed()) return;
                    if (ri == null || ri.bind == null) {
                        continue;
                    }
                    if (ri.type == RequestInfo.Type.StoC_QueuedBroadcast) {
                    }                    
                    else if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
                    }                    
                    else if (ri.type == RequestInfo.Type.CtoS_QueuedRequest) {
                        if (ri.connectionId == connectionId) {
                            hsQueuedRequest.add(ri.messageId);
                        }
                        continue;
                    }
                    else if (ri.type == RequestInfo.Type.StoC_QueuedResponse) {
                        // 9:CtoS_QueuedRequest send back to client
                        if (ri.connectionId != connectionId) {
                            continue;
                        }
                        if (!hsQueuedRequest.remove(ri.messageId)) {
                            hsQueuedRequest.add(ri.messageId);
                            continue;  // wait for it to show up the second time
                        }
                    }
                    else if (ri.type == RequestInfo.Type.CtoS_QueuedRequestNoResponse) {
                        // 7:CtoS_QueuedRequestNoResponse END
                        continue;
                    }
                    else if (ri.type == RequestInfo.Type.CtoS_QueuedResponse) {
                        continue;  
                    }                    
                    else if (ri.type == RequestInfo.Type.StoC_QueuedRequest) {
                        if (ri.connectionId != connectionId) {
                            continue;
                        }
                    }
                    else if (ri.type == RequestInfo.Type.StoC_QueuedRequestNoResponse) {
                        continue;
                    }
                    else {
                        continue;
                    }

                    waitForProcessedByServer(ri);

                    synchronized (vsocket) {
                        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(vsocket, hmClassDescOutput, aiClassDescOutput);
                        oos.writeByte(ri.type.ordinal());
    
                        if (ri.type == RequestInfo.Type.StoC_QueuedResponse) {
                            // 10:CtoS_QueuedRequest write to client END 
                            if (ri.exception != null) {
                                oos.writeByte(0);
                                oos.writeObject(ri.exception);
                            }
                            else if (ri.exceptionMessage != null) {
                                oos.writeByte(1);
                                oos.writeObject(ri.exceptionMessage);
                            }
                            else if (ri.responseBindName != null) {
                                oos.writeByte(2);
                                oos.writeObject(new Object[] {ri.responseBindName, ri.responseBindUsesQueue} );
                            }
                            else {
                                oos.writeByte(3);
                                oos.writeObject(ri.response);
                            }
                            oos.writeInt(ri.messageId);
                        }
                        else if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
                            oos.writeInt(ri.connectionId);
                            oos.writeInt(ri.messageId);
                            if (ri.connectionId != connectionId) {
                                oos.writeAsciiString(ri.bindName);
                                oos.writeAsciiString(ri.methodInfo.methodNameSignature);
                                oos.writeObject(ri.args);
                            }
                        }
                        else if (ri.type == RequestInfo.Type.StoC_QueuedRequest) {
                            oos.writeAsciiString(ri.bindName);
                            oos.writeAsciiString(ri.methodInfo.methodNameSignature);
                            processStoCArguments(ri, Session.this);  // this is only done once, right before it's sent
                            oos.writeObject(ri.args);
                            oos.writeInt(ri.messageId);
                        }
                        else if (ri.type == RequestInfo.Type.StoC_QueuedRequestNoResponse) {
                            oos.writeAsciiString(ri.bindName);
                            oos.writeAsciiString(ri.methodInfo.methodNameSignature);
                            processStoCArguments(ri, Session.this);  // this is only done once, right before it's sent
                            oos.writeObject(ri.args);
                        }
                        else if (ri.type == RequestInfo.Type.StoC_QueuedBroadcast) {
                            oos.writeAsciiString(ri.bindName);
                            oos.writeAsciiString(ri.methodInfo.methodNameSignature);
                            oos.writeObject(ri.args);  // args are already be processed (processStoCArguments)
                        }
                        oos.flush();
                    }
                }
            }
        }
    }
    
    
    protected void notifyMethodInvoked(RequestInfo ri) {
        if (ri == null) return;
        synchronized (ri) {
            ri.methodInvoked = true;
            ri.notifyAll();
        }
    }
    protected boolean waitForMethodInvoked(RequestInfo ri) {
        return waitForMethodInvoked(ri, 0);
    }
    protected boolean waitForMethodInvoked(RequestInfo ri, int maxSeconds) {
        if (ri == null) return false;
        boolean bResult = true;
        synchronized (ri) {
            for (int i=0; !ri.methodInvoked; i++) {
                try {
                    if (maxSeconds > 0) {
                        if (i >= maxSeconds) {
                            bResult = false;
                            break;
                        }
                    }
                    ri.wait(1000);
                }
                catch (Exception e) {}
            }
        }
        return bResult;
    }
    
    protected void notifyProcessedByServer(RequestInfo ri) {
        if (ri == null) return;
        synchronized (ri) {
            ri.processedByServerQueue = true;
            ri.notifyAll();
        }
    }
    protected void waitForProcessedByServer(RequestInfo ri) {
        if (ri == null) return;
        if (!ri.bind.usesQueue) return;
        
        // 20160215 dont wait if thread is already processing a que request
        Thread t = Thread.currentThread();
        if (t instanceof OARemoteThread) {
            OARemoteThread rt = (OARemoteThread) t;
            RequestInfo rix = rt.requestInfo;
            if (rix != null && ri != rix) {
                if (ri.bind.usesQueue && rix.bind.usesQueue) {
                    return;
                }
            }
        }
        
        synchronized (ri) {
            for (int i=0; !ri.processedByServerQueue; i++) {
                try {
                    ri.wait(1000);
                }
                catch (Exception e) {}
            }
            return;
        }
    }

    // 20160202
    private AtomicInteger aiMethodCallCnt = new AtomicInteger();
    private AtomicInteger aiReceivedMethodCallCnt = new AtomicInteger();
    
    /**
     * number of remote methods called.
     */
    public long getMethodCallCount() {
        return aiMethodCallCnt.get();
    }
    /*
     * number of methods/broadcast received
     */
    public long getReceivedMethodCount() {
        return aiReceivedMethodCallCnt.get();
    }
    
    public long getQueueHeadPos() {
        for (Map.Entry<String, OACircularQueue<RequestInfo>> entry : this.hmAsyncCircularQueue.entrySet()) { 
            OACircularQueue<RequestInfo> cq = entry.getValue();
            return cq.getHeadPostion();
        }        
        return 0;
    }

}
