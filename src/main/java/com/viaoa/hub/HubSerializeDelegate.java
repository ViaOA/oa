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

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

import com.viaoa.object.*;


/**
 * Delegate used for serializing Hub.
 * @author vvia
 *
 */
public class HubSerializeDelegate {
	private static Logger LOG = Logger.getLogger(HubSerializeDelegate.class.getName());

	/**
	    Used by serialization to store Hub.
	*/
	protected static void _writeObject(Hub thisHub, java.io.ObjectOutputStream stream) throws IOException {
	    if (HubSelectDelegate.isMoreData(thisHub)) {
	        try {
	        	OAThreadLocalDelegate.setSuppressCSMessages(true);
	            HubSelectDelegate.loadAllData(thisHub);  // otherwise, client will not have the correct datasource
	        }
	        finally {
	        	OAThreadLocalDelegate.setSuppressCSMessages(false);	        
	        }
	    }
	    stream.defaultWriteObject();
	}
	
	public static int replaceObject(Hub thisHub, OAObject objFrom, OAObject objTo) {
		if (thisHub == null) return -1;
        if (thisHub.data == null) return -1;
		if (thisHub.data.vector == null) return -1;
		int pos = thisHub.data.vector.indexOf(objFrom);
		if (pos >= 0) thisHub.data.vector.setElementAt(objTo, pos);
		return pos;
	}

	public static void replaceMasterObject(Hub thisHub, OAObject objFrom, OAObject objTo) {
		if (thisHub == null) return;
		if (thisHub.datam.masterObject == objFrom) thisHub.datam.masterObject = objTo;
	}
	
	/** qqqqqqqqqqq
	 * Used by OAObjectSerializeDelegate, should only be needed to handle some temp "bad" files.
	 */
	public static boolean isResolved(Hub thisHub) {
		return (thisHub != null && thisHub.data != null && thisHub.data.vector != null);
	}

	/**
	    Used by serialization when reading objects from stream.
	*/
	protected static Object _readResolve(Hub thisHub) throws ObjectStreamException {
		HubDelegate.setObjectClass(thisHub, thisHub.datau.objClass);  // this will update HubController and datau
	    if (thisHub.datam == null) thisHub.datam = new HubDataMaster();

	    if (thisHub.datam.masterObject != null && thisHub.datam.liDetailToMaster != null) {
	        // need to reasign  linkInfo, to eliminate linkinfo dups
	    	boolean bFound = false;	    	
	        if (thisHub.datau.objectInfo != null) {  // will only happen if objClass is null
	        	ArrayList al = thisHub.datau.objectInfo.getLinkInfos();
	        	OALinkInfo liOld = thisHub.datam.liDetailToMaster;
	        	for (int i=0; i < al.size(); i++) {
	            	OALinkInfo li = (OALinkInfo) al.get(i);
	            	if (liOld == li) {
	            		bFound = true;
	            		break;
	            	}
	                if ((liOld != null) && (liOld.getName() != null)) {
	                    if (liOld.getName().equalsIgnoreCase(li.getName())) {
	                    	thisHub.datam.liDetailToMaster = li;
	                    	bFound = true;//qqqqqq
	                        break;
	                    }
	                }
	            }
	        	if (!bFound) {
		        	for (int i=0; i < al.size(); i++) {
		            	OALinkInfo li = (OALinkInfo) al.get(i);
		            	OALinkInfo liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
		            	if (liRev == null) continue;
	                    if (liRev.getType() == OALinkInfo.MANY && thisHub.datam.masterObject.getClass().equals(li.getToClass()) ) {
	                    	thisHub.datam.liDetailToMaster = li;
                    		bFound = true;
	                        break;
	                    }
	                }
	        		
	        	}
	        }
			if (!bFound) {
				LOG.warning("Could not find OALinkInfo, hub="+thisHub);
			}
	        HubDetailDelegate.setMasterObject(thisHub, thisHub.datam.masterObject, thisHub.datam.liDetailToMaster);
	    }
	    if (thisHub.datau.sharedHub != null) {
	        thisHub.datau.sharedHub = null; // so gc() will dispose this hub
	    }
	
		if (thisHub.data.bSelectAllHub) {
			OAObjectCacheDelegate.setSelectAllHub(thisHub);
		}
	    
        /* 20110204 removed - sortListener for client is now created in OAObjectReflectDelegate.getReferenceHub(..)
            //          otherwise, the hubSortListener might have a dependent propPath that has to fetch other objects from server
        if (thisHub.data.sortProperty != null) {
            thisHub.data.sortListener = new HubSortListener(thisHub, null, thisHub.data.sortProperty, thisHub.data.sortAsc);
        }
        */
		boolean b = false;
		for (int i=0; ; i++) {
		    
		    if (thisHub.data == null || thisHub.data.vector == null) {
		        // break;
		        //qqqqqqqqqqqqqqqq BAD bugger qqqqqqqqqqqq		        
		    }
		    
		    Object obj = thisHub.getAt(i);
		    if (obj == null) break;
		    
		    Object key = obj;
		    if (thisHub.datau.oaObjectFlag) {
		        if (!b) {
		            // dont initialize this hub if the master object is a duplicate.
		            // check by looking to see if this object already belongs to a hub that has the same masterObject/linkinfo
		            if ( OAObjectHubDelegate.isAlreadyInHub((OAObject)obj, thisHub.datam.liDetailToMaster) ) {
		                return thisHub;
		            }
		            b = true;
		        }
		        OAObjectHubDelegate.addHub((OAObject) obj, thisHub);
		    }
		}

		return thisHub;
	}

	
	
}
