package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;

import test.theice.tsam.datasource.DataSource;
import test.theice.tsam.model.oa.*;

public class OAAnnotationVerifierTest extends OAUnitTest {

    @Test
    public void verifyTest() throws Exception {
        DataSource ds = new DataSource();
        ds.open();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(Server.class);
        
        OAAnnotationVerifier v = new OAAnnotationVerifier();
        v.verify(oi);
        
        v.verify(Server.class, ds.getDatabase());
        
        ds.close();
    }
    
    /* not used
    @Test
    public void verifyLinksTest() throws Exception {
        DataSource ds = new DataSource();
        ds.open();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(Server.class);
        
        OAAnnotationVerifier v = new OAAnnotationVerifier();
        
        v.verifyLinks(ds.getDatabase());
        
        ds.close();
    }
    */
    
    @Test
    public void compareTest() throws Exception {
        DataSource ds = new DataSource();
        ds.open();
        
        OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(Server.class);
        
        OAAnnotationVerifier v = new OAAnnotationVerifier();
        
        v.compare(ds.getDatabase(), ds.getDatabase());
        
        ds.close();
    }
    
}
