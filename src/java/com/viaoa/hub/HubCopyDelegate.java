package com.viaoa.hub;

public class HubCopyDelegate {

    /**
     * Used to find a HubCopy that thisHub is based on.
     */
    public static HubCopy getHubCopy(Hub thisHub) {
        Hub h = HubShareDelegate.getMainSharedHub(thisHub);
        if (h.datam.masterObject != null || h.datam.masterHub != null) {
            // copied hubs will not have a master
            return null;
        }
        
        // find a HubCopy in the listener list
        HubListener[] hls = HubEventDelegate.getHubListeners(h);
        for (HubListener hl : hls) {
            if (hl instanceof HubCopy) {
                return (HubCopy) hl;
            }
        }
        return null;
    }
}
