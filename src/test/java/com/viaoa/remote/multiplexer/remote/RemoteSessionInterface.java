package com.viaoa.remote.multiplexer.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteSessionInterface {

    String ping(String msg);
    
    
}
