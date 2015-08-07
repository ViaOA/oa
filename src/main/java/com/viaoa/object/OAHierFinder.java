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
import com.viaoa.util.*;

// 20150806
/**
 * This is used to find the first value in an object hierarchy (include recursive) that 
 * has a matching (or not matching) value. 
 *
 * example:
 *  Employee.department.location.region.country;
 *  
 *  where location is recursive (has parent locations)
 *  and each object in the hierarchy has a property to know if it is "special" or not.
 *  
 *  OAHierFinder f = new OAHierFinder(EmployeePP.special(), EmployeePP.location().special(), LocationPP.region().special(), RegionPP.country().specialFlag())
 *    // notice that each has the path to the property to check, and the next propPath begins from the previous pp.  
 *  f.findFirst(employee);  // find the first non-empty (ex: true)
 *  f.findFirst(employee, true); // find first with special prop = true
 *  
 */
public class OAHierFinder<F> {
    private Info[] infos;
    class Info {
        private String strPropertyPath;
        private OAPropertyPath propertyPath;
        
        private OALinkInfo liRecursiveRoot;
        private OALinkInfo[] linkInfos;
        private OALinkInfo[] recursiveLinkInfos;
        private Method[] methods;
    }

    public OAHierFinder(String ... propPaths) {
        if (propPaths == null) return;
        for (String pp : propPaths) {
//qqqqqqqqq            
        }
    }
    public Object find(F fromObject) {
        return null;
    }
    
}
