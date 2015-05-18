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

import java.awt.*;
import java.awt.print.*;



/** 
    Extends java printable.
    Note: since pageFormat uses sizes in points, you can convert from points to pixels by 
    multiplying the following by the amount of points.
    pointToPixel = (float) (Toolkit.getDefaultToolkit().getScreenResolution() / 72.0)
    
    @see OAImageUtil#convertPointsToPixels
*/
public interface OAPrintable extends Printable {

	/** Used to know the size of a page.  This is used for reports that have multiple printables per
	 *  page, usually when using a header and footer. 
	 *  Note: This could be called before the page is actually printed.  In most cases, this will return
	 *  the total size of the printable.
	 *  @param pageFormat which has sizes in points - (which might need to be converted to pixels, for comparisons) 
	 *  @param width is in pixels
	*/
	public Dimension getPrintSize(int pageIndex, PageFormat pageFormat, int width);


	public void beforePrint(PageFormat pageFormat);
    public void afterPrint();
	
    /**
     * This should call the print()
     */
    public int preview(final Graphics graphics, final PageFormat pageFormat, final int pageIndex);
    public void beforePreview(PageFormat pageFormat);
    public void afterPreview();

}
