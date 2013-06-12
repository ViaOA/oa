package com.viaoa.remote.multiplexer;

import com.viaoa.remote.multiplexer.info.RequestInfo;

public class OARemoteThread extends Thread {
    
    final Object Lock = new Object();
    
    volatile RequestInfo ri;

    public OARemoteThread(Runnable r) {
        super(r);
    }
    public OARemoteThread() {
    }

    public void startNextMessage() {
    }
}
