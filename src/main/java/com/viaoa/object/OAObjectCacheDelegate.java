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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.*;

import com.viaoa.ds.OADataSource;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubSelectDelegate;
import com.viaoa.hub.HubTemp;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAPropertyPath;
import com.viaoa.util.OAString;
import com.viaoa.util.filter.OAEqualFilter;
import com.viaoa.util.filter.OAFilterDelegate;
import com.viaoa.util.filter.OAFilterDelegate.FinderInfo;

/**
 * 
 * @author vincevia
 * @see OAThreadLocalDelegate#setObjectCacheAddMode(int)
 * @see OAThreadLocalDelegate#getObjectCacheAddMode()
 */
public class OAObjectCacheDelegate {
	private static Logger LOG = Logger.getLogger(OAObjectCacheDelegate.class.getName());
	
	protected static int DefaultAddMode = 1;

    /** throw an exception if a duplicate object is added. This is Default.  
	    @see HubController#setAddMode
	*/
	static public final int NO_DUPS = 1;   // dont use 0
	
	/** dont store object if a duplicate is already stored. 
	    If the object is being deserialized (see OAObject.readResolve)
	    then the object that is already loaded will be used.
	    @see HubController#setAddMode
	    @see OAObject#readResolve
	*/
	static public final int IGNORE_DUPS = 2;
	
	/** store object even if another exists
	    @see HubController#setAddMode
	*/
	static public final int OVERWRITE_DUPS = 3;
	
	/** dont store objects.
	    @see HubController#setAddMode
	*/
	static public final int IGNORE_ALL = 4;
	static protected final int MODE_MAX = 4;
    
    /**
     * Automatically set by Hub.select() when a select is done without a where clause.
     * A weakReference is used for storage.  
     * When a new OAObject is created, it will be added to a SelectAllHub.
     * @since 2007/08/16
     */
    public static Hub[] getSelectAllHubs(Class clazz) {
    	if (clazz == null) return null;
        WeakReference[] refs = (WeakReference[]) OAObjectHashDelegate.hashCacheSelectAllHub.get(clazz);
        if (refs == null) return null;
    	synchronized (OAObjectHashDelegate.hashCacheSelectAllHub) {
	        Hub[] hubs = new Hub[refs.length];
	        for (int i=0; i<refs.length; i++) {
	        	hubs[i] = (Hub) refs[i].get();
	        	if (hubs[i] == null) {
	        		if (refs.length == 1) {
	        			OAObjectHashDelegate.hashCacheSelectAllHub.remove(clazz);
	        			return null;
	        		} 
	        		else {
	        			OAObjectHashDelegate.hashCacheSelectAllHub.put(clazz, removeSelectAllHubs(refs, refs[i]));
	        			return getSelectAllHubs(clazz);
	        		}
	        	}
	        }
	        return hubs;
        }
    }
    /** returns first hub from getSelectAllHubs() */
    public static Hub getSelectAllHub(Class clazz) {
    	Hub[] hs = getSelectAllHubs(clazz);
    	if (hs != null && hs.length > 0) return hs[0];
    	return null;
    }   
    private static WeakReference[] removeSelectAllHubs(WeakReference[] refs, WeakReference refRemove) {
		WeakReference[] refs2 = new WeakReference[refs.length-1];
		boolean bFound = false;
        int j = 0;
		for (int i=0; i<refs.length; i++) {
			if (refs[i] == refRemove) bFound = true;
			else refs2[j++] = refs[i];
		}
		if(!bFound) return refs;
		return refs2;
    }
    /**
     * Used by Hub.select() to register a Hub that has all data selected.
     * @since 2007/08/16
     */
     public static void setSelectAllHub(Hub hub) {
    	if (hub == null) return;
    	Class clazz = hub.getObjectClass();
    	LOG.fine("Hub.objectClass = "+clazz);
    	
    	synchronized (OAObjectHashDelegate.hashCacheSelectAllHub) {
	        WeakReference[] refs = (WeakReference[]) OAObjectHashDelegate.hashCacheSelectAllHub.get(clazz);
	        if (refs == null) {
	        	refs = new WeakReference[1];
	        }
	    	else {
		    	// first see if Hub is already in the list
		        for (int i=0; i<refs.length; i++) {
		        	if (hub == refs[i].get()) return;
				}	
	        	WeakReference[] refs2 = new WeakReference[refs.length+1];
				System.arraycopy(refs, 0, refs2, 0, refs.length);
				refs = refs2;
	    	}
	    	refs[refs.length-1] = new WeakReference(hub);
	    	OAObjectHashDelegate.hashCacheSelectAllHub.put(clazz, refs);
	    	LOG.finer("total for class="+clazz+" is now "+refs.length);
		}
    }
    
