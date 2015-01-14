package com.viaoa.hub;


import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.theice.tsac.model.oa.*;

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
    
}
