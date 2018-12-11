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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectHubDelegate;
import com.viaoa.util.OAFilter;

/**
 * Delegate used for sharing hub. 
 * @author vvia
 *
 */
public class HubShareDelegate {
    private static Logger LOG = Logger.getLogger(HubShareDelegate.class.getName());

    /**
        List of Hubs that are sharing the same objects as this Hub.  Each of these Hubs will
        have the same HubData object.  If the active object is also being shared, then
        the HubDataActive object will also be the same.
    */
    public static Hub[] getAllSharedHubs(Hub thisHub) {
        return getAllSharedHubs(thisHub, false, null);
    }
    public static Hub[] getAllSharedHubs(Hub thisHub, boolean bChildrenOnly) {
        return getAllSharedHubs(thisHub, bChildrenOnly, null);
    }
    public static Hub[] getAllSharedHubs(Hub thisHub, OAFilter<Hub> filter) {
        return getAllSharedHubs(thisHub, false, filter);
    }
    public static Hub[] getAllSharedHubs(Hub thisHub, boolean bChildrenOnly, OAFilter<Hub> filter) {
        return getAllSharedHubs(thisHub, bChildrenOnly, filter, false, false);
    }

    /** 
     * Used to get all Hubs that share the same data.
     * @param thisHub
     * @param bChildrenOnly only include Hubs that are shared from thisHub.  Otherwise, go to root of shared hubs
     * @param filter used to determine if a shared hub that is found should be included.
     * @param bIncludeFilteredHubs if true then HubFilter will also be include
     * @return array (could be size 0) of found hubs, including thisHub.
     */
    public static Hub[] getAllSharedHubs(Hub thisHub, boolean bChildrenOnly, OAFilter<Hub> filter, boolean bIncludeFilteredHubs, boolean bOnlyIfSharedAO) {
        if (thisHub == null) return null;

        Hub h = thisHub;
        if (!bChildrenOnly) {
            h = getMainSharedHub(h);
        }            
        ArrayList<Hub> alHub = new ArrayList<Hub>(10);
        _getAllSharedHubs(h, thisHub, alHub, filter, 0, bIncludeFilteredHubs, bOnlyIfSharedAO, bIncludeFilteredHubs);
        Hub[] hubs = new Hub[alHub.size()];
        alHub.toArray(hubs);
        return hubs;
    }
    
        
    private static void _getAllSharedHubs(final Hub hub, final Hub findHub, final ArrayList<Hub> alHub, final OAFilter<Hub> filter, final int cnter, final boolean bIncludeFilteredHubs, boolean bOnlyIfSharedAO, boolean bIncludeHubShareAO) {
        if (filter == null || filter.isUsed(hub)) {
            alHub.add(hub);
        }
        
        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(hub);
        for (int i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            if (bOnlyIfSharedAO && !HubShareDelegate.isUsingSameSharedAO(findHub, h2)) continue;
            _getAllSharedHubs(h2, findHub, alHub, filter, cnter+1, bIncludeFilteredHubs, bOnlyIfSharedAO, bIncludeHubShareAO);
        }        
        
        if (!bIncludeFilteredHubs || cnter > 0) return;
        HubFilter hc = getHubFilter(hub);
        if (hc != null) {
            if (!bOnlyIfSharedAO || hc.isSharingAO()) {
                Hub mh = hc.getMasterHub();
                Hub h = getMainSharedHub(mh);
                // note: use "mh" instead of findHub, since it is going thru a hubFiler
                _getAllSharedHubs(h, mh, alHub, filter, 0, bIncludeFilteredHubs, bOnlyIfSharedAO, bIncludeHubShareAO);
            }
        }
        
        if (!bIncludeHubShareAO) return;
        HubShareAO hs = getHubShareAO(hub);
        if (hs != null) {
            Hub mh = hs.getHub2();
            if (mh == hub) mh = hs.getHub1();
            Hub h = getMainSharedHub(mh);
            // note: use "mh" instead of findHub, since it is going thru a hubFilter
            _getAllSharedHubs(h, mh, alHub, filter, 0, bIncludeFilteredHubs, bOnlyIfSharedAO, (h!=mh));
        }
    }

