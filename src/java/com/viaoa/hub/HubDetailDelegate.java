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


import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.util.OAFilter;

/**
 * Delegate that manages master/detail functionality for Hubs.
 * @author vvia
 *
 */
public class HubDetailDelegate {
    private static Logger LOG = Logger.getLogger(HubDetailDelegate.class.getName());
    
    /**
        Used to create Master/Detail relationships.
        Set the controlling/master hub for this hub
        @param path is the property path from masterHub to get to this hub
     */
    public static void setMasterHub(Hub thisHub, Hub masterHub, String path, boolean bShared, String selectOrder) {
        if (thisHub.datau.sharedHub != null) {
            if (masterHub == null) {
                throw new RuntimeException("sharedHub cant have a master hub");
            }
        }
    
        if (thisHub.datam.masterHub != null) {
            // this will set all props back to default values
            thisHub.datam.masterHub.removeDetailHub(thisHub);
        }
    
        if (masterHub != null) {
            getDetailHub(masterHub, path, null, thisHub.getObjectClass(), thisHub, bShared, selectOrder);
        }
    }
    
    
    // if getPos(object) is not found and a masterHub exists, then the
    // masterHub needs to be searched and then this Hub will be able to find the object
    // in the updated list
    // called when object is not found and there is a master hub
    // added 4/11: checkLink option, if masterHub has a linkHub, then it will not be adjusted
    protected static boolean setMasterHubActiveObject(Hub thisHub, Object detailObject, boolean bUpdateLink) {
        // make sure none of these have a linkHub
        // and find the sharedHub that has a masterHub
        HubDataMaster dm = getDataMaster(thisHub);
        boolean result = false;
        if (dm.masterHub != null && dm.liDetailToMaster != null) {

            if (dm.liDetailToMaster.getType() == OALinkInfo.MANY) { // Many2Many link
                Hub h = (Hub) OAObjectReflectDelegate.getProperty((OAObject)detailObject, dm.liDetailToMaster.getName());
                dm.masterHub.setSharedHub(h, false);
                HubAODelegate.setActiveObject(dm.masterHub, 0, false, false,false); // pick any one, so that detailObject will be in it.
                return true;
            }
            Object obj = OAObjectReflectDelegate.getProperty((OAObject)detailObject, dm.liDetailToMaster.getName());
            // 20121010 if obj==null then dont adjust:  ex: hi5  employeeAward.awardType that was from program.awardTypes, and now the list is in location.awardTpes
            if (obj != null && dm.masterHub.getActiveObject() != obj ) { 
            //was: if (dm.masterHub.getActiveObject() != obj) {
                if (dm.masterHub.datau.bUpdatingActiveObject) return false;
                // see if masterHub (or a share of it) has a link
                //  if it does, then dont allow it to adjustMaster
                HubAODelegate.setActiveObject(dm.masterHub, obj, true, bUpdateLink, false); // adjustMaster, updateLink, force
                result = true;
            }
        }
        return result;
    }

    
    /**
        Called by add(), insert(), remove to update an object's reference property to that of the master object.
    */
    protected static void setPropertyToMasterHub(Hub thisHub, Object detailObject, Object objMaster) {
        if (detailObject == null) return;
        if (objMaster == null) return;
        HubDataMaster dm = getDataMaster(thisHub, objMaster.getClass());
        if (dm.liDetailToMaster == null) return;

        // 20090705
        if (thisHub.data.bInFetch) return;
        
        
        // 20120920 if thisHub is a detailHub of type=One, then need to update the masterObj.linkProp
        OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster);
        if (liRev != null && liRev.getType() == OALinkInfo.ONE) {
            if (objMaster == null) {
                // remove was called
                Object objx = HubDetailDelegate.getMasterObject(thisHub);
                if (objx != null) {
                    OAObjectReflectDelegate.setProperty((OAObject)objx, liRev.getName(), null, null);
                }
            }
            else {
                // add was called
                OAObjectReflectDelegate.setProperty((OAObject)objMaster, liRev.getName(), detailObject, null);
                // the AO will also be set, and thisHub.datau.dupAllowAddRemove = false; 
            }
        }
        
        Method method = OAObjectInfoDelegate.getMethod(thisHub.getObjectClass(), "get"+dm.liDetailToMaster.getName());
        if (method == null) {
            // LOG.warning("liDetailToMaster invalid, method not found, hub="+thisHub+", method=get"+dm.liDetailToMaster.getName()); 
            return;
        }
        
