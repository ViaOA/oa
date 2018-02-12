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
    Convert to/from a Point value.
    <br>
    <b>Converts the following to a  Point</b>
    <ul>
    </ul>
    <br>
    <b>Converts a Point to any of the following Classes</b>
    <ul>
    <li>String, using a comma separated list.  Ex: "x,y"
    </ul>

    @see OAConverter
*/
public class OAConverterPoint implements OAConverterInterface {

    /**
        Convert to/from a Rectangle value.
        @return Object of type clazz if conversion can be done, else null.
    */
    public Object convert(Class clazz, Object value, String fmt) {
        if (clazz == null) return null;
        if (clazz.equals(Point.class)) return convertToPoint(value);
        if (value != null && value instanceof Point) return convertFromPoint(clazz, (Point) value);
        return null;
    }
    
    protected Point convertToPoint(Object value) {
        if (value == null) return null;
        if (value instanceof Point) return (Point) value;
        if (value instanceof Number) {
            long l = ((Number)value).longValue();
            int x = (int) ((l >>> 16) & 0xFFFF);
            int y = (int) (l & 0xFFFF);

            Point pt = new Point(x,y);
            return pt;
        }
        if (value instanceof String) {
            String svalue = (String) value;
            Point pt = new Point();
            try {
            	pt.x = Integer.parseInt(OAString.field(svalue,",", 1));
                pt.y = Integer.parseInt(OAString.field(svalue,",", 2));
            }
            catch (Exception e) {
            }
            return pt;
        }
        return null;
    }

    protected Object convertFromPoint(Class toClass, Point pt) {
        if (toClass.equals(String.class)) {
            return pt.x+","+ pt.y;
        }
        return null;
    }
}
