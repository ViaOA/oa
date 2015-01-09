package com.viaoa.object;

import java.util.Vector;

import com.vetjobs.Job;

public class OAObjectCacheDelegateTest {

    private Test[] tests;
    
    public OAObjectCacheDelegateTest() throws Exception {
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
Vector<Job> vec = new Vector<Job>(1000);    
    void setup() {
        tests = new Test[1];
        
        int pos = 0;
        tests[pos++] = new Test("add/remove") {
            @Override
            public String test(int id) {
                Job job = new Job();
//vec.add(job);                
                Job jobx = (Job) OAObjectCacheDelegate.get(job);
                if (job != jobx) {
                    jobx = (Job) OAObjectCacheDelegate.get(job);
                    return "failed#1, job != jobx, jobx="+jobx;
                }
                delay();
                
                job.setId(id);
                jobx = (Job) OAObjectCacheDelegate.get(job);
                if (job != jobx) {
                    jobx = (Job) OAObjectCacheDelegate.get(job);
                    return "failed#2, job != jobx, jobx="+jobx;
                }
                delay();
                
                jobx = (Job) OAObjectCacheDelegate.get(Job.class, id);
                if (job != jobx) {
                    jobx = (Job) OAObjectCacheDelegate.get(Job.class, id);
                    return "failed#3, job != jobx, jobx="+jobx;
                }
                
                delay();
                OAObjectCacheDelegate.removeObject(job);
                jobx = (Job) OAObjectCacheDelegate.get(job);
                if (jobx != null) {
                    jobx = (Job) OAObjectCacheDelegate.get(job);
                    // OAObjectCacheDelegate.removeObject(job);
                    return "failed#4, jobx should be null";
                }
                
                delay();
                OAObjectCacheDelegate.add(job, true, false);
                jobx = (Job) OAObjectCacheDelegate.get(job);
                if (job != jobx) {
                    jobx = (Job) OAObjectCacheDelegate.get(job);
                    return "failed#5, job != jobx, jobx="+jobx;
                }

                OAObjectCacheDelegate.removeObject(job);
                jobx = (Job) OAObjectCacheDelegate.get(job);
                if (jobx != null) return "failed#6, jobx should be null";
                
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
            int max = 7500;
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
        
        OAObjectCacheDelegateTest test = new OAObjectCacheDelegateTest();
        test.test();
    }
    
    
    
    
}
