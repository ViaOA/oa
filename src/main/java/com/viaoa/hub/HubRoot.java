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

import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;

/**
 * Used for recursive Hubs, so that the hub is always using the rootHub.
 * The default behavior when using a recursive Hub is that the hub will be shared to 
 * whatever hub the AO is set to.  This will allow a hubRoot to always be the top level hub.

 * example:
    // get the original root
    this.hub = hubModel.getDetailHub(Model.P_Containers).createSharedHub();
    // create empty hub that will then always contain the root hub objects
    this.hubRoot = new Hub&lt;Container&gt;(Container.class);
    new HubRoot(hub, hubRoot);  // hubRoot will always have top level hub, initially using hub
    ...
    OATreeNode node = new OATreeNode(App.P_Label, hubRoot, hub) ... 
 * 
   
 * @author vvia
 * 20120302
 */
public class HubRoot {
    private Hub hubRoot;
    private Hub hubMaster;
    private HubCopy hubCopy;
    private String propertyFromMaster;
    private HubListener hubListener;
    
    /**
     * This is used for recursive hubs, so that a Hub will stay at the root.
     * By default, a shared hub that is recursive could change to be shared with a child hub.
     * This class is used to make sure that the hub does not change to share a child hub.
     * @param hubRoot Hub to use as the root, it will auto populated to always be the root hub.
     */
    public HubRoot(Hub hub, Hub hubRoot) {
        if (hub == null) return;
        if (hubRoot == null) return;

        this.hubRoot = hubRoot;
        
        Class clazz = hub.getObjectClass();
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(clazz);
        OALinkInfo li = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
        if (li == null) {
            hubRoot.setSharedHub(hub, true);
            return;
        }

        li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hub);
        if (li != null) li = OAObjectInfoDelegate.getReverseLinkInfo(li);
        if (li == null || !li.getRecursive()) {
            hubRoot.setSharedHub(hub, false);
            return;
        }
        
        hubMaster = hub.getMasterHub();  // master hub of root hub - this is the 'source' to listen to.
        if (hubMaster == null) {
            Hub h = hub;
            for (;;) {
                Hub hx = h.getSharedHub();
                if (hx == null) break;
                h = hx;
            }
            hubCopy = new HubCopy(h, hubRoot, false);
            return;
        }
        
        propertyFromMaster = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
        
        hubListener = new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                HubRoot.this.update();
            }
        };
        hubMaster.addHubListener(hubListener);
        
        update();
    }
    
    private void update() {
        if (hubCopy != null) {
            hubCopy.close();
            hubCopy = null;
        }
        this.hubRoot.clear();

        OAObject obj = (OAObject) hubMaster.getAO();
        if (obj == null) return;
        
        Hub h = (Hub) obj.getProperty(propertyFromMaster);
        hubCopy = new HubCopy(h, hubRoot, false);
    }
    
    public void close() {
        if (hubListener != null && hubMaster != null) {
            hubMaster.removeHubListener(hubListener);
        }
    }
    
}
