/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.util;

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
            };
            xmlReader.parseString(xml);
            return xmlReader.getRootObjects();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Convert to OAXML so that OAXMLReader can be used to load the Hubs and OAObjects
     * @param rootClass class for the root object.  If it is a Hub, then it needs to be the OAObjectClass of the Hub.
     */
    public String convertToXML(String jsonText, Class rootClass) {
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
    
    protected String getName(String name) {
        return name;
    }
    
    /* A "INSERTCLASS" will be inserted as a placeholder for the class name.  OAXMLReader will then
       find the correct value when it is converting to objects. 
     */
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
                name = getName(token.value);
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

        for ( ; !bReturn && pos<len; pos++) {
            boolean bReturnNow = false;
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

            if (ch == '\t' || ch == '\f' || ch == '\n' || ch == '\r' || ch == ' ') continue;
            
            if (ch == '\'') {
                if (token.type != null) break; // bad
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
                if (Character.isDigit(ch)) {
                }
                else {
                    token.type = TokenType.string;
                }
            }
            if (bReturnNow) break; // leave on current pos
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
