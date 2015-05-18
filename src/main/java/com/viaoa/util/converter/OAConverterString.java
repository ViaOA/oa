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













