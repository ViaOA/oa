package com.viaoa.remote.multiplexer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.remote.*;  // test package only
import com.viaoa.util.OADateTime;

public class RemoteMultiplexerTest extends OAUnitTest {
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer; 
    public final int port = 1101;
    final String queueName = "que";
    final int queueSize = 2500;
    
    private RemoteBroadcastInterface remoteBroadcast;
    private RemoteBroadcastInterface remoteBroadcastProxy;

    RequestInfo riClient, riServer;
    volatile int cntCtoSRequestClient, cntCtoSRequestServer;
    volatile int cntStoCRequestClient, cntStoCRequestServer;
    volatile int cntPingNoReturn, cntPingNoReturnNoQ;
    volatile int cntBroadcastPing;
    private volatile RemoteClientInterface remoteClientInterfaceOnServer;
    private volatile RemoteClientInterface remoteClientInterfaceOnServerNoQ;
    
    
    @Before
    public void setup() throws Exception {
        System.out.println("Before, calling setup");
        // setup server
        multiplexerServer = new MultiplexerServer(port);        
        remoteMultiplexerServer = new RemoteMultiplexerServer(multiplexerServer) {
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestServer++;
                riServer = ri;
            }
            @Override
            protected void afterInvokeForStoC(RequestInfo ri) {
                cntStoCRequestServer++;
                riServer = ri;
            }
        };
    
