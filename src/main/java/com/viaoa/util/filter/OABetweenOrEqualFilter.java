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

public class OABetweenOrEqualFilter implements OAFilter {
    private static Logger LOG = Logger.getLogger(OABetweenOrEqualFilter.class.getName());

    private Object value1, value2;
    private OAPropertyPath pp;

    public OABetweenOrEqualFilter(Object val1, Object val2) {
        this.value1 = val1;
        this.value2 = val2;
    }
    public OABetweenOrEqualFilter(OAPropertyPath pp, Object val1, Object val2) {
        this.pp = pp;
        this.value1 = val1;
        this.value2 = val2;
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
        return OACompare.isBetweenOrEqual(obj, value1, value2);
    }
}

