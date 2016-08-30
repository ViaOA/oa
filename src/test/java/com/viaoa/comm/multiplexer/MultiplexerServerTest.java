package com.viaoa.comm.multiplexer;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.util.OAString;

import test.theice.tsac3.model.oa.*;

public class MultiplexerServerTest extends OAUnitTest {
    private volatile boolean bStopCalled;
    private MultiplexerServer multiplexerServer;
    
    public void test(final int maxConnections) throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    _test(maxConnections);
                }
                catch (Exception e) {
                    System.out.println("Error in MultiplexerServerTest serverSocket");
                    e.printStackTrace();
                }
            }
        };
        t.setDaemon(true);
        t.start();
    }
    private void _test(int maxConnections) throws Exception {

        multiplexerServer = new MultiplexerServer(null, 1101);
        ServerSocket ss = multiplexerServer.createServerSocket("test");
        multiplexerServer.start();

        multiplexerServer.DEBUG = true;
        
        for (int i=0; (maxConnections==0 && i==0) || i<maxConnections; i++) {
            Socket s = ss.accept();
            test(s);
        }
    }
    public void stop() {
        bStopCalled = true;
        try {
            multiplexerServer.stop();
        }
        catch (Exception e) {
            // TODO: handle exception
        }
    }
    
    protected void test(final Socket socket) throws Exception {
        Thread t = new Thread() {
            @Override
            public void run() {
                _test(socket);
            }
        };
        t.setName("ServerTestThread.value.x");
        t.start();
    }

    protected void _test(final Socket socket) {
        try {
            aiRunnngCount.incrementAndGet();
            _test2(socket);
        }
        catch (Exception e) {
            System.out.println("Exception with server virtual socket, exception="+e);
            e.printStackTrace();
        }
        finally {
            aiRunnngCount.decrementAndGet();
        }
    }

    private AtomicInteger aiRunnngCount = new AtomicInteger();
    public int getRunningCount() {
        return aiRunnngCount.get();        
    }
    
    private int grandTotal;
    protected void _test2(final Socket socket) throws Exception {
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        
        DataInputStream dis = new DataInputStream(is);
        DataOutputStream dos = new DataOutputStream(os);

        long tot = 0;
        byte[] bs = null;
        for (int i=0; !bStopCalled; i++) {
            
if (i == 500) {
    int xx = 4;
    xx++;
}
            int x = dis.readInt();
            if (x < 0) break;
            if (i == 0) {
                Thread.currentThread().setName("ServerTestThread.value."+x);
            }
            if (bs == null) bs = new byte[x];

            dis.readFully(bs);
            tot += x;
            //System.out.println("server, cnt="+i+", totBytes="+tot);

            dos.writeInt(bs.length);
            dos.write(bs);
        }
    }

    public static void main(String[] args) throws Exception {
        MultiplexerServerTest test = new MultiplexerServerTest();
        test.test(0);
    }
}
