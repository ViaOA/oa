// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import java.util.ArrayList;
import com.theice.tsam.model.oa.*;
import com.viaoa.remote.multiplexer.annotation.*;
import com.viaoa.util.OAProperties;

@OARemoteInterface
public interface RemoteBroadcastInterface {

    public final static String BindName = "RemoteBroadcast";

    public void sendName(Site site, String name);
    public void sendName(Server server, String name);
    public void sendAppCount(Server server, int cnt);
    public void startTest();
    public void stopTest();
    public void sendResults();
    public void onClientStart();
    public void onClientDone();
    
}
