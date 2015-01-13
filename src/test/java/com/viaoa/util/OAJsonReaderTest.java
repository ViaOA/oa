package com.viaoa.util;

import static org.junit.Assert.*;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectPropertyDelegate;

public class OAJsonReaderTest extends OAUnitTest {

    private static final String cid = "_cid";
    
    @Test
    public void jsonTest() throws Exception {
        String txt = OAFile.readTextFile("runtime/test/OAJsonReaderTest/test.txt", 0);
     
        OAJsonReader jr = new OAJsonReader() {
            String lastName;
            @Override
            protected String getClassName(String className) {
                lastName = className;
                return "com.viaoa.object.OAObject";
            }
            
            @Override
            protected Object getValue(OAObject obj, String name, Object value) {
                if (lastName != null) {
                    OAObjectPropertyDelegate.unsafeSetProperty(obj, cid, lastName);
                    lastName = null;
                }
                if (value instanceof String) {
                    OAObjectPropertyDelegate.unsafeSetProperty(obj, name, (String) value);
                }
                
                return super.getValue(obj, name, value);
            }
        };

        //String xml = jr.convertToXML(txt, OAObject.class);
        //System.out.println(xml);
        Object[] objs = jr.parse(txt, OAObject.class);
        int x = objs.length;
        
        for (Object obj : objs) {
            if (!(obj instanceof OAObject)) continue;
            OAObject oaObj = (OAObject) obj;
            String objectName = (String) OAObjectPropertyDelegate.getProperty(oaObj, cid);
            if (objectName == null) continue;
            if (objectName.equalsIgnoreCase("RepoVersionOutput")) {
                String repoVersion = (String) OAObjectPropertyDelegate.getProperty(oaObj, "build_date");
            }
        }
    
    }
    
}
