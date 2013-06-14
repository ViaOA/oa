package com.viaoa.remote.multiplexer;

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
        
        RemoteTestImpl remoteTestQueue = new RemoteTestImpl();
        rms.createLookup("testQueue", remoteTestQueue, RemoteTestInterface.class, "test", 25000);

        RemoteTestImpl rti = new RemoteTestImpl();
        final RemoteTestInterface rtx = (RemoteTestInterface) rms.createBroadcast("clientBroadcast", rti, RemoteTestInterface.class, "test", 2500);
        
        
        final BroadcastInterface bc = (BroadcastInterface) rms.createBroadcast("broadcast", null, BroadcastInterface.class, "test", 2500);
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
//                            rms.performDGC();
                        }
                    }
                }
                catch (Exception e) {
                }
            }
        });
        t.start();
    }
    
    
    
    public static void main(String[] args) throws Exception {
        ServerTest test = new ServerTest();
        test.test();
        System.out.println("Server has been started");
        for (;;) Thread.sleep(10000);
    }
}
