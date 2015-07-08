package com.viaoa.comm;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public abstract class SSLClientTest {

    /**
     * Preferred encryption cipher to use for SSL sockets.
     */
    public static final String[] PREFERRED_CIPHER_NAMES = new String[] {"SSL_RSA_WITH_RC4_128_MD5"}; 
    
    private SSLContext _sslContext;
    private SSLEngine _sslEngine;

    private SSLContext getSSLContext() throws Exception {
        if (_sslContext == null) {
            _sslContext = SSLContext.getInstance("SSLv3");

            KeyStore keystore = KeyStore.getInstance("JKS");

            InputStream isJKS = this.getClass().getResourceAsStream("icesslclient.jks");
            keystore.load(isJKS, "password".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            TrustManager[] trustManagers = tmf.getTrustManagers();

            _sslContext.init(null, trustManagers, null);
        }
        return _sslContext;
    }

    String _host = "localhost";
    int _port = 1100;
    
    protected SSLEngine getSSLEngine() throws Exception {
        if (_sslEngine == null) {
            _sslEngine = getSSLContext().createSSLEngine(_host, _port);
            _sslEngine.setUseClientMode(true);
            _sslEngine.setWantClientAuth(false);
            _sslEngine.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
        }
        return _sslEngine;
    }


    
    abstract void sendEncryptedData(byte[] bs, int offset, int len, boolean bForHandshake) throws Exception;
    

    /** buffer used for SSL sockets */
    private byte[] _writeSSLBuffer;
    private int _writeSSLBufferLength;
    /** used for SSL encryption */
    private ByteBuffer _writeSSLByteBuffer;

    private void encrypt(byte[] bs, int offset, int len) throws Exception {
        int consumed = 0;

        SSLEngineResult.HandshakeStatus hs = getSSLEngine().getHandshakeStatus();
        for ( ;; ) {
            hs = getSSLEngine().getHandshakeStatus();
            consumed += _encrypt(bs, offset+consumed, len-consumed);
            hs = getSSLEngine().getHandshakeStatus();
            if (_writeSSLBufferLength > 0) { 
                sendEncryptedData(_writeSSLBuffer, 0, _writeSSLBufferLength, (consumed != len));
            }
            hs = getSSLEngine().getHandshakeStatus();
            if (consumed >= len) break;
        }
    }
    private int _encrypt(byte[] bs, int offset, int len) throws Exception {

        if (_writeSSLBuffer == null || _writeSSLBuffer.length < len) {
            int x = getSSLEngine().getSession().getPacketBufferSize(); 
            x = Math.max(x, len); 
            _writeSSLBuffer = new byte[x];
            _writeSSLByteBuffer = ByteBuffer.wrap(_writeSSLBuffer, 0, x);;
        }
        else {
            _writeSSLByteBuffer.clear();
        }
        
        ByteBuffer buffer = ByteBuffer.wrap(bs, offset, len);
        SSLEngineResult result = getSSLEngine().wrap(buffer, _writeSSLByteBuffer);

        if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            int x = _writeSSLBuffer.length+1024;
            _writeSSLBuffer = new byte[x];
            _writeSSLByteBuffer = ByteBuffer.wrap(_writeSSLBuffer, 0, x);
            return _encrypt(bs, offset, len);
        }

        int consumed = result.bytesConsumed();
        _writeSSLBufferLength = result.bytesProduced();
        
        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            Runnable runnable;
            while ((runnable = getSSLEngine().getDelegatedTask()) != null) {
                runnable.run();
            }
        }
        
        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_UNWRAP) {
            // need to get more data from handshake
        }
        return consumed;
    }

    private byte[] bsOutput;
    protected int decrypt(byte[] bs, int len, boolean bForHandshake) throws Exception {
        ByteBuffer inBuffer = ByteBuffer.wrap(bs, 0, len);
        
//qqqqqqqqqq NO, needs to use a different buffer        
        // this will use the same buffer to unwrap the data. This assumes that the unwrapped data is <= the encrypted data.
//        byte[] bs2 = new byte[bs.length];
// would need to return a new byte[]
        
        ByteBuffer inBufferUnwrapped = ByteBuffer.wrap(bs, 0, bs.length); 

        SSLEngineResult result = null;
        int consumed = 0;
        int produced = 0;

        for (int i=0; consumed < len;i++) {
            result = getSSLEngine().unwrap(inBuffer, inBufferUnwrapped);
            consumed += result.bytesConsumed();
            produced += result.bytesProduced();
            
            switch (result.getStatus()) {
            case BUFFER_OVERFLOW: // should never happen for unwrap
                throw new SSLException("Buffer_Overflow, should not happen for an unwrap");
            case BUFFER_UNDERFLOW: // not enough data to do SSL,  should never happen for unwrap: since we make sure all data is in buffer
                throw new SSLException("Buffer_Underflow, should not happen for an unwrap");
            }         
            
            switch (result.getHandshakeStatus()) {
            case NEED_TASK:
                Runnable runnable;
                while ((runnable = getSSLEngine().getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
//qqqqqq needs to send encrypted data for handshaking                 
                encrypt(new byte[0], 0, 0);
                break;
            default:
            }
SSLEngineResult.HandshakeStatus hs = getSSLEngine().getHandshakeStatus();
int xx = 4;
xx++;
            if (bForHandshake) break;
        }
        return produced;
    }


    private SSLClientTest sslClientTest2;
    public SSLClientTest getSSLClientTest2() {
        if (sslClientTest2 == null) {
            sslClientTest2 = new SSLClientTest() {
                @Override
                void sendEncryptedData(byte[] bs, int offset, int len, boolean bForHandshake) throws Exception {
                    SSLClientTest.this.decrypt(bs, len, bForHandshake);
                }
            };
        }
        return sslClientTest2;
    }
    
    
    public void test() throws Exception {
        getSSLContext();
        getSSLEngine();
        
//        getSSLEngine().beginHandshake();

        SSLEngineResult.HandshakeStatus hs = getSSLEngine().getHandshakeStatus();
        
        String s = "vince";
        byte[] bs = s.getBytes();
        encrypt(bs, 0, bs.length);
    }    
    
    
    public static void main(String[] args) throws Exception {
        SSLClientTest test = new SSLClientTest() {
            @Override
            void sendEncryptedData(byte[] bs, int offset, int len, boolean bForHandshake) throws Exception {
                getSSLClientTest2().decrypt(bs, len, bForHandshake);
            }
        };
        
        test.test();
    }
    
    
    
    
}
