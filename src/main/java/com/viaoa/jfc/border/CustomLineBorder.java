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
    //private Insets insets;
    private int top, left, bottom, right;
    private Color color;
    private Insets insets;

    public CustomLineBorder(int t, int l, int b, int r) {
        this(t,l,b,r,null);
    }
    public CustomLineBorder(int t, int l, int b, int r, Color c) {
        this.top = t;
        this.left = l;
        this.bottom = b;
        this.right = r;
        insets = new Insets(t, l, b, r);
        if (c == null) {
            c = UIManager.getColor("controlDkShadow");
            if (c == null) c = Color.gray;
        }
        this.color = c;
    }

    @Override
    public Insets getBorderInsets(Component c) {
        return insets; 
    }
    @Override
    public boolean isBorderOpaque() {
        return true;
    }
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        if (insets != null) {
            insets.top = this.insets.top;
            insets.left = this.insets.left;
            insets.bottom = this.insets.bottom;
            insets.right = this.insets.right;
        }
        return insets;
    }
    
    public void paintBorder(Component comp, Graphics gr, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) gr;
        
        g.setColor(color);
        Stroke strokeHold = g.getStroke();

        if (top > 0) {
            Stroke s = new BasicStroke(top);
            g.setStroke(s);
            
            int x1 = x + (top/2);
            int x2 = x + (w - (top/2));
            
            int y1 = y + (top/2);
            
            g.drawLine(x1, y1, x2, y1);
        }
        
        if (bottom > 0) {
            Stroke s = new BasicStroke(bottom);
            g.setStroke(s);
            
            
            int x1 = x + (bottom/2);
            int x2 = x + (w - (bottom/2));
            
            int y1 = y + (h - (bottom/2));
            
            g.drawLine(x1, y1, x2, y1);
        }

        if (left > 0) {
            Stroke s = new BasicStroke(left);
            g.setStroke(s);
            
             int x2 = x + (left/2);
            
            int y1 = y + (left/2);
            int y2 = y + (h - (left/2));
            
            g.drawLine(x2, y1, x2, y2);
        }
        
        if (right > 0) {
            Stroke s = new BasicStroke(right);
            g.setStroke(s);
            
            int x2 = x + (w - (right/2));
            g.drawLine(x2, y, x2, y+h);
        }
        
        g.setStroke(strokeHold);
    }
    
    
}
