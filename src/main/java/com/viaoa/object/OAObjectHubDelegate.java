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
package com.viaoa.object;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAArray;
import com.viaoa.util.OANullObject;

/**
 * Used by Hub to manage the list of Hubs that an OAObject is a member of.
 * 
 * @author vincevia
 */
public class OAObjectHubDelegate {

    private static Logger LOG = Logger.getLogger(OAObjectHubDelegate.class.getName());
    public static boolean ShowWarnings=true; // if weakHubs.len % 50


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
        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        if (refs == null) return false;
        for (WeakReference<Hub<?>> ref : refs) {
            if (ref != null) {
                if (ref.get() != null) return true;
            }
        }
        return false;
    }

    public static boolean isInHubWithMaster(OAObject oaObj) {
        if (oaObj == null) return false;
        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        if (refs == null) return false;
        for (WeakReference<Hub<?>> ref : refs) {
            if (ref != null) {
                Hub h = ref.get();
                if (h != null && h.getMasterObject() != null) return true;
            }
        }
        return false;
    }


    // 20150105 the obj.weakhubs can be shared, so it can not be nulled out
    /**
     * Called by Hub when an OAObject is removed from a Hub.
     */
    public static void removeHub(final OAObject oaObj, Hub hub, boolean bIsOnHubFinalize) {
        if (oaObj == null || oaObj.weakhubs == null) return;
        
        // 20151230
        Hub hubx = hub.getRealHub();
        if (hubx != hub) {
            if (bIsOnHubFinalize) return; // the sharedHub is being finalized
            hub = hubx;
        }

        
        boolean bFound = false;
        synchronized (oaObj) {
            if (oaObj.weakhubs == null) return;
            int currentSize = oaObj.weakhubs.length;
            int lastEndPos = currentSize - 1;

            for (int pos = 0; !bFound && pos < currentSize; pos++) {
                if (oaObj.weakhubs[pos] == null) break; // the rest will be nulls

                Hub hx = oaObj.weakhubs[pos].get();

                bFound = (hx == hub);
                if (hx != null && !bFound) continue;
                
                if (currentSize < 4) {
                    // 20160105 weakhubs[] size <4 can be shared by other objs - need to create a new weakref[]
                    if (currentSize == 1) oaObj.weakhubs = null;
                    else oaObj.weakhubs = (WeakReference<Hub<?>>[]) OAArray.removeAt(WeakReference.class, oaObj.weakhubs, pos);
                    if (!bFound && oaObj.weakhubs != null) {
                        pos--; // need to revisit
                        currentSize--;  // array was resized
                        continue;
                    }
                    break;
                }
                else {
                    oaObj.weakhubs[pos] = null;
    
                    // compress: get last one, move it back to this slot
                    for (; lastEndPos > pos; lastEndPos--) {
                        if (oaObj.weakhubs[lastEndPos] == null) continue;
                        if (oaObj.weakhubs[lastEndPos] instanceof WeakReference && ((WeakReference) oaObj.weakhubs[lastEndPos]).get() == null) {
                            oaObj.weakhubs[lastEndPos] = null;
                            continue;
                        }
                        oaObj.weakhubs[pos] = oaObj.weakhubs[lastEndPos];
                        oaObj.weakhubs[lastEndPos] = null;
                        if (!bFound) {
                            pos--; // need to revisit this slot (currentSize is still the same)
                        }
                        break;
                    }
                    if (!bFound) continue;
                    
                    if (currentSize > 10 && ((currentSize - lastEndPos) < (currentSize * .75))) {
                        // resize array
                        int newSize = lastEndPos + (lastEndPos / 10) + 1;
                        newSize = Math.min(lastEndPos + 20, newSize);
                        WeakReference<Hub<?>>[] refs = new WeakReference[newSize];
    
                        System.arraycopy(oaObj.weakhubs, 0, refs, 0, lastEndPos);
                        oaObj.weakhubs = refs;
                        currentSize = newSize;
                    }
                    if (oaObj.weakhubs[0] == null) {
                        oaObj.weakhubs = null;
                        break;
                    }
                    if (bFound) {
                        break;
                    }
                }
            }

            // 20150827
            if (!OASyncDelegate.isClient(oaObj)) return;
            
            // 20130707 could be a hub from hubMerger, that populates with One references
            // which means that the one reference keeps it from gc
            if (!bIsOnHubFinalize && hub.getMasterObject() != null) {
                // 20141201 add !bIsOnHubFinalize so that if it is from a Hub finalize, then dont 
                //    use the finalizer thread to send msg to server.
                if (!isInHubWithMaster(oaObj)) {
                    if (OARemoteThreadDelegate.shouldSendMessages() && !oaObj.isDeleted()) {
                        // CACHE_NOTE: if it was on the Server.cache, it was removed when it was added
                        // to a hub. Need to add to cache now that it is no longer in a hub.
                        
                        // 20150827 dont cache if one2one and owned, and it is assigned to owner
                        // which means that the owner will "hold on to it"
                        boolean b = true;
                        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
                        if (oi.isOwnedAndNoReverseMany()) {
                            OALinkInfo li = oi.getOwnedByOne();
                            if (li != null && OAObjectPropertyDelegate.getProperty(oaObj, li.getName()) != null) {
                                b = false;
                            }
                        }
                        
                        if (b) OAObjectCSDelegate.addToServerSideCache(oaObj);
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

        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        if (refs == null) return null;

        Hub[] hubs = new Hub[refs.length];

        for (int i = 0; i < refs.length; i++) {
            WeakReference<Hub<?>> ref = refs[i];
            if (ref == null) continue;
            hubs[i] = ref.get();
        }
        return hubs;
    }

    public static WeakReference<Hub<?>>[] getHubReferencesNoCopy(OAObject oaObj) { // Note: this needs to be public
        if (oaObj == null) return null;
        return oaObj.weakhubs;
    }

    public static int getHubReferenceCount(OAObject oaObj) {
        if (oaObj == null) return 0;
        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        int cnt = 0;
        for (int i = 0; refs != null && i < refs.length; i++) {
            if (refs[i] != null && refs[i].get() != null) cnt++;
        }
        return cnt;
    }

    public static boolean addHub(OAObject oaObj, Hub hub) {
        // 20140313 was: addHub(oaObj, hub, true, false);
        return addHub(oaObj, hub, false);
    }

    /**
     * Called by Hub when an OAObject is added to a Hub.
     */
    public static boolean addHub(final OAObject oaObj, final Hub hubOrig, final boolean bAlwaysAddIfM2M) {
        if (oaObj == null || hubOrig == null) return false;
        final Hub hub = hubOrig.getRealHub();

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
        boolean bReused = false;
        synchronized (oaObj) {
            int pos;
            if (oaObj.weakhubs == null) {
                
                // 20160105 check to use same as another object in hub
                for (int i=0; i<4; i++) {
                    OAObject objx = (OAObject) hub.getAt(i);
                    if (objx == null) break;
                    if (objx == oaObj) continue;
                    WeakReference[] wrs = objx.weakhubs;
                    if (wrs != null && wrs.length == 1 && wrs[0].get() == hub) {
                        oaObj.weakhubs = wrs;
                        bReused = true;
                        break;
                    }
                }
                if (!bReused) {
                    oaObj.weakhubs = new WeakReference[1];
                }
                pos = 0;
                // CACHE_NOTE: if it was on the Server.cache, it can be removed when it is added to a
                // hub. Need to add to cache if/when it is no longer in a hub.
                if (hub.getMasterObject() != null) {
                    bRemoveFromServerCache = true;
                }
            }
            else {
                final int currentSize = oaObj.weakhubs.length;

                // check for empty slot at the end
                for (pos = currentSize - 1; pos >= 0; pos--) {
                    if (oaObj.weakhubs[pos] == null) continue;

                    Hub h = oaObj.weakhubs[pos].get(); 
                    if (h == null) {
                        oaObj.weakhubs[pos] = null;
                        continue;
                    }
                    
                    if (h == hub) {
                        return false;
                    }
                    
                    // found last used slot
                    if (pos < currentSize - 1) {
                        pos++; // first empty slot
                        break;
                    }

                    // no room
                    
                    // 20160105 if currentSize<3, check to see if it can use the same as another obj in hub
                    for (int i=0; currentSize<3 && i<4; i++) {
                        OAObject objx = (OAObject) hub.getAt(i);
                        if (objx == null) break;
                        if (objx == oaObj) continue;
                        WeakReference[] wrs = objx.weakhubs;
                        if (wrs == null) continue;
                        if (wrs.length != currentSize+1) continue;
                        
                        bReused = true;
                        for (int j=0; j<currentSize; j++) {
                            if (wrs[j] != oaObj.weakhubs[j]) {
                                bReused = false;
                                break;
                            }
                        }
                        if (bReused) {
                            if (wrs[currentSize].get() != hub) bReused = false;
                            else {
                                oaObj.weakhubs = wrs;
                                pos = currentSize;
                                break;
                            }
                        }
                    }
                    
                    if (!bReused) {
                        // need to expand
                        int newSize = currentSize + (currentSize / 10) + 1;
                        newSize = Math.min(newSize, currentSize + 20);
                        WeakReference<Hub<?>>[] refs = new WeakReference[newSize];
    
                        System.arraycopy(oaObj.weakhubs, 0, refs, 0, currentSize);
                        oaObj.weakhubs = refs;
                        pos = currentSize;
                    }                    
                    break;
                }
                if (pos < 0) pos = 0;

                if (hub.getMasterObject() != null) {
                    bRemoveFromServerCache = true;
                    for (int i = 0; i < pos; i++) {
                        WeakReference<Hub<?>> ref = oaObj.weakhubs[i];
                        if (ref == null) continue;
                        Hub h = ref.get();
                        if (h != null && h.getMasterObject() != null) {
                            bRemoveFromServerCache = false; // already done
                            break;
                        }
                    }
                }
            }
            if (!bReused) {
                // 20160105 see if weakRef can be reused
                boolean b = false;
                for (int i=0; !b && i<4; i++) {
                    OAObject objx = (OAObject) hub.getAt(i);
                    if (objx == null) break;
                    if (objx == oaObj) continue;
                    WeakReference<Hub<?>>[] wrs = objx.weakhubs;
                    if (wrs == null) continue;
                    for (WeakReference wr : wrs) {
                        if (wr == null) break;
                        if (wr.get() == hub) {
                            oaObj.weakhubs[pos] = wr;
                            b = true;
                            break;
                        }
                    }
                }
                if (!b) {
                    oaObj.weakhubs[pos] = new WeakReference(hub);
                }
                else aiReuseWeakRef.incrementAndGet();
                if (pos>0 && pos%50==0 && ShowWarnings) {
                    LOG.warning("object="+oaObj+", weakhubs="+pos);
                }
            }
        }
        if (bReused) aiReuseWeakRefArray.incrementAndGet();

        if (bRemoveFromServerCache && OARemoteThreadDelegate.shouldSendMessages()) {
            OAObjectCSDelegate.removeFromServerSideCache(oaObj);
        }
        return true;
    }

    public static final AtomicInteger aiReuseWeakRefArray = new AtomicInteger();
    public static final AtomicInteger aiReuseWeakRef = new AtomicInteger();
    /**
     * Used by Hub to read serialized objects. Check to see if this object is already loaded in a hub
     * with same LinkInfo.
     */
    public static boolean isAlreadyInHub(OAObject oaObj, OALinkInfo li) {
        if (oaObj == null || li == null) return false;

        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        for (int i = 0; refs != null && i < refs.length; i++) {
            WeakReference<Hub<?>> ref = refs[i];
            if (ref == null) continue;
            Hub h = ref.get();
            if (h != null && HubDetailDelegate.getLinkInfoFromDetailToMaster(h) == li) return true;
        }
        return false;
    }

    public static Hub getHub(OAObject oaObj, OALinkInfo li) {
        if (oaObj == null || li == null) return null;

        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        for (int i = 0; refs != null && i < refs.length; i++) {
            WeakReference<Hub<?>> ref = refs[i];
            if (ref == null) continue;
            Hub h = ref.get();
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

        WeakReference<Hub<?>>[] refs = oaObj.weakhubs;
        for (int i = 0; refs != null && i < refs.length; i++) {
            WeakReference<Hub<?>> ref = refs[i];
            if (ref == null) continue;
            Hub h = ref.get();
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

    protected static void setMasterObject(Hub hub, OAObject oaObj, OALinkInfo liDetailToMaster) {
        if (HubDetailDelegate.getMasterObject(hub) == null) {
            HubDetailDelegate.setMasterObject(hub, oaObj, liDetailToMaster);
        }
    }

    public static void setMasterObject(Hub hub, OAObject oaObj, String nameFromMasterToDetail) {
        if (hub == null || oaObj == null || nameFromMasterToDetail == null) return;
        Object objx = HubDetailDelegate.getMasterObject(hub);
        if (objx != null && objx == oaObj) {
            return;  // already set
        }

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        
        OALinkInfo li = oi.getLinkInfo(nameFromMasterToDetail);
        if (li == null) return;
        li = OAObjectInfoDelegate.getReverseLinkInfo(li);
        HubDetailDelegate.setMasterObject(hub, oaObj, li);
    }
}
