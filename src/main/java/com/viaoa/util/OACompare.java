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
package com.viaoa.util;

import java.lang.reflect.Array;
import java.util.Collection;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;

// 20140124
/**
 * Used to compare objects.
 * @author vvia
 */
public class OACompare {

    public static boolean isEqualOrIn(Object obj, Object matchValue) {
        return isIn(obj, matchValue);
    }
    
    public static boolean isIn(Object obj, Object matchValue) {
        if (obj == null|| matchValue == null) return false;
        if (matchValue instanceof Hub) {
            return ((Hub) matchValue).contains(obj);
        }
        if (matchValue.getClass().isArray()) {
            int x = Array.getLength(matchValue);
            for (int i=0; i<x; i++) {
                Object objx = Array.get(matchValue, i);
                if (isEqual(obj, objx)) return true;
            }
        }
        return isEqual(obj, matchValue);
    }
    
    /**
     * @param matchValue if a String, then it can begin or end with '*'|'%' as a wildcard.
     */
    public static boolean isLike(Object value, Object matchValue) {
        if (value == matchValue) return true;
        if (value == null || matchValue == null) return false;
        if (value.equals(matchValue)) return true;

        if (!(matchValue instanceof String)) {
            return isEqual(value, matchValue);
        }
        
        // convert to strings
        String sValue;
        if (!(value instanceof String)) {
            sValue = OAConverter.toString(value);
            if (sValue == null) return false;
        }
        else sValue = (String) value;
        sValue = sValue.toLowerCase();
        
        String sMatchValue = (String) matchValue;
        sMatchValue = sMatchValue.toLowerCase();
        boolean b1 = false;
        boolean b2 = false;
        
        int x = sMatchValue.length();
        if (x > 0) {
            char ch = sMatchValue.charAt(0);
            if (ch == '*' || ch == '%') {
                b1 = true;
                sMatchValue = sMatchValue.substring(1);
                x--;
            }
        }
        if (x > 0) {
            char ch = sMatchValue.charAt(x-1);
            if (ch == '*' || ch == '%') {
                b2 = true;
                sMatchValue = sMatchValue.substring(0, x-1);
            }
        }
        if (!b1 && !b2) {
            return sValue.equals(sMatchValue);
        }
        else if (b1 && b2) {
            return (sValue.indexOf(sMatchValue) >= 0);
        }
        else if (b1) {
            return sValue.endsWith(sMatchValue);
        }
        //else if (b2) {
        return sValue.startsWith(sMatchValue);
    }    

    
    public static boolean isEqualIgnoreCase(Object value, Object matchValue) {
        return isEqual(value, matchValue, true);
    }
    public static boolean isEqual(Object value, Object matchValue) {
        return isEqual(value, matchValue, false);
    }    
    
    public static boolean isEqual(Object value, Object matchValue, boolean bIgnoreCase) {
        if (value == matchValue) return true;
        if (value == null || matchValue == null) return false;
        if (value.equals(matchValue)) return true;

        
        // allow OAObject to be compared with a pkey vvalue
        //    ex:  Order.equals(5), is true if order.id == 5
        if (value instanceof OAObject) {
            return ((OAObject)value).equals(matchValue);
        }
        if (matchValue instanceof OAObject) {
            return ((OAObject)matchValue).equals(value);
        }
        
        if (matchValue instanceof Hub) {
            Hub h = (Hub) matchValue;
            return (h.getSize() == 1 && h.getAt(0) == matchValue);
        }
        if (matchValue.getClass().isArray()) {
            int x = Array.getLength(matchValue);
            if (x != 1) return false;
            Object objx = Array.get(matchValue, 0);
            return (isEqual(value, objx));
        }
        
        if (bIgnoreCase) {
            if (!(value instanceof String)) {
                value = OAConverter.toString(value);
                if (value == null) return false;
            }
            if (!(matchValue instanceof String)) {
                matchValue = OAConverter.toString(matchValue);
                if (matchValue == null) return false;
            }
        }
        else {
            Class c = matchValue.getClass();
            if (!c.equals(value.getClass())) {
                Object valx = OAConverter.convert(c, value);
                if (valx == null) return false;
                return matchValue.equals(valx);
            }
        }
        
        if (value instanceof String) {
            if (bIgnoreCase) {
                return ((String) value).equalsIgnoreCase((String) matchValue);
            }
            return ((String) value).equals((String) matchValue);
        }
        return value.equals(matchValue); 
    }

