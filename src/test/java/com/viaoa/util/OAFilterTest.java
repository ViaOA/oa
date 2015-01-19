package com.viaoa.util;


import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;
import com.viaoa.util.filter.*;
import com.theice.tsactest.model.oa.*;

public class OAFilterTest extends OAUnitTest {

    @Test
    public void emptyFilterTest() {
        OAEmptyFilter f = new OAEmptyFilter();
        
        assertTrue(f.isUsed(null));
        assertTrue(f.isUsed(""));
        assertTrue(f.isUsed(""));
    }
    
    @Test
    public void betweenFilterTest() {
        OABetweenFilter f = new OABetweenFilter(5, 8);
        
        assertTrue(f.isUsed(6));
        assertTrue(f.isUsed(7));
        assertFalse(f.isUsed(5));
        assertFalse(f.isUsed(8));
        assertFalse(f.isUsed(9));
        assertFalse(f.isUsed(0));
        assertFalse(f.isUsed(""));
        assertFalse(f.isUsed(-99));
    }

    @Test
    public void betweenOrEqualFilterTest() {
        OABetweenOrEqualFilter f = new OABetweenOrEqualFilter(5, 8);
        
        assertTrue(f.isUsed(6));
        assertTrue(f.isUsed(5));
        assertTrue(f.isUsed(8));
        assertFalse(f.isUsed(9));
        assertFalse(f.isUsed(0));
        assertFalse(f.isUsed(""));
        assertFalse(f.isUsed(-99));
    }
    
    @Test
    public void equalFilterTest() {
        OAEqualFilter f = new OAEqualFilter("a");
        
        assertTrue(f.isUsed("a"));
        assertFalse(f.isUsed("A"));
        
        assertFalse(f.isUsed(null));
        assertFalse(f.isUsed(-99));

        f = new OAEqualFilter("a", true);
        assertTrue(f.isUsed("a"));
        assertTrue(f.isUsed("A"));
        
        
        f = new OAEqualFilter(null);
        assertFalse(f.isUsed(123));
        assertFalse(f.isUsed(""));
        assertTrue(f.isUsed(null));

        f = new OAEqualFilter(5);
        assertTrue(f.isUsed(5));
        assertTrue(f.isUsed(5.0d));
        assertTrue(f.isUsed(5.0f));
        assertTrue(f.isUsed("5.0"));
        assertTrue(f.isUsed("5.00000001"));  // only uses Integer portion
        
        f = new OAEqualFilter(5.0);
        assertTrue(f.isUsed(5));
        assertTrue(f.isUsed(5.0d));
        assertTrue(f.isUsed(5.0f));
        assertTrue(f.isUsed("5.0"));
        assertFalse(f.isUsed("5.00000001"));
        
        f = new OAEqualFilter("5.0");
        assertFalse(f.isUsed(5));
        assertTrue(f.isUsed(5.0d));
        assertTrue(f.isUsed(5.00000d));
        assertFalse(f.isUsed(5.000001d));
        assertTrue(f.isUsed(5.0f));
        assertTrue(f.isUsed("5.0"));
        assertFalse(f.isUsed("5.00000001"));
        assertFalse(f.isUsed("5.00"));
    }

    @Test
    public void notEqualFilterTest() {
        OANotEqualFilter f = new OANotEqualFilter("a");
        
        assertFalse(f.isUsed("a"));
        assertTrue(f.isUsed("A"));
        
        assertTrue(f.isUsed(null));
        assertTrue(f.isUsed(-99));

        f = new OANotEqualFilter("a", true);
        assertFalse(f.isUsed("a"));
        assertFalse(f.isUsed("A"));
        
        
        f = new OANotEqualFilter(null);
        assertTrue(f.isUsed(123));
        assertTrue(f.isUsed(""));
        assertFalse(f.isUsed(null));

        f = new OANotEqualFilter(5);
        assertFalse(f.isUsed(5));
        assertFalse(f.isUsed(5.0d));
        assertFalse(f.isUsed(5.0f));
        assertFalse(f.isUsed("5.0"));
        assertFalse(f.isUsed("5.00000001"));  // only uses Integer portion
        
        f = new OANotEqualFilter(5.0);
        assertFalse(f.isUsed(5));
        assertFalse(f.isUsed(5.0d));
        assertFalse(f.isUsed(5.0f));
        assertFalse(f.isUsed("5.0"));
        assertTrue(f.isUsed("5.00000001"));
        
        f = new OANotEqualFilter("5.0");
        assertTrue(f.isUsed(5));
        assertFalse(f.isUsed(5.0d));
        assertFalse(f.isUsed(5.00000d));
        assertTrue(f.isUsed(5.000001d));
        assertFalse(f.isUsed(5.0f));
        assertFalse(f.isUsed("5.0"));
        assertTrue(f.isUsed("5.00000001"));
        assertTrue(f.isUsed("5.00"));
    }
    
    
    @Test
    public void greaterFilterTest() {
        OAGreaterFilter f = new OAGreaterFilter("b");
        assertTrue(f.isUsed("c"));
        assertFalse(f.isUsed("a"));
        assertFalse(f.isUsed("b"));

        f = new OAGreaterFilter(5);
        assertTrue(f.isUsed(6));
        assertFalse(f.isUsed(5));
        assertFalse(f.isUsed(4));
        assertFalse(f.isUsed(4.9999));
        assertFalse(f.isUsed("4.9999"));
        assertFalse(f.isUsed(5.0001));  // will convert to (int) 5
        assertFalse(f.isUsed("5.001"));  // will convert to (int) 5
        assertFalse(f.isUsed("5.0")); 
        assertFalse(f.isUsed(null));

        f = new OAGreaterFilter("5.001");
        assertTrue(f.isUsed("5.002"));
        assertFalse(f.isUsed("5.001"));
        assertTrue(f.isUsed("5.001000001"));
        assertTrue(f.isUsed("51.001000001"));
    }    

