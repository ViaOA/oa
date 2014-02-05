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

import java.awt.*;


/**
    Convert to/from a Point value.
    <br>
    <ul><b>Converts the following to a  Point</b>
    </ul>
    <br>
    <ul><b>Converts a Point to any of the following Classes</b>
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
