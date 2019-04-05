// Copied from OATemplate project by OABuilder 03/22/14 07:48 PM
package test.xice.tsam.control.server;


import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Logger;

import com.viaoa.sync.OASyncServer;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.util.OADateTime;

public abstract class RemoteServerController {

    private static Logger LOG = Logger.getLogger(RemoteServerController.class.getName());
    private int port;
    private OASyncServer syncServer;

    public RemoteServerController(int port) {
        LOG.config("OAServer using port="+port);
        this.port = port;
    }

    public OASyncServer getSyncServer() {
        if (syncServer == null) {
            syncServer = new OASyncServer(port) {
                @Override
                protected String getLogFileName() {
                    return RemoteServerController.this.getLogFileName();
                }
                @Override
                protected void onClientConnect(Socket socket, int connectionId) {
                    super.onClientConnect(socket, connectionId);
                    RemoteServerController.this.onClientConnect(socket, connectionId);
                }
                @Override
                protected void onClientDisconnect(int connectionId) {
                    super.onClientDisconnect(connectionId);
                    RemoteServerController.this.onClientDisconnect(connectionId);
                }
                @Override
                public void onUpdate(ClientInfo ci) {
                    super.onUpdate(ci);
                    RemoteServerController.this.onUpdate(ci);
                }
                @Override
                protected void onClientException(ClientInfo ci, String msg, Throwable ex) {
                    super.onClientException(ci, msg, ex);
                    RemoteServerController.this.onClientException(ci, msg, ex);
                }
            };
            
            ClientInfo ci = syncServer.getClientInfo();
            ci.setUserId("");
            ci.setUserName(System.getProperty("user.name"));
            try {
                InetAddress localHost = InetAddress.getLocalHost();
                ci.setHostName(localHost.getHostName());
                ci.setIpAddress(localHost.getHostAddress());
            }
            catch (Exception e) {
            }
            ci.setLocation("");
            int release = 1;
            ci.setVersion(""+release);
            onUpdate(ci);
        }
        return syncServer;
    }
    
    public abstract void onUpdate(ClientInfo ci);
    protected abstract void onClientException(ClientInfo ci, String msg, Throwable ex);
    
    public void stop() throws Exception {
        if (syncServer != null) {
            syncServer.stop();
        }
    }
    
    public void start() throws Exception {
        String msg = "test";
        msg += ", version=1";
        msg += ", started=" + (new OADateTime());
        getSyncServer().setInvalidConnectionMessage(msg);
        getSyncServer().start();
        LOG.config("Started remote server, "+msg);
    }

    
    protected String getLogFileName() {
        return null;
    }
    protected void onClientConnect(Socket socket, int connectionId) {
    }
    protected void onClientDisconnect(int connectionId) {
    }
}
