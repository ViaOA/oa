/* This software and documentation is the confidential and proprietary information of ViaOA, Inc.
 * ("Confidential Information"). You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with ViaOA, Inc.
 * 
 * ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 * 
 * Copyright (c) 2001-2013 ViaOA, Inc. All rights reserved. */
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private ConcurrentHashMap<String, OACircularQueue<RequestInfo>> hmAsnycCircularQueue = new ConcurrentHashMap<String, OACircularQueue<RequestInfo>>();

    // track connections
    private ConcurrentHashMap<Integer, Session> hmSession = new ConcurrentHashMap<Integer, Session>();

    // types of commands sent from Client to server
    static final byte CtoS_Command_RunMethod = 0;
    static final byte CtoS_Command_GetLookupInfo = 1;
    static final byte CtoS_Command_RemoveSessionBroadcastThread = 2;
    static final byte CtoS_Command_GetBroadcastClass = 3;

    /**
     * Create a new RemoteServer using multiplexer.
     * 
     * @see MultiplexerServer#start() to have the server allow for client connections.
     * @see #start() to have this server start recieving remote calls.
     */
    public RemoteMultiplexerServer(MultiplexerServer server) {
        this.multiplexerServer = server;
    }

    /**
     * This can be called when MultiplexerServer.onClientDisconnet(..) is called. If this is not called,
     * then the next socket.IO method will throw an IOException.
     * 
     * @see MultiplexerServer#onClientDisconnet
     */
    public void removeSession(int connectionId) {
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
        Session session = getSession(connectionId);
        session.realSocket = socket;
    }

    protected Session getSession(int connectionId) {
        Session session = hmSession.get(connectionId);
        if (session == null) {
            session = new Session();
            session.connectionId = connectionId;
            hmSession.put(connectionId, session);
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
        t.setName("VServerSocket_CtoS");
        t.start();
        LOG.config("created Client to Server serversocket thread");
    }

    // new vsocket connection for client to server messages
    protected void onNewConnectionForCtoS(Socket socket) {
        final VirtualSocket iceSocket = (VirtualSocket) socket;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processSocketForCtoS(iceSocket);
                }
                catch (Exception e) {
                    if (!iceSocket.isClosed()) {
                        LOG.log(Level.WARNING, "error processing socket request", e);
                    }
                }
            }
        });
        t.setName("VSocket_CtoS." + iceSocket.getConnectionId() + "." + iceSocket.getId());
        t.start();
    }

    protected void processSocketForCtoS(final VirtualSocket socket) throws Exception {
        for (;;) {
            if (socket.isClosed()) {
                break;
            }

            RequestInfo ri = new RequestInfo();
            ri.socket = socket;
            ri.connectionId = ri.socket.getConnectionId();
            ri.vsocketId = ri.socket.getId();

            Session session = getSession(ri.connectionId);
            boolean bShouldReturnValue;
            try {
                bShouldReturnValue = processNextRequestForCtoS(ri, session);
            }
            catch (Exception e) {
                bShouldReturnValue = true;
                ri.exception = e;
            }
            catch (Throwable tx) {
                bShouldReturnValue = true;
                ri.exception = new Exception(tx.toString(), tx);
            }

            long t1 = System.nanoTime();
            // return response
            if (ri.bind != null && ri.bind.usesQueue) {
                OACircularQueue<RequestInfo> cq = hmAsnycCircularQueue.get(ri.bind.asyncQueueName);
                cq.addMessageToQueue(ri);
            }
            else if (bShouldReturnValue && (ri.methodInfo == null || !ri.methodInfo.noReturnValue)) {
                RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, session.hmClassDescOutput, session.aiClassDescOutput);
                oos.writeBoolean(ri.exception == null && ri.exceptionMessage == null);
                Object resp;
                if (ri.exception != null) {
                    if (ri.exception instanceof Serializable) {
                        resp = ri.exception;
                    }
                    else resp = new Exception(ri.exception.toString() + ", info: " + ri.toLogString());
                }
                else if (ri.exceptionMessage != null) {
                    resp = new Exception(ri.exceptionMessage + ", info: " + ri.toLogString());
                }
                else if (ri.responseBindName != null) {
                    resp = new Object[] { ri.responseBindName, ri.responseBindUsesQueue };
                }
                else resp = ri.response;
                oos.writeObject(resp);
                oos.flush();
            }

            ri.nsEnd = System.nanoTime();
            ri.nsWrite = ri.nsEnd - t1;

            afterInvokeForCtoS(ri);
        }
    }

    protected boolean processNextRequestForCtoS(final RequestInfo ri, final Session session) throws Exception {
        RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, session.hmClassDescInput);

        // wait for next message
        byte bCommand = ois.readByte();
        ri.msStart = System.currentTimeMillis();
        ri.nsStart = System.nanoTime();

        if (bCommand == CtoS_Command_GetLookupInfo) {
            // lookup, needs to return Java Interface class.
            ri.bindName = ois.readAsciiString();
            BindInfo bind = getBindInfo(ri.bindName);
            if (bind != null) {
                ri.response = new Object[] { bind.interfaceClass, bind.usesQueue, bind.isBroadcast };
                if (bind.usesQueue) {
                    session.setupAsyncQueueSender(bind.asyncQueueName, bind.name);
                }
            }
            else {
                ri.exceptionMessage = "object not found";
            }
            return true;
        }
        if (bCommand == CtoS_Command_GetBroadcastClass) {
            ri.bindName = ois.readAsciiString();
            BindInfo bind = getBindInfo(ri.bindName);
            if (bind != null) {
                if (!bind.isBroadcast) {
                    ri.exceptionMessage = "found, but not a broadcast remote object";
                }
                else {
                    ri.response = bind.interfaceClass;
                    session.setupAsyncQueueSender(bind.asyncQueueName, bind.name);
                }
            }
            else {
                ri.exceptionMessage = "object not found";
            }
            return true;
        }
        if (bCommand == CtoS_Command_RemoveSessionBroadcastThread) {
            // remove StoC thread used for broadcast object
            ri.bindName = ois.readAsciiString();
            session.removeBindInfo(ri.bindName);
            return false; // do not respond
        }
        // else: bCommand = CtoS_Command_RunMethod

        ri.bindName = ois.readAsciiString();
        ri.methodNameSignature = ois.readAsciiString();
        ri.args = (Object[]) ois.readObject();
        ri.bind = getBindInfo(ri.bindName);
        if (ri.bind == null) {
            ri.exceptionMessage = "bind Object not found on server";
            return true;
        }

        if (ri.bind.usesQueue) {
            ri.messageId = ois.readInt();
            session.setupAsyncQueueSender(ri.bind.asyncQueueName, ri.bindName);
        }
        ri.nsRead = System.nanoTime() - ri.nsStart;

        ri.object = ri.bind.getObject();
        ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
        if (ri.methodInfo != null) ri.method = ri.methodInfo.method;
        if (ri.method == null) {
            if (ri.exceptionMessage == null) ri.exceptionMessage = "method not found";
            return true;
        }

        // check for compressed params
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.compressedParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (!ri.methodInfo.compressedParams[i]) continue;
                ri.args[i] = ((OACompressWrapper) ri.args[i]).getObject();
            }
        }

        // check to see if any of the args[] are remote objects
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
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
                        bindx = session.createBindInfo(bindName, obj, ri.methodInfo.remoteParams[i]);
                    }
                }
                ri.args[i] = bindx.getObject();
            }
        }

        if (ri.bind.isBroadcast) {
            return true;
        }

        int x = (ri.args == null) ? 0 : ri.args.length;

        try {
            OAThreadLocalDelegate.setRemoteRequestInfo(ri);
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
        OAThreadLocalDelegate.setRemoteRequestInfo(null);


        if (ri.methodInfo == null || !ri.methodInfo.noReturnValue) {
            if (ri.response != null && ri.methodInfo.remoteReturn != null) {
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
                    else bindx = null;
                    if (bindx == null) {
                        // make remote
                        String bindNamex = "server." + aiBindCount.incrementAndGet(); // this will be sent to client
                        bindx = createBindInfo(bindNamex, ri.response, ri.methodInfo.remoteReturn);
                    }
                }
                ri.responseBindName = bindx.name; // this will be returned to client
                ri.responseBindUsesQueue = bindx.usesQueue;
                session.hmBindObject.put(bindx, ri.response); // make sure it wont get gc'd
            }
            else if (ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
                ri.response = new OACompressWrapper(ri.response);
            }
        }
        return true;
    }

    /**
     * Called after a CtoS remote method is called.
     */
    protected void afterInvokeForCtoS(RequestInfo ri) {
        if (ri == null) return;
        LOG.fine(ri.toLogString());
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
        t.setName("VServerSocket_StoC");
        t.start();
        LOG.config("created Server to Client serversocket thread");
    }

    /**
     * a client has created a new server to client (StoC) vsocket, that can be used for the server to
     * call methods on a client's remote object.
     */
    protected void onNewConnectionForStoC(Socket socket) {
        final VirtualSocket iceSocket = (VirtualSocket) socket;
        int connectionId = iceSocket.getConnectionId();
        Session session = getSession(connectionId);
        session.addSocketForStoC(iceSocket);
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

    protected Object onInvokeForStoC(Object proxyInstance, Session session, String bindName, Method method, Object[] args) throws Exception {
        RequestInfo ri = new RequestInfo();
        try {
            ri.msStart = System.currentTimeMillis();
            ri.nsStart = System.nanoTime();
            ri.object = proxyInstance;
            ri.bindName = bindName;
            ri.method = method;
            ri.args = args;
            onInvokeForStoC(session, ri);
        }
        catch (Exception e) {
            ri.exception = e;
        }
        finally {
            if (ri.socket != null) {
                session.addSocketForStoC(ri.socket);
            }
        }
        ri.nsEnd = System.nanoTime();
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
        if (ri.bind == null) {
            ri.exceptionMessage = "object was remved on client (GCd)";
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

        // compress flagged arguments
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
                        bindx = createBindInfo(bindNamex, ri.args[i], ri.methodInfo.remoteParams[i]);
                    }
                    else {
                        bindx.setObject(ri.args[i], referenceQueue);
                    }
                }
                session.hmBindObject.put(bindx, ri.args[i]); // hold the remote object from getting GCd
                ri.args[i] = bindx.name;
            }
        }

        ri.socket = session.getSocketForStoC();

        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, session.hmClassDescOutput, session.aiClassDescOutput);
        oos.writeBoolean(false); // flag to know this is a method call
        oos.writeBoolean(true);
        oos.writeAsciiString(ri.bind.name);
        oos.writeAsciiString(ri.methodInfo.methodNameSignature);
        oos.writeObject(ri.args);
        oos.flush();

        if (ri.methodInfo == null || !ri.methodInfo.noReturnValue) {
            RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, session.hmClassDescInput);
            if (ois.readBoolean()) {
                ri.response = ois.readObject();
            }
            else ri.exception = (Exception) ois.readObject();

            // check to see if return value is a remote object
            if (ri.methodInfo.remoteReturn != null) {
                String bindNamex = (String) ri.response;
                BindInfo bindx = session.getBindInfo(bindNamex);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    if (bindx == null) {
                        bindx = getBindInfo(ri.response);
                        objx = bindx != null ? bindx.weakRef.get() : null;
                        if (objx == null) bindx = null;
                    }
                    else bindx = null;
                    if (bindx == null) {
                        Object obj = createProxyForStoC(session, ri.methodInfo.remoteReturn, bindNamex);
                        bindx = createBindInfo(bindNamex, obj, ri.methodInfo.remoteReturn);
                    }
                }
                ri.response = bindx.getObject();
            }
            else if (ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
                ri.response = ((OACompressWrapper) ri.response).getObject();
            }
        }
    }

    /**
     * Called after a StoC remote method is called.
     */
    protected void afterInvokeForStoC(RequestInfo ri) {
        if (ri == null) return;
        LOG.log(Level.FINE, ri.toLogString(), ri.exception);
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
        BindInfo bind = createBindInfo(name, obj, interfaceClass, false, queueName, queueSize);
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
     * Create a queue that will be used by any remote objects for a class.
     * 
     * @param clazz
     *            type of remote object created
     * @param qname
     *            name of queue
     * @param size
     *            size of queue
     */
    public void registerClassWithQueue(Class clazz, String qname, int size) {
        ClassQueue cq = new ClassQueue();
        cq.clazz = clazz;
        cq.queueName = qname;
        cq.queueSize = size;
        hmClassQueue.put(clazz, cq);
    }

    /** used to map Class to a queue */
    private ConcurrentHashMap<Class<?>, ClassQueue> hmClassQueue = new ConcurrentHashMap<Class<?>, RemoteMultiplexerServer.ClassQueue>();

    class ClassQueue {
        Class clazz;
        String queueName;
        int queueSize;
    }

    /**
     * Create Bind information for a remote object.
     * 
     * @param name
     *            of object.
     * @param obj
     *            instance
     * @param interfaceClass
     *            the Interface of the obj. This is used when creating the proxy instance. Important: a
     *            weakref is used to store the remote object "obj"
     */
    protected BindInfo createBindInfo(String name, Object obj, Class interfaceClass) {
        return createBindInfo(name, obj, interfaceClass, false, null, -1);
    }

    protected BindInfo createBindInfo(String name, Object obj, Class interfaceClass, boolean bIsBroadcast, String queueName, int queueSize) {
        if (name == null || interfaceClass == null) {
            throw new IllegalArgumentException("name and interfaceClass can not be null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("interfaceClass must be a Java interface");
        }

        if (queueName == null) {
            // check to see if the remote class has a queue assigned for it
            ClassQueue cq = hmClassQueue.get(interfaceClass);
            if (cq == null) cq = hmClassQueue.get(obj.getClass());
            if (cq != null) {
                queueName = cq.queueName;
                queueSize = cq.queueSize;
            }
        }

        BindInfo bind = new BindInfo(name, obj, interfaceClass, referenceQueue, bIsBroadcast, queueName, queueSize);

        bind.loadMethodInfo();
        hmNameToBind.put(name, bind);
        if (bind.usesQueue) {
            OACircularQueue<RequestInfo> cq = hmAsnycCircularQueue.get(bind.asyncQueueName);
            if (cq == null) {
                cq = new OACircularQueue<RequestInfo>(bind.asyncQueueSize) {
                };
                hmAsnycCircularQueue.put(bind.asyncQueueName, cq);
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
    public Object createBroadcast(final String bindName, Object callback, Class interfaceClass, String queueName, int queueSize) {
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
        final BindInfo bind = createBindInfo(bindName, callback, interfaceClass, true, queueName, queueSize);
        if (callback != null) hmBindObject.put(bind, callback); // hold from getting gc'd

        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                RequestInfo ri = onInvokeBroadcast(bind, method, args);
                if (ri.object != null) {
                    synchronized (ri) {
                        for (; !ri.processedByServer;) {
                            try {
                                ri.wait();
                            }
                            catch (Exception e) {
                            }
                        }
                    }
                }
                ri.nsEnd = System.nanoTime();
                return ri.response;
            }
        };
        Object obj = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, handler);

        if (callback != null) {
            // create thread to get messages from queue
            setupBroadcastQueueReader(bind.asyncQueueName, bind.name);
        }
        return obj;
    }

    protected RequestInfo onInvokeBroadcast(BindInfo bind, Method method, Object[] args) throws Exception {
        RequestInfo ri = new RequestInfo();
        ri.connectionId = 0;
        ri.msStart = System.currentTimeMillis();
        ri.nsStart = System.nanoTime();
        ri.bindName = bind.name;
        ri.method = method;
        ri.args = args;
        ri.bind = bind;

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

        // compress flagged arguments
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
                        bindx = createBindInfo(bindNamex, ri.args[i], ri.methodInfo.remoteParams[i]);
                    }
                    else {
                        bindx.setObject(ri.args[i], referenceQueue);
                    }
                }
                ri.args[i] = bindx.name;
            }
        }

        // check to see if return is a primitive
        Class c = ri.method.getReturnType();
        if (c.isPrimitive()) {
            if (c.equals(boolean.class)) {
                ri.response = true;
            }
            else if (c.equals(int.class)) {
                ri.response = 0;
            }
            else if (c.equals(long.class)) {
                ri.response = 0L;
            }
            else if (c.equals(short.class)) {
                ri.response = (short) 0;
            }
            else if (c.equals(double.class)) {
                ri.response = 0.0D;
            }
            else if (c.equals(float.class)) {
                ri.response = 0.0F;
            }
        }

        // put "ri" in circular queue for clients to pick up.       
        OACircularQueue<RequestInfo> cque = hmAsnycCircularQueue.get(ri.bind.asyncQueueName);
        cque.addMessageToQueue(ri);
        return ri;
    }

    // queues that this session has a thread created to send to client
    private ConcurrentHashMap<String, String> hmAsyncQueue = new ConcurrentHashMap<String, String>();

    protected void setupBroadcastQueueReader(final String asyncQueueName, final String bindName) {
        synchronized (hmAsyncQueue) {
            if (hmAsyncQueue.get(asyncQueueName) != null) return;
            hmAsyncQueue.put(asyncQueueName, "");
        }

        final OACircularQueue<RequestInfo> cq = hmAsnycCircularQueue.get(asyncQueueName);
        final long qPos = cq.getHeadPostion();

        // set up thread that will get messages from queue and send to client
        final String threadName = "Broadcast.queue." + asyncQueueName;
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processBroadcastMessages(cq, bindName, qPos);
                }
                catch (Exception e) {
                    String s = "async queue thread exception, thread=" + threadName + ", thread is stopping, "
                            + "which will stop message from being sent to this client, queue=" + asyncQueueName;
                    LOG.log(Level.WARNING, s, e);
                }
            }
        });
        t.setName(threadName);
        t.start();
    }

    private void processBroadcastMessages(final OACircularQueue<RequestInfo> cque, final String bindName, long qpos) throws Exception {
        int cntMsg = 0;
        for (;;) {
            RequestInfo[] ris;
            ris = cque.getMessages(qpos, 50, 2000);
            
            if (ris == null) {  // remove any unneeded threads
                if (alRemoteClientThread.size() > 3) {
                    for (OARemoteThread rct : alRemoteClientThread) {
                        alRemoteClientThread.remove(rct);
                        synchronized (rct.Lock) {
                            rct.stopCalled = true;
                            rct.Lock.notify();
                        }
                        break;
                    }
                }
                continue;
            }
            
            qpos += ris.length;
            for (RequestInfo ri : ris) {
                if (ri.connectionId == 0) { // sent from object on this server
                    ri.processedByServer = true;
                    synchronized (ri) {
                        ri.notifyAll();
                    }
                    continue;
                }
                if (!ri.bind.isBroadcast) { // msg is using queue, but is not a broadcast
                    synchronized (ri) {
                        ri.processedByServer = true;
                        ri.notifyAll();
                    }
                    continue;
                }

                // sent by client, invoke method on object
                Object obj = ri.bind.getObject();
                if (obj == null) continue;

                OARemoteThread remoteThread = null;
                int x = alRemoteClientThread.size();
                for (int i=0; i<x; i++) {
                    OARemoteThread rct = alRemoteClientThread.get(cntMsg++ % x);
                    if (rct.requestInfo == null) {
                        remoteThread = rct;
                        break;
                    }
                }
                if (remoteThread == null) {
                    remoteThread = createRemoteClientThread();
                    alRemoteClientThread.add(remoteThread);
                    if (alRemoteClientThread.size() > 20) {
                        LOG.warning("alRemoteClientThread.size() = " + alRemoteClientThread.size());
                    }
                }
                
                long ms1 = System.currentTimeMillis();                    
                synchronized (remoteThread.Lock) {
                    remoteThread.requestInfo = ri;
                    remoteThread.Lock.notify(); // so that remoteThread will call processBroadcast(ri)
                    remoteThread.Lock.wait(1250);
                }
                long ms2 = System.currentTimeMillis();

                // qqqqqq this can be removed, sanity check only
                if (!ri.processedByServer && (ms2-ms1) > 1200) {
                    StackTraceElement[] stes = remoteThread.getStackTrace();
                    Exception ex = new Exception();
                    ex.setStackTrace(stes);
                    LOG.log(Level.WARNING, "timeout waiting for message, will continue, this is stacktrace for remoteThread, request="
                            + ri.toLogString(), ex);
                }
            }
        }
    }

    // use OARemoteThread to process broadcast messages on the server
    private AtomicInteger aiClientThreadCount = new AtomicInteger();
    private ArrayList<OARemoteThread> alRemoteClientThread = new ArrayList<OARemoteThread>();

    private OARemoteThread createRemoteClientThread() {
        OARemoteThread t = new OARemoteThread() {
            @Override
            public void run() {
                for ( ;!stopCalled; ) {
                    try {
                        synchronized (Lock) {
                            reset();
                            if (requestInfo == null) {
                                Lock.wait();
                                if (requestInfo == null) continue;
                            }
                        }

                        processBroadcast(this.requestInfo);
                        this.msLastUsed = System.currentTimeMillis();                

                        synchronized (Lock) {
                            this.requestInfo = null;
                            Lock.notify(); // notify socket reader thread to continue to next message
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "error in remoteThread loop, will continue", e);
                    }
                }
            }

            @Override
            public void startNextThread() {
                super.startNextThread();
                synchronized (Lock) {
                    Lock.notify(); // lets the main queue reader thread get the next msg
                }
            }
        };
        t.setName("OARemoteThread." + aiClientThreadCount.getAndIncrement());
        t.start();
        return t;
    }

    protected void processBroadcast(RequestInfo ri) throws Exception {
        try {
            OAThreadLocalDelegate.setRemoteRequestInfo(ri);
            ri.response = ri.method.invoke(ri.object, ri.args);
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
        catch (Exception e) {
            ri.exception = e;
        }
        ri.processedByServer = true;
        OAThreadLocalDelegate.setRemoteRequestInfo(null);
        synchronized (ri) {
            ri.notifyAll(); // waiting clients getting messages from queue 
        }
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
        private boolean bDisconnected;

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
                if (bindx.weakRef.get() == obj) {
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
            boolean bRequestedNew = false;
            boolean bWaitedForFirst = false;
            for (int i = 0; socket == null; i++) {
                boolean bRequestNew = false;
                synchronized (alSocketFromStoC) {
                    if (bDisconnected) {
                        throw new Exception("closed connection/session=" + connectionId);
                    }
                    int x = alSocketFromStoC.size();
                    if (x > 1) {
                        socket = alSocketFromStoC.remove(0);
                    }
                    else if (x == 1 && !bRequestedNew) {
                        // request client to open more CtoS sockets
                        bRequestNew = true;
                        socket = alSocketFromStoC.remove(0);
                    }
                    else if (x == 0 && !bWaitedForFirst) {
                        alSocketFromStoC.wait(250);
                        bWaitedForFirst = true;
                    }
                    else if (x == 0 && i > 10) {
                        throw new Exception("no StoC sockets available for connection/session=" + connectionId);
                    }
                    else {
                        alSocketFromStoC.wait(100);
                    }
                }
                if (bRequestNew) {
                    RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket);
                    oos.writeBoolean(true); // this will tell client to create more StoC sockets
                    oos.flush();
                    bRequestedNew = true;
                    releaseSocketForStoC(socket);
                    socket = null;
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

        public BindInfo createBindInfo(String name, Object obj, Class interfaceClass) {
            if (name == null || interfaceClass == null) {
                throw new IllegalArgumentException("name and interfaceClass can not be null");
            }
            if (!interfaceClass.isInterface()) {
                throw new IllegalArgumentException("interfaceClass must be a Java interface");
            }
            BindInfo bind = new BindInfo(name, obj, interfaceClass, null, false, null, -1); // dont need to use referenceQueue
            bind.loadMethodInfo();
            hmNameToBind.put(name, bind);
            return bind;
        }

        // start thread that will send async return values back to client
        public void setupAsyncQueueSender(final String asyncQueueName, final String bindName) {
            synchronized (hmAsyncQueue) {
                if (hmAsyncQueue.get(asyncQueueName) != null) return;

                hmAsyncQueue.put(asyncQueueName, "");
                final OACircularQueue<RequestInfo> cq = hmAsnycCircularQueue.get(asyncQueueName);
                final long qPos = cq.getHeadPostion();

                // set up thread that will get messages from queue and send to client
                final String threadName = "Client." + connectionId + ".queue." + asyncQueueName;
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            writeQueueMessages(cq, bindName, qPos);
//qqqqqqqqqq need to send client overflow error
//qqqqq so that it can show error and disconnect                           
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
                t.start();
            }
        }

        private void writeQueueMessages(final OACircularQueue<RequestInfo> cque, final String clientBindName, final long startQuePos)
                throws Exception {
            // have all messages sent using single vsocket
            VirtualSocket vsocket = getSocketForStoC();
            try {
                _writeQueueMessages(cque, clientBindName, vsocket, startQuePos);
            }
            finally {
                releaseSocketForStoC(vsocket);
            }
        }

        // used to send broadcast messages to client
        private void _writeQueueMessages(final OACircularQueue<RequestInfo> cque, final String bindName, VirtualSocket vsocket, long qpos)
                throws Exception {
            int connectionId = vsocket.getConnectionId();

            for (;;) {
                if (vsocket.isClosed()) {
                    return;
                }

                RequestInfo[] ris = null;
                try {
                    ris = cque.getMessages(qpos, 100);
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "Message queue overrun with msg CircularQueue", e);
                    onException(connectionId, "Message queue overrun", "Message queue overrun", e, true);
                    throw e;
                }
                if (ris == null) {
                    continue;
                }
                qpos += ris.length;
                for (RequestInfo ri : ris) {
                    if (vsocket.isClosed()) return;
                    if (!ri.bind.isBroadcast) {
                        if (ri.connectionId != connectionId) {
                            continue;
                        }
                    }
                    else {
                        synchronized (ri) {
                            for (; !ri.processedByServer;) {
                                try {
                                    ri.wait();
                                }
                                catch (Exception e) {
                                }
                            }
                        }
                    }

                    //qqqqq  this is not used for async, need a way for async queue
                    if (ri.bind.isBroadcast) {
                        // if (getBindInfo(bindName) == null) return; // client has removed it
                    }

                    RemoteObjectOutputStream oos = new RemoteObjectOutputStream(vsocket, hmClassDescOutput, aiClassDescOutput);
                    oos.writeBoolean(false); // flag to know this is a method call
                    oos.writeBoolean(false); // do not return a response
                    oos.writeAsciiString(ri.bindName);
                    if (!ri.bind.isBroadcast || ri.connectionId == connectionId) {
                        oos.writeBoolean(true);
                        if (ri.exception != null) oos.writeByte(0);
                        else if (ri.exceptionMessage != null) oos.writeByte(1);
                        else oos.writeByte(2);

                        if (ri.exception != null) oos.writeObject(ri.exception);
                        else if (ri.exceptionMessage != null) oos.writeObject(ri.exceptionMessage);
                        else oos.writeObject(ri.response);
                        oos.writeInt(ri.messageId);
                    }
                    else {
//qqqqqqq create hook to see if the client will need this                        
                        oos.writeBoolean(false); // broadcast
                        oos.writeAsciiString(ri.methodInfo.methodNameSignature);
                        oos.writeObject(ri.args);
                    }
                    oos.flush();
                }
            }
        }
    }
}