    public static HubCopy getHubCopy(Hub thisHub) {
        Hub h = HubShareDelegate.getMainSharedHub(thisHub);
        if (h.datam.getMasterObject() != null || h.datam.getMasterHub() != null) {
            // filtered hubs will not have a master
            return null;
        }
        
        // find a HubFilter in the listener list
        HubListener[] hls = HubEventDelegate.getHubListeners(h);
        if (hls != null) {
            for (HubListener hl : hls) {
                if (hl instanceof HubCopy) {
                    return (HubCopy) hl;
                }
            }
        }
        return null;
    }
    
    public static HubFilter getHubFilter(Hub thisHub) {
        Hub h = HubShareDelegate.getMainSharedHub(thisHub);
        if (h.datam.getMasterObject() != null || h.datam.getMasterHub() != null) {
            // filtered hubs will not have a master
            return null;
        }
        
        // find a HubFilter in the listener list
        HubListener[] hls = HubEventDelegate.getHubListeners(h);
        if (hls != null) {
            for (HubListener hl : hls) {
                if (hl instanceof HubFilter) {
                    return (HubFilter) hl;
                }
            }
        }
        return null;
    }

    public static HubShareAO getHubShareAO(Hub thisHub) {
        Hub h = HubShareDelegate.getMainSharedHub(thisHub);
        
        // find a HubShareAO in the listener list
        HubListener[] hls = HubEventDelegate.getHubListeners(h);
        if (hls == null || hls.length == 0) return null;
        for (HubListener hl : hls) {
            if (hl instanceof HubShareAO) {
                return (HubShareAO) hl;
            }
        }
        return null;
    }

    
    // 20131117
    public static Hub getSharedHub(final Hub thisHub, boolean bIncludeFilteredHubs, boolean bOnlyIfSharedAO) {
        if (thisHub == null) return null;

        if (thisHub.datau.getSharedHub() != null) {
            if (bOnlyIfSharedAO && !HubShareDelegate.isUsingSameSharedAO(thisHub, thisHub.datau.getSharedHub())) {
                return null;
            }
            return thisHub.datau.getSharedHub();
        }
        if (!bIncludeFilteredHubs) return null;
        
        // a HubCopy could also be sharing the AO
        HubCopy hc = getHubCopy(thisHub);
        if (hc != null) {
            if (!bOnlyIfSharedAO || hc.isSharingAO()) { 
                return hc.getMasterHub();
            }
        }
        HubShareAO hs = getHubShareAO(thisHub);
        if (hs != null) {
            Hub mh = hs.getHub2();
            if (mh == thisHub) mh = hs.getHub1();
            return mh;
        }
        return null;
    }
    
    // 20131116
    public static Hub getFirstSharedHub(Hub thisHub, OAFilter<Hub> filter, boolean bIncludeFilteredHubs, boolean bOnlyIfSharedAO) {
        Hub h = getMainSharedHub(thisHub);
        return _getFirstSharedHub(h, thisHub, filter, bIncludeFilteredHubs, 0, bOnlyIfSharedAO, bIncludeFilteredHubs);
    }
    
    private static Hub _getFirstSharedHub(
        final Hub thisHub, final Hub findHub, 
        final OAFilter<Hub> filter, final boolean bIncludeFilteredHubs, 
        final int cnter, boolean bOnlyIfSharedAO, boolean bIncludeHubShareAO) {
    
        if (filter == null) return thisHub;
        
        // first try a quickcheck on the main shared hub
        if (!bOnlyIfSharedAO || HubShareDelegate.isUsingSameSharedAO(findHub, thisHub)) {
            if (filter.isUsed(thisHub)) {
                return thisHub;
            }
        }

        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(thisHub);
        for (int i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            
            Hub hx = _getFirstSharedHub(h2, findHub, filter, bIncludeFilteredHubs, cnter+1, bOnlyIfSharedAO, bIncludeHubShareAO);
            if (hx != null) return hx;
        }
        if (!bIncludeFilteredHubs || cnter > 0) return null;
        
        // not found, check to see if there is a HubCopy that is shared
        HubCopy hc = getHubCopy(thisHub);
        if (hc != null) {
            if (!bOnlyIfSharedAO || hc.isSharingAO()) {
                Hub mh = hc.getMasterHub();
                Hub h = getMainSharedHub(mh);
                // note: use "mh" instead of findHub, since this is going thru a hubFilter
                Hub hx = _getFirstSharedHub(h, mh, filter, bIncludeFilteredHubs, 0, bOnlyIfSharedAO, bIncludeHubShareAO);
                if (hx != null) return hx;
            }
        }
        
        if (!bIncludeHubShareAO) return null;
        HubShareAO hs = getHubShareAO(thisHub);
        if (hs != null) {
            Hub mh = hs.getHub2();
            if (mh == thisHub) mh = hs.getHub1();
            Hub h = getMainSharedHub(mh);
            // note: use "mh" instead of findHub, since this is going thru a hubFilter
            boolean b = ((mh != h) && (mh.dataa != h.dataa));
            Hub hx = _getFirstSharedHub(h, mh, filter, bIncludeFilteredHubs, 0, bOnlyIfSharedAO, b);
            if (hx != null) return hx;
        }
        return null;
    }
    
