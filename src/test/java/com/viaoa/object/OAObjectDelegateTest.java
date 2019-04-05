package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac2.model.oa.*;

public class OAObjectDelegateTest extends OAUnitTest {

//    @Test
    public void getAutoAddTest() {
        assertFalse(OAObjectDelegate.getAutoAdd(null));
        
        Server server = new Server();
        assertTrue(OAObjectDelegate.getAutoAdd(server));
        
        server.setAutoAdd(false);
        assertFalse(OAObjectDelegate.getAutoAdd(server));
        
        server.setAutoAdd(true);
        assertTrue(OAObjectDelegate.getAutoAdd(server));
    }
    
    @Test
    public void getAutoAddTest2() {
        Silo silo = new Silo();
        assertTrue(OAObjectDelegate.getAutoAdd(silo));
        assertTrue(silo.getAutoAdd());
        
        Server server = new Server();
        assertTrue(OAObjectDelegate.getAutoAdd(server));
        assertTrue(server.getAutoAdd());
        
        server.setAutoAdd(false);
        assertFalse(server.getAutoAdd());

        server.setSilo(silo);
        assertFalse(server.getAutoAdd());

        assertEquals(server.getSilo(), silo);
        assertEquals(0, silo.getServers().size());
        
        server.setAutoAdd(true);
        assertEquals(server.getSilo(), silo);
        assertEquals(1, silo.getServers().size());
        assertEquals(silo.getServers().getAt(0), server);
        assertTrue(server.getAutoAdd());
        
        server = new Server();
        server.setAutoAdd(false);
        assertFalse(server.getAutoAdd());
        
        silo.getServers().add(server);
        assertFalse(server.getAutoAdd());
        
        assertEquals(server.getSilo(), silo);
        assertEquals(2, silo.getServers().size());
        
        Application app = new Application();
        server.getApplications().add(app);
        assertFalse(app.getAutoAdd());
        
        server.save();
        assertTrue(server.getAutoAdd());
        assertTrue(app.getAutoAdd());
        
        app = new Application();
        app.setServer(server);
        assertTrue(app.getAutoAdd());
        assertEquals(2, server.getApplications().getSize());
        
        app = new Application();
        app.setAutoAdd(false);
        app.setServer(server);
        assertFalse(app.getAutoAdd());
        assertEquals(2, server.getApplications().getSize());

        app.save();
        app.setAutoAdd(true);
        assertEquals(3, server.getApplications().getSize());
    }
}
