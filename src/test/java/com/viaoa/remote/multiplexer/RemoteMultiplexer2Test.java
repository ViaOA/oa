package com.viaoa.remote.multiplexer;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.remote.*;  // test package only
import com.viaoa.util.OAString;

public class RemoteMultiplexer2Test extends OAUnitTest {
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer; 

    private MultiplexerClient multiplexerClient;
    private RemoteMultiplexerClient remoteMultiplexerClient;

    public final int port = 1099;
    final String queueName = "que";
    final int queueSize = 500;
    
    private RemoteBroadcastInterface remoteBroadcast;
    TestClient[] testClients = new TestClient[1];
    private boolean bServerStarted;

    
    @Before
    public void setup() throws Exception {
        // setup server
        multiplexerServer = new MultiplexerServer(port);        
        remoteMultiplexerServer = new RemoteMultiplexerServer(multiplexerServer);
        
        // with queue
        remoteMultiplexerServer.createLookup("server", createRemoteServerInterface("server"), RemoteServerInterface.class, queueName, queueSize);

        // without queue
        remoteMultiplexerServer.createLookup("serverNoQ", createRemoteServerInterface("serverNoQ"), RemoteServerInterface.class);
        
        RemoteBroadcastInterface rb = new RemoteBroadcastInterface() {
            @Override
            public void stop() {
                bServerStarted = false;
            }
            @Override
            public void start() {
                bServerStarted = true;
            }
        };
        remoteBroadcast = (RemoteBroadcastInterface) remoteMultiplexerServer.createBroadcast("broadcast", rb, RemoteBroadcastInterface.class, queueName, queueSize);
        
        
        multiplexerServer.start();
        remoteMultiplexerServer.start();

        // setup client
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient);
        multiplexerClient.start();
        
    }

    @After
    public void tearDown() throws Exception {
        multiplexerClient.close();
        multiplexerServer.stop();
    }
    
    
    private RemoteServerInterface createRemoteServerInterface(final String name) {
        RemoteServerInterface rsi = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
                RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                testClients[id].remoteClientInterface = rci;
                System.out.println("");
            }
            @Override
            public boolean isStarted() {
                return bServerStarted;
            }
        };
        return rsi;
    }
    
    @Test
    //(timeout=15000)
    public void test() throws Exception {
        
        for (int i=0; i<testClients.length; i++) {
            final TestClient client = new TestClient(i);
            testClients[i] = client;
            
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        client.run();
                    }
                    catch (Exception e) {
                        System.out.println("Client thread exception, ex="+e);
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        
        Thread.sleep(100);

        for (TestClient tc : testClients) {
            boolean b = tc.remoteClientInterface.isStarted();
            assertFalse(b);
        }
        
        
        remoteBroadcast.start();
        for (TestClient tc : testClients) {
            boolean b = tc.remoteClientInterface.isStarted();
            assertTrue(b);
        }
        
        for (int i=0; i<100; i++) {
            for (TestClient tc : testClients) {
                String s = OAString.getRandomString(3, 22);
                assertEquals(tc.remoteClientInterface.ping(s), tc.id+s);
                Thread.sleep(100);
            }
        }
        
        remoteBroadcast.stop();
        for (TestClient tc : testClients) {
            assertFalse(tc.remoteClientInterface.isStarted());
        }
        
        for (int i=0; i<1000; i++)  {
            Thread.sleep(10000);
        }
        int xx = 4;
        xx++;
    }
    
    class TestClient {
        int id;
        volatile boolean bStarted;
        final Object lock = new Object();
        RemoteClientInterface remoteClientInterface;
        
        public TestClient(final int id) {
            this.id = id;
        }
        public void run() throws Exception {
            RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
    
            RemoteBroadcastInterface remoteBroadcast = new RemoteBroadcastInterface() {
                @Override
                public void stop() {
                    synchronized (lock) {
                        bStarted = false;
                        lock.notifyAll();
                    }
                }
                @Override
                public void start() {
                    synchronized (lock) {
                        bStarted = true;
                        lock.notifyAll();
                    }
                }
            };
            remoteMultiplexerClient.lookupBroadcast("broadcast", remoteBroadcast);
            
            RemoteClientInterface remoteClient = new RemoteClientInterface() {
                @Override
                public String ping(String msg) {
                    return id+msg;
                }
                @Override
                public boolean isStarted() {
                    return bStarted;
                }
            };
            remoteServer.register(this.id, remoteClient);
            
            for (;;) {
                synchronized (lock) {
                    if (!bStarted) {
                        System.out.println("Thread #"+id+" is waiting for start command");
                        lock.wait();
                    }
                }
                System.out.println("Thread #"+id+" is running, server is started="+remoteServer.isStarted());
                Thread.sleep(1000);
            }
        }
    }

}