    // find the root Hub that is shared
    public static Hub getMainSharedHub(Hub hub) {
    	Hub h = hub;
    	for (;;) {
    		Hub hx = h.getSharedHub();
    		if (hx == null) break;
    		h = hx;
    	}
    	return h;
    }
    
    public static boolean isUsingSameSharedHub(Hub hub1, Hub hub2) {
    	if (hub1 == null || hub2 == null) return false;
    	return hub1.data == hub2.data;
    }

    public static boolean isUsingSameSharedAO(Hub hub1, Hub hub2) {
        return isUsingSameSharedAO(hub1, hub2, false);
    }
    public static boolean isUsingSameSharedAO(Hub hub1, Hub hub2, boolean bIncludeFilteredHubs) {
    	if (hub1 == null || hub2 == null) return false;
    	if (hub1.dataa == hub2.dataa) return true;
    	if (!bIncludeFilteredHubs) return false;
    	
    	Hub[] hs1 = getAllSharedHubs(hub1, false, null, true, true);
        Hub[] hs2 = getAllSharedHubs(hub2, false, null, true, true);
        
        for (Hub h1 : hs1) {
            for (Hub h2 : hs2) {
                if (h1 == h2) return true;
            }
        }
    	return false;
    }

    
	protected static void syncSharedHubs(Hub thisHub, boolean bShareActiveObject, HubDataActive daOld, HubDataActive daNew, boolean bUpdateLink) {
	    // all shared hubs need to use same data
	    Hub[] hubs = getAllSharedHubs(thisHub, true);  // 201809123 added "true" so that other details using core hub would not be changed
	    for (int i=0; i<hubs.length; i++) {
	        if (hubs[i] == thisHub) continue;
	        hubs[i].data = thisHub.data;  // use same data
            hubs[i].datam = thisHub.datam;  // 20171218
	        if (bShareActiveObject) {
	            // all hubs that are shared with the "dHub" need to have dataa shared
	            if (hubs[i].dataa == daOld) hubs[i].dataa = daNew;
	        }
	        else {
	        	if (hubs[i] != thisHub && hubs[i].dataa != thisHub.dataa) {
	        		if (hubs[i].dataa.activeObject != null && !hubs[i].contains(hubs[i].dataa.activeObject)) {
	        			// make sure that it is not linked
	        		    //   20120505 note: it could have a detail that is linked, so bUpdateLink was added so that the linked to prop wont be changed
	        			if (hubs[i].datau.getLinkToHub() == null) {
	        			    // 20120505 added new arg for bUpdateDetail
	        			    HubAODelegate.setActiveObject(hubs[i], null, false, bUpdateLink, false);  // adjustMaster, bUpdateLink, bForce	        			    
	        				// was: hubs[i].setAO(null); 
	        			}
	        		}
	        	}
	        }
	    }
	}

	// 20140501 similiar to setSharedHubAfterRemove(..)
    protected static void setSharedHubsAfterRemoveAll(Hub thisHub) {
        thisHub.dataa.activeObject = null;
        HubAODelegate.setActiveObject(thisHub, -1, false, false, false); // bUpdateLink, bForce, bCalledByShareHub

        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(thisHub);
        for (int i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            setSharedHubsAfterRemoveAll(h2);
        }        
    }
	

