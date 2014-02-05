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
import java.awt.image.*;

import javax.swing.*;

/**
    Resizes an image so that it meets a max Width or Height.
*/
public class ScaledImageIcon implements Icon {
    Icon icon;
    int w,h, maxW, maxH;
    double scale = 0.0;
    Image imgScaled;

    /**
     * @param imageIcon
     * @param maxW if 0 then it is ignored
     * @param maxH if 0 then it is ignored
     */
    public ScaledImageIcon(Icon icon, int maxW, int maxH) {
    	this.icon = icon;
    	this.maxW = maxW;
    	this.maxH = maxH;
    	if (icon != null) {
    		h = icon.getIconHeight();
    		w = icon.getIconWidth();
    	}
    	if (w == 0) w = 1;
    	if (h == 0) h = 1;
    	if (maxW == 0) this.maxW = w;
    	if (maxH == 0) this.maxH = h;
    	
		double d1=1, d2=1;
		if (maxW != 0) {
			d1 = (double) ((double)this.maxW)/((double)w); 
			this.scale = d1;
		}
		if (maxH != 0) {
			d2 = (double) ((double)this.maxH)/((double)h);
			this.scale = d2;
		}
		if (maxH > 0 && maxW > 0) {
			this.scale = Math.min(d1, d2);
		}
    }
        
    public int getIconHeight() {
        return (int) ((double)((double)h)*scale);
    }
    public int getIconWidth() {
        return (int) ((double)((double)w)*scale);
    }
    
    private BufferedImage bi;
    public Image getImage() {
    	if (bi == null) {
    	    
    		int h = getIconHeight();
    		int w = getIconWidth();
    		
    		//bi = OAImageUtil.createScreenBufferedImage(w, h);
    		
    		bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    	
	        Graphics g2 = bi.getGraphics();
	        g2.setColor(new Color(0,0,0,0));  // make sure transparency works
	        g2.fillRect(0, 0, getIconWidth(), getIconHeight());
	        ((Graphics2D)g2).scale(scale, scale);
	        if (icon != null) icon.paintIcon(null, g2, 0, 0);
	        //was: g2.drawImage(icon.getImage(), 0, 0, null);
    	}
    	return bi;
    }
    
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.drawImage(getImage(), x, y, null);
    }
}                      

