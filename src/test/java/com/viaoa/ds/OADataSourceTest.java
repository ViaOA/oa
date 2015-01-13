package com.viaoa.ds;

import org.junit.Test;

import com.theice.tsac.model.oa.*;
import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.viaoa.util.OAFilter;

import static org.junit.Assert.*;

public class OADataSourceTest extends OAUnitTest {
    
    
    /**
     * Test to make sure that the correct datasource is returned.
     */
    @Test
    public void registerTest() {
        reset();
        OADataSource[] dss = OADataSource.getDataSources();
        assertTrue(dss==null || dss.length == 0);

        Server server = new Server();
        assertEquals(server.getId(), 0);
        
        
        getCacheDataSource();
        getAutoDataSource();
        
        dss = OADataSource.getDataSources();
        assertEquals(dss.length, 2);

        OADataSource ds = OADataSource.getDataSource(Server.class);
        assertEquals(dsAuto, ds);
        
        
        OAFilter filter = new OAFilter() {
            public boolean isUsed(Object obj) {
                return false;
            }
        };
        
        ds = OADataSource.getDataSource(Server.class, filter);
        assertEquals(dsAuto, ds);  // no selectAll hub for dsCache to use

        Hub<Server> hub = new Hub<Server>(Server.class);
        hub.select();  // select all hub

        ds = OADataSource.getDataSource(Server.class);
        assertEquals(dsAuto, ds); // cache needs to have a filter
        
        ds = OADataSource.getDataSource(Server.class, filter);  // now, it has a selectAll hub and filter
        assertEquals(dsCache, ds);
        
        
        // clean up
        reset();
    }
    
    @Test
    public void Test() {
        
    }
    
    
    
}
