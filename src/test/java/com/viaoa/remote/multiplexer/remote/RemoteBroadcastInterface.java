package com.viaoa.remote.multiplexer.remote;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

@OARemoteInterface()
public interface RemoteBroadcastInterface {

    void start();
    void stop();
    void close();
    
}
