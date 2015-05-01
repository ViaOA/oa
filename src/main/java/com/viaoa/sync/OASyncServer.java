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
package com.viaoa.sync;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.object.OACascade;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.model.*;
import com.viaoa.sync.remote.*;
import com.viaoa.util.*;

/**
 * Server used to work with 1+ OASyncClients so that all OAObjects stay in sync.
 * This allows OASyncClients to connect and lookup remote objects and have a server side session.
 * @author vvia
 * @see OASync
 */
public class OASyncServer {
    private static Logger LOG = Logger.getLogger(OASyncServer.class.getName());

    public static final String ServerLookupName = "syncserver";
    public static final String SyncLookupName = "oasync";
    public static final String SyncQueueName = "oasync";
    public static final int QueueSize = 50000;
    
    private int port;
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer;

    private RemoteSyncImpl remoteSync;
    private RemoteServerImpl remoteServer;

    /** used to log requests to a log file */
    private ArrayBlockingQueue<RequestInfo> queRemoteRequestLogging;

    private ConcurrentHashMap<Integer, ClientInfoExt> hmClientInfoExt = new ConcurrentHashMap<Integer, ClientInfoExt>();

    // for this server instance
    private ServerInfo serverInfo;
    private ClientInfo clientInfo;
    private RemoteSessionInterface remoteSessionServer;
    private RemoteClientInterface remoteClientForServer;
    