    /**
	    Used to set the active object in all shared Hubs after an object is removed.
	    Used bNullOnRemove to determine which object to make the active object.
	    Note: If Hub is using a Link Hub, then active object is not set.
	*/
	protected static void setSharedHubsAfterRemove(Hub thisHub, Object objRemoved, int posRemoved) {
	    if (thisHub.dataa.activeObject == objRemoved) {
	        /* this must be set to null. Otherwise, setActiveObject
	           could fail when it sends out event.
	        */
	    	thisHub.dataa.activeObject = null;
	        if (thisHub.getSize() == 0 || thisHub.getLinkHub(true) != null || thisHub.datau.isNullOnRemove()) {
	            // 20120505 dont update a linked value that has already been set  
	            HubAODelegate.setActiveObject(thisHub, -1, false, true, false); // bUpdateLink, bForce, bCalledByShareHub
                // was: HubAODelegate.setActiveObject(thisHub, -1, true, true,false); // bUpdateLink,bForce,bCalledByShareHub
	        }
	        else {
	        	// 20101228
	            if (thisHub.getSize() > posRemoved) { 
	                HubAODelegate.setActiveObject(thisHub, posRemoved, false, false, false);
	            }
	            else {
	            //was: if (thisHub.dataa.activeObject == null && posRemoved > 0) {
	                HubAODelegate.setActiveObject(thisHub, posRemoved-1, false, false, false);
	            }
	        }
	    }
	
        // 20120715
        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(thisHub);
        for (int i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            setSharedHubsAfterRemove(h2, objRemoved, posRemoved);
        }        
        /* was
	    Hub[] hubs = getSharedHubs(thisHub);
	    for (int i=0; i<hubs.length; i++) {
	    	setSharedHubsAfterRemove(hubs[i], objRemoved, posRemoved);
	    }
	    */
	}

    public static Hub createSharedHub(Hub thisHub, boolean bShareActive) {
        Hub sharedHub = new Hub(thisHub.getObjectClass() );
        HubShareDelegate.setSharedHub(sharedHub, thisHub, bShareActive);
        return sharedHub;
    }

    /**
	    Navigational method used to create a shared version of another Hub, so that this Hub
	    will use the same objects as the shared hub.  All events that affect the data will be
	    sent to all shared Hubs.
	    @param sharedMasterHub Hub that is to be shared.
	    @param shareActiveObject true=use same activeObject as shared hub, false:use seperate activeObject
	    @see SharedHub
	*/
	public static void setSharedHub(Hub thisHub, Hub sharedMasterHub, boolean shareActiveObject) {
	    setSharedHub(thisHub, sharedMasterHub, shareActiveObject, null);
	}

    protected static void setSharedHub(Hub thisHub, Hub sharedMasterHub, boolean shareActiveObject, Object newLinkValue) {
        _setSharedHub(thisHub, sharedMasterHub, shareActiveObject, newLinkValue);
        // 20181030 update temp listener cache
        HubEventDelegate.clearGetAllListenerCache(thisHub);
    }
	
