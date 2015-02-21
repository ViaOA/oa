package com.viaoa.remote.multiplexer.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteServerInterface {

    void register(int id, RemoteClientInterface rci);
    
    boolean isStarted();
    
}
