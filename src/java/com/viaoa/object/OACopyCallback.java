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
