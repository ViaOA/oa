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
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLTest {

    /**
     * Preferred encryption cipher to use for SSL sockets.
     */
    public static final String[] PREFERRED_CIPHER_NAMES = new String[] {"SSL_RSA_WITH_RC4_128_MD5"}; 
    
    private SSLContext _sslContext;
    private SSLEngine _sslEngineServer;
    private SSLEngine _sslEngineClient;

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
    
    protected SSLEngine getServerSSLEngine() throws Exception {
        if (_sslEngineServer == null) {
            _sslEngineServer = getSSLContext().createSSLEngine(_host, _port);
            _sslEngineServer.setUseClientMode(false);
            _sslEngineServer.setNeedClientAuth(false);
            _sslEngineServer.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
        }
        return _sslEngineServer;
    }

    protected SSLEngine getClientSSLEngine() throws Exception {
        if (_sslEngineClient == null) {
            _sslEngineClient = getSSLContext().createSSLEngine(_host, _port);
            _sslEngineClient.setUseClientMode(true);
            _sslEngineClient.setWantClientAuth(false);
            _sslEngineClient.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
        }
        return _sslEngineClient;
    }

    

    /** buffer used for SSL sockets */
    private byte[] _writeSSLBuffer;
    /** used for SSL encryption */
    private ByteBuffer _writeSSLByteBuffer;

    
    private int encrypt(byte[] bs, int offset, int len) throws Exception {
        if (_writeSSLBuffer == null || _writeSSLBuffer.length < len) {
            int x = _sslEngineServer.getSession().getPacketBufferSize(); 
            x = Math.max(x, len); 
            _writeSSLBuffer = new byte[x];
            _writeSSLByteBuffer = ByteBuffer.wrap(_writeSSLBuffer, 0, x);;
        }
        else {
            _writeSSLByteBuffer.clear();
        }
        ByteBuffer buffer = ByteBuffer.wrap(bs, offset, len);
        SSLEngineResult result = getServerSSLEngine().wrap(buffer, _writeSSLByteBuffer);
        
        
        if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            int x = _writeSSLBuffer.length+1024;
            _writeSSLBuffer = new byte[x];
            _writeSSLByteBuffer = ByteBuffer.wrap(_writeSSLBuffer, 0, x);
            return encrypt(bs, offset, len);
        }

        int consumed = result.bytesConsumed();
        int produced = result.bytesProduced();
        
        if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
            Runnable runnable;
            while ((runnable = _sslEngineServer.getDelegatedTask()) != null) {
                runnable.run();
            }
        }
        if (consumed < len) {
            produced *= -1;  // flag to know that the data is for handshaking, and that the "bs' needs to be resent.
        }
        return produced;
    }




    protected int decrypt(byte[] bs, int len) throws SSLException 
    {
        ByteBuffer inBuffer = ByteBuffer.wrap(bs, 0, len);
        
        // this will use the same buffer to unwrap the data. This assumes that the unwrapped data is <= the encrypted data.
        ByteBuffer inBufferUnwrapped = ByteBuffer.wrap(bs, 0, bs.length); 

        SSLEngineResult result = null;
        int consumed = 0;
        int produced = 0;

        for (int i=0; consumed < len;i++) {
            result = _sslEngineServer.unwrap(inBuffer, inBufferUnwrapped);
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
                while ((runnable = _sslEngineServer.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
            }
        }
        return produced;
    }


    public void test() throws Exception {
        getSSLContext();
        getServerSSLEngine();
        getClientSSLEngine();

        
        SSLEngineResult.HandshakeStatus hs = _sslEngineClient.getHandshakeStatus();
        
        
        
        String s = "vince";
        
        byte[] bs = s.getBytes();
        
        int x = s.length();
        x = encrypt(bs, 0, x);
        
        x = encrypt(bs, 0, x);
        x = encrypt(bs, 0, x);
        x = encrypt(bs, 0, x);
        
        
        
    }    
    
    
    public static void main(String[] args) throws Exception {
        SSLTest test = new SSLTest();
        test.test();
    }
    
    
    
    
}
