package com.viao.remote.multiplexer;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;

public class ServerTest {
    public void test() throws Exception {
        MultiplexerServer ms = new MultiplexerServer(1099);
        ms.start();
        final RemoteMultiplexerServer rms = new RemoteMultiplexerServer(ms);
        rms.start();
        
        RemoteTestImpl remoteTest = new RemoteTestImpl();
        rms.createLookup("test", remoteTest, RemoteTestInterface.class);
        
        RemoteTestImpl rti = new RemoteTestImpl();
        final RemoteTestInterface rtx = (RemoteTestInterface) rms.createClientBroadcast("clientBroadcast", rti, RemoteTestInterface.class);
        
        final BroadcastInterface bc = (BroadcastInterface) rms.createServerBroadcast("broadcast", BroadcastInterface.class);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i=0;; i++) {
                        rtx.ping("called by Server");                        
                        long usedMemeory = Runtime.getRuntime().totalMemory();
                        usedMemeory -= Runtime.getRuntime().freeMemory();
                        bc.memory(usedMemeory);
                        Thread.sleep(1000);
                        if (i % 30 == 0) {
                            rms.performDGC();
                        }
                    }
                }
                catch (Exception e) {
                }
            }
        });
//qqqq        t.start();
    }
    
    
    
    public static void main(String[] args) throws Exception {
        ServerTest test = new ServerTest();
        test.test();
        System.out.println("Server has been started");
        for (;;) Thread.sleep(10000);
    }
}
