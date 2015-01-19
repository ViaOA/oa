package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.theice.tsactest.model.oa.Server;
import com.viaoa.OAUnitTest;

public class OAObjectKeyTest extends OAUnitTest {

    @Test
    public void objectIdTest() {
        reset();
        Server server = new Server();
        OAObjectKey key = server.getObjectKey();
        
        server.setId(1);
        OAObjectKey key2 = server.getObjectKey();
        
        assertEquals(key.getGuid(), key2.getGuid());
        
        assertFalse(key.equals(key2));
        
        // clean up
        reset();
    }
    
    
    @Test
    public void newTest() {
        reset();
        OAObjectKey key = new OAObjectKey(null, 12, true);
        OAObjectKey key2 = new OAObjectKey(null, 12, true);
        assertEquals(key, key2);
        
        key2 = new OAObjectKey(null, 12, false);
        assertEquals(key, key2);

        key2 = new OAObjectKey(null, 13, false);
        assertTrue(!key.equals(key2));
        reset();
    }    

    @Test
    public void idTest() {
        reset();
        OAObjectKey key = new OAObjectKey(1);
        OAObjectKey key2 = new OAObjectKey(1);
        assertEquals(key, key2);

        key2 = new OAObjectKey(new Object[]{1}, 14, true);
        assertEquals(key, key2);
        reset();
    }
    
    @Test
    public void moreThenOneIdTest() {
        reset();
        OAObjectKey key = new OAObjectKey(1, 2, 3);
        
        Object[] objs = key.getObjectIds();
        assertTrue(objs.length == 3 && objs[0].equals(new Integer(1)) && objs[1].equals(new Integer(2)) && objs[2].equals(new Integer(3)));
        assertEquals(key.getGuid(), 0);
        
        OAObjectKey key2 = new OAObjectKey(1, 2, 3);
        assertTrue(key.equals(key2));
        assertEquals(key.getGuid(), 0);
        reset();
    }
    
}
