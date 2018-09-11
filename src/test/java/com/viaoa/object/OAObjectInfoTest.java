package com.viaoa.object;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import test.hifive.model.oa.Employee;

public class OAObjectInfoTest extends OAUnitTest {

    @Test
    public void test() {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(Employee.class);
        
        OAMethodInfo mi = oi.getMethodInfo("command");
        
        assertNotNull(mi);
        
        assertNotNull(mi.getEditQueryMethod());
        
        assertEquals("birthDate", mi.getVisibleProperty());
        assertEquals(true, mi.getVisibleValue());
        
        assertNotNull(oi.getEnabledProperty());
        assertFalse(oi.getEnabledValue());
        assertNotNull(oi.getEditQueryMethod());
        
    }
    
}
