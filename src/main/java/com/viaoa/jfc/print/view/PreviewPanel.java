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
package com.viaoa.jfc.print.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;


/**
 * Used to display PagePanels for the PrintPreviewDialog.
 * @author vincevia
 */
public class PreviewPanel extends JPanel implements Scrollable {
	protected int H_GAP = 16;
	protected int V_GAP = 10;
    protected int incScrollAmountX=30, incScrollAmountY=40;

	public Dimension getPreferredSize() {
		int n = getComponentCount();
		if (n == 0) return new Dimension(H_GAP, V_GAP);

		Component comp = getComponent(0);
		Dimension dc = comp.getPreferredSize();
		int w = dc.width;
		int h = dc.height;

		Dimension dp = getParent().getSize();
		int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
		int nRow = n/nCol;
		if (nRow*nCol < n) nRow++;

		int ww = nCol*(w+H_GAP) + H_GAP;
		int hh = nRow*(h+V_GAP) + V_GAP;
		Insets ins = getInsets();
		return new Dimension(ww+ins.left+ins.right,hh+ins.top+ins.bottom);
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public void doLayout() {
		Insets ins = getInsets();
		int x = ins.left + H_GAP;
		int y = ins.top + V_GAP;

		int n = getComponentCount();
		if (n == 0)
			return;
		Component comp = getComponent(0);
		Dimension dc = comp.getPreferredSize();
		int w = dc.width;
		int h = dc.height;

		Dimension dp = getParent().getSize();
		int nCol = Math.max((dp.width-H_GAP)/(w+H_GAP), 1);
		int nRow = n/nCol;
		if (nRow*nCol < n)
			nRow++;

		int index = 0;
		for (int k = 0; k<nRow; k++) {
			for (int m = 0; m<nCol; m++) {
				if (index >= n)
					return;
				comp = getComponent(index++);
				comp.setBounds(x, y, w, h);
				x += w+H_GAP;
			}
			y += h+V_GAP;
			x = ins.left + H_GAP;
		}
	}

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.HORIZONTAL) return (incScrollAmountX);
        return incScrollAmountY;
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableBlockIncrement(Rectangle visibleRect,int orientation,int direction) {
        JViewport vp = (JViewport) this.getParent();
        Dimension d2 = vp.getExtentSize();

        if (orientation == SwingConstants.HORIZONTAL) return d2.width;
        return d2.height;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
}
