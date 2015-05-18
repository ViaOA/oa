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