package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.OADataSourceMock;
import com.viaoa.ds.OASelect;

import test.theice.tsam.model.oa.*;

public class HubSelectDelegateTest extends OAUnitTest {

    @Test
    public void fetchMoreTest() {
        OADataSourceMock ds = new OADataSourceMock(50);
        
        OADataSource dsx = OADataSource.getDataSource(Server.class, null);
        assertEquals(ds, dsx);        
        
        Hub<Server> hub = new Hub<Server>(Server.class);
        hub.select();
        
        assertNotNull(hub.data.hubDatax.select);
        
        hub.loadAllData();
        
        assertNull(hub.data.hubDatax);
        
        
        hub = new Hub<Server>(Server.class);
        OASelect<Server> sel = new OASelect<Server>(Server.class);
        assertFalse(sel.hasBeenStarted());
        
        HubSelectDelegate.fetchMore(hub, sel, 10);
        assertTrue(sel.hasBeenStarted());
        assertEquals(10, hub.getCurrentSize());
        assertEquals(10, hub.getSize());
        
        assertFalse(HubSelectDelegate.isMoreData(hub));
        assertTrue(sel.hasBeenStarted());
        assertTrue(sel.hasMore());
        
        HubSelectDelegate.loadAllData(hub, sel);
        assertFalse(sel.hasMore());
        
        hub = new Hub<Server>(Server.class);
        assertNull(HubSelectDelegate.getSelect(hub));
        assertNull(hub.data.hubDatax);
        hub.select();
        assertNotNull(HubSelectDelegate.getSelect(hub));
        assertNotNull(hub.data.hubDatax.select);
        hub.loadAllData();
        assertNull(hub.data.hubDatax);
        assertNull(HubSelectDelegate.getSelect(hub));
        assertNull(hub.getSelect());
        
        
        hub = new Hub<Server>(Server.class);
        sel = HubSelectDelegate.getSelect(hub, false);
        assertNull(sel);

        sel = HubSelectDelegate.getSelect(hub, true);
        assertNotNull(sel);
        
        ds.close();
    }
    
    @Test
    public void selectTest() {
        OADataSourceMock ds = new OADataSourceMock(50);
        Hub<Server> hub = new Hub<Server>(Server.class);
        OASelect<Server> sel = null;

        assertNull(hub.data.hubDatax);
        HubSelectDelegate.select(hub, sel);
        
        assertNull(hub.data.hubDatax);
        assertEquals(0, hub.getCurrentSize());

        sel = new OASelect<Server>(Server.class);
        HubSelectDelegate.select(hub, sel);

        if (OADataSource.getDataSource(Server.class) != null) {
            assertEquals(45, hub.getCurrentSize());
        }
        ds.close();
    }
    
    @Test
    public void cancelSelectTest() {
        OADataSourceMock ds = new OADataSourceMock(50);
        Hub<Server> hub = new Hub<Server>(Server.class);
        assertNull(hub.data.hubDatax);
        OASelect<Server> sel = new OASelect<Server>(Server.class);

        hub.select(sel);
        if (OADataSource.getDataSource(Server.class) != null) {
            assertNotNull(hub.data.hubDatax.select);
            assertEquals(45, hub.getCurrentSize());
        }
        else {
            assertNull(hub.data.hubDatax.select);
            assertEquals(0, hub.getCurrentSize());
        }

        HubSelectDelegate.cancelSelect(hub, false);
        assertNotNull(hub.data.hubDatax.select);
        assertEquals(45, hub.getCurrentSize());

        HubSelectDelegate.cancelSelect(hub, true);
        assertNull(hub.data.hubDatax);
        assertEquals(45, hub.getCurrentSize());
        
        ds.close();
    }

    @Test
    public void refreshSelectTest() {
        OADataSourceMock ds = new OADataSourceMock(50);
        Hub<Server> hub = new Hub<Server>(Server.class);

        OASelect<Server> sel = new OASelect<Server>(Server.class);

        HubSelectDelegate.select(hub, sel);
        assertEquals(45, hub.getCurrentSize());

        HubSelectDelegate.refreshSelect(hub);
        assertEquals(50, hub.getCurrentSize());
        
        assertNotNull(hub.data.hubDatax.select);
        
        ds.close();
    }
    
    @Test
    public void select2Test() {
        OADataSourceMock ds = new OADataSourceMock(50);
        Hub<Server> hub = new Hub<Server>(Server.class);

        HubSelectDelegate.select(hub, null, "whereClause", null, "orderByClause", true);
        
        OASelect<Server> sel = HubSelectDelegate.getSelect(hub);
        assertNotNull(sel);
        assertEquals("whereClause", sel.getWhere());
        assertEquals("orderByClause", sel.getOrder());
        assertNull(sel.getParams());
        assertTrue(sel.getAppend());
        
        assertEquals(45, hub.getCurrentSize());
        assertNotNull(hub.data.hubDatax.select);

        HubSelectDelegate.loadAllData(hub);
        assertEquals(50, hub.getCurrentSize());
        
        assertNull(hub.data.hubDatax);
        
        ds.close();
    }

}




