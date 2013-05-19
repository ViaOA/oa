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
