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
package com.viaoa.object;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;


/**
 * Used to perform XML read/write for OAObjects.
 * @author vincevia  2007/10/03
 */
public class OAObjectXMLDelegate {

	private static Logger LOG = Logger.getLogger(OAObjectXMLDelegate.class.getName());

	/**
        Called by OAXMLWriter to save object as xml.  All ONE, MANY2MANY, and MANY w/o reverse getMethod References
        will store reference Ids, using the name of reference property as the tag.<br>
        Note: if a property's value is null, then it will not be included.
        @see #read
    */
    public static void write(final OAObject oaObj, final OAXMLWriter ow, final String tagName, boolean bKeyOnly, final OACascade cascade) {
        write(oaObj, ow, tagName, bKeyOnly, cascade, false);
    }
	public static void write(final OAObject oaObj, final OAXMLWriter ow, String tagName, boolean bKeyOnly, final OACascade cascade, final boolean bWriteClassName) {
        if (oaObj == null || ow == null) return;
        try {
            if (tagName != null) ow.push(tagName);
    	    _write(oaObj, ow, tagName, bKeyOnly, cascade, bWriteClassName);
        }
        finally {
            if (tagName != null) ow.pop();
        }
	}
	
    private static void _write(final OAObject oaObj, final OAXMLWriter ow, String tagName, boolean bKeyOnly, final OACascade cascade, final boolean bWriteClassName) {
	    Class c = oaObj.getClass();
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

	    // 20150909
	    if (!bKeyOnly) {
	        if (ow.willBeIncludedLater(oaObj)) bKeyOnly = true;
	        else if (cascade.wasCascaded(oaObj, true)) bKeyOnly = true;
	    }
	    String attrib = " ";
	    if (bKeyOnly) {
	        attrib += "idref=\"g"+OAObjectDelegate.getGuid(oaObj)+"\""; 
	    }
	    else {
            attrib += "id=\"g"+OAObjectDelegate.getGuid(oaObj)+"\""; 
	    }
	    
	    
        if (bWriteClassName || tagName == null) {
            attrib += " class=\""+ow.getClassName(c)+"\"";
        }

        String[] ids = oi.idProperties;
        if (bKeyOnly) {
            attrib += "/";
            //if (ids == null || ids.length == 0) attrib += "/";
        }
	    
	    if (tagName == null) {
	        tagName = c.getSimpleName();
	    }
	    
        ow.indent();
        ow.println("<"+tagName + attrib + ">");
	    

        ow.writing(oaObj);  // hook to let oaxmlwriter subclass know when objects are being written
        if (bKeyOnly) return;
	    //if (bKeyOnly && (ids == null || ids.length == 0)) return;
	
	    ow.indent++;
	
	    ArrayList alProp = oi.getPropertyInfos();  // reg props, not link props
	    for (int i=0; i < alProp.size(); i++) {
	    	OAPropertyInfo pi = (OAPropertyInfo) alProp.get(i);
	        if (bKeyOnly && !pi.getId()) continue;
	        
	        String propName = pi.getName();
	        Object value = OAObjectReflectDelegate.getProperty(oaObj, propName);
	        if (value == null) continue;
	
	        if (OAConverter.getConverter(value.getClass()) == null && !(value instanceof String)) {
	            if (value instanceof OAObject) {
	                write(((OAObject)value), ow, propName, false, cascade);
	                continue;
	            }
	            Class cval = value.getClass();
	            value = ow.convertToString(propName, value);
	            if (value == null) continue;
	            ow.indent();
	            ow.print("<" + propName + " class=\""+ow.getClassName(cval)+"\">");
	        }
	        else {
	            ow.indent();
	            ow.print("<" + propName + ">");
	        }
	
	        if (value instanceof String) {
	            if (OAString.isLegalXML((String)value)) ow.printXML((String)value);
	            else ow.printCDATA((String)value);
	        }
	        else if (value instanceof OADate) ow.print(((OADate)value).toString("yyyy-MM-dd"));
	        else if (value instanceof OATime) ow.print(((OATime)value).toString("HH:mm:ss"));
	        else if (value instanceof OADateTime) ow.print(((OADateTime)value).toString("yyyy-MM-dd HH:mm:ss"));
	        else {
	            value = OAConv.toString(value);
	            if (OAString.isLegalXML((String)value)) ow.printXML((String)value);
	            else ow.printCDATA((String)value);
	        }
	        ow.println("</" + propName + ">");
	    }
	
	    // Save link properties
	    List alLink = oi.getLinkInfos();
	    for (int i=0;  i<alLink.size(); i++) {
	        OALinkInfo li = (OALinkInfo) alLink.get(i);
	        if (li.getTransient()) continue;
	        if (li.getCalculated()) continue;
	        if (li.getPrivateMethod()) continue;
	
	        // Method m = oi.getPropertyMethod(c, "get"+li.getProperty());
	        // if (m == null) continue;
	        Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.getName());
	        // Object obj = ClassModifier.getPropertyValue(this, m);
	        if (obj == null && !ow.getIncludeNullProperties()) continue;
	
	        if (bKeyOnly && !isObjectKey(li.getName(), ids)) continue;
	
	        int x = ow.shouldWriteProperty(oaObj, li.getName(), obj);
	        if (x != ow.WRITE_NO) {
	            if (obj instanceof OAObject) {
	                boolean b = Modifier.isAbstract(li.getToClass().getModifiers());
	                write(((OAObject)obj), ow, li.getName(), (x == ow.WRITE_KEYONLY), cascade, b);
	            }
	            else if (obj instanceof Hub) {
	                Hub h = (Hub) obj;
	                if (h.getSize() > 0 || ow.getIncludeEmptyHubs()) {
	                    HubXMLDelegate.write(h, ow, li.getName(), x, cascade); // 2006/09/26
	                }
	            }
	        }
	    }
	    if (!bKeyOnly) {
	        String[] propNames = OAObjectPropertyDelegate.getPropertyNames(oaObj);
	        for (int i=0; propNames != null && i<propNames.length; i++) {
	            String key = propNames[i];
	            if (OAObjectInfoDelegate.getLinkInfo(oi, key) != null) continue; 
	            Object value = OAObjectPropertyDelegate.getProperty(oaObj, key, false, true);
	            if (value == null) continue;

	            if (ow.writeProperty(oaObj, key, value) != ow.WRITE_YES) continue;
	
	            Class cval = value.getClass();
	            if (value instanceof String);
	            else if (value instanceof OADate) value = ((OADate)value).toString("yyyy-MM-dd");
	            else if (value instanceof OATime) value = ((OATime)value).toString("HH:mm:ss");
	            else if (value instanceof OADateTime) value = ((OADateTime)value).toString("yyyy-MM-dd HH:mm:ss");
	            else {
	                if (OAConverter.getConverter(value.getClass()) == null && !(value instanceof String)) {
	                    value = ow.convertToString((String)key, value);
	                    if (value == null) continue;
	                }
	                value = OAConv.toString(value);
	            }
	
	            ow.indent();
	            if (cval.equals(String.class)) ow.print("<"+key+">");
	            else ow.print("<"+key+" class=\""+ow.getClassName(cval)+"\">");
	            if (OAString.isLegalXML((String)value)) ow.printXML((String)value);
	            else ow.printCDATA((String)value);
	            ow.println("</"+key+">");
	        }
	    }

