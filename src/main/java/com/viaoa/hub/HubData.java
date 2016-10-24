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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OANullObject;
import com.viaoa.ds.*;

/**
	Internally used by Hub to store objects.  Shared Hubs will use this same object.<br>
*/
public class HubData implements java.io.Serializable {
    static final long serialVersionUID = 1L;  // used for object serialization
    private static Logger LOG = Logger.getLogger(HubData.class.getName());

    /** Class of objects in this Hub */
    protected volatile Class objClass;
    
    // Used to store objects so that the order of the objects is known.
    protected transient Vector vector;

    /**
        Counter that is incremented on: add(), insert(), remove(), setting shared hub,
        remove(), move(), sort(), select().
        This can be used to know if a hub has been changed without requiring the set up of a HubListener.
        <p>
        This is used by OA.JSP components to know if a frame should be updated.  See com.viaoa.html.OATable.
    */
    protected volatile transient int changeCount;
    
    // used by setChanged
    protected volatile boolean changed;
    
    protected transient volatile HubDatax hubDatax; // extension
    
	/**
	    Constructor that supplies params for sizing Vector.
	*/
	public HubData(Class objClass, int size) {
	    int x = size * 2;
	    x = Math.max(5, x);
	    x = Math.min(25, x);
	    vector = new Vector(size, x);
	    this.objClass = objClass;
	}
	public HubData(Class objClass) {
		this(objClass, 5);
	}
    public HubData(Class objClass, int size, int incrementSize) {
        int x = Math.max(1, incrementSize);
        x = Math.min(100, x);
        vector = new Vector(size, x);
        this.objClass = objClass;
    }
	
    static int qq;    
    private HubDatax getHubDatax() {
        if (hubDatax == null) {
            synchronized (this) {
                if (hubDatax == null) {
                    if (++qq % 500 == 0) {
                        LOG.finer((qq)+") HubDatax created");
                    }
                    this.hubDatax = new HubDatax();
                }
            }
        }
        return hubDatax;
    }
    
/*    
    public int getNewListCount() {
        if (hubDatax == null) return 0;
        return hubDatax.newListCount;
    }
    public void setNewListCount(int newListCount) {
        if (hubDatax != null || newListCount != 0) {
            getHubDatax().newListCount = newListCount;
        }
    }
*/    
    public Vector getVecAdd() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.vecAdd;
    }
    public void setVecAdd(Vector vecAdd) {
        if (hubDatax != null || vecAdd != null) {
            getHubDatax().vecAdd = vecAdd;
        }
    }
    public Vector getVecRemove() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.vecRemove;
    }
    public void setVecRemove(Vector vecRemove) {
        if (hubDatax != null || vecRemove != null) {
            getHubDatax().vecRemove = vecRemove;
        }
    }
    
    // see also: HubDataMaster.getSortProperty(), which uses linkinfo.sortProperty
    public String getSortProperty() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.sortProperty;
    }
    public void setSortProperty(String sortProperty) {
        if (hubDatax != null || sortProperty != null) {
            getHubDatax().sortProperty = sortProperty;
        }
    }
    public boolean isSortAsc() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return true;
        return hdx.sortAsc;
    }
    public void setSortAsc(boolean sortAsc) {
        if (hubDatax != null || !sortAsc) {
            getHubDatax().sortAsc = sortAsc;
        }
    }
    public HubSortListener getSortListener() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.sortListener;
    }
    public void setSortListener(HubSortListener sortListener) {
        if (hubDatax != null || sortListener != null) {
            getHubDatax().sortListener = sortListener;
        }
    }
    
    
    public OASelect getSelect() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.select;
    }
    public void setSelect(OASelect select) {
        if (hubDatax != null || select != null) {
            getHubDatax().select = select;
            if (select == null) {
                if (!hubDatax.isNeeded()) hubDatax = null;
            }
        }
    }
    public boolean isRefresh() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return false;
        return hdx.refresh;
    }
    public void setRefresh(boolean refresh) {
        if (hubDatax != null || refresh) {
            getHubDatax().refresh = refresh;
        }
    }

