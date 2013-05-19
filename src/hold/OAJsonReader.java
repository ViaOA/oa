/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2004 ViaOA, Inc.
All rights reserved.
*/


/*
2003/10/14 objects are created after all object data is loaded.
2003/10/20 call OAObject.setThreadLoading(true) before creating new OAObject
2003/10/21 changed to load properties stored in object attributes (for previous format)
2003/10/22 added bHasParent parameter to endObject() to know if object is a child/reference from another object
2003/10/22 object properties are not loaded until root object is finished
2003/10/25 put setLoading(false) after all objects are created and populated
2003/10/26 set up to call OAObject.getRealObject()
2003/10/30 fixed problem where sub-Hubs not updating stack correctly
2007/05/24 set up internal getRealObject()

2003/10/22 needs to "hold" top level objects so that they are not finalized.
*/

package com.viaoa.util;

import java.util.*;
import java.util.logging.Level;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.ds.*;

/*
qqqqqqqqqqqqqq
qqqqqqqqqqq needs to use JasonParserTool & JasonParser


    MyRoot : {
        MyChild : 'my_child_value',
        MyAnotherChild: 10,
        MyArray : [ 'test', 'test2' ],
        MyArrayRecords : [ 
            {
                ttt : 'vvvv' 
            },
            {
                ttt : 'vvvv2' 
            }                     
        ]
    }


    <MyRoot>
        MyChild : 'my_child_value',
        MyAnotherChild: 10,
        MyArray : [ 'test', 'test2' ],
        MyArrayRecords : [ 
            {
                ttt : 'vvvv' 
            },
            {
                ttt : 'vvvv2' 
            }                     
        ]
    </MyRoot>


[]


*/

/**
    OAJsonReader using a SAXParser to parse and automatically create OAObjects from a JSON file.
    @see OAJsonWriter
*/
public class OAJsonReader extends DefaultHandler {
    private String fileName;
    String value;
    int indent;
    String className;
    int total;
    boolean bWithinTag;
    Object[] stack = new Object[10];
    private String decodeMessage;
    protected boolean bUseRef;
    protected Object refValue;
    protected Object firstObject;
    private Object nullObject = new Object();
    private static final String XML_CLASS = "XML_CLASS";
    private static final String XML_KEYONLY = "XML_KEYONLY";
    private static final String XML_OBJECT = "XML_OBJECT";
    private static final String XML_GUID = "XML_GUID";
    protected Class conversionClass;  // type of class that value needs to be converted to
    protected Vector vecIncomplete, vecRoot;
    protected Hashtable hashGuid;

    // objects that have been removed from a Hub and might not have been saved
    //   these objects will then be checked and saved at the end of the import
    protected Vector vecRemoved = new Vector();

