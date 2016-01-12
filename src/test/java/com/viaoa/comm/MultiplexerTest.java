package com.viaoa.comm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.junit.Test;
import static org.junit.Assert.*;

import com.theicetest.tsactest.model.oa.*;
import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;

public class MultiplexerTest extends OAUnitTest {
    /*
    private final static int[] Maxes = {
        8000000, 200000, 500, 250000, 10000, 5, 20,
        10500, 1, 20,
        21234, 15, 20
    };
    */
    private final static int[] Maxes = {
        50, 1, 20,
        234, 15, 20, 2,3,4,5
    };
    
    @Test
    public void test() throws Exception {
        
        MultiplexerServerTest stest = new MultiplexerServerTest();
        stest.test(Maxes.length);
        
        MultiplexerClientTest ctest = new MultiplexerClientTest();
        ctest.test(Maxes);
        
        Thread.sleep(10000);
        ctest.stop();

        Thread.sleep(250);
        stest.stop();
        
        int x = ctest.getCount();
        assertTrue(x > 10);
        
        //for (;;)Thread.sleep(10000);
    }
    
    
    public static void main(String[] args) throws Exception {
        MultiplexerServerTest stest = new MultiplexerServerTest();
        stest.test(Maxes.length);
        
        MultiplexerClientTest ctest = new MultiplexerClientTest();
        ctest.test(Maxes);
        
        for (;;)Thread.sleep(10000);
    }
    

}
















