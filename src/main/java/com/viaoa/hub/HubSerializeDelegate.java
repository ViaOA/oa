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

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.object.*;


/**
 * Delegate used for serializing Hub.
 * @author vvia
 *
 */
public class HubSerializeDelegate {
    private static Logger LOG = Logger.getLogger(HubSerializeDelegate.class.getName());

    /**
        Used by serialization to store Hub.
    */
    protected static void _writeObject(Hub thisHub, java.io.ObjectOutputStream stream) throws IOException {
        if (HubSelectDelegate.isMoreData(thisHub)) {
            try {
                OAThreadLocalDelegate.setSuppressCSMessages(true);
                HubSelectDelegate.loadAllData(thisHub);  // otherwise, client will not have the correct datasource
            }
            finally {
                OAThreadLocalDelegate.setSuppressCSMessages(false);         
            }
        }
        stream.defaultWriteObject();
    }
    
    public static int replaceObject(Hub thisHub, OAObject objFrom, OAObject objTo) {
        if (thisHub == null) return -1;
        if (thisHub.data == null) return -1;
        if (thisHub.data.vector == null) return -1;
        int pos = thisHub.data.vector.indexOf(objFrom);
        if (pos >= 0) thisHub.data.vector.setElementAt(objTo, pos);
        return pos;
    }

    public static void replaceMasterObject(Hub thisHub, OAObject objFrom, OAObject objTo) {
        if (thisHub == null) return;
        if (thisHub.datam.masterObject == objFrom) thisHub.datam.masterObject = objTo;
    }
    
    /** qqqqqqqqqqq
     * Used by OAObjectSerializeDelegate, should only be needed to handle some temp "bad" files.
     */
    public static boolean isResolved(Hub thisHub) {
        return (thisHub != null && thisHub.data != null && thisHub.data.vector != null);
    }

    /**
        Used by serialization when reading objects from stream.
        This needs to add the hub to OAObject.hubs, but only if it is not a duplicate (and is not needed)
    */
    protected static Object _readResolve(Hub thisHub) throws ObjectStreamException {
        // 20141115 reworked after changing read/writeObject of hubDataMaster
        for (int i=0; ; i++) {
            Object obj = thisHub.getAt(i);
            if (obj == null) break;

            if (i == 0) {
                if (obj instanceof OAObject) {
                    // dont initialize this hub if the master object is a duplicate.
                    // check by looking to see if this object already belongs to a hub that has the same masterObject/linkinfo
                    if ( OAObjectHubDelegate.isAlreadyInHub((OAObject)obj, thisHub.datam.liDetailToMaster) ) {
                        break; // this hub is a dup and wont be used
                    }
                }
            }
            OAObjectHubDelegate.addHub((OAObject) obj, thisHub);
        }
        
        // 20141116 make sure that masterObject.properties has hub
        // 20141204 added caching, weakRef
        if (thisHub.datam.masterObject != null && thisHub.datam.liDetailToMaster != null) {
            // this will always set the locally found masterObject, and not a duplicate
            Object value = thisHub;
            OALinkInfo liRev = thisHub.datam.liDetailToMaster.getReverseLinkInfo();
            
            if (liRev != null && OAObjectInfoDelegate.cacheHub(liRev, thisHub)) {
                value = new WeakReference(value);
            }
            OAObjectPropertyDelegate.setProperty(thisHub.datam.masterObject, liRev.getName(), value); 
        }
        else {
//qqqqqqqqqqqqqqq
int xx = 4;
xx++;
        }
        return thisHub;
    }
}
