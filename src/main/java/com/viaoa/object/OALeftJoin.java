/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
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
