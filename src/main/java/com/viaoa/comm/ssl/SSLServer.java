package com.viaoa.comm.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * SSLServer used to encrypt data
 * @author vvia
 *
 */
public abstract class SSLServer extends SSLBase {

    public SSLServer(String host, int port) {
        super(host, port);
    }

    protected SSLContext createSSLContext() throws Exception {
        // 20171118
        SSLContext sslContext = SSLContext.getInstance("TLS");
// SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        //was: SSLContext sslContext = SSLContext.getInstance("SSLv3");

        KeyStore keystore = KeyStore.getInstance("JKS");

        // see keystore.txt 
        InputStream is = SSLServer.class.getResourceAsStream("sslserver.jks");
        if (is == null) throw new IOException("sslserver.jks not found");
        keystore.load(is, "vince1".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keystore, "vince1".toCharArray());

        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }

    protected SSLEngine createSSLEngine() throws Exception {
        SSLEngine sslEngine = getSSLContext().createSSLEngine(host, port);
        sslEngine.setUseClientMode(false);
        sslEngine.setNeedClientAuth(false);
        return sslEngine;
    }

    @Override
    protected void log(String msg) {
        System.out.println("SERVER: "+msg);
    }

    public static void main(String[] args) throws Exception {
        SSLServerSocketFactory ssf = (SSLServerSocketFactory)SSLServerSocketFactory.getDefault();

        String[] defaultCiphers = ssf.getDefaultCipherSuites();
        String[] availableCiphers = ssf.getSupportedCipherSuites();
        int xx = 4;
        xx++;
    }

}
