package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

public class HubTempTest extends OAUnitTest {

    @Test
    public void test() {
        Server server = new Server();
        Hub h = HubTemp.createHub(server);
        assertEquals(server, h.getAO());
        assertEquals(1, h.getSize());
        assertEquals(1, HubTemp.getCount(server));
        assertEquals(h, HubTemp.createHub(server));
        assertEquals(2, HubTemp.getCount(server));
        assertEquals(h, HubTemp.createHub(server));
        assertEquals(3, HubTemp.getCount(server));
        assertEquals(h, HubTemp.createHub(server));
        assertEquals(4, HubTemp.getCount(server));

        assertEquals(1, h.getSize());
        assertEquals(server, h.getAO());
        
        assertNull(HubTemp.createHub(null));
        
        assertEquals(1, HubTemp.getCount());
        HubTemp.deleteHub(new Hub());
        assertEquals(1, HubTemp.getCount());
        assertEquals(4, HubTemp.getCount(server));
        HubTemp.deleteHub(server);
        assertEquals(1, HubTemp.getCount());
        assertEquals(3, HubTemp.getCount(server));
        HubTemp.deleteHub(server);
        HubTemp.deleteHub(server);
        HubTemp.deleteHub(server);
        assertEquals(0, HubTemp.getCount());
        assertEquals(0, HubTemp.getCount(server));
        
    }
    
}
