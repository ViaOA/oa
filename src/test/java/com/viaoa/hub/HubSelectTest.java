package com.viaoa.hub;


import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.theice.tsactest.model.oa.*;

public class HubSelectTest extends OAUnitTest {

    @Test
    public void selectTest() {
        reset();
        Hub<Server> hubServer = new Hub<Server>(Server.class); 
        assertNull(hubServer.getSelect());
        
        hubServer.select();
        assertNotNull(hubServer.getSelect());

        hubServer.cancelSelect();
        assertNotNull(hubServer.getSelect());  // select will stay so that it can be refreshed

        hubServer.refreshSelect();
        assertNotNull(hubServer.getSelect());
        
        HubSelectDelegate.cancelSelect(hubServer, true);
        assertNull(hubServer.getSelect());
        
        hubServer.refreshSelect();
        assertNull(hubServer.getSelect());
    }
    
}
