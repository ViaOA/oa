package com.viaoa.util;


public class CirculrQueueTest {

    OACircularQueue<Short> que;
    
    public CirculrQueueTest() {
        que = new OACircularQueue<Short>(35) {};
    }
    
    void runWriter() {
        for (int i=0;;i++) {
            Short st = new Short((short)i);
            if ((i%100)==0) System.out.printf("\n(W."+i+") ");
            // else if ((i%20)==0) System.out.printf( ((i%2==0)?'W':'w')+".");
            que.addMessageToQueue(st);
            try {
                Thread.sleep(2);
            }
            catch (Exception e) {}
        }
    }
    
    void runReader(int id) {
        long pos = que.getHeadPostion();
        for (int i=0;;i++) {
            try {
                Short[] sms = que.getMessages(pos);
                pos += sms == null ? 0 : sms.length;
                if ((i%20)==0) System.out.printf("[R"+id+"."+pos+"] ");
                Thread.sleep(32);
            }
            catch (Exception e) {
                System.out.println("runReader Exception: "+e);
                e.printStackTrace();
                break;
            }
        }
    }
    
    void runTests() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                runWriter();
            }
        });
        t.setName("Writer");
        t.start();
        
        for (int i=0; i<10; i++) {
            final int id = i;
            Thread tx = new Thread(new Runnable() {
                @Override
                public void run() {
                    runReader(id);
                }
            });
            tx.setName("Reader."+i);
            tx.start();
        }
    }

    
    public static void main(String[] args) {
        CirculrQueueTest test = new CirculrQueueTest();
        test.runTests();
    }
    
}
