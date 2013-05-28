package com.viaoa.comm.multiplexer.io;

import java.net.*;
import java.io.*;

/**
 * A "virtual" socket connection to an MultiplexerServerSocket through a multiplexed connection. A
 * MultiplexerSocket is created and managed by MultiplexerSocketController, so that the input and output
 * streams are able to share the real socket.
 * 
 * @author vvia
 */
public abstract class VirtualSocket extends Socket {

    /**
     * Real socket connection id (assigned on server)
     */
    protected final int _connectionId;
    /**
     * unique identifer (assigned by MultiplexerSocketController)
     */
    protected final int _id;

    /**
     * Name of server socket.
     */
    protected String _serverSocketName;

    /**
     * the internal stream returned by getInputStream. All reads will be share the real socket.
     */
    private InputStream _inputStream;

    /**
     * the internal stream returned by getOutputStream. All writes will share the real socket.
     */
    private OutputStream _outputStream;

    /**
     * Lock used when waiting to perform a read on the real socket.
     */
    protected final Object _lockObject = new Object();

    /**
     * Called by MultiplexerSocketController when creating a new socket or receiving a command to create
     * a new virtual socket.
     * 
     * @param id
     *            seq number assigned by MultiplexerSocketController.
     */
    protected VirtualSocket(int connectionId, int id, String serverSocketName) {
        if (id < 0) {
            throw new IllegalArgumentException("id can not be less then 0");
        }
        this._connectionId = connectionId;
        this._id = id;
        this._serverSocketName = serverSocketName;
        createInputStream();
        createOutputStream();
    }

    /**
     * Implement an inputStream that will use/share/multiplex the "real" socket.
     */
    protected synchronized void createInputStream() {
        if (_inputStream != null) return;

        _inputStream = new InputStream() {
            @Override
            public int read() throws IOException {
                int value = VirtualSocket.this.read();
                return value;
            }

            @Override
            public int read(byte[] bs) throws IOException {
                return this.read(bs, 0, bs.length);
            }

            @Override
            public int read(byte[] bs, int off, int len) throws IOException {
                if (bs == null || len < 1) return 0;
                int x = VirtualSocket.this.read(bs, off, len);
                return x;
            }
        };
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (_inputStream != null) {
            createInputStream();
        }
        return _inputStream;
    }

    /**
     * Implement an outputStream that will use/share/multiplex the "real" socket.
     */
    protected synchronized void createOutputStream() {
        if (_outputStream != null) return;

        _outputStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                VirtualSocket.this.write(b);
            }

            @Override
            public void write(byte[] bs) throws IOException {
                write(bs, 0, bs.length);
            }

            @Override
            public void write(byte[] bs, int off, int len) throws IOException {
                if (bs == null || len < 1) return;
                VirtualSocket.this.write(bs, off, len);
            }
        };
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        }

        if (_outputStream == null) {
            createOutputStream();
        }
        return _outputStream;
    }

    /**
     * The ID that is assigned on the server to the real socket connection.
     */
    public int getConnectionId() {
        return _connectionId;
    }

    /**
     * The ID that is assigned by MultiplexerSocketController. This is assigned during the constructor,
     * and is a sequential number.
     */
    public int getId() {
        return _id;
    }

    public String getServerSocketName() {
        return this._serverSocketName;
    }

    /**
     * Used so that the real socket is used for the vsocket read. MultiplexerSocketController will
     * implement these methods and then manage access to the "real" socket.
     */
    public abstract int read(byte[] bs, int off, int len) throws IOException;

    public abstract int read() throws IOException;

    /**
     * Used so that the real socket is used for the vsocket write. MultiplexerSocketController will
     * implement these methods and then manage access to the "real" socket.
     */
    public abstract void write(byte[] bs, int off, int len) throws IOException;

    public abstract void write(int b) throws IOException;

    public abstract void close(boolean bSendCommand) throws IOException;
}
