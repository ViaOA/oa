package com.viaoa.comm.ssl;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;

public abstract class SSLServer {

    /**
     * Preferred encryption cipher to use for SSL sockets.
     */
    public static final String[] PREFERRED_CIPHER_NAMES = new String[] { "SSL_RSA_WITH_RC4_128_MD5" };

    private SSLContext sslContext;
    private SSLEngine sslEngine;

    private byte[] bsWrap;
    private ByteBuffer bbWrap;
    private final Object lock = new Object();
    private byte[] bsBlank;

    private String host;
    private int port;
    
    public SSLServer(String host, int port) {
        this.host = host;
        this.port = port;
    }
    
    protected SSLContext getSSLContext() throws Exception {
        if (sslContext == null) {
            sslContext = SSLContext.getInstance("SSLv3");

            KeyStore keystore = KeyStore.getInstance("JKS");

            InputStream is = this.getClass().getResourceAsStream("server.jks");
            keystore.load(is, "password".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, "password".toCharArray());

            sslContext.init(kmf.getKeyManagers(), null, null);
        }
        return sslContext;
    }

    public SSLEngine getSSLEngine() throws Exception {
        if (sslEngine == null) {
            sslEngine = getSSLContext().createSSLEngine(host, port);
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(false);
            sslEngine.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
        }
        return sslEngine;
    }


    /**
     * Use this to send data to the client computer.
     * This will check to see if any SSL handshaking needs to be done.
     */
    public void output(final byte[] bs, final int offset, final int len) throws Exception {
        for (;;) {
            needUnwrap();
            if (!needWrap()) break;
        }
        wrap(bs, offset, len, false);
    }
    private boolean needWrap() throws Exception {
        for (;;) {
            synchronized (lock) {
                SSLEngineResult.HandshakeStatus hs = sslEngine.getHandshakeStatus();
                if (hs != hs.NEED_WRAP) return false;
                if (bsBlank == null) bsBlank = new byte[0];
                wrap(bsBlank, 0, 0, true);
                lock.notifyAll();
                break;
            }
        }
        return true;
    }
    private void needUnwrap() throws Exception {
        for (;;) {
            synchronized (lock) {
                SSLEngineResult.HandshakeStatus hs = sslEngine.getHandshakeStatus();
                if (hs != hs.NEED_UNWRAP) return;
                lock.wait(); // wait for input to unwrap
            }
        }
    }
    
    /**
     * used by output(..) to encrypt data and call sendOutput(..)
     * If the data is not all encrypted, then it will continue to call output(..) with the remaining data.
     */
    private void wrap(final byte[] bs, final int offset, final int len, final boolean bHandshakeOnly) throws Exception {
        if (bsWrap == null) {
            int max = sslEngine.getSession().getPacketBufferSize();
            bsWrap = new byte[max];
            bbWrap = ByteBuffer.wrap(bsWrap, 0, max);
        }
        else bbWrap.clear();

        for (;;) {
            ByteBuffer bb = ByteBuffer.wrap(bs, offset, len);
            SSLEngineResult result = sslEngine.wrap(bb, bbWrap);

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
            if (bHandshakeOnly) break;
            
            final int consumed = result.bytesConsumed();
            sendOutput(bsWrap, 0, result.bytesProduced(), consumed < len, consumed > 0);

            if (consumed < len) {
                output(bs, offset+consumed, len-consumed);
            }
            break;
        }

    }

    protected void input(final byte[] bs, final int len, final boolean bHandshakeData, final boolean bRealData) throws Exception {
        ByteBuffer bb = ByteBuffer.wrap(bs, 0, len);

        // this will use the same buffer to unwrap the data. This assumes that the unwrapped data is <= the encrypted data.
        ByteBuffer bb2 = ByteBuffer.wrap(bs, 0, bs.length);

        synchronized (lock) {
            SSLEngineResult result = getSSLEngine().unwrap(bb, bb2);
            switch (result.getStatus()) {
            case BUFFER_OVERFLOW: // should never happen for unwrap
                throw new SSLException("Buffer_Overflow, should not happen for an unwrap");
            case BUFFER_UNDERFLOW: // not enough data to do SSL,  should never happen for unwrap: since we make sure all data is in buffer
                throw new SSLException("Buffer_Underflow, should not happen for an unwrap");
            }

            if (result.getHandshakeStatus() == HandshakeStatus.NEED_TASK) {
                Runnable runnable;
                while ((runnable = getSSLEngine().getDelegatedTask()) != null) {
                    runnable.run();
                }
            }
            if (bHandshakeData) {
                lock.notifyAll();
            }
        }
    }

    /**
     * This should be overwritten to call the SSLCLient input(..) method.
     */
    protected abstract void sendOutput(final byte[] bs, final int offset, final int len, final boolean bHandshakeData, final boolean bRealData);
    
    

    public static void main(String[] args) throws Exception {
        SSLServer ser = new SSLServer("localhost", 1101) {
            @Override
            protected void sendOutput(byte[] bs, int offset, int len, boolean bHandshakeData, boolean bRealData) {
                // TODO Auto-generated method stub
                
            }
        };
        
    }

}
