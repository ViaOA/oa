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
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.util.converter;

import com.viaoa.util.*;

import java.util.*;


/**
    Convert to/from a OATime value.
    <br>
    <ul><b>Converts the following to an OATime</b>
    <li>String, using optional format string.    
    <li>Time
    <li>Date
    <li>OADateTime
    <li>All others value will return null.
    </ul>
    <br>
    <ul><b>Converts an OATime to any of the following</b>
    <li>String, using an optional format.
    </ul>

    See OADateTime for format definitions.
    @see OAConverter
    @see OADateTime
    @see OATime
*/
public class OAConverterOATime implements OAConverterInterface {

    /**
        Convert to/from a OATime value.
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(OATime.class)) return convertToOATime(value, fmt);
        if (value != null && value instanceof OATime) return convertFromOATime(clazz, (OATime) value, fmt);
        return null;
    }
    
    protected OATime convertToOATime(Object value, String fmt) {
        if (value == null) return null;
        if (value instanceof OATime) return (OATime) value;
        if (value instanceof String) {
            return (OATime) OATime.valueOf((String)value, fmt);
        }
        if (value instanceof Date) {
            return new OATime((Date) value);
        }
        if (value instanceof OADateTime) {
            return new OATime((OADateTime) value);
        }
        if (value instanceof byte[]) {
        	return new OATime(new java.math.BigInteger((byte[]) value).longValue());
        }
        if (value instanceof Number) {
        	return new OATime(((Number)value).longValue());
        }
        return null;
    }

    protected Object convertFromOATime(Class toClass, OATime timeValue, String fmt) {
        if (toClass.equals(String.class)) {
            return (timeValue).toString(fmt);
        }
        return null;
    }

}
