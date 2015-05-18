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
	boolean bUpdateWeakHub;
	
	public HubLinkEventListener(Hub fromHub, Hub linkToHub) {
	    this.fromHub = fromHub;
	    this.linkToHub = linkToHub;  // hub that is linked to, that this HubListener is listening to.
	    
	    // 20130708
        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(linkToHub);
        if (li != null && li.getPrivateMethod()) {
            if (OAObjectInfoDelegate.isMany2Many(li)) {
                bUpdateWeakHub = true;
            }
        }
	}
	
	public @Override void afterChangeActiveObject(HubEvent hubEvent) {
		HubLinkDelegate.updateLinkedToHub(fromHub, linkToHub, hubEvent.getObject());
	}
	
	public @Override void afterPropertyChange(HubEvent hubEvent) {
	    if (hubEvent.getObject() == linkToHub.getActiveObject()) {
	    	String prop = hubEvent.getPropertyName(); 
            if (prop != null && prop.equalsIgnoreCase(fromHub.datau.getLinkToPropertyName())) {
            	HubLinkDelegate.updateLinkedToHub(fromHub, linkToHub, hubEvent.getObject(), prop);
            }
	    }
	}
	
	// 20130708 check if linkToHub is based on a M2M&private, where the oaObj.weakRefs[] do not have the hub
	//     if so, then need to add it
	@Override
	public void onNewList(HubEvent e) {
	    if (!bUpdateWeakHub) return;
	    for (Object objx : linkToHub) {
	        OAObject oaObj = (OAObject) objx;
	        if (!OAObjectHubDelegate.addHub(oaObj, linkToHub, true)) {
	            break;
	        }
        }
	}
}

