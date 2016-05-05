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


import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.viaoa.hub.Hub;


/**
 * List of Hashtables used by Delegates, some of which need to have OAObject rehashed 
 * when the ObjectKey is changed.
 * @author vincevia
 */
public class OAObjectHashDelegate {

	
    /** 
     * Static cache of Methods
     * Key   = Class
     * Value = Hashtable of name/methods.  Ex: GETLASTNAME, getLastName() method
     */
    static private final ConcurrentHashMap<Class, Map<String, Method>> hashClassMethod = new ConcurrentHashMap<Class, Map<String, Method>>(151, 0.75F);
    static private final ConcurrentHashMap<Class, Set<String>> hashClassMethodNotFound = new ConcurrentHashMap<Class, Set<String>>(151, 0.75F);

    /** 
     * Used by OALinkInfo to cache Hubs so that they are not strong linked within object.  
     * Key   = OALinkInfo
     */
    protected static final Hashtable<OALinkInfo, ReentrantReadWriteLock> hashLinkInfoCacheLock = new Hashtable<OALinkInfo, ReentrantReadWriteLock>(47,0.75f);
	protected static final Hashtable<OALinkInfo, ArrayList> hashLinkInfoCacheArrayList = new Hashtable<OALinkInfo, ArrayList>(47,0.75f);
    protected static final Hashtable<OALinkInfo, HashSet> hashLinkInfoCacheHashSet = new Hashtable<OALinkInfo, HashSet>(47,0.75f);


    /**
     * Used to flag that an OAObject.Id property is gettting safely assigned
     */
    static private final ConcurrentHashMap<Integer, Integer> hashAssigningId = new ConcurrentHashMap<Integer, Integer>(15, 0.75F);
    public static Map<Integer, Integer> getAssigningIdHash() {
        return hashAssigningId;
    }
	
	/** 
	 *  Used by OAObjectInfoDelegate to store the Root Hub for recursive classes.
     * 	Key   = Class
     * 	Value = Hub (that is the 'top' (root) Hub of a recursive hub) 
	 */
	protected static final Hashtable<OAObjectInfo, Hub> hashRootHub = new Hashtable<OAObjectInfo, Hub>(13, .75f);
	

    
    /** 
     * Static cache of OAObjectInfo, keyed on Class.
     * Key   = Class
     * Value = OAObjectINfo 
     */
    static protected final ConcurrentHashMap<Class, OAObjectInfo> hashObjectInfo = new ConcurrentHashMap<Class, OAObjectInfo>(147, 0.75F);
    public static Map<Class, OAObjectInfo> getObjectInfoHash() {
    	return hashObjectInfo;
    }
    
    
    
    /** 
     * Locking support for OAObject.  See OALock
     * Key   = OAObject
     * Value = OALock
     */
    protected static final Hashtable hashLock = new Hashtable(11, 0.75F);
    
    /** 
     * Used by Cache to store all OAObjects.  
     * Key   = Class
     * Value = TreeMap with all of the OAObjects in it.
     */
	protected static final ConcurrentHashMap<Class, Object> hashCacheClass = new ConcurrentHashMap<Class, Object>(147, 0.75f);
	
	/** 
     * List of listeners for Cached objects    
     * Key   = Class
     * Value = Vector of listeners 
     */
	protected static final ConcurrentHashMap hashCacheListener = new ConcurrentHashMap(); // stores vector per class
    
	
    /** 
     * Used by Cache to store all hubs that have selected all objects.  
     * Key   =  Class
     * Value = WeakRef of Hubs
     */
    protected static final Hashtable hashCacheSelectAllHub = new Hashtable(37,.75F); // clazz, Hub
    
    
    /** 
     * Used by Cache to store a Hub using a name.
     * Key   = upperCase(Name)
     * Value = Hub
     */
    protected static final Hashtable hashCacheNamedHub = new Hashtable(29,.75F); // clazz, Hub
    
    
	// ============ Get Hashtable Methods =================

	protected static Map<String, Method> getHashClassMethod(Class clazz) {
		Map<String, Method> map = hashClassMethod.get(clazz);
    	if (map == null) {
	    	synchronized (hashClassMethod) {
	        	map =  OAObjectHashDelegate.hashClassMethod.get(clazz);
	        	if (map == null) {
	        		map = new ConcurrentHashMap<String, Method>(37, .75f);
	        		OAObjectHashDelegate.hashClassMethod.put(clazz, map);
	        	}
	    	}	    	
    	}
    	return map;
	}
    protected static Set<String> getHashClassMethodNotFound(Class clazz) {
        Set<String> map = hashClassMethodNotFound.get(clazz);
        if (map == null) {
            synchronized (hashClassMethodNotFound) {
                map =  OAObjectHashDelegate.hashClassMethodNotFound.get(clazz);
                if (map == null) {
                    map = new HashSet<String>(3, .75f);
                    OAObjectHashDelegate.hashClassMethodNotFound.put(clazz, map);
                }
            }           
        }
        return map;
    }
    
	
    // ================== REHASHing =======================
    // ================== REHASHing =======================
    // ================== REHASHing =======================
    // ================== REHASHing =======================
    
    // list of Hashtables that an OAObject could be in.  If ObjectKey is changed, then it needs to be rehashed.
    protected static ArrayList lstRehash = new ArrayList(7);
    static {
    	lstRehash.add(hashLock);
    }
    
    /**
     * This is called by OAObjectKeyDelegate.updateKey() when an OAObject.OAObjectKey is changed so that it can be rehashed.
     * @param oaObj
     * @param keyOld
     * @param keyNew
     */
    public static void rehash(OAObject oaObj, OAObjectKey keyOld) {
    	//OAObjectKey keyNew = OAObjectKeyDelegate.getKey(oaObj);
		for (int i=0; i<lstRehash.size(); i++) {
			Map hash = (Map) lstRehash.get(i);
    		Object value = hash.remove(keyOld);
    		if (value != null) hash.put(oaObj, value);
		}
    }

}



