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
package com.viaoa.util.filter;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;

public class OAEqualFilter implements OAFilter {
    private static Logger LOG = Logger.getLogger(OAEqualFilter.class.getName());
    private Object value;
    private boolean bIgnoreCase;
    private OAPropertyPath pp;
    private OAFinder finder;

    public OAEqualFilter(Object value) {
        this.value = value;
    }

    public OAEqualFilter(OAPropertyPath pp, Object value) {
        this.pp = pp;
        this.value = value;
        check();
    }
qqqqqqqqq
    public OAEqualFilter(Object value, boolean bIgnoreCase) {
        this.value = value;
        this.bIgnoreCase = bIgnoreCase;
    }
    
    public OAEqualFilter(OAPropertyPath pp, Object value, boolean bIgnoreCase) {
        this.pp = pp;
        this.value = value;
        this.bIgnoreCase = bIgnoreCase;
        check();
    }
    

    // see if an oaFinder should be used instead of PropertyPath
    private void check() {
        if (pp == null) return;
        Method[] ms = pp.getMethods();
        if (ms == null) return;
        for (Method m : ms) {
             if (!m.getReturnType().equals(Hub.class)) continue;
             final OAEqualFilter f = new OAEqualFilter(value, bIgnoreCase);
             finder = new OAFinder(pp.getPropertyPath()) {
                 @Override
                 protected boolean isUsed(OAObject obj) {
                     return f.isUsed(obj);
                 }                         
             };
             break;
        }
    }
    
    @Override
    public boolean isUsed(Object obj) {
        if (finder != null) {
            if (obj instanceof OAObject) {
                obj = finder.findFirst((OAObject)obj);
            }
            else if (obj instanceof Hub) {
                obj = finder.findFirst((Hub)obj);
            }
            return obj != null;
        }
        else if (pp != null) {
            try {
                obj = pp.getValue(obj);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error getting value for property path", e);
            }
        }
        return OACompare.isEqual(obj, value, bIgnoreCase);
    }
    
}

