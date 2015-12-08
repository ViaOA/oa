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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.util.*;
import com.viaoa.hub.*;


// 2007/10/31 qqqqqqqqqq NOTE: Have DataSource use  OAObjectReflectDelegate.getRawReference(oaObj, prop) to get reference properties ..............

public class OAObjectSaveDelegate {
    private static Logger LOG = Logger.getLogger(OAObjectSaveDelegate.class.getName());
    
    protected static void save(OAObject oaObj, int iCascadeRule) {
    	if (oaObj == null) return;

        if (OAObjectCSDelegate.isWorkstation(oaObj)) {
            OAObjectCSDelegate.save(oaObj, iCascadeRule);
        	return;
        }

        OACascade cascade = new OACascade();
    	save(oaObj, iCascadeRule, cascade, true);
    }

    public static void save(OAObject oaObj, int iCascadeRule, OACascade cascade) {
        save(oaObj, iCascadeRule, cascade, false);
    }
    
    // also called by HubSaveDelegate
    public static void save(OAObject oaObj, int iCascadeRule, OACascade cascade, boolean bIsFirst) {
        if (cascade.getDepth() > 50) {
            if (!cascade.wasCascaded(oaObj, false)) {
                cascade.add(oaObj);
            }
            return;
        }
        if (cascade.wasCascaded(oaObj, true)) return;
        cascade.depthAdd();
        
        OAObjectSaveDelegate._save(oaObj, true, iCascadeRule, cascade); // "ONE" relationships

        // cascadeSave() will check hash to see if object has already been checked
        if (oaObj.newFlag || oaObj.changedFlag || bIsFirst) {
            Hub[] hubs = OAObjectHubDelegate.getHubReferences(oaObj);
            if (hubs != null) {
                for (Hub h : hubs) {
                    if (h != null) HubEventDelegate.fireBeforeSaveEvent(h, oaObj);
                }
            }
            
            for (int i=0; i<2; i++) {
                if (OAObjectSaveDelegate.onSave(oaObj)) break;
                
                // try again, object might have been changed in the process
                String msg = "error saving, class="+oaObj.getClass().getName()+", key="+oaObj.getObjectKey();
                if (i == 0) msg += ", will try again now";
                else msg += ", will try again the next time save is called";
                LOG.warning(msg);
                
                OAObjectSaveDelegate._save(oaObj, true, iCascadeRule, cascade); // "ONE" relationships
            }
            
            if (hubs != null) {
                for (Hub h : hubs) {
                	if (h != null) HubEventDelegate.fireAfterSaveEvent(h, oaObj);
                }
            }
        }
        OAObjectSaveDelegate._save(oaObj, false, iCascadeRule, cascade); // "MANY" relationships
        
        cascade.depthSubtract();
        if (cascade.getDepth() < 1) {
            ArrayList<Object> al = cascade.getList();
            if (al != null) {
                cascade.clearList();
                cascade.setDepth(0);
                if (al != null) {
                    for (Object obj : al) {
                        save(((OAObject) obj), iCascadeRule, cascade, false);
                    }
                }
            }
        }
    }

    
	/**
	 * Called by HubSaveDelegate.saveAll() to save all New Many2Many added objects.
	 */
	public static void _saveObjectOnly(OAObject oaObj, OACascade cascade) {
		_save(oaObj, true, OAObject.CASCADE_NONE, cascade);
		onSave(oaObj);                		
	}
    
