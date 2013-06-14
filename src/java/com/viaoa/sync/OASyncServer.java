package com.viaoa.sync;

import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;

public class OASyncServer {
    private static Logger LOG = Logger.getLogger(OASyncServer.class.getName());
    
    private int port;
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer;
    private OASync oaSync;
    
    public OASyncServer(int port) {
        this.port = port;
    }

    public OASync getOASync() {
        if (oaSync == null) {
            oaSync = new OASync();
        }
        return oaSync;
    }
    
    public MultiplexerServer getMultiplexerServer() {
        if (multiplexerServer == null) {
            multiplexerServer = new MultiplexerServer(port);
        }
        return multiplexerServer;
    }
    public RemoteMultiplexerServer getRemoteMultiplexerServer() {
        if (remoteMultiplexerServer == null) {
            remoteMultiplexerServer = new RemoteMultiplexerServer(getMultiplexerServer());
        }
        return remoteMultiplexerServer;
    }

    public final String LookupName = "oasync";
    public final int QueueSize = 12500;
    
    public void start() throws Exception {
        getMultiplexerServer().start();
        getRemoteMultiplexerServer().start();
        getRemoteMultiplexerServer().createBroadcast(LookupName, getOASync(), OASyncInterface.class, LookupName, QueueSize); 
    }
    
    public void stop() throws Exception {
        if (multiplexerServer != null) {
            multiplexerServer.stop();
        }
    }
    
}
