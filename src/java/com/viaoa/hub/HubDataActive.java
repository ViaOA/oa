package com.viaoa.hub;

/**
	Internally used by Hub
	to know the current active object.
	A shared Hub can also use this same object.
*/
class HubDataActive implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization
	
	/**
	    Current object in Hub that the active object.
	    @see Hub#setActiveObject
	    @see Hub#getActiveObject
	*/
	protected transient Object activeObject;
	
	/**
	    Used by Hub.updateDetail() when calling setSharedHub, for Hubs that
	    do not shared same active object, so that active object is set to null.
	*/
	public void clear(boolean eof) {
	    activeObject = null;
	}
}

