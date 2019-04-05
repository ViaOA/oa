package com.viaoa.sync;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import test.xice.tsac.model.oa.propertypath.SitePP;
import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.delegate.RemoteDelegate;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.cs.ClientRoot;
import test.xice.tsam.model.oa.cs.ServerRoot;
import test.xice.tsam.remote.RemoteAppImpl;
import test.xice.tsam.remote.RemoteAppInterface;
import test.xice.tsam.remote.RemoteModelImpl;
import test.xice.tsam.remote.RemoteModelInterface;
import test.xice.tsam.util.DataGenerator;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.remote.RemoteBroadcastInterface;
import com.viaoa.sync.remote.RemoteTestInterface;
import com.viaoa.sync.remote.RemoteTsamInterface;
import com.viaoa.util.*;

/**
 *  Run this manually, and then run OASyncClientTest multiple times, and then run it as a junit test.
 */
public class OASyncServerTest {
    private static Logger LOG = Logger.getLogger(OASyncServerTest.class.getName());
    
    private ServerRoot serverRoot;    
    public OASyncServer syncServer;

    public void start() throws Exception {
        DataGenerator dg = new DataGenerator();
        serverRoot = dg.readSerializeFromFile();
        setupTestB();
        ModelDelegate.initialize(serverRoot, null);
        getSyncServer().start();
    }
    public void stop() throws Exception {
        if (syncServer != null) {
            syncServer.stop();
        }
    }

