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
package com.viaoa.object;

import java.lang.ref.*;  // java1.2


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


