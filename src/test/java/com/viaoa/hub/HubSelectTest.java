package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class HubSelectTest extends OAUnitTest {
    @Test
    public void selectTest() {
        reset();
        Hub<Server> hubServer = new Hub<Server>(Server.class); 
        assertNull(hubServer.getSelect());
        
        hubServer.select();
        assertNotNull(hubServer.getSelect());

        hubServer.cancelSelect();
        assertNull(hubServer.getSelect());
/**qqqqq
        hubServer.refreshSelect();
        assertNotNull(hubServer.getSelect());
        
        HubSelectDelegate.cancelSelect(hubServer, true);
        assertNull(hubServer.getSelect());
        
        hubServer.refreshSelect();
        assertNull(hubServer.getSelect());
*/        
    }
}