    public void setupTestB() {
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
                        System.out.println("TestA start "+((new OATime()).toString("HH:mm:ss.SSS")));
                        Silo silo = site.getEnvironments().getAt(0).getSilos().getAt(0);
                        Server server = silo.getServers().getAt(0);
                        MRADServer mradServer = silo.getMRADServer();
                        if (mradServer == null) {
                            mradServer = new MRADServer();
                            silo.setMRADServer(mradServer);
                        }
                        
                        for (int i=0; i<2000; i++) {
                            if ((i+1)%250==0) {
                                System.out.println((i+1));
                            }
                            
                            site.setAbbrevName("test."+i);
                            server.setName("test."+i);
                            
                            MRADServerCommand sc = new MRADServerCommand();
                            for (int j=0; j<5; j++) {
                                MRADClientCommand cc = new MRADClientCommand();
                                sc.getMRADClientCommands().add(cc);
                                if (j == 0) mradServer.getMRADServerCommands().add(sc);
                            }
                            sc.delete();
                        }
                        site.setProduction(false);
                        System.out.println("TestA done "+((new OATime()).toString("HH:mm:ss.SSS")));
                    }
                }); 
                t.start();
            }
        });
    }
    
    public OASyncServer getSyncServer() {
        if (syncServer != null) return syncServer;
        
        // for non-DB objects
        OADataSourceObjectCache ds = new OADataSourceObjectCache();
        // ds.setAssignIdOnCreate(true);
        
        syncServer = new OASyncServer(1102) {
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
            int cnt=0;
            @Override
            protected void afterInvokeRemoteMethod(RequestInfo ri) {
//                super.afterInvokeRemoteMethod(ri);
System.out.println((++cnt)+") "+ri.toLogString());
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
            @Override
            public MRADServerCommand createMRADServerCommand() {
                MRADServerCommand msc = new MRADServerCommand();
msc.save();                
                return msc;
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


        // TSAM test
        RemoteTsamInterface remoteTsam = new RemoteTsamInterface() {
            @Override
            public MRADServerCommand createMRADServerCommand(AdminUser user, Hub<MRADClient> hub, Command command) {
                MRADServerCommand msc = new MRADServerCommand();
                msc.setAdminUser(user);
                msc.setCommand(command);
                if (hub != null) {
                    for (MRADClient mcx : hub) {
                        MRADClientCommand ccmd = new MRADClientCommand();
                        ccmd.setMRADClient(mcx);
                        msc.getMRADClientCommands().add(ccmd);
                    }
                }
                ModelDelegate.getDefaultSilo().getMRADServer().getMRADServerCommands().add(msc);
                return msc;
            }
            @Override
            public boolean runCommand(MRADServerCommand cmd) {
                // cause some "noise"
                OASyncServerTest.this.runCommand(cmd);
                return true;
            }
        }; 
        syncServer.createLookup(RemoteTsamInterface.BindName, remoteTsam, RemoteTsamInterface.class, OASyncServer.SyncQueueName, 0);
        
        
        
        remoteBroadcast = new RemoteBroadcastInterface() {
            @Override
            public void startTest() {
            }
            @Override
            public void stopTest() {
            }
            @Override
            public void sendStats() {
                OAFinder<Site, Server> f = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp) {
                    @Override
                    protected void onFound(Server server) {
                        respondStats(server, server.getName(), server.getApplications().getSize(), server.getNameChecksum());
                    }
                };
                f.find(ModelDelegate.getSites());
            }
            @Override
            public void respondStats(Site site, String name) {
            }
            @Override
            public void respondStats(Server server, String name, int cntApps, long nameChecksum) {
            }
            @Override
            public void respondStats(String msg) {
            }
            @Override
            public void onClientTestStarted() {
            }
            @Override
            public void onClientTestDone() {
            }
            @Override
            public void onClientStatsSent() {
            }
            @Override
            public void onClientDone() {
            }
        }; 
        remoteBroadcast = (RemoteBroadcastInterface) syncServer.createBroadcast(RemoteBroadcastInterface.BindName, remoteBroadcast, RemoteBroadcastInterface.class, OASyncServer.SyncQueueName, 100);
        //was, in a separate queue
        // remoteBroadcast = (RemoteBroadcastInterface) syncServer.createBroadcast(RemoteBroadcastInterface.BindName, remoteBroadcast, RemoteBroadcastInterface.class, "broadcast", 100);
        
        return syncServer;
    }   
    private RemoteBroadcastInterface remoteBroadcast;
    

    private boolean bRunCommand;
    private final AtomicInteger aiRunCommand = new AtomicInteger();
    private final Object lock = new Object();
    protected void runCommand(final MRADServerCommand sc) {
        synchronized (lock) {
            aiRunCommand.incrementAndGet();
            lock.notifyAll();
        }
        if (bRunCommand) return;
        bRunCommand = true;
        for (MRADClientCommand mcz : sc.getMRADClientCommands()) {
            final MRADClientCommand mcx = mcz;
            Thread t = new Thread() {
                int lastCnt;
                @Override
                public void run() {
                    for (;;) {
                        lastCnt = aiRunCommand.get();
                        update();
                        synchronized (lock) {
                            if (lastCnt == aiRunCommand.get()) {
                                try {
                                    lock.wait();
                                }
                                catch (Exception e) {
                                }
                            }
                        }
                    }
                }
                void update() {
                    MRADClient mc = mcx.getMRADClient();
                    if (mc == null) return;
                    Application app = mc.getApplication();
                    if (app == null) {
                        app = new Application();
                        mc.setApplication(app);
                    }
                    HostInfo hi = mc.getHostInfo();
                    if (hi == null) {
                        hi = new HostInfo();
                        mc.setHostInfo(hi);
                    }
                    for (int i=0; i<5; i++) {
                        mc.setStartScript(OAString.createRandomString(8,24));
                        mc.setStopScript(OAString.createRandomString(8,24));
                        hi.setJarDirectory(OAString.createRandomString(88,824));
                        mc.setApplicationStatus(OAString.createRandomString(6,24));
                    }
                    Hub<ApplicationStatus> h = serverRoot.getApplicationStatuses();
                    ApplicationStatus as = h.getAt( (int) (Math.random() * h.size()));
                    app.setApplicationStatus(as);
                }
            };
            t.start();
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        OAObject.setDebugMode(true);
        OALogUtil.consoleOnly(Level.FINE, OACircularQueue.class.getName());//"com.viaoa.util.OACircularQueue");
        
        Logger logx = Logger.getLogger(OACircularQueue.class.getName());
        
        OASyncServerTest test = new OASyncServerTest();
        
        test.start();
        
        int scnt = -1;
        for (int i=1;;i++) {
            int x;
            do {
                x = test.syncServer.getSessionCount();
                Thread.sleep(1 * 1000);
            }
            while (x == scnt);
            scnt = x;
            System.out.println(i+") ServerTest is running "+(new OATime())+",  sessionCount="+x);
        }
    }
    
}
