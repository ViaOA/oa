package com.viaoa.sync;

import java.util.logging.Level;
import java.util.logging.Logger;
import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.model.oa.cs.ServerRoot;
import test.xice.tsam.util.DataGenerator;

import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.object.OAObject;
import com.viaoa.sync.remote.RemoteTestInterface2;
import com.viaoa.util.*;

/**
 *  simple server to be run manually
 */
public class OASyncServerTest2 {
    private static Logger LOG = Logger.getLogger(OASyncServerTest2.class.getName());
    
    private ServerRoot serverRoot;    
    public OASyncServer syncServer;

    public void start() throws Exception {
        DataGenerator dg = new DataGenerator();
        serverRoot = dg.readSerializeFromFile();
        ModelDelegate.initialize(serverRoot, null);
        getSyncServer().start();
    }
    
    public OASyncServer getSyncServer() {
        if (syncServer != null) return syncServer;
        
        OADataSourceObjectCache ds = new OADataSourceObjectCache();
        ds.setAssignIdOnCreate(true);
        
        syncServer = new OASyncServer(1102);

        
        RemoteTestInterface2 remoteTest = new RemoteTestInterface2() {
            @Override
            public ServerRoot getServerRoot() {
                return serverRoot;
            }
        }; 
        syncServer.createSyncLookup(RemoteTestInterface2.BindName, remoteTest, RemoteTestInterface2.class);
        
        return syncServer;
    }   

    
    public static void main(String[] args) throws Exception {
        OAObject.setDebugMode(true);
        OALogUtil.consoleOnly(Level.FINE, OACircularQueue.class.getName());//"com.viaoa.util.OACircularQueue");
        
        OASyncServerTest2 test = new OASyncServerTest2();
        test.start();
        
        int scnt = -1;
        for (int i=1;;i++) {
            int x;
            do {
                x = test.syncServer.getSessionCount();
                Thread.sleep(1 * 1000);
            }
            while (x == scnt);
            scnt = x;
            System.out.println(i+") ServerTest is running "+(new OATime())+",  sessionCount="+x);
        }
    }
    
}
