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

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.util.OAString;

/**
 * Delegate used for sorting hub.
 * @author vvia
 *
 */
public class HubSortDelegate {

    /**
	    Reorder objects in this Hub, sorted by the value(s) from propertyPath(s).
	    @see sort(String,boolean)
	    @see HubSorter
	    @see #cancelSort
	    Note: all sort methods need to call this method, since this will handle Client/Server issues.
	*/
    public static void sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp) {
        sort(thisHub, propertyPaths, bAscending, comp, false);
    }
    public static void sort(Hub thisHub, String propertyPaths, boolean bAscending) {
        sort(thisHub, propertyPaths, bAscending, null, false);
    }
    
    public static void sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp, boolean bAlreadySortedAndLocalOnly) {
        // 20110204 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            _sort(thisHub, propertyPaths, bAscending, comp, bAlreadySortedAndLocalOnly);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
    }
    
    public static HubSortListener getSortListener(Hub thisHub) {
        return thisHub.data.getSortListener();
    }
    
    private static void _sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp, boolean bAlreadySortedAndLocalOnly) {
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
        boolean bSame = false;
        if (propertyPaths == thisHub.data.getSortProperty() || (propertyPaths != null && propertyPaths.equalsIgnoreCase(thisHub.data.getSortProperty()))) {
            if (bAscending == thisHub.data.isSortAsc()) {
                if (comp == null || comp == thisHub.data.getSortListener().comparator) {
                    bSame = true;
                }
            }
        }
        
        if (thisHub.data.getSortListener() != null) {
            if (bSame) return;
            thisHub.data.getSortListener().close();
            thisHub.data.setSortListener(null);
        }
        else {
            if (bSame) {
                if (OAString.isEmpty(propertyPaths) && comp == null) return;
            }
        }
        
        thisHub.data.setSortProperty(propertyPaths);
        thisHub.data.setSortAsc(bAscending);
        
        if (propertyPaths != null || comp != null) {
            thisHub.data.setSortListener(new HubSortListener(thisHub, comp, propertyPaths, bAscending));
            if (!bAlreadySortedAndLocalOnly) performSort(thisHub);
        }
        else { // cancel sort
            thisHub.data.setSortAsc(false);
        }
        
        if (!bAlreadySortedAndLocalOnly) {  // otherwise, no other client has this hub yet
            if (thisHub.datam.masterObject != null) {
                HubCSDelegate.sort(thisHub, propertyPaths, bAscending, comp);
            }
        }
    }
    
    
	public static void resort(Hub thisHub) {
		sort(thisHub);
	}
	
	/**
	    Re-sort using parameters from last sort or select.
	*/
	public static void sort(Hub thisHub) {
        // 20131014 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            performSort(thisHub);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
	}

	private static void performSort(Hub thisHub) {
		if (thisHub.data.getSortListener() == null) return;
		HubSelectDelegate.loadAllData(thisHub);
	    thisHub.data.changeCount++;
	    Collections.sort(thisHub.data.vector, thisHub.data.getSortListener().comparator);
	    HubEventDelegate.fireAfterSortEvent(thisHub);
	}
	
    /**
	    Removes/disconnects HubSorter (if any) that is keeping objects in a sorted order.
	    @see sort(String,boolean)
	*/
	public static void cancelSort(Hub thisHub) {
	    if (isSorted(thisHub)) {
	        sort(thisHub, null, false, null);
	    }
	}
	
	/**
	    Used to keep objects sorted based on last call to select method.  By default, the sort order
	    used in a select is not maintained within the Hub.  This method will keep the objects sorted
	    using the same property paths used by select.
	*/
	public static void keepSorted(Hub thisHub) {
	    // 20090801 cant have sorter if a AutoSequence is being used
	    if (thisHub.data.getAutoSequence() != null) {
	        return;
	    }
	    if (thisHub.data.getSortListener() != null) return;
	    if (HubSelectDelegate.getSelect(thisHub) == null) return;
	    String s = HubSelectDelegate.getSelect(thisHub).getOrder();
	    if (s == null || s.length() == 0) return;
	    sort(thisHub, s, true, null, true);
	}

	/**
	 * used to determine if the Hub is currently kept sorted.
	 * Otherwise, it might have been sorted when it was loaded, but not kept sorted.
	 * ex: if there is a sequence property used to autoSeq the objects in the hub
	 */
    public static boolean isSorted(Hub thisHub) {
        return (thisHub.data.getSortListener() != null);
    }

    /**
     * @see #isSorted(Hub) to see if the hub is kept sorted. 
     */
    public static String getSortProperty(Hub thisHub) {
        String s = thisHub.data.getSortProperty();
        if (s == null) s = thisHub.datam.getSortProperty();
        return s;
    }
    /**
     * @see #isSorted(Hub) to see if the hub is kept sorted. 
     */
    public static boolean getSortAsc(Hub thisHub) {
        boolean b = thisHub.data.isSortAsc();
        b = b || thisHub.datam.isSortAsc();
        return b;
    }
    /**
     * @see #isSorted(Hub) to see if the hub is kept sorted. 
     */
    public static String getSeqProperty(Hub thisHub) {
        String s = thisHub.datam.getSeqProperty();
        return s;
    }
}


