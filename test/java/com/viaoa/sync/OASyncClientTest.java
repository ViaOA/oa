package com.viaoa.sync;

import java.util.logging.Level;

import com.viaoa.hub.Hub;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.model.oa.Company;
import com.viaoa.sync.model.oa.ServerRoot;
import com.viaoa.sync.model.oa.User;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.TestInterface;
import com.viaoa.util.OALogUtil;

public class OASyncClientTest {

    OASyncClient client;
    RemoteServerInterface remoteServer;
    RemoteClientInterface remoteClient;
    
    public void test() throws Exception {
        client = new OASyncClient("localhost", 1099) {
        };
        client.start();

        ClientInfo ci = client.getClientInfo();
        remoteServer = client.getRemoteServerInterface();
        remoteClient = client.getRemoteClientInterface();

        
        
        TestInterface ti = (TestInterface) client.getRemoteMultiplexerClient().lookup("test");
        ServerRoot serverRoot = ti.getServerRoot();
        Hub<Company> hub = serverRoot.getCompanies();

        Company comp = hub.getAt(0);
        if (comp == null) {
            comp = new Company();
            hub.add(comp);
        }
        for (int i=0; i<5; i++) {
            Thread.sleep(1250);
            comp.setName(""+i);
if (i % 500 == 0) System.out.println(""+i);            
        }
        
        
/*        
        User user = new User();
        user.setFirstName("test");
        comp.getUsers().add(user);

        long ts1 = System.currentTimeMillis();
        for (int i=0; i<5000 ;i++) {
            remoteServer.ping("hey");
        }
        long ts2 = System.currentTimeMillis();
        System.out.println("total time is="+ (ts2-ts1) );
        
        boolean b = remoteClient.isLockedByAnotherClient(null, null);
        System.out.println("b="+b);
*/        
    }
    
    
    public static void main(String[] args) throws Exception {
        //OALogUtil.consoleOnly(Level.FINEST, "com.viaoa");
        OALogUtil.disable();
        
        OASyncClientTest test = new OASyncClientTest();
        test.test();
        System.out.println("DONE");
        for (;;) Thread.sleep(10000);
    }
}
