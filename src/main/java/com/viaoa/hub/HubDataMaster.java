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
    private transient volatile Hub masterHub;

    /** The object that Hub "belongs" to. */
    private transient volatile OAObject masterObject;
    
    public Hub getMasterHub() {
        return this.masterHub;
    }
    public void setMasterHub(Hub h) {
        this.masterHub = h;
    }
    
    public void setMasterObject(OAObject obj) {
        this.masterObject = obj;
    }
    public OAObject getMasterObject() {
        return this.masterObject;
    }

    
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
        
        if (liDetailToMaster.getCalculated()) {
            return false;
        }

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
    // 201607 dont need to write masterobject or linkinfo
    //    OAObjectPropertyDelegate.setProperty will do this
    private void writeObject(java.io.ObjectOutputStream s) throws java.io.IOException{
        s.defaultWriteObject();
        s.writeByte(0);
    }    
    
    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        s.defaultReadObject();
        byte bx = s.readByte();
    }
}

