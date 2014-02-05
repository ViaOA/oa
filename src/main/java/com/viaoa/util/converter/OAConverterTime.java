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
import java.sql.*;

/**
    Convert to/from a Time value.
    <br>
    <ul><b>Converting the following to a Date</b>
    <li>String, using optional format for parsing.
    <li>OADateTime
    <li>Date
    <li>All others value will return null.
    </ul>
    <br>
    <ul><b>Converts a Time to any of the following</b>
    <li>String, using an optional format.
    </ul>
    
    @see OAConverter
    @see OADateTime
    @see OATime
*/
public class OAConverterTime implements OAConverterInterface {

    /**
        Convert to/from a Time value.
        @param clazz Class to convert to.
        @param value to convert
        @param fmt format string 
        @see OADateTime
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Time.class)) return convertToTime(value, fmt);
        if (value != null && value instanceof Time) return convertFromTime(clazz, (Time) value, fmt);
        return null;
    }
    
    protected Time convertToTime(Object value, String fmt) {
        if (value == null) return null;
        if (value instanceof Time) return (Time) value;

        if (value instanceof String) {
            value = OADateTime.valueOf((String) value, fmt);
        }

        if (value instanceof OADateTime) {
            return new Time(((OADateTime)value).getDate().getTime());
        }

        if (value instanceof java.util.Date) {
            return new Time(((java.util.Date)value).getTime());
        }
        if (value instanceof byte[]) {
        	return new Time(new java.math.BigInteger((byte[]) value).longValue());
        }
        
        return null;
    }

    protected Object convertFromTime(Class toClass, Time timeValue, String fmt) {
        if (toClass.equals(String.class)) {
            if (timeValue == null) return null;
            OATime od = new OATime(timeValue);
            return od.toString(fmt);
        }
        return null;
    }
}

