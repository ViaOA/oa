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
package com.viaoa.jfc.editor.html.view;

import java.util.*;

import java.io.*;
import java.awt.*;
import java.awt.Dialog.ModalityType;
import java.awt.event.*;
import java.awt.print.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.event.*;
// import javax.help.*;

import com.viaoa.jfc.*;
import com.viaoa.jfc.text.view.OpenList;

public class FontDialog extends JDialog
{
	protected boolean m_succeeded = false;
	protected OpenList m_lstFontName;
	protected OpenList m_lstFontSize;
	protected MutableAttributeSet m_attributes;
	protected JCheckBox m_chkBold;
	protected JCheckBox m_chkItalic;
	protected JCheckBox m_chkUnderline;

	protected JCheckBox m_chkStrikethrough;
	protected JCheckBox m_chkSubscript;
	protected JCheckBox m_chkSuperscript;

	protected JComboBox m_cbColor;
	protected JLabel m_preview;
	private int[] sizes;

	public FontDialog(Window parent, String[] names, int[] sizes)
	{
		super(parent, "Font", ModalityType.APPLICATION_MODAL);
		this.sizes = sizes;
		this.setResizable(true);
		
		
        JPanel px = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        Insets ins = new Insets(1, 1, 1, 1);
        gc.insets = ins;
        gc.anchor = gc.NORTHWEST;
        gc.fill = gc.BOTH;

        gc.gridwidth = gc.REMAINDER;
        px.setBorder(new EmptyBorder(5,5,5,5));

        
		JPanel pp = new JPanel();
		pp.setBorder(new EmptyBorder(5,5,5,5));
		pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));

		JPanel p = new JPanel(new GridLayout(1, 2, 10, 2));
		p.setBorder(new TitledBorder(new EtchedBorder(), "Font"));
		m_lstFontName = new OpenList(names, "Name:");
		p.add(m_lstFontName);
		
		m_lstFontSize = new OpenList(sizes, "Size:");
		p.add(m_lstFontSize);

        gc.fill = gc.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;
		px.add(p, gc);
		// pp.add(p);

		p = new JPanel(new GridLayout(2, 3, 10, 5));
		p.setBorder(new TitledBorder(new EtchedBorder(), "Effects"));
		m_chkBold = new JCheckBox("Bold");
		p.add(m_chkBold);
		m_chkItalic = new JCheckBox("Italic");
		p.add(m_chkItalic);
		m_chkUnderline = new JCheckBox("Underline");
		p.add(m_chkUnderline);
		m_chkStrikethrough = new JCheckBox("Strikeout");
		p.add(m_chkStrikethrough);
		m_chkSubscript = new JCheckBox("Subscript");
		p.add(m_chkSubscript);
		m_chkSuperscript = new JCheckBox("Superscript");
		p.add(m_chkSuperscript);
		
        gc.weighty = .0;
        gc.fill = gc.HORIZONTAL;
        px.add(p, gc);
		//pp.add(p);
		//pp.add(Box.createVerticalStrut(5));

		p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(Box.createHorizontalStrut(10));
		p.add(new JLabel("Color:"));
		p.add(Box.createHorizontalStrut(20));
		m_cbColor = new JComboBox();

		int[] values = new int[] { 0, 128, 192, 255 };
		for (int r=0; r<values.length; r++) {
			for (int g=0; g<values.length; g++) {
				for (int b=0; b<values.length; b++) {
					Color c = new Color(values[r], values[g], values[b]);
					m_cbColor.addItem(c);
				}
			}
		}

		m_cbColor.setRenderer(new ColorComboRenderer());
		p.add(m_cbColor);
		p.add(Box.createHorizontalStrut(10));

        gc.weighty = .0;
        px.add(p, gc);
		//pp.add(p);

		ListSelectionListener lsel = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updatePreview();
			}
		};
		m_lstFontName.addListSelectionListener(lsel);
		m_lstFontSize.addListSelectionListener(lsel);

		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updatePreview();
			}
		};
		m_chkBold.addActionListener(lst);
		m_chkItalic.addActionListener(lst);
		m_cbColor.addActionListener(lst);

		p = new JPanel(new BorderLayout());
		p.setBorder(new TitledBorder(new EtchedBorder(), "Preview"));
		m_preview = new JLabel("Preview Font", JLabel.CENTER);
		m_preview.setBackground(Color.white);
		m_preview.setForeground(Color.black);
		m_preview.setOpaque(true);
		m_preview.setBorder(new LineBorder(Color.black));
		m_preview.setPreferredSize(new Dimension(120, 50));
		p.add(m_preview, BorderLayout.CENTER);
        gc.weighty = .25;
        gc.fill = gc.BOTH;
		px.add(p, gc);
		//pp.add(p);

		p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0,0));
		JPanel p1 = new JPanel(new GridLayout(1, 2, 10, 0));
		JButton btOK = new JButton("OK");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_succeeded = true;
				dispose();
			}
		};
		btOK.addActionListener(lst);
		p1.add(btOK);

		JButton btCancel = new JButton("Cancel");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		btCancel.addActionListener(lst);
		
        String cmd = "escape";
        btCancel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,false), cmd);
        btCancel.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
		
		
		p1.add(btCancel);
		p.add(p1);
        gc.weighty = .0;
        gc.fill = gc.HORIZONTAL;
		px.add(p, gc);
		//pp.add(p);

		getContentPane().add(px, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(parent);
	}

	public void setAttributes(AttributeSet a) {
		m_attributes = new SimpleAttributeSet(a);
		String name = StyleConstants.getFontFamily(a);
		m_lstFontName.setSelected(name);
		int size = StyleConstants.getFontSize(a);
		m_lstFontSize.setSelectedInt(size);
		m_chkBold.setSelected(StyleConstants.isBold(a));
		m_chkItalic.setSelected(StyleConstants.isItalic(a));
		m_chkUnderline.setSelected(StyleConstants.isUnderline(a));
		m_chkStrikethrough.setSelected(
			StyleConstants.isStrikeThrough(a));
		m_chkSubscript.setSelected(StyleConstants.isSubscript(a));
		m_chkSuperscript.setSelected(StyleConstants.isSuperscript(a));
		m_cbColor.setSelectedItem(StyleConstants.getForeground(a));
		updatePreview();
	}

	public AttributeSet getAttributes() {
		if (m_attributes == null)
			return null;
		StyleConstants.setFontFamily(m_attributes,
			m_lstFontName.getSelected());
		
		
		
        int size = m_lstFontSize.getSelectedInt();
        if (size <= 0) {
            int pos = m_lstFontSize.getSelectedIndex();
            if (pos < 0 || pos >= sizes.length) size = 12;
            else size = sizes[pos];
        }
		// StyleConstants.setFontSize(m_attributes, size);
        //vv: need to set in "pt" 
		m_attributes.addAttribute(StyleConstants.FontSize, size+"pt");  		
		
		StyleConstants.setBold(m_attributes,
			m_chkBold.isSelected());
		StyleConstants.setItalic(m_attributes,
			m_chkItalic.isSelected());
		StyleConstants.setUnderline(m_attributes,
			m_chkUnderline.isSelected());
		StyleConstants.setStrikeThrough(m_attributes,
			m_chkStrikethrough.isSelected());
		StyleConstants.setSubscript(m_attributes,
			m_chkSubscript.isSelected());
		StyleConstants.setSuperscript(m_attributes,
			m_chkSuperscript.isSelected());
		StyleConstants.setForeground(m_attributes, (Color)m_cbColor.getSelectedItem());
		return m_attributes;
	}


	public boolean succeeded() {
		return m_succeeded;
	}

    @Override
    public void setVisible(boolean aFlag) {
        m_succeeded = false;        
        super.setVisible(aFlag);
    }
	
	
	protected void updatePreview() {
		String name = m_lstFontName.getSelected();
		
		int size = m_lstFontSize.getSelectedInt();
		if (size <= 0) {
	        int pos = m_lstFontSize.getSelectedIndex();
	        if (pos < 0 || pos >= sizes.length) return;
		    size = sizes[pos];
		}
		int style = Font.PLAIN;
		if (m_chkBold.isSelected())
			style |= Font.BOLD;
		if (m_chkItalic.isSelected())
			style |= Font.ITALIC;

		// Bug Alert! This doesn't work if only style is changed.
		Font fn = new Font(name, style, size);
		m_preview.setFont(fn);

		Color c = (Color)m_cbColor.getSelectedItem();
		m_preview.setForeground(c);
		m_preview.repaint();
	}
}

class ColorComboRenderer extends JPanel implements ListCellRenderer
{
	protected Color m_color = Color.black;
	protected Color m_focusColor =
		(Color) UIManager.get("List.selectionBackground");
	protected Color m_nonFocusColor = Color.white;

	public Component getListCellRendererComponent(JList list,
	 Object obj, int row, boolean sel, boolean hasFocus)
	{
		if (hasFocus || sel)
			setBorder(new CompoundBorder(
				new MatteBorder(2, 10, 2, 10, m_focusColor),
				new LineBorder(Color.black)));
		else
			setBorder(new CompoundBorder(
				new MatteBorder(2, 10, 2, 10, m_nonFocusColor),
				new LineBorder(Color.black)));

		if (obj instanceof Color)
			m_color = (Color) obj;
		return this;
	}

	public void paintComponent(Graphics g) {
		setBackground(m_color);
		super.paintComponent(g);
	}
	
}

