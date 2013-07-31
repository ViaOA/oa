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
package com.viaoa.hub;

import java.util.*;
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.util.OANullObject;


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
            if (thisHub.data.vecAdd != null) thisHub.data.vecAdd.removeAllElements();
    		if (thisHub.data.vecRemove != null) thisHub.data.vecRemove.removeAllElements();
    		thisHub.data.vector.removeAllElements();
    	}
        thisHub.data.changed = false;
		thisHub.data.changeCount++;
	}
	
	protected static void ensureCapacity(Hub thisHub, int size) {
		thisHub.data.vector.ensureCapacity(size);
	}
	protected static void resizeToFit(Hub thisHub) {
		if (thisHub.data.vector == null) return; // could be called during serialization
//LOG.config("resizing, from:"+thisHub.data.vector.capacity()+", to:"+x+", hub:"+thisHub);//qqqqqqqqqqqqqqqqqq                        
		thisHub.data.vector.trimToSize();
	}

	protected static void setChanged(Hub thisHub, boolean b) {
        boolean old = thisHub.data.changed;
        thisHub.data.changed = b;
        if (!b) {
        	synchronized (thisHub.data) {
                if (thisHub.data.vecAdd != null) thisHub.data.vecAdd.removeAllElements();
        		if (thisHub.data.vecRemove != null) thisHub.data.vecRemove.removeAllElements();
        	}
        }
    }
    protected static void copyInto(Hub thisHub, Object anArray[]) {
        synchronized (thisHub.data) {
            thisHub.data.vector.copyInto(anArray);
        }
    }
	
	public static Object[] toArray(Hub thisHub) {
	    thisHub.getSize(); // all before sync, in case it needs to load
	    synchronized (thisHub.data) {
	        Object[] objs = new Object[thisHub.getSize()];
	        thisHub.data.vector.copyInto(objs);
		    return objs;
	    }
	}
    
    public static int getCurrentSize(Hub thisHub) {
        return thisHub.data.vector.size();
    }
	
    /** called by Hub.clone(); */
    public static void _clone(Hub thisHub, Hub newHub) {
    	newHub.data.vector = (Vector) thisHub.data.vector.clone();
    }
    
	// called by HubAddRemoveDelegate
    protected static int _remove(Hub thisHub, Object obj, boolean bDeleting) {
        int pos = 0;
        try {
            OAThreadLocalDelegate.lock(thisHub);
            pos = _remove2(thisHub, obj, bDeleting);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        return pos;
    }
    private static int _remove2(Hub thisHub, Object obj, boolean bDeleting) {
        int pos;
	    Object key = obj;
	    if (obj instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject)obj);
	    synchronized (thisHub.data) {
	        pos = thisHub.getPos(obj);
	        if (pos >= 0) thisHub.data.vector.removeElementAt(pos);
            else {
            	// System.out.println("HubDataDelegate ****************** REMOVE *********** OBJECT NOT FOUND");	  //qqqqqqqqqqqqqqqqqqqqqqqqqqq      
            }
	    }
	    if (pos >= 0) {
	    	if (thisHub.data.bTrackChanges && (obj instanceof OAObject)) {
		        synchronized (thisHub.data) {
		            if (thisHub.data.vecAdd != null && thisHub.data.vecAdd.contains(obj)) {
	            		thisHub.data.vecAdd.removeElement(obj);
		            }
		            else {
		                if (!bDeleting) {
	                    	createVecRemove(thisHub).addElement(obj);
		                }
		            }
		        }
		        thisHub.setChanged( (thisHub.data.vecAdd != null && thisHub.data.vecAdd.size() > 0) || (thisHub.data.vecRemove != null && thisHub.data.vecRemove.size() > 0) );
		    }
		    else {
		    	setChanged(thisHub, true);
		    }
	    }	    
	    return pos;
	}

	
	// called by HubAddRemoveDelegate.internalAdd
	protected static boolean _add(Hub thisHub, OAObjectKey key, Object obj) {
        boolean b = false;
        try {
            OAThreadLocalDelegate.lock(thisHub);
            b = _add2(thisHub, key, obj);
        }
        catch (Exception e) {
            //qqqqqqqqqqqqqqqqqqqqqqqqq TEST ONLY qqqqqqqqqqqqqqq
            int x = 4;
            x++;
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        return b;
    }
    private static boolean _add2(Hub thisHub, OAObjectKey key, Object obj) {
        synchronized (thisHub.data) {
            if (thisHub.contains(obj)) return false;
        	thisHub.data.vector.addElement(obj);
        }
        
        int xx = thisHub.data.vector.size();
        if (xx >= 100 && thisHub.datam.masterObject != null && (xx%100)==0) {
//System.out.println("large Hub with masterObject, Hub="+thisHub);//qqqqqqqqqqqqqq
            LOG.fine("large Hub with masterObject, Hub="+thisHub);//qqqqqqqqqqqqqq
        }
        
        
	    thisHub.data.changeCount++;
	    if (!HubSelectDelegate.isFetching(thisHub)) {
	        if (thisHub.data.bTrackChanges && (obj instanceof OAObject)) {
	            synchronized (thisHub.data) {
	                if (thisHub.data.vecRemove != null && thisHub.data.vecRemove.contains(obj)) {
                		thisHub.data.vecRemove.removeElement(obj);
	                }
	                else {
                    	createVecAdd(thisHub).addElement(obj);
	                }
	            }
	            thisHub.setChanged( (thisHub.data.vecAdd != null && thisHub.data.vecAdd.size() > 0) || (thisHub.data.vecRemove != null && thisHub.data.vecRemove.size() > 0) );
	        }
	        else thisHub.setChanged(true);
	    }
	    return true;
	}

    protected static boolean _insert(Hub thisHub, OAObjectKey key, Object obj, int pos) {
        return _insert(thisHub, key, obj, pos, true);
    }
    protected static boolean _insert(Hub thisHub, OAObjectKey key, Object obj, int pos, boolean bLock) {
        boolean b = false;
        try {
            OAThreadLocalDelegate.lock(thisHub);
            b = _insert2(thisHub, key, obj, pos, bLock);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        return b;
    }
	private static boolean _insert2(Hub thisHub, OAObjectKey key, Object obj, int pos, boolean bLock) {
	    if (bLock) {
            synchronized (thisHub.data) {
                if (thisHub.contains(obj)) return false;
            	thisHub.data.vector.insertElementAt(obj, pos);
            }
	    }
	    else {
            thisHub.data.vector.insertElementAt(obj, pos);
	    }
	    if (thisHub.data.bTrackChanges && (obj instanceof OAObject)) {
	        synchronized (thisHub.data) {
	            if (thisHub.data.vecRemove != null && thisHub.data.vecRemove.contains(obj)) {
            		thisHub.data.vecRemove.removeElement(obj);
	            }
	            else {
                	createVecAdd(thisHub).addElement(obj);
	            }
	        }
	        thisHub.setChanged( (thisHub.data.vecAdd != null && thisHub.data.vecAdd.size() > 0) || (thisHub.data.vecRemove != null && thisHub.data.vecRemove.size() > 0) );
	    }
	    else thisHub.setChanged(true);
		
	    thisHub.data.changeCount++;
	    return true;
	}

	// called by HubAddRemoveDelegate.move
	protected static void _move(Hub thisHub, Object obj, int posFrom, int posTo) {
        try {
            OAThreadLocalDelegate.lock(thisHub);
            thisHub.data.changeCount++;
            synchronized (thisHub.data) {
                thisHub.data.vector.removeElementAt(posFrom);
                thisHub.data.vector.insertElementAt(obj, posTo);
            }
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
	        thisHub.data.vecAdd.add(objx);	        
	    }
	}
	
	private static Vector createVecAdd(Hub thisHub) {
        if (thisHub.data.vecAdd == null) {
	        synchronized (thisHub.data) {
	            if (thisHub.data.vecAdd == null) thisHub.data.vecAdd = new Vector(10,0);
	        }
        }
        return thisHub.data.vecAdd;
	}
	private static Vector createVecRemove(Hub thisHub) {
		if (thisHub.data.vecRemove == null) {
	        synchronized (thisHub.data) {
	            if (thisHub.data.vecRemove == null) thisHub.data.vecRemove = new Vector(10,0);
	        }
		}
        return thisHub.data.vecRemove;
	}
	
	// used to "know" which objects have been added to the Hub.
	public static OAObject[] getAddedObjects(Hub thisHub) {
        Vector v = thisHub.data.vecAdd;
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
        Vector v = thisHub.data.vecRemove;
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
	    
if (thisHub == null || thisHub.data == null || thisHub.data.vector == null) {
    int xx = 4;
    xx++;
    return null;//qqqqqqqqqqqqqqqqqqqqqqqq this is a bug with hub readResolve   20120927 that I'm working on
}
	    
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
	    This will cause this Hub to have the object.
	    <p>
	    Note: if masterHub (or one of its shared) has a linkHub, it will still be updated.
	*/
	public static int getPos(Hub thisHub, Object object, boolean adjustMaster, boolean bUpdateLink) {
	    int pos;
	    if (object == null) return -1;

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
        if (pos < 0) {
            // 2008/04/19 was: if (thisHub.datau.sharedHub != null) {   // this created a problem when using contains(...), that only wants to know if the current hub has an object - not to "adjust it"
            if (thisHub.datau.sharedHub != null && adjustMaster) {
                OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(thisHub.datau.objectInfo, OALinkInfo.ONE);
                if (liRecursive != null) {  // if recursive
                    Object parent = OAObjectReflectDelegate.getProperty((OAObject)object, liRecursive.getName());
                    if (parent == null) {  // must be in root hub
                        Hub h = thisHub.getRootHub();
                        if (h != null && h != thisHub) {
                        	HubShareDelegate.setSharedHub(thisHub, h, false);
                            pos = getPos(h, object, adjustMaster, bUpdateLink);
                        }
                    }
                    else {
                    	OALinkInfo li = OAObjectInfoDelegate.getReverseLinkInfo(liRecursive);
                    	if (li != null) {
	                    	Object val = OAObjectReflectDelegate.getProperty((OAObject)parent, li.getName());
	                    	// 20110110 if there is a linkToHub, then ignore the linkToProp.value (use object instead)
                            HubShareDelegate.setSharedHub(thisHub, (Hub) val, false, object);
	                    	//was:  HubShareDelegate.setSharedHub(thisHub, (Hub) val, false);

                            // 20120229
                            pos = getPos((Hub)val, object, adjustMaster, bUpdateLink);
	                        //was: pos = getPos(thisHub, object, adjustMaster, bUpdateLink);
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
		
	protected static void removeFromAddedList(Hub thisHub, Object obj) {
	    synchronized (thisHub.data) {
	    	Vector v = thisHub.data.vecAdd;
	    	if (v != null) v.remove(obj);
	    }
	}
	public static void removeFromRemovedList(Hub thisHub, Object obj) {
	    synchronized (thisHub.data) {
	    	Vector v = thisHub.data.vecRemove;
	    	if (v != null) v.remove(obj);
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
	public static int getNewListCount(Hub thisHub) {
	    return thisHub.data.newListCount;
	}

}







