package com.viaoa.util;


import java.lang.reflect.*;
import java.util.IdentityHashMap;

/**
 * Compare two objects, finding which fields do not match.
 */
public class OACompare {
    // hashmap used to add a Visitor pattern
    IdentityHashMap<Object, Object> hmVisitor = new IdentityHashMap<Object, Object>();
    
    public boolean compare(Object objLeft, Object objRight) throws IllegalAccessException {
        String s = objLeft == null ? "" : objLeft.getClass().getName();
        int x = s.lastIndexOf('.');
        if (x > 0) s = s.substring(x+1);
        return _compare(s, objLeft, objRight);
    }    
    private boolean _compare(String propertyPath, Object objLeft, Object objRight) throws IllegalAccessException {
        boolean bResult = true;
        bResult = _compare(propertyPath, objLeft, objRight, true);
        return bResult;
    }

    private boolean _compare(String propertyPath, Object objLeft, Object objRight, boolean bReportNotEquals) throws IllegalAccessException {
        if (objLeft == objRight) return true;
        if (objLeft == null || objRight == null) {
            if (bReportNotEquals) foundOne(propertyPath, objLeft, objRight);
            return false;
        }

        if (!objLeft.getClass().equals(objRight.getClass())) {
            if (bReportNotEquals) foundOne(propertyPath, objLeft, objRight);
            return false;
        }
        
        String s = objLeft.getClass().getName();
        if (s.indexOf("java.") == 0) {
            boolean b = objLeft.equals(objRight);
            if (!b && bReportNotEquals) {
                foundOne(propertyPath, objLeft, objRight);
            }
            return b;
        }
        
        if (objLeft.getClass().isArray()) {
            int x = Array.getLength(objLeft);
            if (Array.getLength(objRight) != x) {
                if (bReportNotEquals) foundOne(propertyPath, "objLeft.length="+Array.getLength(objLeft), "objRight.length="+Array.getLength(objRight));
                return false;
            }

            boolean bMatch = true;
            for (int i=0; i<x; i++) {
                Object o1 = Array.get(objLeft, i);
                boolean bFound = false;
                for (int j=-1; j<x; j++) { // start at -1, to try same pos as "i" first
                    Object o2 = Array.get(objRight, (j<0?i:j) );
                    Object k1 = getKey(o1);
                    Object k2 = getKey(o2);
                    if (_compare(null, k1, k2, false)) {
                        boolean b = _compare(propertyPath+"["+i+","+(j<0?i:j)+"]", o1, o2, bReportNotEquals);
                        if (!b) bMatch = false;
                        bFound = true;
                        break;
                    }
                }
                if (!bFound) {
                    bMatch = false;
                    if (bReportNotEquals) {
                        foundOne(propertyPath+"["+i+"] not found", objLeft, objRight);
                    }
                    break;
                }
            }
            return bMatch;
        }

        boolean b = false;
        if (!bReportNotEquals) b = objLeft.equals(objRight);
        
        if (!b && bReportNotEquals) {
            b = _compareFields(propertyPath, objLeft, objRight, bReportNotEquals);
        }
        return b;
    }

    protected Object getKey(Object obj) {
        return obj;
    }
    
    private boolean _compareFields(String propertyPath, Object objLeft, Object objRight, boolean bReportNotEquals) throws IllegalAccessException {
        // check to see if these objects have already been compared
        Object objx = hmVisitor.get(objLeft);
        if (objx == objRight) {
            objx = hmVisitor.get(objRight);
            if (objx == objLeft) {
                return true; // already compared
            }
        }
        hmVisitor.put(objLeft, objRight);
        hmVisitor.put(objRight, objLeft);

        Field[] objFields = objLeft.getClass().getDeclaredFields();
        AccessibleObject.setAccessible(objFields, true);
        boolean bResult = true;
        for (Field field : objFields) {
            if (field.getName().indexOf('$') >= 0) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;

            Object o1 = field.get(objLeft);
            Object o2 = field.get(objRight);
            
            if (!_compare(propertyPath+"."+field.getName(), o1, o2, bReportNotEquals) ) {
                bResult = false;
            }
        }
        return bResult;
    }
    
    public void foundOne(String propertyPath, Object objLeft, Object objRight) {
        String s1 = objLeft+"";
        if (s1.length() > 40) s1 = s1.substring(0,40)+"...";
        String s2 = objRight+"";
        if (s2.length() > 40) s2 = s2.substring(0,40)+"...";
        System.out.println(propertyPath+": objLeft="+s1+", objRight="+s2);
    }
    
    public static void main(String[] args) throws Exception {
        Object obj1 = "test";
        Object obj2 = null;
        
        OACompare oc = new OACompare() {
            @Override
            public void foundOne(String propertyPath, Object objLeft, Object objRight) {
                super.foundOne(propertyPath, objLeft, objRight);
            }
        };
        oc.compare(obj1, obj2);
    }
}
