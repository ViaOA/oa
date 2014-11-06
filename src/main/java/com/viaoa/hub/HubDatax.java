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

import java.lang.reflect.Method;
import java.util.*;

import com.viaoa.object.*;
import com.viaoa.util.OANullObject;
import com.viaoa.ds.*;

/**
	Internally used by Hub to store objects.  Shared Hubs will use this same object.<br>
	A Vector and Hashtable are used to store the objects.
*/
public class HubDatax implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization

	
	/**
	    Counter that is incremented when a new list of objects is loaded.
	    Incremented by select, setSharedHub, and when
	    detail hubs list is changed to match the master hub's activeObject.<br>
	    This can be used to know if a hub has been changed without requiring the set up of a HubListener.
	    <p>
	    This is used by JSP components to know if a frame should be updated. <br>
	    See com.viaoa.html.OATable and com.viaoa.html.OANav
	*/
	protected transient int newListCount;
	
	// If bTrackChanges is true, then all objects that are added to Hub are added to this vector.
	protected transient Vector vecAdd; // only for OAObjects
	
	// If bTrackChanges is true, then all objects that are removed from Hub are added to this vector.
	protected transient Vector vecRemove;  // only for OAObjects
	
	protected transient HubSortListener sortListener;
    //  info to keep Hub objects sorted when sent to other computers, see HubSerializerDelegate._readResolve - it will set up sorting when received
	protected String sortProperty;
	protected boolean sortAsc;
	
	// Used to select objects from OADataSource.
	protected transient OASelect select;
	
	
	/**
	    Flag used by Hub.setFresh() so that active objects are always refreshed from
	    datasource.
	    <p>
	    Note: this is not implemented.
	*/
	protected boolean refresh = false;

	
	// Name of property that must be unique for all objects in the Hub.
	protected String uniqueProperty;
	protected transient Method uniquePropertyGetMethod;
	
	protected transient boolean disabled;
	
    
}

