package com.viaoa.object;

import com.viaoa.hub.Hub;


/**
 * Used by  OAObjectReflectDelegate.createCopy, copyInto(..) to control how an object is copied.
 * 
 */
public class OACopyCallback {
    
    /**
     * Called when checking to copy owned objects.
     */
    protected boolean shouldCopyOwnedHub(OAObject oaObj, String path, boolean bDefault) {
        return bDefault;
    }
    
    /**
     * Called when adding owned objects to new hub.
     * default is to create a copy of the object;
     */
    protected OAObject createCopy(OAObject oaObj, String path, Hub hub, OAObject currentValue) {
        return OAObjectReflectDelegate.createCopy((OAObject)currentValue, null, this);
        // or: return currentValue.createCopy();
    }

    /**
     * Called when copying a property or LinkType=One
     * Default is to return currentValue.
     */
    protected Object getPropertyValue(OAObject oaObj, String path, Object currentValue) {
        return currentValue;
    }
    
}
