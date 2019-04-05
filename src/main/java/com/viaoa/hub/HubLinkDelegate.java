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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.viaoa.object.*;
import com.viaoa.util.*;

/**
 * Delegate used for linking Hubs.
 * @author vvia
 *
 */
public class HubLinkDelegate {
    /**
        Main LinkHub method used to link from one hub to another. 
    */
    protected static void setLinkHub(Hub thisHub, String propertyFrom, Hub linkToHub, String propertyTo, boolean linkPosFlag, boolean bAutoCreate, boolean bAutoCreateAllowDups) {
        // 20110809 add bAutoCreateAllowDups
        if (linkToHub == thisHub) return;

        // 20181211 verify that no other shared hub is linked
        Hub hx = HubLinkDelegate.getHubWithLink(thisHub, true);
        if (hx != null && hx != thisHub) {
            String s = "Hub link failed, since another shared hub is already linked, thisHub="+thisHub+", linkToHub="+linkToHub+", propertyTo="+propertyTo;
            throw new RuntimeException(s);
        }
        
        
	    if (thisHub.datau.getLinkToHub() != null) {
	        if (thisHub.datau.getLinkToHub() == linkToHub) {
	            if (thisHub.datau.isAutoCreate() == bAutoCreate && thisHub.datau.isAutoCreateAllowDups() == bAutoCreateAllowDups) {
	                return;
	            }
	        }
	        HubEventDelegate.removeHubListener(thisHub.datau.getLinkToHub(), thisHub.datau.getHubLinkEventListener() );
	        thisHub.datau.setLinkToHub(null);
	        thisHub.datau.setHubLinkEventListener(null);
	        thisHub.datau.setAutoCreate(false);
	        thisHub.datau.setAutoCreateAllowDups(false);
	    }
	    if (linkToHub == null) {
	        HubEventDelegate.fireAfterPropertyChange(thisHub, null, "Link", null, null, null);
	        return;
	    }
	
	    if (propertyTo == null && linkToHub != null) {
	        Class c = linkToHub.getObjectClass();
	        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);  // this never returns null

	        List al = oi.getLinkInfos();
		    for (int i=0; i<al.size(); i++) {
		    	OALinkInfo li = (OALinkInfo) al.get(i);
		    	if (li.getType() != li.ONE) continue;
		    	if (li.getToClass().equals(thisHub.data.objClass)) {
		    		propertyTo = li.getName();
		    		break;
		    	}
		    }
	    }
	
	    Class verifyClass = thisHub.getObjectClass();
	    thisHub.datau.setLinkFromPropertyName(propertyFrom);
	    thisHub.datau.setLinkFromGetMethod(null);
	    if (propertyFrom != null) {  // otherwise, use object
	    	thisHub.datau.setLinkFromGetMethod(OAReflect.getMethod(thisHub.getObjectClass(), "get"+propertyFrom));
	        if (thisHub.datau.getLinkFromGetMethod() == null) throw new RuntimeException("cant find method for property "+propertyFrom);
	        verifyClass = thisHub.datau.getLinkFromGetMethod().getReturnType();
	    }
	
	    thisHub.datau.setLinkToGetMethod(OAReflect.getMethod(linkToHub.getObjectClass(), "get"+propertyTo));
	    if (thisHub.datau.getLinkToGetMethod() == null) {
	        throw new RuntimeException("cant find method for property \""+propertyTo+"\" from linkToHub class="+linkToHub.getObjectClass());
	    }
	    if (!linkPosFlag) {
	        Class c = thisHub.datau.getLinkToGetMethod().getReturnType();
            if ( !c.equals(verifyClass) ) {
                if (c.isPrimitive()) c = OAReflect.getPrimitiveClassWrapper(c);
                if ( !c.equals(verifyClass) ) {
                    throw new RuntimeException("property is wrong class, property="+propertyTo+", class="+c);
                }
            }
        }
        thisHub.datau.setLinkToSetMethod(OAReflect.getMethod(linkToHub.getObjectClass(), "set"+propertyTo));
        if (thisHub.datau.getLinkToSetMethod() == null) throw new RuntimeException("cant find set method for property "+propertyTo);
    