        RemoteServerInterface remoteServer = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
                remoteClientInterfaceOnServer = rci;
            }
            @Override
            public void registerNoResponse(int id, RemoteClientInterface rci) {
                remoteClientInterfaceOnServer = rci;
            }
            @Override
            public boolean isStarted() {
                return true;
            }
            @Override
            public boolean isRegister(int id) {
                return false;
            }
            @Override
            public RemoteSessionInterface getSession(int id) {
                return null;
            }
            @Override
            public void pingNoReturn(String msg) {
                cntPingNoReturn++;
            }
            @Override
            public void registerTest(int id, RemoteClientInterface rci, RemoteBroadcastInterface rbi) {
                remoteClientInterfaceOnServer = rci;
                rbi.ping("hey");
            }
        };
        // with queue
        remoteMultiplexerServer.createLookup("server", remoteServer, RemoteServerInterface.class, queueName, queueSize);

        // without queue
        RemoteServerInterface remoteServerNoQ = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
                remoteClientInterfaceOnServerNoQ = rci;
            }
            @Override
            public boolean isStarted() {
                return true;
            }
            @Override
            public boolean isRegister(int id) {
                return false;
            }
            @Override
            public RemoteSessionInterface getSession(int id) {
                return null;
            }
            @Override
            public void pingNoReturn(String msg) {
                cntPingNoReturnNoQ++;
            }
            @Override
            public void registerNoResponse(int id, RemoteClientInterface rci) {
                remoteClientInterfaceOnServerNoQ = rci;
            }
            @Override
            public void registerTest(int id, RemoteClientInterface rci, RemoteBroadcastInterface rbi) {
                remoteClientInterfaceOnServer = rci;
                rbi.ping("hey");
            }
        };
        remoteMultiplexerServer.createLookup("serverNoQ", remoteServerNoQ, RemoteServerInterface.class);
        
        
        remoteBroadcast = new RemoteBroadcastInterface() {
            @Override
            public void stop() {
            }
            @Override
            public void start() {
            }
            @Override
            public void close() {
            }
            @Override
            public boolean ping(String msg) {
                cntBroadcastPing++;
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


    @Test //(timeout=2000)
    public void testCtoS_QueuedRequest() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        Thread.sleep(200);
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        boolean b = remoteServer.isStarted();
        
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.CtoS_QueuedRequest, riClient.type);
        assertEquals(RequestInfo.Type.StoC_QueuedResponse, riServer.type);
        
        assertTrue(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }

    @Test(timeout=2000)
    public void testCtoS_QueuedRequestNoResponse() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        Thread.sleep(200);
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        
        cntPingNoReturn = 0;
        remoteServer.pingNoReturn("hey");
        Thread.sleep(200);  // give server time
        
        assertEquals(1, cntPingNoReturn);
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.CtoS_QueuedRequestNoResponse, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_QueuedRequestNoResponse, riServer.type);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }
    
    @Test
    //(timeout=4500)
    public void testStoC_QueuedRequest() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
            @Override
            public void afterInvokForStoC(RequestInfo ri) {
                cntStoCRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        Thread.sleep(200);
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntStoCRequestServer = cntStoCRequestClient = 0;
        RemoteClientInterface rci = new RemoteClientInterface() {
            @Override
            public String ping(String msg) {
                return msg;
            }
            @Override
            public boolean isStarted() {
                return false;
            }
        };
        
        remoteServer.register(1, rci);
        
        Thread.sleep(100);
        assertNotNull(remoteClientInterfaceOnServer);
        assertEquals(1, cntStoCRequestClient);
        assertEquals(0, cntStoCRequestServer);
        
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        //assertEquals(RequestInfo.Type.CtoS_QueuedRequest, riClient.type);
        assertEquals(RequestInfo.Type.StoC_QueuedResponse, riServer.type);
        
        
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntStoCRequestServer = cntStoCRequestClient = 0;
        String s = remoteClientInterfaceOnServer.ping("xxx");
        assertEquals("xxx", s);
        Thread.sleep(100);
        
        assertEquals(1, cntStoCRequestClient);
        
        assertEquals(1, cntStoCRequestServer);
        
        assertEquals(0, cntCtoSRequestClient);
        assertEquals(0, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.StoC_QueuedRequest, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_QueuedResponse, riServer.type);
        
        assertTrue(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }
    
    @Test(timeout=12000)
    public void testStoC_QueuedRequestNoResponse() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
            @Override
            public void afterInvokForStoC(RequestInfo ri) {
                cntStoCRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        Thread.sleep(200);
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntStoCRequestServer = cntStoCRequestClient = 0;
        RemoteClientInterface rci = new RemoteClientInterface() {
            @Override
            public String ping(String msg) {
                return msg;
            }
            @Override
            public boolean isStarted() {
                return false;
            }
        };
        
        remoteServer.registerNoResponse(1, rci);
        Thread.sleep(100);
        
        assertNotNull(remoteClientInterfaceOnServer);
        assertEquals(0, cntStoCRequestClient);
        assertEquals(0, cntStoCRequestServer);

        Thread.sleep(100);
        
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.CtoS_QueuedRequestNoResponse, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_QueuedRequestNoResponse, riServer.type);
        
        multiplexerClient.close();
    }    

    @Test(timeout=2000)
    public void testStoC_SocketRequest() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
            @Override
            public void afterInvokForStoC(RequestInfo ri) {
                cntStoCRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("serverNoQ");
        Thread.sleep(200);
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntStoCRequestServer = cntStoCRequestClient = 0;
        RemoteClientInterface rci = new RemoteClientInterface() {
            @Override
            public String ping(String msg) {
                return msg;
            }
            @Override
            public boolean isStarted() {
                return false;
            }
        };
        
        remoteServer.register(1, rci);
        Thread.sleep(120);
        
        assertNotNull(remoteClientInterfaceOnServerNoQ);
        assertEquals(0, cntStoCRequestClient);
        assertEquals(0, cntStoCRequestServer);
        
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.CtoS_SocketRequest, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_SocketRequest, riServer.type);
        
        
        
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntStoCRequestServer = cntStoCRequestClient = 0;
        String s = remoteClientInterfaceOnServerNoQ.ping("xxx");
        assertEquals("xxx", s);
        Thread.sleep(120);
        
        assertEquals(1, cntStoCRequestClient);
        
        assertEquals(1, cntStoCRequestServer);
        
        assertEquals(0, cntCtoSRequestClient);
        assertEquals(0, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.StoC_SocketRequest, riClient.type);
        assertEquals(RequestInfo.Type.StoC_SocketRequest, riServer.type);
        
        assertFalse(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }    

    @Test(timeout=5000)
    public void testStoC_SocketRequestNoResponse() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
            @Override
            public void afterInvokForStoC(RequestInfo ri) {
                cntStoCRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("serverNoQ");
        Thread.sleep(200);

        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntStoCRequestServer = cntStoCRequestClient = 0;
        cntPingNoReturnNoQ = 0;
        
        remoteServer.pingNoReturn("xxx");
        Thread.sleep(200);
        
        assertEquals(1, cntPingNoReturnNoQ);
        
        assertEquals(0, cntStoCRequestClient);
        assertEquals(0, cntStoCRequestServer);
        
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        assertEquals(RequestInfo.Type.CtoS_SocketRequestNoResponse, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_SocketRequestNoResponse, riServer.type);
        
        assertTrue(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }    

    
    
    private volatile int cntClientBroadcastPing;
    @Test //(timeout=2000)
    public void testStoC_QueuedBroadcast() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntCtoSRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteBroadcastInterface remoteBroadcastImpl = new RemoteBroadcastInterface() {
            @Override
            public void stop() {
            }
            @Override
            public void start() {
            }
            @Override
            public void close() {
            }
            @Override
            public boolean ping(String msg) {
                cntClientBroadcastPing++;
                return true;
            }
        };
        RemoteBroadcastInterface remoteBroadcast = (RemoteBroadcastInterface) remoteMultiplexerClient.lookupBroadcast("broadcast", remoteBroadcastImpl);
        Thread.sleep(200);
        
        cntClientBroadcastPing=0;
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        
        boolean b = remoteBroadcast.ping("test");
        Thread.sleep(200);
        assertEquals(0, cntClientBroadcastPing);
        
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);

        cntClientBroadcastPing=0;
        cntCtoSRequestServer = cntCtoSRequestClient = 0;
        cntBroadcastPing = 0;
        b = remoteBroadcast.ping("testx");
        assertEquals(0, cntClientBroadcastPing);
        Thread.sleep(200); // wait for server to get it
        assertEquals(1, cntBroadcastPing);
        assertEquals(1, cntCtoSRequestClient);
        assertEquals(1, cntCtoSRequestServer);
        
        
        assertEquals(RequestInfo.Type.CtoS_QueuedBroadcast, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_QueuedBroadcast, riServer.type);
        
        assertTrue(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }
    
    public static void main(String[] args) throws Exception {
        MultiplexerClient.DEBUG = MultiplexerServer.DEBUG = true;
        
        System.out.println("START: "+(new OADateTime()));
        
        RemoteMultiplexerTest test = new RemoteMultiplexerTest();
        test.setup();
//qqqqqqqqqqqqqqqqqqqq        
test.testStoC_SocketRequest();        
        
        test.testCtoS_QueuedRequest();
        test.testCtoS_QueuedRequestNoResponse();
        test.testStoC_QueuedRequest();
        test.testStoC_QueuedRequestNoResponse();
        
        test.testStoC_SocketRequest();
        test.testStoC_SocketRequestNoResponse();
        test.testStoC_QueuedBroadcast();
        
        test.tearDown();
        System.out.println("DONE: "+(new OADateTime()));
    }
}

