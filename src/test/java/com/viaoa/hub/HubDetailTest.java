package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.HifiveDataGenerator;
import com.viaoa.OAUnitTest;
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
        final Hub<Employee> hubEmployee = hubLocation.getDetailHub(Location.P_Employees);
        final Hub<EmployeeAward> hubEmployeeAward = hubEmployee.getDetailHub(Employee.P_EmployeeAwards);
        
        
        
        
        reset();
    }
    
    
    
}
