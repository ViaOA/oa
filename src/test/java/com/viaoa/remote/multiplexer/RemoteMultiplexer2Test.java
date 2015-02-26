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
import com.viaoa.util.OADateTime;
import com.viaoa.util.OAString;

public class RemoteMultiplexer2Test extends OAUnitTest {
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer; 
    private final AtomicInteger aiRemoteCount = new AtomicInteger();

    public final int port = 1099;
    final String queueName = "que";
    final int queueSize = 2500;
    
    int maxSecondsToRun = 20;
    
    private RemoteBroadcastInterface remoteBroadcast;
    private RemoteBroadcastInterface remoteBroadcastProxy;
    final TestClient[] testClients = new TestClient[75];
    final RemoteSessionInterface[] remoteSessions = new RemoteSessionInterface[testClients.length];
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
                //System.out.println("Server. registered called, client id="+id+"");                
                if (id < 0|| id >= testClients.length) return;
                aiClientRegisterCount.incrementAndGet();
                RequestInfo ri = OAThreadLocalDelegate.getRemoteRequestInfo();
                synchronized (testClients) {
                    testClients[id].remoteClientInterface = rci;
                }
                //System.out.println("Server. registered DONE, client id="+id+"");                
                synchronized (lockServer) {
                    lockServer.notifyAll();
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
            @Override
            public RemoteSessionInterface getSession(int id) {
                if (id < 0|| id >= testClients.length) return null;
                if (remoteSessions[id] == null) {
                    remoteSessions[id] = new RemoteSessionInterface() {
                        @Override
                        public String ping(String msg) {
                            return msg;
                        }
                    };
                }
                return remoteSessions[id];
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
            @Override
            public RemoteSessionInterface getSession(int id) {
                if (id < 0|| id >= testClients.length) return null;
                if (remoteSessions[id] == null) {
                    remoteSessions[id] = new RemoteSessionInterface() {
                        @Override
                        public String ping(String msg) {
                            return msg;
                        }
                    };
                }
                return remoteSessions[id];
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
            @Override
            public boolean ping(String msg) {
                return true;
            }
        };
        remoteBroadcastProxy = (RemoteBroadcastInterface) remoteMultiplexerServer.createBroadcast("broadcast", remoteBroadcast, RemoteBroadcastInterface.class, queueName, queueSize);
        
        multiplexerServer.start();
        remoteMultiplexerServer.start();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("unittest After(), calling tearDown");
        multiplexerServer.stop();
    }

    @Test(timeout=35000)
    public void test() throws Exception {
        final long tsBegin = System.currentTimeMillis();
        
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
        }
        System.out.println(testClients.length+" clients created, waiting for all to call register with remote server");

        for (int i=0 ; i < 10 && aiClientRegisterCount.get() != testClients.length; i++) {
            synchronized (lockServer) {
                System.out.println("   server is waiting for all clients to call register(..), total registered="+aiClientRegisterCount.get());
                lockServer.wait(500);
            }
            Thread.sleep(500);
        }
        assertEquals(testClients.length, aiClientRegisterCount.get());

        for (int i=0 ; i<10 && aiClientRegisterCountNoQ.get() != testClients.length; i++) {
            synchronized (lockServerNoQ) {
                System.out.println("   serverNoQ is waiting for all clients to call register(..), total regsitered="+aiClientRegisterCountNoQ.get());
                lockServerNoQ.wait(500);
            }
            Thread.sleep(500);
        }
        for (TestClient tc : testClients) {
            if (tc.remoteClientInterfaceNoQ == null) System.out.println("testClient NoQ not registered ====> id="+tc.id);
        }
        assertEquals(testClients.length, aiClientRegisterCountNoQ.get());
        
        for (TestClient tc : testClients) {
            if (!tc.bInitialized) {
                System.out.println("**** testClient not Initialized ====> id="+tc.id+"");
            }
        }
        for (TestClient tc : testClients) {
            assertTrue(tc.bInitialized);
        }
        System.out.println(testClients.length+" clients created and registered with server");
        
        for (TestClient tc : testClients) {
            boolean b = tc.remoteClientInterface.isStarted();
            assertFalse(b);
            b = tc.remoteClientInterfaceNoQ.isStarted();
            assertFalse(b);
        }

        System.out.println("calling broadcast start(), to have all clients start testing remote methods on server");
        remoteBroadcast.start();

        Thread.sleep(500);
        
        for (TestClient tc : testClients) {
            boolean b = tc.remoteClientInterface.isStarted();
            assertTrue( b);
            b = tc.remoteClientInterfaceNoQ.isStarted();
            assertTrue(b); 
        }

        System.out.println("   ... all clients are Started, each client is calling remoteServer.ping and broadcast.ping,");

        for (int i=0; i<10; i++) {
            Thread t = new Thread() {
                public void run() {
                    for (TestClient tc : testClients) {
                        boolean b = tc.remoteClientInterface.isStarted();
                        assertTrue( b);
                        b = tc.remoteClientInterfaceNoQ.isStarted();
                        assertTrue(b); 
                        
                        tc.remoteClientInterface.ping("xxx");
                        tc.remoteClientInterfaceNoQ.ping("xxx");
                        aiRemoteCount.addAndGet(4);
                        if ((System.currentTimeMillis() - tsBegin) > ((maxSecondsToRun-2)*1000)) break;
                    }
                }
            };
            t.start();
        }
        
        System.out.println("       10 server threads are calling clients, main server thread is calling each client");
        
        for (; ;) {
            System.out.print(".");
            for (TestClient tc : testClients) {
                String s = OAString.getRandomString(3, 22);
                String s2 = tc.remoteClientInterface.ping(s);
                assertEquals(s2, tc.id+s);

                s = OAString.getRandomString(3, 22);
                assertEquals(tc.remoteClientInterfaceNoQ.ping(s), tc.id+s);
                
                aiRemoteCount.addAndGet(2);
                if ((System.currentTimeMillis() - tsBegin) > ((maxSecondsToRun-2)*1000)) break;
            }
            if ((System.currentTimeMillis() - tsBegin) > ((maxSecondsToRun-2)*1000)) break;
            aiRemoteCount.incrementAndGet();
            remoteBroadcastProxy.ping("hey");
        }
        System.out.println();

        System.out.println("calling broadcast Stop");
        remoteBroadcast.stop();
        Thread.sleep(500);
        
        for (TestClient tc : testClients) {
            assertFalse(tc.remoteClientInterface.isStarted());
        }
        
        System.out.println("    ... all clients are Stopped");

        Thread.sleep(500);
        System.out.println("calling broadcast Close");
        
        remoteBroadcast.close();
        
        int x = aiRemoteCount.get();
        System.out.println("There were "+x+" remote calls, should be 1000+");
        assertTrue(x > 1000);
        
        Thread.sleep(500);
    }
    