    public OAJsonReader() {
    }
    public OAJsonReader(String fileName) {
        setFileName(fileName);
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getFileName() {
        return this.fileName;
    }


    /**
        Used to parse and create OAObjects from an XML file.
        @return topmost object from XML file.
    */
    public Object read(String fileName) throws Exception {
        setFileName(fileName);
        return read();
    }

    /**
        Used to parse and create OAObjects from an XML file.
        @return topmost object from XML file.
    */
    public Object read() throws Exception {
        return parse(this.fileName);
    }

    /**
        Used to parse and create OAObjects from an XML file.
        @return topmost object from XML file.
    */
    public Object parse() throws Exception {
        Object obj = null;
        obj = parse(fileName);

        int x = vecRemoved.size();
        for (int i=0; i<x; i++) {
            OAObject oa = (OAObject) vecRemoved.elementAt(i);
            if (oa.getNew()) continue; // object was deleted
            if (oa.getChanged()) endObject(oa, false);
        }
        vecRemoved.removeAllElements();

        return obj;
    }

    /**
        Used to parse and create OAObjects from an XML file.
        @return topmost object from XML file.
    */
    public Object parse(String fileName) throws Exception {
        if (fileName == null) throw new IllegalArgumentException("fileName is required");
        
        URI uri = null;
        File f = new File(OAString.convertFileName(fileName));
        if (f.exists()) uri = f.toURI();
        else uri = new URI(fileName);
        
        vecRoot = new Vector();
        vecIncomplete = new Vector();
        setFileName(fileName);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        firstObject = null;
        hashGuid = new Hashtable();
        // saxParser.parse( new File(OAString.convertFileName(fileName)), this );
        saxParser.parse( uri.toString(), this );
        if (firstObject == nullObject) firstObject = null;
        hashGuid = null;
        return firstObject;
    }

    /**
        Returns all root objects from last call to parse.
    */
    public Object[] getRootObjects() {
        int x = vecRoot==null?0:vecRoot.size();
        Object[] objects = new Object[x];
        if (vecRoot != null) vecRoot.copyInto(objects);
        return objects;
    }

    private void replaceRootObject(Object oldValue, Object newValue) {
        if (vecRoot == null) return;
        int pos = vecRoot.indexOf(oldValue);
        if (pos >= 0) vecRoot.set(pos, newValue);
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


    /**
        SAXParser callback method.
    */
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
if (qName.equals("com.viaoa.ds.jdbc.db.DBMetaData")) {
    int qqq = 4;
}
        value = "";
        bWithinTag = true;
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // not namespaceAware

        p(eName);
        indent++;

        if (indent == 1) {
            //qqqq need to verify that this is a valid OAXML file
            // ex:  <OAXML VERSION='1.0' DATETIME='08/12/2003 11:56AM'>
            stack[0] = null; // place holder
            stack[1] = null; // place holder
            return;
        }

        // stack ex:  null | null | Department object | "Employees" | Hub object | Employee | "name" ...

        // stack ex:  null | null | Employee object | "Department" | Department object | "Manager" | Employee ...

        if (stack.length <= indent) {
            Object[] objs = new Object[stack.length + 20];
            System.arraycopy(stack, 0, objs, 0, stack.length);
            stack = objs;
        }
        stack[indent] = eName;


        if (stack[indent-1] == null || (stack[indent-1] instanceof Vector) || (stack[indent-1] instanceof Hub) || (stack[indent-1] instanceof String)) {
            // start of new object/hub
            // whenever startElement() is called and the previous stack element has a String in it,
            //   then the next property is the reference Object/Hub

            Class c;
            try {
                c = Class.forName(resolveClassName(eName));
            }
            catch (Exception e) {
                throw new SAXException("cant find class "+eName+" Error:"+e);
            }

            if (c.equals(Hub.class)) {
int vvv = OAConverter.toInt(attrs.getValue("total"));
if (vvv == 0) {
	vvv++;
}
                startHub(attrs.getValue("ObjectClass"), OAConverter.toInt(attrs.getValue("total")));
                if (indent > 3) {
                    // get Hub from previous object
                    Hashtable hash = (Hashtable) stack[indent-2];
                    Vector vec = (Vector) hash.get(stack[indent-1]); // name of Hub property
                    if (vec == null) {
                        vec = new Vector(43,25);
                        hash.put(stack[indent-1], vec);  // propertyName, vector to hold objects
                    }
                    stack[indent] = vec;  // add objects to this
                }
                else {
                    try {
                        Hub h = new Hub(Class.forName(resolveClassName(attrs.getValue("ObjectClass"))));
                        stack[indent] = h;
                        vecRoot.add(h);
                    }
                    catch (Exception e) {
                        throw new SAXException("Error getting Class for Hub: "+e);
                    }
                }
                if (firstObject == null && stack[indent-1] == null) firstObject = nullObject;
            }
            else {
                // create hashtable to hold values
                Hashtable hash = new Hashtable(23, .75f);
                hash.put(XML_CLASS, c);

                stack[indent] = hash;

                if (attrs != null) {
                    for (int i = 0; i < attrs.getLength(); i++) {
                        String aName = attrs.getLocalName(i); // Attr name
                        if ("".equals(aName)) aName = attrs.getQName(i);
                        String aValue = attrs.getValue(i);
                        if (aName.equalsIgnoreCase("keyonly")) hash.put(XML_KEYONLY, XML_KEYONLY);
                        else if (aName.equalsIgnoreCase("guid")) hash.put(XML_GUID, aValue);
                        else processProperty(aName, aValue, null, hash);
                    }
                }
            }
        }
        else {
            // this needs to check to see if there is a "class" attribute
            conversionClass = null;
            String sclass = attrs.getValue("class");
            if (sclass != null) {
                try {
                    conversionClass = Class.forName(resolveClassName(sclass));
                }
                catch (Exception e) {
                    throw new SAXException("cant create class "+sclass+" Error:"+e);
                }
            }
        }
    }

    /**
        SAXParser callback method.
    */
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        bWithinTag = false;
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // not namespaceAware

        Object stackobj = stack[indent];
        if (stackobj instanceof Hashtable) {
            // ending an object

            /*
                1: create OAObjectKey using propertyId values
                2: call HubController to find object
                3: if not found, create new object
                4: load/update property values
            */

            Hashtable hash = (Hashtable) stackobj;
            boolean bKeyOnly = hash.remove(XML_KEYONLY) != null;
            String guid = (String) hash.remove(XML_GUID);

            Class c = (Class) hash.get(XML_CLASS);
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            String[] ids = oi.getIdProperties();
            Object[] values = new Object[ ids == null ? 0 : ids.length ];
            for (int i=0; i<ids.length; i++) {
                String id = ids[i].toUpperCase();
                Class c2 = OAObjectInfoDelegate.getPropertyClass(c, id);
                values[i] = hash.get(id);
                if (values[i] instanceof String) values[i] = OAConverter.convert(c2, values[i]);
                hash.remove(id);
            }
            OAObjectKey key = new OAObjectKey(values);
            OAObject object = null;
            if (ids != null && ids.length > 0) {
                object = OAObjectCacheDelegate.get(c, key);
            }

            if (object == null && guid != null) {
                object = (OAObject) hashGuid.get(guid);
            }

            if (bKeyOnly) {
                if (stack[indent-1] instanceof Vector) {
                    Vector vec = (Vector) stack[indent-1];
                    if (object != null) vec.addElement(object);
                    else if (guid != null) vec.addElement(XML_GUID+guid);
                    else vec.addElement(key);
                }
                else if (stack[indent-1] instanceof Hub) {
                    Hub h = (Hub) stack[indent-1];
                    if (object != null) h.add(object);
                    else h.add(key);
                }
                else if (indent > 3) {
                    // use this value when updating property
                    bUseRef = true;
                    if (object != null) refValue = object;
                    else if (guid != null) refValue = XML_GUID+guid;
                    else refValue = key;
                }
            }
            else {
                // create object, only load objectId properties
                if (object == null && ids != null && ids.length > 0) {
                    if (object == null) object = (OAObject) OADataSource.getObject(c, key);
                }

                if (object == null) {
                    try {
                    	object = createNewObject(c);
                        // set property ids
                        for (int i=0; ids != null && i<ids.length; i++) {
                            values[i] = getValue(object, ids[i], values[i]);  // hook method for subclass
                            object.setProperty(ids[i], values[i]);
                        }
                    }
                    catch (Exception e) {
                        throw new SAXException("cant create object for class "+c.getName()+" Error:"+e, e);
                    }
                }
                else {
                }

                if (guid != null) hashGuid.put(guid, object);

                boolean bIncomplete = true;
                if (stack[indent-1] == null) bIncomplete = false;
                else if (stack[indent-1] instanceof Hub) bIncomplete = false;

                if (bIncomplete) {
                    hash.put(XML_OBJECT, object);
                    vecIncomplete.addElement(hash);
                }
                else {
                    int x = vecIncomplete.size();
                    for (int i=0; i<x; i++) {
                        Hashtable hashx = (Hashtable) vecIncomplete.elementAt(i);
                        OAObject oaobj = (OAObject) hashx.get(XML_OBJECT);
                        processProperties(oaobj, hashx);
                    }
                    processProperties(object, hash);

                    for (int i=0; i<x; i++) {
                        Hashtable hashx = (Hashtable) vecIncomplete.elementAt(i);
                        OAObject oaobj = (OAObject) hashx.get(XML_OBJECT);
                        endObject(oaobj, true);

                        Object objx = getRealObject(oaobj);
                        if (firstObject == oaobj) {
                            replaceRootObject(firstObject, objx);
                            firstObject = objx;
                        }
                    }
                    endObject(object, false);
                    Object objx = getRealObject(object);
                    if (firstObject == object) {
                        replaceRootObject(firstObject, objx);
                        firstObject = objx;
                    }

                    vecIncomplete.removeAllElements();
                }


                if (stack[indent-1] == null) {
                    vecRoot.add(object);
                    if (firstObject == null) firstObject = object;
                }
                if (stack[indent-1] instanceof Vector) {
                    Vector vec = (Vector) stack[indent-1];
                    vec.addElement(object);
                }
                else if (stack[indent-1] instanceof Hub) {
                    Hub h = (Hub) stack[indent-1];
                    h.add(object);
                }
                else if (indent > 3) {
                    // use this value when updating property
                    bUseRef = true;
                    refValue = object;
                }

            }
        }
        else if (stackobj == null) {
        }
        else if (stackobj instanceof Vector) {
        }
        else if (stackobj instanceof Hub) {  // root level Hub
        }
        else {  // String (Property Name)
            Hashtable hash = (Hashtable)stack[indent-1];
            if (!(hash.get(eName) instanceof Vector)) {
                processProperty(eName, value, conversionClass, hash);
            } // else it was a Hub property
            conversionClass = null;
        }

        indent--;
        p("/"+eName);//qqqqqq
    }

