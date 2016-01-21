package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;
import com.viaoa.OAUnitTest;

import test.theice.tsac3.model.oa.*;

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
    
    
}
