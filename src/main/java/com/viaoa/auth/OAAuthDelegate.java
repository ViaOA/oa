package com.viaoa.auth;

import java.util.ArrayList;

import com.viaoa.object.OAObject;

/**
 * System wide service for getting the current User object.
 * @author vvia
 */
public class OAAuthDelegate {

    private static final ArrayList<OAAuthLookupInterface> al = new ArrayList<>(); 
    
    
    public static OAObject getCurrentUser() {
        OAObject obj = null;
        for (OAAuthLookupInterface ali : al) {
            obj = ali.getCurrentUser();
            if (obj != null) break;
        }
        return obj;
    }
    
    public static void add(OAAuthLookupInterface ali) {
        if (ali != null && !al.contains(ali)) al.add(ali);
    }
    
    
}
