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

import java.util.ArrayList;

import com.viaoa.object.OAObject;

/**
    OAJsonReader that converts to XML, and then uses OAXMLReader to convert to OAObjects and Hubs.  
    @see OAJsonWriter
    @author vvia
    @since 20120518
*/
public class OAJsonReader {
    private String jsonText;
    private int len;
    private int pos;
    private StringBuilder sb;
    private JsonToken token, lastToken;
    private Class rootClass;

    
    /**
     * Parses the JSON text and returns the root object(s).
     * @param rootClass class for the root object.  If it is a Hub, then it needs to be the OAObjectClass of the Hub.
     */
    public Object[] parse(String json, Class rootClass) {
        try {
            String xml = convertToXML(json, rootClass);
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
                    return OAJsonReader.this.getClassName(className);
                }
                @Override
                public Object getValue(OAObject obj, String name, Object value) {
                    return OAJsonReader.this.getValue(obj, name, value);
                }
                @Override
                protected String getPropertyName(OAObject obj, String propName) {
                    return OAJsonReader.this.getPropertyName(obj, propName);
                }
                @Override
                public void endObject(OAObject obj, boolean hasParent) {
                    OAJsonReader.this.endObject(obj, hasParent);
                }
            };
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
    
   
    
    // 20141222 rewrote
    public String convertToXML(String jsonText, Class rootClass) {
        this.jsonText = jsonText;
        this.rootClass = rootClass;
        pos = 0;
        len = jsonText.length();
        sb = new StringBuilder(len*3);

        sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
        sb.append("<OAXML VERSION='2.0' DATETIME='9/9/15 9:03 AM'>\n");
        //sb.append("<com.viaoa.hub.Hub ObjectClass=\""+rootClass.getName()+"\">\n");
        
        for (int i=0; ;i++) {
            parseRootObject();
            if (token.type == TokenType.eof) break;
        }
        
        //sb.append("</com.viaoa.hub.Hub>\n");
        sb.append("</OAXML>\n");
        return new String(sb);
    }

    protected void parseRootObject() {
        for (int i=0; ;i++) {
            nextToken();
            if (token.type == TokenType.eof) break;
            if (token.type == TokenType.string) {
                String s = token.value;
                nextToken();
                parseObject(s);
            }
            else {
                parseObject(rootClass.getName());
            }
        }
    }
    
    protected void parseObject(String name) {
        name = OAString.trim(name);
        if (token.type == TokenType.leftSquareBracket) {
            parseObjects(name);
        }
        else if (token.type == TokenType.leftBracket) {
            sb.append("<"+name+">\n");
            parseProperties();
            sb.append("</"+name+">\n");
        }
    }

    protected void parseObjects(String name) {
        for (;;) {
            nextToken();
            if (token.type == TokenType.rightSquareBracket) break;
            if (token.type == TokenType.eof) break;
            parseObject(name);
        }
    }

    protected void parseProperties() {
        for (;;) {
            nextToken();
            if (token.type == TokenType.rightBracket) break;
            if (token.type == TokenType.eof) break;
            
            String pname = OAString.trim(token.value);
            nextToken(); // colon
            nextToken(); // propValue, [, or {
            
            if (token.type == TokenType.leftSquareBracket || token.type == TokenType.leftBracket) {
                parseObject(pname);
            }
            else {
                sb.append("<"+pname+">");
                sb.append(token.value);
                sb.append("</"+pname+">\n");
            }
        }
    }
//qqqqqqqqqqqqqqqq end
    
    
    
    /**
     * Convert to OAXML so that OAXMLReader can be used to load the Hubs and OAObjects
     * @param rootClass class for the root object.  If it is a Hub, then it needs to be the OAObjectClass of the Hub.
     */
// NOT USED qqqqqqqqq    
    public String convertToXML__OLD__(String jsonText, Class rootClass) {
        this.jsonText = jsonText;
        this.rootClass = rootClass;
        pos = 0;
        len = jsonText.length();
        sb = new StringBuilder(len*3);

        sb.append("<?xml version='1.0' encoding='utf-8'?>\n");
        sb.append("<OAXML VERSION='1.0' DATETIME='5/18/12 10:42 AM'>\n");
        convert(false);
        sb.append("</OAXML>\n");
        return new String(sb);
    }
    
    
    /* A "INSERTCLASS" will be inserted as a placeholder for the class name.  OAXMLReader will then
       find the correct value when it is converting to objects. 
     */
// NOT USED qqqqqqqqq    
    protected void convert(boolean bNeedsTag) {
        boolean bFirstEver = (token == null && lastToken == null);
        boolean bFirstIsHub = false;

        for (;;) {
            nextToken();
            
            if (bFirstEver && lastToken == null) {
                if (token.type == TokenType.leftSquareBracket) {
                    sb.append("<com.viaoa.hub.Hub ObjectClass=\""+rootClass.getName()+"\">\n");
                    bFirstIsHub = true;
                }
                else sb.append("<"+rootClass.getName()+">\n");
            }
            
            if (token.type == TokenType.eof) break; 
            if (token.type == TokenType.comma) continue; 
            if (token.type == TokenType.rightBracket) break;
            if (token.type == TokenType.rightSquareBracket) break;
            
            String name = null;
            if (token.type == TokenType.string) {
//qqqq                name = getName(token.value);
                sb.append("<" + name + ">");
                nextToken();
                if (token.type == TokenType.colon) nextToken();
            }
            
            if (token.type == TokenType.leftBracket) {
                if (bNeedsTag) sb.append("<INSERTCLASS>\n");
                convert(true);
                if (bNeedsTag) sb.append("</INSERTCLASS>\n");
            }
            else if (token.type == TokenType.leftSquareBracket) {
                if (bNeedsTag) sb.append("<com.viaoa.hub.Hub ObjectClass=\"INSERTCLASS\">\n");
                convert(true); // convert all of the objects in the collection
                if (bNeedsTag) sb.append("</com.viaoa.hub.Hub>\n");
            }
            else {
                // see if <![CDATA[xxx.value.xxx]]>   is needed   qqqqqqqqqqqqq
                sb.append(token.value);
            }

            if (name != null) sb.append("</" + name + ">\n");
        }
        if (bFirstEver) {
            if (bFirstIsHub) {
                sb.append("</com.viaoa.hub.Hub>\n");
            }
            else sb.append("</"+rootClass.getName()+">\n");
        }
        
    }
    
