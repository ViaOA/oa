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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.border.MatteBorder;


/**
 * Used to display each individual page for PrintPreviewDialog.
 * @author vincevia
 * @see PreviewPanel
 * @see PrintPreviewDialog
 */
public abstract class PagePanel extends JPanel {
    protected int page;
	protected int m_w;
	protected int m_h;

	public PagePanel(int page) {
	    this.page = page;
		setBackground(Color.white);
		setBorder(new MatteBorder(1, 1, 2, 2, Color.black));
	}

	
	public void setScaledSize(int w, int h) {
		m_w = w;
		m_h = h;
		repaint();
	}

	public Dimension getPreferredSize() {
		Insets ins = getInsets();
		return new Dimension(m_w+ins.left+ins.right,
			m_h+ins.top+ins.bottom);
	}

	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	public void paint(Graphics g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage(getImage(), 0, 0, m_w, m_h, this);
		paintBorder(g);
	}

	public int getPage() {
	    return page;
	}
	
    protected abstract Image getImage();

}
	


