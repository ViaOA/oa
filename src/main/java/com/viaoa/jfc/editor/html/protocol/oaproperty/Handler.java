/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jfc.editor.html.protocol.oaproperty;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException; 
import java.io.InputStream;
import java.net.URL; 
import java.net.URLConnection; 
import java.util.logging.Logger;

import com.viaoa.ds.OASelect;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectCacheDelegate;
import com.viaoa.util.OAConv;
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
        String query = u.getQuery();

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
        
        if (query == null || query.length() == 0) {
            String s = "id is required, URL="+u;
            LOG.fine(s);
            throw new IOException(s);
        }
        
        String[] params = query.split("&");
        String id = params[0];
        
        if (id == null || id.length() == 0) {
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
        
        int w = 0;
        int h = 0;
        for (int i=0; i<params.length; i++) {
            String s = params[i];
            int pos = s.indexOf("=");
            if (pos < 0) continue;
            s = s.substring(0, pos);
            String s2 = params[i].substring(pos+1); 
            if (s.equalsIgnoreCase("w")) {
               w = OAConv.toInt(s2);
            }
            else if (s.equalsIgnoreCase("h")) {
                h = OAConv.toInt(s2);
            }
        }
        if (w > 0 || h > 0) {
            BufferedImage bi = OAImageUtil.convertToBufferedImage(bx);
            bi = OAImageUtil.scaleDownToSize(bi, w, h);
            bx = OAImageUtil.convertToBytes(bi);
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


