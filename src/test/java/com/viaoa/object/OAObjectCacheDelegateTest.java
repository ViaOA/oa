package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;

import com.theice.tsactest.model.oa.*;
import com.theice.tsactest.model.oa.propertypath.*;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.autonumber.OADataSourceAuto;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;

public class OAObjectCacheDelegateTest extends OAUnitTest {
    
    @Test
    public void findTest() {
        reset();
        Silo silo = new Silo();

        SiloType siloType = new SiloType();

        ServerType serverType = new ServerType();
        serverType.setCode("1");
        
        Server server = new Server();
        
        server.setServerType(serverType);
        
        silo.getServers().add(server);
        
        Silo silox = (Silo) OAObjectCacheDelegate.find(Silo.class, SiloPP.servers().serverType().code(), "1");        
        assertTrue(silox != null);

        serverType.setCode("2");
        silox = (Silo) OAObjectCacheDelegate.find(Silo.class, SiloPP.servers().serverType().code(), "1");        
        assertTrue(silox == null);
        
        silox = (Silo) OAObjectCacheDelegate.find(Silo.class, SiloPP.servers().serverType().code(), "2");        
        assertTrue(silox != null);
    }

    @Test
    public void selectAllHubTest() {
        reset();
        getDataSource();
        
        Hub<Server> hub = new Hub<Server>(Server.class);
        Hub[] hubs = OAObjectCacheDelegate.getSelectAllHubs(Server.class);
        assertNull(hubs);
        
        hub.select();
        hubs = OAObjectCacheDelegate.getSelectAllHubs(Server.class);
        assertTrue(hubs != null && hubs.length == 1);
        Hub h = hubs[0];
        
        OAObjectCacheDelegate.removeSelectAllHub(h);
        hubs = OAObjectCacheDelegate.getSelectAllHubs(Server.class);
        assertNull(hubs);
        
        reset();
    }

    private int cnt1;
    private int cnt2;
    @Test
    public void listenerTest() {
        reset();
        cnt1 = 0;
        cnt2 = 0;
        OAObjectCacheListener hl = new OAObjectCacheListener() {
            @Override
            public void afterPropertyChange(OAObject obj, String propertyName, Object oldValue, Object newValue) {
                cnt2++;
            }
            @Override
            public void afterAdd(OAObject obj) {
                cnt1++;
            }
        };
        OAObjectCacheDelegate.addListener(Server.class, hl);
        
        OAObjectCacheListener[] hls = OAObjectCacheDelegate.getListeners(Server.class);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        
        Hub<Server> hub = new Hub<Server>(Server.class);
        assertEquals(cnt1, 0);
        Server server = new Server();
        hub.add(server);
        assertEquals(cnt1, 1);

        cnt2 = 0;
        server.setHostName("x.z");
        assertEquals(cnt2, 1);
        
        
        OAObjectCacheDelegate.removeListener(Server.class, hl);
        hls = OAObjectCacheDelegate.getListeners(Server.class);
        assertNull(hls);

        server = new Server();
        hub.add(server);
        assertEquals(cnt1, 1);
    }
    
    @Test
    public void removeAllTest() {
        reset();
        Server server = new Server();
        server.setId(4);
        
        Object objx = OAObjectCacheDelegate.get(Server.class, 4);
        assertTrue(objx != null);
        
        OAObjectCacheDelegate.removeAllObjects();
        objx = OAObjectCacheDelegate.get(Server.class, 4);
        assertTrue(objx == null);
    }
    
    @Test
    public void callbackTest() {
        reset();
        cnt1 = 0;
        cnt2 = 0;
        Server server = new Server();
        server.setId(4);
        
        OACallback cb = new OACallback() {
            @Override
            public boolean updateObject(Object obj) {
                cnt1++;
                return true;
            }
        };
        
        OAObjectCacheDelegate.callback(cb);
        assertEquals(cnt1, 1);
    }
    
    @Test
    public void getObjectTest() {
        reset();
        Server server = new Server();

        Object objx = OAObjectCacheDelegate.getObject(Server.class, server);
        assertEquals(server, objx);
        
        server.setId(4);
        objx = OAObjectCacheDelegate.getObject(Server.class, 4);
        assertEquals(server, objx);

        OAObjectKey key = new OAObjectKey(4);
        objx = OAObjectCacheDelegate.getObject(Server.class, key);
        assertEquals(server, objx);
        
        server.setId(5);
        objx = OAObjectCacheDelegate.getObject(Server.class, 4);
        assertNull(objx);
        objx = OAObjectCacheDelegate.getObject(Server.class, 5);
        assertEquals(server, objx);

        key = new OAObjectKey(4);
        objx = OAObjectCacheDelegate.getObject(Server.class, key);
        assertNull(objx);
        
        key = new OAObjectKey(5);
        objx = OAObjectCacheDelegate.getObject(Server.class, key);
        assertEquals(server, objx);
    }

    @Test
    public void findNextTest() {
        reset();
        Silo silo = new Silo();

        SiloType siloType = new SiloType();

        ServerType serverType = new ServerType();
        serverType.setCode("1");
        
        Server server = new Server();
        server.setServerType(serverType);
        silo.getServers().add(server);
        
        Silo silox = (Silo) OAObjectCacheDelegate.findNext(null, Silo.class, SiloPP.servers().serverType().code(), "1");        
        assertTrue(silox != null);

        silox = (Silo) OAObjectCacheDelegate.findNext(silox, Silo.class, SiloPP.servers().serverType().code(), "1");        
        assertNull(silox);
    }

    @Test
    public void refreshTest() {
        reset();
        OAObjectCacheDelegate.clearCache(Server.class);
        OAObjectDelegate.setNextGuid(0);

        final Server server = new Server();
        
        dsAuto = new OADataSourceAuto() {
            @Override
            public Object getObject(OAObjectInfo oi, Class clazz, OAObjectKey key, boolean bDirty) {
                server.setHostName("worked");
                return server;
            }
        };
        
        OAObjectCacheDelegate.refresh(Server.class);
        assertEquals("worked", server.getHostName());
        
        dsAuto.close();
    }


    private boolean bStop;
    @Test
    public void concurrentTest() {
        reset();
        for (int i=0; i<10; i++) {
            Thread t = new TestThread(i+1);
            t.start();
        }
        for (int i=0; i<3; i++) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
        bStop = true;
    }
    class TestThread extends Thread {
        int id;
        public TestThread(int id) {
            this.id = id;
        }
        public void run() {
            int max = 1500;
            int rootId = id * max;
            for (int i=0; i<max && !bStop; i++) {
                test(rootId + i);
            }
        }
    }
    private void test(int id) {
        Server server = new Server();
        Server serverx = (Server) OAObjectCacheDelegate.get(server);
        assertEquals(server, serverx);
        
        server.setId(id);
        serverx = (Server) OAObjectCacheDelegate.get(server);
        assertEquals(server, serverx);
        
        serverx = (Server) OAObjectCacheDelegate.get(Server.class, id);
        assertEquals(server, serverx);
        delay(2);

        OAObjectCacheDelegate.removeObject(server);
        serverx = (Server) OAObjectCacheDelegate.get(server);
        assertNull(serverx);

        OAObjectCacheDelegate.add(server, true, false);
        serverx = (Server) OAObjectCacheDelegate.get(server);
        assertEquals(server, serverx);

        OAObjectCacheDelegate.removeObject(server);
        serverx = (Server) OAObjectCacheDelegate.get(server);
        assertNull(serverx);
    }

}

