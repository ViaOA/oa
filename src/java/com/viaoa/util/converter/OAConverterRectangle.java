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
