package com.viaoa.object;

import java.util.ArrayList;

import org.junit.Test;

import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.hub.Hub;
import com.tmgsc.hifivetest.model.oa.*;

public class OAObjectDeleteDelegateTest extends OAUnitTest {

    @Test
    public void test() {
        getCacheDataSource();
        ImageStore is = new ImageStore();
        assertEquals(is.getId(), 1); // auto assigned
        
        // 1toM private 
        Program program = new Program();
        Hub h = program.getLogoImageStores();
        h.add(is);
        
        Object objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_LogoProgram);
        assertEquals(objx, program);

        // 1to1 private
        Location loc = new Location();
        loc.setCeoImageStore(is);

        is.save();
        // has to call ds to get it
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_CeoLocation);
        assertEquals(objx, loc);
        
        is.delete();
        // make sure that ds will remove references, and link table records        
        
        assertEquals(0, program.getLogoImageStores().getSize());
        assertNull(loc.getCeoImageStore());
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_LogoProgram);
        assertNull(objx);
        objx = OAObjectReflectDelegate.getReferenceObject(is, is.P_CeoLocation);
        assertNull(objx);
        
        // Mto1 private
//qqqqqqqqqqqqqqqqqqqqqq        
        

        // MtoM private
        // AwardTypes <-> Products
        
        Product product = new Product();
        ArrayList al = new ArrayList();
        for (int i=0; i<10; i++) {
            AwardType at = new AwardType();
            al.add(at);
            at.getPackageProducts().add(product);
        }
        product.delete();
        
        
        reset();
    }
    
}










