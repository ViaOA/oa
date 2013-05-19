
// see: http://www.javadocexamples.com/java_source/frost/gui/ScrollableBar.java.html


package com.viaoa.jfc;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.UIManager;


/**
 * Scroll component  for toolbar, etc.
 * @author vincevia
 *
 */
public class OAScroller extends JComponent implements SwingConstants {

    static {
        UIManager.put("OAScrollerUI", "com.viaoa.jfc.scroller.OAScrollerUI");
    }

    public OAScroller(Component comp) {
        this(comp, HORIZONTAL);
    }

    public OAScroller(Component comp, int orientation) {
        this.comp = comp;
        if (orientation == HORIZONTAL) {
            horizontal = true;
        }
        else {
            horizontal = false;
        }
        small = true; // Arrow size on scroll button.
        inc = 10;      // Scroll width in pixels.
        updateUI();
    }

    public String getUIClassID() {
        return "OAScrollerUI";
    }

    public void updateUI() {
        setUI(UIManager.getUI(this));
        invalidate();
    }

    public Component getComponent() {
        return comp;
    }

    public void setComponent(Component comp) {
        if (this.comp != comp) {
            Component old = this.comp;
    
            firePropertyChange("component", old, comp);
        }
    }

    public int getIncrement() {
        return inc;
    }

    public void setIncrement(int inc) {
        if (inc > 0 && inc != this.inc) {
            int old = this.inc;
            this.inc = inc;
            firePropertyChange("increment", old, inc);
        }
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isSmallArrows() {
        return small;
    }

    public void setSmallArrows(boolean small) {
        if (small != this.small) {
            boolean old = this.small;
            this.small = small;
            firePropertyChange("smallArrows", old, small);
        }
    }

    private Component comp;
    private boolean horizontal, small;
    private int inc;
    
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
if (d.width > 900) {
    d.width += 0;
//qqqqqqqqqqqqqqqqqqqqqqqqq testing ... not sure if this could be returning a huge width    
}
        return d;
    }
    
    
}