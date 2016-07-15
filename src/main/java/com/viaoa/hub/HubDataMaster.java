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
    protected transient volatile Hub masterHub;
    
    /** The object that Hub "belongs" to. */
    protected transient volatile OAObject masterObject;
    
    /** LinkInfo from Detail (MANY) to Master (ONE).  */
    protected transient volatile OALinkInfo liDetailToMaster; 
    
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
    /**
     * True if there is a masterObject and it is not a calculated Hub.
     */
    public boolean getTrackChanges() {
        if (masterObject == null) return false;
        
        // 20160505 change to false.  ex: ServerRoot.hubUsers (calc/merged)
        if (liDetailToMaster == null) {
            return false;
        }
        //was:  if (liDetailToMaster == null) return true;
        
        if (liDetailToMaster.getCalculated()) return false;

        // 20160623 so that serverRoot wont store changes to objects
        if (!liDetailToMaster.getToObjectInfo().getUseDataSource()) return false;
        
        // 20160505 check to see if rev li is calc.
        OALinkInfo liRev = liDetailToMaster.getReverseLinkInfo();
        if (liRev != null && liRev.getCalculated()) {
            return false;
        }
        
        return true;
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

    public String getSeqProperty() {
        if (liDetailToMaster == null) return null;
        OALinkInfo rli = OAObjectInfoDelegate.getReverseLinkInfo(liDetailToMaster);
        if (rli == null) return null; 
        return rli.getSeqProperty();
    }
    
    
    // 20141125 custom writer so that linkInfo is not written, and so masterObject can use key instead             
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();
        
        if (masterObject == null) {
            s.writeByte(0);
        }
        else if (true) {
//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqq
            // 20160715 try without sending master info.  It might be needed, so just testing for now
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
            Class cx = null;
            if (bx == 1) {
                cx = (Class) s.readObject();
                OAObjectKey key = (OAObjectKey) s.readObject();
                this.masterObject = (OAObject) OAObjectCacheDelegate.get(cx, key);
            }
            else if (bx == 2) {
                this.masterObject = (OAObject) s.readObject();
                if (masterObject != null) cx = masterObject.getClass();
            }
            
            String revName = (String) s.readObject();
            if (revName != null && cx != null) {
                OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(cx);
                OALinkInfo li = oi.getLinkInfo(revName);
                if (li != null) {
                    li = OAObjectInfoDelegate.getReverseLinkInfo(li);
                    this.liDetailToMaster = li;
                }
            }
        }
    }
}

