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

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import com.viaoa.object.*;

/**
    XMLReader using a SAXParser to parse and load into a hierarchy of name/value, where value is a hashmap 
    or an ArrayList of values or hashmaps.
    
    NOTE: all name/values are put in uppercase
    see: SimpleXMLReaderTest
*/
public class SimpleXMLReader extends DefaultHandler {
    protected String value;
    protected int indent;
    protected Object[] stack;
    
    public SimpleXMLReader() {
    }

    /**
     * @param xml text to parse.
     * @return hashmap of name/values, where values is either a HashMap or an ArrayList of HashMaps
     */
    public HashMap<String, Object> parse(String xml) throws Exception {
        stack = new Object[20];
        stack[0] = new HashMap(10);
        indent = 0;
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        InputStream is = new StringBufferInputStream(xml);
        saxParser.parse(is, this);
        return (HashMap) stack[0];
    }
    
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        value = "";
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // not namespaceAware

        indent++;

        if (stack.length <= indent) {
            Object[] objs = new Object[stack.length + 20];
            System.arraycopy(stack, 0, objs, 0, stack.length);
            stack = objs;
        }
        stack[indent] = eName;

        Object prev = stack[indent-1];

        HashMap hm = null;
        
        if (prev == null) {
            hm = new HashMap();
            stack[++indent] = hm;
        }
        else if (prev instanceof String) {
            hm = new HashMap();
            stack[indent] = hm;
            stack[++indent] = eName;
        }
        if (hm != null && attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name
                if ("".equals(aName)) aName = attrs.getQName(i);
                String aValue = attrs.getValue(i);
                processProperty(aName, aValue, hm);
            }
        }
    }

    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // not namespaceAware
        
        Object stackObj = stack[indent--];
        Object prev = stack[indent];
        
        if (stackObj instanceof String) {
            HashMap hm = (HashMap) prev;
            processProperty(eName, value, hm);
            return;
        }

        // obj is done
        eName = (String) prev;
        HashMap hm = (HashMap) stack[--indent];
        processProperty(eName, (HashMap) stackObj, hm);
    }
    
    public void characters(char buf[], int offset, int len) throws SAXException {
        String s = new String(buf, offset, len);
        value += OAString.decodeIllegalXML(s);
    }

    protected void processProperty(String eName, HashMap value, HashMap hm) {
        Object objx = hm.get(eName.toUpperCase());
        if (objx == null) {
            hm.put(eName.toUpperCase(), value);
        }
        else {
            if (objx instanceof ArrayList) {
                ((ArrayList) objx).add(value);
            }
            else {
                ArrayList al = new ArrayList(5);
                al.add(objx);
                al.add(value);
                hm.put(eName.toUpperCase(), al);
            }
        }
    }
    
    protected void processProperty(String eName, String value, HashMap hm) {
        Object objValue = value;
        
        if (objValue == null) return;

        Object objx = hm.get(eName.toUpperCase());
        if (objx == null) {
            hm.put(eName.toUpperCase(), objValue);
        }
        else {
            if (objx instanceof ArrayList) {
                ((ArrayList) objx).add(objValue);
            }
            else {
                ArrayList al = new ArrayList(5);
                al.add(objx);
                al.add(objValue);
                hm.put(eName.toUpperCase(), al);
            }
        }
    }

    public void startDocument() throws SAXException {
    }

    public void endDocument() throws SAXException {
    }
}
