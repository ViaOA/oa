package com.viao.remote.multiplexer;

public class RemoteTestImpl implements RemoteTestInterface {
    int cnt;
    @Override
    public String ping(String msg) {
        if (cnt++ % 25000 == 0) System.out.println(cnt+" ping called, msg="+msg);
        else System.out.println(cnt+" ping called, msg="+msg);
        return msg;
    }

}
