package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

public class OADateTest extends OAUnitTest {

    @Test
    public void test() {

        long d1 = (new OADate()).getTime();
        long d2 = ((new OADate()).addDays(1).getTime());  // next day
        
        long x = 24 * 60 * 60 * 1000;
        
        assertEquals(d1+x, d2);
        
    }
    
}
