package com.viaoa.cs;

import java.net.Socket;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.hub.Hub;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;
import com.viaoa.util.OADateTime;

/**
 * Server component that allows OAClients to connect. 
 * @author vvia
 */
public class OAServer {
    private static Logger LOG = Logger.getLogger(OAServer.class.getName());
    private int port;
    private MultiplexerServer multiplexerServer;  // creates socket connection
    private RemoteMultiplexerServer remoteMultiplexerServer;
    private transient OAServerImpl oaServer;

    // each client session
    private ConcurrentHashMap<Integer, ClientSession> hmClientSession = new ConcurrentHashMap<Integer, OAServer.ClientSession>();

    public OAServer(int port) {
        this.port = port;
    }
    
    public void setInvalidConnectionMessage(String msg) {
        getMultiplexerServer().setInvalidConnectionMessage(msg);
    }
    
    // tracks each client connection
    class ClientSession {
        final int connectionId;   // from multiplexer connection
        OAClientInfo clientInfo;  // updated by both client and server
        OAObjectServerImpl oaObjectServer; // remote object used by each client

        public ClientSession(int connectionId) {
            this.connectionId = connectionId;
        }
    }
    
    protected ClientSession createClientSession(int connectionId) {
        ClientSession cs = new ClientSession(connectionId);
        cs.clientInfo = new OAClientInfo();
        cs.clientInfo.setId(connectionId);
        cs.clientInfo.setCreated(new OADateTime());
        hmClientSession.put(connectionId, cs);
        return cs;
    }
    protected ClientSession getClientSession(int connectionId) {
        return hmClientSession.get(connectionId);
    }
    
    public OAClientInfo[] getClientInfos() {
        ClientSession[] css = new ClientSession[0];
        css = (ClientSession[]) hmClientSession.values().toArray(css);
        if (css == null) return new OAClientInfo[0]; 
        OAClientInfo[] cis = new OAClientInfo[css.length];
        for (int i=0; i<css.length; i++) {
            cis[i] = css[i].clientInfo;
        }
        return cis;
    }
    
    public OAServerImpl getOAServerImpl() {
        if (oaServer == null) {
            oaServer = new OAServerImpl() {
                @Override
                public OAObjectServerInterface createOAObjectServer(OAClientInfo ci) {
                    int connectionId = ci.getId();
                    ClientSession cs = getClientSession(connectionId);
                    
                    cs.oaObjectServer = (OAObjectServerImpl) super.createOAObjectServer(cs.clientInfo);
                    return cs.oaObjectServer;
                }
                public OAClientInfo updateClientInfo(OAClientInfo ci) {
                    if (ci == null) return null;
                    ClientSession cs = getClientSession(ci.id);
                    if (cs != null) {
                        ci.created = cs.clientInfo.created;  // this value is assigned on server, others are controlled by client side
                        cs.clientInfo = ci;
                    }
                    if (cs != null && cs.oaObjectServer != null) {
                        cs.oaObjectServer.updateClientInfo(ci);
                    }
                    OAServer.this.onClientInfoUpdated(ci);
                    return ci;
                }
                
                @Override
                public void getInfo(Vector vec, boolean bIncludeClosed) {
                    super.getInfo(vec, bIncludeClosed);
                    
                    for (Entry<Integer, ClientSession> entry : hmClientSession.entrySet()) {
                        ClientSession cs = entry.getValue();
                        
                        if (cs.oaObjectServer != null && (bIncludeClosed || cs.oaObjectServer.bConnected)) {
                            try {
                                cs.oaObjectServer.updateClientInfo(cs.clientInfo);
                                String[] ss = cs.clientInfo.asStrings();
                                if (ss == null) vec.add("User "+cs.clientInfo.getId()+" has not been initialized from client");
                                for (int j=0; ss != null && j<ss.length; j++) {
                                    vec.addElement(ss[j]);
                                }
                            }
                            catch (Exception e) {
                                LOG.log(Level.WARNING, "Error in OAServer getInfo",e);
                            }
                        }
                    }
                    
                }
            };
        }
        return oaServer;
    }
    
    /**
     * create a remote object for clients to use.
     * @param name lookup name
     * @param obj remote object
     * @param interfaceClass interface class used by clients to create a proxy instance.
     */
    public void bind(String name, Object obj, Class interfaceClass) {
        getRemoteMultiplexerServer().bind(name, obj, interfaceClass); 
    }
    
    public void start() throws Exception {
        LOG.finer("creating remote object OAServer");
        
        getRemoteMultiplexerServer().bind(OAClient.OAServer_BindName, getOAServerImpl(), OAServerInterface.class); 

        ClientSession cs = createClientSession(0);
        OAClient c = new OAClient(oaServer) {
            public void handleException(int clientId, boolean bFromServer, String msg, Throwable e) {
                OAServer.this.handleException(clientId, bFromServer, msg, e);
            }
        };
        cs.clientInfo = c.getClientInfo();
        cs.clientInfo.created = new OADateTime();
        cs.clientInfo.setUserId("Server");
        cs.clientInfo.setUserName(System.getProperty("user.name"));
        cs.clientInfo.setLocation("Server");
        cs.clientInfo.setConnectionStatus("server");
        
        
        getRemoteMultiplexerServer().start();
        getMultiplexerServer().start();

        LOG.config("Java RemoteServer Started");
    }
    
    public void stop() throws Exception {
        OAClient c = OAClient.getClient();
        if (c != null) {
            c.getClientInfo().setConnectionStatus("stopped");
        }
        getMultiplexerServer().stop();
    }
    
    public RemoteMultiplexerServer getRemoteMultiplexerServer() {
        if (remoteMultiplexerServer == null) {
            remoteMultiplexerServer = new RemoteMultiplexerServer(getMultiplexerServer());
        }
        return remoteMultiplexerServer;
    }
    public MultiplexerServer getMultiplexerServer() {
        if (multiplexerServer == null) {
            multiplexerServer = new MultiplexerServer(this.port) {
                @Override
                protected void onClientConnect(Socket socket, int connectionId) {
                    OAServer.this.onClientConnect(socket, connectionId);
                }
                @Override
                protected void onClientDisconnect(int connectionId) {
                    OAServer.this.onClientDisconnect(connectionId);
                }
            };
        }
        return multiplexerServer;
    }

    public OAServerImpl getOAServer() {
        return oaServer;
    }


    /**
        Called by OAServerImpl to allow for "hook" for all messages.
        NOTE: this is called for every client that is sent a message!!!
    */
    public OAObjectMessage sendMessage(int clientId, OAObjectMessage msg) {
        return msg;
    }

    protected void onClientConnect(Socket socket, int connectionId) {
        ClientSession cs = createClientSession(connectionId);
        onClientConnect(cs.clientInfo);
    }
    protected void onClientDisconnect(int connectionId) {
        ClientSession cs = getClientSession(connectionId);
        if (cs != null) {
            OAClientInfo ci = cs.clientInfo;
            ci.setConnectionStatus("disconnected");
            ci.lastClientUpdateReceived = new OADateTime();
            ci.ended = new OADateTime();
            onClientDisconnect(cs.clientInfo);
        }
    }

    
    protected void onClientConnect(OAClientInfo ci) {
    }
    protected void onClientDisconnect(OAClientInfo ci) {
    }
    protected void handleException(int clientId, boolean bFromServer, String msg, Throwable e) {
    }
    protected void onClientInfoUpdated(OAClientInfo ci) {
    }

}
