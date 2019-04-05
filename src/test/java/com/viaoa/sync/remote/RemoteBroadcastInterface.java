// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import test.xice.tsam.model.oa.Server;
import test.xice.tsam.model.oa.Site;
import com.viaoa.remote.multiplexer.annotation.*;

import test.xice.tsam.model.oa.*;

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
    public void respondStats(Server server, String name, int cntApps, long nameChecksum);
    public void respondStats(String msg);
    
    
    
}
