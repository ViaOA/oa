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

public class RemoteMultiplexer3Test extends OAUnitTest {
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer; 
    public final int port = 1099;
    final String queueName = "que";
    final int queueSize = 2500;
    
    private RemoteBroadcastInterface remoteBroadcast;
    private RemoteBroadcastInterface remoteBroadcastProxy;

    RequestInfo riClient, riServer;
    volatile int cntRequestClient, cntRequestServer;
    volatile int cntPingNoReturn, cntPingNoReturnNoQ;
    volatile int cntBroadcastPing;
    
    
    @Before
    public void setup() throws Exception {
        System.out.println("Before, calling setup");
        // setup server
        multiplexerServer = new MultiplexerServer(port);        
        remoteMultiplexerServer = new RemoteMultiplexerServer(multiplexerServer) {
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntRequestServer++;
                riServer = ri;
            }
        };
    
        RemoteServerInterface remoteServer = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
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
        };
        // with queue
        remoteMultiplexerServer.createLookup("server", remoteServer, RemoteServerInterface.class, queueName, queueSize);

        // without queue
        RemoteServerInterface remoteServerNoQ = new RemoteServerInterface() {
            @Override
            public void register(int id, RemoteClientInterface rci) {
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

    
    @Test(timeout=2000)
    public void testQueuedRequest() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        
        cntRequestServer = cntRequestClient = 0;
        boolean b = remoteServer.isStarted();
        
        assertEquals(1, cntRequestClient);
        assertEquals(1, cntRequestServer);

        assertEquals(RequestInfo.Type.CtoS_QueuedRequest, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_ResponseForQueuedRequest, riServer.type);
        
        assertTrue(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }

    @Test(timeout=2000)
    public void testQueuedRequestNoResponse() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        
        cntRequestServer = cntRequestClient = 0;
        
        cntPingNoReturn = 0;
        remoteServer.pingNoReturn("hey");
        Thread.sleep(200);  // give server time
        
        assertEquals(1, cntPingNoReturn);
        assertEquals(1, cntRequestClient);
        assertEquals(1, cntRequestServer);

        assertEquals(RequestInfo.Type.CtoS_QueuedRequestNoResponse, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_QueuedRequestNoResponse, riServer.type);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }
    
    @Test(timeout=2000)
    public void testNoQueueRequest() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntRequestClient++;
                riClient = ri;
            }
        };
        multiplexerClient.start();
            
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("serverNoQ");
        
        cntRequestServer = cntRequestClient = 0;
        boolean b = remoteServer.isStarted();
        
        assertEquals(1, cntRequestClient);
        assertEquals(1, cntRequestServer);

        assertEquals(RequestInfo.Type.CtoS_SocketRequest, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_SocketRequest, riServer.type);
        
        assertFalse(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }

    
    private volatile int cntClientBroadcastPing;
    @Test(timeout=2000)
    public void testBroadcast() throws Exception {
        RemoteClientInterface remoteClient;
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient) {
            @Override
            protected void afterInvokeForCtoS(RequestInfo ri) {
                cntRequestClient++;
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
        
        cntClientBroadcastPing=0;
        cntRequestServer = cntRequestClient = 0;
//qqqqqqqqqqqqqqq errrrrrrrrrrrrrror        
        boolean b = remoteBroadcast.ping("test");
        assertEquals(1, cntClientBroadcastPing);
        assertEquals(1, cntRequestClient);
        assertEquals(1, cntRequestServer);

        cntClientBroadcastPing=0;
        cntRequestServer = cntRequestClient = 0;
        b = remoteBroadcast.ping("testx");
        assertEquals(1, cntClientBroadcastPing);
        assertEquals(1, cntRequestClient);
        assertEquals(1, cntRequestServer);
        
        
        assertEquals(RequestInfo.Type.CtoS_QueuedBroadcast, riClient.type);
        assertEquals(RequestInfo.Type.CtoS_QueuedBroadcast, riServer.type);
        
        assertFalse(riServer.processedByServerQueue);
        assertTrue(riServer.methodInvoked);
        
        assertTrue(riServer.nsEnd > 0);
        assertTrue(riClient.nsEnd > 0);
        multiplexerClient.close();
    }
    
    public static void main(String[] args) throws Exception {
        MultiplexerClient.DEBUG = MultiplexerServer.DEBUG = true;
        
        System.out.println("START: "+(new OADateTime()));
        RemoteMultiplexer3Test test = new RemoteMultiplexer3Test();
        test.setup();
        test.testQueuedRequest();
        test.testNoQueueRequest();
        test.testQueuedRequestNoResponse();
        //test.testBroadcast();
        
        test.tearDown();
        System.out.println("DONE: "+(new OADateTime()));
       // System.exit(0);
    }
}

