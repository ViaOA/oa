package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;

import test.xice.tsac3.model.oa.*;

public class OACompareTest extends OAUnitTest {

    @Test
    public void isLikeTest() {
        String s = "abcde";
        
        assertTrue(OACompare.isLike(s, "A*"));
        assertFalse(OACompare.isLike(s, "*A"));
        assertTrue(OACompare.isLike(s, "a*"));
        assertFalse(OACompare.isLike(s, "*a"));
        
        assertTrue(OACompare.isLike(s, "*E"));
        assertFalse(OACompare.isLike(s, "E*"));
        assertTrue(OACompare.isLike(s, "*e"));
        assertFalse(OACompare.isLike(s, "e*"));
        
        assertFalse(OACompare.isLike(s, "A*E"));   // current only allows '*' at begin or end
        assertTrue(OACompare.isLike(s, "ABC*"));
        
        assertFalse(OACompare.isLike(null, "A*E"));
        assertFalse(OACompare.isLike(s, null));
    }
    
    @Test
    public void isEqualTest() {
        String s = "abcde";
        assertTrue(OACompare.isEqual(s, s));
        assertFalse(OACompare.isEqual(s, null));
        assertFalse(OACompare.isEqual(null, s));
        assertTrue(OACompare.isEqual(s, "ABcde", true));
        assertFalse(OACompare.isEqual(s, "ABcde", false));
        assertTrue(OACompare.isEqual(s, "abcde", false));
        
        assertTrue(OACompare.isEqual(null, null));
        assertTrue(OACompare.isEqual(null, 0));
        assertTrue(OACompare.isEqual(false, 0));
        
        assertTrue(OACompare.isEqual(0.0D, 0F));
        assertTrue(OACompare.isEqual(1.0D, 1F));
        assertTrue(OACompare.isEqual(0.01D, 0.01F, 2));
        assertTrue(OACompare.isEqual(0.01D, 0.009999F, 2));
        assertTrue(OACompare.isEqual(0.01D, ".010001", 2));
        assertTrue(OACompare.isEqual(0.01D, ".010001", 5));
        
        assertTrue(OACompare.isEqual(true, 1));
        assertTrue(OACompare.isEqual(true, -1));
        assertFalse(OACompare.isEqual(true, 0));
        assertTrue(OACompare.isEqual(false, 0));
        assertTrue(OACompare.isEqual(true, 't'));
        assertFalse(OACompare.isEqual(false, 'f'));
        assertTrue(OACompare.isEqual(true, "true"));
        assertTrue(OACompare.isEqual("true", true));
        assertTrue(OACompare.isEqual(false, "false"));
        assertTrue(OACompare.isEqual("false", false));
        assertTrue(OACompare.isEqual(true, "fx"));
        assertTrue(OACompare.isEqual("fx", true));
        assertTrue(OACompare.isEqual(false, ""));
        assertTrue(OACompare.isEqual(false, null));

    }
    
    @Test
    public void miscTest() {
        Object val1 = 222;
        Object val2 = "2*";
        
        assertTrue(OACompare.isLess(val2, val1));
        assertFalse(OACompare.isLess(val1, val2));
        
        assertTrue(OACompare.isLike(val1, val2));
        
        assertFalse(OACompare.isEqualOrLess(val1, val2));
        assertTrue(OACompare.isGreater(val1, val2));
        assertTrue(OACompare.isEqualOrGreater(val1, val2));
        
        assertFalse(OACompare.isEqualIgnoreCase(val1, val2));
        assertFalse(OACompare.isEqualIgnoreCase(val1, val2));
        assertFalse(OACompare.isEqual(val1, val2));
        
        val1 = 222;
        val2 = 222;
        assertTrue(OACompare.isEqualOrLess(val1, val2));
        assertFalse(OACompare.isLess(val1, val2));
        assertTrue(OACompare.isEqualOrGreater(val1, val2));
        assertFalse(OACompare.isGreater(val1, val2));
        
        val1 = 221;
        val2 = 222;
        assertTrue(OACompare.isEqualOrLess(val1, val2));
        assertTrue(OACompare.isLess(val1, val2));
        assertFalse(OACompare.isEqualOrGreater(val1, val2));
        assertFalse(OACompare.isGreater(val1, val2));
        assertTrue(OACompare.isGreater(val2, val1));

        assertTrue(OACompare.isBetween(val1, 0, 999));
        assertFalse(OACompare.isBetween(val1, 0, 5));
        assertFalse(OACompare.isBetween(val1, 999, 9999));
        assertFalse(OACompare.isBetween(val1, 221, 222));
        
        assertTrue(OACompare.isBetweenOrEqual(val1, 0, 221));
        assertFalse(OACompare.isBetweenOrEqual(val1, 0, 220));
        assertTrue(OACompare.isBetweenOrEqual(val1, 221, 999));
        assertFalse(OACompare.isBetweenOrEqual(val1, 222, 223));
        
        assertFalse(OACompare.isEmpty("a", true));
        assertTrue(OACompare.isEmpty("", true));
        assertTrue(OACompare.isEmpty(null, true));
        assertTrue(OACompare.isEmpty(0));
        assertFalse(OACompare.isEmpty(-1));
    }
    
    @Test
    public void testArray() {
        assertEquals(0, OACompare.compare(new String[] {}, false));
        assertEquals(-1, OACompare.compare(new String[] {}, true));
        assertEquals(1, OACompare.compare(new String[] {"z"}, false));
        assertEquals(0, OACompare.compare(new String[] {"z"}, true));

        assertEquals(0, OACompare.compare(false, new String[] {}));
        assertEquals(1, OACompare.compare(true, new String[] {}));
        assertEquals(-1, OACompare.compare(false, new String[] {"z"}));
        assertEquals(0, OACompare.compare(true, new String[] {"z"}));
    }    
    
    
    
}




