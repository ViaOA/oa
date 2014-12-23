package com.viaoa.util;

import com.viaoa.object.OAObject;

public class OAJsonReaderTest {

    
    
    public void test() throws Exception {
        String txt = OAFile.readTextFile("test.txt", 0);
     
        OAJsonReader jr = new OAJsonReader();
        // String xml1 = jr.convertToXML(txt, OAObject.class);
        // Object[] objs = jr.parse(txt, OAObject.class);

        
        jr = new OAJsonReader();
        String xml2 = jr.convertToXML(txt, OAObject.class);
        
        System.out.println(xml2);
        int xx = 4;
        xx++;
        
    }
    
    public static void main(String[] args) throws Exception {
        OAJsonReaderTest test = new OAJsonReaderTest();
        test.test();
        
    }
}
