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
	


