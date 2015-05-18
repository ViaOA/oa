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

