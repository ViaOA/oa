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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.*;

import com.viaoa.remote.multiplexer.OARemoteThread;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.OASyncClient;
import com.viaoa.hub.Hub;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.transaction.OATransaction;
import com.viaoa.util.OAArray;
import com.viaoa.util.OADateTime;
import com.viaoa.util.Tuple;

/**
 * Delegate class used to store information about the local thread. 
 * This is used internally throughout OA to set specific features for a thread.
 * 
 * Note: it is important to make sure to call the corresponding reverse value, so that the flags and counters will be unset and 
 * the Thread will be removed from internal map.
 * @author vvia
 */
public class OAThreadLocalDelegate {

	private static Logger LOG = Logger.getLogger(OAThreadLocalDelegate.class.getName());
	
	private static final ThreadLocal<OAThreadLocal> threadLocal = new ThreadLocal<OAThreadLocal>();
	
	private static AtomicInteger TotalIsLoadingObject = new AtomicInteger();
    private static AtomicInteger TotalIsAssigningObjectKey = new AtomicInteger();
	private static AtomicInteger TotalObjectCacheAddMode = new AtomicInteger();
	private static AtomicInteger TotalObjectSerializer = new AtomicInteger();
	private static AtomicInteger TotalSkipObjectInitialize = new AtomicInteger();
	private static AtomicInteger TotalSuppressCSMessages = new AtomicInteger();
	private static AtomicInteger TotalSkipFirePropertyChange = new AtomicInteger();
	private static AtomicInteger TotalDelete = new AtomicInteger();
    private static AtomicInteger TotalTransaction = new AtomicInteger();
    private static AtomicInteger TotalCaptureUndoablePropertyChanges = new AtomicInteger();
    private static AtomicInteger TotalHubMergerChanging = new AtomicInteger();
    private static AtomicInteger TotalIsSendingEvent = new AtomicInteger(); // used to manage calcPropertyChanges while another event(s) is being processed
    private static AtomicInteger TotalHubListenerTreeCount = new AtomicInteger();
    private static AtomicInteger TotalGetDetailHub = new AtomicInteger();
//qqq    private static AtomicInteger TotalRunnable = new AtomicInteger();
    private static AtomicInteger TotalRemoteMultiplexerClient = new AtomicInteger();
    private static AtomicInteger TotalNotifyWaitingObject = new AtomicInteger();

    public static final HashMap<Object, OAThreadLocal[]> hmLock = new HashMap<Object, OAThreadLocal[]>(53, .75f);
    
    
	protected static OAThreadLocal getThreadLocal(boolean bCreateIfNull) {
		OAThreadLocal ti = threadLocal.get();
		if (ti == null && bCreateIfNull) {
			ti = new OAThreadLocal();
			ti.time = System.currentTimeMillis();
			threadLocal.set(ti);
			// LOG.finest("new OAThreadLocal created");
		}
		return ti;
	}
	
	// Transaction -------------------
    private static long msTransaction;
	/**
	 * Called by OATransaction.start() 
	 * Used internally by classes that work with the transaction.
	 * Use OATransaction instead of calling directly.
	 */
	public static void setTransaction(OATransaction t) {
	    OAThreadLocal ti = getThreadLocal(true);
	    ti.transaction = t;
	    int x;
	    if (t != null) {
	        x = TotalTransaction.incrementAndGet();
	    }
	    else {
	        x = TotalTransaction.decrementAndGet();
	    }
        if (x > 7 || x < 0) {
            msTransaction = throttleLOG("TotalTransaction ="+x, msTransaction);
        }
	}
	

	
	
	public static OATransaction getTransaction() {
	    if (TotalTransaction.get() == 0) return null; 
        OAThreadLocal ti = getThreadLocal(false);
	    if (ti == null) return null;
	    return ti.transaction;
	}
	
	// Loading -----------------------
	public static boolean isLoadingObject() {
		boolean b; 
		if (OAThreadLocalDelegate.TotalIsLoadingObject.get() == 0) {
			// LOG.finest("fast");
			b = false;
		}
		else {
			b = isLoadingObject(OAThreadLocalDelegate.getThreadLocal(false));
			// LOG.finest(""+b);
		}
		return b;
	}
    protected static boolean isLoadingObject(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.loadingObject > 0;
    }
	public static void setLoadingObject(boolean b) {
		// LOG.finer(""+b);
		setLoadingObject(OAThreadLocalDelegate.getThreadLocal(b), b);
	}
    private static long msLoadingObject;
	protected static void setLoadingObject(OAThreadLocal ti, boolean b) {
		if (ti == null) return;
		int x,x2;
		if (b) {
			x = ++ti.loadingObject;
			x2 = OAThreadLocalDelegate.TotalIsLoadingObject.getAndIncrement();
		}
		else {
    		x = --ti.loadingObject;
    		x2 = OAThreadLocalDelegate.TotalIsLoadingObject.decrementAndGet();
		}
        if (x > 50 || x < 0 || x2 > 50 || x2 < 0) {
            msLoadingObject = throttleLOG("TotalIsLoadingObject="+x2+", ti="+x, msLoadingObject);
        }
	}
	
	
	// CacheAddMode ----------------------
	public static int getObjectCacheAddMode() {
		int mode;
		if (OAThreadLocalDelegate.TotalObjectCacheAddMode.get() == 0) {
			mode = OAObjectCacheDelegate.DefaultAddMode;
			// LOG.finest("fast");
		}
		else {
			mode = getObjectCacheAddMode(OAThreadLocalDelegate.getThreadLocal(false));
			// LOG.finest(""+mode);
		}
		return mode;
	}
    private static long msObjectCacheAddMode;
	public static void setObjectCacheAddMode(int mode) {
		// LOG.finer("mode="+mode);
		if (mode == OAObjectCacheDelegate.DefaultAddMode) mode = 0;
		OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(mode != 0);
		if (ti == null) return;

		int old = ti.cacheAddMode;
		if (old == mode) return;  // no change
		ti.cacheAddMode = mode;

		if (old == 0 || mode == 0) {  // dont update total if it has already been called for this ti
			if (mode == 0) {
				if (OAThreadLocalDelegate.TotalObjectCacheAddMode.get() > 0) OAThreadLocalDelegate.TotalObjectCacheAddMode.decrementAndGet();
			}
			else {
			    int x = OAThreadLocalDelegate.TotalObjectCacheAddMode.incrementAndGet();
	            if (x > 15) {
	                msObjectCacheAddMode = throttleLOG("TotalObjectCacheAddMode ="+x, msObjectCacheAddMode);
	            }
			}
		} 
	}

