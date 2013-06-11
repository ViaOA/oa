package com.viaoa.remote.multiplexer;

import com.viaoa.remote.multiplexer.info.RequestInfo;

public class RemoteClientThread extends Thread {
    
    public final Object Lock = new Object();
    
    public volatile RequestInfo ri;
    
    public void startNextMessage() {
        
    }

    
}
