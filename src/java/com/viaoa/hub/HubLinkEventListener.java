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

import com.viaoa.object.*;

/** 
 	Used/created by HubLinkDelegate to "track" the Linked "To" Hub, so that the AO for the Linked "From" hub can
    changed to match the AO in the Link "To" Hub.
	@see Hub#setLink(Hub,String) Full Description of Linking Hubs
*/
class HubLinkEventListener extends HubListenerAdapter implements java.io.Serializable {
	Hub linkToHub;
	Hub fromHub;
	
	public HubLinkEventListener(Hub fromHub, Hub linkToHub) {
	    this.fromHub = fromHub;
	    this.linkToHub = linkToHub;  // hub that is linked to, that this HubListener is listening to.
	}
	
	public @Override void afterChangeActiveObject(HubEvent hubEvent) {
		HubLinkDelegate.updateLinkedToHub(fromHub, linkToHub, hubEvent.getObject());
	}
	
	public @Override void afterPropertyChange(HubEvent hubEvent) {
	    if (hubEvent.getObject() == linkToHub.getActiveObject()) {
	    	String prop = hubEvent.getPropertyName(); 
            if (prop != null && prop.equalsIgnoreCase(fromHub.datau.linkToPropertyName)) {
            	HubLinkDelegate.updateLinkedToHub(fromHub, linkToHub, hubEvent.getObject(), prop);
            }
	    }
	}
}

