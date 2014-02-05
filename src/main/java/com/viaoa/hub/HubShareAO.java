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
 * Used to have two hubs share the same AO.
 */
public class HubShareAO extends HubListenerAdapter {
	private Hub hub1;
	private Hub hub2;
	
	public HubShareAO(Hub hub1, Hub hub2) {
		this.hub1 = hub1;
        this.hub2 = hub2;

//qqqqqqqqqqqqq        
if (HubShareDelegate.isUsingSameSharedAO(hub1, hub2)) {
    int xx = 4;
    xx++;
}
        
		hub1.addHubListener(this);
        hub2.addHubListener(this);
	}

    @Override
    public void afterChangeActiveObject(HubEvent evt) {
        Hub h = evt.getHub();
        Object obj = h.getAO();
        if (h == hub1) hub2.setAO(obj);
        else hub1.setAO(obj);
    }
	
	public void close() {
        hub1.removeHubListener(this);
        hub2.removeHubListener(this);
	}
	
	public Hub getHub1() {
	    return hub1;
	}
    public Hub getHub2() {
        return hub2;
    }
}
