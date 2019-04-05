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
package com.viaoa.jfc.text.view;

import java.util.*;

import java.io.*;
import java.awt.*;
import java.awt.font.*;
import java.awt.event.*;
import java.awt.print.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.viaoa.jfc.*;

/**
 * Adds a search/find disloag to textfield.
 * @author vvia
 *
 */
public class FindDialog extends JDialog {

	protected JTabbedPane m_tb;
	protected JTextField m_txtFind1;
	protected JTextField m_txtFind2;
	protected Document m_docFind;
	protected Document m_docReplace;
	protected ButtonModel m_modelWord;
	protected ButtonModel m_modelCase;
	protected ButtonModel m_modelUp;
	protected ButtonModel m_modelDown;

	protected int m_searchIndex = -1;
	protected boolean m_searchUp = false;
	protected String m_searchData;
	private JTextComponent editor;

	public FindDialog(JFrame frm, JTextComponent ed) {
        // this((Window)frm, ed);
        super(frm, "Find and Replace", false);
        this.editor = ed;
        setup();
        setLocationRelativeTo(frm);
	}
    public FindDialog(Window window, JTextComponent ed) {
        super(window, "Find and Replace", ModalityType.MODELESS);
        this.editor = ed;
        setup();
        setLocationRelativeTo(window);
    }
    private void setup() {
		m_tb = new JTabbedPane();

		// "Find" panel
		JPanel p1 = new JPanel(new BorderLayout());

		JPanel pc1 = new JPanel(new BorderLayout());

		JPanel pf = new JPanel();
		pf.setLayout(new DialogLayout2(20, 5));
		pf.setBorder(new EmptyBorder(8, 5, 8, 0));
		pf.add(new JLabel("Find:"));

		m_txtFind1 = new JTextField();
		m_docFind = m_txtFind1.getDocument();
		pf.add(m_txtFind1);
		pc1.add(pf, BorderLayout.CENTER);

		JPanel po = new JPanel(new GridLayout(2, 2, 8, 2));
		po.setBorder(new TitledBorder(new EtchedBorder(),
			"Options"));

		JCheckBox chkWord = new JCheckBox("Whole words only");
		chkWord.setMnemonic('w');
		m_modelWord = chkWord.getModel();
		po.add(chkWord);

		ButtonGroup bg = new ButtonGroup();
		JRadioButton rdUp = new JRadioButton("Search up");
		rdUp.setMnemonic('u');
		m_modelUp = rdUp.getModel();
		bg.add(rdUp);
		po.add(rdUp);

		JCheckBox chkCase = new JCheckBox("Match case");
		chkCase.setMnemonic('c');
		m_modelCase = chkCase.getModel();
		po.add(chkCase);

		JRadioButton rdDown = new JRadioButton("Search down", true);
		rdDown.setMnemonic('d');
		m_modelDown = rdDown.getModel();
		bg.add(rdDown);
		po.add(rdDown);
		pc1.add(po, BorderLayout.SOUTH);

		p1.add(pc1, BorderLayout.CENTER);

		JPanel p01 = new JPanel(new FlowLayout());
		JPanel p = new JPanel(new GridLayout(2, 1, 2, 8));

		ActionListener findAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findNext(false, true);
			}
		};
		JButton btFind = new JButton("Find Next");
		btFind.addActionListener(findAction);
		btFind.setMnemonic('f');
		p.add(btFind);

		ActionListener closeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		};
		JButton btClose = new JButton("Close");
		btClose.addActionListener(closeAction);
		btClose.setDefaultCapable(true);
		p.add(btClose);
		p01.add(p);
		p1.add(p01, BorderLayout.EAST);

		m_tb.addTab("Find", p1);

		// "Replace" panel
		JPanel p2 = new JPanel(new BorderLayout());

		JPanel pc2 = new JPanel(new BorderLayout());

		JPanel pc = new JPanel();
		pc.setLayout(new DialogLayout2(20, 5));
		pc.setBorder(new EmptyBorder(8, 5, 8, 0));

		pc.add(new JLabel("Find:"));
		m_txtFind2 = new JTextField();
		m_txtFind2.setDocument(m_docFind);
		pc.add(m_txtFind2);

		pc.add(new JLabel("Replace:"));
		JTextField txtReplace = new JTextField();
		m_docReplace = txtReplace.getDocument();
		pc.add(txtReplace);
		pc2.add(pc, BorderLayout.CENTER);

		po = new JPanel(new GridLayout(2, 2, 8, 2));
		po.setBorder(new TitledBorder(new EtchedBorder(),
			"Options"));

		chkWord = new JCheckBox("Whole words only");
		chkWord.setMnemonic('w');
		chkWord.setModel(m_modelWord);
		po.add(chkWord);

		bg = new ButtonGroup();
		rdUp = new JRadioButton("Search up");
		rdUp.setMnemonic('u');
		rdUp.setModel(m_modelUp);
		bg.add(rdUp);
		po.add(rdUp);

		chkCase = new JCheckBox("Match case");
		chkCase.setMnemonic('c');
		chkCase.setModel(m_modelCase);
		po.add(chkCase);

		rdDown = new JRadioButton("Search down", true);
		rdDown.setMnemonic('d');
		rdDown.setModel(m_modelDown);
		bg.add(rdDown);
		po.add(rdDown);
		pc2.add(po, BorderLayout.SOUTH);

		p2.add(pc2, BorderLayout.CENTER);

		JPanel p02 = new JPanel(new FlowLayout());
		p = new JPanel(new GridLayout(3, 1, 2, 8));

		ActionListener replaceAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				findNext(true, true);
			}
		};
		JButton btReplace = new JButton("Replace");
		btReplace.addActionListener(replaceAction);
		btReplace.setMnemonic('r');
		p.add(btReplace);

		ActionListener replaceAllAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int counter = 0;
				while (true) {
					int result = findNext(true, false);
					if (result < 0)		// error
						return;
					else if (result == 0)		// no more
						break;
					counter++;
				}
				JOptionPane.showMessageDialog(FindDialog.this,
					counter+" replacement(s) have been done",
					"",
					JOptionPane.INFORMATION_MESSAGE);
			}
		};
		JButton btReplaceAll = new JButton("Replace All");
		btReplaceAll.addActionListener(replaceAllAction);
		btReplaceAll.setMnemonic('a');
		p.add(btReplaceAll);

		btClose = new JButton("Close");
		btClose.addActionListener(closeAction);
		btClose.setDefaultCapable(true);
		p.add(btClose);
		p02.add(p);
		p2.add(p02, BorderLayout.EAST);

		// Make button columns the same size
		p01.setPreferredSize(p02.getPreferredSize());

		m_tb.addTab("Replace", p2);

		// m_tb.setSelectedIndex(index);

		JPanel pp = new JPanel(new BorderLayout());
		pp.setBorder(new EmptyBorder(5,5,5,5));
		pp.add(m_tb, BorderLayout.CENTER);
		getContentPane().add(pp, BorderLayout.CENTER);

		pack();
		setResizable(false);

		WindowListener flst = new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				m_searchIndex = -1;
			}

			public void windowDeactivated(WindowEvent e) {
				m_searchData = null;
			}
		};
		addWindowListener(flst);


		String cmd = "esc";
		m_tb.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), cmd);
		m_tb.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        cmd = "find";
        m_tb.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), cmd);
        m_tb.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (m_tb.getSelectedIndex() == 0) findNext(false, true);
                else findNext(true, true);
            }
        });

	}

	public void setSelectedIndex(int index) {
		m_tb.setSelectedIndex(index);
		setVisible(true);
		if (index == 0) {
		    m_txtFind1.requestFocus();
		    m_txtFind1.selectAll();
		}
        else {
            m_txtFind2.requestFocus();
            m_txtFind2.selectAll();
        }
		m_searchIndex = -1;
	}

	public int findNext(boolean doReplace, boolean showWarnings) {
		JTextComponent monitor = editor;
		int pos = monitor.getCaretPosition();
		if (m_modelUp.isSelected() != m_searchUp) {
			m_searchUp = m_modelUp.isSelected();
			m_searchIndex = -1;
		}

		if (m_searchIndex == -1) {
			try {
				Document doc = editor.getDocument();
				if (m_searchUp)
					m_searchData = doc.getText(0, pos);
				else
					m_searchData = doc.getText(pos, doc.getLength()-pos);
				m_searchIndex = pos;
			}
			catch (BadLocationException ex) {
				warning(ex.toString());
				return -1;
			}
		}

		String key = "";
		try {
			key = m_docFind.getText(0, m_docFind.getLength());
		}
		catch (BadLocationException ex) {}
		if (key.length()==0) {
			warning("Please enter the target to search");
			return -1;
		}
		if (!m_modelCase.isSelected()) {
			m_searchData = m_searchData.toLowerCase();
			key = key.toLowerCase();
		}
		if (m_modelWord.isSelected()) {
			for (int k=0; k<Utils.WORD_SEPARATORS.length; k++) {
				if (key.indexOf(Utils.WORD_SEPARATORS[k]) >= 0) {
					warning("The text target contains an illegal "+
						"character \'"+Utils.WORD_SEPARATORS[k]+"\'");
					return -1;
				}
			}
		}

		String replacement = "";
		if (doReplace) {
			try {
				replacement = m_docReplace.getText(0,
					m_docReplace.getLength());
			} catch (BadLocationException ex) {}
		}

		int xStart = -1;
		int xFinish = -1;
		while (true)
		{
			if (m_searchUp)
				xStart = m_searchData.lastIndexOf(key, pos-1);
			else
				xStart = m_searchData.indexOf(key, pos-m_searchIndex);
			if (xStart < 0) {
				if (showWarnings)
					warning("Text not found");
				return 0;
			}

			xFinish = xStart+key.length();

			if (m_modelWord.isSelected()) {
				boolean s1 = xStart>0;
				boolean b1 = s1 && !Utils.isSeparator(m_searchData.charAt(
					xStart-1));
				boolean s2 = xFinish<m_searchData.length();
				boolean b2 = s2 && !Utils.isSeparator(m_searchData.charAt(
					xFinish));

				if (b1 || b2)		// Not a whole word
				{
					if (m_searchUp && s1)		// Can continue up
					{
						pos = xStart;
						continue;
					}
					if (!m_searchUp && s2)		// Can continue down
					{
						pos = xFinish+1;
						continue;
					}
					// Found, but not a whole word, and we cannot continue
					if (showWarnings)
						warning("Text not found");
					return 0;
				}
			}
			break;
		}

		if (!m_searchUp) {
			xStart += m_searchIndex;
			xFinish += m_searchIndex;
		}
		if (doReplace) {
		    setSelection(xStart, xFinish, m_searchUp);
			monitor.replaceSelection(replacement);
			setSelection(xStart, xStart+replacement.length(),m_searchUp);
			m_searchIndex = -1;
		}
		else {
			setSelection(xStart, xFinish, m_searchUp);
		}
		return 1;
	}

	public void setSelection(int xStart, int xFinish, boolean moveUp) {
        if (editor != null) {
		    if (moveUp) {
		    	editor.setCaretPosition(xFinish);
		    	editor.moveCaretPosition(xStart);
		    }
		    else editor.select(xStart, xFinish);
        }
	}

	public JTabbedPane getTabbedPane() {
	    return m_tb;
	}
	
	protected void warning(String message) {
		JOptionPane.showMessageDialog(this,
			message, "",
			JOptionPane.INFORMATION_MESSAGE);
	}
}

