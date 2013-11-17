package com.viaoa.hub;

public class HubCopyDelegate {

    
    public static HubCopy findHubCopy(Hub thisHub) {
        HubListener[] hls = HubEventDelegate.getAllListeners(thisHub);
        for (HubListener hl : hls) {
            if (hl instanceof HubCopy) {
                return (HubCopy) hl;
            }
        }
        return null;
    }
}
