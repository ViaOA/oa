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
package com.viaoa.object;

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
	public static void write(OAObject oaObj, OAXMLWriter ow, boolean bKeyOnly, OACascade cascade) {
	    if (oaObj == null || ow == null) return;
	    Class c = oaObj.getClass();
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

        if (cascade.wasCascaded(oaObj, true)) bKeyOnly = true;
	    
	    String[] ids = oi.idProperties;
	    ow.indent();
	    String s = "";
	    if (ids == null || ids.length == 0) {
	        s = " guid=\""+OAObjectDelegate.getGuid(oaObj)+"\"";  // objects w/o ObjectId property
	        if (bKeyOnly) s += "/";
	    }
	
        ow.println("<"+ow.getClassName(c)+ (bKeyOnly?" keyonly=\"true\"":"") + s + ">");
	    ow.writing(oaObj);  // hook to let oaxmlwriter subclass know when objects are being written
	    if (bKeyOnly && (ids == null || ids.length == 0)) return;
	
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
	                ow.indent();
	                ow.println("<" + propName + ">");
	                ow.indent++;
	                write(((OAObject)value), ow, false, cascade);
	                ow.indent--;
	                ow.indent();
	                ow.println("</" + propName + ">");
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
	                ow.indent();
	                ow.println("<"+li.getName()+">");
	                ow.indent++;
	                write(((OAObject)obj), ow, (x == ow.WRITE_KEYONLY), cascade);
	                ow.indent--;
	                ow.indent();
	                ow.println("</"+li.getName()+">");
	            }
	            else if (obj instanceof Hub) {
	                Hub h = (Hub) obj;
	                if (h.getSize() > 0 || ow.getIncludeEmptyHubs()) {
	                    ow.indent();
	                    ow.println("<"+li.getName()+">");
	                    ow.indent++;
	                    HubXMLDelegate.write(h, ow, x, cascade); // 2006/09/26
	                    // was: h.write(cascade, ow, (x == ow.WRITE_KEYONLY)); 
	                    ow.indent--;
	                    ow.indent();
	                    ow.println("</"+li.getName()+">");
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
	
	    //if (!bKeyOnly) {
	        ow.indent--;
	        ow.indent();
	        ow.println("</"+ow.getClassName(c)+">");
	    //}
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


