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

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OANullObject;

/**
 * Used by Hub to manage the list of Hubs that an OAObject is a member of.
 * 
 * @author vincevia
 */
public class OAObjectHubDelegate {

    private static Logger LOG = Logger.getLogger(OAObjectHubDelegate.class.getName());

    /*
     * // 20120827 public static int getHubFlags(OAObject oaObj) { if (oaObj == null) return 0; return
     * oaObj.hubEmptyFlags; } // set empty hub flags to 0, so that they can be updated as needed. //
     * since the flag is for performance only, this has no harm. public static void
     * resetEmptyHubFlags(OAObject oaObj) { if (oaObj == null) return; if (oaObj.hubEmptyFlags != 0) {
     * oaObj.hubEmptyFlags = 0; //qqqqqqqqqq this will need to be true if DS needs to know, //
     * oaObj.changedFlag = true; } } public static void resetEmptyHubFlag(OAObject objMaster, String
     * hubPropName) { if (objMaster == null) return; if (objMaster.hubEmptyFlags == 0) return;
     * 
     * OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objMaster); if (oi == null) return;
     * 
     * String[] ss = oi.getHubProperties(); int pos = Arrays.binarySearch(ss,
     * hubPropName.toUpperCase()); synchronized (objMaster) { int x = 1; x <<= pos; x = ~x;
     * objMaster.hubEmptyFlags = objMaster.hubEmptyFlags & x; } } // returns true if the Hub.size is
     * known to be 0 - so that the DS does not need to select it. public static boolean
     * getEmptyHubFlag(OAObject objMaster, String hubPropName) { if (objMaster == null || hubPropName ==
     * null) return false; OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(objMaster); if (oi ==
     * null) return false; String[] ss = oi.getHubProperties(); int pos = Arrays.binarySearch(ss,
     * hubPropName.toUpperCase()); if (pos >= 0 && pos < 32) { int x = 1; x <<= pos; return (x &
     * objMaster.hubEmptyFlags) > 0; } return false; }
     * 
     * // 20120827 // called by datasource to update hub flag in masterObject public static void
     * updateMasterObjectEmptyHubFlag(Hub thisHub, boolean bSetChangeFlag) { if (thisHub == null)
     * return; Object obj = HubDelegate.getMasterObject(thisHub); if (!(obj instanceof OAObject))
     * return; String prop = HubDetailDelegate.getPropertyFromMasterToDetail(thisHub);
     * updateMasterObjectEmptyHubFlag(thisHub, prop, (OAObject) obj, bSetChangeFlag); } public static
     * void updateMasterObjectEmptyHubFlag(Hub thisHub, String prop, OAObject objMaster, boolean
     * bSetChangeFlag) { if (thisHub == null || prop == null || objMaster == null) return; OAObjectInfo
     * oi = OAObjectInfoDelegate.getOAObjectInfo(objMaster); if (oi == null) return; String[] ss =
     * oi.getHubProperties(); int pos = Arrays.binarySearch(ss, prop.toUpperCase()); if (pos >= 0 && pos
     * < 32) { boolean b = thisHub.getSize() == 0; synchronized (objMaster) { int hold =
     * objMaster.hubEmptyFlags;
     * 
     * int x = 1; x <<= pos; if (b) { objMaster.hubEmptyFlags = objMaster.hubEmptyFlags | x; } else { x
     * = ~x; objMaster.hubEmptyFlags = objMaster.hubEmptyFlags & x; } if (bSetChangeFlag &&
     * !objMaster.changedFlag && objMaster.hubEmptyFlags != hold) { //qqqqqqqqqq this will need to be
     * true if DS needs to know, // for now, we're only using the hubEmptyFlags to know if the client
     * needs to get it from the server // objMaster.changedFlag = true; } } } }
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
         * if (bRefreshFlag || thisHub.getSize() < 2) { updateMasterObjectEmptyHubFlag(thisHub, prop,
         * (OAObject)objMaster, true); }
         */
        OAObjectEventDelegate.sendHubPropertyChange((OAObject) objMaster, prop, thisHub, thisHub, null);
        OAObjectCacheDelegate.fireAfterPropertyChange((OAObject) objMaster, OAObjectKeyDelegate.getKey((OAObject) objMaster), prop, thisHub, thisHub, true, true);
    }

    public static boolean isInHub(OAObject oaObj) {
        if (oaObj == null) return false;
        Object[] objs = oaObj.hubs;
        if (objs == null) return false;
        for (Object obj : objs) {
            if (obj != null) {
                if (obj instanceof WeakReference) {
                    obj = ((WeakReference) obj).get();
                    if (obj == null) continue;
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isInHubWithMaster(OAObject oaObj) {
        if (oaObj == null) return false;
        Object[] objs = oaObj.hubs;
        if (objs == null) return false;
        for (Object obj : objs) {
            if (obj != null) {
                if (obj instanceof WeakReference) {
                    obj = ((WeakReference) obj).get();
                    if (obj == null) continue;
                }
                Hub h = (Hub) obj;
                if (h.getMasterObject() != null) return true;
            }
        }
        return false;
    }

    /**
     * 20141029 This is to change an OAObjet.hubs, so that it uses weakReferences or Hub instances.
     */
    public static void changeHubReferences(OAObject oaObj) {
        if (oaObj == null || oaObj.hubs == null) return;
        if (!OASyncDelegate.isServer()) return;

        boolean b = (oaObj.isChanged() || oaObj.isNew());
        synchronized (oaObj) {
            for (int i = 0; oaObj.hubs != null && i < oaObj.hubs.length; i++) {
                Object obj = oaObj.hubs[i];
                if (obj == null) break;

                if (b) {
                    if (obj instanceof WeakReference) {
                        oaObj.hubs[i] = ((WeakReference) obj).get();
                    }
                }
                else {
                    if (obj instanceof Hub) oaObj.hubs[i] = new WeakReference((Hub) obj);
                }
            }
        }
    }

    /**
     * Called by Hub when an OAObject is removed from a Hub.
     */
    public static void removeHub(OAObject oaObj, Hub hub) {
        if (oaObj == null || oaObj.hubs == null) return;
        hub = hub.getRealHub();

        boolean bFound = false;
        synchronized (oaObj) {
            if (oaObj.hubs == null) return;
            int currentSize = oaObj.hubs.length;
            int lastEndPos = currentSize - 1;

            for (int pos = 0; !bFound && pos < currentSize; pos++) {
                if (oaObj.hubs[pos] == null) break; // the rest will be nulls

                Hub hx;
                if (oaObj.hubs[pos] instanceof WeakReference) {
                    hx = (Hub) ((WeakReference) oaObj.hubs[pos]).get();
                }
                else hx = (Hub) oaObj.hubs[pos];

                if (hx != null && hx != hub) continue;
                bFound = (hx == hub);
                oaObj.hubs[pos] = null;

                // compress: get last one, move it back to this slot
                for (; lastEndPos > pos; lastEndPos--) {
                    if (oaObj.hubs[lastEndPos] == null) continue;
                    if (oaObj.hubs[lastEndPos] instanceof WeakReference && ((WeakReference) oaObj.hubs[lastEndPos]).get() == null) {
                        oaObj.hubs[lastEndPos] = null;
                        continue;
                    }
                    oaObj.hubs[pos] = oaObj.hubs[lastEndPos];
                    oaObj.hubs[lastEndPos] = null;
                    break;
                }
                if (currentSize > 10 && ((currentSize - lastEndPos) > currentSize / 3)) {
                    // resize array
                    int newSize = lastEndPos + (lastEndPos / 10) + 1;
                    newSize = Math.min(lastEndPos + 20, newSize);
                    Object[] objs = new Object[newSize];

                    System.arraycopy(oaObj.hubs, 0, objs, 0, lastEndPos);
                    oaObj.hubs = objs;
                    currentSize = newSize;
                }
            }

            if (oaObj.hubs[0] == null) {
                oaObj.hubs = null;
            }

            // 20130707 could be a hub from hubMerger, that populates with One references
            // which means that the one reference keeps it from gc
            if (hub.getMasterObject() != null) {
                if (!isInHubWithMaster(oaObj)) {
                    if (OARemoteThreadDelegate.shouldSendMessages()) {
                        // CACHE_NOTE: if it was on the Server.cache, it was removed when it was added
                        // to a hub. Need to add to cache now that it is no longer in a hub.
                        OAObjectCSDelegate.addToServerSideCache(oaObj);
                    }
                }
            }
        }
    }

    /**
     * Return all Hubs that this object is a member of. Note: could have null values
     */
    public static Hub[] getHubReferences(OAObject oaObj) { // Note: this needs to be public
        if (oaObj == null) return null;

        Object[] objs = oaObj.hubs;
        if (objs == null) return null;

        Hub[] hubs = new Hub[objs.length];

        for (int i = 0; i < objs.length; i++) {
            Object obj = objs[i];
            if (obj instanceof WeakReference) obj = ((WeakReference) obj).get();
            Hub h = (Hub) obj;
            hubs[i] = h;
        }
        return hubs;
    }

    public static Object[] getHubReferencesNoCopy(OAObject oaObj) { // Note: this needs to be public
        if (oaObj == null) return null;
        return oaObj.hubs;
    }

    public static int getHubReferenceCount(OAObject oaObj) {
        if (oaObj == null) return 0;
        Object[] objs = oaObj.hubs;
        int cnt = 0;
        for (int i = 0; objs != null && i < objs.length; i++) {
            Object obj = objs[i];
            if (obj instanceof WeakReference) obj = ((WeakReference) obj).get();
            if (obj != null) cnt++;
        }
        return cnt;
    }

    public static boolean addHub(OAObject oaObj, Hub hub) {
        // 20140313 was: addHub(oaObj, hub, true, false);
        return addHub(oaObj, hub, false, false);
    }

    /**
     * Called by Hub when an OAObject is added to a Hub.
     */
    public static boolean addHub(OAObject oaObj, Hub hub, boolean bAlwaysAddIfM2M, boolean bCheckExisting) {
        if (oaObj == null || hub == null) return false;
        hub = hub.getRealHub();

        // 20120702 dont store hub if M2M&Private: reverse linkInfo does not have a method.
        // since this could have a lot of references (ex: VetJobs JobCategory has m2m Jobs)
        if (!bAlwaysAddIfM2M) {
            OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
            if (li != null && li.getPrivateMethod()) {
                if (OAObjectInfoDelegate.isMany2Many(li)) {
                    return false;
                }
            }
        }
        boolean bRemoveFromServerCache = false;
        synchronized (oaObj) {
            int pos;
            if (oaObj.hubs == null) {
                oaObj.hubs = new Object[1];
                pos = 0;
                // CACHE_NOTE: if it was on the Server.cache, it can be removed when it is added to a
                // hub. Need to add to cache if/when it is no longer in a hub.
                if (hub.getMasterObject() != null) {
                    bRemoveFromServerCache = true;
                }
            }
            else {
                int currentSize = oaObj.hubs.length;

                if (bCheckExisting) {
                    for (int i = 0; i < currentSize; i++) {
                        if (oaObj.hubs[i] == null) continue;
                        if (oaObj.hubs[i] == hub) return false;
                        if (oaObj.hubs[i] instanceof WeakReference && ((WeakReference) oaObj.hubs[i]).get() == hub) return false;
                    }
                }

                // check for empty slot at the end
                for (pos = currentSize - 1; pos >= 0; pos--) {
                    if (oaObj.hubs[pos] == null) continue;

                    if (oaObj.hubs[pos] instanceof WeakReference && ((WeakReference) oaObj.hubs[pos]).get() == null) {
                        oaObj.hubs[pos] = null;
                        continue;
                    }
                    // found last used slot
                    if (pos < currentSize - 1) {
                        pos++; // first empty slot
                        break;
                    }

                    // need to expand
                    int newSize = currentSize + 1 + (currentSize / 3);
                    newSize = Math.min(newSize, currentSize + 50);
                    Object[] objs = new Object[newSize];

                    System.arraycopy(oaObj.hubs, 0, objs, 0, currentSize);
                    oaObj.hubs = objs;
                    pos = currentSize;
                    break;
                }
                if (pos < 0) pos = 0;

                if (hub.getMasterObject() != null) {
                    bRemoveFromServerCache = true;
                    boolean b = false;
                    for (int i = 0; i < pos; i++) {
                        Object obj = oaObj.hubs[i];
                        if (obj == null) continue;
                        Hub h;
                        if (obj instanceof WeakReference) {
                            h = (Hub) ((WeakReference) obj).get();
                            if (h == null) continue;
                        }
                        else h = (Hub) obj;

                        if (h.getMasterObject() != null) {
                            bRemoveFromServerCache = false; // already done
                            break;
                        }
                    }
                }
            }
            if (oaObj.isChanged() || oaObj.isNew()) oaObj.hubs[pos] = hub; // so that it is reachable by
                                                                           // hubs, and that they dont
                                                                           // get GCd.
            else oaObj.hubs[pos] = new WeakReference(hub);
        }
        if (bRemoveFromServerCache && OARemoteThreadDelegate.shouldSendMessages()) {
            OAObjectCSDelegate.removeFromServerSideCache(oaObj);
        }
        return true;
    }

    /**
     * Used by Hub to read serialized objects. Check to see if this object is already loaded in a hub
     * with same LinkInfo.
     */
    public static boolean isAlreadyInHub(OAObject oaObj, OALinkInfo li) {
        if (oaObj == null || li == null) return false;

        Object[] objs = oaObj.hubs;
        for (int i = 0; objs != null && i < objs.length; i++) {
            Object obj = objs[i];
            if (obj == null) continue;
            if (obj instanceof WeakReference) {
                obj = ((WeakReference) obj).get();
            }
            Hub h = (Hub) obj;
            if (h != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(h) == li) return true;
        }
        return false;
    }

    public static Hub getHub(OAObject oaObj, OALinkInfo li) {
        if (oaObj == null || li == null) return null;

        Object[] objs = oaObj.hubs;
        for (int i = 0; objs != null && i < objs.length; i++) {
            Object obj = objs[i];
            if (obj == null) continue;
            if (obj instanceof WeakReference) {
                obj = ((WeakReference) obj).get();
            }
            Hub h = (Hub) obj;
            if (h != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(h) == li) return h;
        }
        return null;
    }

    /**
     * Used by Hub.add() before adding, quicker then checking array
     */
    public static boolean isAlreadyInHub(OAObject oaObj, Hub hubFind) {
        if (oaObj == null || hubFind == null) return false;
        hubFind = hubFind.getRealHub();
        boolean b = _isAlreadyInHub(oaObj, hubFind);
        if (b) return true;

        Object master = hubFind.getMasterObject();
        if (master == null) return false;

        // could be in the hub, but not in weakHubs, if M2M and private
        // ex: VJ jobCategories M2M Jobs, where jobCategory objects dont have weakhub for
        // all of the Job.jobCategories Hubs that exist
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hubFind);
        if (li == null) return false;
        if (li.getPrivateMethod()) { // if hub method is off
            if (OAObjectInfoDelegate.isMany2Many(li)) { // m2m objects do not have Hub in weakRef[]
                return HubDataDelegate.containsDirect(hubFind, oaObj);
            }
        }
        return false;
    }

    private static boolean _isAlreadyInHub(OAObject oaObj, Hub hubFind) {
        if (oaObj == null) return false;

        Object[] objs = oaObj.hubs;
        for (int i = 0; objs != null && i < objs.length; i++) {
            Object obj = objs[i];
            if (obj == null) continue;
            if (obj instanceof WeakReference) {
                obj = ((WeakReference) obj).get();
            }
            Hub h = (Hub) obj;
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
