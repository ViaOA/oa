package com.viaoa.sync;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.object.OACascade;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.model.*;
import com.viaoa.sync.remote.*;
import com.viaoa.util.*;

public class OASyncServer {
    private static Logger LOG = Logger.getLogger(OASyncServer.class.getName());

    public static final String ServerLookupName = "syncserver";
    public static final String SyncLookupName = "oasync";
    public static final String SyncQueueName = "oasync";
    public static final int QueueSize = 12500;
    
    private int port;
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer;

    private RemoteSyncImpl remoteSync;
    private RemoteServerImpl remoteServer;

    /** used to log requests to a log file */
    private ArrayBlockingQueue<RequestInfo> queRemoteRequestLogging;

    private ConcurrentHashMap<Integer, ClientInfoExt> hmClientInfoExt = new ConcurrentHashMap<Integer, OASyncServer.ClientInfoExt>();

    private ClientInfo clientInfo;
    private RemoteClientInterface remoteClientForServer;
    private RemoteClientSyncInterface remoteClientSyncForServer;
    
    /** information about this server instance */
    private ServerInfo serverInfo;
    
    public OASyncServer(int port) {
        this.port = port;
        OASyncDelegate.setSyncServer(this);
    }

    public RemoteSyncImpl getRemoteSync() {
        if (remoteSync == null) {
            remoteSync = new RemoteSyncImpl();
            OASyncDelegate.setRemoteSyncInterface(remoteSync);
        }
        return remoteSync;
    }
    
    public RemoteServerImpl getRemoteServer() {
        if (remoteServer == null) {
            remoteServer = new RemoteServerImpl() {
                @Override
                public RemoteClientInterface getRemoteClientInterface(ClientInfo ci) {
                    return createRemoteClient(ci);
                }
                @Override
                public RemoteClientSyncInterface getRemoteClientSyncInterface(ClientInfo ci) {
                    return createRemoteClientSync(ci);
                }
            };
            OASyncDelegate.setRemoteServerInterface(remoteServer);
            getRemoteClientForServer();
        }
        return remoteServer;
    }

    
    
    protected ClientInfo getClientInfo() {
        if (clientInfo == null) {
            clientInfo = new ClientInfo();
            clientInfo.setConnectionId(0);
            clientInfo.setCreated(new OADateTime());
        }
        return clientInfo;
    }
    protected RemoteClientInterface getRemoteClientForServer() {
        if (remoteClientForServer == null) {
            remoteClientForServer = createRemoteClient(getClientInfo());
            OASyncDelegate.setRemoteClientInterface(remoteClientForServer);
        }
        return remoteClientForServer;
    }
    protected RemoteClientSyncInterface getRemoteClientSyncForServer() {
        if (remoteClientSyncForServer == null) {
            remoteClientSyncForServer = createRemoteClientSync(getClientInfo());
            OASyncDelegate.setRemoteClientSyncInterface(remoteClientSyncForServer);
        }
        return remoteClientSyncForServer;
    }
    
