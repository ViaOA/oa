package com.viaoa.sync;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.theice.tsam.model.oa.cs.ServerRoot;
import com.theice.tsam.model.oa.propertypath.*;
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


/**
 * **** IMPORTANT **** 
 *      to run as Junit test, OASyncServerTest will need to be running in a separate JVM
 * *******************
 */
public class OASyncClientTest extends OAUnitTest {
    private static int port = 1099;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;

    private RemoteAppInterface remoteApp;
    
    
    @Test
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

    @Test
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
    public void test() {
        if (serverRoot == null) return;
        
        for (Site site : serverRoot.getSites()) {
            int x = 0;
            for (Environment env : site.getEnvironments()) {
                x++;
                for (Silo silo : env.getSilos()) {
                    x++;
                    for (Server server : silo.getServers()) {
                        x++;
                        for (Application app : server.getApplications()) {
                            x++;
                        }
                    }
                }
            }
        }

        OAFinder<Site, Application> finder = new OAFinder<Site, Application>(SitePP.environments().silos().servers().applications().pp);
        for (Application app : finder.find(serverRoot.getSites())) {
            //System.out.println(app.getApplicationType().getName());
            int x = 0;
            x++;
        }
  
        System.out.println("Done reading on client");
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
            serverRoot = remoteApp.getServerRoot();
            
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
        OASyncClientTest test = new OASyncClientTest();
        test.setup();
        test.test();
        //Thread.sleep(1300);
        test.tearDown();
    }
}
