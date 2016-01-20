package com.viaoa.sync;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.theice.tsam.model.oa.cs.ServerRoot;
import com.theice.tsam.model.oa.propertypath.*;
import com.theice.tsam.delegate.ModelDelegate;
import com.theice.tsam.model.oa.*;
import com.theice.tsam.remote.RemoteAppInterface;
import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObjectKey;
import com.viaoa.sync.remote.RemoteBroadcastInterface;
import com.viaoa.util.OALogUtil;
import com.viaoa.util.OAString;


/**
 * **** IMPORTANT **** 
 *      to run as Junit test, OASyncServerTest will need to be running in a separate JVM
 *
 *      Run this class in 1+ other JVMs
 * *******************
 */
public class OASyncClientTest extends OAUnitTest {
    private static int port = 1099;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;

    private RemoteAppInterface remoteApp;
    private RemoteBroadcastInterface remoteBroadcast, remoteBroadcastHold;
    private ArrayBlockingQueue<String> queBroadcastMessages = new ArrayBlockingQueue<String>(100);
    final CountDownLatch countDownLatchMain = new CountDownLatch(1);
    
    //@Test
    public void testA() throws Exception {
        if (serverRoot == null) return;
    
        remoteApp.isRunningAsDemo();
        remoteApp.getRelease();
        
        final Site site = serverRoot.getSites().getAt(0);
        site.setProduction(false);
        
        final AtomicInteger ai = new AtomicInteger();
        
        serverRoot.getSites().addHubListener(new HubListenerAdapter<Site>() {
            @Override
            public void afterPropertyChange(HubEvent<Site> e) {
                if (!Site.P_AbbrevName.equalsIgnoreCase(e.getPropertyName())) return;
                if (site != e.getObject()) return;
                if (site == null) return;
                if (!site.getProduction()) return;
                ai.incrementAndGet();
            }
        });
        
        site.setName("xx");
        
        assertEquals(0, ai.get());
        site.setProduction(true);
        
        for (int i=0; i<10; i++) {
            Thread.sleep(100);
            if (!site.getProduction()) break;
        }

        assertEquals(100, ai.get());
        assertFalse(site.getProduction());
        assertNotEquals("xx", site.getName());
    }

    //@Test
    public void testB() throws Exception {
        if (serverRoot == null) return;
    
        remoteApp.isRunningAsDemo();
        remoteApp.getRelease();
        
        final Site site = serverRoot.getSites().getAt(0);
        site.setProduction(false);  // dont trigger testA

        final int maxThreads = 7;
        final int maxIterations = 500;
        final CyclicBarrier barrier = new CyclicBarrier(maxThreads);
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreads);
        final AtomicInteger aiDone = new AtomicInteger();
        
        final AtomicInteger ai = new AtomicInteger();
        
