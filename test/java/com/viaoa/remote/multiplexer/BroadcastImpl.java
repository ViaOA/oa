package com.viaoa.remote.multiplexer;

public class BroadcastImpl implements BroadcastInterface {

    @Override
    public void memory(long amt) {
        System.out.println("server memory: "+amt);
    }

}
