package com.viaoa.jfc.print;

import java.awt.Toolkit;

/**
 * Utility methods for converting between pixels and points.
 * @author vvia
 *
 */
public class OAPrintUtil {

    private static float pointToPixel; 
    public static float convertPointsToPixels(double pointSize) {
        if (pointToPixel == 0.0) {
            pointToPixel = (float) (Toolkit.getDefaultToolkit().getScreenResolution() / 72.0);
        }
        return (float) (pointToPixel * pointSize);
    }
    
    private static float pixelToPoint;
    public static float convertPixelsToPoints(double pixelSize) {
        return (float) (getPixelToPointScale() * pixelSize);
    }

    public static float getPixelToPointScale() {
        if (pixelToPoint == 0.0) {
            pixelToPoint = (float) (72.0 / Toolkit.getDefaultToolkit().getScreenResolution());
        }
        return pixelToPoint;
    }

}


