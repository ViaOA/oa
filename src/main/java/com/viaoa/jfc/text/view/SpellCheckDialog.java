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


import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.viaoa.jfc.text.spellcheck.SpellChecker;


/**
 * Dialog used to add spellcheck dialog to a textfield.
 * @author vvia
 *
 */
public class SpellCheckDialog extends JDialog implements ActionListener {
    protected JTextField txtNotFound;
    protected OpenList lstSuggestions;
    protected String searchWord;
    protected boolean bCanceled;
    protected SpellChecker spellChecker;  // needed for callbacks
    protected Hashtable hashIgnore;

	public SpellCheckDialog(JFrame frm, SpellChecker spellChecker) {
		super(frm, "Spelling", true);
		setResizable(true);
		this.spellChecker = spellChecker;
		

		JPanel p = new JPanel();
		p.setBorder(new EmptyBorder(5, 5, 5, 5));
		p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
		p.add(new JLabel("Not in dictionary:"));
		p.add(Box.createHorizontalStrut(10));
		txtNotFound = new JTextField();
		txtNotFound.setEditable(false);
		p.add(txtNotFound);
		getContentPane().add(p, BorderLayout.NORTH);

		lstSuggestions = new OpenList("Change to:", 12);
		lstSuggestions.setBorder(new EmptyBorder(0, 5, 5, 5));
		getContentPane().add(lstSuggestions, BorderLayout.CENTER);

		JPanel p1 = new JPanel();
		p1.setBorder(new EmptyBorder(20, 0, 5, 5));
		p1.setLayout(new FlowLayout());
		p = new JPanel(new GridLayout(3, 2, 8, 2));

		JButton cmd = new JButton("Change");
		cmd.setActionCommand("change");
		cmd.addActionListener(this);
		cmd.setMnemonic('c');
		p.add(cmd);

		cmd = new JButton("Add");
		cmd.setActionCommand("add");
		cmd.addActionListener(this);
		cmd.setMnemonic('a');
		p.add(cmd);

		cmd = new JButton("Ignore");
		cmd.setActionCommand("ignore");
		cmd.addActionListener(this);
		cmd.setMnemonic('i');
		p.add(cmd);

		cmd = new JButton("Ignore All");
		cmd.setActionCommand("ignoreAll");
		cmd.addActionListener(this);
		cmd.setMnemonic('g');
		p.add(cmd);

		cmd = new JButton("Close");
		cmd.setActionCommand("close");
		cmd.addActionListener(this);
        cmd.registerKeyboardAction(this, "close", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
			
		cmd.setDefaultCapable(true);
		p.add(cmd);
		p1.add(p);
		getContentPane().add(p1, BorderLayout.EAST);

		pack();
		setLocationRelativeTo(frm);
	}

	public boolean suggest(String word, String[] results, Hashtable hashIgnore) {
	    this.hashIgnore = hashIgnore;
		bCanceled = false;
		searchWord = word;
		txtNotFound.setText(word);
		lstSuggestions.append(results);

		show();
		
		return bCanceled;
	}

    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (cmd.equalsIgnoreCase("change")) {
			spellChecker.replaceSelection(searchWord, (String) lstSuggestions.getSelected());
			bCanceled = true;
			setVisible(false);
		}
        else if (cmd.equalsIgnoreCase("add")) {
			spellChecker.addNewWord(searchWord);
			bCanceled = true;
			setVisible(false);
        }
        else if (cmd.equalsIgnoreCase("ignore")) {
			bCanceled = true;
			setVisible(false);
        }
        else if (cmd.equalsIgnoreCase("ignoreAll")) {
		    hashIgnore.put(searchWord.toLowerCase(), searchWord);
		    bCanceled = true;
		    setVisible(false);
        }
        else if (cmd.equalsIgnoreCase("close")) {
		    bCanceled = false;
		    setVisible(false);
        }
    }
}

