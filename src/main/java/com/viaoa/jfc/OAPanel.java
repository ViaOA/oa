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
package com.viaoa.jfc;

import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.OAJfcController;
import com.viaoa.jfc.control.OAJfcControllerFactory;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;

/**
 *
 */
public class OAPanel extends JPanel implements OAJfcComponent {
    private Hub hub;
    private OAJfcController control;
    private OAJfcComponent jfccomp;
    private OAHTMLTextPane htmlTextPane;
    
    public OAPanel(Hub h, LayoutManager lm) {
        super(lm);
        this.hub = h;
        setup();
    }
    public OAPanel(Hub h) {
        this.hub = h;
        setup();
    }
    public OAPanel(LayoutManager lm) {
        super(lm);
    }

    // wrapper
    public OAPanel(OAJfcComponent comp) {
        this.jfccomp = comp;
    }
    // wrapper
    public OAPanel(OAJfcController control) {
        this.control = control;
    }
    // wrapper
    public OAPanel(OAHTMLTextPane htmlTextPane) {
        this.htmlTextPane = htmlTextPane;
    }
    
    
    protected void setup() {
        if (control == null && jfccomp == null && htmlTextPane == null) {
            control = OAJfcControllerFactory.createAoNotNull(hub, this);
        }
    }
    
    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLable() {
        return getController().getLabel();
    }
    @Override
    public OAJfcController getController() {
        if (jfccomp != null) return jfccomp.getController();
        if (htmlTextPane != null) return htmlTextPane.getController().getBindController();
        return control;
    }
    @Override
    public void initialize() {
    }

    public void setToolTipTextTemplate(String s) {
        this.control.setToolTipTextTemplate(s);
    }
    public String getToolTipTextTemplate() {
        return this.control.getToolTipTextTemplate();
    }

    
    // 20181006 TESTING:  use minimum size if there are any OAResizePanels
/*  > I ended up adjusting OAResizePanel getPreferredSize when an resizable component (using OAResizePanel) is inside of a JScrollPane
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        Dimension dx = super.getMinimumSize();

        
        
        return dx;
    }
*/    
    
    
//testing    
    /* 20181006 added scrollable support.
     * JScrollPane will set size of internal components no less then preferred size.
     * This will allow it to go down to minimumsize
     */
    
/**     
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        Dimension d = this.getPreferredSize();
        return d;
    }
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return OATable.getCharHeight();
        }
        return OATable.getCharWidth(5);
    }
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return OATable.getCharHeight() * 10;
        }
        return OATable.getCharWidth(5);
    }
int xx;    
    private boolean bTrackWidth=true;
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
System.out.println("xxxxxxxxxxxxxxxx "+(++xx));        
        Dimension d = this.getSize();

        Dimension dMin = this.getMinimumSize();
        if (d.width < (dMin.width+10)) {
            bTrackWidth = false; // need scrollbar
System.out.println("YES");            
        }
        else {
            if (!bTrackWidth) {
                if (d.width > dMin.width+10) {
                    bTrackWidth = true; // dont need scrollbar
System.out.println("NO");            
                }
            }
        }
        return bTrackWidth;
    }
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
*/
}
