package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

public class OADateTest extends OAUnitTest {

    @Test
    public void test() {

        long d1 = (new OADate()).getTime();
        long d2 = ((new OADate()).addDays(1).getTime());  // next day
        
        long x = 24 * 60 * 60 * 1000;
        
        assertEquals(d1+x, d2);
        
    }

    @Test
    public void msUntilMidnight() {

        OADateTime dtNow = new OADateTime();
        OADate d = new OADate(dtNow.addDays(1));
        long ms = d.getTime() - dtNow.getTime();
        
        assertTrue(ms > 0);
        
    }
    
    
}
