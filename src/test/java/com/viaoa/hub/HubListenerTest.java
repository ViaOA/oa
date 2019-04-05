package com.viaoa.hub;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.viaoa.OAUnitTest;
import com.viaoa.object.OAFinder;

import test.hifive.model.oa.PointsAwardLevel;
import test.xice.tsac3.model.Model;
import test.xice.tsac3.Tsac3DataGenerator;
import test.xice.tsac3.model.oa.*;
import test.xice.tsac3.model.oa.propertypath.EnvironmentPP;
import test.xice.tsac3.model.oa.propertypath.ServerPP;
import test.xice.tsac3.model.oa.propertypath.SitePP;

public class HubListenerTest extends OAUnitTest {
    private int cntAdd;
    private int cntRemove;
    private int cntChange, cntChange2;
    private int cntNewList;
    private int cntChangeActiveObject;
    
    @Override
    protected void reset() {
        super.reset();
        cntAdd = cntRemove = cntChange = cntChange2 = cntNewList = cntChangeActiveObject = 0;
    }

    @Test
    public void listenerTest() {
        reset();
        
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                cntAdd++;
            }
            @Override
            public void afterRemove(HubEvent e) {
                cntRemove++;
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                cntChange++;
            }
            @Override
            public void onNewList(HubEvent e) {
                cntNewList++;
            }
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                cntChangeActiveObject++;
            }
        };
        Hub<Server> hub = new Hub<Server>(Server.class);
        hub.addHubListener(hl);
        
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);
        
        Server server = new Server();
        hub.add(server);
        assertEquals(cntAdd, 1);

        server.setHostName("x.z");
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 1);
        assertEquals(cntRemove, 0);
        
        hub.add(server); // should not re-add
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 1);
        assertEquals(cntRemove, 0);
        
        hub.remove(0);
        assertEquals(hub.getSize(), 0);
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 1);
        assertEquals(cntRemove, 1);
        
        hub.add(server);
        assertEquals(hub.getSize(), 1);
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 2);
        assertEquals(cntRemove, 1);
        
        hub.clear();  
        assertEquals(hub.getSize(), 0); 
        assertEquals(cntNewList, 1);
        assertEquals(cntChange, 1);
        assertEquals(cntAdd, 2);
        assertEquals(cntRemove, 1); // does not send remove events, only a newListEvent  

        // AO test
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(null);
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(-1);
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(0);  // no objects
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(99); 
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(server);
        assertEquals(cntChangeActiveObject, 0);
        hub.setPos(-1);
        assertEquals(cntChangeActiveObject, 0);
        
        
        hub.add(server);
        assertEquals(cntChangeActiveObject, 0);
        hub.setAO(server);
        assertEquals(cntChangeActiveObject, 1);
        hub.setAO(99); 
        assertEquals(cntChangeActiveObject, 2);
        assertNull(hub.getAO());
        hub.remove(0);
        assertEquals(cntChangeActiveObject, 2);
        hub.add(server);
        assertEquals(cntChangeActiveObject, 2);
        hub.setPos(0);
        assertEquals(cntChangeActiveObject, 3);
        
        hub.removeAll();
        assertEquals(cntChangeActiveObject, 4);
        assertNull(hub.getAO());
        
        int cAdd = cntAdd;
        for (int i=0; i<10; i++) {
            server = new Server();
            hub.add(server);
            assertEquals(cntAdd, ++cAdd);
        }
        
        int cAO = cntChangeActiveObject;
        for (int i=0; i<100; i++) {
            int pos = (int) (Math.random() * 100);
            if (pos < hub.getSize()) {
                if (hub.getAO() != hub.getAt(pos)) cAO++;
            }
            else {
                if (hub.getAO() != null) cAO++;
            }
            
            hub.setPos(pos);
            assertEquals(cntChangeActiveObject, cAO);
        }
        
        int cChange = cntChange;
        for (int i=0; i<100; i++) {
            int pos = (int) (Math.random() * hub.getSize());
            server = hub.getAt(pos);
            String s = "c"+pos;
            if (!s.equals(server.getHostName())) cChange++;
            server.setHostName(s);
            assertEquals(cntChange, cChange);
        }
        
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        reset();
    }

    
    @Test
    public void listener2Test() {
        reset();
        
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                cntAdd++;
            }
            @Override
            public void afterRemove(HubEvent e) {
                cntRemove++;
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                cntChange++;
                if ("xxx".equalsIgnoreCase(e.getPropertyName())) {
                    cntChange2++;
                }
            }
            @Override
            public void onNewList(HubEvent e) {
                cntNewList++;
            }
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                cntChangeActiveObject++;
            }
        };
        
        Model modelTsac = new Model();
        Tsac3DataGenerator data = new Tsac3DataGenerator(modelTsac);
        data.createSampleData();
        
        
        Hub<Server> hub = new Hub<Server>(Server.class);
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);

        HubMerger<Site, Server> hm = new HubMerger<Site, Server>(modelTsac.getSites(), hub, SitePP.environments().silos().servers().pp, true);

        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        hub.addHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);

        //---
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        //(Hub thisHub, HubListener hl, String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly) {
        hub.addHubListener(hl, "xxx", new String[] {
            ServerPP.silo().environment().name(), 
            ServerPP.silo().mradServer().mradClients().server().name(),
            ServerPP.silo().servers().name() 
        }, false);
        
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 3);
        
        int cChange2 = 0;
        assertEquals(cntChange2, cChange2); 

        Server server = hub.getAt(5);
        server.getSilo().getEnvironment().setName("xxx");
        
        OAFinder<Environment, Server> finder = new OAFinder<Environment, Server>(EnvironmentPP.silos().servers().pp);
        cChange2 += finder.find(server.getSilo().getEnvironment()).size();
        
        assertEquals(cChange2, cntChange2); 
        assertEquals(cChange2, cntChange); 
        
        cntChange = cntChange2 = cChange2 = 0;
        
        server.setName("zzz");
        assertEquals(cntChange2, 5); 
        assertEquals(cntChange, 7);  

        
        //---
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        //(Hub thisHub, HubListener hl, String property, String[] dependentPropertyPaths, boolean bActiveObjectOnly) {
        hub.addHubListener(hl, "name");
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 1);
        

        server = hub.getAt(4);
        cntChange = cntChange2 = 0;
        
        server.setName("zz1");
        assertEquals(cntChange, 2);  
        assertEquals(cntChange2, 0); 

        //---
        hub.addHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 1);
        
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);

        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        reset();
    }
    
    
    @Test
    public void listener3Test() {
        reset();
        
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                cntAdd++;
            }
            @Override
            public void afterRemove(HubEvent e) {
                cntRemove++;
            }
            @Override
            public void afterPropertyChange(HubEvent e) {
                cntChange++;
                if ("xxx".equalsIgnoreCase(e.getPropertyName())) {
                    cntChange2++;
                }
            }
            @Override
            public void onNewList(HubEvent e) {
                cntNewList++;
            }
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                cntChangeActiveObject++;
            }
        };
        
        Model modelTsac = new Model();
        Tsac3DataGenerator data = new Tsac3DataGenerator(modelTsac);
        data.createSampleData();
        
        Hub<Site> hub = modelTsac.getSites();
        
        hub.addHubListener(hl);
        HubListener[] hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 1 && hls[0] == hl);

        //---
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);
        
        hub.addHubListener(hl, "xxx", new String[] {
            SitePP.environments().silos().servers().name(),
            SitePP.environments().silos().networkMask(),
            SitePP.environments().name()
        }, false);
        
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls != null && hls.length == 2);

        hub.getAt(1).getEnvironments().getAt(1).getSilos().getAt(1).getServers().getAt(0).setName("xx4");
        assertEquals(cntChange, 2); // site is owner of envs.sios.servers and will get a change event

        hub.getAt(1).getEnvironments().getAt(1).getSilos().getAt(0).setNetworkMask("xx5");
        assertEquals(cntChange, 3);

        hub.getAt(1).getEnvironments().getAt(0).setName("xx5");
        assertEquals(cntChange, 4);
        
        //---
        hub.removeHubListener(hl);
        hls = HubEventDelegate.getAllListeners(hub);
        assertTrue(hls == null || hls.length == 0);

        hub.addHubListener(hl, "xxx", new String[] {
            SitePP.environments().silos().servers().silo().environment().site().name()
        }, false);
        
        cntChange = cntChange2 = 0;
        hub.getAt(1).setName("qqq");
        assertEquals(cntChange2, 1);
        
        reset();
    }

    @Test
    public void listener4Test() {
        reset();
        
        final Hub hub1 = new Hub();
        final Hub hub2 = new Hub();
        final Hub hub3 = new Hub();
        final Hub hub4 = new Hub();
        
        final int maxThreads = 7;
        final int maxIterations = 500;
        final CyclicBarrier barrier = new CyclicBarrier(maxThreads);
        final CountDownLatch countDownLatch = new CountDownLatch(maxThreads);
        final AtomicInteger aiDone = new AtomicInteger();
        
        final HubListener hl = new HubListenerAdapter();
        
        final AtomicInteger aiListenerCount = new AtomicInteger();
        
        for (int i=0; i<maxThreads; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        for (int i=0; i<maxIterations; i++) {
                            hub1.addHubListener(hl);
                            hub2.addHubListener(hl);
                            hub3.addHubListener(hl);
                            hub4.addHubListener(hl);
                            
                            aiListenerCount.addAndGet(4);
                            
                            if (i%6==0) HubEventDelegate.getListenerCount(hub1);
                            if (i%7==0) HubEventDelegate.getListenerCount(hub2);
                            if (i%3==0) HubEventDelegate.getListenerCount(hub3);
                            if (i%4==0) HubEventDelegate.getListenerCount(hub4);
                            if (i%5 == 0) {
                                try {
                                    Thread.sleep( (int) (Math.random() * 3));
                                }
                                catch (Exception e) {
                                    System.out.println("Exception in HutListenerTest, ex="+e);
                                }
                            }
                            
                            if ( Math.random() < .40) {
                                hub1.removeHubListener(hl);
                                hub2.removeHubListener(hl);
                                hub3.removeHubListener(hl);
                                hub4.removeHubListener(hl);
                                aiListenerCount.addAndGet(-4);
                            }
                        }
                    }
                    catch (Exception e) {
                        System.out.println("HubFilterTest error: "+e);
                        e.printStackTrace();
                    }
                    finally {
                        aiDone.getAndIncrement();
                        countDownLatch.countDown();
                    }
                }
            });
            t.start();
        }
        
        for (int i=0;;i++) {
            try {
                countDownLatch.await();

                if (aiDone.get() == maxThreads) break;
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
        
        int x = aiListenerCount.get();
        assertTrue(HubEventDelegate.getAllListeners(hub1).length < 2);
        assertTrue(HubEventDelegate.getAllListeners(hub2).length < 2);
        assertTrue(HubEventDelegate.getAllListeners(hub3).length < 2);
        assertTrue(HubEventDelegate.getAllListeners(hub4).length < 2);
    
        for (int i=0; i<1; i++) {
            hub1.removeHubListener(hl);
            hub2.removeHubListener(hl);
            hub3.removeHubListener(hl);
            hub4.removeHubListener(hl);
        }

        assertEquals(0, HubEventDelegate.getAllListeners(hub1).length);
        assertEquals(0, HubEventDelegate.getAllListeners(hub2).length);
        assertEquals(0, HubEventDelegate.getAllListeners(hub3).length);
        assertEquals(0, HubEventDelegate.getAllListeners(hub4).length);
    
    }
    
    
}