	protected static int getObjectCacheAddMode(OAThreadLocal ti) {
		if (ti == null) return OAObjectCacheDelegate.DefaultAddMode;
		if (ti.cacheAddMode == 0) return OAObjectCacheDelegate.DefaultAddMode;
		return ti.cacheAddMode;
	}
	
	// OAObjectSerializeInterface ---------------
	/** used by Serialization for the current thread.  OAObjectSerializeInterface is called to return
	    the type of serialization to perform.   SERIALIZE_STRIP_NONE or SERIALIZE_STRIP_REFERENCES
	*/
	public static OAObjectSerializer getObjectSerializer() {
		OAObjectSerializer si;
		if (OAThreadLocalDelegate.TotalObjectSerializer.get() == 0) {
			si = null; 
			// LOG.finest("fast");
		}
		else {
			si = getObjectSerializer(OAThreadLocalDelegate.getThreadLocal(false));
			// LOG.finest("OAObjectSerializer="+(si != null));
		}
		return si;
	}
    protected static OAObjectSerializer getObjectSerializer(OAThreadLocal ti) {
        if (ti == null) return null;
        return ti.objectSerializer;
    }
	public static void setObjectSerializer(OAObjectSerializer si) {
		// LOG.finer("OAObjectSerializer="+(si != null));
		setObjectSerializer(OAThreadLocalDelegate.getThreadLocal(si != null), si);
	}
    private static long msObjectSerializer;
	protected static void setObjectSerializer(OAThreadLocal ti, OAObjectSerializer si) {
	    if (ti == null) return;
		if (ti.objectSerializer == si) return;
		OAObjectSerializer old = ti.objectSerializer;
		if (si == old) return; // no change
		ti.objectSerializer = si;

		if (old == null || si == null) {  // dont update total if it has already been called for this ti
		    int x;
		    if (si != null) {
		        x = OAThreadLocalDelegate.TotalObjectSerializer.incrementAndGet();
		    }
			else {
			    x = OAThreadLocalDelegate.TotalObjectSerializer.decrementAndGet();
			}
            if (x > 15 || x < 0) {
                msObjectSerializer = throttleLOG("TotalObjectSerializeInterface ="+x, msObjectSerializer);
            }
		}
	}
	
	
	/**
	 * Convenience method, used by DataSource when loading OAObjects, so that OAObject.initialize is skipped,
	 * no property changes are sent, and object loading flag is set. 
	 * @param b true if object is being loaded, false when done.
	 */
	public static void setDataSourceLoadingObject(boolean b) {
        OAThreadLocalDelegate.setSkipObjectInitialize(b);
        OAThreadLocalDelegate.setSkipFirePropertyChange(b); // qqqq change: need to be on for "dirty" reads ?, turned off for news  qqqqqqqqqqq
        OAThreadLocalDelegate.setLoadingObject(b);  // object will not send events, wont be added to cache (will be added manually)
	}
	
	
	// SkipInitialize -----------------------
	public static boolean isSkipObjectInitialize() {
		boolean b;
		if (OAThreadLocalDelegate.TotalSkipObjectInitialize.get() == 0) {
			// LOG.finest("fast");
			b = false;
		}
		else {
			b = isSkipObjectInitialize(OAThreadLocalDelegate.getThreadLocal(false));
			// LOG.finest(""+b);
		}
		return b;
	}
	public static boolean isSkipObjectInitialize(OAThreadLocal ti) {
		if (ti == null) return false;
		return ti.skipObjectInitialize > 0;
	}
    public static void setSkipObjectInitialize(boolean b) {
        // LOG.finer(""+b);
        setSkipObjectInitialize(OAThreadLocalDelegate.getThreadLocal(b), b);
    }
    private static long msSkipObjectInitialize;
	public static void setSkipObjectInitialize(OAThreadLocal ti, boolean b) {
		if (ti == null) return;
		int x, x2;
		if (b) {
			x = ++ti.skipObjectInitialize;
            x2 = OAThreadLocalDelegate.TotalSkipObjectInitialize.incrementAndGet();
		}
		else {
            x = --ti.skipObjectInitialize;
			x2 = OAThreadLocalDelegate.TotalSkipObjectInitialize.decrementAndGet();
		}
        if (x > 10 || x < 0 || x2 > 15 || x2 < 0) {
            msSkipObjectInitialize = throttleLOG("TotalSkipInitialize ="+x2+", ti="+x, msSkipObjectInitialize);
        }
	}
	


	// SuppressCSMessages -----------------------
	public static boolean isSuppressCSMessages() {
		boolean b;
		if (OAThreadLocalDelegate.TotalSuppressCSMessages.get() == 0) {
			// LOG.finest("fast");
			b = false;
		}
		else {
			b = isSuppressCSMessages(OAThreadLocalDelegate.getThreadLocal(false));
			// LOG.finest(""+b);
		}
		return b;
	}
    public static boolean isSuppressCSMessages(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.suppressCSMessages > 0;
    }
	public static void setSuppressCSMessages(boolean b) {
		// LOG.finest(""+b);
		setSuppressCSMessages(OAThreadLocalDelegate.getThreadLocal(b), b);
	}
    private static long msSuppressCSMessages;
	public static void setSuppressCSMessages(OAThreadLocal ti, boolean b) {
		if (ti == null) return;
		int x, x2;
		if (b) {
			x = ++ti.suppressCSMessages;
			x2 = OAThreadLocalDelegate.TotalSuppressCSMessages.incrementAndGet();
		}
		else {
			x = --ti.suppressCSMessages;
			x2 = OAThreadLocalDelegate.TotalSuppressCSMessages.decrementAndGet();
		}
        if (x > 30 || x < 0 || x2 > 50 || x2 < 0) {
            msSuppressCSMessages = throttleLOG("TotalSuppressCSMessages ="+x2+", ti="+x, msSuppressCSMessages);
        }
	}
	