//20160828qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq remove this???QQQQQQQQQQQQQQQQQQQQQQQQQ    
    private static ConcurrentHashMap<HubData, HubData> hmLoadingAllData = new ConcurrentHashMap<HubData, HubData>(11, .85f);
    public boolean isLoadingAllData() {
        return hmLoadingAllData.contains(this);
    }
    public void setLoadingAllData(boolean loadingAllData) {
        if (loadingAllData) hmLoadingAllData.put(this, this);
        else hmLoadingAllData.remove(this);
    }

    private static ConcurrentHashMap<HubData, HubData> hmSelectAllHub = new ConcurrentHashMap<HubData, HubData>(11, .85f);
    public boolean isSelectAllHub() {
        return hmSelectAllHub.contains(this);
    }
    public void setSelectAllHub(boolean bSelectAllHub) {
        if (bSelectAllHub) hmSelectAllHub.put(this, this);
        else hmSelectAllHub.remove(this);
    }

    // note: could also be in HubDataMaster.
    public String getUniqueProperty() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.uniqueProperty;
    }
    public void setUniqueProperty(String uniqueProperty) {
        if (hubDatax != null || uniqueProperty != null) {
            getHubDatax().uniqueProperty = uniqueProperty;
        }
    }
    public Method getUniquePropertyGetMethod() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.uniquePropertyGetMethod;
    }
    public void setUniquePropertyGetMethod(Method uniquePropertyGetMethod) {
        if (hubDatax != null || uniquePropertyGetMethod != null) {
            getHubDatax().uniquePropertyGetMethod = uniquePropertyGetMethod;
        }
    }
    public boolean isDisabled() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return false;
        return hdx.disabled;
    }
    public void setDisabled(boolean disabled) {
        if (hubDatax != null || disabled) {
            getHubDatax().disabled = disabled;
        }
    }

    public Hashtable getHashProperty() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.hashProperty;
    }
    public void setHashProperty(Hashtable hashProperty) {
        if (hubDatax != null || hashProperty != null) {
            getHubDatax().hashProperty = hashProperty;
        }
    }
    public OAObjectInfo getObjectInfo() {
        OAObjectInfo oi;
        HubDatax hdx = hubDatax;
        if (hdx != null) {
            oi = hdx.objectInfo;
            if (oi != null) return oi;
        }
        oi = OAObjectInfoDelegate.getObjectInfo(objClass);
        if (objClass != null && hubDatax != null) hubDatax.objectInfo = oi;
        return oi;
    }
    public void setObjectInfo(OAObjectInfo objectInfo) {
        if (hubDatax != null) hubDatax.objectInfo = objectInfo;
        if (objectInfo != null && objClass == null) {
            this.objClass = objectInfo.getForClass();
        }
    }

    public HubAutoSequence getAutoSequence() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.autoSequence;
    }
    public void setAutoSequence(HubAutoSequence autoSequence) {
        if (hubDatax != null || autoSequence != null) {
            getHubDatax().autoSequence = autoSequence;
        }
    }
    
    public HubAutoMatch getAutoMatch() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return null;
        return hdx.autoMatch;
    }
    public void setAutoMatch(HubAutoMatch autoMatch) {
        if (hubDatax != null || autoMatch != null) {
            getHubDatax().autoMatch = autoMatch;
        }
    }

    public boolean isOAObjectFlag() {
        HubDatax hdx = hubDatax;
        if (hdx != null) {
            if (hdx.oaObjectFlag) return true;
            boolean b = objClass != null && OAObject.class.isAssignableFrom(objClass);
            hdx.oaObjectFlag = b;
            return b;
        }
        return objClass != null && OAObject.class.isAssignableFrom(objClass);
    }
    public void setOAObjectFlag(boolean oaObjectFlag) {
        if (hubDatax != null) hubDatax.oaObjectFlag = oaObjectFlag; 
    }


    public boolean isDupAllowAddRemove() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return true; // default
        return hdx.dupAllowAddRemove;
    }
    public void setDupAllowAddRemove(boolean dupAllowAddRemove) {
        if (hubDatax != null || !dupAllowAddRemove) {
            getHubDatax().dupAllowAddRemove = dupAllowAddRemove;
        }
    }


    /**
     * Used to have Hub add/removes tracked.  By default, this is false.
     * @see HubDataMaster#getTrackChanges()
     * @return
     */
    public boolean getTrackChanges() {
        HubDatax hdx = hubDatax;
        if (hdx == null) return false;
        return hdx.bTrackChanges;
    }
    public void setTrackChanges(boolean bTrackChanges) {
        if (hubDatax != null || bTrackChanges) {
            getHubDatax().bTrackChanges = bTrackChanges;
        }
    }


    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();
        
        HubDatax hdx = hubDatax;
        if (hdx != null && !hdx.shouldSerialize()) hdx = null;
        s.writeObject(hdx);
        
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
        hubDatax = (HubDatax) s.readObject();
        vector = readVector(s);
        
        Vector vec = readVector(s);
        if (vec != null && vec.size() > 0) setVecAdd(vec);
        
        vec = readVector(s);
        if (vec != null && vec.size() > 0) setVecRemove(vec);
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

