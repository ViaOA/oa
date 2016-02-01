package com.viaoa.comm.multiplexer;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;


public class MultiplexerTest extends OAUnitTest {
    static final int[] Maxes = {
        1200000, 200000, 15000, 501, 232, 14, 1 
    };


    @Test
    public void test() throws Exception {
        
        MultiplexerServerTest stest = new MultiplexerServerTest();
        stest.test(Maxes.length);
        
        MultiplexerClientTest ctest = new MultiplexerClientTest();
        ctest.test(Maxes);
        
        Thread.sleep(1000);
        ctest.stop();
        
        int x = ctest.getCount();
        assertTrue(x > 1000);
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
















