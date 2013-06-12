package com.viao.remote.multiplexer;

import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;


@OARemoteInterface()
public interface BroadcastInterface {

    void memory(long amt);
}
