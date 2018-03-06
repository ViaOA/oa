package com.viaoa.process;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.viaoa.hub.Hub;

import test.hifive.model.oa.Program;


public class OAChangeRefresherTest {
    
    @Test
    public void test() throws Exception {
        final AtomicInteger ai = new AtomicInteger();
        
        OAChangeRefresher r = new OAChangeRefresher() {
            @Override
            protected void process() throws Exception {
                ai.incrementAndGet();
                try {
                    Thread.sleep(10);
                }
                catch (Exception e) {
                }
            }
        };
        r.start();
        
        Thread.sleep(100);
        assertEquals(0, ai.get());
        
        Hub h = new Hub(Program.class);
        r.addListener(h, Program.P_Code);
        assertEquals(0, ai.get());

        Program p = new Program();
        h.add(p);
        assertEquals(0, ai.get());
        
        for (int i=0; i<20; i++) {
            p.setCode(i+"");
        }
        
        Thread.sleep(200);
        assertTrue(ai.get() > 0);
        
        p.setCode("x");

        Thread.sleep(500);

        assertEquals(3, ai.get());
    }    
    
    @Test
    public void test2() throws Exception {
        final AtomicInteger ai = new AtomicInteger();
        
        OAChangeRefresher r = new OAChangeRefresher() {
            @Override
            protected void process() throws Exception {
                ai.incrementAndGet();
                for (int i=0; i<10; i++) {
                    try {
                        Thread.sleep(10);
                    }
                    catch (Exception e) {
                    }
                    if (hasChanged()) break;
                }
            }
        };
        r.start();
        
        assertEquals(0, ai.get());
        
        Hub h = new Hub(Program.class);
        r.addListener(h, Program.P_Code);
        assertEquals(0, ai.get());

        Program p = new Program();
        h.add(p);
        assertEquals(0, ai.get());
        
        for (int i=0; i<20; i++) {
            p.setCode(i+"");
            try {
                Thread.sleep(10);
            }
            catch (Exception e) {
            }
        }
        
        assertTrue(ai.get() > 5);
    }    

}
