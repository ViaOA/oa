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

import com.theice.tsactest.model.oa.RCInstalledVersionDetail;
import com.viaoa.annotation.OAClass;
import com.viaoa.annotation.OAMany;
import com.viaoa.annotation.OAOne;
import com.viaoa.hub.Hub;

/**
 * Utility class, used by HubGroupBy, as an object that creates a reference to two others objects.
 * 
 * @param <A> left side object
 * @param <B> detail hub from A
 * 
 * @see HubGroupBy#
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class OAGroupBy<A extends OAObject, B extends OAObject> extends OAObject {
    static final long serialVersionUID = 1L;
    
    public static final String P_A = "A"; 
    public static final String P_B = "B"; 
    public static final String PROPERTY_A = "A"; 
    public static final String PROPERTY_B = "B"; 
    private A a;
    private Hub<B> hubB;
    
    public OAGroupBy() {
    }
    
    public OAGroupBy(A a) {
        setA(a);
    }
    
    @OAOne
    public A getA() {
        return a;
    }
    public void setA(A obj) {
        OAObject hold = this.a;
        fireBeforePropertyChange("A", hold, obj);
        this.a = obj;
        firePropertyChange("A", hold, obj);
    }
    public Hub<B> getHubB() {
        return getHub();
    }
    
    @OAMany
    public Hub<B> getB() {
        return getHub();
    }

    public Hub<B> getHub() {
        if (hubB == null) {
            hubB = getHub(PROPERTY_B);
        }
        return hubB;
    }
}
