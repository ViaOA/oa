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

import com.viaoa.object.OAObject;

/**
 * Creates a Hub using HubMerger
*/
public class MergedHub<TYPE> extends Hub<TYPE> {
    
    private HubMerger hm;

    public MergedHub(Class<TYPE> clazz, Hub hubMasterRoot, String propertyPath) {
        super(clazz);
        this.hm = new HubMerger(hubMasterRoot, this, propertyPath, false, null, true); 
    }

    public MergedHub(Class<TYPE> clazz, Hub hubMasterRoot, String propertyPath, boolean bUseAll) {
        super(clazz);
        this.hm = new HubMerger(hubMasterRoot, this, propertyPath, false, null, bUseAll); 
    }
    
    public MergedHub(Class<TYPE> clazz, Hub hubMasterRoot, String propertyPath, boolean bShareActiveObject, String selectOrder, boolean bUseAll) {
    	super(clazz);
    	this.hm = new HubMerger(hubMasterRoot, this, propertyPath, bShareActiveObject, selectOrder, bUseAll); 
    }

    public HubMerger getHubMerger() {
        return this.hm;
    }

    public MergedHub(Class<TYPE> clazz, OAObject obj, String propertyPath) {
        super(clazz);
        
        Hub hubMasterRoot = new Hub(obj.getClass());
        hubMasterRoot.add(obj);
        hubMasterRoot.setPos(0);
        
        this.hm = new HubMerger(hubMasterRoot, this, propertyPath, false, null, true);
    }

}

