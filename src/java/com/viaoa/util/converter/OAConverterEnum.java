package com.viaoa.util.converter;

import java.text.DecimalFormat;

import com.viaoa.util.OAReflect;
import com.viaoa.util.converter.OAConverterNumber.FormatPool;

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
