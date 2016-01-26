package com.viaoa.ds;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.ds.jdbc.OADataSourceJDBC;

import test.theice.tsac3.model.oa.*;
import test.theice.tsam.datasource.DataSource;

public class OADataSourceDelegateTest extends OAUnitTest {

    @Test
    public void getJDBCDataSourceTest() throws Exception {
        assertNull(OADataSourceDelegate.getJDBCDataSource());
        
        DataSource ds = new DataSource();
        ds.open();
        
        assertNotNull(OADataSourceDelegate.getJDBCDataSource());
        
        assertEquals(ds.getOADataSource(), OADataSourceDelegate.getJDBCDataSource());
        
        ds.close();
        assertNull(OADataSourceDelegate.getJDBCDataSource());
    }
    
    
    
}
