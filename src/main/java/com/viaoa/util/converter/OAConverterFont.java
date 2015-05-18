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
