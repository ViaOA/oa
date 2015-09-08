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

import java.util.*;
import java.io.*;
import java.lang.reflect.Modifier;
import java.net.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import com.viaoa.ds.OASelect;
import com.viaoa.hub.Hub;
import com.viaoa.object.*;

/**
    OAXMLReader using a SAXParser to parse and automatically create OAObjects from an XML file.
    
    This will do the following to find the existing object:
    1: if OAProperty.importMatch, then it will search to find a matching object
    2: if objectId props, then it will search to find a matching object
    3: use guid
    if not found, then a new object will be created.

    20150906 created to be correct xml, removing OA specific format and tags. old version renamed to OAXMLReader1
    
    @see OAXMLWriter
*/
public class OAXMLReader extends DefaultHandler {
    private String fileName;
    String value;
    int indent;
    int total;
    boolean bWithinTag;
    Object[] stack = new Object[10];
    private String decodeMessage;
    private static final String XML_KEYONLY = "XML_KEYONLY";
    private static final String XML_GUID = "XML_GUID";
    private static final String XML_VALUE = "XML_VALUE";
    private static final String XML_CLASS = "XML_CLASS";
    protected Class conversionClass;  // type of class that value needs to be converted to
    
    protected HashMap<String, OAObject> hashGuid;
    
    
    // used for classes that are abstract, so that the real class can be found when a keyonly is used.
    protected HashMap<String, Class> hashGuidAbstractClass; 
    
    private boolean bImportMatching = true;
    
    // flag to know if OAXMLWriter wrote the object, which adds an additonal tag for the start of each object.
    private int versionOAXML;
    
    public OAXMLReader() {
    }


    public void setImportMatching(boolean b) {
        this.bImportMatching = b;
    }
    public boolean getImportMatching() {
        return this.bImportMatching;
    }
    
    /**
        Used to unencrypt an XML file created by OAXMLWriter that used an encryption code.
        @see OAXMLWriter#setEncodeMessage(String)
    */
    public void setDecodeMessage(String msg) {
        if (msg != null && msg.length() == 0) throw new IllegalArgumentException("DecodeMessage cant be an empty string");
        decodeMessage = msg;
    }
    public String getDecodeMessage() {
        return decodeMessage;
    }
    
    protected void reset() {
        indent = 0;
        total = 0;
        bWithinTag = false;
        hashGuid = new HashMap();
        hashGuidAbstractClass = new HashMap();
        versionOAXML = 0;
    }

    /**
        Used to parse and create OAObjects from an XML file.
    */
    public void parseFile(String fileName) throws Exception {
        if (fileName == null) throw new IllegalArgumentException("fileName is required");
        reset();
        
        URI uri = null;
        File f = new File(OAString.convertFileName(fileName));
        if (f.exists()) uri = f.toURI();
        else uri = new URI(fileName);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse( uri.toString(), this );
        
        Object[] objs = new Object[indent + 1];
        System.arraycopy(stack, 0, objs, 0, indent+1);
        stack = objs;
    }

    /**
        Used to parse and create OAObjects from an XML string.
    */
    public void parseString(String xmlData) throws Exception {
        if (xmlData == null) throw new IllegalArgumentException("xmlData is required");
        reset();
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
    
        saxParser.parse(new StringBufferInputStream(xmlData), this);
    }
    
    
    
    public ArrayList<? extends OAObject> process(final Class<? extends OAObject> rootClass) throws Exception {
        ArrayList<OAObject> al = null;
        try {
            OAThreadLocalDelegate.setLoadingObject(true);
            al = _process(rootClass);
        }
        finally {
            OAThreadLocalDelegate.setLoadingObject(false);
        }
        return al;
    }
    
    
    public ArrayList<OAObject> _process(final Class<? extends OAObject> rootClass) throws Exception {
        final ArrayList<OAObject> alReturn = new ArrayList();
        final HashMap<String, Object> hm = (HashMap) stack[1];
        
        /* the stages to loading
            0: create when: not key only and not abstract class
               if using guid and abstract, put in hmGuidAbstractClass
            1: have all keyonly with guid update class in hmGuidAbstractClass
            2: create abstract class using correct class in hmGuidAbstractClass
            3: if keyonly, find in hmGuid
        */
        for (int i=0; i<4; i++) {
            if (i > 0 && hashGuid.isEmpty() && hashGuidAbstractClass.isEmpty()) i = 3;
            
            for (Map.Entry<String, Object> e : hm.entrySet()) {
                Object v = e.getValue();
                if (v instanceof HashMap) {
                    OAObject objx = _process( (HashMap) v, rootClass, i);
                    if (objx != null && alReturn.size() == 0) alReturn.add(objx);
                    break;
                }
                if (v instanceof ArrayList) {
                    boolean bWasEmpty = (alReturn.size() == 0);
                    for (HashMap<String, Object> hmx : (ArrayList<HashMap<String, Object>>) v) {
                        OAObject objx = _process( (HashMap) v, rootClass, i);
                        if (bWasEmpty && objx != null) alReturn.add(objx);
                    }
                    break;
                }
            }        
        }
        return alReturn;
    }

