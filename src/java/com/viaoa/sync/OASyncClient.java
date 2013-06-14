package com.viaoa.sync;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
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
    private RemoteSyncInterface remoteSyncInterface;
    private String serverHostName;
    private int serverHostPort;

    public OASyncClient(String serverHostName, int serverHostPort) {
        this.serverHostName = serverHostName;
        this.serverHostPort = serverHostPort;
    }
    
    public RemoteServerInterface getRemoteServerInterface() throws Exception {
        if (remoteServerInterface == null) {
            remoteServerInterface = (RemoteServerInterface) getRemoteMultiplexerClient().lookup(ServerLookupName);
        }
        return remoteServerInterface;
    }
    public RemoteSyncInterface getRemoteSyncInterface() throws Exception {
        if (remoteSyncInterface == null) {
            remoteSyncInterface = (RemoteSyncInterface) getRemoteMultiplexerClient().lookup(SyncLookupName);
        }
        return remoteSyncInterface;
    }
    public RemoteClientInterface getRemoteClientInterface() throws Exception {
        if (remoteClientInterface == null) {
            remoteClientInterface = getRemoteServerInterface().getRemoteClientInterface(getClientInfo());
        }
        return remoteClientInterface;
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
    
    /** allows remote method calls to GSMR server. */
    protected RemoteMultiplexerClient getRemoteMultiplexerClient() {
        if (remoteMultiplexerClient == null) { 
            remoteMultiplexerClient = new RemoteMultiplexerClient(getMultiplexerClient());
        }
        return remoteMultiplexerClient;
    }
}
