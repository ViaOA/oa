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
package com.viaoa.hub;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
	    @see Hub#setLinkHub(Hub,String) Full Description of Linking Hubs
	    @see HubLink
	*/
	protected static void setLinkHub(Hub thisHub, String propertyFrom, Hub linkToHub, String propertyTo, boolean linkPosFlag, boolean bAutoCreate, boolean bAutoCreateAllowDups) {
	    // 20110809 add bAutoCreateAllowDups
	    if (linkToHub == thisHub) return;
	
	    if (thisHub.datau.linkToHub != null) {
	        if (thisHub.datau.linkToHub == linkToHub) return;
	        HubEventDelegate.removeHubListener(thisHub.datau.linkToHub, thisHub.datau.hubLinkEventListener );
	        thisHub.datau.linkToHub = null;
	        thisHub.datau.hubLinkEventListener = null;
	        thisHub.datau.bAutoCreate = false;
	        thisHub.datau.bAutoCreateAllowDups = false;
	    }
	    if (linkToHub == null) {
	        //qqqqqqqqq figure out what to do with this: firePropertyChange("link",false, false);
	        return;
	    }
	
	    if (propertyTo == null && linkToHub != null) {
	        Class c = linkToHub.getObjectClass();
	        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(c);  // this never returns null
    
		    ArrayList al = oi.getLinkInfos();
		    for (int i=0; i<al.size(); i++) {
		    	OALinkInfo li = (OALinkInfo) al.get(i);
		    	if (li.getType() != li.ONE) continue;
		    	if (thisHub.datau.objClass.equals(li.getToClass()) ) {
		    		propertyTo = li.getName();
		    		break;
		    	}
		    }
	    }
	
	    Class verifyClass = thisHub.getObjectClass();
	    thisHub.datau.linkFromPropertyName = propertyFrom;
	    thisHub.datau.linkFromGetMethod = null;
	    if (propertyFrom != null) {  // otherwise, use object
	    	thisHub.datau.linkFromGetMethod = OAReflect.getMethod(thisHub.getObjectClass(), "get"+propertyFrom);
	        if (thisHub.datau.linkFromGetMethod == null) throw new RuntimeException("cant find method for property "+propertyFrom);
	        verifyClass = thisHub.datau.linkFromGetMethod.getReturnType();
	    }
	
	    thisHub.datau.linkToGetMethod = OAReflect.getMethod(linkToHub.getObjectClass(), "get"+propertyTo);
	    if (thisHub.datau.linkToGetMethod == null) {
	        throw new RuntimeException("cant find method for property \""+propertyTo+"\" from linkToHub class="+linkToHub.getObjectClass().getName());
	    }
	    if (!linkPosFlag) {
	        Class c = thisHub.datau.linkToGetMethod.getReturnType();
            if ( !c.equals(verifyClass) ) {
                if (c.isPrimitive()) c = OAReflect.getPrimitiveClassWrapper(c);
                if ( !c.equals(verifyClass) ) {
                    throw new RuntimeException("property is wrong class, property="+propertyTo+", class="+c);
                }
            }
	    }
	    thisHub.datau.linkToSetMethod = OAReflect.getMethod(linkToHub.getObjectClass(), "set"+propertyTo);
	    if (thisHub.datau.linkToSetMethod == null) throw new RuntimeException("cant find set method for property "+propertyTo);
	
	    Class[] cc = thisHub.datau.linkToSetMethod.getParameterTypes();
	
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
	
	    if (thisHub.datau.linkToHub != null) {
	        // remove hub listener from previous linkHub
	    	thisHub.datau.linkToHub.removeHubListener(thisHub.datau.hubLinkEventListener);
	    }
	    thisHub.datau.linkPos = linkPosFlag;
	    thisHub.datau.linkToHub = linkToHub;
	    thisHub.datau.linkToPropertyName = propertyTo;
	    thisHub.datau.hubLinkEventListener = new HubLinkEventListener(thisHub, linkToHub);
	    thisHub.datau.bAutoCreate = bAutoCreate;
        thisHub.datau.bAutoCreateAllowDups = bAutoCreate && bAutoCreateAllowDups; // 20110809
	    
	    HubEventDelegate.addHubListener(linkToHub, thisHub.datau.hubLinkEventListener);
	    thisHub.datau.hubLinkEventListener.onNewList(null);
	    
	    // 20121028
	    Object ao = thisHub.datau.linkToHub.getActiveObject();
	    int pos = thisHub.datau.linkToHub.getPos();
	    
	    // fire a fake changeActiveObject
        HubEventDelegate.fireAfterChangeActiveObjectEvent(thisHub.datau.linkToHub, ao, pos, true);
	    //was: HubEventDelegate.fireAfterChangeActiveObjectEvent(thisHub.datau.linkToHub, thisHub.datau.linkToHub.getActiveObject(), 0, true);
	    
	    HubEventDelegate.fireAfterPropertyChange(thisHub, null, "Link", null, null, null);
	}

	public static boolean isLinkAutoCreated(Hub thisHub) {
	    return isLinkAutoCreated(thisHub, false);
	}
	// 20131116	
    public static boolean isLinkAutoCreated(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.bAutoCreate) return true;
        if (!bIncludeCopiedHubs) return false;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.bAutoCreate) {
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
        if (thisHub.datau.linkPos) return true;
        if (!bIncludeCopiedHubs) return false;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.linkPos) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        return (hubx != null);
    }
	
    public static void updateLinkProperty(Hub thisHub, Object fromObject, int pos) {
        if (thisHub.datau.linkToHub == null || thisHub.datau.linkToHub.datau.bUpdatingActiveObject) return;
        try {
        	_updateLinkProperty(thisHub, fromObject, pos);
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
    }	
    
	/** Called by HubAODelegate when ActiveObject is changed in Link From Hub.
        @param linkObj object to update
        @param object new property value
        @param pos, if object is link by position
    */
    private static void _updateLinkProperty(Hub thisHub, Object fromObject, int pos) throws Exception {
    	Object linkToObject = null;
        if (thisHub.datau.bAutoCreate) {
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
                if (!thisHub.datau.bAutoCreateAllowDups) {  // 20110809 added flag, was: always did this check
                    // see if object already exists
                    for (int i=0; ;i++) {
                        Object obj = thisHub.datau.linkToHub.elementAt(i);
                        if (obj == null) break;
                        Object obj2 = thisHub.datau.linkToGetMethod.invoke(obj, null);
                        if (obj2 == fromObject) {
                        	thisHub.datau.linkToHub.setAO(obj);
                            return;
                        }
                    }
                }
                // create new object and link to it
                Class c = thisHub.datau.linkToHub.getObjectClass();
                Constructor constructor = c.getConstructor(new Class[] {});
                linkToObject = constructor.newInstance(new Object[] {});
                thisHub.datau.linkToSetMethod.invoke(linkToObject, new Object[] { fromObject } );
                if (thisHub.datau.linkToHub.getObject(linkToObject) == null) { 
                    thisHub.datau.linkToHub.add(linkToObject);
                }
                thisHub.datau.linkToHub.setAO(linkToObject);
                return;
            }
        }

        if (linkToObject == null) linkToObject = thisHub.datau.linkToHub.getActiveObject();
        if (linkToObject != null) {
            Object obj = thisHub.datau.linkToGetMethod.invoke(linkToObject, null);
            if (thisHub.datau.linkPos) {  // allow number returned to set pos of active object, set by setLinkOnPos()
                if (obj instanceof Number) {
                    int x = ((Number)obj).intValue();
                    if (x != pos) {
                        thisHub.datau.linkToSetMethod.invoke(linkToObject, new Object[] { new Integer(pos) } );
                        if (pos == -1 && linkToObject instanceof OAObject) { // 20131101 setting to null
                            ((OAObject)linkToObject).setNull(thisHub.datau.linkToPropertyName);
                        }
                    }
                }
            }
            else {
                if (fromObject != null && thisHub.datau.linkFromGetMethod != null) {
                    // if linking a property to another property
                    fromObject = thisHub.datau.linkFromGetMethod.invoke(fromObject, null );
                }

                if (obj != null || fromObject != null) {
                    if ( (obj == null || fromObject == null) || (!obj.equals(fromObject)) ) {
                        thisHub.datau.linkToSetMethod.invoke(linkToObject, new Object[] { fromObject } );
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
	    if (thisHub.datau.linkToGetMethod == null) return linkObject;
	    try {
	        if (linkObject != null) {
	            if (linkObject instanceof OAObject) {
	                OAObject oa = (OAObject) linkObject;
	                if (oa.isNull(thisHub.datau.linkToPropertyName)) {
	                    linkObject = null;
	                }
	            }
	            if (linkObject != null) {
	                linkObject = thisHub.datau.linkToGetMethod.invoke(linkObject, null);
	            }
	        }
	        if (thisHub.datau.linkPos) {
	            int x = -1;
	            if (linkObject != null && linkObject instanceof Number) x = ((Number)linkObject).intValue();
	            return thisHub.elementAt(x);
	        }
	
	        if (thisHub.datau.linkFromGetMethod != null) {
	            // if linking a property to another property, need to find which object has matching property
	            for (int i=0; ;i++) {
	                Object obj = thisHub.elementAt(i);
	                if (obj == null) {
	                    linkObject = null;
	                    break;
	                }
	                Object obj2 = thisHub.datau.linkFromGetMethod.invoke(obj, null);
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
        if (thisHub.datau.linkToPropertyName != null) {
            return thisHub.datau.linkToPropertyName;
        }
        if (!bIncludeCopiedHubs) return null;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.linkToPropertyName != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.linkToPropertyName;
    }
	
	public static String getLinkFromProperty(Hub thisHub) {
        return getLinkFromProperty(thisHub, false);
	}
    // 20131116 
    public static String getLinkFromProperty(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkFromPropertyName != null) return thisHub.datau.linkFromPropertyName;
        if (!bIncludeCopiedHubs) return null;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.linkFromPropertyName != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.linkFromPropertyName;
    }
	
    public static Hub getLinkToHub(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkToHub != null) return thisHub.datau.linkToHub;
        if (!bIncludeCopiedHubs) return null;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.linkToHub != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.linkToHub;
    }
    public static Hub getHubWithLink(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkToHub != null) return thisHub;
        if (!bIncludeCopiedHubs) return null;
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h.datau.linkToHub != null) {
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
	    @see Hub#setLink(Hub,String) Full Description of Linking Hubs
	    @see HubLink
	*/
	public static boolean getLinkHubOnPos(Hub thisHub) {
	    return getLinkHubOnPos(thisHub, false);
	}
    // 20131116 
    public static boolean getLinkHubOnPos(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkPos) return true;
        if (!bIncludeCopiedHubs) return false;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.linkPos) {
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
	    @see updateLinkProperty
	    @see Hub#setLink(Hub,String) Full Description of Linking Hubs
	    @see HubLink
	*/
	public static Method getLinkSetMethod(Hub thisHub) {
	    return getLinkSetMethod(thisHub, false);
	}
    // 20131116 
    public static Method getLinkSetMethod(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkToSetMethod != null) {
            return thisHub.datau.linkToSetMethod;
        }
        if (!bIncludeCopiedHubs) return null;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.linkToSetMethod != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.linkToSetMethod;
    }

    /**
	    Used to get value from active object in masterHub, that is then used to set active object in this hub.
	    @see Hub#setLink(Hub,String) Full Description of Linking Hubs
	    @see HubLink
	*/
	public static Method getLinkGetMethod(Hub thisHub) {
	    return getLinkGetMethod(thisHub, false);
	}
    // 20131116 
    public static Method getLinkGetMethod(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkToGetMethod != null) {
            return thisHub.datau.linkToGetMethod;
        }
        if (!bIncludeCopiedHubs) return null;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.linkToGetMethod != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.linkToGetMethod;
    }

	/**
	    Returns the property path of the property that this Hub is
	    linked to.
	    @see Hub#setLink(Hub,String) Full Description of Linking Hubs
	    @see HubLink
	*/
	public static String getLinkHubPath(Hub thisHub) {
	    return getLinkHubPath(thisHub, false);
	}
    // 20131116 
    public static String getLinkHubPath(final Hub thisHub, boolean bIncludeCopiedHubs) {
        if (thisHub.datau.linkToPropertyName != null) {
            return thisHub.datau.linkToPropertyName;
        }
        if (!bIncludeCopiedHubs) return null;
        
        Hub hubx = HubShareDelegate.getFirstSharedHub(thisHub, new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub obj) {
                Hub h = (Hub) obj;
                if (h == thisHub) return false;
                if (h.datau.linkToPropertyName != null) {
                    return true;
                }
                return false;
            }
        }, bIncludeCopiedHubs, true);
        if (hubx == null) return null;
        return hubx.datau.linkToPropertyName;
    }
	
	
	
	/**
	 * This is called by HubLinkEventListener, (which is created in this class) whenever the linked to (linkToHub) Hub 
	 *   is changed (active object or linked to property).
	 * This will handle recursive hubs, hubs that have master/detail that are linked to themselves, etc.
	 * Called by HubLinkEventListener, which is created by Hub.setLinkHub(...) methods.
	 */
	protected static void updateLinkedToHub(Hub fromHub, Hub linkToHub, Object obj) {
		updateLinkedToHub(fromHub, linkToHub, obj, null);
	}
	protected static void updateLinkedToHub(final Hub fromHub, Hub linkToHub, Object obj, String changedPropName) {
		if (fromHub.datau.bAutoCreate) return;

	    obj = HubLinkDelegate.getPropertyValueInLinkedToHub(fromHub, obj);  // link property value
	    if (fromHub.datau.linkPos) {
	    	fromHub.setActiveObject(obj);
	    }
	    else {
	        // see if master can be set to null (flag)
	        // see if this hub is linked to a master (bForce)
	
	        if (obj != null && fromHub.datau.linkFromGetMethod == null) {
	            int pos = HubDataDelegate.getPos(fromHub, obj, true, false);  // adjust master, bUpdateLink
	            if (pos < 0 && HubDelegate.isValid(fromHub)) {
	            	// add to fromHub
	            	//   - only if it does not have a masterObject
	                
	                // 20120716
	                OAFilter<Hub> filter = new OAFilter<Hub>() {
	                    @Override
	                    public boolean isUsed(Hub h) {
	                        return (h.datam.masterObject != null);
	                    }
	                };
	                Hub[] hubs = HubShareDelegate.getAllSharedHubs(fromHub, filter);
	                
                    //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(fromHub);
                    boolean b = true;
                    for (int i=0; i < hubs.length && b; i++) {
                        if (hubs[i].datam.masterObject != null) b = false;
                    }
	            	if (b) fromHub.addElement(obj);
	            }
	        }
	        else {
	            if (changedPropName == null) {
	            	// Update Master/Detail hubs for the LinkedFromHub
	                // if none of the master hubs have links or details, then set their
	                // activeObject to null
	                Hub h = fromHub;
	                for (; h != null;) {
	                    if (!h.datau.dupAllowAddRemove && h.getSize() == 1) break;  // detail hub using an object instead of a Hub
	
	                    Hub[] hubs = HubShareDelegate.getAllSharedHubs(h);
	                    int flag = 0;
	                    for (int i=0; i < hubs.length && flag == 0; i++) {
	                        if (hubs[i] == fromHub) continue;
	                        if (hubs[i] == fromHub.getLinkHub()) flag = 5; // this hub is linked to hubs[i]
	
	                        if ( (hubs[i].getLinkHub() != null) || (hubs[i].datau.vecHubDetail != null && hubs[i].datau.vecHubDetail.size() > 1)) {
	                            if (hubs[i].getMasterHub() == h.getMasterHub()) flag = 1;
	                            if (hubs[i].datam == h.datam) flag = 5; // || (hubs[i] == h) flag = 5;
	                        }
	                    }
	                    if (flag < 2 && h != fromHub) HubAODelegate.setActiveObject(h,null,-1,false,false,false); // bUpdateLink, force,bCalledByShareHub
	                    if (flag != 0) break;
	
	                    HubDataMaster dm = HubDetailDelegate.getDataMaster(h);
	                    h = dm.masterHub;
	                }
	            }
	        }

/*qqqqqqq MIGHT not need this new change (reverted to previous) qqqqqqqqqqqq	 
 ** ==> use the hubEvent.newList to get the change       
            // 20110808 if AO is not changing in fromHub then need to set force=true so that the fromHub hub listeners will
	        //    be notified.  Example:  if masterHub.ao was null, fromHub.ao=null and fromHub was invalid (because masterHub.ao=null)
	        //                           then if masterHub.ao is not null, but fromHub.ao was still null (but now is valid)
            HubAODelegate.setActiveObject(fromHub, obj,false,false,true); // adjustMaster, bUpdateLink, force
*/	        
///* was:	was checking to see if bForce should be used
	        
	        
	        
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
	            if (dm.masterHub == h) break;
	            h = dm.masterHub;
	            if (h == null) break;
	            if (h == fromHub.getLinkHub()) bForce = true; // if this hub is linked to its masterHub
	        }

	        // 20110810 if fromHub AO=null and linkToHub.AO=null then fromHub.isValid
	        //             if linkToHub.AO is changed to != null, but fromHub.AO is still null, then need to set bForce=true
	        //                so listeners will be notified of the change
	        // ex: in SalesOrder there is a hubCustomer linked to it that needs to know when SalesOrder.AO is not null  
	        if (fromHub.getAO() == null && obj == null) bForce = true;
	        
	        // finally :), change the active object in the from hub.
	        HubAODelegate.setActiveObject(fromHub, obj,false,false,bForce); // adjustMaster, bUpdateLink, force
	        
// 20110905 might not need this, since Enable/Visible Controllers now listen to linkToHub	        
	    }
	}
	
	
}


