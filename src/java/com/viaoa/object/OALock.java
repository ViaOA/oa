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
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.object;

import java.util.*;
import java.lang.ref.*;  // java1.2

import com.viaoa.cs.*;

/** 
    OALock is used for setting and sharing locks on Objects.  
    <p>
    Note: setting a lock does not restrict access to an Object, it only serves as 
    a flag.  It is currently the applications responsiblity to enforce rules based on 
    a lock being set.
    <p>
    Note: this also works with OASync (Clients/Server) to create distributed locks.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OALock implements java.io.Serializable {
    static final long serialVersionUID = 1L;
    protected Object object;
    protected transient WeakReference ref;
    protected Object miscObject;
    protected int waitCnt;

    /** 
        Used for creating a lock on an object.
        @param object to lock
        @param refObject reference object used with a WeakReference.  
            If it is garbage collected, then the lock is removed. 
        @param miscObject object to store with locked object
    */
    protected OALock(Object object, Object refObject, Object miscObject) {
        if (object == null) throw new IllegalArgumentException("object can not be null");
        this.object = object;
        if (refObject != null) ref = new WeakReference(refObject);
        this.miscObject = miscObject;
    }
    
    public Object getObject() {
        return object;
    }
    
    public Object getReferenceObject() {
        if (ref == null) return null;
        return ref.get();
    }
    
    public Object getMiscObject() {
        return miscObject;
    }
}


