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
public class HubShareAO {
	private Hub hub1;
	private Hub hub2;
	private HubListener hl1, hl2;
	
	public HubShareAO(Hub hub1, Hub hub2) {
		this.hub1 = hub1;
        this.hub2 = hub2;
	
        hl1 = new HubListenerAdapter() {
			@Override
			public void afterChangeActiveObject(HubEvent evt) {
			    Object obj = HubShareAO.this.hub1.getAO();
			    HubShareAO.this.hub2.setAO(obj);
			}
		};
		hub1.addHubListener(hl1);

		hl2 = new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent evt) {
                Object obj = HubShareAO.this.hub2.getAO();
                HubShareAO.this.hub1.setAO(obj);
            }
        };
        hub2.addHubListener(hl1);
	}
	
	public void close() {
        if (hl1 != null) {
            hub1.removeHubListener(hl1);
            hl1 = null;
        }
        if (hl2 != null) {
            hub2.removeHubListener(hl2);
            hl2 = null;
        }
	}
}
