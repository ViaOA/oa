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
 * Used to perform JSON read/write for OAObjects.
 * @author vincevia  2012/01/20
 */
public class OAObjectJsonDelegate {

	private static Logger LOG = Logger.getLogger(OAObjectJsonDelegate.class.getName());
	
    /**
	    Called by OAJsonWriter to save object as JSON.  All ONE, MANY2MANY, and MANY w/o reverse getMethod References
	    will store reference Ids, using the name of reference property as the tag.<br>
	    Note: if a property's value is null, then it will not be included.
	    @see #read
	*/
    public static void write(OAObject oaObj, OAJsonWriter ow, boolean bKeyOnly, OACascade cascade) {
        ow.println("{");
        ow.indent++;
        //ow.indent();
        _write(oaObj, ow, bKeyOnly, cascade);
        ow.println("");
        ow.indent--;
        ow.indent();
        ow.print("}");
    }
	private static void _write(OAObject oaObj, OAJsonWriter ow, boolean bKeyOnly, OACascade cascade) {
	    if (oaObj == null || ow == null) return;
	    Class c = oaObj.getClass();
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	
	    if (cascade.wasCascaded(oaObj, true)) bKeyOnly = true;

        String s = "\"guid\": \""+OAObjectDelegate.getGuid(oaObj)+"\"";
        // ow.print(s);
        ow.writing(oaObj);  // hook to let oaJsonwriter subclass know when objects are being written
	    
        int writeCnt = 0;
	    ArrayList alProp = oi.getPropertyInfos();  // regular props, not link props
	    for (int i=0; i < alProp.size(); i++) {
	    	OAPropertyInfo pi = (OAPropertyInfo) alProp.get(i);
	        if (bKeyOnly && !pi.getId()) continue;
	        
	        String propName = OAString.mfcl(pi.getName());
	        Object value = OAObjectReflectDelegate.getProperty(oaObj, propName);
	        // if (value == null) continue;
	
            boolean x = ow.shouldIncludeProperty(oaObj, propName, value, null);
            if (!x) continue;
	        
	        
	        if (value != null && OAConverter.getConverter(value.getClass()) == null && !(value instanceof String)) {
	            if (value instanceof OAObject) {
	                if (writeCnt++ > 0) ow.println(", ");
	                ow.indent();
	                ow.print("\"" + propName + "\": ");
	                write(((OAObject)value), ow, false, cascade);
	                continue;
	            }
	            value = ow.convertToString(propName, value);
	            // if (value == null) continue;

	            if (writeCnt++ > 0) ow.println(", "); 
                ow.indent();
	            if (value == null) ow.print("\"" + propName + "\": null");
	            else ow.print("\"" + propName + "\": \""+value+"\"");
	        }
	        else {
	            if (writeCnt++ > 0) ow.println(", "); 
                ow.indent();
	            ow.print("\"" + propName + "\": ");
	        }
	        
	        if (value == null) {
                ow.print("null"); 
	        }
	        else if (value instanceof String) {
	            value = OAString.convert((String)value, "\"", "\\\"");
                ow.print("\""); 
                ow.printJson((String)value);
                ow.print("\""); 
	        }
	        else if (value instanceof OADate) ow.print("\""+((OADate)value).toString("yyyy-MM-dd")+"\"");
	        else if (value instanceof OATime) ow.print("\""+((OATime)value).toString("HH:mm:ss")+"\"");
	        else if (value instanceof OADateTime) ow.print("\""+((OADateTime)value).toString("yyyy-MM-dd'T'HH:mm:ss")+"\"");
	        else {
	            value = OAConv.toString(value);
                value = OAString.convert((String)value, "\"", "\\\"");
                ow.print("\""); 
                ow.printJson((String)value);
                ow.print("\""); 
	        }
	    }
	    if (bKeyOnly) return;
	    
	    // Save link properties
	    ArrayList alLink = oi.getLinkInfos();
	    for (int i=0; i<alLink.size(); i++) {
	        OALinkInfo li = (OALinkInfo) alLink.get(i);
	        if (li.getTransient()) continue;
            // if (li.getCalculated()) continue;
	
	        // Method m = oi.getPropertyMethod(c, "get"+li.getProperty());
	        // if (m == null) continue;
	        Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.getName());
	        // Object obj = ClassModifier.getPropertyValue(this, m);
	        if (obj == null) continue;
	
    	        boolean x = ow.shouldIncludeProperty(oaObj, li.getName(), obj, li);
    	        
    	        if (x) {
    	            try {
                        ow.pushReference(li.getName());
        	            if (obj instanceof OAObject) {
        	                if (writeCnt++ > 0) ow.println(", ");
                            ow.indent();
        	                ow.println("\""+OAString.mfcl(li.getName())+"\": ");
        	                ow.indent++;
        	                ow.indent();
        	                write(((OAObject)obj), ow, false, cascade);
                            ow.indent--;
        	            }
        	            else if (obj instanceof Hub) {
        	                Hub h = (Hub) obj;
        	                if (writeCnt++ > 0) ow.println(", "); 
                            ow.indent();
                            ow.println("\""+OAString.mfcl(li.getName())+"\": ");
                            ow.indent();
                            HubJsonDelegate.write(h, ow, cascade);
        	            }
    	            }
    	            finally {
                        ow.popReference();
    	            }
    	        }
	    }
	}
	

    private static boolean isObjectKey(String propertyName, String[] propIds) {
        if (propertyName == null || propIds == null) return false;
        for (int i=0; i < propIds.length; i++) {
            if (propertyName.equalsIgnoreCase(propIds[i])) return true;
        }
        return false;
    }

}


