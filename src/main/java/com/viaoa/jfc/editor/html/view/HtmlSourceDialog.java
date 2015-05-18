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
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;

/**
 * Dialog used to display HTML source code.
 * @author vvia
 *
 */
public class HtmlSourceDialog extends JDialog {
	protected boolean m_succeeded = false;

	protected JTextArea m_sourceTxt;

	public HtmlSourceDialog(Window parent) {
		super(parent, "HTML Source", ModalityType.APPLICATION_MODAL);

		JPanel pp = new JPanel(new BorderLayout());
		pp.setBorder(new EmptyBorder(10, 10, 5, 10));

		m_sourceTxt = new JTextArea("", 20, 60);
		m_sourceTxt.setFont(new Font("Courier", Font.PLAIN, 12));
		JScrollPane sp = new JScrollPane(m_sourceTxt);
		pp.add(sp, BorderLayout.CENTER);

		JPanel p = new JPanel(new FlowLayout());
		JPanel p1 = new JPanel(new GridLayout(1, 2, 10, 0));
		JButton bt = new JButton("Save");
		btSave = bt;
		ActionListener lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_succeeded = true;
				setVisible(false);
			}
		};
		bt.addActionListener(lst);
        bt.registerKeyboardAction(lst, "", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		p1.add(bt);

		bt = new JButton("Cancel");
		lst = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_succeeded = false;
				setVisible(false);
			}
		};
		bt.addActionListener(lst);
        bt.registerKeyboardAction(lst, "", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		p1.add(bt);
		p.add(p1);
		pp.add(p, BorderLayout.SOUTH);

		getContentPane().add(pp, BorderLayout.CENTER);
		pack();
		setResizable(true);
		if (parent != null) setLocationRelativeTo(parent);
	}

	private JButton btSave;
	public JButton getSaveButton() {
	    return btSave;
	}
	
    public void setVisible(boolean b) {
        if (b) m_succeeded = false;
        if (b) {
            m_sourceTxt.requestFocus();
            m_sourceTxt.setCaretPosition(0);
        }
        super.setVisible(b);
    }


	public boolean succeeded() {
		return m_succeeded;
	}

    public void setSource(String s) {
        m_sourceTxt.setText(s);
    }

	public String getSource() {
		return m_sourceTxt.getText();
	}
	
	public JTextArea getTextArea() {
	    return m_sourceTxt;
	}
}
