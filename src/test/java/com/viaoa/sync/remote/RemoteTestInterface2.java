// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import com.viaoa.remote.multiplexer.annotation.*;
import test.theice.tsam.model.oa.Server;
import test.theice.tsam.model.oa.cs.ServerRoot;

@OARemoteInterface
public interface RemoteTestInterface2 {

    public final static String BindName = "RemoteTest2";

    public ServerRoot getServerRoot();
}
