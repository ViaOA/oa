package com.viaoa.util;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObject;

import test.xice.tsac.model.oa.*;

public class OAXMLReaderTest extends OAUnitTest {

    
//    @Test
    public void test() throws Exception {
        
        // simple test to load into oaobjects 
        
        String s;
        s = "<xml>"; // must wrap in outer tag
        s += "<ssh><command>runcommand1</command><output>output text here1</output><more><a>Aaa</a><b>Bbb</b></more></ssh>\n";
//        s += "<ssh><command>runcommand2</command><output>output text here2</output></ssh>\n";
//        s += "<ssh><command>runcommand3</command><output>output text here3</output></ssh>\n";
        s += "</xml>";
        
        OAXMLReader1 xr = new OAXMLReader1() {
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

    @Test
    public void test2() throws Exception {
        
        Application app = new Application();
        app.setName("appName");
        Server serv = new Server();
        serv.setName("serverName");
        app.setServer(serv);
        

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        OAXMLWriter xw = new OAXMLWriter(pw);
        xw.write(app);
        xw.close();
        String xml = new String(baos.toString());
        
        OAXMLReader xr = new OAXMLReader();
        Object[] objs = xr.readXML(xml);
        
        assertEquals(1, objs.length);
        
        assertEquals(app, objs[0]);
        
    }

}
