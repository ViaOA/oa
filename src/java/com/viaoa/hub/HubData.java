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
public class HubData implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization

	// Used to store objects so that the order of the objects is known.
	protected transient Vector vector;

	/**
	    Counter that is incremented on: add(), insert(), remove(), setting shared hub,
	    remove(), move(), sort(), select().
	    This can be used to know if a hub has been changed without requiring the set up of a HubListener.
	    <p>
	    This is used by OA.JSP components to know if a frame should be updated.  See com.viaoa.html.OATable.
	*/
	protected transient int changeCount;
	
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
	
	// used by setChanged
	protected boolean changed;
	
	// Flag to know if add/insert/remove objects should be tracked. Set to true when master object is set.
	protected boolean bTrackChanges;
	
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
	
	// Flag to know that all objects are being loaded from datasource.
	protected transient boolean loadingAllData;
	
	// Flag to know that objects are being fetched from datasource.
	protected transient boolean bInFetch;
	
	// used to "know" if hub should be add to HubController.setSelectAllHub()
	protected boolean bSelectAllHub; 
	
	// Name of property that must be unique for all objects in the Hub.
	protected String uniqueProperty;
	protected transient Method uniquePropertyGetMethod;
	
	
	/**
	    Constructor that supplies params for sizing Vector.
	*/
	public HubData(int size) {
	    vector = new Vector(size);
	}
	public HubData() {
		this(10);
	}
	
	
	
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();
        writeVector(s, vector);
        writeVector(s, vecAdd);
        writeVector(s, vecRemove);
    }
    
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        vector = readVector(s);
        vecAdd = readVector(s);
        vecRemove = readVector(s);
    }

    private void writeVector(java.io.ObjectOutputStream s, Vector vec) throws java.io.IOException{
        if (vec == null) {
            s.writeInt(-1);
            return;
        }
        
        int cap = vec.capacity();
        s.writeInt(cap);
        int max = vec.size();
        s.writeInt(max);
        
        
        int i = 0;
        for (; i<max; i++) {
            Object obj;
            try {
                obj = vec.elementAt(i);
            }
            catch (Exception e) {
                break;
            }
            s.writeObject(obj);
        }
        for (; i<max; i++) {
            // write out bogus objects
            s.writeObject(OANullObject.instance);
        }        
    }
    private Vector readVector(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        int capacity = s.readInt();
        if (capacity < 0) return null;
        Vector vec = new Vector(capacity);

        int max = s.readInt();

        // Read in all elements in the proper order. 
        for (int i=0; i<max; i++) {
            Object obj = s.readObject();
            if (!(obj instanceof OANullObject)) vec.addElement(obj);
        }
        return vec;
    }
    
    
}

