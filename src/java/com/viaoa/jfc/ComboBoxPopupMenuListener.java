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

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

/**
 * 
 * 
 */
public class ComboBoxPopupMenuListener implements PopupMenuListener {
	private JComboBox cbo;
	private int width;
	
	public ComboBoxPopupMenuListener(JComboBox cbo, int width) {
		this.cbo = cbo;
		this.width = width;
	}
	
	
	JScrollPane getScrollPane(Container cont) {
        Component[] comps = cont.getComponents();
        for (int i=0; comps != null && i < comps.length; i++) {
        	if (comps[i] instanceof JScrollPane) return (JScrollPane) comps[i];
        	if (comps[i] instanceof Container) {
        		JScrollPane sp = getScrollPane((Container) comps[i]);
        		if (sp != null) return sp;
        	}
        }
        return null;
	}

	public @Override void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		JPopupMenu pop = null;
		int x = cbo.getUI().getAccessibleChildrenCount(cbo);
		for (int i=0; i<x; i++) {
			Object obj = cbo.getUI().getAccessibleChild(cbo, i);
			if (obj instanceof JPopupMenu) {
				pop = (JPopupMenu) obj;
			}
		}
		if (pop == null) return;
		
		if (!(pop.getLayout() instanceof BorderLayout)) {
			JScrollPane sp = getScrollPane(pop);
			sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			if (sp == null) return;
			pop.setLayout(new BorderLayout());
			pop.add(sp, BorderLayout.CENTER);
			pop.pack();
		}

		Dimension d = pop.getPreferredSize();
		d.width = width;
		pop.setPreferredSize(d);
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}
	

}
