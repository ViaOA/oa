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

