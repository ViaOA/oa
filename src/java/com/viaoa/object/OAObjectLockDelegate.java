package com.viaoa.object;


import java.util.*;
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
public class OAObjectLockDelegate {
    
    /** 
	    Used to set a lock on an Object.
	    @see #lock(Object,Object,Object) lock
	*/
	public static void lock(Object object) {
	    lock(object,null,null);
	}
	
	/** 
	    Used to set a lock on an Object.
	    @see #lock(Object,Object,Object) lock
	*/
	public static void lock(Object object, Object refObject) {
	    lock(object, refObject, null);
	}    
	
	/** 
	    Checks to see if an object is locked.  If it is locked, then it will wait for it to be
	    released.  An OALock is then created and added to lock table.
	    @see OALock#OALock(Object,Object,Object) OALock()
	*/
	public static void lock(Object object, Object refObject, Object miscObject) {
	    if (object == null) throw new IllegalArgumentException("object can not be null");
	
	    if (OAObjectCSDelegate.isWorkstation()) {
	    	OAObjectCSDelegate.lock(object, refObject, miscObject);
	    	return;
	    }
	            
	    OALock newLock = new OALock(object, refObject, miscObject);
	
	    synchronized (OAObjectHashDelegate.hashLock) {
	        for (;;) {
	            OALock lock = getLock(object);
	            if (lock == null) break;
	            try {
	                lock.waitCnt++;
	                System.out.println("OALock.lock.wait");//qqqqqq
	                OAObjectHashDelegate.hashLock.wait();
	                System.out.println("OALock.lock.wokeup");//qqqqqq
	            }
	            catch (InterruptedException e) {
	            }
	        }
	        OAObjectHashDelegate.hashLock.put(object, newLock);
	    }
	}
	
	/** 
	    Number of other processes waiting to lock an object 
	*/
	public static int getWaitCount(Object object) {
	    if (object == null) throw new IllegalArgumentException("object can not be null");

	    if (OAObjectCSDelegate.isWorkstation()) {
	    	return OAObjectCSDelegate.getLockWaitCount(object);
	    }
	    
	    synchronized (OAObjectHashDelegate.hashLock) {
	        OALock lock = getLock(object);
	        if (lock == null) return 0;
	        return lock.waitCnt;
	    }
	}
	
	
	
	/** 
	    Removes lock from table.
	    @param object to release
	*/
	public static void unlock(Object object) {
	    if (object == null) return;

	    if (OAObjectCSDelegate.isWorkstation()) {
	    	OAObjectCSDelegate.unlock(object);
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
	public static boolean isLocked(Object object) {
	    if (object == null) return false;
	    
	    if (OAObjectCSDelegate.isWorkstation()) {
	    	return OAObjectCSDelegate.isLocked(object);
	    }
	    
	    return (getLock(object) != null);
	}
	
	/** 
	    returns the OALock object that is being used to lock an object.  This is nonblocking. 
	    @see O#OALock(Object,Object,Object) OALock()
	*/
	public static OALock getLock(Object object) {
	    if (object == null) throw new IllegalArgumentException("object can not be null");

	    if (OAObjectCSDelegate.isWorkstation()) {
	    	return OAObjectCSDelegate.getLock(object);
	    }
	    
	    OALock lock;
	    synchronized (OAObjectHashDelegate.hashLock) {
	        lock = (OALock) OAObjectHashDelegate.hashLock.get(object);
	        if (lock != null) {
	            if (lock.ref != null) {
	                // might have expired
	                if (lock.ref.get() == null) {
	                	OAObjectHashDelegate.hashLock.remove(object);
	                    lock = null;
	                }
	            }
	        }
	    }
	    return lock;
	}
	
	/** 
	    Returns all OALock objects that are locked.
	*/
	public static Object[] getAllLockedObjects() {
	
	    if (OAObjectCSDelegate.isWorkstation()) {
	    	return OAObjectCSDelegate.getAllLockedObjects();
	    }
	    
	    Object[] objs;
	    synchronized (OAObjectHashDelegate.hashLock) {
	        objs = new Object[OAObjectHashDelegate.hashLock.size()];
	        Enumeration enumx = OAObjectHashDelegate.hashLock.elements();
	        for (int i=0; enumx.hasMoreElements(); i++) {
	            objs[i] = ((OALock)enumx.nextElement()).object;
	        }
	    }
	    return objs;
	}
    
}


