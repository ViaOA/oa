package com.viaoa.jfc;

import java.awt.*;
import javax.swing.Icon;

/**
 * Used to create a colored icon
 *
 */
public class OAColorIcon implements Icon {
    private Color color;
    private int w, h;

    public OAColorIcon(Color color, int w, int h) {
        this.color = color;
        this.w = w;
        this.h = h;
    }
        
    public void setColor(Color color) {
        this.color = color;
    }
    public Color getColor() {
        return this.color;
    }

    public int getIconHeight() {
        return h;
    }
    public int getIconWidth() {
        return w;
    }

    public void paintIcon(Component c, Graphics graphic, int x, int y) {
        Graphics2D g = (Graphics2D) graphic;
        g.setColor(color);
        g.fillRoundRect(x, y, w, h, 2, 2);
    }
}