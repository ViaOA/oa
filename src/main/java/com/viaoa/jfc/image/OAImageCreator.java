/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.jfc.image;

import java.io.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
//was: import com.sun.image.codec.jpeg.*;

import com.viaoa.util.*;

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
        // 20151215
        ImageIO.write(image, "jpeg", fos);
        /*was:
        JPEGImageEncoder je = JPEGCodec.createJPEGEncoder(fos);
        je.encode(image); 
        fos.flush(); 
        fos.close();
        */
    }

}

