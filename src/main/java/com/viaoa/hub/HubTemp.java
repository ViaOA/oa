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
    
    public static int getCount(Object hubObject) {
        if (hubObject == null) return 0;
        
        Hashtable hash = getHash(hubObject.getClass());
        synchronized (hash) {
            WeakReference ref = (WeakReference) hash.get(hubObject);
            if (ref == null) return 0;
            HubTemp ht = (HubTemp) ref.get();
            return ht.cnt;
        }
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

