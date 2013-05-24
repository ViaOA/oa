package com.viaoa.comm.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.util.OALogUtil;

/**
 * Allows a server to broadcast it's availability to client servers.
 * The server listens for client servers broadcast on a separate "client" port, that will
 * trigger the server to send out broadcast messages.
 * 
 * @see DiscoveryClient 
 * @author vvia
 */
public class DiscoveryServer {
    private static Logger LOG = Logger.getLogger(DiscoveryServer.class.getName());
    private int portReceive;
    private int portSend;
    private DatagramSocket sockSend, sockReceive;
    private InetAddress inetAddress;
    private String msg;
    private volatile boolean bStarted;
    private AtomicInteger aiStartStop = new AtomicInteger();

    /**
     * 
     * @param serverPort port that the server will broadcast on.
     * @param clientPort port that client broadcasts on.
     * @param msg message to broadcast
     */
    public DiscoveryServer(int serverPort, int clientPort) {
        LOG.config(String.format("serverPort=%d, clientPort=%d", serverPort, clientPort));
        this.portSend = serverPort;
        this.portReceive = clientPort;
    }

    /**
     * Runs thread to send udp broadcast messages, and listen for discoveryClient requests. 
     */
    public void start() throws Exception {
        if (bStarted) return;
        LOG.fine("starting thread that will send out broadcast messages, and listen for discoveryClient msgs");
        bStarted = true;
        final int iStartStop = aiStartStop.incrementAndGet();

        if (sockSend == null) {
            sockSend = new DatagramSocket();
            inetAddress = InetAddress.getLocalHost();
            msg = inetAddress.getHostAddress();
        }
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DiscoveryServer.this.runSend(iStartStop);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: " + e);
                }
            }
        }, "Discovery_Server");
        t.start();
    }

    protected void runSend(int iStartStop) throws Exception {
        byte[] bsReceive = new byte[1024];
        for (int i = 0; bStarted && iStartStop == aiStartStop.get(); i++) {
            for (int j = 0; j < 8 && bStarted && iStartStop == aiStartStop.get(); j++) {
                send();
                Thread.sleep(250);
            }
            if (sockReceive == null) {
                sockReceive = new DatagramSocket(portReceive);
            }
            DatagramPacket dpReceive = new DatagramPacket(bsReceive, bsReceive.length);
            sockReceive.receive(dpReceive);
            String s = new String(dpReceive.getData());
            LOG.fine("received: " + s);
        }
        LOG.config("thread stopped");
    }

    public void stop() {
        bStarted = false;
        aiStartStop.getAndIncrement();
        LOG.config("stopping");
    }
    
    public synchronized void send() throws Exception {
        LOG.finer("Sending: " + msg);
        byte[] bsSend = msg.getBytes();
        DatagramPacket sendPacket = new DatagramPacket(bsSend, bsSend.length, inetAddress, portSend);
        sockSend.send(sendPacket);
    }
    
    public static void main(String args[]) throws Exception {
        OALogUtil.consoleOnly(Level.FINEST, "com");
        DiscoveryServer ds = new DiscoveryServer(9998, 9999);
        ds.start();
        for (;;) Thread.sleep(10000);
    }
}
