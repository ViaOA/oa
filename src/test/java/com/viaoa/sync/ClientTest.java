package com.viaoa.sync;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;
import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObjectKey;
import com.theice.tsactest2.model.oa.*;
import com.theice.tsactest2.model.oa.cs.ServerRoot;
import com.theice.tsactest2.model.oa.propertypath.*;

/**
 * **** IMPORTANT **** 
 *      to run as Junit test, ServerTest will need to be running in a separate JVM
 */
public class ClientTest extends OAUnitTest {
    private static int port = 1099;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;
    
    @Test
    public void clientTest() {
    }
    
    @Test
    public void objectLinkMatchTest() {
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
    
    @BeforeClass
    public static void start() throws Exception {
        ClientTest control = new ClientTest();
        syncClient = new OASyncClient("localhost", port);
        
        // **NOTE** need to make sure that ServerTest is running in another jvm
        syncClient.start();
        
        serverRoot = (ServerRoot) syncClient.getRemoteServer().getObject(ServerRoot.class, new OAObjectKey(777));
    }

    public static void main(String[] args) throws Exception {
        ClientTest control = new ClientTest();
        control.start();
    }
}
