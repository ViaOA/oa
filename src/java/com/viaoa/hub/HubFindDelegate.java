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

import com.viaoa.object.OAFinder;



/**
 * Delegate used for the Find methods in Hub.
 * @author vvia
 *
 */
public class HubFindDelegate {
// 20140120 changed from HubFinder to OAFinder
    
	/**
	    Returns first object in Hub that matches HubFinder object settings.
	    Returns null if not found.
	    @param bSetAO if true then the active object is set to found object.
	    @see HubFinder
	*/
    
	public static Object findFirst(Hub thisHub, OAFinder finder, Object findObject, boolean bSetAO) {
	    thisHub.datau.finder = finder;
	    if (finder == null) return null;
        Object obj = finder.findFirstRoot(findObject);
        if (bSetAO) thisHub.setAO(obj);
        return obj;
	}
	
    
    public static Object findFirst(Hub thisHub, String propertyPath, Object findObject, boolean bSetAO) {
	    OAFinder hf = new OAFinder(thisHub, null, propertyPath);
	    Object obj = hf.findFirstRoot(findObject);
	    if (bSetAO) thisHub.setAO(obj);
	    return obj;
	}
	
    /**
	    Find the next object in Hub that has property equal to findObject.
	    Starts with the next object after AO.
	*/
	public static Object findNext(Hub thisHub, boolean bSetAO) {
		OAFinder hf = thisHub.datau.finder;
		if (hf != null) {
		    Object objx = hf.findNextRoot();
        	if (bSetAO) thisHub.setAO(objx);
            return objx;
		}
	    if (bSetAO) thisHub.setPos(-1);
	    return null;
	}
    

	
}










