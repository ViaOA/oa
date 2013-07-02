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
package com.viaoa.jfc.text.view;

import java.util.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;


public class OpenList extends JPanel implements ListSelectionListener, ActionListener {
	protected JLabel m_title;
	protected JTextField m_text;
	protected JList m_list;
	protected JScrollPane m_scroll;

	public OpenList(int[] data, String title) {
		setLayout(null);
		m_title = new JLabel(title, JLabel.LEFT);
		add(m_title);
		m_text = new JTextField();
		m_text.addActionListener(this);
		add(m_text);
		String[] ss = new String[data.length];
		for (int i=0; i<data.length; i++) ss[i] = data[i]+"pt";
		m_list = new JList(ss);
		    
		
		m_list.setVisibleRowCount(5);
		m_list.addListSelectionListener(this);
		m_list.setFont(m_text.getFont());
		m_scroll = new JScrollPane(m_list);
		add(m_scroll);
	}

    public OpenList(String[] data, String title) {
        setLayout(null);
        m_title = new JLabel(title, JLabel.LEFT);
        add(m_title);
        m_text = new JTextField();
        m_text.addActionListener(this);
        add(m_text);
        m_list = new JList(data);
        m_list.setVisibleRowCount(4);
        m_list.addListSelectionListener(this);
        m_list.setFont(m_text.getFont());
        m_scroll = new JScrollPane(m_list);
        add(m_scroll);
    }
	
	// NEW
	public OpenList(String title, int numCols) {
		setLayout(null);
		m_title = new JLabel(title, JLabel.LEFT);
		add(m_title);
		m_text = new JTextField(numCols);
		m_text.addActionListener(this);
		add(m_text);
		m_list = new JList();
		m_list.setVisibleRowCount(4);
		m_list.addListSelectionListener(this);
		m_scroll = new JScrollPane(m_list);
		add(m_scroll);
	}

	public void append(String[] suggestions) {
		m_text.setText("");
		DefaultListModel model = new DefaultListModel();
        for (int i=0; suggestions != null && i < suggestions.length; i++) {
			String str = suggestions[i];
			model.addElement(str);
		}

		m_list.setModel(model);
		if (model.getSize() > 0)
			m_list.setSelectedIndex(0);
	}

	public int getSelectedIndex() {
	    return m_list.getSelectedIndex();
	}
	 
	public void setSelected(String sel) {
		m_list.setSelectedValue(sel, true);
		m_text.setText(sel);
	}

	public String getSelected() { return m_text.getText(); }

	public void setSelectedInt(int value) {
		setSelected(Integer.toString(value));
	}

	public int getSelectedInt() {
		try {
			return Integer.parseInt(getSelected());
		}
		catch (NumberFormatException ex) { return -1; }
	}

	public void valueChanged(ListSelectionEvent e) {
		Object obj = m_list.getSelectedValue();
		if (obj != null)
			m_text.setText(obj.toString());
	}

	public void actionPerformed(ActionEvent e) {
		ListModel model = m_list.getModel();
		String key = m_text.getText().toLowerCase();
		for (int k=0; k<model.getSize(); k++) {
			String data = (String)model.getElementAt(k);
			if (data.toLowerCase().startsWith(key)) {
				m_list.setSelectedValue(data, true);
				break;
			}
		}
	}

	public void addListSelectionListener(ListSelectionListener lst) {
		m_list.addListSelectionListener(lst);
	}

	public Dimension getPreferredSize() {
		Insets ins = getInsets();
		Dimension d1 = m_title.getPreferredSize();
		Dimension d2 = m_text.getPreferredSize();
		Dimension d3 = m_scroll.getPreferredSize();
		int w = Math.max(Math.max(d1.width, d2.width), d3.width);
		int h = d1.height + d2.height + d3.height;
		return new Dimension(w+ins.left+ins.right,
			h+ins.top+ins.bottom);
	}

	public Dimension getMaximumSize() {
		Insets ins = getInsets();
		Dimension d1 = m_title.getMaximumSize();
		Dimension d2 = m_text.getMaximumSize();
		Dimension d3 = m_scroll.getMaximumSize();
		int w = Math.max(Math.max(d1.width, d2.width), d3.width);
		int h = d1.height + d2.height + d3.height;
		return new Dimension(w+ins.left+ins.right,
			h+ins.top+ins.bottom);
	}

	public Dimension getMinimumSize() {
		Insets ins = getInsets();
		Dimension d1 = m_title.getMinimumSize();
		Dimension d2 = m_text.getMinimumSize();
		Dimension d3 = m_scroll.getMinimumSize();
		int w = Math.max(Math.max(d1.width, d2.width), d3.width);
		int h = d1.height + d2.height + d3.height;
		return new Dimension(w+ins.left+ins.right,
			h+ins.top+ins.bottom);
	}

	public void doLayout() {
		Insets ins = getInsets();
		Dimension d = getSize();
		int x = ins.left;
		int y = ins.top;
		int w = d.width-ins.left-ins.right;
		int h = d.height-ins.top-ins.bottom;

		Dimension d1 = m_title.getPreferredSize();
		m_title.setBounds(x, y, w, d1.height);
		y += d1.height;
		Dimension d2 = m_text.getPreferredSize();
		m_text.setBounds(x, y, w, d2.height);
		y += d2.height;
		m_scroll.setBounds(x, y, w, h-y);
	}
}


