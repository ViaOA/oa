package com.viaoa.util;

import static org.junit.Assert.*;

import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObject;

public class OAXMLReaderTest extends OAUnitTest {

    
    @Test
    public void test() throws Exception {
        
        // simple test to load into oaobjects 
        
        String s;
        s = "<xml>"; // must wrap in outer tag
        s += "<ssh><command>runcommand</command><output>output text here</output></ssh>\n";
        s += "<ssh><command>runcommand2</command><output>output text here2</output></ssh>\n";
        s += "</xml>";
        
        OAXMLReader xr = new OAXMLReader() {
            @Override
            protected String resolveClassName(String className) {
                return null;
            }
        };
        Object objx = xr.parseString(s);
        Object[] objs = xr.getRootObjects();
        
        assertEquals(2, objs.length);
        
        assertEquals(OAObject.class, objs[0].getClass());
        
        OAObject oaobj = (OAObject) objs[1];
        Object val = oaobj.getProperty("command");
        assertEquals("runcommand2", val);
        
    }


}