	protected static void _setSharedHub(Hub thisHub, Hub sharedMasterHub, boolean shareActiveObject, Object newLinkValue) {
        if (thisHub == null) return;
        if (thisHub == sharedMasterHub) sharedMasterHub = null;
	    // added: 2004/05/13, removed 2004/05/14
	    // if (getMasterHub() != null) throw new OAHubException(this,61);
	
        // 20180328 check to see if thisHub has masterObject and no masterHub
        if (OAObject.getDebugMode() && thisHub.datam.getMasterObject() != null) {
            if (thisHub.datam.getMasterHub() == null) {
                OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(thisHub);
                if (li != null && !li.getCalculated()) {
                    li = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(thisHub);
                    if (li != null && li.getType() == OALinkInfo.ONE) {
                        LOG.log(Level.WARNING, "thisHub should not be used for sharing, thisHub="+thisHub+", sharedMasterHub="+sharedMasterHub, new Exception("illegal hub share"));
                        return;
                    }
                }
            }
        }
        
	    HubDataDelegate.incChangeCount(thisHub);
	    final Hub hubOrigSharedHub = thisHub.datau.getSharedHub();
	    if (hubOrigSharedHub == sharedMasterHub) {
	        if (sharedMasterHub == null) return;
	        if (shareActiveObject == (thisHub.dataa == sharedMasterHub.dataa)) {
	            
	            // 20110809 this was removed, since there could be a linkToHub, which
	            //     would mean that the setting thisHub.setPos(-1) should instead  
	            //     set AO to the linkToHub.ao.propertyValue
	            /*was
                if (!shareActiveObject) thisHub.setPos(-1);  // in case masterHub was re-shared after a new select
                return; // same as previous call
                */
	        
	            // 20130331 since the SharedHub is the same, do more checking to see if thisHub has changed or not
	            if (!shareActiveObject || (thisHub.dataa.activeObject == sharedMasterHub.dataa.activeObject))  {
	                if (thisHub.datau.getLinkToHub() == null) {
	                    if (!shareActiveObject) {
	                        // 20180305
	                        Object objx = thisHub.getAO();
	                        if (objx != null && !thisHub.contains(objx)) {
	                            thisHub.setPos(-1);  // in case masterHub was re-shared after a new select
	                        }
	                        // was: thisHub.setPos(-1);  // in case masterHub was re-shared after a new select
	                    }
	                    return;
	                }
	                
	                // see if this AO is already set correctly with the linkHub
	                try {
	                    Object obj = thisHub.datau.getLinkToHub().getActiveObject();
	                    if (obj != null) {
	                        obj = thisHub.datau.getLinkToGetMethod().invoke(obj, null );
	                    }
	                    
	                    // 20110110 the link value is in the process of being changed - see HubDataDelegate.getPos(...)
	                    if (newLinkValue != null && newLinkValue != obj) return;
	                    
	                    if (thisHub.datau.isLinkPos()) {
	                        int x = -1;
	                        if (obj != null && obj instanceof Number) x = ((Number)obj).intValue();
	                        if (thisHub.getPos() == x) return;
	                    }
	                    else {
	                        if (thisHub.dataa.activeObject == obj) return;
	                    }
	                }
	                catch (Exception e) {
	                    e.printStackTrace();
	                    throw new RuntimeException(e);
	                }
	            }
	        }
	    }

        if (sharedMasterHub != null && sharedMasterHub.datau.getSharedHub() == thisHub) {
            throw new RuntimeException("the masterHub is already shared with thisHub - cant set thisHub.sharedHub with masterHub");
        }
	    
	    // 20110120
        if (sharedMasterHub == thisHub) return;
	    //was: if (sharedMasterHub == thisHub) sharedMasterHub = null;


	    // make sure both hubs are compatible
	    if (sharedMasterHub != null) {
	        if (thisHub.getObjectClass() == null) {
	            HubDelegate.setObjectClass(thisHub, sharedMasterHub.getObjectClass());
	        }
	        else if (sharedMasterHub.getObjectClass() == null) {
                HubDelegate.setObjectClass(sharedMasterHub, thisHub.getObjectClass());
            }
	        Class c = thisHub.getObjectClass(); 
	        if (c != null && !c.equals(sharedMasterHub.getObjectClass()) ) {
	            if (!c.isAssignableFrom(sharedMasterHub.getObjectClass())) {
	                throw new RuntimeException("objectClasses do not match");
	            }
	        }
	    }
	
	    // save orig dataa so that hubs that are shared with this hub can be updated
	    HubDataActive originalDataa = thisHub.dataa;
	
	    // first unset any prev set sharedHub
	    Hub h = thisHub.datau.getSharedHub();
	    if (h != null) {
	        removeSharedHub(h, thisHub);
	        if (h.dataa == thisHub.dataa) thisHub.dataa = new HubDataActive();
	    }
	    else {
	        // 20171015 need to remove objects from it
            for (Object obj : thisHub) {
                OAObjectHubDelegate.removeHub((OAObject)obj, thisHub, false);
            }
	    }
	    
	    Object activeObject = null;
	    boolean shareActiveObject2 = true;
	
	    if (sharedMasterHub == null) {
	    	thisHub.data = new HubData(thisHub.data.objClass);
	    	thisHub.datam = new HubDataMaster();
	    }
	    else {
	        activeObject = sharedMasterHub.getAO();
	
	        // recursive hubs
	        // if this hub is a masterHub of the sharedMasterHub
	        // then use the "original" shared hub of the sharedMasterHub and dont share AO
	        h = sharedMasterHub.getMasterHub();
	        
	        ArrayList<Hub> al = null;
	        for (int i=0;h != null; i++) {
	            if (h == thisHub) {
	                h = sharedMasterHub;
	                for (;;) {
	                    if (h.datau.getSharedHub() == null) break;
	                    h = h.datau.getSharedHub();
	                }
	                sharedMasterHub = h;
	                shareActiveObject2 = false;
	                break;
	            }
                // 20120717 added extra check against endless loop, since a recursive hub being shared by multiple parents can casue a loop
                if (i > 5) {
                    if (al == null) al = new ArrayList<Hub>();
                    else if (al.contains(h)) break;
                    al.add(h);
                }
	            h = h.getMasterHub();
	        }
	
	        // 2006/05/31 moved from below
	        addSharedHub(sharedMasterHub, thisHub); // adds to datau.vecSharedHub
	        thisHub.data = sharedMasterHub.data;
	        thisHub.datam = sharedMasterHub.datam;  // 20171218
	        // dont share "datau"
	        // dont share "dataa" unless shareActiveObject is true
	
	        if (shareActiveObject && shareActiveObject2) {
	            /** 2004/03/18
	            HubDataActive hold = this.dataa;
	            this.dataa = sharedMasterHub.dataa;
	            for (int i=0; i<hubShared.length; i++) {
	                if (hubShared[i].dataa == hold) hubShared[i].dataa = this.dataa;
	            }
	            */
	        }
	        else {
	            if (thisHub.getLinkHub(true) != null) {  // 2003/04/25
	                shareActiveObject = false; // cant share since this hub is linked to a master hub
	            }
	        }
	        // 2006/05/31 moved to above
	        // sharedMasterHub.datau.addSharedHub(this); // adds to datau.vecSharedHub
	    }
	
	    thisHub.datau.setSharedHub(sharedMasterHub); // the master Hub that this hub is shared with

	    Hub[] hubShared = getAllSharedHubs(thisHub, true, null); // get shared hubs under this Hub
	    if (sharedMasterHub != null && shareActiveObject && shareActiveObject2) {
	        thisHub.dataa = sharedMasterHub.dataa;
	    }
	    for (int i=0; i<hubShared.length; i++) {
	        hubShared[i].data = thisHub.data;  // share same data
            hubShared[i].datam = thisHub.datam;  // 20171218
	        if (hubShared[i].dataa == originalDataa) hubShared[i].dataa = thisHub.dataa;
	    }
	
	    // set active object in each shared hub, which will update detail hubs
	    for (int i=0; i<hubShared.length; i++) {
	        h = hubShared[i];
	        if (h.datau.getLinkToHub() == null) {
	            // if there is not a linkHub, then go to first object
	            int pos;
	            if (h.datau.getSharedHub() != null && h.dataa == h.datau.getSharedHub().dataa) {
	                // shared hubs
	                pos = h.datau.getSharedHub().getPos();
	            }
	            else {
	                // 08/18/2001 - always set to null
	                // pos = size() > 0 ? 0 :-1;
	                pos = h.datau.getDefaultPos();  // default is -1
	            }
	            HubAODelegate.setActiveObject(h, pos, false, true, true);  // updateLink, bForce, bCalledByShareHub
	        }
	        else {
	            // if linkHub & !bUpdateLink, then retrieve value from linked property
	            // and make that the activeObject in this Hub
	            try {
	                Object obj = h.datau.getLinkToHub().getActiveObject();
	                if (obj != null) {
	                	obj = h.datau.getLinkToGetMethod().invoke(obj, null );
	                }
	                
	                // 20110110 the link value is in the process of being changed - see HubDataDelegate.getPos(...)
	                if (newLinkValue != null && newLinkValue != obj) continue;
	                
	                if (h.datau.isLinkPos()) {
	                    int x = -1;
	                    if (obj != null && obj instanceof Number) x = ((Number)obj).intValue();
	                    if (h.getPos() != x) {
	                    	HubAODelegate.setActiveObject(h, h.elementAt(x),x,false,false,true);//bUpdateLink,bForce,bCalledByShareHub
	                    }
	                }
	                else {
	                    int pos = h.getPos(obj);
	                    if (obj != null &&  pos < 0) obj = null;
                    	HubAODelegate.setActiveObject(h,obj,pos,false,false,true);//bUpdateLink,bForce,bCalledByShareHub
	                }
	            }
	            catch (Exception e) {
	            	e.printStackTrace();
	                throw new RuntimeException(e);
	            }
	        }
	    }
	    
	    // 20120229 might need to temp set AO=newLinkValue
	    boolean b = (newLinkValue != null && newLinkValue != thisHub.dataa.activeObject);
	    Object hold = null;
	    if (b) {
	        hold = thisHub.dataa.activeObject;
	        thisHub.dataa.activeObject = newLinkValue;
	    }

	    // 20130317 added this to stop an infinite loop
        if (thisHub.datau.getSharedHub() != hubOrigSharedHub) {
            HubEventDelegate.fireOnNewListEvent(thisHub, false); // only for this shared hub
        }
	    // was: HubEventDelegate.fireOnNewListEvent(thisHub, false); // only for this shared hub
        
	    // 20101113 not sure why this is here, since it would resort the sharedMasterHub
	    // HubSortDelegate.sort(thisHub);

        // 20120614 the change from 0229 looks wrong
	    if (b) thisHub.dataa.activeObject = hold;
        /*was:
        // 20120229
        if (b && hold == thisHub.dataa.activeObject) {
            thisHub.dataa.activeObject = hold;
        }
        */
    }

	
	/**
	    Returns an array of all of the Hubs that are shared with this Hub.
	*/
    /*
	protected static Hub[] getSharedHubs_OLD(Hub thisHub) {
	    if (thisHub.datau.vecSharedHub == null) return new Hub[0];
	    synchronized (thisHub.datau.vecSharedHub) {
		    int x = thisHub.datau.vecSharedHub.size();
		    Hub[] hubs = new Hub[x];
		    thisHub.datau.vecSharedHub.copyInto(hubs);
		    return hubs;
	    }
	}
	*/
	/**
	    Add Hub that is being shared with this Hub.  This will use a WeakReference, so that
	    the shared Hub will be removed when it is garbage collected.
	*/
	/*
	protected static void addSharedHub_OLD(Hub thisHub, Hub hub) {
	    if (thisHub.datau.vecSharedHub == null) {
		    synchronized (thisHub.datau) {
		    	if (thisHub.datau.vecSharedHub == null) thisHub.datau.vecSharedHub = new Vector(3,5);
		    }
	    }
	    thisHub.datau.vecSharedHub.addElement(hub);
	}
	*/
	/**
	    Remove shared Hub from list of shared Hubs.
	*/
	/*
	protected static void removeSharedHub_OLD(Hub thisHub, Hub hub) {
	    if (thisHub.datau.vecSharedHub == null) return;
	    synchronized (thisHub.datau.vecSharedHub) {
        	thisHub.datau.vecSharedHub.removeElement(hub);
	    }
	}
	*/


