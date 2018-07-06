package com.viaoa.object;

import java.util.ArrayList;

public class OASiblingHelperDelegate {

    public static void onGetObjectReference(final OAObject obj, final String linkPropertyName) {

        ArrayList<OASiblingHelper> al = OAThreadLocalDelegate.getSiblingHelpers();
        if (al == null) return;
        
        for (OASiblingHelper sh : al) {
            sh.onGetReference(obj, linkPropertyName);
        }
        
    }
    
}
