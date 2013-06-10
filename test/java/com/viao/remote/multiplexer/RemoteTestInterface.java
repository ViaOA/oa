package com.viao.remote.multiplexer;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;


@OARemoteInterface(asyncQueueName="test", asyncQueueSize=2500)
public interface RemoteTestInterface {

    String ping(String msg); 
}
