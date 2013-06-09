package com.viao.remote.multiplexer;

import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.util.OADateTime;

public class ClientTest {
    RemoteTestInterface rt;
    public void test() throws Exception {
        MultiplexerClient ms = new MultiplexerClient("localhost", 1099);
        ms.start();
        RemoteMultiplexerClient rms = new RemoteMultiplexerClient(ms);
        
        rt = (RemoteTestInterface) rms.lookup("test");

        for (int i=0; i<5; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    testx(id);
                }
            });
            t.start();
        }
    }

    void testx(int id) {
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
            String msg = rt.ping(s);
            if (b) System.out.println("ping "+msg+", thread="+id);
        }
    }
    
    public static void main(String[] args) throws Exception {
        ClientTest test = new ClientTest();
        test.test();
    }
}
