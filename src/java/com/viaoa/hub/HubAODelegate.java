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

import java.util.HashMap;
import java.util.HashSet;

import com.viaoa.object.*;
import com.viaoa.util.OAFilter;


/**
 * Main delegate used for working with the Active Object for a Hub.
 * All methods that have an "_" prefix should not be called directly, as there is
 * a calling method that should be used, that performs additional functionality.
 * If a method does not have the "_" prefix and is accessible, then it is ok
 * to call it, but will most likely have a matching method name in the Hub class.
 * @author vincevia
 */
public class HubAODelegate {

    /**
	    Navigational method that will set the position of the active object.
	    GUI components use this to recongnize which object that they are working with.
	    @param pos position to set.  If > size() or < 0 then it will be set to null, and getPos() will return -1
	    @see Hub#getActiveObject
	 */
	public static Object setActiveObject(Hub thisHub, int pos) {
	    return setActiveObject(thisHub, pos, true, false, false); //bUpdateLink,bForce,bCalledByShareHub
	}
	
    /**
	    Navigational method to set the active object.
	    @param object Object to make active.  If it does not exist in Hub, then active object will be set to null
	    @see Hub#getActiveObject
	 */
	public static void setActiveObject(Hub thisHub, Object object) {
	    if (object != null) {
	        object = HubDelegate.getRealObject(thisHub, object);
	    }
	    setActiveObject(thisHub, object, true, true, false);
	}
    /**
	    Change active object even if it has not changed.  By default, if the active object is the same, it
	    will not reset it.
	 */
	 public static void setActiveObjectForce(Hub thisHub, Object object) {
		 if (object != null) {
	        object = HubDelegate.getRealObject(thisHub, object);
		 }
		 setActiveObject(thisHub, object, true, true, true);
	 }
	
    /**
	    Navigational method that is another form of setActiveObject() that will adjust the master hub.
	    This is when this Hub is a detail Hub and the object is not found in this hub.  This will use
	    the object to find what the master object should be and then change the active object in the
	    Master Hub, which will cause this Hub to be refreshed, allowing the object to be found. <i>Makes sense?</i>
	    <p>
	    @param adjustMaster - see getPos(Object, boolean) for notes
	    @see Hub#set(Object,boolean)
	*/
	public static void setActiveObject(Hub thisHub, Object object, boolean adjustMaster) {
	    if (object != null) {
	        object = HubDelegate.getRealObject(thisHub, object);
	    }
	    setActiveObject(thisHub, object, adjustMaster, true, false); // adjMaster, updateLink, force
	}
	
	public static void setActiveObject(Hub thisHub, Object object, boolean adjustMaster, boolean bUpdateLink, boolean bForce) {
	    int pos = HubDataDelegate.getPos(thisHub, object, adjustMaster, bUpdateLink);
	    setActiveObject(thisHub, (pos<0?null:object), pos, bUpdateLink, bForce, false);
	}
	
    public static void setActiveObject(Hub thisHub, Object object, int pos) {
        setActiveObject(thisHub, object, pos, true, false, false); // bUpdateLink,bForce
    }
	
	protected static Object setActiveObject(Hub thisHub, int pos, boolean bUpdateLink, boolean bForce, boolean bCalledByShareHub) {
        Object ho;
        if (pos < 0) ho = null;
        else ho = HubDataDelegate.getObjectAt(thisHub, pos);

        if (ho == null) {
            setActiveObject(thisHub, null, -1, bUpdateLink, bForce, bCalledByShareHub);
        }
        else {
            setActiveObject(thisHub, ho, pos, bUpdateLink, bForce, bCalledByShareHub);
        }
        return ho;
    }
	
/* test	
static HashMap<Hub, Integer> hs = new HashMap<Hub, Integer>(397);
public static void clearCache() {
    hs.clear();
}
*/
    /** Main setActiveObject
	    Naviagational method that sets the current active object.
	    This is the central routine for changing the ActiveObject.  It is used by setPos,
	    setActiveObject(int), setActiveObject(object), setActiveObject(object,boolean), replace, setSharedHub
	    @param bCalledByShareHub true if the active object is being called when a Hub is being shared with an existing hub.  This is so that all of the shared hubs dont recv an event.
	*/
	protected static void setActiveObject(final Hub thisHub, Object object, int pos, boolean bUpdateLink, boolean bForce, boolean bCalledByShareHub) {
		if (thisHub.dataa.activeObject == object && !bForce) return;
	
	    if (thisHub.datau.bUpdatingActiveObject) return;
	
	    Object origActiveObject = thisHub.dataa.activeObject;
	    thisHub.dataa.activeObject = object;
	    
	    // notify all HubDetail links
	    //  if OAObject = null, then set all links to null
	    try {
	    	
	    	thisHub.datau.bUpdatingActiveObject = true;
	        HubDetailDelegate.updateAllDetail(thisHub, bUpdateLink);
	        if (bUpdateLink) HubLinkDelegate.updateLinkProperty(thisHub, object, pos);
	        thisHub.datau.bUpdatingActiveObject = false;
	
	        // Now call for all sharedHubs with same "dataa"
	        // 20120716
	        OAFilter<Hub> filter = new OAFilter<Hub>() {
	            @Override
	            public boolean isUsed(Hub h) {
	                return h.dataa == thisHub.dataa; 
	            }
	        };
	        Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
	
	        for (int i=0; i<hubs.length; i++) {
	            Hub h = hubs[i];
	            if (h != thisHub && h.dataa == thisHub.dataa) {
                    h.datau.bUpdatingActiveObject = true;
                    HubDetailDelegate.updateAllDetail(h, bUpdateLink);
                    if (bUpdateLink) HubLinkDelegate.updateLinkProperty(h,object,pos);
                    h.datau.bUpdatingActiveObject = false;
	            }
	        }
	
	        // must send event After updateAllDetail()
	        // this will send event to all sharedHubs with same "dataa" only
		    HubEventDelegate.fireAfterChangeActiveObjectEvent(thisHub, object, pos, !bCalledByShareHub);
	
	        for (int i=0; object != null && i<hubs.length; i++) {
	            Hub h = hubs[i];
	            if (h.dataa == thisHub.dataa) {
	                if (h.datau.addHub != null) {
	                    if (h.datau.addHub.getObject(object) == null) h.datau.addHub.add(object);
	                    setActiveObject(h.datau.addHub, object);
	                }
	            }
	        }
	    }
	    finally {
	        thisHub.datau.bUpdatingActiveObject = false;  // just in case it wasnt executed
	    }
	}

}


