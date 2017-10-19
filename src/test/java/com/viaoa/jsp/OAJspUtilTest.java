package com.viaoa.jsp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.util.OAString;

public class OAJspUtilTest extends OAUnitTest {

    @Test
    public void test() {
        String s1 = "A 'a\"''";
        String s2 = OAJspUtil.createJsString(s1, '\"');
        String s3 = "A 'a\\\"''";
        assertEquals(s2, s3);
    }
    
    @Test
    public void testCreateHtml() {
        String s1 = "<This is a test";
        String s2 = OAJspUtil.createJsString(s1, (char) 0, false, true);
        String s3 = "&lt;This is a test";
        assertEquals(s2, s3);

        s1 = "<This is a \ntest";
        s2 = OAJspUtil.createJsString(s1, (char) 0, false, true);
        s3 = "&lt;This is a <br>test";
        assertEquals(s2, s3);
        
        s1 = "<This is a \r\ntest";
        s2 = OAJspUtil.createJsString(s1, (char) 0, false, true);
        s3 = "&lt;This is a <br>test";
        assertEquals(s2, s3);
        
        s1 = "<This is a test>";
        s2 = OAJspUtil.createJsString(s1, (char) 0, false, true);
        s3 = "<This is a test>";
        assertEquals(s2, s3);

        s1 = "<This is a \ntest>";
        s2 = OAJspUtil.createJsString(s1, (char) 0, false, true);
        s3 = "<This is a \\ntest>";
        assertEquals(s2, s3);
        
        s1 = "<This is a \\ntest>";
        s2 = OAJspUtil.createJsString(s1, (char) 0, false, true);
        s3 = "<This is a \\\\ntest>";
        assertEquals(s2, s3);
    }

    @Test
    public void testCreateJsString() {
        String s1;
        String s2;
        String s3;
        
        s1 = "js code here";
        s2 = OAJspUtil.createJsString(s1, '\'', false, false);
        s3 = "js code here";
        assertEquals(s2, s3);
        
        s1 = "js '\"'\" code here";
        s2 = OAJspUtil.createJsString(s1, '\'', false, false);
        s3 = "js \\'\"\\'\" code here";
        assertEquals(s2, s3);
        
        s1 = "js \\'\"'\" code here";
        s2 = OAJspUtil.createJsString(s1, '\'', false, false);
        s3 = "js \\\\\\'\"\\'\" code here";
        assertEquals(s2, s3);

        
        s1 = "js code here";
        s2 = OAJspUtil.createJsString(s1, '\"', false,false);
        s3 = "js code here";
        assertEquals(s2, s3);
        
        s1 = "js '\"'\" code here";
        s2 = OAJspUtil.createJsString(s1, '\"', false,false);
        s3 = "js '\\\"'\\\" code here";
        assertEquals(s2, s3);


        s1 = "js \\'\"'\\\" code here";
        s2 = OAJspUtil.createJsString(s1, '\"', false,false);
        s3 = "js \\\\'\\\"'\\\\\\\" code here";
        assertEquals(s2, s3);
        
        s1 = "te\nst";
        s2 = OAJspUtil.createJsString(s1, '\"', false, false);
        s3 = "te\\nst";
        assertEquals(s2, s3);
    }

    @Test
    public void testCreateEmbeddedJsString() {
        String s1;
        String s2;
        String s3;
        
        s1 = "t'est";
        s2 = OAJspUtil.createJsString(s1, '\"', true,false);
        s3 = "t\\x5Cx27est";
        assertEquals(s2, s3);

        s1 = "te\nst";
        s2 = OAJspUtil.createJsString(s1, '\"', true,false);
        s3 = "te\\x5Cnst";
        assertEquals(s2, s3);
        
        s1 = "t'e\nst";
        s2 = OAJspUtil.createJsString(s1, '\"', true,false);
        s3 = "t\\x5Cx27e\\x5Cnst";
        assertEquals(s2, s3);
        
        
        s1 = "t'es\"t";
        s2 = OAJspUtil.createJsString(s1, '\"', true,false);
        s3 = "t\\x5Cx27es\\x5Cx22t";
        assertEquals(s2, s3);

        s1 = "100% Alpaca T'E\"S'T\" Scarf <b>NICE</b>\\END";
        s2 = OAJspUtil.createJsString(s1, '\"', true,false);
        s3 = "100% Alpaca T\\x5Cx27E\\x5Cx22S\\x5Cx27T\\x5Cx22 Scarf <b>NICE</b>\\x5C\\x5CEND";
        assertEquals(s2, s3);
    }

    
    
}
