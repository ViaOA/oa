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


import java.util.*;
import java.lang.ref.*;  // java1.2

import com.viaoa.sync.OASync;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.remote.RemoteSessionInterface;


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
public class OAObjectLockDelegate {
    
    /** 
	    Used to set a lock on an Object.
	    @see #lock(Object,Object,Object) lock
	*/
	public static void lock(OAObject object) {
	    if (object == null) throw new IllegalArgumentException("object can not be null");
	
	    RemoteSessionInterface rc = OASync.getRemoteSession(object.getClass());
	    if (rc != null) {
	        rc.setLock(object.getClass(), object.getObjectKey(), true);
	    	return;
	    }
	            
	    OALock newLock = new OALock(object, null, null);
	    synchronized (OAObjectHashDelegate.hashLock) {
	        for (;;) {
	            OALock lock = (OALock) OAObjectHashDelegate.hashLock.get(object);
	            if (lock == null) break;
	            try {
	                lock.waitCnt++;
	                OAObjectHashDelegate.hashLock.wait();
	            }
	            catch (InterruptedException e) {
	            }
	        }
	        OAObjectHashDelegate.hashLock.put(object, newLock);
	    }
	}
	
	/** 
	    Removes lock from table.
	    @param object to release
	*/
	public static void unlock(OAObject object) {
	    if (object == null) return;

        RemoteSessionInterface rc = OASync.getRemoteSession(object.getClass());
        if (rc != null) {
            rc.setLock(object.getClass(), object.getObjectKey(), false);
            return;
        }
	    
	    synchronized (OAObjectHashDelegate.hashLock) {
	    	OAObjectHashDelegate.hashLock.remove(object);
	    	OAObjectHashDelegate.hashLock.notifyAll();
	    }
	}
	
	/** 
	    Used to check to see if an object is locked. This is nonblocking. 
	*/
	public static boolean isLocked(OAObject object) {
	    if (object == null) return false;

        RemoteSessionInterface rc = OASyncDelegate.getRemoteSession(object.getClass());
        if (rc != null) {
            return rc.isLocked(object.getClass(), object.getObjectKey());
        }
        synchronized (OAObjectHashDelegate.hashLock) {
            return (OAObjectHashDelegate.hashLock.get(object) != null);
        }
        
	}
	
    
}


