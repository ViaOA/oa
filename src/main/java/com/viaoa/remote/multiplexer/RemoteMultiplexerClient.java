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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.comm.multiplexer.io.VirtualSocket;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.BindInfo;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.io.RemoteObjectInputStream;
import com.viaoa.remote.multiplexer.io.RemoteObjectOutputStream;
import com.viaoa.util.OACompressWrapper;
import com.viaoa.util.OAPool;
import com.viaoa.util.OAReflect;


/*** DEBUGing 
 *  use this for debugging, so that remote methods wont timeout while debugging:  
 *      MultiplexerClient.DEBUG = true;
 */


/**
 * Remoting client, that allows a client to access Objects on a server, and call methods on those
 * objects. It allows for any method to have args that are remote objects, which would allow the server
 * to call the client. A method can also return a remote object.
 * 
 * Broadcasting is supported, where calling a method on a remote object will be invoked on all other
 * clients and server.
 * 
 * This is similar to RMI, except that it allows for many objects (on either server or client) to be
 * remote, uses multiplexer, and less overhead.
 * 
 * Example: get a remote object "A" from server call method "a.test(argX)", where arg is a RemoteClass -
 * the server will then be able to call methods on argX.
 *
 * Note:
 * OARemoteThread is used to process requests. 
 * 
 * @author vvia
 */
public class RemoteMultiplexerClient {
    private static Logger LOG = Logger.getLogger(RemoteMultiplexerClient.class.getName());

    // / multiplexer client
    private MultiplexerClient multiplexerClient;

    // remote objects that have already been retrieved from server.
    private ConcurrentHashMap<String, Object> hmLookup = new ConcurrentHashMap<String, Object>();

    // used to uniquely identify any objects that this client sends to server.
    private AtomicInteger aiBindCount = new AtomicInteger();

    // pool of vsockets 
    private OAPool<VirtualSocket> poolVirtualSocketCtoS;

    // mapping for Remote objects
    private ConcurrentHashMap<String, BindInfo> hmNameToBind = new ConcurrentHashMap<String, BindInfo>();
    // used to manage GC for remote objects.  See performDGC.
    private ReferenceQueue referenceQueue = new ReferenceQueue();

    // performance enhancement for ObjectSteams
    private ConcurrentHashMap<Integer, ObjectStreamClass> hmClassDescInput = new ConcurrentHashMap<Integer, ObjectStreamClass>();
    private ConcurrentHashMap<String, Integer> hmClassDescOutput = new ConcurrentHashMap<String, Integer>();
    private AtomicInteger aiClassDescOutput = new AtomicInteger();

    private ConcurrentHashMap<Integer, RequestInfo> hmAsyncRequestInfo = new ConcurrentHashMap<Integer, RequestInfo>();
    private AtomicInteger aiMessageId = new AtomicInteger();

    /**
     * Creates a new Distributed Client, using the ICEClient multiplexer connection as the transport.
     */
    public RemoteMultiplexerClient(MultiplexerClient multiplexerClient) {
        LOG.fine("new multiplexer client");
        if (multiplexerClient == null) throw new IllegalArgumentException("multiplexerClient is required");
        this.multiplexerClient = multiplexerClient;
        setupRequestQueueThread();
        setupSyncRequestQueueThread();
    }

    public MultiplexerClient getMultiplexerClient() {
        return multiplexerClient;
    }

    /**
     * Register a remote object to be called for server broadcasts.
     * 
     * @param lookupName
     *            name used on server, see: RemoteMultiplexerServer.createClientBroadcast
     * @param callback
     *            an impl used when receiving messages from other clients
     * @see RemoteMultiplexerServer#createClientBroadcast(String, Class)
     */
    public void registerBroadcast(final String lookupName, Object callback) throws Exception {
        lookupBroadcast(lookupName, callback);
    }
    public Object lookupBroadcast(final String lookupName, Object callback) throws Exception {
        if (lookupName == null) throw new IllegalArgumentException("lookupName cant be null");
        if (callback == null) throw new IllegalArgumentException("callback cant be null");
        Object proxyInstance = hmLookup.get(lookupName);
        if (proxyInstance != null) return proxyInstance;
        LOG.fine("lookupName=" + lookupName);

        VirtualSocket socket = getSocketForCtoS();
        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, hmClassDescOutput, aiClassDescOutput);

        oos.writeByte(RequestInfo.Type.CtoS_GetBroadcastClass.ordinal());
        oos.writeAsciiString(lookupName);
        oos.flush();

        RemoteObjectInputStream ois = new RemoteObjectInputStream(socket, hmClassDescInput);
        Exception ex = null;
        Class c = null;
        if (!ois.readBoolean()) {
            ex = (Exception) ois.readObject();
        }
        else {
            c = (Class) ois.readObject();
            ;
        }

        releaseSocketForCtoS(socket);
        LOG.fine("lookupName=" + lookupName + ", interface class=" + c);
        if (ex != null) throw ex;

        if (!c.isAssignableFrom(callback.getClass())) {
            throw new Exception("callback must be same class as " + c);
        }