	// SuppressFirePropertyChange -----------------------
	public static boolean isSkipFirePropertyChange() {
		boolean b;
		if (OAThreadLocalDelegate.TotalSkipFirePropertyChange.get() == 0) {
			b = false;
			// LOG.finest("fast");
		}
		else {
			b = isSkipFirePropertyChange(OAThreadLocalDelegate.getThreadLocal(false));
			// LOG.finest(""+b);
		}
		return b;
	}
    public static boolean isSkipFirePropertyChange(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.skipFirePropertyChange > 0;
    }
	public static void setSkipFirePropertyChange(boolean b) {
		// LOG.finer(""+b);
		setSkipFirePropertyChange(OAThreadLocalDelegate.getThreadLocal(b), b);
	}
    private static long msSkipFirePropertyChange;
	public static void setSkipFirePropertyChange(OAThreadLocal ti, boolean b) {
		if (ti == null) return;
		int x, x2;
		if (b) {
			x = ++ti.skipFirePropertyChange;
			x2 = OAThreadLocalDelegate.TotalSkipFirePropertyChange.incrementAndGet();
		}
		else {
			x = --ti.skipFirePropertyChange;
			x2 = OAThreadLocalDelegate.TotalSkipFirePropertyChange.decrementAndGet();
		}
        if (x > 10 || x < 0 || x2 > 15 || x2 < 0) {
            msSkipFirePropertyChange = throttleLOG("SkipFirePropertyChange ="+x2+", ti="+x, msSkipFirePropertyChange);
        }
	}

	
	

	
	// Deleting -----------------------
    
    private static Vector vecDeleting = new Vector(10);
	/**
	 * Is this thread currently deleting.
	 */
	public static boolean isDeleting() {
        if (OAThreadLocalDelegate.TotalDelete.get() == 0) return false;
        OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(false);
        if (ti == null) return false;
        return ti.deleting != null && ti.deleting.length > 0;
    }
	/**
	 *  Is any thread currently deleting an object.
	 */
	public static boolean isDeleting(Object obj) {
		if (obj == null) return false;
	    return vecDeleting.contains(obj);
	}

	/**
     *  Is this thread currently deleting an object/hub.
     */
    public static boolean isThreadDeleting(Object obj) {
        if (obj == null) return false;
        if (OAThreadLocalDelegate.TotalDelete.get() == 0) {
            // LOG.finest("fast");
            return false;
        }
        
        if (!vecDeleting.contains(obj)) return false;
        
        boolean b = isDeleting(OAThreadLocalDelegate.getThreadLocal(false), obj);
        // LOG.finest(""+b);
        
        return b;
    }
	
	protected static boolean isDeleting(OAThreadLocal ti, Object obj) {
        if (obj == null) return false;
		if (ti == null || ti.deleting == null) return false;
		int x = ti.deleting.length;
		if (x == 0) return false;
		for (int i=0; i<x; i++) if (ti.deleting[i] == obj) return true;
		return false;
	}
    private static long msDeleting;
	public static void setDeleting(Object obj, boolean b) {
		// LOG.finer(""+b);
        if (obj == null) return;

		if (b) {
		    vecDeleting.add(obj);
	        if (vecDeleting.size() > 25) {
	            msDeleting = throttleLOG("TotalDeleting ="+vecDeleting.size(), msDeleting);
	        }
		}
        else vecDeleting.remove(obj);

		setDeleting(OAThreadLocalDelegate.getThreadLocal(b), obj, b);
	}
	protected static void setDeleting(OAThreadLocal ti, Object obj, boolean b) {
		if (ti == null) return;
        if (obj == null) return;
		if (b) {
			if (ti.deleting == null) ti.deleting = new Object[1];
			int x = ti.deleting.length;
			for (int i=0; ;i++) {
				if (i == x) {
					Object[] objs = new Object[x+3];
					System.arraycopy(ti.deleting, 0, objs, 0, x);
					ti.deleting = objs;
					ti.deleting[x] = obj;
					break;
				}
				if (ti.deleting[i] == obj) return;
				if (ti.deleting[i] == null) {
					ti.deleting[i] = obj;
					break;
				}
			}
			x = OAThreadLocalDelegate.TotalDelete.incrementAndGet();
	        if (x > 100) {
	            msDeleting = throttleLOG("TotalDelete ="+x, msDeleting);
	        }
		}
		else {
			if (ti.deleting == null) return;
			int x = ti.deleting.length;
			boolean bAllNull = true;
			boolean bFound = false;
			for (int i=0; i<x; i++) {
				if (ti.deleting[i] == obj) {
					bFound = true;
					ti.deleting[i] = null;
				}
				else {
					if (ti.deleting[i] != null) bAllNull = false;
				}
			}
			if (bFound) OAThreadLocalDelegate.TotalDelete.decrementAndGet();
			if (bAllNull) ti.deleting = null;
		}
	}

	
    // isAssigningObjectKey -----------------------
    public static boolean isAssigningObjectKey() {
        boolean b; 
        if (OAThreadLocalDelegate.TotalIsAssigningObjectKey.get() == 0) {
            // LOG.finest("fast");
            b = false;
        }
        else {
            b = isAssigningObjectKey(OAThreadLocalDelegate.getThreadLocal(false));
            // LOG.finest(""+b);
        }
        return b;
    }
    protected static boolean isAssigningObjectKey(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.assigningObjectKey > 0;
    }
    public static void setAssigningObjectKey(boolean b) {
        // LOG.finer(""+b);
        setAssigningObjectKey(OAThreadLocalDelegate.getThreadLocal(b), b);
    }
    private static long msAssigningObjectKey;
    protected static void setAssigningObjectKey(OAThreadLocal ti, boolean b) {
        if (ti == null) return;
        int x,x2;
        if (b) {
            x = ++ti.assigningObjectKey;
            x2 = OAThreadLocalDelegate.TotalIsAssigningObjectKey.incrementAndGet();
        }
        else {
            x = --ti.assigningObjectKey;
            x2 = OAThreadLocalDelegate.TotalIsAssigningObjectKey.decrementAndGet();
        }
        if (x > 10 || x < 0 || x2 > 15 || x2 < 0) {
            msAssigningObjectKey = throttleLOG("TotalIsAssigningObjectKey ="+x2+", ti="+x, msAssigningObjectKey);
        }
    }
	
    
    /** getFlag -----------------------
     * Flag used for generic/misc purposes
     */
    public static boolean isFlag(Object obj) {
        return isFlag(OAThreadLocalDelegate.getThreadLocal(false), obj);
    }
    protected static boolean isFlag(OAThreadLocal ti, Object obj) {
        if (ti == null) return false;
        return OAArray.contains(ti.flags, obj);
    }
    
