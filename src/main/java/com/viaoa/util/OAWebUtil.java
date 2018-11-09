package com.viaoa.util;

import java.awt.image.BufferedImage;
import java.net.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public class OAWebUtil {

    private static boolean bSetupHttpsAccess;
    
    /**
     * This is needed to use urlConnections to website that have https.
     */
    public static void setupHttpsAccess() throws Exception {
        if (bSetupHttpsAccess) return;
        try {
            _setupHttpsAccess();
            bSetupHttpsAccess = true;
        }
        catch (Exception e) {
            throw new RuntimeException("OAWebUti.setupHttpsAccess failed", e);
        }
    }
    
    protected static void _setupHttpsAccess() throws Exception {
        // Create a trust manager that does not validate certificate chains
       TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
           public java.security.cert.X509Certificate[] getAcceptedIssuers() {
               return null;
           }
           @Override
           public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
           }
           @Override
           public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
           }
       } };

       // Install the all-trusting trust manager
       SSLContext sc = SSLContext.getInstance("SSL");
       sc.init(null, trustAllCerts, new java.security.SecureRandom());
       HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

       // Create all-trusting host name verifier
       HostnameVerifier allHostsValid = new HostnameVerifier() {
           public boolean verify(String hostname, SSLSession session) {
               return true;
           }
       };

       // Install the all-trusting host verifier
       HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }
    
    
    
    
}
