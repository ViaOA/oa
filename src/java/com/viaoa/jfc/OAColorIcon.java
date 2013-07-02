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