     /**
      * Used by Hub to unregister a Hub that had all data selected.
      * @since 2007/08/16
      */
     public static void removeSelectAllHub(Hub hub) {
    	if (hub == null) return;
    	Class clazz = hub.getObjectClass();
    	if (clazz == null) return;
    	// LOG.finest("Hub.objectClass = "+clazz);
        WeakReference[] refs = (WeakReference[]) OAObjectHashDelegate.hashCacheSelectAllHub.get(clazz);
        if (refs == null) return;
    	synchronized (OAObjectHashDelegate.hashCacheSelectAllHub) {
	        for (int i=0; i<refs.length; i++) {
	        	Hub h = (Hub) refs[i].get();
	        	if (h == hub) {
	        		if (refs.length == 1) {
	        			OAObjectHashDelegate.hashCacheSelectAllHub.remove(clazz); 
	        	    	LOG.fine("total for class="+clazz+" is now 0");
	        		}
	        		else {
	        			WeakReference[] refNew = removeSelectAllHubs(refs, refs[i]);
	        			OAObjectHashDelegate.hashCacheSelectAllHub.put(clazz, refNew);
	        	    	LOG.finer("total for class="+clazz+" is now "+refNew.length);
	        		}
	        	}
	        }
		}
     }
     public static void removeAllSelectAllHubs() {
         synchronized (OAObjectHashDelegate.hashCacheSelectAllHub) {
             OAObjectHashDelegate.hashCacheSelectAllHub.clear();
         }
     }

    /**
     * Used to store a global hub by name, using a weakReference.
     * @param name reference name to use, not case-sensitive
     * @return if found then Hub, else null.
     */
    static public void setNamedHub(String name, Hub hub) {
    	LOG.fine("Hub="+hub+", name="+name);
    	if (name == null || hub == null) return;
    	OAObjectHashDelegate.hashCacheNamedHub.put(name.toUpperCase(), new WeakReference(hub));
    	LOG.fine("total named Hubs is now ="+OAObjectHashDelegate.hashCacheNamedHub.size());
    }
    /**
     * Gets a hub that is stored by name.
     * @param name reference name to use, not case-sensitive
     * @return if found then Hub, else null.
     */
    public static Hub getNamedHub(String name) {
    	//LOG.finer("Name="+name);
    	if (name == null) return null;
        WeakReference ref = (WeakReference) OAObjectHashDelegate.hashCacheNamedHub.get(name.toUpperCase());
        Hub hub = null;
        if (ref != null) {
        	hub = (Hub) ref.get();
        	if (hub == null) OAObjectHashDelegate.hashCacheNamedHub.remove(name);
        }
    	return hub;
    }
    

    private static int listenerCount;
    /** Listeners support for HubEvents.  
        <p>
        The following events are sent:<br>
        Events from Hubs: afterAdd, afterRemove<br>
        Events from OAObjects: afterPropertyChange
    */
    public static void addListener(Class clazz, OAObjectCacheListener l) {
    	LOG.fine("class="+clazz);
        Vector vecListener = (Vector) OAObjectHashDelegate.hashCacheListener.get(clazz);
        if (vecListener == null) {
            synchronized (OAObjectHashDelegate.hashCacheListener) {
                vecListener = (Vector) OAObjectHashDelegate.hashCacheListener.get(clazz);
                if (vecListener == null) {
                    vecListener = new Vector(5,5);
                    OAObjectHashDelegate.hashCacheListener.put(clazz, vecListener);
                }
            }
        }
        if (!vecListener.contains(l)) {
        	listenerCount++;
        	vecListener.addElement(l);
        	LOG.fine("total listeners="+listenerCount);
        }
    }

    /**
     * @deprecated replaced with addListener(..) 
     */
    /*
    public static void addListener(Class c, HubListener hl) {
    }
    */
    
    /** @see addListener(Class, HubListener) */
    public static void removeListener(Class clazz, OAObjectCacheListener l) {
    	LOG.fine("class="+clazz);
        Vector vecListener = (Vector) OAObjectHashDelegate.hashCacheListener.get(clazz);
        if (vecListener != null) {
            synchronized(vecListener) {
                if (vecListener.remove(l)) {
                	listenerCount--;
                	LOG.fine("total listeners="+listenerCount);
                }
            }
        }
    }

