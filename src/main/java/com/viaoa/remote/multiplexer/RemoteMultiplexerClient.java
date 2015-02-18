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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.io.VirtualSocket;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.BindInfo;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.io.RemoteObjectInputStream;
import com.viaoa.remote.multiplexer.io.RemoteObjectOutputStream;
import com.viaoa.util.OACompressWrapper;
import com.viaoa.util.OAPool;


/**
 * Remoting client, that allows a client to access Objects on a server, and call methods on those
 * objects. It allows for any method to have args that are remote objects, which would allow the server
 * to call the client. A method can also return a remote object.
 * 
 * Broadcasting is support, where calling a method on a remote object will be invoked on 
 * all other clients and server.
 * 
 * This is similar to RMI, except that it allows for many objects (on either server or client) to be
 * remote, uses multiplexer, and less overhead.
 * 
 * Example: get a remote object "A" from server call method "a.test(argX)", where arg 
 * is a RemoteClass - the server will then be able to call methods on argX.
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
     * Creates a new Distriubed Client, using the ICEClient multiplexer connection as the transport.
     */
    public RemoteMultiplexerClient(MultiplexerClient multiplexerClient) {
        LOG.fine("new multiplexer client");
        if (multiplexerClient == null) throw new IllegalArgumentException("multiplexerClient is required");
        this.multiplexerClient = multiplexerClient;
    }

    public MultiplexerClient getMultiplexerClient() {
        return multiplexerClient;
    }
    

    /**
     * Create a remote object that is sent to all clients.
     * @param lookupName name used on server, see: RemoteMultiplexerServer.createClientBroadcast
     * @param callback an impl used when receiving messages from other clients
     * @see RemoteMultiplexerServer#createClientBroadcast(String, Class)
     */
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
            c = (Class) ois.readObject();;
        }
        
        releaseSocketForCtoS(socket);
        LOG.fine("lookupName=" + lookupName + ", interface class=" + c);
        if (ex != null) throw ex;

        if (!c.isAssignableFrom(callback.getClass())) {
            throw new Exception("callback must be same class as "+c);
        }

        proxyInstance = createProxyForCtoS(lookupName, c, true, callback);
        hmLookup.put(lookupName, proxyInstance);
        return proxyInstance;
    }    
    
    
    /**
     * Get a remote object from the server.
     * 
     * @param lookupName
     *            name that the server has used to bind the object.
     */
    public Object lookup(final String lookupName) throws Exception {
        Object proxyInstance = hmLookup.get(lookupName);
        if (proxyInstance != null) return proxyInstance;
        LOG.fine("lookupName=" + lookupName);

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
            throw new Exception("must use lookupBroadcast() for "+lookupName+", instead of lookup()");
        }
        
        releaseSocketForCtoS(socket);
        LOG.fine("lookupName=" + lookupName + ", interface class=" + c);

        if (c != null) {
            proxyInstance = createProxyForCtoS(lookupName, c, bUsesQueue);
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
    
    /**
     * Create a proxy instance for an Object that is on the server. This is used for lookups and when
     * the server returns a remote instance. All methods that are called on the proxy will be sent to
     * the server, and act as-if it were ran locally.
     */
    protected Object createProxyForCtoS(String name, Class c, boolean bUsesQueue) throws Exception {
        final BindInfo bind = createBindInfo(name, null, c, bUsesQueue);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = RemoteMultiplexerClient.this.onInvokeForCtoS(bind, proxy, method, args);
                return result;
            }
        };
        Object proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, handler);
        bind.setObject(proxy, referenceQueue);
        bind.loadMethodInfo();

        if (bind.usesQueue) {
            if (!bFirstStoCsocketCreated) {
                createSocketForStoC(); // to process message from server to this object
            }
        }
        
        LOG.fine("Created proxy instance, class=" + c + ", name=" + name);
        return proxy;
    }
    
    protected Object createProxyForCtoS(String name, Class c, boolean bUsesQueue, Object callback) throws Exception {
        final BindInfo bind = createBindInfo(name, callback, c, bUsesQueue);
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object result = RemoteMultiplexerClient.this.onInvokeForCtoS(bind, proxy, method, args);
                return result;
            }
        };
        Object proxy = Proxy.newProxyInstance(c.getClassLoader(), new Class[] { c }, handler);
        bind.loadMethodInfo();

        if (bind.usesQueue) {
            if (!bFirstStoCsocketCreated) {
                createSocketForStoC(); // to process message from server to this object
            }
        }
        
        LOG.fine("Created proxy instance, class=" + c + ", name=" + name);
        return proxy;
    }

    protected Object onInvokeForCtoS(BindInfo bind, Object proxy, Method method, Object[] args) throws Throwable {
        RequestInfo ri = new RequestInfo();
        VirtualSocket socket = getSocketForCtoS(); // used to send message, and get response
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
            if (ri.methodInfo != null) ri.methodNameSignature = ri.methodInfo.methodNameSignature;
            ri.bSent = _onInvokeForCtoS(ri);
            
            if (ri.bSent && ri.bind.usesQueue && ri.type.hasReturnValue()) {
                releaseSocketForCtoS(socket);
                socket = null;
                synchronized (ri) {
                    for (;;) {
                        if (ri.methodInvoked) break;
                        ri.wait(60000);  // request timeout
                    }
                }
                if (!ri.methodInvoked) {
                    ri.exceptionMessage = "timeout waiting on response from server";
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
        LOG.fine(ri.toLogString());
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
            else {
                ri.exceptionMessage = "Method not found in Methods";
            }
            return false;
        }
        
        // check if remoteThread, and if it has already processed it's msg before calling remote method
        if (!OARemoteThreadDelegate.isSafeToCallRemoteMethod()) {
            if (errorCnt++ < 25 || (errorCnt % 100 == 0)) {
                Exception e = new Exception("isSafeToCallRemoteMethod is false");
                LOG.log(Level.WARNING, "note: isSafeToCallRemoteMethod is false, will continue, starting another OARemoteThread", e);
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

                BindInfo bindx = getBindInfo((Object) ri.args[i]);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    bindx = createBindInfo(createBindName(ri), ri.args[i], ri.methodInfo.remoteParams[i], false);
                    if (!bFirstStoCsocketCreated) {
                        createSocketForStoC(); // to process message from server to this object
                    }
                }
                ri.args[i] = bindx.name;
            }
        }

        if (ri.bind.usesQueue) {
            hmAsyncRequestInfo.put(ri.messageId, ri); // used to wait for server to send it back on StoC
            if (!bFirstStoCsocketCreated) {
                createSocketForStoC(); // to process message from server to this object
            }
        }
        
        long ns1 = System.nanoTime();
        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);

        if (ri.bind.usesQueue) {
            if (ri.methodInfo != null && ri.methodInfo.noReturnValue) {
                ri.type = RequestInfo.Type.CtoS_QueuedRequestNoResponse;
            }
            else {
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
        
        oos.writeByte(ri.type.ordinal());
        oos.writeAsciiString(ri.bind.name);
        oos.writeAsciiString(ri.methodNameSignature);
        oos.writeObject(ri.args);
        
        if (ri.type.hasReturnValue() && ri.type.usesQueue()) {
            oos.writeInt(ri.messageId);
        }
        oos.flush();
        ri.nsWrite = System.nanoTime() - ns1;

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
                    Object obj = createProxyForCtoS(bindName, ri.methodInfo.remoteReturn, bUsesQueue);
                    bindx = createBindInfo(bindName, obj, ri.methodInfo.remoteReturn, bUsesQueue);
                }
                ri.response = bindx.getObject();
            }
            else {
                ri.response = ois.readObject();
                if (ri.response != null && ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
                    ri.response = ((OACompressWrapper) ri.response).getObject();
                }
            }

            ns1 = System.nanoTime();
            ri.methodInvoked = true;
            ri.nsRead = System.nanoTime() - ns1;
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
        return poolVirtualSocketCtoS;
    }
    
    protected VirtualSocket getSocketForCtoS() throws Exception {
        VirtualSocket vs = getVirtualSocketCtoSPool().get();
        return vs;
    }
    protected void releaseSocketForCtoS(VirtualSocket vs) throws Exception {
        if (vs == null) return;
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
     * These are vsockets used to listen/wait for method calls from server.
     * This is used when a client sends a remote object to the server, so that server
     * can then call methods on it, and have it invoked on the client.
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
                for (;;) {
                    try {
                        if (socket.isClosed()) {
                            break;
                        }
                        processMessageForStoC(socket, id);
                    }
                    catch (Exception e) {
                        if (!socket.isClosed()) {
                            errorCnt++;
                            long ms = System.currentTimeMillis();
                            if (msLastError == 0 || ms-msLastError > 5000 || errorCnt < 5) {
                                LOG.log(Level.WARNING, "Exception in StoC thread, errorCnt="+errorCnt, e);
                                if (errorCnt > 50) break;
                                msLastError = ms;
                            }
                        }
                    }
                }
            }
        });
        t.setName("OASocket_StoC." + socket.getConnectionId() + "." + socket.getId());
        t.start();
        bFirstStoCsocketCreated = true;
        LOG.fine("created StoC socket and thread, connectionId=" + socket.getConnectionId() + ", vid=" + id);
    }

    private AtomicInteger aiClientThreadCount = new AtomicInteger();
    private ArrayList<OARemoteThread> alRemoteClientThread = new ArrayList<OARemoteThread>();
    private long msLastCreatedRemoteThread;

    private OARemoteThread getRemoteClientThread(RequestInfo ri) {
        OARemoteThread remoteThread;
        synchronized (alRemoteClientThread) {
            for (OARemoteThread rt : alRemoteClientThread) {
                synchronized (rt.Lock) {
                    if (rt.requestInfo == null) {
                        rt.requestInfo = ri;
                        return rt;
                    }
                }
            }
            msLastCreatedRemoteThread = System.currentTimeMillis();
            remoteThread = createRemoteClientThread();
            remoteThread.requestInfo = ri;
            alRemoteClientThread.add(remoteThread);
        }
        onRemoteThreadCreated(alRemoteClientThread.size());
        return remoteThread;
    }
    private OARemoteThread createRemoteClientThread() {
        OARemoteThread t = new OARemoteThread(true) {
            @Override
            public void run() {
                for (; !stopCalled; ) {
                    try {
                        synchronized (Lock) {
                            reset();
                            if (requestInfo == null) {
                                Lock.wait();
                                if (requestInfo == null) continue;
                            }
                        }

                        this.msLastUsed = System.currentTimeMillis();
                        
                        processMessageForStoC(requestInfo);

                        // 20140303 get events that need to be processed in another thread
                        final ArrayList<Runnable> al = OAThreadLocalDelegate.getRunnables(true);
                        if (al != null) {
                            Runnable rr = new Runnable() {
                                @Override
                                public void run() {
                                    // set thread to match???
                                    for (Runnable r : al) {
                                        r.run();
                                    } 
                                }
                            };
                            getExecutorService().submit(rr);
                            int x = queExecutorService.size();
                            if (x > 19 && x % 10 == 0) {
                                LOG.fine("queueSize="+x);
                            }
                        }
                        
                        synchronized (Lock) {
                            this.requestInfo = null;
                            Lock.notify();
                        }
                        if (shouldClose(this)) break;
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
                    Lock.notify();
                }
            }
        };
        t.setName("OARemoteThread."+aiClientThreadCount.getAndIncrement());
        t.start();
        return t;
    }
    protected void onRemoteThreadCreated(int threadCount) {
        if (threadCount > 25) {
            LOG.warning("alRemoteClientThread.size() = "+threadCount);
        }
    }
    private boolean shouldClose(OARemoteThread remoteThread) {
        if (alRemoteClientThread.size() < 4) return false;
        long msNow = System.currentTimeMillis();
        if (msNow - msLastCreatedRemoteThread < 1000) return false;;
        int cnt = 0;
        int minFree = 2;
        if (alRemoteClientThread.size() > 10) minFree = 4;
        synchronized (alRemoteClientThread) {
            for (OARemoteThread rt : alRemoteClientThread) {
                if (rt.msLastUsed+1000 > msNow) continue;
                synchronized (rt.Lock) {
                    if (rt.requestInfo == null) {
                        cnt++;
                        if (cnt > minFree) {
                            alRemoteClientThread.remove(remoteThread);
                            remoteThread.stopCalled = true;
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    protected void processMessageForStoC(final VirtualSocket socket, int threadId) throws Exception {
        if (socket.isClosed()) return;
        RemoteObjectInputStream ois = new RemoteObjectInputStream(socket, hmClassDescInput);
        
        RequestInfo.Type type = RequestInfo.getType(ois.readByte());
        
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

        if (ri.type == RequestInfo.Type.CtoS_QueuedRequest) {
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
                    Object obj = createProxyForCtoS(bindName, ri.methodInfo.remoteReturn, bUsesQueue);
                    bindx = createBindInfo(bindName, obj, ri.methodInfo.remoteReturn, bUsesQueue);
                }
                ri.response = bindx.getObject();
            }
            else {
                ri.response = ois.readObject();
            }
            ri.messageId = ois.readInt();

            RequestInfo rix = hmAsyncRequestInfo.remove(ri.messageId);
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
                    rix.notify();  // wake up waiting thread that made this request.  See onInvokeForCtoS(..)
                }
            }
            return;
        }
        
        if (ri.type == RequestInfo.Type.StoC_QueuedRequest) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.messageId = ois.readInt();

            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "could not find bind object";
            }

            beforeInvokForStoC(ri);
            
            OARemoteThread t = getRemoteClientThread(ri);
            synchronized (t.Lock) {
                if (t.requestInfo != null) {
                    t.Lock.notify();  // have RemoteClientThread call processMessageforStoC(..)
                    t.Lock.wait(250);
                }
            }
            return;
        }
        if (ri.type == RequestInfo.Type.StoC_QueuedRequestNoResponse) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();

            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "could not find bind object";
            }

            beforeInvokForStoC(ri);
            
            OARemoteThread t = getRemoteClientThread(ri);
            synchronized (t.Lock) {
                if (t.requestInfo != null) {
                    t.Lock.notify();  // have RemoteClientThread call processMessageforStoC(..)
                    t.Lock.wait(250);
                }
            }
            return;
        }

        if (ri.type == RequestInfo.Type.StoC_QueuedBroadcast || ri.type == RequestInfo.Type.CtoS_QueuedBroadcast) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) return;  
            beforeInvokForStoC(ri);
            
            OARemoteThread t = getRemoteClientThread(ri);

            long t1 = System.currentTimeMillis();                
            synchronized (t.Lock) {
                if (t.requestInfo != null) {
                    t.Lock.notify();  // have RemoteClientThread call processMessageforStoC(..)
                    t.Lock.wait(250);
                }
            }
            
            long t2 = System.currentTimeMillis();
            long tx = t2 - t1;
            if (tx >= 250) {
                String stackTrace="";
                for (StackTraceElement ste : t.getStackTrace()) {
                    stackTrace += "\n    "+ste.getFileName()+"."+ste.getMethodName()+" line "+ste.getLineNumber();
                }
                LOG.log(Level.WARNING, "timeout waiting on RemoteThread to process message, waited for "+tx + "ms, will continue.\nStacktrace for RemoteThread: "+t.getName()+stackTrace);
            }
        }
        else if (ri.type == RequestInfo.Type.StoC_SocketRequestReponse) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "invalid bind name";
            }
            beforeInvokForStoC(ri);
            
            processMessageForStoC(ri);
        }
        else if (ri.type == RequestInfo.Type.StoC_SocketRequestNoResponse) {
            ri.bindName = ois.readAsciiString();
            ri.methodNameSignature = ois.readAsciiString();
            ri.args = (Object[]) ois.readObject();
            ri.bind = getBindInfo(ri.bindName);
            if (ri.bind == null) {
                ri.exceptionMessage = "invalid bind name";
            }
            beforeInvokForStoC(ri);
            
            processMessageForStoC(ri);
        }
        else {
            ri.exceptionMessage = "invalid command";
        }
    }
    
    protected void processMessageForStoC(RequestInfo ri) throws Exception {
        try {
            _processMessageForStoC(ri);  // invoke
        }
        catch (Exception e) {
            ri.exception = e;
        }

        if (ri.methodInfo == null || !ri.methodInfo.noReturnValue) {
            if (ri.socket == null) {
                // need to send back as async response message 
                VirtualSocket socket = getSocketForCtoS();
                RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, hmClassDescOutput, aiClassDescOutput);
  
                oos.writeByte(RequestInfo.Type.CtoS_QueuedReturnedResponse.ordinal());
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
                LOG.warning("error processing StoC, exception="+ri.exception.toString());
            }
            else if (ri.exceptionMessage != null) {
                LOG.warning("error processing StoC, exception="+ri.exceptionMessage);
            }
        }
        ri.nsEnd = System.nanoTime();

        afterInvokForStoC(ri);
    }

    private void _processMessageForStoC(RequestInfo ri) throws Exception {
        ri.bind = getBindInfo(ri.bindName);
        if (ri.bind == null) {
            ri.exceptionMessage = "bind Object not found";
            return;
        }
        else {
            ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
            if (ri.methodInfo != null) ri.method = ri.methodInfo.method;
        }

        if (ri.method == null) {
            ri.exceptionMessage = "method not found";
            return;
        }

        Object remoteObject = ri.bind.getObject();
        if (remoteObject == null) {
            ri.exceptionMessage = "remote Object has been garbage collected, class="+ri.bind.interfaceClass;

            /*
            // send message to server to remove client remote object from session
            VirtualSocket socket = getSocketForCtoS(); // used to send message, and get response
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);
            oos.writeByte(CtoS_Command_RemoveSessionBroadcastThread);
            oos.writeAsciiString(ri.bind.name);
            oos.flush();
            releaseSocketForCtoS(socket);
            */
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
                    Object obj = createProxyForCtoS(bindName, ri.methodInfo.remoteParams[i], false);
                    bindx = createBindInfo(bindName, obj, ri.methodInfo.remoteParams[i], false);
                }
                ri.args[i] = bindx.getObject();
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
        finally {
            // 20141217
            if (!ri.bind.isBroadcast) {
                OARemoteThreadDelegate.sendMessages(false);
            }
        }
        OAThreadLocalDelegate.setRemoteRequestInfo(null);

        if (ri.response != null && ri.methodInfo.remoteReturn != null) {
            BindInfo bindx = getBindInfo((Object) ri.response);
            Object objx = bindx != null ? bindx.weakRef.get() : null;
            if (bindx == null || objx == null) {
                // make remote
                bindx = createBindInfo(createBindName(ri), ri.response, ri.methodInfo.remoteReturn, false);
            }
            ri.responseBindName = bindx.name;  // this will be the return value
        }
        else if (ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
            ri.response = new OACompressWrapper(ri.response);
        }
    }

    /**
     * called before a callback method from the server to a local remote object.
     */
    public void beforeInvokForStoC(RequestInfo ri) {
    }

    /**
     * called after a callback method from the server to a local remote object.
     */
    public void afterInvokForStoC(RequestInfo ri) {
        if (ri == null) return;
        LOG.fine(ri.toLogString());
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
    protected BindInfo getBindInfo(Object obj) {
        if (obj == null) return null;
        for (BindInfo bindx : hmNameToBind.values()) {
            if (bindx.weakRef.get() == obj) {
                return bindx;
            }
        }
        return null;
    }

    /**
     * Create information that is used to manage a remote object.
     */
    protected BindInfo createBindInfo(String name, Object obj, Class interfaceClass, boolean bUsesQueue) {
        if (name == null || interfaceClass == null) {
            throw new IllegalArgumentException("name and interfaceClass can not be null");
        }
        String qn;
        if (bUsesQueue) qn = "yes";
        else qn = null;
        BindInfo bind = new BindInfo(name, obj, interfaceClass, referenceQueue, bUsesQueue, qn, -1);
        bind.loadMethodInfo();
        hmNameToBind.put(name, bind);
        return bind;
    }


    // thread pool
    private ThreadPoolExecutor executorService;
    private LinkedBlockingQueue<Runnable> queExecutorService;
    protected ExecutorService getExecutorService() {
        if (executorService != null) return executorService;

        ThreadFactory tf = new ThreadFactory() {
            AtomicInteger ai = new AtomicInteger();
            @Override
            public Thread newThread(Runnable r) {
                OARemoteThread t = new OARemoteThread(r, false); // needs to be this type of thread
                t.setName("Multiplexer.executorService."+ai.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };
        
        queExecutorService = new LinkedBlockingQueue<Runnable>(Integer.MAX_VALUE);
        // min/max must be equal, since new threads are only created when queue is full
        executorService = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, 
                queExecutorService, tf) 
        {
            @Override
            public Future<?> submit(Runnable task) {
                LOG.fine("running task in thread="+Thread.currentThread().getName());
                return super.submit(task);
            }
        };
        executorService.allowCoreThreadTimeOut(true);  // must have this
        
        return executorService;
    }
}
