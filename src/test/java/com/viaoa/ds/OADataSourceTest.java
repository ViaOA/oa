package com.viaoa.ds;

import org.junit.Test;
import static org.junit.Assert.*;
import com.theice.tsactest.model.oa.*;
import com.tmgsc.hifivetest.DataSource;
import com.viaoa.OAUnitTest;
import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.hub.Hub;
import com.viaoa.util.OAFilter;

public class OADataSourceTest extends OAUnitTest {
    
    
    /**
     * Test to make sure that the correct datasource is returned.
     */
    @Test
    public void registerTest() {
        init();
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
    public void Test() throws Exception {
        init();
        // hi5 datasource
        DataSource ds = new DataSource();
        ds.open();
        OADataSourceJDBC oads = ds.getOADataSource();
        
        ds.close();
        reset();
    }
    
}