    public static boolean isBetween(Object value, Object fromValue, Object toValue) {
        if (value == null) return false;
        if (toValue == null) return false;
        int x = compare(value, fromValue);
        if (x <= 0) return false;

        x = compare(value, toValue);
        if (x >= 0) return false;
        return true;
    }    
    public static boolean isEqualOrBetween(Object value, Object fromValue, Object toValue) {
        if (value == null) return (fromValue == null);
        if (toValue == null) return false;
        int x = compare(value, fromValue);
        if (x < 0) return false;

        x = compare(value, toValue);
        if (x > 0) return false;
        return true;
    }    
    public static boolean isBetweenOrEqual(Object value, Object fromValue, Object toValue) {
        return isEqualOrBetween(value, fromValue, toValue);
    }

    public static boolean isGreater(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x > 0;
    }
    public static boolean isEqualOrGreater(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x >= 0;
    }
    public static boolean isGreaterOrEqual(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x >= 0;
    }

    public static boolean isLess(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x < 0;
    }
    public static boolean isEqualOrLess(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x <= 0;
    }
    public static boolean isLessOrEqual(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x <= 0;
    }
    
    public static int compare(Object value, Object matchValue) {
        if (value == null) {
            if (matchValue == null) return 0;
            return -1;
        }
        if (matchValue == null) return 1;
        
        
        Class c = matchValue.getClass();
        if (!c.equals(value.getClass())) {
            value = OAConverter.convert(c, value);
            if (value == null) return -1;
        }
        
        if (!(matchValue instanceof Comparable)) {
            if (value.equals(matchValue)) return 0;
            return -1;
        }
        int x = ((Comparable)value).compareTo(matchValue);
        return x;
    }
    
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, false);
    }
    /**
     * Checks to see if the value of an object can be considered empty.
     * 
     * example:  null, an empty array, an collection with no elements, a primitive set to 0, a string will only spaces (if using bTrim)
     * @param obj
     * @param bTrim if true and object is a string, then spaces will be ignored.
     */
    public static boolean isEmpty(Object obj, boolean bTrim) {
        if (obj == null) return true;
        
        if (obj instanceof Hub) {
            return ((Hub) obj).getSize() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return (Array.getLength(obj) == 0);
        }

        Class c = obj.getClass();
        if (OAReflect.isPrimitiveClassWrapper(c)) {
            if (obj instanceof Number) {
                return (((Number) obj).doubleValue() == 0.0);
            }
            if (obj instanceof Boolean) {
                return (((Boolean) obj).booleanValue() == false);
            }
            if (obj instanceof Character) {
                return (((Character) obj).charValue() == 0);
            }
            return false;
        }
        
        return OAString.isEmpty(obj, bTrim);
    }
    
    public static void main(String[] args) {
        Object val1 = 222;
        Object val2 = "2*";
        
        boolean b;
        b = isEmpty(null);
        b = isEmpty("");
        b = isEmpty(new String[0]);
        b = isEmpty(false);
        b = isEmpty(true);
        b = isEmpty(0);
        b = isEmpty(0.0);
        b = isEmpty(0.0000001);
        b = isEmpty((char) 0);
        b = isEmpty('a');
        
        b = isLess(val1, val2);
        b = isLike(val1, val2);
        b = isLess(val1, val2);
        b = isEqualOrLess(val1, val2);
        b = isGreater(val1, val2);
        b = isEqualOrGreater(val1, val2);
        
        b = isEqualIgnoreCase(val1, val2);
        b = isEqual(val1, val2);
        
        int xx = 4;
        xx++;
    }
}
