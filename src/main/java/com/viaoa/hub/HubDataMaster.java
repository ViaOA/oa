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

import com.viaoa.object.*;
    
/**
    Internally used by Hub
    that is used to know the owner object of this Hub.  The owner is the object that
    was used to get this Hub.  If this Hub was created by using getDetail(), then
    the MasterHub is set.  When creating a shared Hub, this object will also be
    used for shared Hub.
    <p>
    Example: a Hub of Employee Objects can "come" from a Department Object by calling
    department.getEmployees() method.  For this, the masterObject for the employee Hub will
    be set to the Department Object.
*/
class HubDataMaster implements java.io.Serializable {
    static final long serialVersionUID = 2L;  // used for object serialization
    
    /** Only used for a Detail Hub, created by Hub.getDetail() */
    protected transient Hub masterHub;
    
    /** The object that Hub "belongs" to. */
    protected transient OAObject masterObject;
    
    /** LinkInfo from Detail (MANY) to Master (ONE).  */
    protected transient OALinkInfo liDetailToMaster;  // Note: Dont make transient: it will get replaced in resolveObject, but needs the old one to find the match

    
    public String getUniqueProperty() {
        if (liDetailToMaster == null) return null;
        OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(liDetailToMaster);
        if (rli == null) return null; 
        return rli.getUniqueProperty();
    }
    public Method getUniquePropertyGetMethod() {
        if (liDetailToMaster == null) return null;
        OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(liDetailToMaster);
        if (rli == null) return null; 
        return rli.getUniquePropertyGetMethod();
    }
    public boolean getTrackChanges() {
        return (masterObject != null) && (liDetailToMaster == null || !liDetailToMaster.getCalculated());
    }

    public String getSortProperty() {
        if (liDetailToMaster == null) return null;
        OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(liDetailToMaster);
        if (rli == null) return null; 
        return rli.getSortProperty();
    }

    public boolean isSortAsc() {
        if (liDetailToMaster == null) return false;
        OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(liDetailToMaster);
        if (rli == null) return false; 
        return rli.isSortAsc();
    }
    
    // 20141115
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();
        
        if (masterObject == null) {
            s.writeByte(0);
        }
        else {
            OAObjectKey key = null;
            OAObjectSerializer serializer = OAThreadLocalDelegate.getObjectSerializer();
            if (serializer != null) {
                Object objx = serializer.getReferenceValueToSend(masterObject);
                if (objx instanceof OAObjectKey) {
                    key = (OAObjectKey) objx;
                }
            }
            if (key != null) {
                s.writeByte(1);
                s.writeObject(masterObject.getClass());
                s.writeObject(key);
            }
            else {
                s.writeByte(2);
                s.writeObject(masterObject);
            }
            s.writeObject(liDetailToMaster==null?null:liDetailToMaster.getReverseName());
        }
    }    
    
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        byte bx = s.readByte();
        if (bx != 0) {
            if (bx == 1) {
                Class cx = (Class) s.readObject();
                OAObjectKey key = (OAObjectKey) s.readObject();
                this.masterObject = (OAObject) OAObjectCacheDelegate.get(cx, key);
if (masterObject == null) {
    System.out.println("Error: HubDataMaster object not found.  Class="+cx+", key="+key);
}
            }
            else if (bx == 2) {
                this.masterObject = (OAObject) s.readObject();
            }
            String revName = (String) s.readObject();
            if (revName != null) {
                OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(masterObject.getClass());
                OALinkInfo li = oi.getLinkInfo(revName);
                if (li != null) {
                    li = OAObjectInfoDelegate.getReverseLinkInfo(li);
                    this.liDetailToMaster = li;
                }
            }
        }
    }
    
}

