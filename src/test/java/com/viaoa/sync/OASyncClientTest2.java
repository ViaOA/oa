package com.viaoa.sync;

import java.util.logging.Level;
import test.xice.tsam.model.oa.cs.ServerRoot;
import test.xice.tsam.model.oa.*;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAObject;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.sync.remote.RemoteTestInterface2;
import com.viaoa.util.OALogUtil;

/**
 */
public class OASyncClientTest2 extends OAUnitTest {
    private static int port = 1102;
    private static ServerRoot serverRoot;    
    private static OASyncClient syncClient;

    
    public void start() throws Exception {
        syncClient = new OASyncClient("localhost", port) {
            @Override
            public void onStopCalled(String title, String msg) {
                super.onStopCalled(title, msg);
            }
            int cnt=0;
            @Override
            protected void afterInvokeRemoteMethod(RequestInfo ri) {
 //               super.afterInvokeRemoteMethod(ri);
 System.out.println((++cnt)+") "+ri.toLogString());
            }
            @Override
            protected void logRequest(RequestInfo ri) {
                System.out.println((++cnt)+") "+ri.toLogString());
            }
        };
        
        syncClient.start();
            
        RemoteTestInterface2 rti = (RemoteTestInterface2) syncClient.lookup(RemoteTestInterface2.BindName);

        serverRoot = rti.getServerRoot();
        serverRoot.getAdminUsers();
        
        Server server = new Server();
        int x = server.getId();
        int xx = 4;
        xx++;
    }

    
    public static void main(String[] args) throws Exception {
        OAObject.setDebugMode(true);
        OALogUtil.consoleOnly(Level.CONFIG);

        OASyncClientTest2 test = new OASyncClientTest2();
        test.start();
        
        
        System.out.println("DONE running test, exiting program");
    }
}
