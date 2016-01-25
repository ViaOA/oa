// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import test.theice.tsam.model.oa.Server;
import test.theice.tsam.model.oa.Site;
import com.viaoa.remote.multiplexer.annotation.*;

import test.theice.tsam.model.oa.*;

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
