package com.viaoa.sync;

import java.util.logging.Level;

import com.viaoa.sync.model.oa.Company;
import com.viaoa.sync.model.oa.ServerRoot;
import com.viaoa.sync.remote.BroadcastInterface;
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
        server.createLookup("test", ti, TestInterface.class);
        ServerRoot root = ti.getServerRoot();
        Company company = new Company();
        root.getCompanies().add(company);
        
        BroadcastInterface bc = (BroadcastInterface) server.createBroadcast("broadcast", BroadcastInterface.class, "oasync", 250);
        bc.start();
        boolean bStarted = true;
        for (int i=0; ;i++) {
            bc.sendCompanyName(company.getName());
            Thread.sleep(100);
            if (i % 10 == 0) System.out.println(i+") company.name="+company.getName());
            if (i % 100 == 0 && i > 0) {
                if (bStarted) {
                    bc.stop();
                    System.out.println("Stopped, company.name="+company.getName());
                }
                else {
                    bc.start();
                    System.out.println("Started, company.name="+company.getName());
                }
                bStarted = !bStarted;
            }
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        // OALogUtil.consoleOnly(Level.FINEST, "com.viaoa");
        OALogUtil.disable();
        
        OASyncServerTest test = new OASyncServerTest();
        test.test();
        System.out.println("DONE");
        for (;;) Thread.sleep(10000);
    }
}
