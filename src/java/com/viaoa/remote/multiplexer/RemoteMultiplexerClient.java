package com.viaoa.remote.multiplexer;

import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.io.VirtualSocket;
import com.viaoa.remote.multiplexer.info.BindInfo;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.io.RemoteObjectInputStream;
import com.viaoa.remote.multiplexer.io.RemoteObjectOutputStream;
import com.viaoa.util.OACompressWrapper;
import static com.viaoa.remote.multiplexer.RemoteMultiplexerServer.*;


/**
 * Remoting client, that allows a client to access Objects on a server, and call methods on those
 * objects. It allows for any method to have args that are remote objects, which would allow the server
 * to call the client. A method can also return a remote object.
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
    private ArrayList<VirtualSocket> alSocketForCtoS = new ArrayList<VirtualSocket>();
    // minimum size of vsocket pool
    private int iMinimumSocketsForCtoS = 3;

    // mapping for Remote objects
    private ConcurrentHashMap<String, BindInfo> hmNameToBind = new ConcurrentHashMap<String, BindInfo>();
    // used to manage GC for remote objects.  See performDGC.
    private ReferenceQueue referenceQueue = new ReferenceQueue();

    // performance enhancement for ObjectSteams
    private ConcurrentHashMap<Integer, ObjectStreamClass> hmClassDescInput = new ConcurrentHashMap<Integer, ObjectStreamClass>();
    private ConcurrentHashMap<String, Integer> hmClassDescOutput = new ConcurrentHashMap<String, Integer>();
    private AtomicInteger aiClassDescOutput = new AtomicInteger();
    
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
        oos.writeByte(CtoS_Command_GetInterfaceClass); // 0=method, 1=get interface class, 2=remove session bindInfo
        oos.writeAsciiString(lookupName);
        oos.flush();

        RemoteObjectInputStream ois = new RemoteObjectInputStream(socket, hmClassDescInput);
        if (!ois.readBoolean()) {
            Exception ex = (Exception) ois.readObject();
            throw ex;
        }
        Class c = (Class) ois.readObject();
        releaseSocketForCtoS(socket);
        LOG.fine("lookupName=" + lookupName + ", interface class=" + c);

        if (c != null) {
            proxyInstance = createProxyForCtoS(lookupName, c);
            hmLookup.put(lookupName, proxyInstance);
        }
        return proxyInstance;
    }

    /**
     * Create a proxy instance for an Object that is on the server. This is used for lookups and when
     * the server returns a remote instance. All methods that are called on the proxy will be sent to
     * the server, and act as-if it were ran locally.
     */
    protected Object createProxyForCtoS(String name, Class c) {
        final BindInfo bind = createBindInfo(name, null, c, true);
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

        LOG.fine("Created proxy instance, class=" + c + ", name=" + name);
        return proxy;
    }

    protected Object onInvokeForCtoS(BindInfo bind, Object proxy, Method method, Object[] args) throws Throwable {
        RequestInfo ri = new RequestInfo();
        VirtualSocket socket = getSocketForCtoS(); // used to send message, and get response
        boolean bSent = false;
        try {
            ri.msStart = System.currentTimeMillis();
            ri.nsStart = System.nanoTime();
            ri.socket = socket;
            ri.connectionId = socket.getConnectionId();
            ri.vsocketId = socket.getId();
            ri.object = proxy;
            ri.bind = bind;
            ri.bindName = bind.name;
            ri.method = method;
            ri.args = args;
            ri.methodInfo = ri.bind.getMethodInfo(ri.method);
            if (ri.methodInfo != null) ri.methodNameSignature = ri.methodInfo.methodNameSignature;
            ri.bSent = onInvokeForCtoS(ri);
        }
        catch (Exception e) {
            ri.exception = e;
        }
        finally {
            ri.nsEnd = System.nanoTime();
            releaseSocketForCtoS(socket);
        }
        afterInvokForCtoS(ri);

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
    protected void afterInvokForCtoS(RequestInfo ri) {
        if (ri == null || !ri.bSent) return;
        LOG.fine(ri.toLogString());
    }

    // "dummy" object, that is used when methods are not supported in proxy interface, but are in Object
    // class
    private final Object stuntObject = new Object();

    /**
     * Called when a remote/proxy object method is invoked. The method info will be sent to the server,
     * and return the method return value from the server.
     */
    protected boolean onInvokeForCtoS(RequestInfo ri) throws Exception {
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
                    ri.response = ri.method.invoke(stuntObject, ri.args);
                }
            }
            else {
                ri.exceptionMessage = "Method not found in Methods";
            }
            return false;
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

        long ns1 = System.nanoTime();
        RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);
        // 20130601 changed from boolean to byte
        oos.writeByte(CtoS_Command_RunMethod);
        oos.writeAsciiString(ri.bind.name);
        oos.writeAsciiString(ri.methodNameSignature);
        oos.writeObject(ri.args);
        oos.flush();
        ri.nsWrite = System.nanoTime() - ns1;

        if (ri.methodInfo == null || !ri.methodInfo.noReturnValue) {
            RemoteObjectInputStream ois = new RemoteObjectInputStream(ri.socket, hmClassDescInput);
            if (!ois.readBoolean()) {
                ri.exception = (Exception) ois.readObject();
                return true;
            }

            ns1 = System.nanoTime();
            ri.response = ois.readObject();
            ri.nsRead = System.nanoTime() - ns1;

            // check to see if return value is a remote object
            if (ri.response != null && ri.methodInfo.remoteReturn != null) {
                String bindName = (String) ri.response;

                BindInfo bindx = getBindInfo(bindName);
                Object objx = bindx != null ? bindx.weakRef.get() : null;
                if (bindx == null || objx == null) {
                    Object obj = createProxyForCtoS(bindName, ri.methodInfo.remoteReturn);
                    bindx = createBindInfo(bindName, obj, ri.methodInfo.remoteReturn, true);
                }
                ri.response = bindx.getObject();
            }
            else if (ri.response != null && ri.methodInfo.compressedReturn && ri.methodInfo.remoteReturn == null) {
                ri.response = ((OACompressWrapper) ri.response).getObject();
            }
        }
        return true;
    }

    protected VirtualSocket getSocketForCtoS() throws Exception {
        VirtualSocket socket = null;
        synchronized (alSocketForCtoS) {
            if (alSocketForCtoS.size() > 0) {
                socket = alSocketForCtoS.remove(0);
            }
        }
        if (socket == null) socket = multiplexerClient.createSocket("CtoS");
        return socket;
    }


    public void setMinimumSocketsForCtoS(int x) {
        iMinimumSocketsForCtoS = x;
    }

    public int getMinimumSocketsForCtoS() {
        return iMinimumSocketsForCtoS;
    }

    protected void releaseSocketForCtoS(VirtualSocket socket) throws Exception {
        synchronized (alSocketForCtoS) {
            if (alSocketForCtoS.size() > iMinimumSocketsForCtoS) {
                socket.close();
            }
            else alSocketForCtoS.add(socket);
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
                for (;;) {
                    try {
                        if (socket.isClosed()) break;
                        processMessageForStoC(socket, id);
                    }
                    catch (Exception e) {
                        if (!socket.isClosed()) {
                            LOG.log(Level.WARNING, "Exception in StoC thread", e);
                        }
                    }
                }
            }
        });
        t.setName("VSocket_StoC." + socket.getConnectionId() + "." + socket.getId());
        t.start();
        bFirstStoCsocketCreated = true;
        // LOG.config("created Server to Client socket and thread, connectionId=" + socket.getConnectionId() + ", vid=" + id);
    }

    protected void processMessageForStoC(final VirtualSocket socket, int threadId) throws Exception {
        if (socket.isClosed()) return;

        RemoteObjectInputStream ois = new RemoteObjectInputStream(socket, hmClassDescInput);
        if (ois.readBoolean()) {
            // server is requesting another vsocket "stoc"
            createSocketForStoC();
            return;
        }

        boolean bSendResponse = ois.readBoolean(); // flag to know if this is a broadcast message (false) or not.
        
        RequestInfo ri = new RequestInfo();
        ri.msStart = System.currentTimeMillis();
        ri.nsStart = System.nanoTime();
        ri.socket = socket;
        ri.connectionId = socket.getConnectionId();
        ri.vsocketId = socket.getId();
        ri.threadId = threadId;

        ri.bindName = ois.readAsciiString();
        ri.methodNameSignature = ois.readAsciiString();
        ri.args = (Object[]) ois.readObject();
        beforeInvokForStoC(ri);

        try {
            processMessageForStoC(ri);
        }
        catch (Exception e) {
            ri.exception = e;
        }

        if (bSendResponse && (ri.methodInfo == null || !ri.methodInfo.noReturnValue)) {
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(socket, hmClassDescOutput, aiClassDescOutput);
            if (ri.exception != null) {
                Object resp;
                if (ri.exception instanceof Serializable) {
                    resp = ri.exception;
                }
                else {
                    resp = new Exception(ri.exception.toString() + ", info: " + ri.toLogString());
                }
                oos.writeBoolean(false); // false=error
                oos.writeObject(resp);
            }
            else if (ri.exceptionMessage != null) {
                oos.writeBoolean(false);  // false=error
                Exception ex = new Exception(ri.exceptionMessage + ", info: " + ri.toLogString());
                oos.writeObject(ex);
            }
            else {
                oos.writeBoolean(true);  // true=success
                oos.writeObject(ri.response);
            }
            oos.flush();
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

    protected void processMessageForStoC(RequestInfo ri) throws Exception {
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
            // 20130601 send message to server to remove client remote object from session
            VirtualSocket socket = getSocketForCtoS(); // used to send message, and get response
            RemoteObjectOutputStream oos = new RemoteObjectOutputStream(ri.socket, hmClassDescOutput, aiClassDescOutput);
            oos.writeByte(CtoS_Command_RemoveSessionBroadcastThread);
            oos.writeAsciiString(ri.bind.name);
            oos.flush();
            releaseSocketForCtoS(socket);
            
            ri.exceptionMessage = "remote Object has been garbage collected, message sent to server to stop thread";
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
                    Object obj = createProxyForCtoS(bindName, ri.methodInfo.remoteParams[i]);
                    bindx = createBindInfo(bindName, obj, ri.methodInfo.remoteParams[i], true);
                }
                ri.args[i] = bindx.getObject();
            }
        }
        ri.response = ri.method.invoke(ri.bind.getObject(), ri.args);

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
    protected BindInfo createBindInfo(String name, Object obj, Class interfaceClass, boolean bIsProxy) {
        if (name == null || interfaceClass == null) {
            throw new IllegalArgumentException("name and interfaceClass can not be null");
        }
        BindInfo bind = new BindInfo();
        bind.name = name;
        if (obj != null) bind.setObject(obj, referenceQueue);
        bind.interfaceClass = interfaceClass;
        bind.loadMethodInfo();
        hmNameToBind.put(name, bind);
        return bind;
    }
}
