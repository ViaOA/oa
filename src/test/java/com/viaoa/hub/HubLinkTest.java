package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.TsacDataGenerator;
import com.theice.tsactest.model.Model;
import com.theice.tsactest.model.oa.*;
import com.theice.tsactest.model.oa.propertypath.SiloPP;

public class HubLinkTest extends OAUnitTest {

    @Test
    public void linkTest() {
        reset();
        TsacDataGenerator data = new TsacDataGenerator(model);
        data.createSampleData1();

        Hub<ServerType> hubServerType = model.getServerTypes();
        Hub<ServerStatus> hubServerStatus = model.getServerStatuses();
        
        Hub<Site> hubSite = model.getSites();
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
    
    @Test
    public void autoCreateLinkTest() {
        reset();
        TsacDataGenerator data = new TsacDataGenerator(model);
        data.createSampleData1();
    
        
        Hub<Site> hubSite = model.getSites();
        Hub<Environment> hubEnvironment = hubSite.getDetailHub(Site.P_Environments);
        Hub<Silo> hubSilo = hubEnvironment.getDetailHub(Environment.P_Silos);
        Hub<Server> hubServer = hubSilo.getDetailHub(Silo.P_Servers);

        Hub<ServerType> hubServerType = model.getServerTypes();
        hubServerType.setLinkHub(hubServer, Server.P_ServerType, true, true);

        hubSite.setPos(0);
        hubEnvironment.setPos(0);
        hubSilo.setPos(0);
        
        int x = hubServer.getSize();
        
        ServerType st = hubServerType.getAt(5);
        hubServerType.setAO(st);
        assertEquals(hubServer.getSize(), x+1);
        hubServerType.setAO(null);
        assertEquals(hubServer.getSize(), x+1);

        hubServerType.setLinkHub(hubServer, Server.P_ServerType, true, false);
        st = hubServerType.getAt(5);
        hubServerType.setAO(st);
        assertEquals(hubServer.getSize(), x+1);
        hubServerType.setAO(null);
        assertEquals(hubServer.getAO().getServerType(), st);
        
        Silo silo = new Silo();
        hubEnvironment.getAO().getSilos().add(silo);
        hubSilo.setAO(silo);
        assertNull(hubServer.getAO());
        assertEquals(hubServer.getSize(), 0);
         
        hubServerType.setAO(st);
        assertNotNull(hubServer.getAO());
        assertEquals(hubServer.getSize(), 1);
        assertEquals(hubServer.getAO().getServerType(), st);
        
        reset();
    }

    @Test
    public void autoCreateLinkTest2() {
        reset();
        TsacDataGenerator data = new TsacDataGenerator(model);
        data.createSampleData1();
    
        
        Hub<Site> hubSite = model.getSites();
        Hub<Environment> hubEnvironment = hubSite.getDetailHub(Site.P_Environments);
        Hub<Silo> hubSilo = hubEnvironment.getDetailHub(Environment.P_Silos);
        Hub<Server> hubServer = hubSilo.getDetailHub(Silo.P_Servers);
        assertNull(hubServer.getAO());
        assertEquals(hubServer.getSize(), 0);

        // ServerTypes for silo
        Hub<ServerType> hubServerType = new Hub<ServerType>(ServerType.class);
        new HubMerger(hubSilo, hubServerType, SiloPP.siloType().serverTypes().pp, false);
        hubServerType.setLinkHub(hubServer, Server.P_ServerType, true, true);

        Hub<ServerType> hubServerType2 = model.getServerTypes().createShared();
        hubServerType2.setLinkHub(hubServer, Server.P_ServerType);
        
        hubSite.setPos(0);
        hubEnvironment.setPos(0);
        hubSilo.setPos(0);
        
        int x = hubServer.getSize();
        assertEquals(hubSilo.getAO().getSiloType().getServerTypes().getSize(), hubServerType.getSize());
        assertNull(hubServer.getAO());
        assertNull(hubServerType.getAO());
        assertNull(hubServerType2.getAO());
        
        hubServerType.setAO(null);
        assertNull(hubServer.getAO());
        assertNull(hubServerType2.getAO());
        
        Server server = hubServer.setPos(0);
        ServerType st = server.getServerType();
        assertNotNull(st);
        assertNull(hubServerType.getAO());
        assertEquals(hubServerType2.getAO(), st);

        // set Server.serverType
        st = model.getServerTypes().getAt(3);
        server.setServerType(st);
        assertNull(hubServerType.getAO());
        assertEquals(hubServerType2.getAO(), st);

        // change serverType2 AO
        st = hubServerType2.setPos(2);
        assertEquals(server.getServerType(), st);
        assertNull(hubServerType.getAO());
        
        
        // change serverType AO - create new server
        assertEquals(hubServer.getSize(), x);
        st = hubServerType.setPos(1);
        assertTrue(server != hubServer.getAO());
        server = hubServer.getAO();
        assertEquals(server.getServerType(), st);
        assertEquals(hubServer.getSize(), x+1);
        
        // change silo and try again
        hubSite.setPos(1);
        hubEnvironment.setPos(0);
        hubSilo.setPos(0);
        assertNull(hubServer.getAO());
        assertNull(hubServerType.getAO());
        assertNull(hubServerType2.getAO());
        
        x = hubServer.getSize();
        st = hubServerType.setPos(2);
        assertEquals(hubServer.getSize(), x+1);
        assertTrue(server != hubServer.getAO());
        server = hubServer.getAO();
        assertEquals(server.getServerType(), st);
        
        
        reset();
    }
}







