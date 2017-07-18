package com.viaoa.util;


import static org.junit.Assert.*;
import org.junit.Test;

import com.viaoa.OAUnitTest;

public class OAStringTest extends OAUnitTest {

    
    @Test 
    public void dcountTest() {
        String s = "A.b.c.d";
        assertEquals(OAString.dcount(s, '.'), 4);
        
        s = "A..c.d";
        assertEquals(OAString.dcount(s, '.'), 4);

        s = null;
        assertEquals(OAString.dcount(s, '.'), 0);

        s = "A.b.   .d";
        assertEquals(OAString.dcount(s, '.'), 4);
        
        s = "A.b:.'.c.d";
        assertEquals(OAString.dcount(s, '.'), 5);
        
        s = ".";
        assertEquals(OAString.dcount(s, '.'), 2);
        
        s = "1.2.3.4";
        s = OAString.field(s, ".", OAString.dcount(s, '.'));
        assertEquals(s, "4");

        s = "1.2.3.4";
        s = OAString.field(s, ".", OAString.dcount(s, '.'));
        assertEquals(s, "4");

        s = "1.2.3.4";
        s = OAString.field(s, ".", 1, OAString.dcount(s, '.')-1);
        assertEquals(s, "1.2.3");
    }
    @Test 
    public void fieldTest() {
        String s = "A.b.c.d";
        assertEquals(OAString.field(s, '.', OAString.dcount(s, '.')), "d");
        assertEquals(OAString.field(s, '.', 0), null);  // first field is 1, not 0.  0 will always return null

        s = "A.b.c.d";
        assertEquals(OAString.field(s, '.', 9), null);

        s = "A.b..d";
        assertEquals(OAString.field(s, '.', 3), "");

        s = "A.b..d";
        assertEquals(OAString.field(s, '.', -1), null);
        
        s = "A.b..d";
        assertEquals(OAString.field(s, '.', 3, 2), ".d");
        assertEquals(OAString.field(s, '.', 2, 3), "b..d");
        assertEquals(OAString.field(s, '.', 1, 99), s);
    }
    
    
    
    @Test
    public void formatTest() {
        String s = OAString.format("1234.56", "  R4,");
        assertEquals(s, "1,234.5600  ");

        s = OAString.format("1234.56", "4L");
        assertEquals(s, "1234");
    }
    
    @Test
    public void trimTest() {
        String s = OAString.trim(" a b    c  ");
        assertEquals(s, "a b c");
    }
    
    @Test
    public void convertTest() {
        String s = "abAcdEfA";
        s = OAString.convert(s, "A", "X");
        assertEquals(s, "abXcdEfX");
        s = OAString.convert(s, "X", "bb");
        assertEquals(s, "abbbcdEfbb");
        s = OAString.convert(s, "X", "bb");
        assertEquals(s, "abbbcdEfbb");
        s = OAString.convert(s, "bb", "b");
        assertEquals(s, "abbcdEfb");
        s = OAString.convert(s, "b", "");
        assertEquals(s, "acdEf");
    }
    
    @Test
    public void convertIgnoreCaseTest() {
        String s = "abAcdEfA";
        s = OAString.convertIgnoreCase(s, "A", "X");
        assertEquals(s, "XbXcdEfX");
        s = OAString.convertIgnoreCase(s, "x", "bb");
        assertEquals(s, "bbbbbcdEfbb");
        s = OAString.convertIgnoreCase(s, "X", "bb");
        assertEquals(s, "bbbbbcdEfbb");
        s = OAString.convertIgnoreCase(s, "BB", "b");
        assertEquals(s, "bbbcdEfb");
        s = OAString.convertIgnoreCase(s, "B", "");
        assertEquals(s, "cdEf");
    }
    
    @Test
    public void removeOtherCharactersTest() {
        String s = "1,234,5z67,ABC.123A4f5";
        s = OAString.removeOtherCharacters(s, "1234567890.");
        assertEquals(s, "1234567.12345");
    }

    @Test
    public void removeNonDigitsTest() {
        String s = "1,234,5z67,ABC.123A4f5";
        String sx = OAString.removeNonDigits(s);
        assertEquals(sx, "123456712345");
        sx = OAString.removeNonDigits(s, true);
        assertEquals(sx, "1234567.12345");
    }
    
    @Test
    public void pluralSingularTest() {
        String s = "Tree";
        String s2 = OAString.makePlural(s);
        assertEquals(s2, "Trees");
        s2 = OAString.makeSingular(s2);
        assertEquals(s, s2);
        
        s = "try";
        s2 = OAString.makePlural(s);
        assertEquals(s2, "tries");
        s2 = OAString.makeSingular(s2);
        assertEquals(s, s2);
    }

    @Test
    public void convertForSingleQuotesTest() {
        String s = "'";
        s = OAString.convertForSingleQuotes(s);
        assertEquals(s, "\\'");
        
        s = "\\'";
        s = OAString.convertForSingleQuotes(s);
        assertEquals(s, "\\\\'");

        s = "\\'x'";
        s = OAString.convertForSingleQuotes(s);
        assertEquals(s, "\\\\'x\\'");
    }
    
    @Test
    public void convertForDoubleQuotesTest() {
        String s = "\"";
        s = OAString.convertForDoubleQuotes(s);
        assertEquals(s, "\\\"");
        
        s = "\\\"";
        s = OAString.convertForDoubleQuotes(s);
        assertEquals(s, "\\\\\"");

        s = "\\\"x\"";
        s = OAString.convertForDoubleQuotes(s);
        assertEquals(s, "\\\\\"x\\\"");
    }
}
