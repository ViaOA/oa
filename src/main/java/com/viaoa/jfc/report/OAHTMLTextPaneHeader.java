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