	/**
		Internal method used when saving an objects cascade save references.
		<p>
		Check all Links with TYPE=MANY and CASCADE=true to either call "save()" or to check
		if objects can be saved.<br>
		This will also check any Link with TYPE=ONE to see if isNew().
		If it isNew then it will be saved (but not its links) before this object can be saved.
		This is needed since the OADataSource's will require the parent to exist before this object
		can be saved.
		@param checkOnly if true then "canSave" is called, else "save()" is called
		@return null if all objects can be saved
	*/
	private static void _save(OAObject oaObj, boolean bOne, int iCascadeRule, OACascade cascade) {
		OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj);
		List al = oi.getLinkInfos();
		for (int i=0;  i < al.size(); i++) {
			OALinkInfo li = (OALinkInfo) al.get(i);
			
		    if (bOne != (li.type == OALinkInfo.ONE)) continue;
			
			if (li.getTransient()) continue;
            if (li.getCalculated()) continue;
            if (li.getPrivateMethod()) continue;
		    String prop = li.getName();
		    if (prop == null || prop.length() < 1) continue;
		    
	        if (OAObjectReflectDelegate.isReferenceNullOrNotLoaded(oaObj, prop)) continue;
            
		    boolean bValidCascade = false;
            if (iCascadeRule == OAObject.CASCADE_LINK_RULES && li.cascadeSave) bValidCascade = true;
            else if (iCascadeRule == OAObject.CASCADE_OWNED_LINKS && li.getOwner()) bValidCascade = true;
            else if (iCascadeRule == OAObject.CASCADE_ALL_LINKS) bValidCascade = true;

    		// Note: if (iCascadeRule == OAObject.CASCADE_NONE) then only save ONE links that are new objects - so ref integrity is maintained.
            
		    if (li.type == OALinkInfo.ONE) {
		        Object obj = OAObjectReflectDelegate.getProperty(oaObj, li.getName());
	            if ((obj instanceof OAObject)) {
	            	OAObject oaRef = (OAObject) obj;
		            if (oaRef.getNew()) {
		            	if (cascade.wasCascaded(oaRef, false)) {
		            		// have to save new reference object before oaObj can be saved.
			    		    OAObjectInfo oiRef = OAObjectInfoDelegate.getOAObjectInfo(oaRef.getClass());
			    		    if (oiRef.getUseDataSource()) {
			    			    OAObjectDSDelegate.saveWithoutReferences(oaRef);
			    		    }
			    		    OAObjectDelegate.setNew(oaRef, false);
		                }
		                else {
		                	if (bValidCascade) save(oaRef, iCascadeRule, cascade);
		                	else {
		                	    save(oaRef, OAObject.CASCADE_NONE, cascade); 
		                	}
		                }
		            }
	                else {
	                	if (bValidCascade) save(oaRef, iCascadeRule, cascade);
	                }
	            }
		    }
		    else {
	    		if (iCascadeRule == OAObject.CASCADE_NONE) continue;
			    if (bValidCascade) {
			        Hub hub = (Hub) OAObjectReflectDelegate.getProperty(oaObj, li.getName());  // get/load "real" objects
		        	OAObjectHubDelegate.saveAll(hub, iCascadeRule, cascade);
			    }
			    else {
			    	// save all adds/removes from hub.
				    Hub hub = (Hub) OAObjectReflectDelegate.getRawReference(oaObj, prop); // could be Hub with OAObjectKey objects
				    if (hub.isOAObject()) {
				    	// update all links even if cascade is false
				    	OAObjectHubDelegate.saveAll(hub, OAObject.CASCADE_NONE, cascade); // only save M2M link changes, not the actual objects in the Hub.
				    }
			    }
		    }
		}
	}
	
	private static ArrayList<Integer> alSaveNewLock = new ArrayList<Integer>(5);
	
    /** @param bFullSave false=dont flag as unchanged, used when object needs to be saved twice. First to create
	    object in datasource so that reference objects can refer to it
	*/
	protected static boolean onSave(OAObject oaObj) {
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(oaObj.getClass());

        // if new, then need to hold a lock
	    boolean bIsNew = oaObj.isNew();
	    if (bIsNew) {
//qqqqqqqqqqqqqqqqqqqqqq	        
//LOG.warning("SAVING NEW vvvvvv: obj="+oaObj+", id="+oaObj.getProperty("id"));	        
	        synchronized (alSaveNewLock) {
	            boolean b = false;
	            for ( ; ; ) {
	                if (!alSaveNewLock.contains(oaObj.guid)) {
	                    if (b) return true; // already saved
	                    alSaveNewLock.add(oaObj.guid);
	                    break;
	                }
	                b = true;
	                try {
	                    alSaveNewLock.wait();
	                }
	                catch (Exception e) {}
	            }    
	        }
	    }
	    
        /*
        if (oi.getUseDataSource()) {
            OAObjectKey key = OAObjectKeyDelegate.getKey(oaObj);
            String s = String.format("Save, class=%s, id=%s",
                    OAString.getClassName(oaObj.getClass()),
                    key.toString()
            );
            OAObject.OALOG.fine(s);
        }
        */
	    
	    try {
            // 20130504 moved before actually save, in case another thread makes a change
            oaObj.setDeleted(false);  // in case it was deleted, and then re-saved
            oaObj.setChanged(false);
	        
            if (oi.getUseDataSource()) {
                try {
                    OAObjectDSDelegate.save(oaObj);
                }
                catch (Exception e) {
                    String msg = "error saving, class="+oaObj.getClass().getName()+", key="+oaObj.getObjectKey();
                    LOG.log(Level.WARNING, msg, e);
                    oaObj.setChanged(true);
                    return false;
                }
            }
            OAObjectLogDelegate.logToXmlFile(oaObj, true);
            if (bIsNew) {
                OAObjectDelegate.setNew(oaObj, false);
            }
	    }
	    finally {
	        if (bIsNew) {
	            synchronized (alSaveNewLock) {
                    alSaveNewLock.remove((Object) (new Integer(oaObj.guid)) ); // needs to use Object instead of primitive
                    alSaveNewLock.notifyAll();           
	            }
	        }
	        //was, before 5/4:
	        // 20130404 moved to always mark as saved, so that an error can not keep other objects from saving later
	        //oaObj.setDeleted(false);  // in case it was deleted, and then re-saved
	        //oaObj.setChanged(false);
	    }
        oaObj.saved();
        return true;
	}
	
}




