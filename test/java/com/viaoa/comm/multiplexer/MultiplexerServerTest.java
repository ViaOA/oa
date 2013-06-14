package com.viaoa.comm.multiplexer;

import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.*;

import com.viaoa.comm.multiplexer.MultiplexerServer;
import com.viaoa.comm.multiplexer.io.VirtualServerSocket;

public class MultiplexerServerTest {
    private static Logger LOG = Logger.getLogger(MultiplexerServerTest.class.getName());

    public void test() throws Exception {
        LOG.config("Starting");
        MultiplexerServer server = new MultiplexerServer("localhost", 1099) {
            @Override
            protected void onClientConnect(Socket socket, int connectionId) {
                super.onClientConnect(socket, connectionId);
                LOG.fine("Client has been disconnected, id="+connectionId);                
            }
        };
        server.setThrottleLimit(100);
        server.start();
        
        VirtualServerSocket vss = server.createServerSocket("test");
        for (int i=1;;i++) {
            LOG.fine("waiting for client to connect");
            Socket socket = vss.accept();

            LOG.fine(String.format("new client connection #%d", i));
            onNewConnection(socket, i);
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
        t.setName("Test."+id);
        t.start();
    }
    
    protected void runTests(final Socket socket, final int id) throws Exception {
        InputStream is = socket.getInputStream();
        DataInputStream dis = new DataInputStream(is);
        OutputStream os = socket.getOutputStream();
        DataOutputStream dos = new DataOutputStream(os);
        int[] sizes = new int[] { 2, 20, 50, 150, 250, 400, 550, 1000, 1500, 5000, 10000, 50000};
        for (int i=1;;i++) {
            int pos = (i % sizes.length);
            int min = (pos==0) ? 0 : sizes[pos-1];
            String s = getRandomString(min, sizes[pos]);
            dos.writeUTF(s);
            s = dis.readUTF();
            LOG.fine(String.format("%d) clientId=%d, size=%d", i, id, s.length()));
        }
    }
    public String getRandomString(int min, int max) {
        String result = "";
        int x = min;
        if (min < max)  x += (int) (Math.random() * (max-min));
        
        for (int i=0; i<x; i++) {
            char ch = (char) (Math.random() * 26);
            ch += '0';
            result += ch;
        }
        return result;
    }
    
    
    public static void main(String[] args) throws Exception {
        Logger log = Logger.getLogger("com.viaoa");
        log.setLevel(Level.FINE);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.FINE);
        log.addHandler(ch);

        MultiplexerServerTest test = new MultiplexerServerTest();
        
        test.test();
    }
}
