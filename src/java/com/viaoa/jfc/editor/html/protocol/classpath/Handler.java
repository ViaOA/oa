package com.viaoa.jfc.editor.html.protocol.classpath;

import java.io.IOException; 
import java.net.InetAddress;
import java.net.URL; 
import java.net.URLConnection; 
import java.net.URLStreamHandler; 
import java.net.UnknownHostException;
import java.util.logging.Logger;


/**
 *  This is used to register a URL handler for url schema/protocol "classpath", 
 *  to load files from classpath. 
 *  This will allow html linkref to find files within the compiled code.
 *  
 *  Example:  format is "classpath://" + javapackage "/" + filename
 *  URL url = new URL("classpath://com.viaoa.jfc.editor.html.css/html.css");
 *  
 *  Ex:
 *  <img src='classpath://com.vetplan.view.image/exam.jpg' height='10' width='8'>
 *  
 *  !!! NOTE !!!: must call static method "register()" or "jwsregister" to have this URL Handler used/registered.
 *  
 
    NOTES: 3 ways to "register" URL protocol handlers:
    
    1: System Property on the command line or as a JWS property
        -Djava.protocol.handler.pkgs=com.viaoa.jfc.editor.html.protocol
        <property name="java.protocol.handler.pkgs" value="com.viaoa.jfc.editor.html.protocol"/>
            * BUG: in 1.6, this will double launch and still not work
            * !! jws uses more then one classloader and the correct one is never set
                if starting a JWS app, it's best to call URLStreamHandler.jwsregister()
    
    2: have program set system property
        String sp = "com.viaoa.jfc.editor.html.protocol"; 
        System.setProperty( "java.protocol.handler.pkgs", s );
            * this does not work in jws (see #1)
    
    3: URL.setURLStreamHandlerFactory(...)
         this works, unless other code (ex: Tomcat, etc) has already called it.

    Note:  Hub2ImageHandler has static method that calls register.

 */
public class Handler extends URLStreamHandler { 
    private static boolean bRegister;
    private static boolean bRegisterResult;
    private static boolean bJWSRegistered;

    private static Logger LOG = Logger.getLogger(Handler.class.getName());
    
    
    

    /**
       This will need to set the system property, which is the same as runtime setting:
       -Djava.protocol.handler.pkgs=com.viaoa.jfc.editor.html.protocol
       Note: for JWS, use method jwsregister instead, since jws uses a classloader that can not be accessed.     
     */
    public static boolean register() {
        if (bRegister) return bRegisterResult;
        if (bJWSRegistered) return true;
        bRegister = true;
        LOG.fine("setting register using System.setProperty");
        
        String s = System.getProperty("java.protocol.handler.pkgs", "");
        if (s == null) s = "";
        if (s.length() > 0) s += "|";
        
        String sx = "com.viaoa.jfc.editor.html.protocol";
        if (s.indexOf(sx) >= 0) return true;
        
        
        System.setProperty( "java.protocol.handler.pkgs", s + sx );

        s = System.getProperty("java.protocol.handler.pkgs", "");
        if (s == null) s = "";
        int pos = (s.indexOf(sx));
        bRegisterResult = (pos >= 0);

        return bRegisterResult;
    }


    /**
     *  This should be used for JWS applications.  (see notes above)
     *  
     *  it is also necessary to call:
     *  System.setSecurityManager(null);  // needed for setting OA URL protocol handlers        
     *  
     *  
     */
    public static void jwsregister() {
        if (bJWSRegistered) return;
        bJWSRegistered = true;
        LOG.config("setting register for JWS");
        
        java.net.URL.setURLStreamHandlerFactory(new java.net.URLStreamHandlerFactory() {
            public java.net.URLStreamHandler createURLStreamHandler(final String protocol) {

                if(protocol == null) return null;
                if (protocol.equalsIgnoreCase("classpath")) {
                    return new Handler();
                }
                if (protocol.equalsIgnoreCase("oaproperty")) {
                    return new com.viaoa.jfc.editor.html.protocol.oaproperty.Handler();
                }
                return null;
            }
        });
    }
    
    
    // hack: so that the host name in the URL (which is the java classpath for this handler) is not resolved/looked-up.  
    //       the superClass uses the resolved address for hashing (ouch)
    private static InetAddress LocalHost;
    @Override 
    protected synchronized InetAddress getHostAddress(URL u) {
        if (LocalHost == null) {
            try {
                LocalHost = InetAddress.getLocalHost();
            }
            catch (UnknownHostException e) {
            }
        }
        return LocalHost;
    }    
    
    
    
    private static ClassLoader classLoader;
    @Override 
    protected URLConnection openConnection(URL u) throws IOException {
        LOG.finer("URL="+u);
        String path = u.getAuthority();
        if (path == null) path = "";
        if (path.length() > 0) {
            path = path.replace('.', '/');
        }
        path += u.getPath();  // includes a leading '/'
         
        
        if (classLoader == null) {
            classLoader = Handler.class.getClassLoader();
        }
        URL resourceUrl = classLoader.getResource(path);
        
        if (resourceUrl == null) {
            String s = "classpath resource not found, path="+path;
            LOG.fine(s);
            throw new IOException(s);
        }
        URLConnection con = resourceUrl.openConnection();
        return con;
    }
    

    
    
    public static void main(String[] args) throws Exception {
        boolean b = Handler.register();
        System.out.println("b="+b);
              
        URL url = new URL("classpath://com.viaoa.jfc.editor.html.css/html.css");
        System.out.println("url="+url);
        
        Object objx = url.getContent();
        System.out.println("content="+objx);
    }
} 
