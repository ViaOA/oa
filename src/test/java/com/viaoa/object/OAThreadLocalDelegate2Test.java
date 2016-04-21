package com.viaoa.object;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import com.viaoa.OAUnitTest;
import com.viaoa.transaction.OATransaction;

public class OAThreadLocalDelegate2Test extends OAUnitTest {
    private volatile boolean bStop;
    private Test[] tests;
    private AtomicInteger aiCnt = new AtomicInteger();
    

    @org.junit.Test
    public void concurrentTest() {
        setup();
        bStop = false;
        for (int i=0; i<10; i++) {
            Thread t = new TestThread(i);
            t.start();
        }
        for (int i=0; i<3; i++) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception e) {
            }
            if (aiCnt.get() > 2000) break;
        }
        bStop = true;
        assertTrue(aiCnt.get() > 100);
    }

    class TestThread extends Thread {
        int id;
        public TestThread(int id) {
            this.id = id;
        }
        public void run() {
            for (int i=0; i<1500 && !bStop; i++) {
                int x = (int) (Math.random() * tests.length);
                Test test = tests[x];
                test.test();
                aiCnt.incrementAndGet();
            }
        }
    }
    
    abstract class Test {
        String name;
        public Test(String name) {
            this.name = name;
        }
        public abstract void test();
    }
    
    void setup() {
        tests = new Test[6];  
        
        int pos = 0;
        tests[pos++] = new Test("LoadingObject") {
            @Override
            public void test() {
                OAThreadLocalDelegate.setLoadingObject(true);
                delay();
                assertTrue(OAThreadLocalDelegate.isLoadingObject());

                OAThreadLocalDelegate.setLoadingObject(false); 
                delay();
                assertFalse(OAThreadLocalDelegate.isLoadingObject()); 
            }
        };
    
        tests[pos++] = new Test("AssigningObjectKey") {
            @Override
            public void test() {
                OAThreadLocalDelegate.setAssigningObjectKey(true); 
                delay();
                assertTrue(OAThreadLocalDelegate.isAssigningObjectKey());
                OAThreadLocalDelegate.setAssigningObjectKey(false); 
                delay();
                assertFalse(OAThreadLocalDelegate.isAssigningObjectKey()); 
            }
        };
        
        tests[pos++] = new Test("SkipInitialize") {
            @Override
            public void test() {
                OAThreadLocalDelegate.setSkipObjectInitialize(true); 
                delay();
                assertTrue(OAThreadLocalDelegate.isSkipObjectInitialize());
                OAThreadLocalDelegate.setSkipObjectInitialize(false); 
                delay();
                assertFalse(OAThreadLocalDelegate.isSkipObjectInitialize()); 
            }
        };

        tests[pos++] = new Test("SuppressCSMessages") {
            @Override
            public void test() {
                OAThreadLocalDelegate.setSuppressCSMessages(true); 
                delay();
                assertTrue(OAThreadLocalDelegate.isSuppressCSMessages());
                OAThreadLocalDelegate.setSuppressCSMessages(false); 
                delay();
                assertFalse(OAThreadLocalDelegate.isSuppressCSMessages()); 
            }
        };

        tests[pos++] = new Test("SkipFirePropertyChange") {
            @Override
            public void test() {
                OAThreadLocalDelegate.setSkipFirePropertyChange(true); 
                delay();
                assertTrue(OAThreadLocalDelegate.isSkipFirePropertyChange());
                OAThreadLocalDelegate.setSkipFirePropertyChange(false); 
                delay();
                assertFalse(OAThreadLocalDelegate.isSkipFirePropertyChange()); 
            }
        };
        
        tests[pos++] = new Test("Transaction") {
            @Override
            public void test() {
                OATransaction t = new OATransaction(1);
                OAThreadLocalDelegate.setTransaction(t); 
                delay();
                OATransaction tx = OAThreadLocalDelegate.getTransaction();
                assertEquals(t, tx);
                OAThreadLocalDelegate.setTransaction(null); 
                delay();
                tx = OAThreadLocalDelegate.getTransaction(); 
                assertNull(tx);;
            }
        };
    }
    
/*    
Others to do:
    private static AtomicInteger TotalObjectCacheAddMode = new AtomicInteger();
    private static AtomicInteger TotalObjectSerializer = new AtomicInteger();
    private static AtomicInteger TotalDelete = new AtomicInteger();

*/    
    
}