    /** 
        Returns array of HubListeners for a given class.
        @see addListener(Class, HubListener) 
    */
    public static OAObjectCacheListener[] getListeners(Class c) {
        if (listenerCount == 0) return null;
        // LOG.finest("class="+c);
    	Vector vecListener = (Vector) OAObjectHashDelegate.hashCacheListener.get(c);
        if (vecListener == null) return null;
        
        OAObjectCacheListener[] listeners = null;
        synchronized(vecListener) {
            int x = vecListener.size();
            listeners = new OAObjectCacheListener[x];
            for (int i=0; i<x; i++) {
            	listeners[i] = (OAObjectCacheListener) vecListener.elementAt(i);
            }
        }
        // LOG.finest("total size="+x);
        return listeners;
    }

    /** called by OAObject to send a HubEvent. */
    protected static void fireAfterPropertyChange(OAObject obj, OAObjectKey origKey, String propertyName, Object oldValue, Object newValue, boolean bLocalOnly, boolean bSendEvent) {
        // Note: oldValue could be OAObjectKey, but will be resolved when HubEvent.getOldValue() is called
    	if (listenerCount == 0) return;
        if (obj == null || propertyName == null) return;
        if (bSendEvent) {
            // LOG.finest("object="+obj+", propertyName="+propertyName+", key="+origKey);
            OAObjectCacheListener[] hl = getListeners(obj.getClass());
            if (hl != null && hl.length > 0) {
                for (int i=0; i<hl.length; i++) {
                    hl[i].afterPropertyChange(obj, propertyName, oldValue, newValue);
                }
            }
        }
    }

	protected static void fireAfterAddEvent(Object obj) {
        if (listenerCount == 0) return;
        if (obj == null) return;
        final OAObjectCacheListener[] hl = getListeners(obj.getClass());
        if (hl == null) return; 
	    final int x = hl.length;
	    if (x > 0) {
            // LOG.finest("Hub="+thisHub+", object="+obj);
	        for (int i=0; i<x; i++) { 
	        	hl[i].afterAdd((OAObject) obj);
	        }
	    }
	}
    
    /**
        Removes all objects from HubController.
    */
    public static void removeAllObjects() {
        LOG.warning("removing all Objects was called (fyi only)");
        OAObjectHashDelegate.hashCacheClass.clear();
    }

    

	/**
    Used to <i>visit</i> every object in the Cache.
	*/
	public static void callback(OACallback callback) {
        LOG.fine("callback");
    	Object[] cs = OAObjectHashDelegate.hashCacheClass.keySet().toArray();
    	if (cs == null) return;
    	int x = cs.length;
    	for (int i=0; i<x; i++) {
            callback(callback, (Class) cs[i]);
    	}
	}

	public static void callback(Class clazz, OACallback callback) {
		callback(callback, clazz);
	}
	
    /**
    Used to <i>visit</i> every object in the Cache for a Class.
	*/
	public static void callback(OACallback callback, Class clazz) {
        if (callback == null) return;
        TreeMapHolder tmh = (TreeMapHolder) OAObjectHashDelegate.hashCacheClass.get(clazz);
        if (tmh == null) return;
        
        try {
            tmh.rwl.readLock().lock();
            TreeMap tm = tmh.treeMap;
            Map.Entry me = tm.firstEntry();
            while (me != null) {
                WeakReference ref = (WeakReference) me.getValue();
                Object obj = ref.get();
                if (obj != null) {
                    callback.updateObject(obj);
                }
                me = tm.higherEntry(me.getKey());
            }
        }
        finally {
            tmh.rwl.readLock().unlock();
        }
    }

	
	
    /**
        Populates a Vector of Strings that describe the Classes and amount of objects that are loaded.
    */
    public static void getInfo(Vector vec) {
        // LOG.finer("called");
        Vector v = getInfo();
        int x = v.size();
        for (int i=0; i<x; i++) {
        	vec.add(v.get(i));
        }
    }
    
    public static Class[] getClasses() {
        Class[] cs = OAObjectHashDelegate.hashCacheClass.keySet().toArray(new Class[0]);
        return cs;
    }
    public static int getTotal(Class clazz) {
        TreeMapHolder tmh = (TreeMapHolder) OAObjectHashDelegate.hashCacheClass.get(clazz);
        if (tmh == null) return 0;
        try {
            tmh.rwl.readLock().lock();
            return tmh.treeMap.size();
        }
        finally {
            tmh.rwl.readLock().unlock();
        }
    }
    
