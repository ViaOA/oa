package com.viaoa.sync;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.object.OAThreadLocal;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.sync.remote.RemoteBroadcastInterface;
import com.viaoa.sync.remote.RemoteTestInterface;
import com.viaoa.util.*;
import com.theice.tsam.delegate.ModelDelegate;
import com.theice.tsam.delegate.RemoteDelegate;
import com.theice.tsam.model.oa.AdminUser;
import com.theice.tsam.model.oa.Environment;
import com.theice.tsam.model.oa.Server;
import com.theice.tsam.model.oa.Site;
import com.theice.tsam.model.oa.cs.ClientRoot;
import com.theice.tsam.model.oa.cs.ServerRoot;
import com.theice.tsam.remote.RemoteAppImpl;
import com.theice.tsam.remote.RemoteAppInterface;
import com.theice.tsam.remote.RemoteModelImpl;
import com.theice.tsam.remote.RemoteModelInterface;

/**
 *  Run this manually to then run OASyncClientTest junit tests.
 */
public class OASyncServerTest {
    private static Logger LOG = Logger.getLogger(OASyncServerTest.class.getName());
    
    private ServerRoot serverRoot;    
    public OASyncServer syncServer;

    public void start() throws Exception {  
        serverRoot = readSerializeFromFile();
        setupTestA();
        ModelDelegate.initialize(serverRoot, null);
        getSyncServer().start();
    }
    public void stop() throws Exception {
        if (syncServer != null) {
            syncServer.stop();
        }
    }

    public void setupTestA() {
        serverRoot.getSites().addHubListener(new HubListenerAdapter<Site>() {
            @Override
            public void afterPropertyChange(HubEvent<Site> e) {
                final Site site = e.getObject();
                if (site == null) return;
                if (!Site.P_Production.equalsIgnoreCase(e.getPropertyName())) return;
                
                if (!site.getProduction()) return;

                OAThreadLocalDelegate.setSendMessages(true);
                site.setName("Server running test");
                
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i<100; i++) {
                            site.setAbbrevName("ab."+i);
                        }
                        site.setProduction(false);
                    }
                }); 
                t.start();
            }
        });
    }
    
    
    public OASyncServer getSyncServer() {
        if (syncServer != null) return syncServer;
        
        syncServer = new OASyncServer(1099) {
            @Override
            protected void onClientConnect(Socket socket, int connectionId) {
                super.onClientConnect(socket, connectionId);
                //OASyncServerTest.this.onClientConnect(socket, connectionId);
            }
            @Override
            protected void onClientDisconnect(int connectionId) {
                super.onClientDisconnect(connectionId);
                //OASyncServerTest.this.onClientDisconnect(connectionId);
            }
        };

        // setup remote objects
        RemoteAppInterface remoteApp = new RemoteAppImpl() {
            @Override
            public void saveData() {
            }
            @Override
            public AdminUser getUser(int clientId, String userId, String password, String location, String userComputerName) {
                return null;
            }
            @Override
            public ServerRoot getServerRoot() {
                return serverRoot;
            }
            @Override
            public ClientRoot getClientRoot(int clientId) {
                return null;
            }
            @Override
            public boolean isRunningAsDemo() {
                return false;
            }
            @Override
            public boolean disconnectDatabase() {
                return false;
            }
            @Override
            public OAProperties getServerProperties() {
                return null;
            }
            @Override
            public boolean writeToClientLogFile(int clientId, ArrayList al) {
                return false;
            }
        };
        
        syncServer.createSyncLookup(RemoteAppInterface.BindName, remoteApp, RemoteAppInterface.class);
        RemoteDelegate.setRemoteApp(remoteApp);
        
        
        RemoteModelImpl remoteModel = new RemoteModelImpl();
        syncServer.createSyncLookup(RemoteModelInterface.BindName, remoteModel, RemoteModelInterface.class);
        RemoteDelegate.setRemoteModel(remoteModel);

        
        RemoteTestInterface remoteTest = new RemoteTestInterface() {
            @Override
            public String getName(Server server) {
                return server.getName();
            }
        }; 
        syncServer.createLookup(RemoteTestInterface.BindName, remoteTest, RemoteTestInterface.class);

        remoteBroadcast = new RemoteBroadcastInterface() {
            @Override
            public void sendName(Server server, String name) {
            }
            @Override
            public void sendAppCount(Server server, int cnt) {
            }
            @Override
            public void startTest() {
/*                
                Server server = new Server();
                ModelDelegate.getSites().getAt(0).getEnvironments().getAt(0).getSilos().getAt(0).getServers().add(server);
                server.setName("name on server");
                remoteBroadcast.sendName(server, server.getName());
                remoteBroadcast.sendAppCount(server, server.getApplications().getSize());
*/                
            }
            @Override
            public void sendName(Site site, String name) {
            }
            @Override
            public void stopTest() {
            }
            @Override
            public void sendResults() {
            }
            @Override
            public void onClientStart() {
                // TODO Auto-generated method stub
                
            }
            @Override
            public void onClientDone() {
                // TODO Auto-generated method stub
                
            }
        }; 
        remoteBroadcast = (RemoteBroadcastInterface) syncServer.createBroadcast(RemoteBroadcastInterface.BindName, remoteBroadcast, RemoteBroadcastInterface.class, "broadcast", 100);
        
        return syncServer;
    }   
    private RemoteBroadcastInterface remoteBroadcast;
    

    private ServerRoot readSerializeFromFile() throws Exception {
        File file = new File(OAFile.convertFileName("runtime/test/tsam.bin"));
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = new FileInputStream(file);
    
        Inflater inflater = new Inflater();
        InflaterInputStream inflaterInputStream = new InflaterInputStream(fis, inflater, 1024*3);
        
        ObjectInputStream ois = new ObjectInputStream(inflaterInputStream) {
            @Override
            protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
                ObjectStreamClass cd = super.readClassDescriptor();
                /*
                try {
                    Field f = cd.getClass().getDeclaredField("name");
                    f.setAccessible(true);
                    String name = (String) f.get(cd);
                    String name2 = OAString.convert(name, ".tsac.", ".tsac2.");
                    name2 = OAString.convert(name2, "com.", "test.");
                    f.set(cd, name2);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                */
                return cd;
            }
        };

        OAObjectSerializer wrap = (OAObjectSerializer) ois.readObject(); 
        ServerRoot serverRoot = (ServerRoot) wrap.getObject();
        String version = (String) ois.readObject();
        
        ois.close();
        fis.close();

        return serverRoot;
    }

    public static void main(String[] args) throws Exception {
        MultiplexerServer.DEBUG = true;
        OALogUtil.consoleOnly(Level.FINE);
        OASyncServerTest test = new OASyncServerTest();
        
        test.start();
        
        for (int i=1;;i++) {
            int x = test.syncServer.getSessionCount();
            System.out.println(i+") ServerTest is running "+(new OATime())+",  sessionCount="+x);
            Thread.sleep(10 * 1000);
        }
    }
    
}
