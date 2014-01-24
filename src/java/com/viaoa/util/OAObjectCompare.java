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
package com.viaoa.util;

import java.lang.reflect.*;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Compare two objects, finding which fields do not match.
 */
public class OAObjectCompare {
    // hashmap used to add a Visitor pattern
    IdentityHashMap<Object, Object> hmVisitor = new IdentityHashMap<Object, Object>();
    private String leftName;
    private String rightName;
    
    public OAObjectCompare() {
        setObjectNames(null, null);
    }
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

    public void setObjectNames(String leftName, String rightName) {
        setLeftObjectName(leftName);
        setRightObjectName(leftName);
    }
    public String getLeftObjectName() {
        return leftName;
    }
    public void setLeftObjectName(String name) {
        if (name == null) name = "leftObj";
        leftName = name;
    }
    public String getRightObjectName() {
        return rightName;
    }
    public void setRightObjectName(String name) {
        if (name == null) name = "rightObj";
        rightName = name;
    }
    
    
    protected boolean _compare(String propertyPath, Object objLeft, Object objRight, boolean bReportNotEquals) throws IllegalAccessException {
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
            boolean bMatch = true;
            if (Array.getLength(objRight) != x) {
                bMatch = false;
                if (bReportNotEquals) {
                    foundOne(propertyPath, "length="+Array.getLength(objLeft), "length="+Array.getLength(objRight));
                }
            }

            HashMap<Object, Object> hm = new HashMap<Object, Object>((int)(x * 1.5), .85f);
            for (int i=0; i<x; i++) {
                Object obj = Array.get(objLeft, i);
                Object key = getKey(obj);
                Object objx = hm.put(key, obj);
                if (objx != null) {
                    foundOne(propertyPath, "duplicate key in collection", key);
                }
            }
            if (hm.size() != x) {
                foundOne(propertyPath, "duplicate keys in collection", "");
            }
            x = Array.getLength(objRight);
            for (int i=0; i<x; i++) {
                Object objR = Array.get(objRight, i);
                Object key = getKey(objR);
                
                Object objL = hm.remove(key);
                if (objL == null) {
                    bMatch = false;
                    if (bReportNotEquals) {
                        foundOne(propertyPath+"["+i+"]", "not found", key);
                    }
                }
                else {
                    boolean b = _compare(propertyPath+"["+i+"]", objL, objR, bReportNotEquals);
                    if (!b) bMatch = false;
                }
            }
            for (Map.Entry<Object, Object> ex : hm.entrySet()) {
                Object key = ex.getKey();
                bMatch = false;
                if (bReportNotEquals) {
                    int pos = OAArray.indexOf((Object[]) objLeft, ex.getValue());
                    foundOne(propertyPath+"["+pos+"]", key, "not found");
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

            Object oL = field.get(objLeft);
            Object oR = field.get(objRight);
            
            if (!_compare(propertyPath+"."+field.getName(), oL, oR, bReportNotEquals) ) {
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
        System.out.println(propertyPath+": "+leftName+"="+s1+", "+rightName+"="+s2);
    }
    
    public static void main(String[] args) throws Exception {
        Object obj1 = "test";
        Object obj2 = null;
        
        OAObjectCompare oc = new OAObjectCompare() {
            @Override
            public void foundOne(String propertyPath, Object objLeft, Object objRight) {
                super.foundOne(propertyPath, objLeft, objRight);
            }
        };
        oc.compare(obj1, obj2);
    }
}