        ow.indent--;
        ow.indent();
        ow.println("</"+tagName+">");
	}
	

    private static boolean isObjectKey(String propertyName, String[] propIds) {
        if (propertyName == null || propIds == null) return false;
        for (int i=0; i < propIds.length; i++) {
            if (propertyName.equalsIgnoreCase(propIds[i])) return true;
        }
        return false;
    }

    
    
//qqqqqqqqqqqqqqq these were taken out of OAObjectInfo.java    
    /**
	    used by OAObject to create an XML attributes for an objects Id using OAObjectKey.
	    Ex:  tty.region.id="NW" tty.id="CO" id="12"
	/
	public String createXMLId(OAObjectKey key) {
	    return createXMLId("", key);
	}
	protected String createXMLId(String prefix, OAObjectKey key) {
	    Object[] idValues = key.getObjectIds();
	    String[] idNames = getObjectIdProperties();
	    String result = null;
	    for (int i=0; idNames != null && i < idNames.length; i++) {
	        if (result == null) result = "";
	        else result += " ";
	        if (idValues != null && idValues.length > i) {
	            if (idValues[i] instanceof OAObjectKey) {
	                Class c = getPropertyClass(idNames[i]);
	                if (c != null) {
	                    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
	                    result += oi.createXMLId(prefix+idNames[i]+".", (OAObjectKey) idValues[i]);
	                    continue;
	                }
	            }
	        }
	        result += prefix+idNames[i]+"=\"";
	        result += OAConverter.toString(idValues[i]);
	        result += "\"";
	    }
	    return result;
	}
*/    
}


