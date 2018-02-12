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
	*/
	public static Hub getRootHub(final Hub thisHub) {
		
		OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.data.getObjectInfo(), OALinkInfo.ONE);
	    // 1: must be recursive
	    if (liRecursive == null) return null;

	    // 2: check for root hub
	    Hub h = OAObjectInfoDelegate.getRootHub(thisHub.data.getObjectInfo());
	    if (h != null) return h;
	    
	    // 3: get dm
        // 20120717 could be more then one master hub available, find the one that owns this object
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub hx) {
                return (hx.datam.getMasterHub() != null);
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
            return OAObjectInfoDelegate.getRootHub(thisHub.data.getObjectInfo());
        }
        if (thisHub.datam.getMasterObject() == null && thisHub.datam.getMasterHub() == null) {
            return OAObjectInfoDelegate.getRootHub(thisHub.data.getObjectInfo());
        }
        if (thisHub.datam.getMasterObject() == null) {
            if (thisHub.datam.getMasterHub() != null) {
                Class mc = thisHub.datam.getMasterHub().getObjectClass();
                if (mc != null) {
                    if (mc.equals(thisHub.getObjectClass())) {
                        h = getRootHub(thisHub.datam.getMasterHub());
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
            return OAObjectInfoDelegate.getRootHub(thisHub.data.getObjectInfo());
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
	    if ( dm.liDetailToMaster != OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.data.getObjectInfo(), OALinkInfo.ONE) ) {
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
	        	return (Hub) OAObjectReflectDelegate.getProperty((OAObject)dm.getMasterObject(), OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster).getName());
	        }
	
	        // the linkInfo for the parent is not the owner or a recursive parent
	        // The root hub needs to be manually set by calling Hub.setRootHub,
	        //     since the recursive hub does not have an owner object
	        return OAObjectInfoDelegate.getRootHub(thisHub.data.getObjectInfo());
	    }
	
	
	    // 6: dm.masterObject is the same as this class - recursive parent hub
	    //    use it to get the owner object and then the root hub (from owner object)
	    // find owner link
	    OALinkInfo linkOwner = OAObjectInfoDelegate.getLinkToOwner(thisHub.data.getObjectInfo());
	    if (linkOwner != null) {
	        OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(linkOwner);
	        if (liRev != null && liRev.getType() == OALinkInfo.MANY) {
	            // get owner object:
	            Object owner = OAObjectReflectDelegate.getProperty((OAObject)dm.getMasterObject(), linkOwner.getName());
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
		OAObjectInfoDelegate.setRootHub(thisHub.data.getObjectInfo(), b?thisHub:null);
	}
	
	
}
