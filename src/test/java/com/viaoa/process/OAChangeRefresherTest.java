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
                    Thread.sleep(100);
                }
                catch (Exception e) {
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
        }
        
        assertEquals(1, ai.get());
        
        Thread.sleep(1000);

        p.setCode("x");

        Thread.sleep(300);

        assertEquals(2, ai.get());
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
