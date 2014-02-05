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


import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.util.OAFilter;


/**
 * Delegate used for getting the root hub of a recursive hub.
 * @author vvia
 *
 */
public class HubRootDelegate {
    private static Logger LOG = Logger.getLogger(HubRootDelegate.class.getName());

    /**
	    If this is a recursive hub with an owner, then the root hub will be returned, else null.
	    @see #setRoot
	    @see #setRootHub
	    @see OALinkInfo
	*/
	public static Hub getRootHub(final Hub thisHub) {
		
		OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.datau.objectInfo, OALinkInfo.ONE);
	    // 1: must be recursive
	    if (liRecursive == null) return null;

	    // 2: check for root hub
	    Hub h = OAObjectInfoDelegate.getRootHub(thisHub.datau.objectInfo);
	    if (h != null) return h;
	    
	    // 3: get dm
        // 20120717 could be more then one master hub available, find the one that owns this object
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub hx) {
                return (hx.datam.masterHub != null);
            }
        };
        Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
        HubDataMaster dm = null;
        for (int i=0; hubs != null && i < hubs.length; i++) {
            dm = hubs[i].datam;
            if (dm.liDetailToMaster == null) continue;
            OALinkInfo rev = OAObjectInfoDelegate.getReverseLinkInfo(hubs[i].datam.liDetailToMaster);
            if (rev != null && rev.isOwner()) break; 
        }
        if (dm == null) dm = thisHub.datam;
        // was: HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
	    
	    
	    // 20120304 added other cases on how to find the root hub         
        if (dm.liDetailToMaster == null) {
            return OAObjectInfoDelegate.getRootHub(thisHub.datau.objectInfo);
        }
        if (thisHub.datam.masterObject == null && thisHub.datam.masterHub == null) {
            return OAObjectInfoDelegate.getRootHub(thisHub.datau.objectInfo);
        }
        if (thisHub.datam.masterObject == null) {
            if (thisHub.datam.masterHub != null) {
                Class mc = thisHub.datam.masterHub.getObjectClass();
                if (mc != null) {
                    if (mc.equals(thisHub.getObjectClass())) {
                        h = getRootHub(thisHub.datam.masterHub);
                        if (h != null) return h;
                    }
                    else {
                        // could be owner / master Hub
                        if (OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster).getOwner()) {
                            return thisHub; // thisHub is a detail from the owner.  When the owner hub AO is changed, then thisHub will have root 
                        }
                    }
                }
            }
            return OAObjectInfoDelegate.getRootHub(thisHub.datau.objectInfo);
        }
        // End 20120304     
	    
/*was	    
	    // 4: check to see if there is a valid masterObject - must have a link to it
	    if (thisHub.datam.masterObject == null || dm.liDetailToMaster == null) {
	        // does not belong to a owner or master object.
	        // The root hub needs to be manually set by calling Hub.setRootHub,
	        //     since the recursive hub does not have an owner object
	        return OAObjectInfoDelegate.getRootHub(thisHub.datau.objectInfo);
	    }
*/

	    
	    
	    // 5: if parent is not recursive - if the LinkInfos are different
	    if ( dm.liDetailToMaster != OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.datau.objectInfo, OALinkInfo.ONE) ) {
	        // if dm.masterObject is owner, then it is owner
	        OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster);
	        if (rli == null) {
	            LOG.warning("cant find reverse linkInfo, hub="+thisHub);
	        }
	        
	        if (rli != null && rli.getOwner()) {
	            // found the root hub and owner
	            // cant use the masterHub, need to get the "real" detail hub of master object
	            //   For recursive hubs that are linked, the master (owner) might not be using the root hub.
	            //   By getting the hub value of the masterObject, it will call its hub getMethod, which will be the root hub
	        	return (Hub) OAObjectReflectDelegate.getProperty((OAObject)dm.masterObject, OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster).getName());
	        }
	
	        // the linkInfo for the parent is not the owner or a recursive parent
	        // The root hub needs to be manually set by calling Hub.setRootHub,
	        //     since the recursive hub does not have an owner object
	        return OAObjectInfoDelegate.getRootHub(thisHub.datau.objectInfo);
	    }
	
	
	    // 6: dm.masterObject is the same as this class - recursive parent hub
	    //    use it to get the owner object and then the root hub (from owner object)
	    // find owner link
	    OALinkInfo linkOwner = OAObjectInfoDelegate.getLinkToOwner(thisHub.datau.objectInfo);
	    if (linkOwner != null) {
	        OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(linkOwner);
	        if (liRev != null && liRev.getType() == OALinkInfo.MANY) {
	            // get owner object:
	            Object owner = OAObjectReflectDelegate.getProperty((OAObject)dm.masterObject, linkOwner.getName());
	            if (owner != null) {
	                Object root = OAObjectReflectDelegate.getProperty((OAObject)owner, liRev.getName());
	                if (!(root instanceof Hub)) throw new RuntimeException("Hub.getRootHub() method from owner object not returning a Hub.");
	                return (Hub) root;
	            }
	        }
	    }
	
	    return null;
	}
	
	
    /**
	    Used for recursive object relationships, to set the root Hub.
	    A recursive relationship is where an object has a reference to many children (Hub) of objects
	    that are the same class.
	    <p>
	    The root is the Hub that where the
	    reference to a parent object is null.  OAObject and Hub will automatically keep/put objects in the
	    correct Hub based on the parent reference.
	    <p>
	    Note: the root Hub is automatically set when a master object owns a Hub.<br>
	    Example:<br>
	    If a Class "Test" is recursive and a Class Employee has many "Tests", then each Employee object
	    will own a recursive list of "Test" Hubs.  Each "Test" object under the Employee object will
	    have a reference to the Employee object.
	    <p>
	    Calls OAObjectInfo to set this hub as the root hub for other recursive hubs in same object class.
	    If this is not a recursive hub then an exception will be thrown.
	    @param b if true then set thisHub as the root, else remove as the rootHub.
	*/
	public static void setRootHub(Hub thisHub, boolean b) {
		OAObjectInfoDelegate.setRootHub(thisHub.datau.objectInfo, b?thisHub:null);
	}
	
	
}
