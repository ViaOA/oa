package com.viaoa.comm.multiplexer;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.comm.multiplexer.io.VirtualSocket;
import com.viaoa.comm.multiplexer.io.MultiplexerSocketController;

/**
 * Creates multiplexed sockets over a single socket. This is used so that a client can have multiple
 * socket connections to multiple "virtual" server sockets through a single real socket.
 * <p>
 * An Client can then have a single connection to a server and then have multiple virtual socket
 * connections through this connection.
 * 
 * @see #start() to create the connection to the server.
 * @author vvia
 */
public class MultiplexerClient {
    private static Logger LOG = Logger.getLogger(MultiplexerClient.class.getName());

    /**
     * Server port to connect on.
     */
    private int _port;
    /**
     * Server host/ip to connect to.
     */
    private String _host;

    /**
     * Internal flag that is set to true once the create method has been called.
     */
    private boolean _bCreated;

    /**
     * Real socket to server.
     */
    private Socket _socket;

    /**
     * Controls the real socket, and manages the multiplexed "virtual" sockets.
     */
    private MultiplexerSocketController _controlSocket;

    // used by multiplexerOutputStream
    private int mbThrottleLimit;

    private Thread keepAliveThread;
    private int keepAliveSeconds;
    
    /**
     * Create a new client.
     * 
     * @param uri
     *            URI, that includes host and port
     * @see #MultiplexerClient(String, int)
     */
    public MultiplexerClient(URI uri) throws Exception {
        this(uri.getHost(), uri.getPort());
        LOG.fine("uri=" + uri);
    }

    /**
     * Create a new client.
     * 
     * @param host
     *            ip or name of server to connect to.
     * @param port
     *            port number of the server to connect to.
     */
    public MultiplexerClient(String host, int port) {
        LOG.fine("host=" + host + ", port=" + port);
        this._host = host;
        this._port = port;
    }

    /**
     * Used to create the connection to the MultiplexerServer.
     */
    public void start() throws Exception {
        if (_bCreated) return;
        LOG.fine("creating real socket, setting tcpNoDelay=true");

        _socket = new Socket(_host, _port);
        _socket.setTcpNoDelay(true);
        _bCreated = true;

        _controlSocket = new MultiplexerSocketController(_socket) {
            protected void onSocketException(Exception e) {
                MultiplexerClient.this.onSocketException(e);
            };
        };
        setThrottleLimit(this.mbThrottleLimit);
        runKeepAliveThread();        
    }

    /**
     * Called when there is a socket exception
     */
    protected void onSocketException(Exception e) {
    }

    public void setKeepAlive(int seconds) {
        this.keepAliveSeconds = seconds;
        if (seconds < 1) return;
        if (keepAliveThread == null && _bCreated) {
            runKeepAliveThread();
        }
    }
    public int getKeepAlive() {
        return keepAliveSeconds;
    }
    
    public void runKeepAliveThread() {
        if (keepAliveSeconds < 1) return;
        if (keepAliveThread != null) return;
        keepAliveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                long msLast = 0;
                Thread threadHold = keepAliveThread;
                for (;;) {
                    try {
                        if (keepAliveSeconds < 1) break;
                        if (threadHold != keepAliveThread) break;

                        long msNow = System.currentTimeMillis();
                        if (_controlSocket != null) {
                            msLast = Math.max(msLast, _controlSocket.getInputStreamController().getLastReadTime());
                        }
                        if (msLast < 1) msLast = msNow;

                        long msWait = (keepAliveSeconds * 1000) - (msNow - msLast);
                        if (msWait > 0) {
                            Thread.sleep(msWait);
                        }
                        else {
                            pingServer();
                            msLast = System.currentTimeMillis();
                        }
                    }
                    catch (Exception e) {
                        if (isConnected()) {
                            LOG.log(Level.WARNING, "", e);
                        }
                        break;
                    }
                }
                MultiplexerClient.this.keepAliveThread = null;
            }
        }, "MultiplexerClient.keepalive");
        keepAliveThread.setDaemon(true);
        keepAliveThread.start();
    }
    public void pingServer() throws Exception {
        if (_controlSocket != null) {
            _controlSocket.getOutputStreamController().sendPingCommand();
        }
    }
    
    
    /**
     * Used to set the limit on the number of bytes that can be written per second (in MB).  
     * @see MultiplexerOutputStreamController#
     */
    public void setThrottleLimit(int mbPerSecond) {
        mbThrottleLimit = mbPerSecond;
        if (_controlSocket != null) {
            _controlSocket.getOutputStreamController().setThrottleLimit(mbThrottleLimit);
        }
    }
    public int getThrottleLimit() {
        if (_controlSocket != null) {
            mbThrottleLimit = _controlSocket.getOutputStreamController().getThrottleLimit();
        }
        return mbThrottleLimit;
    }

    
    /**
     * Create a socket to a VServerSocket. The server socket must be created on the server first.
     * 
     * @param serverSocketName
     *            the name given to the socket when creating in MultiplexerServer.
     */
    public VirtualSocket createSocket(String serverSocketName) throws IOException {
        LOG.fine("creating new socket, name=" + serverSocketName);
        VirtualSocket vs = _controlSocket.createSocket(serverSocketName);
        return vs;
    }

    /**
     * Close the "real" socket to server.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        LOG.fine("closing real socket");
        if (_controlSocket != null) {
            _controlSocket.close();
        }
    }


    /**
     * The connectionId that has been assigned by the server.
     */
    public int getConnectionId() {
        if (_controlSocket == null) return -1;
        return _controlSocket.getId();
    }
    
    /**
     * Returns true if real socket is connected to server.
     */
    public boolean isConnected() {
        if (_controlSocket == null) return false;
        try {
            return !_controlSocket.isClosed();
        }
        catch (Exception e) {
        }
        return false;
    }

    /**
     * Get the "real" socket.
     */
    public Socket getSocket() {
        return _socket;
    }

    /**
     * Port used for connecting to the server.
     */
    public int getPort() {
        return _port;
    }

    /**
     * Server host name or ip address for the Server.
     */
    public String getHost() {
        return _host;
    }

}
