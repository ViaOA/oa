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
    private static final String XML_ID = "XML_ID";
    private static final String XML_IDREF = "XML_IDREF";
    private static final String XML_VALUE = "XML_VALUE";
    private static final String XML_CLASS = "XML_CLASS";
    private static final String XML_OBJECT = "XML_OBJECT";
    protected Class conversionClass;  // type of class that value needs to be converted to
    
    protected HashMap<String, OAObject> hashGuid;
    
    private OAXMLReader1 xmlReader1;
    
    private boolean bImportMatching = true;
    
    // flag to know if OAXMLWriter wrote the object, which adds an additonal tag for the start of each object.
    private int versionOAXML;
    
    public OAXMLReader() {
    }


    public void setImportMatching(boolean b) {
        this.bImportMatching = b;
        if (xmlReader1 != null) xmlReader1.setImportMatching(b);
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
        if (xmlReader1 != null) xmlReader1.setDecodeMessage(msg);
    }
    public String getDecodeMessage() {
        return decodeMessage;
    }
    
    protected void reset() {
        indent = 0;
        total = 0;
        bWithinTag = false;
        hashGuid = new HashMap();
        versionOAXML = 0;
        xmlReader1 = null;
    }

    /**
     * Read the xml data from a file and load into objects.
     */
    public ArrayList readFile(String fileName, final Class<? extends OAObject> rootClass) throws Exception {
        parseFile(fileName);
        return process(rootClass);
    }
    public ArrayList read(File file, final Class<? extends OAObject> rootClass) throws Exception {
        return readFile(file.getPath(), rootClass);
    }
    /**
     * Read the xml data and load into objects.
     */
    public ArrayList readXML(String xmlText, final Class<? extends OAObject> rootClass) throws Exception {
        parseString(xmlText);
        return process(rootClass);
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
    
    public ArrayList process(final Class<? extends OAObject> rootClass) throws Exception {
        if (xmlReader1 != null) {
            ArrayList<OAObject> al = new ArrayList<OAObject>();
            for (Object objx : xmlReader1.getRootObjects()) {
                if (objx instanceof Hub) {
                    for (Object obj : ((Hub)objx)) {
                        al.add((OAObject) obj);
                    }
                    break;
                }
                al.add((OAObject) objx);
            }
            return al;
        }
        ArrayList<? extends OAObject> al = _process(rootClass);
        hashGuid = new HashMap();
        return al;
    }
    
    
    public ArrayList<? extends OAObject> _process(final Class<? extends OAObject> rootClass) throws Exception {
        final ArrayList<OAObject> alReturn = new ArrayList();
        final HashMap<String, Object> hm = (HashMap) stack[1];

        // two stage load, the 2nd is to match the idrefs
        for (int i=0; i<2; i++) {
            
            for (Map.Entry<String, Object> e : hm.entrySet()) {
                Object v = e.getValue();
                if (v instanceof HashMap) {
                    OAObject objx = _process( (HashMap) v, rootClass, i==0, 0);
                    if (objx != null && alReturn.size() == 0) {
                        alReturn.add(objx);
                    }
                    break;
                }
                if (v instanceof ArrayList) {
                    boolean bWasEmpty = (alReturn.size() == 0);
                    for (HashMap<String, Object> hmx : (ArrayList<HashMap<String, Object>>) v) {
                        OAObject objx = _process( hmx, rootClass, i==0, 0);
                        if (bWasEmpty && objx != null) {
                            alReturn.add(objx);
                        }
                    }
                    break;
                }
            }
        }
        return alReturn;
    }

    /**
     * This can be overwritten to change/expand a class name
     */
    protected String resolveClassName(String className) {
        return className;
    }
    
    protected OAObject _process(HashMap<String, Object> hm, Class<? extends OAObject> toClass, final boolean bIsPreloading, final int level) throws Exception {
        OAObject objNew = null;
        
        String guid = (String) hm.get(XML_ID);
        boolean bKeyOnly = false;
        if (guid == null) {
            guid = (String) hm.get(XML_IDREF);
            if (guid != null) bKeyOnly = true;
        }
        
        objNew = hashGuid.get(guid);
        if (bKeyOnly) return objNew;
        
        if (objNew == null && !bIsPreloading) {
            objNew = (OAObject) hm.get(XML_OBJECT);
        }
        if (objNew != null) toClass = objNew.getClass();
        else {
            String cname = (String) hm.get(XML_CLASS);
            if (!OAString.isEmpty(cname)) {
                cname = resolveClassName(cname);
                toClass = (Class<? extends OAObject>) Class.forName(cname);
            }
        }
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(toClass);
        if (objNew == null) {
    
            // try to find using pkey props, AND remove pkey properties from hash
            String[] ids = oi.getIdProperties();
            Object[] values = new Object[ ids == null ? 0 : ids.length ];
            for (int i=0; i<ids.length; i++) {
                String id = ids[i].toUpperCase();
                Class c2 = OAObjectInfoDelegate.getPropertyClass(toClass, id);
                values[i] = hm.get(id);
                if (values[i] instanceof String) values[i] = OAConverter.convert(c2, values[i]);
                hm.remove(id);
            }
            int iguid = 0;
            if (guid != null && guid.length()>1) iguid = OAConv.toInt(guid.substring(1));
            final OAObjectKey key = new OAObjectKey(values, iguid, false);
            
            // try to find using matching props
            final String[] matchProps = getImportMatching() ? oi.getImportMatchProperties() : null;
            final Object[] matchValues = new Object[ matchProps == null ? 0 : matchProps.length ];
            if (matchProps != null && matchProps.length > 0) {
                for (int i=0; i<matchProps.length; i++) {
                    String id = matchProps[i].toUpperCase();
                    Class c2 = OAObjectInfoDelegate.getPropertyClass(toClass, id);
                    matchValues[i] = hm.get(id);
                    
                    if (matchValues[i] instanceof HashMap) {
                        matchValues[i] = _process((HashMap) matchValues[i], c2, true, level+1);
                    }
                    else if (matchValues[i] instanceof String) matchValues[i] = OAConverter.convert(c2, matchValues[i]);
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
                OAThreadLocalDelegate.setLoadingObject(true);
                try {
                    objNew = createNewObject(toClass);
                    // set property ids
                    if (matchProps == null || matchProps.length == 0) {
                        for (int i=0; ids != null && i<ids.length; i++) {
                            values[i] = getValue(objNew, ids[i], values[i]);  // hook method for subclass
                            objNew.setProperty(ids[i], values[i]);
                        }
                    }
                }
                finally {
                    OAThreadLocalDelegate.setLoadingObject(false);
                }
            }
            if (guid != null && objNew != null) hashGuid.put(guid, objNew);
            hm.put(XML_OBJECT, objNew); 
        }
        
        
        final boolean bLoadingNew = objNew.getNew() && !bIsPreloading; 
        if (bLoadingNew) OAThreadLocalDelegate.setLoadingObject(true);
        
        for (Map.Entry<String, Object> e : hm.entrySet()) {
            String k = e.getKey();

            if (XML_VALUE.equals(k)) continue;
            if (XML_ID.equals(k)) continue;
            if (XML_IDREF.equals(k)) continue;
            if (XML_CLASS.equals(k)) continue;
            if (XML_OBJECT.equals(k)) continue;

            Object v = e.getValue();
            
            if (v instanceof String) {
                // set prop
                if (!bIsPreloading) objNew.setProperty(k, v);
                continue;
            }

            OALinkInfo li = oi.getLinkInfo(k);

            if (v instanceof HashMap && (li == null || li.getType() == li.MANY)) {
                // check to see if it has an arrayList or a Many property, making this a hub prop
                //   and skip this tag (outer collection tag) to get the the objects in it.
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
                if (bIsPreloading) h = null;
                else if (li == null) h = new Hub(OAObject.class);
                else h = (Hub) li.getValue(objNew);
                
                for (HashMap hmx : (ArrayList<HashMap>)v) {
                    
                    if (bLoadingNew) OAThreadLocalDelegate.setLoadingObject(false);
                    Object objx = _process(hmx, li==null?OAObject.class:li.getToClass(), bIsPreloading, level+1);
                    if (bLoadingNew) OAThreadLocalDelegate.setLoadingObject(true);
                    
                    if (!bIsPreloading) {
                        h.add(objx);
                    }
                }

                if (li == null && !bIsPreloading) {
                    objNew.setProperty(k, h);
                }
            }
            else { 
                // hashmap for another object
                HashMap<String, Object> hmx = (HashMap<String, Object>) v;
                Class c = li == null ? OAObject.class : li.getToClass();
                if (bLoadingNew) OAThreadLocalDelegate.setLoadingObject(false);
                OAObject objx = _process(hmx, c, bIsPreloading, level+1);
                if (bLoadingNew) OAThreadLocalDelegate.setLoadingObject(true);
                if (!bIsPreloading) objNew.setProperty(k, objx);
            }
        }
        if (bLoadingNew) OAThreadLocalDelegate.setLoadingObject(false);
        if (!bIsPreloading) {
            endObject(objNew, level>0);
        }
        return objNew;
    } 
    
    // SAXParser callback method.
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        if (xmlReader1 != null) {
            xmlReader1.startElement(namespaceURI, sName, qName, attrs);
            return;
        }
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
                if (versionOAXML == 1) {
                    xmlReader1 = new OAXMLReader1() {
                        @Override
                        protected String resolveClassName(String className) {
                            return OAXMLReader.this.resolveClassName(className);
                        }
                        @Override
                        public Object convertToObject(String propertyName, String value, Class propertyClass) {
                            return OAXMLReader.this.convertToObject(propertyName, value, propertyClass);
                        }
                        @Override
                        public OAObject createNewObject(Class c) throws Exception {
                            return OAXMLReader.this.createNewObject(c);
                        }
                        @Override
                        public void endObject(OAObject obj, boolean hasParent) {
                            OAXMLReader.this.endObject(obj, hasParent);
                        }
                        @Override
                        protected String getPropertyName(OAObject obj, String propName) {
                            return OAXMLReader.this.getPropertyName(obj, propName);
                        }
                        @Override
                        protected Object getRealObject(OAObject object) {
                            return OAXMLReader.this.getRealObject(object);
                        }
                        @Override
                        public Object getValue(OAObject obj, String name, Object value) {
                            return OAXMLReader.this.getValue(obj, name, value);
                        }
                    };
                    xmlReader1.reset();
                    xmlReader1.setDecodeMessage(getDecodeMessage());
                    xmlReader1.setImportMatching(getImportMatching());
                    xmlReader1.startElement(namespaceURI, sName, qName, attrs);
                }
                else if (versionOAXML != 2) throw new RuntimeException("version OAXML "+version+" not supported, current version is 2.0");
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
            String guid = null;
            boolean bKeyOnly = false;
            for (int i = 0; i < attrs.getLength(); i++) {
                String aName = attrs.getLocalName(i); // Attr name
                if ("".equals(aName)) aName = attrs.getQName(i);
                aName = aName.toUpperCase();
                String aValue = attrs.getValue(i);
                
                if (aName.equalsIgnoreCase("id")) hm.put(XML_ID, aValue);
                else if (aName.equalsIgnoreCase("idref")) hm.put(XML_IDREF, aValue);
                else if (aName.equalsIgnoreCase("class")) hm.put(XML_CLASS, aValue);
                else if (aName.equalsIgnoreCase("keyonly")) bKeyOnly = true;
                else if (aName.equalsIgnoreCase("guid")) guid = aValue;
                else {
                    if (aValue == null || aValue.length() == 0) hm.put(aName, "true");
                    else hm.put(aName, aValue);
                }
            }
            if (guid != null) {
                if (!bKeyOnly) hm.put(XML_ID, guid);
                else hm.put(XML_IDREF, guid);
            }
        }
    }
    
    // SAXParser callback method.
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        if (xmlReader1 != null) {
            xmlReader1.endElement(namespaceURI, sName, qName);
            return;
        }
        bWithinTag = false;
        String eName = sName; // element name
        if (eName == null || "".equals(eName)) eName = qName; // not namespaceAware
        eName = eName.toUpperCase();
        
        HashMap hm = (HashMap) stack[indent];

        
        if (decodeMessage != null && value != null && value.startsWith(decodeMessage)) {
            value = Base64.decode(value.substring(decodeMessage.length()));
        }

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
        if (xmlReader1 != null) {
            xmlReader1.characters(buf, offset, len);
            return;
        }
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

    // SAXParser callback method.
    public void startDocument() throws SAXException {
    }

    // SAXParser callback method.
    public void endDocument() throws SAXException {
        if (xmlReader1 != null) {
            xmlReader1.endDocument();
            return;
        }
    }

    /**
        Method that can be overwritten by subclass to create a new Object for a specific Class.
    */
    public OAObject createNewObject(Class c) throws Exception {
        OAObject obj = (OAObject) c.newInstance();
        return obj;
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
    protected OAObject getRealObject(OAObject object) {
        if (object == null) return object;
        OAObject obj = OAObjectCacheDelegate.getObject(object.getClass(), OAObjectKeyDelegate.getKey(object));
        if (obj != null) return obj;
        return object;
    }

    /**
        Method that can be overwritten by subclass when an object is completed.
    */
    public void endObject(OAObject obj, boolean hasParent) {
    }

    // return null to ignore property
    protected String getPropertyName(OAObject obj, String propName) {
        return propName;
    }
    
    public static void main(String[] args) throws Exception {
        OAXMLReader r = new OAXMLReader();
        r.parseFile("C:\\Projects\\java\\OABuilder_git\\models\\testxml2.obx");
        ArrayList al = r.process(OAObject.class);
        int xx = 4;
        xx++;
    }
}
