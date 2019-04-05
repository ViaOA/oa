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
import javax.swing.*;
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
            if (sp == null) return;
			sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			pop.setLayout(new BorderLayout());
			pop.add(sp, BorderLayout.CENTER);
			pop.pack();
		}

		Dimension d = pop.getPreferredSize();
		d.width = width;
		if (cbo != null) d.height = 24 * (OAJfcUtil.getCharHeight(cbo, cbo.getFont()) + 6);
		pop.setPreferredSize(d);
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {
	}
	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	}
	

}
