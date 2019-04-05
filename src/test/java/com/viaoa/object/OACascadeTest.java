package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

public class OACascadeTest extends OAUnitTest {

    @Test
    public void wasCascadedTest() {
        OACascade cascade = new OACascade();
        Server server = new Server();
        assertFalse(cascade.wasCascaded(server, false));
        assertFalse(cascade.wasCascaded(server, false));
        
        assertFalse(cascade.wasCascaded(server, true));
        assertTrue(cascade.wasCascaded(server, true));
    }
    
    
    @Test
    public void depthTest() {
        OACascade cascade = new OACascade();
        
        for (int i=0; i<20; i++) {
            assertEquals(i, cascade.getDepth());
            cascade.depthAdd();
        }
        cascade.setDepth(0);
        assertEquals(0, cascade.getDepth());
    }
    
    @Test
    public void addTest() {
        OACascade cascade = new OACascade();
        Server server = new Server();
        assertNull(cascade.getList());
        cascade.add(server);
        assertNotNull(cascade.getList());
        assertEquals(1, cascade.getList().size());
    }

    
    @Test
    public void clearListTest() {
        OACascade cascade = new OACascade();
        Server server = new Server();
        assertNull(cascade.getList());
        cascade.add(server);
        assertNotNull(cascade.getList());
        cascade.clearList();
        assertNull(cascade.getList());
    }

    @Test
    public void ignoreTest() {
        OACascade cascade = new OACascade();
        Server server = new Server();
        
        assertFalse(cascade.wasCascaded(server, true));
        assertTrue(cascade.wasCascaded(server, false));
        
        cascade = new OACascade();
        cascade.ignore(Server.class);
        
        assertTrue(cascade.wasCascaded(server, true));
        assertTrue(cascade.wasCascaded(server, false));
    }
    

}
