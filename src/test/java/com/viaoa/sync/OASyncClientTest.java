package com.viaoa.sync;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import test.theice.tsac.model.oa.propertypath.SitePP;
import test.theice.tsam.delegate.ModelDelegate;
import test.theice.tsam.model.oa.*;
import test.theice.tsam.model.oa.cs.ServerRoot;
import test.theice.tsam.remote.RemoteAppInterface;

import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.remote.RemoteBroadcastInterface;
import com.viaoa.util.OALogUtil;
import com.viaoa.util.OAString;
import com.viaoa.util.OATime;

/**
 * Client for OASyncServerTest.  
 * This will run within junit test and will connect to oasyncservertest, and any other standalone instances of oasyncclienttest.
 * 
 * IMPORTANT 
 *      to run as Junit test, OASyncServerTest will need to be running in a separate JVM
 *      and then run 1+ of these in a separate JVM
 *
 * If the server is not running, then unit test will not fail, the tests in this class will just exit without any errors.
 */
public class OASyncClientTest extends OAUnitTest {
    private static int port = 1101;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;
    
    private final int maxThreads = 7;
    private final int testSeconds = 30;

    private RemoteAppInterface remoteApp;
    private RemoteBroadcastInterface remoteBroadcast, remoteBroadcastHold;
    private ArrayBlockingQueue<String> queErrors = new ArrayBlockingQueue<String>(100);
    
    private final CountDownLatch cdlStart = new CountDownLatch(1);
    private final CountDownLatch cdlSendStats = new CountDownLatch(1);
    private CountDownLatch cdlThreadsDone = new CountDownLatch(maxThreads);
    
    private volatile boolean bStopCalled;
    private AtomicInteger aiOnClientTestStart = new AtomicInteger();
    private AtomicInteger aiOnClientTestDone = new AtomicInteger();
    private AtomicInteger aiOnClientSentStats = new AtomicInteger();
    private AtomicInteger aiOnClientDone = new AtomicInteger();
    private AtomicInteger aiSendStats = new AtomicInteger();

    
    @Test
    public void testC() throws Exception {
        if (serverRoot == null) return;

        final Site site = serverRoot.getSites().getAt(0);
        OADataSource ds = OADataSource.getDataSource(MRADClientCommand.class);
        assertNotNull(ds);  // make sure that the server has DS setup
        Silo silo = site.getEnvironments().getAt(0).getSilos().getAt(0);
        Server server = silo.getServers().getAt(0);

        MRADServer mradServer = silo.getMRADServer();
        
        MRADServerCommand msc = new MRADServerCommand();
        assertEquals(0, msc.getId());

        
        
        long ms = System.currentTimeMillis();
        for (int i=0; i<1000 ;i++) {
            MRADClientCommand mcc = new MRADClientCommand();
            assertEquals(0, mcc.getId());
            msc.getMRADClientCommands().add(mcc);
        }
        mradServer.getMRADServerCommands().add(msc);
     
        long msDiff = System.currentTimeMillis() - ms;
        System.out.println("msDiff="+msDiff);
        assertTrue(msDiff < 1500);

        assertEquals(0, msc.getId());
        mradServer.save();
        assertNotEquals(0, msc.getId());
        for (MRADClientCommand mc : msc.getMRADClientCommands()) {
            assertNotEquals(0, mc.getId());
        }
    }    
    
