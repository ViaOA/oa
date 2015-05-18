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