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
package com.viaoa.comm.multiplexer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.io.VirtualServerSocket;
import com.viaoa.comm.multiplexer.io.MultiplexerServerSocketController;

/**
 * Used for creating a multiplexed ServerSockets, so that a client can have multiple 
 * "virtual" server sockets through a single real socket.
 * <p>
 * The MultiplexerServer can be used to have multiple serversockets that a MultiplexerClient can then make
 * many connections through a single real socket. <br>
 * This is useful for situations where multiple real connections are undesired because of
 * routing/loadbalance and connection management issues.
 * <p>
 * An MultiplexerClient can then have a single connection to a server and then have multiple connections
 * through this connection.
 * 
 * @author vvia
 */
public class MultiplexerServer {
    private static Logger LOG = Logger.getLogger(MultiplexerServer.class.getName());

    /**
     * Server port.
     */
    private int _port;

    /**
     * Server host/ip.
     */
    private String _host;

    /**
     * Internal flag used to know when connections can be accepted.
     */
    private boolean _bAllowConnections;

    
    /**
     * The single/only "Real" serversocket that is accepting new connections in behalf of other
     * VServerSockets (virtual server sockets).
     */
    private ServerSocket _serverSocket;

    /**
     * Used by server, to manage the "real" ServerSocket that receives new client connections. Within
     * each real socket will be new "virtual" socket connections that will be forwarded to the correct
     * ServerSocket.accept method.
     */
    private MultiplexerServerSocketController _controlServerSocket;

    /**
     * Message to display if server receives an invalid client connection (not from a Multiplexer Socket)
     */
    private String _invalidConnectionMessage;

    /**
     * Creates a new server that allows for multiplexing many client connections over a single real
     * connection.
     * 
     * @param host
     *            name
     * @param port
     *            port number to connect to
     * @see #start() call start to allow new connections from clients.
     */
    public MultiplexerServer(String host, int port) {
        try {
            if (host == null) host = InetAddress.getLocalHost().getHostAddress();
        }
        catch (Exception e) {
        }
        this._host = host;
        this._port = port;
        LOG.fine("host=" + host + ", port=" + port);
    }
    public MultiplexerServer(int port) {
        this(null, port);
    }

    /**
     * Used to set the limit on the number of bytes that can be written per second (in MB).  
     * @see MultiplexerOutputStreamController#
     */
    public void setThrottleLimit(int mbPerSecond) throws Exception {
        getServerSocketController().setThrottleLimit(mbPerSecond);
    }
    public int getThrottleLimit() throws Exception {
        return getServerSocketController().getThrottleLimit();
    }
    
    /**
     * This must be called to enable serverSocket to begin accepting new connections.
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        if (_bAllowConnections) return;
        LOG.fine("starting");
        _bAllowConnections = true;

        // create the real ServerSocket
        _serverSocket = new ServerSocket(this._port);

        getServerSocketController().start(_serverSocket);
        LOG.fine("start completed");
    }

    public void stop() throws Exception {
        getServerSocketController().close();
    }
    
    /**
     * @return true if serverSocket is accepting new connnections.
     */
    public boolean isStarted() {
        return this._bAllowConnections;
    }

    /**
     * Creates a serverSocket (virtual) through the real socket. This can then be used to accept new
     * socket connections from a MultiplexerClient.
     * 
     * @param serverSocketName
     *            unique name that will be used by clients when creating an vsocket.
     * @return new ServerSocket that can be used to accept new socket connections.
     */
    public VirtualServerSocket createServerSocket(String serverSocketName) throws IOException {
        LOG.fine("serverSocketName=" + serverSocketName);
        VirtualServerSocket ss;
        try {
            ss = getServerSocketController().getServerSocket(serverSocketName);
        }
        catch (Exception e) {
            throw new IOException("Exception while creating server socket.", e);
        }
        return ss;
    }

    /**
     * Creates a single controller that manages the server sockets.
     */
    private MultiplexerServerSocketController getServerSocketController() throws Exception {
        if (_controlServerSocket == null) {
            LOG.fine("creating single serverSocket controller, to manage all real socket connections");
            // create controller for server sockets
            _controlServerSocket = new MultiplexerServerSocketController() {
                @Override
                public String getInvalidConnectionMessage() {
                    return MultiplexerServer.this.getInvalidConnectionMessage();
                }

                @Override
                public void onClientDisconnet(int connectionId) {
                    MultiplexerServer.this.onClientDisconnect(connectionId);
                }

                @Override
                public void onClientConnect(Socket socket, int connectionId) {
                    MultiplexerServer.this.onClientConnect(socket, connectionId);
                }
            };
        }
        return _controlServerSocket;
    }

    /**
     * Server listen port.
     */
    public int getPort() {
        return _port;
    }

    /**
     * Host name.
     */
    public String getHost() {
        return _host;
    }

    /**
     * Message to display when a non-socket connects. This message will be sent to client, followed
     * by a disconnect.
     */
    public void setInvalidConnectionMessage(String msg) {
        LOG.fine("InvalidConnectionMessage=" + msg);
        this._invalidConnectionMessage = msg;
    }

    public String getInvalidConnectionMessage() {
        return _invalidConnectionMessage;
    }

    /**
     * Called when a real socket is disconnected.
     */
    protected void onClientDisconnect(int connectionId) {
        LOG.fine("connectionId=" + connectionId);
    }

    /**
     * Called when a real socket connection is made.
     */
    protected void onClientConnect(Socket socket, int connectionId) {
        LOG.fine("connectionId=" + connectionId);
    }
}
