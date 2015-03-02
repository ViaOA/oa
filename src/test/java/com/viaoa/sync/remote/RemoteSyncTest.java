package com.viaoa.sync.remote;

import static org.junit.Assert.*;

import java.util.Comparator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.object.OAObjectKey;
import com.viaoa.remote.multiplexer.RemoteMultiplexerClient;
import com.viaoa.remote.multiplexer.RemoteMultiplexerServer;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.sync.OASyncServer;

//qqqqqqqqqqqq NOT Done qqqqqqqqqqqqqqqqqqqq

public class RemoteSyncTest extends OAUnitTest {
    private MultiplexerServer multiplexerServer;
    private RemoteMultiplexerServer remoteMultiplexerServer; 
    public final int port = 1099;
    final String queueName = "que";
    final int queueSize = 2500;
    
    private RemoteSyncInterface remoteSyncImpl;
    
    @Before
    public void setup() throws Exception {
        System.out.println("Before, calling setup");
        multiplexerServer = new MultiplexerServer(port);        
        remoteMultiplexerServer = new RemoteMultiplexerServer(multiplexerServer);
        
        remoteSyncImpl = createRemoteSync();
        
        RemoteSyncInterface rsi = (RemoteSyncInterface) remoteMultiplexerServer.createBroadcast(OASyncServer.SyncLookupName, remoteSyncImpl, RemoteSyncInterface.class, OASyncServer.SyncQueueName, OASyncServer.QueueSize);
        OASyncDelegate.setRemoteSync(rsi);
        
        multiplexerServer.start();
        remoteMultiplexerServer.start();
    }    
    
    @After
    public void tearDown() throws Exception {
        OASyncDelegate.setRemoteSync(null);
        System.out.println("unittest After(), calling tearDown");
        multiplexerServer.stop();
    }

    public RemoteSyncInterface createRemoteSync() {
        RemoteSyncInterface rsi = new RemoteSyncInterface() {
            @Override
            public boolean sort(Class objectClass, OAObjectKey objectKey, String hubPropertyName, String propertyPaths, boolean bAscending, Comparator comp) {
                return false;
            }
            @Override
            public boolean removeFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, Class objectClassX, OAObjectKey objectKeyX) {
                return false;
            }
            @Override
            public boolean removeAllFromHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName) {
                return false;
            }
            @Override
            public boolean propertyChange(Class objectClass, OAObjectKey origKey, String propertyName, Object newValue, boolean bIsBlob) {
                return false;
            }
            @Override
            public boolean moveObjectInHub(Class objectClass, OAObjectKey objectKey, String hubPropertyName, int posFrom, int posTo) {
                return false;
            }
            @Override
            public boolean insertInHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj, int pos) {
                return false;
            }
            @Override
            public boolean delete(Class objectClass, OAObjectKey objectKey) {
                return false;
            }
            @Override
            public boolean addToHub(Class masterObjectClass, OAObjectKey masterObjectKey, String hubPropertyName, Object obj) {
                return false;
            }
        };
        return rsi;
    }
    
    
    @Test
    public void test() throws Exception {
        MultiplexerClient multiplexerClient;
        RemoteMultiplexerClient remoteMultiplexerClient;
        
        multiplexerClient = new MultiplexerClient("localhost", port);
        remoteMultiplexerClient = new RemoteMultiplexerClient(multiplexerClient);
        multiplexerClient.start();

        RemoteSyncInterface remoteSyncImpl = createRemoteSync();

        RemoteSyncInterface rsi = (RemoteSyncInterface) remoteMultiplexerClient.lookupBroadcast(OASyncServer.SyncLookupName, remoteSyncImpl);
        OASyncDelegate.setRemoteSync(rsi);
    }
    
}
