// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import com.viaoa.remote.multiplexer.annotation.*;
import test.xice.tsam.model.oa.Server;
import test.xice.tsam.model.oa.cs.ServerRoot;

@OARemoteInterface
public interface RemoteTestInterface2 {

    public final static String BindName = "RemoteTest2";

    public ServerRoot getServerRoot();
}
