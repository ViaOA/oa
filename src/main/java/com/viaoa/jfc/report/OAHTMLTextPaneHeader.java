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
package com.viaoa.jfc.report;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.print.PageFormat;

import com.viaoa.jfc.editor.html.OAHTMLTextPane;

/**
 * Component used for report headers. 
 * @author vvia
 *
 */
public abstract class OAHTMLTextPaneHeader extends OAHTMLTextPane {
    private boolean bPrintCalled;
    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        bPrintCalled = true;                
        afterPrint();
        setText(getText(pageIndex));
        beforePrint(pageFormat);
        bPrintCalled = false;                
        return super.print(graphics, pageFormat, 0);
    }
    
    @Override
    public Dimension getPrintSize(int pageIndex, PageFormat pageFormat, int width) {
        return super.getPrintSize(0, pageFormat, width);
    }            
    
    @Override
    public void clearImageCache() {
        if (!bPrintCalled) super.clearImageCache();                
    }
    
    public abstract String getText(int pageIndex);
    
}
