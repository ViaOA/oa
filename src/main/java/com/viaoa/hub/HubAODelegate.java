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

import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

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
    private static Logger LOG = Logger.getLogger(HubAODelegate.class.getName());
    /**
	    Navigational method that will set the position of the active object.
	    GUI components use this to recognize which object that they are working with.
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
	        /* not needed, used for debugging 20150920
	        if (object instanceof Hub) {
	            LOG.warning("trying to set active object using a AO=hub, thisHub="+thisHub+", AO="+object);
	            return;
	        }
	        */
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

	    // 20140421 for detailHub where link.type=ONE
	    OALinkInfo li = thisHub.datam.liDetailToMaster;
	    OALinkInfo liRev;
	    if (li != null) {
            liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
	    }
	    else {
            liRev = HubDetailDelegate.getLinkInfoFromMasterObjectToDetail(thisHub);
	    }
	    if (liRev != null) {
            if (liRev.getType() == li.ONE && bUpdateLink) {  // 20171117
	        //was: if (liRev.getType() == li.ONE) {
                Object objMaster = HubDetailDelegate.getMasterObject(thisHub);
                if (objMaster != null) {
                    Object value = OAObjectReflectDelegate.getProperty((OAObject)objMaster, liRev.getName());
                    if (value != object) {
                        if (objMaster != null) {
                            OAObjectReflectDelegate.setProperty((OAObject)objMaster, liRev.getName(), object, null);
                        }
                    }
                }
	        }
	    }
	    
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
	

    /** Main setActiveObject
        Naviagational method that sets the current active object.
        This is the central routine for changing the ActiveObject.  It is used by setPos,
        setActiveObject(int), setActiveObject(object), setActiveObject(object,boolean), replace, setSharedHub
        @param bCalledByShareHub true if the active object is being called when a Hub is being shared with an existing hub.  This is so that all of the shared hubs dont recv an event.
    */
    protected static void setActiveObject(final Hub thisHub, Object object, int pos, boolean bUpdateLink, boolean bForce, boolean bCalledByShareHub) {
        if (thisHub.dataa.activeObject == object && !bForce) return;
        if (thisHub.datau.isUpdatingActiveObject()) return;
    
        OAThreadLocalDelegate.lock(thisHub);
        Object origActiveObject = thisHub.dataa.activeObject;
        thisHub.dataa.activeObject = object;
        OAThreadLocalDelegate.unlock(thisHub);
        
        thisHub.datau.setUpdatingActiveObject(true);
        HubDetailDelegate.updateAllDetail(thisHub, bUpdateLink);
        if (bUpdateLink) HubLinkDelegate.updateLinkProperty(thisHub, object, pos);
        thisHub.datau.setUpdatingActiveObject(false);
        
        // Now call for all sharedHubs with same "dataa"
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
                h.datau.setUpdatingActiveObject(true);
                HubDetailDelegate.updateAllDetail(h, bUpdateLink);
                if (bUpdateLink) HubLinkDelegate.updateLinkProperty(h,object,pos);
                h.datau.setUpdatingActiveObject(false);
            }
        }

        // must send event After updateAllDetail()
        // this will send event to all sharedHubs with same "dataa" only
        HubEventDelegate.fireAfterChangeActiveObjectEvent(thisHub, object, pos, !bCalledByShareHub);

        for (int i=0; object != null && i<hubs.length; i++) {
            Hub h = hubs[i];
            if (h.dataa == thisHub.dataa) {
                if (h.datau.getAddHub() != null) {
                    if (h.datau.getAddHub().getObject(object) == null) h.datau.getAddHub().add(object);
                    setActiveObject(h.datau.getAddHub(), object);
                }
            }
        }
    }
}


