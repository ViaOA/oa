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

import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAMany;
import com.viaoa.annotation.OAOne;
import com.viaoa.hub.Hub;

/**
 * Utility class, used by HubGroupBy, as an object that creates a reference to two others objects.
 * 
 * @param <G> group by object
 * @param <F> detail hub / source.
 * 
 * @see HubGroupBy#
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class OAGroupBy<F extends OAObject, G extends OAObject> extends OAObject {
    static final long serialVersionUID = 1L;
    
    public static final String P_GroupBy = "GroupBy"; 
    public static final String P_Hub = "Hub"; 
    public static final String PROPERTY_GroupBy = "GroupBy"; 
    public static final String PROPERTY_Hub = "Hub"; 
    private G groupBy;
    private Hub<F> hub;
    
    public OAGroupBy() {
    }
    
    public OAGroupBy(G groupBy) {
        setGroupBy(groupBy);
    }
    
    @OAOne
    public G getGroupBy() {
        if (groupBy == null) {
            groupBy = (G) getObject(PROPERTY_GroupBy);
        }
        return this.groupBy;
    }
    public void setGroupBy(G obj) {
        OAObject hold = this.groupBy;
        fireBeforePropertyChange(P_GroupBy, hold, obj);
        this.groupBy = obj;
        firePropertyChange(P_GroupBy, hold, obj);
    }
    
    @OAMany
    public Hub<F> getHub() {
        if (hub == null) {
            hub = getHub(PROPERTY_Hub);
        }
        return hub;
    }
}
