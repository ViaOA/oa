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

/**
    Convert to/from a Boolean value.
    <br>
    <ul><b>Converts the following to a Boolean</b>
    <li>String: 
        if fmt is not null, then compares with true, false format values (case insensitive).  
        If none match then null is returned.
        If fmt is null then returns true if value equals "true", "yes", "t", "y" (all case insensitive).  
        Otherwise false.
    <li>Number: true if value != 0.  Otherwise false.
    <li>Character: true if 't', 'y', isDigit() and not '0' (case insensitive).  Otherwise false.
    <li>All others value will return null.
    </ul>
    <br>
    <ul><b>Converts a Boolean to any of the following</b>
    <li>String, using an optional format.
    </ul>

    @see OAConverter
*/
public class OAConverterBoolean implements OAConverterInterface {

    /**
        Convert to/from a Boolean value.
        @param clazz Class to convert to.  
        @param value if converting to boolean, then any type.  If converting from boolean, then boolean value or null.
        @param fmt format string to determine values for true, false, null.  Ex: "true;false;null", "yes;no;maybe"
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) return convertToBoolean(value, fmt);
        if (value == null || value instanceof Boolean) return convertFromBoolean(clazz, (Boolean) value, fmt);
        return null;
    }        

    private static final Boolean bFalse = new Boolean(false);
    protected Boolean convertToBoolean(Object value, String fmt) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return bFalse;
        }
        
        boolean b = false;
        if (value instanceof String) {
            String str = (String)value;
            if (fmt != null && fmt.length() > 0) {
                String s = OAString.field(fmt,";",1);
                b = (s.equalsIgnoreCase(str));
                if (!b) {
                    s = OAString.field(fmt,";",2);
                    b = (s.equalsIgnoreCase(str));
                    if (!b) return null;
                    b = false;
                }
            }
            else {
                if (str.length() == 1) {
                    char c = str.charAt(0);
                    if (c == 'F' || c == 'f' || c == 'N' || c == 'n' || (Character.isDigit(c) && c == '0')) b = false;
                    else b = true;
                }
                else {
                    if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("no")) b = false;
                    else b = (str.length() > 0);
                }
            }
            return new Boolean(b);
        }
            
        if (value instanceof Number) {
            return new Boolean(((Number) value).doubleValue() != 0.0);
        }
        char c = 0;
        b = false;
        if (value instanceof Byte) {
             c = (char) ((Byte)value).byteValue();
             b = true;
        }            
        if (value instanceof Character) {
            c = ((Character)value).charValue();
            b = true;
        }
        if (b) {
            if (c == 'T' || c == 't' || c == 'Y' || c == 'y' || (Character.isDigit(c) && c != '0')) b = true;
            return new Boolean(b);
        }
        return (value != null);
    }


    protected Object convertFromBoolean(Class toClass, Boolean bValue, String fmt) {
        if (toClass.equals(String.class)) {
            // fmt is three values to use for true/false/null sep by ';'  ex: "yes;no;none"
            if (fmt != null) {
                if (bValue == null) return OAString.field(fmt,";",3);
                if ( bValue.booleanValue() ) return OAString.field(fmt,";",1);
                return OAString.field(fmt,";",2);
            }
            if (bValue == null) bValue = new Boolean(false);
            return bValue.toString();
        }
        return null;
    }

}

