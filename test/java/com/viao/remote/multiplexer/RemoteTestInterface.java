package com.viao.remote.multiplexer;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;

//qqqqqqqqqqq change size to 2500ish
@OARemoteInterface(asyncQueueName="test", asyncQueueSize=250000)
public interface RemoteTestInterface {

    String ping(String msg); 
}