    private static long msFlag;
    public static void setFlag(Object obj) {
        setFlag(OAThreadLocalDelegate.getThreadLocal(true), obj);
    }
    protected static void setFlag(OAThreadLocal ti, Object obj) {
        if (ti == null) return;
        ti.flags = OAArray.add(Object.class, ti.flags, obj);
        if (ti.flags != null && ti.flags.length > 20) {
            msFlag = throttleLOG("OAThreadLocal.tiFlags.length ="+ti.flags.length, msFlag);
        }
    }
    
    public static void removeFlag(Object obj) {
        setFlag(OAThreadLocalDelegate.getThreadLocal(false), obj);
    }
    protected static void removeFlag(OAThreadLocal ti, Object obj) {
        if (ti == null) return;
        ti.flags = OAArray.removeValue(Object.class, ti.flags, obj);
    }
    
    
    
    // 20110104
    // Locking -----------------------
    /**
     * This locking was created to prevent deadlocks.  If a thread is waiting on an object,
     * and the thread already has a lock, then it can be allowed to also have the lock - after waiting
     * a set amount of time.
     * 
     * Each Object that is locked keeps track of the threadLocals that are using it.  The first threadLocal in the
     * array is the owner.  Once it is done (unlocked), it will notify the next threadLocal, etc.  
     * 
     * Ideally, only one threadLocal at a
     * time will have access to the Object - while the other threads wait.  If another thread already has 
     * lock(s) on other objects, then it can also be allowed to use the object - after waiting
     * a certain amount of time, and still not given the lock.  
     * 
     * @param maxWaitTries (default=10) max number of waits (each 50 ms) to wait before taking the lock - 0 to wait
     * until notified. This will only be used if the current threadLocal has 1+ locks already and object is locked
     * by another threadLocal.
     */
    public static void lock(Object object, int maxWaitTries) {
        lock(OAThreadLocalDelegate.getThreadLocal(true), object, maxWaitTries);
    }
    public static void lock(Object object) {
        OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(true);
        lock(ti, object, 2);
    }
    public static boolean hasLock() {
        OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(false);
        return (ti != null && ti.locks != null && ti.locks.length > 0);
    }
    public static boolean hasLock(Object obj) {
        OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(false);
        if (ti == null) return false;
        Object[] objs = ti.locks;
        if (objs == null) return false;
        for (Object objx :  objs) {
            if (objx == obj) return true;
        }
        return false;
    }
    public static Object[] getLocks() {
        OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(false);
        if (ti == null) return null;
        return ti.locks;
    }

    public static boolean isLocked(Object object) {
        synchronized (hmLock) {
            OAThreadLocal[] tis = hmLock.get(object); // threadLocals that are using object (locked or waiting)
            return (tis != null && tis.length > 0);
        }        
    }
    public static boolean isLockOwner(Object object) {
        OAThreadLocal ti = OAThreadLocalDelegate.getThreadLocal(false);
        if (ti == null) return false;
        synchronized (hmLock) {
            OAThreadLocal[] tis = hmLock.get(object); // threadLocals that are using object (locked or waiting)
            return (tis != null && tis.length > 0 && tis[0] == ti);
        }        
    }

    
    public static OAThreadLocal getOAThreadLocal() {
        return OAThreadLocalDelegate.getThreadLocal(true);
    }
        
    private static long timeLastStackTrace;
    private static int errorCnt;    

    // used for lock/unlock
    protected static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

static volatile int openLockCnt;    
static volatile int lockCnt;    
static volatile int unlockCnt;    
    protected static void lock(OAThreadLocal tiThis, Object thisLockObject, int maxWaitTries) {
        //System.out.println((++lockCnt)+") ****** OAThreadLocalDelegate.lock obj="+thisLockObject+", activeLocks="+(++openLockCnt));
        if (thisLockObject == null || tiThis == null) return;
        
        OARemoteThread rt = null;
        
        for (int tries=0; ;tries++) {
    
            rwLock.writeLock().lock();
            try {
                boolean b = _lock(tiThis, thisLockObject, maxWaitTries, tries);
                if (b) {
                    break;
                }
    
                // theadLocal will need to wait
                tiThis.bIsWaitingOnLock = true;
                
                if (tiThis.locks.length > 1) {
                    // need to wake up any threads that are waiting on this thread
                    releaseDeadlock(tiThis, thisLockObject);
                }
            }
            finally {
                rwLock.writeLock().unlock();
            }

            
            // wait on ThreadLocal
            synchronized (tiThis) {
                if (!tiThis.bIsWaitingOnLock) {
                    continue; // it's been notified by thread that had the lock, try again
                }

                if (tries == 0) {
                    Thread t = Thread.currentThread();
                    if (t instanceof OARemoteThread) {
                        rt = (OARemoteThread) t;
                        rt.setWaitingOnLock(true);
                    }
                }                

                int msWait;
                if (tiThis.locks != null && tiThis.locks.length > 1) {
                    msWait = 5;  // could be deadlock situation
                }
                else msWait = 25;
                
                try {
                    tiThis.wait(msWait);  // wait for wake up
                }
                catch (InterruptedException e) {
                    // System.out.println("ERRROR");                    
                }
            }
        }
        if (rt != null) rt.setWaitingOnLock(false);
    }

