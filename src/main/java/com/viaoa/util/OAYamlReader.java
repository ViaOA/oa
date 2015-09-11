/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import com.viaoa.object.OAObject;

/**
    OAJsonReader that converts to XML, and then uses OAXMLReader to convert to OAObjects and Hubs.  
    @see OAJsonWriter
    @author vvia
    @since 20150129
    
    *** NOT complete, only converts simple yaml format.  See Unit test for example
*/
public class OAYamlReader {
    private int len;
    private int pos;
    private StringBuilder sb;
    private Class rootClass;
    private String rootPropertyName, rootPropertyName2;
    private String rootObjectName;

    
    public OAYamlReader(String rootObjectName, String rootPropertyName, String rootPropertyName2) {
        this.rootObjectName = rootObjectName;
        this.rootPropertyName = rootPropertyName;
        this.rootPropertyName2 = rootPropertyName2;
    }
    
    /**
     * @param rootClass class for the root object.  If it is a Hub, then it needs to be the OAObjectClass of the Hub.
     * @param rootPropertyName name of property for top level values in yaml
     */
    public Object[] parse(String yaml, Class rootClass) {
        try {
            String xml = convertToXML(yaml, rootClass);
            OAXMLReader xmlReader = new OAXMLReader() {
                @Override
                public Object convertToObject(String propertyName, String value, Class propertyClass) {
                    if ("null".equals(value)) {
                        return null;
                    }
                    if (OADate.class.equals(propertyClass)) return new OADate(value, "yyyy-MM-dd");
                    if (OATime.class.equals(propertyClass)) return new OATime(value, "HH:mm:ss");
                    if (OADateTime.class.equals(propertyClass)) return new OADate(value, "yyyy-MM-dd'T'HH:mm:ss");
                    return super.convertToObject(propertyName, value, propertyClass);
                }
                @Override
                protected String resolveClassName(String className) {
                    return OAYamlReader.this.getClassName(className);
                }
                @Override
                public Object getValue(OAObject obj, String name, Object value) {
                    return OAYamlReader.this.getValue(obj, name, value);
                }
                @Override
                protected String getPropertyName(OAObject obj, String propName) {
                    return OAYamlReader.this.getPropertyName(obj, propName);
                }
                @Override
                public void endObject(OAObject obj, boolean hasParent) {
                    OAYamlReader.this.endObject(obj, hasParent);
                }
            };
            xmlReader.parseString(xml);
            
            Object[] objs = xmlReader.readXML(xml);
            
            return objs;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // get the classname to use for a property
    protected String getClassName(String className) {
//System.out.println("getClassName className="+className);//qqqqqqqqq
//        className = "com.viaoa.object.OAObject";
        return className;
    }
    // get the propertyName to use
    protected String getPropertyName(OAObject obj, String propName) {
//System.out.println("getPropertyName obj="+obj+", propName="+propName);//qqqqqqqqq
//propName = null;
        return propName;
    }
    // get the value to use when setting a property
    protected Object getValue(OAObject obj, String name, Object value) {
//System.out.println("getValue obj="+obj+", propName="+name+", value="+value);//qqqqqqqqq        
        return value;
    }
    protected void endObject(OAObject obj, boolean hasParent) {
    }
    
   

    public String convertToXML(String text, Class rootClass) {
        this.rootClass = rootClass;
        pos = 0;
        len = text.length();
        sb = new StringBuilder(len*3);

        sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
        sb.append("<OAXML VERSION='2.0' DATETIME='9/9/15 9:08 AM'>\n");
        //sb.append("<com.viaoa.hub.Hub ObjectClass=\""+rootClass.getName()+"\">\n");
        
        BufferedReader br = new BufferedReader(new StringReader(text));
        try {
            boolean indented = false;
            int cntObject = 0;
            for (int i = 0;; i++) {
                String line = br.readLine();
                if (line == null) break;
                // System.out.println(i+") "+line);
                
                String name = OAString.field(line, ':', 1);
                if (name.trim().length() == 0) continue;
                
                if (name.trim().charAt(0) == '#') continue;

                String value = OAString.field(line, ':', 2, 999);

                if (name.length() > 0 && name.charAt(0) == ' ') indented = true;
                else indented = false;

                name = name.trim();
                if (value != null) value = value.trim();

                if (!indented) {
                    if (cntObject++ > 0) {
                        sb.append("</" + rootObjectName + ">\n");
                    }
                    sb.append("<" + rootObjectName + ">\n");

                    /*
                        te:    << rootPropertyName value
                          order: 6      << name/value props
                          login: impact
                          packages: [te, teconfig]
                          type: te
                    
                    
                        pdk-st-ixmts-01: [mts]   <<  rootPropertyName value and rootPropertyName2 value 
                    */
                    if (!OAString.isEmpty(value)) {
                        sb.append("  <" + rootPropertyName + ">" + name + "</" + rootPropertyName + ">\n");
                        name = rootPropertyName2;
                    }
                    else {
                        value = name;
                        name = rootPropertyName;
                    }
                }
                sb.append("  <" + name + ">" + value + "</" + name + ">\n");
            }
            if (cntObject > 0) {
                sb.append("</" + rootObjectName + ">\n");
            }
        }
        catch (Exception e) {
            System.out.println("error: " + e);
            e.printStackTrace();
        }

        //sb.append("</com.viaoa.hub.Hub>\n");
        sb.append("</OAXML>\n");
        return new String(sb);
    }
    
    
}
