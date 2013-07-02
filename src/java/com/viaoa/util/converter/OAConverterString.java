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
package com.viaoa.util.converter;

import java.sql.*;

import com.viaoa.util.*;

/**
 * Converts a null to a blank "", or will use the third value in fmt (seperated by ;)
 * 
*/
public class OAConverterString implements OAConverterInterface {

    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz != null && clazz.equals(String.class)) return convertToString(value, fmt);
        return null;
    }        

    protected String convertToString(Object value, String fmt) {
    	// convert a value to a string.  Use the converter for value.getClass() to do this.
        if (value == null) {
            if (fmt != null) value = OAString.field(fmt,";",3);
            if (value == null) value = "";
        	return (String) value;
        }
        if (value instanceof String) {
            if (fmt != null) value = OAString.fmt((String)value, fmt);
            return (String) value;
        }
        
        
        // this will use indirection to have value converted to a String
        OAConverterInterface conv = OAConverter.getConverter(value.getClass());
        if (conv != null) { 
            Object obj = conv.convert(String.class, value, fmt);
            if (obj instanceof String) return (String) obj;
        }
        
        // other possiblities not covered by other OAConverters

        if (value instanceof Blob) {
        	try {
        		Blob blob = (Blob) value;
        		return new String(blob.getBytes(0, (int) blob.length()));
        	}
        	catch (Exception e) {
        		throw new RuntimeException(e);
        	}
        }        
        
        if (value instanceof byte[]) return new String((byte[]) value);
        if (value instanceof char[]) return new String((char[]) value);

        if (value instanceof Clob) {
        	try {
        		Clob clob = (Clob) value;
        		return clob.getSubString(1, (int) clob.length());
        	}
        	catch (Exception e) {
        		throw new RuntimeException(e);
        	}
        }
        value = value.toString();
        if (fmt != null) value = OAString.fmt((String)value, fmt);
        return (String) value;
    }

}













