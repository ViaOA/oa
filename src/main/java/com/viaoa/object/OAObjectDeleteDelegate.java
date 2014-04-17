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

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.viaoa.ds.OADataSource;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubAddRemoveDelegate;
import com.viaoa.hub.HubDSDelegate;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.hub.HubDelegate;
import com.viaoa.hub.HubDetailDelegate;
import com.viaoa.hub.HubEventDelegate;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAString;

public class OAObjectDeleteDelegate {

    /**
	    Same as delete, without first calling canDelete.
	    @see #delete()
	*/
	public static void delete(OAObject oaObj) {
		if (OAObjectCSDelegate.isWorkstation()) {
		    if (OAObjectCSDelegate.delete(oaObj)) {
		        OARemoteThreadDelegate.startNextThread(); // will be processed by OAObjectServerimpl (delete will be done on server)
		        return;
		    }
		    else {
		        if (!OAObjectInfoDelegate.getOAObjectInfo(oaObj).getLocalOnly()) {
		            return;
		        }
		        // else, local only object that needs to be removed locally only
		    }
		}

		/*
        OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
        String s = String.format("Delete, class=%s, id=%s",
                OAString.getClassName(oaObj.getClass()),
                key.toString()
        );
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        if (oi.bUseDataSource) {
            OAObject.OALOG.fine(s);
        }
        */
		
		OACascade cascade = new OACascade();
        delete(oaObj, cascade);
	}

	
	// 20090822
	/**
	 * Used to know if an object has been deleted, by calling OAObject.delete().
	 * This will send message, and remove oaObj from any other Hubs.
	 * @param oaObj
	 * @param tf
	 */
    public static void setDeleted(OAObject oaObj, boolean tf) {
        if (oaObj.deletedFlag != tf) {
            boolean bOld = oaObj.deletedFlag;
            oaObj.deletedFlag = tf;
            OAObjectEventDelegate.firePropertyChange(oaObj, OAObjectDelegate.WORD_Deleted, bOld?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE, oaObj.deletedFlag?OAObjectDelegate.TRUE:OAObjectDelegate.FALSE, false, false);
            if (tf) {
                if (OARemoteThreadDelegate.isRemoteThread()) {
                    // remove from all hubs - in case the object was used in other hubs that were not used on the server where the delete is performed.
                    WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferences(oaObj);
                    if (refs != null) {
                        for (WeakReference<Hub<?>> ref : refs) {
                            if (ref == null) continue;
                            Hub h = ref.get();
                            if (h == null) continue;
                            HubAddRemoveDelegate.remove(h, oaObj, true, true, true, true, true);  // force, send, deleting, setAO
                        }
                    }
                }
            }
        }
    }
	
	public static void delete(final OAObject oaObj, OACascade cascade) {
	    if (cascade.wasCascaded(oaObj, true)) return;
	
        WeakReference<Hub<?>>[] refs = OAObjectHubDelegate.getHubReferences(oaObj);
        if (refs != null) {
            for (WeakReference<Hub<?>> ref : refs) {
                if (ref == null) continue;
                Hub h = ref.get();
                if (h == null) continue;
                HubEventDelegate.fireBeforeDeleteEvent(h, oaObj);
            }
        }
        try {
            OAThreadLocalDelegate.setDeleting(oaObj, true);
	
	        OAObjectDeleteDelegate.deleteChildren(oaObj, cascade); // delete children first
	        if (!oaObj.getNew()) {
	        	OAObjectDeleteDelegate.onDelete(oaObj);  // this will delete from OADataSource
	        }

            oaObj.setDeleted(true);
	        
	        // remove from all hubs
            if (refs != null) {
                for (WeakReference<Hub<?>> ref : refs) {
                    if (ref == null) continue;
                    Hub h = ref.get();
                    if (h == null) continue;
    	            HubAddRemoveDelegate.remove(h, oaObj, true, true, true, true, true);  // force, send, deleting, setAO
    	        }
            }
            
	        // 20120702 if m2m and nullHub, then need to find any hub that is not in the getHubs()
	        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
	        for (OALinkInfo li : oi.getLinkInfos()) {
	            if (!li.getPrivateMethod()) continue;
	            
	            final OALinkInfo rev = OAObjectInfoDelegate.getReverseLinkInfo(li);
	            if (rev == null) continue;
	            OAObjectCacheDelegate.callback(new OACallback() {
                    @Override
                    public boolean updateObject(Object obj) {
                        if (OAObjectReflectDelegate.isReferenceNullOrNotLoadedOrEmptyHub((OAObject) obj, rev.getName())) return true;
                        Object objx = rev.getValue(obj);
                        if (!(objx instanceof Hub)) return true;
                        Hub hx = (Hub) objx;
                        hx.remove(oaObj);
                        return true;
                    }
                }, li.getToClass());
	        }
	        
	        oaObj.setChanged(false);
	        OAObjectDelegate.setNew(oaObj, true);
	    }
	    finally {
            OAThreadLocalDelegate.setDeleting(oaObj, false);
	    }
        if (refs != null) {
            for (WeakReference<Hub<?>> ref : refs) {
                if (ref == null) continue;
                Hub h = ref.get();
                if (h == null) continue;
                HubEventDelegate.fireAfterDeleteEvent(h, oaObj);
            }
        }
	}

