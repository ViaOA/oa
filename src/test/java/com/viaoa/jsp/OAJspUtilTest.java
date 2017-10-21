package com.viaoa.jsp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.util.OAString;

public class OAJspUtilTest extends OAUnitTest {

    @Test
    public void test() {
        
        String s = "te'st";
        s = OAJspUtil.createJsString(s, '\'');
        String s1 = "tags: ['"+s+"']";

        String s2 = OAJspUtil.createJsString(s1, '\"');
        String s3 = "tags: ['te\\\\\'st']";
        
        assertEquals(s2, s3);
    }
    
    
    
    @Test
    public void testC() {
        String id = "testId";

        String s = "Ni'ce";
        s = OAJspUtil.createEmbeddedJsString(s, '\"');

        String code = "";
        code += "if (!window.confirm(\""+s+"\")) return false;";  
        code += "$(\"#oacommand\").val(\""+id+"\");"; 

    
        code = "onClick='" + code + "'";
        
        String test = "<button id='"+id+"' "+code+">";;
        
        
        String s1 = test;
        String s2 = OAJspUtil.createJsString(s1, '\"');
        String s3 = "<button id='testId' onClick='if (!window.confirm(\\\"Ni\\\\x27ce\\\")) return false;$(\\\"#oacommand\\\").val(\\\"testId\\\");'>";
        
        assertEquals(s2, s3);
    }

    
    @Test
    public void testA() {
        String s1 = "A 'a\"''";
        String s2 = OAJspUtil.createJsString(s1, '\"');
        String s3 = "A 'a\\\"''";
        assertEquals(s2, s3);
    }
    
    @Test
    public void testB() {
        String s1 = "<This is a test";
        String s2 = OAString.convertToHtml(s1);
        String s3 = "&lt;This is a test";
        assertEquals(s2, s3);

        s1 = "<This is a \ntest";
        s2 = OAString.convertToHtml(s1);
        s3 = "&lt;This is a <br>test";
        assertEquals(s2, s3);
        
        s1 = "<This is a \r\ntest";
        s2 = OAString.convertToHtml(s1);
        s3 = "&lt;This is a <br>test";
        assertEquals(s2, s3);
        
        s1 = "<This is a test>";
        s2 = OAJspUtil.createJsString(s1, (char) 0);
        s3 = "<This is a test>";
        assertEquals(s2, s3);

        s1 = "<This is a \ntest>";
        s2 = OAJspUtil.createJsString(s1, (char) 0);
        s3 = "<This is a \\ntest>";
        assertEquals(s2, s3);
        
        s1 = "<This is a \\ntest>";
        s2 = OAJspUtil.createEmbeddedJsString(s1, (char) 0);
        s3 = "<This is a \\ntest>";

        s1 = "<This is a \\ntest>";
        s2 = OAJspUtil.createJsString(s1, (char) 0);
        s3 = "<This is a \\\\ntest>";
        assertEquals(s2, s3);
    }

    @Test
    public void testCreateJsString() {
        String s1;
        String s2;
        String s3;
        
        s1 = "js code here";
        s2 = OAJspUtil.createJsString(s1, '\'');
        s3 = "js code here";
        assertEquals(s2, s3);
        
        s1 = "js '\"'\" code here";
        s2 = OAJspUtil.createJsString(s1, '\'');
        s3 = "js \\'\"\\'\" code here";
        assertEquals(s2, s3);
        
        s1 = "js \\'\"'\" code here";
        s2 = OAJspUtil.createJsString(s1, '\'');
        s3 = "js \\\\\\'\"\\'\" code here";
        assertEquals(s2, s3);

        
        s1 = "js code here";
        s2 = OAJspUtil.createJsString(s1, '\"');
        s3 = "js code here";
        assertEquals(s2, s3);
        
        s1 = "js '\"'\" code here";
        s2 = OAJspUtil.createJsString(s1, '\"');
        s3 = "js '\\\"'\\\" code here";
        assertEquals(s2, s3);


        s1 = "js \\'\"'\\\" code here";
        s2 = OAJspUtil.createJsString(s1, '\"');
        s3 = "js \\\\'\\\"'\\\\\\\" code here";
        assertEquals(s2, s3);
        
        s1 = "te\nst";
        s2 = OAJspUtil.createJsString(s1, '\"');
        s3 = "te\\nst";
        assertEquals(s2, s3);
    }

    @Test
    public void testCreateEmbeddedJsString() {
        String s1;
        String s2;
        String s3;
        
        s1 = "t'est";
        s2 = OAJspUtil.createEmbeddedJsString(s1, '\"');
        s3 = "t\\x27est";
        assertEquals(s2, s3);

        s1 = "te\nst";
        s2 = OAJspUtil.createEmbeddedJsString(s1, '\"');
        s3 = "te\\nst";
        assertEquals(s2, s3);
        
        s1 = "t'e\nst";
        s2 = OAJspUtil.createEmbeddedJsString(s1, '\"');
        s3 = "t\\x27e\\nst";
        assertEquals(s2, s3);
        
        
        s1 = "t'es\"t";
        s2 = OAJspUtil.createEmbeddedJsString(s1, '\"');
        s3 = "t\\x27es\\x22t";
        assertEquals(s2, s3);

        s1 = "100% Alpaca \\T'E\"S'T\" Scarf <b>NICE</b>\\END";
        s2 = OAJspUtil.createEmbeddedJsString(s1, '\"');
        s3 = "100% Alpaca \\T\\x27E\\x22S\\x27T\\x22 Scarf <b>NICE</b>\\END";
        assertEquals(s2, s3);
    }

    
    
}
