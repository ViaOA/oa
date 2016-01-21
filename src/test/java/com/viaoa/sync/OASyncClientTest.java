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
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAFinder;
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
    
    private final int testSeconds = 10;

    private RemoteAppInterface remoteApp;
    private RemoteBroadcastInterface remoteBroadcast, remoteBroadcastHold;
    private ArrayBlockingQueue<String> queBroadcastMessages = new ArrayBlockingQueue<String>(100);
    final CountDownLatch countDownLatchStart = new CountDownLatch(1);
    final CountDownLatch countDownLatchSendResults = new CountDownLatch(1);
    volatile boolean bStopCalled;
    private AtomicInteger aiOnClientStart = new AtomicInteger();
    private AtomicInteger aiOnClientDone = new AtomicInteger();

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
    
    boolean bRunningInJunit;
    
    @Test
    public void testC() throws Exception {
        if (serverRoot == null) return;
        bRunningInJunit = true;
        
        System.out.println("Starting tests");
        remoteBroadcast.startTest();
        
        testMain(testSeconds);
        
        System.out.println("Broadcast.stopTest, "+aiOnClientStart.get()+" other clients are in this test");
        remoteBroadcast.stopTest();
        
        Thread.sleep(2500);
        for (int i=0; i<20 && aiOnClientStart.get() > aiOnClientDone.get(); i++) {
            System.out.println("waiting for other clients to stop, total started="+aiOnClientStart.get()+", done="+aiOnClientDone.get());
            Thread.sleep(500);
        }
        System.out.println("Broadcast.sendResults, total started="+aiOnClientStart.get()+", done="+aiOnClientDone.get());
        remoteBroadcast.sendResults();
        Thread.sleep(2500);
        
        System.out.println("Error list, size="+queBroadcastMessages.size());
        for (String s : queBroadcastMessages) {
            System.out.println("OASyncClientTest error="+s);
        }
        assertEquals(0, queBroadcastMessages.size());
        
    }
    
    public void testMain() throws Exception {
        testMain(testSeconds);
    }

    CountDownLatch cdlDone = new CountDownLatch(5);
    public void testMain(final int secondsToRun) throws Exception {
        int maxThreads = 5;
        for (int i=0; i<maxThreads; i++) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        _testMain(secondsToRun);
                    }
                    catch (Throwable e) {
                    }
                    finally {
                        cdlDone.countDown();                        
                    }
                }
            });
            t.start();
        }
        _testMain(secondsToRun);
    }
    
    public void _testMain(int secondsToRun) throws Exception {
        Site site = ModelDelegate.getSites().getAt(0);
        Environment env = site.getEnvironments().getAt(0);
        Silo silo = env.getSilos().getAt(0);

        Server server;
        server = new Server();
        silo.getServers().add(server);

        //server = silo.getServers().getAt(0);
        
        long msEnd = System.currentTimeMillis() + (secondsToRun * 1000);
        
        Hub<Application> h = server.getApplications();
        int cnt = 0;
        boolean bServerDelete = false;
        while (System.currentTimeMillis() < msEnd && !bStopCalled) {
            cnt++;
            site.setName(OAString.getRandomString(1, 20)+"."+cnt);
            
            //server = silo.getServers().getAt( (int) (silo.getServers().getSize() * Math.random()) );
            //if (server == null) continue;
            
            /*
            if (!bServerDelete && Math.random() < .1) {
                server.delete();
                bServerDelete = true;
                continue;
            }
            */

            server.setName(OAString.getRandomString(1, 20)+"."+cnt);

            double d;
            int x = h.getSize();
            if (x == 0) d = 1.0;
            else if (x > 100) d = .12;
            else if (x < 25) d = .85;
            else if (x < 50) d = .65;
            else d = .5;
            
            if (Math.random() < d) {
                Application app = new Application();
                if (Math.random() < .5) app.setServer(server);
                else h.add(app);
            }
            else {
                if (x > 20 && Math.random() < .20) {
                    h.deleteAll();
                }
                else h.getAt(0).delete();
            }
        }
    }
    
    public void sendResults() {
        Site site = ModelDelegate.getSites().getAt(0);
        Environment env = site.getEnvironments().getAt(0);
        Silo silo = env.getSilos().getAt(0);
        remoteBroadcast.sendName(site, site.getName());
        for (Server serv : silo.getServers()) {
            remoteBroadcast.sendName(serv, serv.getName());
            remoteBroadcast.sendAppCount(serv, serv.getApplications().getSize());
        }
        
        int xx = 4;
        xx++;
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
                    //System.out.println("server.sendName called, name="+name);
                    if (!OAString.isEqual(server.getName(), name)) {
                        queBroadcastMessages.offer("serverId="+server.getId()+", server.name="+server.getName()+", broadcast name="+name);
                    }
                }
                @Override
                public void sendAppCount(Server server, int cnt) {
                    //System.out.println("sendAppCount called, cnt="+cnt);
                    if (server.getApplications().getSize() != cnt) {
                        queBroadcastMessages.offer("serverId="+server.getId()+", App.cnt="+server.getApplications().getSize()+", broadcast cnt="+cnt);
                    }
                }
                @Override
                public void startTest() {
                    System.out.println("received startTest message");
                    countDownLatchStart.countDown();
                }
                @Override
                public void stopTest() {
                    System.out.println("received stopTest message");
                    bStopCalled = true;
                }
                @Override
                public void sendResults() {
                    System.out.println("received sendResults message");
                    bStopCalled = true;
                    countDownLatchSendResults.countDown();
                }
                @Override
                public void sendName(Site site, String name) {
                    System.out.println("site.sendName called, name="+name);
                    if (!OAString.isEqual(site.getName(), name)) {
                        queBroadcastMessages.offer(site.getId()+" has site.name="+site.getName()+", broadcast name="+name);
                    }
                }
                @Override
                public void onClientStart() {
                    aiOnClientStart.incrementAndGet();
                }
                @Override
                public void onClientDone() {
                    aiOnClientDone.incrementAndGet();
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
        test.countDownLatchStart.await();
        System.out.println("running test");
        test.remoteBroadcast.onClientStart();
        test.testMain();
        test.bStopCalled = true;
        boolean b = test.cdlDone.await(7, TimeUnit.SECONDS);
        if (!b) System.out.println("ERROR qqqqqqqqqqqq all threads were not done qqqqqqqqqqqqqq");
        test.remoteBroadcast.onClientDone();
        System.out.println("test done, waiting on stop message");
        
        test.countDownLatchSendResults.await();
        test.sendResults();
        test.remoteBroadcast.onClientDone();
        Thread.sleep(500);
        
        test.tearDown();
        System.out.println("DONE running test, exiting program");
    }
}