    protected RemoteClientInterface createRemoteClient(final ClientInfo ci) {
        if (ci == null) return null;
        final ClientInfoExt cx = hmClientInfoExt.get(ci.getConnectionId());
        if (cx == null) return null;
        
        RemoteClientImpl rc = new RemoteClientImpl() {
            boolean bClearedCache;
            @Override
            public boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey) {
                for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
                    ClientInfoExt cx = entry.getValue();
                    if (cx.remoteClient == this) continue;
                    if (cx.remoteClient.isLockedByThisClient(objectClass, objectKey)) return true;
                }
                return false;
            }
            @Override
            public void saveCache(OACascade cascade, int iCascadeRule) {
                super.saveCache(cascade, iCascadeRule);
                if (!bClearedCache && cx.ci.getDisconnected() != null) {
                    clearCache();
                    bClearedCache = true;
                }
            }
            @Override
            public boolean isLocked(Class objectClass, OAObjectKey objectKey) {
                boolean b = isLockedByThisClient(objectClass, objectKey);
                if (!b) {
                    b = isLockedByAnotherClient(objectClass, objectKey);
                }
                return b;
            }
            @Override
            public void sendException(String msg, Throwable ex) {
                OASyncServer.this.onClientException(ci, msg, ex);
            }
            @Override
            public void update(ClientInfo ci) {
                OASyncServer.this.onUpdate(ci);
            }
        };
        cx.remoteClient = rc;
        return rc;
    }

    public void onUpdate(ClientInfo ci) {
        int cid = ci.getConnectionId();
        ClientInfoExt cx = hmClientInfoExt.get(cid);
        if (cx != null) cx.ci = ci;
    }
    
    protected RemoteClientSyncInterface createRemoteClientSync(ClientInfo ci) {
        if (ci == null) return null;
        final ClientInfoExt cx = hmClientInfoExt.get(ci.getConnectionId());
        if (cx == null) return null;
        
        RemoteClientSyncImpl rc = new RemoteClientSyncImpl() {
            @Override
            public void setCached(OAObject obj, boolean b) {
                cx.remoteClient.setCached(obj, b);
            }
        };
        cx.remoteClientSync = rc;
        return rc;
    }

    // qqqq needs to be called by server   qqqqqqqqq need something similar ?? this should already be working
    //   for query objects/hubs, etc 
    public void saveCache(OACascade cascade, int iCascadeRule) {
        for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
            ClientInfoExt cx = entry.getValue();
            cx.remoteClient.saveCache(cascade, iCascadeRule);
        }
    }

    
    public ServerInfo getServerInfo() {
        if (serverInfo == null) {
            serverInfo = new ServerInfo();
            // serverInfo.setVersion(Resource.getValue(Resource.APP_Version, ""));
            serverInfo.setCreated(new OADateTime());
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                serverInfo.setHostName(localHost.getHostName());
                serverInfo.setIpAddress(localHost.getHostAddress());
            }
            catch (Exception e) {
            }
        }
        return serverInfo;
    }
    
    public void setInvalidConnectionMessage(String msg) {
        getMultiplexerServer().setInvalidConnectionMessage(msg);
    }
    
    /**
     * Used to manage multiplexed socket connections from client computers.
     * @return
     */
    public MultiplexerServer getMultiplexerServer() {
        if (multiplexerServer == null) {
            multiplexerServer = new MultiplexerServer(port) {
                @Override
                protected void onClientConnect(Socket socket, int connectionId) {
                    OASyncServer.this.onClientConnect(socket, connectionId);
                }
                @Override
                protected void onClientDisconnect(int connectionId) {
                    OASyncServer.this.onClientDisconnect(connectionId);
                }
                long msLastMsg;
                String lastMsg;
                @Override
                public String getInvalidConnectionMessage() {
                    long msNow = System.currentTimeMillis();
                    if (lastMsg == null || msLastMsg+1000 < msNow) {
                        lastMsg = OASyncServer.this.getDisplayMessage();
                        msLastMsg = msNow;
                    }
                    return lastMsg;
                }

            };
        }
        return multiplexerServer;
    }
    
    
    class ClientInfoExt {
        ClientInfo ci;
        Socket socket;
        RemoteClientImpl remoteClient;
        RemoteClientSyncImpl remoteClientSync;
    }
    
    
    protected void onClientConnect(Socket socket, int connectionId) {
        LOG.fine("new client connection, id="+connectionId);
        
        ClientInfo ci = new ClientInfo();
        ci.setCreated(new OADateTime());
        ci.setConnectionId(connectionId);
        ci.setIpAddress(socket.getInetAddress().getHostAddress());
        ci.setHostName(socket.getInetAddress().getHostName());

        ClientInfoExt cx = new ClientInfoExt();
        cx.ci = ci;
        cx.socket = socket;
        
        hmClientInfoExt.put(connectionId, cx);
    }
    protected void onClientDisconnect(int connectionId) {
        LOG.fine("client disconnect, connectionId="+connectionId);
        ClientInfoExt cx = hmClientInfoExt.get(connectionId);
        if (cx != null) {
            cx.ci.setDisconnected(new OADateTime());
            cx.remoteClient.clearLocks();
            cx.remoteClient.clearCache();
        }
    }
  
    protected void onClientException(ClientInfo ci, String msg, Throwable ex) {
        if (ci != null) {
            msg = String.format(
                "ConnectionId=%d, User=%s, msg=%s", 
                ci.getConnectionId(), ci.getUserName(), msg);
        }
        LOG.log(Level.WARNING, msg, ex);
    }
    
    public String getDisplayMessage() {
        String msg = String.format("Server started=%s, version=%s, started=%b, host=%s, " +
                "ipAddress=%s, discovery=%b",
                serverInfo.getCreated().toString(),
                serverInfo.getVersion(),
                serverInfo.isStarted(),
                serverInfo.getHostName(),
                serverInfo.getIpAddress(),
                serverInfo.isDiscoveryEnabled());
        return msg;
    }
    
    public RemoteMultiplexerServer getRemoteMultiplexerServer() {
        if (remoteMultiplexerServer == null) {
            remoteMultiplexerServer = new RemoteMultiplexerServer(getMultiplexerServer()) {
                @Override
                protected void afterInvokeForCtoS(RequestInfo ri) {
                    // OASyncServer.this.afterInvokeRemoteMethod(ri);
                }
                @Override
                protected void afterInvokeForStoC(RequestInfo ri) {
                    // no-op
                }
            };
            
            // register remote objects
            getRemoteMultiplexerServer().createLookup(ServerLookupName, getRemoteServer(), RemoteServerInterface.class); 
            getRemoteMultiplexerServer().createBroadcast(SyncLookupName, getRemoteSync(), RemoteSyncInterface.class, SyncQueueName, QueueSize);
            
            // have RemoteClient objects use sync queue
            getRemoteMultiplexerServer().registerClassWithQueue(RemoteClientSyncInterface.class, SyncQueueName, QueueSize);            
        }
        return remoteMultiplexerServer;
    }

    public void createLookup(String name, Object obj, Class interfaceClass) {
        getRemoteMultiplexerServer().createLookup(name, obj, interfaceClass, null, -1);
    }
    public void createLookup(String name, Object obj, Class interfaceClass, String queueName, int queueSize) {
        getRemoteMultiplexerServer().createLookup(name, obj, interfaceClass, queueName, queueSize);
    }
    public Object createBroadcast(final String bindName, Class interfaceClass, String queueName, int queueSize) {
        return getRemoteMultiplexerServer().createBroadcast(bindName, interfaceClass, queueName, queueSize);
    }
    public Object createBroadcast(final String bindName, Object callback, Class interfaceClass, String queueName, int queueSize) {
        return getRemoteMultiplexerServer().createBroadcast(bindName, callback, interfaceClass, queueName, queueSize);
    }

    
    

    /*
    protected void afterInvokeRemoteMethod(RequestInfo ri) {
        if (ri == null) return;
        try {
            if (queRemoteRequestLogging != null) {
                if (queRemoteRequestLogging.offer(ri, 5, TimeUnit.MILLISECONDS)) {
                    return;
                }
            }
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error adding remote request to log queue", e);
        }
        LOG.fine("RemoteLog data: " + ri.toLogString());
    }
    */
    
    /** thread used to log all requests */
    private Thread threadStatsLogger;
    /** stream to write request logs */
    private PrintWriter pwRemoteRequestLogger;
    /** time to change to another log file, so each file is by day */
    private long msNextRemoteRequestLogDateChange;
    
    /**
     * Thread that will get requests from the queue, and write to request log file.
     */
    void startRequestLoggerThread() throws Exception {
        if (true || false) return;        
        LOG.fine("starting log thread");
        if (threadStatsLogger != null) return;

        queRemoteRequestLogging = new ArrayBlockingQueue<RequestInfo>(250);

        getRemoteRequestLogPrintWriter(); // initialize log remote log file

        String tname = "SyncServer_logRequests";
        LOG.config("starting thread that writes logs, threadName=" + tname);
        threadStatsLogger = new Thread(new Runnable() {
            @Override
            public void run() {
                _runRequestStatsLogger();
            }
        }, tname);
        threadStatsLogger.setDaemon(true);
        threadStatsLogger.setPriority(Thread.MIN_PRIORITY);
        threadStatsLogger.start();
    }
    // loops to log all requests that are added to the queue
    private void _runRequestStatsLogger() {
        LOG.config("Request logger thread is now running");
        int errorCnt = 0;
        long tsLastError = 0;
        for (int i=0;;i++) {
            try {
                RequestInfo ri = queRemoteRequestLogging.take();
                logRequest(ri);
            }
            catch (Exception e) {
                long tsNow = System.currentTimeMillis();
                if (tsLastError == 0 || tsLastError + 30000 < tsNow) {
                    errorCnt++;
                    LOG.log(Level.WARNING, "error processing request from log queue, errorCnt="+errorCnt, e);
                    tsLastError = tsNow;
                }
            }
        }
    }

    protected void logRequest(RequestInfo gsRequest) throws Exception {
        if (gsRequest == null) return;

        PrintWriter pw = null;
        try {
            pw = getRemoteRequestLogPrintWriter();
        }
        catch (Exception e) {
            pw = null;
        }
        if (pw != null) {
            pw.println(gsRequest.toLogString());
            pw.flush();
        }
        else {
            System.out.println("Remote RequestLog data: " + gsRequest.toLogString());
        }
    }    

    private PrintWriter getRemoteRequestLogPrintWriter() throws Exception {
        if (pwRemoteRequestLogger != null) {
            if (System.currentTimeMillis() < msNextRemoteRequestLogDateChange) {
                return pwRemoteRequestLogger;
            }
        }
        OADate date = new OADate();
        msNextRemoteRequestLogDateChange = date.addDays(1).getTime();
        if (pwRemoteRequestLogger != null) {
            pwRemoteRequestLogger.close();
            pwRemoteRequestLogger = null;
        }
        String fileName = getLogFileName();
        LOG.config("Remote log file is " + fileName);
        FileOutputStream fout = new FileOutputStream(fileName, true);
        BufferedOutputStream bout = new BufferedOutputStream(fout);
        pwRemoteRequestLogger = new PrintWriter(bout);
        pwRemoteRequestLogger.println(RequestInfo.getLogHeader());
        pwRemoteRequestLogger.flush();
        return pwRemoteRequestLogger;
    }

    protected String getLogFileName() {
        return "logs/remoteRequests";
    }
    
    public void start() throws Exception {
        // startRequestLoggerThread();
        getMultiplexerServer().start();
        getRemoteMultiplexerServer().start();
    }
    
    public void stop() throws Exception {
        if (multiplexerServer != null) {
            multiplexerServer.stop();
        }
    }
    
    public void performDGC() {
        if (remoteMultiplexerServer != null) {
            getRemoteMultiplexerServer().performDGC();
        }
    }
}

