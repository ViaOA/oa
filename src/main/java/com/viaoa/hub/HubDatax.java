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

import java.lang.reflect.Method;
import java.util.*;
import com.viaoa.object.*;
import com.viaoa.ds.*;

/**
	Internally used by Hub to store objects.  Shared Hubs will use this same object.<br>
	A Vector and Hashtable are used to store the objects.
*/
public class HubDatax implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization


    public boolean shouldSerialize() {
        if (sortProperty != null) return true;
        if (!sortAsc) return true;
        if (uniqueProperty != null) return true;
        if (bTrackChanges) return true;
        return false;
    }
    
	/**
	    Counter that is incremented when a new list of objects is loaded.
	    Incremented by select, setSharedHub, and when
	    detail hubs list is changed to match the master hub's activeObject.<br>
	    This can be used to know if a hub has been changed without requiring the set up of a HubListener.
	    <p>
	    This is used by JSP components to know if a frame should be updated. <br>
	    See com.viaoa.html.OATable and com.viaoa.html.OANav
	*/
//	protected transient int newListCount;
	
	// If bTrackChanges is true, then all objects that are added to Hub are added to this vector.
	protected transient Vector vecAdd; // only for OAObjects
	
	// If bTrackChanges is true, then all objects that are removed from Hub are added to this vector.
	protected transient Vector vecRemove;  // only for OAObjects
	
	protected transient HubSortListener sortListener;
    //  info to keep Hub objects sorted when sent to other computers, see HubSerializerDelegate._readResolve - it will set up sorting when received
	protected String sortProperty;  // defaults to linkInfo.sortProperty
	protected boolean sortAsc=true;
	
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
	
    /** true if this is for a OAObject */
    protected boolean oaObjectFlag;

    /**
        flag set to know if objects can be added or removed.  This is false when a detail hub
        is from an array or non-Hub.  Default is true.
    */
    protected boolean dupAllowAddRemove = true;


    /** OAObjectInfo for the Class of objects in this Hub. */
    protected transient OAObjectInfo objectInfo;  //
    
    /** Misc name/values pairs stored in this Hub.  Name is case insensitive. */
    protected Hashtable hashProperty;
    
    /** property path(s) used for selectOrder */
    protected String selectOrder;
    
    /** used to update property in objects to match its position within Hub */
    protected transient HubAutoSequence autoSequence;
    
    /** makes sure that this Hub will have an object with a reference for each object in another Hub. */
    protected transient HubAutoMatch autoMatch;


    // Flag to know if add/insert/remove objects should be tracked. see also datam.getTrackChanges()
    protected boolean bTrackChanges;
}

