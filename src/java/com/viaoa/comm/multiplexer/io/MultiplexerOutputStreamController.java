package com.viaoa.comm.multiplexer.io;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Internally created by MultiplexerSocketController to manage the OutputStream for the "real" socket.
 * 
 * @author vvia
 */
class MultiplexerOutputStreamController {

    /** outputstream for "real" socket. */
    private DataOutputStream _dataOutputStream;
    /** flag to know if socket has been closed. */
    private volatile boolean _bIsClosed;

    /** set when an vsocket has the socket.outputSream */
    private volatile boolean _bWritingLock;
    /**
     * Keeps track of how many vsockets are waiting to do a write on the real socket outputstream.
     */
    private volatile int _writeLockWaitingCount; // this is only changed within a synch block
    /**
     * used to track outputStream flushing, so that a flush is not done if there are waiting writers.
     */
    private int _iWriteFlush; // counter
    private boolean _needsFlush;

    /** used to synchronize access to outputstream by vsockets. */
    private final transient Object WRITELOCK = new Object();

    /**
     * Created by MultiplexerSocketController to manager the real outputstream.
     */
    MultiplexerOutputStreamController() {
        this._bWritingLock = true;
    }

    /**
     * The real outputstream that is shared by vsockets.
     */
    void setDataOutputStream(DataOutputStream dataOutputStream) {
        this._dataOutputStream = dataOutputStream;
        synchronized (WRITELOCK) {
            this._bWritingLock = false;
            WRITELOCK.notifyAll();
        }
    }

    /**
     * Flag the outputstream as closed and notify all vsockets that are waiting to write to it.
     */
    void close() {
        synchronized (WRITELOCK) {
            this._bIsClosed = true;
            WRITELOCK.notifyAll();
        }
    }

    /**
     * Called by vsockets, to write to the "real" outputstream. A header is created that includes the
     * vsocket Id, length of data. <br>
     * Data is written in "chunks" so that other threads do not have to wait on another thread to send a
     * large amount of data. This will call _write() until the full amount is sent.
     */
    void write(VirtualSocket vs, byte[] bs, int off, int fullLength) throws IOException {
        // make sure that data is sent in chunks
        int pos = 0;
        do {
            int len = fullLength - pos;
            if (len > 8192) {
                // get max length of data "chunk", partially based on the number of threads waiting.
                len = getMaxWriteLength(vs, len);
            }
            _write(vs, bs, off + pos, len);

            pos += len;
        }
        while (pos < fullLength);
    }

    /**
     * Called by write() to output a "chunk" of the data. Locking/Synchronizing the outputstream is
     * accomplished by calling getOutputStream().
     */
    private void _write(VirtualSocket vs, byte[] bs, int offset, int len) throws IOException {
        // this method will create a lock from other threads, since the outputStream is a shared
        // resource.
        // getOutputStream is synchronized until it is released, this will make sure that there are
        // no other threads using the shared objects
        DataOutputStream outputStream = getOutputStream();

        outputStream.writeInt(vs._id); // header
        outputStream.writeInt(len); // header

        outputStream.write(bs, offset, len);
        releaseOutputStream(true); // this will flush
    }

    /**
     * Used to determine the max size that can be written to real socket per request. This is a
     * recommendation, and is not enforced when writing to the real outputstream.
     */
    protected int getMaxWriteLength(VirtualSocket vs, int requestSize) {
        int max;
        if (_writeLockWaitingCount < 2) {
            max = 65536;
        }
        else if (_writeLockWaitingCount < 6) {
            max = 16384;
        }
        else {
            max = 8192;
        }
        max = Math.min(requestSize, max);
        return max;
    }

    /**
     * Used to synchronized access the the real outputstream.
     */
    private DataOutputStream getOutputStream() throws IOException {
        // long tsBegin = System.nanoTime(); // measurement
        for (;;) {
            synchronized (WRITELOCK) {
                if (_bIsClosed) {
                    throw new IOException("real socket has been closed");
                }
                if (!_bWritingLock) {
                    _bWritingLock = true;
                    return _dataOutputStream;
                }
                try {
                    _writeLockWaitingCount++;
                    WRITELOCK.wait();
                }
                catch (InterruptedException e) {
                }
                finally {
                    _writeLockWaitingCount--;
                }
            }
        }
    }

    /**
     * Releases the outputstream, and notifies other threads that it is available.
     * 
     * @param bFlush
     *            if true, since a buffered stream is used, a flush will be done according to the
     *            following: if there are no other vsockets waiting to do a write, or if this is the 5th
     *            write to be done.
     */
    private void releaseOutputStream(boolean bFlush) throws IOException {
        if (_bIsClosed) return;
        synchronized (WRITELOCK) {
            if (bFlush || _needsFlush) {
                if (_writeLockWaitingCount == 0 || ((++_iWriteFlush % 5) == 0)) {
                    _needsFlush = false;
                    _iWriteFlush = 0;
                    _dataOutputStream.flush();
                }
                else _needsFlush = true;
            }

            _bWritingLock = false;
            WRITELOCK.notify();
        }
    }

    /**
     * Send a command to receiver. The command is then read by
     * MultiplexerInputStreamController.readRealSocket() can processed by
     * MultiplexerInputStreamController and/or MultiplexerSocketController.
     */
    protected void sendCommand(int cmd, int param) throws IOException {
        sendCommand(cmd, param, null);
    }

    /**
     * Send a command to receiver. The command is then read by
     * MultiplexerInputStreamController.readRealSocket() can processed by
     * MultiplexerInputStreamController and/or MultiplexerSocketController.
     * 
     * @param cmd
     *            see MultiplexerSocketController for list of commands
     * @param param
     * @param serverProcessName
     *            used for new vsockets, so that the server will know which
     *            MultiplexerServerSocket.accept() to use.
     */
    protected void sendCommand(int cmd, int param, String serverSocketName) throws IOException {
        if (this._bIsClosed) return;

        getOutputStream();
        // this needs to match what is read by readRealSocket, which is Short + Integer + Integer.
        _dataOutputStream.writeInt(MultiplexerSocketController.CMD_Command);
        _dataOutputStream.writeInt(cmd);
        _dataOutputStream.writeInt(param);
        if (serverSocketName != null) {
            _dataOutputStream.writeInt(serverSocketName.length());
            _dataOutputStream.writeBytes(serverSocketName);
        }
        releaseOutputStream(true);
    }

}
