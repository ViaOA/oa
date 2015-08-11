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
package com.viaoa.object;

import java.lang.reflect.Method;

import com.viaoa.hub.Hub;
import com.viaoa.util.*;

// 20150806
/**
 * This is used to find the first value in an object hierarchy (include recursive) that 
 * has a matching (or not matching) value in the first object, or one of the objects in it's hierarch,
 * as defined by propertyPaths.
 *
 * example:
 *  Employee.department.location.region.country;
 *  
 *  where location is recursive (has parent locations)
 *  and each object in the hierarchy has a property to know if it has "specialFlag" or not.
 *  
 *  OAHierFinder f = new OAHierFinder(EmployeePP.special(), EmployeePP.location().special(), LocationPP.region().special(), RegionPP.country().specialFlag())
 *    // notice that each has the path to the property to check, and the next propPath begins from the previous pp.  
 *  f.findFirstValue(employee);  // find the first non-empty (ex: true)
 *  f.findFirstValue(employee, true); // find first with special prop = true
 *  
 */
public class OAHierFinder<F> {
    private String[] strPropPath;
    private OAPropertyPath[] propertyPaths;
    
    
    public OAHierFinder(String ... propPaths) {
        this.strPropPath = propPaths;
    }

    public Object findFirstValue(F fromObject) {
        if (fromObject == null) return null;
        Class c = fromObject.getClass();
        
        propertyPaths = new OAPropertyPath[strPropPath.length] ;
        int i=0;
        for (String pp : strPropPath) {
            OAPropertyPath propPath = new  OAPropertyPath(c, pp);
            propertyPaths[i++] = propPath;
            if (i > 1) {
                Method[] ms = propPath.getMethods();
                c = ms[0].getReturnType();
            }
        }
        
        Object value = null;;
        try {
            value = findFirstValue(null, fromObject, 0, false);
        }
        catch (Exception e) {
            throw new RuntimeException("error finding value", e);
        }
        
        return value;
    }

    protected Object findFirstValue(Object objPrev, Object obj, int pos, boolean bRecursed) throws Exception {
        if (obj == null) return null;
        if (propertyPaths == null || propertyPaths.length <= pos) return null; 

        Object value = null;
        if (pos == 0) {
            value = propertyPaths[pos].getValue(obj);
            if (isUsed(obj, value)) return value;
            
            // recursive
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj.getClass());
            OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
            if (li != null) {
                Object objx = li.getValue(obj);
                if (objx != null) {
                    value = findFirstValue(null, objx, pos, false);
                    if (value != null) return value;
                }
            }
        
            if (pos+1 == propertyPaths.length) return null;
            
            Object objNext = propertyPaths[pos+1].getMethods()[0].invoke(obj);
            if (objNext == null) return null;
            value = findFirstValue(obj, objNext, pos+1, false);
        }        
        else if (!bRecursed) {
            value = propertyPaths[pos].getValue(objPrev);
            if (isUsed(obj, value)) return value;
            
            // recursive
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj.getClass());
            OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
            if (li != null) {
                Object objx = li.getValue(obj);
                if (objx != null) {
                    value = findFirstValue(objPrev, objx, pos, true);
                    if (value != null) return value;
                }
            }

            if (pos+1 == propertyPaths.length) return null;
            
            Object objNext = propertyPaths[pos+1].getMethods()[0].invoke(obj);
            if (objNext == null) return null;
            value = findFirstValue(obj, objNext, pos+1, false);
        }
        else {
            Method[] methods = propertyPaths[pos].getMethods();
            value = obj;
            for (int i=1; i<methods.length; i++) {
                value = methods[i].invoke(value);
                if (value == null) break;
            }
            if (isUsed(obj, value)) return value;
            
            // recursive
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj.getClass());
            OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
            if (li != null) {
                Object objx = li.getValue(obj);
                if (objx != null) {
                    value = findFirstValue(objPrev, objx, pos, true);
                    if (value != null) return value;
                }
            }
            
        }
        return value;
    }
    
    protected boolean isUsed(Object obj, Object value) {
        return !OACompare.isEmpty(value);
    }
    
}