        Class[] cc = thisHub.datau.getLinkToSetMethod().getParameterTypes();
    
        if (!linkPosFlag) {
            if (cc.length == 1 && cc[0].isPrimitive()) cc[0] = OAReflect.getPrimitiveClassWrapper(cc[0]);
            if (cc.length != 1) {
                throw new RuntimeException("wrong type of parameters for method, property:"+propertyTo+" class:" + thisHub.getObjectClass());
            }
            if (!cc[0].equals(verifyClass) ) {
                Class c = verifyClass;
                if (c.isPrimitive()) c = OAReflect.getPrimitiveClassWrapper(c);
                if (!cc[0].equals(c)) {
                    throw new RuntimeException("wrong type of parameter for method, property:"+propertyTo+" class:" + thisHub.getObjectClass());
                }
            }
	    }
	
	    if (thisHub.datau.getLinkToHub() != null) {
	        // remove hub listener from previous linkHub
	    	thisHub.datau.getLinkToHub().removeHubListener(thisHub.datau.getHubLinkEventListener());
	    }
	    thisHub.datau.setLinkPos(linkPosFlag);
	    thisHub.datau.setLinkToHub(linkToHub);
	    thisHub.datau.setLinkToPropertyName(propertyTo);
	    thisHub.datau.setHubLinkEventListener(new HubLinkEventListener(thisHub, linkToHub));
	    thisHub.datau.setAutoCreate(bAutoCreate);
        thisHub.datau.setAutoCreateAllowDups(bAutoCreate && bAutoCreateAllowDups); // 20110809
	    
	    HubEventDelegate.addHubListener(linkToHub, thisHub.datau.getHubLinkEventListener());
	    thisHub.datau.getHubLinkEventListener().onNewList(null);
	    
	    // 20121028
	    Object ao = thisHub.datau.getLinkToHub().getActiveObject();
	    int pos = thisHub.datau.getLinkToHub().getPos();
	    