class Utils
{
	public static String colorToHex(Color color) {
		String colorstr = new String("#");

		// Red
		String str = Integer.toHexString(color.getRed());
		if (str.length() > 2)
			str = str.substring(0, 2);
		else if (str.length() < 2)
			colorstr += "0" + str;
		else
			colorstr += str;

		// Green
		str = Integer.toHexString(color.getGreen());
		if (str.length() > 2)
			str = str.substring(0, 2);
		else if (str.length() < 2)
			colorstr += "0" + str;
		else
			colorstr += str;

		// Blue
		str = Integer.toHexString(color.getBlue());
		if (str.length() > 2)
			str = str.substring(0, 2);
		else if (str.length() < 2)
			colorstr += "0" + str;
		else
			colorstr += str;

		return colorstr;
	}

	public static final char[] WORD_SEPARATORS = {' ', '\t', '\n',
		'\r', '\f', '.', ',', ':', '-', '(', ')', '[', ']', '{',
		'}', '<', '>', '/', '|', '\\', '\'', '\"'};

	public static boolean isSeparator(char ch) {
		for (int k=0; k<WORD_SEPARATORS.length; k++)
			if (ch == WORD_SEPARATORS[k])
				return true;
		return false;
	}

	// NEW
	public static String soundex(String word) {
		char[] result = new char[4];
		result[0] = word.charAt(0);
		result[1] = result[2] = result[3] = '0';
		int index = 1;

		char codeLast = '*';
		for (int k=1; k<word.length(); k++) {
			char ch = word.charAt(k);
			char code = ' ';
			switch (ch) {
				case 'b': case 'f': case 'p': case 'v':
					code = '1';
					break;
				case 'c': case 'g': case 'j': case 'k':
				case 'q': case 's': case 'x': case 'z':
					code = '2';
					break;
				case 'd': case 't':
					code = '3';
					break;
				case 'l':
					code = '4';
					break;
				case 'm': case 'n':
					code = '5';
					break;
				case 'r':
					code = '6';
					break;
				default:
					code = '*';
					break;
			}
			if (code == codeLast)
				code = '*';
			codeLast = code;
			if (code != '*') {
				result[index] = code;
				index++;
				if (index > 3)
					break;
			}
		}
		return new String(result);
	}

	public static boolean hasDigits(String word) {
		for (int k=1; k<word.length(); k++) {
			char ch = word.charAt(k);
			if (Character.isDigit(ch))
				return true;
		}
		return false;
	}

	public static String titleCase(String source) {
		return Character.toUpperCase(source.charAt(0)) +
			source.substring(1);
	}
}
