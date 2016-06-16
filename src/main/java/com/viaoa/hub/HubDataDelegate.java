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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;

/**
 * Main delegate that works with the HubData class.
 * All methods that have an "_" prefix should not be called directly, as there is
 * a calling method that should be used, that performs additional functionality.
 * If a method does not have the "_" prefix and is accessible, then it is ok
 * to call it, but will most likely have a matching method name in the Hub class.
 * @author vincevia
 */
public class HubDataDelegate {
	
    private static Logger LOG = Logger.getLogger(HubDataDelegate.class.getName());
    
	// used by HubSelectDelegate.select()
	protected static void clearAllAndReset(Hub thisHub) {
    	synchronized (thisHub.data) {
            if (thisHub.data.getVecAdd() != null) thisHub.data.getVecAdd().removeAllElements();
    		if (thisHub.data.getVecRemove() != null) thisHub.data.getVecRemove().removeAllElements();
    		thisHub.data.vector.removeAllElements();
    	
            // 20160407
            if (thisHub.data.hubDatax != null) {
                if (!thisHub.data.hubDatax.isNeeded()) thisHub.data.hubDatax = null;
            }
    	}
        thisHub.data.changed = false;
		thisHub.data.changeCount++;
	}
	
	protected static void ensureCapacity(Hub thisHub, int size) {
		thisHub.data.vector.ensureCapacity(size);
	}
	public static void resizeToFit(Hub thisHub) {
		if (thisHub.data.vector == null) return; // could be called during serialization
//LOG.config("resizing, from:"+thisHub.data.vector.capacity()+", to:"+x+", hub:"+thisHub);//qqqqqqqqqqqqqqqqqq                        
		thisHub.data.vector.trimToSize();
	}

	protected static void setChanged(Hub thisHub, boolean b) {
	    if (thisHub == null) return;
        boolean old = thisHub.data.changed;
        if (b == old) return;
        thisHub.data.changed = b;
        if (b != old) thisHub.data.changeCount++;
        if (!b) {
            clearHubChanges(thisHub);
        }
    }
	
    // 20150420
	public static void clearHubChanges(Hub thisHub) {
	    if (thisHub == null) return;
        boolean bSendEvent = false;
        synchronized (thisHub.data) {
            Vector v = thisHub.data.getVecAdd(); 
            if (v != null) {
                bSendEvent = v.size() > 0;
                v.removeAllElements();
            }
            v = thisHub.data.getVecRemove(); 
            if (v != null) {
                bSendEvent = bSendEvent || v.size() > 0;
                v.removeAllElements();
            }
            // 20160407
            if (thisHub.data.hubDatax != null) {
                if (!thisHub.data.hubDatax.isNeeded()) thisHub.data.hubDatax = null;
            }
        }
        if (bSendEvent) {
            HubCSDelegate.clearHubChanges(thisHub);
        }
	}
	
	
    protected static void copyInto(Hub thisHub, Object anArray[]) {
        synchronized (thisHub.data) {
            thisHub.data.vector.copyInto(anArray);
        }
    }
	
	public static Object[] toArray(Hub thisHub) {
	    thisHub.getSize(); // call before sync, in case it needs to load
        Object[] objs;
        for (int i=0;;i++) {
            synchronized (thisHub.data) {
                objs = new Object[thisHub.getSize()];
                try {
                    thisHub.data.vector.copyInto(objs);
                    break;
                }
                catch (Exception e) {
                    // if exception, then try again
                }
            }
        }
	    return objs;
	}
    
    public static int getCurrentSize(Hub thisHub) {
        return thisHub.data.vector.size();
    }
	
    /** called by Hub.clone(); */
    public static void _clone(Hub thisHub, Hub newHub) {
    	newHub.data.vector = (Vector) thisHub.data.vector.clone();
    }
    