	    // fire a fake changeActiveObject
        HubEventDelegate.fireAfterChangeActiveObjectEvent(thisHub.datau.getLinkToHub(), ao, pos, true);
        //was: HubEventDelegate.fireAfterChangeActiveObjectEvent(thisHub.datau.linkToHub, thisHub.datau.linkToHub.getActiveObject(), 0, true);
        HubEventDelegate.fireAfterPropertyChange(thisHub, null, "Link", null, null, null);
    }

    public static boolean isLinkAutoCreated(Hub thisHub) {
        return isLinkAutoCreated(thisHub, false);
    }
    // 20131116 
    public static boolean isLinkAutoCreated(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.isAutoCreate()) return true;
        if (!bIncludeCopiedHubs) return false;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.isAutoCreate()) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        return (hubx != null);
    }
    
    public static boolean getLinkedOnPos(Hub thisHub) {
        return getLinkedOnPos(thisHub, false);
    }
    // 20131116 
    public static boolean getLinkedOnPos(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.isLinkPos()) return true;
        if (!bIncludeCopiedHubs) return false;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.isLinkPos()) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        return (hubx != null);
    }
    
    public static void updateLinkProperty(Hub thisHub, Object fromObject, int pos) {
        Hub h = thisHub.datau.getLinkToHub();
        if (h == null || h.datau.isUpdatingActiveObject()) return;
        try {
            _updateLinkProperty(thisHub, fromObject, pos);
        }
        catch (Exception e) {
            throw new RuntimeException("updateLinkProperty, hub="+thisHub+", fromObject="+fromObject, e);
        }
    }   
    
    /** Called by HubAODelegate when ActiveObject is changed in Link From Hub.
        @param linkObj object to update
        @param object new property value
        @param pos, if object is link by position
    */
    private static void _updateLinkProperty(Hub thisHub, Object fromObject, int pos) throws Exception {
        Object linkToObject = null;
        if (thisHub.datau.isAutoCreate()) {
            boolean bOne = false;  // is there only supposed to be one object in hub
            HubDataMaster dm = HubDetailDelegate.getDataMaster(thisHub);
            if (dm != null && dm.liDetailToMaster  != null) {
                OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster);
                if (liRev != null) {
                    bOne = (liRev.getType() == OALinkInfo.ONE);
                }
            }

            if (fromObject == null) {
                if (!bOne || thisHub.getCurrentSize() == 0) return;
                // ?? set reference to null and delete/remove object from hub
                return;
            }
            if (!bOne || thisHub.getSize() == 0) {
                if (!thisHub.datau.isAutoCreateAllowDups()) {  // 20110809 added flag, was: always did this check
                    // see if object already exists
                    for (int i=0; ;i++) {
                        Object obj = thisHub.datau.getLinkToHub().elementAt(i);
                        if (obj == null) break;
                        Object obj2 = thisHub.datau.getLinkToGetMethod().invoke(obj, null);
                        if (obj2 == fromObject) {
                            thisHub.datau.getLinkToHub().setAO(obj);
                            return;
                        }
                    }
                }
                // create new object and link to it
                Class c = thisHub.datau.getLinkToHub().getObjectClass();
                Constructor constructor = c.getConstructor(new Class[] {});
                linkToObject = constructor.newInstance(new Object[] {});

                if (fromObject == null && thisHub.datau.getLinkToSetMethod().getParameterTypes()[0].isPrimitive()) {
                    ((OAObject)linkToObject).setNull(thisHub.datau.getLinkToPropertyName());
                }
                else thisHub.datau.getLinkToSetMethod().invoke(linkToObject, new Object[] { fromObject } );
                
                if (thisHub.datau.getLinkToHub().getObject(linkToObject) == null) { 
                    thisHub.datau.getLinkToHub().add(linkToObject);
                }
                thisHub.datau.getLinkToHub().setAO(linkToObject);
                return;
            }
        }

        if (linkToObject == null) linkToObject = thisHub.datau.getLinkToHub().getActiveObject();
        if (linkToObject != null) {
            Object obj = thisHub.datau.getLinkToGetMethod().invoke(linkToObject, null);
            if (thisHub.datau.isLinkPos()) {  // allow number returned to set pos of active object, set by setLinkOnPos()
                if (obj instanceof Number) {
                    int x = ((Number)obj).intValue();
                    // need to check to see if prop value is null
                    boolean b = false;
                    if (x == pos && linkToObject instanceof OAObject) {
                        b = (pos != -1) && ((OAObject) linkToObject).isNull(thisHub.datau.getLinkToPropertyName());
                    }
                    if (x != pos || b) {
                        thisHub.datau.getLinkToSetMethod().invoke(linkToObject, new Object[] { new Integer(pos) } );
                        if (pos == -1 && linkToObject instanceof OAObject) { // 20131101 setting to null
                            ((OAObject)linkToObject).setNull(thisHub.datau.getLinkToPropertyName());
                        }
                    }
                }
            }
            else {
                if (fromObject != null && thisHub.datau.getLinkFromGetMethod() != null) {
                    // if linking a property to another property
                    fromObject = thisHub.datau.getLinkFromGetMethod().invoke(fromObject, null );
                }

                if (obj != null || fromObject != null) {
                    if ( (obj == null || fromObject == null) || (!obj.equals(fromObject)) ) {
                        if (fromObject == null && thisHub.datau.getLinkToSetMethod().getParameterTypes()[0].isPrimitive()) {
                            ((OAObject)linkToObject).setNull(thisHub.datau.getLinkToPropertyName());
                        }
                        else thisHub.datau.getLinkToSetMethod().invoke(linkToObject, new Object[] { fromObject } );
                    }
                }
            }
        }
    }
    
    /**
        Used to get the property value in the Linked To Hub, that is used to set the Linked From Hub Active Object.
        <p>
        Example:<br>
        If Department Hub is linked to a Employee Hub on property "department", then for
        any Employee object, this will return the value of employee.getDepartment().
        @see Hub#setLinkHub(Hub,String) Full Description of Linking Hubs
    */
    public static Object getPropertyValueInLinkedToHub(Hub thisHub, Object linkObject) {
        Hub h = getHubWithLink(thisHub, true);
        if (h == null) return null;
        return _getPropertyValueInLinkedToHub(h, linkObject);
    }
    private static Object _getPropertyValueInLinkedToHub(Hub thisHub, Object linkObject) {
        if (thisHub.datau.getLinkToGetMethod() == null) return linkObject;
        try {
            if (linkObject != null) {
                if (linkObject instanceof OAObject) {
                    OAObject oa = (OAObject) linkObject;
                    if (oa.isNull(thisHub.datau.getLinkToPropertyName())) {
                        linkObject = null;
                    }
                }
                if (linkObject != null) {
                    linkObject = thisHub.datau.getLinkToGetMethod().invoke(linkObject, null);
                }
            }
            if (thisHub.datau.isLinkPos()) {
                int x = -1;
                if (linkObject != null && linkObject instanceof Number) x = ((Number)linkObject).intValue();
                return thisHub.elementAt(x);
            }
    
            if (thisHub.datau.getLinkFromGetMethod() != null) {
                // if linking a property to another property, need to find which object has matching property
                for (int i=0; ;i++) {
                    Object obj = thisHub.elementAt(i);
                    if (obj == null) {
                        linkObject = null;
                        break;
                    }
                    Object obj2 = thisHub.datau.getLinkFromGetMethod().invoke(obj, null);
                    if ((linkObject == obj2) || (obj2 != null && obj2.equals(linkObject)) ) {
                        linkObject = obj;
                        break;
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return linkObject;
    }
    
    /**
        Returns the property that this Hub is linked to.
        <p>
        Example:<br>
        DepartmentHub linked to Employee.department will return "department"
    
        @see Hub#setLinkHub(Hub,String) Full Description of Linking Hubs
    */
    public static String getLinkToProperty(Hub thisHub) {
        return getLinkToProperty(thisHub, false);
    }
    // 20131116 
    public static String getLinkToProperty(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkToPropertyName() != null) {
            return thisHub.datau.getLinkToPropertyName();
        }
        if (!bIncludeCopiedHubs) return null;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.getLinkToPropertyName() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.getLinkToPropertyName();
    }
    
    public static String getLinkFromProperty(Hub thisHub) {
        return getLinkFromProperty(thisHub, false);
    }
    // 20131116 
    public static String getLinkFromProperty(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkFromPropertyName() != null) return thisHub.datau.getLinkFromPropertyName();
        if (!bIncludeCopiedHubs) return null;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.getLinkFromPropertyName() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.getLinkFromPropertyName();
    }
    
    public static Hub getLinkToHub(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkToHub() != null) return thisHub.datau.getLinkToHub();
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.getLinkToHub() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.getLinkToHub();
    }
    public static Hub getHubWithLink(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkToHub() != null) return thisHub;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.getLinkToHub() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        return hubx;
    }
    
    /**
        Returns true if this Hub is linked to another Hub using the
        position of the active object.
    */
    public static boolean getLinkHubOnPos(Hub thisHub) {
        return getLinkHubOnPos(thisHub, false);
    }
    // 20131116 
    public static boolean getLinkHubOnPos(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.isLinkPos()) return true;
        if (!bIncludeCopiedHubs) return false;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.isLinkPos()) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        return (hubx != null);
    }

    /**
        Used for linking/connecting Hubs,
        to get method used to set the property value of the active object in the masterHub
        to the active object in this Hub.
    */
    public static Method getLinkSetMethod(Hub thisHub) {
        return getLinkSetMethod(thisHub, false);
    }
    // 20131116 
    public static Method getLinkSetMethod(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkToSetMethod() != null) {
            return thisHub.datau.getLinkToSetMethod();
        }
        if (!bIncludeCopiedHubs) return null;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.getLinkToSetMethod() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.getLinkToSetMethod();
    }

    /**
        Used to get value from active object in masterHub, that is then used to set active object in this hub.
    */
    public static Method getLinkGetMethod(Hub thisHub) {
        return getLinkGetMethod(thisHub, false);
    }
    // 20131116 
    public static Method getLinkGetMethod(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkToGetMethod() != null) {
            return thisHub.datau.getLinkToGetMethod();
        }
        if (!bIncludeCopiedHubs) return null;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.getLinkToGetMethod() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.getLinkToGetMethod();
    }

    /**
        Returns the property path of the property that this Hub is
        linked to.
    */
    public static String getLinkHubPath(Hub thisHub) {
        return getLinkHubPath(thisHub, false);
    }
    // 20131116 
    public static String getLinkHubPath(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.getLinkToPropertyName() != null) {
            return thisHub.datau.getLinkToPropertyName();
        }
        if (!bIncludeCopiedHubs) return null;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.getLinkToPropertyName() != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.getLinkToPropertyName();
    }
    
    /**
     * This is called by HubLinkEventListener, (which is created in this class) whenever the linked to (linkToHub) Hub 
     *   is changed (active object or linked to property).
     * This will handle recursive hubs, hubs that have master/detail that are linked to themselves, etc.
     * Called by HubLinkEventListener, which is created by Hub.setLinkHub(...) methods.
     *    ... very tricky :)
     */
    protected static void updateLinkedToHub(Hub fromHub, Hub linkToHub, Object obj) {
        updateLinkedToHub(fromHub, linkToHub, obj, null);
    }
    protected static void updateLinkedToHub(final Hub fromHub, Hub linkToHub, Object obj, String changedPropName) {
        updateLinkedToHub(fromHub, linkToHub, obj, changedPropName, true);
    }
    protected static void updateLinkedToHub(final Hub fromHub, Hub linkToHub, Object obj, String changedPropName, boolean bAdjustMaster) {
        if (fromHub == null) return;
		if (fromHub.datau.isAutoCreate()) return;

        obj = HubLinkDelegate.getPropertyValueInLinkedToHub(fromHub, obj);  // link property value
        if (fromHub.datau.isLinkPos()) {
            HubAODelegate.setActiveObject(fromHub, obj,false,false,false); // adjustMaster, bUpdateLink, force
        }
        else if (obj == null && fromHub.datau.getLinkFromGetMethod() != null && fromHub.datau.getLinkToGetMethod() != null) {  // 20170919 link from prop to prop
            HubAODelegate.setActiveObject(fromHub, null,false,false,false); // adjustMaster, bUpdateLink, force
        }
        else {
            // see if master can be set to null (flag)
            // see if this hub is linked to a master (bForce)
    
            if (obj != null && fromHub.datau.getLinkFromGetMethod() == null) {
                int pos = HubDataDelegate.getPos(fromHub, obj, bAdjustMaster, false);  // adjust master, bUpdateLink
                if (pos < 0 && HubDelegate.isValid(fromHub)) {
                    // add to fromHub
                    //   - only if it does not have a masterObject
                    
                    // 20120716
                    OAFilter<Hub> filter = new OAFilter<Hub>() {
                        @Override
                        public boolean isUsed(Hub h) {
                            return (h.datam.getMasterObject() != null);
                        }
                    };
                    Hub[] hubs = HubShareDelegate.getAllSharedHubs(fromHub, filter);
                    
                    /*
                    //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(fromHub);
                    boolean b = true;
                    for (int i=0; i < hubs.length && b; i++) {
                        if (hubs[i].datam.getMasterObject() != null) {
                            b = false;
                            break;
                        }
                    }
                    */
                    /*
	            	if (b) {
	            	    // 2018117 dont add to fromHub.  Instead use HubCopy (HubFilter), which will add to hub. 
	            	    //was: fromHub.addElement(obj);
	            	}
	            	*/
	            }
	        }
	        else {
	            if (changedPropName == null) {
	            	// Update Master/Detail hubs for the LinkedFromHub
	                // if none of the master hubs have links or details, then set their
	                // activeObject to null
	                Hub h = fromHub;
	                for (; h != null;) {
	                    if (!h.data.isDupAllowAddRemove() && h.getSize() == 1) break;  // detail hub using an object instead of a Hub
	
	                    Hub[] hubs = HubShareDelegate.getAllSharedHubs(h);
	                    int flag = 0;
	                    for (int i=0; i < hubs.length && flag != 5; i++) {
	                        if (hubs[i] == fromHub) continue;
	                        if (hubs[i] == fromHub.getLinkHub(false)) {
	                            flag = 5; // this hub is linked to hubs[i]
	                        }
	                        else if ( (hubs[i].getLinkHub(false) != null) || (hubs[i].datau.getVecHubDetail() != null && hubs[i].datau.getVecHubDetail().size() > 1)) {
                                if (hubs[i].datam == h.datam) flag = 5; // || (hubs[i] == h) flag = 5;
                                else if (hubs[i].getMasterHub() == h.getMasterHub()) flag = 1;
	                        }
	                    }
	                    if (flag < 2 && h != fromHub) HubAODelegate.setActiveObject(h,null,-1,false,false,false); // bUpdateLink, force,bCalledByShareHub
	                    if (flag != 0) break;
	
	                    HubDataMaster dm = HubDetailDelegate.getDataMaster(h);
	                    h = dm.getMasterHub();
	                }
	            }
	        }

/* MIGHT not need this new change (reverted to previous     
 ** ==> use the hubEvent.newList to get the change       
            // 20110808 if AO is not changing in fromHub then need to set force=true so that the fromHub hub listeners will
            //    be notified.  Example:  if masterHub.ao was null, fromHub.ao=null and fromHub was invalid (because masterHub.ao=null)
            //                           then if masterHub.ao is not null, but fromHub.ao was still null (but now is valid)
            HubAODelegate.setActiveObject(fromHub, obj,false,false,true); // adjustMaster, bUpdateLink, force
*/          
///* was:   was checking to see if bForce should be used
            
            
            
            // check for self referring links, where a link is based on master/details that then also have a link back to this hub.
            boolean bForce = false;
            Hub h = fromHub;
            ArrayList<Hub> al = null;
            for (int i=0;!bForce; i++) {
                // 20120717 endless loop caused by recursive hubs
                if (i > 5) {
                    if (al == null) al = new ArrayList<Hub>();
                    else if (al.contains(h)) break;
                    al.add(h);
                    break;
                }
                HubDataMaster dm = HubDetailDelegate.getDataMaster(h);
                // 20110805 recursive hubs could be changing, where a hub could be now sharing the same hub as it's detailHubs
                if (dm.getMasterHub() == h) break;
                h = dm.getMasterHub();
                if (h == null) break;
                if (h == fromHub.getLinkHub(false)) bForce = true; // if this hub is linked to its masterHub
            }

            // 20110810 if fromHub AO=null and linkToHub.AO=null then fromHub.isValid
            //             if linkToHub.AO is changed to != null, but fromHub.AO is still null, then need to set bForce=true
            //                so listeners will be notified of the change
            // ex: in SalesOrder there is a hubCustomer linked to it that needs to know when SalesOrder.AO is not null  
            if (fromHub.getAO() == null && obj == null) bForce = true;
            
            // finally :), change the active object in the from hub.
            HubAODelegate.setActiveObject(fromHub, obj, false, false, bForce); // adjustMaster, bUpdateLink, force
        }
    }
    
    
}


