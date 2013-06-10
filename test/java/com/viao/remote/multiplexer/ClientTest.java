package com.viao.remote.multiplexer;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;


public class ClientTest {
    RemoteTestInterface remoteTest;
    RemoteTestInterface clientBroadcast;
    RemoteTestInterface clientBroadcastCallback;
    
    public void test() throws Exception {
        MultiplexerClient ms = new MultiplexerClient("localhost", 1099);
        ms.start();
        RemoteMultiplexerClient rmc = new RemoteMultiplexerClient(ms);
        
//qqqqqqqqqqqqqqqqq        
        BroadcastInterface broadcast = new BroadcastImpl();
        
//        rmc.broadcast(broadcast);
        
        
        remoteTest = (RemoteTestInterface) rmc.lookup("test");
        
        clientBroadcastCallback = new RemoteTestImpl() {
            @Override
            public String ping(String msg) {
                System.out.println("ping on Client "+msg);
                return "xx";
            }
        };
        
        clientBroadcast = (RemoteTestInterface) rmc.createClientBroadcastProxy("clientBroadcast", clientBroadcastCallback);
        

        for (int i=0; i<5; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    test1(id);  // 2 stops 1
                    //test1(id);
                    //test2(id);
                }
            });
            t.start();
        }
    }

    
    void test1(int id) {
        long msLast = System.currentTimeMillis();
        long iLast = 0;
        for (int i=0; ;i++) {
            long ms = System.currentTimeMillis();
            String s;
            boolean b;
            if (msLast + 1000 < ms) {
                s = "";
                // s = (new OADateTime(ms).toString("HHmmss.SSS"));
                s += ", amt="+(i-iLast);
                msLast = ms;
                iLast = i;
                b = true;
                // s = "ping."+id+"."+i+" "+s;
            }
            else {
                s = "xx";
                b = false;
            }

            try {
                String msg = remoteTest.ping(s);
//                if (b) System.out.println("ping "+msg+", thread="+id);
System.out.println("");                
//                Thread.sleep(250);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("ClientTest exception: "+e);
            }
        }
    }

    void test2(int id) {
        long msLast = System.currentTimeMillis();
        long iLast = 0;
        for (int i=0; ; i++) {
            clientBroadcast.ping(i+" yoo, id="+id);
            long ms = System.currentTimeMillis();
            if (msLast + 1000 < ms) {
                System.out.println(i+" amt/second="+(i-iLast));
                msLast = ms;
                iLast = i;
            }
            try {
                Thread.sleep(200);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
    }    
    
    public static void main(String[] args) throws Exception {
        ClientTest test = new ClientTest();
        test.test();
    }
}
