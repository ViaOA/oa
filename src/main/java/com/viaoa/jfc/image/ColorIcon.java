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
