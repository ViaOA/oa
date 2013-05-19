package com.viaoa.hub;

import java.util.logging.Logger;

import com.viaoa.cs.*;
import com.viaoa.object.*;

/**
 * Delegate for handling adding and removing from Hub.
 * @author vvia
 *
 */
public class HubAddRemoveDelegate {

    private static Logger LOG = Logger.getLogger(HubAddRemoveDelegate.class.getName());
    
    public static void remove(Hub thisHub, Object obj) {
        remove(thisHub, obj, false, true, false, true, true);
    }

    public static void remove(Hub thisHub, int pos) {
        remove(thisHub, pos, false);
    }
    
    protected static void remove(Hub thisHub, int pos, boolean bForce) {
        Object obj = HubDataDelegate.getObjectAt(thisHub, pos);
        remove(thisHub, obj, bForce, true, false, true, true);
    }

    
    public static void remove(Hub thisHub, Object obj, boolean bForce, boolean bSendEvent, boolean bDeleting, boolean bSetAO, boolean bSetPropToMaster) {
        if (obj == null) return;
        
        if (thisHub.datau.sharedHub != null) {
            remove(thisHub.datau.sharedHub, obj, bForce, bSendEvent, bDeleting, bSetAO, true);
            return;
        }

        // 20110104 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            _remove(thisHub, obj, bForce, bSendEvent, bDeleting, bSetAO, bSetPropToMaster);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
    }
    
    private static void _remove(Hub thisHub, Object obj, boolean bForce, boolean bSendEvent, boolean bDeleting, boolean bSetAO, boolean bSetPropToMaster) {
        obj = HubDelegate.getRealObject(thisHub, obj);
        if (obj == null) return;

        // check to see if this hub is a detail with LinkInfo.Type.ONE
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(thisHub);
        if (li != null && (li.getType() == OALinkInfo.MANY)) {
            li = OAObjectInfoDelegate.getReverseLinkInfo(li);
            if (li != null && li.getType() == OALinkInfo.ONE) {
                if (!OAThreadLocalDelegate.isDeleting(obj)) {
                    if (!OAClient.isClientThread()) {
                        throw new RuntimeException("Cant remove object from Hub that is based on a LinkInfo.ONE, hub="+thisHub);
                    }
                }
            }
        }
        
        int pos = HubDataDelegate.getPos(thisHub, obj, false, false); // dont adjust master or update link when finding the postion of the object.
        if (pos < 0) return;
        if (bSendEvent) {
            HubEventDelegate.fireBeforeRemoveEvent(thisHub, obj, pos);
        }

        // send message to OAServer
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(thisHub.getObjectClass());
        OAObjectMessage msg = null;
        
        try {
            if (thisHub.isOAObject()) {
                msg = HubCSDelegate.removeFromHub(thisHub, (OAObject) obj, pos);
            }
            pos = HubDataDelegate._remove(thisHub, obj, bDeleting);
            if (pos < 0) {
                LOG.warning("object not removed, obj="+obj);
                return;
            }
        }
        finally {
            // this will "tell" OAClientMessageHandler that it is ok to continue
            HubCSDelegate.messageProcessed(msg); // even if msg is null, since this could be a OAClientThread
        }
        
        if (bSetAO) {
            HubShareDelegate.setSharedHubsAfterRemove(thisHub, obj, pos);
        }
        if (bSetPropToMaster) {
            // set the reference in detailObject to null.  Ex: if this is DeptHub, and Obj is Emp then call emp.setDept(null)
            HubDetailDelegate.setPropertyToMasterHub(thisHub, obj, null);
        }

        /* 20110439 need to do this before sending event, since
            hub.containts(obj) now uses obj.weakHubs to know if an object is in the hub.
        */
        if (thisHub.isOAObject()) OAObjectHubDelegate.removeHub((OAObject)obj, thisHub);  
        
        // this must be after bSetAO, so that the active object is updated. 
        if (bSendEvent) {
            HubEventDelegate.fireAfterRemoveEvent(thisHub, obj, pos);
        }

/** 20110439 was
        // this must be after all events have been sent,
        //   otherwise, if the object is removed from the Hub, then it will not send events to it.
        if (thisHub.isOAObject()) OAObjectHubDelegate.removeHub((OAObject)obj, thisHub);  
*/    
    }

    public static void clear(Hub thisHub) {
        clear(thisHub, true, true);
    }
    
