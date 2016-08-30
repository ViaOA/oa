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
package com.viaoa.hub;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import com.viaoa.object.*;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.*;

/**
 * Main delegate used for working with the Active Object for a Hub.
 * All methods that have an "_" prefix should not be called directly, as there is
 * a calling method that should be used, that performs additional functionality.
 * If a method does not have the "_" prefix and is accessible, then it is ok
 * to call it, but will most likely have a matching method name in the Hub class.
 * @author vincevia
 */
public class HubDelegate {

	public static final Boolean TRUE  = new Boolean(true);
	public static final Boolean FALSE = new Boolean(false);
	
	public static boolean getChanged(Hub thisHub, int iCascadeRule, OACascade cascade) {
	    if (cascade.wasCascaded(thisHub, true)) return false;
	
	    if (HubDataDelegate.getChanged(thisHub)) {
	        return true;
	    }
	    if (iCascadeRule == OAObject.CASCADE_NONE) {
	        return false;
	    }
	    
	    if (thisHub.isOAObject()) {
	        for (int i=0; ; i++) {
	            Object object = HubDataDelegate.getObjectAt(thisHub, i);
	            if (object == null) break;
	            if (object instanceof OAObject) {
	                OAObject obj = (OAObject) object;
	                if ( OAObjectDelegate.getChanged(obj, iCascadeRule, cascade) ) {
	                    return true;
	                }
	            }
	        }
	    }
	    return false;
	}
	
	

    
    /**
	    Verifies that the property in obj is unique for all objects in this Hub (not in all of system).
	    <br>
	    Note: if OAObject and isLoading, then true is always returned.
	    <br>
	    Note: if property value is a null or a blank String, then it is not checked.
	    @returns true if object is valid, false if another object already uses same unique property value.
	*/
	public static boolean verifyUniqueProperty(Hub thisHub, Object object) {
	    if (thisHub == null || object == null) return true;
	
	    if (object instanceof OAObject) {
	        if (OAThreadLocalDelegate.isLoading()) return true;
	    }
	
	    Object object2;
	    Method m = null;
	    try {
	        m = thisHub.data.getUniquePropertyGetMethod();
	        if (m == null) {
	            m = thisHub.datam.getUniquePropertyGetMethod();
                if (m == null) return true;
	        }
	        
	        object2 = m.invoke(object, null);
	        if (object2 == null) return true;
	        if (object2 instanceof String && ((String)object2).equals("")) return true;
	    }
	    catch (Exception e) {
	        String s = m==null?"":m.getName();
	        throw new RuntimeException("Error invoking "+s, e);
	    }
	
	    for (int i=0; ;i++) {
	        Object obj = thisHub.elementAt(i);
	        if (obj == null) break;
	        if (obj == object) continue;
	        try {
	            Object obj2 = m.invoke(obj, null);
	            if (obj2 == null) continue;
	            if (obj2 == object2 || obj2.equals(object2)) return false;
	        }
	        catch (Exception e) {
	            String s = m==null?"":m.getName();
	            throw new RuntimeException("Error invoking "+s, e);
	        }
	    }
	    return true;
	}
	
	protected static Object getRealObject(Hub hub, Object object) {
        if (object != null && !object.getClass().equals(hub.getObjectClass()) ) {
        	Object objx = OAObjectCacheDelegate.get(hub.getObjectClass(), object);
        	if (objx != null) return objx;
            object = HubDataDelegate.getObject(hub, object);  // might not have loaded all data yet (fetchMore will be called)
        }
        return object;
    }

    protected static String getPropertyPathforClasses(Hub hub, Class[] classes) {
        if (classes == null) return null;
        Class c = hub.getObjectClass();
        String path = null;
        int x = classes.length;
        for (int i=0; i<x; i++) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);  // this never returns null

