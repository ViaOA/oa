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

/**
 * Utility class, used by HubLeftJoin, as an object that creates a reference to two others objects.
 * This is used by HubCombined to combine two hubs, to create a hub of objects - similar
 * to a database "left join"
 * @author vvia
 *
 * @param <A> left side object
 * @param <B> right side object
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class OALeftJoin<A extends OAObject, B extends OAObject> extends OAObject {
    static final long serialVersionUID = 1L;
    
    public static final String P_A = "A"; 
    public static final String P_B = "B"; 
    public static final String PROPERTY_A = "A"; 
    public static final String PROPERTY_B = "B"; 
    private A a;
    private B b;
    
    public OALeftJoin() {
    }
    
    public OALeftJoin(A a, B b) {
        setA(a);
        setB(b);
    }
    
    public A getA() {
        return a;
    }
    public void setA(A obj) {
        OAObject hold = this.a;
        this.a = obj;
        firePropertyChange("A", hold, obj);
    }

    public B getB() {
        return b;
    }
    public void setB(B obj) {
        OAObject hold = this.b;
        this.b = obj;
        firePropertyChange("B", hold, obj);
    }
}
