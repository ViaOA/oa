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
    <b>Converts the following to a  Rectangle</b>
    <ul>
    <li>String.  ex: "x,y,w,h"
    <li>Number, by encoding in 16bit positions.
    <li>All others value will return null.
    </ul>
    <br>
    <b>Converts a Rectangle to any of the following Classes</b>
    <ul>
    <li>String, using a comma separated list.  Ex: "x,y,w,h"
    </ul>

    @see OAConverter
*/
public class OAConverterRectangle implements OAConverterInterface {

    /**
        Convert to/from a Rectangle value.
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Rectangle.class)) return convertToRectangle(value);
        if (value != null && value instanceof Rectangle) return convertFromRectangle(clazz, (Rectangle) value);
        return null;
    }
    
    protected Rectangle convertToRectangle(Object value) {
        if (value == null) return null;
        if (value instanceof Rectangle) return (Rectangle) value;
        if (value instanceof Number) {
            long l = ((Number)value).longValue();
            int x = (int) ((l >>> 48) & 0xFFFF);
            int y = (int) ((l >>> 32) & 0xFFFF);
            int w = (int) ((l >>> 16) & 0xFFFF);
            int h = (int) (l & 0xFFFF);

            Rectangle r = new Rectangle(x,y,w,h);
            return r;
        }
        if (value instanceof String) {
            String svalue = (String) value;
            Rectangle r = new Rectangle();
            try {
                r.x = Integer.parseInt(OAString.field(svalue,",", 1));
                r.y = Integer.parseInt(OAString.field(svalue,",", 2));
                r.width = Integer.parseInt(OAString.field(svalue,",", 3));
                r.height = Integer.parseInt(OAString.field(svalue,",", 4));
            }
            catch (Exception e) {
            }
            return r;
        }
        return null;
    }

    protected Object convertFromRectangle(Class toClass, Rectangle rect) {
        if (toClass.equals(String.class)) {
            return rect.x+","+ rect.y+","+rect.width+","+rect.height;
        }
        return null;
    }
}
