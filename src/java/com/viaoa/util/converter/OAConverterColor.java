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

import java.awt.Color;

/**
    Convert to/from a Color value.
    <br>
    <ul><b>Converts the following to a Color</b>
    <li>String: name of a color (see Color, case insensitive) or be able to be converted by using Color.decode    
    <li>Number or Character: Color for that value will be returned.
    <li>All others value will return null.
    </ul>
    <br>
    <ul><b>Converts a Color to any of the following</b>
    <li>String, using a Hex value.  ex: "#FF88DB"
    </ul>

    @see OAConverter
*/
public class OAConverterColor implements OAConverterInterface {

    /**
        Convert to/from a Color value.
        @param clazz is Color.class
        @param value object to convert. <br>
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Color.class)) return convertToColor(value);
        if (value != null && value instanceof Color) return convertFromColor(clazz, (Color) value);
        return null;
    }
    
    protected Color convertToColor(Object value) {
        if (value instanceof Color) return (Color) value;
        if (value == null) return null;

        if (value instanceof Number) {
            return new Color( ((Number) value).intValue());
        }

        if (value instanceof Character) {
            return new Color( (int) (((Character) value).charValue()) );
        }

        if (value instanceof String) {
            String sValue = (String) value;
            // Strip off whitespace
            sValue = sValue.trim();

            if (sValue.equalsIgnoreCase("black")) return Color.black;
            if (sValue.equalsIgnoreCase("blue")) return Color.blue;
            if (sValue.equalsIgnoreCase("cyan")) return Color.cyan;
            if (sValue.equalsIgnoreCase("darkGray")) return Color.darkGray;
            if (sValue.equalsIgnoreCase("gray")) return Color.gray;
            if (sValue.equalsIgnoreCase("lightGray")) return Color.lightGray;
            if (sValue.equalsIgnoreCase("magenta")) return Color.magenta;
            if (sValue.equalsIgnoreCase("orange")) return Color.orange;
            if (sValue.equalsIgnoreCase("pink")) return Color.pink;
            if (sValue.equalsIgnoreCase("red")) return Color.red;
            if (sValue.equalsIgnoreCase("white")) return Color.white;
            if (sValue.equalsIgnoreCase("yellow")) return Color.yellow;
            else {
                try {
                    Color c = Color.decode(sValue);
                    return c;
                }
                catch (Exception e) {
                }
            }
            
            // rgb(r, g, b)
            String s = sValue.trim();
            s = s.toLowerCase();
            if (sValue.startsWith("rgb(") && sValue.endsWith(")")) {
                s = s.substring(4, s.length()-1);
                s = OAString.convert(s, " ", "");
                String[] ss = s.split(",");
                try {
                    int r = Integer.valueOf(ss[0]);
                    int g = Integer.valueOf(ss[1]);
                    int b = Integer.valueOf(ss[2]);
                    Color c = new Color(r, g, b);
                    return c;
                }
                catch (Exception e) {
                }
            }
            
        }

        if (value instanceof byte[]) {
			return new Color(new java.math.BigInteger((byte[]) value).intValue());
    	}
        
        return null;
    }

    protected Object convertFromColor(Class toClass, Color color) {
        if (toClass.equals(String.class)) {
            return OAString.colorToHex(color).toUpperCase();
        }
        if (Number.class.isAssignableFrom(toClass)) {
        	return new Integer(color.getRGB());
        }
        return null;
    }

}
