// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import com.theice.tsam.model.oa.*;
import com.viaoa.remote.multiplexer.annotation.*;

@OARemoteInterface
public interface RemoteBroadcastInterface {

    public final static String BindName = "RemoteBroadcast";

    public void startTest();
    public void stopTest();
    public void sendStats();

    public void onClientTestStarted();
    public void onClientTestDone();
    public void onClientStatsSent();
    public void onClientDone();
    
    public void respondStats(Site site, String name);
    public void respondStats(Server server, String name, int cntApps);
    public void respondStats(String msg);
    
    
    
}
