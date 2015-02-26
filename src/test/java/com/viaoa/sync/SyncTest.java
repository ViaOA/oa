package com.viaoa.sync;

import static org.junit.Assert.*;

import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.theice.tsactest.model.oa.Server;
import com.viaoa.OAUnitTest;
import com.viaoa.object.OAThreadLocalDelegate;

/**
 * NOT Done:
 * 
 *       might not be able to run syncServer and syncClient together
 * 
 */
public class SyncTest extends OAUnitTest {
    
    private OASyncServer syncServer;
    private OASyncClient syncClient;
    private Server serverTest;
    public final int port = 1099;
    
    //@Before
    public void setup() throws Exception {
        // setup server
        
        syncServer = new OASyncServer(port);
        syncServer.start();
        
        syncClient = new OASyncClient("localhost", port);
        syncClient.start();
//        ServerRoot serverRoot = (ServerRoot) syncClient.getRemoteServer().getObject(ServerRoot.class, new OAObjectKey(777));
        

        // create sample object on server
        OAThreadLocalDelegate.setLoadingObject(true);
        serverTest = new Server();
        serverTest.setId(1);
        OAThreadLocalDelegate.setLoadingObject(false);
        
        serverTest.setName("test");
        
        int xx = 4;
        xx++;
    }

    //@After
    public void tearDown() throws Exception {
        syncClient.stop();
        syncServer.stop();
    }
    
    //@Test
    //(timeout=5000)
    public void test() throws Exception {
        
        
    }
}
