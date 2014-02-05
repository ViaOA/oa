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

/**
 * Used to have two hubs use the same objects, so that the ordering can be different.
 */
public class HubCopy<TYPE> extends HubFilter {

	public HubCopy(Hub<TYPE> hubMaster, Hub<TYPE> hubCopy, boolean bShareAO) {
	    super(hubMaster, hubCopy, bShareAO);
	}

	// if object is directly removed from filtered hub, then remove from hubMaster
	@Override
	protected void afterRemoveFromFilteredHub(Object obj) {
	    hubMaster.remove(obj);
	}
	
	@Override
	public boolean isUsed(Object object) {
	    return true;
	}
}
