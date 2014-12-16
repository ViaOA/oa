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

import com.viaoa.ds.OADataSource;
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfoDelegate;


/**
 * Delegate that manages datasource related functionality for Hub.
 * @author vvia
 *
 */
public class HubDSDelegate {

	/**
	    Returns the OADataSource that works with this objects Class.
	*/
	protected static OADataSource getDataSource(Class c) {
	    return OADataSource.getDataSource(c);
	}
    
	// called by HubDelegate.updateMany2ManyLinks()
	protected static void updateMany2ManyLinks(OAObject masterObject, OAObject[] adds, OAObject[] removes, String propFromMaster) {
		OADataSource ds = OADataSource.getDataSource(masterObject.getClass());
		if (ds != null) ds.updateMany2ManyLinks(masterObject, adds, removes, propFromMaster);
	}

	// 20120612 remove m2m link table records when an object is deleted
    public static void removeMany2ManyLinks(Hub hub) {
        if (hub == null) return;
        Object objMaster = hub.getMasterObject();
        if (objMaster == null) return;
        if (!(objMaster instanceof OAObject)) return;
        if (!OAObject.class.isAssignableFrom(hub.getObjectClass())) {
            return;
        }
        OALinkInfo link = hub.datam.liDetailToMaster;
        if (link == null) return;
        if (!OAObjectInfoDelegate.isMany2Many(link)) return;
        
        String propFromMaster = OAObjectInfoDelegate.getReverseLinkInfo(link).getName();

        OAObject[] objs = HubAddRemoveDelegate.getRemovedObjects(hub);
        if (objs == null || objs.length == 0) return;
       
        OADataSource ds = OADataSource.getDataSource(objMaster.getClass());
        if (ds == null) return;
        
        ds.updateMany2ManyLinks((OAObject)objMaster, null, objs, propFromMaster);
    }

}
