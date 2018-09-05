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
 * Used to compare objects, even if objects are different classes.
 * Ex: String "1234" will equal double 1234.00
 * ex: boolean true will equal any number but 0, any value except null, any string except '' blank
 * 
 * Also allows for comparing with Hub or Array to another Hub/Array, or single object (hub.AO or hub[0]&size=1,  array.length=1&array[0])
 * 
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
        if (compare(value, matchValue) == 0) return true;
        if (value == null || matchValue == null) {
            return false;
        }
        if (!(matchValue instanceof String)) {
            return false;
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
        int x = compare(value, matchValue);
        return x == 0;
    }    
    
    
    public static boolean isEqual(Object value, Object matchValue, boolean bIgnoreCase) {
        return isEqual(value, matchValue, bIgnoreCase, -1);
    }
    public static boolean isEqual(Object value, Object matchValue, int decimalPlaces) {
        return isEqual(value, matchValue, false, decimalPlaces);
    }
    
    public static boolean isEqual(Object value, Object matchValue, boolean bIgnoreCase, int decimalPlaces) {
        if (bIgnoreCase) {
            if (value instanceof String) value = ((String) value).toLowerCase();
            if (matchValue instanceof String) matchValue = ((String) matchValue).toLowerCase();
        }
        int x = compare(value, matchValue, decimalPlaces);
        return x == 0;
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

    public static int compare(int a, int b) {
        if (a == b) return 0;
        if (a > b) return 1;
        return -1;
    }

    public static int compare(Object value, Object matchValue) {
        return compare(value, matchValue, -1);
    }
    
    /**
     * Compare objects, converting them (using OAConverter class) if necessary.
     * value, matchValue can be any type of object, including Hub or Array.
     * value, matchValue do not have to be same class. 
     */
    public static int compare(Object value, Object matchValue, final int decimalPlaces) {
        if (value == matchValue) return 0;

        Class classValue = (value == null) ? null : value.getClass();
        Class classMatchValue = (matchValue == null) ? null : matchValue.getClass();

        // check if using array        
        if (classValue != null && classValue.isArray()) {
            if (classMatchValue != null && classMatchValue.isArray()) {
                // all objects must be same
                int x1 = Array.getLength(value);
                int x2 = Array.getLength(matchValue);
                if (x1 < x2) return -1;
                if (x1 > x2) return 1;
                for (int i=0; i<x1; i++) {
                    Object v1 = Array.get(value, i);
                    Object v2 = Array.get(matchValue, i);
                    int x = compare(v1, v2);
                    if (x != 0) return x;
                }
                return 0;
            }
            // take value from [0]
            int x = Array.getLength(value);
            if (x > 1) return 1;
            if (x == 0) value = null;
            else value = Array.get(value, 0);
            x = compare(value, matchValue, decimalPlaces);
            return x;
        }
        if (classMatchValue != null && classMatchValue.isArray()) {
            // take value from [0]
            int x = Array.getLength(matchValue);
            if (x > 1) return -1;
            if (x == 0) matchValue = null;
            else matchValue = Array.get(matchValue, 0);
            x = compare(value, matchValue);
            return x;
        }
        
        // check if using hub
        if (value instanceof Hub) {
            if (matchValue instanceof Hub) {
                // all objects must be same
                Hub h1 = (Hub) value;
                Hub h2 = (Hub) matchValue;
                int x1 = h1.getSize();
                int x2 = h2.getSize();
                if (x1 < x2) return -1;
                if (x1 > x2) return 1;
                for (int i=0; i<x1; i++) {
                    if (h1.getAt(i) != h2.getAt(i)) return -1;
                }
                return 0;
            }
            // take value from hub.AO or pos=0 & size=1
            Hub h = (Hub) value;
            value = h.getAO();
            if (value == null) {
                if (h.getSize() > 1) return 1;
                value = h.getAt(0);
            }
            int x = compare(value, matchValue);
            return x;
        }
        else if (matchValue instanceof Hub) {
            Hub h = (Hub) matchValue;
            matchValue = h.getAO();
            if (matchValue == null) {
                if (h.getSize() > 1) return -1;
                matchValue = h.getAt(0);
            }
            int x = compare(value, matchValue);
            return x;
        }
        
        
        if (value == null) {
            value = OAConverter.convert(classMatchValue, value);
            classValue = (value == null) ? null : value.getClass();
        }
        else if (matchValue == null) {
            matchValue = OAConverter.convert(classValue, matchValue);
            classMatchValue = (matchValue == null) ? null : matchValue.getClass();
        }
        else if (classValue.equals(classMatchValue)) {
            // noop
        }
        else if (classValue.equals(Boolean.class)) {
            matchValue = OAConv.toBoolean(matchValue);
            classMatchValue = (matchValue == null) ? null : matchValue.getClass();
        }
        else if (classMatchValue.equals(Boolean.class)) {
            value = OAConv.toBoolean(value);
            classValue = (value == null) ? null : value.getClass();
        }
        else if (OAReflect.isFloat(classValue) && (matchValue == null || OAReflect.isNumber(classMatchValue) || (classMatchValue.equals(String.class) && OAString.isNumber((String) matchValue)))) {
            matchValue = OAConv.convert(classValue, matchValue);
            classMatchValue = (matchValue == null) ? null : matchValue.getClass();
        }
        else if (OAReflect.isFloat(classMatchValue) && (value == null || OAReflect.isNumber(classValue) || (classValue.equals(String.class) && OAString.isNumber((String) value)))) {
            value = OAConv.convert(classMatchValue, value);
            classValue = (value == null) ? null : value.getClass();
        }
        else {
            try {
                value = OAConverter.convert(classMatchValue, value);
                if (value == null) return -1;
            }
            catch (Throwable e) {
                try {
                    matchValue = OAConverter.convert(classValue, matchValue);
                }
                catch (Throwable ex) {
                    return 1;
                }
            }
            classValue = (value == null) ? null : value.getClass();
            classMatchValue = (matchValue == null) ? null : matchValue.getClass();
        }

        if (decimalPlaces > 0) {
            if (OAReflect.isFloat(classValue)) {
                double d = OAConv.toDouble(value);
                value = OAConv.round(d, decimalPlaces);
            }
            if (OAReflect.isFloat(classMatchValue)) {
                double d = OAConv.toDouble(matchValue);
                matchValue = OAConv.round(d, decimalPlaces);
            }
            classValue = (value == null) ? null : value.getClass();
            classMatchValue = (matchValue == null) ? null : matchValue.getClass();
        }
        
        if (!(matchValue instanceof Comparable) || !(value instanceof Comparable)) {
            if (value == null) {
                if (value == matchValue) return 0;
                return -1;
            }
            if (matchValue == null) return 1;
            if (value.equals(matchValue)) return 0;
            return -1;
        }
        int x = ((Comparable)value).compareTo(matchValue);
        return x;
    }
    
    
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }
    public static boolean isNotEmpty(Object obj, boolean bTrim) {
        return !isEmpty(obj, bTrim);
    }
    
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, false);
    }
    /**
     * Checks to see if the value of an object can be considered empty.
     * 
     * example:  null, an empty array, an collection with no elements, a primitive set to 0, primitive boolean that is false, a string with only spaces (if using bTrim)
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
