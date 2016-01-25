// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import java.util.ArrayList;

import com.viaoa.remote.multiplexer.annotation.*;
import com.viaoa.util.OAProperties;

import test.theice.tsam.model.oa.Server;

@OARemoteInterface
public interface RemoteTestInterface {

    public final static String BindName = "RemoteTest";

    public String getName(Server server);
    
    
    
}
