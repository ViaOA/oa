package com.viaoa.util;

import java.lang.reflect.Array;
import java.util.Collection;

import com.viaoa.hub.Hub;

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
            Class c = value.getClass();
            if (!c.equals(matchValue.getClass())) {
                matchValue = OAConverter.convert(c, matchValue);
                if (matchValue == null) return false;
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

    public static boolean isGreater(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x > 0;
    }
    public static boolean isEqualOrGreater(Object value, Object fromValue) {
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
    
    public static int compare(Object value, Object fromValue) {
        if (value == null) {
            if (fromValue == null) return 0;
            return -1;
        }
        if (fromValue == null) return 1;
        Class c = value.getClass();

        if (!c.equals(fromValue.getClass())) {
            fromValue = OAConverter.convert(c, fromValue);
            if (fromValue == null) return 1;
        }
        if (!(fromValue instanceof Comparable)) {
            if (value.equals(fromValue)) return 0;
            return -1;
        }
        int x = ((Comparable)value).compareTo(fromValue);
        return x;
    }
    
    public static boolean isEmpty(Object obj) {
        return isEmpty(obj, false);
    }
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
        
        if (obj instanceof String) {
            if (bTrim) {
                if (((String)obj).trim().length() == 0) return true;
            }
            else {
                if (((String)obj).length() == 0) return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
        Object val1 = 222;
        Object val2 = "2*";
        
        boolean b = isLess(val1, val2);
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