    /**
        Returns a Vector of Strings that describe the Classes and amount of objects that are loaded.
    */
    public static Vector getInfo() {
        // LOG.finer("called");
        Vector vec = new Vector(20,20);
        vec.addElement("HubController Info --- ");
        
    	Class[] cs = getClasses();
    	if (cs == null) return vec;
    	int x = cs.length;
    	
    	int max = 0;
    	for (int i=0; i<x; i++) {
    		max = Math.max(max, ((Class)cs[i]).getName().length());
    	}    	
    	String fmt = max+"L";

    	/* this requires that the SizeOf -D property is set when starting
    	long ll = SizeOf.sizeOf(OAObjectHashDelegate.hashCacheClass, true);
        vec.addElement(OAString.fmt("  SizeOf cache", fmt)+" "+OAString.format(ll,"#,##0"));
    	*/
    	
    	for (int i=0; i<x; i++) {
            TreeMapHolder tmh = (TreeMapHolder) OAObjectHashDelegate.hashCacheClass.get(cs[i]);
            vec.addElement(String.format(((Class)cs[i]).getName(), fmt)+" "+String.format("%,2d", getTotal(cs[i])));
        }    
        vec.addElement(OAString.fmt("TempHubs", fmt) + " " + HubTemp.getCount());
        Collections.sort(vec);
        return vec;
    }
    
    
    /** 
        The DefaultAddMode determines how HubController.addObject() will handle an object if it already exists.
        This method sets the Default mode for all unassigned threads.
        @param mode AddModes are NO_DUPS (default), IGNORE_DUPS, OVERWRITE_DUPS.
        @see  HubController#setAddMode
    */
    static public void setDefaultAddMode(int mode) {
        LOG.config("default add mode="+mode);
        if (mode > 4 || mode < 0) throw new IllegalArgumentException("HubController.setDefaultAddMode() must be 0,1,2,3 or 4");
        DefaultAddMode = mode;
    } 
    /**
        @see #setDefaultAddMode(int)
    */
    static public int getDetaultAddMode() {
        return DefaultAddMode;
    }
    
    /** 
        Used by OAObject to cache new objects.
        Objects are removed by OAObject.finalize()
        @see #setAddMode(int)
        @return either the object that was "obj" or the object that was already in the tree.
    */
    public static OAObject add(OAObject obj) {
        return add(obj, false, true);
    }

    // 20121226
    /** *
     *  Note: also use setDisableRemove, since the OAObject finalize will call remove from the 
     *  cache. 
     */
    public static void clearCache() {
        OAObjectHashDelegate.hashCacheClass.clear();
    }
    public static void clearCache(Class clazz) {
        synchronized (OAObjectHashDelegate.hashCacheClass) {
            OAObjectHashDelegate.hashCacheClass.remove(clazz);
        }
    }

    
    public static OAObject add(OAObject obj, boolean bErrorIfExists, boolean bAddToSelectAll) {
        if (bDisableCache) return obj;
        OAObject objx = _add(obj, bErrorIfExists, bAddToSelectAll);
        /* removed, since serializer does this
        if (objx != obj) {
            OAObjectDelegate.dontFinalize(obj);
        }
        */ 
        return objx;
    }
    
    private static boolean bDisableCache = false;
    public static void setDisableCache(boolean b) {
        bDisableCache = b;
    }
    private static boolean bDisableRemove = false;
    public static void setDisableRemove(boolean b) {
        bDisableRemove = b;
    }
    

