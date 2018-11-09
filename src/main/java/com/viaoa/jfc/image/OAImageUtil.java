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

/**

 ** MouseInfo 

 RescaleOp(multiplier, addition) - brighten, contrast


 Using FilteredImageSource -
 ReplicateScaleFilter(width, height) - rescale


 BufferedImage img = null;
 try {
 URL url = new URL(getCodeBase(), "strawberry.jpg");
 img = ImageIO.read(url);
 } catch (IOException e) {
 }


 GraphicsConfiguration.createCompatibleImage(width, height, transparency)


 BufferedImage off_Image = new BufferedImage(100, 50, BufferedImage.TYPE_INT_RGB);



 try {
 BufferedImage bi = getMyImage(); // retrieve image
 File outputfile = new File("saved.jpg");  
 ImageIO.write(bi, "jpg", outputfile);  // can be: gif (no transparency), jpg (no animation), png (lossy, not good for text)
 } catch (IOException e) {
 ...
 }

 VVVVVVVVV ================ RescaleOp
 // Brighten the image by 30%
 float scaleFactor = 1.3f;
 RescaleOp op = new RescaleOp(scaleFactor, 0, null);
 bufferedImage = op.filter(bufferedImage, null);

 // Darken the image by 10%
 scaleFactor = .9f;
 op = new RescaleOp(scaleFactor, 0, null);
 bufferedImage = op.filter(bufferedImage, null);


 VVVVVVVVV ============== ConvolvOp
 // sharpen
 Kernel kernel = new Kernel(3, 3,
 new float[] {
 -1, -1, -1,
 -1, 9, -1,
 -1, -1, -1});
 BufferedImageOp op = new ConvolveOp(kernel);
 bufferedImage = op.filter(bufferedImage, null);


 VVVVVVVVV ================= AffineTransform 
 AffineTransform tx = new AffineTransform();
 tx.scale(scalex, scaley);
 tx.shear(shiftx, shifty);
 tx.translate(x, y);
 tx.rotate(radians, bufferedImage.getWidth()/2, bufferedImage.getHeight()/2);
 also: .quadrantRotate(int)


 AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
 bufferedImage = op.filter(bufferedImage, null);  


 VVVVVVV ================== Crop
 image = createImage(new FilteredImageSource(image.getSource(),
 new CropImageFilter(73, 63, 141, 131)));

 Image src = getImage("doc:///demo/images/duke/T1.gif");
 ImageFilter colorfilter = new RedBlueSwapFilter();
 Image img = createImage(new FilteredImageSource(src.getSource(),
 colorfilter));


 ======== Samples
 http://www.java2s.com/Code/Java/2D-Graphics-GUI/ImageOperations.htm 

 */

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.awt.image.RenderedImage;
import java.awt.image.RescaleOp;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.viaoa.jfc.image.jpg.CMYKJPEGImageReader;
import com.viaoa.util.OAWebUtil;

/**
 * Various untility methods for working with Images.
 * @author vvia
 *
 */
public class OAImageUtil {

    
    /**
     * Use a byte array to create an image.  This uses ImageIO, which will determine the type of image format (png,gif,jpg).
     */
    public static BufferedImage convertToBufferedImage(byte[] bs) throws IOException {
        if (bs == null) return null;
        BufferedImage bi;
        try {
            bi = ImageIO.read(new ByteArrayInputStream(bs));
            return bi;
        }
        catch (Exception e) {
        }
        
        ImageInputStream iis = new MemoryCacheImageInputStream(new ByteArrayInputStream(bs));
        CMYKJPEGImageReader readerx = new CMYKJPEGImageReader(null);
        readerx.setInput(iis);
        bi = readerx.read(0, null);
        
        return bi;
    }

    private static MediaTracker mediaTracker;
    private static int idMediaTracker;
    private static AtomicInteger aiMediaTracker = new AtomicInteger();

    private static MediaTracker getMediaTracker() {
        if (mediaTracker == null) {
            Component comp = new Component() {
            };
            mediaTracker = new MediaTracker(comp);
        }
        return mediaTracker;
    }

    /*
     * This will use a mediaTracker to make sure that an Image is fully loaded.
     */
    public static void loadImage(Image img) {
        if (img == null) return;
        int x = aiMediaTracker.incrementAndGet();
        MediaTracker mt = getMediaTracker();
        mt.addImage(img, x);
        try {
            mt.waitForID(x);
        }
        catch (InterruptedException e) {
        }
        finally {
            mt.removeImage(img, x);
        }
    }

