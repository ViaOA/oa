package com.dispatcher.editor;

import java.util.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.*;
import javax.swing.event.*;

import com.viaoa.jfc.*;


public class ComponentView {
    JToggleButton cmdBold, cmdItalic, cmdUnderline;
    JButton cmdImage, cmdFind, cmdSpell, cmdSource;
    JButton cmdFontColor;
    JToggleButton cmdCenter, cmdLeft, cmdRight, cmdJustified;
    JMenuItem miImage, miTable, miFont, miSource, miFind, miReplace;
    JComboBox cboFont, cboFontSize;

    String[] fontNames;
    String[] genericFontFamily;
    String[] fontSizes;

    protected ColorMenu foregroundColorMenu, colorMenu;
	protected ColorMenu backgroundColorMenu;

	
	public JMenuItem getFindMenuItem() {
		if (miFind == null) {
			miFind = new JMenuItem("Find ...");
			miFind.setMnemonic('F');
		}
		return miFind;
	}	
	public JMenuItem getReplaceMenuItem() {
		if (miReplace == null) {
			miReplace = new JMenuItem("Replace ...");
			miReplace.setMnemonic('R');
		}
		return miReplace;
	}	
	
	
	
	public JMenuItem getSourceMenuItem() {
		if (miSource == null) {
			miSource = new JMenuItem("HTML Source...");
	        miSource.setIcon(new ImageIcon(this.getClass().getResource("icons/Source.gif")));
			miSource.setMnemonic('s');
		}
		return miSource;
	}	
	
	
	public JMenuItem getFontMenuItem() {
		if (miFont == null) {
			miFont = new JMenuItem("Font...");
			miFont.setMnemonic('o');
		}
		return miFont;
	}

	public ColorMenu getBackgroundColorMenu() {
		if (backgroundColorMenu == null) {
	        backgroundColorMenu = new ColorMenu("Background color");
	        backgroundColorMenu.setMnemonic('f');
		}
		return backgroundColorMenu;
	}
	
	public ColorMenu getForegroundColorMenu() {
		if (foregroundColorMenu == null) {
	        foregroundColorMenu = new ColorMenu("Foreground color");
	        foregroundColorMenu.setMnemonic('f');
		}
		return foregroundColorMenu;
	}
	
	
    public JComboBox getFontSizeComboBox() {
    	if (cboFontSize == null) {
    		getFontSizes();
    		cboFontSize = new JComboBox(fontSizes) {
                public boolean isFocusTraversable() {
                    return false;
                }
    		};
    		cboFontSize.setToolTipText("Set/Change size of font");

    		// cboSize.setEditable(true);
            int w = cboFontSize.getMinimumSize().width;
            w += cboFontSize.getFontMetrics(cboFontSize.getFont()).stringWidth("Mx");
            cboFontSize.setMinimumSize(new Dimension(w, 25));
            cboFontSize.setMaximumSize(new Dimension(w, 25));
            cboFontSize.setPreferredSize(new Dimension(w, 25));
            cboFontSize.setMaximumRowCount(10);
            cboFontSize.setRequestFocusEnabled(false);
            cboFontSize.setSelectedIndex(-1);
    	}
    	return cboFontSize;
    }
    
    public String[] getFontSizes() {
    	if (fontSizes == null) {
    		// from CSS.java: static int sizeMap[] = { 8, 10, 12, 14, 18, 24, 36 };
    		fontSizes = new String[] {"8", "10", "12", "14", "18", "24", "36" };
    	}
    	return fontSizes;
    }
    
    public String[] getFontNames() {
    	if (fontNames == null) {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			fontNames = ge.getAvailableFontFamilyNames(Locale.getDefault());
    	}
    	return fontNames;
    }
    
    public String[] getGenericFontNames() {
    	if (genericFontFamily == null) {
	        genericFontFamily = new String[] {"serif", "sans-serif", "cursive", "fantasy", "monospace" };
    	}
    	return genericFontFamily;
    }
    
    
    public JComboBox getFontComboBox() {
    	if (cboFont == null) {
	
			getGenericFontNames();
	        Vector vec = new Vector();
	        for (int i=0; i<genericFontFamily.length; i++) vec.add(genericFontFamily[i]);

	        getFontNames();
	        for (int i=0; i<fontNames.length; i++) vec.add(fontNames[i]);
	
			cboFont = new JComboBox(vec) {
	            public boolean isFocusTraversable() {
	                return false;
	            }
			};
	        cboFont.setRenderer(new MyComboBoxCellRenderer());
			cboFont.setToolTipText("Set/Change Font");
			cboFont.setMaximumRowCount(25);
			cboFont.setRequestFocusEnabled(false);

			/*
			String s = "";
			for (int i=0; fontNames != null && i<fontNames.length; i++) {
			    if (fontNames[i].length() > s.length()) s = fontNames[i];
			}
			s += "MMMMMM";
			w = cboFont.getFontMetrics(cboFont.getFont()).stringWidth(s);
	        */

	        int w = cboFont.getFontMetrics(cboFont.getFont()).stringWidth("MMX");
	        w += cboFont.getMinimumSize().width;
			cboFont.setMaximumSize(new Dimension(w, 20));
	        cboFont.setSelectedIndex(-1);
			
    	}
    	return cboFont;
    }
    
