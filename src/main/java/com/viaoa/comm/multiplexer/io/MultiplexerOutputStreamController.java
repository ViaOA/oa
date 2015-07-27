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


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Internally created by MultiplexerSocketController to manage the OutputStream for the "real" socket.
 * 
 * @author vvia
 */
public class MultiplexerOutputStreamController {
    private static Logger LOG = Logger.getLogger(MultiplexerOutputStreamController.class.getName());

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
     * Throttle to limit the number of MB per second, calculated 10 times per second.
     * if 0 then no limit.
     */
    private int mbThrottleLimitPerSecond;
    private int iThrottleLimitPerFractionSecond; // number of bytes per 1/iThrottleGrandularity second

    
    /**
     *  how often to check (times per second) for throttling
     *  Max value should not be greater then 1000 (which is a single milisecond).
     */
    private final int iThrottleFractionOfSecond = 250; // this will use 1000/250 = 4 miliseconds
    
    /** used to synchronize access to outputstream by vsockets. */
    private final transient Object WRITELOCK = new Object();

    /**
     * Created by MultiplexerSocketController to manager the real outputstream.
     */
    MultiplexerOutputStreamController() {
        this._bWritingLock = true;
    }

    /**
     * Throttle to limit the number of MB per second, calculated 10 times per second.
     * if 0 then no limit.
     * Each write will check to see if the max has been written for the current time slice (10th sec),
     * and sleep for the remainder if the limit has been exceeded.
     */
    public void setThrottleLimit(int mbPerSecond) {
        mbThrottleLimitPerSecond = mbPerSecond;
        if (mbPerSecond > 0) {
            iThrottleLimitPerFractionSecond = (mbPerSecond * 1024 * 1024) / iThrottleFractionOfSecond;
        }
        else iThrottleLimitPerFractionSecond = 0;
    }
    public int getThrottleLimit() {
        return mbThrottleLimitPerSecond;
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
        try {
            _write(vs, bs, offset, len, outputStream);
        }
        finally {
            releaseOutputStream(); // this will flush
        }
    }    
    private void _write(VirtualSocket vs, byte[] bs, int offset, int len, DataOutputStream outputStream) throws IOException {
//System.out.println("   mosc_write=>  vs.id="+vs._id+", offset="+offset+", len="+len+", waitingCount="+_writeLockWaitingCount+", thread="+Thread.currentThread().getName());//qqqqqqqqqqqq        
        outputStream.writeInt(vs._id); // header
        outputStream.writeInt(len); // header
        outputStream.write(bs, offset, len);

        // this is to throttle the amount of data that can be written per fraction of a second (ex: 10x = 100ms)
        if (iThrottleLimitPerFractionSecond > 0) {
            throttleTotalBytesWritten += len;
            long msNow = System.currentTimeMillis();
            if (throttleLastMs == 0) throttleLastMs = msNow; 
            long msDiff = msNow - throttleLastMs; 
            if (msDiff >= (1000/iThrottleFractionOfSecond)) {  // compare in miliseconds
                // reset
                throttleTotalBytesWritten = len;
                throttleLastMs = msNow;
            }
            else if (throttleTotalBytesWritten > iThrottleLimitPerFractionSecond) {
                // need to sleep for remainder of fraction of second
                int msSleep = (int) ((1000/iThrottleFractionOfSecond) - msDiff);
                try {
                    Thread.sleep(msSleep);
                }
                catch (InterruptedException e) {}
                throttleTotalBytesWritten = (throttleTotalBytesWritten - iThrottleLimitPerFractionSecond);
                throttleLastMs = msNow + msSleep;
            }
        }
    }
    // used to track throttling
    private long throttleLastMs;
    private long throttleTotalBytesWritten;
    
    /**
     * Used to determine the max size that can be written to real socket per request. This is a
     * recommendation, and is not enforced when writing to the real outputstream.
     */
    protected int getMaxWriteLength(VirtualSocket vs, int requestSize) {
        int max;
        if (_writeLockWaitingCount == 0) {
            max = 32768;
        }
        else if (_writeLockWaitingCount < 2) {
            max = 16384;
        }
        else if (_writeLockWaitingCount < 6) {
            max = 8192;
        }
        else {
            max = 4096;
        }
        max = Math.min(requestSize, max);
        return max;
    }

    // used to control thread fairness
    private final int MaxWaitingThreads = 15;
    private Thread[] waitingThreads = new Thread[MaxWaitingThreads];
    private long headWaitingThreads;
    private long tailWaitingThreads;
    
    /**
     * Used to synchronized access the the real outputstream.
     */
    private DataOutputStream getOutputStream() throws IOException {
        // long tsBegin = System.nanoTime(); // measurement
        boolean bIsInWaitingThreads = false;
        synchronized (WRITELOCK) {
            for (int i=0; ;i++) {
                if (_bIsClosed) {
                    throw new IOException("real socket has been closed");
                }
/*                
System.out.println("getOutputStream "+Thread.currentThread().getName()+", _bWritingLock="+_bWritingLock+", i="+
                i+", _writeLockWaitingCount="+_writeLockWaitingCount+
                ", headWaitingThreads="+headWaitingThreads +
                ", tailWaitingThreads="+tailWaitingThreads
                );//qqqqqq
*/

                if (!_bWritingLock) {
                    if (tailWaitingThreads == headWaitingThreads) {
                        if (_writeLockWaitingCount == 0 || i>0) {
                            _bWritingLock = true;
                            return _dataOutputStream;
                        }
                    }
                    else if (waitingThreads[(int) (tailWaitingThreads % MaxWaitingThreads)] == Thread.currentThread()) {
                        _bWritingLock = true;
                        tailWaitingThreads++;
                        return _dataOutputStream;
                    }
                }
                
                try {
                    _writeLockWaitingCount++;
                    if (!bIsInWaitingThreads && i > 5) {
                        if (headWaitingThreads - tailWaitingThreads < MaxWaitingThreads) {
                            bIsInWaitingThreads = true;
                            int pos = (int) (headWaitingThreads++ % MaxWaitingThreads);
                            waitingThreads[pos] = Thread.currentThread();
                        }
                    }
                    WRITELOCK.wait(150);
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
    private void releaseOutputStream() throws IOException {
        if (_bIsClosed) return;
        synchronized (WRITELOCK) {
            try {
                if (_writeLockWaitingCount == 0) {
                    _dataOutputStream.flush();
                }
            }
            catch (SocketException e) {
                onSocketException(e);
                throw(e);
            }
            finally {
                _bWritingLock = false;
                WRITELOCK.notifyAll();
            }
        }
    }

    public void sendPingCommand() throws IOException {
        sendCommand(MultiplexerSocketController.CMD_Ping, 0, null);
    }

    protected void onSocketException(Exception e) {
        
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
        try {
            // this needs to match what is read by readRealSocket, which is Short + Integer + Integer.
            _dataOutputStream.writeInt(MultiplexerSocketController.CMD_Command);
            _dataOutputStream.writeInt(cmd);
            _dataOutputStream.writeInt(param);
            if (serverSocketName != null) {
                _dataOutputStream.writeInt(serverSocketName.length());
                _dataOutputStream.writeBytes(serverSocketName);
            }
        }
        finally {
            releaseOutputStream();
        }
    }
}
