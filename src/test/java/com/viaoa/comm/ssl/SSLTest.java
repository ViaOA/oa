package com.viaoa.comm.ssl;

import static org.junit.Assert.assertTrue;

import java.util.logging.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.viaoa.OAUnitTest;


/**
 * Sample app that creates a server and client threads that read/write data using ssl
 * @author vvia
 *
 */
public class SSLTest extends OAUnitTest {
    private static Logger LOG = Logger.getLogger(SSLTest.class.getName());
    
    SSLServer server;
    SSLClient client;
    volatile boolean bStop;
    volatile int cntServer, cntClient;

    @Before
    public void setup() throws Exception {
        server = new SSLServer("localhost", 1101) {
            @Override
            protected void sendOutput(byte[] bs, int offset, int len, boolean bHandshakeOnly) throws Exception {
                client.input(bs, len, bHandshakeOnly);
            }
        };
        server.initialize();
        
        client = new SSLClient("localhost", 1101) {
            @Override
            protected void sendOutput(byte[] bs, int offset, int len, boolean bHandshakeOnly) throws Exception {
                server.input(bs, len, bHandshakeOnly);
            }
        };
        client.initialize();
        test();
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("Stopping SSLTest");
        bStop = true;
    }
    
    
    public void test() throws Exception {
        System.out.println("Starting SSLTest");
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runServer();
                }
                catch (Exception e) {
                    System.out.println("runServer exception "+e);
                    e.printStackTrace();
                }
            }
        });
        t.start();

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runClient();
                }
                catch (Exception e) {
                    System.out.println("runServer exception "+e);
                    e.printStackTrace();
                }
            }
        });
        t2.start();
    }
    private void runServer() throws Exception {
        for (; !bStop; cntServer++) {
            byte[] bs = ("server."+cntServer).getBytes();
            System.out.println(cntServer+") server");            
            //System.out.println("server: sending output");            
            server.output(bs, 0, bs.length);
            //System.out.println("server: getting input");            
            byte[] bs2 = server.input();
            new String(bs2);
            
            if (cntServer % 500 == 0) server.resetSSL();  // test that causes ssl to re-handshake
        }
    }
        
    private void runClient() throws Exception {
        for (; !bStop; cntClient++) {
            System.out.println(cntClient+") client");            
            //System.out.println("client: getting input");            
            byte[] bs2 = client.input();
            new String(bs2);
            

            //System.out.println("client: sending output");            
            byte[] bs = ("client."+cntClient).getBytes();
            client.output(bs, 0, bs.length);
            
            if (cntClient % 319 == 0) server.resetSSL();
        }
    }
    
    @Test
    public void monitor() throws Exception {
        Thread.sleep(1000 * 10);
        bStop = false;
        assertTrue(cntClient > 10);
        assertTrue(cntServer > 10);
    }

    
    
    
    public static void main(String[] args) throws Exception {
        SSLTest test = new SSLTest();
        test.setup();
        test.test();
        for (;;) Thread.sleep(10 * 1000);
    }
}
