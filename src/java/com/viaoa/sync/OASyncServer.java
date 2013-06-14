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
    
    /** information about this server instance */
    private ServerInfo serverInfo;
    
    public OASyncServer(int port) {
        this.port = port;
    }

    public RemoteSyncImpl getRemoteSync() {
        if (remoteSync == null) {
            remoteSync = new RemoteSyncImpl();
        }
        return remoteSync;
    }
    
    public RemoteServerImpl getRemoteServer() {
        if (remoteServer == null) {
            remoteServer = new RemoteServerImpl() {
                @Override
                public RemoteClientInterface getRemoteClientInterface(ClientInfo clientInfo) {
                    return createRemoteClient(clientInfo);
                }
            };
        }
        return remoteServer;
    }
    
    protected RemoteClientInterface createRemoteClient(ClientInfo clientInfo) {
        if (clientInfo == null) return null;
        final ClientInfoExt cx = hmClientInfoExt.get(clientInfo.getConnectionId());
        if (cx == null) return null;
        
        RemoteClientImpl rc = new RemoteClientImpl() {
            boolean bClearedCache;
            @Override
            public boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey) {
                for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
                    ClientInfoExt cx = entry.getValue();
                    if (cx.remote == this) continue;
                    if (cx.remote.isLockedByThisClient(objectClass, objectKey)) return true;
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
        };
        cx.remote = rc;
        return rc;
    }

    // qqqq needs to be called by server   qqqqqqqqq need something similar
    //   for query objects/hubs, etc 
    public void saveCache(OACascade cascade, int iCascadeRule) {
        for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
            ClientInfoExt cx = entry.getValue();
            cx.remote.saveCache(cascade, iCascadeRule);
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
        RemoteClientImpl remote;
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
            cx.remote.clearLocks();
            cx.remote.clearCache();
        }
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
        }
        return remoteMultiplexerServer;
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
      //qqqqqqqqqqqqqqqqqqqqqqqq
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
}
