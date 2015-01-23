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

import java.util.ArrayList;

import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObject;
import com.viaoa.util.OACompare;

/**
 * Delegate used for the Find methods in Hub.
 * @author vvia
 *
 */
public class HubFindDelegate {
// 20140120 changed from HubFinder to OAFinder

	/**
	    Returns first object in Hub that matches propertyPath findValue.
	    Returns null if not found.
	    @param bSetAO if true then the active object is set to the found object.
	    @see HubFind
	*/
    public static Object findFirst(Hub thisHub, String propertyPath, final Object findValue, final boolean bSetAO, OAObject lastFoundObject) {
        if (thisHub == null) return null;
        
        OAFinder finder = new OAFinder();
        finder.addEqualFilter(propertyPath, findValue);
        Object foundObj = finder.findNext(thisHub, (OAObject) lastFoundObject);

        
/*        
        Object foundObj = null;
        for (int i=0; ;i++) {
            Object obj = thisHub.getAt(i);
            if (obj == null) break;
            if (finder.findFirst((OAObject) obj) != null) {
                foundObj = obj;
                thisHub.datau.setFinderPos(i);
                break;
            }
        }
        if (foundObj != null) thisHub.datau.setFinder(finder);
        else thisHub.datau.setFinderPos(-1);
        
        if (bSetAO) thisHub.setPos(thisHub.datau.getFinderPos());
*/        
        if (bSetAO) thisHub.setAO(foundObj);
        return foundObj;
	}

    
    /**
	    Find the next object in Hub that has property equal to findObject.
	    Starts with the next object after last find from findFirst or findNext.
	
	public static Object findNext(Hub thisHub, boolean bSetAO) {
		OAFinder find = thisHub.datau.getFinder();
		if (find == null) {
	        if (bSetAO) thisHub.setPos(-1);
	        return null;
		}

        Object foundObj = null;
        for (int i=thisHub.datau.getFinderPos()+1; ;i++) {
            Object obj = thisHub.getAt(i);
            if (obj == null) break;
            ArrayList al = find.find((OAObject) obj);
            if (al.size() > 0) {
                thisHub.datau.setFinderPos(i);
                foundObj = obj;
                break;
            }
        }
        if (foundObj == null) {
            thisHub.datau.setFinderPos(-1);
            thisHub.datau.setFinder(null);
        }
        if (bSetAO) thisHub.setPos(thisHub.datau.getFinderPos());
	    return foundObj;
	}
	*/
    	
}

