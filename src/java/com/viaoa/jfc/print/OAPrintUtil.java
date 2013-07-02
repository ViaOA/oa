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


