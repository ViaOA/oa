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
package com.viaoa.hub;
import java.util.*;
import java.lang.ref.*;  // java1.2

/** 
    Used by OA components to create temporary hubs when using Object without a Hub.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class HubTemp {
    Hub hub;
    Object object;
    int cnt;

    
    /** 
        Temp Hub objects used when a Hub is needed for a OAObject that does not have a Hub.
    */
    private static transient Hashtable hashClass = new Hashtable();
    
    static Hashtable getHash(Class c) {
        if (c == null) return null;
        Hashtable h = (Hashtable) hashClass.get(c);
        if (h == null) {
            synchronized (hashClass) {
                // make sure it hasnt been created by another thread
                h = (Hashtable) hashClass.get(c);
                if (h == null) {
                    h = new Hashtable();
                    hashClass.put(c, h);
                }
            }
        }
        return h;
    }
    
    public static Hub createHub(Object hubObject) {
        if (hubObject == null) return null;
        
        Hashtable hash = getHash(hubObject.getClass());
        
        HubTemp ht = null;
        synchronized (hash) {
            WeakReference ref = (WeakReference) hash.get(hubObject);
            if (ref != null) ht = (HubTemp) ref.get();
            if (ht != null) ht.cnt++;
            else {
                ht = new HubTemp();
                ht.hub = new Hub(hubObject.getClass());
                ht.object = hubObject;
                ht.cnt = 1;
                ht.hub.add(hubObject);
                ht.hub.setActiveObject(0);
                hash.put(hubObject, new WeakReference(ht));
            }
        }
        return ht.hub;
    }

    public static synchronized void deleteHub(Object hubObject) {
        if (hubObject == null) return;
        Hashtable hash = getHash(hubObject.getClass());

        WeakReference ref = (WeakReference) hash.get(hubObject);  // java1.2
        if (ref == null) return;
        
        HubTemp ht = (HubTemp) ref.get(); 

        if (ht == null || (ht.object == hubObject && (--ht.cnt) == 0) ) hash.remove(hubObject);
    }
    
    public static int getCount() {
        Enumeration enumx = hashClass.elements();
        int cnt = 0;
        for ( ;enumx.hasMoreElements(); ) {
            Hashtable h = (Hashtable) enumx.nextElement();
            cnt += h.size();
        }    
        return cnt;
    }
}

