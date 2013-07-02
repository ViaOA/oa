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

public class OAConverterEnum implements OAConverterInterface {
    
    public Object convert(Class clazz, Object value, String fmt) {
        if (value == null || clazz == null) return null;
        if (value != null && value.getClass().equals(clazz)) return value;
        
        if (clazz.isEnum()) {
            Object[] enums = clazz.getEnumConstants();
            for (Object obj : enums) {
                Enum e = (Enum) obj;
                String s = e.toString();
                if (s != null && value instanceof String && s.equalsIgnoreCase((String)value)) return e;
                else {
                    int x = e.ordinal();
                    if (value.equals(x)) return e;
                }
            }
        }
        else {
            if ((value instanceof Enum) && clazz.equals(String.class)) {
                return ((Enum)value).name();
            }
        }
        
        
        return null;
    }
    

}
