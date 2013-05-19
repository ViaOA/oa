package com.viaoa.object;

import com.viaoa.transaction.OATransaction;


public class OAThreadInfoDelegateTest {
    private Test[] tests;
    
    public OAThreadInfoDelegateTest() throws Exception {
        setup();
    }

    abstract class Test {
        String name;
        public Test(String name) {
            this.name = name;
        }
        public abstract String test();
        public void delay() {
            try {
                Thread.sleep(1);
                //Thread.yield();
            }
            catch (Exception e) {}
        }
    }
    
    void setup() {
        tests = new Test[6];  //qqqqqqqqqqqqqqqqqqqqqqqqqq
        
        int pos = 0;
        tests[pos++] = new Test("LoadingObject") {
            @Override
            public String test() {
                OAThreadLocalDelegate.setLoadingObject(true);
                delay();
                boolean b = OAThreadLocalDelegate.isLoadingObject();
                if (!b) return "should be true";
                OAThreadLocalDelegate.setLoadingObject(false); 
                delay();
                b = OAThreadLocalDelegate.isLoadingObject(); 
                if (b) return "should be false";
                return null;
            }
        };
    
        tests[pos++] = new Test("AssigningObjectKey") {
            @Override
            public String test() {
                OAThreadLocalDelegate.setAssigningObjectKey(true); 
                delay();
                boolean b = OAThreadLocalDelegate.isAssigningObjectKey();
                if (!b) return "should be true";
                OAThreadLocalDelegate.setAssigningObjectKey(false); 
                delay();
                b = OAThreadLocalDelegate.isAssigningObjectKey(); 
                if (b) return "should be false";
                return null;
            }
        };
        
        tests[pos++] = new Test("SkipInitialize") {
            @Override
            public String test() {
                OAThreadLocalDelegate.setSkipObjectInitialize(true); 
                delay();
                boolean b = OAThreadLocalDelegate.isSkipObjectInitialize();
                if (!b) return "should be true";
                OAThreadLocalDelegate.setSkipObjectInitialize(false); 
                delay();
                b = OAThreadLocalDelegate.isSkipObjectInitialize(); 
                if (b) return "should be false";
                return null;
            }
        };

        tests[pos++] = new Test("SuppressCSMessages") {
            @Override
            public String test() {
                OAThreadLocalDelegate.setSuppressCSMessages(true); 
                delay();
                boolean b = OAThreadLocalDelegate.isSkipObjectInitialize();
                if (!b) return "should be true";
                OAThreadLocalDelegate.setSuppressCSMessages(false); 
                delay();
                b = OAThreadLocalDelegate.isSkipObjectInitialize(); 
                if (b) return "should be false";
                return null;
            }
        };

        tests[pos++] = new Test("SkipFirePropertyChange") {
            @Override
            public String test() {
                OAThreadLocalDelegate.setSkipFirePropertyChange(true); 
                delay();
                boolean b = OAThreadLocalDelegate.isSkipFirePropertyChange();
                if (!b) return "should be true";
                OAThreadLocalDelegate.setSkipFirePropertyChange(false); 
                delay();
                b = OAThreadLocalDelegate.isSkipFirePropertyChange(); 
                if (b) return "should be false";
                return null;
            }
        };
        
        tests[pos++] = new Test("Transaction") {
            @Override
            public String test() {
                OATransaction t = new OATransaction(1);
                OAThreadLocalDelegate.setTransaction(t); 
                delay();
                OATransaction tx = OAThreadLocalDelegate.getTransaction();
                if (t != tx) return "should be set";
                OAThreadLocalDelegate.setTransaction(null); 
                delay();
                tx = OAThreadLocalDelegate.getTransaction(); 
                if (tx != null) return "should be null";
                return null;
            }
        };
    }
    
/*    
Others to do:
    private static AtomicInteger TotalObjectCacheAddMode = new AtomicInteger();
    private static AtomicInteger TotalObjectSerializer = new AtomicInteger();
    private static AtomicInteger TotalDelete = new AtomicInteger();

*/    
    private boolean bStop;
    
    class TestThread extends Thread {
        int id;
        public TestThread(int id) {
            this.id = id;
        }
        public void run() {
            for (int i=0; i<1500 && !bStop; i++) {
                if (i%50==0) System.out.println(id+") "+i);
                
                int x = (int) Math.random() * tests.length;
                
                Test test = tests[x];
                if (test == null) continue;
                String s = test.test();
                if (s != null) {
                    System.err.println("Error, test="+test.name+", error="+s);
                    bStop = true;
                }
            }
        }
    }
    
    public void test() {
        for (int i=0; i<15; i++) {
            Thread t = new TestThread(i);
            t.start();
        }
    }


    
    // Test Locking
    private Object objTest0 = new Object();
    private Object objTest1 = new Object();
    private Object objTest2 = new Object();

    public void test2() {
        Thread t = new Thread() {
            @Override
            public void run() {
                OAThreadLocalDelegate.lock(objTest2);
                OAThreadLocalDelegate.lock(objTest1, 0);
                
                System.out.println("thread done");
            }
        };

        OAThreadLocalDelegate.lock(objTest1);
        t.start();

        for (int i=0; i<=10;  i++) {
            try {
                Thread.sleep(1000);
                System.out.println(""+(10-i));
            }
            catch (InterruptedException e) {}
        }

        OAThreadLocalDelegate.lock(objTest2, 100);

        OAThreadLocalDelegate.unlock(objTest1);
        
        
        System.out.println("unlock");
        
    }
    
    
    public static void main(String[] args) throws Exception {
        
        OAThreadInfoDelegateTest test = new OAThreadInfoDelegateTest();
        
        test.test2();
    }
    
    
    
    
}
