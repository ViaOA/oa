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
import com.viaoa.jfc.editor.image.OAImagePanel.MyUndoableEdit;
import com.viaoa.object.*;


// 20150906 created to be correct xml, removing OA specific format and tags. old version renamed to OAXMLReader1

/**
    OAXMLReader using a SAXParser to parse and automatically create OAObjects from an XML file.
    
    This will do the following to find the existing object:
    1: if OAProperty.importMatch, then it will search to find a matching object
    2: if objectId props, then it will search to find a matching object
    3: use guid
    if not found, then a new object will be created.

    @see OAXMLWriter
*/
public class OAXMLReader {
    private String fileName;
    protected String value;
    protected int indent;
    protected int total;
    protected boolean bWithinTag;
    protected Object[] stack = new Object[10];
    private String decodeMessage;
    private static final String XML_ID = "XML_ID";
    private static final String XML_IDREF = "XML_IDREF";
    private static final String XML_VALUE = "XML_VALUE";
    private static final String XML_CLASS = "XML_CLASS";
    private static final String XML_OBJECT = "XML_OBJECT";
    private static final String XML_HUB = "XML_HUB";
    protected Class conversionClass;  // type of class that value needs to be converted to
    
    protected HashMap<String, OAObject> hashGuid;
    
    private OAXMLReader1 xmlReader1;
    
    private boolean bImportMatching = true;
    
    private MyDefaultHandler myDefaultHandler;
    
    // flag to know if OAXMLWriter wrote the object, which adds an additonal tag for the start of each object.
    private int versionOAXML;
    
