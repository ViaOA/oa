package com.viaoa.hub;

import java.util.Comparator;
import java.util.logging.Logger;

import com.viaoa.cs.*;
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
	public static OAObjectMessage removeFromHub(Hub thisHub, OAObject obj, int pos) {
//qqq todo: send pos??		
        if (!(thisHub.datam.masterObject instanceof OAObject)) return null;
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return null;
	    
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
	    if (oi.getLocalOnly()) return null;

	    // 20130319 dont send out calc changes
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return null;
        }
    	
        if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return null;
    	
        // must have a master object to be able to know which hub to add object to
        // send REMOVE message
        OAObjectMessage msg = new OAObjectMessage();
        msg.setType(msg.REMOVE);
        msg.setObjectClass(obj.getClass());
        msg.setObjectKey( OAObjectKeyDelegate.getKey((OAObject) obj) );
        msg.setMasterClass( thisHub.datam.masterObject.getClass() );
        msg.setMasterObjectKey(OAObjectKeyDelegate.getKey(thisHub.datam.masterObject));
        msg.setProperty(HubDetailDelegate.getPropertyFromMasterToDetail(thisHub));
        int x = 0;
        if (obj.getNew()) {
            if (!OAObjectHubDelegate.isInHub(obj)) x = 1;
        }
        msg.setPosTo(x);  // flag to know if object should be added back to the client cache on server
        msg.setWillNotifyWhenProcessed(true);
        client.sendMessage(msg);
        return msg;
	}

	
	/**
	 * Have object added to same Hub on other workstations.
	 */
	public static OAObjectMessage addToHub(Hub thisHub, OAObject obj) {
//qqqqqqqqq change: obj could be a String, etc.		
        return addToHub(thisHub, obj, OAObjectMessage.ADD, 0);
	}
	
	private static OAObjectMessage addToHub(Hub thisHub, OAObject obj, int msgType, int pos) {
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return null;
	    
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(obj);
	    if (oi.getLocalOnly()) return null;

	    // 20130319 dont send out calc changes
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return null;
        }

        if (!(thisHub.datam.masterObject instanceof OAObject)) return null;
	    if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return null;

        // must have a master object to be able to know which hub to add object to
        // send ADD message
        OAObjectMessage msg = new OAObjectMessage();
        msg.setType(msgType);
        msg.setPos(pos);

        // flag to know if object should be removed from client cache on server 
        int x = 0;
        if (obj.getNew()) {
            if (OAObjectHubDelegate.isInHub(obj)) x = 1;
        }
        msg.setPosTo(x);  // flag to know if object should be removed from client cache on server
        
        // 20110323 note: must send object, other clients might not have it.        
        // 20110315 note: was: dont send object, since it will also be returned - after it has been changed on server (set property to master object)
        // msg.setNewValue(OAObjectKeyDelegate.getKey(obj));
        msg.setNewValue(obj);
        msg.setMasterClass(thisHub.datam.masterObject.getClass());
        msg.setMasterObjectKey(OAObjectKeyDelegate.getKey(thisHub.datam.masterObject));
        msg.setProperty(HubDetailDelegate.getPropertyFromMasterToDetail(thisHub));
        msg.setWillNotifyWhenProcessed(true);
        client.sendMessage(msg);
        return msg;
	}	

	protected static void messageProcessed(OAObjectMessage msg) {
		OAClientDelegate.messageProcessed(msg); 
	}
	
	/**
	 * Have object inserted in same Hub on other workstations.
	 */
	public static OAObjectMessage insertInHub(Hub thisHub, OAObject obj, int pos) {
		return addToHub(thisHub, obj, OAObjectMessage.INSERT, pos);
	}	
	
	
	
	/**
	 * Have object added to same Hub on other workstations.
	 */
	public static OAObjectMessage moveObjectInHub(Hub thisHub, int posFrom, int posTo, boolean bWillNotifyWhenProcessed) {
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return null;

	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
	    if (oi.getLocalOnly()) return null; 
    	
        // 20130319 dont send out calc changes
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return null;
        }
	    
        if (!(thisHub.datam.masterObject instanceof OAObject)) return null;
	    if (OAObjectInfoDelegate.getOAObjectInfo((OAObject)thisHub.datam.masterObject).getLocalOnly()) return null;
	    
	    
	    
        // must have a master object to be able to know which hub to use
        // send MOVE message
        OAObjectMessage msg = new OAObjectMessage();
        msg.setType(msg.MOVE);
        msg.setObjectClass(thisHub.getObjectClass());
        msg.setMasterClass(thisHub.datam.masterObject.getClass());
        msg.setMasterObjectKey(OAObjectKeyDelegate.getKey(thisHub.datam.masterObject));
        msg.setPos(posFrom);
        msg.setPosTo(posTo);
        msg.setProperty(HubDetailDelegate.getPropertyFromMasterToDetail(thisHub));
        msg.setWillNotifyWhenProcessed(bWillNotifyWhenProcessed);
        client.sendMessage(msg);
        if (bWillNotifyWhenProcessed) return msg;
        return null;
	}

	public static boolean isServer() {
		OAClient client = OAClient.getClient();
		return (client == null || client.isServer());
	}		
	public static boolean isRunning() {
		OAClient client = OAClient.getClient();
		return (client != null);
	}		
	public static boolean isClientThread() {
		OAClient client = OAClient.getClient();
		return (client != null && client.isClientThread());
	}		
	
	/**
	 * @return true if sort is done, else false if sort has not been done.
	 */
	public static OAObjectMessage sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp) {
        OAClient client = OAClient.getClient();
	    if (client == null || !OAClientDelegate.shouldSendMessage()) return null;
	    
        if (!(thisHub.datam.masterObject instanceof OAObject)) return null;
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
        if (oi.getLocalOnly()) return null; 
        // 20130319 dont send out calc changes
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return null;
        }

        
	    OAObjectMessage msg = new OAObjectMessage();
        msg.setType(msg.SORT);
        msg.setObjectClass(thisHub.getObjectClass());
        msg.setMasterClass(thisHub.datam.masterObject.getClass());
        msg.setMasterObjectKey(OAObjectKeyDelegate.getKey(thisHub.datam.masterObject));
        msg.setProperty(HubDetailDelegate.getPropertyFromMasterToDetail(thisHub));
        msg.setPos((bAscending?0:1));
        msg.setNewValue(propertyPaths);
        // msg.setWillNotifyWhenProcessed(bWillNotifyWhenProcessed);
        client.sendMessage(msg);
        //if (bWillNotifyWhenProcessed) return msg;
        return null;
	}
	
    /**
     * 20120325
    */
    protected static boolean deleteAll(Hub thisHub) {
        LOG.fine("hub="+thisHub);
        OAClient client = OAClient.getClient();
        if (client == null || !OAClientDelegate.shouldSendMessage()) return false;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
        if (oi.getLocalOnly()) return false; 
        // 20130319 dont send out calc changes
        OALinkInfo li = thisHub.datam.liDetailToMaster;
        if (li != null) {
            OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (liRev != null && liRev.getCalculated()) return false;
        }

        Object master = thisHub.getMasterObject();
        if (master == null) return false;
        if (!(master instanceof OAObject)) return false;

        String prop = HubDetailDelegate.getPropertyFromMasterToDetail(thisHub);
        if (prop == null) return false;

        OAObjectMessage msg = new OAObjectMessage();
        msg.setType(OAObjectMessage.DELETEALL);
        msg.setObjectClass(thisHub.getObjectClass());
        msg.setMasterClass(master.getClass());
        msg.setMasterObjectKey( OAObjectKeyDelegate.getKey((OAObject)master) );
        msg.setProperty(prop);
        
        client.sendMessage(msg);
        if (msg.getNewValue() != null) throw new RuntimeException((String) msg.getNewValue());
        
        return true;
    }
	
}






















