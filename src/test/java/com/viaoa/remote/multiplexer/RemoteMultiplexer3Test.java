package com.viaoa.remote.multiplexer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectKey;
import com.viaoa.sync.model.ClientInfo;
import com.viaoa.sync.remote.*;

import test.xice.tsac3.model.oa.Server;

public class RemoteMultiplexer3Test extends OAUnitTest {
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer; 

    private MultiplexerClient multiplexerClient;
    private RemoteMultiplexerClient remoteMultiplexerClient;

    public final int port = 1101;
    final String queueName = "que";
    final int queueSize = 500;
    
    RemoteSyncInterface remoteSyncImpl;
    RemoteSyncInterface remoteSync;

    private RemoteClientCallbackInterface clientCallbackOnServer;
    private RemoteClientCallbackInterface remoteCallback;
    
    private Server serverTest;
    
    @Before
    public void setup() throws Exception {
        // setup server
        multiplexerServer = new MultiplexerServer(port);        
        remoteMultiplexerServer = new RemoteMultiplexerServer(multiplexerServer);
        
        // create server side for C2S socket request
        remoteMultiplexerServer.createLookup("server", createRemoteServerInterface("server"), RemoteServerInterface.class);

        // create server side for C2S queue request 
        remoteMultiplexerServer.createLookup("serverQ", createRemoteServerInterface("serverQ"), RemoteServerInterface.class, queueName, queueSize);

        // create Broadcast 
        remoteSyncImpl = new RemoteSyncImpl();
        remoteSync = (RemoteSyncInterface) remoteMultiplexerServer.createBroadcast("oasync", remoteSyncImpl, RemoteSyncInterface.class, queueName, queueSize);
        
        multiplexerServer.start();
        remoteMultiplexerServer.start();

        // setup client
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient);
        multiplexerClient.start();
        
        // create sample object on server
        serverTest = new Server();
        serverTest.setId(1);
    }

    @After
    public void tearDown() throws Exception {
        if (multiplexerClient != null) multiplexerClient.close();
        if (multiplexerServer != null) multiplexerServer.stop();
    }
    
    private volatile int serverPingCount, serverPingCount2;
    private RemoteServerInterface createRemoteServerInterface(final String name) {
        RemoteServerInterface rsi = new RemoteServerImpl() {
            public String ping(String msg) {
                String s = ++serverPingCount+" server ping, remote name="+name+", msg="+msg;
                //System.out.println(s);
                return s;
            }
            @Override
            public void ping2(String msg) {
                String s = ++serverPingCount2+" server ping2, remote name="+name+", msg="+msg;
                //System.out.println(s);
            }
            @Override
            public String getDisplayMessage() {
                return null;
            }
            @Override
            public void refresh(Class clazz) {
            }
            @Override
            public RemoteClientInterface getRemoteClient(ClientInfo clientInfo) {
                return null;
            }
            @Override
            public RemoteSessionInterface getRemoteSession(ClientInfo clientInfo, RemoteClientCallbackInterface callback) {
                RemoteMultiplexer3Test.this.clientCallbackOnServer = callback;
                RemoteSessionInterface rsi = new RemoteSessionImpl(1) {
                    @Override
                    public void sendException(String msg, Throwable ex) {
                    }
                    @Override
                    public void removeGuids(int[] guids) {
                    }
                    @Override
                    public boolean isLockedByAnotherClient(Class objectClass, OAObjectKey objectKey) {
                        return false;
                    }
                    @Override
                    public boolean isLocked(Class objectClass, OAObjectKey objectKey) {
                        return false;
                    }
                };
                return rsi;
            }
            @Override
            public OAObject getUnique(Class<? extends OAObject> clazz, String propertyName, Object uniqueKey, boolean bAutoCreate) {
// TODO Auto-generated method stub  qqqqqqqqqqqqqq
                return null;
            }
        };
        return rsi;
    }
    
    int clientPingCount;
    public RemoteClientCallbackInterface getRemoteClientCallback() {
        if (remoteCallback == null) {
            remoteCallback = new RemoteClientCallbackInterface() {
                @Override
                public void stop(String title, String msg) {
                    //qqq
                }
                @Override
                public String ping(String msg) {
                    clientPingCount++;
                    //System.out.println(clientPingCount+" client callback ping");
                    return "client recvd "+msg;
                }
                @Override
                public String performThreadDump(String msg) {
                    return null;
                }
            };
        }
        return remoteCallback;
    }

    
    @Test(timeout=5000)
    public void test() throws Exception {
        // C2S using socket request/reply
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        serverPingCount = 0;
        for (int i=0; i<100; i++) remoteServer.ping("test "+i);
        assertEquals(serverPingCount, 100);

        // C2S using socket request/no reply
        serverPingCount2 = 0;
        for (int i=0; i<100; i++) {
            String s = "test2 "+i;
            remoteServer.ping2(s);  // async call
        }
        if (serverPingCount2 == 0) Thread.sleep(25);
        assertTrue("serverPingCount2="+serverPingCount2, serverPingCount2 > 0);
        
        // C2S using queued request/reply
        RemoteServerInterface remoteServerQ = (RemoteServerInterface) remoteMultiplexerClient.lookup("serverQ");
        for (int i=0; i<100; i++) {
            remoteServerQ.ping("test");
        }
        // C2S using queued request/no reply
        for (int i=0; i<100; i++) {
            remoteServerQ.ping2("test2");
        }

        // S2C using socket request/reply
        RemoteSessionInterface remoteSession = remoteServer.getRemoteSession(new ClientInfo(), getRemoteClientCallback());
        assertNotNull(this.clientCallbackOnServer);  // make sure that server recvd it

        // test callback, by calling using the server side instance
        clientPingCount = 0;
        for (int i=0; i<100; i++) clientCallbackOnServer.ping("callback.ping."+i);
        assertEquals(clientPingCount, 100);

        remoteSession.ping("test");
        remoteSession.ping2("test2");
        
        // S2C broadcast
        RemoteSyncImpl remoteSyncImpl = new RemoteSyncImpl() {
            @Override
            public boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob) {
                return super.propertyChange(objectClass, origKey, propertyName, newValue, bIsBlob);
            }
        };
        RemoteSyncInterface remoteSync = (RemoteSyncInterface) remoteMultiplexerClient.lookupBroadcast("oasync", remoteSyncImpl);
        
        Server server = (Server) remoteServerQ.getObject(Server.class, new OAObjectKey(1));
        
        // C2S broadcast
        int xx = 4;
        for (int i=0; i<100; i++) {
            remoteSync.propertyChange(Server.class, server.getObjectKey(), Server.P_Name, "new name."+i, false);
            xx++;
            assertEquals(server.getName(), "new name."+i);
        }
        xx++;
        
    }
}