    public static void clear(Hub thisHub, boolean bSetAOtoNull, boolean bSendNewList) {
        if (thisHub.datau.sharedHub != null) {
            clear(thisHub.datau.sharedHub, bSetAOtoNull, bSendNewList);
            return;
        }
    
        HubSelectDelegate.cancelSelect(thisHub, false);

        HubEventDelegate.fireBeforeRemoveAllEvent(thisHub);
        
        int x = HubDataDelegate.getCurrentSize(thisHub);
        if (bSetAOtoNull) thisHub.setActiveObject(null);

        // 20110104 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            
            // 20120627 need to send event to clients if there is a masterObject
            boolean bSendEvent = thisHub.getMasterObject() != null;
            
            for ( x--; x>=0; x-- ) {
                Object ho = HubDataDelegate.getObjectAt(thisHub, x);
                remove(thisHub, ho, false, bSendEvent, false, bSetAOtoNull, bSetAOtoNull); // dont force, dont send remove events
                //was: remove(thisHub, ho, false, false, false, bSetAOtoNull, bSetAOtoNull); // dont force, dont send remove events
            }
            if (bSendNewList) {
                HubEventDelegate.fireOnNewListEvent(thisHub, true);
            }
            HubEventDelegate.fireAfterRemoveAllEvent(thisHub);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
            
    }
    
