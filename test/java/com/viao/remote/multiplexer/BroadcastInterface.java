package com.viao.remote.multiplexer;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;


@OARemoteInterface(asyncQueueName="test")
public interface BroadcastInterface {

    void memory(long amt);
}
