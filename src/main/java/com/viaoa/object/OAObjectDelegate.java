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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.*;
import com.viaoa.hub.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

/**
 * This is the central Delegate class that performs services for OAObjects.
 * The other Delegate classes are specialized for specific tasks.  This Delegate
 * is used for multi-specific and misc functionality.
 * The Delegates are designed so that the OAObject class can be light weight
 * and have various functionalities built in.
 * @author vincevia
 */
public class OAObjectDelegate {

	private static Logger LOG = Logger.getLogger(OAObjectDelegate.class.getName());
	
	public static final String WORD_New      = "NEW";
	public static final String WORD_Changed  = "CHANGED";
    public static final String WORD_Deleted  = "DELETED";
    public static final String WORD_AutoAdd  = "AutoAdd";
	
	public static final Boolean TRUE  = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);
	
    /** Static global lock used when setting global properties (ex: guidCounter) */

    /** global counter used for local objects.  Value is positive */
    static protected AtomicInteger guidCounter = new AtomicInteger(); // unique identifier needed for objects past from client/server

    /** global counter used for local objects.  Value is negative */
    static protected AtomicInteger localGuidCounter = new AtomicInteger();

    /** Flag to know if finalized objects should be automatically saved.  Default is false. */
    protected static boolean bFinalizeSave = false;

    /** tracks which OAObjects should not automatically add themself to a detailHub when an oaObj property is set. */
    private static final ConcurrentHashMap<Integer, Integer> hmAutoAdd = new ConcurrentHashMap<Integer, Integer>();

    
    /**
	    Called by OAObject constructor to assign guid and initialize new OAObject.
	    If OAObjectFlagDelegate.isLoading() == false then initialize(...) will be called
	    using the values from getOAObjectInfo()
	*/
    protected static void initialize(OAObject oaObj) {
		if (oaObj == null) return;
    	assignGuid(oaObj);  // must get a guid before calling setInConstructor, so that it will have a valid hash key

        /** set OAObject.nulls to know if a primitive property is null or not.
         * All "bits" are flagged/set to 1.  Ordering and positions are set by the position of uppercase/sorted property in array.
         * See: OAObjectInfoDelegate.initialize()
         */
    	OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
    	String[] ps = oi.getPrimitiveProperties();
        int x = (ps==null) ? 0 : ((int) Math.ceil(ps.length / 8.0d));
        oaObj.nulls = new byte[x];
    	
    	if (OAThreadLocalDelegate.isSkipObjectInitialize()) {
    	    return;
    	}

    	initialize(oaObj, oi, oi.getInitializeNewObjects(), oi.getUseDataSource(), oi.getAddToCache(), !oi.getLocalOnly(), true);
    }
    
    /**
     * @param oaObj
     * @param oi if null, then the correct oi will be retrieved.
     * @param bInitializeNulls set all primitive properties to null
     * @param bInitializeWithDS will call OAObjectDSDelegateinitialize()
     * @param bAddToCache if true then call OAObjectCacheDelegate.add()
     * @param bInitializeWithCS if true, then call OAObjectCSDelegate.initialize().
     */
    private static void initialize(OAObject oaObj, OAObjectInfo oi, boolean bInitializeNulls, boolean bInitializeWithDS, boolean bAddToCache, boolean bInitializeWithCS, boolean bSetChangedToFalse) {
    	try {
    		OAThreadLocalDelegate.setLoadingObject(true);
            if (oi == null) oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            
            if (bInitializeNulls) {
                for (int i=0; i<oaObj.nulls.length; i++) {
                    oaObj.nulls[i] = (byte) ~oaObj.nulls[i];  
                }
            }
            
            // 20140307
            for (OALinkInfo li : oi.getLinkInfos()) {
                if (li.getCalculated()) continue;
                if (li.getPrivateMethod()) continue;
                if (li.getMatchProperty() != null) continue;
                // 20140409 added check for 1to1, in which case one side will not have an
                //    fkey, since it uses it's own pkey as the fkey
                if (!OAObjectInfoDelegate.isOne2One(li)) {
                    OAObjectPropertyDelegate.unsafeSetProperty(oaObj, li.getName(), null);
                }
            }
            
	        if (bAddToCache) {  // needs to run before any property could be set, so that OACS changes will find this new object.
	        	OAObjectCacheDelegate.add(oaObj, false, false);  // 20090525, was true,true:  dont add to selectAllHub until after loadingObject is false
	        }
	        
	        if (bInitializeWithCS) {
	        	OAObjectCSDelegate.initialize(oaObj);
	        }
	        if (bInitializeWithDS) {  
	        	OAObjectDSDelegate.initialize(oaObj);
	        }
	    	if (bSetChangedToFalse) {
	    		oaObj.setChanged(false);
	    	}
	    	/*
            OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
            String s = String.format("New, class=%s, id=%s",
                    OAString.getClassName(oaObj.getClass()),
                    key.toString()
            );
            if (oi.bUseDataSource) {
                OAObject.OALOG.fine(s);
            }
            */
    	}
	    finally {
	    	OAThreadLocalDelegate.setLoadingObject(false);
	    }
        if (bAddToCache) {  // 20090525 needs to run after setLoadingObject(false), so that add event is handled correctly.
            OAObjectCacheDelegate.addToSelectAllHubs(oaObj);
        }
    }

    
    /**
	    Flag to know if object is new and has not been saved.
	*/
	public static void setNew(final OAObject oaObj, final boolean b) {
	    if (b == oaObj.newFlag) return;
        boolean old = oaObj.newFlag;
        oaObj.newFlag = b;
        try {
        	OAObjectKeyDelegate.updateKey(oaObj, false);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "oaObj="+oaObj.getClass()+", key="+OAObjectKeyDelegate.getKey(oaObj), e);
        }
    	OAObjectEventDelegate.firePropertyChange(oaObj, WORD_New, old?TRUE:FALSE, b?TRUE:FALSE, false, false);
        if (!b) setAutoAdd(oaObj, true);
        OAObjectHubDelegate.changeHubReferences(oaObj);
	}
	
    protected static void assignGuid(OAObject obj) {
        if (obj.guid != 0) return;
        if (OAObjectInfoDelegate.getOAObjectInfo(obj).getLocalOnly()) { 
        	obj.guid = localGuidCounter.decrementAndGet();
        }
        else {
            if (!OASyncDelegate.isServer()) {
                obj.guid = OAObjectCSDelegate.getServerGuid();
                if (obj.guid == 0) obj.guid = getNextGuid();
            }
            else {
                obj.guid = getNextGuid();
            }
        }
    }

    /**
     * Gets the next GUID for the current computer.
     * also called by OAObjectServerImpl.java
     */
    public static int getNextGuid() { 
        return guidCounter.incrementAndGet();  // cant be 0
    }
    public static int getNextFiftyGuids() { 
        return guidCounter.getAndAdd(50) + 1; 
    }
    
    /**
     * Used when there is a duplicate object created, so that it will not be finalized
     * Called by OAObjectCacheDelegate.add(OAObject, ...) when an object already exists.
     */
    protected static void dontFinalize(OAObject obj) {
        if (obj != null) obj.guid = 0; // flag so that OAObject.finalize should ignore this object.
    }
        
	/**
	 * Used when "reading" serialized objects.
	 */
    protected static void updateGuid(int guid) {
        if (guidCounter.get() < guid) {
            guidCounter.set(guid);
        }
    }
	
	/**
	    Removes object from HubController and calls super.finalize().
	*/
	public static void finalizeObject(OAObject oaObj) {
		if (oaObj.guid == 0) return; // set to 0 by readResolve or ObjectCacheDelegate.add() to ignore finalization
	    if (oaObj.guid > 0 && !oaObj.deletedFlag) {  // set to 0 by readResolve or ObjectCacheDelegate.add() to ignore finalization
            if ((oaObj.changedFlag || oaObj.newFlag) && !OAObjectCSDelegate.isWorkstation()) {

                // 20131128 added autoAttach check
                if (OAObjectDelegate.getAutoAdd(oaObj)) {
                    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
                    if (oi != null && oi.getUseDataSource()) {
                        LOG.fine("object was not saved, object="+oaObj.getClass().getName()+", key="+OAObjectKeyDelegate.getKey(oaObj)+", willSaveNow="+bFinalizeSave);                         
                		if (bFinalizeSave) {
                        	try {
                        	    oaObj.save(OAObject.CASCADE_NONE);
                        	}
                        	catch (Exception e) {
                                LOG.log(Level.WARNING, "object had error while saving, object="+oaObj.getClass().getName()+", key="+OAObjectKeyDelegate.getKey(oaObj), e);                         
                        	}
                        }
                    }
                }
            }
		}
        OAObjectCacheDelegate.removeObject(oaObj);
        if (oaObj.guid > 0) OAObjectCSDelegate.finalizeObject(oaObj);
        hmAutoAdd.remove(oaObj.guid);
        oaObj.hubs = null;
	}
	
    /**
	    Returns true if this object is new or any changes have been made to this object or
	    any objects in Links that are TYPE=MANY and CASCADE=true that match the relationshipType parameter.
	*/
	public static boolean getChanged(OAObject oaObj, int changedRule) {
	    if (changedRule == OAObject.CASCADE_NONE) {
	        return (oaObj.changedFlag || oaObj.newFlag);
	    }
	    OACascade cascade = new OACascade();
	    boolean b = getChanged(oaObj, changedRule, cascade);
	    return b;
	}
	
	public static boolean getChanged(OAObject oaObj, int iCascadeRule, OACascade cascade) {
	    if (oaObj.changedFlag || oaObj.newFlag) {
	    	return true;
	    }
        if (iCascadeRule == oaObj.CASCADE_NONE) return false;
	    if (cascade.wasCascaded(oaObj,true)) return false;

	    if (oaObj.properties == null) return false;
	
	    // check link cascade objects
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	    List al = oi.getLinkInfos();
	    for (int i=0; i < al.size(); i++) {
	    	OALinkInfo li = (OALinkInfo) al.get(i); 
	        String prop = li.getName();
	        if (prop == null || prop.length() < 1) continue;
            if (li.getCalculated()) continue;
	        
	        // same as OAObjectSaveDelegate.cascadeSave()
            if (OAObjectReflectDelegate.isReferenceNullOrNotLoaded(oaObj, prop)) continue;
            
            boolean bValidCascade = false;
            if (iCascadeRule == OAObject.CASCADE_LINK_RULES && li.cascadeSave) bValidCascade = true;
            else if (iCascadeRule == OAObject.CASCADE_OWNED_LINKS && li.getOwner()) bValidCascade = true;
            else if (iCascadeRule == OAObject.CASCADE_ALL_LINKS) bValidCascade = true;
            
            if (OAObjectInfoDelegate.isMany2Many(li)) {
            	Hub hub = (Hub) OAObjectReflectDelegate.getRawReference(oaObj, prop);
            	if (HubDelegate.getChanged(hub, OAObject.CASCADE_NONE, cascade)) return true;
            }
        	if (!bValidCascade) continue;
            
	        Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.name);  // if Hub with Keys, then this will load the correct objects to check
	        if (obj == null) continue;
	
	        if (obj instanceof Hub) {
	            if (OAObjectHubDelegate.getChanged((Hub) obj, iCascadeRule, cascade)) return true;  //  if there have been adds/removes to hub
	        }
	        else {
	            if (obj instanceof OAObject) { // 20110420 could be OANullObject
	                if (getChanged((OAObject)obj, iCascadeRule, cascade)) return true;
	            }
	        }
	    }
	    return false;
	}
	
    /**
	    Used to recursively get all reference objects below this one.  All objects will
	    only be visited once.
	*/
	public static void recurse(OAObject oaObj, OACallback callback) {
	    OACascade cascade = new OACascade();
        recurse(oaObj, callback, cascade);
	}
	
	/** @see #recurse(OACallback) */
	public static void recurse(OAObject oaObj, OACallback callback, OACascade cascade) {
	    if (cascade.wasCascaded(oaObj, true)) return;
	
	    if (callback != null) callback.updateObject(oaObj);
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	    
	    List al = oi.getLinkInfos();
	    for (int i=0; i < al.size(); i++) {
	    	OALinkInfo li = (OALinkInfo) al.get(i); 
            if (li.getCalculated()) continue;
	        String prop = li.name;
	
	        Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.name); // select all
	        if (obj == null) continue;
	
	        if (obj instanceof Hub) {
	            Hub h = (Hub) obj;
	            for (int j=0; ;j++) {
	                Object o = h.elementAt(j);
	                if (o == null) break;
	                if (o instanceof OAObject) {
	                    recurse((OAObject) o, callback, cascade);
	                }
	                else {
	                    if (callback != null) callback.updateObject(o);
	                }
	                Object o2 = h.elementAt(j);
	                if (o != o2) j--;
	            }
	        }
	        else {
	            if (obj instanceof OAObject) {
	                recurse((OAObject) obj, callback, cascade);
	            }
	            else {
	                if (callback != null) callback.updateObject(obj);
	            }
	        }
	    }
	}
	

    protected static Object[] find(OAObject base, String propertyPath, Object findValue, boolean bFindAll) {
        if (propertyPath == null || propertyPath.length() == 0) return null;
        StringTokenizer st = new StringTokenizer(propertyPath, ".");
        Object result = base;
        for ( ;st.hasMoreTokens(); ) {
            String s = st.nextToken();
            base = (OAObject) result;  // previous object
            result = base.getProperty(s);

            if (!st.hasMoreTokens()) {
                // last property, check against findValue
                if (result == findValue || (result != null && result.equals(findValue))) {
                    Object[] objs = new Object[] { base };
                    return objs;
                }
                return null;
            }

            if (result == null) return null;

            if (result instanceof Hub) {
                String pp = null;
                for ( ;st.hasMoreTokens(); ) {
                    s = st.nextToken();
                    if (pp == null) pp = s;
                    else pp += "." + s;
                }
                ArrayList al = null;
                Hub h = (Hub) result;
                for (int ii=0; ;ii++) {
                    Object obj = h.elementAt(ii);
                    if (obj == null) break;
                    Object[] objs = find((OAObject) obj, pp, findValue, bFindAll);
                    if (objs != null) {
                        if (!bFindAll) return objs;
                        if (al == null) al = new ArrayList(10);
                        for (int i3=0; i3<objs.length; i3++) al.add(objs[i3]);
                    }
                }
                if (al == null) return null;
                Object[] objs = new Object[al.size()];
                objs = al.toArray(objs);
                return objs;
            }
            if (!(result instanceof OAObject)) return null;
        }
        return null;
    }
    

    
    
    /**
     * Central method that is used when the object property Key is changed (OAObjectKey)
     * and needs to be rehashed in all Hashtables that it could exist in.
     * @param oaObj
     * @param oldKey
     */
    protected static void rehash(OAObject oaObj, OAObjectKey oldKey) {
	    // Need to rehash all Hashtables that OAObject is stored in:
	    // 1: CacheDelegate hashtable
	    // 2: obj.Hubs - NOTE: not needed, since Hubs dont use hashtables anymore
	    // 3: HashDelegate hashtables
	    
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	    if (oi.getAddToCache()) {
	    	OAObjectCacheDelegate.rehash(oaObj, oldKey);
	    }
	    OAObjectHashDelegate.rehash(oaObj, oldKey);
    }

	public static int getGuid(OAObject obj) {
		if (obj == null) return -1;
		return obj.guid;
	}

	/**
	 * Used to determine if an object should be added to a reference/master hub when one
	 * of it's OAObject properties is set.  If false, then the object will not be added to
	 * masterHubs until this is called with "true" or when oaObj is saved.
	 * @param oaObj
	 * @param bEnabled (default is true)
	 */
	public static void setAutoAdd(OAObject oaObj, boolean bEnabled) {
	    if (oaObj == null) return;
	    if (!bEnabled && !oaObj.isNew()) return;
	    
	    boolean bOld = !hmAutoAdd.contains(oaObj.guid);
	    if (bOld == bEnabled) return;

	    
	    if (!bEnabled) {
	        hmAutoAdd.put(oaObj.guid, oaObj.guid);
	    }
	    else {
            hmAutoAdd.remove(oaObj.guid);
	    }
        OAObjectEventDelegate.firePropertyChange(oaObj, WORD_AutoAdd, bOld?TRUE:FALSE, bEnabled?TRUE:FALSE, false, false);

        if (!bEnabled || oaObj.deletedFlag) return;
        
        try {
            OAThreadLocalDelegate.setSuppressCSMessages(true);               
            // need to see if object should be put into linkOne/masterObject hub(s)             
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
            for (OALinkInfo li : oi.getLinkInfos()) {
                if (li.getType() != li.ONE) continue;
                Object objx = OAObjectReflectDelegate.getRawReference(oaObj, li.getName());
                if (!(objx instanceof OAObject)) continue;
                
                OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
                if (liRev == null) continue;
                if (liRev.getType() != li.MANY) continue;
                if (liRev.getPrivateMethod()) continue;
                
                Object objz = OAObjectReflectDelegate.getProperty((OAObject) objx, liRev.getName());
                if (objz instanceof Hub) {
                    ((Hub) objz).add(oaObj);
                }
            }
        }
        finally {
            OAThreadLocalDelegate.setSuppressCSMessages(false);               
        }
	}
	public static boolean getAutoAdd(OAObject oaObj) {
        if (oaObj == null) return false;
        return hmAutoAdd.get(oaObj.guid) == null;
	}
}

