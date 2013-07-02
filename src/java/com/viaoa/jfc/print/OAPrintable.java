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
