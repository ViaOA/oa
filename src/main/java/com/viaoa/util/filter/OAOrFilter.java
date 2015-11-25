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
 
import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;

public class OAOrFilter implements OAFilter {

    private OAFilter filter1, filter2;
    
    public OAOrFilter(OAFilter filter1, OAFilter filter2) {
        this.filter1 = filter1;
        this.filter2 = filter2;
    }
    @Override
    public boolean isUsed(Object obj) {
        if (filter1 != null) { 
            if (filter1.isUsed(obj)) return true;
        }
        if (filter2 == null) return false;
        return filter1.isUsed(obj);
    }
}

