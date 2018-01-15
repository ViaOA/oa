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

import java.io.*;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.sync.OASyncDelegate;


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
        if (thisHub.datam.getMasterObject() == objFrom) {
            thisHub.datam.setMasterObject(objTo);
        }
    }
    
    /** 
     * Used by OAObjectSerializeDelegate
     */
    public static boolean isResolved(Hub thisHub) {
        return (thisHub != null && thisHub.data != null && thisHub.data.vector != null);
    }

    /**
        Used by serialization when reading objects from stream.
        This needs to add the hub to OAObject.hubs, but only if it is not a duplicate (and is not needed)
    */
    protected static Object _readResolve(Hub thisHub) throws ObjectStreamException {
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
        return thisHub;
    }
}
