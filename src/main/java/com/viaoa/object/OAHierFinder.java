/*  Copyright 1999-2016 Vince Via vvia@viaoa.com
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
import com.viaoa.util.*;

/**
 * This is used to find the first value in an object hierarchy (include recursive) that 
 * has a matching (or not matching) value in the first object, or one of the objects in it's hierarchy,
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
 *
qqqqqqqqqqqqqqqqqqqqqqqqqqq  
 OAHierFinder f = new OAHierFinder(EmployeePP.location().region().country().specialFlag())
 *  
 *  
 */
public class OAHierFinder<F> {
    private String property;
    private String propertyPath;
    private OAPropertyPath propPath;
    
    public OAHierFinder(String propertyName, String propertyPath) {
        this.property = propertyName;
        this.propertyPath = this.propertyPath;
    }
    
    
    
    
    public Object findFirst(F fromObject, OAComparator comp, OAFilter filter) {
        if (fromObject == null) return null;
        Class c = fromObject.getClass();
        
        propPath = new  OAPropertyPath(c, propertyPath);
        
        Object value = findFirstValue(fromObject, filter, 0, 0);
        return value;
    }

    protected Object findFirstValue(final Object obj, OAFilter filter, final int pos, final int startPos) throws Exception {
        if (obj == null) return null;
        if (propertyPaths == null || propertyPaths.length <= pos) return null; 

        Method[] methods = propertyPaths[pos].getMethods();
        Object value = obj;
        for (int i=startPos; i<methods.length; i++) {
            value = methods[i].invoke(value);
            if (value == null) break;
        }
        if (isUsed(obj, value)) return value;
        
        // recursive
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj.getClass());
        OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
        if (li != null) {
            Object objParent = li.getValue(obj);
            if (objParent != null) {
                value = findFirstValue(objParent, pos, startPos);
                if (value != null) return value;
            }
        }
        if (pos+1 == propertyPaths.length) return null;

        Object objNext = propertyPaths[pos+1].getMethods()[0].invoke(obj);
        if (objNext == null) return null;
        value = findFirstValue(objNext, pos+1, 1);

        return value;
    }
    
    protected boolean isUsed(Object obj, Object value) {
        return !OACompare.isEmpty(value);
    }
    
}