    public OASyncServer(int port) {
        this.port = port;
        OASyncDelegate.setSyncServer(this);
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
                public RemoteSessionInterface getRemoteSession(ClientInfo ci, RemoteClientCallbackInterface callback) {
                    RemoteSessionInterface rsi = OASyncServer.this.getRemoteSession(ci, callback);
                    return rsi;
                }
                @Override
                public RemoteClientInterface getRemoteClient(ClientInfo ci) {
                    RemoteClientInterface rci = OASyncServer.this.getRemoteClient(ci);
                    return rci;
                }
                @Override
                public String getDisplayMessage() {
                    return OASyncServer.this.getDisplayMessage();
                }
                @Override
                public void refresh(Class clazz) {
                    OAObjectCacheDelegate.refresh(clazz);
                }
            };
            OASyncDelegate.setRemoteServer(remoteServer);
            getRemoteSessionForServer();
        }
        return remoteServer;
    }

    public ClientInfo getClientInfo() {
        if (clientInfo == null) {
            clientInfo = new ClientInfo();
            clientInfo.setConnectionId(0);
            clientInfo.setCreated(new OADateTime());
        }
        return clientInfo;
    }
    protected RemoteSessionInterface getRemoteSessionForServer() {
        if (remoteSessionServer == null) {
            remoteSessionServer = getRemoteSession(getClientInfo(), null);
            OASyncDelegate.setRemoteSession(remoteSessionServer);
        }
        return remoteSessionServer;
    }
    protected RemoteClientInterface getRemoteClientForServer() {
        if (remoteClientForServer == null) {
            remoteClientForServer = getRemoteClient(getClientInfo());
            OASyncDelegate.setRemoteClient(remoteClientForServer);
        }
        return remoteClientForServer;
    }
    
    protected RemoteSessionInterface getRemoteSession(final ClientInfo ci, RemoteClientCallbackInterface callback) {
        if (ci == null) return null;
        final ClientInfoExt cx = hmClientInfoExt.get(ci.getConnectionId());
        if (cx == null) return null;
        
        RemoteSessionImpl rs = cx.remoteSession;
        if (rs != null) return rs;
        cx.remoteClientCallback = callback;
        
        rs = new RemoteSessionImpl(ci.getConnectionId()) {
            boolean bClearedCache;
            @Override
            public boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey) {
                for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
                    ClientInfoExt cx = entry.getValue();
                    if (cx.remoteSession == this) continue;
                    if (cx.remoteSession.isLockedByThisClient(objectClass, objectKey)) return true;
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
            @Override
            public void removeGuids(int[] guids) {
                if (guids == null) return;
                int x = guids.length;
                for (int i=0; i<x; i++) {
                    removeFromCache(guids[i]);
                }
                cx.remoteClient.removeGuids(guids);  // remove from getDetail cache/tree
            }
            
        };
        cx.remoteSession = rs;
        return rs;
    }

    public void onUpdate(ClientInfo ci) {
        int cid = ci.getConnectionId();
        ClientInfoExt cx = hmClientInfoExt.get(cid);
        if (cx != null) cx.ci = ci;
    }
    
    protected RemoteClientInterface getRemoteClient(ClientInfo ci) {
        if (ci == null) return null;
        final ClientInfoExt cx = hmClientInfoExt.get(ci.getConnectionId());
        if (cx == null) return null;
        
        RemoteClientImpl rc = cx.remoteClient; 
        if (rc != null) return rc;
        rc = new RemoteClientImpl(ci.getConnectionId()) {
            /**
             * Add objects that need to be cached to the session.
             * This is used by datasource and copy methods. 
             */
            @Override
            public void setCached(OAObject obj) {
                cx.remoteSession.addToCache(obj);
            }
        };
        cx.remoteClient = rc;
        return rc;
    }
    
    // qqqq needs to be called by server   qqqqqqqqq need something similar ?? this should already be working
    //   for query objects/hubs, etc 
    public void saveCache(OACascade cascade, int iCascadeRule) {
        for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
            ClientInfoExt cx = entry.getValue();
            if (cx.remoteSession != null) {
                cx.remoteSession.saveCache(cascade, iCascadeRule);
            }
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
    
    public String getInvalidConnectionMessage(String defaultMsg) {
        return defaultMsg;
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
                    getRemoteMultiplexerServer().removeSession(connectionId);
                    OASyncServer.this.onClientDisconnect(connectionId);
                }
                
                @Override
                public String getInvalidConnectionMessage() {
                    String s = super.getInvalidConnectionMessage();
                    if (s == null) {
                        s = OASyncServer.this.getDisplayMessage();
                    }

                    s = OASyncServer.this.getInvalidConnectionMessage(s);
                    return s;
                }

            };
        }
        return multiplexerServer;
    }
    
    
    class ClientInfoExt {
        ClientInfo ci;
        Socket socket;
        RemoteSessionImpl remoteSession;
        RemoteClientImpl remoteClient;
        RemoteClientCallbackInterface remoteClientCallback;
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
        
        // this allows remoting to know if connection was removed
        getRemoteMultiplexerServer().createSession(socket, connectionId);
        hmClientInfoExt.put(connectionId, cx);
    }
    protected void onClientDisconnect(int connectionId) {
        LOG.fine("client disconnect, connectionId="+connectionId);
        ClientInfoExt cx = hmClientInfoExt.get(connectionId);
        if (cx != null) {
            cx.ci.setDisconnected(new OADateTime());
            cx.remoteSession.clearLocks();
            cx.remoteSession.clearCache();
        }
    }
    public Socket getSocket(int connectionId) {
        ClientInfoExt cx = hmClientInfoExt.get(connectionId);
        if (cx != null) return cx.socket;
        return null;
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
        int ccnt = 0;
        for (Map.Entry<Integer, ClientInfoExt> entry : hmClientInfoExt.entrySet()) {
            ClientInfoExt cx = entry.getValue();
            if (cx.ci.getDisconnected() == null) ccnt++;
        }
        
        String msg = String.format("Server started=%s, version=%s, started=%b, host=%s, " +
            "ipAddress=%s, discovery=%b, oa=%d, clients connected=%d, total=%d",
            serverInfo.getCreated().toString(),
            serverInfo.getVersion(),
            serverInfo.isStarted(),
            serverInfo.getHostName(),
            serverInfo.getIpAddress(),
            serverInfo.isDiscoveryEnabled(),
            OAObject.version,
            ccnt, 
            hmClientInfoExt.size()
        );
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
                
                @Override
                protected void onException(int connectionId, String title, String msg, Exception e, boolean bWillDisconnect) {
                    ClientInfoExt cx = hmClientInfoExt.get(connectionId);
                    if (cx != null && cx.remoteClientCallback != null) {
                        cx.remoteClientCallback.stop(title, msg);
                    }
                }            
            };
            
            // register remote objects
            remoteMultiplexerServer.createLookup(ServerLookupName, getRemoteServer(), RemoteServerInterface.class, SyncQueueName, QueueSize); 

            RemoteSyncInterface rsi = (RemoteSyncInterface) remoteMultiplexerServer.createBroadcast(SyncLookupName, getRemoteSync(), RemoteSyncInterface.class, SyncQueueName, QueueSize);
            OASyncDelegate.setRemoteSync(rsi);
            
            // have RemoteClient objects use sync queue
            // remoteMultiplexerServer.registerClassWithQueue(RemoteClientInterface.class, SyncQueueName, QueueSize);            
        }
        return remoteMultiplexerServer;
    }

    public void createLookup(String name, Object obj, Class interfaceClass) {
        getRemoteMultiplexerServer().createLookup(name, obj, interfaceClass, null, -1);
    }
    /**
     * use the same queue that is used by sync remote object.
     */
    public void createSyncLookup(String name, Object obj, Class interfaceClass) {
        getRemoteMultiplexerServer().createLookup(name, obj, interfaceClass, SyncQueueName, QueueSize);
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
    public Object createSyncBroadcast(final String bindName, Class interfaceClass) {
        return getRemoteMultiplexerServer().createBroadcast(bindName, interfaceClass, SyncQueueName, QueueSize);
    }
    public Object createSyncBroadcast(final String bindName, Object callback, Class interfaceClass) {
        return getRemoteMultiplexerServer().createBroadcast(bindName, callback, interfaceClass, SyncQueueName, QueueSize);
    }
    
/*qqq removed  args/result that are remote objects will use same queue as parent object    
    public void registerClassWithQueue(Class clazz) {
        getRemoteMultiplexerServer().registerClassWithQueue(clazz, SyncQueueName, QueueSize);
    }
*/    

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
        getServerInfo();
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