    // returns true if it's ok to continue, and not if wait on lock to be released
    private static boolean _lock(OAThreadLocal tlThis, Object thisLockObject, int maxWaitTries, int tries) {
        OAThreadLocal[] tls = hmLock.get(thisLockObject); // threadLocals that are using object (locked or waiting)

        if (tls != null && tls.length > 0 && tls[0] == tlThis) {
            // this ThreadLocal already is the owner for this object
            if (tries == 0) {
                // need to add it to ti.locks, since it will be released more then once
                tlThis.locks = OAArray.add(Object.class, tlThis.locks, thisLockObject);
            }
            // check locks to make sure that it is not getting too big
            if (tlThis.locks.length > 39 && (tlThis.locks.length % 10) == 0) {
                // see if all objects are still locked
                String s = "";
                for (Object objx : tlThis.locks) {
                    OAThreadLocal[] tisx = hmLock.get(objx);
                    if (tisx == null) {
                        s = ", error: there are objects in ti.locks that are no longer locked";
                    }
                }
                s = "OAThreadLocal.locks size="+tlThis.locks.length+s;
                LOG.warning(s);   
            }
            tlThis.bIsWaitingOnLock = false;
            return true; // already is the lock owner
        }
        
        if (tries == 0) {  
            // must be inside sync: add to list of objects that this TI is locking
            tlThis.locks = OAArray.add(Object.class, tlThis.locks, thisLockObject);

            if (tls == null) {
                tls = new OAThreadLocal[] {tlThis};
            }
            else tls = (OAThreadLocal[]) OAArray.add(OAThreadLocal.class, tls, tlThis);
            hmLock.put(thisLockObject, tls);
        }

        if (tls[0] == tlThis) {
            tlThis.bIsWaitingOnLock = false;
            return true; // this thread owns the lock
        }

        if (maxWaitTries > 0 && tries >= maxWaitTries && tries > 1) {
            if (tls[1] != tlThis) {
                // need to be second in list, since the owner (at pos [0]) will notify [1] when it is done - and not another threadLocal                     
                tls = (OAThreadLocal[]) OAArray.removeValue(OAThreadLocal.class, tls, tlThis);
                tls = (OAThreadLocal[]) OAArray.insert(OAThreadLocal.class, tls, tlThis, 1);
                hmLock.put(thisLockObject, tls);
            }
            tlThis.bIsWaitingOnLock = false;
            if (maxWaitTries > 2) { 
                String s = "this.thread "+Thread.currentThread().getName()+", timedout waiting for:"+thisLockObject+", locked by:" + tls[0].threadName;
                LOG.fine(s);
            }
            return true; // done trying
        }
        return false;
    }
    
    
    public static int cntDeadlock;
    public static int getDeadlockCount() {
        return cntDeadlock;
    }
    
    // this should be called with rwLock.write locked
    private static void releaseDeadlock(OAThreadLocal tiThis, Object lockObject) {
        OAThreadLocal[] tls = hmLock.get(lockObject);
        if (tls == null) return;
        OAThreadLocal tlOwner = tls[0];
        
        Object[] ownerLocks = tlOwner.locks;
        if (ownerLocks == null) return;
        
        for (Object ownerLockObj : ownerLocks) {
            if (ownerLockObj == lockObject) continue;
            tls = hmLock.get(ownerLockObj);
            if (tls == null || tls[0] != tiThis) continue; // not locked by ti

            int pos = OAArray.indexOf(tls, tlOwner);
            if (pos < 0) continue;
            tls[0] = tlOwner;
            tls[pos] = tiThis;

            if (pos != 1) {
                tls[pos] = tls[1];
                tls[1] = tiThis;
            }
            
            cntDeadlock++;
            synchronized (tlOwner) {
                tlOwner.bIsWaitingOnLock = false;
                tlOwner.notify();
            }
            
            LOG.warning("LOCK:OAThreadLocalDelegate: Found Deadlock, obj="+lockObject+", releasing one of the locks");            
            break;
        }        
    }
    public static void releaseAllLocks() {
        OAThreadLocal tl = OAThreadLocalDelegate.getThreadLocal(false);
        if (tl == null) return;
        Object[] locks = tl.locks;
        if (locks == null) return;
        for (Object obj : locks) {
            unlock(obj);
        }
    }
    
    public static void unlock(Object object) {
        unlock(OAThreadLocalDelegate.getThreadLocal(true), object);
    }


