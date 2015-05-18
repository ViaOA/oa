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

import java.util.Comparator;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteSyncInterface;
import com.viaoa.object.*;

/**
 * Delegate that manages client/server functionality, so that the same hub in 
 * other systems is in-sync.
 * @author vvia
 *
 */
public class HubCSDelegate {
    private static Logger LOG = Logger.getLogger(HubCSDelegate.class.getName());

    /**
     * 20140422
     * @param thisHub
     */
    public static void removeAllFromHub(Hub thisHub) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!(thisHub.datam.masterObject instanceof OAObject)) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) {
            return;
        }

        // 20140708 
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return;
        }
        

        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.removeAllFromHub(
                thisHub.datam.masterObject.getClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub) 
            );
        }
    }
    
    /**
	 * Have object removed from same Hub on other workstations.
	 */
	public static void removeFromHub(Hub thisHub, OAObject obj, int pos) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!(thisHub.datam.masterObject instanceof OAObject)) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) {
            return;
        }
	    
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
	    if (oi.getLocalOnly()) return;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) {
                return;
            }
        }
    	
        if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return;
    	
        // must have a master object to be able to know which hub to add object to
        // send REMOVE message
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.removeFromHub(
                    thisHub.datam.masterObject.getClass(), 
                    thisHub.datam.masterObject.getObjectKey(), 
                    HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), 
                    obj.getClass(), obj.getObjectKey());
        }
	}

	/**
	 * Have object added to same Hub on other workstations.
	 */
	public static void addToHub(Hub thisHub, OAObject obj) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;
        
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
	    if (oi.getLocalOnly()) return;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return;
        }

        if (!(thisHub.datam.masterObject instanceof OAObject)) return;
	    if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return;

        // must have a master object to be able to know which hub to add object to
        // send ADD message

	    if (thisHub.isFetching()) {
	        return; // 20140309
	    }
	    
	    // 20140314 dont need to send if only on client so far
        boolean bClientSideCache = OAObjectCSDelegate.isInClientSideCache(thisHub.datam.masterObject);
	    if (bClientSideCache) {
	        return;
	    }

        // 20110323 note: must send object, other clients might not have it.        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.addToHub(
                thisHub.datam.masterObject.getClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), obj);
        }
	}	

	/**
	 * Have object inserted in same Hub on other workstations.
	 */
	public static boolean insertInHub(Hub thisHub, OAObject obj, int pos) {
        if (OASyncDelegate.isSingleUser()) return false;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return  false;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
        if (oi.getLocalOnly()) return false;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return false;
        }

        if (!(thisHub.datam.masterObject instanceof OAObject)) return false;
        if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return false;

        // must have a master object to be able to know which hub to add object to
        // send ADD message

        // 20110323 note: must send object, other clients might not have it.        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.insertInHub(
                    thisHub.datam.masterObject.getClass(), 
                    thisHub.datam.masterObject.getObjectKey(), 
                    HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), 
                    obj, pos);
        }
        return true;
	}	
	
	/**
	 * Have object added to same Hub on other workstations.
	 */
	public static void moveObjectInHub(Hub thisHub, int posFrom, int posTo) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;
        
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
	    if (oi.getLocalOnly()) return; 
    	
        // 20130319 dont send out calc changes
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return;
        }

        OAObject objMaster = thisHub.datam.masterObject;
        if (objMaster == null) return;
	    if (OAObjectInfoDelegate.getOAObjectInfo(objMaster).getLocalOnly()) return;
	    
	    
        // must have a master object to be able to know which hub to use
        // send MOVE message
	    
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.moveObjectInHub(thisHub.getObjectClass(), 
                    thisHub.datam.masterObject.getObjectKey(), 
                    HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), posFrom, posTo);
        }
	}

	public static boolean isServer() {
        return OASyncDelegate.isServer();
	}		
	public static boolean isRemoteThread() {
		return (OARemoteThreadDelegate.isRemoteThread());
	}		
	
	/**
	 * @return true if sort is done, else false if sort has not been done.
	 */
	public static void sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return;

        OAObject objMaster = thisHub.datam.masterObject;
        if (objMaster == null) return;
        if (OAObjectInfoDelegate.getOAObjectInfo(objMaster).getLocalOnly()) return;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return;
        }

        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.sort(objMaster.getClass(), objMaster.getObjectKey(), 
                    HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), 
                    propertyPaths, bAscending, comp);
        }
	}
	
    /**
     * 20150206 returns true if this should be deleted on this computer, false if it is done on the server. 
    */
    protected static boolean deleteAll(Hub thisHub) {
        LOG.fine("hub="+thisHub);
        if (OASyncDelegate.isServer()) return true;  // invoke on the server
        
        if (!OARemoteThreadDelegate.shouldSendMessages()) return true;
        if (OAThreadLocalDelegate.isSuppressCSMessages()) return true;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
        if (oi.getLocalOnly()) return true; 
        
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return true;
        }

        OAObject master = thisHub.getMasterObject();
        if (master == null) return true;

        String prop = HubDetailDelegate.getPropertyFromMasterToDetail(thisHub);
        if (prop == null) return true;

        RemoteClientInterface rs = OASyncDelegate.getRemoteClient();
        if (rs == null) return true;
        
        rs.deleteAll(master.getClass(), master.getObjectKey(), prop);
        return false;
    }
    
    // 20150420
    /**
     * Hub hubData.vecAdd/Remove cleared on clients
     */
    public static boolean clearHubChanges(Hub thisHub) {
        if (thisHub == null) return false;
        if (OASync.isSingleUser()) return false;
        if (!OASync.shouldSendMessages()) return  false;
        if (OASync.getSuppressCSMessages()) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
        if (oi.getLocalOnly()) return false;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return false;
        }

        if (!(thisHub.datam.masterObject instanceof OAObject)) return false;
        if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return false;

        RemoteSyncInterface rs = OASyncDelegate.getRemoteSync();
        if (rs != null) {
            rs.clearHubChanges(
                thisHub.datam.masterObject.getClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub) 
            );
        }
        return true;
    }   
}

