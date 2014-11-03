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


import java.util.*;
import java.lang.ref.*;  // java1.2

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
	
	    RemoteSessionInterface rc = OASyncDelegate.getRemoteSession();
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

        RemoteSessionInterface rc = OASyncDelegate.getRemoteSession();
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

        RemoteSessionInterface rc = OASyncDelegate.getRemoteSession();
        if (rc != null) {
            return rc.isLocked(object.getClass(), object.getObjectKey());
        }
        synchronized (OAObjectHashDelegate.hashLock) {
            return (OAObjectHashDelegate.hashLock.get(object) != null);
        }
        
	}
	
    
}


