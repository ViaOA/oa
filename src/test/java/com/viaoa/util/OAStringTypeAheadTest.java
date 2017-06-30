package com.viaoa.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import com.viaoa.OAUnitTest;

// OAStrintTypeAhead not needed, txtfield uses a lookups (array<string>) instead
public class OAStringTypeAheadTest extends OAUnitTest {

    @Test
    public void test() {

/*        
        ArrayList<String> al = new ArrayList<>(); 
        for (int i=0; i<100; i++) {
            al.add(String.format("aaAaa%03d",i));
        }
        assertEquals(100, al.size());
        
        OAStringTypeAhead ta = new OAStringTypeAhead(al);
        ArrayList<String> alResults = ta.search("aaa");
        assertEquals(100, alResults.size());
        
        ta.setMaxResults(5);
        alResults = ta.search("aaa");
        assertEquals(5, alResults.size());

        alResults = ta.search("Xaa");
        assertEquals(0, alResults.size());
        
        alResults = ta.search(null);
        assertEquals(0, alResults.size());
        
        ta = new OAStringTypeAhead(al);
        alResults = ta.search("aaa");
        int i = 0;
        for (String s : alResults) {
            String s2 = al.get(i++);
            assertEquals(s, s2);
            s2 = ta.getDisplayValue(s);
            assertEquals(s, s2);
            s2 = ta.getSortValue(s);
            assertEquals(s.toUpperCase(), s2);
            s2 = ta.getDropDownDisplayValue(s);
            assertEquals(s, s2);
        }
        
        int x = 4;
        x++;
*/        
    }
    
    public static void main(String[] args) {
        OAStringTypeAheadTest test = new OAStringTypeAheadTest();
        test.test();
    }
    
}
