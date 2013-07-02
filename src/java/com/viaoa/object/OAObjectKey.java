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
package com.viaoa.object;

import java.io.*;
import com.viaoa.hub.*;
import com.viaoa.ds.*;

    
/** 
    Used to represent the objectId properties of an OAObject.  
    <br>
    It is used as the key when storing objects in a hashtable.<br>
    If the object key property is assigned it will be used, otherwise the guid is used for the hash code.
    <p>
    OAObjectKey overwrites equals() to work the following way:<br>  
    if guid is equal then objects are equal.  If property key is equal,
    then objects are equal, unless either of the objects being compared is new.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAObjectKey implements Serializable, Comparable {
    static final long serialVersionUID = 1L;
    private Object[] objectIds;  // cant be changed, it will affect hashCode
    protected int hc = -1;
    protected boolean bNew; // if new and objectId is unassigned
    protected int guid = 0;  // changed to object's guid (if object not used, then it stays as 0)
    protected boolean bEmpty;
    

    /*
    public OAObjectKey(Object[] ids) {
        setIds(ids);
        bEmpty = isEmpty();
    }
    */
    
    public OAObjectKey(Object... ids) {
        setIds(ids);
        bEmpty = isEmpty();
    }
    
    public OAObjectKey(Object[] ids, int guid, boolean bNew) {
    	setIds(ids);
    	this.guid = guid;
    	this.bNew = bNew;
        bEmpty = isEmpty();
    }
    
    public OAObjectKey(OAObject obj) {
        setIds(OAObjectInfoDelegate.getPropertyIdValues(obj));
        guid = obj.guid;
        bNew = obj.getNew();
        bEmpty = isEmpty();
    }

    private void setIds(Object[] ids) {
    	if (ids == null) return;
        for (int i=0; i<ids.length; i++) {
            if (ids[i] instanceof OAObject) {
            	ids[i] = OAObjectKeyDelegate.getKey((OAObject) ids[i]);
            }            	
        }
        this.objectIds = ids;
    }
    
    public int getGuid() {
    	return guid;
    }
    
    /** create a key for an object that has a parent.  Example: an Order that has/owns OrderItems: an OAObjectKey can
        be created for an OrderItem: new OAObjectKey(order, 1).  This can be used to then find the OrderItem using a 
        OADataSource.getObject(OrderItem.class, key) or HubController.get(OrderItem.class, key)
    */
    public OAObjectKey(OAObject parent, Object value) {
        this(new Object[] { parent, value});
    }

    public OAObjectKey(Object value) {
        this(new Object[] { value});
    }
    public OAObjectKey(int value) {
        this(new Object[] { new Integer(value)});
    }
    public OAObjectKey(long value) {
        this(new Object[] { new Long(value)});
    }
    public OAObjectKey(String value) {
        this(new Object[] {value});
    }

    public int hashCode() {
        if (hc == -1) {
            if (bEmpty) {
                if (guid != 0) hc = guid;
                else hc = super.hashCode();
            }
            else {
                hc = 0;
                for (int i=0; objectIds != null && i<objectIds.length; i++) {
                    if (objectIds[i] != null) hc += objectIds[i].hashCode();
                }
            }
        }
        return hc;
    }

    public boolean exactEquals(OAObjectKey key) {
        if (!equals(key)) return false;
        if (key.bNew != this.bNew) return false;
        if (key.guid != this.guid) return false;
        return true;
    }

    public boolean equals(Object obj) {
        // If object id(s) are null, then the guid is used for comparing.
        if (obj == null) return bEmpty;
        if (obj instanceof OAObject) obj = OAObjectKeyDelegate.getKey((OAObject)obj);
        if (obj == this) return true;

        if (obj instanceof OAObjectKey) {
        	OAObjectKey ok = (OAObjectKey) obj;
        	
        	if (this.bEmpty) {
                if (ok.bEmpty && ((ok.guid == 0 || this.guid == 0) || (ok.guid == this.guid))) {
                	return true;
                }
                return false;
            }
            else {
                if (ok.bEmpty) return false;
            }

            int x = objectIds.length;
            if (x != ok.objectIds.length) return false;
            if (x == 0) return (this.guid == ok.guid);

            for (int j=0; j<x; j++) {
                if (objectIds[j] == ok.objectIds[j]) continue;
                if (objectIds[j] == null || ok.objectIds[j] == null) return false;
                if (!objectIds[j].equals(ok.objectIds[j])) return false;
            }
            return true;
        }
        if (this.bEmpty) return (obj == null);
        if (objectIds.length == 1) {
            if (objectIds[0] != null && objectIds[0].equals(obj)) return true;
        }
        return false;
    }

	public int compareTo(Object obj) {
	    // If object id(s) are null, then the guid is used for comparing.
        if (obj == null) {
        	if (this.bEmpty) return 0;
        	return 1;
        }
        if (obj instanceof OAObject) obj = OAObjectKeyDelegate.getKey((OAObject)obj);
        if (obj == this) return 0;

        if (obj instanceof OAObjectKey) {
            OAObjectKey ok = (OAObjectKey) obj;
            if (this.bEmpty) {
                if (!ok.bEmpty) return -1;
                if (this.guid == 0 || ok.guid == 0) return 0;
                if (this.guid == ok.guid) return 0;
                if (this.guid > ok.guid) return 1;
                return -1;
            }
            else {
                if (ok.bEmpty) return 1;
            }

            int x = this.objectIds.length;
            int x2 = ok.objectIds.length;
            for (int j=0; j<x; j++) {
            	if (j == x2) return 1;
            	int cmp = compare(this.objectIds[j], ok.objectIds[j]);
            	if (cmp != 0) return cmp;
            }
            if (x2 > x) return -1;
            return 0;
        }
        
        if (bEmpty) return -1;
        if (this.objectIds.length == 0) return -1;
        if (this.objectIds[0] == null) return -1;

        int cmp = compare(objectIds[0], obj);
        if (cmp != 0) return cmp;
        
        if (objectIds.length > 1) return 1;
        return 0;
	}
	
	private int compare(Object obj1, Object obj2) {
        if (obj1 == null) {
        	if (obj2 == null) return 0;
        	else return -1;
        }
    	if (obj2 == null) return 1;
        if (obj1 instanceof Number && obj2 instanceof Number) {
            double d1 = ((Number)obj1).doubleValue();
            double d2 = ((Number)obj2).doubleValue();
        	if (d1 == d2) return 0;
        	if (d1 > d2) return 1;
        	return -1;
        }
        String s1, s2;
        if (obj1 instanceof String) s1 = (String) obj1;
        else s1 = obj1.toString();
        
        if (obj2 instanceof String) s2 = (String) obj2;
        else s2 = obj2.toString();
        return s1.compareTo(s2);
	}
	
    
    private boolean isEmpty() {
        if (objectIds == null) return true;
        for (int i=0; i<objectIds.length; i++) {
            if (objectIds[i] != null) return false;
        }
        return true;
    }

    public Object[] getObjectIds() {
        return this.objectIds;
    }

    public String toString() {
        String s = null;
        for (int i=0; objectIds != null && i < objectIds.length; i++) {
            if (s == null) s = "" + objectIds[i];
            else s += " " + objectIds[i];
        }
        if (s == null) {
            s = "new.guid=" + guid;
        }
        return s;
    }

}

