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
import com.viaoa.remote.multiplexer.info.BindInfo;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.io.RemoteObjectInputStream;
import com.viaoa.remote.multiplexer.io.RemoteObjectOutputStream;
import com.viaoa.util.OACircularQueue;
import com.viaoa.util.OACompressWrapper;

/**
 * Server component used to allow remoting method calls with Clients.
 * Uses a MultiplexerServer for communication with clients.
 * <p>
 * <ol>Different ways to create a remote object:
 * <li>Server can bind an Object so that clients can then do a lookup to get the object, and
 * all method calls will be invoked on the server.
 * <li>A method that has a remote class parameter.  This can be used by client or server - 
 * where a method argument is a remote object.
 * <li>A method returns a remote class.  This can be used by client or server - 
 * where a method returns a remote object.
 * <li>The server can create a single remote object, that will then "broadcast" to all clients
 * that have it.
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
    // used to hold all objects from the "bind()" method.
    private ConcurrentHashMap<BindInfo, Object> hmBindObject = new ConcurrentHashMap<BindInfo, Object>();
    
    // CircularQueues used for broadcast remote objects
    private ConcurrentHashMap<BindInfo, OACircularQueue<RequestInfo>> hmBroadcastCircularQueue = new ConcurrentHashMap<BindInfo, OACircularQueue<RequestInfo>>();
     
    // Java instance used to broadcast messages to clients
    private ConcurrentHashMap<Class, BindInfo> hmBroadcastClass = new ConcurrentHashMap<Class, BindInfo>();
        

    // track connections
    private ConcurrentHashMap<Integer, Session> hmSession = new ConcurrentHashMap<Integer, Session>();
    
    /**
     * Create a new RemoteServer using multiplexer.
     * @see MultiplexerServer#start() to have the server allow for client connections.
     * @see #start() to have this server start recieving remote calls.
     */
    public RemoteMultiplexerServer(MultiplexerServer server) {
        this.multiplexerServer = server;
    }

    /**
     * This can be called when  MultiplexerServer.onClientDisconnet(..) is called.
     * If this is not called, then the next socket.IO method will throw an IOException.
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
        t.setName("VSocket_CtoS."+iceSocket.getConnectionId()+"."+iceSocket.getId());
        
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
            try {
                processNextRequestForCtoS(ri, session);
            }
            catch (Exception e) {
                ri.exception = e;
            }

            long t1 = System.nanoTime();                
            // return response
            if (ri.methodInfo == null || !ri.methodInfo.streaming) {
                RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, session.hmClassDescOutput, session.aiClassDescOutput);
                oos.writeBoolean(ri.exception == null && ri.exceptionMessage == null);
                Object resp;
                if (ri.exception != null) {
                    if (ri.exception instanceof Serializable) {
                        resp = ri.exception;
                    }
                    else resp = new Exception(ri.exception.toString()+", info: "+ri.toLogString());
                }
                else if (ri.exceptionMessage != null) {
                    resp = new Exception(ri.exceptionMessage+", info: "+ri.toLogString());
                }
                else if (ri.responseBindName != null) resp = ri.responseBindName;
                else resp = ri.response;
                oos.writeObject(resp);
                oos.flush();
            }
            ri.nsEnd = System.nanoTime();
            ri.nsWrite = ri.nsEnd - t1;
            afterInvokeForCtoS(ri);
        }
    }
    protected void processNextRequestForCtoS(final RequestInfo ri, final Session session) throws Exception {
        RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, session.hmClassDescInput);
      
        // wait for next message
        boolean b = ois.readBoolean();  // true: method, false: get interface class
        ri.msStart = System.currentTimeMillis();
        ri.nsStart = System.nanoTime();
 
        if (!b) {  // lookup, needs to return Java Interface class.
            ri.bindName = ois.readAsciiString();
            BindInfo bind = getBindInfo(ri.bindName);
            if (bind != null) {
                ri.response = bind.interfaceClass;
            }
            else {
                ri.exceptionMessage = "object not found"; 
            }
            return;
        }

        
        ri.bindName = ois.readAsciiString();
        
        ri.methodNameSignature = ois.readAsciiString();
        ri.args = (Object[]) ois.readObject();
        ri.nsRead = System.nanoTime() - ri.nsStart;
        
        ri.bind = getBindInfo(ri.bindName);
        
        if (ri.bind == null) {
            ri.exceptionMessage = "bind Object not found";
            return;
        }
        ri.object = ri.bind.getObject();
        ri.methodInfo = ri.bind.getMethodInfo(ri.methodNameSignature);
        if (ri.methodInfo != null) ri.method = ri.methodInfo.method;
        if (ri.method == null) {
            if (ri.exceptionMessage == null) ri.exceptionMessage = "method not found";
            return;
        }

        // check for compressed params
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i=0; i<ri.methodInfo.compressedParams.length && i<ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (!ri.methodInfo.compressedParams[i]) continue;
                ri.args[i] = ((OACompressWrapper) ri.args[i]).getObject();
            }            
        }

        // check to see if any of the args[] are remote objects
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i=0; i<ri.methodInfo.remoteParams.length && i<ri.args.length; i++) {
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
                        Object obj;
                        final BindInfo bindz = hmBroadcastClass.get(ri.methodInfo.remoteParams[i]);
                        if (bindz != null) {
                            // this is a server side broadcast object.
                            obj = bindz.getObject();
                            bindx = session.createBindInfo(bindName, obj, ri.methodInfo.remoteParams[i]);

                            final OACircularQueue<RequestInfo> cque = hmBroadcastCircularQueue.get(bindz);
                            // set up thread that will get messages from queue and send to client
                            final String threadName = "Client."+ri.connectionId+"."+ri.bindName;
                            Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        session.writeQueueMessages(cque, bindName);
                                    }
                                    catch (Exception e) {
                                        LOG.log(Level.WARNING, "broadcast thread exception, thread="+threadName+", thread is stopping, which will stop message from being sent to this client", e);
                                    }
                                }
                            });
                            t.setName(threadName);
                            t.start();
                        }
                        else {
                            obj = createProxyForStoC(session, ri.methodInfo.remoteParams[i], bindName);
                            bindx = session.createBindInfo(bindName, obj, ri.methodInfo.remoteParams[i]);
                        }
                    }
                }
                ri.args[i] = bindx.getObject();
            }
        }
        
        int x = (ri.args == null) ? 0 : ri.args.length;
        try {
            ri.response = ri.method.invoke(ri.bind.getObject(), ri.args);
        }
        catch (InvocationTargetException iex) {
            ri.exception = new Exception(iex.toString(), iex);
        }
        catch (Throwable tx) {
            ri.exception = new Exception(tx.toString(), tx);
        }
        
        if (ri.methodInfo == null || !ri.methodInfo.streaming) {
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
                        String bindNamex = "server."+aiBindCount.incrementAndGet(); // this will be sent to client
                        bindx = createBindInfo(bindNamex, ri.response, ri.methodInfo.remoteReturn);
                    }
                }
                ri.responseBindName = bindx.name; // this will be returned to client
                session.hmBindObject.put(bindx, ri.response);  // make sure it wont get gc'd
            }
            else if (ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
                ri.response = new OACompressWrapper(ri.response);
            }
        }
    }
    
    /**
     * Called after a CtoS remote method is called. 
     */
    protected void afterInvokeForCtoS(RequestInfo ri) {
        if (ri == null) return;
        LOG.fine(ri.toLogString());
    }

    /**
     * VServerSocket that is used for vsockets that are used when a method is
     * called on the server that needs to be invoked on the client where the object came from.
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

    /** a client has created a new server to client (StoC) vsocket,
     * that can be used for the server to call methods on a client's remote object. 
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
            Exception ex = new Exception(ri.exceptionMessage+", info: "+ri.toLogString());
            throw ex;
        }
        return ri.response;
    }
    // "dummy" object, that is used when methods are not supported in proxy interface, but are in Object class
    private final Object stuntObject = new Object();
    protected void onInvokeForStoC(Session session, RequestInfo ri) throws Exception {
        ri.bind = session.getBindInfo(ri.bindName);
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
                else ri.response = ri.method.invoke(stuntObject, ri.args);
            }
            else ri.exceptionMessage = "Method  not found";
            return;
        }

        // compress flagged arguments
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i=0; i<ri.methodInfo.compressedParams.length && i<ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (ri.methodInfo.compressedParams[i]) {
                    ri.args[i] = new OACompressWrapper(ri.args[i]);
                }
            }            
        }
        
        // check to see if any of the args[] are remote objects
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i=0; i<ri.methodInfo.remoteParams.length && i<ri.args.length; i++) {
                if (ri.methodInfo.remoteParams[i] == null) continue;
                if (ri.args[i] == null) continue;
                 
                BindInfo bindx = getBindInfo((Object) ri.args[i]);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    if (bindx == null) {
                        String bindNamex = "server."+aiBindCount.incrementAndGet();
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
        
        if (ri.methodInfo == null || !ri.methodInfo.streaming) {
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
            if (bindx.weakRef.get() == obj) {
                return bindx;
            }
        }
        return null;
    }
    
    /**
     * Register/Bind an Object so that it can used by clients
     */
    public void bind(String name, Object obj, Class interfaceClass) {
        BindInfo bind = createBindInfo(name, obj, interfaceClass);
        hmBindObject.put(bind, obj);
    }
    /**
     * Remove an object that was previously used for a bind.
     */
    public boolean unbind(String name) {
        BindInfo bind = getBindInfo(name);
        if (bind == null) return false;
        hmBindObject.remove(bind);
        hmNameToBind.remove(name);
        return true;
    }

    
    /**
     * Create Bind information for a remote object.
     * @param name of object.
     * @param obj instance
     * @param interfaceClass the Interface of the obj.  This is used when creating the proxy instance. 
     */
    protected BindInfo createBindInfo(String name, Object obj, Class interfaceClass) {
        if (name == null || interfaceClass == null) {
            throw new IllegalArgumentException("name and interfaceClass can not be null");
        }
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("interfaceClass must be a Java interface");
        }
        BindInfo bind = new BindInfo();
        bind.name = name;
        if (obj != null) {
            bind.setObject(obj, referenceQueue);
        }
        bind.interfaceClass = interfaceClass;
        bind.loadMethodInfo();
        hmNameToBind.put(name, bind);
        return bind;
    }

    
    /**
     * This is used for async broadcasts from server to clients.
     * This allows the server to call methods that can be invoked on any/all clients.
     * A client will send an implementation of interfaceClass, and the server will
     * call it's methods when the server proxy method's are invoked.
     * 
     * All methods that are invoked are added to a circular queue that can then be 
     * sent to clients that have sent an implementation to the server as a remote object.
     */
    public Object createProxyForBroadcast(Class interfaceClass) {
        final String bindName = "broadcast." + aiBindCount.incrementAndGet();
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object resp = onInvokeForBroadcast(proxy, bindName, method, args);
                return resp;
            }
        };
        Object obj = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[] { interfaceClass }, handler);
        BindInfo bind = createBindInfo(bindName, obj, interfaceClass);
        hmBindObject.put(bind, obj);
        
        // this is the queue where all invoked messages will be put - for clients to pick up
        OACircularQueue<RequestInfo> cque = new OACircularQueue<RequestInfo>(500) {
        };
        hmBroadcastCircularQueue.put(bind, cque);        

        // need to be able to lookup based on class        
        hmBroadcastClass.put(interfaceClass, bind);
        
        return obj;
    }
    protected Object onInvokeForBroadcast(Object proxy, String bindName, Method method, Object[] args) {
        RequestInfo ri = new RequestInfo();
        try {
            ri.msStart = System.currentTimeMillis();
            ri.nsStart = System.nanoTime();
            ri.object = proxy;
            ri.bindName = bindName;
            ri.method = method;
            ri.args = args;
            onInvokeForBroadcast(ri);
        }
        catch (Exception e) {
            ri.exception = e;
        }
        ri.nsEnd = System.nanoTime();
        //qqqq afterInvokeForBroadcast(ri);                
        return ri.response;
    }
    protected void onInvokeForBroadcast(RequestInfo ri) throws Exception {
        ri.bind = getBindInfo(ri.bindName);
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
                else ri.response = ri.method.invoke(stuntObject, ri.args);
            }
            else ri.exceptionMessage = "Method  not found";
            return;
        }

        // compress flagged arguments
        if (ri.methodInfo.compressedParams != null && ri.args != null) {
            for (int i=0; i<ri.methodInfo.compressedParams.length && i<ri.args.length; i++) {
                if (ri.methodInfo.remoteParams != null && ri.methodInfo.remoteParams[i] != null) continue;
                if (ri.methodInfo.compressedParams[i]) {
                    ri.args[i] = new OACompressWrapper(ri.args[i]);
                }
            }            
        }
        
        // check to see if any of the args[] are remote objects
        if (ri.methodInfo.remoteParams != null && ri.args != null) {
            for (int i=0; i<ri.methodInfo.remoteParams.length && i<ri.args.length; i++) {
                if (ri.methodInfo.remoteParams[i] == null) continue;
                if (ri.args[i] == null) continue;
                 
                BindInfo bindx = getBindInfo((Object) ri.args[i]);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    if (bindx == null) {
                        String bindNamex = "server."+aiBindCount.incrementAndGet();
                        bindx = createBindInfo(bindNamex, ri.args[i], ri.methodInfo.remoteParams[i]);
                    }
                    else {
                        bindx.setObject(ri.args[i], referenceQueue);
                    }
                }
                ri.args[i] = bindx.name;
            }
        }

        // put "ri" in circular queue for clients to pick up.        
        OACircularQueue<RequestInfo> cque = hmBroadcastCircularQueue.get(ri.bind);        
        cque.addMessageToQueue(ri);
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

        protected BindInfo getBindInfo(String name) {
            if (name == null) return null;
            return hmNameToBind.get(name);
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
         * used by server when calling methods on a remote object that was created
         * on the client, so that the server can call any of the methods on it.
         */
        public VirtualSocket getSocketForStoC() throws Exception {
            VirtualSocket socket = null;
            boolean bRequestedNew = false;
            boolean bWaitedForFirst = false;
            for (int i=0 ;socket == null; i++) {
                boolean bRequestNew = false;
                synchronized (alSocketFromStoC) {
                    if (bDisconnected) {
                        throw new Exception("closed connection/session="+connectionId);
                    }
                    int x = alSocketFromStoC.size();
                    if (x > 1) {
                        socket = alSocketFromStoC.remove(0);
                    }            
                    else if (x == 1 && !bRequestedNew) {
                        // request client to open more CtoS sockets
                        bRequestNew = true;
                        socket = alSocketFromStoC.get(0);
                    }
                    else if (x == 0 && !bWaitedForFirst) { 
                        alSocketFromStoC.wait(250);
                        bWaitedForFirst = true;
                    }            
                    else if (x == 0 && i > 10) { 
                        throw new Exception("no StoC sockets available for connection/session="+connectionId);
                    }
                    else {
                        alSocketFromStoC.wait(100);
                    }
                }
                if (bRequestNew) {
                    RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket);
                    oos.writeBoolean(true); // this will tell client to create more StoC sockets
                    oos.flush();
                    socket = null;
                    bRequestedNew = true;
                }
            }
            return socket;
        }
        
        public void releaseSocketForStoC(VirtualSocket socket) throws Exception {
            if (socket == null) return;
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
            BindInfo bind = new BindInfo();
            bind.name = name;
            if (obj != null) bind.setObject(obj, null); // dont need to use referenceQueue
            bind.interfaceClass = interfaceClass;
            bind.loadMethodInfo();
            hmNameToBind.put(name, bind);
            return bind;
        }


        // used to send broadcast messages to client
        public void writeQueueMessages(final OACircularQueue<RequestInfo> cque, final String clientBindName) throws Exception {
            long qpos = cque.getHeadPostion();
            // todo: need a way to stop messages
            
            for (;;) {
                RequestInfo[] ris = cque.getMessages(qpos, 50);
                VirtualSocket vsocket = getSocketForStoC();
                for (RequestInfo ri : ris) {
                    RemoteObjectOutputStream oos = new RemoteObjectOutputStream(vsocket, hmClassDescOutput, aiClassDescOutput);
                    oos.writeBoolean(false); // flag to know this is a method call
                    oos.writeBoolean(false); // do not return a response
                    oos.writeAsciiString(clientBindName);
                    oos.writeAsciiString(ri.methodInfo.methodNameSignature);
                    oos.writeObject(ri.args);
                    oos.flush();
                    qpos++;
                }
                releaseSocketForStoC(vsocket);
            }
        }
    }
}
