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
import java.util.concurrent.ConcurrentHashMap;
import com.viaoa.util.OANullObject;
import com.viaoa.ds.*;

/**
	Internally used by Hub to store objects.  Shared Hubs will use this same object.<br>
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
    
    // used by setChanged
    protected boolean changed;
    
    // Flag to know if add/insert/remove objects should be tracked. Set to true when master object is set.
    protected boolean bTrackChanges;

    private HubDatax hubDatax; // extension
    
	/**
	    Constructor that supplies params for sizing Vector.
	*/
	public HubData(int size) {
	    vector = new Vector(size);
	}
	public HubData() {
		this(7);
	}
	
	   
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();
        writeVector(s, vector);
        Vector vec;
        if (hubDatax != null) vec = hubDatax.vecAdd;
        else vec = null;
        writeVector(s, vec);
        if (hubDatax != null) vec = hubDatax.vecRemove;
        else vec = null;
        writeVector(s, vec);
    }
    
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        vector = readVector(s);
        
        Vector vec = readVector(s);
        setVecAdd(vec);
        
        vec = readVector(s);
        setVecRemove(vec);
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
    
    
    static int qq;    
    private HubDatax getHubDatax() {
        if (hubDatax == null) {
            synchronized (this) {
                if (hubDatax == null) {
//qqqqqqqqqqqqqq                    
System.out.println((++qq)+") HubDatax created");                    
                    this.hubDatax = new HubDatax();
                }
            }
        }
        return hubDatax;
    }
    

    
    public int getNewListCount() {
        if (hubDatax == null) return 0;
        return hubDatax.newListCount;
    }
    public void setNewListCount(int newListCount) {
        if (hubDatax != null || newListCount != 0) {
            getHubDatax().newListCount = newListCount;
        }
    }
    public Vector getVecAdd() {
        if (hubDatax == null) return null;
        return hubDatax.vecAdd;
    }
    public void setVecAdd(Vector vecAdd) {
        if (hubDatax != null || vecAdd != null) {
            getHubDatax().vecAdd = vecAdd;
        }
    }
    public Vector getVecRemove() {
        if (hubDatax == null) return null;
        return hubDatax.vecRemove;
    }
    public void setVecRemove(Vector vecRemove) {
        if (hubDatax != null || vecRemove != null) {
            getHubDatax().vecRemove = vecRemove;
        }
    }
    public HubSortListener getSortListener() {
        if (hubDatax == null) return null;
        return hubDatax.sortListener;
    }
    public void setSortListener(HubSortListener sortListener) {
        if (hubDatax != null || sortListener != null) {
            getHubDatax().sortListener = sortListener;
        }
    }
    public String getSortProperty() {
        if (hubDatax == null) return null;
        return hubDatax.sortProperty;
    }
    public void setSortProperty(String sortProperty) {
        if (hubDatax != null || sortProperty != null) {
            getHubDatax().sortProperty = sortProperty;
        }
    }
    public boolean isSortAsc() {
        if (hubDatax == null) return false;
        return hubDatax.sortAsc;
    }
    public void setSortAsc(boolean sortAsc) {
        if (hubDatax != null || sortAsc) {
            getHubDatax().sortAsc = sortAsc;
        }
    }
    public OASelect getSelect() {
        if (hubDatax == null) return null;
        return hubDatax.select;
    }
    public void setSelect(OASelect select) {
        if (hubDatax != null || select != null) {
            getHubDatax().select = select;
        }
    }
    public boolean isRefresh() {
        if (hubDatax == null) return false;
        return hubDatax.refresh;
    }
    public void setRefresh(boolean refresh) {
        if (hubDatax != null || refresh) {
            getHubDatax().refresh = refresh;
        }
    }
    
    private static ConcurrentHashMap<HubData, HubData> hmLoadingAllData = new ConcurrentHashMap<HubData, HubData>(11, .85f);
    public boolean isLoadingAllData() {
        return hmLoadingAllData.contains(this);
    }
    public void setLoadingAllData(boolean loadingAllData) {
        if (loadingAllData) hmLoadingAllData.put(this, this);
        else hmLoadingAllData.remove(this);
    }

    private static ConcurrentHashMap<HubData, HubData> hmInFetch = new ConcurrentHashMap<HubData, HubData>(11, .85f);
    public boolean isInFetch() {
        return hmInFetch.contains(this);
    }
    public void setInFetch(boolean bInFetch) {
        if (bInFetch) hmInFetch.put(this, this);
        else hmInFetch.remove(this);
    }

    
    private static ConcurrentHashMap<HubData, HubData> hmSelectAllHub = new ConcurrentHashMap<HubData, HubData>(11, .85f);
    public boolean isSelectAllHub() {
        return hmSelectAllHub.contains(this);
    }
    public void setSelectAllHub(boolean bSelectAllHub) {
        if (bSelectAllHub) hmSelectAllHub.put(this, this);
        else hmSelectAllHub.remove(this);
    }

    public String getUniqueProperty() {
        if (hubDatax == null) return null;
        return hubDatax.uniqueProperty;
    }
    public void setUniqueProperty(String uniqueProperty) {
        if (hubDatax != null || uniqueProperty != null) {
            getHubDatax().uniqueProperty = uniqueProperty;
        }
    }
    public Method getUniquePropertyGetMethod() {
        if (hubDatax == null) return null;
        return hubDatax.uniquePropertyGetMethod;
    }
    public void setUniquePropertyGetMethod(Method uniquePropertyGetMethod) {
        if (hubDatax != null || uniquePropertyGetMethod != null) {
            getHubDatax().uniquePropertyGetMethod = uniquePropertyGetMethod;
        }
    }
    public boolean isDisabled() {
        if (hubDatax == null) return false;
        return hubDatax.disabled;
    }
    public void setDisabled(boolean disabled) {
        if (hubDatax != null || disabled) {
            getHubDatax().disabled = disabled;
        }
    }

}

