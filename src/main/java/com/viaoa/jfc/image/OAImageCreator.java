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
package com.viaoa.jfc.image;

import java.io.*;
import java.awt.*;
import java.awt.image.*;
import com.sun.image.codec.jpeg.*;
import com.viaoa.util.OAFile;

// save as PNG file
// import javax.imageio.*;
// ImageIO.write((BufferedImage) ic.getImage(),"PNG", new FileOutputStream("TEST.png"));

// creates a gif or jpg out of an image


public class OAImageCreator implements java.io.Serializable {
    protected Dimension dimSize;
    protected BufferedImage image;
    protected Color transparentColor;
    
    public Graphics getGraphics(Dimension d) {
        dimSize = d;
        if (d == null) {
            image = null;
            return null;
        }
        image = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
        return image.getGraphics();
    }

    public Image getImage() {
        return image;
    }
    
    public Dimension getSize() {
        return dimSize;
    }

    public Color getTransparentColor() {
         return transparentColor;
    }

    public void setTransparentColor(Color c) {
         transparentColor = c;
    }
    
    protected void verify(String fileName) {
        if (dimSize == null) throw new IllegalArgumentException("must setSize before calling");
        if (fileName == null) throw new IllegalArgumentException("fileName can not be null");
        if (image == null) throw new IllegalArgumentException("image is null");
    }
    
    public void createGif(String fileName) throws Exception {
        verify(fileName);
        OAFile.mkdirsForFile(fileName);
        File f = new File(fileName);
        FileOutputStream fos = new FileOutputStream(f); 
        GifEncoder ge = new GifEncoder(image, fos);
        if (transparentColor != null) {
            ge.setTransparentRgb(transparentColor.getRGB());
        }
        ge.encode();
        fos.flush(); 
        fos.close();
    }

    public void createJpeg(String fileName) throws Exception {
        verify(fileName);
        File f = new File(fileName);
        FileOutputStream fos = new FileOutputStream(f); 
        JPEGImageEncoder je = JPEGCodec.createJPEGEncoder(fos);
        je.encode(image); 
        fos.flush(); 
        fos.close();
    }

}

