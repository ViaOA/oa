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
import java.lang.reflect.Method;
import java.util.*;

import com.viaoa.object.*;

/**
	Internally used by Hub
	for unique settings/data for this Hub, that are not shared with Shared Hubs.
*/
class HubDataUnique implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization
	
	/** Class of objects in this Hub */
	protected Class objClass;
	
	/** true if this is for a OAObject */
	protected boolean oaObjectFlag;
	
	/** these options are not enforced on OAObjects, they are used to flag options */
//	boolean allowNew = true, allowDelete = true, allowEdit = true;
	
	/**
	    flag set to know if objects can be added or removed.  This is false when a detail hub
	    is from an array or non-Hub.  Defalut is true.
	*/
	boolean dupAllowAddRemove = true;
	
	/**
	    Single finder object used by last call to findX methods that did not use a HubFinder object.
	    <p>
	    Note: not using a HubFinder with the Hub.findX methods is not thread safe, since there is only one
	    hubFinder stored with a Hub.
	*/
	transient OAFinder finder;
	transient int finderPos;
	
	/** OAObjectInfo for the Class of objects in this Hub. */
	transient OAObjectInfo objectInfo;  //
	
	/** Misc name/values pairs stored in this Hub.  Name is case insensitive. */
	protected Hashtable hashProperty;
	
	/** property path(s) used for selectOrder */
	protected String selectOrder;
	
	/** used to update property in objects to match its position within Hub */
	protected transient HubAutoSequence autoSequence;
	
	/** makes sure that this Hub will have an object with a reference for each object in another Hub. */
	protected transient HubAutoMatch autoMatch;
	
	/**
	    Position of active object to set for new list. Can be set to 0 so that first object
	    is always made the active object whenever a new list is created.  Default is -1 (set to null).
	    <p>
	    This can be set for Detail Hubs, so that the first object is active whenever a new list
	    is create - which is when the master Hub changes its active object.
	*/
	protected transient int defaultPos = -1;
	
	/** Set ActiveObject to null when active object is removed.
	    Default is false which will go to next object, unless last object in Hub
	    is being removed, which will set it to previous object.
	*/
	protected transient boolean bNullOnRemove;
	
	/**
	    Hub Listeners that receives all Hub and OAObject events.
	    @see HubListener
	    @see HubEvent
	*/
	// protected transient Vector vecListener;
	
	// 20101218 replaces vetListener
	protected transient HubListenerTree listenerTree;
	
    
	
	/**
	    Detail Hubs that this Hub has.
	    @see Hub#getDetail
	*/
	protected transient Vector<HubDetail> vecHubDetail;
	
	/** flag set while updating active object */
	protected transient boolean bUpdatingActiveObject;
	
	/**
	    List of listeners for calculated properties.
	    The hub will automatically listen for changes to any property that a calculated property
	    is dependent on.
	    @see Hub#addListener(HubListener,String)
	*/
// 20101218 replaced by HubListenerTree	
//	transient Vector<HubCalcEventListener> calcEventListeners;
	
	
	/**
	    "Master" Hub that this Hub is linked to.
	    <p>
	    A hub can only be linked to a property in only one "master" hub.
	    It's active object will then reflect the value of this property in the "master"
	    hub's active object.  If the active object of the link hub changes, then the
	    property in the "master" hub will be set to the new object.
	    @see hub#setLink
	 */
	transient Hub linkToHub;
	
	
	/**
	    This can be used to set a property in the Link Hub to the value
	    of the position of the active object in this Hub.
	    <p>
	    Example: an object can have a property that is set to 0-9.  Another
	    Hub can be created with ten objects, and linked to this property.
	    Instead of setting the property to the object, it is set to the position
	    of the object.
	*/
	transient boolean linkPos;
	
	/**
	    Property that this Hub is linked to.
	*/
	transient String linkToPropertyName;  // ex: hubDept linked to Emp on property  "dept"
	
	/** Method used to get value of link to object. */
	transient Method linkToGetMethod;     //     getDept()
	
	/** Method used to set value of link to object. */
	transient Method linkToSetMethod;     //     setDept()
	
	
	/**
	    Links can also be set up so that a property in the link Hub is used to update
	    a property in the linkedTo/Master Hub.
	    <p>
	    LinkPropertyName is the name of the property that is used in the Linked Hub.
	    <p>
	    Example:
	    A Hub that has objects of type "State" can be linked to another Hub, so that the State.name
	    is linked to a property in the Master Hub.  The linkFromPropertyName is "name".
	
	*/
	transient String linkFromPropertyName;
	
	/**
	    Method that gets value from linkFromPropertyName.
	*/
	transient Method linkFromGetMethod;
	
	/**
	    Used to automatically create a new object in the Master Hub whenever
	    the active object in Link Hub is changed.  The new object will then
	    have its link property set.
	*/
	transient boolean bAutoCreate;

    /**
     * If true and bAutoCreate, then new objects will be created.
     * If false and a new object with value already exists, then a new object will not be created
     *    and the current object will be set to AO
    */
    transient boolean bAutoCreateAllowDups;
	
	
	/**
	    Hub Listener used to update active object when the active object in
	    the Master Hub changes, or when the link property in the Master Hub is changed.
	*/
	transient HubLinkEventListener hubLinkEventListener;
	
	/**
	    Hub that this Hub is a sharing with.
	    Hubs can be set up so that they use (share) the same data.  The active object is
	    not shared, unless specified otherwise.
	    <p>
	    Detail Hubs that are using properties that are Hubs will use a shared Hub that is
	    changed whenever the active object in the Master Hub is changed.
	
	    @see Hub#createSharedHub
	    @see Hub#getDetail
	*/
	transient Hub sharedHub;
	
	
	/**
	    List of Hubs that are sharing the same objects as this Hub.  Each of these Hubs will
	    have the same HubData object.  If the active object is also being shared, then
	    the HubDataActive object will also be the same.
	*/
//	transient Vector vecSharedHub;

	// 20120715 replaces vecSharedHubs
	transient WeakReference<Hub>[] weakSharedHubs;	
	
	
	/**
	    Hub used to add active object to whenever active object is changed in this Hub.
	    This can be used for building a pick list type program, where a user can select
	    objects that are then added to a list.
	*/
	protected transient Hub addHub;
	
	
}