    protected String resolveClassName(String className) {
        return className;
    }
    
    protected OAObject _process(HashMap<String, Object> hm, Class<? extends OAObject> toClass, final int stage) throws Exception {
        OAObject objNew = null;
        
//qqqqqq might not have guid.  Also, research xml standared for IDRef/etc
//qqqqq if no guids used, then only need to have the first process
        
        String guid = (String) hm.get(XML_GUID);
        boolean bIsAbstract = Modifier.isAbstract(toClass.getModifiers());

        String cname = (String) hm.get(XML_CLASS);
        if (!OAString.isEmpty(cname)) {
            cname = resolveClassName(cname);
            toClass = (Class<? extends OAObject>) Class.forName(cname);
            
//qqqqqqqq            
if (!cname.equals("com.viaoa.builder.model.Property")) {  
    if (!cname.equals("com.viaoa.builder.model.LinkProperty")) {
        if (!cname.equals("com.viaoa.builder.model.CalcProperty")) {
        int xx = 4;
        xx++;
    }
    }
}
        }
        
        
        if (guid != null) {
            objNew = hashGuid.get(guid);
            
            boolean bKeyOnly = (hm.get(XML_KEYONLY) != null);

if (guid.equals("318") && !bKeyOnly) {
    int xx = 4;
    xx++;
}
            
            
            if (bIsAbstract && !bKeyOnly) {
                if (stage == 1) return objNew;  // bKeyOnly to update hashGuidAbstractClass
                // will need to find out the "real" class to use on the bSecondPass
                
                Class c = hashGuidAbstractClass.get(guid);
                if (c == null) {
                    if (stage == 0) {
                        hashGuidAbstractClass.put(guid, toClass);
                    }
                }
                else {
                    toClass = c;
                    bIsAbstract = Modifier.isAbstract(toClass.getModifiers());
                    if (bIsAbstract) {
                        // stage 2 needs to know the correct class
                        throw new RuntimeException("cant determine class to use, the abstract class is "+c);
                    }
                }
            }
            else if (stage==1 && bKeyOnly && toClass != null && !bIsAbstract) {
                if (hashGuidAbstractClass.containsKey(guid)) {
                    hashGuidAbstractClass.put(guid, toClass);
                }
            }

            if (bKeyOnly) {
                return objNew;
            }
        }

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(toClass);
        if (objNew == null && !bIsAbstract) {
    
            // try to find using pkey props, AND remove pkey properties from hash
            String[] ids = oi.getIdProperties();
            Object[] values = new Object[ ids == null ? 0 : ids.length ];
            for (int i=0; i<ids.length; i++) {
                String id = ids[i].toUpperCase();
                Class c2 = OAObjectInfoDelegate.getPropertyClass(toClass, id);
                values[i] = hm.get(id);
                if (values[i] instanceof String) values[i] = OAConverter.convert(c2, values[i]);
                if (stage > 0 || !bIsAbstract) hm.remove(id);
            }
            final OAObjectKey key = new OAObjectKey(values, OAConv.toInt(guid), false);
            
            // try to find using matching props
            final String[] matchProps = getImportMatching() ? oi.getImportMatchProperties() : null;
            final Object[] matchValues = new Object[ matchProps == null ? 0 : matchProps.length ];
            if (matchProps != null && matchProps.length > 0) {
                for (int i=0; i<matchProps.length; i++) {
                    String id = matchProps[i].toUpperCase();
                    Class c2 = OAObjectInfoDelegate.getPropertyClass(toClass, id);
                    matchValues[i] = hm.get(id);
                    if (matchValues[i] instanceof String) matchValues[i] = OAConverter.convert(c2, matchValues[i]);
                }
    
                OASelect sel = new OASelect(toClass);
                sel.setFilter(new OAFilter() {
                    @Override
                    public boolean isUsed(Object obj) {
                        if (!(obj instanceof OAObject)) return false;
                        for (int i=0; i<matchProps.length; i++) {
                            Object val1 = ((OAObject)obj).getProperty(matchProps[i]);
                            if (!OACompare.isEqual(val1, matchValues[i])) return false;
                        }
                        return true;
                    }
                });
                sel.select();
                objNew = sel.next();
                sel.close();
            }
            else {
                if (ids != null && ids.length > 0) {
                    objNew = OAObjectCacheDelegate.get(toClass, key);
                }
            }
    
            if (objNew == null) {
                objNew = createNewObject(toClass);
            }
            if (guid != null && objNew != null) hashGuid.put(guid, objNew);
        }
        
        for (Map.Entry<String, Object> e : hm.entrySet()) {
            String k = e.getKey();

            if (XML_VALUE.equals(k)) continue;
            if (XML_KEYONLY.equals(k)) continue;
            if (XML_GUID.equals(k)) continue;
            if (XML_CLASS.equals(k)) continue;

            Object v = e.getValue();
            
            if (v instanceof String) {
                // set prop
                if (objNew != null && stage == 3) objNew.setProperty(k, v);
                continue;
            }

            OALinkInfo li = oi.getLinkInfo(k);

            if (v instanceof HashMap && (li == null || li.getType() == li.MANY)) {
                // check to see if it has an arrayList or a Many property, making this a hub prop
                HashMap<String, Object> hmx = (HashMap<String, Object>) v;
                for (Map.Entry<String, Object> ex : hmx.entrySet()) {
                    Object vx = ex.getValue();
                    if (vx instanceof ArrayList) {
                        v = vx;
                        break;
                    }
                    if (vx instanceof HashMap) {  // hub with only one
                        ArrayList al = new ArrayList();
                        al.add(vx);
                        v = al;
                        break;
                    }
                }
            }
            
            if (v instanceof ArrayList) {
                // load into Hub
                Hub h;
                if (objNew == null || stage != 3) h = null;
                else if (li == null) h = new Hub(OAObject.class);
                else h = (Hub) li.getValue(objNew);
                
                for (HashMap hmx : (ArrayList<HashMap>)v) {
                    Object objx = _process(hmx, li==null?OAObject.class:li.getToClass(), stage);
                    if (h != null) h.add(objx);
                }
                if (li == null) {
                    if (objNew != null && stage == 3) {
                        objNew.setProperty(k, h);
                    }
                }
            }
            else { 
                // hashmap for another object
                HashMap<String, Object> hmx = (HashMap<String, Object>) v;
                Class c = li == null ? OAObject.class : li.getToClass();
                OAObject objx = _process(hmx, c, stage);
                if (objNew != null && stage == 3) objNew.setProperty(k, objx);
            }
        }
        return objNew;
    } 
    
