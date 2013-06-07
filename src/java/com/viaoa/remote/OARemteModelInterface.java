package com.viaoa.remote;

import com.viaoa.cs.OAObjectMessage;
import com.viaoa.remote.multiplexer.annotation.OARemoteInterface;
import com.viaoa.remote.multiplexer.annotation.OARemoteMethod;

@OARemoteInterface
public interface OARemteModelInterface {

    @OARemoteMethod(noReturnValue=true)
    void sendMessage(OAObjectMessage objMsg);

}
