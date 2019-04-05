package com.viaoa.comm.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * SSLClient used to encrypt data
 * @author vvia
 *
 */
public abstract class SSLClient extends SSLBase {

    public SSLClient(String host, int port) {
        super(host, port);
    }
    
    protected SSLContext createSSLContext() throws Exception {
        // 20171118
        SSLContext sslContext = SSLContext.getInstance("TLS");
// SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        //was: SSLContext sslContext = SSLContext.getInstance("SSLv3");

        KeyStore keystore = KeyStore.getInstance("JKS");

        // see keystore.txt 
        InputStream is = SSLClient.class.getResourceAsStream("sslclient.jks");
        if (is == null) throw new IOException("sslclient.jks not found");
        keystore.load(is, "vince1".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keystore);

        TrustManager[] trustManagers = tmf.getTrustManagers();

        sslContext.init(null, trustManagers, null);
        return sslContext;
    }

    protected SSLEngine createSSLEngine() throws Exception {
        SSLEngine sslEngine = getSSLContext().createSSLEngine(host, port);
        sslEngine.setUseClientMode(true);
        sslEngine.setWantClientAuth(false);
        return sslEngine;
    }
    @Override
    protected void log(String msg) {
        System.out.println("CLIENT: "+msg);
    }
}