    /**
     * Run basic tests with oasyncservertest
     * @throws Exception
     */
//qq    @Test
    public void testA() throws Exception {
        if (serverRoot == null) return;
    
        remoteApp.isRunningAsDemo();
        remoteApp.getRelease();
        
        final Site site = serverRoot.getSites().getAt(0);
        site.setProduction(false);  // if true, then this is the trigger on server to start a new thread

        Silo silo = site.getEnvironments().getAt(0).getSilos().getAt(0);
        Server server = silo.getServers().getAt(0);
        MRADServer mradServer = silo.getMRADServer();
        
        
        final AtomicInteger aiServerCalledPropChange = new AtomicInteger();
        
        serverRoot.getSites().addHubListener(new HubListenerAdapter<Site>() {
            @Override
            public void afterPropertyChange(HubEvent<Site> e) {
                if (!Site.P_AbbrevName.equalsIgnoreCase(e.getPropertyName())) return;
                if (site != e.getObject()) return;
                if (site == null) return;
                if (!site.getProduction()) return;
                aiServerCalledPropChange.incrementAndGet();
            }
        });

        mradServer.getMRADServerCommands().addHubListener(new HubListenerAdapter<MRADServerCommand>() {
            @Override
            public void afterAdd(HubEvent<MRADServerCommand> e) {
                // this will cause a callback to the server, which should not cause the client to disconnect/etc
                MRADServerCommand mc = e.getObject();
                mc.getMRADClientCommands();
            }
        });
        
        site.setName("xx");
        
        assertEquals(0, aiServerCalledPropChange.get());
        site.setProduction(true);  // this will trigger the server
        
        for (int i=0; i<120; i++) {
            if (!site.getProduction()) break;
            Thread.sleep(1000);
            if (bStopCalled) break;
        }
        boolean b = site.getProduction();
        
        assertEquals(2000, aiServerCalledPropChange.get());
        assertFalse(site.getProduction());
        assertNotEquals("xx", site.getName());
    }

//qq    @Test
    public void testB() throws Exception {
        if (serverRoot == null) return;
    
        remoteApp.isRunningAsDemo();
        remoteApp.getRelease();
        
        final Site site = serverRoot.getSites().getAt(0);
        site.setProduction(false);  // dont trigger testA

        final int maxIterations = 100;
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
        boolean b = countDownLatch.await(45, TimeUnit.SECONDS);
        if (!b) System.out.println("Warning: countDownLatch had a timeout, over 45 seconds");
        
        assertEquals(maxThreads * maxIterations, ai.get());
    }
    
    /**
     * This will run with other instances that are running in their own jvm
     */
//qq    @Test
    public void testForMain() throws Exception {
        if (serverRoot == null) return;

        Site site = serverRoot.getSites().getAt(0);
        site.setName("run");
        
        System.out.println("Starting tests "+(new OATime()).toString("hh:mm:ss.S")+", for "+testSeconds+" seconds");

        // send message 
        remoteBroadcast.startTest();

        testMain(testSeconds);

        System.out.println("DONE tests "+(new OATime()).toString("hh:mm:ss.S"));
        sendStats(); // for this

        System.out.println("Broadcast.stopTest, "+aiOnClientTestStart.get()+" other clients are in this test, "+(new OATime()).toString("hh:mm:ss.S"));
        remoteBroadcast.stopTest();
        
        for (int i=0; aiOnClientTestStart.get() == 0 || aiOnClientTestStart.get() > aiOnClientTestDone.get(); i++) {
            if (aiOnClientTestStart.get() == 0) {
                if (i > 2) break; /// no other clients
            }
            else System.out.println((i+1)+") waiting for other clients to stop, total started="+aiOnClientTestStart.get()+", testDone="+aiOnClientTestDone.get());
            Thread.sleep(1000);
        }

        site.setName("done");  // puts message in queue for other clients to notify they are done once they see this
        
        
        System.out.println("Broadcast.sendResults, total started="+aiOnClientTestStart.get()+", done="+aiOnClientTestDone.get());
        remoteBroadcast.sendStats();
        
        for (int i=0; aiOnClientTestStart.get() > aiOnClientSentStats.get(); i++) {
            System.out.println((i+1)+"0) waiting for other clients to sendStats, total started="+aiOnClientTestStart.get()+", sentStats="+aiOnClientSentStats.get());
            Thread.sleep(1000);
        }

        /* not really needed
        for (int i=0; aiOnClientTestStart.get() > aiOnClientDone.get(); i++) {
            System.out.println((i+1)+") waiting for other clients to stop, total started="+aiOnClientTestStart.get()+", done="+aiOnClientTestDone.get());
            Thread.sleep(1000);
        }
        */

        System.out.println("server.stats sent="+aiSendStats.get());
        
        if (queErrors.size() == 0) System.out.println("No errors"); 
        else {
            System.out.println("Error list, size="+queErrors.size());
            for (String s : queErrors) {
                System.out.println("   ERROR: "+s);
            }
        }
        assertEquals(0, queErrors.size());
        
        
        assertTrue(aiOnClientTestStart.get() == 0 || aiSendStats.get() > 0);
    }
    
    public void testMain() throws Exception {
        testMain(testSeconds);
    }

