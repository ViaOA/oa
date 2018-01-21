package com.viaoa.remote.multiplexer;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.object.OAObject;
import com.viaoa.remote.multiplexer.info.RequestInfo;
import com.viaoa.remote.multiplexer.remote.*;  // test package only
import com.viaoa.util.OADateTime;

public class RemoteMultiplexerTest4 extends OAUnitTest {
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
        remoteMultiplexerServer.createLookup("server", remoteServer, RemoteServerInterface.class);
        
        
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

    
    private volatile int cntClientBroadcastPing;
    @Test(timeout=2000)
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

        remoteClient = new RemoteClientInterface() {
            @Override
            public String ping(String msg) {
                return msg;
            }
            @Override
            public boolean isStarted() {
                return true;
            }
        };
        
        RemoteServerInterface remoteServer = (RemoteServerInterface) remoteMultiplexerClient.lookup("server");
        remoteServer.registerTest(123, remoteClient, remoteBroadcast);
        
        Thread.sleep(200);

        multiplexerClient.close();
    }
    
    public static void main(String[] args) throws Exception {
        OAObject.setDebugMode(true);
        
        System.out.println("START: "+(new OADateTime()));
        
        RemoteMultiplexerTest4 test = new RemoteMultiplexerTest4();
        test.setup();
        test.testStoC_QueuedBroadcast();
        
        test.tearDown();
        System.out.println("DONE: "+(new OADateTime()));
    }
}