    private static OAObject _add(final OAObject obj, final boolean bErrorIfExists, boolean bAddToSelectAll) {
        if (bDisableCache) return obj;
        // LOG.finer("obj="+obj);
        if (obj == null) return null;

        OAObject result = null;
        Object removeObj = null;
        final OAObjectKey ok = OAObjectKeyDelegate.getKey(obj);
        boolean bSendAddEvent = false;
        
        final TreeMapHolder tmh = getTreeMapHolder(obj.getClass(), true);
        try {
            tmh.rwl.writeLock().lock();
            final TreeMap tm = tmh.treeMap;

            WeakReference ref = (WeakReference) tm.get(ok);

            if (ref != null) {
                result = (OAObject) ref.get();
                if (result == obj) {
                    return obj;
                }
            }
            
            int mode = OAThreadLocalDelegate.getObjectCacheAddMode();
            if (result == null) {
                if (ref != null) tm.remove(ok);  // previous value was gc'd
                if (mode != IGNORE_ALL) {
                    ref = new WeakReference(obj);
                    tm.put(ok, ref);
                    bSendAddEvent = true;
                }
                result = obj;
            }
            else {  // already in treemap
                if (mode == NO_DUPS) {
                    if (bErrorIfExists) {
                        throw new RuntimeException("OAObjectCacheDelegate.add() object already exists "+obj);
                    }
                    bAddToSelectAll = false;
                }
                else if (mode == OVERWRITE_DUPS) {
                    if (ref != null) tm.remove(ok);  // previous value was gc'd
                    ref = new WeakReference(obj);
                    tm.put(ok, ref);
                    removeObj = result;
                    result = obj;
                    bSendAddEvent = true;
                    // LOG.fine("overwrite object="+obj);
                }
                else {
                    // Ignore duplicate - automatically set as the default mode when using OA RMI
                    bAddToSelectAll = false;
                }
            }
        }
        finally {
            tmh.rwl.writeLock().unlock();
        }

        // outside of lock
        if (bAddToSelectAll && (result==obj)) {
            Hub[] hs = getSelectAllHubs(obj.getClass());
            for (int i=0; hs != null && i<hs.length; i++) {
                LOG.finer("adding to selectAll Hub="+hs[i]);
                if (removeObj != null) hs[i].remove(removeObj);
                hs[i].add(obj);
            }
        }

        if (bSendAddEvent) {
            fireAfterAddEvent(obj);
        }
        return result;
    }        

    
    public static void addToSelectAllHubs(OAObject obj) {
        Hub[] hs = getSelectAllHubs(obj.getClass());
        for (int i=0; hs != null && i<hs.length; i++) {
            LOG.finer("adding to selectAll Hub="+hs[i]);
            if (!hs[i].contains(obj)) hs[i].add(obj);
        }
    }

    /** Used by OAObjectKeyDelegate.updateKey when object Id property is changed. */
    protected static void rehash(OAObject obj, OAObjectKey oldKey) {
        if (bDisableCache) return;
    	//LOG.fine("obj="+obj);
    	TreeMapHolder tmh = getTreeMapHolder(obj.getClass(), true);

        OAObjectKey ok = OAObjectKeyDelegate.getKey(obj);
        try {
            tmh.rwl.writeLock().lock();
            if (oldKey != null) {
                WeakReference refx = tmh.treeMap.remove(oldKey);
            }
            tmh.treeMap.put(ok, new WeakReference(obj));
        }
        finally {
            tmh.rwl.writeLock().unlock();
        }
    }

    /** 
        Used by OAObject.finalize to remove object from HubContoller cache. 
    */
    static public void removeObject(OAObject obj) {
        if (bDisableCache) return;
        if (bDisableRemove) return;
    	//LOG.finer("obj="+obj);
        if (obj == null) return;

        Class clazz = obj.getClass();
        TreeMapHolder tmh = getTreeMapHolder(clazz, false);
        if (tmh != null) {
            OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
            
            boolean b = true;
            try {
                tmh.rwl.writeLock().lock();
                WeakReference ref = tmh.treeMap.remove(key);
                // 20140307 make sure that the obj in tree is the one being removed
                //   since an obj that is finalized could be reloaded. 
                if (ref != null) {
                    Object objx = ref.get();
                    if (objx != null && objx != obj) {
                        tmh.treeMap.put(key, ref); // put it back
                        b = false;
                    }
                }
            }
            finally {
                tmh.rwl.writeLock().unlock();
            }
            if (b) {
                // allow object to be removed from CS
                int guid = obj.getObjectKey().getGuid();
                if (guid > 0) {
                    OAObjectCSDelegate.objectRemovedFromCache(obj, guid);
                }
            }
        }        
    }

    /**
        @return Hashtable of all objects loaded for a Class c.
    */
    static TreeMapHolder getTreeMapHolder(Class c) {
        return getTreeMapHolder(c, true);
    }

    
    static class TreeMapHolder {
        TreeMap<OAObjectKey, WeakReference<OAObject>> treeMap = new TreeMap<OAObjectKey, WeakReference<OAObject>>();
        ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    } 
    
