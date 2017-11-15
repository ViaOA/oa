/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
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

    public static boolean DEBUG = false;
    
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
     * Stop the serverSocket from accepting new connections.
     * Dont accept new client connections, but keep current client sockets connected
     */
    public void stopServerSocket() throws Exception {
        if (!_bAllowConnections || _serverSocket == null) return;
        LOG.fine("stopping");
        _bAllowConnections = false;
        _serverSocket.close();
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
                public void onClientDisconnect(int connectionId) {
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

    /**
     * @return number of reads made.
     */
    public long getReadCount() {
        if (_controlServerSocket == null) return 0;
        return _controlServerSocket.getReadCount(); 
    }
    /*
     * size of data that has been read.
     */
    public long getReadSize() {
        if (_controlServerSocket == null) return 0;
        return _controlServerSocket.getReadSize(); 
    }

    /**
     * @return number of writes made.
     */
    public long getWriteCount() {
        if (_controlServerSocket == null) return 0;
        return _controlServerSocket.getWriteCount(); 
    }
    /*
     * size of data that has been written.
     */
    public long getWriteSize() {
        if (_controlServerSocket == null) return 0;
        return _controlServerSocket.getWriteSize(); 
    }

    public int getCreatedConnectionCount() {
        if (_controlServerSocket == null) return 0;
        return _controlServerSocket.getCreatedConnectionCount();
    }
    public int getLiveConnectionCount() {
        if (_controlServerSocket == null) return 0;
        return _controlServerSocket.getLiveConnectionCount();
    }
    
}
