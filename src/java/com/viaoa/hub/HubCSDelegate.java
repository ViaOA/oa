package com.viaoa.hub;

import java.util.Comparator;
import java.util.HashSet;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;
import com.viaoa.sync.remote.RemoteServerInterface;
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
	 * Have object removed from same Hub on other workstations.
	 */
	public static void removeFromHub(Hub thisHub, OAObject obj, int pos) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!(thisHub.datam.masterObject instanceof OAObject)) return;

        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
	    
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
	    if (oi.getLocalOnly()) return;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return;
        }
    	
        if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return;
    	
        // must have a master object to be able to know which hub to add object to
        // send REMOVE message
        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        rs.removeFromHub(
                thisHub.datam.masterObject.getClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), 
                obj.getClass(), obj.getObjectKey());
	}

	/**
	 * Have object added to same Hub on other workstations.
	 */
	public static void addToHub(Hub thisHub, OAObject obj) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
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

        // 20110323 note: must send object, other clients might not have it.        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        rs.addToHub(
                thisHub.datam.masterObject.getClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), obj);
	}	

    private static HashSet<Integer> hashServerSideCache = new HashSet<Integer>(379, .75f);
	
	/**
	 * Have object inserted in same Hub on other workstations.
	 */
	public static void insertInHub(Hub thisHub, OAObject obj, int pos) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
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

        // 20110323 note: must send object, other clients might not have it.        
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        rs.insertInHub(
                thisHub.datam.masterObject.getClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), 
                obj, pos);
	}	
	
	/**
	 * Have object added to same Hub on other workstations.
	 */
	public static void moveObjectInHub(Hub thisHub, int posFrom, int posTo) {
        if (OASyncDelegate.isSingleUser()) return;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return;
        
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
	    
        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        rs.moveObjectInHub(thisHub.getObjectClass(), 
                thisHub.datam.masterObject.getObjectKey(), 
                HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), posFrom, posTo);
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

        OAObject objMaster = thisHub.datam.masterObject;
        if (objMaster == null) return;
        if (OAObjectInfoDelegate.getOAObjectInfo(objMaster).getLocalOnly()) return;

        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return;
        }

        RemoteSyncInterface rs = OASyncDelegate.getRemoteSyncInterface();
        if (rs != null) {
            rs.sort(objMaster.getClass(), objMaster.getObjectKey(), 
                    HubDetailDelegate.getPropertyFromMasterToDetail(thisHub), 
                    propertyPaths, bAscending, comp);
        }
	}
	
    /**
     * 20120325
    */
    protected static boolean deleteAll(Hub thisHub) {
        LOG.fine("hub="+thisHub);
        if (OASyncDelegate.isSingleUser()) return false;
        if (!OARemoteThreadDelegate.shouldSendMessages()) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
        if (oi.getLocalOnly()) return false; 
        
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return false;
        }

        OAObject master = thisHub.getMasterObject();
        if (master == null) return false;

        String prop = HubDetailDelegate.getPropertyFromMasterToDetail(thisHub);
        if (prop == null) return false;

        RemoteServerInterface rs = OASyncDelegate.getRemoteServerInterface();
        rs.deleteAll(master.getClass(), master.getObjectKey(), prop);
        return true;
    }
}

