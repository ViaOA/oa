package com.viaoa.jfc.image;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Combines multiple icons into one.
 * @author vvia
 *
 */
public class MultiIcon implements Icon {
    private Icon icon1, icon2;
    private int gap = 0;
    
    public void setIcon1(Icon ic) {
        this.icon1 = ic;
    }
    public void setIcon2(Icon ic) {
        this.icon2 = ic;
    }
    
    public int getIconHeight() {
        int x = 0;
        if (icon1 != null) {
            x = icon1.getIconHeight();
        }
        if (icon2 != null) x = Math.max(x, icon2.getIconHeight());
        return x;
    }
    public int getIconWidth() {
        int x = 0;
        if (icon1 != null) {
            x = icon1.getIconWidth();
            if (icon2 != null) x += gap;
        }
        if (icon2 != null) x += icon2.getIconWidth();
        return x;
    }

    public void paintIcon(Component c,Graphics g,int x,int y) {
        int h1 = icon1 == null ? 0 : icon1.getIconHeight();
        int h2 = icon2 == null ? 0 : icon2.getIconHeight();
        int max = Math.max(h1, h2);
        g.translate(0, max);
        if (icon1 != null) {
            g.translate(0, -h1);
            icon1.paintIcon(c, g, x, y);
            x += icon1.getIconWidth();
            if (icon2 != null) x += gap;
            g.translate(0, +h1);
        }
        if (icon2 != null) {
            g.translate(0, -h2);
            icon2.paintIcon(c, g, x, y);
            g.translate(0, h2);
        }
        g.translate(0, -max);
    }

}
