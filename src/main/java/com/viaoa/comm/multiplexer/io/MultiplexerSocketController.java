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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Hashtable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Manages the "real" socket connection and the multiplexed MultiplexerSockets that use it. There will be an MultiplexerSocketController at both ends of a real socket: client and server. This class will manage the transport layer, encryption, compression and validation.
 * <p>
 * Each client will have one SC on the client, and a matching SC on the server. A server will have a SC for each "real" client connection, all managed by MultiplexerServerSocketController.
 * 
 * @author vvia
 */
public class MultiplexerSocketController implements Serializable {
    private static Logger LOG = Logger.getLogger(MultiplexerSocketController.class.getName());
    /**
     * Status of real connection.
     */
    public static final int STATUS_Running = 0;
    public static final int STATUS_DisconnetedByClient = 1;
    public static final int STATUS_DisconnetedByServer = 2;
    public static final int STATUS_DisconnectedByError = 3;

    /**
     * Internal commands between endpoints (client and server) for the connection.
     */
    static final int CMD_Command = 0; // flag to know that socket message is a command, and not client data
    static final int CMD_CloseRealSocket = 1; // command to have the real socket closed
    static final int CMD_CreateVSocket = 2; // sent to server from client to create a new MultiplexerSocket (virtual socket)
    static final int CMD_CloseVSocket = 3; // tells server to remove an MultiplexerSocket
    static final int CMD_Ping = 4;  // send ping, does not return a response 

    /**
     * flag to know if this ISC is for a client connection, else it is for a server connection.
     */
    private boolean _bIsClient;

    /**
     * sequential id assigned by MultiplexerServerSocketController.
     */
    private int _connectionId;

    /**
     * "real" socket, shared by vsockets.
     */
    private transient Socket _socket;

    /**
     * flag to know if the socket is valid. The server timeout thread will disconnect any invalid threads after 5 seconds.
     * 
     * @see MultiplexerServerSocketController#acceptConnections()
     */
    private boolean _bIsValid;

    /** start time/ms */
    private long _msStarted;
    /** thread that manages the "real" socket, named "MultiplexerSocketController" */
    private transient Thread _thread;
    /** boolean flag to know that close has been done. */
    private AtomicBoolean _abClosing = new AtomicBoolean(false);

    /** used to manage the real socket outputstream */
    private MultiplexerOutputStreamController _outputStreamController;
    
    /** used to manage the real socketinputstream */
    private MultiplexerInputStreamController _inputStreamController;
    private final Object _LockStreamController = new Object();

    /** max ID assigned to virtual socket. */
    private volatile int _maxSocketId;
    private final Object _LockMaxSocketId = new Object();

    /**
     * Used to assign VSocket.id Must be greater then zero.
     */
    private final AtomicInteger _aiSocketIdSeq = new AtomicInteger(1); // must not use 0, since 0 is used in the header for commands.

    /* *
     * Collection of all open MultiplexerSockets, by Id
     */
    private Hashtable<Integer, VirtualSocket> _hashtableSocket;

    private transient InetAddress _inetAddress;
    private int _status; // see STATUS_* above

    /** */
    static final String Signature = "MULTIPLEXER_SIGNATURE_B"; // unique signature expected from MultiplexerServerSocketController.accept()

    /**
     * Assigned on server to create a unique identifier for the server instance.
     */
    private static final String _threadName = "MultiplexerSocketController";

    /**
     * Message to display if server receives an invalid client connection (not from an MultiplexerSocket)
     */
    private String _invalidConnectionMessage;

    /**
     * Used by client to create a new multiplexed socket.
     */
    public MultiplexerSocketController(Socket socket) throws Exception {
        if (socket == null) throw new IllegalArgumentException("socket can not be null");
        this._bIsClient = true;
        this._socket = socket;

        // perform handshake/verification in this thread.
        try {
            performHandshake();
        }
        catch (SocketException e) {
            onSocketException(e);
            return;
        }
        catch (IOException e) {
            onSocketException(e);
            return;
        }

        // start the real socket reader thread.
        startSocketReaderThread(false);
    }

    /**
     * Created by MultiplexerServerSocketController for server-side connections. Note: needs to return as soon as possible, since this is executed on the serverSocket.accept() thread.
     * 
     * @param socket
     *            real connection to server.
     */
    protected MultiplexerSocketController(Socket socket, int id) {
        if (socket == null) throw new IllegalArgumentException("socket can not be null");
        this._bIsClient = false;
        this._socket = socket;
        this._connectionId = id;
        LOG.fine("new client connection, id="+id);
        // start real socket reader thread, and have it validate/handshake in that thread.
        startSocketReaderThread(true);
    }

