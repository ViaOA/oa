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

import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectKey;
import com.viaoa.object.OAObjectReflectDelegate;

/** 
    This is the single event used by OAObject and Hub that is sent to HubListeners.
*/
public class HubEvent<T> extends java.beans.PropertyChangeEvent {
    T object;
    int pos, toPos;
    boolean bCancel;

    /** used to testing/watching events. */
    static int cnt = 0; 
    void p(String s) {
        if ( (cnt%10) == 0) System.out.println("Event =========> "+(++cnt)+" "+s);
    }

    /** 
        Used for propertyChange events, when an objects property is changed.
    */
    public HubEvent(Hub source, T obj, String propertyName, Object oldValue, Object newValue) {
        super(source, propertyName, oldValue, newValue);
        //p("1: propChange "+obj+" "+propertyName );//qqqqqqqqqqq
        this.object = obj;
    }

    /** 
        Used for propertyChange events, when an objects property is changed.
    */
    public HubEvent(T obj, String propertyName, Object oldValue, Object newValue) {
        super(obj, propertyName, oldValue, newValue);
        //p("2: propChange "+obj+" "+propertyName );//qqqqqqqqqqq
        this.object = obj;
    }

    public Hub<T> getHub() {
        Object obj = getSource();
        if (obj instanceof Hub) return (Hub<T>) obj;
        return null;
    }
    
    /** 
        Used for Hub replace events, when an object is replaced with another object.
    */
    public HubEvent(Hub<T> source, T oldValue, T newValue) {
        super(source, null, oldValue, newValue);
        //p("3: replace "+source.getObjectClass() );//qqqqqqqqqqq
        object = newValue;
    }
    
    /** 
        Used for Hub move events, when an object is moved within a Hub.
    */
    public HubEvent(Hub<T> source, int posFrom, int posTo) {
        super(source, null, null, null);
        //p("4: move "+source.getObjectClass() );//qqqqqqqqqqq
        this.pos = posFrom;
        this.toPos = posTo;
    }
    
    /** 
        Used for Hub insert events, when an object is inserted into a Hub.
    */
    public HubEvent(Hub<T> source, T obj, int pos) {
        super(source, null, null, null);
        //p("5: add/insert/remove "+source.getObjectClass()+" "+obj );//qqqqqqqqqqq
        this.object = obj;
        this.pos = pos;
    }

    /** 
        Used for Hub add events, when an object is added to a Hub.
    */
    public HubEvent(Hub<T> source, T obj) {
        this(source, obj, -1);
    }

    public HubEvent(T obj) {
        super(obj, null, null, null);
        this.object = obj;
    }

    public HubEvent(Hub<T> source) {
        this(source, null, -1);
    }

    public T getObject() {
        return (T) object;
    }
    /**
        Position object when setting active object, adding or inserting an object, removing 
        an object.
    */
    public int getPos() {
        return pos;
    }
    
    /**
        Flag that can be used to cancel this event.
    */
    public boolean getCancel() {
        return bCancel;
    }
    /**
        Flag that can be used to cancel this event.
    */
    void setCancel(boolean b) {
        bCancel = b;
    }
    
    /**
        From position of object when moving an object.
    */
    public int getFromPos() {
        return pos;
    }
    /**
        To position used when moving an object.
    */
    public int getToPos() {
        return toPos;
    }

    
    private Object oldValue2;
    @Override
    public Object getOldValue() {
    	if (oldValue2 != null) return oldValue2;
    	Object oldObj = super.getOldValue();
    	boolean bError = false;
		if (oldObj instanceof OAObjectKey && object instanceof OAObject) {
			OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo((OAObject)object);
		    if (oi != null) {
			    OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, getPropertyName());
			    if (li != null) {
			    	oldObj = OAObjectReflectDelegate.getObject(li.getToClass(), (OAObjectKey)oldObj);
			    	oldValue2 = oldObj;
			    } else bError = true;// else error qqqqqqq
		    } else bError = true;// else error qqqqqqq
		}
//qqqqqqqqqqq
if (bError) {
	System.out.println("HubEvent.getOldValue() not finding Object for OAObjectKey: "+oldObj+", object="+object+", prop="+getPropertyName());
}
    	
		return oldObj;
    }
    
}
