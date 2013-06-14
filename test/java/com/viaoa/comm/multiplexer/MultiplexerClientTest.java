package com.viaoa.comm.multiplexer;

import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import com.viaoa.comm.multiplexer.MultiplexerClient;


public class MultiplexerClientTest {
    private static Logger LOG = Logger.getLogger(MultiplexerClientTest.class.getName());
    
    public void test() throws Exception {
        LOG.config("Starting");
        MultiplexerClient client = new MultiplexerClient("localhost", 1099) {
            @Override
            protected void onSocketException(Exception e) {
                super.onSocketException(e);
            }
        };
        client.start();
        
        for (int i=0; i<25; i++) {
            Socket socket = client.createSocket("test");
            onNewConnection(socket, i+1);
        }
    }
    
    
    protected void onNewConnection(final Socket socket, final int id) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    runTests(socket, id);
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "error in socket testing", e);
                }
            }
        };
        // t.setDaemon(true);
        t.start();
    }
    
    protected void runTests(final Socket socket, final int id) throws Exception {
        InputStream is = socket.getInputStream();
        DataInputStream dis = new DataInputStream(is);
        OutputStream os = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os); 
        for (int i=1;;i++) {
            String s = dis.readUTF();
            LOG.fine(String.format("%d) clientId=%d, size=%d", i, id, s.length()));
            dos.writeUTF(s);
        }
    }
    
    
    public static void main(String[] args) throws Exception {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        Logger log = Logger.getLogger("com.viaoa");
        log.setLevel(Level.FINE);
        log.addHandler(ch);
        
        MultiplexerClientTest test = new MultiplexerClientTest();
        test.test();
    }

}
