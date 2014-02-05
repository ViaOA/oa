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
    Convert a value to/from a Character value.
    <br>
    <ul><b>Converts the following to a Character</b>
    <li>If a String and one char in length, then the first char in the String.
    <li>If a boolean, then it will be converted to either a 'T' or 'F'.
    <li>If numeric, and value is within the MIN and MAX values of a Character, then the intValue.
    <li>Otherwise, null is returned.
    </ul>
    <br>
    <ul><b>Converts a Character to any of the following</b>
    <li>String, single character. ex: 'T' = "T"
    </ul>

    @see OAConverter
*/
public class OAConverterCharacter implements OAConverterInterface {

    /**
        Convert a value to/from a Character value.
        @parma value is object to convert.<br>
        @param clazz is Character.Class if converting a value to a Character or the Class to convert a Character to.
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Character.class) || clazz.equals(char.class)) return convertToCharacter(value);
        if (value != null && value instanceof Character) return convertFromCharacter(clazz, (Character) value);
        return null;
    }
        
    protected Character convertToCharacter(Object value) {
        if (value instanceof Character) return (Character) value;
        if (value instanceof String) {
            String str = (String)value;
            if (str.length() == 1) return new Character(str.charAt(0));
            return null;
        }
        
        if (value instanceof Number) {
            int x = ((Number) value).intValue();
            if (x >= Character.MIN_VALUE && x <= Character.MAX_VALUE) return new Character((char)x);
            return null;
        }
        if (value instanceof Boolean) {
            return new Character( ((Boolean)value).booleanValue() ? 'T' : 'F' );
        }
        return null;
    }


    protected Object convertFromCharacter(Class toClass, Character charValue) {
        if (toClass.equals(String.class)) {
            return charValue.toString();
        }
        return null;
    }


}
