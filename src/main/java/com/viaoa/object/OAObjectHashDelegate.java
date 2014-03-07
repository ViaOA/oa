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


import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    static private ConcurrentHashMap<Class, Map<String, Method>> hashClassMethod = new ConcurrentHashMap<Class, Map<String, Method>>(151, 0.75F);
    static private ConcurrentHashMap<Class, Set<String>> hashClassMethodNotFound = new ConcurrentHashMap<Class, Set<String>>(151, 0.75F);
	
	
    /** 
     * Used by OALinkInfo to cache Hubs so that they are not strong linked within object.  
     * Key   = OALinkInfo
     * Value = Vector of Hubs.  The number of hubs is determined by OALinkInfo.setCacheSize(x) method.
     */
	protected static Hashtable hashLinkInfoCache = new Hashtable(47,0.75f);

	
	/** 
	 *  Used by OAObjectInfoDelegate to store the Root Hub for recursive classes.
     * 	Key   = Class
     * 	Value = Hub (that is the 'top' (root) Hub of a recursive hub) 
	 */
	protected static Hashtable<OAObjectInfo, Hub> hashRootHub = new Hashtable<OAObjectInfo, Hub>(13, .75f);
	

    
    /** 
     * Static cache of OAObjectInfo, keyed on Class.
     * Key   = Class
     * Value = OAObjectINfo 
     */
    static protected ConcurrentHashMap<Class, OAObjectInfo> hashObjectInfo = new ConcurrentHashMap<Class, OAObjectInfo>(147, 0.75F);
    public static Map<Class, OAObjectInfo> getObjectInfoHash() {
    	return hashObjectInfo;
    }
    
    
    
    /** 
     * Locking support for OAObject.  See OALock
     * Key   = OAObject
     * Value = OALock
     */
    protected static Hashtable hashLock = new Hashtable(11, 0.75F);
    
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
	protected static Hashtable hashCacheListener = new Hashtable(51, .75F); // stores vector per class
    
	
    /** 
     * Used by Cache to store all hubs that have selected all objects.  
     * Key   =  Class
     * Value = WeakRef of Hubs
     */
    protected static Hashtable hashCacheSelectAllHub = new Hashtable(37,.75F); // clazz, Hub
    
    
    /** 
     * Used by Cache to store a Hub using a name.
     * Key   = upperCase(Name)
     * Value = Hub
     */
    protected static Hashtable hashCacheNamedHub = new Hashtable(29,.75F); // clazz, Hub
    
    
	// ============ Get Hashtable Methods =================

	protected static Map<String, Method> getHashClassMethod(Class clazz) {
		Map<String, Method> map = hashClassMethod.get(clazz);
    	if (map == null) {
	    	synchronized (hashClassMethod) {
		    	if (map == null) {
		        	map =  OAObjectHashDelegate.hashClassMethod.get(clazz);
		        	if (map == null) {
		        		map = new ConcurrentHashMap<String, Method>(37, .75f);
		        		OAObjectHashDelegate.hashClassMethod.put(clazz, map);
		        	}
		    	}
	    	}	    	
    	}
    	return map;
	}
    protected static Set<String> getHashClassMethodNotFound(Class clazz) {
        Set<String> map = hashClassMethodNotFound.get(clazz);
        if (map == null) {
            synchronized (hashClassMethodNotFound) {
                if (map == null) {
                    map =  OAObjectHashDelegate.hashClassMethodNotFound.get(clazz);
                    if (map == null) {
                        map = new HashSet<String>(3, .75f);
                        OAObjectHashDelegate.hashClassMethodNotFound.put(clazz, map);
                    }
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



