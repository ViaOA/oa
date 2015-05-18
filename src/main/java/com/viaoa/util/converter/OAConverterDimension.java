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

import java.awt.*;


/**
    Convert to/from a Rectangle value.
    <br>
    <ul><b>Converts the following to a  Rectangle</b>
    <li>String.  ex: "x,y,w,h"
    <li>Number, by encoding in 16bit positions.
    <li>All others value will return null.
    </ul>
    <br>
    <ul><b>Converts a Rectangle to any of the following Classes</b>
    <li>String, using a comma separated list.  Ex: "x,y,w,h"
    </ul>

    @see OAConverter
*/
public class OAConverterDimension implements OAConverterInterface {

    /**
        Convert to/from a Dimension value.
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Dimension.class)) return convertToDimension(value);
        if (value != null && value instanceof Dimension) return convertFromDimension(clazz, (Dimension) value);
        return null;
    }
    
    protected Dimension convertToDimension(Object value) {
        if (value == null) return null;
        if (value instanceof Dimension) return (Dimension) value;
        if (value instanceof Number) {
            long l = ((Number)value).longValue();
            int w = (int) ((l >>> 16) & 0xFFFF);
            int h = (int) (l & 0xFFFF);

            Dimension d = new Dimension(w,h);
            return d;
        }
        if (value instanceof String) {
            String svalue = (String) value;
            Dimension d = new Dimension();
            try {
                d.width = Integer.parseInt(OAString.field(svalue,",", 1));
                d.height = Integer.parseInt(OAString.field(svalue,",", 2));
            }
            catch (Exception e) {
            }
            return d;
        }
        return null;
    }

    protected Object convertFromDimension(Class toClass, Dimension d) {
        if (toClass.equals(String.class)) {
            return d.width+","+ d.height;
        }
        return null;
    }
}
