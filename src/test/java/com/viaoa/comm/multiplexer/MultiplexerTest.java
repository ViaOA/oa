package com.viaoa.comm.multiplexer;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;


public class MultiplexerTest extends OAUnitTest {
    static final int[] Maxes = {
        1000000, 123000, 15000, 501, 232, 14, 1 
    };


    @Test
    public void test() throws Exception {
        
        MultiplexerServerTest stest = new MultiplexerServerTest();
        stest.test(Maxes.length);
        
        MultiplexerClientTest ctest = new MultiplexerClientTest();
        ctest.test(Maxes);
        
        Thread.sleep(1600);
        ctest.stop();
        
        for (int i=0; i<40; i++) {
            Thread.sleep(250);
            int x1 = ctest.getCount();
            int x2 = ctest.getRunningCount();
            if (x2 == 0) break;
            int x3 = stest.getRunningCount();
            int xx = 4;
            xx++;
        }

        stest.stop();
        
        int x = ctest.getCount();
        assertTrue("x="+x+", should be > 1000", x > 1000);
    }

    
    public static void main(String[] args) throws Exception {
        MultiplexerServerTest stest = new MultiplexerServerTest();
        stest.test(Maxes.length);
        
        MultiplexerClientTest ctest = new MultiplexerClientTest();
        ctest.test(Maxes);
        
        for (;;) {
            Thread.sleep(10000);
        }
    }
    

}
















