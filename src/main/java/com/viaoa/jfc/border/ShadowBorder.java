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