            // find property to use
            List al = oi.getLinkInfos();
            OALinkInfo liFound = null;
            for (int ii=0; ii<al.size(); ii++) {
            	OALinkInfo li = (OALinkInfo) al.get(ii);
            	if (classes[i].equals(li.getToClass())) {
            		if (li.getToClass() == null) {
            			if (liFound != null) continue;
            		}
            		if (liFound != null) {
            		    throw new RuntimeException("more then one link for hubClass="+c+", find linkClass="+classes[i]);
            		}
        			liFound = li;
                	// if (li.getType() == li.ONE) break;  // try to find ONE type, but will settle on MANY
            	}
            }
            if (liFound == null) return null;
            if (path == null) path = liFound.getName();
            else path += "." + liFound.getName();
            c = classes[i];
        }
        return path;
    }

    /**
	    Returns the OAObject that owns this Hub
	    @see HubDetailDelegate#getDataMaster to get the sharedHub that has a DataMaster
	*/
	public static OAObject getMasterObject(Hub hub) {
		if (hub == null) return null;
	    HubDataMaster dm = HubDetailDelegate.getDataMaster(hub);
	    if (dm == null) return null;
	    return dm.masterObject;
	}
	
	
	/**
	    Returns the Class of OAObject that owns this Hub.
	*/
	public static Class getMasterClass(Hub hub) {
		if (hub == null) return null;
		HubDataMaster dm = HubDetailDelegate.getDataMaster(hub);
		Object obj = dm.masterObject;
	    if (obj != null) return obj.getClass();
    	if (dm.masterHub != null) return dm.masterHub.getObjectClass();
    	return null;
	}

    /**
	    Set the Class of objects that this Hub will contain.
	    <p>
	    Note: you can not reset the Object class if there are any objects in it.
	    @param objClass Class for the object being stored
	*/
	public static void setObjectClass(Hub thisHub, Class objClass) {
	    if (thisHub.data.objClass != null && !thisHub.data.objClass.equals(objClass) && !thisHub.data.objClass.equals(OAObject.class)) {
	        if (HubDataDelegate.getCurrentSize(thisHub) > 0 || (thisHub.datau.getVecHubDetail() != null && thisHub.datau.getVecHubDetail().size() > 0) ) {
	            throw new RuntimeException("cant change object class if objects are in hub");
	        }
	        HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
	        if (dm.masterHub != null || thisHub.datam.masterObject != null) {
	            throw new RuntimeException("cant change object class if masterObject exists");
	        }
	        if (thisHub.datau.getSharedHub() != null || HubShareDelegate.getSharedWeakHubSize(thisHub) > 0) {
	            throw new RuntimeException("cant change object class since this is a shared hub.");
	        }
	    }
	    // 20141111 removed since the select could be valid
	    // HubSelectDelegate.cancelSelect(thisHub, true);
	    thisHub.data.objClass = objClass;
	
	    /* 20141111 not needed here
	    if (objClass != null) {
	        // find out if class is OAObject
	    	thisHub.data.setOAObjectFlag(OAObject.class.isAssignableFrom(objClass));
	    	// thisHub.data.setObjectInfo(OAObjectInfoDelegate.getOAObjectInfo(objClass));
	    }
	    else {
	        thisHub.data.setObjectInfo(null);
	        thisHub.data.setOAObjectFlag(false);
	    }
	    */
	}
	
    /**
	    If this hub has a master Hub or link Hub and that Hub does not have an active Object set,
	    then this Hub is invalid.
	    @see #setLinkHub
	    @see #getDetailHub
	    @see #findControllingHub(Hub) to get the Hub that this hub depends on.
	*/
	public static boolean isValid(Hub hub) {
	    HubDataMaster dm = HubDetailDelegate.getDataMaster(hub);
	    if (dm.masterHub != null && dm.masterObject == null) {
	    	return false;
	    }
	    if (hub.datau.getLinkToHub() != null) {
            if (!HubDelegate.isValid(hub.datau.getLinkToHub())) {
                return false;
            }
            
            if (hub.datau.getLinkToHub().dataa.activeObject == null) {
                if (!hub.datau.isAutoCreate()) return false;
            }
            return true;
	    }
	    if (hub.datau.getAddHub() != null) {
	        return HubDelegate.isValid(hub.datau.getAddHub());
	    }
	    return true;
	}
	
	/** created 20110904
	 * Find the hub that controls whether or not this hub is valid.
	 * So that it can also be listened to, so that hub isValid can be recalculated
	 * @return controlling hub, or param hub if there is no controlling hub
	 */
	public static Hub getControllingHub(Hub hub) {
        HubDataMaster dm = HubDetailDelegate.getDataMaster(hub);
        if (dm.masterHub != null) {
            return dm.masterHub;
        }
        if (hub.datau.getLinkToHub() != null) {
            if (hub.datau.isAutoCreate()) {
                return getControllingHub(hub.datau.getLinkToHub());
            }
            return hub.datau.getLinkToHub();
        }        
        if (hub.datau.getAddHub() != null) {
            return HubDelegate.getControllingHub(hub.datau.getAddHub());
        }
        return hub;
	}
	
	/**
	    Returns this hub, or one of its shared Hubs, that has
	    an addHub established.
	    @see #setAddHub
	*/
	public static Hub getAnyAddHub(final Hub hub) {
	    if (hub.getAddHub() != null) return hub;
	    
	    // 20120716
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub h) {
                return h.getAddHub() != null; 
            }
        };
        Hub[] hubs = HubShareDelegate.getAllSharedHubs(hub, filter);
	    
	    // was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(hub);
	    for (int i=0; i<hubs.length; i++) {
	        if (hubs[i].getAddHub() != null) return hubs[i];
	    }
	    return null;
	}

    /**
	    Returns this or any shared hub that uses the same active object and is linked to another Hub.
	*/
	public static Hub getAnyLinkHub(final Hub thisHub) {
	    if (thisHub.getLinkHub() != null) return thisHub;
	    
        // 20120716
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub h) {
                return (h.dataa == thisHub.dataa && h.getLinkHub() != null);
            }
        };
        Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
	    
	    
	    //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub);
	    for (int i=0; i<hubs.length; i++) {
	        if (hubs[i].dataa == thisHub.dataa && hubs[i].getLinkHub() != null) return hubs[i];
	    }
	    return null;
	}

	
	// called by deleteAll() and saveAll()
	protected static void _updateHubAddsAndRemoves(Hub thisHub, OACascade cascade) {
        // removed Objects need to be saved if reference = null.
    	HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
    	boolean bM2M = (dm != null && dm.liDetailToMaster != null && dm.liDetailToMaster.getType() == OALinkInfo.MANY);
    	OALinkInfo liRev = null;
    	if (dm != null && dm.liDetailToMaster != null) {
    		liRev = OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster);
    	}
        
        boolean bHasMethod = true;
        if (dm == null) {
        }
        else if (bM2M) {
    	    bHasMethod = false;
    	    if (dm.masterObject != null && dm.liDetailToMaster != null) { 
    	    	updateMany2ManyLinks(thisHub, dm); // update any link tables
    	    }
    	}
    	else {
        	// 20120907 cases where there is not a public method created, and would use a link table.
            Method method = OAObjectInfoDelegate.getMethod(dm.liDetailToMaster);
        	if (method == null || ((method.getModifiers() & (Modifier.PRIVATE)) > 0) ) {
        	    bHasMethod = false;
                updateMany2ManyLinks(thisHub, dm); // update any link tables
        	}
    	}
    	
		Object[] objs = HubDataDelegate.getRemovedObjects(thisHub);
	    if (objs == null) return;

	    for (int i=0; i<objs.length; i++) {
    		OAObject obj = (OAObject) objs[i];
    		if (obj.getNew()) continue;  // does not exist in DS
    		if (liRev != null && liRev.isOwner()) {
                if (dm.liDetailToMaster != null) {
                    Object ox = OAObjectReflectDelegate.getProperty(obj, dm.liDetailToMaster.getName());
                    if (ox == null) {
                        OAObjectDeleteDelegate.delete(obj, cascade);
                    }
                }
    		}
    		else {
    			if (dm != null && dm.liDetailToMaster != null && bHasMethod) {
	        		Object ox = OAObjectReflectDelegate.getProperty(obj, dm.liDetailToMaster.getName());
	    			if (ox == null) { // else property has been reassigned
                        // 20120925
                        OAObjectDSDelegate.removeReference(obj, dm.liDetailToMaster);
	    			    //was: OAObjectSaveDelegate._saveObjectOnly(obj, cascade);
	    			}
    			}
    		}
    	}
	}
	private static void updateMany2ManyLinks(Hub thisHub, HubDataMaster dm) {
        if (dm == null || dm.liDetailToMaster == null) {
            return;
        }
	    OAObject[] adds = HubAddRemoveDelegate.getAddedObjects(thisHub);
	    OAObject[] removes = HubAddRemoveDelegate.getRemovedObjects(thisHub);
	    
	    boolean b = false;
	    // cross update opposite hub vecAdd/Remove
	    for (int i=0; adds != null && i< adds.length; i++) {
	        b = true;
	    	if (!(adds[i] instanceof OAObject)) continue;
	    	OAObject obj = (OAObject) adds[i];
	    	if (obj.getNew()) continue;
	    	Object objx = OAObjectReflectDelegate.getRawReference(obj, dm.liDetailToMaster.getName());
	    	if (objx instanceof Hub) HubDataDelegate.removeFromAddedList((Hub) objx, dm.masterObject);
	    }
	    for (int i=0; removes != null && i< removes.length; i++) {
            b = true;
	    	if (!(removes[i] instanceof OAObject)) continue;
	    	OAObject obj = (OAObject) removes[i];
	    	Object objx = OAObjectReflectDelegate.getRawReference(obj, dm.liDetailToMaster.getName());
	    	if (objx instanceof Hub) HubDataDelegate.removeFromRemovedList((Hub) objx, dm.masterObject);
	    }
	    if (b) {
	        String propFromMaster = OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster).getName();
	        HubDSDelegate.updateMany2ManyLinks(dm.masterObject, adds, removes, propFromMaster);
	    }
    }
	
	
    public static void setUniqueProperty(Hub thisHub, String propertyName) {
        if (propertyName == null) {
        	thisHub.data.setUniqueProperty(null);
        	thisHub.data.setUniquePropertyGetMethod(null);
            return;
        }
        if (propertyName.indexOf('.') >= 0) {
            throw new IllegalArgumentException("Property "+propertyName+" can only be for a property in "+thisHub.getObjectClass().getName());
        }

        thisHub.data.setUniquePropertyGetMethod(OAObjectInfoDelegate.getMethod(thisHub.getObjectClass(), "get"+propertyName));
        if (thisHub.data.getUniquePropertyGetMethod() == null) {
            throw new IllegalArgumentException("Get Method for Property "+propertyName+" not found");
        }
        if (thisHub.data.getUniquePropertyGetMethod().getParameterTypes().length > 0) {
            throw new IllegalArgumentException("Get Method for Property "+propertyName+" expects parameters");
        }
        thisHub.data.setUniqueProperty(propertyName);
    }
    
    
    /**
	    Used to update a property in each object to equal/store the position within this Hub.
	    @param property is neme of property to update.
	    @param startNumber is number to begin numbering at.  Default is 0, which will match the Hub position.
	    @param bKeepSeq, if false then seq numbers are not updated when an object is removed
	*/
	public static void setAutoSequence(Hub thisHub, String property, int startNumber, boolean bKeepSeq) {
	    // 20091030 only set for server for detail hubs
	    boolean bServerOnly = false;
	    if (thisHub.getMasterObject() != null) {	    
	        if (!HubCSDelegate.isServer(thisHub)) return; // only set up for server
	        bServerOnly = true;
	    }
        if (thisHub.data.getAutoSequence() != null) thisHub.data.getAutoSequence().close();
        thisHub.cancelSort(); // 20090801 need to remove any sorters
    	thisHub.data.setAutoSequence(new HubAutoSequence(thisHub, property, startNumber, bKeepSeq, bServerOnly));
	}
    public static HubAutoSequence getAutoSequence(Hub thisHub) {
        return thisHub.data.getAutoSequence();
    }
    
    /**
	    Makes sure that for each object in a hubMaster, there exists an object in this hub where property
	    is equal to the hubMaster object.
	
	    @param hubMaster hub with list of object
	    @param property Property in this hubs objects that match object type in hubMaster
	*/
	public static void setAutoMatch(Hub thisHub, String property, Hub hubMaster, boolean bServerSideOnly) {
	    if (thisHub.data.getAutoMatch() != null) thisHub.data.getAutoMatch().close();
	    if (hubMaster != null) {
	        HubAutoMatch am = new HubAutoMatch();
            thisHub.data.setAutoMatch(am);
            am.setServerSideOnly(bServerSideOnly);
	        am.init(thisHub, property, hubMaster);
	    }
	}
    public static HubAutoMatch getAutoMatch(Hub thisHub) {
        return thisHub.data.getAutoMatch();
    }

    public static int getSize(Hub thisHub) {
        if (HubSelectDelegate.isMoreData(thisHub)) {
            if (!HubSelectDelegate.isCounted(thisHub)) {
            	if (HubDataDelegate.getCurrentSize(thisHub) == 0) {
            	    HubSelectDelegate.fetchMore(thisHub); // see if this will load it, before calling count on the select
                    if (!HubSelectDelegate.isMoreData(thisHub)) {
                        return getSize(thisHub);
                    }
            	}
            }
            int x = HubSelectDelegate.getCount(thisHub);
            if (x > 0) return x;
        }
       	return HubDataDelegate.getCurrentSize(thisHub);
    }

    protected static void setProperty(Hub thisHub, String name, Object obj) {
        if (name == null) return;
        name = name.toUpperCase();
        if (thisHub.data.getHashProperty() == null) thisHub.data.setHashProperty(new Hashtable(7));
        thisHub.data.getHashProperty().put(name, (obj==null)?OANullObject.instance:obj);
    }
    protected static Object getProperty(Hub thisHub, String name) {
        if (thisHub.data.getHashProperty() == null) return null;

        name = name.toUpperCase();
        Object obj = thisHub.data.getHashProperty().get(name);
        if (obj instanceof OANullObject) obj = null;
        return obj;
    }
    protected static void removeProperty(Hub thisHub, String name) {
        if (thisHub.data.getHashProperty() != null) {
            name = name.toUpperCase();
            thisHub.data.getHashProperty().remove(name);
        }
    }
    
    // 20141030
    public static void setReferenceable(Hub hub, boolean bReferenceable) {
        if (hub == null) return;
        if (!OASyncDelegate.isServer(hub)) return;

        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(hub.getObjectClass());
        if (!OAObjectInfoDelegate.isWeakReferenceable(oi)) return;
        boolean bSupportStorage = oi.getSupportsStorage();
        
        Object master = HubDelegate.getMasterObject(hub);
        if (!(master instanceof OAObject)) return;

        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li == null) return;
        OALinkInfo liRev = li.getReverseLinkInfo();
        if (liRev == null) return;
        
        if (liRev.getCacheSize() > 0) {
            if (bReferenceable || bSupportStorage) {
                boolean b = OAObjectPropertyDelegate.setPropertyWeakRef((OAObject) master, liRev.getName(), !bReferenceable);
                if (!b) return; // already done, dont need to check/change parents
            }
        }
        
        if (bReferenceable) {
            // make parents referenceable
            OAObjectPropertyDelegate.setReferenceable((OAObject)master, bReferenceable);
        }
    }
}

