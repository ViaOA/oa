package com.viaoa.hub;



/**
 * Delegate used for the Find methods in Hub.
 * @author vvia
 *
 */
public class HubFindDelegate {

    
	/**
	    Returns first object in Hub that matches HubFinder object settings.
	    Returns null if not found.
	    @param bSetAO if true then the active object is set to found object.
	    @see HubFinder
	*/
	public static Object findFirst(Hub thisHub, HubFinder hubFinder, boolean bSetAO) {
	    thisHub.datau.hubFinder = hubFinder;
	    if (hubFinder == null) return null;
	    int pos = 0;
	    for ( ; ;pos++) {
	        Object object = thisHub.elementAt(pos);
	        if (object == null) break;
	        if (hubFinder.isEqual(object)) {
	            if (bSetAO) thisHub.setPos(pos);
	            return object;
	        }
	    }
	    if (bSetAO) thisHub.setPos(-1);
	    return null;
	}
	
	public static Object findFirst(Hub thisHub, String propertyPath, Object findObject, boolean bSetAO) {
	    HubFinder hf = new HubFinder(thisHub.getObjectClass(), propertyPath, findObject);
	    return findFirst(thisHub, hf, bSetAO);
	}
	
    /**
	    Find the next object in Hub that has property equal to findObject.
	    Starts with the next object after AO.
	*/
	public static Object findNext(Hub thisHub, boolean bSetAO) {
		HubFinder hf = thisHub.datau.hubFinder;
		if (hf != null) {
		    int pos = thisHub.getPos()+1;
		    for ( ; ;pos++) {
		        Object object = thisHub.elementAt(pos);
		        if (object == null) break;
		        if (hf.isEqual(object)) {
		        	if (bSetAO) thisHub.setPos(pos);
		            return object;
		        }
		    }
		}
	    if (bSetAO) thisHub.setPos(-1);
	    return null;
	}
    

	
}










