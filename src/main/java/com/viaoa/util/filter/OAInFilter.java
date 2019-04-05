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

import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;

/**
 * Creates a filter to see if an object is in a property path or Hub.
 * 
 * @author vvia
 */
public class OAInFilter implements OAFilter {
    private static Logger LOG = Logger.getLogger(OAInFilter.class.getName());
    
    private final OAObject fromObject;
    private String strPropPath;
    private OAPropertyPath pp;
    private Hub hubIn;

    private OAPropertyPath ppReverse;
    private String strReversePropPath;
    private OAFinder finder;
    private Object objFind;

    public OAInFilter(Hub hubIn) {
        this.hubIn = hubIn; 
        fromObject = null;
        strPropPath = null;
        pp = null;
    }
    
    public OAInFilter(OAObject fromObject, String propPath) {
        this.fromObject = fromObject;
        if (fromObject == null) return;
        this.strPropPath = propPath;
        if(propPath == null) return;
        this.hubIn = null;
        
        this.pp = new OAPropertyPath(fromObject.getClass(), propPath);
        
        if (!pp.isLastPropertyLinkInfo()) {
            throw new RuntimeException("invalid propPath "+propPath);
        }
        
        /*
         * Split the pp into two:
         * 1: from reverse direction, as long as it's linkTyp=one
         * 2: from forward, removing reverse pp
         * Then use the reverse pp to convert obj to one close in the pp.
         * Then use the pp to find the obj
         */
        
        // follow reverse pp as long as linkType=one and not recursive and no filter
        OALinkInfo[] lis = pp.getLinkInfos();
        Constructor[] cs = pp.getFilterConstructors();
        OALinkInfo[] lisRecursive = pp.getRecursiveLinkInfos();

        String ppNew1 = propPath;
        String ppNew2 = "";
        for (int i=lis.length-1; i>=0; i--) {
            if (cs[i] != null) break;
            if (lisRecursive[i] != null) break;
            OALinkInfo li = lis[i];
            OALinkInfo liRev = li.getReverseLinkInfo();
            if (liRev == null) break;
            if (liRev.getType() != OALinkInfo.ONE) break;
            if (liRev.getPrivateMethod()) break;
            
            ppNew1 = OAString.field(ppNew1, '.', 1, OAString.dcount(ppNew1, '.')-1);
            if (ppNew2.length() > 0) ppNew2 += ".";
            ppNew2 += liRev.getName();
            
        }
        if (ppNew2.length() > 0) strReversePropPath = ppNew2;
        
        if (ppNew1.length() == 0) return;
            
        
        OAPropertyPath ppx = new OAPropertyPath(fromObject.getClass(), ppNew1);
        // see if it ends in a Hub and does not have any other many links before it
        lis = ppx.getLinkInfos();
        for (int i=0; i < lis.length; i++) {
            OALinkInfo li = lis[i];
            if (li.getType() == OALinkInfo.MANY) {
                if (i == lis.length-1) {
                    hubIn = (Hub) ppx.getValue(fromObject);
                }
                break;
            }
        }
            
        if (hubIn == null) {
            finder = new OAFinder(ppNew1) {
                @Override
                protected boolean isUsed(OAObject obj) {
                    return (obj == objFind);
                }
            };
        }
    }
    
    public OAPropertyPath getPropertyPath() {
        return pp;
    }
    
    @Override
    public boolean isUsed(Object obj) {
        if (hubIn != null && strReversePropPath == null) {
            return hubIn.contains(obj);
        }
        
        objFind = obj;
        if (strReversePropPath != null) {
            if (ppReverse == null) {
                ppReverse = new OAPropertyPath(obj.getClass(), strReversePropPath);
            }
            objFind = ppReverse.getValue(obj);
            if (objFind == null) return false;
        }

        boolean bResult;
        if (hubIn != null) {
            bResult = hubIn.contains(objFind);
        }
        else if (finder != null) {
            Object objx = finder.findFirst(fromObject);
            bResult = (objx != null);
        }
        else bResult = false;
        return bResult;
    }
    
}