    /**
        @return Hashtable of all objects loaded for a Class c.
    */
    static TreeMapHolder getTreeMapHolder(Class c, boolean bCreate) {
        if (c == null) return null;
    	// LOG.finer("class="+c);
        TreeMapHolder tmHolder = (TreeMapHolder) OAObjectHashDelegate.hashCacheClass.get(c);
        if (tmHolder == null) {
            if (!bCreate) return null;
            synchronized (OAObjectHashDelegate.hashCacheClass) {
                // make sure it hasnt been created by another thread
                tmHolder = (TreeMapHolder) OAObjectHashDelegate.hashCacheClass.get(c);
                if (tmHolder == null) {
                    tmHolder = new TreeMapHolder();
                    OAObjectHashDelegate.hashCacheClass.put(c, tmHolder);
                }
            }
        }
        return tmHolder;
    }

    
    
    /** 
        Used to retreive any object based on its Object Id property value.
        @param key object to compare to, object or objects[] to compare this object's objectId(s) with or OAObjectKey to compare with this object's objectId
        @see OAObjectKey#OAObjectKey
        @see OAObject#equals
    */
    public static OAObject getObject(Class clazz, Object key) {
        return get(clazz,key);
    }

    /** 
        Used to retreive any object based on its Object Id property value.
        @see getObject(Class, Object)
    */
    public static OAObject get(Class clazz, int id) {
        return get(clazz, new Integer(id));
    }
    