	/**
	 * Checks to see if an Object can be deleted.
	 * Checks that all child links that have mustBeEmpty are empty.
	 * 
	 * NOTE: this is not called/used when deleteing an OAObject
	 */
    public static boolean canDelete(OAObject oaObj) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        List al = oi.getLinkInfos();
        for (int i=0; i < al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (!li.getMustBeEmptyForDelete()) continue;
            // if (li.getCalculated()) continue;
            
            String prop = li.name;
            if (prop == null || prop.length() < 1) continue;
            Object obj = OAObjectReflectDelegate.getProperty(oaObj, prop);
            if (obj == null) continue;
            
            if (li.getType() == OALinkInfo.ONE) {
                return false;
            }
            else {
                if (((Hub) obj).getSize() > 0) return false;
            }
        }
        return true;
    }
    public static OALinkInfo[] getMustBeEmptyBeforeDelete(OAObject oaObj) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
        List al = oi.getLinkInfos();
        OALinkInfo[] lis = null;
        for (int i=0; i < al.size(); i++) {
            OALinkInfo li = (OALinkInfo) al.get(i);
            if (!li.getMustBeEmptyForDelete()) continue;
            
            String prop = li.name;
            if (prop == null || prop.length() < 1) continue;
            Object obj = OAObjectReflectDelegate.getProperty(oaObj, prop);
            if (obj == null) continue;
            
            if (li.getType() == OALinkInfo.ONE) {
                lis = (OALinkInfo[]) OAArray.add(OALinkInfo.class, lis, li);
            }
            else {
                if (((Hub) obj).getSize() > 0) {
                    lis = (OALinkInfo[]) OAArray.add(OALinkInfo.class, lis, li);
                }
            }
        }
        return lis;
    }
	
	
	/**
	    Internal method used by delete(oaObj) when deleting an objects cascade delete references.
	    <p>
	    Checks to see if all Links with TYPE=MANY and CASCADE and be deleted.<br>
	    If reference object is not set up to be deleted (cascade delete is false), then it will
	    have its reference to this object set to null.
	    <p>
	    Steps:
	    <ol>
	    <li> delete any link objects
	    <li> if !cascade then remove and save all elements from detailHub.  This will
	         take out the reference to this object.
	    <li> if cascade then call Hub.deleteAll
	    </ol>
	*/
	private static void deleteChildren(OAObject oaObj, OACascade cascade) {
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
	    List al = oi.getLinkInfos();
	    for (int i=0; i < al.size(); i++) {
	    	OALinkInfo li = (OALinkInfo) al.get(i);
            if (li.getCalculated()) continue;
			
	    	String prop = li.name;
		    if (prop == null || prop.length() < 1) continue;
	    	
	        if (li.getType() == OALinkInfo.ONE) {
	            if (li.getOwner() || li.cascadeDelete) {
        	    	Object obj = OAObjectReflectDelegate.getProperty(oaObj, prop);
        	    	if (obj == null) continue;
                    if (obj instanceof OAObject) delete((OAObject) obj, cascade);
                    continue;
                }
	            
		    	OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
		    	if (liRev == null) continue;
		        if (liRev.getType() == OALinkInfo.ONE) {
			    	Object obj = OAObjectReflectDelegate.getProperty(oaObj, prop);
			    	if (obj == null) {
		                Method method = OAObjectInfoDelegate.getMethod(li);
		                if (method != null && ((method.getModifiers() & Modifier.PRIVATE) == 0) ) {
		                    continue;
		                }

		                // 20121011 no method for reference - need to find/select it and remove the reference
		                OADataSource ds = OADataSource.getDataSource(li.getToClass());
		                if (ds == null) return;
		                Iterator itx = ds.select(li.getToClass(), liRev.getName() + " = ?", oaObj, "");
		                if (itx != null && itx.hasNext()) obj = itx.next();
			    	}

			    	// this object is being deleted, remove its reference from reference object
		        	if (obj instanceof OAObject) {
	                    OAObjectReflectDelegate.setProperty((OAObject)obj, liRev.name, null, null);
                        OAObjectDSDelegate.removeReference((OAObject)obj, liRev);
		        	}
		        	continue;
		        }

	            // 20120907 if method is not created, then it uses a LinkTable; need to remove from liRev Hub and remove from link table 
                Method method = OAObjectInfoDelegate.getMethod(li);
                if (method != null && ((method.getModifiers() & Modifier.PRIVATE) == 0) ) {
                    continue;
                }
                
                OAObject masterObj;
                Hub hubx = OAObjectHubDelegate.getWeakRefHub(oaObj, li);
                if (hubx != null) {
                    masterObj = HubDelegate.getMasterObject(hubx);
                }
                else {
                    Object objx = OAObjectReflectDelegate.getReferenceObject(oaObj, li.getName());
                    if (objx instanceof OAObject) {
                        masterObj = (OAObject) objx;
                        objx = masterObj.getHub(liRev.getName());
                        if (objx instanceof Hub) {
                            hubx = (Hub) objx;
                        }
                    }
                    else masterObj = null;
                }

                if (masterObj != null) {
                    OADataSource ds = OADataSource.getDataSource(masterObj.getClass());
                    if (ds != null) {
                        ds.updateMany2ManyLinks(masterObj, null, new OAObject[] {oaObj}, liRev.name);
                    }
                }
                if (hubx != null) {
                    hubx.remove(oaObj);
                    HubDataDelegate.removeFromRemovedList(hubx, oaObj);
                }
                oaObj.removeProperty(li.getName());

	            continue;
	        }
	        
	        // Hub
	    	Object obj = OAObjectReflectDelegate.getProperty(oaObj, prop);
	    	if (!(obj instanceof Hub)) {  // no method assigned, need to get Hub directly.  Ex: a one2many where the one is used as a lookup.
	    		obj = OAObjectReflectDelegate.getReferenceHub(oaObj, prop, null, false, null);
	    	}
	    	Hub hub = (Hub) obj;
	        hub.loadAllData();
	
        	OAObjectHubDelegate.setMasterObject(hub, oaObj,OAObjectInfoDelegate.getReverseLinkInfo(li)); // make sure that master object is set.

            // 20120612 need to remove link table records
            boolean bIsM2m = OAObjectInfoDelegate.isMany2Many(li);
        	
	        if (!li.cascadeDelete && !li.getOwner()) {  // remove reference in any object to this object
                if (hub.isOAObject()) {
                    OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
                    int x = hub.getSize();
		            for (--x; x >= 0; x--) {
		                obj = hub.elementAt(x);
		                hub.remove(x);  // hub will set property for references master to null.

		                if (!bIsM2m) OAObjectDSDelegate.removeReference((OAObject)obj, liRev); // update DB so that fkey violation is not thrown
	                }
	            }
	        }
	        else {
	            OAObjectHubDelegate.deleteAll(hub, cascade);
	        }
            if (bIsM2m) {
                // 20120612 need to remove link table records
                HubDSDelegate.removeMany2ManyLinks(hub);
            }
	    }
	}
	
	
	/** called after beforeDelete() and after all listeners have been called.
	    It will find the OADataSource to use and call its "delete(this)"
	*/
	private static void onDelete(OAObject oaObj) {
		if (oaObj == null) return;
	    OAObjectLogDelegate.logToXmlFile(oaObj, false);
	    OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());
	    if (oi.getUseDataSource()) {
        	OAObjectDSDelegate.delete(oaObj);
	    }
	    oaObj.deleted();
	}
}