        proxyInstance = getProxyForBroadcast(lookupName, c, callback);
        hmLookup.put(lookupName, proxyInstance);
        return proxyInstance;
    }

    /**
     * Get a remote object from the server.
     * 
     * @param lookupName
     *            name that the server has used to bind the object.
     */
    public Object lookup(String lookupName) throws Exception {
        LOG.fine("lookupName=" + lookupName);
        if (lookupName == null) return null;
        Object proxyInstance = hmLookup.get(lookupName);
        if (proxyInstance != null) return proxyInstance;

        VirtualSocket socket = getSocketForCtoS();
        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, hmClassDescOutput, aiClassDescOutput);

        // 20130601 changed from boolean to byte
        oos.writeByte(RequestInfo.Type.CtoS_GetLookupInfo.ordinal());
        oos.writeAsciiString(lookupName);
        oos.flush();

        RemoteObjectInputStream ois = new RemoteObjectInputStream(socket, hmClassDescInput);
        if (!ois.readBoolean()) {
            Exception ex = new Exception((String) ois.readObject());
            throw ex;
        }
        Object[] objs = (Object[]) ois.readObject();
        Class c = (Class) objs[0];
        boolean bUsesQueue = (Boolean) objs[1];
        boolean bIsBroadcast = (Boolean) objs[2];
        if (bIsBroadcast) {
            throw new Exception("must use lookupBroadcast() for " + lookupName + ", instead of lookup()");
        }

        releaseSocketForCtoS(socket);
        LOG.fine("lookupName=" + lookupName + ", interface class=" + c);

        if (c != null) {
            proxyInstance = getProxyForCtoS(lookupName, c, bUsesQueue);
            hmLookup.put(lookupName, proxyInstance);
        }
        return proxyInstance;
    }

    /**
     * Get the real socket.
     */
    public Socket getSocket() {
        return multiplexerClient.getSocket();
    }

    /** create a name that will be unique on the server. */
    protected String createBindName(RequestInfo ri) {

        String bindName = "C." + ri.socket.getConnectionId() + "." + aiBindCount.incrementAndGet();
        return bindName;
    }

    private ConcurrentHashMap<String, Object> hmProxyCtoS = new ConcurrentHashMap<String, Object>();

    /**
     * Create a proxy instance for an Object that is on the server. This is used for lookups and when
     * the server returns a remote instance. All methods that are called on the proxy will be sent to
     * the server, and act as-if it were ran locally.
     */
    protected Object getProxyForCtoS(RequestInfo ri, String name, Class c, boolean bDontUseQueue) throws Exception {
        return getProxyForCtoS(name, c, (ri.bind.usesQueue && !bDontUseQueue));
    }

    protected Object getProxyForCtoS(String name, Class c, boolean bUsesQueue) throws Exception {
        if (name == null) return null;
        Object proxy = hmProxyCtoS.get(name);
        if (proxy != null) return proxy;

        final BindInfo bind = getBindInfo(name, null, c, bUsesQueue, false);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = RemoteMultiplexerClient.this.onInvokeForCtoS(bind, proxy, method, args);
                return result;
            }
        };
        proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, handler);
        hmProxyCtoS.put(name, proxy);
        bind.setObject(proxy, referenceQueue);

        if (bind.usesQueue && bUsesQueue) {
            if (!bFirstStoCsocketCreated) {
                createSocketForStoC(); // to process message from server to this object
            }
        }

        LOG.fine("Created proxy instance, class=" + c + ", name=" + name);
        return proxy;
    }

    private ConcurrentHashMap<String, Object> hmProxyBroadcast = new ConcurrentHashMap<String, Object>();

    protected Object getProxyForBroadcast(String name, Class c, Object callback) throws Exception {
        if (name == null) return null;
        Object proxy = hmProxyBroadcast.get(name);
        if (proxy != null) return proxy;

        final BindInfo bind = getBindInfo(name, callback, c, true, true);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = RemoteMultiplexerClient.this.onInvokeForCtoS(bind, proxy, method, args);
                return result;
            }
        };
        proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, handler);
        hmProxyBroadcast.put(name, proxy);

        if (!bFirstStoCsocketCreated) {
            createSocketForStoC(); // to process message from server to this object
        }
        LOG.fine("Created proxy instance, class=" + c + ", name=" + name);
        return proxy;
    }

    // volatile static int threadCheck;    
    protected Object onInvokeForCtoS(BindInfo bind, Object proxy, Method method, Object[] args) throws Throwable {
        //LOG.fine(method.getName());
        aiMethodCallCnt.incrementAndGet();
        
        RequestInfo ri = new RequestInfo();
        // 1:CtoS_QueuedRequest start
        // 1:CtoS_QueuedRequestNoResponse

        if (Thread.currentThread() instanceof OARemoteThread) {
            //if (threadCheck++ < 50) LOG.log(Level.WARNING, "Info only: bind="+bind.name+", method="+method.getName(), new Exception("RemoteThread used for CtoS method call"));            
        }

        VirtualSocket socket = getSocketForCtoS(); // used to send message
        try {
            ri.msStart = System.currentTimeMillis();
            ri.nsStart = System.nanoTime();
            ri.socket = socket;
            ri.connectionId = socket.getConnectionId();
            ri.messageId = aiMessageId.incrementAndGet();
            ri.vsocketId = socket.getId();
            ri.object = proxy;
            ri.bind = bind;
            ri.bindName = bind.name;
            ri.method = method;
            ri.args = args;
            ri.methodInfo = ri.bind.getMethodInfo(ri.method);
            if (ri.methodInfo != null) {
                ri.methodNameSignature = ri.methodInfo.methodNameSignature;
                ri.socket.setTimeoutSeconds(ri.methodInfo.timeoutSeconds);
            }
            ri.isRemoteThread = (Thread.currentThread() instanceof OARemoteThread);
            
            ri.bSent = _onInvokeForCtoS(ri);

            // 4:CtoS_QueuedRequestNoResponse END
  
            if (ri.bSent && (ri.bind.usesQueue && (ri.type.hasReturnValue() || ri.bind.isOASync)) ) {
                releaseSocketForCtoS(socket);
                socket = null;
                // 4:CtoS_QueuedRequest wait on return value from server
                synchronized (ri) {
                    for (int i = 0; ; i++) {
                        if (ri.methodInvoked) break;
                        if (i > 0) {
                            if (!multiplexerClient.isConnected()) break;
                            if (ri.methodInfo.timeoutSeconds > 0 && i >= ri.methodInfo.timeoutSeconds) {
                                if (!MultiplexerClient.DEBUG && !MultiplexerServer.DEBUG) {
                                    break;
                                }
                            }
                        }
                        ri.wait(1000); // request timeout
                    }
                }
                // 7:CtoS_QueuedRequest END
                if (!ri.methodInvoked) {
                    ri.exceptionMessage = "timeout waiting on response from server";
                }
                else {
                    // 20160122 queue thread will wait for OARemoteThreadDelegate.startNextThread()
                    //    to call OAThreadLocalDelegate.notifyWaitingThread(), and wake up que thread waiting on ri lock
                    if (ri.bind.isOASync) {
                        OAThreadLocalDelegate.setNotifyObject(ri);
                    }
                }
            }
        }
        catch (Exception e) {
            ri.exception = e;
        }
        finally {
            ri.nsEnd = System.nanoTime();
            if (socket != null) releaseSocketForCtoS(socket);
        }
        afterInvokeForCtoS(ri);

        if (ri.exception != null) throw ri.exception;
        if (ri.exceptionMessage != null) {
            Exception ex = new Exception(ri.exceptionMessage + ", info: " + ri.toLogString());
            throw ex;
        }
        return ri.response;
    }

    /**
     * Called after a CtoS remote method is called.
     */
    protected void afterInvokeForCtoS(RequestInfo ri) {
        if (ri == null || !ri.bSent) return;
        //LOG.fine(ri.toLogString());
    }

    // "dummy" object, that is used when methods are not supported in proxy interface, but are in Object
    // class
    private final Object stuntObject = new Object();
    private int errorCnt;

    /**
     * Called when a remote/proxy object method is invoked. The method info will be sent to the server,
     * and return the method return value from the server.
     */
    protected boolean _onInvokeForCtoS(RequestInfo ri) throws Exception {
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
                        for (int i = 0; i < 10; i++) {
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
            else {
                ri.exceptionMessage = "Method not found in Methods";
            }
            return false;
        }

        // check if remoteThread, and if it has already processed it's msg before calling remote method
        if (!OARemoteThreadDelegate.isSafeToCallRemoteMethod()) {
            if (errorCnt++ < 25 || (errorCnt % 100 == 0)) {
                //Exception e = new Exception("isSafeToCallRemoteMethod is false");
                //LOG.log(Level.WARNING, "note: isSafeToCallRemoteMethod is false, will continue, starting another OARemoteThread", e);
            }
            OARemoteThreadDelegate.startNextThread();
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

        // check to see if any of the args[] are remote objects, that will have
        // the server call the methods on this client.
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i = 0; i < ri.methodInfo.remoteParams.length && i < ri.args.length; i++) {
                if (ri.methodInfo.remoteParams[i] == null) continue;
                if (ri.args[i] == null) continue;

                BindInfo bindx = getBindInfoForObject((Object) ri.args[i]);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    
                    boolean b = ri.methodInfo.dontUseQueues != null && ri.methodInfo.dontUseQueues[i];
                    bindx = getBindInfo(ri, createBindName(ri), ri.args[i], ri.methodInfo.remoteParams[i], b);
                    if (!bFirstStoCsocketCreated) {
                        createSocketForStoC(); // to process message from server to this object
                    }
                }
                ri.args[i] = bindx.name;
            }
        }

        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);

        
        // set the correct type of message that this will be, which determines how it will be handled.
        if (ri.bind.usesQueue && ri.methodInfo.returnOnQueueSocket) {
            ri.type = RequestInfo.Type.CtoS_ReturnOnQueueSocket;
        }
        else if (ri.bind.usesQueue && !ri.methodInfo.dontUseQueue) {
            if (ri.bind.isBroadcast) {
                ri.type = RequestInfo.Type.CtoS_QueuedBroadcast;
            }
            else if (ri.methodInfo != null && ri.methodInfo.noReturnValue) {
                // 2:CtoS_QueuedRequestNoResponse
                ri.type = RequestInfo.Type.CtoS_QueuedRequestNoResponse;
            }
            else {
                // 2:CtoS_QueuedRequest send to server
                ri.type = RequestInfo.Type.CtoS_QueuedRequest;
            }
        }
        else {
            if (ri.methodInfo != null && ri.methodInfo.noReturnValue) {
                ri.type = RequestInfo.Type.CtoS_SocketRequestNoResponse;
            }
            else {
                ri.type = RequestInfo.Type.CtoS_SocketRequest;
            }
        }
        
        if (ri.type.usesQueue() && (ri.type.hasReturnValue() || ri.bind.isOASync) ) {
            // 3:CtoS_QueuedRequest put in hm to wait on server response
            hmAsyncRequestInfo.put(ri.messageId, ri); // used to wait for server to send it back on StoC
            if (!bFirstStoCsocketCreated) {
                createSocketForStoC(); // to process message from server to this object
            }
        }
        else if (!ri.type.hasReturnValue()) {
            // 3:CtoS_QueuedRequestNoResponse
            ri.response = OAReflect.getEmptyPrimitive(ri.method.getReturnType());
        }
        
        oos.writeByte(ri.type.ordinal());
        oos.writeAsciiString(ri.bind.name);
        oos.writeAsciiString(ri.methodNameSignature);
        oos.writeObject(ri.args);

        if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
            oos.writeInt(ri.connectionId);
            oos.writeInt(ri.messageId);
        }
        else if (ri.type.usesQueue() && (ri.type.hasReturnValue() || ri.bind.isOASync)) {
            oos.writeInt(ri.messageId);
        }
        oos.flush();

        if (ri.type == RequestInfo.Type.CtoS_SocketRequest) {
            RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, hmClassDescInput);
            int x = ois.readByte();
            if (x == 0) {
                ri.exception = (Exception) ois.readObject();
            }
            else if (x == 1) {
                ri.exceptionMessage = (String) ois.readObject();
            }
            else if (x == 2) {
                Object[] responses = (Object[]) ois.readObject();
                String bindName = (String) responses[0];

                BindInfo bindx = getBindInfo(bindName);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    boolean bUsesQueue = (Boolean) responses[1];
                    Object obj = getProxyForCtoS(bindName, ri.methodInfo.remoteReturn, bUsesQueue);
                    ri.response = obj;
                }
                else ri.response = bindx.getObject();
            }
            else {
                ri.response = ois.readObject();
                if (ri.response != null && ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
                    ri.response = ((OACompressWrapper) ri.response).getObject();
                }
            }
            ri.methodInvoked = true;
        }
        return true;
    }

    public void setMinimumSocketsForCtoS(int x) {
        getVirtualSocketCtoSPool().setMinimum(x);
    }

    public int getMinimumSocketsForCtoS() {
        if (poolVirtualSocketCtoS == null) return 1;
        return getVirtualSocketCtoSPool().getMinimum();
    }

    public void setMaximumSocketsForCtoS(int x) {
        getVirtualSocketCtoSPool().setMaximum(x);
    }

    public int getMaximumSocketsForCtoS() {
        if (poolVirtualSocketCtoS == null) return 0;
        return getVirtualSocketCtoSPool().getMaximum();
    }

    protected OAPool<VirtualSocket> getVirtualSocketCtoSPool() {
        if (poolVirtualSocketCtoS != null) return poolVirtualSocketCtoS;
        poolVirtualSocketCtoS = new OAPool<VirtualSocket>(getMinimumSocketsForCtoS(), getMaximumSocketsForCtoS()) {
            @Override
            protected void removed(VirtualSocket vs) {
                try {
                    vs.close();
                }
                catch (Exception e) {
                    throw new RuntimeException("Error while closing vsocket", e);
                }
            }

            @Override
            protected VirtualSocket create() {
                VirtualSocket vs = null;
                try {
                    vs = multiplexerClient.createSocket("CtoS");
                }
                catch (Exception e) {
                    throw new RuntimeException("Error while creating a new vsocket", e);
                }
                return vs;
            }
        };
        poolVirtualSocketCtoS.setHighMarkTimeLimit(10000);
        return poolVirtualSocketCtoS;
    }

    protected VirtualSocket getSocketForCtoS() throws Exception {
        VirtualSocket vs = getVirtualSocketCtoSPool().get();
        return vs;
    }

    protected void releaseSocketForCtoS(VirtualSocket vs) throws Exception {
        if (vs == null) return;
        vs.setTimeoutSeconds(0);
        if (vs.isClosed()) {
            getVirtualSocketCtoSPool().remove(vs);
        }
        else {
            getVirtualSocketCtoSPool().release(vs);
        }
    }

    // used to assign unique int for each StoC vsocket 
    private AtomicInteger aiCountForStoC = new AtomicInteger();
    // flag to know if the initial StoC vsocket has been created
    private volatile boolean bFirstStoCsocketCreated;

    /**
     * These are vsockets used to listen/wait for method calls from server. This is used when a client
     * sends a remote object to the server, so that server can then call methods on it, and have it
     * invoked on the client.
     * 
     * On the server, each client has a session that has a list of the StoC vsockets.
     */
    protected void createSocketForStoC() throws Exception {
        final VirtualSocket socket = (VirtualSocket) multiplexerClient.createSocket("StoC");
        final int id = aiCountForStoC.getAndIncrement();
        // accept new connections
        Thread t = new Thread(new Runnable() {
            public void run() {
                int errorCnt = 0;
                long msLastError = 0;
                /* 20151103 on hold for OAsyncCombinedClient work
                OAThreadLocalDelegate.setRemoteMultiplexerClient(RemoteMultiplexerClient.this);
                */
                for (;;) {
                    try {
                        if (socket.isClosed()) {
                            break;
                        }
                        processStoCSocket(socket, id);
                    }
                    catch (Exception e) {
                        if (!socket.isClosed()) {
                            errorCnt++;
                            long ms = System.currentTimeMillis();
                            if (msLastError == 0 || ms - msLastError > 5000 || errorCnt < 5) {
                                LOG.log(Level.WARNING, "Exception in StoC thread, errorCnt=" + errorCnt, e);
                                if (errorCnt > 50) break;
                                msLastError = ms;
                            }
                        }
                    }
                }
            }
        });
        t.setName("OASocket_StoC." + socket.getConnectionId() + "." + socket.getId());
        t.setDaemon(true);
        t.start();
        bFirstStoCsocketCreated = true;
        LOG.fine("created StoC socket and thread, connectionId=" + socket.getConnectionId() + ", vid=" + id);
    }

    private final AtomicInteger aiClientThreadCount = new AtomicInteger();
    private final ArrayList<OARemoteThread> alRemoteClientThread = new ArrayList<OARemoteThread>();

    private OARemoteThread getRemoteClientThread(RequestInfo ri, boolean bSendMessgage) {
        
        OARemoteThread remoteThread;
        synchronized (alRemoteClientThread) {
            for (int i=0; ; i++) {
                for (OARemoteThread rt : alRemoteClientThread) {
                    synchronized (rt.Lock) {
                        if (rt.requestInfo == null) {
                            rt.requestInfo = ri;
                            rt.setSendMessages(bSendMessgage);
                            return rt;
                        }
                    }
                }

                // note: too many threads can increase the vsockets, and reduce the msgQue speed
                
                int x = alRemoteClientThread.size();
                if (x < 10) break;
                if (x < 15) {
                    if (i > 2) break;   // 50ms
                }
                else if (x < 20) {
                    if (i > 4) break;   // 100ms
                }
                else if (x < 30) {
                    if (i > 8) break;   // 200ms
                }
                else if (x < 40) {
                    if (i > 20) break;  // 500ms
                }
                else if (x < 50) {
                    if (i > 40) break;  // 1 second
                }
                else if (x < 100) {
                    if (i > 60) break;  // 1.5 seconds 
                }
                else {
                    // otherwise 100 is max and need to wait
                    if (i > 0 && i % 100 == 0) {
                        LOG.warning("waiting on free remoteThread to use, waitTime="+(i*25)+"ms");
                    }
                }
                
                try {
                    alRemoteClientThread.wait(25);
                }
                catch (Exception e) {
                }
            }
        }
        remoteThread = createRemoteClientThread();
        remoteThread.setSendMessages(bSendMessgage);
            
        synchronized (alRemoteClientThread) {
            remoteThread.requestInfo = ri;
            alRemoteClientThread.add(remoteThread);
        }
        LOG.fine("new remoteThread created, liveCount=" + alRemoteClientThread.size()+", totalCreated="+aiClientThreadCount.get());
        onRemoteThreadCreated(aiClientThreadCount.get(), alRemoteClientThread.size());
        return remoteThread;
    }
    protected void onRemoteThreadCreated(int totalCount, int liveCount) {
    }

    private OARemoteThread createRemoteClientThread() {
        OARemoteThread t = new OARemoteThread() {
            @Override
            public void run() {
                /* 20151103 on hold for OAsyncCombinedClient work
                OAThreadLocalDelegate.setRemoteMultiplexerClient(RemoteMultiplexerClient.this);
                */
                for (; ;) {
                    try {
                        synchronized (Lock) {
                            if (stopCalled) {
                                break;
                            }
                            reset();
                            if (requestInfo == null) {
                                Lock.wait();
                                if (requestInfo == null) continue;
                            }
                        }

                        this.msLastUsed = System.currentTimeMillis();
                        processMessageForStoC(requestInfo);
                        this.msLastUsed = System.currentTimeMillis();

                        synchronized (Lock) {
                            if (requestInfo != null) {
                                requestInfo.methodInvoked = true;
                                this.requestInfo = null;
                            }
                            Lock.notifyAll();
                        }
                        shouldClose(this);
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "error in OARemoteThread", e);
                    }
                }
            }

            @Override
            public void startNextThread() {
                if (startedNextThread) return;
                super.startNextThread();
                synchronized (Lock) {
                    if (requestInfo != null) requestInfo.methodInvoked = true;                    
                    Lock.notify();
                }
            }
        };
        t.setDaemon(true);
        t.setName("OARemoteThread." + aiClientThreadCount.getAndIncrement());
        t.start();
        //LOG.fine("thread name=" + t.getName());
        return t;
    }


    private boolean shouldClose(final OARemoteThread remoteThread) {
        if (alRemoteClientThread.size() < 4) return false;
        long msNow = System.currentTimeMillis();
        int cnt = 0;
        int minFree = 2;
        int msMax = 2000;
        
        int x = alRemoteClientThread.size(); 
        if (x > 10) {
            minFree = 4;
            if (x > 40) msMax = 250;
            else msMax = 500;
        }
        
        synchronized (alRemoteClientThread) {
            for (OARemoteThread rt : alRemoteClientThread) {
                synchronized (rt.Lock) {
                    if (rt == remoteThread) {
                        if (rt.requestInfo != null) return false;
                        continue;
                    }
                    if (rt.requestInfo == null) {
                        if (rt.msLastUsed + msMax < msNow) { 
                            cnt++;
                        }
                    }
                }
                if (cnt > minFree) {
                    alRemoteClientThread.remove(remoteThread);
                    remoteThread.stopCalled = true;
                    return true;
                }
            }
            alRemoteClientThread.notifyAll();
        }
        return false;
    }

