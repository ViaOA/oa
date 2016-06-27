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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASync;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.ds.OADataSource;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OANotExist;
import com.viaoa.util.OANullObject;
import com.viaoa.util.OAString;


public class OAObjectEventDelegate {

    private static Logger LOG = Logger.getLogger(OAObjectEventDelegate.class.getName());
    private static final String WORD_CHANGED = "CHANGED";
    
    /**
	    Used to manage property changes.
	    Sends a "hubPropertyChange()" to all listeners of the Hubs that this object is a member of.  <br>
	*/
	protected static void fireBeforePropertyChange(final OAObject oaObj, final String propertyName, 
	        Object oldObj, final Object newObj, final boolean bLocalOnly, final boolean bSetChanged) {
	    
	    if (oaObj == null || propertyName == null) return;
	    if (OAThreadLocalDelegate.isSkipFirePropertyChange()) return;
	    final boolean bIsLoading = OAThreadLocalDelegate.isLoadingObject(); 
	    if (bIsLoading) {
	        if (!OAObjectHubDelegate.isInHub(oaObj)) {  // 20110719: could be in the OAObjectCache.SelectAllHubs
	            // no listeners, need to load quick as possible
	            if (OASyncDelegate.isServer(oaObj)) {  // 20150604 if client, then it needs to send prop change to server
	                return; 
	            }
	            if (OAObjectCSDelegate.isInNewObjectCache(oaObj)) {  // 20160212 created on client, has not been sent to server yet
	                return;
	            }
	        }
	    }
	    
	    // check to see if it is actually changed
        if (oldObj != null) {
            if (OAObjectReflectDelegate.getPrimitiveNull(oaObj, propertyName) || oldObj instanceof OANullObject) oldObj = null;
        }
        
        if (oldObj == newObj) return;
        if (oldObj != null && oldObj.equals(newObj)) return;

        
        // 20160606 verify to the prop can be changed
        if (!bIsLoading && !OARemoteThreadDelegate.isRemoteThread()) {
            Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
            if (hubs != null) {
                for (Hub h : hubs) {
                    if (h == null) continue;
                    if (!h.canChangeProperty(oaObj, propertyName, oldObj, newObj)) {
                        throw new RuntimeException("can change property returned false, property cant be changed.");
                    }
                }
            }
        }
        
        
        // 20150109 verify that change is permitted
        // verify if recursive link that new parent is allowed
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        final String propertyU = propertyName.toUpperCase();
        OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, propertyU);
        OALinkInfo toLinkInfo;
        if (linkInfo != null) toLinkInfo = OAObjectInfoDelegate.getReverseLinkInfo(linkInfo);
        else toLinkInfo = null;
        
