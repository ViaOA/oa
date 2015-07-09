package com.viaoa.comm.ssl;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

/**
 * SSL base class used by client and server to be able to send/receive data that is first encrypted.
 * @author vvia
 *
 */
public abstract class SSLBase {
    private static Logger LOG = Logger.getLogger(SSLBase.class.getName());
    
    /**
     * Preferred encryption cipher to use for SSL sockets.
     */
    public static final String[] PREFERRED_CIPHER_NAMES = new String[] { "SSL_RSA_WITH_RC4_128_MD5" };

    protected SSLContext sslContext;
    protected SSLEngine sslEngine;

    // used to store encrypted data
    private byte[] bsWrap;
    private ByteBuffer bbWrap;

    // lock used when waiting on handshake data
    private final Object lock = new Object();
    // used during handshaking to 
    private byte[] bsBlank;
    // used by getInput(..) to allow for blocking, to wait on receiveInput(..) to return data.
    private final Object lockGetInput = new Object();

    protected final String host;
    protected final int port;

    public SSLBase(String host, int port) {
        this.host = host;
        this.port = port;
    }

    protected SSLContext getSSLContext() throws Exception {
        if (sslContext == null) {
            sslContext = createSSLContext();
        }
        return sslContext;
    }

    protected SSLEngine getSSLEngine() throws Exception {
        if (sslEngine == null) {
            sslEngine = createSSLEngine();
            sslEngine.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
            sslEngine.beginHandshake();
        }
        return sslEngine;
    }

    /**
     * This will reset and cause the SSL handshaking to have to be done again.
     * Should not be needed, except for testing.
     */
    public void resetSSL() throws Exception {
        sslEngine.getSession().invalidate();
        sslEngine.beginHandshake();
    }

    /**
     * This can be called to initialize the SSLEngine, etc.
     * This is not required.
     */
    public void initialize() throws Exception {
        //log("initialize");
        getSSLEngine();
    }
    
    
    /**
     * This is the only way to send data to the client computer. 
     */
    public void output(final byte[] bs, final int offset, final int len) throws Exception {
        //log("ouput");
        getSSLEngine();
        int consumed = 0;
        for (;;) {
            for (;;) {
                needUnwrap();
                if (!needWrap()) break;
            }
            consumed += wrap(bs, offset + consumed, len - consumed, false);
            if (consumed >= len) break;
        }
    }

    // check to see if ssl handshake needs input data
    private void needUnwrap() throws Exception {
        for (int i=0;; i++) {
            synchronized (lock) {
                SSLEngineResult.HandshakeStatus hs = getSSLEngine().getHandshakeStatus();
                if (hs != hs.NEED_UNWRAP) break;
                try {
                    log("need_unwrap, i="+i);
                    lock.wait(250); // wait for input to unwrap
                }
                catch (Exception e) {
                }
            }
        }
    }

    // check to see if ssl handshake needs output data
    private boolean needWrap() throws Exception {
        SSLEngineResult.HandshakeStatus hs = getSSLEngine().getHandshakeStatus();
        if (hs != hs.NEED_WRAP) return false;
        log("need_wrap");
        if (bsBlank == null) bsBlank = new byte[0];
        wrap(bsBlank, 0, 0, true);
        return true;
    }

    /**
     * used by needWrap(..) and output(..) to encrypt data and call sendOutput(..) If the data is not
     * all encrypted, then it will continue to call output(..) with the remaining data.
     */
    private int wrap(final byte[] bs, final int offset, final int len, final boolean bHandshakeOnly) throws Exception {
        // log("wrap");
        int consumed = 0;
        for (;;) {
            if (bsWrap == null) {
                int max = getSSLEngine().getSession().getPacketBufferSize();
                bsWrap = new byte[max];
                bbWrap = ByteBuffer.wrap(bsWrap, 0, max);
            }
            else bbWrap.clear();

            ByteBuffer bb = ByteBuffer.wrap(bs, offset, len);
            SSLEngineResult result = getSSLEngine().wrap(bb, bbWrap);

            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                bsWrap = new byte[bsWrap.length + 1024];
                bbWrap = ByteBuffer.wrap(bsWrap, 0, bsWrap.length);
                continue;
            }

            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                Runnable runnable;
                while ((runnable = sslEngine.getDelegatedTask()) != null) {
                    runnable.run();
                }
            }

            consumed = result.bytesConsumed();
            if (result.bytesProduced() > 0) {
                sendOutput(bsWrap, 0, result.bytesProduced(), bHandshakeOnly);
            }
            break;
        }
        return consumed;
    }

    protected void input(final byte[] bs, final int len, final boolean bHandshakeOnly) throws Exception {
        //log("input");
        ByteBuffer bb = ByteBuffer.wrap(bs, 0, len);

        // this will use the same buffer to unwrap the data. This assumes that the unwrapped data is <=
        // the encrypted data.
        ByteBuffer bb2 = ByteBuffer.wrap(bs, 0, bs.length);

        synchronized (lock) {
            SSLEngineResult result = getSSLEngine().unwrap(bb, bb2);
            switch (result.getStatus()) {
            case BUFFER_OVERFLOW: // should never happen for unwrap
                throw new SSLException("Buffer_Overflow, should not happen for an unwrap");
            case BUFFER_UNDERFLOW: // not enough data to do SSL, should never happen for unwrap: since
                                   // we make sure all data is in buffer
                throw new SSLException("Buffer_Underflow, should not happen for an unwrap");
            }

            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                Runnable runnable;
                while ((runnable = getSSLEngine().getDelegatedTask()) != null) {
                    runnable.run();
                }
            }
            if (!bHandshakeOnly) {
                receiveInput(bs, 0, result.bytesProduced());
            }
            lock.notifyAll();
        }
    }

    /**
     * Implemented by Server/Client to set up correct ssl context.
     */
    protected abstract SSLContext createSSLContext() throws Exception;

    protected abstract SSLEngine createSSLEngine() throws Exception;
    
    
    /**
     * This is the encrypted data that needs to go to the client. This should be overwritten to call the
     * SSLCLient input(..) method.
     */
    protected abstract void sendOutput(final byte[] bs, final int offset, final int len, final boolean bHandshakeOnly) throws Exception;

    private byte[] bsGetInput;

    /**
     * This is unencrypted data from the client, called from input(..)
     */
    protected void receiveInput(final byte[] bs, final int offset, final int len) throws Exception {
        //log("receiveInput");
        synchronized (lockGetInput) {
            bsGetInput = new byte[len];
            if (len > 0) System.arraycopy(bs, offset, bsGetInput, 0, len);
            lockGetInput.notifyAll();
        }
    }

    /**
     * performs a blocking read.
     */
    public byte[] input() throws Exception {
        //log("input");
        int consumed = 0;

        byte[] bs;
        for (;;) {
            for (;;) {
                needUnwrap();
                if (!needWrap()) break;
            }
            synchronized (lockGetInput) {
                if (bsGetInput != null) {
                    bs = bsGetInput;
                    bsGetInput = null;
                    break;
                }
                try {
                    lockGetInput.wait(500);
                }
                catch (Exception e) {
                    // TODO: handle exception
                }
            }
        }
        return bs;
    }
    
    protected void log(String msg) {
        System.out.println(msg);
    }
}
