/**
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

Copyright (c) 2001-2007 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.util.OANullObject;

/**
 * Used by Hub to manage the list of Hubs that an OAObject is a member of.
 * @author vincevia
 */
public class OAObjectHubDelegate {

    private static Logger LOG = Logger.getLogger(OAObjectHubDelegate.class.getName());

    
    
    /*
    // 20120827
    public static int getHubFlags(OAObject oaObj) {
        if (oaObj == null) return 0;
        return oaObj.hubEmptyFlags;
    }
    // set empty hub flags to 0, so that they can be updated as needed.
    //   since the flag is for performance only, this has no harm. 
    public static void resetEmptyHubFlags(OAObject oaObj) {
        if (oaObj == null) return;
        if (oaObj.hubEmptyFlags != 0) {
            oaObj.hubEmptyFlags = 0;
            //qqqqqqqqqq this will need to be true if DS needs to know,
            // oaObj.changedFlag = true;
        }
    }
    public static void resetEmptyHubFlag(OAObject objMaster, String hubPropName) {
        if (objMaster == null) return;
        if (objMaster.hubEmptyFlags == 0) return;

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objMaster);
        if (oi == null) return;

        String[] ss = oi.getHubProperties();
        int pos = Arrays.binarySearch(ss, hubPropName.toUpperCase());
        synchronized (objMaster) {
            int x = 1;
            x <<= pos;
            x = ~x;
            objMaster.hubEmptyFlags = objMaster.hubEmptyFlags & x;
        }
    }
    // returns true if the Hub.size is known to be 0 - so that the DS does not need to select it.
    public static boolean getEmptyHubFlag(OAObject objMaster, String hubPropName) {
        if (objMaster == null || hubPropName == null) return false;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objMaster);
        if (oi == null) return false;
        String[] ss = oi.getHubProperties();
        int pos = Arrays.binarySearch(ss, hubPropName.toUpperCase());
        if (pos >= 0 && pos < 32) {
            int x = 1;
            x <<= pos;
            return (x & objMaster.hubEmptyFlags) > 0;
        }
        return false;
    }
    
    // 20120827
    // called by datasource to update hub flag in masterObject
    public static void updateMasterObjectEmptyHubFlag(Hub thisHub, boolean bSetChangeFlag) {
        if (thisHub == null) return;
        Object obj = HubDelegate.getMasterObject(thisHub);
        if (!(obj instanceof OAObject)) return;
        String prop = HubDetailDelegate.getPropertyFromMasterToDetail(thisHub);
        updateMasterObjectEmptyHubFlag(thisHub, prop, (OAObject) obj, bSetChangeFlag);
    }
    public static void updateMasterObjectEmptyHubFlag(Hub thisHub, String prop, OAObject objMaster, boolean bSetChangeFlag) {
        if (thisHub == null || prop == null || objMaster == null) return;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objMaster);
        if (oi == null) return;
        String[] ss = oi.getHubProperties();
        int pos = Arrays.binarySearch(ss, prop.toUpperCase());
        if (pos >= 0 && pos < 32) {
            boolean b = thisHub.getSize() == 0;
            synchronized (objMaster) {
                int hold = objMaster.hubEmptyFlags;
                
                int x = 1;
                x <<= pos;
                if (b) {
                    objMaster.hubEmptyFlags = objMaster.hubEmptyFlags | x;
                }
                else {
                    x = ~x;
                    objMaster.hubEmptyFlags = objMaster.hubEmptyFlags & x;
                }
                if (bSetChangeFlag && !objMaster.changedFlag && objMaster.hubEmptyFlags != hold) {
//qqqqqqqqqq this will need to be true if DS needs to know,
//            for now, we're only using the hubEmptyFlags to know if the client needs to get it from the server                   
//                    objMaster.changedFlag = true;
                }
            }
        }
    }
    */
    