    public static void addSharedHub(Hub thisHub, Hub hub) {
        _addSharedHub(thisHub, hub);
        // 20181030 update temp listener cache
        HubEventDelegate.clearGetAllListenerCache(thisHub);
    }
	
	// 20120715
    protected static void _addSharedHub(Hub thisHub, Hub hub) {
        if (thisHub == null || hub == null) return;
        
        int pos;
        synchronized (thisHub.datau) {
            if (thisHub.datau.getWeakSharedHubs() == null) {
                thisHub.datau.setWeakSharedHubs(new WeakReference[1]);
                pos = 0;
            }
            else {
                // check for empty slot at the end
                int currentSize = thisHub.datau.getWeakSharedHubs().length;
                for (pos=currentSize-1; pos>=0; pos--) {
                    if (thisHub.datau.getWeakSharedHubs()[pos] == null) continue;
                    if (thisHub.datau.getWeakSharedHubs()[pos].get() == null) {
                        thisHub.datau.getWeakSharedHubs()[pos] = null;
                        continue;
                    }
                    // found last used slot
                    if (pos < currentSize-1) {
                        pos++; // first empty slot
                        break;
                    }
                    
                    // need to expand
                    int newSize = currentSize + 1 + (currentSize/3);
                    newSize = Math.min(newSize, currentSize + 50);
                    WeakReference<Hub>[] refs = new WeakReference[newSize];
                    
                    System.arraycopy(thisHub.datau.getWeakSharedHubs(), 0, refs, 0, currentSize);
                    thisHub.datau.setWeakSharedHubs(refs);
                    pos = currentSize;
                    break;
                }
                if (pos < 0) pos = 0;
            }
            thisHub.datau.getWeakSharedHubs()[pos] = new WeakReference(hub);
        }
        if (pos > 99) {
            if (pos+1 % 25 == 0) {
                LOG.warning("Hub has "+(pos+1)+" sharedHubs, Hub="+thisHub);
            }
        }
    }

