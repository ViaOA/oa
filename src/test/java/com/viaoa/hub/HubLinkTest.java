package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.TsacDataGenerator;
import com.theice.tsactest.delegate.ModelDelegate;
import com.theice.tsactest.model.oa.*;

public class HubLinkTest extends OAUnitTest {

    @Test
    public void linkTest() {
        reset();
        TsacDataGenerator data = new TsacDataGenerator();
        data.createSampleData1();

        Hub<ServerType> hubServerType = ModelDelegate.getServerTypes();
        Hub<ServerStatus> hubServerStatus = ModelDelegate.getServerStatuses();
        
        Hub<Site> hubSite = ModelDelegate.getSites();
        Hub<Environment> hubEnvironment = hubSite.getDetailHub(Site.P_Environments);
        Hub<Silo> hubSilo = hubEnvironment.getDetailHub(Environment.P_Silos);
        Hub<Server> hubServer = hubSilo.getDetailHub(Silo.P_Servers);
        
        hubServerType.setLinkHub(hubServer, Server.P_ServerType);
        hubServerStatus.setLinkHub(hubServer, Server.P_ServerStatus);
        
        hubSite.setPos(0);
        hubEnvironment.setPos(0);
        hubSilo.setPos(0);
        hubServer.setPos(0);
        
        Server server = hubServer.getAO();
        assertNotNull(server);
        
        assertNull(hubServerStatus.getAO());
        
        int cntServerStatus = hubServerStatus.getSize();
        server.setServerStatus(hubServerStatus.getAt(1));
        assertEquals(hubServerStatus.getPos(), 1);
        
        server.setServerStatus(hubServerStatus.getAt(2));
        assertEquals(hubServerStatus.getPos(), 2);
        
        server.setServerStatus(null);
        assertEquals(hubServerStatus.getPos(), -1);
        
        ServerStatus st = new ServerStatus();
        server.setServerStatus(st);  // this will add st to the hubServerStatus
        
        assertNotNull(hubServerStatus.getAO());
        assertEquals(hubServerStatus.getAO(), st);
        assertEquals(hubServerStatus.getSize(), cntServerStatus+1);
        
        hubServer.setPos(1);
        assertNull(hubServerStatus.getAO());
        assertEquals(hubServerStatus.getSize(), cntServerStatus+1);
        
        hubServer.setPos(0);
        assertEquals(hubServerStatus.getAO(), server.getServerStatus());
        
        // change site AO, which will set server AO to null
        hubSite.setPos(1);
        assertNull(hubServer.getAO());
        assertNull(hubServerStatus.getAO());
        
        hubServer.setAO(server);
        assertEquals(hubServer.getAO(), server);
        assertEquals(hubServerStatus.getAO(), server.getServerStatus());
        
        reset();
    }
    
// link on pos
// link from one prop to another
// link that creates    
    // only if one does not exist
}



