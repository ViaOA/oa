package com.viaoa.sync;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.theicetest.tsactest2.model.oa.*;
import com.theicetest.tsactest2.model.oa.cs.ServerRoot;
import com.theicetest.tsactest2.model.oa.propertypath.*;
import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObjectKey;

/**
 * **** IMPORTANT **** 
 *      to run as Junit test, ServerTest will need to be running in a separate JVM
 */
public class OASyncClientTest extends OAUnitTest {
    private static int port = 1099;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;
    
    
    //@Test
    public void test() {
        if (serverRoot == null) return;
/*        
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
*/        
  
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

            h2 = app.getInstallVersions();
            assertEquals(h1.size(), h2.size());
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
    
    //@Before
    public void setup() throws Exception {
        syncClient = new OASyncClient("localhost", port);
        
        // **NOTE** need to make sure that ServerTest is running in another jvm
        try {
            syncClient.start();
        
            serverRoot = (ServerRoot) syncClient.getRemoteServer().getObject(ServerRoot.class, new OAObjectKey(777));
        }
        catch (Exception e) {
            System.out.println("NOT running ClientTest, ServerTest is not running in a separate jvm");
        }
    }
    //@After
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
