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

import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;

public class OAGreaterOrEqualFilter implements OAFilter {
    private static Logger LOG = Logger.getLogger(OAGreaterOrEqualFilter.class.getName());
    private OAPropertyPath pp;
    private Object value;

    public OAGreaterOrEqualFilter(Object value) {
        this.value = value;
    }
    public OAGreaterOrEqualFilter(OAPropertyPath pp, Object value) {
        this.pp = pp;
        this.value = value;
    }
    
    @Override
    public boolean isUsed(Object obj) {
        if (pp != null) {
            try {
                obj = pp.getValue(obj);
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "error getting value for property path", e);
            }
        }
        return OACompare.isGreaterOrEqual(obj, value);
    }
}

