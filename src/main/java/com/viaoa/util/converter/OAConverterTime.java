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

