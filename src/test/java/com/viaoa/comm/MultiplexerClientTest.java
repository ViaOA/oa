package com.viaoa.comm;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import static org.junit.Assert.*;

import com.theicetest.tsactest.model.oa.*;
import com.viaoa.OAUnitTest;
import com.viaoa.comm.multiplexer.MultiplexerClient;

public class MultiplexerClientTest extends OAUnitTest {
    private AtomicInteger aiCount = new AtomicInteger();
    
    public void test(int ... msgSizes) throws Exception {
        final MultiplexerClient mc = new MultiplexerClient("localhost", 1101);
        mc.start();
        
        mc.DEBUG = true;
        
//Thread.sleep(5000);

        for (int i=0; i<msgSizes.length; i++) {
            final int id = i;
            final int msgSize = msgSizes[i];
            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        _test(id, mc, msgSize);
                    }
                    catch (Exception e) {
                        System.out.println("Exception with client virtual socket, exception="+e);
                        e.printStackTrace();
                    }
                }
            };
            t.setName("Thread."+i+".value."+msgSize);
            t.start();
        }
    }
    
    private volatile boolean bStopCalled;
    public void stop() {
        bStopCalled = true;
    }
    
    
    public void _test(final int id, final MultiplexerClient mc, final int msgSize) throws Exception {
        final Socket socket = mc.createSocket("test");
        
        InputStream is = socket.getInputStream();
        OutputStream os = socket.getOutputStream();
        
        DataInputStream dis = new DataInputStream(is);
        DataOutputStream dos = new DataOutputStream(os);
        
        // BufferedOutputStream bos = new BufferedOutputStream(dos); 
        
        long tot = 0;
        byte[] bs = new byte[msgSize];
        dos.writeInt(msgSize);
        
        for (int i=0; !bStopCalled ;i++) {
            dos.write(bs);

//            int x = dis.readInt();
//            dis.read(bs,0,1);
            
/*qqq             
            int x = dis.readInt();
            dis.readFully(bs);
*/
            tot += msgSize;
            if (i % 1000 == 0) {
                System.out.println("client, id="+id+", cnt="+i+", bs="+msgSize+", totBytes="+tot);
            }
            
            aiCount.incrementAndGet();
        }
        socket.close();
    }
    
    public int getCount() {
        return aiCount.get();
    }
    

    public static void main(String[] args) throws Exception {
        MultiplexerClientTest test = new MultiplexerClientTest();
        test.test(25);
    }
}
