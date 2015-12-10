package com.viaoa.ds.jdbc;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.theice.tsactest.model.oa.*;
import com.tmgsc.hifivetest.DataSource;

public class QueryConverterTest extends OAUnitTest {

    @Test
    public void Test() throws Exception {
        init();
        // hi5 datasource
        DataSource ds = new DataSource();
        ds.open();
        OADataSourceJDBC oads = ds.getOADataSource();

        
        
        oads.close();
        reset();
    }
    
}