    /**
        Used to find out if an object can be added/inserted to this Hub.
        Makes sure that object that being added is for the correct class.
        Calls all HubListeners.hubBeforeAdd() where HubEvent.object and pos are both set.
        If objects are OAObjets, then canAdd is called for each object.
        If it is a recursive Hub, then it will verify that it can have the parent set.
        @see OAObject#canAdd
        @see #setObjectClass
    */
    public static boolean canAdd(Hub thisHub, Object obj) {
        String s = canAddMsg(thisHub, obj);
        return s == null;
    }
    // returns null if obj can be added; otherwise an error msg is returned.
    public static String canAddMsg(Hub thisHub, Object obj) {
        if (obj == null) return "obj is null";
        if (thisHub == null) return "hub is null";
        if (thisHub.datau.sharedHub != null) {
            return canAddMsg(thisHub.datau.sharedHub, obj);
        }
    
        HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
        if (dm.masterHub != null) {
            // if there is a masterHub, then make sure that this Hub is active/valid
            if (thisHub.datam.masterObject == null) {
                return "has masterHub, but masterObject is null";
            }
        }
        
        Class c = obj.getClass();
        if (thisHub.datau.objClass == null) HubDelegate.setObjectClass(thisHub, c);
        if (!thisHub.datau.objClass.isAssignableFrom(c) ) return "class not assignable, class="+c;

        if (thisHub.isLoading()) return null;
        if (thisHub.data.uniqueProperty != null) {
            if (!HubDelegate.verifyUniqueProperty(thisHub, obj)) {
                return "verifyUniqueProperty returned false";
            }
        }

        if (obj.getClass().equals(c)) {
            // cant add a recursive object to its children Hub
            // cant make a recursive object have one of its children as the parent

            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);
            OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
            if (li != null) { 
                Object master = HubDetailDelegate.getMasterObject(thisHub); 
                if (master != null) {
                    for (; master != null;) {
                        if (master == obj) return "recursive hub, cant add child as parent";
                        master = li.getValue(master); 
                    }
                }
            }           
        }
        return null;
    }

    
    public static void add(Hub thisHub, Object obj) {
        if (obj == null) return;
        if (thisHub.datau.sharedHub != null) {
            add(thisHub.datau.sharedHub, obj);
            return;
        }

        if (!thisHub.data.bInFetch && thisHub.data.sortListener != null) {
            insert(thisHub, obj, thisHub.getSize());
            return;
        }

        try {
            OAThreadLocalDelegate.lock(thisHub);
            _add(thisHub, obj);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
    }
    
    /**
        Add an Object to end of collection.  All listeners will be notified of add event.
     */
    private static void _add(Hub thisHub, Object obj) {
        if (obj instanceof OAObjectKey) {
            // store OAObjectKey.  Real object will be retrieved when it is accessed
            internalAdd(thisHub, obj, true);
            return;
        }

        if (thisHub.contains(obj)) return;
        
        String s = canAddMsg(thisHub, obj);
        if (s != null) {
            throw new RuntimeException("Hub.canAddMsg() returned error="+s+", Hub="+thisHub);
        }
        
        if (!thisHub.data.bInFetch) {
            HubEventDelegate.fireBeforeAddEvent(thisHub, obj, thisHub.getCurrentSize());
        }
    
        // send message to OAServer
        OAObjectMessage msg = null;
        
        try {
            if (thisHub.isOAObject()) {
                msg = HubCSDelegate.addToHub(thisHub, (OAObject) obj);
            }
            if (!internalAdd(thisHub,obj,true)) {
                //LOG.warning("VVVVVVVVVVVV NOT ADDED <<<<<<<<<<<<<<<<<<<<<<<<<<<<<");//qqqqqqqqqqqqqqqqq
                return;
            }
        }
        finally {
            // this will "tell" OAClientMessageHandler that it is ok to continue
            HubCSDelegate.messageProcessed(msg); // even if msg is null, since this could be a OAClientThread
        }
        
        // moved before listeners are notified.  Else listeners could ask for more objects
        HubDetailDelegate.setPropertyToMasterHub(thisHub, obj, thisHub.datam.masterObject);
        
        // if recursive and this is the root hub, then need to set parent to null (since object is now in root, it has no parent)
        Hub rootHub = thisHub.getRootHub();
        if (rootHub != null) {
            if (rootHub == thisHub) {
                OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.datau.objectInfo, OALinkInfo.ONE);
                if (liRecursive != null) {
                    OAObjectReflectDelegate.setProperty((OAObject)obj, liRecursive.getName(), null, null);
                }
            }
        }
        if (!thisHub.data.bInFetch) {
            HubEventDelegate.fireAfterAddEvent(thisHub, obj, thisHub.getCurrentSize()-1);
        }
        else { // 20120425 need to send ObjectCache event
            // 20130518 dont send if bInFetch (too much noise)
            // OAObjectCacheDelegate.fireAfterAddEvent(thisHub, obj, thisHub.getCurrentSize()-1);
        }
    }

    /** internal method to add to vector and hashtable
     */
    protected static boolean internalAdd(Hub thisHub, Object obj, boolean bUpdateChanged) {
        if (obj == null) return false;

        OAObjectKey key;
        if (obj instanceof OAObjectKey) key = (OAObjectKey) obj;
        else {
            if (obj instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject)obj);
            else {
                key = OAObjectKeyDelegate.convertToObjectKey(thisHub.getObjectClass(), obj);
            }
        }

        if (!HubDataDelegate._add(thisHub, key, obj, false)) return false;
       
        if (obj instanceof OAObject) {
            OAObjectHubDelegate.addHub((OAObject)obj, thisHub);
        }
        return true;
    }

    
    protected static void sortMove(Hub thisHub, Object obj) {
        int pos = thisHub.getPos(obj);
        move(thisHub, pos, pos);
    }

    /**
        Swap the position of two different objects within the hub.  This will
        call the move method.  Sends a hubMove event to all HubListeners.
        @param posFrom position of object to move
        @param posTo position where object should be after the move
    */
    protected static void move(Hub thisHub, int posFrom, int posTo) {
        if (posFrom == posTo) {
            if (thisHub.data.sortListener == null) return;
        }
        if (posFrom < 0 || posTo < 0) return;
        if (thisHub.datau.sharedHub != null) {
            move(thisHub.datau.sharedHub, posFrom, posTo);
            return;
        }
        
        // 20110104 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            _move(thisHub, posFrom, posTo);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
    }
    private static void _move(Hub thisHub, int posFrom, int posTo) {

        Object objFrom = thisHub.elementAt(posFrom);
        if (objFrom == null) return;
        
        int max = thisHub.getSize();
        if (posFrom >= max) return;
    
        /* if Hub is sorted, need to find valid toPosition. */
        if (thisHub.data.sortListener != null) {
            boolean b=false;
            for (int i=0; ; i++) {
                Object cobj = thisHub.elementAt(i);
                if (cobj == null) {
                    posTo = (i-1);
                    break;
                }
                if (cobj == objFrom) {
                    b = true;
                    continue; // skip object that is moving
                }
                if (thisHub.data.sortListener.comparator.compare(objFrom, cobj) <= 0) {
                    posTo = i;
                    if (b) posTo--;
                    break;
                }
            }
            if (posFrom == posTo) return;
        }
        if (posTo >= max) posTo = (max-1);

        HubEventDelegate.fireBeforeMoveEvent(thisHub, posFrom, posTo);
        
        OAObjectMessage msg = null;
        try {
            //  OAClient must send message to OAServer before continuing
            msg = HubCSDelegate.moveObjectInHub(thisHub, posFrom, posTo, true);
            HubDataDelegate._move(thisHub, objFrom, posFrom, posTo);
        }
        finally {
            // this will "tell" OAClientMessageHandler that it is ok to continue
            HubCSDelegate.messageProcessed(msg); // even if msg is null, since this could be a OAClientThread
        }   
        
        HubEventDelegate.fireAfterMoveEvent(thisHub, posFrom, posTo);
        // dont reset activeObject, it will reset detailHubs
    }

    
    
    
    /**
        Insert an Object at a position.
        Hub Listeners will be notified with an insert event.
        <p>
        If Hub is sorted, then object will be inserted at correct/sorted position.
    
        @param obj Object to insert, must be from the same class that was used when creating the Hub
        @param pos position to insert the object into the Hub.  If greater then size of Hub, then it will be added to the end.
        @return true if object was added else false (event hubBeforeAdd() threw an exception)
        @see #getObjectClass
        @see #add
        @see #sort
    */
    public static boolean insert(Hub thisHub, Object obj, int pos) {
        if (obj == null) return false;
        if (thisHub.datau.sharedHub != null) {
            return insert(thisHub.datau.sharedHub, obj, pos);
        }
        // 20110104 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            return _insert(thisHub, obj, pos);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
    }
    private static boolean _insert(Hub thisHub, Object obj, int pos) {
        
        if (obj instanceof OAObjectKey) {
            // store OAObjectKey.  Real object will be retrieved when it is accessed
            internalAdd(thisHub, obj, true);
            return true;
        }

        if (thisHub.contains(obj)) return false; 
        
        
        OAObjectKey key;
        if (obj instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject)obj);
        else key = OAObjectKeyDelegate.convertToObjectKey(thisHub.getObjectClass(), obj);
        
        if (HubDataDelegate.getObject(thisHub, key) != null) return false;

        if (thisHub.data.sortListener != null) {
            for (int i=0; ; i++) {
                Object cobj = thisHub.elementAt(i);
                if (cobj == null) {
                    pos = i;
                    break;
                }
                if (thisHub.data.sortListener.comparator.compare(obj, cobj) <= 0) {
                    pos = i;
                    break;
                }
            }
        }
    
        if (pos < 0) pos = 0;
        thisHub.elementAt(pos); // make sure object is loaded
    
        int x = thisHub.getSize();
        if (pos > x) pos = x;
        if (!canAdd(thisHub, obj)) return false; 

        
        HubEventDelegate.fireBeforeInsertEvent(thisHub, obj, pos);

        // send message to OAServer
        OAObjectMessage msg = null;
        
        try {
            //  OAClient must send message to OAServer before continuing
            if (thisHub.isOAObject()) {
                msg = HubCSDelegate.insertInHub(thisHub, (OAObject) obj, pos);
                if (HubDataDelegate.getObject(thisHub, key) != null) return false;
            }
            if (!HubDataDelegate._insert(thisHub, key, obj, pos, false)) return false;  // false=dont lock, since this method is locked
        }
        finally {
            // this will "tell" OAClientMessageHandler that it is ok to continue
            HubCSDelegate.messageProcessed(msg); // even if msg is null, since this could be a OAClientThread
        }       
        if (thisHub.isOAObject()) OAObjectHubDelegate.addHub((OAObject)obj,thisHub);
    
        // moved before listeners are notified.  Else listeners could ask for more objects
        HubDetailDelegate.setPropertyToMasterHub(thisHub, obj, thisHub.datam.masterObject);
        
        // if recursive and this is the root hub, then need to set parent to null (since object is now in root, it has no parent)
        if (thisHub.getRootHub() == thisHub) {
            OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.datau.objectInfo, OALinkInfo.ONE);
            if (liRecursive != null) {
                OAObjectReflectDelegate.setProperty((OAObject) obj, liRecursive.getName(), null, null);
            }
        }

        HubEventDelegate.fireAfterInsertEvent(thisHub, obj, pos);
        
        return true;
    }
    

    /**
        Swap the positon of two different objects within the hub.  This will
        call the move method.
        @param pos1 position of object to move from, if there is not an object at this position, then no move is performed.
        @param pos2 position of object to move to, if there is not an object at this position, then no move is performed.
        @see #move
    */
    public static void swap(Hub thisHub, int pos1, int pos2) {
        if (thisHub.datau.sharedHub != null) {
            swap(thisHub.datau.sharedHub, pos1, pos2);
            return;
        }
        // 20110104 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            _swap(thisHub, pos1, pos2);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
    }
    private static void _swap(Hub thisHub, int pos1, int pos2) {
        if (pos1 == pos2) return;
        if (pos1 > pos2) {
            int i = pos2;
            pos2 = pos1;
            pos1 = i;
        }
        Object obj1 = thisHub.elementAt(pos1);
        Object obj2 = thisHub.elementAt(pos2);
    
        if (obj1 == null || obj2 == null) return;
    
        move(thisHub, pos2,pos1);
        move(thisHub, pos1+1,pos2);
    }

    
    public static OAObject[] getAddedObjects(Hub thisHub) {
        return HubDataDelegate.getAddedObjects(thisHub);
    }
    public static OAObject[] getRemovedObjects(Hub thisHub) {
        return HubDataDelegate.getRemovedObjects(thisHub);
    }
    public static boolean isAllowAddRemove(Hub thisHub) {
        if (thisHub == null) return false;
        return thisHub.datau.dupAllowAddRemove;
    }
    
}

