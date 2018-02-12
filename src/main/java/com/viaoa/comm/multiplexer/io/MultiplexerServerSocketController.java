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
package com.viaoa.comm.multiplexer.io;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Manages multiplexed server socket connections. This will use a "real" ServerSocket to accept new
 * "real" client socket connections. The real connections are then managed by a
 * MultiplexerSocketController so that new sockets can be multiplexed inside the "real" socket.
 * 
 * @author vvia
 */
public class MultiplexerServerSocketController {
    private static Logger LOG = Logger.getLogger(MultiplexerServerSocketController.class.getName());

    /**
     * used to wait on new MultiplexerServerSocket connections
     */
    private Object ACCEPTLOCK = new Object();

    /**
     * used by timeout thread to check for connections that have been timedout and disconnect them.
     */
    private final Object TIMEOUTLOCK = new Object();

    /**
     * "real" serversocket used for accepting "real" client connections.
     */
    private ServerSocket _serverSocket;

    /**
     * next client socket connection for a vserversocket to accept.
     */
    private volatile VirtualSocket _nextSocket;

    /**
     * name of next client socket connection for a vserversocket to accept.
     */
    private volatile String _nextServerSocketName;

    /**
     * sequential counter used for assigning id number to socketcontrollers.
     */
    private int _cntSocketController; // note: first connection needs to start at "1", since server is assumed to be "0"

    /**
     * total valid sockets created.
     */
    private int _totalValidSocketsCreated;

    /**
     * Thread that listens for new client connections, named: "MultiplexerServerSocket.Accept"
     */
    private Thread _threadAccept;

    /**
     * Thread that listens for new client connections, named: "MultiplexerServerSocket.timeout"
     */
    private Thread _threadTimeout;

    /**
     * flag to know if serversocket is closed.
     */
    private boolean _bClosed;

    /**
     * List of all client connections that are live, each managed by MultiplexerSocketController.
     */
    private ArrayList<MultiplexerSocketController> _alSocketController = new ArrayList<MultiplexerSocketController>();

    /**
     * Collection of serverSockets that are using accept() to get new client connections.
     */
    private Hashtable<String, VirtualServerSocket> hashServerSocketName = new Hashtable<String, VirtualServerSocket>();

    /**
     * Message to display if server receives an invalid client connection (not from an
     * MultiplexerSocket)
     */
    private String _invalidConnectionMessage;

