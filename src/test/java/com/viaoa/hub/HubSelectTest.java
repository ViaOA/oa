package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.ds.OADataSource;

import test.theice.tsac3.model.oa.*;

public class HubSelectTest extends OAUnitTest {
    @Test
    public void selectTest() {
        reset();
        Hub<Server> hubServer = new Hub<Server>(Server.class); 
        assertNull(hubServer.getSelect());
        
        hubServer.select();
        hubServer.loadAllData();
        
        assertNull(hubServer.getSelect());
        
        
        //assertEquals(0, hubServer.getSize());

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
