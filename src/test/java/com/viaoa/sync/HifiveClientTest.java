package com.viaoa.sync;


import static org.junit.Assert.*;

import java.util.logging.Level;
import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.ds.cs.OADataSourceClient;
import com.viaoa.hub.Hub;
import com.viaoa.util.OALogUtil;

import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.Program;
import test.hifive.model.oa.cs.ServerRoot;

public class HifiveClientTest extends OAUnitTest {
    private static int port = 1103;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;
    
    public void test() throws Exception {
        MultiplexerClient.DEBUG = true;
        syncClient = new OASyncClient("localhost", port);
        
        // **NOTE** need to make sure that HifiveServerTest is running in another jvm
        syncClient.start();
            
        
        OADataSourceClient ds = new OADataSourceClient();
        serverRoot = (ServerRoot) ds.select(ServerRoot.class).next();
        
        Hub<Program> h = serverRoot.getActivePrograms();
        for (Program p : h) {
            int xx = 4;
            xx++;
            //p.getEmployees();
             p.getLocations();
        }
        
        ModelDelegate.initialize(serverRoot);
    }
    
    
    
    public static void main(String[] args) throws Exception {
        MultiplexerClient.DEBUG = true;
        OALogUtil.consoleOnly(Level.CONFIG);

        HifiveClientTest test = new HifiveClientTest();
        test.test();
        
        System.out.println("DONE running hifive test, exiting program");
    }
}
