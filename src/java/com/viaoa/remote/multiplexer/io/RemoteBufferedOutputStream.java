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
package com.viaoa.remote.multiplexer.io;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Customized from BufferedOutputStream, but uses a shared pool of buffers so that it does not have 
 * to create a new one each time. Also removed sync on methods, since this is only used by one
 * thread at a time.
 * 
 * @author vvia
 */
public class RemoteBufferedOutputStream extends FilterOutputStream {
    private static final int TotalBuffers = 18;
    private static final int BufferSize = 1024 * 8;
    protected byte[] bsBuffer;
    protected int count;
    protected boolean bOwnedBuffer;  // true if the bsBuffer is not from the pool

    /**
     * Creates a new buffered output stream to write data to the specified underlying output stream with
     * the specified buffer size.
     */
    public RemoteBufferedOutputStream(OutputStream out) {
        super(out);
    }

    // create a pool of available buffers
    static boolean[] isUsed = new boolean[TotalBuffers];
    static byte[][] buffers = new byte[TotalBuffers][];
    static final Object Lock = new Object();

    protected static byte[] getPoolBuffer() {
        synchronized (Lock) {
            for (int i = 0; i < TotalBuffers; i++) {
                if (!isUsed[i]) {
                    isUsed[i] = true;
                    if (buffers[i] == null) {
                        buffers[i] = new byte[BufferSize];
                    }
                    return buffers[i];
                }
            }
        }
        return null;
    }
    protected static void releasePoolBuffer(byte[] bs) {
        if (bs == null) return;
        synchronized (Lock) {
            for (int i = 0; i < TotalBuffers; i++) {
                if (buffers[i] == bs) {
                    isUsed[i] = false;
                    break;
                }
            }
        }
    }
    protected byte[] getBuffer() {
        byte[] bs = getPoolBuffer();
        if (bs == null) {
            bs = new byte[2048]; // use a smaller size
            bOwnedBuffer = true;
        }
        return bs;
    }

    private void freeBuffer() {
        if (!bOwnedBuffer && bsBuffer != null) {
            releasePoolBuffer(bsBuffer);
            bsBuffer = null;
        }
    }
    
    @Override
    public void close() throws IOException {
        freeBuffer();
        super.close();
    }

    @Override
    protected void finalize() throws Throwable {
        freeBuffer();
        super.finalize();
    }
    
    /** writes the internal buffer to output, and resets position to 0 */
    private void writeBuffer() throws IOException {
        if (count > 0 && bsBuffer != null) {
            out.write(bsBuffer, 0, count);
            count = 0;
        }
    }

    public void write(int b) throws IOException {
        if (bsBuffer == null) {
            bsBuffer = getBuffer();
        }
        else if (count >= bsBuffer.length) {
            writeBuffer();
        }
        bsBuffer[count++] = (byte) b;
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (bsBuffer == null) {
            bsBuffer = getBuffer();
        }
        if (len >= bsBuffer.length) {
            if (count > 0) {
                writeBuffer();
            }
            out.write(b, off, len);
            return;
        }
        if (len > bsBuffer.length - count) {
            writeBuffer();
        }
        System.arraycopy(b, off, bsBuffer, count, len);
        count += len;
    }

    /**
     * Overwritten to include freeing the byte[] buffer from the shared pool.
     */
    public void flush() throws IOException {
        writeBuffer();
        out.flush();
        freeBuffer();  // good chance that it is not needed anymore
    }
}