        for (int i=0; i<maxThreads; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        for (int i=0; i<maxIterations; i++) {
                            site.setAbbrevName("id."+i);
                            ai.incrementAndGet();

                        }
                    }
                    catch (Exception e) {
                        System.out.println("Test error: "+e);
                        e.printStackTrace();
                    }
                    finally {
                        aiDone.getAndIncrement();
                        countDownLatch.countDown();
                    }
                }
            });
            t.start();
        }
        countDownLatch.await(5, TimeUnit.SECONDS);
        
        assertEquals(maxThreads * maxIterations, ai.get());
    }
    
    @Test
    public void testC() throws Exception {
        if (serverRoot == null) return;
        
        remoteBroadcast.startTest();
        
        testMain();
        
        Thread.sleep(5000);
        
        for (String s : queBroadcastMessages) {
            System.out.println("OASyncClientTest error="+s);
        }
        assertEquals(0, queBroadcastMessages.size());
        
    }
    
    public void testMain() throws Exception {
        Server server = new Server();
        ModelDelegate.getSites().getAt(0).getEnvironments().getAt(0).getSilos().getAt(0).getServers().add(server);
        
        long ms1 = System.currentTimeMillis();
        
        Hub<Application> h = server.getApplications();
        boolean b = false;
        while ( (System.currentTimeMillis() - ms1) < (15 * 60 * 1000)) {
            server.setName(OAString.getRandomString(1, 20));
            if (h.getSize() == 0 || (h.getSize() < 100 && Math.random() < .5)) {
                Application app = new Application();
                h.add(app);
            }
            else {
                if (Math.random() < .33) {
                    h.deleteAll();
                    b = true;
                }
                else h.getAt(0).delete();
            }
        }
        if (!b) h.removeAll();
        
        String s = server.getName();
        remoteBroadcast.sendName(server, s);
        remoteBroadcast.sendAppCount(server, server.getApplications().getSize());
    }
    
    
    
    //@Test
    public void objectLinkMatchTest() {
        if (serverRoot == null) return;
        OAFinder<Site, Application> finder = new OAFinder<Site, Application>(SitePP.environments().silos().servers().applications().pp);
        for (Application app : finder.find(serverRoot.getSites())) {
            // app.AppVersions is an autoMatch, and it should be auto populated
            Hub h1 = app.getApplicationType().getPackageTypes();
            
            Hub h2 = app.getApplicationVersions();
            assertEquals(h1.size(), h2.size());

            //h2 = app.getInstallVersions();
            //assertEquals(h1.size(), h2.size());
        }
    }
    
    //@Test
    public void deleteTest() {
        if (serverRoot == null) return;
        String pp = SitePP.environments().silos().pp;
        OAFinder<Site, Silo> finder = new OAFinder<Site, Silo>(pp) {
            @Override
            protected void onFound(Silo silo) {
                silo.getServers().deleteAll();
            }
        };
        
        ArrayList<Silo> al = finder.find(serverRoot.getSites());
        
        for (Silo silo : al) {
            assertEquals(silo.getServers().size(), 0);
        }
    }
    
    @Before
    public void setup() throws Exception {
        MultiplexerClient.DEBUG = true;
        syncClient = new OASyncClient("localhost", port);
        
        
        // **NOTE** need to make sure that ServerTest is running in another jvm
        try {
            syncClient.start();

            remoteApp = (RemoteAppInterface) syncClient.lookup(RemoteAppInterface.BindName);
            remoteBroadcastHold = new RemoteBroadcastInterface() {
                @Override
                public void sendName(Server server, String name) {
                    System.out.println("sendName called");
                    if (!OAString.isEqual(server.getName(), name)) {
                        queBroadcastMessages.add(server.getId()+" has name="+server.getName()+", broadcast name="+name);
                    }
                }
                @Override
                public void sendAppCount(Server server, int cnt) {
                    System.out.println("sendAppCount called");
                    if (server.getApplications().getSize() != cnt) {
                        queBroadcastMessages.add(server.getId()+" has cnt="+server.getApplications().getSize()+", broadcast cnt="+cnt);
                    }
                }
                @Override
                public void startTest() {
                    countDownLatchMain.countDown();
                }
            };
            
            remoteBroadcast = (RemoteBroadcastInterface) syncClient.lookupBroadcast(RemoteBroadcastInterface.BindName, remoteBroadcastHold);
            
            
            serverRoot = remoteApp.getServerRoot();
            ModelDelegate.initialize(serverRoot, null);
            // serverRoot = (ServerRoot) syncClient.getRemoteServer().getObject(ServerRoot.class, new OAObjectKey(777));
        }
        catch (Exception e) {
            System.out.println("NOT running OASyncClientTest, OASyncServerTest is not running in a separate jvm");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        System.out.println("stopping client");
        syncClient.stop();
        System.out.println("client stopped");
    }

    public static void main(String[] args) throws Exception {
        OALogUtil.consoleOnly(Level.FINE);
        OASyncClientTest test = new OASyncClientTest();
        test.setup();
        System.out.println("waiting for unit test OASyncClientTest to broadcast start message");
        test.countDownLatchMain.await();
        System.out.println("running test");
        test.testMain();
        Thread.sleep(2500);
        test.tearDown();
        System.out.println("DONE running test");
    }
}
