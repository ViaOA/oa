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
package com.viaoa.comm.multiplexer.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Internally created by MultiplexerSocketController to manage the InputStream for the 
 * "real" socket.
 */
public abstract class MultiplexerInputStreamController {
    private static Logger LOG = Logger.getLogger(MultiplexerInputStreamController.class.getName());

    private int _connectionId;

    /** real inputstream. */
    private DataInputStream _dataInputStream;

    /** flag to know if socket has been closed. */
    private boolean _bIsClosed;
    
    /** time of last read */
    private long msLastRead;

    /**
     * Max amount of time that real socket will wait for an vsocket to read real data from real inputstream
     * 
     * five seconds would be more then enough, since data is "chunked", but the thread could be
     * "busy" outside of reading the data.
     */
    private final int _timeoutSeconds = 20;
    
    /** Lock used to manage access to inputstream. */
    private final transient Object READLOCK = new Object();

    /** flag to know if real socket reader is waiting for vsocket to finish. */
    private volatile boolean _bRealReaderIsWaiting;

    /** Information about the data that is next to be read. */
    private volatile int _nextReadId = -1; // from the header, < 0 means that it is not assigned, 0 is
                                           // for commands.
    private volatile int _nextReadLen; // from the header, length of data for the next reader to read.
    private volatile int _nextReadOffset; // used when reading n bytes at a time. This keeps track of
                                          // the amount of bytes from _nextRadLen that have been read.

    /**
     * Created by MultiplexerSocketController to manager the real inputStream.
     */
    MultiplexerInputStreamController(int connectionId) {
        this._connectionId = connectionId;
    }

    /**
     * The real inputstream that is shared by vsockets.
     */
    void setDataInputStream(DataInputStream dataInputStream) {
        this._dataInputStream = dataInputStream;
    }

    /**
     * Flag the inputstream as closed and notify all vsockets that are waiting to read from it.
     */
    protected void close() throws IOException {
        synchronized (READLOCK) {
            this._bIsClosed = true;
            READLOCK.notifyAll();
        }
    }

    public long getLastReadTime() {
        return msLastRead;
    }
    
    /**
     * Call by MultiplexerSocketController thread that manages the input stream. This will read the
     * header and then allow the correct vsocket to read the data from the real socket. If the header
     * is a command, then the command is performed. This will loop until close() is called.
     */
    void readRealSocketLoop() throws Exception {
        for (int cntx = 0; !_bIsClosed; cntx++) {
            int readId = _dataInputStream.readInt(); // socket.id or 0 for command
            msLastRead = System.currentTimeMillis();
            _nextReadLen = _dataInputStream.readInt(); // the length of data for the vsocket to read.

            if (readId == MultiplexerSocketController.CMD_Command) {
                // internal command
                // this needs to match what was sent by sendCommand, which is Short + Integer + Integer.
                int param = _dataInputStream.readInt();
                processCommand(_nextReadLen, param);
                continue;
            }

            VirtualSocket vs = getSocket(readId);

            // check for errors, should only happen if socket is disconnected, or data is corrupted.
            boolean bError;
            if (vs == null) {
                if (readId > getMaxSocketId()) {
                    bError = true;
                }
                else {
                    // consume the message, since the socket is closed
                    skipFully(_nextReadLen);
                    continue;
                }
            }
            else if (vs.isClosed()) {
                skipFully(_nextReadLen);
                continue;
            }
            else if (_nextReadLen < 0) bError = true;
            else bError = false;

            if (bError) {
                String s = "MultiplexerInputStreamController: Socket stream is corrupted, received id=" + readId + ", length=" + _nextReadLen + ", vsc.id=" + readId + ". ";
                if (vs == null) s += "VirtualSocket not in list, maxSocketId=" + getMaxSocketId() + ". ";
                s += "Real socket will be closed.";
                LOG.log(Level.WARNING, s);
                throw new Exception(s);
            }

            _nextReadOffset = 0;

            synchronized (vs._lockObject) {
                if (vs.isClosed()) {
                    skipFully(_nextReadLen);
                    continue;
                }
                _nextReadId = readId; // this is the VS.id that needs to read the data after the header
                vs._lockObject.notify();
            }

            synchronized (READLOCK) { // wait for VSocket (vsId=nextReadId) to perform read.
                for (int cnt = 0; (_nextReadId >= 0) && !_bIsClosed; cnt++) {
                    _bRealReaderIsWaiting = true;
                    try {
                        READLOCK.wait(250);
                        if (_nextReadId >= 0) {
                            if (vs.isClosed()) {
                                synchronized (vs._lockObject) {
                                    if (_nextReadId >= 0) {
                                        skipFully(_nextReadLen - _nextReadOffset);
                                        _nextReadId = -1;
                                        _nextReadOffset = _nextReadLen;
                                    }
                                }
                            }
                            else if (cnt == (_timeoutSeconds * 4)) {
                                LOG.warning("VSocket id=" + vs._id + ", name=" + vs.getServerSocketName() + ", has been timed out, will disconnect socket and continue");
                                synchronized (vs._lockObject) {
                                    vs.close(); // this will notify the thread
                                }
                            }
                        }
                    }
                    catch (InterruptedException e) {
                        // dont rethrow, since this is internal to real socket and would cause corrupted
                        // stream.
                    }
                    finally {
                        _bRealReaderIsWaiting = false;
                    }
                }
            }
        }
        LOG.fine("MultiplexerInputStreamController: socket has been closed, (leaving readRealSocket loop)");
    }