    protected static void unlock(OAThreadLocal ti, Object object) {
        //System.out.println((++unlockCnt)+") ****** OAThreadLocalDelegate.unlock obj="+object+", activeLocks="+(--openLockCnt));
        try {
            rwLock.writeLock().lock();
            _unlock(ti, object);
        }
        finally {
            rwLock.writeLock().unlock();
        }
    }    
    private static void _unlock(OAThreadLocal tl, Object object) {
        final int pos = OAArray.indexOf(tl.locks, object);
        if (pos < 0) return;
        
        final boolean bMoreLocks = OAArray.indexOf(tl.locks, object, pos+1) >= 0;
        
        OAThreadLocal[] tls = hmLock.get(object);
        if (tls != null) {
            boolean bIsLockOwner = (tls.length > 0 && tls[0] == tl);

            if (tls.length == 1) {
                if (bIsLockOwner && !bMoreLocks) {
                    hmLock.remove(object);
                }
                tls = null;
            }
            else {
                if (!bMoreLocks) {
                    tls = (OAThreadLocal[]) OAArray.removeValue(OAThreadLocal.class, tls, tl);
                    hmLock.put(object, tls);
                }
            }
            
            if (tls != null && bIsLockOwner && !bMoreLocks) {
                synchronized (tls[0]) {
                    tls[0].bIsWaitingOnLock = false;  // notify the next one waiting
                    tls[0].notify();
                }
            }
        }
        tl.locks = OAArray.removeAt(Object.class, tl.locks, pos); // must be inside sync
    }

    
    
    
    // HubListenerTree uses this to ignore dependent property changes caused by add/remove objects from hubMerger.hubMaster                                
    public static boolean isHubMergerChanging() {
        boolean b; 
        if (OAThreadLocalDelegate.TotalHubMergerChanging.get() == 0) {
            // LOG.finest("fast");
            b = false;
        }
        else {
            b = isHubMergerChanging(OAThreadLocalDelegate.getThreadLocal(false));
        }
        return b;
    }
    protected static boolean isHubMergerChanging(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.hubHubMergerIsChanging > 0;
    }
    public static void setHubMergerIsChanging(boolean b) {
        // LOG.finer(""+b);
        setHubMergerIsChanging(OAThreadLocalDelegate.getThreadLocal(b), b);
    }
    private static long msHubMergerIsChanging;
    protected static void setHubMergerIsChanging(OAThreadLocal ti, boolean b) {
        if (ti == null) return;
        int x;
        
        if (b) {
            ti.hubHubMergerIsChanging++;
            x = OAThreadLocalDelegate.TotalHubMergerChanging.getAndIncrement();
        }
        else {
            ti.hubHubMergerIsChanging--;
            x = OAThreadLocalDelegate.TotalHubMergerChanging.decrementAndGet();
        }
        if (x > 200 || x < 0) {
            msHubMergerIsChanging = throttleLOG("TotalHubMergerChanging="+x, msHubMergerIsChanging);
        }
    }

    // CaptureUndoablePropertyChanges -----------------------
    public static boolean getCreateUndoablePropertyChanges() {
        boolean b; 
        if (OAThreadLocalDelegate.TotalCaptureUndoablePropertyChanges.get() == 0) {
            // LOG.finest("fast");
            b = false;
        }
        else {
            b = getCreateUndoablePropertyChanges(OAThreadLocalDelegate.getThreadLocal(false));
            // LOG.finest(""+b);
        }
        return b;
    }
    protected static boolean getCreateUndoablePropertyChanges(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.createUndoablePropertyChanges;
    }


//qqqqqqqqqq add support for other undoables, like:  hub.add, .remove, etc.    
    
    
    
    public static void startUndoable(String compoundName) {
        startUndoable(OAThreadLocalDelegate.getThreadLocal(true), compoundName);
    }
    private static long msUndoable;
    protected static void startUndoable(OAThreadLocal ti, String compoundName) {
        if (ti == null) return;
        if (compoundName == null) compoundName = "changes";
        ti.createUndoablePropertyChanges = true;
        ti.compoundUndoableName = compoundName;
        OAUndoManager.startCompoundEdit(compoundName);

        int x = OAThreadLocalDelegate.TotalCaptureUndoablePropertyChanges.getAndIncrement();
        if (x > 50 || x < 0) {
            msUndoable = throttleLOG("TotalCaptureUndoablePropertyChanges="+x+", ti.createUndoablePropertyChanges="+ti.createUndoablePropertyChanges, msUndoable);
        }
    }
    public static void endUndoable() {
        endUndoable(OAThreadLocalDelegate.getThreadLocal(true));
    }
    protected static void endUndoable(OAThreadLocal ti) {
        if (ti == null) return;
        ti.createUndoablePropertyChanges = false;
        ti.compoundUndoableName = null;
        OAUndoManager.endCompoundEdit();

        OAThreadLocalDelegate.TotalCaptureUndoablePropertyChanges.decrementAndGet();
    }

    
/// 20150323 remove this qqqqqqqq    
    public static void setCreateUndoablePropertyChanges(boolean b) {
        // LOG.finer(""+b);
        setCreateUndoablePropertyChanges(OAThreadLocalDelegate.getThreadLocal(b), b);
    }
    private static long msCreateUndoablePropertyChanges;
    protected static void setCreateUndoablePropertyChanges(OAThreadLocal ti, boolean b) {
        if (ti == null) return;
        if (ti.compoundUndoableName != null) return;
        int x;
        ti.createUndoablePropertyChanges = b;
        if (b) {
            x = OAThreadLocalDelegate.TotalCaptureUndoablePropertyChanges.getAndIncrement();
        }
        else {
            x = OAThreadLocalDelegate.TotalCaptureUndoablePropertyChanges.decrementAndGet();
        }
        if (x > 50 || x < 0) {
            msCreateUndoablePropertyChanges = throttleLOG("TotalCaptureUndoablePropertyChanges="+x+", ti.createUndoablePropertyChanges="+ti.createUndoablePropertyChanges, msCreateUndoablePropertyChanges);
        }
    }
    
    
    
