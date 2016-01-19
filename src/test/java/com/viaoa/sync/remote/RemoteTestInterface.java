// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import java.util.ArrayList;

import com.theice.tsam.model.oa.Server;
import com.viaoa.remote.multiplexer.annotation.*;
import com.viaoa.util.OAProperties;

@OARemoteInterface
public interface RemoteTestInterface {

    public final static String BindName = "RemoteTest";

    public String getName(Server server);
    
    
    
}