    protected static void removeSharedHub(Hub sharedHub, Hub hub) {
        _removeSharedHub(sharedHub, hub);
        // 20181030 update temp listener cache
        HubEventDelegate.clearGetAllListenerCache(hub);  // will clear both hubs
    }
	
    protected static void _removeSharedHub(Hub sharedHub, Hub hub) {
        if (sharedHub.datau.getWeakSharedHubs() == null) return;
        boolean bFound = false;
        synchronized (sharedHub.datau) {
            if (sharedHub.datau.getWeakSharedHubs() == null) return;
            int currentSize = sharedHub.datau.getWeakSharedHubs().length;
            int lastEndPos = currentSize-1;
            for (int pos=0; !bFound && pos<currentSize; pos++) {
                if (sharedHub.datau.getWeakSharedHubs()[pos] == null) break;  // the rest will be nulls
                
                Hub hx = sharedHub.datau.getWeakSharedHubs()[pos].get();
                if (hx != null && hx != hub) continue;
                bFound = (hx == hub);
                sharedHub.datau.getWeakSharedHubs()[pos] = null;
                
                // compress:  get last one, move it back to this slot
                for (; lastEndPos>pos; lastEndPos--) {
                    if (sharedHub.datau.getWeakSharedHubs()[lastEndPos] == null) continue;
                    if (sharedHub.datau.getWeakSharedHubs()[lastEndPos].get() == null) {
                        sharedHub.datau.getWeakSharedHubs()[lastEndPos] = null;
                        continue;
                    }
                    sharedHub.datau.getWeakSharedHubs()[pos] = sharedHub.datau.getWeakSharedHubs()[lastEndPos];
                    sharedHub.datau.getWeakSharedHubs()[lastEndPos] = null;
                    break;
                }
                if (currentSize > 20 && ((currentSize - lastEndPos) > currentSize/3)) {
                    // resize array
                    int newSize = lastEndPos + (lastEndPos/10) + 1;
                    newSize = Math.min(lastEndPos + 20, newSize);
                    WeakReference<Hub>[] refs = new WeakReference[newSize];
                    
                    System.arraycopy(sharedHub.datau.getWeakSharedHubs(), 0, refs, 0, lastEndPos);
                    sharedHub.datau.setWeakSharedHubs(refs);
                    currentSize = newSize;
                }
            }
            if (sharedHub.datau.getWeakSharedHubs()[0] == null) {
                sharedHub.datau.setWeakSharedHubs(null);
            }
        }
    }
	
