package com.viaoa.object;

import java.util.ArrayList;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.TsacDataGenerator;
import com.theice.tsac.delegate.ModelDelegate;
import com.theice.tsac.model.oa.*;
import com.theice.tsac.model.oa.propertypath.*;

public class OAFinderTest extends OAUnitTest {

/*qqqqqqqqq    
    OAFinder finder = new OAFinder<Environment, Server>(EnvironmentPP.silos().servers().pp);
    finder.addEqualFilter(ServerPP.hostName(), hostName);
    finder.addEqualFilter(ServerPP.serverType().packageTypes().packageName(), packageName);
    
    OAFinder finder2 = new OAFinder<Server, ServerInstall>(ServerPP.serverInstalls().pp);
    finder.setFinder(finder2);
*/
    
    @Test
    public void test() {
        reset();
        TsacDataGenerator data = new TsacDataGenerator();
        data.createSampleData();

        OAFinder<Site, Server> finder = new OAFinder<Site, Server>(SitePP.environments().silos().servers().pp);

        Server server = finder.findFirst(ModelDelegate.getSites());
        assertEquals(server.getName(), "Server.0.0.0.0");
        assertEquals(server.getId(), 1);
        
        ArrayList<Server> alServer = finder.find(ModelDelegate.getSites());
        int cnt = 0;
        for (Server ser : alServer) {
            assertEquals(ser.getId(), ++cnt);
        }
        
        finder.clearFilters();
        finder.addEqualFilter(Server.P_Id, 5);
        alServer = finder.find();
        assertEquals(alServer.size(), 1);
        server = alServer.get(0);
        assertEquals(server.getId(), 5);
        
        

        reset();
    }
    
    
    
}