    // 20120827 might be used later
    // send event to master object when a change is made to one of its reference hubs
    // called by HubEventDelegate when a change happens to a hub
    public static void fireMasterObjectHubChangeEvent(Hub thisHub, boolean bRefreshFlag) {
        if (thisHub == null) return;
        
        Object objMaster = HubDelegate.getMasterObject(thisHub);
        if (!(objMaster instanceof OAObject)) return;
        
        String prop = HubDetailDelegate.getPropertyFromMasterToDetail(thisHub);
        if (prop == null) return;
        /*
        if (bRefreshFlag || thisHub.getSize() < 2) {
            updateMasterObjectEmptyHubFlag(thisHub, prop, (OAObject)objMaster, true);
        }
        */
        OAObjectEventDelegate.sendHubPropertyChange((OAObject)objMaster, prop, thisHub, thisHub, null);
        OAObjectCacheDelegate.fireAfterPropertyChange(
                (OAObject)objMaster, 
                OAObjectKeyDelegate.getKey((OAObject)objMaster), 
                prop, thisHub, thisHub, true, true);
    }
    
    
	public static boolean isInHub(OAObject oaObj) {
		if (oaObj == null) return false;
		WeakReference[] refs = oaObj.weakHubs;
        if (refs == null) return false;
		for (WeakReference ref : refs) {
		    if (ref != null && ref.get() != null) return true;
		}
		return false;
	}

    public static boolean isInHubWithMaster(OAObject oaObj) {
        if (oaObj == null) return false;
        WeakReference[] refs = oaObj.weakHubs;
        if (refs == null) return false;
        for (WeakReference ref : refs) {
            if (ref != null && ref.get() != null) {
                Hub h = (Hub) ref.get();
                if (h != null && h.getMasterObject() != null) return true;
            }
        }
        return false;
    }
	
	// 20120725 memory leak fixed, rewritten to handle weakrefs with nulls correctly, and keep array compress (empty space at the end only)
    /**
	    Called by Hub when an OAObject is removed from a Hub.
	*/
    public static void removeHub(OAObject oaObj, Hub hub) {
        if (oaObj == null || oaObj.weakHubs == null) return;
        hub = hub.getRealHub();
        
        // 20120702 dont store hub if M2M and reverse linkInfo does not have a method.
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li != null && li.getPrivateMethod()) {
            if (OAObjectInfoDelegate.isMany2Many(li)) {
                return;
            }
        }
        