  //qqqqqqqqqqqq check to use DecodeMessage
// compare with oaxmlreader1    
    


    // SAXParser callback method.
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        value = "";
        bWithinTag = true;
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // not namespaceAware

        p(eName);
        indent++;
        
        if (indent == 1) {
            versionOAXML = "OAXML".equalsIgnoreCase(eName) ? 1 : 0;
            if (versionOAXML > 0) {
                // ex:  <OAXML VERSION='2.0' DATETIME='08/12/2015 11:56AM'>
                String version = null;
                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        String aName = attrs.getLocalName(i); 
                        if (!"version".equalsIgnoreCase(aName)) continue;
                        version = attrs.getValue(i);
                        if ("2.0".equals(version)) versionOAXML = 2; 
                        break;
                    }
                }
                if (versionOAXML != 2) throw new RuntimeException("version OAXML "+version+" not supported, current version is 2.0");
            }
            HashMap hm = new HashMap();
            stack[indent] = hm;
            return;
        }

        if (stack.length <= indent+4) {
            Object[] objs = new Object[indent + 20];
            System.arraycopy(stack, 0, objs, 0, stack.length);
            stack = objs;
        }

        stack[indent++] = eName;
        HashMap hm = new HashMap();
        stack[indent] = hm;

        if (attrs != null) {
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name
                if ("".equals(aName)) aName = attrs.getQName(i);
                String aValue = attrs.getValue(i);
                if (aName.equalsIgnoreCase("keyonly")) hm.put(XML_KEYONLY, XML_KEYONLY);
                else if (aName.equalsIgnoreCase("guid")) hm.put(XML_GUID, aValue);
                else if (aName.equalsIgnoreCase("class")) hm.put(XML_CLASS, aValue);
                else {
                    if (aValue == null || aValue.length() == 0) hm.put(aName, "true");
                    else hm.put(aName, aValue);
                }
            }
        }
    }
    
    // SAXParser callback method.
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        bWithinTag = false;
        String eName = sName; // element name
        if (eName == null || "".equals(eName)) eName = qName; // not namespaceAware
        
        HashMap hm = (HashMap) stack[indent];

        Object insertValue = value;
        if (!hm.isEmpty()) {
            hm.put(XML_VALUE, value);
            insertValue = hm;
        }
        if (indent == 1) {
            return;
        }

        HashMap hmParent = (HashMap) stack[indent-2];
        Object val = hmParent.get(eName);
        if (val != null) {
            ArrayList al;
            if (!(val instanceof ArrayList)) {
                al = new ArrayList();
                al.add(val);
                hmParent.put(eName, al);
            }
            else al = (ArrayList) val;
            al.add(insertValue);
        }
        else {
            hmParent.put(eName, insertValue);
        }
        indent -= 2;
    }
    
    // SAXParser callback method.
    public void characters(char buf[], int offset, int len) throws SAXException {
        if (bWithinTag && value != null) {
            String s = new String(buf, offset, len);
            value += OAString.decodeIllegalXML(s);
        }
    }

    private int holdIndent;
    private String sIndent="";
    void p(String s) {
        if (true) return;
        if (indent != holdIndent) {
            holdIndent = indent;
            sIndent = "";
            for (int i=0; i<indent; i++) sIndent += "  ";
        }
        System.out.println(sIndent+s);
    }



    // ============== These methods can be overwritten to get status of parsing ================

    /**
        Method that can be used to replace the value of an element/attribute.
    */
    public Object getValue(OAObject obj, String name, Object value) {
        return value;
    }

    /**
        Method that can be overwritten by subclass to provide status of reader.
    */
    public void startHub(String className, int total) {
    }

    /**
        Method that can be overwritten by subclass when an object is completed.
    */
    public void endObject(OAObject obj, boolean hasParent) {
    }

    /**
        SAXParser callback method.
    */
    public void startDocument() throws SAXException {
    }

    /**
        SAXParser callback method.
    */
    public void endDocument() throws SAXException {
    }

    /**
        Method that can be overwritten by subclass to create a new Object for a specific Class.
    */
    public OAObject createNewObject(Class c) throws Exception {
        try {
        OAObject obj = (OAObject) c.newInstance();
        
        return obj;
        }
        catch (Exception e) {
int xx = 4;//qqqq
xx++;
        }
        return null;
    }

    /**
        Convert from String to correct type.
        @param clazz type of object to convert value to
        @return null to skip property.
    */
    public Object convertToObject(String propertyName, String value, Class propertyClass) {
        if (propertyClass == null) return value;
        if (String.class.equals(propertyClass)) return value;

        Object result = OAConverter.convert(conversionClass, value);
        
        return result;
    }

    /** By default, this will check to see if object already exists 
        in OAObjectCache and return that object.  Otherwise this object is returned.
    */
    protected Object getRealObject(OAObject object) {
        Object obj = OAObjectCacheDelegate.getObject(object.getClass(), OAObjectKeyDelegate.getKey(object));
        if (obj != null) return obj;
        return object;
    }

    public static void main(String[] args) throws Exception {
        OAXMLReader r = new OAXMLReader();
        r.parseFile("C:\\Projects\\java\\OABuilder_git\\models\\testxml2.obx");
        ArrayList al = r.process(OAObject.class);
        
        int xx = 4;
        xx++;
    }
}
