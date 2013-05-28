package com.viaoa.cs;

import com.viaoa.hub.HubListenerTree;
import com.viaoa.hub.HubMerger;
import com.viaoa.util.OADateTime;

public class OAClientInfoDelegate {

    
    public static void update(OAClientInfo ci) {
        if (ci == null) return;
        ci.lastClientUpdate = new OADateTime();
        ci.totalMemory = Runtime.getRuntime().totalMemory();
        ci.freeMemory = Runtime.getRuntime().freeMemory();
        ci.maxMemory = Runtime.getRuntime().maxMemory();

        ci.hubListenerCount = HubListenerTree.ListenerCount;
        ci.hubMergerHubListenerCount = HubMerger.HubListenerCount;
        
        OAClient client = OAClient.getClient();
        if (client != null) {
            ci.setId(client.getId());
            if (client.clientMessageHandler != null) {
                client.clientMessageHandler.updateClientInfo(ci);
            }
            if (client.clientMessageReader != null) {
                client.clientMessageReader.updateClientInfo(ci);
            }
        }
    }
    
    public static void update(OAClientInfo ci, OAObjectServerImpl os) {
        
    }
    
    
}