    /**
       Returns object with objectId of key.
    */
    public static OAObject get(Class clazz, Object key) {
        if (bDisableCache) return null;
        if (key == null || clazz == null) return null;
        // LOG.finer("class="+clazz+", key="+key);

		if (!OAObject.class.isAssignableFrom(clazz)) {
			//LOG.warning("invalid class="+clazz);
			return null;
		}
        
        TreeMapHolder tmh = getTreeMapHolder(clazz, false);
        if (tmh != null) {
            if (!(key instanceof OAObjectKey)) {
            	if (key instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject)key);
            	else key = OAObjectKeyDelegate.convertToObjectKey(clazz, key);
            }
        	
            WeakReference ref;
            
            try {
                tmh.rwl.readLock().lock();
                ref = (WeakReference) tmh.treeMap.get(key);
            }
            finally {
                tmh.rwl.readLock().unlock();
            }

	        if (ref != null) {
	            // LOG.finer("found, class="+clazz+", key="+key);
	        	OAObject objx = (OAObject) ref.get();
	        	return objx;
	        }
        }
        // LOG.finer("not found, class="+clazz+", key="+key);
        return null;
    }

    /** 
        Used to retrieve any object.
        @param key object to find.
    */
    public static Object get(OAObject obj) {
        if (bDisableCache) return null;
        if (obj == null) return null;
        return get(obj.getClass(), OAObjectKeyDelegate.getKey((OAObject) obj));
    }    

    public static Object findNext(Object fromObject) {
        if (fromObject == null) return null;
        return _find(fromObject, fromObject.getClass(), null, null, false, true);
    }
    public static Object findNext(Object fromObject, String propertyPath, Object findObject) {
    	if (fromObject == null) return null;
        return _find(fromObject, fromObject.getClass(), propertyPath, findObject, false, true);
    }
    public static Object findNext(Object fromObject, String propertyPath, Object findObject, boolean bSkipNew, boolean bThrowException) {
    	if (fromObject == null) return null;
        return _find(fromObject, fromObject.getClass(), propertyPath, findObject, bSkipNew, bThrowException);
    }
    public static Object findNext(Object fromObject, Class fromClass, String propertyPath, Object findObject) {
        if (fromObject == null && fromClass == null) return null;
        if (fromClass == null) fromClass = fromObject.getClass();
        return _find(fromObject, fromClass, propertyPath, findObject, false, true);
    }
    
    /** 
        Searches all objects in Class clazz for an object with property equalTo findObject.
    */
    public static Object find(Class clazz) {
        return _find(null, clazz, null, null, false, true);
    }
    public static Object find(Class clazz, OAFinder finder) {
        return _find(null, clazz, finder, false, true);
    }

    public static Object find(Class clazz, String propertyPath, Object findObject) {
        return _find(null, clazz, propertyPath, findObject, false, true);
    }
    
    public static Object find(Class clazz, String propertyPath, Object findObject, boolean bSkipNew, boolean bThrowException) {
    	return _find(null, clazz, propertyPath, findObject, bSkipNew, bThrowException);
    }    
    public static Object find(Class clazz, OAFinder finder, boolean bSkipNew, boolean bThrowException) {
        return _find(null, clazz, finder, false, true);
    }

    
    protected static Object _find(Object fromObject, Class clazz, String propertyPath, Object findObject, boolean bSkipNew, boolean bThrowException) {
        return _find(fromObject, clazz, propertyPath, findObject, bSkipNew, bThrowException, 1, null);
    }
    protected static Object _find(Object fromObject, Class clazz, OAFinder finder, boolean bSkipNew, boolean bThrowException) {
        return _find(fromObject, clazz, finder, bSkipNew, bThrowException, 1, null);
    }
    
    public static Object find(Object fromObject, Class clazz, OAFinder finder, boolean bSkipNew, boolean bThrowException, int fetchAmount, ArrayList<Object> alResults) {
        return _find(fromObject, clazz, finder, bSkipNew, bThrowException, fetchAmount, alResults);
    }
    public static Object find(Object fromObject, Class clazz, OAFilter filter, boolean bSkipNew, boolean bThrowException, int fetchAmount, ArrayList<Object> alResults) {
        OAFinder finder = new OAFinder();
        if (filter != null) finder.addFilter(filter);
        return _find(fromObject, clazz, finder, bSkipNew, bThrowException, fetchAmount, alResults);
    }
    public static Object find(Object fromObject, Class clazz, int fetchAmount, ArrayList<Object> alResults) {
        return _find(fromObject, clazz, null, false, false, fetchAmount, alResults);
    }
    
    // 20140125 get objects from cache
    /**
     * Returns objects from the objectCache.
     * @param clazz type of objects
     * @param fromObject null to start from the beginning, else use the last object previously returned.
     * @param fetchAmount max number to add to the alResults
     * @param alResults list of objects, after the fromObject
     * @return last object in alResults, that can be used as the fromObject on the next call to fetch
     */
    protected static Object _find(Object fromObject, Class clazz, String propertyPath, Object findValue, boolean bSkipNew, boolean bThrowException, int fetchAmount, ArrayList<Object> alResults) {
        if (bDisableCache) return null;
        // LOG.fine("class="+clazz+", propertyPath="+propertyPath+" findObject="+findObject+", bSkipNew="+bSkipNew);
        if (propertyPath == null || propertyPath.length() == 0) {
            propertyPath = null;
            // throw new IllegalArgumentException("HubController.find() property cant be null");
        }
        if (clazz == null) throw new IllegalArgumentException("HubController.find() class cant be null");

        // 20140201 replace methods with finder
        OAFinder finder;
        OAFilter filter = null;
        if (!OAString.isEmpty(propertyPath)) {
            OAPropertyPath pp = new OAPropertyPath(clazz, propertyPath);
            FinderInfo fi;
            try {
                fi = OAFilterDelegate.createFinder(clazz, pp);
            }
            catch (Exception e) {
                throw new RuntimeException("find error with propertyPath", e);
            }
            
            
            if (fi != null) {
                finder = fi.finder;
                filter = new OAEqualFilter(fi.pp, findValue, true);
            }
            else {
                finder = new OAFinder();
                filter = new OAEqualFilter(pp, findValue, true);
            }
        }
        else {
            finder = new OAFinder();
            if (findValue != null) filter = new OAEqualFilter(findValue, true);
        }
        if (filter != null) finder.addFilter(filter);
        return _find(fromObject, clazz, finder, bSkipNew, bThrowException, fetchAmount, alResults);
    }

    protected static Object _find(final Object fromObject, final Class clazz, final OAFinder finder, final boolean bSkipNew, final boolean bThrowException, int fetchAmount, final ArrayList<Object> alResults) {
        if (bDisableCache) return null;

        TreeMapHolder tmh = getTreeMapHolder(clazz, false);
        if (tmh == null) return null;

        try {
            tmh.rwl.readLock().lock();
        
            Map.Entry<OAObjectKey, WeakReference<OAObject>> me = null;
            if (fromObject != null) {
                OAObjectKey key;
                if (fromObject instanceof OAObjectKey) key = (OAObjectKey) fromObject;
                else if (fromObject instanceof OAObject) key = OAObjectKeyDelegate.getKey((OAObject) fromObject);
                else key = OAObjectKeyDelegate.convertToObjectKey(clazz, fromObject);
                if (key != null) {
                    me = tmh.treeMap.ceilingEntry(key);
                }
            }
            if (me == null) me = tmh.treeMap.firstEntry();
            
            boolean b = OAObject.class.isAssignableFrom(clazz);
            while (me != null) {
                WeakReference ref = (WeakReference) me.getValue();
                Object object = ref.get();
                if (object != null && object != fromObject) {
                    if (!bSkipNew || !b || !((OAObject)object).getNew()) {
                        if (finder != null) {
                            if (finder.findFirst((OAObject) object) != null) {
                                if (alResults == null) return object;
                                alResults.add(object);
                                if (alResults.size() >= fetchAmount) return object;
                            }
                        }
                    }
                }            
                me = tmh.treeMap.higherEntry(me.getKey());
            }
        }
        finally {
            tmh.rwl.readLock().unlock();
        }
        return null;
    }


    /**
     * Refresh all objects from the datasource.
     * This will be ran on the server, if called by client then it will async to run on server.
     * @param clazz Class of objects to update, will also requery all hubs for this class.
     */
    public static void refresh(Class clazz) {
        if (clazz == null) return;
        LOG.fine("refreshing "+clazz.getSimpleName());
        
        if (!OASyncDelegate.isServer(clazz)) {
            OASyncDelegate.getRemoteServer(clazz).refresh(clazz);
            LOG.fine("refreshing "+clazz.getSimpleName()+" will be ran on the server");
            return;
        }
        HashSet<Hub> hsHub = new HashSet<Hub>();
        
        OADataSource ds = OADataSource.getDataSource(clazz);
        if (ds == null) return;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        
        OADataSourceObjectCache dsCache = new OADataSourceObjectCache(false);
        Iterator it = dsCache.select(clazz);

        int cntTotal = 0;
        int cntAlone = 0;
        for ( ;it.hasNext(); cntTotal++) {
            OAObject obj = (OAObject) it.next();
            Hub[] hubs = OAObjectHubDelegate.getHubReferences(obj);

            boolean bNeedsRefreshed = true;
            if (hubs != null) {
                for (Hub h : hubs) {
                    if (h == null) continue;
                    if (h.getSelect() == null) {
                        if (h.getMasterObject() == null) continue;
                        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(h);
                        if (li != null) {
                            li = li.getReverseLinkInfo();
                            if (li == null || li.getCalculated()) continue;
                            if (li.getPrivateMethod()) continue;
                        }
                    }                
                    bNeedsRefreshed = false;
                    if (!hsHub.contains(h)) hsHub.add(h);
                }
            }
            
            if (bNeedsRefreshed) {
                OAObjectKey key = OAObjectKeyDelegate.getKey(obj);
                ds.getObject(oi, clazz, key, true);
                cntAlone++;
                continue;
            }
        }

        int cntHubs = 0;
        int cntInHubs = 0;
        for (Hub h: hsHub) {
            HubSelectDelegate.refreshSelect(h);
            cntHubs++;
            cntInHubs += h.getSize();
        }
        dsCache.close();
        LOG.fine(String.format("refreshed %s, total=%d, alongCnt=%d, hubCnt=%d, inHubsCnt=%d", 
                clazz.getSimpleName(), cntTotal, cntAlone, cntHubs, cntInHubs));
    }
    
    
   