    protected void processProperty(String eName, String value, Class conversionClass, Hashtable hash) {
        Object objValue = value;
        if (bUseRef) {
            bUseRef = false;
            objValue = refValue;
        }
        else {
            if (decodeMessage != null && value != null && value.startsWith(decodeMessage)) {
                objValue = Base64.decode(value.substring(decodeMessage.length()));
            }
            if (conversionClass != null) {
                if (OAConverter.getConverter(conversionClass) == null && !conversionClass.equals(String.class) ) {
                    objValue = convertToObject(conversionClass, (String) objValue);
                }
                else objValue = OAConverter.convert(conversionClass, objValue);
            }
        }

        // p(""+objValue);
        if (objValue != null) hash.put(eName.toUpperCase(), objValue);
    }


    protected void processProperties(OAObject object, Hashtable hash) {
    	boolean bLoadingObject = false;
        if (object.getNew()) {
        	bLoadingObject = true;
        	OAThreadLocalDelegate.setLoadingObject(true);
        	if (OAObjectCSDelegate.isServer()) OAThreadLocalDelegate.setSuppressCSMessages(true);
        	// no, needs to have OAObjectEventDelegate.firePropertyChange() process property changes
        	//   since it has already created the object w/o setLoading(true), which means that there are null primitive properties
        	//     that would not be "unset" if firePropertyChange() was not ran.
        }
        Class c = (Class) hash.get(XML_CLASS);
        hash.remove(XML_CLASS);
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);

