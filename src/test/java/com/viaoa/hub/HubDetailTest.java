package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.hifive.HifiveDataGenerator;
import test.hifive.HifiveUnitTest;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;

public class HubDetailTest extends HifiveUnitTest {

    @Test
    public void detailHubTest() {
        reset();
        Hub<Program> hubProgram = new Hub<Program>(Program.class); 

        Hub<Location> hubLocation = hubProgram.getDetailHub(Program.P_Locations);
        
        Program program;
        for (int i=0; i<10; i++) {
            program = new Program();
            hubProgram.add(program);
        }
        for (Program p : hubProgram) {
            hubProgram.setAO(p);
            assertEquals(p.getLocations(), hubProgram.getSharedHub());
        }
        
        reset();
    }
    
    @Test
    public void detailHub2Test() {
        reset();

        HifiveDataGenerator data = new HifiveDataGenerator();
        data.createSampleData();

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
        
        HifiveDataGenerator data = getDataGenerator();
        data.createSampleData();

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
        
        HifiveDataGenerator data = getDataGenerator();
        data.createSampleData();

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