int qqq=0;//qqqqqqqqqqq    
    protected void processStoCSocket(final VirtualSocket socket, int threadId) throws Exception {
        if (socket.isClosed()) return;
        RemoteObjectInputStream ois = new RemoteObjectInputStream(socket, hmClassDescInput);
        // wait for next message
        RequestInfo.Type type = RequestInfo.getType(ois.readByte());
        aiReceivedMethodCallCnt.incrementAndGet();
        
        if (type == RequestInfo.Type.StoC_CreateNewStoCSocket) {
            // server is requesting another vsocket "stoc"
            createSocketForStoC();
            return;
        }

        RequestInfo ri = new RequestInfo();
        ri.type = type;
        ri.msStart = System.currentTimeMillis();
        ri.nsStart = System.nanoTime();
        ri.socket = socket;
        ri.connectionId = socket.getConnectionId();
        ri.vsocketId = socket.getId();
        ri.threadId = threadId;
        
        boolean b = false;
        try {
            b = _processSocket(ri, ois);
        }
        finally {
            ri.nsEnd = System.nanoTime();
            if (b) afterInvokForStoC(ri);
        }
    }


    private LinkedBlockingQueue<RequestInfo> queRequestInfo = new LinkedBlockingQueue<RequestInfo>();
    /**
     * que that has a remoteThread process the request
     */
    protected void setupRequestQueueThread() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                    try {
                        RequestInfo  ri = queRequestInfo.take();
                        
                        OARemoteThread t = getRemoteClientThread(ri, true);
                        synchronized (t.Lock) {
                            t.Lock.notify(); // have RemoteClientThread call processMessageforStoC(..)
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "RequestQueueThread error", e);
                    }
                }
            }
        });
        t.setName("QueueController");
        t.setDaemon(true);
        t.start();
    }
    
    
    private LinkedBlockingQueue<RequestInfo> queSyncRequestInfo = new LinkedBlockingQueue<RequestInfo>();
    /**
     * que that is for sync requests, and sync return values/ack
     */
    protected void setupSyncRequestQueueThread() {
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                    try {
                        RequestInfo ri = queSyncRequestInfo.take(); // blocks

                        if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {                        
                            if (ri.bind != null && ri.bind.isOASync) {
                                if (ri.connectionId == multiplexerClient.getConnectionId()) {
                                    // 20160122 this client called a oasync method. This is the return ack.
                                    RequestInfo rix = hmAsyncRequestInfo.remove(ri.messageId);
                                    synchronized (rix) {
                                        rix.response = OAReflect.getEmptyPrimitive(rix.method.getReturnType());
                                        rix.methodInvoked = true;
                                        rix.notifyAll(); // wake up waiting thread that made this request.  See onInvokeForCtoS(..)
        
                                        // 20160121 wait for OARemoteThreadDelegate.startNextThread to be called, which will 
                                        //      then notify rix that sync msg is done, and then the next sync msg can be processed
                                        rix.wait(25);
                                    }
                                    continue;
                                }
                            }
                        }

                        int maxSeconds = Math.max(ri.methodInfo == null ? 0 : ri.methodInfo.timeoutSeconds, 0);
                        if (maxSeconds < 1) maxSeconds = 1;

                        OARemoteThread t = getRemoteClientThread(ri, false);
                        synchronized (t.Lock) {
                            t.Lock.notify(); // have RemoteClientThread call processMessageforStoC(..)
                            for (int i=0 ; t.requestInfo == ri && !ri.methodInvoked && i < (maxSeconds*10); i++) {
                                t.Lock.wait(100);
                            }
                            if (t.requestInfo == ri && !ri.methodInvoked) {
                                StackTraceElement[] stes = t.getStackTrace();
                                Exception ex = new Exception();
                                ex.setStackTrace(stes);
                                LOG.log(Level.WARNING, "timeout waiting for sync message to process, will continue, this is stacktrace for the remoteThread, request="
                                        + ri.toLogString(), ex);
                            }
                        }
                    }
                    catch (Exception e) {
                        LOG.log(Level.WARNING, "SyncRequestQueueThread error", e);
                    }
                }
            }
        });
        t.setName("SyncQueueController");
        t.setDaemon(true);
        t.start();
    }
    
    
    
    /**
     * @return true if this message is completed and can be logged
     */
    private boolean _processSocket(final RequestInfo ri, final RemoteObjectInputStream ois) throws Exception {

        if (ri.type == RequestInfo.Type.StoC_QueuedResponse || ri.type == RequestInfo.Type.CtoS_ReturnOnQueueSocket) {
            // 5:CtoS_QueuedRequest get back from server
            // response for CtoS_QueuedRequest
            int x = ois.readByte();
            Object objx = ois.readObject();
            
            ri.messageId = ois.readInt();
            RequestInfo rix = hmAsyncRequestInfo.remove(ri.messageId);
            
            if (x == 0) {
                ri.exception = (Exception) objx;
            }
            else if (x == 1) {
                ri.exceptionMessage = (String) objx;
            }
            else if (x == 2) {
                Object[] responses = (Object[]) objx;
                String bindName = (String) responses[0];

                if (rix != null) {
                    BindInfo bindx = getBindInfo(bindName);
                    objx = bindx != null ? bindx.weakRef.get() : null;
                    if (bindx == null || objx == null) {
                        boolean bUsesQueue = (Boolean) responses[1];
                        Object obj = getProxyForCtoS(bindName, rix.methodInfo.remoteReturn, bUsesQueue);
                        ri.response = obj;
                    }
                    else ri.response = bindx.getObject();
                }
            }
            else {
                ri.response = objx;
            }

            if (rix == null) {
                ri.exceptionMessage = "StoC requestInfo not found";
            }
            else {
                if (ri.response != null && rix.methodInfo.compressedReturn && rix.methodInfo.remoteReturn == null) {
                    ri.response = ((OACompressWrapper) ri.response).getObject();
                }
                synchronized (rix) {
                    rix.response = ri.response;
                    rix.exception = ri.exception;
                    rix.exceptionMessage = ri.exceptionMessage;
                    rix.methodInvoked = true;
                    // 6:CtoS_QueuedRequest  notify waiting thread from #4
                    rix.notifyAll(); // wake up waiting thread that made this request.  See onInvokeForCtoS(..)
                }
            }
            return true;
        }

        // put ri on queue to be processed by remoteClientThread
        
        if (ri.type == RequestInfo.Type.StoC_QueuedRequest) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.messageId = ois.readInt();
            queRequestInfo.put(ri);
            return false;
        }
        
        if (ri.type == RequestInfo.Type.StoC_QueuedRequestNoResponse) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();

            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "could not find bind object";
                return false;
            }
            ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
            queRequestInfo.put(ri);
            return false;
        }

        if (ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
            ri.connectionId = ois.readInt();
            ri.messageId = ois.readInt();
            
            if (ri.connectionId == multiplexerClient.getConnectionId()) {
                // this client called a broadcast
                RequestInfo rix = hmAsyncRequestInfo.get(ri.messageId);
                if (rix == null) {
                    ri.exceptionMessage = "StoC requestInfo not found";
                    return true;
                }

                
                // if oasync was called by remoteThread, then dont put in queue, which would have made it take more remoteThreads to get to it.
                //     instead, notify it when it is received back from the server.
                if (rix.bind.isOASync && !rix.isRemoteThread) {
                    ri.bind = rix.bind;
                    queSyncRequestInfo.put(ri);  // sync que will notify the original thread
                }
                else {
                    hmAsyncRequestInfo.remove(ri.messageId);
                    synchronized (rix) {
                        rix.response = OAReflect.getEmptyPrimitive(rix.method.getReturnType());
                        rix.methodInvoked = true;
                        rix.notifyAll(); // wake up waiting thread that made this request.  See onInvokeForCtoS(..)
                    }
                }
                return true;
            }
            
            // one client sent the broadcast, this is where other clients will process it
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                // ri.exceptionMessage = "could not find bind object", this client not set up to receive it.
                return false;
            }
            else ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);

            if (ri.bind.isOASync) {
                queSyncRequestInfo.put(ri);
            }
            else {
                queRequestInfo.put(ri);
            }
            return false;
        }
        
        if (ri.type == RequestInfo.Type.StoC_QueuedBroadcast) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) return false;
            ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);

            if (ri.bind.isOASync) {
                queSyncRequestInfo.put(ri);
            }
            else {
                queRequestInfo.put(ri);
            }
            return false;
        }
        
        if (ri.type == RequestInfo.Type.StoC_SocketRequest) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "invalid bind name";
            }
            else ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
            queRequestInfo.put(ri);
            return false;
        }
        
        if (ri.type == RequestInfo.Type.StoC_SocketRequestNoResponse) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "invalid bind name";
            }
            else ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
            queRequestInfo.put(ri);
            return false;
        }

        ri.exceptionMessage = "invalid command";
        return true;
    }

    protected void processMessageForStoC(RequestInfo ri) throws Exception {
        try {
            _processMessageForStoC(ri); // invoke
            if (ri.type.hasReturnValue()) {
                sendResponseForStoC(ri);
            }
            afterInvokForStoC(ri);
        }
        catch (Exception e) {
            ri.exception = e;
        }
        finally {
            ri.methodInvoked = true;
        }
    }

    private void _processMessageForStoC(RequestInfo ri) throws Exception {

        if (ri.bind == null) {
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "bind Object not found";
                return;
            }
        }
        if (ri.methodInfo == null) ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
        if (ri.methodInfo != null) ri.method = ri.methodInfo.method;

        if (ri.method == null) {
            ri.exceptionMessage = "method not found";
            return;
        }

        Object remoteObject = ri.bind.getObject();
        if (remoteObject == null) {
            ri.exceptionMessage = "remote Object has been garbage collected, class=" + ri.bind.interfaceClass;

            /*// send message to server to remove client remote object from session VirtualSocket socket
             * = getSocketForCtoS(); // used to send message, and get response RemoteObjectOutputStream
             * oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);
             * oos.writeByte(CtoS_Command_RemoveSessionBroadcastThread);
             * oos.writeAsciiString(ri.bind.name); oos.flush(); releaseSocketForCtoS(socket); */
            return;
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
                if (ri.args[i] == null) continue;
                if (!(ri.args[i] instanceof String)) {
                    LOG.warning("expected remote object, recvd=" + ri.args[i] + ", will ignore, info:" + ri.toLogString());
                    continue;
                }

                // convert the param to real object
                String bindName = (String) ri.args[i];

                BindInfo bindx = getBindInfo(bindName);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    boolean bDontUseQueue = (ri.methodInfo.dontUseQueues != null && ri.methodInfo.dontUseQueues[i]); 
                    Object obj = getProxyForCtoS(ri, bindName, ri.methodInfo.remoteParams[i], bDontUseQueue); 
                    
                    ri.args[i] = obj;
                }
                else ri.args[i] = bindx.getObject();
            }
        }

        try {
            OAThreadLocalDelegate.setRemoteRequestInfo(ri);

            // 20141217
            if (!ri.bind.isBroadcast) {
                OARemoteThreadDelegate.sendMessages(true);
            }
            ri.response = ri.method.invoke(ri.bind.getObject(), ri.args);
        }
        catch (InvocationTargetException e) {
            Exception ex = e;
            for (int i = 0; i < 10; i++) {
                Throwable t = ex.getCause();
                if (t == null || t == ex || !(t instanceof Exception)) {
                    ri.exception = ex;
                    break;
                }
                ex = (Exception) t;
                ri.exception = ex;
            }
        }
        finally {
            // 20141217
            if (!ri.bind.isBroadcast) {
                OARemoteThreadDelegate.sendMessages(false);
            }
        }
        OAThreadLocalDelegate.setRemoteRequestInfo(null);

        if (ri.response != null && ri.methodInfo.remoteReturn != null) {
            BindInfo bindx = getBindInfoForObject((Object) ri.response);
            Object objx = bindx != null ? bindx.weakRef.get() : null;
            if (bindx == null || objx == null) {
                // make remote
                boolean b = ri.methodInfo.dontUseQueueForReturnValue;
                bindx = getBindInfo(ri, createBindName(ri), ri.response, ri.methodInfo.remoteReturn, b);
            }
            ri.responseBindName = bindx.name; // this will be the return value
        }
        else if (ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
            ri.response = new OACompressWrapper(ri.response);
        }
        ri.nsEnd = System.nanoTime();
    }

    protected void sendResponseForStoC(RequestInfo ri) throws Exception {
        if (ri.type.hasReturnValue()) {
            if (ri.socket == null || (ri.bind != null && ri.bind.usesQueue)) {
                // need to send back as async response message 
                VirtualSocket socket = getSocketForCtoS();
                RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, hmClassDescOutput, aiClassDescOutput);

                oos.writeByte(RequestInfo.Type.CtoS_QueuedResponse.ordinal());
                oos.writeInt(ri.messageId);
                if (ri.exception != null) {
                    Object resp;
                    if (ri.exception instanceof Serializable) {
                        resp = ri.exception;
                    }
                    else {
                        resp = new Exception(ri.exception.toString() + ", info: " + ri.toLogString());
                    }
                    oos.writeByte(0);
                    oos.writeObject(resp);
                }
                else if (ri.exceptionMessage != null) {
                    oos.writeByte(1);
                    oos.writeObject(ri.exceptionMessage);
                }
                else if (ri.responseBindName != null) {
                    oos.writeByte(2);
                    oos.writeObject(new Object[] { ri.responseBindName, ri.responseBindUsesQueue });
                }
                else {
                    oos.writeByte(3);
                    oos.writeObject(ri.response);
                }
                oos.flush();
                releaseSocketForCtoS(socket);
            }
            else {
                RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);
                if (ri.exception != null) {
                    Object resp;
                    if (ri.exception instanceof Serializable) {
                        resp = ri.exception;
                    }
                    else {
                        resp = new Exception(ri.exception.toString() + ", info: " + ri.toLogString());
                    }
                    oos.writeByte(0); // false=error
                    oos.writeObject(resp);
                }
                else if (ri.exceptionMessage != null) {
                    oos.writeByte(1);
                    oos.writeObject(ri.exceptionMessage);
                }
                else if (ri.responseBindName != null) {
                    oos.writeByte(2);
                    oos.writeObject(new Object[] { ri.responseBindName, ri.responseBindUsesQueue });
                }
                else {
                    oos.writeByte(3);
                    oos.writeObject(ri.response);
                }
                oos.flush();
            }
        }
        else {
            if (ri.exception != null) {
                LOG.warning("error processing StoC, exception=" + ri.exception.toString());
            }
            else if (ri.exceptionMessage != null) {
                LOG.warning("error processing StoC, exception=" + ri.exceptionMessage);
            }
        }
    }
    

    /**
     * called after a callback method from the server to a local remote object.
     */
    public void afterInvokForStoC(RequestInfo ri) {
        if (ri == null) return;
        //LOG.fine(ri.toLogString());
    }

    /**
     * Find the bind information based on unique name assigned to it.
     */
    protected BindInfo getBindInfo(String name) {
        if (name == null) return null;
        return hmNameToBind.get(name);
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
     * This is used to be able to find the unique name given to a remote object.
     */
    protected BindInfo getBindInfoForObject(Object obj) {
        if (obj == null) return null;
        for (BindInfo bindx : hmNameToBind.values()) {
            if (bindx.weakRef.get() == obj) {
                return bindx;
            }
        }
        return null;
    }

    private ConcurrentHashMap<String, BindInfo> hmBindInfo = new ConcurrentHashMap<String, BindInfo>();

    /**
     * Create information that is used to manage a remote object.
     */
    protected BindInfo getBindInfo(String name, Object obj, Class interfaceClass, boolean bUsesQueue, boolean bIsBroadcast) {
        if (name == null || interfaceClass == null) {
            throw new IllegalArgumentException("name and interfaceClass can not be null");
        }
        BindInfo bind = hmBindInfo.get(name);
        if (bind != null) return bind;

        String qn;
        if (bUsesQueue) qn = "qIsOnServer";
        else qn = null;
        bind = new BindInfo(name, obj, interfaceClass, referenceQueue, bIsBroadcast, qn, -1);
        bind.loadMethodInfo();
        hmNameToBind.put(name, bind);
        return bind;
    }

    protected BindInfo getBindInfo(RequestInfo ri, String name, Object obj, Class interfaceClass, boolean bDontUseQueue) {
        return getBindInfo(name, obj, interfaceClass, (ri.bind.usesQueue&&!bDontUseQueue), ri.bind.isBroadcast);
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
    
    
}
