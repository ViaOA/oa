package com.viaoa.util;


import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.viaoa.OAUnitTest;

import test.theice.tsac.model.oa.*;

public class SimpleXMLReaderTest extends OAUnitTest {

    
    @Test
    public void test() throws Exception {
        
        // simple test to load into oaobjects 
        
        String s;
        s = "<xml>"; // must wrap in outer tag
        s += "<ssh><command>runcommand1</command><output>output text here1</output><more><a>Aaa</a><b>Bbb</b></more></ssh>\n";
        s += "<ssh><command>runcommand2</command><output>output text here2</output></ssh>\n";
        s += "<ssh><command>runcommand3</command><output>output text here3</output></ssh>\n";
        s += "<fn>Aaaa</fn><fn>Bbbb</fn></xml>";
        
        
        
        SimpleXMLReader xr = new SimpleXMLReader();
        HashMap<String, Object> hm = xr.parse(s);
        display(hm);

        int xx = 4 ;
        xx++;
        
    }


    public void display(HashMap<String, Object> hm) {
        if (hm == null) return;
        
        for (Map.Entry<String, Object> ex : hm.entrySet()) {
            String keyx = ex.getKey();
            Object valuex = ex.getValue();
            display(keyx, valuex, 0);
        }
    }
    protected void display(String key, Object value, int indent) {
        String sx = "";
        for (int i=0; i<indent; i++) sx += "  ";

        if (value instanceof ArrayList) System.out.println(sx+key+"[]");
        else System.out.println(sx+key);
        
        if (value instanceof ArrayList) {
            ArrayList al = (ArrayList) value;
            for (Object obj : al) {
                if (obj instanceof HashMap) {
                    HashMap hm = (HashMap) obj;
                    display(key, hm, indent+1);
                }
                else {
                    display(key, obj, indent+1);
                }
            }
        }
        else if (value instanceof HashMap) {
            HashMap<String, Object> hm = (HashMap<String, Object>) value;
            for (Map.Entry<String, Object> ex : hm.entrySet()) {
                String keyx = ex.getKey();
                Object valuex = ex.getValue();
                display(keyx, valuex, indent+1);
            }
        }
        else {
            System.out.println(sx+"  "+value);
        }
        
        if (value instanceof ArrayList) System.out.println(sx+"/"+key+"[]");
        else System.out.println(sx+"/"+key);
    }
    
}
