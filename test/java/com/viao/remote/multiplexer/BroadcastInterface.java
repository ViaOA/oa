package com.viao.remote.multiplexer;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;


@OARemoteInterface(asyncQueueName="test", asyncQueueSize=250000)
public interface BroadcastInterface {

    void memory(long amt);
}
