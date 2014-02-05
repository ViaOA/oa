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

import com.viaoa.util.*;

import java.util.*;
import java.sql.Time;

/**
    Convert to/from a OADate value.
    <br>
    <ul><b>Converting the following to an OADate</b>
    <li>String, using optional format string.    
    <li>Time
    <li>Date
    <li>OADateTime
    <li>All others value will return null.
    </ul>
    <br>
    <ul><b>Converting an OADate to any of the following</b>
    <li>String, using an optional format.
    </ul>

    See OADateTime for format definitions.
    @see OAConverter
    @see OADateTime
    @see OADate
*/
public class OAConverterOADate implements OAConverterInterface {

    /**
        Convert to/from a OADate value.
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(OADate.class)) return convertToOADate(value, fmt);
        if (value != null && value instanceof OADate) return convertFromOADate(clazz, (OADate) value, fmt);
        return null;
    }

    protected OADate convertToOADate(Object value, String fmt) {
        if (value instanceof OADate) return (OADate) value;
        if (value == null) return null;
        OADate d = null;
        if (value instanceof String) {
            d = (OADate) OADate.valueOf((String)value, fmt);
        }
        else if (value instanceof Time) {
            d = new OADate((Time)value);
        }
        else if (value instanceof Date) {
            d = new OADate((Date) value);
        }
        else if (value instanceof OADateTime) {
            d = new OADate((OADateTime)value);
        }
        else if (value instanceof byte[]) {
        	d = new OADate(new java.math.BigInteger((byte[]) value).longValue());
        }
        else if (value instanceof Number) {
        	d = new OADate(((Number)value).longValue());
        }
        
        
        if (d != null) {
            if (d.getYear() > 9999) d = null;  // Access will not allow dates where year is > 4 digits
        }
        return d;
    }

    protected Object convertFromOADate(Class toClass, OADate dateValue, String fmt) {
        if (toClass.equals(String.class)) {
            return (dateValue).toString(fmt);
        }
//qqqqqqqq Date, long, etc.        
        return null;
    }

}

