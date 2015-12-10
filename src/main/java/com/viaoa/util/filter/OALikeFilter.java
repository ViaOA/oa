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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.filter.OAFilterDelegate.FinderInfo;

/**
 * Creates a filter to see if the value from the propertyPath is like the filter value.
 * 
 * @author vvia
 * @see OACompare#isLike(Object, Object)
 */
public class OALikeFilter implements OAFilter {
    private static Logger LOG = Logger.getLogger(OALikeFilter.class.getName());
    private OAPropertyPath pp;
    private Object value;
    private OAFinder finder;

    public OALikeFilter(Object value) {
        this.value = value;
    }
    public OALikeFilter(OAPropertyPath pp, Object value) {
        this. pp = pp;
        this.value = value;
    }
    public OALikeFilter(String pp, Object value) {
        this(pp==null?null:new OAPropertyPath(pp), value);
    }
    
    private boolean bSetup;
    private int cntError;
    
    @Override
    public boolean isUsed(Object obj) {
        if (!bSetup && pp != null && obj != null) {
            // see if an oaFinder is needed
            bSetup = true;
            FinderInfo fi = OAFilterDelegate.createFinder(obj.getClass(), pp);
            if (fi != null) {
                this.finder = fi.finder;
                OAFilter f = new OALikeFilter(fi.pp, value);
                finder.addFilter(f);
            }
        }
        if (finder != null) {
            if (obj instanceof OAObject) {
                obj = finder.findFirst((OAObject)obj);
                return obj != null;
            }
            else if (obj instanceof Hub) {
                obj = finder.findFirst((Hub)obj);
                return obj != null;
            }
        }
        if (pp != null) {
            try {
                obj = pp.getValue(obj);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error getting value for property path", e);
            }
        }
        return OACompare.isLike(obj, value);
    }
}