    class TestClient {
        final int id;
        volatile boolean bStarted;
        volatile boolean bClosed;
        volatile boolean bInitialized;
        final Object lock = new Object();
        volatile RemoteClientInterface remoteClientInterface, remoteClientInterfaceNoQ;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        public TestClient(final int id) {
            this.id = id;
        }
        public void run() throws Exception {
            multiplexerClient = new MultiplexerClient("localhost", port);
            remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient);
            multiplexerClient.start();
            
            // System.out.println("TestClient, id="+this.id+""+", Thread="+Thread.currentThread().getName()+", connectionId="+multiplexerClient.getConnectionId());                
            
            RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
            RemoteServerInterface remoteServerNoQ = (RemoteServerInterface) remoteMultiplexerClient.lookup("serverNoQ");
            
            assertNotSame(remoteServer, remoteServerNoQ);
    
            RemoteBroadcastInterface remoteBroadcastImpl = new RemoteBroadcastInterface() {
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
                @Override
                public boolean ping(String msg) {
                    return true;
                }
            };
            RemoteBroadcastInterface remoteBroadcast = (RemoteBroadcastInterface) remoteMultiplexerClient.lookupBroadcast("broadcast", remoteBroadcastImpl);
            
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
            
            //System.out.println("TestClient. register START, client id="+this.id+", thread="+Thread.currentThread().getName());                
            remoteServer.register(this.id, remoteClient);
            //System.out.println("TestClient. register DONE, client id="+this.id+"");                
                
            assertTrue(remoteServer.isRegister(id));

            RemoteSessionInterface session = remoteServer.getSession(id);
            assertNotNull(session);

            RemoteSessionInterface session2 = remoteServer.getSession(id);
            assertSame(session, session2);

            RemoteSessionInterface sessionNoQ = remoteServerNoQ.getSession(id);
            assertSame(session, sessionNoQ);
            
            
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

            
            bInitialized = true;
            
            for ( ;!bClosed; ) {
                synchronized (lock) {
                    if (!bStarted) {
                        // System.out.println("Thread #"+id+" is waiting for start command");
                        lock.wait();
                    }
                }

                Thread.sleep(50);
                // System.out.println("Thread #"+id+" is running, server is started="+remoteServer.isStarted());
                remoteServer.isStarted();
                remoteServerNoQ.isStarted();
                boolean b = remoteBroadcast.ping("xx");
                
                String s = session.ping("aa");
                s = sessionNoQ.ping("zz");
                aiRemoteCount.addAndGet(5);
            }
            //System.out.println("Thread #"+id+" is closed");
            multiplexerClient.close();
        }
    }
     
    
    public static void main(String[] args) throws Exception {
        int delay = 0;
        System.out.println("starting in "+delay+" seconds");
        for (int i=0; i<delay; i++) {
            Thread.sleep(1000);
            System.out.print(". ");
        }
        System.out.println("START: "+(new OADateTime()));
        RemoteMultiplexer2Test test = new RemoteMultiplexer2Test();
        test.maxSecondsToRun = 60 * 5;
        test.setup();
        test.test();
        test.tearDown();
        System.out.println("DONE: "+(new OADateTime()));
        System.exit(0);
    }
}

