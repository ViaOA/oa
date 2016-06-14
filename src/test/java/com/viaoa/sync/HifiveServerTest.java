package com.viaoa.sync;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.ds.objectcache.OADataSourceObjectCache;
import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAObjectSerializer;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.sync.remote.RemoteBroadcastInterface;
import com.viaoa.sync.remote.RemoteTestInterface;
import com.viaoa.sync.remote.RemoteTsamInterface;
import com.viaoa.util.*;

import test.hifive.DataGenerator;
import test.hifive.delegate.ModelDelegate;
import test.hifive.model.oa.*;
import test.hifive.model.oa.cs.ServerRoot;

/**
 *  Run this manually, and then run OASyncClientTest multiple times, and then run it as a junit test.
 */
public class HifiveServerTest {
    private static Logger LOG = Logger.getLogger(HifiveServerTest.class.getName());
    
    private ServerRoot serverRoot;    
    public OASyncServer syncServer;

    public void start() throws Exception {
        
        // for non-DB objects
        OADataSourceObjectCache ds = new OADataSourceObjectCache();
        ds.setAssignIdOnCreate(true);

        serverRoot = new ServerRoot();
        
        // serverRoot = dg.readSerializeFromFile();
        long t1 = System.currentTimeMillis();
        // gen data
        for (int i=0; i<5; i++) {
            Program prog = new Program();
            
            for (int cnt=0; cnt<5; cnt++) {
                Location loc = new Location();
                prog.getLocations().add(loc);
                for (int cnt2=0; cnt2<10; cnt2++) {
                    Employee emp = new Employee();
                    loc.getEmployees().add(emp);
                }
            }
            serverRoot.getActivePrograms().add(prog);
        }
        
        
        long t2 = System.currentTimeMillis();
        
        long diff = t2 - t1;
        
        ModelDelegate.initialize(serverRoot);
        
        
        syncServer = new OASyncServer(1103) {
            @Override
            protected void onClientConnect(Socket socket, int connectionId) {
                super.onClientConnect(socket, connectionId);
                //OASyncServerTest.this.onClientConnect(socket, connectionId);
            }
            @Override
            protected void onClientDisconnect(int connectionId) {
                super.onClientDisconnect(connectionId);
                //OASyncServerTest.this.onClientDisconnect(connectionId);
            }
        };
        syncServer.start();
    }   
    
    
    public static void main(String[] args) throws Exception {
        MultiplexerServer.DEBUG = true;
        OALogUtil.consoleOnly(Level.FINE, OACircularQueue.class.getName());//"com.viaoa.util.OACircularQueue");
        
        Logger logx = Logger.getLogger(OACircularQueue.class.getName());
        
        HifiveServerTest test = new HifiveServerTest();
        test.start();
        
        System.out.println("Hifive test server is now running");
        for (;;) {
            Thread.sleep(10000);
        }
    }
    
}
