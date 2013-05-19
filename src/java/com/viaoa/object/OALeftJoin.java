package com.viaoa.object;

import com.viaoa.annotation.OAClass;

/**
 * Utility class, used for an object that creates a reference to two others objects.
 * This is used by HubCombined to combine two hubs, to create a hub of objects similar
 * to a database "left join"
 * @author vvia
 *
 * @param <A> left side object
 * @param <B> right side object
 */
@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class OALeftJoin<A extends OAObject, B extends OAObject> extends OAObject {
    static final long serialVersionUID = 1L;
    
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