    /**
     * Created by MultiplexerServer to manage a "real" serversocket, to accept client connections
     * created by MultiplexerClient.
     */
    public MultiplexerServerSocketController() {
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

    public synchronized void start(ServerSocket ss) {
        if (ss == null) throw new IllegalArgumentException("ServerSocket can not be null");

        if (_threadAccept != null) return;
        this._serverSocket = ss;

        // create a thread to accept all new connections.
        _threadAccept = new Thread(new Runnable() {
            // @Override
            public void run() {
                MultiplexerServerSocketController.this.acceptConnections();
            }
        }, "MultiplexerServerSocket.Accept");
        _threadAccept.setDaemon(true);
        _threadAccept.start();

        // thread to timeout connections
        _threadTimeout = new Thread(new Runnable() {
            // @Override
            public void run() {
                MultiplexerServerSocketController.this.timeoutConnections();
            }
        }, "MultiplexerServerSocket.timeout");
        _threadTimeout.setDaemon(true);
        _threadTimeout.setPriority(Thread.MIN_PRIORITY);
        _threadTimeout.start();
    }

    /**
     * Accepts new connections for Server and create an MultiplexerSocketController to manage the
     * connection.
     */
    private void acceptConnections() {
        for (int i = 0; !_bClosed; i++) {
            try {
                Socket socket = this._serverSocket.accept();
                if (_bClosed) continue;
                onAcceptRealClientConnection(socket);
            }
            catch (Exception e) {
                LOG.finer("MultiplexerServerSocketController: exception while accepting new connections, ex="+ e);
            }
        }
    }

    // used by multiplexerOutputStream
    private int mbThrottleLimit;

    /**
     * Used to set the limit on the number of bytes that can be written per second (in MB).  
     */
    public void setThrottleLimit(int mbPerSecond) {
        LOG.config("new value="+mbPerSecond);
        mbThrottleLimit = mbPerSecond;
    }
    public int getThrottleLimit() {
        return mbThrottleLimit;
    }
    
    
    /**
     * This is internally called when a new "real" client socket connection is accepted on the server.
     * This will create a MultiplexerSocketController (ISC) to manage the connection. The ISC will then
     * create MultiplexerSockets, which will then be "given" to MultiplexerServerSocket accept(). This
     * method is thread safe, since only one accept() can be done at a time.
     */
    protected void onAcceptRealClientConnection(final Socket socket) throws IOException {
        _cntSocketController++;  

        final int connectionId = _cntSocketController;

        /**
         * This will create a new SocketController that will manage the new connection. Methods are
         * overwritten to so that new MultiplexerSocket connections can be given to the correct
         * MultiplexerServerSocket.
         */
        MultiplexerSocketController sc = new MultiplexerSocketController(socket, connectionId) {
            @Override
            protected boolean verifyServerSideHandshake() throws IOException {
                boolean b = super.verifyServerSideHandshake();
                if (b) {
                    _totalValidSocketsCreated++;
                    onClientConnect(socket, connectionId);
                }
                return b;
            }
            @Override
            protected VirtualSocket createSocket(int connectionId, int id, String serverSocketName) throws IOException {
                // The Multiplexer sockets that are created on the client need to be sent to the
                // vserversocket accept().
                VirtualServerSocket serverSocket = hashServerSocketName.get(serverSocketName);
                if (serverSocket == null) {
                    LOG.warning("serverSocket not found, socketName=" + serverSocketName);
                    return null; // invalid request
                }

                VirtualSocket vs = super.createSocket(connectionId, id, serverSocketName);

                synchronized (ACCEPTLOCK) {
                    for (;;) {
                        if (_nextSocket == null) {
                            _nextServerSocketName = serverSocketName;
                            _nextSocket = vs;
                            ACCEPTLOCK.notifyAll(); // will give to MultiplexerServerSocket that is
                                                    // waiting on accept()
                            return vs;
                        }
                        try {
                            ACCEPTLOCK.wait();
                        }
                        catch (Exception e) {
                        }
                    }
                }
            }

            @Override
            protected void close(boolean error) throws IOException {
                super.close(error);
                MultiplexerServerSocketController.this.remove(this);
            }

            @Override
            public String getInvalidConnectionMessage() {
                return MultiplexerServerSocketController.this.getInvalidConnectionMessage();
            }
        };
        sc.getOutputStreamController().setThrottleLimit(getThrottleLimit());
        
        // add the socketcontroller to list.
        add(sc);
    }

    /**
     * Used by Thread to check new connections to make sure that they are valid connections. Each new
     * connection is given 5 seconds to be validated by MultiplexerSocketController.
     */
    protected void timeoutConnections() {
        for (; !_bClosed;) {
            boolean bFound = _timeoutConnections();
            try {
                if (!bFound) {
                    synchronized (TIMEOUTLOCK) {
                        TIMEOUTLOCK.wait();
                    }
                }
                if (!_bClosed) {
                    Thread.sleep(5000);
                }
            }
            catch (Exception e) {
            }
        }
    }
    private boolean _timeoutConnections() {
        MultiplexerSocketController[] scs = getSocketControllers();
        long msNow = System.currentTimeMillis();
        boolean bFound = false;
        for (MultiplexerSocketController sc : scs) {
            try {
                if (!sc.isValid()) {
                    long ms = sc.getStartTimeMS();
                    if (ms > 0 && ((msNow - ms) > 5000)) {
                        LOG.fine("MultiplexerServerSocketController: connection timeout, closing now, Id="+sc.getId());
                        sc.close(true);
                    }
                    else {
                        bFound = true;
                    }
                }
            }
            catch (Exception e) {
            }
        }
        return bFound;
    }

    /**
     * Adds a new SocketController to list.
     */
    protected void add(MultiplexerSocketController vsc) {
        aiCreatedConnectionCnt.incrementAndGet();
        synchronized (_alSocketController) {
            _alSocketController.add(vsc);
        }
        synchronized (TIMEOUTLOCK) {
            TIMEOUTLOCK.notify();
        }
    }

    
    
    /**
     * Removes a SocketController from list.
     */
    protected void remove(MultiplexerSocketController vsc) {
        if (vsc == null) return;
        
        aiRemovedReadCnt.addAndGet(vsc.getInputStreamController().getReadCount());
        aiRemovedReadSize.addAndGet(vsc.getInputStreamController().getReadSize());

        aiRemovedWriteCnt.addAndGet(vsc.getOutputStreamController().getWriteCount());
        aiRemovedWriteSize.addAndGet(vsc.getOutputStreamController().getWriteSize());
        
        boolean b;
        synchronized (_alSocketController) {
            b = _alSocketController.remove(vsc) && vsc.isValid();
        }
        if (b) {
            onClientDisconnect(vsc.getId());
        }
    }

    /**
     * @return list of all client connections.
     */
    protected MultiplexerSocketController[] getSocketControllers() {
        MultiplexerSocketController[] vscs;
        synchronized (_alSocketController) {
            vscs = new MultiplexerSocketController[_alSocketController.size()];
            _alSocketController.toArray(vscs);
        }
        return vscs;
    }

    /**
     * Create a new ServerSocket that will accept new client MultiplexerSockets through a multiplexed
     * connection. This is used by MultiplexerServer to create new server sockets.
     */
    public VirtualServerSocket getServerSocket(final String serverSocketName) throws IOException {
        if (serverSocketName == null || serverSocketName.length() == 0) return null;

        VirtualServerSocket serverSocket = hashServerSocketName.get(serverSocketName);
        if (serverSocket != null) {
            return serverSocket;
        }

        // create the server socket that will accept new client connections, through the multiplexed
        // connection.
        serverSocket = new VirtualServerSocket(serverSocketName) {
            @Override
            public Socket accept() throws IOException {
                synchronized (ACCEPTLOCK) {
                    for (;;) {
                        if (_nextSocket != null && _nextServerSocketName != null && _nextServerSocketName.equals(serverSocketName)) {
                            VirtualSocket sock = _nextSocket;
                            _nextSocket = null;
                            _nextServerSocketName = null;
                            ACCEPTLOCK.notifyAll();
                            return sock;
                        }
                        try {
                            // this will wait until a new client connection has been requested through a
                            // client multiplexed socket.
                            ACCEPTLOCK.wait();
                        }
                        catch (Exception e) {
                        }
                    }
                }
            }
        };
        hashServerSocketName.put(serverSocketName, serverSocket);

        return serverSocket;
    }

    /**
     * Close all client connections.
     */
    public void close() throws Exception {
        LOG.fine("closing all connections");
        _bClosed = true;
        MultiplexerSocketController[] vscs = getSocketControllers();
        for (MultiplexerSocketController vsc : vscs) {
            try {
                vsc.close();
            }
            catch (Exception e) {
            }
        }
        if (_serverSocket != null) _serverSocket.close();
    }

    public void onClientConnect(Socket socket, int connectionId) {
    }

    public void onClientDisconnect(int connectionId) {
    }



    
    // 20160202
    private AtomicLong aiRemovedReadCnt = new AtomicLong();
    private AtomicLong aiRemovedReadSize = new AtomicLong();
    
    private AtomicLong aiRemovedWriteCnt = new AtomicLong();
    private AtomicLong aiRemovedWriteSize = new AtomicLong();
    
    private AtomicInteger aiCreatedConnectionCnt = new AtomicInteger();
    
    /**
     * @return number of writes made.
     */
    public long getWriteCount() {
        long cnt = aiRemovedWriteCnt.get();
        for (MultiplexerSocketController sc : _alSocketController) {
            cnt += sc.getOutputStreamController().getWriteCount();
        }
        return cnt;
    }
    /*
     * size of data that has been written.
     */
    public long getWriteSize() {
        long size = aiRemovedWriteSize.get();
        for (MultiplexerSocketController sc : _alSocketController) {
            size += sc.getOutputStreamController().getWriteSize();
        }
        return size;
    }

    
    /**
     * @return number of reads made.
     */
    public long getReadCount() {
        long cnt = aiRemovedReadCnt.get();
        for (MultiplexerSocketController sc : _alSocketController) {
            cnt += sc.getInputStreamController().getReadCount();
        }
        return cnt;
    }
    /*
     * size of data that has been read.
     */
    public long getReadSize() {
        long size = aiRemovedReadSize.get();
        for (MultiplexerSocketController sc : _alSocketController) {
            size += sc.getInputStreamController().getReadSize();
        }
        return size;
    }

    public int getCreatedConnectionCount() {
        return aiCreatedConnectionCnt.get();
    }
    public int getLiveConnectionCount() {
        if (_alSocketController == null) return 0;
        return _alSocketController.size();
    }
    
}
