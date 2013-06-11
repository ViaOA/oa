package com.viaoa.remote.multiplexer;

import com.viaoa.remote.multiplexer.info.RequestInfo;

public class RemoteClientThread extends Thread {
    
    final Object Lock = new Object();
    
    volatile RequestInfo ri;
    
    public void startNextMessage() {
        
    }

    
}