        if (toLinkInfo != null && toLinkInfo.bRecursive) {
            OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.ONE);  // ex: "ParentSection"
            if (liRecursive == linkInfo) {
                // parent property changed.  ex: "setParentSection"
                // verify that it can be placed
                if (newObj != null) {
                    if (oaObj == newObj) {  // object cant be its own parent
                        throw new RuntimeException("Can not set the Parent to itself");
                    }
                    // cant assign a child of this object as the new parent - causes orphaned objects
                    for (Object obj=newObj; ;) {
                        obj = OAObjectReflectDelegate.getProperty((OAObject)obj, liRecursive.getName());
                        if (obj == null) break;
                        if (obj == oaObj) {
                            OAObjectReflectDelegate.setProperty(oaObj, linkInfo.getName(), oldObj, null);
                            throw new RuntimeException("Can not assign Parent to a Child");// causes orphans
                        }
                    }
                }
            }
        }     
        
        // 20151205 check to see if owner is being reassigned
        if (linkInfo != null && oldObj != null && newObj != null && !oaObj.isNew() && linkInfo.getType() == OALinkInfo.ONE && !linkInfo.getCalculated()) {
            OALinkInfo revLinkInfo = OAObjectInfoDelegate.getReverseLinkInfo(linkInfo);
            if (revLinkInfo != null && revLinkInfo.getOwner()) {
                String s = "owner is being reassigned, object="+oaObj.getClass().getSimpleName()+", property="+propertyName+", new value="+newObj;
                RuntimeException e = new RuntimeException(s);
                LOG.log(Level.WARNING, s, e);
                throw e;
            }
        }
        
        if (linkInfo == null && !OARemoteThreadDelegate.isRemoteThread()) {
            OAPropertyInfo propInfo = OAObjectInfoDelegate.getPropertyInfo(oi, propertyU);
            if (propInfo != null) {
                
                if (propInfo.getId() && !OAObjectDSDelegate.isAssigningId(oaObj)) {
                    String s = OAObjectKeyDelegate.verifyKeyChange(oaObj);
                    if (s != null) {
                        throw new RuntimeException(s);
                    }
                } 
                
                if (propInfo.getUnique() && newObj != null && !OAObjectDSDelegate.isAssigningId(oaObj)) {
                    OAFilter<OAObject> filter = new OAFilter<OAObject>() {
                        public boolean isUsed(OAObject obj) {
                            Object objx = obj.getProperty(propertyU);
                            if (objx == null) return false;
                            return objx.equals(newObj);
                        }
                    };
                    OADataSource ds = OADataSource.getDataSource(oaObj.getClass(), filter);
                    
                    if (ds != null && (!(ds instanceof OADataSourceObjectCache))) {
                        Iterator it = ds.select(oaObj.getClass(), propertyU+" = ?", new Object[] {newObj}, null, null, null, null, 2, filter, false);
                        try {
                            for ( ;it != null && it.hasNext(); ) {
                                Object objx = it.next();
                                if (objx != oaObj) {
                                    throw new RuntimeException("property is unique, and value is assigned to another object.");
                                }
                            }
                        }
                        finally {
                            if (it != null) it.remove();
                        }
                    }
                    else if (!propInfo.getId()) {
                        Object objLast = null;
                        for (;;) {
                            Object objx = OAObjectCacheDelegate.findNext(objLast, oaObj.getClass(), propertyU, newObj);
                            if (objx == null) break;
                            if (objx != oaObj) {
                                throw new RuntimeException("property is unique, and value is assigned to another object.");
                            }
                            objLast = objx;
                        }
                    }
                }
            }
        }
        
        sendHubBeforePropertyChange(oaObj, propertyName, oldObj, newObj);
        
        if (!bLocalOnly) {
            // 20140314 if it is in newObjectCache (this computer only), then dont send prop changes
            if (!OAObjectCSDelegate.isInNewObjectCache(oaObj)) {
                OAObjectCSDelegate.fireBeforePropertyChange(oaObj, propertyName, oldObj, newObj);
            }
        }
	}
	
	
	
	

    /**
	    Used to manage property changes.
	    This will:
	    1: update null property information for primitive property types
	    2: update the objectKey, which would then update the ObjectCache
	    3: update object hubs if this is a reference property change
	    4: Send "hubPropertyChange()" to all listeners of the Hubs that this object is a member of.
	    5: Send event to Server.
	    @see OAThreadLocalDelegate#setSuppressFirePropertyChange(boolean) to suppress this method from running by the current thread.
	*/
	protected static void firePropertyChange(final OAObject oaObj, final String propertyName, Object oldObj, Object newObj, boolean bLocalOnly, boolean bSetChanged) {
	    if (oaObj == null || propertyName == null) return;
	    if (OAThreadLocalDelegate.isSkipFirePropertyChange()) return;
	    
	    String propertyU = propertyName.toUpperCase();
	    
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);

	    if (oldObj != null) {
	    	if (OAObjectReflectDelegate.getPrimitiveNull(oaObj, propertyU) || oldObj instanceof OANullObject) oldObj = null;
	    }
		
	    //  note: a primitive null can only be set by calling OAObjectReflectDelegate.setProperty(...)
        if (newObj instanceof OANullObject) newObj = null;
		if (newObj != null) {
		    OAObjectReflectDelegate.removePrimitiveNull(oaObj, propertyU);
		}
		else { // 20121001 for byte[] props
            OAObjectReflectDelegate.setPrimitiveNull(oaObj, propertyU);
        }
        if (oldObj instanceof OANullObject) oldObj = null;


        
    	OALinkInfo linkInfo = OAObjectInfoDelegate.getLinkInfo(oi, propertyU);
    	boolean bWasEmpty = false;
		if (linkInfo != null && oldObj == null) {
		    // oldObj might never have been loaded before setMethod was called, which will have the oldValue=null -
		    //   need to check in oaObj.properties to see what orig value was.
		    oldObj = OAObjectPropertyDelegate.getProperty(oaObj, propertyName, true, true);
		    if (oldObj == OANotExist.instance) {
		        bWasEmpty = true;
		        oldObj = null;
		    }
	    }
		
		Object origOldObj = oldObj;
        if (oldObj instanceof OAObjectKey) {
            boolean b = false;
            if (newObj instanceof OAObject) {
                if (OAObjectKeyDelegate.getKey((OAObject)newObj).equals(oldObj)) {
                    oldObj = newObj;
                    b = true;
                }
            }
            if (!b) {
                Object objx = OAObjectCacheDelegate.get(linkInfo.toClass, (OAObjectKey)oldObj);
                if (objx != null) oldObj = objx;
            }
        }
	
        if (oldObj == newObj && !bWasEmpty) return;
        if (oldObj != null && oldObj.equals(newObj)) return;

	    OAPropertyInfo propInfo = null;
        OACalcInfo calcInfo = null;
        if (linkInfo == null) {
            propInfo = OAObjectInfoDelegate.getPropertyInfo(oi, propertyU);            
            if (propInfo == null) {
                calcInfo = OAObjectInfoDelegate.getOACalcInfo(oi, propertyU);
            }
        }	    
		OAObjectKey origKey; 
        if (propInfo != null && propInfo.getId()) {
    		origKey = OAObjectKeyDelegate.getKey(oaObj, oldObj);  // make sure key uses the prevId, so that it can be found on other computers
            OAObjectKeyDelegate.updateKey(oaObj, true);  // this will make sure that it is a valid (unique) value
        }
        else {
        	origKey = OAObjectKeyDelegate.getKey(oaObj);
        }
        
    	if (linkInfo != null){
    		// must update ref properties before sending events
            // 20110314: need to store nulls, so that it wont go back to server everytime
            OAObjectPropertyDelegate.setPropertyCAS(oaObj, propertyName, newObj, origOldObj, bWasEmpty, false);         
        }
    	else {
    	    // 20130318
    	    if (propInfo != null && propInfo.isBlob()) {
                OAObjectPropertyDelegate.setPropertyCAS(oaObj, propertyName, newObj, origOldObj, bWasEmpty, false);         
    	    }
    	}

    	boolean bChangeHold = oaObj.changedFlag;
    	
    	boolean bIsChangeProp = WORD_CHANGED.equals(propertyU);
    	if (!bIsChangeProp) oaObj.changedFlag = true;

    	// 20100406
        boolean bIsLoading = OAThreadLocalDelegate.isLoadingObject();
        
        if (!bIsLoading) {
            OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
            
            Object objOld = oldObj;
            if (objOld instanceof OAObject) {
                objOld = OAObjectKeyDelegate.getKey((OAObject)objOld);
            }
            Object objNew = newObj;
            if (objNew instanceof OAObject) {
                objNew = OAObjectKeyDelegate.getKey((OAObject)objNew);
            }
            
            /*
            Object oldx;
            if (objOld instanceof byte[]) {
                oldx = "byte[" + ((byte[])objOld).length +"]";
            }
            else oldx = objOld;
            */
            /*
            Object newx;
            if (objNew instanceof byte[]) {
                newx = "byte[" + ((byte[])objNew).length +"]";
            }
            else newx = objNew;
            

            String s = String.format("Change, class=%s, id=%s, property=%s, oldValue=%s, newVaue=%s",
                    OAString.getClassName(oaObj.getClass()),
                    key.toString(),
                    propertyName,
                    OAConv.toString(oldx),
                    OAConv.toString(newx)
            );
            if (oi.bUseDataSource) { // 20120429
                if (calcInfo == null) {
                    if (linkInfo == null || !linkInfo.bCalculated) { 
                        OAObject.OALOG.fine(s);
                    }
                }
            }
            LOG.fine(s);
            */
            
            if (!bLocalOnly) {
                // prior to 20100406, this was always calling these methods
                OARemoteThreadDelegate.startNextThread(); // if this is OAClientThread, so that OAClientMessageHandler can continue with next message
                
                //note: this next method will just return, since fireBeforePropChange is now doing this
                // OAObjectCSDelegate.fireAfterPropertyChange(oaObj, origKey, propertyName, oldObj, newObj);
            }
        }

        if (!bIsLoading) {
            // 20110603 add support for creating undoable events if oaThreadLocal.createUndoablePropertyChanges=true
            //      default=false, which means that the individual UI components are controlling this
            if (OAThreadLocalDelegate.getCreateUndoablePropertyChanges()) {
                if (OAUndoManager.getUndoManager() != null) {
                    OAUndoableEdit ue = OAUndoableEdit.createUndoablePropertyChange("", oaObj, propertyName, oldObj, newObj); 
                    OAUndoManager.add(ue);
                }
            }
        }
   
        // 20151117 if one2one, and new value is null, then set prop to null in link prop
        if (linkInfo != null){
            OALinkInfo revLinkInfo = OAObjectInfoDelegate.getReverseLinkInfo(linkInfo);
            if (revLinkInfo != null) {
                if (revLinkInfo.type == OALinkInfo.ONE) {
                    if (oldObj instanceof OAObjectKey) {
                        if (OASync.isClient(oaObj)) { // 20151117 dont get from server if this is client
                            Object objx = OAObjectCacheDelegate.get(linkInfo.toClass, (OAObjectKey)oldObj);
                            if (objx instanceof OAObject) OAObjectPropertyDelegate.setPropertyCAS((OAObject)objx, revLinkInfo.getName(), null, oaObj);
                        }
                    }
                }                
            }
        }
        
    	// Note: this needs to be ran even if isSuppressingEvents(), it wont send messages but it might need to update detail hubs
    	if (!bIsLoading) {
            if (OAObjectHubDelegate.isInHub(oaObj)) {  // 20110719 needs to send if obj is in a Hub - in case other clients need the change
                sendHubPropertyChange(oaObj, propertyName, oldObj, newObj, linkInfo);
            }
    	    OAObjectCacheDelegate.fireAfterPropertyChange(oaObj, origKey, propertyName, oldObj, newObj, bLocalOnly, true);
    	}

    	oaObj.changedFlag = bChangeHold;
	
        // 20160304
        if (!bIsLoading) {
            if (oi.getHasTriggers()) {
                HubEvent hubEvent = new HubEvent(oaObj, propertyName, oldObj, newObj);
                oi.onChange(oaObj, propertyName, hubEvent);
            }
        }
    	
        // set to Changed
        if (!bIsChangeProp && bSetChanged && !bChangeHold && (calcInfo == null)) {
            if (!oaObj.isChanged()) {
                if (linkInfo == null || !linkInfo.bCalculated) { // 20120429
                    try {
                        OAThreadLocalDelegate.setSuppressCSMessages(true);  // the client will setChanged when it gets the propertyChange message
                        oaObj.setChanged(true);
                    }
                    finally {
                        OAThreadLocalDelegate.setSuppressCSMessages(false);
                    }
                }
            }
        }
        
        if (linkInfo != null) {
        	updateLink(oaObj, oi, linkInfo, oldObj, newObj);
        }
	}

	
	
	protected static void sendHubBeforePropertyChange(OAObject oaObj, String propertyName, Object oldObj, Object newObj) {
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
        if (hubs == null) return;
        for (Hub h : hubs) {
            if (h != null) HubEventDelegate.fireBeforePropertyChange(h, oaObj, propertyName, oldObj, newObj);
        }
	}	

	public static void sendHubPropertyChange(final OAObject oaObj, final String propertyName, final Object oldObj, final Object newObj, final OALinkInfo linkInfo) {
    	// Note: don't add this, HubEventDelegate will do it after it updates detail hubs:
		//        if (OAObjectFlagDelegate.isSuppressingPropertyChangeEvents()) return;
		// Note: oldObj could be OAObjectKey
		
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
        if (hubs == null) return;
        for (Hub h : hubs) {
            if (h != null) HubEventDelegate.fireAfterPropertyChange(h, oaObj, propertyName, oldObj, newObj, linkInfo);
        }

/* 20101218 replaced by HubListenerTree
        
        // Check to see if a Calculated property is changed.
        / * how do properties from other link object notify this objects calc objects?
        Answer: when you add a HubListener to Hub, it will create detail hub and
            listeners and send calcPropertyChange event
            @see Hub#addHubListener(HubListener hl, String property) {
        this code here will check for property changes within this object and determine
        if it affects a calc property
        * /
        // see if the property change affects a Calc property
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        ArrayList al = oi.getCalcInfos();
        for (int i=0; i < al.size(); i++) {
        	OACalcInfo ci = (OACalcInfo) al.get(i); 
            if (ci.getListenerCount() == 0) continue;  // set by HubEventDelegate.addHubListener(..., property) when a calc property is being used and prop changes need to be checked (here).
            String[] s = ci.properties;
            for (int j=0; s != null && j < s.length; j++) {
                if (propertyName.equalsIgnoreCase(s[j])) {
                    for (j=0; j<h.length; j++) {
                    	HubEventDelegate.fireCalcPropertyChange(h[j], oaObj, ci.getName());
                    }
                    break;
                }
            }
        }
*/        
	}

	
	/**
	    Called by firePropertyChange when a reference object is changed.<br>
	    This will move this object out of one Hub and into another when a property is changed.<br>
	    This will also manage changes that involve recursive relationships.
	    <p>
	    Example: if the dept for an emp is changed, then the emp will be taken out of the orig
	    dept.hubEmp hub and put into the new dept hubEmp
	*/
	private static void updateLink(final OAObject oaObj, OAObjectInfo oi, OALinkInfo linkInfo, Object oldObj, Object newObj) {
		// NOTE: oldObj could be OAObjectKey
        // taken out, since it will set OAClientThread.status = STATUS_FinishingAsServer 		
        //		if (!OAClientDelegate.processIfServer()) return; // only process on server, and send events to clients (even if this is OAThreadClient)
	    
	    OALinkInfo revLinkInfo = OAObjectInfoDelegate.getReverseLinkInfo(linkInfo);
	    if (revLinkInfo == null) return;
	
	    Object obj;

	    // 20160426 make sure that it has not changed
	    obj = OAObjectPropertyDelegate.getProperty(oaObj, linkInfo.name);
	    if (obj != newObj) {
	        return;
	    }
	    
	    if (revLinkInfo.type == OALinkInfo.ONE) {
	        try {
	            OAObjectInfo oiRev = OAObjectInfoDelegate.getOAObjectInfo(linkInfo.toClass);
	            Method m = OAObjectInfoDelegate.getMethod(oiRev, "get"+revLinkInfo.name, 0); // make sure that the method exists
	        	if (m != null) {
                    if (oldObj instanceof OAObjectKey) {
                        if (OASync.isClient(oaObj)) { // 20151117 dont get from server if this is client
                            oldObj = OAObjectCacheDelegate.get(linkInfo.toClass, (OAObjectKey)oldObj);
                        }
                        else {
                            oldObj = OAObjectReflectDelegate.getObject(linkInfo.toClass, (OAObjectKey)oldObj);
                        }
                    }
	                if (oldObj != null) {
	                    // 20150820 if one2one, then dont load if null and isClient
	                    //   this was discovered when deleting an IDL and function/gsmrFunction (1to1) kept going to server for other value
	                    boolean b = true;
	                    if (OASync.isClient(oaObj)) {
	                        obj = OAObjectPropertyDelegate.getProperty((OAObject)oldObj, revLinkInfo.name);
	                        if (obj == null) {
	                            // dont get from server
	                            b = false;
	                        }
	                    }
	            	    if (b) {
    	                	obj = OAObjectReflectDelegate.getProperty((OAObject)oldObj, revLinkInfo.name);
    	                    if (obj == oaObj) {
    	                    	OAObjectReflectDelegate.setProperty((OAObject)oldObj, revLinkInfo.name, null, null);
    	                    }
	            	    }
	                }
	                if (newObj != null) {
	                    obj = OAObjectReflectDelegate.getProperty((OAObject)newObj, revLinkInfo.name);
	                    if (obj != oaObj) {
	                    	OAObjectReflectDelegate.setProperty((OAObject)newObj, revLinkInfo.name, oaObj, null);
	                    }
	                }
	            }
	        }
	        catch (Exception e) {
	        }
	        return;
	    }
	
	    if (revLinkInfo.type != OALinkInfo.MANY) return;
	
	    Hub hub;
	    boolean bUpdateHub = false;

	    // 20131009 each link now has its own recursive flag
	    OALinkInfo liRecursive;
	    if (revLinkInfo.bRecursive) {
	        liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.ONE);  // ex: "ParentSection"
	    }
	    else liRecursive = null;
	    //was: OALinkInfo liRecursive = OAObjectInfoDelegate.getRecursiveLinkInfo(oi, OALinkInfo.ONE);  // ex: "ParentSection"
	
	    boolean bOldIsKeyOnly = (oldObj instanceof OAObjectKey);
	    
	    // find all Hubs using this as the active object.
	    // By changing a reference property, the object could be moved to another hub
	    ArrayList<Hub> alUpdateHub = null;
	    if (oldObj != null || liRecursive != null) {
	        Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
	        if (hubs != null) {
    	        for (Hub h : hubs) {
                    if (h == null) continue;
    	            
                    // 20120716
                    OAFilter<Hub> filter = new OAFilter<Hub>() {
                        @Override
                        public boolean isUsed(Hub h) {
                            return (h.getAO() == oaObj);
                        }
                    };
                    Hub[] hubss = HubShareDelegate.getAllSharedHubs(h, filter);
    	            
    	            //was:Hub[] hubss = HubShareDelegate.getAllSharedHubs(h);
    	            for (int ii=0; ii<hubss.length; ii++) {
    	                hub = hubss[ii];
    	                if (hub.getAO() == oaObj) {
    	                    if (alUpdateHub == null) alUpdateHub = new ArrayList<Hub>();
    	                    alUpdateHub.add(hub);
    	                }
    	            }
    	        }
	        }
	    }
	    
	    
	    /* recursive hub logic
	        See if recursive hub
	        ex:  Section.setCatalog(catalog)  or  Section.setParentSection(section)
	        This: "Section"
	        Changed Prop: "Catalog" or "ParentSection"
	
	        linkInfo: from Section -> Catalog or ParentSection
	        toLinkInfo: =  from  Catalog or ParentSection -> Sections
	        liRecursive = "ParentSection"
	        Note: all recursive objects all assigned to the same owner object as the root hub.
	            ex: all sections under a Catalog have Catalog assigned to it.
	            This allows for queries to find all sections for a catalog
	            To find all root (top level) sections for a catalog, select sections without a parentSection assigned
	    */
	    if (liRecursive != null) {  // if recursive
	        if (revLinkInfo.getOwner() && linkInfo != liRecursive) {
	            // owner property changed.  ex: "Catalog"
	            // need to update all recursive objects under this one.  ex: "hubSections.section.catalog = catalog"
	        	
	        	obj = OAObjectReflectDelegate.getProperty(oaObj, OAObjectInfoDelegate.getReverseLinkInfo(liRecursive).getName()); // hubSections
	            if (!(obj instanceof Hub)) throw new RuntimeException("OAObject.updateLink() method for recursive link not returning a Hub.");
	            hub = (Hub) obj;
	            for (int i=0; ;i++) {
	                obj = hub.elementAt(i);  // section
	                if (obj == null) break;
	                if (OAObjectReflectDelegate.getProperty((OAObject)obj, linkInfo.getName()) != newObj) {
	                	OAObjectReflectDelegate.setProperty((OAObject)obj, linkInfo.getName(), newObj, null);  // setCatalog.  This will set all of its recursive children
	                }
	            }
	
	            obj = OAObjectReflectDelegate.getProperty(oaObj, liRecursive.getName()); // get parent (section)
	            if (obj != null) {
		            obj = OAObjectReflectDelegate.getProperty((OAObject)obj, linkInfo.getName()); // catalog
	                if (obj == newObj) newObj = null; // otherwise, this object will be added to the rootHub
	                else {
	                    // set Parent to null  2003/09/21
	                	OAObjectReflectDelegate.setProperty(oaObj, liRecursive.getName(), null, null); // set ParentSection = null
	                }
	            }
	        }
	        else {
	            if (liRecursive == linkInfo) {
	                // parent property changed.  ex: "setParentSection"
	
	                // verfy that it can be placed
	                if (newObj != null) {
	                    if (oaObj == newObj) {  // object cant be its own parent
	                    	if (bOldIsKeyOnly) {
	                    		bOldIsKeyOnly = false;
	                    		oldObj = OAObjectReflectDelegate.getObject(linkInfo.toClass, (OAObjectKey)oldObj);
	                    	}
	                    	OAObjectReflectDelegate.setProperty(oaObj, linkInfo.getName(), oldObj, null);
	                        throw new RuntimeException("Can not set the Parent to Itself");
	                    }
	                    // cant assign a child of this object as the new parent - causes orphaned objects
	                    for (obj=newObj; ;) {
	                    	obj = OAObjectReflectDelegate.getProperty((OAObject)obj, liRecursive.getName());
	                        if (obj == null) break;
	                        if (obj == oaObj) {
		                    	if (bOldIsKeyOnly) {
		                    		bOldIsKeyOnly = false;
		                    		oldObj = OAObjectReflectDelegate.getObject(linkInfo.toClass, (OAObjectKey)oldObj);
		                    	}
		                    	OAObjectReflectDelegate.setProperty(oaObj, linkInfo.getName(), oldObj, null);
	                            throw new RuntimeException("Can not assign Parent to a Child");// causes orphans
	                        }
	                    }
	                }
	
	                // find owner link
	                boolean bOwned = false;
	                OALinkInfo linkOwner = OAObjectInfoDelegate.getLinkToOwner(oi); // link to catalog
	                OALinkInfo liRev = null;
	                if (linkOwner != null) liRev = OAObjectInfoDelegate.getReverseLinkInfo(linkOwner);
	
	                if (liRev != null && liRev.type == OALinkInfo.MANY) {
	                    bOwned = true;
	                    if (newObj == null) {  // parentSection = null
	                        // if being set to null, then add to root hub.
	                        // if it was removed from old hub, then dont add to root hub
	                        boolean bAdd = !OAThreadLocalDelegate.isDeleting(oaObj);

	                        if (bAdd && !bOldIsKeyOnly && OAObjectReflectDelegate.isReferenceHubLoadedAndNotEmpty((OAObject)oldObj, revLinkInfo.getName())) {
	                            hub = (Hub) OAObjectReflectDelegate.getProperty((OAObject)oldObj, revLinkInfo.getName()); // Catalog.sections (original hub that this objects belonged to)
	                            bAdd = hub.contains(oaObj);
	                        }
	
	                        if (bAdd) {
	                        	obj = OAObjectReflectDelegate.getProperty(oaObj, linkOwner.getName()); // Catalog
	                            if (obj != null) {
	                                Object obj2 = OAObjectReflectDelegate.getProperty((OAObject)obj, liRev.getName()); // catalog.hubSection
	                                if (!(obj2 instanceof Hub)) {
	                                    throw new RuntimeException("OAObject.updateLink() method for recursive link owner not returning a Hub.");
	                                }
	                                hub = (Hub) obj2;
	                                if (hub.getObject(oaObj) == null) hub.add(oaObj);
	                            }
	                        }
	                    }
	                    else {
	                        // make sure owner is set for this object.  this.catalog = ((Section)newObj).catalog
	                    	obj = OAObjectReflectDelegate.getProperty((OAObject)newObj, linkOwner.getName());
	                    	
	                        if (OAObjectReflectDelegate.getProperty(oaObj, linkOwner.getName()) != obj) {
	                        	OAObjectReflectDelegate.setProperty(oaObj, linkOwner.getName(), obj, null); // setCatalog (this will also set child recursive objects)
	                        }
	
	                        if (oldObj == null) {
	                            // remove from root hub, it is now assigned a parentSection
		                    	obj = OAObjectReflectDelegate.getProperty(oaObj, linkOwner.getName()); // Catalog
	                            if (obj != null) {
	                            	obj = OAObjectReflectDelegate.getProperty((OAObject) obj, liRev.getName()); // catalog.catalogSections
	                                if (!(obj instanceof Hub)) {
	                                    throw new RuntimeException("OAObject.updateLink() method for recursive link owner not returning a Hub.");
	                                }
	                                hub = (Hub) obj; // catalog.catalogSections
	                	            HubAddRemoveDelegate.remove(hub, oaObj, false, true, false, true, false, false);
	                            }
	                        }
	                    }
	                }
	
	                if (!bOwned) {
	                    Hub h = OAObjectInfoDelegate.getRootHub(oi);
	                    if (h != null) {
	                        if (oldObj == null) {
	                            // take out of unowned root hubs
	                            h.remove(oaObj);
	                        }
	                        else if (newObj == null) {
	                            // add to unowned root hubs
	                            // if it was removed from old hub, then dont add to root hub
	                            boolean bAdd = true;
	                            if (oldObj != null && !bOldIsKeyOnly && OAObjectReflectDelegate.isReferenceHubLoaded((OAObject)oldObj,revLinkInfo.getName())) {
	                            	hub = (Hub) OAObjectReflectDelegate.getProperty((OAObject)oldObj, revLinkInfo.getName()); // Catalog.sections (original hub that this objects belonged to)
	                                bAdd = hub.contains(oaObj);
	                            }
	                            if (bAdd && h.getObject(oaObj) == null) {
                                    h.add(oaObj);
	                            }
	                        }
	                    }
	                }
	            }
	        }
	    }
	    // end of recursive logic
	
	    if (oldObj != null && !bOldIsKeyOnly) {
	        try {
	        	if (OAObjectCSDelegate.isServer(oaObj) || OAObjectReflectDelegate.isReferenceHubLoaded((OAObject)oldObj, revLinkInfo.getName())) { 
	            	obj = OAObjectReflectDelegate.getProperty((OAObject)oldObj, revLinkInfo.getName()); 
	    	        if (obj instanceof Hub) {
	    	            Hub h = (Hub) obj;
	    	            if (h.contains(oaObj)) h.remove(oaObj);
	    	        }
	        	}
	        }
	        catch (Exception e) {
	        }
	    }
	
	    if (newObj != null) {
	        try {
	        	if (OAObjectCSDelegate.isServer(oaObj) || OAObjectReflectDelegate.isReferenceHubLoaded((OAObject)newObj, revLinkInfo.getName())) { 
	        	    hub = (Hub) OAObjectReflectDelegate.getProperty((OAObject)newObj, revLinkInfo.getName());
	            	
	            	// 20130630 added autoAttach check
                    boolean b = OAObjectDelegate.getAutoAdd(oaObj);

                    boolean bMasterFlag = false;
                    OAObject objx = hub.getMasterObject();
                    if (objx != null && !OAObjectDelegate.getAutoAdd(objx)) {
                        bMasterFlag = true;
                    }
                    
	            	if (b || bMasterFlag) {
	    	            hub.add(oaObj);
	    	            
	    	            if (bMasterFlag && b && oaObj.isNew()) {
    	                    // turn off autoAdd for this object
    	                    OAObjectDelegate.setAutoAdd(oaObj, false);
	    	            }
                    }
	        	}
	        }
	        catch (Exception e) {
	        }
	    }
	
	    // reset Hub activeObjects in shared hubs
	    if (alUpdateHub != null) {
	        int x = alUpdateHub.size();
	        for (int i=0; i<x; i++) {
	            hub = (Hub) alUpdateHub.get(i);
	            // 20110805 dont allow adjusting master if hub is not shared, or if it does not have a masterHub
                boolean bAllowAdjustMaster = (newObj != null) && (hub.getSharedHub()!=null && HubDetailDelegate.getHubWithMasterHub(hub)!=null);
                HubAODelegate.setActiveObject(hub, oaObj, bAllowAdjustMaster, false, false); // adjMaster, updateLink, force
                //was: HubAODelegate.setActiveObject(hub, oaObj, (newObj != null), false, false); // adjMaster, updateLink, force
	        }
	    }
	}

	
	protected static void fireAfterLoadEvent(OAObject oaObj) {
        Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
        if (hubs == null) return;
        for (Hub h : hubs) {
            if (h != null) HubEventDelegate.fireAfterLoadEvent(h, oaObj);
        }
	}
	
}


