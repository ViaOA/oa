package com.viao.remote.multiplexer;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;

public class ClientTest {
    RemoteTestInterface remoteTest;
    RemoteTestInterface clientRemoteTest;
    RemoteTestInterface remoteTestCallback;
    
    public void test() throws Exception {
        MultiplexerClient ms = new MultiplexerClient("localhost", 1099);
        ms.start();
        RemoteMultiplexerClient rms = new RemoteMultiplexerClient(ms);
        
        remoteTest = (RemoteTestInterface) rms.lookup("test");
        
        remoteTestCallback = new RemoteTestImpl() {
            @Override
            public String ping(String msg) {
                System.out.println("ping on Client "+msg);
                return "xx";
            }
        };
        
        clientRemoteTest = (RemoteTestInterface) rms.createClientBroadcastProxy("clientBroadcast", remoteTestCallback);
        

        for (int i=0; i<1; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    test2(id);  // 2 stops 1
                    //test1(id);
                    //test2(id);
                }
            });
            t.start();
        }
    }

    void test2(int id) {
        long msLast = System.currentTimeMillis();
        long iLast = 0;
        for (int i=0; i<1;i++) {
            clientRemoteTest.ping(i+" yoo, id="+id);
            long ms = System.currentTimeMillis();
            if (msLast + 1000 < ms) {
                System.out.println(i+" amt/second="+(i-iLast));
                msLast = ms;
                iLast = i;
            }
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
System.exit(0);
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
                Thread.sleep(8500);
            }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("ClientTest exception: "+e);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        ClientTest test = new ClientTest();
        test.test();
    }
}