    /**
     * Called by vsockets, to read from the "real" inputstream. Once the "readRealSocket" receives a
     * header that is for an vsocket, it will then allow the vsocket to have access to read from the
     * socket.inputstream. The header also includes the length of data, and the vsocket will only be
     * able to read this amount.
     */
    int read(VirtualSocket vs, byte[] bs, int off, int len) throws IOException {
        int x;
        try {
            x = _read(vs, bs, off, len);
        }
        finally {
            _releaseInputStream(vs);
        }
        return x;
    }

    /**
     * Controls shared access to the real socket, based on which vsocket matches the current header. 
     */
    private int _read(VirtualSocket vs, byte[] bs, int off, int len) throws IOException {
        int readAmt = 0;

        for (;;) {
            synchronized (vs._lockObject) {
                if (vs._id == _nextReadId) {
                    readAmt = _dataInputStream.read(bs, off, Math.min(len, (_nextReadLen - _nextReadOffset)));
                    _nextReadOffset += readAmt;
                    return readAmt;
                }
                try {
                    if (vs.isClosed()) {
                        throw new IOException("socket has been closed");
                    }
                    vs._lockObject.wait();
                }
                catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Used to skip a message from a vsocket that is no longer available.
     */
    private void skipFully(int skipAmount) throws IOException {
        for (; skipAmount > 0;) {
            long x = _dataInputStream.skip(skipAmount);
            if (x < 0) break; // EOF
            skipAmount -= x;
        }
    }

    /**
     * The inputstream is shared by all of the vsockets. This will release the inputstream, and allow
     * the controller to then read the next header.
     */
    private void _releaseInputStream(VirtualSocket vs) {
        synchronized (READLOCK) {
            if ((_nextReadOffset == _nextReadLen) && (vs._id == _nextReadId)) // done reading full
                                                                              // amount from real
                                                                              // socket.
            {
                _nextReadId = -1; // this is used so that the real socket reader will "know" that the
                                  // vsocket is done reading.
                if (_bRealReaderIsWaiting) {
                    READLOCK.notify();
                }
            }
            // else: will need to do another read for the rest of the data from real socket.
        }
    }

    /**
     * Process "real" socket commands.
     * 
     * @see MultiplexerOutputStreamController#sendCommand(int, int)
     */
    protected void processCommand(int cmd, int param) throws Exception {
        switch (cmd) {
        case MultiplexerSocketController.CMD_CreateVSocket:
            String serverSocketName;
            int len = _dataInputStream.readInt();
            byte[] bs = new byte[len];
            _dataInputStream.readFully(bs);
            serverSocketName = new String(bs);
            createNewSocket(_connectionId, param, serverSocketName);
            break;
        case MultiplexerSocketController.CMD_CloseVSocket:
            closeSocket(param, false);
            break;
        case MultiplexerSocketController.CMD_CloseRealSocket:
            closeRealSocket();
            break;
        case MultiplexerSocketController.CMD_Ping:
            break;
        }
    }

    /**
     * Abstract methods that are implemented by MultiplexerSocketController.
     */

    /**
     * These methods are needed to be supplied by the user of this object. Currently used/created by
     * MultiplexerSocketController
     */

    /** Called by processCommand to create a new vsocket. */
    protected abstract void createNewSocket(int connectionId, int id, String serverSocketName);

    /** Called by processCommand to close an existing vsocket. */
    protected abstract void closeSocket(int id, boolean bSendCommand);

    /** Called by processCommand to close the "real" socket. */
    protected abstract void closeRealSocket();

    /** Used by readRealSocket when reading the header for the next vsocket data to read. */
    protected abstract VirtualSocket getSocket(int id);

    /** this is needed to verify the max ID used for virtual sockets */
    protected abstract int getMaxSocketId();
}
