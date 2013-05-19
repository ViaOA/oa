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

