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
//import javax.help.*;

import com.viaoa.jfc.*;
import com.viaoa.jfc.text.view.DialogLayout2;
import com.viaoa.util.*;

/**
 * Used to insert an HTML table into HTML document.
 * @author vvia
 *
 */
public class TableDialog extends JDialog {
	protected boolean m_succeeded = false;

	protected JSpinner m_rowsSpn;
	protected JSpinner m_colsSpn;
	protected JSpinner m_spacingSpn;
	protected JSpinner m_paddingSpn;

	protected JSpinner m_borderWidthSpn;
	protected JSpinner m_tableWidthSpn;
	protected JSpinner m_tableHeightSpn;
	protected JComboBox m_tableUnitsCb;

	protected JTextPane m_previewPane;

	protected Color m_borderColor;
	protected Color m_backgroundColor;

	protected HTMLEditorKit m_kit = new HTMLEditorKit();

	public TableDialog(Window parent) {
		super(parent, "Insert Table", ModalityType.APPLICATION_MODAL);

		ActionListener lst;
		JButton bt;

		JPanel pp = new JPanel(new DialogLayout2());
		pp.setBorder(new EmptyBorder(10, 10, 5, 10));

		JPanel p1 = new JPanel(new DialogLayout2());
		p1.setBorder(new EmptyBorder(10, 10, 5, 10));

		p1.add(new JLabel("Rows:"));
		m_rowsSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(2), new Integer(0), null, new Integer(1)));
		p1.add(m_rowsSpn);

		p1.add(new JLabel("Columns:"));
		m_colsSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(2), new Integer(0), null, new Integer(1)));
		p1.add(m_colsSpn);

		p1.add(new JLabel("Cell spacing:"));
		m_spacingSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(2), new Integer(0), null, new Integer(1)));
		p1.add(m_spacingSpn);

		p1.add(new JLabel("Cell padding:"));
		m_paddingSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(2), new Integer(0), null, new Integer(1)));
		p1.add(m_paddingSpn);

		JPanel p2 = new JPanel(new DialogLayout2());
		p2.setBorder(new EmptyBorder(10, 10, 5, 10));

		p2.add(new JLabel("Border width:"));
		m_borderWidthSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(1), new Integer(0), null, new Integer(1)));
		p2.add(m_borderWidthSpn);

		p2.add(new JLabel("Table width:"));
		m_tableWidthSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(0), new Integer(0), null, new Integer(1)));
		p2.add(m_tableWidthSpn);

		p2.add(new JLabel("Table height:"));
		m_tableHeightSpn = new JSpinner(new SpinnerNumberModel(
			new Integer(0), new Integer(0), null, new Integer(1)));
		p2.add(m_tableHeightSpn);

		p2.add(new JLabel("Units:"));
		m_tableUnitsCb = new JComboBox(new String[] {"Percent", "Pixels" });
		p2.add(m_tableUnitsCb);

		JPanel p3 = new JPanel(new FlowLayout());
		p3.setBorder(new EmptyBorder(10, 10, 5, 10));
		JPanel pb = new JPanel(new GridLayout(2, 1, 5, 5));
		p3.add(pb);

		bt = new JButton("Border");
		bt.setMnemonic('b');
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_borderColor = JColorChooser.showDialog(TableDialog.this,
					"Border Color", m_borderColor);
			}
		};
		bt.addActionListener(lst);
		pb.add(bt);

		bt = new JButton("Background");		// Changed - Pavel
		bt.setMnemonic('c');
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_backgroundColor = JColorChooser.showDialog(TableDialog.this,
					"Background Color", m_backgroundColor);
			}
		};
		bt.addActionListener(lst);
		pb.add(bt);

		JPanel p4 = new JPanel(new BorderLayout());
		p4.setBorder(new EmptyBorder(10, 10, 5, 10));

		m_previewPane = new JTextPane();
		m_previewPane.setEditorKit(m_kit);
		m_previewPane.setBackground(Color.white);
		m_previewPane.setEditable(false);
		JScrollPane sp = new JScrollPane(m_previewPane);
		sp.setPreferredSize(new Dimension(200, 100));
		p4.add(sp, BorderLayout.CENTER);

		final JTabbedPane tb = new JTabbedPane();
		tb.addTab("Table", p1);
		tb.addTab("Size", p2);
		tb.addTab("Color", p3);
		tb.addTab("Preview", p4);
		pp.add(tb);

		ChangeListener chl = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (tb.getSelectedIndex() != 3)
					return;
				setCursor(Cursor.getPredefinedCursor(
					Cursor.WAIT_CURSOR));
				try {
					HTMLDocument doc = (HTMLDocument)m_kit.createDefaultDocument();
					doc.setAsynchronousLoadPriority(0);
					StringReader sr = new StringReader(getHTML());
					m_kit.read(sr, doc, 0);
					sr.close();

					m_previewPane.setDocument(doc);
					validate();
					repaint();
				}
				catch (Exception ex) {
					ex.printStackTrace();
				}
				finally {
					setCursor(Cursor.getPredefinedCursor(
						Cursor.DEFAULT_CURSOR));
				}
			}
		};
		tb.addChangeListener(chl);

		bt = new JButton("Insert");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_succeeded = true;
				dispose();
			}
		};
		bt.addActionListener(lst);
		pp.add(bt);

		bt = new JButton("Cancel");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		};
		bt.addActionListener(lst);
		pp.add(bt);

		String cmd = "escape";
        bt.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,false), cmd);
        bt.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
		
		
		getContentPane().add(pp, BorderLayout.CENTER);
		pack();
		setResizable(true);
		setLocationRelativeTo(parent);
	}

	public boolean succeeded() {
		return m_succeeded;
	}

	public String getHTML() {
		StringBuffer buff = new StringBuffer(8192);
		buff.append(" <table");

		int tableWidth = ((Integer)m_tableWidthSpn.getValue()).intValue();
		int tableHeight = ((Integer)m_tableHeightSpn.getValue()).intValue();
		String unit = "";
		if (m_tableUnitsCb.getSelectedIndex()==0)
			unit = "%";
		if (tableWidth > 0)
			buff.append(" width=\"").append(tableWidth).append(unit).append("\"");
		if (tableHeight > 0)
			buff.append(" height=\"").append(tableHeight).append(unit).append("\"");

		buff.append(" cellspacing=\"").append(m_spacingSpn.getValue()).append("\"");
		buff.append(" cellpadding=\"").append(m_paddingSpn.getValue()).append("\"");
		buff.append(" border=\"").append(m_borderWidthSpn.getValue()).append("\"");
		if (m_borderColor != null)
			buff.append(" bordercolor=\"").append(OAString.colorToHex(m_borderColor)).append("\"");
		if (m_backgroundColor != null)
			buff.append(" bgcolor=\"").append(OAString.colorToHex(m_backgroundColor)).append("\"");
		buff.append(">\n");

		int nRows = ((Integer)m_rowsSpn.getValue()).intValue();
		int nCols = ((Integer)m_colsSpn.getValue()).intValue();
		for (int k=0; k<nRows; k++) {
			buff.append("<tr>\n");
			for (int s=0; s<nCols; s++)
				buff.append("<td>&nbsp;</td>\n");
			buff.append("</tr>\n");
		}

		buff.append("</table> \n");
		return buff.toString();
	}

}
