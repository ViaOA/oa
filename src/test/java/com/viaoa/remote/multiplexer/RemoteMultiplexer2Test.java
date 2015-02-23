package com.viaoa.remote.multiplexer;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;
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

    public final int port = 1099;
    final String queueName = "que";
    final int queueSize = 2500;
    
    private RemoteBroadcastInterface remoteBroadcast;
    private RemoteBroadcastInterface remoteBroadcastProxy;
    TestClient[] testClients = new TestClient[50];
    private volatile boolean bServerStarted;
    private volatile boolean bServerClosed;
    private AtomicInteger aiClientRegisterCount = new AtomicInteger();
    private AtomicInteger aiClientRegisterCountNoQ = new AtomicInteger();

    final Object lockServer = new Object();
    final Object lockServerNoQ = new Object();
    
    
    @Before
    public void setup() throws Exception {
        System.out.println("Before, calling setup");
        // setup server
        multiplexerServer = new MultiplexerServer(port);        
        remoteMultiplexerServer = new RemoteMultiplexerServer(multiplexerServer);
    
        RemoteServerInterface remoteServer = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
                System.out.println("Server. registered called, client id="+id);                
                if (id < 0|| id >= testClients.length) return;
                RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                testClients[id].remoteClientInterface = rci;
                aiClientRegisterCount.incrementAndGet();
                synchronized (lockServer) {
                    lockServer.notify();
                }
            }
            @Override
            public boolean isStarted() {
                return bServerStarted;
            }
            @Override
            public boolean isRegister(int id) {
                if (id < 0|| id >= testClients.length) return false;
                return testClients[id].remoteClientInterface != null;
            }
        };
        // with queue
        remoteMultiplexerServer.createLookup("server", remoteServer, RemoteServerInterface.class, queueName, queueSize);

        // without queue
        RemoteServerInterface remoteServerNoQ = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
                if (id < 0|| id >= testClients.length) return;
                // System.out.println("*** REGISTERING NoQ, id="+id);                
                testClients[id].remoteClientInterfaceNoQ = rci;
                aiClientRegisterCountNoQ.incrementAndGet();
                synchronized (lockServerNoQ) {
                    lockServerNoQ.notify();
                }
            }
            @Override
            public boolean isStarted() {
                return bServerStarted;
            }
            @Override
            public boolean isRegister(int id) {
                if (id < 0|| id >= testClients.length) return false;
                return testClients[id].remoteClientInterfaceNoQ != null;
            }
        };
        remoteMultiplexerServer.createLookup("serverNoQ", remoteServerNoQ, RemoteServerInterface.class);
        
        
        remoteBroadcast = new RemoteBroadcastInterface() {
            @Override
            public void stop() {
                bServerStarted = false;
                remoteBroadcastProxy.stop();
            }
            @Override
            public void start() {
                bServerStarted = true;
                remoteBroadcastProxy.start();
            }
            @Override
            public void close() {
                bServerClosed = true;
                remoteBroadcastProxy.close();
            }
        };
        remoteBroadcastProxy = (RemoteBroadcastInterface) remoteMultiplexerServer.createBroadcast("broadcast", remoteBroadcast, RemoteBroadcastInterface.class, queueName, queueSize);
        
        multiplexerServer.start();
        remoteMultiplexerServer.start();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("After, calling tearDown");
        multiplexerServer.stop();
    }

    
    
    @Test
    //(timeout=40000)
    public void test() throws Exception {
        
        System.out.println("creating "+testClients.length+" clients");
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
            t.setName("TestClient."+i);
            t.start();
//Thread.sleep(500);//qqqqqq            
        }
        System.out.println(testClients.length+" clients created, verifing that all have registered with server");

        for (int i=0 ; i < 5 && aiClientRegisterCount.get() != testClients.length; i++) {
            synchronized (lockServer) {
                System.out.println("   server is waiting for all clients to call register(..), total regsitered="+aiClientRegisterCount.get());
                lockServer.wait(1000);
            }
            Thread.sleep(1000);
        }
        assertEquals(aiClientRegisterCount.get(), testClients.length);

