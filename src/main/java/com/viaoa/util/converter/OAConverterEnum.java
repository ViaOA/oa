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