    /**
     * Creates a new BufferedImage with the given image.
     */
    public static BufferedImage createBufferedImage(Image img) {
        if (img == null) return null;

        // 20121104
        if (img instanceof BufferedImage) {
            return (BufferedImage) img; 
        }
        
        loadImage(img);
        int w = img.getWidth(null);
        int h = img.getHeight(null);

        boolean bAlpha = hasAlpha(img);
        BufferedImage bi = null;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            int transparency;
            if (bAlpha) transparency = Transparency.BITMASK;
            else transparency = Transparency.OPAQUE;

            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bi = gc.createCompatibleImage(img.getWidth(null), img.getHeight(null), transparency);
        }
        catch (HeadlessException e) {
        }

        if (bi == null) {
            bi = new BufferedImage(w, h, bAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g = bi.createGraphics();

        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bi;
    }

    public static boolean hasAlpha(Image img) {
        if (img == null) return false;
        ColorModel cm = getColorModel(img);
        if (cm == null) return false;
        return cm.hasAlpha();
    }

    public static ColorModel getColorModel(Image img) {
        if (img instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage) img;
            return bimage.getColorModel();
        }

        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        }
        catch (InterruptedException e) {
        }

        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm;
    }

    /**
     * 
     * @param img
     *            if this is a BufferedImage, then it will be returned, else a
     *            BufferedImage will be created.
     * @return
     */
    public static BufferedImage convertToBufferedImage(Image img) {
        if (img == null) return null;
        if (img instanceof BufferedImage) return (BufferedImage) img;
        return createBufferedImage(img);
    }

    /**
     * calls convertToJavaJPG(bi).
     */
    public static byte[] convertToBytes(BufferedImage bi) throws IOException {
        if (bi == null) return null;

        boolean bAlpha = hasAlpha(bi);
        
/** 20171016 need alpha for transparency, otherwise jpg will show as black        
        // 20171013 remove alphaChannel (and all of the jpeg headaches)
        if (bAlpha) {
            bi = removeAlphaChannel(bi);
            bAlpha = false;
        }
*/        
        byte[] bs;
        if (!bAlpha) bs = convertToJavaJPG(bi);
        else bs = convertToPNG(bi);
        
        return bs;
    }
    /**
     * Converts a bufferedImage to a "java only" byte array "jpg" image.
     * This is the internal format used by OA, which use JPG format, with support for Alpha (transparency).
     * 
     * IMPORTANT: If there is transparency (alpha channel), then it will work only for Java
     * programs that use the image.  
     * To use for outside of Java: if hasAlpha() is true, then convert to PNG, instead of jpg
     * 
     */
    public static byte[] convertToJavaJPG(BufferedImage bi) throws IOException {
        if (bi == null) return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024 * 24);
        // see: com.viaoa.jfc.editor.html.protocol.oaproperty.Handler for url type "oaproperty"

        ImageIO.write(bi, "jpg", os);
        os.flush();
        byte[] bs = os.toByteArray();
        os.close();
        return bs;
    }

    public static byte[] convertToPNG(Image img) throws IOException {
        BufferedImage bi = createBufferedImage(img);
        byte[] bs = convertToBytes(bi);
        return convertToPNG(bi, bs.length*6);
    }
    public static byte[] convertToPNG(byte[] bs) throws IOException {
        BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
        return convertToPNG(bi, bs.length*6);
    }
    public static byte[] convertToPNG(BufferedImage bi) throws IOException {
        return convertToPNG(bi, 1024 * 32);
    }
    public synchronized static byte[] convertToPNG(BufferedImage bi, int bufferSize) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream(bufferSize);
        ImageIO.write(bi, "png", os);
        os.flush();
        byte[]  bs = os.toByteArray();
        os.close();
        return bs;
    }
    
    public static byte[] convertToJPG(Image img) throws IOException {
        BufferedImage bi = createBufferedImage(img);
        return convertToJPG(bi);
    }
    
    
    public static byte[] convertToJPG(BufferedImage bi) throws IOException {
        if (hasAlpha(bi)) {
            BufferedImage imageRGB = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = imageRGB.createGraphics();
            // Color.WHITE for background to white
            g.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), Color.WHITE, null);
            bi = imageRGB;
        }
        
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024 * 24);
        ImageIO.write(bi, "jpg", os);
        os.flush();
        byte[] bs = os.toByteArray();
        os.close();
        return bs;
    }
    public static byte[] convertToJPG(byte[] bs) throws IOException {
        BufferedImage bi = OAImageUtil.convertToBufferedImage(bs);
        return convertToJPG(bi);
    }
    
    /**
     * Converts image to bufferedImage and then returns
     * convertToBytes(BufferedImage bi).
     */
    public static byte[] convertToBytes(Image img) throws IOException {
        if (img == null) return null;
        BufferedImage bi = convertToBufferedImage(img);
        return convertToBytes(bi);
    }

    public static BufferedImage crop(Image image, Rectangle rect) {
        return crop(image, rect.x, rect.y, (rect.x + rect.width) - 1, (rect.y + rect.height) - 1);
    }

    public static BufferedImage crop(Image image, int l, int t, int r, int b) {
        int w = (r - l) + 1;
        int h = (b - t) + 1;

        BufferedImage bi;
        if (image instanceof BufferedImage) {
            bi = (BufferedImage) image;
            bi = bi.getSubimage(l, t, w, h);
        }
        else {
            boolean bAlpha = hasAlpha(image);
            bi = new BufferedImage(w, h, (bAlpha ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB));
            bi.getGraphics().drawImage(image, 0, 0, w, h, l, t, r, b, null);
        }
        return bi;
    }

    public static BufferedImage scale(Image image, double xPercent, double yPercent) {

        //loadImage(image);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        int w2 = (int) (w * xPercent);
        int h2 = (int) (h * yPercent);

        // new 20120201, replaces the rest of this code
        BufferedImage bix = getScaledInstance(image, w2, h2);
        return bix;

        /*was
        int type = BufferedImage.TYPE_INT_ARGB;
        if (!hasAlpha(image)) type = BufferedImage.TYPE_INT_RGB;

        BufferedImage bi = new BufferedImage(w2, h2, type);
        Graphics2D g = (Graphics2D) bi.getGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC); // VALUE_INTERPOLATION_BILINEAR);

        // g.scale(xPercent, yPercent);

        // 20110717
        g.setColor(new Color(0, 0, 0, 0)); // make sure transparency works
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        // g.drawImage(image, 0, 0, null);
        g.drawImage(image, 0, 0, w2, h2, null);

        return bi;
        */
    }

    public static BufferedImage getScaledInstance(Image image, int newWidth, int newHeight) {
        // http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html
        loadImage(image);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        int type;
        if (!hasAlpha(image)) type = BufferedImage.TYPE_INT_RGB;
        else type = BufferedImage.TYPE_INT_ARGB;

        BufferedImage bi = convertToBufferedImage(image);

        double scaleIncrement = .60;  // see: blog.nobel-joergensen.com/2008/12/20/downscaling-images-in-java/
        do {
            if (w > newWidth) {
                w = (int) (w * scaleIncrement);
                if (w < newWidth) {
                    w = newWidth;
                }
            }
            else w = newWidth;

            if (h > newHeight && w != newWidth) {
                h = (int) (h * scaleIncrement);
                if (h < newHeight) {
                    h = newHeight;
                    w = newWidth;
                }
            }
            else h = newHeight;

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g = tmp.createGraphics();

            g.setColor(new Color(0, 0, 0, 0)); // make sure transparency works
            g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);// VALUE_INTERPOLATION_BICUBIC);

            g.drawImage(bi, 0, 0, w, h, null);
            g.dispose();

            bi = tmp;
        }
        while (w != newWidth || h != newHeight);
        return bi;
    }

    public static BufferedImage scaleToSize(Image image, int maxWidth, int maxHeight) {
        loadImage(image);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        double d = 0.0;
        if (maxWidth > 0) {
            d = ((double) maxWidth) / w;
        }
        if (maxHeight > 0) {
            double d2 = ((double) maxHeight) / h;
            if (d == 0 || d2 < d) d = d2;
        }
        if (d == 0) d = 1.0;
        return scale(image, d, d);
    }

    public static BufferedImage scaleDownToSize(Image image, int maxWidth, int maxHeight) {
        loadImage(image);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        double d = 0.0;
        if (maxWidth > 0) {
            d = ((double) maxWidth) / w;
        }
        if (maxHeight > 0) {
            double d2 = ((double) maxHeight) / h;
            if (d == 0 || d2 < d) d = d2;
        }
        if (d == 0) d = 1.0;
        if (d > 1.0) d = 1.0;
        return scale(image, d, d);
    }

    public static BufferedImage scale(Image image, int newWidth, int newHeight) {

        // qqqqqqqqqqqqqq new 20120201
        BufferedImage bix = getScaledInstance(image, newWidth, newHeight);
        return bix;

        /*was
        loadImage(image);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        int type = BufferedImage.TYPE_INT_ARGB;
        if (!hasAlpha(image)) type = BufferedImage.TYPE_INT_RGB;

        BufferedImage bi = new BufferedImage(newWidth, newHeight, type);
        bi.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, 0, 0, w, h, null);
        return bi;
        */
    }

    public static BufferedImage rotate(Image image, int degrees) {
        loadImage(image);
        int w = image.getWidth(null);
        int h = image.getHeight(null);

        AffineTransform at = new AffineTransform();
        double d = Math.toRadians(degrees);
        at.rotate(d, w, h);

        // find correct translation point (x,y)
        Point2D pt;
        double transY, transX;
        AffineTransform at2;

        if (degrees > 0) {
            pt = new Point2D.Double(0.0, 0.0);
            pt = at.transform(pt, null);
            transY = pt.getY();

            pt = new Point2D.Double(0, h);
            pt = at.transform(pt, null);
            transX = pt.getX();

            at2 = new AffineTransform();
            at2.translate(-transX, -transY);
        }
        else {
            pt = new Point2D.Double(w, 0.0);
            pt = at.transform(pt, null);
            transY = pt.getY();

            pt = new Point2D.Double(0, 0);
            pt = at.transform(pt, null);
            transX = pt.getX();

            at2 = new AffineTransform();
            at2.translate(-transX, -transY);
        }
        at.preConcatenate(at2);

        int type = BufferedImage.TYPE_INT_ARGB;
        if (!hasAlpha(image)) type = BufferedImage.TYPE_INT_RGB;

        BufferedImage bi = new BufferedImage(h, w, type);
        Graphics2D g = (Graphics2D) bi.getGraphics();

        g.setTransform(at);

        // double d = Math.toRadians(degrees);
        // g.rotate(d, w, h);
        // g.translate(50, 50);
        g.drawImage(image, 0, 0, w, h, 0, 0, w, h, null);
        return bi;
    }

    public static boolean saveAsJpeg(Image img, File file) {
        return saveAsJpeg(img, file, .95f);
    }

    public static BufferedImage loadImage(String fname) throws IOException {
        File file = new File(fname);
        if (!file.exists()) return null;
        return loadImage(file);
    }

    private static ImageReader imageReaderJpeg;
    private static boolean bImageReaderJpeg;

    /**
     * Uses imageio to read an image.
     * If this fails, then have the image externally saved.
     * There are issues with reading jpg, and they should be converted to RGB, instead of CMYK, since
     * the later has too many variations.
     */
    public static BufferedImage loadImage(File file) throws IOException {
        IOException ex = null;
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
            return bi;
        }
        catch (IOException e) {
            ex = e;
        }
        catch (Exception exx) {
        }

        // try to read jpg that has a format that ImageIO cant read
        String s = file.getName().toUpperCase();
        if (!s.endsWith(".JPG")) {
            if (!s.endsWith(".JPEG")) { throw ex; }
        }
        
        /*
        if (imageReaderJpeg == null && !bImageReaderJpeg) {
            bImageReaderJpeg = true;
            Iterator readers = ImageIO.getImageReadersByFormatName("jpg"); // also "JPEG"
            while (readers.hasNext()) {
                imageReaderJpeg = (ImageReader) readers.next();
                // imageReaderJpeg = (ImageReader) readers.next();
                if (imageReaderJpeg.canReadRaster()) {
                    break;
                }
                imageReaderJpeg = null;
            }
        }
        if (imageReaderJpeg == null) throw ex;
        */

        // convert CMYK JPG
        CMYKJPEGImageReader readerx = new CMYKJPEGImageReader(null);
        ImageInputStream iis = new FileImageInputStream(file);
        readerx.setInput(iis);
        bi = readerx.read(0, null);
        return bi;
    }

    
    /**
     * @param img
     * @param file
     * @param quality
     *            between 0.0 and 1.0, 0=best compression
     */
    public static boolean saveAsJpeg(Image img, File file, float quality) {
        ImageWriter writer = null;
        ImageOutputStream ios = null;

        try {
            Iterator iter = ImageIO.getImageWritersByFormatName("jpeg");
            if (!iter.hasNext()) return false;
            writer = (ImageWriter) iter.next();

            ios = ImageIO.createImageOutputStream(file);
            writer.setOutput(ios);

            ImageWriteParam iwp = writer.getDefaultWriteParam();
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(quality);

            writer.write(null, new IIOImage((RenderedImage) img, null, null), iwp);
        }
        catch (IOException e) {
            System.out.println("saveAsJpeg " + e);
            return false;
        }
        finally {
            try {
                if (ios != null) {
                    ios.flush();
                    ios.close();
                }
                if (writer != null) writer.dispose();
            }
            catch (IOException e2) {
            }
        }
        return true;
    }

    public static BufferedImage getScreenImage() {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle rect = new Rectangle(dim);
        return getScreenImage(rect);
    }

    public static BufferedImage getScreenImage(Window window) {
        BufferedImage bi = null;
        Rectangle rect = new Rectangle(window.getLocation(), window.getSize());
        return getScreenImage(rect);
    }

    public static BufferedImage getScreenImage(Rectangle rect) {
        BufferedImage bi = null;
        try {
            Robot robot = new Robot();
            bi = robot.createScreenCapture(rect);
        }
        catch (Exception e) {
            System.out.println("Error getScreenImage() rect=" + rect + ", error: " + e);
        }
        return bi;
    }

    /**
     * @param offset
     *            to add to each pixel, between 0-255
     */
    public static BufferedImage brighter(Image image, byte offset) {
        loadImage(image);

        ColorModel colorModel = getColorModel(image);
        int x = colorModel.getNumComponents();

        float[] scales = new float[x];
        for (int i = 0; i < x; i++) {
            scales[i] = 1.0f;
        }
        float[] offsets = new float[x];
        for (int i = 0; i < x; i++)
            offsets[i] = offset;
        if (x > 3) offsets[3] = 0;

        RescaleOp rop = new RescaleOp(scales, offsets, null);
        return _apply(image, rop);
    }

    /**
     * @param scale
     *            contrast multiplier
     */
    public static BufferedImage contrast(Image image, float scale) {
        ColorModel colorModel = getColorModel(image);
        int x = colorModel.getNumComponents();

        float[] scales = new float[x];
        for (int i = 0; i < x; i++) {
            scales[i] = scale;
        }
        if (x > 3) scales[3] = 1.0f;

        float[] offsets = new float[x];

        RescaleOp rop = new RescaleOp(scales, offsets, null);
        return _apply(image, rop);
    }

    private static BufferedImage _apply(Image image, RescaleOp rop) {
        loadImage(image);
        int h = image.getHeight(null);
        int w = image.getWidth(null);

        boolean bAlpha = hasAlpha(image);

        int type = BufferedImage.TYPE_INT_ARGB;
        if (!hasAlpha(image)) type = BufferedImage.TYPE_INT_RGB;

        BufferedImage bi = new BufferedImage(w, h, type);
        BufferedImage biSrc = new BufferedImage(w, h, type);

        Graphics2D g = (Graphics2D) biSrc.createGraphics();
        g.drawImage(image, 0, 0, null);

        g = (Graphics2D) bi.getGraphics();

        // Draw the image, applying the filter
        g.drawImage(biSrc, rop, 0, 0);

        return bi;
    }

    public static BufferedImage removeTransparency(Image image) {
        loadImage(image);
        int h = image.getHeight(null);
        int w = image.getWidth(null);

        int type = BufferedImage.TYPE_INT_RGB;

        BufferedImage bi = new BufferedImage(w, h, type);
        Graphics2D g = (Graphics2D) bi.createGraphics();
        g.drawImage(image, 0, 0, null);

        return bi;
    }

    public static BufferedImage setTransparentColor(Image img, final Color color) {
        ImageFilter filter = new RGBImageFilter() {
            public int markerRGB = color.getRGB();
            public int min = markerRGB - 25;
            public int max = markerRGB + 25;

            public final int filterRGB(int x, int y, int rgb) {
                if ( rgb > min && rgb < max ) {
                    return 0x00FFFFFF & rgb;
                }
                else {
                    // nothing to do
                    return rgb;
                }
            }
        };
        ImageProducer ip = new FilteredImageSource(img.getSource(), filter);
        Image image = Toolkit.getDefaultToolkit().createImage(ip);
        
        final BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        
        final Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        
        return bufferedImage;
    }
    
    public static void saveAsGif(Image image, File file) {
        // String[] formats = ImageIO.getWriterFormatNames();
        // http://java.sun.com/docs/books/tutorial/2d/images/saveimage.html

        BufferedImage bi = convertToBufferedImage(image);
        try {
            ImageIO.write(bi, "gif", file);
        }
        catch (IOException ex) {
        }
    }

    public static void saveAsPng(Image image, File file) {
        BufferedImage bi = convertToBufferedImage(image);
        try {
            ImageIO.write(bi, "png", file);
        }
        catch (IOException ex) {
        }
    }

    /*
     * Examples: "classpath://com.viaoa.jfc.editor.html.image/test.jpg"
     * "oaproperty://com.vetplan.oa.Pet/picture?932" will load from
     * OAObject.property, using Id = 932 "file://c:/projects/cdi/logo.gif"
     * "jar:file:schedulercdi.jar!/com/viaoa/scheduler/help.gif"
 


     */
    public static BufferedImage loadImageUsingURL(URL url) {
        return loadImage(url);
    }
    public static BufferedImage loadImageUsingURL(String urlString) {
        try {
            URL url = new URL(urlString);
            return loadImage(url);
        }
        catch (Exception e) {
            System.err.printf("Failed while trying to access url %s: %s", urlString, e.getMessage()); 
            e.printStackTrace (); 
        }
        return null;
    }
    public static BufferedImage loadImage(URLConnection conn) {
        try { 
            InputStream is = conn.getInputStream();
            BufferedImage bi = loadImage(is);
            return bi;
        }
        catch (Exception e) { 
        }

        try { 
            InputStream is = conn.getInputStream();
            BufferedImage bi = loadCMYK(is);
        }
        catch (Exception e) { 
        }
        
        return null;
    }
    public static BufferedImage loadImage(URL url) {
        try { 
            BufferedImage bi = ImageIO.read(url);
            return bi;
        }
        catch (Exception e) { 
            //System.err.printf("Failed while reading bytes from URL %s: %s", url.toExternalForm(), e.getMessage()); 
            //e.printStackTrace (); 
        }

        try {
            InputStream is = url.openStream();
            BufferedImage bi = loadCMYK(is);
            return bi;
        }        
        catch (Exception e) { 
            //System.err.printf("Failed while reading bytes from URL %s: %s", url.toExternalForm(), e.getMessage()); 
            //e.printStackTrace (); 
        }
        return null;
    }
    public static BufferedImage loadImage(InputStream is) throws Exception {

        BufferedImage bi = null;
        try {
            ImageInputStream input = ImageIO.createImageInputStream(is);
            bi = ImageIO.read(input);
        }
        catch (IOException e) {
        }
        return bi;
    }
    protected static BufferedImage loadCMYK(InputStream is) throws Exception {
        BufferedImage bi = null;
        // convert CMYK JPG
        CMYKJPEGImageReader readerx = new CMYKJPEGImageReader(null);
        readerx.setInput(is);
        bi = readerx.read(0, null);
        return bi;
    }

    public static String getHtmlForJarImg(String classPath, String fileName) {
        return getHtmlForJarImg(classPath, fileName, 0, 0);
    }

    /**
     * ex: /com/vetplan/view/image, or com.vetplan.view.image
     * 
     * will return: <img src='classpath://com.vetplan.view.image/test.gif'
     * width='15' height='12' alt="">
     * 
     * @see com.viaoa.jfc.editor.html.protocol.classpath.Handler
     */
    public static String getHtmlForJarImg(String classPath, String fileName, int maxW, int maxH) {
        if (classPath == null || fileName == null) return null;
        classPath = classPath.replace('/', '.');
        if (classPath.length() == 0) return null;
        if (classPath.charAt(0) == '.') classPath = classPath.substring(1);

        String sUrl = "classpath://" + classPath + "/" + fileName;
        try {
            if (maxW > 0 && maxH > 0) {
                double scale;
                URL url = new URL(sUrl);
                if (url == null) return null;
                Image img = new ImageIcon(url).getImage();

                int h = img.getHeight(null);
                int w = img.getWidth(null);

                double sH = 0.0d;
                double sW = 0.0d;

                sH = ((double) maxH) / h;
                sH = maxH / h;

                if (maxW > 0 && w > maxW) {
                    sW = ((double) maxW) / w;
                }

                scale = Math.max(sH, sW);
                if (scale > 0) {
                    h = (int) (scale * h);
                    w = (int) (scale * w);
                    sUrl = "<IMG src='" + sUrl + "' height='" + h + "' width='" + w + "'>";
                }
                else sUrl = "<IMG src='" + sUrl + "'>";
            }
            else {
                sUrl = "<IMG src='" + sUrl + "'>";
            }
        }
        catch (Exception e) {
            System.out.println("error getting jar image " + sUrl);
            sUrl = null;
        }
        return sUrl;
    }

    // 20100926 need to research on how ColorModel works ... problems when
    // trying to scale, etc. - things that change colors
    public static BufferedImage createScreenBufferedImage(int width, int height) {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();

        BufferedImage bimage = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        return bimage;
    }

    // 20100926
    public static BufferedImage createScreenBufferedImage(Graphics2D g2d, int width, int height) {
        BufferedImage bimage = g2d.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        return bimage;
    }


    
    public static String convertOAPropertyToImageServlet(String html) {
        return convertOAPropertyToImageServlet(html, "");
    }
    /* Convert from "img src" format used by OAHTMLTextEditor to ImageServlet
        
        src='oaproperty://com.tmgsc.hifive.model.ImageStore/Bytes?2'
        src='../servlet/img?c=ImageStore&i=8105&p=bytes'
 
     * @param html
     * @param servletPath any prefix for path to servlet, default (null, or blank) is "/servlet"
     * @return
     */
    public static String convertOAPropertyToImageServlet(String html, String servletPath) {
        if (html == null) return "";
        if (servletPath == null) servletPath = "";
        
        for (;;) {
            int x = html.length();
            String s = "oaproperty://";
            int pos = html.indexOf(s);
            if (pos < 0) break;
            
            int max = html.indexOf(">", pos+s.length());
            if (max < 0) max = html.length();
            
            int pos2 = html.indexOf("/", pos+s.length());
            if (pos2 < 0 || pos2 > max) break;
            int pos3 = html.indexOf("?", pos2);
            if (pos3 < 0 || pos2 > max) break;
            int pos4 = pos3+1;
            for ( ; pos4 < max && Character.isDigit(html.charAt(pos4)); pos4++);
            
            String className = html.substring(pos+s.length(), pos2);
            String propName = html.substring(pos2+1, pos3);
            String id = html.substring(pos3+1, pos4);
            
            html = html.substring(0, pos) + servletPath + "/servlet/img?c="+className+"&i="+id+"&p="+propName + html.substring(pos4);
        }        
        return html;
    }
    
    
    
    // 20101101 testing for compression
    public static void test() throws Exception {
        ImageOutputStream imageOutputStream;
        ImageWriter imageWriter = null;
        ImageWriteParam pngparams;

        Iterator writers = ImageIO.getImageWritersByFormatName("jpg");

        // Fetch the first writer in the list
        for (; writers.hasNext();) {
            imageWriter = (ImageWriter) writers.next();
            System.out.println("" + imageWriter);
        }

        // Just to confirm that the writer in use is CLibPNGImageWriter
        System.out.println("\n Writer used : " + imageWriter.getClass().getName() + "\n");

        // Specify the parameters according to those the output file will be
        // written

        // Get Default parameters
        pngparams = imageWriter.getDefaultWriteParam();

        // String[] ss = pngparams.getCompressionQualityDescriptions();

        // Define compression mode
        // pngparams.setCompressionMode(
        // javax.imageio.ImageWriteParam.MODE_COPY_FROM_METADATA );

        // Define compression quality
        pngparams.setCompressionQuality(0.5F);

        // Define progressive mode
        pngparams.setProgressiveMode(javax.imageio.ImageWriteParam.MODE_COPY_FROM_METADATA);

        System.out.println("" + pngparams);
    }

    
    public static void mainA(String[] args) throws Exception {
        File file = new File("\\c:\\temp\\1.0.png");
        
        BufferedInputStream bs = new BufferedInputStream(new FileInputStream(file)); 
        
        boolean b = file.exists();
        InputStream is = new FileInputStream(file);

        ImageInputStream input = ImageIO.createImageInputStream(is);
        
        BufferedImage bi = ImageIO.read(input);
        
        //input = ImageIO.createImageInputStream(file);

        Iterator<ImageReader> itx = ImageIO.getImageReaders(input);
        
        // Iterator<ImageReader> itx = ImageIO.getImageReadersByFormatName("png");
        for ( ;itx.hasNext(); ) {
            ImageReader ir = itx.next();
            // bi = ir.read();
            int xx = 4;
            xx++;
        }
        System.out.println("Done");
    }
    
    public static void main(String[] args) throws Exception {
        final String dir = "\\temp\\images";
        File fileDir = new File(dir);
        
        System.out.println("Starting");
        File[] files = fileDir.listFiles();

        for (File file : files) {
            String s = file.getPath();
            if (!s.endsWith(".jpg")) continue;
            
            String s2 = s.substring(0, s.length() - 3) + "png";
            File f2 = new File(s2);
            if (f2.exists()) continue;

            System.out.println("converting "+s+" to .png file");
            
            BufferedImage bi = loadImage(s);
            
            bi = setTransparentColor(bi, Color.green);
            
            saveAsPng(bi, f2);
            
        }
        System.out.println("Done");
        
        
    }

    public static void mainB(String[] args) throws Exception {
        String s = "\\temp\\shutterstock_425802652.jpg";
        BufferedImage bi = loadImage(s);
        //saveAsGif(bi, new File("c:\\temp\\test.gif"));
        saveAsPng(bi, new File("c:\\temp\\test.png"));
        System.out.println("Done");
    }
    
    public static void mainZ(String[] args) throws Exception {

        // BufferedImage bi = loadImageUsingURL("http://vjw2.vetjobs.com/image/vetBanner.gif");
        
        BufferedImage bi = loadImage("c:\\temp\\aa\\cert2.jpg");
        
        if (bi != null) {
            System.out.println("converting");
            saveAsJpeg(bi, new File("c:\\temp\\aa\\x.jpg"));
        }
        System.out.println("Done");
    }
    public static void mainx(String[] args) throws Exception {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
        Connection conn = DriverManager.getConnection("jdbc:derby:database");
        // Statement s = conn.createStatement();
        // s.executeUpdate("CREATE TABLE documents (id INT, pic blob(16M))");
        // conn.commit();

        BufferedImage bi = ImageIO.read(new File("c:\\ss.gif"));
        ByteArrayOutputStream os = new ByteArrayOutputStream(25);
        ImageIO.write(bi, "png", os);
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

        PreparedStatement ps = conn.prepareStatement("INSERT INTO TEST (Image) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        ps.setBlob(1, is);
        ps.execute();
        os.close();
        ResultSet rs = ps.getGeneratedKeys();
        rs.next();
        int id = rs.getInt(1);

        Statement st = conn.createStatement();
        rs = st.executeQuery("Select Id, Image from TEST WHERE ID = " + id);
        rs.next();
        InputStream isx = rs.getBlob(2).getBinaryStream();
        // if (!rs.wasNull()) ... qqq

        bi = ImageIO.read(isx);

        JFrame frm = new JFrame();
        JLabel lbl = new JLabel(new ImageIcon(bi));
        frm.add(lbl);
        frm.setVisible(true);
        frm.setBounds(10, 10, 400, 400);

        System.out.println("Done");
    }

    public static BufferedImage removeAlphaChannel(BufferedImage src) {
        BufferedImage convertedImg = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
        convertedImg.getGraphics().drawImage(src, 0, 0, null);
        return convertedImg;
    }    
   
    public static BufferedImage loadWebImage(String url) throws Exception {
        OAWebUtil.setupHttpsAccess();
        BufferedImage bi = ImageIO.read(new URL(url));
        return bi;
    }
    public static byte[] loadWebImageBytes(String url) throws Exception {
        BufferedImage bi = OAImageUtil.loadWebImage(url);
        if (bi == null) return null;
        byte[] bs = OAImageUtil.convertToBytes(bi);
        return bs;
    }
  
    
}