    protected void nextToken() {
        lastToken = token;
        token = getNext();
    }

    static class JsonToken {
        TokenType type;
        String value;
    }
    enum TokenType {
        string,
        number,
        colon,
        comma,
        leftBracket,
        rightBracket,
        leftSquareBracket,
        rightSquareBracket,
        eof
    }
    
    public JsonToken getNext() {
        JsonToken token = new JsonToken();
        char charWaitFor = 0;
        char ch=0, chLast;
        
        boolean bReturn = false;
        String sError = null;
        StringBuilder sb = new StringBuilder(64);
        boolean bComma = false;

        for ( ; !bReturn && pos<len; pos++) {
            chLast = ch;
            ch = jsonText.charAt(pos);
            
            
            if (charWaitFor != 0) {
                if (ch == charWaitFor) {
                    if (chLast != '\\') {
                        bReturn = true;
                        continue;
                    }
                }
                if (ch != '\\' || chLast == '\\') { 
                    sb.append(ch);
                }
                continue;
            }
            
            if (ch == '\n' || ch == '\r') {
                if (token.type != null) {
                    bComma = false;
                    bReturn = true;
                }
                continue;
            }
            if (ch == '\t' || ch == '\f' || ch == ' ') {
                if (token.type == null) {
                    continue;
                }
            }
            //was: if (ch == '\t' || ch == '\f' || ch == '\n' || ch == '\r' || ch == ' ') continue;
            
            if (ch == ',') {
                if (token.type != null) {
                    bReturn = true;
                }
                else bComma = true;
                continue;
            }
            
            if (ch == '\'') {
                if (token.type != null) break; // start of next
                charWaitFor = ch;
                token.type = TokenType.string;
                continue;
            }
            if (ch == '\"') {
                if (token.type != null) break;
                charWaitFor = ch;
                token.type = TokenType.string;
                continue;
            }

            if (token.type == null) {
                if (Character.isDigit(ch) || ch == '-') {
                    token.type = TokenType.number;
                }
                else if (ch == '+') {
                    token.type = TokenType.number;
                    continue;
                }
                else if (ch == '.') {
                    token.type = TokenType.number;
                }
                else if (ch == '{') {
                    token.type = TokenType.leftBracket;
                    bReturn = true;
                }
                else if (ch == '}') {
                    token.type = TokenType.rightBracket;
                    bReturn = true;
                }
                else if (ch == '[') {
                    token.type = TokenType.leftSquareBracket;
                    bReturn = true;
                }
                else if (ch == ']') {
                    token.type = TokenType.rightSquareBracket;
                    bReturn = true;
                }
                else if (ch == ':') {
                    token.type = TokenType.colon;
                    bReturn = true;
                }
                else if (ch == ',') {
                    token.type = TokenType.comma;
                    bReturn = true;
                }
                else if ( (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                    token.type = TokenType.string;
                }
                else {
                    sError = "Illegal token '"+ch+"'";
                    bReturn = true;
                }
            }
            else if (token.type == TokenType.number) {
                if (!Character.isDigit(ch)) {
                    token.type = TokenType.string;
                }
            }
            else if (ch == '{' || ch == '}' || ch == '[' || ch == ']') {
                break;
            }
            
            if (bComma) {
                sb.append(',');
                bComma = false;
            }
            sb.append(ch);
        }

        
        if (sError != null) {
            throw new RuntimeException(sError + " at position "+pos+" in json string=" + jsonText);
        }
        token.value = new String(sb);
        if (token.type == null) token.type = TokenType.eof;

        return token;
    }
}
