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

import com.viaoa.util.*;

import java.util.*;
import java.sql.*;

import com.viaoa.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
    Convert to/from a Timestamp value.
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
public class OAConverterTimestamp implements OAConverterInterface {
    // !!!!! REMEMBER:  date.month values are 0-11


    
    
    /**
        Convert to/from a Time value.
        @param clazz Class to convert to.
        @param value to convert
        @param fmt format string 
        @return Object of type clazz if conversion can be done, else null.
        @see OADateTime
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Timestamp.class)) return convertToTimestamp(value, fmt);
        if (value != null && value instanceof Timestamp) return convertFromTimestamp(clazz, (Timestamp) value, fmt);
        return null;
    }
    
    protected Timestamp convertToTimestamp(Object value, String fmt) {
        if (value == null) return null;
        if (value instanceof Timestamp) return (Timestamp) value;

        if (value instanceof String) {
            OADateTime d = (OADateTime) OADateTime.valueOf((String)value, fmt);
            if (d == null) return null;
            return new java.sql.Timestamp(d.getDate().getTime());
        }

        if (value instanceof OADateTime) {
            return new Timestamp(((OADateTime)value).getDate().getTime());
        }

        if (value instanceof java.util.Date) {
            return new Timestamp(((java.util.Date)value).getTime());
        }

        if (value instanceof byte[]) {
        	return new Timestamp(new java.math.BigInteger((byte[]) value).longValue());
        }
        
        return null;
    }

    protected Object convertFromTimestamp(Class toClass, Timestamp tsValue, String fmt) {
        if (toClass.equals(String.class)) {
            if (tsValue == null) return null;
            OADateTime od = new OADateTime(tsValue);
            return od.toString(fmt);
        }
        return null;
    }

}

