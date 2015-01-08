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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;
import com.viaoa.ds.OADataSource;

/**
 * Delegate that manages deleting an object from a Hub.
 * @author vvia
 *
 */
public class HubDeleteDelegate {

    public static void deleteAll(Hub thisHub) {
        boolean b = thisHub.getSize() > 0 && HubCSDelegate.deleteAll(thisHub);
        try {
            if (b) OAThreadLocalDelegate.setSuppressCSMessages(true);
            OACascade cascade = new OACascade();
            deleteAll(thisHub, cascade);
        }
        finally {
            if (b) OAThreadLocalDelegate.setSuppressCSMessages(false);
            OARemoteThreadDelegate.startNextThread(); 
        }
    }
    
    
    public static boolean isDeletingAll(Hub thisHub) {
        return OAThreadLocalDelegate.isDeleting(thisHub);
    }
    
    public static void deleteAll(Hub thisHub, OACascade cascade) {
        if (cascade.wasCascaded(thisHub, true)) return;
        try {
            OAThreadLocalDelegate.setDeleting(thisHub, true);
            OAThreadLocalDelegate.lock(thisHub);
            _deleteAll(thisHub, cascade);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
            OAThreadLocalDelegate.setDeleting(thisHub, false);
        }
    }

    private static void _deleteAll(Hub thisHub, OACascade cascade) {
        boolean bIsOa = thisHub.isOAObject();
        Object objLast = null;
        int pos = 0;

        // 20121005 need to check to see if a link table was used for a 1toM, where createMethod for One is false
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(thisHub);
        OALinkInfo liRev = null;
        OAObject masterObj = null;
        OADataSource dataSource = null;
        if (bIsOa && li != null && li.getType() == li.ONE) {
            Method method = OAObjectInfoDelegate.getMethod(li);
            if (method == null || ((method.getModifiers() & Modifier.PRIVATE) == 1) ) {
                // uses a link table, need to delete from link table first
                liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);

                masterObj = HubDetailDelegate.getMasterObject(thisHub);
                if (masterObj != null) dataSource = OADataSource.getDataSource(masterObj.getClass());
            }
        }        

        for (; ; ) {
            Object obj = thisHub.elementAt(pos);
            if (obj == null) break;

            if (obj == objLast) {
                // object was not deleted
                pos++;
                continue;
            }
            objLast = obj;
            
            // 20150107
            HubAddRemoveDelegate.remove(thisHub, obj, false, true, true, true, true, true);
            //was:thisHub.remove(obj);  // oaobject.delete will remove object from hubs, this "remove" will make sure that recursive owner reference is removed. 
       
            // 20121005
            if (dataSource != null) {
                dataSource.updateMany2ManyLinks(masterObj, null, new OAObject[] {(OAObject)obj}, liRev.getName());
            }
            
            if (bIsOa) {
                OAObjectDeleteDelegate.delete((OAObject)obj, cascade);
            }
            else {
            	if (thisHub.isOAObject() && obj instanceof OAObject) OAObjectDSDelegate.delete((OAObject)obj);
            }
        }
    	HubDelegate._updateHubAddsAndRemoves(thisHub, cascade);
     	
    	thisHub.setChanged(false); // removes all vecAdd, vecRemove objects
    }
}



