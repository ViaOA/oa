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

import java.io.Serializable;
import java.util.*;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.util.OAComparator;
import com.viaoa.util.OAString;

/**
 * Delegate used for sorting hub.
 * @author vvia
 */
public class HubSortDelegate {

    /**
	    Reorder objects in this Hub, sorted by the value(s) from propertyPath(s).
	*/
    public static void sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp) {
        sort(thisHub, propertyPaths, bAscending, comp, false);
    }
    public static void sort(Hub thisHub, String propertyPaths, boolean bAscending) {
        sort(thisHub, propertyPaths, bAscending, null, false);
    }
    
    public static void sort(Hub thisHub, String propertyPaths, boolean bAscending, Comparator comp, boolean bAlreadySortedAndLocalOnly) {
        if (thisHub == null) return;
        // 20110204 added locking
        boolean b = false;
        try {
            OAThreadLocalDelegate.lock(thisHub);
            b = _sort(thisHub, propertyPaths, bAscending, comp, bAlreadySortedAndLocalOnly);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        if (b) afterPerformSort(thisHub); // outside of lock
    }
    
    public static HubSortListener getSortListener(Hub thisHub) {
        if (thisHub == null) return null;
        return thisHub.data.getSortListener();
    }
    
    private static boolean _sort(Hub thisHub, String propertyPaths, final boolean bAscending, Comparator comp, boolean bAlreadySortedAndLocalOnly) {
        OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message

        if (comp != null && !(comp instanceof Serializable)) {
            throw new RuntimeException("comparator is not Serializable");
        }
        
        boolean bSame = false;
        HubSortListener hsl = thisHub.data.getSortListener();
        if (OAString.isEqual(propertyPaths, thisHub.data.getSortProperty(),true)) {
            if (bAscending == thisHub.data.isSortAsc()) {
                bSame = true;
            }
        }
        
        if (hsl != null) {
            if (bSame) {
                // make sure that comparator is same
                if (hsl.comparator == null) return false;
                if (hsl.comparator instanceof OAComparator) {
                    OAComparator compx = (OAComparator) hsl.comparator;
                    if (OAString.isEqual(propertyPaths, compx.getPropertyPaths(),true)) {
                        if (bAscending == compx.getAsc()) {
                            return false;
                        }
                    }
                }
                bSame = false;
            }
            hsl.close();
            thisHub.data.setSortListener(null);
        }
        else {
            if (bSame) {
                if (OAString.isEmpty(propertyPaths) && comp == null) return false;
            }
        }
        
        thisHub.data.setSortProperty(propertyPaths);
        thisHub.data.setSortAsc(bAscending);
        
        if (propertyPaths != null || comp != null) {
            thisHub.data.setSortListener(new HubSortListener(thisHub, comp, propertyPaths, bAscending));
            if (!bAlreadySortedAndLocalOnly) performSort(thisHub);
        }
        else { // cancel sort
            thisHub.data.setSortAsc(true);
        }
        
        if (!bAlreadySortedAndLocalOnly) {  // otherwise, no other client has this hub yet
            if (thisHub.datam.getMasterObject() != null) {
                // 20171028 need to send if sort is cancelled
                //was: if (propertyPaths != null || comp != null) { // otherwise it was a cancel
                    HubCSDelegate.sort(thisHub, propertyPaths, bAscending, comp);
                //}
            }
        }
        return true;
    }
    
    
	public static void resort(Hub thisHub) {
		sort(thisHub);
	}
	
	/**
	    Re-sort using parameters from last sort or select.
	*/
	public static void sort(Hub thisHub) {
        if (thisHub == null) return;
        // 20131014 added locking
        try {
            OAThreadLocalDelegate.lock(thisHub);
            performSort(thisHub);
        }
        finally {
            OAThreadLocalDelegate.unlock(thisHub);
        }
        afterPerformSort(thisHub); // outside of lock
	}

	private static void performSort(Hub thisHub) {
		if (thisHub.data.getSortListener() == null) return;
		HubSelectDelegate.loadAllData(thisHub);
	    thisHub.data.changeCount++;
	    for (int i=0; i<3; i++) {
	        try {
    	        Collections.sort(thisHub.data.vector, thisHub.data.getSortListener().comparator);
    	        break;
	        }
	        catch (ConcurrentModificationException e) {
	        }
	    }
	}
    private static void afterPerformSort(Hub thisHub) {
        HubEventDelegate.fireAfterSortEvent(thisHub);
    }
	
	
	
    /**
	    Removes/disconnects HubSorter (if any) that is keeping objects in a sorted order.

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
/**qqqqqqqqqqq  20150810 removed, sort will keepSorted by default	
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
*/
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


