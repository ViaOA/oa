package com.viaoa.comm.discovery;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.util.OALogUtil;

/**
 * Used by clients to be able to find all available discoveryServers.
 * 
 * @see DiscoveryServer
 * @author vvia
 */
public class DiscoveryClient {
    private static Logger LOG = Logger.getLogger(DiscoveryClient.class.getName());
    private int portReceive;
    private int portSend;
    private DatagramSocket sockSend, sockReceive;
    private InetAddress inetAddress;
    // list of servers registered (host name)
    private HashSet<String> hmServer = new HashSet<String>();
    private volatile boolean bStarted;
    private AtomicInteger aiStartStop = new AtomicInteger();
    private String msg;

    /**
     * 
     * @param serverPort
     *            port that server will send udp broadcast messages "here I am" on.
     * @param clientPort
     *            port that clients will send broadcast message "where are you" to servers.
     */
    public DiscoveryClient(int serverPort, int clientPort) {
        LOG.config(String.format("serverPort=%d, clientPort=%d", serverPort, clientPort));
        this.portSend = clientPort;
        this.portReceive = serverPort;
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }
    public String getMessage() {
        if (msg == null) {
            try {
                inetAddress = InetAddress.getLocalHost();
                this.msg = inetAddress.getHostAddress();
            }
            catch (Exception e) {
            }
        }
        return this.msg;
    }
    
    /**
     * Runs thread to send udp broadcast messages, and listen for discoveryServer messages.
     */
    public void start() throws Exception {
        LOG.fine("starting thread that will send out broadcast message, and listen for discoveryServer broadcast msgs");
        bStarted = true;
        final int iStartStop = aiStartStop.incrementAndGet();
        sockSend = new DatagramSocket();
        inetAddress = InetAddress.getLocalHost();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DiscoveryClient.this.runReceive(iStartStop);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: " + e);
                }
            }
        }, "Discovery_Client");
        t.start();
    }

    
    protected void runReceive(int iStartStop) throws Exception {
        byte[] bsSend = getMessage().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(bsSend, bsSend.length, inetAddress, portSend);
        for (int j = 0; j < 4 && bStarted; j++) {
            sockSend.send(sendPacket);
            Thread.sleep(250);
        }

        byte[] bsReceive = new byte[1024];
        for (; bStarted && iStartStop == aiStartStop.get();) {
            if (sockReceive == null) {
                LOG.finer("Sending: "+getMessage());
                sockReceive = new DatagramSocket(portReceive);
            }
            DatagramPacket dpReceive = new DatagramPacket(bsReceive, bsReceive.length);
            sockReceive.receive(dpReceive);

            String serverMsg = new String(dpReceive.getData());
            LOG.finer("Received: " + serverMsg);

            if (!hmServer.contains(serverMsg)) {
                hmServer.add(serverMsg);
                onNewServerMessage(serverMsg);
            }

            /*
             * InetAddress ia = dpReceive.getAddress(); String s = ia.getHostAddress();
             * System.out.println("  hostAddress="+s+", port="+dpReceive.getPort());
             */
        }
        LOG.config("thread stopped");
    }

    public void stop() {
        bStarted = false;
        aiStartStop.getAndIncrement();
        LOG.config("stopping");
    }

    /**
     * This should be overwritten to capture all new servers.
     * @param serverHostName
     */
    public void onNewServerMessage(String serverMessage) {
        System.out.println("New Server Message: " + serverMessage);
    }

    public static void main(String args[]) throws Exception {
        OALogUtil.consoleOnly(Level.FINE, "com");
        DiscoveryClient ds = new DiscoveryClient(9998, 9999);
        ds.start();
        for (;;) Thread.sleep(10000);
    }
}