        if (Hub.class.isAssignableFrom(method.getReturnType())) {
            if (detailObject instanceof OAObjectKey) return;
            if (thisHub.data.bInFetch) return;  // otherwise, a recursive loop could happen
            Object obj = OAObjectReflectDelegate.getProperty((OAObject)detailObject, dm.liDetailToMaster.getName());
            if (objMaster == null) {  // remove
                if (thisHub.datam.masterObject != null) objMaster = thisHub.datam.masterObject;
                else if (dm.masterObject != null) objMaster = dm.masterObject;
                else {
                    if (dm.masterHub != null) objMaster = thisHub.getActiveObject();
                }
    
                // 20101228 pos() could cause the master hub AO to be changed
                //was: if (objMaster != null && ((Hub)obj).getPos(objMaster) >= 0) {
                if (objMaster != null && ((Hub)obj).contains(objMaster) ) {
                    ((Hub)obj).remove(objMaster);
                }
            }
            else {  // add
                // 20101228 
                //was: if ( ((Hub)obj).getPos(objMaster) < 0 ) {
                if ( !((Hub)obj).contains(objMaster) ) {
                    ((Hub)obj).add(objMaster);
                }
            }
        }
        else {
            method = OAObjectInfoDelegate.getMethod(thisHub.getObjectClass(), "set"+dm.liDetailToMaster.getName());
            if (method == null) {
                // LOG.warning("liDetailToMaster invalid, method not found, hub="+thisHub+", method=set"+dm.liDetailToMaster.getName()); 
                return;
            }
            Object currentValue = OAObjectReflectDelegate.getProperty((OAObject) detailObject, dm.liDetailToMaster.getName());
            if (currentValue == objMaster) return;

            if (objMaster == null) {  // must have been called by remove()
                // if "real" current master == obj, then set new value to null
                //   otherwise then the remove is being done by OAObject during
                //   a propertyChange and the object is being moved from one hub to another
                if (dm.masterObject != null) {
                    if (currentValue != dm.masterObject) return;
                }
                else if (dm.masterHub != null) {
                    if (currentValue != dm.masterHub.getActiveObject()) return;
                }
            }
    
            OAObjectReflectDelegate.setProperty((OAObject) detailObject, dm.liDetailToMaster.getName(), objMaster, null);
        }
    }

    
    /**
        Called by setActiveObject to automatically adjust Detail Hubs.
    */
    protected static void updateAllDetail(Hub thisHub, boolean bUpdateLink) {
        int x = thisHub.datau.vecHubDetail == null ? 0 : thisHub.datau.vecHubDetail.size();
        // get objects that go with detail hub
        for (int i=0; i<x; i++) {
            HubDetail hd = (HubDetail) thisHub.datau.vecHubDetail.elementAt(i);
            Hub h = hd.masterHub;
            if (h == null) {
                thisHub.datau.vecHubDetail.removeElementAt(i);
                x--;
                i--;
            }
            else updateDetail(thisHub, hd,h, bUpdateLink);
        }
    }
    
    
    /**
        Internal method to update any detail hubs.  This is called whenever activeObject is
        changed, or the property value that is used for the link gets modified
     */
    protected static void updateDetail(Hub thisHub, HubDetail detail, Hub dHub, boolean bUpdateLink) {
        /* get Hub, Object, OAObject or Array value from property
           ex:  Emp
                  String name;
                  Dept[] depts;  or
                  Hub depts;     or
                  Dept dept;
           then add to dHub.vector
        */
        if (detail == null || detail.type == detail.HUBMERGER) return;
        Object obj = null; // reference property
        try {
            if (thisHub.dataa.activeObject == null) obj = null;
            else obj = OAObjectReflectDelegate.getProperty((OAObject) thisHub.dataa.activeObject, detail.liMasterToDetail.getName());
        }
        catch(Exception e) {
            throw new RuntimeException("error calling get method for master to detail: " + detail.liMasterToDetail.getName());
        }
    
        int i, x;
        // 20080627 commented out next line.  It should always set datam properties
        // if ( dHub.isOAObject() ) {
            // dont call setMasterObject(), it will add hub to OAObject
            // 2004/05/14 if this hub was previously shared, it could be sharing datam
            if (dHub.datau.sharedHub != null && dHub.datau.sharedHub.datam == dHub.datam) dHub.datam = new HubDataMaster();
            dHub.datam.masterObject = (OAObject)thisHub.dataa.activeObject;
            dHub.datam.liDetailToMaster = OAObjectInfoDelegate.getReverseLinkInfo(detail.liMasterToDetail);
            dHub.datam.masterHub = thisHub;
        //}
    
        boolean wasShared = false;
        if (detail.type == HubDetail.HUB) {
            if (dHub.datau.sharedHub != null) {
                HubShareDelegate.removeSharedHub(dHub.datau.sharedHub, dHub);
                dHub.datau.sharedHub = null;
                wasShared = true;
            }
        }
        else {
            // see if the detail list needs changed
            if (obj == dHub.dataa.activeObject) {
                // 20120720 need to send newList event, in case master object was previously null
                if (obj == null) {
                    HubEventDelegate.fireOnNewListEvent(dHub, false);  // notifies all of this hub's shared hubs
                }
                return;
            }
    
            if (dHub.isOAObject()) {
                for (i=0; ; i++) {
                    Object objx = HubDataDelegate.getObjectAt(dHub, i);
                    if (objx == null) break;
                    OAObjectHubDelegate.removeHub((OAObject) objx, dHub);
                }
            }
            dHub.data.vector.removeAllElements();
        }

        dHub.datau.dupAllowAddRemove = true;
    
        if (obj == null) {
            HubDataActive daOld = dHub.dataa;
    
            if (wasShared) {
                // have to create its own since it might have been sharing the current one
                dHub.data = new HubData();
                if (detail.bShareActiveObject) dHub.dataa = new HubDataActive();
            }
            dHub.datau.dupAllowAddRemove = false; // 2004/08/23
            //was: if (detail.type != HubDetail.HUB) dHub.datau.dupAllowAddRemove = false;
            HubShareDelegate.syncSharedHubs(dHub, true, daOld, dHub.dataa, bUpdateLink);
        }
        else if (detail.type == HubDetail.HUB) { // Hub
            // share oaObject info and activeObject info
            // dont share listeners and links ("datau")
            // dont share activeObject ("dataa")
            //     unless DetailHub.bShareActiveObject is true then set it after events
            Hub h = (Hub) obj;
            if (dHub.datau.selectOrder != null) h.setSelectOrder(dHub.datau.selectOrder);
    
            // need to select before assigning to detail hub so that add events wont
            //            be sent to detail hubs listeners
            dHub.data = h.data;
            dHub.datau.sharedHub = h;
            HubShareDelegate.addSharedHub(h, dHub);

            // 20120926 "h" could be a shared/calc Hub 
            if (dHub.datam.masterObject != (OAObject) h.datam.masterObject) {
                if (dHub.datau.sharedHub != null && dHub.datau.sharedHub.datam == dHub.datam) dHub.datam = new HubDataMaster();
                dHub.datam.masterObject = (OAObject) h.datam.masterObject;
                dHub.datam.liDetailToMaster = h.datam.liDetailToMaster;
                dHub.datam.masterHub = h.datam.masterHub;
            }            
            
            
            HubShareDelegate.syncSharedHubs(dHub, detail.bShareActiveObject, dHub.dataa, h.dataa, bUpdateLink); 

            // 20080628 add "if" statement.
            if (dHub.datam.masterObject != null && h.datam.masterObject == null) {
                HubDetailDelegate.setMasterObject(h, dHub.datam.masterObject, dHub.datam.liDetailToMaster);
            }
        }
        else if (detail.type == HubDetail.OAOBJECT || detail.type == HubDetail.OBJECT) {
            HubAddRemoveDelegate.internalAdd(dHub, (OAObject) obj);
            dHub.datau.dupAllowAddRemove = false;
        }
        else {
            // HubDetail.OBJECTARRAY || HubDetail.OAOBJECTARRAY
            int j = Array.getLength(obj);
            for (int k=0; k<j; k++) {
                Object objx = Array.get(obj,k);
                HubAddRemoveDelegate.internalAdd(dHub, objx);
            }
            dHub.datau.dupAllowAddRemove = false;
        }
    
        /** 20111005 internalAdd will .addHub
        if ( detail.type != HubDetail.HUB && thisHub.isOAObject() ) {
            for (i=0; ; i++) {
                OAObject oaObj = (OAObject) HubDataDelegate.getObjectAt(dHub,i);
                if (oaObj == null) break;
                OAObjectHubDelegate.addHub(oaObj, dHub);
            }
        }
        */
        HubDataDelegate.incChangeCount(dHub);
        HubDetailDelegate.updateDetailActiveObject(dHub, dHub, bUpdateLink, detail.bShareActiveObject);
        HubEventDelegate.fireOnNewListEvent(dHub, false);  // notifies all of this hub's shared hubs
    
        if (detail.type == HubDetail.OAOBJECT || detail.type == HubDetail.OBJECT) {
            dHub.setPos(0);
        }
    }

    
    
    /**  initialize activeObject in detail hub.  */
    protected static void updateDetailActiveObject(Hub thisHub, Hub hubDetailHub, boolean bUpdateLink, boolean bShareActiveObject) {
        boolean bUseCurrent = (bShareActiveObject && thisHub.dataa == hubDetailHub.dataa);  // if hubs are sharing active object then dont change it.
        if (!bUseCurrent || (thisHub == hubDetailHub)) {
            if (thisHub.datau.linkToHub == null) {
                // if there is not a linkHub, then go to default object
                int pos;
                if (bUseCurrent) pos = thisHub.getPos();
                else pos = thisHub.datau.defaultPos;  // default is -1
                HubAODelegate.setActiveObject(thisHub, pos, bUpdateLink, true, false);  // bForce=true,bCalledByShareHub=false
            }
            else if (bUpdateLink) {
                int pos;
                if (bUseCurrent) pos = thisHub.getPos();
                else pos = -1;
                HubAODelegate.setActiveObject(thisHub, pos, bUpdateLink, true,false);  // bForce=true, this will recursivly notify this links HubDetails
            }
            else {
                // if linkHub & !bUpdateLink, then retreive value from linked property
                // and make that the activeObject in this Hub
                try {
                    Object obj = thisHub.datau.linkToHub.getActiveObject();
                    if (obj != null) obj = thisHub.datau.linkToGetMethod.invoke(obj, null );
                    if (thisHub.datau.linkPos) {
                        int x = -1;
                        if (obj != null && obj instanceof Number) x = ((Number)obj).intValue();
                        if (thisHub.getPos() != x) {
                            HubAODelegate.setActiveObject(thisHub, thisHub.elementAt(x),x,bUpdateLink,false,false);//bUpdateLink,bForce,bCalledByShareHub
                        }
                    }
                    else if (thisHub.datau.linkFromPropertyName != null ) { // 20110116 ex: Breed.name linked to Pet.breed (string)
                        Object objx;
                        if (obj != null) objx = thisHub.find(thisHub.datau.linkFromPropertyName, obj);
                        else objx = null;
                        HubAODelegate.setActiveObject(thisHub,objx,bUpdateLink,false,false);
                    }
                    else {
                        int pos = thisHub.getPos(obj);
                        if (obj != null &&  pos < 0) obj = null;
                        HubAODelegate.setActiveObject(thisHub,obj,pos,bUpdateLink,false,false);//bUpdateLink,bForce,bCalledByShareHub
                    }
                }
                catch (Exception e) {
                    throw new RuntimeException(thisHub.datau.linkToGetMethod.getName(), e); // wrap orig exception
                }
            }
        }

        // 20120715 
        WeakReference<Hub>[] refs = HubShareDelegate.getSharedWeakHubs(thisHub);
        for (int i=0; refs != null && i<refs.length; i++) {
            WeakReference<Hub> ref = refs[i];
            if (ref == null) continue;
            Hub h2 = ref.get();
            if (h2 == null)  continue;
            
            // only update sharedHubs with diff dataa, setActiveObject will do others
            if (h2.dataa != hubDetailHub.dataa) {
                updateDetailActiveObject(h2, hubDetailHub,false,bShareActiveObject); // dont update link properties
            }
        }
        
        /* was
        Hub[] hubs = HubShareDelegate.getSharedHubs(thisHub);
        for (int i=0; i<hubs.length; i++) {
            Hub h2 = hubs[i];
            if (h2 == null) continue;
            // only update sharedHubs with diff dataa, setActiveObject will do others
            if (h2.dataa != hubDetailHub.dataa) {
                updateDetailActiveObject(h2, hubDetailHub,false,bShareActiveObject); // dont update link properties
            }
        }
        */
    }

    
    /** returns DataMaster from any shared hub that has a MasterHub set. 
     *  If none is found, then the DataMaster for thisHub is returned.
     * */
    protected static HubDataMaster getDataMaster(final Hub thisHub) {
        return getDataMaster(thisHub, null);
    }
    protected static HubDataMaster getDataMaster(final Hub thisHub, final Class masterClass) {
        if (thisHub == null) return null;
        if (thisHub.datam.masterHub != null) return thisHub.datam;
        
        // 20120716
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub h) {
                if (h.datam.masterHub != null) {
                    if (masterClass == null || masterClass.equals(h.datam.masterHub.getObjectClass())) {
                        return true;
                    }
                }
                return false;
            }
        };
        Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
        
        //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub);
        for (int i=0; hubs != null && i < hubs.length; i++) {
            if (hubs[i].datam.masterHub != null) {
                return hubs[i].datam;
            }
        }
        return thisHub.datam;
    }

    /** returns any shared hub with a MasterHub set. */
    public static Hub getHubWithMasterHub(final Hub thisHub) {
        if (thisHub.datam.masterHub != null) return thisHub;

        // 20120716
        OAFilter<Hub> filter = new OAFilter<Hub>() {
            @Override
            public boolean isUsed(Hub h) {
                return (h.datam.masterHub != null);
            }
        };
        Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub, filter);
        
        
        //was: Hub[] hubs = HubShareDelegate.getAllSharedHubs(thisHub);
        for (int i=0; i < hubs.length; i++) {
            if (hubs[i].datam.masterHub != null) {
                return hubs[i];
            }
        }
        return null;
    }

    /** returns the MasterHuib of any shared hub. */
    public static Hub getMasterHub(Hub thisHub) {
        Hub h = getHubWithMasterHub(thisHub);
        if (h != null) h = h.datam.masterHub;
        return h;
    }
    
    
    /**
        Returns the OAObject that owns this Hub
    */
    public static OAObject getMasterObject(Hub thisHub) {
        return thisHub.datam.masterObject;
    }

    public static Class getMasterClass(Hub thisHub) {
        if (thisHub.datam.masterObject != null) {
            return thisHub.datam.masterObject.getClass();
        }
        Hub h = getHubWithMasterHub(thisHub);
        if (h != null) return h.getObjectClass();
        return null;
    }
    

    public static Hub getDetailHub(Hub thisHub, Class[] clazz) {
        return getDetailHub(thisHub, null, clazz, null, null,false, null);
    }
    public static Hub getDetailHub(Hub thisHub, Class clazz, boolean bShareActive, String selectOrder) {
        return getDetailHub(thisHub, null, new Class[] { clazz }, null, null,bShareActive, selectOrder);
    }
    public static Hub getDetailHub(Hub thisHub, String path, Class objectClass, boolean bShareActive) {
        return getDetailHub(thisHub,path,null,objectClass,null,bShareActive,null);
    }   
    public static Hub getDetailHub(Hub thisHub, String path) {
        return getDetailHub(thisHub,path,null,null,null,false,null);
    }
    public static Hub getDetailHub(Hub thisHub, String path, String selectOrder) {
        return getDetailHub(thisHub,path,null,null,null,false,selectOrder);
    }
    public static Hub getDetailHub(Hub thisHub, String path, boolean bShareActive) {
        return getDetailHub(thisHub,path,null,null,null,bShareActive,null);
    }
    public static Hub getDetailHub(Hub thisHub, String path, boolean bShareActive, String selectOrder) {
        return getDetailHub(thisHub,path,null,null,null,bShareActive,selectOrder);
    }
    
    /**
        Main method for setting Master/Detail relationship.
        @see Hub#getDetailHub(String,boolean,String) Full Description on Master/Detail Hubs
    */
    protected static Hub getDetailHub(final Hub thisHub, String path, Class[] classes, Class lastClass, Hub detailHub, boolean bShareActive, String selectOrder) {
        // linkHub is Hub that is the detail hub, it is supplied by setMaster()
        // lastClass can be the class to use for the last class in the path

        if (path != null && path.length() > 0 && thisHub.datau.objClass == null) return null;

        // 2004/03/19 taken out, so that it can be set in this method
        // if (linkHub != null) linkHub.checkObjectClass();
        // ex:  ("dept.manager.orders.items.product.vendor")
        //  or  ( {Dept.Class, Emp.class, Order.class, Item.class, Product.class, Vendor.class } )
    
        if (path == null) {
            Class[] c = classes;
            if (c == null && lastClass != null) {
               c = new Class[1];
               c[0] = lastClass;
            }
            if (c != null) path = HubDelegate.getPropertyPathforClasses(thisHub, c);
            if (path == null) {
                throw new RuntimeException("cant find path.");
            }
        }
        else if (path.length() == 0) {
            return thisHub;  // since this is a recursive method
        }
    
        // added support for using HubMerger if property path has more then one ending object/hub
        Class clazz = thisHub.getObjectClass();
        StringTokenizer st = new StringTokenizer(path, ".");
        boolean bLastMany = false;
        int cntMany = 0;
        for ( ;st.hasMoreTokens(); ) {
            String prop = st.nextToken();
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, prop);
            if (li == null) {
                throw new IllegalArgumentException("Cant find "+prop+" for PropertyPath \""+path+"\" starting with Class "+thisHub.getObjectClass().getName());
            }
            if (li.getType() == OALinkInfo.MANY) {
                bLastMany = true;
                cntMany++;
            }
            else bLastMany = false;
            clazz = li.getToClass();
        }
    
        if (cntMany > 1 || (cntMany > 0 && !bLastMany)) {
            // use HubMerger to create DetailHub
            // see if HubDetail is already created
            if (detailHub == null) {
                HubDetail hd = null;
                int x = thisHub.datau.vecHubDetail == null ? 0 : thisHub.datau.vecHubDetail.size();
                for (int i=0; i<x; i++) {
                    hd = (HubDetail) thisHub.datau.vecHubDetail.elementAt(i);
                    if (hd.type == hd.HUBMERGER && path.equalsIgnoreCase(hd.path) ) {
                        hd.referenceCount++;
                        return hd.masterHub;
                    }
                }
            }
    
            // 20101220 added clazz param
            if (detailHub == null) detailHub = new Hub(clazz);
            //was: if (detailHub == null) detailHub = new Hub(clazz);
            HubMerger hm = new HubMerger(thisHub, detailHub, path, bShareActive, selectOrder, false);
    
            // 2005/02/23 create HubDetail
            HubDetail hd = new HubDetail(path, detailHub);
            hd.referenceCount = 1;
            if (thisHub.datau.vecHubDetail == null) thisHub.datau.vecHubDetail = new Vector<HubDetail>(3,5);
            thisHub.datau.vecHubDetail.addElement(hd);
    
            return detailHub;
        }
    
        int pos = path.indexOf('.');
        String propertyName;
        if (pos < 0) propertyName = path;
        else propertyName = path.substring(0,pos);
    
        // verify class & property
        Class newClass = null;
        if (pos < 0) newClass = lastClass;
        else if (classes != null && classes.length > 0) newClass = classes[0];
    
        // get LinkInfo
        OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(thisHub.datau.objectInfo, propertyName);
    
        // find method
        if (linkInfo == null) throw new RuntimeException("cant find linkInfo");
    
        Method method  = OAObjectInfoDelegate.getMethod(thisHub.datau.objectInfo, "get"+linkInfo.getName(), 0);
        if (method == null) {
            throw new RuntimeException("cant find method get"+linkInfo.getName());
        }
    
        // verify or get the type of class
        Class returnClass = method.getReturnType();
    
        // support for casting a property to a subclass ex:  "(Manager) Employee.Department"
        propertyName = linkInfo.getName(); // in case a "cast" was used on the property
    
        if (newClass != null) {  // class is supplied
            if (!newClass.equals(OAObjectInfoDelegate.getPropertyClass(thisHub.getObjectClass(), propertyName))) {
                if ( !(Hub.class.isAssignableFrom(returnClass)) ) throw new RuntimeException("classes do not match.");
            }
        }
        else {
            newClass = OAObjectInfoDelegate.getPropertyClass(thisHub.getObjectClass(), propertyName);
            if (newClass == null) throw new RuntimeException("cant find property class");
        }

        // see what type of object the property returns: Array, Hub, OAObject, Object
        int type = -1; // must be assign < 0
        if (returnClass.isArray()) {  // see if it is an Array
            type = HubDetail.ARRAY;
            returnClass = returnClass.getComponentType();
        }
    
        if ( Hub.class.isAssignableFrom(returnClass) ) {
            if (type != HubDetail.ARRAY) type = HubDetail.HUB;
        }
        else if ( OAObject.class.isAssignableFrom(returnClass) ) {
            if (type == HubDetail.ARRAY) type = HubDetail.OAOBJECTARRAY;
            else type = HubDetail.OAOBJECT;
        }
        else {
            if (type == HubDetail.ARRAY) type = HubDetail.OBJECTARRAY;
            else type = HubDetail.OBJECT;
        }
    
        //  see if HubDetail is already created
        Hub hub = null;
        HubDetail hd = null;
        int x = thisHub.datau.vecHubDetail == null ? 0 : thisHub.datau.vecHubDetail.size();
        for (int i=0; i<x; i++) {
            hd = (HubDetail) thisHub.datau.vecHubDetail.elementAt(i);
            if (hd.liMasterToDetail != null && hd.liMasterToDetail.equals(linkInfo) && hd.masterHub != null) {
                if (detailHub == null || detailHub == hd.masterHub) {
                    hub = hd.masterHub;
                    break;
                }
            }
        }
    
        // support for casting a property to a subclass ex:  "(Manager) Employee.Department"
        newClass = linkInfo.getToClass();  // property path could be cast to a subclass name
    
        boolean bFound = false;
        if (hub == null) {
            if (pos > 0 || detailHub == null) {
                hub = new Hub(newClass); // create new hub to reference objects
                hd = new HubDetail(hub,linkInfo,type,propertyName);
            }
            else {
                hd = new HubDetail(null,linkInfo,type,propertyName); // from call to "setMaster()"
            }
            if (thisHub.datau.vecHubDetail == null) thisHub.datau.vecHubDetail = new Vector(3,5);
            thisHub.datau.vecHubDetail.addElement(hd);
        }
        else bFound = true;
    
        if (pos < 0 && bShareActive) hd.bShareActiveObject = true;
    
        if (pos < 0) {
            if (detailHub != null) {  // verify that linkHub can work
                if (detailHub.getObjectClass() == null) {
                    HubDelegate.setObjectClass(detailHub, newClass);
                }
                if ( hub != null && !hub.getObjectClass().equals(detailHub.getObjectClass())) {
                    if (!hub.getObjectClass().isAssignableFrom(detailHub.getObjectClass())) {
                        throw new RuntimeException("ObjectClass is different.");
                    }
                }
                hub = detailHub;
                hd.masterHub = hub;
            }
            if (selectOrder != null) hub.setSelectOrder(selectOrder);
            hd.referenceCount++;
    
            path = "";
        }
        else {
            path = path.substring(pos+1);
        }
        hub.datam.masterHub = thisHub;
        if (type == HubDetail.OAOBJECT || type == HubDetail.OBJECT) hub.datau.defaultPos = 0;
    
        if (!bFound) {
            updateDetail(thisHub, hd, hd.masterHub, false);
        }

        int i = (classes == null) ? 0 : classes.length;
        if (i > 0) i--;
        Class[] c = new Class[i];
    
        if (i > 0) System.arraycopy(classes, 1, c, 0, i);
        return getDetailHub(hub, path, c, lastClass, detailHub, bShareActive, selectOrder);
    }

    
    
    /**
        Set the object that "owns" this hub.  This is set by OAObject.getHub() and by updateDetail(),
        when a detail Hub is updated.  All changes (adds/removes/replaces) will automatically be
        tracked.
        <p>
        Example: if a dept object has an emp hub, then
        it will be the masterObject of the hubEmp.  All additions and removes will be tracked
        for a OADataSource that uses links.
        @param linkInfo is from the detail object to the master.
    */
    public static void setMasterObject(Hub thisHub, OAObject masterObject, OALinkInfo liDetailToMaster) {
        // OAObject needs to know which hubs are under it
        thisHub.datam.liDetailToMaster = liDetailToMaster;
        if (masterObject == thisHub.datam.masterObject) return;
        thisHub.datam.masterObject = masterObject;
        thisHub.data.bTrackChanges = (masterObject != null) && (liDetailToMaster == null || !liDetailToMaster.getCalculated());
        thisHub.setChanged(false);
    }
    public static void setMasterObject(Hub thisHub, OAObject masterObject) {
        setMasterObject(thisHub, masterObject, thisHub.datam.liDetailToMaster);
    }
    
    /**
        Returns the OALinkInfo from detail (MANY) to master (ONE).
    */
    public static OALinkInfo getLinkInfoFromDetailToMaster(Hub thisHub) {
        return thisHub.datam.liDetailToMaster;
    }
    

    /**
        Returns true if any of the master hubs above this hub have an active object that is new.
    */
    public static boolean isMasterNew(Hub thisHub) {
        Hub h = thisHub;
        for (; h!=null ;) {
            HubDataMaster dm = HubDetailDelegate.getDataMaster(h);
    
            Object obj = null;
            if (dm.masterHub != null) {
                h = dm.masterHub;
                obj = h.getActiveObject();
            }
            else {
                if (dm.masterObject != null) obj = dm.masterObject;
                h = null;
            }
    
            if (obj == null) break;
            if ( !(obj instanceof OAObject) ) break;
            if ( ((OAObject) obj).getNew() ) return true;
        }
        return false;
    }
    
    
    /**
        Used to remove Master/Detail relationships.
    */
    public static boolean removeDetailHub(Hub thisHub, Hub hubDetail) {
        // remove HubDetail if it does not have any more listeners or links
        if (hubDetail == thisHub) {
            return false;
        }
    
        int x = thisHub.datau.vecHubDetail == null ? 0 : thisHub.datau.vecHubDetail.size();
        for (int i=0; i<x; i++) {
            HubDetail hd = (HubDetail) thisHub.datau.vecHubDetail.elementAt(i);
            Hub h = hd.masterHub;
            if (h == hubDetail) {
                hd.referenceCount--;
                if (hd.referenceCount <=0) {
                    if (h.datau.vecHubDetail == null || h.datau.vecHubDetail.size() == 0) {
                        thisHub.datau.vecHubDetail.removeElementAt(i);
                        hubDetail.data = new HubData();
                        hubDetail.datam = new HubDataMaster();
                        hubDetail.dataa = new HubDataActive();
                        return true;
                    }
                    hd.referenceCount = 0;
                }
                return false;
            }
            // if not found, this will recursively look to find hub in other linked hubDetails
            if (h != null) {
                boolean b = removeDetailHub(h, hubDetail);
                if (b && hd.referenceCount <= 0) {
                    if (h.datau.vecHubDetail != null || h.datau.vecHubDetail.size() == 0) {
                        removeDetailHub(thisHub, h);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
        Used for master/detail Hubs, returns the name of the property from the master Hub to detail Hub.
        <p>
        Example:<br>
        If master is Department and Detail is Employee then "Employees", which is from Department.getEmployees()
    */
    public static String getPropertyFromMasterToDetail(Hub thisHub) {
        if (thisHub.datam.liDetailToMaster != null) {
            String name = thisHub.datam.liDetailToMaster.getReverseName();
            if (name != null) return name;
        }
        OAObject master = thisHub.datam.masterObject;
        if (master != null) {
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(master.getClass());
            OALinkInfo li = OAObjectInfoDelegate.getLinkInfo(oi, master, thisHub);
            if (li != null) {
                return li.getName();
            }
        }
        
        // see if it can be found using detailHub info
        Hub hubMaster = thisHub.datam.masterHub;
        if (hubMaster != null) {
            int x = hubMaster.datau.vecHubDetail == null ? 0 : hubMaster.datau.vecHubDetail.size();
            for (int i=0; i<x; i++) {
                HubDetail hd = (HubDetail) hubMaster.datau.vecHubDetail.elementAt(i);
                if (hd.masterHub == thisHub) {
                    OALinkInfo li = hd.liMasterToDetail;
                    if (li != null) {
                        return li.getName();
                    }
                }
            }
        }
        return null;
    }
    /**
        Used for master/detail Hubs, returns the name of the property from the detail Hub to master Hub.
        <p>
        Example:<br>
        If master is Department and Detail is Employee then "Department", which is from Employee.getDepartment()
    */
    public static String getPropertyFromDetailToMaster(Hub thisHub) {
        if (thisHub.datam.liDetailToMaster != null) {
            return thisHub.datam.liDetailToMaster.getName();
        }
        return null;
    }

    
    /**
        Returns true if this hub of objects is owned by a master object.
    */
    public static boolean isOwned(Hub thisHub) {
        HubDataMaster dm = getDataMaster(thisHub);
        if (dm.masterObject != null && dm.liDetailToMaster != null) {
            OALinkInfo li = OAObjectInfoDelegate.getReverseLinkInfo(dm.liDetailToMaster);
            if (li != null) return li.getOwner();
        }
        return false;
    }
    
    
    /** 
     * Get the real hub that this hub should be using.
     * This could be based on the fact that this hub has not yet been updated after a masterHub.AO has been changed.
     * @param thisHub
     * @return
     */
    public static Hub getRealHub(Hub thisHub) {
        Hub hubMaster = HubDetailDelegate.getMasterHub(thisHub);
        if (hubMaster == null) return thisHub;
        hubMaster = getRealHub(hubMaster);
        
        Hub h = thisHub;
        OAObject o = HubDetailDelegate.getMasterObject(thisHub);
        if (o != null && o != hubMaster.getAO()) {
            h = (Hub) OAObjectReflectDelegate.getProperty(o, getPropertyFromMasterToDetail(hubMaster));
        }
        return h;
    }

    public static boolean hasDetailHubs(Hub thisHub) {
        if (thisHub == null || thisHub.datau == null) return false;
        return thisHub.datau.vecHubDetail != null && thisHub.datau.vecHubDetail.size() > 0;
    }
    
/** 20111008 finish if/when needed  
    public static HubDetail getHubDetail(Hub hubDetail) {
        Hub hubMaster = hubDetail.getMasterHub();
        
        Vector<HubDetail> vec = hubMaster.datau.vecHubDetail;
        if (vec == null) return null;
        
        for (HubDetail hd : vec) {
            if (hd.)
        }

    }
*/
    
}