	// called by HubAddRemoveDelegate
    protected static int _remove(Hub thisHub, Object obj, boolean bDeleting, boolean bIsRemovingAll) {
        int pos = 0;
        try {
            OAThreadLocalDelegate.lock(thisHub);
            pos = _remove2(thisHub, obj, bDeleting, bIsRemovingAll);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        if (!bIsRemovingAll) {
            OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        }
        return pos;
    }
    
    private static int _remove2(Hub thisHub, Object obj, boolean bDeleting, boolean bIsRemovingAll) {
        int pos;
        if (bIsRemovingAll) {
            pos = -1;
            /*
            if (thisHub.data.vector.remove(obj)) pos = 0;
            else pos = -1;
            */
        }
        else {
	        pos = thisHub.getPos(obj);
	        if (pos >= 0) {
	            thisHub.data.vector.removeElementAt(pos);
	        }
        }

	    if (pos >= 0) {
	    	if (!thisHub.isLoading() && (thisHub.datam.getTrackChanges() || thisHub.data.getTrackChanges()) && (obj instanceof OAObject)) {
	            if (thisHub.data.getVecAdd() != null && thisHub.data.getVecAdd().removeElement(obj)) {
	                // no-op
	            }
	            else {
	                if (!bDeleting) {
                    	Vector vec = createVecRemove(thisHub);
                    	if (!vec.contains(obj)) vec.addElement(obj);
	                }
	            }
		        thisHub.setChanged( (thisHub.data.getVecAdd() != null && thisHub.data.getVecAdd().size() > 0) || (thisHub.data.getVecRemove() != null && thisHub.data.getVecRemove().size() > 0) );
		    }
		    else {
		    	setChanged(thisHub, true);
		    }
	    }	    
	    return pos;
	}

	
	// called by HubAddRemoveDelegate.internalAdd
    protected static boolean _add(Hub thisHub, Object obj, boolean bIsLoading, boolean bHasLock) {
        boolean b = false;
        try {
            if (!bHasLock && !bIsLoading) OAThreadLocalDelegate.lock(thisHub);
            b = _add2(thisHub, obj, bIsLoading);
        }
        finally {
            if (!bHasLock && !bIsLoading) OAThreadLocalDelegate.unlock(thisHub);
        }
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        return b;
    }
    private static boolean _add2(Hub thisHub, Object obj, boolean bIsLoading) {
        if (!bIsLoading && thisHub.contains(obj)) return false;
    	thisHub.data.vector.addElement(obj);
        
        int xx = thisHub.data.vector.size();
        if (xx > 499 && thisHub.datam.masterObject != null && (xx%100)==0) {
            LOG.fine("large Hub with masterObject, Hub="+thisHub);//qqqqqqqqqqqqqq
        }
        
        if ((thisHub.datam.getTrackChanges() || thisHub.data.getTrackChanges()) && (obj instanceof OAObject)) {
            if (thisHub.data.getVecRemove() != null && thisHub.data.getVecRemove().contains(obj)) {
        		thisHub.data.getVecRemove().removeElement(obj);
            }
            else {
                if (!bIsLoading) {
                    createVecAdd(thisHub).addElement(obj);
                }
            }
            if (!bIsLoading) thisHub.setChanged( (thisHub.data.getVecAdd() != null && thisHub.data.getVecAdd().size() > 0) || (thisHub.data.getVecRemove() != null && thisHub.data.getVecRemove().size() > 0) );
        }
        else if (!bIsLoading) thisHub.setChanged(true);

        thisHub.data.changeCount++;
	    return true;
	}


    protected static boolean _insert(Hub thisHub, Object obj, int pos, boolean bIsLoading, boolean bIsLocked) {
        boolean b = false;
        try {
            if (!bIsLocked && !bIsLoading) OAThreadLocalDelegate.lock(thisHub);
            //was b = _insert2(thisHub, key, obj, pos, bLock);
            b = _insert2(thisHub, obj, pos, bIsLoading);
        }
        finally {
            if (!bIsLocked && !bIsLoading) OAThreadLocalDelegate.unlock(thisHub);
        }
        
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        return b;
    }
    //was: private static boolean _insert2(Hub thisHub, OAObjectKey key, Object obj, int pos, boolean bLock) {
	private static boolean _insert2(Hub thisHub, Object obj, int pos, boolean bIsLoading) {
        if (!bIsLoading && thisHub.contains(obj)) return false;
    	thisHub.data.vector.insertElementAt(obj, pos);

    	if ((thisHub.datam.getTrackChanges() || thisHub.data.getTrackChanges()) && (obj instanceof OAObject)) {
            if (thisHub.data.getVecRemove() != null && thisHub.data.getVecRemove().contains(obj)) {
        		thisHub.data.getVecRemove().removeElement(obj);
            }
            else {
                if (!bIsLoading) {
                    createVecAdd(thisHub).addElement(obj);
                }
            }
            if (!bIsLoading) thisHub.setChanged( (thisHub.data.getVecAdd() != null && thisHub.data.getVecAdd().size() > 0) || (thisHub.data.getVecRemove() != null && thisHub.data.getVecRemove().size() > 0) );
	    }
	    else if (!bIsLoading) thisHub.setChanged(true);
		
	    thisHub.data.changeCount++;
	    return true;
	}

	// called by HubAddRemoveDelegate.move
	protected static void _move(Hub thisHub, Object obj, int posFrom, int posTo) {
        try {
            OAThreadLocalDelegate.lock(thisHub);
            thisHub.data.changeCount++;
            thisHub.data.vector.removeElementAt(posFrom);
            thisHub.data.vector.insertElementAt(obj, posTo);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
	}
	
	public static void addAllToAddVector(Hub thisHub) {
	    if (thisHub == null) return;
        createVecAdd(thisHub);
	    for (Object objx :  thisHub) {
	        thisHub.data.getVecAdd().add(objx);	        
	    }
	}
	
	protected static Vector createVecAdd(Hub thisHub) {
        if (thisHub.data.getVecAdd() == null) {
	        synchronized (thisHub.data) {
	            if (thisHub.data.getVecAdd() == null) thisHub.data.setVecAdd(new Vector(10, 10));
	        }
        }
        return thisHub.data.getVecAdd();
	}
	protected static Vector createVecRemove(Hub thisHub) {
		if (thisHub.data.getVecRemove() == null) {
	        synchronized (thisHub.data) {
	            if (thisHub.data.getVecRemove() == null) thisHub.data.setVecRemove(new Vector(10,10));
	        }
		}
        return thisHub.data.getVecRemove();
	}
	
	// used to "know" which objects have been added to the Hub.
	public static OAObject[] getAddedObjects(Hub thisHub) {
        Vector v = thisHub.data.getVecAdd();
        if (v == null || v.size() == 0) return null;
        synchronized (thisHub.data) {
     		OAObject[] objs;
			int x = (v == null) ? 0 : v.size();
			objs = new OAObject[x];
			if (x > 0) v.copyInto(objs);
			return objs;
        }
	}
	// used to "know" which objects have been removed to the Hub.
	public static OAObject[] getRemovedObjects(Hub thisHub) {
        Vector v = thisHub.data.getVecRemove();
        if (v == null || v.size() == 0) return null;
        synchronized (thisHub.data) {
			OAObject[] objs;
			int x = (v == null) ? 0 : v.size();
			objs = new OAObject[x];
			if (x > 0) v.copyInto(objs);
			return objs;
        }
	}

	public static boolean getChanged(Hub thisHub) {
	    return (thisHub.data.changed);
	}
	
	public static Object getObject(Hub thisHub, Object key) {
		if (key == null) return null;
	    if (!(key instanceof OAObjectKey)) {
	    	if (key instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject) key);
	    	else key = OAObjectKeyDelegate.convertToObjectKey(thisHub.getObjectClass(), key);
	    }
		for (int i=0; ; i++) {
			Object obj = getObjectAt(thisHub, i);
			if (obj == null) break;
			if (obj == key) return obj;
			if (obj instanceof OAObject) {
				Object k = OAObjectKeyDelegate.getKey((OAObject) obj);
				if (k.equals(key)) return obj;
			}
		}
		return null;
	}
	
	protected static Object getObjectAt(Hub thisHub, int pos) {
	    Object ho;
	    if (pos < 0) return null;
	    
	    int size = thisHub.data.vector.size();
	    if (pos < size) {
	        Object obj = null;
	        try {
	        	obj = thisHub.data.vector.elementAt(pos);
	        }
	        catch (Exception e) {
	        	obj = null;  // hub could have changed, and pos is not valid anymore
	        }
	        if (obj instanceof OAObjectKey && thisHub.isOAObject()) {
            	obj = OAObjectReflectDelegate.getObject(thisHub.getObjectClass(), obj);
                if (obj != null) {
	                OAObjectHubDelegate.addHub((OAObject)obj, thisHub);
	                thisHub.data.vector.setElementAt(obj, pos);
	                if (thisHub.datam.masterObject != null) {
		                // need to set property to MasterHub
	                	HubDetailDelegate.setPropertyToMasterHub(thisHub, obj, thisHub.datam.masterObject);
	                }
                }
	        }
	        if (obj != null) return obj;
	    }
	
	    if (!HubSelectDelegate.isMoreData(thisHub)) {
	        return null;
	    }
	
	    // fetch more records from data source
	    for ( ; pos >= thisHub.data.vector.size() && HubSelectDelegate.isMoreData(thisHub) ; ) {
	    	HubSelectDelegate.fetchMore(thisHub);
	    }
	    ho = HubDataDelegate.getObjectAt(thisHub, pos);
	    return ho;
	}
	
	/*
	    Find the position for an object within the Hub.  If the object is not in the Hub and there
	    is a Master Hub from getDetailHub(), then the Master Hub will updated to the master object.
	    This will also check and adjust for recursive hubs.
	    <p>
	    Note: if masterHub (or one of its shared) has a linkHub, it will still be updated.
	*/
	public static int getPos(final Hub thisHub, Object object, final boolean adjustMaster, final boolean bUpdateLink) {
	    int pos;
	    if (object == null || thisHub == null) return -1;

	    if (!(object instanceof OAObject)) {
	        if (OAObject.class.isAssignableFrom(object.getClass())) {  // could be hub of strings
	            object = HubDelegate.getRealObject(thisHub, object);
	        }
	    }
	    pos = -1;
	    if (object != null) {
	        for ( ;; ) {
	            pos = thisHub.data.vector.indexOf(object);
	            if (pos >= 0) return pos;
	            if (!HubSelectDelegate.isMoreData(thisHub)) break;
                HubSelectDelegate.fetchMore(thisHub);
	        }
	    }

        if (pos < 0 && adjustMaster && (thisHub.datau.getSharedHub() != null || thisHub.datam.masterHub != null)) {
            OALinkInfo liRecursiveOne = OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.data.getObjectInfo(), OALinkInfo.ONE);

            // need to verify that this hub is recursive with masterObject
            if (liRecursiveOne != null) {  
                OALinkInfo li = thisHub.datam.liDetailToMaster;
                if (li != null) {
                    li = OAObjectInfoDelegate.getReverseLinkInfo(li);
                    if (li == null || !li.getRecursive()) {
                        liRecursiveOne = null;
                    }
                }
            }

            boolean bUseMaster = false;
            if (liRecursiveOne != null) {  // if recursive
                Object parent = OAObjectReflectDelegate.getProperty((OAObject)object, liRecursiveOne.getName());
                if (parent == null) {  // must be in root hub
                    Hub h = thisHub.getRootHub();  // could be owner of hub
                    if (h != null && h != thisHub && thisHub.datau.getSharedHub() != h) {
                        HubShareDelegate.setSharedHub(thisHub, h, false);
                        pos = getPos(h, object, adjustMaster, bUpdateLink);
                    }
                    if (pos < 0) {
                        bUseMaster = true;  // adjust master/owner for this recursive hub
                    }
                }
                else {
                	OALinkInfo liMany = OAObjectInfoDelegate.getReverseLinkInfo(liRecursiveOne);
                	if (liMany != null) {
                        if (hashRecursiveHubDetail.get(thisHub) == null) {
                            HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
                            if (dm.liDetailToMaster != null) hashRecursiveHubDetail.put(thisHub, dm.liDetailToMaster);
                        }
                    	Object val = OAObjectReflectDelegate.getProperty((OAObject)parent, liMany.getName());
                    	
                    	HubShareDelegate.setSharedHub(thisHub, (Hub) val, false, object);
                        pos = getPos((Hub)val, object, adjustMaster, bUpdateLink);
                	}
                }
            }

            if (bUseMaster) {
                if (thisHub.datam.masterHub != null && thisHub.datam.liDetailToMaster != null) {  
                    // only do this if a masterHub, since a hub that has a masterObject (w/o hub) should not do this adjustment
                    Object parent = OAObjectReflectDelegate.getProperty((OAObject)object, thisHub.datam.liDetailToMaster.getName());
                    if (parent != null) {
                        OALinkInfo li = OAObjectInfoDelegate.getReverseLinkInfo(thisHub.datam.liDetailToMaster);
                        if (li != null) {
                            if (hashRecursiveHubDetail.get(thisHub) == null) {
                                HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
                                if (dm.liDetailToMaster != null) hashRecursiveHubDetail.put(thisHub, dm.liDetailToMaster);
                            }
                            Object val = OAObjectReflectDelegate.getProperty((OAObject)parent, li.getName());
                            HubShareDelegate.setSharedHub(thisHub, (Hub) val, false, object);
                            pos = getPos((Hub)val, object, adjustMaster, bUpdateLink);
                        }
                    }
                }
                else {
                    // see if it was a master/detail that was reassigned (shared) to a child hub that is recursive
                    OALinkInfo li = hashRecursiveHubDetail.get(thisHub);
                    if (li != null) {
                        Object parent = OAObjectReflectDelegate.getProperty((OAObject)object, li.getName());
                        if (parent != null) {
                            Object val = OAObjectReflectDelegate.getProperty((OAObject)parent, li.getReverseName());
                            HubShareDelegate.setSharedHub(thisHub, (Hub) val, false, object);
                            pos = getPos((Hub)val, object, adjustMaster, bUpdateLink);
                        }
                    }
                }
            }
        }
        

        if (pos < 0 && adjustMaster) {
            if (HubDetailDelegate.setMasterHubActiveObject(thisHub, object, bUpdateLink)) {
                pos = getPos(thisHub, object, false, false);
            }
        }
	    return pos;
	}
    /**
     * Used by HubDataDelegate.getPos(..) when finding the object for recursive links
     */
    static private final ConcurrentHashMap<Hub, OALinkInfo> hashRecursiveHubDetail = new ConcurrentHashMap<Hub, OALinkInfo>(11, 0.75F);
    
	
	
