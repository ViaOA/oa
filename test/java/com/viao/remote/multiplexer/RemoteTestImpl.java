package com.viao.remote.multiplexer;

public class RemoteTestImpl implements RemoteTestInterface {
    int cnt;
    @Override
    public String ping(String msg) {
        if (cnt++ % 25000 == 0) System.out.println("ping called, msg="+msg);
        return msg;
    }

}
