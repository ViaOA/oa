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


