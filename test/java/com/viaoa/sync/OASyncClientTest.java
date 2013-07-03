package com.viaoa.sync;

import java.util.logging.Level;

import com.viaoa.hub.Hub;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.model.oa.Company;
import com.viaoa.sync.model.oa.ServerRoot;
import com.viaoa.sync.remote.BroadcastImpl;
import com.viaoa.sync.remote.RemoteClientInterface;
import com.viaoa.sync.remote.RemoteServerInterface;
import com.viaoa.sync.remote.TestInterface;
import com.viaoa.util.OALogUtil;
import com.viaoa.util.OAString;

public class OASyncClientTest {

    OASyncClient client;
    RemoteServerInterface remoteServer;
    RemoteClientInterface remoteClient;
    volatile boolean started=true;
    TestInterface testInterface;
    ClientInfo clientInfo;
    ServerRoot serverRoot;
    Hub<Company> hubCompany;
    int cId;
    BroadcastImpl bc;

    public void connect() throws Exception {
        if (client != null) return;
        client = new OASyncClient("localhost", 1099);
        client.start();
        clientInfo = client.getClientInfo();
        remoteServer = client.getRemoteServerInterface();
        remoteClient = client.getRemoteClientInterface();
        testInterface = (TestInterface) client.lookup("test");
        serverRoot = testInterface.getServerRoot();
        hubCompany = serverRoot.getCompanies();
        cId = client.getConnectionId();
        
        bc = new BroadcastImpl(hubCompany) {
            @Override
            public void start() {
                started = true;
            }
            @Override
            public void stop() {
                started = false;
            }
        };
        client.getRemoteMultiplexerClient().lookupBroadcast("broadcast", bc);
        
    }
    public void test() throws Exception {
        connect();
        for (int i=0; i<2; i++) {
            Thread t = new Thread(new Runnable() {
                public void run() {
                    try {
                        test1();
                    }
                    catch (Exception e) {
                        System.err.println("error in test1, ex="+e);
                        e.printStackTrace();
                    }
                }
            }, "TestThread"+i);
            t.start();
        }
    }
    
    public void test1() throws Exception {
        for (int i=0; ; i++) {
            String msg = cId + "." + i + "." + OAString.getRandomString(3, 22);
            Company company = hubCompany.getAt(0);
            
            int x = hubCompany.getSize();
            if (x < 2 || (x < 30 && Math.random() < .5d)) {
                company = new Company();
                hubCompany.add(company);
            }
            else {
                hubCompany.remove(company);
            }
            
            company.setName(msg);
            if (i % 1500 == 0) System.out.println(""+msg);
            //Thread.sleep(10);
        }
    }
    
    public void test2() throws Exception {
        client = new OASyncClient("localhost", 1099) {
        };
        client.start();

        ClientInfo ci = client.getClientInfo();
        remoteServer = client.getRemoteServerInterface();
        remoteClient = client.getRemoteClientInterface();
        
        TestInterface ti = (TestInterface) client.lookup("test");
        ServerRoot serverRoot = ti.getServerRoot();
        Hub<Company> hub = serverRoot.getCompanies();

        Company company = hub.getAt(0);
        if (company == null) {
            company = new Company();
            hub.add(company);
        }
        
        BroadcastImpl bc = new BroadcastImpl(hub) {
            @Override
            public void start() {
                started = true;
            }
            @Override
            public void stop() {
                started = false;
            }
        };
        client.getRemoteMultiplexerClient().lookupBroadcast("broadcast", bc);
        
        String prefix = OAString.getRandomString(5, 7);
        for (int i=0; ; i++) {
            if (!started) {
                System.out.println("Stopped, company.name="+company.getName());
                Thread.sleep(250);
            }
            else {
                company.setName(prefix+"."+i);
                if (i % 500 == 0) System.out.println(""+i);
            }
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
        OALogUtil.consoleOnly(Level.CONFIG, "com.viaoa");
        //OALogUtil.disable();
        
        OASyncClientTest test = new OASyncClientTest();
        test.test();
        System.out.println("started");
        for (;;) Thread.sleep(10000);
    }
}
