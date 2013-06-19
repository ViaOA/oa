package com.viaoa.sync;

import java.util.logging.Level;

import com.viaoa.sync.remote.TestImpl;
import com.viaoa.sync.remote.TestInterface;
import com.viaoa.util.OALogUtil;

public class OASyncServerTest {

    OASyncServer server;
    
    public void test() throws Exception {
        server = new OASyncServer(1099) {
            @Override
            protected String getLogFileName() {
                return super.getLogFileName();
            }
        };
        server.start();
        
        TestImpl ti = new TestImpl();
        server.getRemoteMultiplexerServer().createLookup("test", ti, TestInterface.class);
    }
    
    
    public static void main(String[] args) throws Exception {
        OALogUtil.consoleOnly(Level.FINEST, "com.viaoa");
        
        OASyncServerTest test = new OASyncServerTest();
        test.test();
        System.out.println("DONE");
        for (;;) Thread.sleep(10000);
    }
}