    // TotalIsSendingEvent  20120104
    public static boolean isSendingEvent() {
        boolean b; 
        if (OAThreadLocalDelegate.TotalIsSendingEvent.get() == 0) {
            b = false;
        }
        else {
            b = isSendingEvent(OAThreadLocalDelegate.getThreadLocal(false));
        }
        return b;
    }
    protected static boolean isSendingEvent(OAThreadLocal ti) {
        if (ti == null) return false;
        return ti.sendingEvent > 0;
    }
    public static void setSendingEvent(boolean b) {
        setSendingEvent(OAThreadLocalDelegate.getThreadLocal(b), b);
    }
    private static boolean bSendingEventReset = false;
    private static long msSendingEvent;
    protected static void setSendingEvent(OAThreadLocal ti, boolean b) {
        if (ti == null) return;
        int x;
        if (b) {
            ti.sendingEvent++;
            x = OAThreadLocalDelegate.TotalIsSendingEvent.getAndIncrement();
        }
        else {
            ti.sendingEvent--;
            if (ti.sendingEvent == 0) {
                ti.calcPropertyEvents = null;
            }
            x = OAThreadLocalDelegate.TotalIsSendingEvent.decrementAndGet();
        }
        if (x > 100 || x < 0 || bSendingEventReset) {
            msSendingEvent = throttleLOG("TotalIsSendingEvent="+x, msSendingEvent);
            if (x == 0) bSendingEventReset = false;
            else bSendingEventReset = true;
        }
    }

    public static boolean hasSentCalcPropertyChange(Object object, String propertyName) {
        if (object == null || propertyName == null) return false;
        if (!isSendingEvent()) return false;
        OAThreadLocal tl = OAThreadLocalDelegate.getThreadLocal(true);
        
        if (tl.calcPropertyEvents == null) {
            tl.calcPropertyEvents = new Tuple[1];
            tl.calcPropertyEvents[0] = new Tuple<Object, String>(object, propertyName);
            return false;
        }
        for (Tuple<Object, String> tup : tl.calcPropertyEvents) {
            if (tup.a == object) {
                if (propertyName.equalsIgnoreCase(tup.b)) return true;
            }
        }
        int x = tl.calcPropertyEvents.length;
        Tuple<Object, String>[] temp = new Tuple[x+1];
        System.arraycopy(tl.calcPropertyEvents, 0, temp, 0, x);
        temp[x] = new Tuple<Object, String>(object, propertyName);
        tl.calcPropertyEvents = temp;
        
        return false;
    }

    // HubListenerTree used to determine how deep tree is, caused by listening to dependent props (like calcs, etc)                                
    public static int getHubListenerTreeCount() {
        int x; 
        if (OAThreadLocalDelegate.TotalHubListenerTreeCount.get() == 0) {
            x = 0;
        }
        else {
            x = getHubListenerTreeCount(OAThreadLocalDelegate.getThreadLocal(false));
        }
        return x;
    }
    protected static int getHubListenerTreeCount(OAThreadLocal ti) {
        if (ti == null) return 0;
        return ti.hubListenerTreeCount;
    }
    public static void setHubListenerTree(boolean b) {
        setHubListenerTree(OAThreadLocalDelegate.getThreadLocal(b), b);
    }
    private static long msHubListenerTree;
    protected static void setHubListenerTree(OAThreadLocal ti, boolean b) {
        if (ti == null) return;
        int x;
        
        if (b) {
            ti.hubListenerTreeCount++;
            x = OAThreadLocalDelegate.TotalHubListenerTreeCount.getAndIncrement();
        }
        else {
            ti.hubListenerTreeCount--;
            x = OAThreadLocalDelegate.TotalHubListenerTreeCount.decrementAndGet();
        }
        if (x > 20 || x < 0) {
            msHubListenerTree = throttleLOG("TotalHubListenerTreeCount="+x, msHubListenerTree);
        }
    }

    public static void setIgnoreTreeListenerProperty(String prop) {
        getThreadLocal(true).ignoreTreeListenerProperty = prop;            
    }
    public static String getIgnoreTreeListenerProperty() {
        return getThreadLocal(true).ignoreTreeListenerProperty;            
    }
    

    // TotalIsSendingEvent  20120104
    public static Hub getGetDetailHub() {
        Hub h; 
        if (OAThreadLocalDelegate.TotalGetDetailHub.get() == 0) {
            h = null;
        }
        else {
            h = getGetDetailHub(OAThreadLocalDelegate.getThreadLocal(false));
        }
        return h;
    }
    protected static Hub getGetDetailHub(OAThreadLocal ti) {
        if (ti == null) return null;
        return ti.getDetailHub;
    }
    public static Hub setGetDetailHub(Hub h) {
        return setGetDetailHub(OAThreadLocalDelegate.getThreadLocal(true), h);
    }
    public static void resetGetDetailHub(Hub h) {
        resetGetDetailHub(OAThreadLocalDelegate.getThreadLocal(true), h);
    }
    private static long msGetDetailHub;
    protected static Hub setGetDetailHub(OAThreadLocal ti, Hub hub) {
        if (ti == null) return null;
        Hub hubx = ti.getDetailHub;
        ti.getDetailHub = hub;
        int x = OAThreadLocalDelegate.TotalGetDetailHub.getAndIncrement();
        if (x > 50 || x < 0) {
            msGetDetailHub = throttleLOG("TotalGetDetailHub="+x, msGetDetailHub);
        }
        return hubx;
    }
    protected static void resetGetDetailHub(OAThreadLocal ti, Hub hub) {
        if (ti == null) return;
        ti.getDetailHub = hub;
        int x = OAThreadLocalDelegate.TotalGetDetailHub.decrementAndGet();
        if (x > 25 || x < 0) {
            msGetDetailHub = throttleLOG("TotalGetDetailHub="+x, msGetDetailHub);
        }
    }

    
    // 20151111
    private static long msThrottleStackTrace;
    public static long throttleLOG(String msg, long msLast) {
        long ms = System.currentTimeMillis();
        if (ms > msLast + 5000) {
            LOG.warning(msg);
            
/*qqqqqqq            
            if (ms > msThrottleStackTrace + 30000) {
                if (msThrottleStackTrace != 0) LOG.warning("ThreadLocalDelegate.stackTraces\n"+getAllStackTraces());
                msThrottleStackTrace = ms;
            }
*/            
        }
        else ms = msLast;
        return ms;
    }
    
    
    public static String getAllStackTraces() {
        String result = "";
        String s = "DumpAllStackTraces "+(new OADateTime());
        result += s + "\n";

        Map<Thread,StackTraceElement[]> map = Thread.getAllStackTraces();
        Iterator it = map.entrySet().iterator();
        for (int i=1 ; it.hasNext(); i++) {
            Map.Entry me = (Map.Entry) it.next();
            Thread t = (Thread) me.getKey();
            s = i+") " + t.getName();
            result += s + "\n";
            
            StackTraceElement[] stes = (StackTraceElement[]) me.getValue();
            if (stes == null) continue;
            for (StackTraceElement ste : stes) {
                s = "  "+ste.toString(); //was: ste.getClassName()+" "+ste.getMethodName()+" "+ste.getLineNumber();
                result += s + "\n";
            }
        }
        return result;
    }
    
