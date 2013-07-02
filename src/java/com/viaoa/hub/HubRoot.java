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

import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.util.OAString;


/**
 * Used for recursive Hubs, so that the hub is always using the rootHub.
 * The default behaviour when using a recursive Hub is that the hub will be shared to whatever hub the AO is set to.
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
     * @param hub recursive hub
     * @param hubRoot Hub to use as the root, it will auto populated using hub.
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

        hubMaster = hub.getMasterHub();  // master hub of root hub - this is the 'source' to listen to.
        if (hubMaster == null) {
            // 20121107 
            hubCopy = new HubCopy(hubRoot, hub, true);
            // was: hubRoot.setSharedHub(hub, true);
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
            hubMaster.addHubListener(hubListener);
        }
    }
    
}
