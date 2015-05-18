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
