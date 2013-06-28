package com.viaoa.sync;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.object.OAObject;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteClientSyncInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.RemoteSyncImpl;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.util.OADateTime;

import static com.viaoa.sync.OASyncServer.*;

public class OASyncClient {
    protected static Logger LOG = Logger.getLogger(OASyncClient.class.getName());

    /** this is used to create a connection (socket) to GSMR server. */
    private MultiplexerClient multiplexerClient;

    /** Allow for making remote method calls to an object instance on the server. */
    private RemoteMultiplexerClient remoteMultiplexerClient;

    /** information about this client */ 
    private ClientInfo clientInfo;

    private RemoteServerInterface remoteServerInterface;
    private RemoteClientInterface remoteClientInterface;
    private RemoteClientSyncInterface remoteClientSyncInterface;
    private RemoteSyncInterface remoteSyncInterface;
    private RemoteSyncImpl remoteSyncImpl;
    private String serverHostName;
    private int serverHostPort;

    public OASyncClient(String serverHostName, int serverHostPort) {
        this.serverHostName = serverHostName;
        this.serverHostPort = serverHostPort;
        OASyncDelegate.setSyncClient(this);
    }

    public void startClientUpdateThread(final int seconds) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for (;;) {
                    getClientInfo().setFreeMemory(Runtime.getRuntime().freeMemory());
                    clientInfo.setTotalMemory(Runtime.getRuntime().totalMemory());
                    try {
                        getRemoteClientInterface().update(clientInfo);
                        Thread.sleep(seconds * 1000);
                    }
                    catch (Exception e) {
                        break;
                    }
                }
            }
        }, "UpdateClientInfo");
        t.setDaemon(true);
        t.start();
        
    }
    
    public Object getDetail(OAObject oaObj, String propertyName) {
        Object objx = null;
        try {
            objx = getRemoteClientSyncInterface().getDetail(oaObj.getClass(), oaObj.getObjectKey(), propertyName);
        }
        catch (Exception e) {
        }
        return objx;
    }

    
    public RemoteServerInterface getRemoteServerInterface() throws Exception {
        if (remoteServerInterface == null) {
            remoteServerInterface = (RemoteServerInterface) getRemoteMultiplexerClient().lookup(ServerLookupName);
            OASyncDelegate.setRemoteServerInterface(remoteServerInterface);
        }
        return remoteServerInterface;
    }
    // used for oasync callback (messages from other computers)
    public RemoteSyncImpl getRemoteSyncImpl() throws Exception {
        if (remoteSyncImpl == null) {
            remoteSyncImpl = new RemoteSyncImpl();
        }
        return remoteSyncImpl;
    }
    public RemoteSyncInterface getRemoteSyncInterface() throws Exception {
        if (remoteSyncInterface == null) {
            remoteSyncInterface = (RemoteSyncInterface) getRemoteMultiplexerClient().lookupBroadcast(SyncLookupName, getRemoteSyncImpl());
            OASyncDelegate.setRemoteSyncInterface(remoteSyncInterface);
        }
        return remoteSyncInterface;
    }
    public RemoteClientInterface getRemoteClientInterface() throws Exception {
        if (remoteClientInterface == null) {
            remoteClientInterface = getRemoteServerInterface().getRemoteClientInterface(getClientInfo());
            OASyncDelegate.setRemoteClientInterface(remoteClientInterface);
        }
        return remoteClientInterface;
    }
    public RemoteClientSyncInterface getRemoteClientSyncInterface() throws Exception {
        if (remoteClientSyncInterface == null) {
            remoteClientSyncInterface = getRemoteServerInterface().getRemoteClientSyncInterface(getClientInfo());
            OASyncDelegate.setRemoteClientSyncInterface(remoteClientSyncInterface);
        }
        return remoteClientSyncInterface;
    }
    

    public Object lookup(String lookupName) throws Exception {
        return getRemoteMultiplexerClient().lookup(lookupName);
    }
    public Object lookupBroadcast(String lookupName, Object callback) throws Exception {
        return getRemoteMultiplexerClient().lookupBroadcast(lookupName, callback);
    }
    
    
    public ClientInfo getClientInfo() {
        if (clientInfo == null) {
            clientInfo = new ClientInfo();
            clientInfo.setCreated(new OADateTime());
            clientInfo.setServerHostName(this.serverHostName);
            clientInfo.setServerHostPort(this.serverHostPort);
            
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                clientInfo.setHostName(localHost.getHostName());
                clientInfo.setIpAddress(localHost.getHostAddress());
            }
            catch (Exception e) {
            }
            
        }
        return clientInfo;
    }
    
    public void start() throws Exception {
        LOG.config("starting");

        getClientInfo();
        getMultiplexerClient().setKeepAlive(115); 
        
        LOG.fine("starting multiplexer client");
        getMultiplexerClient().start(); // this will connect to server using multiplexer
        clientInfo.setConnectionId(getMultiplexerClient().getConnectionId());
        
        LOG.fine("getting remote object for Client Session");
        getRemoteServerInterface();
        getRemoteSyncInterface();
        getRemoteClientInterface();
        getRemoteClientSyncInterface();
        
        clientInfo.setStarted(true);
        LOG.config("startup completed successful");
    }
    public boolean isStarted() {
        return clientInfo.isStarted();
    }
    /** Sets the stop flag, which will stop Gemstone methods from being sent to GSMRServer */
    public void stop() throws Exception {
        LOG.fine("Client stop");
        clientInfo.setStarted(false);
        if (isConnected()) {
            getMultiplexerClient().close();
        }
        multiplexerClient = null;
        remoteMultiplexerClient = null;
    }

    /**
     * checks to see if this client has been connected to the GSMR server.
     */
    public boolean isConnected() {
        if (multiplexerClient == null) return false;
        if (!multiplexerClient.isConnected()) return false;
        return true;
    }

    /** the socket connection to GSMR server. 
     * @see #onSocketException(Exception) for connection errors
     * */
    protected MultiplexerClient getMultiplexerClient() {
        if (multiplexerClient != null) return multiplexerClient; 
        multiplexerClient = new MultiplexerClient(clientInfo.getServerHostName(), clientInfo.getServerHostPort()) {
            @Override
            protected void onSocketException(Exception e) {
                OASyncClient.this.onSocketException(e);
            }
            @Override
            protected void onClose(boolean bError) {
                OASyncClient.this.onSocketClose(bError);
            }
        };
        return multiplexerClient;
    }
    
    /**
     * Called when there is an exception with the real socket.
     */
    protected void onSocketException(Exception e) {
        LOG.log(Level.WARNING, "exception with connection to server", e);
        try {
            stop();
        }
        catch (Exception ex) {
        }
    }
    protected void onSocketClose(boolean bError) {
        LOG.fine("closing, isError="+bError);
        try {
            stop();
        }
        catch (Exception ex) {
        }
    }
    
    /** allows remote method calls to GSMR server. */
    public RemoteMultiplexerClient getRemoteMultiplexerClient() {
        if (remoteMultiplexerClient == null) { 
            remoteMultiplexerClient = new RemoteMultiplexerClient(getMultiplexerClient());
        }
        return remoteMultiplexerClient;
    }
    public int getConnectionId() {
        return getMultiplexerClient().getConnectionId();
    }

}
