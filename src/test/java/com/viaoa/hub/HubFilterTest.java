package com.viaoa.hub;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.tmgsc.hifivetest.model.oa.*;
import com.viaoa.OAUnitTest;

public class HubFilterTest extends OAUnitTest {

    @Test
    public void test() {
        init();

        Hub<PointsAwardLevel> hubMaster = new Hub<PointsAwardLevel>(PointsAwardLevel.class);
        
        for (int i=0; i<200; i++) {
            PointsAwardLevel pal = new PointsAwardLevel();
            hubMaster.add(pal);
        }
        
        hubMaster.saveAll();
        
        _test(hubMaster);
        
        for (int i=0; i<10; i++) System.gc();

        PointsAwardLevel pal = new PointsAwardLevel();
        hubMaster.add(pal);
        // should have cause hubFilters to be closed
    }

    public void _test(final Hub<PointsAwardLevel> hubMasterMain) {
        System.out.println("HubFilterTest, thread="+Thread.currentThread().getName());
        for (int i=0; i<5000; i++) {
            final Hub<PointsAwardLevel> hubMaster = hubMasterMain.createSharedHub();
            Hub<PointsAwardLevel> hubFiltered = new Hub<PointsAwardLevel>(PointsAwardLevel.class);
            //hubMaster.copyInto(hubFiltered);

            HubFilter<PointsAwardLevel> hf = new HubFilter<PointsAwardLevel>(hubMaster, hubFiltered) {
                public boolean isUsed(PointsAwardLevel level) {
                    return true;
                }
            }; 
            
            int x = hubFiltered.getSize();
            

            if (i % 50 == 0) {
                for (int j=0; j<10; j++) System.gc();
            }
            
            //System.out.println("i="+i+", hubFiltered.getSize="+hubFiltered.getSize());
            assertEquals(200, hubFiltered.getSize());
            // hf.close();

            x = HubEventDelegate.getListenerCount(hubMaster);
        }
    }
    
    @Test
    public void test2() {
        final int max = 5;
        
        final Hub<PointsAwardLevel> hubMaster1 = new Hub<PointsAwardLevel>(PointsAwardLevel.class);
        for (int i=0; i<20; i++) {
            PointsAwardLevel pal = new PointsAwardLevel();
            hubMaster1.add(pal);
        }
        final Hub<PointsAwardLevel> hubMaster2 = new Hub<PointsAwardLevel>(PointsAwardLevel.class);
        for (int i=0; i<20; i++) {
            PointsAwardLevel pal = new PointsAwardLevel();
            hubMaster2.add(pal);
        }

        
        final CyclicBarrier barrier = new CyclicBarrier(max);
        final CountDownLatch countDownLatch = new CountDownLatch(max);
        final AtomicInteger aiDone = new AtomicInteger(); 
        
        for (int i=0; i<max; i++) {
            final int id = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        barrier.await();
                        Hub<PointsAwardLevel> hub = (id %2 == 0) ? hubMaster1 : hubMaster2;
                        _test(hub);
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
                countDownLatch.await(1, TimeUnit.SECONDS);
                if (aiDone.get() == max) break;
                hubMaster1.setPos(i%hubMaster1.getSize());
                hubMaster2.setPos(i%hubMaster2.getSize());
            }
            catch (Exception e) {
                // TODO: handle exception
            }
        }
    }    
    

    public static void main(String[] args) throws Exception {
        /*
        System.out.println("first of two 30 second count down");
        for (int i=0; i<120; i++) {
            if (i%4==0) System.out.println("countdown "+((120-i)/4));
            Thread.sleep(250);
        }

        ArrayList<PointsAwardLevel> al = new ArrayList<PointsAwardLevel>();
        for (int i=0; i<100000; i++) {
            PointsAwardLevel pal = new PointsAwardLevel();
            al.add(pal);
        }
        
        System.out.println("second of two 30 second count down");
        for (int i=0; i<120; i++) {
            if (i%4==0) System.out.println("countdown "+((120-i)/4));
            Thread.sleep(250);
        }
        */
        HubFilterTest test = new HubFilterTest();
        test.test();
        System.out.println("test is done");
        /*
        for (;;) {
            Thread.sleep(30 * 1000);
        }
        */
    }
    
}