    public JMenuItem getTableMenuItem() {
    	if (miTable == null) {
            miTable = new JMenuItem("Table...");// ("Print", new ImageIcon(this.getClass().getResource("icons/Print.gif")));
            miTable.setMnemonic(KeyEvent.VK_T);
            miTable.setToolTipText("Insert Table");
    	}
    	return miTable;
    }

    public JMenuItem getImageMenuItem() {
    	if (miImage == null) {
            miImage = new JMenuItem("Image...");// ("Print", new ImageIcon(this.getClass().getResource("icons/Print.gif")));
            miImage.setToolTipText("Insert Image");
            miImage.setMnemonic(KeyEvent.VK_I);
            miImage.setToolTipText("Insert Image");
    	}
    	return miImage;
    }
    
    
    public JToggleButton getJustifiedToggleButton() {
    	if (cmdJustified == null) {
			cmdJustified = new JToggleButton();
			cmdJustified.setToolTipText("Justified-Align Paragraph");
		    cmdJustified.setIcon(new ImageIcon(this.getClass().getResource("icons/AlignJustified.gif")));
			cmdJustified.setRequestFocusEnabled(false);
		    cmdJustified.setFocusPainted(false);
		    // cmdJustified.setBorderPainted(false);
		    cmdJustified.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdJustified;
    }
    
    public JToggleButton getRightToggleButton() {
    	if (cmdRight == null) {
			cmdRight = new JToggleButton();
			cmdRight.setToolTipText("Right-Align Paragraph");
		    cmdRight.setIcon(new ImageIcon(this.getClass().getResource("icons/AlignRight.gif")));
			cmdRight.setRequestFocusEnabled(false);
		    cmdRight.setFocusPainted(false);
		    // cmdRight.setBorderPainted(false);
		    cmdRight.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdRight;
    }
    
    

    public JToggleButton getLeftToggleButton() {
    	if (cmdLeft == null) {
			cmdLeft = new JToggleButton();
			cmdLeft.setToolTipText("Left-Align Paragraph");
		    cmdLeft.setIcon(new ImageIcon(this.getClass().getResource("icons/AlignLeft.gif")));
			cmdLeft.setRequestFocusEnabled(false);
		    cmdLeft.setFocusPainted(false);
		    // cmdLeft.setBorderPainted(false);
		    cmdLeft.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdLeft;
    }
    
    public JToggleButton getCenterToggleButton() {
    	if (cmdCenter == null) {
			cmdCenter = new JToggleButton();
			cmdCenter.setToolTipText("Center-Align Paragraph");
		    cmdCenter.setIcon(new ImageIcon(this.getClass().getResource("icons/AlignCenter.gif")));
			cmdCenter.setRequestFocusEnabled(false);
		    cmdCenter.setFocusPainted(false);
		    // cmdCenter.setBorderPainted(false);
		    cmdCenter.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdCenter;
    }
    
    
    public JButton getFontColorButton() {
    	if (cmdFontColor == null) {
			cmdFontColor = new JButton();
	        cmdFontColor.setToolTipText("Font color");
	        OAButton.setup(cmdFontColor);
	        cmdFontColor.setIcon(new ImageIcon(this.getClass().getResource("icons/Color.gif")));
    	}
    	return cmdFontColor;
    }

    
    public JButton getSourceButton() {
    	if (cmdSource == null) {
			cmdSource = new JButton();
		    cmdSource.setToolTipText("Edit HTML source code");
		    OAButton.setup(cmdSource);
		    cmdSource.setIcon(new ImageIcon(this.getClass().getResource("icons/Source.gif")));
    	}
    	return cmdSource;
    }
    
    public JButton getSpellButton() {
    	if (cmdSpell == null) {
			cmdSpell = new JButton();
		    cmdSpell.setToolTipText("Spell Check");
		    OAButton.setup(cmdSpell);
		    cmdSpell.setIcon(new ImageIcon(this.getClass().getResource("icons/Spell.gif")));
    	}
		return cmdSpell;
    }
    
    
    public JButton getFindButton() {
    	if (cmdFind == null) {
			cmdFind = new JButton();
		    cmdFind.setToolTipText("Find text ^F and/or Replace Text ^R");
		    OAButton.setup(cmdFind);
		    cmdFind.setIcon(new ImageIcon(this.getClass().getResource("icons/Find.gif")));
    	}
    	return cmdFind;
    }    
    
    public JButton getImageButton() {
    	if (cmdImage == null) {
    		cmdImage = new JButton();
            cmdImage.setToolTipText("Insert Image");
            OAButton.setupButton(cmdImage);
            cmdImage.setIcon(new ImageIcon(this.getClass().getResource("icons/Image.gif")));
    	}
    	return cmdImage;
    }
    
    public JToggleButton getBoldToggleButton() {
    	if (cmdBold == null) {
			cmdBold = new JToggleButton();
		    cmdBold.setToolTipText("Bold ^B");
		    cmdBold.setIcon(new ImageIcon(this.getClass().getResource("icons/Bold.gif")));
			cmdBold.setRequestFocusEnabled(false);
		    cmdBold.setFocusPainted(false);
		    // cmdBold.setBorderPainted(false);
		    cmdBold.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdBold;
    }
	
    public JToggleButton getItalicToggleButton() {
    	if (cmdItalic == null) {
    		cmdItalic = new JToggleButton();
            cmdItalic.setToolTipText("Italic ^I");
            cmdItalic.setIcon(new ImageIcon(this.getClass().getResource("icons/Italic.gif")));
    		cmdItalic.setRequestFocusEnabled(false);
            cmdItalic.setFocusPainted(false);
            // cmdItalic.setBorderPainted(false);
            cmdItalic.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdItalic;
    }

    public JToggleButton getUnderlineToggleButton() {
    	if (cmdUnderline == null) {
			cmdUnderline = new JToggleButton();
		    cmdUnderline.setToolTipText("Underline ^U");
		    cmdUnderline.setIcon(new ImageIcon(this.getClass().getResource("icons/Underline.gif")));
			cmdUnderline.setRequestFocusEnabled(false);
		    cmdUnderline.setFocusPainted(false);
		    // cmdUnderline.setBorderPainted(false);
		    cmdUnderline.setMargin(new Insets(1,1,1,1));
    	}
    	return cmdUnderline;
    }

    
    
    
    
//qqqqqqqqqqqqq TESTING qqqqqqqqqqqqqq
    
    public JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;


		menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
		menu.add(getFindMenuItem());
		menu.add(getReplaceMenuItem());
	    menuBar.add(menu);


	    menu = new JMenu("Insert");
        menu.setMnemonic(KeyEvent.VK_I);
        menu.add(getImageMenuItem());
        menu.add(getTableMenuItem());

	    menuBar.add(menu);


		menu = new JMenu("Format");
        menu.setMnemonic(KeyEvent.VK_F);
		menu.add(getFontMenuItem());
		menu.addSeparator();
		menu.add(getForegroundColorMenu());
		// menu.add(backgroundColorMenu);
	    menuBar.add(menu);

		menu = new JMenu("Tools");
        menu.setMnemonic(KeyEvent.VK_T);
		menu.add(getSourceMenuItem());
	    menuBar.add(menu);


        menu.addSeparator();
		menu.addSeparator();

		/*
		Action[] actions = editor.getEditorKit().getActions();
		// same as: Action[] actions = editor.getActions();
        int i = 0;
        int x = actions.length;
		for (int cnt=0; i<x; cnt++) {
		    menu = new JMenu("All Commands "+cnt);
	        menuBar.add(menu);
		    for (int j=0; j<18 && i<x; j++,i++) {
		        menu.add(actions[i]);
		    }
        }
		*/

	    return menuBar;
    }

    
    public JToolBar getToolBar() {
        JToolBar toolbar = new JToolBar();

        toolbar.add(getFindButton());
        toolbar.add(getSpellButton());
        toolbar.addSeparator();

        toolbar.add(getFontComboBox());
		toolbar.add(getFontSizeComboBox());

		toolbar.addSeparator();

        toolbar.add(getBoldToggleButton());
        toolbar.add(getItalicToggleButton());
        toolbar.add(getUnderlineToggleButton());
        toolbar.addSeparator();

        toolbar.add(getFontColorButton());
        toolbar.add(getImageButton());

        toolbar.addSeparator();

        toolbar.add(getLeftToggleButton());
        toolbar.add(getCenterToggleButton());
        toolbar.add(getRightToggleButton());

        toolbar.addSeparator();
        toolbar.add(getSourceButton());
        return toolbar;
    }
    
    
}



class MyComboBoxCellRenderer extends JLabel implements ListCellRenderer {
    public MyComboBoxCellRenderer() {
        setOpaque(true);
    }
    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        if (value == null) return this;
        setText(value.toString());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        }
        else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        setFont(new Font((String) value, 0, 12));
        return this;
    }
}







