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

import java.util.*;

/**
*/
public class OAConverterTimeZone implements OAConverterInterface {

    private static HashMap<String, TimeZone> hmTz = new HashMap<String, TimeZone>();
    static {
        String[] ss = TimeZone.getAvailableIDs();
        for (String s : ss) {
            TimeZone tz = TimeZone.getTimeZone(s);
            hmTz.put(s.toUpperCase(), tz);
        }
    }
    
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(TimeZone.class)) return convertToTimeZone(value, fmt);
        if (value != null && value instanceof TimeZone) return convertFromTimeZone(clazz, (TimeZone) value, fmt);
        return null;
    }

    protected TimeZone convertToTimeZone(Object value, String fmt) {
        if (value instanceof TimeZone) return (TimeZone) value;
        if (value == null) return null;
        TimeZone tz = null;
        if (value instanceof String) {
            tz = hmTz.get( ((String)value).toUpperCase());
        }
        return tz;
    }

    protected Object convertFromTimeZone(Class toClass, TimeZone tz, String fmt) {
        if (toClass.equals(String.class)) {
            return tz.getDisplayName();
        }
        return null;
    }

}