/*qqqqqqqq    
    public static void updateClientInfo(OAClientInfo ci) {
    	// LOG.fine("called");
        Enumeration enumx = OAObjectHashDelegate.hashCacheClass.keys();
        ci.getCacheHashMap().clear();

        Object[] cs = OAObjectHashDelegate.hashCacheClass.keySet().toArray();
    	if (cs == null) return;
    	int x = cs.length;
    	for (int i=0; i<x; i++) {
            TreeMapHolder tmh = (TreeMapHolder) OAObjectHashDelegate.hashCacheClass.get(cs[i]);
        	ci.getCacheHashMap().put(cs[i], tmh.treeMap.size());
        }    
    }
*/    
}

/**qqqqqqqqqqqqqqqqqqqqqq	
static {
	Thread t = new Thread(new Runnable() {
	@Override
		public void run() {
			for (;;) {
				try {
					LOG.finer(Thread.currentThread() + " sleeping for 5 minutes");
					Thread.sleep(5 * 60000);
					LOG.finer(Thread.currentThread() + " awake and calling clean()");
					clean();
				}
				catch (Exception e) {
					LOG.log(Level.WARNING, "Error in cleaning thread run()", e);
				}
				
			}
		}	
	}, "OAObjectCacheDelegate.clean");
	t.setDaemon(true);
	t.setPriority(t.MIN_PRIORITY);
	LOG.config("create thread "+t);
	t.start();
}
***/	