    public static void setStatus(String msg) {
        getOAThreadLocal().status = msg;
    }

    // 20140121
    public static void setRemoteRequestInfo(RequestInfo ri) {
        getOAThreadLocal().requestInfo = ri;
    }
    public static RequestInfo getRemoteRequestInfo() {
        return getOAThreadLocal().requestInfo;
    }

//qqqqqqqqqqqqqqqqqqqqqqq    

    // Runnable ---------------
    /**
       These are the events from a OARemoteThread, that will be put in a queue
       to be processed by an ExecutionerService
    */
/**QQQQQQQQQQQQQ    
    public static ArrayList<Runnable> getRunnables(boolean bClear) {
        if (OAThreadLocalDelegate.TotalRunnable.get() == 0) {
            return null;
        }
        ArrayList<Runnable> al = getRunnables(OAThreadLocalDelegate.getThreadLocal(false), bClear);
        return al;
    }
    protected static ArrayList<Runnable> getRunnables(OAThreadLocal ti, boolean bClear) {
        if (ti == null) return null;
        ArrayList<Runnable> al = ti.alRunnable;
        if (bClear && ti.alRunnable != null) {
            ti.alRunnable = null;
            OAThreadLocalDelegate.TotalRunnable.decrementAndGet();
        }
        return al;
    }
    public static void addRunnable(Runnable run) {
        addRunnable(OAThreadLocalDelegate.getThreadLocal(true), run);
    }
    protected static void addRunnable(OAThreadLocal ti, Runnable run) {
        if (ti == null) return;
        if (ti.alRunnable == null) {
            ti.alRunnable = new ArrayList<Runnable>();
            OAThreadLocalDelegate.TotalRunnable.incrementAndGet();
        }
        ti.alRunnable.add(run);
    }
    public static void clearRunnables() {
        clearRunnables(OAThreadLocalDelegate.getThreadLocal(true));
    }
    protected static void clearRunnables(OAThreadLocal ti) {
        if (ti == null) return;
        ti.alRunnable = null;
    }
***/    
    /**
     * Flag that can be set to allow messages from OARemoteThread to be
     * sent to other clients/server.
     */
    public static boolean setSendMessages(boolean b) {
        return OARemoteThreadDelegate.sendMessages(b);
    }

    
    /* 20151103 on hold for OAsyncCombinedClient work
    public static void setRemoteMultiplexerClient(RemoteMultiplexerClient rmc) {
        setRemoteMultiplexerClient(OAThreadLocalDelegate.getThreadLocal(true), rmc);
    }
    protected static void setRemoteMultiplexerClient(OAThreadLocal ti, RemoteMultiplexerClient rmc) {
        ti.remoteMultiplexerClient = rmc;
        int x;
        if (rmc != null) x = TotalRemoteMultiplexerClient.incrementAndGet();
        else x = TotalRemoteMultiplexerClient.decrementAndGet();
        //if (x > 25 || x < 0) LOG.warning("TotalRemoteMultiplexerClient="+x);
    }
    protected static void setSyncClient(OASyncClient sc) {
        if (sc != null) setRemoteMultiplexerClient(sc.getRemoteMultiplexerClient());
        else setRemoteMultiplexerClient(null);
    }
    
    
    public static RemoteMultiplexerClient getRemoteMultiplexerClient() {
        RemoteMultiplexerClient mc;
        if (OAThreadLocalDelegate.TotalRemoteMultiplexerClient.get() == 0) {
            mc = null;
        }
        else {
            OAThreadLocal tl = OAThreadLocalDelegate.getThreadLocal(false);
            if (tl == null) mc = null;
            else mc = tl.remoteMultiplexerClient;
        }
        return mc;
    }
    */

    // 20160121 allows an object to wait to be notified by OARemoteThreadDelegate.startNextThread()
    public static void setNotifyObject(Object obj) {
        if (obj == null) {
            if (OAThreadLocalDelegate.TotalNotifyWaitingObject.get() == 0) return;
            OAThreadLocal tl = OAThreadLocalDelegate.getThreadLocal(false);
            if (tl != null && (tl.notifyObject != null)) {
                TotalNotifyWaitingObject.decrementAndGet();
                tl.notifyObject = obj;
            }
        }
        else {
            OAThreadLocal tl = OAThreadLocalDelegate.getThreadLocal(true);
            if (tl.notifyObject == null) TotalNotifyWaitingObject.incrementAndGet();
            tl.notifyObject = obj;
        }
    }
    public static void notifyWaitingThread() {
        if (OAThreadLocalDelegate.TotalNotifyWaitingObject.get() == 0) return;

        OAThreadLocal tl = OAThreadLocalDelegate.getThreadLocal(false);
        if (tl == null) return;
        if (tl.notifyObject == null) return;
        synchronized (tl.notifyObject) {
            tl.notifyObject.notifyAll();
        }
        setNotifyObject(null);
    }
}

