package com.viaoa.sync;

import java.util.logging.Level;

import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.util.OALogUtil;

public class OASyncClientTest {

    OASyncClient client;
    
    public void test() throws Exception {
        client = new OASyncClient("localhost", 1099) {
        };
        client.start();
        
        client.getClientInfo();
        RemoteServerInterface rs = client.getRemoteServerInterface();
        RemoteClientInterface rc = client.getRemoteClientInterface();
        
        boolean b = rc.isLockedByAnotherClient(null, null);
        System.out.println("b="+b);
        long ts1 = System.currentTimeMillis();
        for (int i=0; i<5000 ;i++) {
            rs.ping("hey");
        }
        long ts2 = System.currentTimeMillis();
        System.out.println("total time is="+ (ts2-ts1) );
    }
    
    
    public static void main(String[] args) throws Exception {
        // OALogUtil.consoleOnly(Level.FINEST, "com.viaoa");
        OALogUtil.disable();
        
        OASyncClientTest test = new OASyncClientTest();
        test.test();
        System.out.println("DONE");
        for (;;) Thread.sleep(10000);
    }
}
