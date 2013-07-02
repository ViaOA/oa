/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jfc.image;

import java.awt.*;
import javax.swing.Icon;

/**
 * Used to create a colored icon, can be used with another icon, to have
 * a colored line under the icon.  The height of the line is 10 if there is not an icon,
 * else 4.  The width will be 12 if no icon, else it will be the same as the icon width. 
 * @author vvia
 *
 */
public class ColoredLineUnderIcon implements Icon {
    private Color color;
    private Icon icon;
    int h1, h2;  // height of colored line, with and withOut icon
    int w1=12;

    public ColoredLineUnderIcon() {
        h1 = 4;
        h2 = 10;
    }    
    public ColoredLineUnderIcon(Icon icon) {
        this.icon = icon;
        h1 = 4;
        h2 = 10;
    }
        
    public void setColor(Color color) {
        this.color = color;
    }
    public Color getColor() {
        return this.color;
    }

    public int getIconHeight() {
        int h;
        if (icon != null) h = icon.getIconHeight() + h1 + 1;  
        else h = h2; 
        return h;
    }
    public int getIconWidth() {
        int w = w1;
        if (icon != null) w = icon.getIconHeight(); 
        w = Math.max(w, 5);
        return w;
    }


    
    public void paintIcon(Component c, Graphics graphic, int x, int y) {
        Graphics2D g = (Graphics2D) graphic;
        
        int h = getIconHeight();
        int w = getIconWidth();
        
        if (icon != null) {
            icon.paintIcon(c, g, x, y);
            y += icon.getIconHeight();
            h -= icon.getIconHeight() + 1;
        }
        if (color != null) {
            g.setColor(color);
            g.fillRect(x, y, w+1, h);
        }
    }
}