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
package com.viaoa.jfc.editor.html.protocol.oaimage;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
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


/**
 * 
 * This to handle jpg images that jdk cant handle, that OA can
 * 
 * used by OAHTMLTextPane.setFixedSizeBackgroundImage
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
        
        byte[] bx = null;

        String srcName = u.getPath();
        File f = new File(srcName);
        
        BufferedImage bi = OAImageUtil.loadImage(f);
        if (bi != null) {
            bx = OAImageUtil.convertToBytes(bi);
        }
        
        String query = u.getQuery();
        if (query != null && query.length() > 0) {
            String[] params = query.split("&");

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
                bi = OAImageUtil.convertToBufferedImage(bx);
                bi = OAImageUtil.scaleDownToSize(bi, w, h);
                bx = OAImageUtil.convertToBytes(bi);
            }
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


