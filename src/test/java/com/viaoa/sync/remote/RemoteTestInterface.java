// Copied from OATemplate project by OABuilder 12/13/15 02:58 PM
package com.viaoa.sync.remote;

import com.viaoa.remote.multiplexer.annotation.*;
import test.xice.tsam.model.oa.Server;

@OARemoteInterface
public interface RemoteTestInterface {

    public final static String BindName = "RemoteTest";

    public String getName(Server server);
}
