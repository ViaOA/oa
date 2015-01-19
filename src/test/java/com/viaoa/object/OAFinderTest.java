package com.viaoa.object;

import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.TsacDataGenerator;
import com.viaoa.hub.Hub;
import com.theice.tsactest.delegate.ModelDelegate;
import com.theice.tsactest.model.oa.*;
import com.theice.tsactest.model.oa.propertypath.*;

public class OAFinderTest extends OAUnitTest {
    
    @Test
    public void finderTest() {
        reset();
        TsacDataGenerator data = new TsacDataGenerator();
        data.createSampleData();

        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);

        Hub<Site> hubSite = ModelDelegate.getSites();
        Server server = finder.findFirst(hubSite);
        assertEquals(server.getName(), "Server.0.0.0.0");
        assertEquals(server.getId(), 1);
        
        ArrayList<Server> alServer = finder.find(ModelDelegate.getSites());
        int cnt = 0;
        for (Server ser : alServer) {
            assertEquals(ser.getId(), ++cnt);
        }
        
        finder.clearFilters();
        finder.addEqualFilter(Server.P_Id, 5);
        alServer = finder.find(hubSite);
        assertEquals(alServer.size(), 1);
        server = alServer.get(0);
        assertEquals(server.getId(), 5);

        finder.clearFilters();
        finder.addLessFilter(Server.P_Id, 5);
        alServer = finder.find(hubSite);
        assertEquals(alServer.size(), 4);
        
        finder.clearFilters();
        finder.addLessOrEqualFilter(Server.P_Id, 5);
        alServer = finder.find(hubSite);
        assertEquals(alServer.size(), 5);

        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.id(), 1);
        server = finder.findFirst(hubSite);
        assertNotNull(server);
        assertEquals(server.getId(), 1);
        
        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.silo().environment().name(), "Environment.0.0");
        server = finder.findFirst(hubSite);
        assertNotNull(server);
        assertEquals(server.getId(), 1);

        finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);
        finder.addEqualFilter(ServerPP.silo().environment().name(), "Environment.0.0");
        server = finder.findFirst(hubSite);
        assertNotNull(server);
        assertEquals(server.getId(), 1);

        reset();
    }
    
    
    
}
