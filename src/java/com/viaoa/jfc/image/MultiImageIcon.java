package com.viaoa.jfc.image;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
    creates an image from multiple images
*/
public class MultiImageIcon extends ImageIcon {
    Image[] images;
    int w,h, gap;
    double scale = 1.0;
    Color backgroundColor = Color.white;
    private Image img;

    public MultiImageIcon(Image[] images) {
        this(images, 1);
    }
    public MultiImageIcon(Image[] images, int gap) {
        this.images = images;
        this.gap = Math.max(gap,0);
        for (int i=0; images != null && i < images.length; i++) {
            if (images[i] != null) {
                w += images[i].getWidth(null);
                h = Math.max(h, images[i].getHeight(null));
                w += gap;
            }
        }
    }
        
    public void setScale(double d) {
        this.scale = d;
        img = null;
    }
    public double getScale() {
        return this.scale;
    }

    public int getIconHeight() {
        return (int) (h*scale);
    }
    public int getIconWidth() {
        return (int) (w*scale);
    }

    public void setBackground(Color c) {
        backgroundColor = c;
        img = null;
    }
    public Color getBackground() {
        return backgroundColor;
    }

    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (scale != 1.0 && g instanceof Graphics2D) {
            ((Graphics2D)g).scale(scale, scale);
        }
        for (int i=0; images != null && i < images.length; i++) {
            if (images[i] != null) {
                g.drawImage(images[i], x, y, null);
                x += images[i].getWidth(null);
                x += gap;
            }
        }
    }

    public Image getImage() {
        if (img == null) {
            if (images == null || images.length == 0) {
                BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.getGraphics();
                g.setColor(new Color(0,0,0,0));
                g.fillRect(0, 0, 1, 1);
                img = bi;
            }
            else {
                BufferedImage bi = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics g = bi.getGraphics();
                if (backgroundColor != null) {
                    g.setColor(backgroundColor);
                    g.fillRect(0, 0, w, h);
                }
                paintIcon(null, g, 0, 0);
                img = bi;
            }
        }
        return img;
    }

}                      
