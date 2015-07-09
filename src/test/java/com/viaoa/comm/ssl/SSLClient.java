package com.viaoa.comm.ssl;


import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLClient {

    /**
     * Preferred encryption cipher to use for SSL sockets.
     */
    public static final String[] PREFERRED_CIPHER_NAMES = new String[] {"SSL_RSA_WITH_RC4_128_MD5"}; 
    
    private SSLContext _sslContextServer;
    private SSLContext _sslContextClient;

    String _host = "localhost";
    int _port = 1100;
    

    private SSLContext getClientSSLContext() throws Exception {
        if (_sslContextClient == null) {
            _sslContextClient = SSLContext.getInstance("SSLv3");

            KeyStore keystore = KeyStore.getInstance("JKS");

            InputStream isJKS = this.getClass().getResourceAsStream("icesslclient.jks");
            keystore.load(isJKS, "password".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keystore);

            TrustManager[] trustManagers = tmf.getTrustManagers();

            _sslContextClient.init(null, trustManagers, null);
        }
        return _sslContextClient;
    }

    protected SSLContext getServerSSLContext() throws Exception 
    {
        if (_sslContextServer == null) {
            _sslContextServer = SSLContext.getInstance("SSLv3");
            
            KeyStore keystore = KeyStore.getInstance("JKS");
            
            InputStream is = this.getClass().getResourceAsStream("icesslserver.jks");
            keystore.load(is, "password".toCharArray());
            
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keystore, "password".toCharArray());
            
            _sslContextServer.init(kmf.getKeyManagers(), null, null);
        }
        return _sslContextServer;
    }
    
    
    
    
    public void test() throws Exception {
        SSLEngine sslEngineServer = getServerSSLContext().createSSLEngine(_host, _port);
        sslEngineServer.setUseClientMode(false);
        sslEngineServer.setNeedClientAuth(false);
        sslEngineServer.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
        
        SSLEngine sslEngineClient = getClientSSLContext().createSSLEngine(_host, _port);
        sslEngineClient.setUseClientMode(true);
        sslEngineClient.setWantClientAuth(false);
        sslEngineClient.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);

        
        
        byte[] bs = new byte[0];
        ByteBuffer bbBlank = ByteBuffer.wrap(bs, 0, 0);
        
        int max = sslEngineServer.getSession().getPacketBufferSize(); 
        byte[] bs1 = new byte[max];
        ByteBuffer bb1 = ByteBuffer.wrap(bs1, 0, max);;


        SSLEngineResult.HandshakeStatus hsServer = sslEngineServer.getHandshakeStatus(); // need_wrap
        SSLEngineResult.HandshakeStatus hsClient = sslEngineClient.getHandshakeStatus(); // need_unwrap
        
        // 1: client creates a handshake
        SSLEngineResult result = sslEngineClient.wrap(bbBlank, bb1);
        
        hsServer = sslEngineServer.getHandshakeStatus(); // need_wrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_unwrap

        // 2: server gets client data, needs to unwrap
        bb1 = ByteBuffer.wrap(bs1, 0, result.bytesProduced());;
        byte[] bs2 = new byte[max];
        ByteBuffer bb2 = ByteBuffer.wrap(bs2, 0, max);;
        
        SSLEngineResult result2 = sslEngineServer.unwrap(bb1, bb2);
        Runnable runnable;
        switch (result2.getHandshakeStatus()) {
            case NEED_TASK:
                while ((runnable = sslEngineServer.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
        }
        
        hsServer = sslEngineServer.getHandshakeStatus(); // need_wrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_unwrap

        // 3: server needs a wrap
        bbBlank.clear();
        bb2 = ByteBuffer.wrap(bs2, 0, max);;
        result = sslEngineServer.wrap(bbBlank, bb2);
        
        // 4: client needs to unwrap
        bb1 = ByteBuffer.wrap(bs1, 0, max);;
        bb2 = ByteBuffer.wrap(bs2, 0, result.bytesProduced());;
        result = sslEngineClient.unwrap(bb2, bb1);
        switch (result.getHandshakeStatus()) {
            case NEED_TASK:
                while ((runnable = sslEngineClient.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
        }
            
        hsServer = sslEngineServer.getHandshakeStatus(); // need_unwrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_wrap
        
        // 5: client needs to wrap
        bbBlank.clear();
        bb2 = ByteBuffer.wrap(bs2, 0, max);;
        result = sslEngineClient.wrap(bbBlank, bb2);
        
        // 6: server needs to unwrap
        bb1 = ByteBuffer.wrap(bs1, 0, max);;
        bb2 = ByteBuffer.wrap(bs2, 0, result.bytesProduced());;
        result = sslEngineServer.unwrap(bb2, bb1);
        switch (result.getHandshakeStatus()) {
            case NEED_TASK:
                while ((runnable = sslEngineServer.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
        }
            
        hsServer = sslEngineServer.getHandshakeStatus(); // need_unwrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_wrap
    }        
        
        
    public void test2() throws Exception {
        SSLEngine sslEngineServer = getServerSSLContext().createSSLEngine(_host, _port);
        sslEngineServer.setUseClientMode(false);
        sslEngineServer.setNeedClientAuth(false);
        sslEngineServer.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);
        
        SSLEngine sslEngineClient = getClientSSLContext().createSSLEngine(_host, _port);
        sslEngineClient.setUseClientMode(true);
        sslEngineClient.setWantClientAuth(false);
        sslEngineClient.setEnabledCipherSuites(PREFERRED_CIPHER_NAMES);

        byte[] bsBlank = new byte[0];
        ByteBuffer bbBlank = ByteBuffer.wrap(bsBlank, 0, 0);
        
        int max = sslEngineServer.getSession().getPacketBufferSize(); 
        byte[] bs = new byte[max];
        ByteBuffer bb = ByteBuffer.wrap(bs, 0, max);;

        byte[] bs2 = new byte[max];
        ByteBuffer bb2 = ByteBuffer.wrap(bs, 0, max);;
        
        Runnable runnable;
        SSLEngineResult.HandshakeStatus hs;
        SSLEngineResult result = null;
        
        for (;;) {
            hs = sslEngineServer.getHandshakeStatus();
            switch (hs) {
                case NEED_TASK:
                    while ((runnable = sslEngineServer.getDelegatedTask()) != null) {
                        runnable.run();
                    }
                    break;
                case NEED_WRAP:
                    bbBlank.clear();
                    bb = ByteBuffer.wrap(bs, 0, max);;
                    result = sslEngineServer.wrap(bbBlank, bb);
                    break;
                case NEED_UNWRAP:
                    bb = ByteBuffer.wrap(bs, 0, result.bytesProduced());;
                    bb2 = ByteBuffer.wrap(bs2, 0, max);
                    result = sslEngineServer.unwrap(bb, bb2);
                    switch (result.getHandshakeStatus()) {
                        case NEED_TASK:
                            while ((runnable = sslEngineServer.getDelegatedTask()) != null) {
                                runnable.run();
                            }
                            break;
                        case NEED_WRAP:
                            break;
                        default:
                    }
                    break;
            }
            
            
            hs = sslEngineClient.getHandshakeStatus();
            switch (hs) {
                case NEED_TASK:
                case NEED_WRAP:
                case NEED_UNWRAP:
            }
            
            
            
        }
        
        
        // 1: client creates a handshake
        SSLEngineResult result = sslEngineClient.wrap(bbBlank, bb1);
        
        hsServer = sslEngineServer.getHandshakeStatus(); // need_wrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_unwrap

        // 2: server gets client data, needs to unwrap
        bb1 = ByteBuffer.wrap(bs1, 0, result.bytesProduced());;
        byte[] bs2 = new byte[max];
        ByteBuffer bb2 = ByteBuffer.wrap(bs2, 0, max);;
        
        SSLEngineResult result2 = sslEngineServer.unwrap(bb1, bb2);
        Runnable runnable;
        switch (result2.getHandshakeStatus()) {
            case NEED_TASK:
                while ((runnable = sslEngineServer.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
        }
        
        hsServer = sslEngineServer.getHandshakeStatus(); // need_wrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_unwrap

        // 3: server needs a wrap
        bbBlank.clear();
        bb2 = ByteBuffer.wrap(bs2, 0, max);;
        result = sslEngineServer.wrap(bbBlank, bb2);
        
        // 4: client needs to unwrap
        bb1 = ByteBuffer.wrap(bs1, 0, max);;
        bb2 = ByteBuffer.wrap(bs2, 0, result.bytesProduced());;
        result = sslEngineClient.unwrap(bb2, bb1);
        switch (result.getHandshakeStatus()) {
            case NEED_TASK:
                while ((runnable = sslEngineClient.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
        }
            
        hsServer = sslEngineServer.getHandshakeStatus(); // need_unwrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_wrap
        
        // 5: client needs to wrap
        bbBlank.clear();
        bb2 = ByteBuffer.wrap(bs2, 0, max);;
        result = sslEngineClient.wrap(bbBlank, bb2);
        
        // 6: server needs to unwrap
        bb1 = ByteBuffer.wrap(bs1, 0, max);;
        bb2 = ByteBuffer.wrap(bs2, 0, result.bytesProduced());;
        result = sslEngineServer.unwrap(bb2, bb1);
        switch (result.getHandshakeStatus()) {
            case NEED_TASK:
                while ((runnable = sslEngineServer.getDelegatedTask()) != null) {
                    runnable.run();
                }
                break;
            case NEED_WRAP:
                break;
            default:
        }
            
        hsServer = sslEngineServer.getHandshakeStatus(); // need_unwrap
        hsClient = sslEngineClient.getHandshakeStatus(); // need_wrap
    }        
        

    
    public static void main(String[] args) throws Exception {
        SSLClient test = new SSLClient();
        test.test();
    }
    
    
    
    
}
