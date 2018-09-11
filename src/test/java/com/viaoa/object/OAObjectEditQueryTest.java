package com.viaoa.object;

import static org.junit.Assert.*;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.util.OADate;

import test.hifive.model.oa.Employee;



public class OAObjectEditQueryTest extends OAUnitTest {

    
    @Test
    public void test() throws Exception {
        Employee emp = new Employee();
        boolean b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertTrue(b);
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "lastName");
        assertTrue(b);
        
        emp.setInactiveDate(new OADate());
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertFalse(b);
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "lastName");
        assertFalse(b);

        try {
            emp.setLastName("test");
            fail("setLastName should fail");
        }
        catch (Exception e) {
        }
        
        
        try {
            emp.setInactiveDate(null);
        }
        catch (Exception e) {
            fail("setInactiveDate should not fail");
        }
        
        try {
            emp.setLastName("test");
        }
        catch (Exception e) {
            fail("setLastName should not fail");
        }
        
    
    }
    
}
