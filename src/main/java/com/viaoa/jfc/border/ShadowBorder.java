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
package com.viaoa.jfc.border;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;

/**
 *  A custom border that has a shadow on the right and lower sides.
 */
public class ShadowBorder extends AbstractBorder {
	private int amount;
    private Insets insets;

    public ShadowBorder() {
    	this(9);
    }
    public ShadowBorder(int amount) {
    	this.amount = amount;
    	this.insets = new Insets(0, 0, amount, amount);
    }
    
    public Insets getBorderInsets(Component c) { 
    	return insets; 
    }

    public void paintBorder(Component c, Graphics gr, int x, int y, int w, int h) {
        Graphics2D g = (Graphics2D) gr;
        Color shadow = UIManager.getColor("controlShadow");
        if (shadow == null) shadow = Color.GRAY;
        
        g.translate(x, y);

        for (int i=0; i<amount; i++) {
        	int alpha = 14 + ((amount-(i+1)) * (230/amount));
            Color color = new Color(shadow.getRed(),
                    shadow.getGreen(),
                    shadow.getBlue(),
                    alpha);
            g.setColor(color);
            // right
        	g.fillRect((w-amount)+i, i+3, 1, (h-amount)-3); 
        	// bottom
        	g.fillRect(i+3, (h-amount)+i, (w-amount)-2, 1); 
        }
        g.translate(-x, -y);
    }
}
