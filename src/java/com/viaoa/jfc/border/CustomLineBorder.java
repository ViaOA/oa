package com.viaoa.jfc.border;

import java.awt.*;

import javax.swing.UIManager;
import javax.swing.border.*;


/**
 * Sets a border with specific line widths.
 * 
 * For system colors, check out: BasicLookAndFeel.defaultSystemColors
 * 
 * @author vincevia
 */
public class CustomLineBorder extends AbstractBorder {
    private Insets insets;
    private int t,l,b,r;
    private Color color;

    public CustomLineBorder(int t, int l, int b, int r) {
        this(t,l,b,r,null);
    }
    public CustomLineBorder(int t, int l, int b, int r, Color c) {
        this.t = t;
        this.l = l;
        this.b = b;
        this.r = r;
        insets = new Insets(t, l, b, r);
        if (c == null) c = UIManager.getColor("controlDkShadow");
        if (c == null) c = Color.gray;
        this.color = c;
    }

    public Insets getBorderInsets(Component c) { 
        return insets; 
    }
    
    public void paintBorder(Component comp, Graphics gr, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) gr;
        
        g.setColor(color);
        
        if (t > 0) {
            Stroke s = new BasicStroke(t);
            g.setStroke(s);
            g.drawLine(x, y, x+w, y);
        }
        
        if (b > 0) {
            Stroke s = new BasicStroke(b);
            g.setStroke(s);
            g.drawLine(x, y+h, x+w, y+h);
        }

        if (l > 0) {
            Stroke s = new BasicStroke(l);
            g.setStroke(s);
            g.drawLine(x, y, x, y+h);
        }
        
        if (r > 0) {
            Stroke s = new BasicStroke(r);
            g.setStroke(s);
            g.drawLine(x+w, y, x+w, y+h);
        }
    }
    
    
}