        Enumeration enumx = hash.keys();

        for ( ;enumx.hasMoreElements(); ) {
            Object k = enumx.nextElement();
            Object v = hash.get(k);
            if (v == object) continue;

            if (v instanceof Vector) {
                Vector vec = (Vector) v;

                // change guid objects to real objects
                int x = vec.size();
                for (int ix=0; ix < x; ix++) {
                    Object o = vec.elementAt(ix);
                    if (o instanceof String && ((String)o).startsWith(XML_GUID)) {
                        String guid = ((String)o).substring(XML_GUID.length());
                        o = hashGuid.get(guid);
                        if (o == null) System.out.println("Error: could not find object in hashGuid *****");//qqqqqqq
                        else vec.set(ix, o);   // replace
                    }
                }

                // 2006/05/22 was: Hub h = object.getHub((String)k);
                Hub h = (Hub) object.getProperty((String) k); 
if (h == null) {
	if (vec.size() > 0) System.out.println("ERROR in OAXMLReader: Object:"+object+" Property:"+k+"  error:returned null value, should be a Hub");
}
else {
				h.loadAllData();
                // remove objects in Hub that are not in Vector
                for (int i=0; ;i++) {
                    Object obj = h.elementAt(i);
                    if (obj == null) break;
                    if (vec.indexOf(obj) < 0) {
                        h.remove(obj);
                        vecRemoved.addElement(obj);
                        i--;
                    }
                }

                // add objects in Vector that are not in Hub
                x = vec.size();
                for (int ix=0; ix < x; ix++) {
                    Object o = vec.elementAt(ix);
                    if (h.getObject(o) == null) h.add(o);
                    // position objects in Hub to match order of objects in Vector
                    int pos = h.getPos(o);
                    if (pos != ix) h.move(pos, ix);
                }
}                
            }
            else if (OAObjectInfoDelegate.isHubProperty(oi, (String)k)) {
                // empty hub, otherwise "v" would have been a Vector
            }
            else if (v != null && (v instanceof String) && ((String)v).startsWith(XML_GUID)) {
                String guid = ((String)v).substring(XML_GUID.length());
                v = hashGuid.get(guid);
                if (v == null) System.out.println("Error: could not find object in hashGuid *****");//qqqqqqq
                v = getValue(object, (String)k, v);  // hook method for subclass
                object.setProperty((String)k, v);
            }
            else if (v instanceof OAObjectKey) {
                // try to find "real" object
                Class cx = OAObjectInfoDelegate.getPropertyClass(c, (String) k);
                Object o = OAObjectCacheDelegate.get(cx, (OAObjectKey) v);
                if (o != null) v = o;
                v = getValue(object, (String)k, v);  // hook method for subclass
                object.setProperty((String)k, v);
            }
            else {
                if (v instanceof String) {
                    Class cx = OAObjectInfoDelegate.getPropertyClass(c, (String) k);
                    if (cx != null && OAConverter.getConverter(cx) == null && !cx.equals(String.class) ) {
                        v = convertToObject(cx, (String) v);
                    }
                }
                v = getValue(object, (String)k, v);  // hook method for subclass
                object.setProperty((String)k, v);
            }
        }
        if (bLoadingObject) {
            object.afterLoad();
        	OAThreadLocalDelegate.setLoadingObject(false);
        	if (OAObjectCSDelegate.isServer()) OAThreadLocalDelegate.setSuppressCSMessages(false);
        }
    }

    /**
        SAXParser callback method.
    */
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
        return (OAObject) c.newInstance();
    }

    /**
        Method that can be overwritten to provide custom conversion of a String to an Object.<br>
        Called to convert a value from String to an Object when there does not exist an OAConverter for
        the correct Class.
        @param clazz type of object to convert value to
        @return null to skip property.
    */
    public Object convertToObject(Class clazz, String value) {
        return null;
    }

    /** By default, this will check to see if object already exists 
        in OAObjectCache and return that object.  Otherwise this object is returned.
	*/
	protected Object getRealObject(OAObject object) {
		Object obj = OAObjectCacheDelegate.getObject(object.getClass(), OAObjectKeyDelegate.getKey(object));
	    if (obj != null) return obj;
	    return object;
	}

	protected String resolveClassName(String className) {
	    return className;
	}
}
