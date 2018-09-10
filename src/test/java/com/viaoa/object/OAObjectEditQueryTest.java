package com.viaoa.object;

import static org.junit.Assert.*;

import org.junit.Test;


import com.viaoa.OAUnitTest;
import com.viaoa.util.OADate;

import test.vetjobs.oa.Employer;


public class OAObjectEditQueryTest extends OAUnitTest {

    
    @Test
    public void test() throws Exception {
        Employer emp = new Employer();
        boolean b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertTrue(b);
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "company");
        assertTrue(b);
        
        
        emp.setEndDate(new OADate());
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, null);
        assertFalse(b);
        
        b = OAObjectEditQueryDelegate.getAllowEnabled(emp, "company");
        assertFalse(b);
    }
    
}