        boolean bFound = false;
        synchronized (oaObj) {
            if (oaObj.weakHubs == null) return;
            int currentSize = oaObj.weakHubs.length;
            int lastEndPos = currentSize-1;

            for (int pos=0; !bFound && pos<currentSize; pos++) {
                if (oaObj.weakHubs[pos] == null) break;  // the rest will be nulls

                Hub hx = oaObj.weakHubs[pos].get();
                if (hx != null && hx != hub) continue;
                bFound = (hx == hub);
                oaObj.weakHubs[pos] = null;
                
                // compress:  get last one, move it back to this slot
                for (; lastEndPos>pos; lastEndPos--) {
                    if (oaObj.weakHubs[lastEndPos] == null) continue;
                    if (oaObj.weakHubs[lastEndPos].get() == null) {
                        oaObj.weakHubs[lastEndPos] = null;
                        continue;
                    }
                    oaObj.weakHubs[pos] = oaObj.weakHubs[lastEndPos];
                    oaObj.weakHubs[lastEndPos] = null;
                    break;
                }
                if (currentSize > 10 && ((currentSize - lastEndPos) > currentSize/3)) {
                    // resize array
                    int newSize = lastEndPos + (lastEndPos/10) + 1;
                    newSize = Math.min(lastEndPos + 20, newSize);
                    WeakReference[] refs = new WeakReference[newSize];
                    
                    System.arraycopy(oaObj.weakHubs, 0, refs, 0, lastEndPos);
                    oaObj.weakHubs = refs;
                    currentSize = newSize;
                }
            }
                
            if (oaObj.weakHubs[0] == null || !isInHubWithMaster(oaObj)) {
                oaObj.weakHubs = null;
            }
            if (!isInHubWithMaster(oaObj)) {
                if (OARemoteThreadDelegate.shouldSendMessages()) {
                    // CACHE_NOTE: if it was on the Server.cache, it was removed when it was added to a hub.  Need to add to cache now that it is no longer in a hub.
                    OAObjectCSDelegate.addToServerSideCache(oaObj);
                }
            }
        }
    }
	
    // before 20120725 - did not compress, or clean out ref=null
    /*
	public static void removeHub_ORIG(OAObject oaObj, Hub hub) {
		if (oaObj == null || oaObj.weakHubs == null) return;
        hub = hub.getRealHub();
        
        // 20120702 dont store hub if M2M and reverse linkInfo does not have a method.
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li != null && li.getNullHub()) {
            return;
        }
        
		synchronized (oaObj) {
			if (oaObj.weakHubs == null) return;
			int x = oaObj.weakHubs.length;
            for (int i=0; i<x; i++) {
                if (oaObj.weakHubs[i] == null) continue;
                if (oaObj.weakHubs[i].get() != hub) continue;
                oaObj.weakHubs[i] = null;
                
                // compress:  get last one, move it back to this slot
                for (int j=x-1; j>i; j--) {
                    if (oaObj.weakHubs[j] == null) continue;
                    if (oaObj.weakHubs[j].get() == null) {
                        oaObj.weakHubs[j] = null;
                        continue;
                    }
                    oaObj.weakHubs[i] = oaObj.weakHubs[j];
                    oaObj.weakHubs[j] = null;
                    break;
                }

                
                if (oaObj.weakHubs[0] == null) {
                    oaObj.weakHubs = null;
                    // CACHE_NOTE: if it was on the Server.cache, it was removed when it was added to a hub.  Need to add to cache now that it is no longer in a hub.
                    OAObjectCSDelegate.addToServerSideCache(oaObj);
                }
                break;
            }
		}
	}
    */
	
    /**
	    Return all Hubs that this object is a member of.
	*/
	public static WeakReference<Hub<?>>[] getHubReferences(OAObject oaObj) {  // Note: this needs to be public
        if (oaObj == null) return null;
        return oaObj.weakHubs;
	}
    public static int getHubReferenceCount(OAObject oaObj) {
        if (oaObj == null) return 0;
        WeakReference<Hub<?>>[] refs = oaObj.weakHubs; 
        return (refs==null?0:refs.length);
    }

	/**
	    Called by Hub when an OAObject is added to a Hub.
	*/
	public static void addHub(OAObject oaObj, Hub hub) {
		if (oaObj == null || hub == null) return;

        hub = hub.getRealHub();
		
		/* 20110102 removed, ex: VetPlan Items <-> ItemCategories
		 *  The vetjobs examples should not have a  Categories.getVetUsers method
		 *
		
		// 20090906 see if this should be added to the hub
		//    if M2M and the other hub does not have a reference/method to this hub, then dont store in hubs.  ex: VetUser <-> Categories,
		//         the Category objects can exist in lots of VetUser.hubCategories, creating a huge array that is not really needed.
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li != null) {
            if (OAObjectInfoDelegate.isMany2Many(li)) {
                if (OAObjectInfoDelegate.getMethod(li) == null) {
                    return;
                }
            }
        }
		*/

        // 20120702 dont store hub if M2M and reverse linkInfo does not have a method.
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li != null && li.getPrivateMethod()) {
            if (OAObjectInfoDelegate.isMany2Many(li)) {
                return;
            }
        }
        

		synchronized (oaObj) {
			int pos;
			if (oaObj.weakHubs == null) {
				oaObj.weakHubs = new WeakReference[1];
				pos = 0;
        		// CACHE_NOTE: if it was on the Server.cache, it can be removed when it is added to a hub.  Need to add to cache if/when it is no longer in a hub.
				if (hub.getMasterObject() != null) {
	                if (OARemoteThreadDelegate.shouldSendMessages()) {
	                    OAObjectCSDelegate.removeFromServerSideCache(oaObj);
	                }
				}
			}
			else {
			    // check for empty slot at the end
	            int currentSize = oaObj.weakHubs.length;
                for (pos=currentSize-1; pos>=0; pos--) {
	                if (oaObj.weakHubs[pos] == null) continue;
                    if (oaObj.weakHubs[pos].get() == null) {
                        oaObj.weakHubs[pos] = null;
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
                    WeakReference[] refs = new WeakReference[newSize];
                    
                    System.arraycopy(oaObj.weakHubs, 0, refs, 0, currentSize);
                    oaObj.weakHubs = refs;
                    pos = currentSize;
                    break;
                }
                if (pos < 0) pos = 0;

                if (hub.getMasterObject() != null) {
                    boolean b = false;
                    for (int i=0; i<pos; i++) {
                        if (oaObj.weakHubs[i] == null) continue;
                        Hub h = oaObj.weakHubs[i].get();
                        if (h == null) continue;
                        if (h.getMasterObject() != null) {
                            b = true;
                            break;
                        }
                    }
                    if (!b && hub.getMasterObject() != null) {
                        if (OARemoteThreadDelegate.shouldSendMessages()) {
                            OAObjectCSDelegate.removeFromServerSideCache(oaObj);
                        }
                    }
                }
			}
			oaObj.weakHubs[pos] = new WeakReference(hub);
			/* 20100329 removed since this is well tested and can/will happen with lookup-type objects that are
			 *  used in a lot of objects.  Ex: Item.itemCategory, Order.OrderStatus, etc.
			if (pos > 24) {
			    if (pos % 25 == 0) {
			        LOG.warning("object is in "+(pos+1)+" Hubs, object="+oaObj);
			    }
			}
			*/
	    }
	}
	
		
	/** Used by Hub to read serialized objects.
	    Check to see if this object is already loaded in a hub with same LinkInfo.
	*/
	public static boolean isAlreadyInHub(OAObject oaObj, OALinkInfo li) {
	    if (oaObj.weakHubs == null || li == null) return false;

        WeakReference[] weakHubs = oaObj.weakHubs;
        for (int i=0; weakHubs != null && i<weakHubs.length; i++) {
            WeakReference ref = weakHubs[i];
            if (ref == null) continue;
            Hub h = (Hub) ref.get();
            if (h != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(h) == li) return true;
        }
	    return false;
	}

    public static Hub getWeakRefHub(OAObject oaObj, OALinkInfo li) {
        if (oaObj.weakHubs == null || li == null) return null;

        WeakReference[] weakHubs = oaObj.weakHubs;
        for (int i=0; weakHubs != null && i<weakHubs.length; i++) {
            WeakReference ref = weakHubs[i];
            if (ref == null) continue;
            Hub h = (Hub) ref.get();
            if (h != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(h) == li) return h;
        }
        return null;
    }
	
	/** 
	    Used by Hub.add() before adding, quicker then checking array
	*/
    public static boolean isAlreadyInHub(OAObject oaObj, Hub hubFind) {
        if (oaObj == null || hubFind == null) return false;
        hubFind = hubFind.getRealHub();
        boolean b = _isAlreadyInHub(oaObj, hubFind);
        if (b) return true;

        // could be in the hub, but not in weakHubs, if private, or M2M
        Object master = hubFind.getMasterObject();
        if (master == null) return false;
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hubFind);
        if (li == null) return false;
        if (li.getPrivateMethod()) { // if hub method is off
            return hubFind.contains(oaObj);
        }
        if (OAObjectInfoDelegate.isMany2Many(li)) {  // m2m objets do not have Hub in weakRef[] 
            return hubFind.contains(oaObj);
        }
        return false;
    }
	private static boolean _isAlreadyInHub(OAObject oaObj, Hub hubFind) {
	    WeakReference[] weakHubs = oaObj.weakHubs;
	    if (weakHubs == null) {
	        return false;
	    }
        for (int i=0; weakHubs != null && i<weakHubs.length; i++) {
            WeakReference ref = weakHubs[i];
            if (ref == null) continue;
            Hub h = (Hub) ref.get();
            if (h == hubFind) return true;
        }
	    return false;
	}
	
	protected static boolean getChanged(Hub thisHub, int changedRule, OACascade cascade) {
		return HubDelegate.getChanged(thisHub, changedRule, cascade);
	}
	
	protected static void saveAll(Hub hub, int iCascadeRule, OACascade cascade) {
	    if (hub == null) return; // qqq need to log this
		HubSaveDelegate.saveAll(hub, iCascadeRule, cascade); // cascade save and update M2M links
	}

    protected static void deleteAll(Hub hub, OACascade cascade) {
        HubDeleteDelegate.deleteAll(hub, cascade); // cascade delete and update M2M links
    }

	
    protected static void setMasterObject(Hub hub, OAObject oaObj, OALinkInfo li) {
        if (HubDetailDelegate.getMasterObject(hub) == null) {
        	HubDetailDelegate.setMasterObject(hub, oaObj, li);
        }
    }
    
}