    private void startSocketReaderThread(final boolean bPerformHandshake) {
        _thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (bPerformHandshake) {
                        performHandshake(); // this will throw and exception if invalid handshake
                    }
                    // this will run the real socket read loop.
                    MultiplexerSocketController.this.getInputStreamController().readRealSocketLoop();
                }
                catch (Exception e) {
                    // if (Log.DEBUG) Log.debug("MultiplexerSocketController: error reading real socket, " + e +", vsc.id="+MultiplexerSocketController.this._vscId);
                    onSocketException(e);
                }
            }
        }, _threadName + "." + _connectionId);

        _thread.setDaemon(true);
        _thread.start();
    }

    private void performHandshake() throws Exception {
        _msStarted = System.currentTimeMillis();
        _inetAddress = _socket.getInetAddress();
        _socket.setTcpNoDelay(true);

        // use buffering
        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(_socket.getInputStream(), 16*1024));
        DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(_socket.getOutputStream(), 16*1024));

        // create Handshake between client and server, which includes getting the VSC_ID and UUID from server
        if (_bIsClient) {
            // handshaking code that runs on the client side of the socket
            dataOutputStream.write(Signature.getBytes()); // so that server will know that this is VSC connection
            dataOutputStream.flush(); // since stream is buffered
            // read id from server.
            this._connectionId = dataInputStream.readInt(); // assigned by server
            // ois.close();
        }
        else // server side connection
        {
            boolean bValid = verifyServerSideHandshake();
            if (!bValid) {
                String msg = getInvalidConnectionMessage();
                dataOutputStream.writeBytes(msg == null ? "" : msg);
                dataOutputStream.flush();
                throw new Exception("Invalid connection, failed handshake");
            }
            // handshaking code that runs on the server side of the socket
            // send assigned id to client.
            dataOutputStream.writeInt(_connectionId);
            // oos.close(); // dont close, else it will close socket
            dataOutputStream.flush();
        }

        // flag that the socket is valid
        _bIsValid = true;

        getOutputStreamController().setDataOutputStream(dataOutputStream);
        getInputStreamController().setDataInputStream(dataInputStream);
    }

    /**
     * This will verify that the client that is connected is a valid MultiplexerSocket. To be valid, the client will send a "signature" string that must match the signature on the server.
     * 
     * @return true if client is a valid vsocket, else returns false.
     * @throws IOException
     */
    protected boolean verifyServerSideHandshake() throws IOException {
        boolean bResult = false;

        for (int i = 0;; i++) {
            byte b = (byte) _socket.getInputStream().read();
            if (b != (byte) Signature.charAt(i)) {
                _socket.getInputStream().read(new byte[2048]); // consume the rest of the available message
                break;
            }
            if (i + 1 == Signature.length()) {
                bResult = true;
                break;
            }
        }
        return bResult;
    }

    public int getId() {
        return this._connectionId;
    }

    /**
     * Used to control the real outputstream. 
     * */
    public MultiplexerOutputStreamController getOutputStreamController() { 
        if (_outputStreamController == null) {
            synchronized (_LockStreamController) {
                if (_outputStreamController == null) {
                    _outputStreamController = new MultiplexerOutputStreamController();
                }
            }
        }
        return _outputStreamController;
    }

    public MultiplexerInputStreamController getInputStreamController() {
        if (_inputStreamController != null) return _inputStreamController;

        synchronized (_LockStreamController) {
            if (_inputStreamController != null) return _inputStreamController;

            _inputStreamController = new MultiplexerInputStreamController(_connectionId) {
                @Override
                protected VirtualSocket getSocket(int id) {
                    VirtualSocket vs = getSocketHashtable().get(id);
                    return vs;
                }

                @Override
                protected void closeRealSocket() {
                    try {
                        MultiplexerSocketController.this.close();
                        if (MultiplexerSocketController.this._bIsClient) _status = STATUS_DisconnetedByServer;
                        else _status = STATUS_DisconnetedByClient;
                    }
                    catch (Exception e) {
                    }
                }

                @Override
                protected void closeSocket(int id, boolean bSendCommand) {
                    VirtualSocket vs = getSocketHashtable().get(id);
                    try {
                        if (vs != null) vs.close(bSendCommand);
                    }
                    catch (Exception e) {
                    }

                }

                @Override
                protected void createNewSocket(int connectionId, int id, String serverSocketName) {
                    try {
                        MultiplexerSocketController.this.createSocket(connectionId, id, serverSocketName);
                    }
                    catch (Exception e) {
                    }
                }

                @Override
                protected int getMaxSocketId() {
                    return MultiplexerSocketController.this._maxSocketId;
                }
            };
        }
        return _inputStreamController;
    }

    public int getStatus() {
        return _status;
    }

    /**
     * Flag to know if socket is a valid socket connection.
     */
    public boolean isValid() {
        return _bIsValid;
    }

    public long getStartTimeMS() {
        return _msStarted;
    }

    public Thread getThread() {
        return _thread;
    }

    /**
     * Creates a vsocket on client to communicated with a remote handler on server.
     */
    public VirtualSocket createSocket(String serverSocketName) throws IOException {
        int id = _aiSocketIdSeq.getAndIncrement();
        LOG.fine("creating new socket, name="+serverSocketName+", connectionId="+_connectionId+", id="+id);
        VirtualSocket vs = createSocket(_connectionId, id, serverSocketName);
        return vs;
    }

    /**
     * Used to create a new "virtual" socket. On the server, this is called directly by MultiplexerServerSocketController when it receives a command CMD_CreateVSocket
     */
    protected VirtualSocket createSocket(int connectionId, int id, String serverSocketName) throws IOException {
        LOG.fine("creating new socket, name="+serverSocketName+", connectionId="+connectionId+", id="+id);
        synchronized (_LockMaxSocketId) {
            _maxSocketId = Math.max(_maxSocketId, id);
        }
        VirtualSocket vs = new VirtualSocket(connectionId, id, serverSocketName) {
            private byte[] singleByteRead = new byte[1];
            private byte[] singleByteWrite = new byte[1];

            @Override
            public int read() throws IOException {
                int x = 0;
                do {
                    x = MultiplexerSocketController.this.getInputStreamController().read(this, singleByteRead, 0, 1);
                }
                while (x < 1);
                // the definition of this method is to return an int from 0 to 255 (unsigned byte)
                // need to bitmask to be an int, so that the signed bit is used as a 128 value.
                x = (0x000000FF & ((int) singleByteRead[0])); // 0xFF should also work
                return x;
            }

            @Override
            public int read(byte[] bs, int off, int len) throws IOException {
                if (bs == null || len < 1) return 0;
                return MultiplexerSocketController.this.getInputStreamController().read(this, bs, off, len);
            }

            @Override
            public void write(int b) throws IOException {
                singleByteWrite[0] = (byte) b;
                MultiplexerSocketController.this.getOutputStreamController().write(this, singleByteWrite, 0, 1);
            }

            @Override
            public void write(byte[] bs, int off, int len) throws IOException {
                if (bs == null || len < 1) return;
                MultiplexerSocketController.this.getOutputStreamController().write(this, bs, off, len);
            }

            @Override
            public synchronized void close() throws IOException {
                super.close(); // default behavior is to mark socket as closed
                MultiplexerSocketController.this.closeSocket(this, true);
            }
            @Override
            public synchronized void close(boolean bSendCommand) throws IOException {
                super.close(); // default behavior is to mark socket as closed
                MultiplexerSocketController.this.closeSocket(this, bSendCommand);
            }

            @Override
            public InetAddress getInetAddress() {
                Socket s = _socket;
                if (s == null) return super.getInetAddress();
                return s.getInetAddress();
            }

            @Override
            public InetAddress getLocalAddress() {
                Socket s = _socket;
                if (s == null) return super.getLocalAddress();
                return s.getLocalAddress();
            }

            @Override
            public int getLocalPort() {
                Socket s = _socket;
                if (s == null) return super.getLocalPort();
                return s.getLocalPort();
            }

            @Override
            public SocketAddress getLocalSocketAddress() {
                Socket s = _socket;
                if (s == null) return super.getLocalSocketAddress();
                return s.getLocalSocketAddress();
            }

            @Override
            public SocketAddress getRemoteSocketAddress() {
                Socket s = _socket;
                if (s == null) return super.getRemoteSocketAddress();
                return s.getRemoteSocketAddress();
            }

            @Override
            public boolean getKeepAlive() throws SocketException {
                Socket s = _socket;
                if (s == null) return super.getKeepAlive();
                return s.getKeepAlive();
            }

            @Override
            public boolean getOOBInline() throws SocketException {
                Socket s = _socket;
                if (s == null) return super.getOOBInline();
                return s.getOOBInline();
            }

            @Override
            public int getPort() {
                Socket s = _socket;
                if (s == null) return super.getPort();
                return s.getPort();
            }

            @Override
            public int getTrafficClass() throws SocketException {
                Socket s = _socket;
                if (s == null) return super.getTrafficClass();
                return s.getTrafficClass();
            }

            @Override
            public boolean isBound() {
                Socket s = _socket;
                return (s != null && s.isBound()); // the vsocket will always be false, only check the real socket
            }

            @Override
            public boolean isClosed() {
                return super.isClosed() || MultiplexerSocketController.this.isClosed();
            }

            @Override
            public boolean isConnected() {
                Socket s = _socket;
                return (s != null && s.isConnected()); // the vsocket will always be false, only check the real socket
            }

            @Override
            public boolean isInputShutdown() {
                Socket s = _socket;
                return super.isInputShutdown() || (s != null && s.isInputShutdown());
            }

            @Override
            public boolean isOutputShutdown() {
                Socket s = _socket;
                return super.isOutputShutdown() || (s != null && s.isOutputShutdown());
            }
        };
        getSocketHashtable().put(id, vs);

        if (_bIsClient) {
            getOutputStreamController().sendCommand(CMD_CreateVSocket, id, serverSocketName);
        }
        return vs;
    }

    protected void closeSocket(VirtualSocket vs, boolean bSendCommand) throws IOException {
        LOG.fine("closing vsocket, connectionId="+_connectionId+", id="+vs._id);
        if (bSendCommand) {
            getOutputStreamController().sendCommand(CMD_CloseVSocket, vs._id);
        }
        synchronized (vs._lockObject) {
            vs._lockObject.notify();
        }
        getSocketHashtable().remove(vs._id);
    }

    /**
     * Called when there is a read on the "real" socket. By default, this will call close.
     */
    protected void onSocketException(Exception e) {
        /*
         * if (Log.DEBUG) { Log.debug("MultiplexerSocketController: onSocketException, vsc.id="+_vscId+", will close socket, Exception:"+ e); }
         */
        try {
            this.close(true);
        }
        catch (Exception ex) {
        }
        finally {
            _status = STATUS_DisconnectedByError;
        }
    }

    public InetAddress getInetAddress() {
        return this._inetAddress;
    }

    /**
     * If the socket exists, then the other end of the socket will be notified to close, and then the socket will be closed.
     * 
     * @throws IOException
     */
    public void close() throws IOException {
        close(false);
    }

    public boolean wasCloseAlreadyCalled() {
        return _abClosing.get();
    }
    
    protected void close(boolean bError) throws IOException {
        if (this._socket == null) return;
        if (!_abClosing.compareAndSet(false, true)) {
            return;
        }
        try {
            LOG.fine("closing real socket, connectionId="+_connectionId);
            if (!bError) {
                try {
                    getOutputStreamController().sendCommand(CMD_CloseRealSocket, 0);
                }
                catch (Exception e) {
                    // ignore
                }
                if (this._bIsClient) _status = STATUS_DisconnetedByClient;
                else _status = STATUS_DisconnetedByServer;
            }
            this._socket.close();
        }
        finally {
            this._socket = null;

            if (_inputStreamController != null) {
                getInputStreamController().close();
            }
            if (_outputStreamController != null) {
                getOutputStreamController().close();
            }
            if (_hashtableSocket != null) {
                VirtualSocket[] vss = getMultiplexerSockets();
                for (VirtualSocket vs : vss) {
                    synchronized (vs._lockObject) {
                        vs._lockObject.notify();
                    }
                }
            }
        }
    }

    /**
     * @return array of all active MultiplexerSockets.
     */
    public VirtualSocket[] getMultiplexerSockets() {
        VirtualSocket[] vss = new VirtualSocket[0];
        if (_hashtableSocket != null) {
            vss = (VirtualSocket[]) getSocketHashtable().values().toArray(vss);
        }
        return vss;
    }

    private Hashtable<Integer, VirtualSocket> getSocketHashtable() {
        if (_hashtableSocket == null) {
            _hashtableSocket = new Hashtable<Integer, VirtualSocket>(53, .75f); // all open MultiplexerSockets, by Id
        }
        return _hashtableSocket;
    }

    public boolean isClosed() {
        return (this._socket == null || this._socket.isClosed());
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Message to display when a non-multiplexersocket connects. This message will be sent to client, followed by a disconnect.
     */
    public void setInvalidConnectionMessage(String msg) {
        LOG.fine("setInvalidConnectionMessage, msg="+msg);
        _invalidConnectionMessage = msg;
    }

    public String getInvalidConnectionMessage() {
        return _invalidConnectionMessage;
    }

}
