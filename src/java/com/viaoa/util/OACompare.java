package com.viaoa.util;

// 20140124
/**
 * Used to compare objects.
 * @author vvia
 */
public class OACompare {
    
    /**
     * @param matchValue if a String, then it can begin or end with '*'|'%' as a wildcard. 
     */
    protected static boolean isLike(Object value, Object matchValue) {
        if (value == matchValue) return true;
        if (value == null || matchValue == null) return false;
        if (value.equals(matchValue)) return true;

        // convert to strings
        String sValue;
        if (!(value instanceof String)) {
            sValue = OAConverter.toString(value);
            if (sValue == null) return false;
        }
        else sValue = (String) value;
        
        String sMatchValue;
        if (!(matchValue instanceof String)) {
            sMatchValue = OAConverter.toString(matchValue);
            if (sMatchValue == null) return false;
        }
        else sMatchValue = (String) matchValue;
        
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
            return (sValue).equalsIgnoreCase(sMatchValue);
        }
        else if (b1 && b2) {
            return (sValue.toLowerCase().indexOf(sMatchValue) >= 0);
        }
        else if (b1) {
            return (sValue.toLowerCase().startsWith(sMatchValue));
        }
        //else if (b2) {
        return (sValue.toLowerCase().endsWith(sMatchValue));
    }    

    
    protected static boolean isEqualIgnoreCase(Object value, Object matchValue) {
        return isEqual(value, matchValue, true);
    }
    protected static boolean isEqual(Object value, Object matchValue) {
        return isEqual(value, matchValue, false);
    }    
    
    protected static boolean isEqual(Object value, Object matchValue, boolean bIgnoreCase) {
        if (value == matchValue) return true;
        if (value == null || matchValue == null) return false;
        if (value.equals(matchValue)) return true;

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

    protected static boolean isBetween(Object value, Object fromValue, Object toValue) {
        if (value == null) return false;
        if (toValue == null) return false;
        int x = compare(value, fromValue);
        if (x <= 0) return false;

        x = compare(value, toValue);
        if (x >= 0) return false;
        return true;
    }    
    protected static boolean isBetweenOrEqual(Object value, Object fromValue, Object toValue) {
        if (value == null) return (fromValue == null);
        if (toValue == null) return false;
        int x = compare(value, fromValue);
        if (x < 0) return false;

        x = compare(value, toValue);
        if (x > 0) return false;
        return true;
    }    

    protected static boolean isGreater(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x > 0;
    }
    protected static boolean isGreaterOrEqual(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x >= 0;
    }

    protected static boolean isLess(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x < 0;
    }
    protected static boolean isLessOrEqual(Object value, Object fromValue) {
        int x = compare(value, fromValue);
        return x <= 0;
    }
    
    protected static int compare(Object value, Object fromValue) {
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
    
    
    public static void main(String[] args) {
        Object val1 = 222;
        Object val2 = "2*";
        
        boolean b = isLess(val1, val2);
        b = isLike(val1, val2);
        b = isLess(val1, val2);
        b = isLessOrEqual(val1, val2);
        b = isGreater(val1, val2);
        b = isGreaterOrEqual(val1, val2);
        
        b = isEqualIgnoreCase(val1, val2);
        b = isEqual(val1, val2);
        
        int xx = 4;
        xx++;
    }
}
