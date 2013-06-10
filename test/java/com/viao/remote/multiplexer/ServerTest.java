package com.viao.remote.multiplexer;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;

public class ServerTest {
    public void test() throws Exception {
        MultiplexerServer ms = new MultiplexerServer(1099);
        ms.start();
        RemoteMultiplexerServer rms = new RemoteMultiplexerServer(ms);
        rms.start();
        
        RemoteTestImpl remoteTest = new RemoteTestImpl();
        rms.bind("test", remoteTest, RemoteTestInterface.class);
        
        rms.createClientBroadcast("clientBroadcast", RemoteTestInterface.class);
    }
    
    public static void main(String[] args) throws Exception {
        ServerTest test = new ServerTest();
        test.test();
        System.out.println("Server has been started");
        for (;;) Thread.sleep(10000);
    }
}
