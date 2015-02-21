package com.viaoa.remote.multiplexer.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteClientInterface {

    String ping(String msg);
    
    boolean isStarted();
    
}
