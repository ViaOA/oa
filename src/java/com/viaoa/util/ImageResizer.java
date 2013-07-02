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
package com.viaoa.util;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import com.sun.image.codec.jpeg.*;

/**
 * Resizes image and saves as a scaled jpeg image files on your file system.
 * Uses the com.sun.image.codec.jpeg package shipped
 * by Sun with Java 2 Standard Edition.
 *
 */
public class ImageResizer extends Panel {

    /**
    * @param originalImage the file name of the image to resize
    * @param newImage the new file name for the resized image
    * @param factor the new image's width will be  width * factor.
    *   The height will be proportionally scaled.
    */
    public void doResize(String originalImage, String newImage, double factor) {
        Image img = getToolkit().getImage(originalImage);
        if (img == null) return;
        loadImage(img);
        int iw = img.getWidth(this);
        int ih = img.getHeight(this);

        //Reduce the image
        int w = (int)(iw * factor);
        Image i2 = img.getScaledInstance(w, -1, 0);
        loadImage(i2);

        //Load it into a BufferedImage
        int i2w = i2.getWidth(this);
        int i2h = i2.getHeight(this);
        BufferedImage bi = new BufferedImage(i2w, i2h, BufferedImage.TYPE_INT_RGB);
        Graphics2D big = bi.createGraphics();
        big.drawImage(i2,0,0,this);

        // Use JPEGImageEncoder to write the BufferedImage to a file
        try{
            OutputStream os = new FileOutputStream(newImage);
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(os);
            encoder.encode(bi);
            os.flush();
            os.close();
            img.flush();
            i2.flush();
            bi.flush();
        }
        catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Causes the image to be loaded into the Image object
     */
    private void loadImage(Image img){
        try {
            MediaTracker tracker = new MediaTracker(this);
            tracker.addImage(img, 0);
            tracker.waitForID(0);
        }
        catch (Exception e) {
        }
    }

    public static void main(String args[]) {
        if (args.length != 3) {usage();}
        double factor = Double.parseDouble(args[2]);
        ImageResizer resizer = new ImageResizer();
        resizer.doResize(args[0], args[1], factor);
        System.exit(0);
    }

    public static void usage(){
        System.out.println("usage: java Resize original_file new_filename resize_factor");
        System.exit(1);
    }


}

