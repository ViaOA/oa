package com.viaoa.object;

import java.util.Vector;

import com.vetjobs.Job;

public class OAObjectInfoDelegateTest {

    private Test[] tests;
    
    public OAObjectInfoDelegateTest() throws Exception {
        setup();
    }

    abstract class Test {
        String name;
        public Test(String name) {
            this.name = name;
        }
        public abstract String test(int id);
        public void delay() {
            try {
//                Thread.sleep(1);
                Thread.yield();
            }
            catch (Exception e) {}
        }
    }

    
    void setup() {
        tests = new Test[1];
        
        int pos = 0;
        tests[pos++] = new Test("add/remove") {
            @Override
            public String test(int id) {
                Job job = new Job();
                
                boolean b = OAObjectInfoDelegate.isPrimitiveNull(job, "id");
                if (!b) {
                    return "failed#1";
                }
                delay();

                job.setId(id);
                b = OAObjectInfoDelegate.isPrimitiveNull(job, "id");
                if (b) {
                    return "failed#2";
                }
                delay();
                
                String prop = "hourly";
                b = OAObjectInfoDelegate.isPrimitiveNull(job, prop);
                if (!b) {
                    return "failed#3";
                }
                delay();
                job.setProperty(prop, "true");
                b = OAObjectInfoDelegate.isPrimitiveNull(job, prop);
                if (b) {
                    return "failed#4";
                }
                job.setProperty(prop, null);
                b = OAObjectInfoDelegate.isPrimitiveNull(job, prop);
                if (!b) {
                    return "failed#5";
                }
                
                
                return null;
            }
        };
    
    }
    
    private boolean bStop;
    
    class TestThread extends Thread {
        int id;
        public TestThread(int id) {
            this.id = id;
        }
        public void run() {
            int max = 500;
            int rootId = id * max;
            for (int i=0; i<max && !bStop; i++) { 
                if ((i+1)%50==0) System.out.println(id+") "+(i+1));
                
                int x = (int) Math.random() * tests.length;
                
                Test test = tests[x];
                if (test == null) continue;
                String s = test.test(rootId + i);
                if (s != null) {
                    bStop = true;
                    System.err.println("Error, test="+test.name+", error="+s+", Thread.id="+id+", job.id="+(rootId+i));
                    break;
                }
            }
//            System.out.println("Done id="+id);
        }
    }
    
    public void test() {
        for (int i=0; i<20; i++) {
            Thread t = new TestThread(i+1);
            t.start();
        }
    }

    
    
    public static void main(String[] args) throws Exception {
        
        OAObjectInfoDelegateTest test = new OAObjectInfoDelegateTest();
        test.test();
    }
    
    
    
    
}
