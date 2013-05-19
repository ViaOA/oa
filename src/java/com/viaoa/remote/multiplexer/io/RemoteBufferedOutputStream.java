package com.viaoa.remote.multiplexer.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Customized from BufferedOutputStream, but uses a pool of buffers so that it does not have 
 * to create a new one each time. Also removed sync on methods, since this is only used by one
 * thread at a time.
 * 
 * @author vvia
 */
public class RemoteBufferedOutputStream extends FilterOutputStream {
    private static final int TotalBuffers = 10;
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
        synchronized (Lock) {
            for (int i = 0; i < TotalBuffers; i++) {
                if (buffers[i] == bs) {
                    if (isUsed[i]) isUsed[i] = false;
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
    protected void releaseBuffer(byte[] bs) {
        if (!bOwnedBuffer) {
            releasePoolBuffer(bs);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        releaseBuffer(bsBuffer);
    }

    /** Flush the internal buffer */
    private void flushBuffer() throws IOException {
        if (count > 0) {
            out.write(bsBuffer, 0, count);
            count = 0;
            releaseBuffer(bsBuffer);
        }
    }

    public void write(int b) throws IOException {
        if (count == 0) {
            bsBuffer = getBuffer();
        }
        else if (count >= bsBuffer.length) {
            flushBuffer();
        }
        bsBuffer[count++] = (byte) b;
    }

    public void write(byte b[], int off, int len) throws IOException {
        if (count == 0) {
            bsBuffer = getBuffer();
        }
        if (len >= bsBuffer.length) {
            if (count > 0) flushBuffer();
            out.write(b, off, len);
            return;
        }
        if (len > bsBuffer.length - count) {
            flushBuffer();
        }
        System.arraycopy(b, off, bsBuffer, count, len);
        count += len;
    }

    public void flush() throws IOException {
        flushBuffer();
        out.flush();
    }
}
