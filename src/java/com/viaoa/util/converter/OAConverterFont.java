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

import java.awt.Font;
import com.viaoa.util.*;

/**
    Convert to/from a Font value.

    @see OAConverter
*/
public class OAConverterFont implements OAConverterInterface {

    /**
        Convert to/from a Font value.  Uses Font.decode() to convert from a String.  
        @param clazz is Font.class
        @param value object to convert. <br>
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Font.class)) return convertToFont(value);
        if (value != null && value instanceof Font) return convertFromFont(clazz, (Font) value);
        return null;
    }
    
    protected Font convertToFont(Object value) {
        if (value instanceof Font) return (Font) value;

        if (value instanceof String) {
            String sValue = (String) value;
            Font font = Font.decode(sValue);
        }
        return null;
    }

    protected Object convertFromFont(Class toClass, Font font) {
        if (font != null && toClass.equals(String.class)) {
        	return font.toString();
        }
        return null;
    }

}
