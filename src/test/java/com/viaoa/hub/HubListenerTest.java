package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import com.theice.tsactest.model.oa.Server;
import com.viaoa.OAUnitTest;

public class HubListenerTest extends OAUnitTest {

    private int cntAdd;
    private int cntRemove;
    private int cntChange;
    private int cntNewList;
    private int cntChangeActiveObject;
    @Test
    public void listenerTest() {
        reset();
        cntAdd = cntRemove = cntChange = cntNewList = cntChangeActiveObject = 0;
        
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                cntAdd++;
            }
            @Override
            public void afterRemove(HubEvent e) {
                cntRemove++;
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                cntChange++;
            }
            @Override
            public void onNewList(HubEvent e) {
                cntNewList++;
            }
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                cntChangeActiveObject++;
            }
        };
        Hub<Server> hub = new Hub<Server>(Server.class);
        hub.addHubListener(hl);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        
        Server server = new Server();
        hub.add(server);
        assertEquals(cntAdd, 1);

        server.setHostName("x.z");
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 1);
        assertEquals(cntRemove, 0);
        
        hub.add(server); // should not re-add
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 1);
        assertEquals(cntRemove, 0);
        
        hub.remove(0);
        assertEquals(hub.getSize(), 0);
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 1);
        assertEquals(cntRemove, 1);
        
        hub.add(server);
        assertEquals(hub.getSize(), 1);
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 2);
        assertEquals(cntRemove, 1);
        
        hub.clear();  
        assertEquals(hub.getSize(), 0); 
        assertEquals(cntNewList, 1);
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 2);
        assertEquals(cntRemove, 1); // does not send remove events, only a newListEvent  

        // AO test
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(null);
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(-1);
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(0);  // no objects
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(99); 
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(server);
        assertEquals(cntChangeActiveObject, 0);
        hub.setPos(-1);
        assertEquals(cntChangeActiveObject, 0);
        
        
        hub.add(server);
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(server);
        assertEquals(cntChangeActiveObject, 1);
        hub.setAO(99); 
        assertEquals(cntChangeActiveObject, 2);
        assertNull(hub.getAO());
        hub.remove(0);
        assertEquals(cntChangeActiveObject, 2);
        hub.add(server);
        assertEquals(cntChangeActiveObject, 2);
        hub.setPos(0);
        assertEquals(cntChangeActiveObject, 3);
        
        hub.removeAll();
        assertEquals(cntChangeActiveObject, 4);
        assertNull(hub.getAO());
        
        int cAdd = cntAdd;
        for (int i=0; i<10; i++) {
            server = new Server();
            hub.add(server);
            assertEquals(cntAdd, ++cAdd);
        }
        
        int cAO = cntChangeActiveObject;
        for (int i=0; i<100; i++) {
            int pos = (int) (Math.random() * 100);
            if (pos < hub.getSize()) {
                if (hub.getAO() != hub.getAt(pos)) cAO++;
            }
            else {
                if (hub.getAO() != null) cAO++;
            }
            
            hub.setPos(pos);
            assertEquals(cntChangeActiveObject, cAO);
        }
        
        int cChange = cntChange;
        for (int i=0; i<100; i++) {
            int pos = (int) (Math.random() * hub.getSize());
            server = hub.getAt(pos);
            String s = "c"+pos;
            if (!s.equals(server.getHostName())) cChange++;
            server.setHostName(s);
            assertEquals(cntChange, cChange);
        }
        
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        reset();
    }
    
}
