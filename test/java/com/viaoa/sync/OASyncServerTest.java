package com.viaoa.sync;

import java.util.logging.Level;

import com.viaoa.util.OALogUtil;

public class OASyncServerTest {

    OASyncServer server;
    
    public void test() throws Exception {
        server = new OASyncServer(1099) {
            @Override
            protected String getLogFileName() {
                // TODO Auto-generated method stub
                return super.getLogFileName();
            }
        };
        server.start();
    }
    
    
    public static void main(String[] args) throws Exception {
        OALogUtil.consoleOnly(Level.FINEST, "com.viaoa");
        
        OASyncServerTest test = new OASyncServerTest();
        test.test();
        System.out.println("DONE");
        for (;;) Thread.sleep(10000);
    }
}