    @Test
    public void greaterFilterOrEqualTest() {
        OAGreaterOrEqualFilter f = new OAGreaterOrEqualFilter("b");
        assertTrue(f.isUsed("c"));
        assertFalse(f.isUsed("a"));
        assertTrue(f.isUsed("b"));

        f = new OAGreaterOrEqualFilter(5);
        assertTrue(f.isUsed(6));
        assertTrue(f.isUsed(5));
        assertFalse(f.isUsed(4));
        assertFalse(f.isUsed(4.9999));
        assertFalse(f.isUsed("4.9999"));
        assertTrue(f.isUsed(5.0001));
        assertTrue(f.isUsed("5.001"));
        assertTrue(f.isUsed("5.0"));  // the String compare will be true "5.0" > "5"
        assertFalse(f.isUsed(null));
        assertTrue(f.isUsed("5.0"));  // the String compare will be true "5.0" > "5"

        f = new OAGreaterOrEqualFilter("5.001");
        assertTrue(f.isUsed("5.002"));
        assertTrue(f.isUsed("5.001"));
        assertFalse(f.isUsed("5.00099"));
        assertTrue(f.isUsed("5.001000001"));
        assertTrue(f.isUsed("51.001000001"));
    }    

    @Test
    public void lessFilterTest() {
        OALessFilter f = new OALessFilter("b");
        assertFalse(f.isUsed("c"));
        assertTrue(f.isUsed("a"));
        assertFalse(f.isUsed("b"));

        f = new OALessFilter(5);
        assertFalse(f.isUsed(6));
        assertFalse(f.isUsed(5));
        assertTrue(f.isUsed(4));
        assertTrue(f.isUsed(4.9999));
        assertTrue(f.isUsed("4.9999"));
        assertFalse(f.isUsed(5.0001));
        assertFalse(f.isUsed("5.001"));
        assertFalse(f.isUsed("5.0"));  // the String compare will be true "5.0" > "5"
        assertTrue(f.isUsed(null));

        f = new OALessFilter("5.001");
        assertFalse(f.isUsed("5.002"));
        assertFalse(f.isUsed("5.001"));
        assertFalse(f.isUsed("5.001000001"));
        assertFalse(f.isUsed("51.001000001"));
        assertTrue(f.isUsed("5.000999"));
    }    

    @Test
    public void lessOrEqualFilterTest() {
        OALessOrEqualFilter f = new OALessOrEqualFilter("b");
        assertFalse(f.isUsed("c"));
        assertTrue(f.isUsed("a"));
        assertTrue(f.isUsed("b"));

        f = new OALessOrEqualFilter(5);
        assertFalse(f.isUsed(6));
        assertTrue(f.isUsed(5));
        assertTrue(f.isUsed(4));
        assertTrue(f.isUsed(4.9999));
        assertTrue(f.isUsed("4.9999"));
        assertTrue(f.isUsed(5.0001));   // converts to (int) 5
        assertTrue(f.isUsed("5.001"));// converts to (int) 5
        assertTrue(f.isUsed("5"));
        assertTrue(f.isUsed("5.0"));
        assertTrue(f.isUsed(null));

        f = new OALessOrEqualFilter("5.001");
        assertFalse(f.isUsed("5.002"));
        assertTrue(f.isUsed("5.001"));
        assertFalse(f.isUsed("5.001000001"));
        assertFalse(f.isUsed("51.001000001"));
        assertTrue(f.isUsed("5.000999"));
    }    
}


