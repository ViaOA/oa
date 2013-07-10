/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
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
     */
    public DiscoveryServer(int serverPort, int clientPort) {
        LOG.config(String.format("serverPort=%d, clientPort=%d", serverPort, clientPort));
        this.portSend = serverPort;
        this.portReceive = clientPort;
        try {
            inetAddress = InetAddress.getLocalHost();
        }
        catch (Exception e) {
        }
    }

    public void setMessage(String msg) {
        this.msg = msg;
    }
    public String getMessage() {
        if (msg == null) {
            this.msg = inetAddress.getHostAddress();
        }
        return this.msg;
    }
    
    /**
     * Runs thread to send udp broadcast messages, and listen for discoveryClient requests. 
     */
    public void start() throws Exception {
        if (bStarted) return;
        LOG.fine("starting thread that will send out broadcast messages, and listen for discoveryClient msgs");
        bStarted = true;
        final int iStartStop = aiStartStop.incrementAndGet();
        
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    DiscoveryServer.this.run(iStartStop);
                }
                catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error: " + e);
                }
            }
        }, "Discovery_Server");
        t.start();
    }

    /**
     * Called by start, to listen for clients, and respond by sending out message.
     * @param iStartStop used to know when to stop.
     */
    protected void run(int iStartStop) throws Exception {
        byte[] bsReceive = new byte[1024];
        int amt = 8;
        for (int i = 0; bStarted && iStartStop == aiStartStop.get(); i++) {
            for (int j = 0; j < amt && bStarted && iStartStop == aiStartStop.get(); j++) {
                send();
                Thread.sleep(250);
            }
            if (sockReceive == null) {
                sockReceive = new DatagramSocket(portReceive);
            }
            DatagramPacket dpReceive = new DatagramPacket(bsReceive, bsReceive.length);
            sockReceive.receive(dpReceive);
            String s = new String(dpReceive.getData());
            LOG.fine("received client message: " + s);
            if (!shouldRespond(s)) amt = 0;
            else amt = 2;
        }
        LOG.config("thread stopped");
    }

    /**
     * callback method used to determine if a send message should go out for the given 
     * client message that was received.
     * @param msg message received from client "where are you"
     * @return true (default) if this server should broadcast a "here I am" message
     */
    public boolean shouldRespond(String msg) {
        return true;
    }
    
    public void stop() {
        bStarted = false;
        aiStartStop.getAndIncrement();
        LOG.config("stopping");
    }
    
    public void send() throws Exception {
        LOG.finer("Sending: " + getMessage());
        byte[] bsSend = getMessage().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(bsSend, bsSend.length, inetAddress, portSend);
        if (sockSend == null) {
//qqqqqqqqqqqqqq            
//was            sockSend = new DatagramSocket(portSend);
            sockSend = new DatagramSocket();
        }
        synchronized (sockSend) {
            sockSend.send(sendPacket);
        }
    }
    
    public static void main(String args[]) throws Exception {
        OALogUtil.consoleOnly(Level.FINEST, "com");
        DiscoveryServer ds = new DiscoveryServer(9998, 9999);
        ds.start();
        for (;;) Thread.sleep(10000);
    }
}
