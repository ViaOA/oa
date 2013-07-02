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
package com.viaoa.hub;

import java.lang.reflect.*;

import com.viaoa.util.OAConverter;
import com.viaoa.util.OAReflect;

/**
    Used by Hub.findX methods to perform a search for an object based on using
    a property path.
*/
public class HubFinder {
        
    private Method[] methods;

    /** object that is currently being used for find() methods. */
    private  Object findObject;

    /** property path used for find() methods. */
    private  String findPath;
    
    
    public HubFinder(Class baseClass, String propertyPath, Object findObject) {
        this.findPath = propertyPath;
        this.findObject = findObject;
	    setMethods(OAReflect.getMethods(baseClass, propertyPath));
    }
    
    void setMethods(Method[] ms) {
        methods = ms;
    }

    Method[] getMethods() {
        return methods;
    }
    Object getFindObject() {
        return findObject;
    }


    /**
	    Used to compare objects with find value.
	    If values are Strings, then a case-insensitive compare is used.
	*/
	public boolean isEqual(Object oaObject) {
	    if (methods == null) return false; 
	    Object value = oaObject;
	    if (methods.length != 0) {
	        value = OAReflect.getPropertyValue(oaObject, methods);
	    }
	
	    if (value == findObject) return true;
	    if (value == null || findObject == null) return false;

	    if (value.equals(findObject)) return true;

	    Class c = findObject.getClass();
        if (!c.equals(value.getClass())) {
            value = OAConverter.convert(c, value);
            if (value == null) return false;
            
            if (value.equals(findObject)) return true;
        }
	
	    if (value instanceof String) { // ignore case
	        return ((String)value).equalsIgnoreCase((String)findObject);
	    }
	    return false;
	}
	
    
    
}