	private final static Hub[] EmptyHubs = new Hub[0];
    /**
        Returns an array of all of the Hubs that are shared with this Hub only.
        @deprecated use getAllSharedHubs, or one of the other methods
    */
    protected static Hub[] getSharedHubs(Hub thisHub) {
        if (thisHub.datau.getWeakSharedHubs() == null) return EmptyHubs;
        synchronized (thisHub.datau) {
            if (thisHub.datau.getWeakSharedHubs() == null) return EmptyHubs;
            
            int x = thisHub.datau.getWeakSharedHubs().length;
            for (int j=x-1; j>=0; j--) {
                if (thisHub.datau.getWeakSharedHubs()[j] == null) continue;
                if (thisHub.datau.getWeakSharedHubs()[j].get() == null) {
                    thisHub.datau.getWeakSharedHubs()[j] = null;
                    continue;
                }
                Hub[] hubs = new Hub[j+1];
                for (int i=0; i<hubs.length; i++) {
                    hubs[i] = thisHub.datau.getWeakSharedHubs()[i].get();
                }
                // note: could be nulls in array
                return hubs;
            }
        }
        return EmptyHubs;
    }
    public static WeakReference<Hub>[] getSharedWeakHubs(Hub thisHub) {
        if (thisHub == null) return null;
        return thisHub.datau.getWeakSharedHubs();
    }
    public static int getSharedWeakHubSize(Hub thisHub) {
        if (thisHub == null) return 0;
        WeakReference<Hub>[] refs = thisHub.datau.getWeakSharedHubs();
        if (refs == null) return 0;
        int cnt = 0;
        for (WeakReference<Hub> ref : refs) {
            if (ref != null && ref.get() != null) cnt++;
        }
        return cnt;
    }

    public static void main(String[] args) {
        Hub<String> h = new Hub<String>(String.class);
        for (int i=0; i<1000; i++) {
            Hub<String> hx = new Hub<String>(String.class);
            hx.setSharedHub(h);
            System.gc();
        }
        for (int i=0; i<100; i++) {
            System.gc();
        }
        System.out.println("Done");
    }
}