    public OAXMLReader() {
    }
    
    
    /**
     * Read the xml data from a file and load into objects.
     */
    public Object[] readFile(String fileName) throws Exception {
        parseFile(fileName);
        ArrayList al = process();
        Object[] objs = new Object[al.size()];
        al.toArray(objs);
        return objs;
    }
    public Object[] read(File file) throws Exception {
        return readFile(file.getPath());
    }
    /**
     * Read the xml data and load into objects.
     */
    public Object[] readXML(String xmlText) throws Exception {
        parseString(xmlText);
        ArrayList al = process();
        Object[] objs = new Object[al.size()];
        al.toArray(objs);
        return objs;
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

    
    
    
    // Used to parse and create OAObjects from an XML file.
    protected void parseFile(String fileName) throws Exception {
        if (fileName == null) throw new IllegalArgumentException("fileName is required");
        reset();
        
        URI uri = null;
        File f = new File(OAString.convertFileName(fileName));
        if (f.exists()) uri = f.toURI();
        else uri = new URI(fileName);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse( uri.toString(), this.getDefaultHandler());
        
        Object[] objs = new Object[indent + 1];
        System.arraycopy(stack, 0, objs, 0, indent+1);
        stack = objs;
    }

    // Used to parse and create OAObjects from an XML string.
    protected void parseString(String xmlData) throws Exception {
        if (xmlData == null) throw new IllegalArgumentException("xmlData is required");
        reset();
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
    
        saxParser.parse(new StringBufferInputStream(xmlData), this.getDefaultHandler());
    }
    
    protected ArrayList process() throws Exception {
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
        ArrayList<Object> al = _process();
        hashGuid = new HashMap();
        return al;
    }
    
    
    protected ArrayList<Object> _process() throws Exception {
        final ArrayList alReturn = new ArrayList<Object>();
        HashMap<String, Object> hm = (HashMap) stack[1];

        // uses a two pass, the 2nd is to match the idrefs and load the data
        for (int i=0; i<2; i++) {
            
            for (Map.Entry<String, Object> e : hm.entrySet()) {
                String key = e.getKey();
                Object v = e.getValue();
                if (v instanceof HashMap) {
                    Object objx = _processRoot(key, (HashMap) v, i==0);
                    if (i>0 && objx != null) {
                        alReturn.add(objx);
                    }
                }
                else if (v instanceof ArrayList) {
                    for (HashMap<String, Object> hmx : (ArrayList<HashMap<String, Object>>) v) {
                        Object objx = _processRoot(key, hmx, i==0);
                        if (i>0 && objx != null) {
                            alReturn.add(objx);
                        }
                    }
                }
            }
        }
        return alReturn;
    }
    protected Object _processRoot(String key, HashMap<String, Object> hm, final boolean bIsPreloading) throws Exception {
        if (!"HUB".equalsIgnoreCase(key)) {
            OAObject oaObj = _processChildren(hm, null, bIsPreloading, 0);
            return oaObj;
        }
        
        Class toClass = null;
        String cname = (String) hm.get(XML_CLASS);
        if (!OAString.isEmpty(cname)) {
            cname = resolveClassName(cname);
            toClass = Class.forName(cname);
        }
        if (toClass == null) toClass = OAObject.class;
        
        Hub hub = (Hub) hm.get(XML_HUB);
        if (hub == null) hub = new Hub(toClass);
        
        
        for (Map.Entry<String, Object> e : hm.entrySet()) {
            Object v = e.getValue();
            if (v instanceof HashMap) {
                Object objx = _processChildren( (HashMap) v, toClass, bIsPreloading, 0);
                if (!bIsPreloading && objx != null) {
                    hub.add(objx);
                }
            }
            else if (v instanceof ArrayList) {
                for (HashMap<String, Object> hmx : (ArrayList<HashMap<String, Object>>) v) {
                    Object objx = _processChildren( hmx, toClass, bIsPreloading, 0);
                    if (!bIsPreloading && objx != null) {
                        hub.add(objx);
                    }
                }
            }
        }
        return hub;
    }
    
    protected OAObject _processChildren(HashMap<String, Object> hm, Class<? extends OAObject> toClass, final boolean bIsPreloading, final int level) throws Exception {
        OAObject objNew = null;
        if (toClass == null) toClass = OAObject.class;
        
        
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
            objNew = getObject(toClass, hm);
        }
        
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
                        matchValues[i] = _processChildren((HashMap) matchValues[i], c2, true, level+1);
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
                OAThreadLocalDelegate.setLoading(true);
                try {
                    objNew = createNewObject(toClass);
                    // set property ids
                    if (matchProps == null || matchProps.length == 0) {
                        for (int i=0; ids != null && i<ids.length; i++) {
                            Object v = getValue(objNew, ids[i], values[i]);  // hook method for subclass
                            objNew.setProperty(ids[i], v);
                        }
                    }
                }
                finally {
                    OAThreadLocalDelegate.setLoading(false);
                }
                // 20181115
                OAObjectCacheDelegate.add(objNew);
            }
            if (guid != null && objNew != null) hashGuid.put(guid, objNew);
            hm.put(XML_OBJECT, objNew); 
        }
        
        
        final boolean bLoadingNew = objNew.getNew() && !bIsPreloading; 
        if (bLoadingNew) OAThreadLocalDelegate.setLoading(true);
        
        if (!bIsPreloading) beforeLoadObject(objNew, hm);
        for (Map.Entry<String, Object> e : hm.entrySet()) {
            String k = e.getKey();

            if (XML_VALUE.equals(k)) continue;
            if (XML_ID.equals(k)) continue;
            if (XML_IDREF.equals(k)) continue;
            if (XML_CLASS.equals(k)) continue;
            if (XML_OBJECT.equals(k)) continue;

            Object v = e.getValue();
            v = getValue(objNew, k, v);  // hook method for subclass
            
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
                else {
                    h = (Hub) li.getValue(objNew);
                    if (h == null) h = new Hub(OAObject.class);
                }
                
                for (HashMap hmx : (ArrayList<HashMap>)v) {
                    Object objx = null;
                    try {
                        if (bLoadingNew) OAThreadLocalDelegate.setLoading(false);
                        objx = _processChildren(hmx, li==null?OAObject.class:li.getToClass(), bIsPreloading, level+1);
                    }
                    finally {
                        if (bLoadingNew) OAThreadLocalDelegate.setLoading(true);
                    }
                    
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
                if (bLoadingNew) OAThreadLocalDelegate.setLoading(false);
                Object objx = _processChildren(hmx, c, bIsPreloading, level+1);
                if (bLoadingNew) OAThreadLocalDelegate.setLoading(true);
                if (!bIsPreloading) objNew.setProperty(k, objx);
            }
        }
        if (bLoadingNew) OAThreadLocalDelegate.setLoading(false);
        if (!bIsPreloading) {
            objNew = getRealObject(objNew);
            if (objNew != null) {
                endObject(objNew, level>0);
                afterLoadObject(objNew, hm);
            }
        }
        return objNew;
    } 
    
    // SAXParser callback method.
    protected void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
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
                            if (object == null) return object;
                            OAObject obj = OAObjectCacheDelegate.getObject(object.getClass(), OAObjectKeyDelegate.getKey(object));
                            if (obj != null) object = obj;
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
                    return;
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
    protected void endElement(String namespaceURI, String sName, String qName) throws SAXException {
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
    protected void characters(char buf[], int offset, int len) throws SAXException {
        if (xmlReader1 != null) {
            xmlReader1.characters(buf, offset, len);
            return;
        }
        if (bWithinTag && value != null) {
            String s = new String(buf, offset, len);
            value += OAString.decodeIllegalXML(s);
        }
    }

    public boolean debug;
    private int holdIndent;
    private String sIndent="";
    void p(String s) {
        if (!debug) return;
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
    protected void startDocument() throws SAXException {
    }

    // SAXParser callback method.
    protected void endDocument() throws SAXException {
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
        param clazz type of object to convert value to
        @return null to skip property.
    */
    public Object convertToObject(String propertyName, String value, Class propertyClass) {
        if (propertyClass == null) return value;
        if (String.class.equals(propertyClass)) return value;

        Object result = OAConverter.convert(conversionClass, value);
        
        return result;
    }

    protected OAObject getRealObject(OAObject object) {
        return object;
    }

    /**
     * Called before creating and loading a new object.
     * @param toClass type of object to create
     * @param hm name/value, where name is uppercase. 
     * @return
     */
    protected OAObject getObject(Class toClass, HashMap<String, Object> hm) {
        return null;
    }
    protected void beforeLoadObject(OAObject obj, HashMap<String, Object> hm) {
    }
    protected void afterLoadObject(OAObject obj, HashMap<String, Object> hm) {
    }
    
    
    /**
        Method that can be overwritten by subclass when an object is completed.
    */
    protected void endObject(OAObject obj, boolean hasParent) {
    }

    // return null to ignore property
    protected String getPropertyName(OAObject obj, String propName) {
        return propName;
    }

    /**
     * This can be overwritten to change/expand a class name
     */
    protected String resolveClassName(String className) {
        return className;
    }
    

    protected DefaultHandler getDefaultHandler() {
        if (myDefaultHandler == null) {
            myDefaultHandler = new MyDefaultHandler();
        }
        return myDefaultHandler;
    }
    
    
    class MyDefaultHandler extends DefaultHandler {
        @Override
        public void endDocument() throws SAXException {
            OAXMLReader.this.endDocument();
        }
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            OAXMLReader.this.startElement(uri, localName, qName, attributes);
        }
        @Override
        public void startDocument() throws SAXException {
            OAXMLReader.this.startDocument();
        }
        @Override
        public void characters(char buf[], int offset, int len) throws SAXException {
            OAXMLReader.this.characters(buf, offset, len);
        }
        @Override
        public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
            OAXMLReader.this.endElement(namespaceURI, sName, qName);;
        }
        
    }
    

    
    public static void main(String[] args) throws Exception {
        OAXMLReader r = new OAXMLReader();
        r.debug = true;
        Object[] objs = r.readFile("C:\\Projects\\java\\OABuilder_git\\models\\tsac.obx");
        
        int xx = 4;
        xx++;
    }

}
