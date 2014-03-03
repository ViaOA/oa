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

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;

/**
 * Delegate used to register Hub listeners, get Listeners and to send Events to Hub listeners. 
 */
public class HubEventDelegate {

    // 20120827 might be used later, if we need to have hub changes notify masterobject
    protected static void fireMasterObjectChangeEvent(Hub thisHub, boolean bRefreshFlag) {
        // OAObjectHubDelegate.fireMasterObjectHubChangeEvent(thisHub, bRefreshFlag);
    }
    
	public static void fireBeforeRemoveEvent(Hub thisHub, Object obj, int pos) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub,obj, pos);
	        try {
	            OAThreadLocalDelegate.setSendingEvent(true);
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].beforeRemove(hubEvent);
    	        }
	        }
	        finally {
	            OAThreadLocalDelegate.setSendingEvent(false);
	        }
	    }
	}
	
	public static void fireAfterRemoveEvent(Hub thisHub, Object obj, int pos) {
	    final HubListener[] hl = getAllListeners(thisHub);
	    final int x = hl.length;
	    if (x > 0) {
	        final HubEvent hubEvent = new HubEvent(thisHub,obj,pos);
            if (OARemoteThreadDelegate.isRemoteThread() && !OAObjectCSDelegate.isServer()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OAThreadLocalDelegate.setSendingEvent(true);
                            for (int i=0; i<x; i++) { 
                                hl[i].afterRemove(hubEvent);
                            }
                        }
                        finally {
                            OAThreadLocalDelegate.setSendingEvent(false);
                        }
                    }
                };
                OAThreadLocalDelegate.addRunnable(r);
            }
            else {
    	        try {
        	        OAThreadLocalDelegate.setSendingEvent(true);
        	        for (int i=0; i<x; i++) { 
        	        	hl[i].afterRemove(hubEvent);
        	        }
    	        }
    	        finally {
    	            OAThreadLocalDelegate.setSendingEvent(false);
    	        }
            }
	    }
	    OAObjectCacheDelegate.fireAfterRemoveEvent(thisHub, obj, pos);
        //fireMasterObjectChangeEvent(thisHub, false);
	}

	public static void fireBeforeRemoveAllEvent(Hub thisHub) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub);
	        for (int i=0; i<x; i++) { 
	        	hl[i].beforeRemoveAll(hubEvent);
	        }
	    }
	}
	public static void fireAfterRemoveAllEvent(Hub thisHub) {
	    final HubListener[] hl = getAllListeners(thisHub);
	    final int x = hl.length;
	    if (x > 0) {
            final HubEvent hubEvent = new HubEvent(thisHub);
            if (OARemoteThreadDelegate.isRemoteThread() && !OAObjectCSDelegate.isServer()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i<x; i++) { 
                            hl[i].afterRemoveAll(hubEvent);
                        }
                    }
                };
                OAThreadLocalDelegate.addRunnable(r);
            }
            else {
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].afterRemoveAll(hubEvent);
    	        }
            }
	    }
        //fireMasterObjectChangeEvent(thisHub, true);
	}
	public static void fireBeforeAddEvent(Hub thisHub, Object obj, int pos) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub,obj,pos);
	        try {
	            OAThreadLocalDelegate.setSendingEvent(true);
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].beforeAdd(hubEvent);
    	        }
	        }
	        finally {
	            OAThreadLocalDelegate.setSendingEvent(false);
	        }
	    }
	}
	public static void fireAfterAddEvent(Hub thisHub, Object obj, int pos) {
	    final HubListener[] hl = getAllListeners(thisHub);
	    final int x = hl.length;
	    if (x > 0) {
            final HubEvent hubEvent = new HubEvent(thisHub,obj,pos);
            if (OARemoteThreadDelegate.isRemoteThread() && !OAObjectCSDelegate.isServer()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OAThreadLocalDelegate.setSendingEvent(true);
                            for (int i=0; i<x; i++) { 
                                hl[i].afterAdd(hubEvent);
                            }
                        }
                        finally {
                            OAThreadLocalDelegate.setSendingEvent(false);
                        }
                    }
                };
                OAThreadLocalDelegate.addRunnable(r);
            }
            else {
    	        try {
        	        OAThreadLocalDelegate.setSendingEvent(true);
        	        for (int i=0; i<x; i++) { 
        	        	hl[i].afterAdd(hubEvent);
        	        }
    	        }
    	        finally {
    	            OAThreadLocalDelegate.setSendingEvent(false);
    	        }
            }
	    }
	    OAObjectCacheDelegate.fireAfterAddEvent(thisHub, obj, pos);
        //fireMasterObjectChangeEvent(thisHub, false);
	}
	public static void fireBeforeInsertEvent(Hub thisHub, Object obj, int pos) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        try {
	            OAThreadLocalDelegate.setSendingEvent(true);
    	        HubEvent hubEvent = new HubEvent(thisHub, obj, pos);
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].beforeInsert(hubEvent);
    	        }
	        }
	        finally {
	            OAThreadLocalDelegate.setSendingEvent(false);
	        }
	    }
	}
	public static void fireAfterInsertEvent(Hub thisHub, Object obj, int pos) {
	    final HubListener[] hl = getAllListeners(thisHub);
	    final int x = hl.length;
	    if (x > 0) {
            final HubEvent hubEvent = new HubEvent(thisHub, obj, pos);
            if (OARemoteThreadDelegate.isRemoteThread() && !OAObjectCSDelegate.isServer()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OAThreadLocalDelegate.setSendingEvent(true);
                            for (int i=0; i<x; i++) { 
                                hl[i].afterInsert(hubEvent);
                            }
                        }
                        finally {
                            OAThreadLocalDelegate.setSendingEvent(false);
                        }
                    }
                };
                OAThreadLocalDelegate.addRunnable(r);
            }
            else {
                try {
                    OAThreadLocalDelegate.setSendingEvent(true);
                    for (int i=0; i<x; i++) { 
                        hl[i].afterInsert(hubEvent);
                    }
                }
                finally {
                    OAThreadLocalDelegate.setSendingEvent(false);
                }
            }
	    }
	    OAObjectCacheDelegate.fireAfterInsertEvent(thisHub, obj, pos);
        //fireMasterObjectChangeEvent(thisHub, false);
	}
	public static void fireAfterChangeActiveObjectEvent(Hub thisHub, Object obj, int pos, boolean bAllShared) {
	    HubListener[] hl = getAllListeners(thisHub, bAllShared?1:3);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub, obj, pos);
	        for (int i=0; i<x; i++) { 
	        	hl[i].afterChangeActiveObject(hubEvent);
	        }
	    }
	}
	public static void fireBeforeSelectEvent(Hub thisHub) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub);
	        for (int i=0; i<x; i++) { 
	        	hl[i].beforeSelect(hubEvent);
	        }
	    }
	}
	public static void fireAfterSortEvent(Hub thisHub) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub);
	        for (int i=0; i<x; i++) { 
	        	hl[i].afterSort(hubEvent);
	        }
	    }
        //fireMasterObjectChangeEvent(thisHub, false);
	}
	public static void fireBeforeDeleteEvent(Hub thisHub, Object obj) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub, obj);
	        for (int i=0; i<x; i++) { 
	        	hl[i].beforeDelete(hubEvent);
	        }
	    }
	}
	public static void fireAfterDeleteEvent(Hub thisHub, Object obj) {
	    final HubListener[] hl = getAllListeners(thisHub);
	    final int x = hl.length;
	    if (x > 0) {
	        final HubEvent hubEvent = new HubEvent(thisHub, obj);
	        
            if (OARemoteThreadDelegate.isRemoteThread() && !OAObjectCSDelegate.isServer()) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        for (int i=0; i<x; i++) { 
                            hl[i].afterDelete(hubEvent);
                        }
                    }
                };
                OAThreadLocalDelegate.addRunnable(r);
            }
            else {
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].afterDelete(hubEvent);
    	        }
            }
	    }
        //fireMasterObjectChangeEvent(thisHub, false);
	}
    public static void fireBeforeSaveEvent(Hub thisHub, OAObject obj) {
        HubListener[] hl = getAllListeners(thisHub);
        int x = hl.length;
        if (x > 0) {
            HubEvent hubEvent = new HubEvent(thisHub, obj);
            for (int i=0; i<x; i++) { 
                hl[i].beforeSave(hubEvent);
            }
        }
    }
	public static void fireAfterSaveEvent(Hub thisHub, OAObject obj) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub, obj);
	        for (int i=0; i<x; i++) { 
	        	hl[i].afterSave(hubEvent);
	        }
	    }
	}
	public static void fireBeforeMoveEvent(Hub thisHub, int fromPos, int toPos) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub, fromPos, toPos);
	        try { 
	            OAThreadLocalDelegate.setSendingEvent(true);
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].beforeMove(hubEvent);
    	        }
	        }
	        finally {
	            OAThreadLocalDelegate.setSendingEvent(false);
	        }
	    }
	}
	public static void fireAfterMoveEvent(Hub thisHub, int fromPos, int toPos) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub, fromPos, toPos);
	        try {
	            OAThreadLocalDelegate.setSendingEvent(true);
    	        for (int i=0; i<x; i++) { 
    	        	hl[i].afterMove(hubEvent);
    	        }
	        }
	        finally {
	            OAThreadLocalDelegate.setSendingEvent(false);
	        }
	    }
        //fireMasterObjectChangeEvent(thisHub, false);
	}
	
	/**
	    Used by OAObjects to notify all listeners of a property change.
	    If the property involves a reference to another object, then other objects and Hubs
	    will automaticially be updated.
	    <p>
	    Example:<br>
	    If the Department is changed for an Employee, then the Employee will be removed
	    from the previous Department's Hub of Employees and moved to the new Department's
	    Hub of Employees.
	    <p>
	    If this Hub is linked to a property in another Hub and that property is changed, this
	    Hub will changed it's active object to match the same value as the new property value.
	    @param propertyName name of property that changed. This is case insensitive
	 */
	public static void fireCalcPropertyChange(Hub thisHub, final Object object, final String propertyName) {
	    // 20120104
	    if (OAThreadLocalDelegate.hasSentCalcPropertyChange(object, propertyName)) return;
	    
	    HubListener[] hl = HubEventDelegate.getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub,object,propertyName,null,null);
	        for (int i=0; i<x; i++) {
	            hl[i].afterPropertyChange(hubEvent);
	        }
	        x += 0;//qqqq debug here
	    }
	}

	/**
	    Called by OAObject and Hub, used to notify all listeners of a property change.
	    @param object OAObject that was changed
	    @param propertyName name of property that changed. This is case insensitive
	    @param oldValue previous value of property
	    @param newValue new value of property
	    @see #fireCalcPropertyChange(Object,String)
	*/
	public static void fireBeforePropertyChange(Hub thisHub, OAObject oaObj, String propertyName, Object oldValue, Object newValue) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    int i;
	    if (x > 0) {
	        HubEvent hubEvent = new HubEvent(thisHub,oaObj,propertyName,oldValue,newValue);
	        try {
	            OAThreadLocalDelegate.setSendingEvent(true);
    	        for (i=0; i<x; i++) {
    	            hl[i].beforePropertyChange(hubEvent);
    	        }
	        }
	        finally {
	            OAThreadLocalDelegate.setSendingEvent(false);
	        }
	    }
	}
	
	/**
	    Called by OAObject and Hub, used to notify all listeners of a property change.
	    @param object OAObject that was changed
	    @param propertyName name of property that changed. This is case insensitive
	    @param oldValue previous value of property
	    @param newValue new value of property
	    @see #fireCalcPropertyChange(Object,String)
	*/
	public static void fireAfterPropertyChange(Hub thisHub, OAObject oaObj, String propertyName, Object oldValue, Object newValue, OALinkInfo linkInfo ) {
		// 2007/01/03 need to call propertyChangeDupChain() first, since propertyChange
		//            could need to change a detail hub(s), before a HubLinkEventListener is called, which
		//            could have needed the detail hubs to be changed.
	    
	    if (linkInfo != null) {
            propertyChangeUpdateDetailHubs(thisHub, oaObj, propertyName);
	    }

	    if (thisHub.data.uniqueProperty != null && newValue != null && thisHub.data.uniqueProperty.equalsIgnoreCase(propertyName)) {
	        if (!HubDelegate.verifyUniqueProperty(thisHub, oaObj)) {
	        	throw new RuntimeException("Property "+thisHub.data.uniqueProperty+" already exists");
	        }
	    }
	
	    final HubListener[] hl = getAllListeners(thisHub);
	    final int x = hl.length;
	    if (x > 0) {
	        final HubEvent hubEvent = new HubEvent(thisHub,oaObj,propertyName,oldValue,newValue);
            if (OARemoteThreadDelegate.isRemoteThread() && !OAObjectCSDelegate.isServer()) {
	            Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OAThreadLocalDelegate.setSendingEvent(true);
                            for (int i=0; i<x; i++) {
                                hl[i].afterPropertyChange(hubEvent);
                            }
                        }
                        finally {
                            OAThreadLocalDelegate.setSendingEvent(false);
                        }
                    }
                };
                OAThreadLocalDelegate.addRunnable(r);
	        }
	        else {
    	        try {
    	            OAThreadLocalDelegate.setSendingEvent(true);
        	        for (int i=0; i<x; i++) {
        	            hl[i].afterPropertyChange(hubEvent);
        	        }
    	        }
    	        finally {
    	            OAThreadLocalDelegate.setSendingEvent(false);
    	        }
	        }
	    }
	}
	
	/**
	    If property change affects the property used for a detail Hub, then update detail Hub.
	*/
	private static void propertyChangeUpdateDetailHubs(Hub thisHub, OAObject object, String propertyName) {
	    int i,x;
	
	    if (object == thisHub.dataa.activeObject) {
	        x = thisHub.datau.vecHubDetail == null ? 0 : thisHub.datau.vecHubDetail.size();
	        for (i=0; i<x; i++) {
	            HubDetail detail = (HubDetail)(thisHub.datau.vecHubDetail.elementAt(i));
	
	            Hub dHub = detail.masterHub;
	            if (dHub != null && detail.liMasterToDetail != null && detail.liMasterToDetail.getName().equalsIgnoreCase(propertyName)) {
	                HubDetailDelegate.updateDetail(thisHub, detail,dHub,false); // ex: from activeObject.setDept(dept), dont updateLinkProperty
	            }
	        }
	    }

        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(thisHub);
        for (i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            propertyChangeUpdateDetailHubs(h2, object,propertyName);
        }
	}
	
	/**
	    Used to notify listeners that a new collection has been established.
	    Called by select() and when a detail Hub's source of data is changed.
	    @see #updateDetail
	    @see #select
	 */
	public static void fireOnNewListEvent(Hub thisHub, boolean bAll) {
	    HubListener[] hl = getAllListeners(thisHub, (bAll?0:2) );
	    int x = hl.length;
	    if (x > 0) {
		    HubEvent hubEvent = new HubEvent(thisHub,null);
	        for (int i=0; i<x; i++) hl[i].onNewList(hubEvent);
	    }
	    thisHub.data.newListCount++;
	}

	public static void fireAfterFetchMoreEvent(Hub thisHub) {
	    HubListener[] hl = getAllListeners(thisHub);
	    int x = hl.length;
	    if (x > 0) {
		    HubEvent hubEvent = new HubEvent(thisHub,null);
	        for (int i=0; i<x; i++) hl[i].afterFetchMore(hubEvent);
	    }
	}

	private static HubListenerTree getHubListenerTree(Hub thisHub) {
	    if (thisHub == null) return null;
	    if (thisHub.datau.listenerTree == null) {
            synchronized (thisHub.datau) {
                if (thisHub.datau.listenerTree == null) {
                    thisHub.datau.listenerTree = new HubListenerTree(thisHub);
                }
            }
        }
	    return thisHub.datau.listenerTree;
	}
	
    /**
        Add a Listener to this hub specifying a specific property name.
        If property is a calculated property, then the Hub will automatically
        set up internal listeners to know when the calculated property changes.
        @param listener HubListener object
        @param property name to listen for
        @see HubEvent
    */
    public static void addHubListener(Hub thisHub, HubListener hl, String property, String[] dependentPropertyPaths) {
        if (property != null && property.indexOf('.') >= 0) {
            throw new RuntimeException("dont use a property path for listener, use addHubListener(h,hl,propertyName, String[]) instead");
        }
        getHubListenerTree(thisHub).addListener(hl, property, dependentPropertyPaths);
    }
    public static void addHubListener(Hub thisHub, HubListener hl, String property) {
        getHubListenerTree(thisHub).addListener(hl, property);
	}
	
	// this is taken from HubDataUnique
	/**
	    Add a new Hub Listener, that receives all Hub and OAObject events.
	    @param bFront if true, then this listener is added to beginning of list, so that
	    it is called first.
	    @see #removeListener
	    @see HubListener
	    @see HubEvent
	    @see #addHubListener(Hub, HubListener, String)
	*/
	public static void addHubListener(Hub thisHub, HubListener hl) {
        getHubListenerTree(thisHub).addListener(hl);
    }
	
	public static int TotalHubListeners; 	
	/**
	    Remove HubListener from list.
	    @see #addListener
	*/
	protected static void removeHubListener(Hub thisHub, HubListener l) {
	    if (thisHub.datau.listenerTree == null) return;
	    thisHub.datau.listenerTree.removeListener(thisHub, l);
	}
	
	/**
	    Returns list of registered listeners.
	    @see #addListener
	*/
	protected static HubListener[] getHubListeners(Hub thisHub) {
	    if (thisHub.datau.listenerTree == null) return new HubListener[0];
	    HubListener[] hl = thisHub.datau.listenerTree.getHubListeners();
	    return hl;
	}

    /**
	    Returns a count of all of the listeners for this Hub and all of Hubs that are shared with it.
	    @see #addListener
	*/
	public static int getListenerCount(Hub thisHub) {
	    return getAllListeners(thisHub).length;
	}
	
	/**
	    Returns an array of HubListeners for all of the listeners for this Hub and all of Hubs that are shared with it.
	    @see #addListener
	*/
	protected static HubListener[] getAllListeners(Hub thisHub) {
	    return getAllListeners(thisHub,0);
	}
	protected static HubListener[] getAllListeners(Hub thisHub, int type) {
	    /* 0: get all
	       1: get all that are duplicates (dataa == dataa)
	       2: get all that are shared with this hub only
	       3: get all that are duplicates (dataa == dataa), dont go to beginning
	    */
	    int size = 0;
	
	    Hub h = thisHub;
	
	    // go to beginning of shared hub chain
	    if (type < 2 && type != 3) {
	        for ( ; h.datau.sharedHub != null ; ) h = h.datau.sharedHub;
	    }
	    if (type == 3) type = 1;
	    ArrayList al = new ArrayList(10);
	    getAllListenersRecursive(h, al, thisHub, type);
	    HubListener[] hl = new HubListener[al.size()];
	    al.toArray(hl);
	    return hl;
	}
	protected static void getAllListenersRecursive(Hub thisHub, ArrayList<HubListener> al, Hub hub, int type) {
	    if (type == 0 || type == 2 || thisHub.dataa == hub.dataa) {
	        HubListener[] hls = getHubListeners(thisHub);
	        for (int i=0; hls != null && i<hls.length; i++) {
	            HubListener.InsertLocation loc = hls[i].getLocation();
	            int x = al.size();
	            if (x == 0 || loc == HubListener.InsertLocation.LAST) al.add(hls[i]);
	            else if (loc == HubListener.InsertLocation.FIRST) al.add(0, hls[i]);
	            else {
	                // insert before any listeners that have location=LAST
	                for (int j=x-1; j>=0; j--) {
	                    HubListener hl2 = (HubListener) al.get(j);
	                    if (hl2.getLocation() != HubListener.InsertLocation.LAST) {
	                        al.add((j+1), hls[i]);
	                        break;
	                    }
	                }
	            }
	        }
	    }
	    
        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(thisHub);
        for (int i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            getAllListenersRecursive(h2, al, hub, type);
        }
	}

    public static void fireAfterLoadEvent(Hub thisHub, OAObject oaObj) {
        HubListener[] hl = getAllListeners(thisHub);
        int x = hl.length;
        int i;
        if (x > 0) {
            HubEvent hubEvent = new HubEvent(thisHub, oaObj);
            for (i=0; i<x; i++) {
                hl[i].afterLoad(hubEvent);
            }
        }
    }
}