    public void testMain(final int secondsToRun) throws Exception {
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
                        cdlThreadsDone.countDown();                        
                    }
                }
            }, "TEST_THREAD_"+i);
            t.start();
        }
        _testMain(secondsToRun);
        bStopCalled = true;

        cdlThreadsDone.await();

        System.out.printf("STATS: Cnt=%,d, propChangeTotal=%,dms, Avg=%,.2f\n", 
            aiCnt.get(), aiTimeMs.get(),  
            ((double)aiTimeMs.get()) / aiCnt.get()
        );
    }

    private AtomicInteger aiCnt = new AtomicInteger();
    private AtomicInteger aiCntNewApp = new AtomicInteger();
    private AtomicInteger aiCntDeleteApp = new AtomicInteger();
    private AtomicInteger aiCntDeleteAllApp = new AtomicInteger();
    private AtomicLong aiTimeMs = new AtomicLong();
    
    public void _testMain(int secondsToRun) throws Exception {
        Site site = ModelDelegate.getSites().getAt(0);
        Environment env = site.getEnvironments().getAt(0);
        Silo silo = env.getSilos().getAt(0);

        final Server serverNew = new Server();
        silo.getServers().add(serverNew);
        long msEnd = System.currentTimeMillis() + (secondsToRun * 1000);

        System.out.println("Server size="+silo.getServers().getSize());
        
        AtomicInteger aiThisCnt = new AtomicInteger();
       
        while (System.currentTimeMillis() < msEnd && !bStopCalled) {
            aiCnt.incrementAndGet();
            int cnt = aiThisCnt.incrementAndGet();
            String s = OAString.getRandomString(1, 20)+"."+cnt;
            
            long ts = System.currentTimeMillis();
            site.setName(s);
            long ts1 = System.currentTimeMillis();
            aiTimeMs.addAndGet(ts1-ts);

            serverNew.setSyncCnt(cnt);

            Hub<Server> hubServer = silo.getServers();
            int x = hubServer.getSize();
            Server server = hubServer.getAt( (int) (x * Math.random()) );
            String s1 = server.getName();
            server.setName(s);
//System.out.println(cnt+") server.id="+server.getId()+" old="+s1+", new="+s);            
            
            Hub<Application> hubApplication = server.getApplications();
            x = hubApplication.getSize();
            double d;
            if (x == 0) d = 1.0;
            else if (x > 100) d = .10;
            else if (x > 50) d = .25;
            else if (x > 25) d = .40;
            else d = .5;
           
            if (Math.random() < d) {
                Application app = new Application();
                if (Math.random() < .5) app.setServer(server);
                else hubApplication.add(app);
                aiCntNewApp.incrementAndGet();
            }
            else {
                if (x > 20 && Math.random() < .15) {
                    hubApplication.deleteAll();
                    aiCntDeleteAllApp.incrementAndGet();
                }
                else {
                    hubApplication.getAt(0).delete();
                    aiCntDeleteApp.incrementAndGet();
                }
            }
        }
    }

    public void _testMain2(int secondsToRun) throws Exception {
        Site site = ModelDelegate.getSites().getAt(0);
        Environment env = site.getEnvironments().getAt(0);
        Silo silo = env.getSilos().getAt(0);

        String s = OAString.getRandomString(5, 6);
        
        int cnt = 0;
        for (Server server : silo.getServers()) {
            server.setName(s+"."+(cnt++));
        }
        
    }
    

    public void sendStats() {
        Site site = ModelDelegate.getSites().getAt(0);
        remoteBroadcast.respondStats(site, site.getName());
        
        OAFinder<Site, Server> f = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp) {
            @Override
            protected void onFound(Server server) {
                remoteBroadcast.respondStats(server, server.getName(), server.getApplications().getSize(), server.getNameChecksum());
                int xx = 4;
                xx++;
            }
        };
        f.find(ModelDelegate.getSites());

        String s = String.format("cnt=%d, propChangeAvg=%,.2f, newApp=%d, deleteApp=%d, deleteAllApp=%d, cntAvg=%,.2f", 
            aiCnt.get(), 
            ((double)aiTimeMs.get()/aiCnt.get()),
            aiCntNewApp.get(), aiCntDeleteApp.get(), aiCntDeleteAllApp.get(),
            ((double)(testSeconds*maxThreads*1000)) / aiCnt.get()
        );
        remoteBroadcast.respondStats(s);
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
        syncClient = new OASyncClient("localhost", port) {
            @Override
            public void onStopCalled(String title, String msg) {
                bStopCalled = true;
                super.onStopCalled(title, msg);
            }
            
        };
        
        
        // **NOTE** need to make sure that ServerTest is running in another jvm
        try {
            syncClient.start();

            remoteApp = (RemoteAppInterface) syncClient.lookup(RemoteAppInterface.BindName);
            remoteBroadcastHold = new RemoteBroadcastInterface() {

                @Override
                public void startTest() {
                    System.out.println("received startTest message");
                    cdlStart.countDown();
                }
                @Override
                public void stopTest() {
                    System.out.println("received stopTest message");
                    bStopCalled = true;
                }
                @Override
                public void sendStats() {
                    System.out.println("received sendStats message");
                    cdlSendStats.countDown();
                }
                
                @Override
                public void onClientTestStarted() {
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    System.out.println("received onClientTestStarted"+s);
                    aiOnClientTestStart.incrementAndGet();
                }
                @Override
                public void onClientTestDone() {
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    System.out.println("received onClientTestDone"+s);
                    aiOnClientTestDone.incrementAndGet();
                }
                @Override
                public void onClientStatsSent() {
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    System.out.println("received onClientStatsSent"+s);
                    aiOnClientSentStats.incrementAndGet();
                }
                
                @Override
                public void respondStats(Site site, String name) {
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    if (!OAString.isEqual(site.getName(), name)) {
                        queErrors.offer(site.getId()+" has site.name="+site.getName()+", broadcast name="+name+s);
                        s += " ERROR, this.site.name="+site.getName();
                    }
                    //System.out.println("site.sendName called, name="+name+s);
                }
                @Override
                public void respondStats(Server server, String name, int cntApps, long nameChecksum) {
                    aiSendStats.incrementAndGet();
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    if (!OAString.isEqual(server.getName(), name) || server.getApplications().getSize() != cntApps || server.getNameChecksum() != nameChecksum) {
                        queErrors.offer(
                                "Error: serverId="+server.getId()+", this.server.name="+server.getName()+
                                ", this.server.Apps.size="+server.getApplications().getSize()+
                                ", checksum="+server.getNameChecksum()+
                                ", Broadcast server.name="+name+", broadcast cntApps="+cntApps+", checksum="+nameChecksum+s
                        );
                        s += ", ERROR, this.server.name="+server.getName();
                        s += ", server.apps.size="+server.getApplications().getSize();
                    }
                    //System.out.println("site.sendName/apps.size called, name="+name+", cntApps="+cntApps+s);

                }
                @Override
                public void respondStats(String msg) {
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    System.out.println("received respondStats"+s+", stats: "+msg);
                }
                @Override
                public void onClientDone() {
                    RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                    String s = ri == null ? "" : ", connection="+ri.connectionId;
                    System.out.println("received onClientDone"+s);
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


    public void runLocalClientTest() throws Exception {
        setup();
        System.out.println("waiting for unit test OASyncClientTest call RemoteBroadcast.startTest()");
        cdlStart.await();
        
        remoteBroadcast.onClientTestStarted();
        System.out.println("running test");
        testMain();

        remoteBroadcast.onClientTestDone();
        System.out.println("test done, waiting for unit test OASyncClientTest call RemoteBroadcast.sendStats()");
        
        // wait for site.name = "done"
        Site site = serverRoot.getSites().getAt(0);
        for (;;) {
            if (site.getName().equals("done")) break;
            Thread.sleep(1000);
        }
        
        cdlSendStats.await();
        System.out.println("sending stats");
        sendStats();

        remoteBroadcast.onClientStatsSent();

        System.out.println("sending done");
        
        remoteBroadcast.onClientDone();

        tearDown();
    }
    
    
    public static void main(String[] args) throws Exception {
        MultiplexerClient.DEBUG = true;
        OALogUtil.consoleOnly(Level.CONFIG);

        OASyncClientTest test = new OASyncClientTest();

        /*
        test.setup();        
        test.testA();
        */
        
        test.runLocalClientTest();
        
        System.out.println("DONE running test, exiting program");
    }
}
