package com.viaoa.jfc.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Custom color icon w12 x h17
 * @author vvia
 *
 */
public class ColorIcon implements Icon {
    private Color color;

    public ColorIcon() {
    }
    
    public ColorIcon(Color c) {
        setColor(c);
    }
    
    public void setColor(Color c) {
        this.color = c;
    }
    
    public int getIconHeight() {
        return 17;
    }
    public int getIconWidth() {
        return 12;
    }

    public void paintIcon(Component c,Graphics g,int x,int y) {
        g.setColor(color==null?Color.white:color);
        g.fillRoundRect(x+1,y+3,11,11,2,2);
        // g.fillOval(2,5,8,8);
    }

}
