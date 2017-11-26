package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class OAObjectDelegateTest extends OAUnitTest {

    @Test
    public void getAutoAddTest() {
        assertFalse(OAObjectDelegate.getAutoAdd(null));
        
        Server server = new Server();
        assertTrue(OAObjectDelegate.getAutoAdd(server));
        
        server.setAutoAdd(false);
        assertFalse(OAObjectDelegate.getAutoAdd(server));
        
        server.setAutoAdd(true);
        assertTrue(OAObjectDelegate.getAutoAdd(server));
        
    }
    
    
    
}