	protected static void removeFromAddedList(Hub thisHub, Object obj) {
	    synchronized (thisHub.data) {
            if (thisHub.data.hubDatax == null) return;
	    	Vector v = thisHub.data.getVecAdd();
	    	if (v != null) v.remove(obj);
            // 20160407
            if (thisHub.data.hubDatax != null) {
                if (!thisHub.data.hubDatax.isNeeded()) thisHub.data.hubDatax = null;
            }
	    }
	}
	public static void removeFromRemovedList(Hub thisHub, Object obj) {
        if (thisHub.data.hubDatax == null) return;
	    synchronized (thisHub.data) {
	    	Vector v = thisHub.data.getVecRemove();
	    	if (v != null) v.remove(obj);
            // 20160407
            if (thisHub.data.hubDatax != null) {
                if (!thisHub.data.hubDatax.isNeeded()) thisHub.data.hubDatax = null;
            }
	    }
	}

	
    /**
		Counter that is incremented on: add(), insert(), remove(), setting shared hub,
	    remove(), move(), sort(), select()
	    This is used by html/jsp components so that they "know" when/if Hub has changed,
	    which will cause them to be refreshed.
	*/
	public static int getChangeCount(Hub thisHub) {
	    return thisHub.data.changeCount;
	}
	
	protected static void incChangeCount(Hub thisHub) {
		thisHub.data.changeCount++;
	}

    /**
	    Counter that is incremented when a new list of objects is loaded: select, setSharedHub, and when
	    detail hubs list is changed to match the master hub's activeObject
	    This is used by html/jsp components so that they "know" when/if Hub has changed,
	    which will cause them to be refreshed.
	*/
/*	
	public static int getNewListCount(Hub thisHub) {
	    return thisHub.data.getNewListCount();
	}
*/
    public static boolean contains(Hub hub, Object obj) {
        if (!(obj instanceof OAObject)) {
            if (!hub.data.isOAObjectFlag()) {
                return hub.data.vector.contains(obj);
            }
            obj = OAObjectCacheDelegate.get(hub.getObjectClass(), obj);
            if (obj == null) return false;
        }        
        
        if (hub.data.vector.size() < 20 || !hub.data.isOAObjectFlag()) {
            return hub.data.vector.contains(obj);
        }
        return OAObjectHubDelegate.isAlreadyInHub((OAObject) obj, hub);
    }
    public static boolean containsDirect(Hub hub, Object obj) {
        return hub.data.vector.contains(obj);
    }
}