/*        
        for (int i=0 ; i<5 && aiClientRegisterCountNoQ.get() != testClients.length; i++) {
            synchronized (lockServerNoQ) {
                System.out.println("   serverNoQ is waiting for all clients to call register(..), total regsitered="+aiClientRegisterCountNoQ.get());
                lockServerNoQ.wait(1000);
            }
            Thread.sleep(1000);
        }

        for (TestClient tc : testClients) {
            if (tc.remoteClientInterfaceNoQ == null) System.out.println("testClient NoQ not registered ====> id="+tc.id);
        }
        assertEquals(aiClientRegisterCountNoQ.get(), testClients.length);
*/
        
        for (TestClient tc : testClients) {
            if (!tc.bInitialized) System.out.println("**** testClient not Initialized ====> id="+tc.id);
        }
        for (TestClient tc : testClients) {
            assertTrue(tc.bInitialized);
        }
        System.out.println(testClients.length+" clients created and registered with server");
        
/*        
        for (TestClient tc : testClients) {
            boolean b = tc.remoteClientInterface.isStarted();
            assertFalse(b);
            b = tc.remoteClientInterfaceNoQ.isStarted();
            assertFalse(b);
        }
*/
        System.out.println("calling broadcast Start");
        remoteBroadcast.start();
        
        for (TestClient tc : testClients) {
            boolean b = tc.remoteClientInterface.isStarted();
            // System.out.println("client #"+tc.id+" started="+b);            
            assertTrue(b);
//            b = tc.remoteClientInterfaceNoQ.isStarted();
//            assertTrue(b);
        }
        System.out.println("   ... all clients are Started, each client is calling remoteServer ping, and server is calling each client ping (100 times)");
        
        for (int i=0; i<100; i++) {
            for (TestClient tc : testClients) {
                String s = OAString.getRandomString(3, 22);
                assertEquals(tc.remoteClientInterface.ping(s), tc.id+s);

                s = OAString.getRandomString(3, 22);
//                assertEquals(tc.remoteClientInterfaceNoQ.ping(s), tc.id+s);
            }
        }
        
        System.out.println("calling broadcast Stop");
        remoteBroadcast.stop();
        for (TestClient tc : testClients) {
            assertFalse(tc.remoteClientInterface.isStarted());
        }
        System.out.println("    ... all clients are Stopped");

        System.out.println("calling broadcast Close");
        remoteBroadcast.close();
        Thread.sleep(500);
    }
    
    class TestClient {
        int id;
        volatile boolean bStarted;
        volatile boolean bClosed;
        volatile boolean bInitialized;
        final Object lock = new Object();
        RemoteClientInterface remoteClientInterface, remoteClientInterfaceNoQ;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        public TestClient(final int id) {
            this.id = id;
        }
        public void run() throws Exception {
            System.out.println("TestClient. id=|"+this.id+"|");                

            multiplexerClient = new MultiplexerClient("localhost", port);
            remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient);
            multiplexerClient.start();
            
            RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
            RemoteServerInterface remoteServerNoQ = (RemoteServerInterface) remoteMultiplexerClient.lookup("serverNoQ");
    
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
                @Override
                public void close() {
                    synchronized (lock) {
                        bStarted = false;
                        bClosed = true;
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
            
System.out.println("TestClient. register START, client id=|"+this.id+"|, thread="+Thread.currentThread().getName());                
            remoteServer.register(this.id, remoteClient);
System.out.println("TestClient. register DONE, client id=|"+this.id+"|");                
                
//            assertTrue(remoteServer.isRegister(id));

/*            
            RemoteClientInterface remoteClientNoQ = new RemoteClientInterface() {
                @Override
                public String ping(String msg) {
                    return id+msg;
                }
                @Override
                public boolean isStarted() {
                    return bStarted;
                }
            };
            
            remoteServerNoQ.register(this.id, remoteClientNoQ);
            assertTrue(remoteServerNoQ.isRegister(id));
*/            
            bInitialized = true;
            
            for ( ;!bClosed; ) {
                synchronized (lock) {
                    if (!bStarted) {
                        // System.out.println("Thread #"+id+" is waiting for start command");
                        lock.wait();
                    }
                }
                // System.out.println("Thread #"+id+" is running, server is started="+remoteServer.isStarted());
//                remoteServer.isStarted();
//                remoteServerNoQ.isStarted();
                
                Thread.sleep(10);
            }
            System.out.println("Thread #"+id+" is closed");
            multiplexerClient.close();
        }
    }
}

