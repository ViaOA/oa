package com.viaoa.util;

import java.lang.reflect.*;
import java.util.IdentityHashMap;

/**
 * Search object and it's fields/references for a null value.
 * @author vvia
 */
public class OAFindNull {
    private IdentityHashMap<Object, Object> hm = new IdentityHashMap<Object, Object>();
    /**
     * Find a null value for an object or any of it's fields/references. 
     * @param obj
     * @return true if a null was found, the foundOne(..) will be called.
     */
    public boolean findNull(Object obj) throws IllegalAccessException {
        hm.clear();
        String s = obj == null ? "" : obj.getClass().getName();
        int x = s.lastIndexOf('.');
        if (x > 0) s = s.substring(x+1);
        return _findNull(s, obj);
    }    

    private boolean _findNull(String propertyPath, Object obj) throws IllegalAccessException {
        if (obj == null) {
            foundOne(propertyPath);
            return true;
        }
        if (hm.get(obj) != null) return false;
        hm.put(obj, obj);

        if (obj instanceof String) {
            return false;
        }
        
        if (obj.getClass().isArray()) {
            int x = Array.getLength(obj);

            boolean bMatch = true;
            for (int i=0; i<x; i++) {
                Object o1 = Array.get(obj, i);
                boolean b = _findNull(propertyPath+"["+i+"]", o1);
            }
            return bMatch;
        }

        boolean b = _findNullFields(propertyPath, obj);

        return b;
    }

    
    private boolean _findNullFields(String propertyPath, Object obj) throws IllegalAccessException {
        Field[] objFields = obj.getClass().getDeclaredFields();
        AccessibleObject.setAccessible(objFields, true);
        boolean bResult = false;
        for (Field field : objFields) {
            if (field.getName().indexOf('$') >= 0) continue;
            if (Modifier.isStatic(field.getModifiers())) continue;
            if (Modifier.isTransient(field.getModifiers())) continue;

            Object ox = field.get(obj);
            if (_findNull(propertyPath+"."+field.getName(), ox)) bResult = true;
        }
        return bResult;
    }
    
    /**
     * @return true to continue, false to stop
     */
    public boolean foundOne(String propertyPath) {
        System.out.println(propertyPath);
        return true;
    }
    
    public static void main(String[] args) throws Exception {
        //Object obj1 = "test";
        //Object obj2 = null;
        /*
        TRIOrder obj1 = new TRIOrder();
        TRIOrder obj2 = new TRIOrder();
        obj1.additionalQuantities = new double[0];
        obj2.additionalQuantities = new double[1];
        
        IFindNull oc = new IFindNull() {
            @Override
            public boolean foundOne(String propertyPath) {
                return super.foundOne(propertyPath);
            }
        };
        oc.findNull(obj1);
        */
    }
}
