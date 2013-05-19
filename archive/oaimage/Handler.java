package oaimage;

import java.io.ByteArrayInputStream;
import java.io.IOException; 
import java.io.InputStream;
import java.net.URL; 
import java.net.URLConnection; 
import java.util.logging.Logger;

import com.viaoa.ds.OASelect;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.OAString;


// see:
//  http://doc.novsu.ac.ru/oreilly/java/exp/ch09_06.htm

/**
 *  This is used to register a URL handler for url schema/protocol "oaproperty", 
 *  to load images from an OAObject property of type byte[]. 
 *  
 *  Example:  format is "oaproperty://" + className "/" + propertyName + "?" + Id
 *  URL url = new URL("oaproperty://com.vetplan.oa.Pet/picture?932");
 *  
 *  Note: this expects the property to be of type byte[], which is the "raw" version of image.
 *  
 *  !!! NOTE !!! must call:
 *    "com.viaoa.jfc.editor.html.protocol.classpath.Handler.register()" to have this registered.
 */
public class Handler extends com.viaoa.jfc.editor.html.protocol.classpath.Handler { 
    private final ClassLoader classLoader; 

    private static Logger LOG = Logger.getLogger(Handler.class.getName());
    
    public Handler() { 
        this.classLoader = ClassLoader.getSystemClassLoader();
    } 

    
    @Override 
    protected URLConnection openConnection(final URL u) throws IOException {
        LOG.fine("URL="+u);
        String className = u.getAuthority();
        String propName = u.getPath();
        String id = u.getQuery();

        if (className == null || className.length() == 0) {
            String s = "className is required, URL="+u;
            LOG.fine(s);
            throw new IOException(s);
        }
        if (propName== null || propName.length() == 0) {
            String s = "propertyName is required, URL="+u;
            LOG.fine(s);
            throw new IOException(s);
        }
        propName = OAString.convert(propName, "/",  null);
        if (id== null || id.length() == 0) {
            String s = "id is required, URL="+u;
            LOG.fine(s);
            throw new IOException(s);
        }
                
        if (id.toLowerCase().startsWith("id=")) {
            if (id.length() == 3) id = "";
            else id = id.substring(3);
        }
        
        Class c;
        try {
            c = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            String s = "class not found for image property, class="+className; 
            LOG.fine(s);
            throw new IOException(s);
        }
        final Class clazz = c;

        LOG.fine("getting image, class="+className+", property="+propName+", id="+id);

        OAObject obj;
        obj = OAObjectCacheDelegate.get(c, id);
        if (obj == null) {
            OASelect sel = new OASelect(clazz); 
            sel.select("ID = ?", new Object[] {id});
            obj = (OAObject) sel.next();
            sel.cancel();
        }
        if (obj == null) {
            String s = "object not found, url="+u;
            LOG.fine(s);
            throw new IOException(s);
        }   
        
        byte[] bx;
        try {
             bx = (byte[]) obj.getProperty(propName);
             if (bx == null) throw new IOException("could not read image from property, url="+u);
        }
        catch (Exception e) {
            String s = "read image from property error, url="+u+", exception="+e;
            LOG.fine(s);
            throw new IOException(s, e);
        }
        
        final byte[] bs = bx;
        
        URLConnection uc = new URLConnection(u) {
            synchronized public void connect() throws IOException {
            } 
         
            synchronized public InputStream getInputStream() throws IOException {
                ByteArrayInputStream bais = new ByteArrayInputStream(bs);
                return bais;
            } 
         
            public String getContentType() {
                return guessContentTypeFromName("test.jpg");  // this needs to be the same that is used by OAImageUtil.convertToBytes()
            } 
            
        };
        
        return uc; 
    }
    
} 


