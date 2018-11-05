package com.viaoa.util;

import static org.junit.Assert.*;
import org.junit.Test;

import com.viaoa.OAUnitTest;

public class OAConverterTest extends OAUnitTest {
    @Test
    public void roundTest() {
        
        double d = 1.2345678;
        double d2;
        
        double dx = OAConv.round(d, 2);
        assertTrue(1.23 == dx);
        
        d = 1.23;
        dx = OAConv.round(d, 2);
        assertTrue(1.23 == dx);
        
        d = 1.239;
        dx = OAConv.round(d, 2);
        assertTrue(1.24 == dx);
        
        d = 1.235;
        dx = OAConv.round(d, 2);
        assertTrue(1.24 == dx);

        d = 1.2351;
        dx = OAConv.round(d, 2);
        assertTrue(1.24 == dx);
        
        d = 1.2349999;
        dx = OAConv.round(d, 2);
        assertTrue(1.23 == dx);
        
        d = .9999;
        dx = OAConv.round(d, 2);
        assertTrue(1.0 == dx);

        d = 1.2345;
        d2 = 1.23456666;

        assertTrue(OAConv.compare(d, d2, 0) == 0);
        assertTrue(OAConv.compare(d, d2, 3) == 0);
        assertTrue(OAConv.compare(d, d2, 4) < 0);
        d2 = 1.23454666;
        assertTrue(OAConv.compare(d, d2, 4) == 0);

        
        d = 39.424;
        d = OAConv.round(d, 2);
        assertTrue(39.42 == d);
        
        d = 39.426;
        d = OAConv.round(d, 2);
        assertTrue(39.43 == d);
        
        d = 39.4251;
        d = OAConv.round(d, 2);
        assertTrue(39.43 == d);
        
        d = 39.425;
        d = OAConv.round(d, 2);
        assertTrue(39.43 == d);

        d = 48.5475;
        d = OAConv.round(d, 2);
        assertTrue(48.55 == d);
        
        d = 970.95 * .05;
        d = OAConv.round(d, 2);
        assertTrue(48.55 == d);
    }
    
    @Test
    public void addTest() {
        double d = 1.2345678;
        double d2, dx;
        
        assertEquals((int) OAConv.add(1, 5), 6);
        
        dx = OAConv.add(1.1, 5);
        assertTrue(dx == 6.1);
        assertFalse(dx == 6.100000001);

        dx = OAConv.add(1.1, 5, 0);
        assertTrue(dx == 6.0);
    
        dx = OAConv.add(1.1, 5, 1);
        assertTrue(dx == 6.1);

        dx = OAConv.add(1.1, 5, 2);
        assertTrue(dx == 6.1);

        dx = OAConv.add(1.100499, 5.005, 2);
        assertTrue(dx == 6.11);

        dx = OAConv.add(1.100499, 5.005, 3);
        assertTrue(dx == 6.105);
    }
    
    @Test
    public void subtractTest() {
        double d = 1.2345678;
        double d2, dx;
        
        dx = OAConv.subtract(5.0, 1.0, 3);
        assertTrue(dx == 4.0);
    
        dx = OAConv.subtract(5.999, 1.000, 2);
        assertTrue(dx == 5.0);
        
        dx = OAConv.subtract(5.499, 1.000, 1);
        assertTrue(dx == 4.5);
    }
}



