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
package com.viaoa.util.filter;

import java.lang.reflect.Constructor;
import java.util.logging.Logger;

import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;

/**
 * Creates a filter to see if an object is in a list.
 * 
 * @author vvia
 */
public class OAInFilter implements OAFilter {
    private static Logger LOG = Logger.getLogger(OAInFilter.class.getName());
    private final OAObject fromObject;
    private final String strPropPath;
    private final OAPropertyPath pp;
    private OAPropertyPath ppReverse;
    
    public OAInFilter(OAObject fromObject, String propPath) {
        this.fromObject = fromObject;
        this.strPropPath = propPath;
        
        this.pp = new OAPropertyPath(fromObject.getClass(), propPath);
        try {
            ppReverse = pp.getReversePropertyPath();
        }
        catch (Exception e) {
            ppReverse = null;
            return; // use pp and Finder.findFirst? or merger? qqqqqqqqqqq???
        }
        
        
        
        // find out which direction that will be used
        OALinkInfo[] lis = pp.getLinkInfos();
        if (lis != null) {
            for (OALinkInfo li : lis) {
                if (li != null && li.getRecursive()) {
                    ppReverse = null;
                    return;
                }
            }
        }
        
        Constructor[] cs = pp.getFilterConstructors();
        if (cs != null) {
            for (Constructor c  : cs) {
                if (c != null) {
                    ppReverse = null;
                    return;
                }
            }
        }
        
        
        
    }

    public OAPropertyPath getPropertyPath() {
        return pp;
    }
    
    
    @Override
    public boolean isUsed(Object obj) {
        return false;
    }
    
}

