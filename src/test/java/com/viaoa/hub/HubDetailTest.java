package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.HifiveDataGenerator;
import com.viaoa.OAUnitTest;
import com.viaoa.TsacDataGenerator;
import com.theice.tsactest.model.oa.*;
import com.tmgsc.hifivetest.delegate.ModelDelegate;
import com.tmgsc.hifivetest.model.oa.*;

public class HubDetailTest extends OAUnitTest {

    @Test
    public void detailHubTest() {
        reset();
        Hub<Server> hubServer = new Hub<Server>(Server.class); 

        Hub<ServerInstall> hubServerInstall = hubServer.getDetailHub(Server.P_ServerInstalls);
        
        Server server;
        for (int i=0; i<10; i++) {
            server = new Server();
            hubServer.add(server);
        }
        for (Server s : hubServer) {
            hubServer.setAO(s);
            assertEquals(s.getServerInstalls(), hubServerInstall.getSharedHub());
        }
        
        reset();
    }
    
    @Test
    public void detailHub2Test() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        hubProgram.setPos(0);
        hubLocation.setPos(0);

        assertNotNull(hubLocation.getAO());
        
        hubProgram.setPos(1);
        
        assertNull(hubLocation.getAO());
        
        reset();
    }
    
    @Test
    public void detailHub3Test() {
        reset();
        
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        hubProgram.setPos(0);
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.setPos(0);
        assertNotNull(hubLocation.getAO());
        
        
        HubListener hl = new HubListenerAdapter<Location>() {
            @Override
            public void onNewList(HubEvent<Location> e) {
                assertNull(e.getHub().getAO());
            }
        };
        hubLocation.addHubListener(hl);        
        
        hubProgram.setPos(1);
        assertNull(hubLocation.getAO());
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.removeHubListener(hl);        

        reset();
    }
    

    @Test
    public void detailHub4Test() {
        reset();
        
        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData1();

        final Hub<Program> hubProgram = ModelDelegate.getPrograms();
        final Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        hubProgram.setPos(0);
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.setPos(0);
        assertNotNull(hubLocation.getAO());
        
        
        HubListener hl = new HubListenerAdapter<Location>() {
            @Override
            public void onNewList(HubEvent<Location> e) {
                assertNull(e.getHub().getAO());
            }
        };
        hubLocation.addHubListener(hl);        
        
        hubProgram.setPos(1);
        assertNull(hubLocation.getAO());
        assertEquals(hubLocation.getSharedHub(), hubProgram.getAO().getLocations());

        hubLocation.removeHubListener(hl);        

        reset();
    }
    
    
}
