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
package com.viaoa.jfc.text.autocomplete;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.TextUI;
import javax.swing.text.*;

import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.OATable;


/**
 * Adds autocomplete to a TextComponent, for lookups to any type of data.
 * @author vvia
 */
public abstract class AutoComplete {

    private JTextComponent txt;
    protected JPopupMenu popup; 
    protected JScrollPane scroll;
    private JList list;
    private String currentMatchString;
    private int iMatchesFound;  // used to know which JList items are for "Soundex" matches

    public AutoComplete(JTextComponent txt) {
        this.txt = txt;
        if (txt != null) {
            setupAutoComplete();
        }
    }
    
    protected void setupAutoComplete() {
        String cmd = "autoComplete";
        txt.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK, false), cmd);
        txt.getActionMap().put(cmd, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                AutoComplete.this.showAutoComplete();
            }
        });
        
        
        txt.addKeyListener(new KeyAdapter() {
            boolean bIgnore = false;
            @Override
            public void keyPressed(KeyEvent e) {
                bIgnore = false;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        if (popup.isVisible()) {
                            int si = list.getSelectedIndex(); 
                            if (si < list.getModel().getSize() - 1) { 
                                list.setSelectedIndex(si + 1); 
                                list.ensureIndexIsVisible(si + 1); 
                            } 
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_UP:
                        if (popup.isVisible()) {
                            int si = list.getSelectedIndex(); 
                            if (si > 0) { 
                                list.setSelectedIndex(--si); 
                                list.ensureIndexIsVisible(si); 
                            } 
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_PAGE_DOWN:
                        if (popup.isVisible()) {
                            int si = list.getSelectedIndex();
                            int rc = list.getVisibleRowCount();
                            si += rc;
                            int max = list.getModel().getSize();
                            if (si >= max) si = max-1;
                            if (si < 0) si = 0;
                            list.setSelectedIndex(si); 
                            list.ensureIndexIsVisible(si); 
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_PAGE_UP:
                        if (popup.isVisible()) {
                            int si = list.getSelectedIndex();
                            int rc = list.getVisibleRowCount();
                            si -= rc;
                            if (si < 0) si = 0;
                            list.setSelectedIndex(si); 
                            list.ensureIndexIsVisible(si); 
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        if (popup.isVisible()) {
                            popup.setVisible(false);
                            bIgnore = true;
                        }
                        break;
                    case KeyEvent.VK_ENTER:
                        if (popup.isVisible()) {
                            setTextWithSelectionFromList();
                            popup.setVisible(false); 
                            bIgnore = true;
                        }
                        break;
                }
                if (!bIgnore) {
                    super.keyPressed(e);
                }
                else e.consume(); 
            }  
        });

        txt.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                if (popup != null && popup.isVisible()) {
                    showAutoComplete();
                }
            }
        });
        
        
        
        list = new JList();
        final ListCellRenderer origListCellRenderer = list.getCellRenderer();
        
        // need to show "Soundex" matches in italics
        list.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component comp = origListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (comp != null) {
                    Font font;
                    Color color;
                    Border border;
                    if (index < iMatchesFound) {
                        font = comp.getFont().deriveFont(Font.PLAIN);
                        color = Color.black;
                        border = BorderFactory.createEmptyBorder(0, 0, 0, 0);
                        
                        if (currentMatchString != null && currentMatchString.length() > 0 && comp instanceof JLabel) {
                            int x = currentMatchString.length();
                            String s = (String) value;
                            s = "<html><b>" + s.substring(0,x) + "</b>" + s.substring(x);
                            ((JLabel)comp).setText(s);
                        }
                    }
                    else {
                        font = comp.getFont().deriveFont(Font.ITALIC);
                        color = Color.darkGray;
                        border = BorderFactory.createEmptyBorder(0, 4, 0, 0);
                    }
                    comp.setFont(font);
                    if (!isSelected && !cellHasFocus) {
                        comp.setForeground(color);
                    }
                    if (comp instanceof JComponent) {
                        ((JComponent)comp).setBorder(border);
                    }
                }
                return comp;
            }
        });
        
        
        list.setFocusable(false); 
        list.setRequestFocusEnabled(false);
        Border border = list.getBorder();

        if (border == null) border = BorderFactory.createEmptyBorder(1,5,1,16);
        else border = new CompoundBorder(BorderFactory.createEmptyBorder(1,5,1,16), border);  // extra space is needed
        list.setBorder(border);
        
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AutoComplete completer = AutoComplete.this;
                setTextWithSelectionFromList();
                completer.popup.setVisible(false);
            }
        });

        
        scroll = new JScrollPane(list); 
        scroll.setBorder(null); 
        scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false ); 
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 
        popup = new JPopupMenu();
        popup.setFocusable(false); 
        popup.setRequestFocusEnabled(false);
        
        border = new CompoundBorder(BorderFactory.createEmptyBorder(2,2,2,2), BorderFactory.createLineBorder(Color.black));
        
        popup.setBorder(border); 
        popup.add(scroll);
        
    }

    protected void setTextWithSelectionFromList() {
        try {                                
            Object obj = list.getSelectedValue();
            if (obj == null) return;
            if (currentMatchString == null) return;
            
            String s = (String) obj;
            
            //was: txt.replaceSelection(s);  -- dont use select/replace, since user can continue to type characters while popup search is showing
           
            int pos = txt.getCaretPosition();
            if (s.startsWith(currentMatchString)) {
                s = s.substring(currentMatchString.length());
            }
            else {
                // select a "Soundex" word
                pos -= currentMatchString.length();
                txt.getDocument().remove(pos, currentMatchString.length());
            }
            if (s.length() > 0) txt.getDocument().insertString(pos, s, null);
            
        }
        catch (Exception ex) {}
    }
    
    
    public void showAutoComplete() {
        if (!txt.isEditable()) return;
        //if (popup.isVisible()) popup.setVisible(false); 
        list.scrollRectToVisible(new Rectangle(0,0,1,1));
        Document doc = txt.getDocument();
        Caret caret = txt.getCaret();
        int begin = caret.getDot();
        int end = begin;
        
        try {
            // find begin of word
            begin = Utilities.getWordStart(txt, end);
            if (begin == end && end > 0) {
                begin = Utilities.getWordStart(txt, end-1);
            }
            /* was:
            for ( ;begin >= 0; begin--) {
                String s = doc.getText(begin, 1);
                char c = s.charAt(0);
                if (!Character.isLetter(c)) {
                    begin++;
                    break;
                }
            }
            */

            currentMatchString = doc.getText(begin, (end-begin)).trim();
            String[] ss;
            //if (end >= (begin+2)) {
                String[] ss1 = getMatches(currentMatchString);

                String[] ss2 = getSoundexMatches(currentMatchString);
                
                int x = (ss1 != null) ? ss1.length : 0;
                x += (ss2 != null) ? ss2.length : 0;
                ss = new String[x];
                
                iMatchesFound = 0;
                if (ss1 != null) {
                    System.arraycopy(ss1, 0, ss, 0, ss1.length);
                    iMatchesFound = ss1.length;
                }
                if (ss2 != null) {
                    System.arraycopy(ss2, 0, ss, iMatchesFound, ss2.length);
                }
            // }
            /*
            else {
                ss = new String[0];
            }
            */
            list.setListData(ss);
            list.setVisibleRowCount(ss.length<24 ? Math.max(ss.length,3) : 24);
            _showPopup();
        }
        catch (Exception e) {
            System.out.println("AutoCompleteSpellCheck exception: "+e);
            e.printStackTrace();
        }
    }
    private void _showPopup() {
        int offset = txt.getCaretPosition();

        Dimension d = list.getPreferredScrollableViewportSize();        
        if (d == null) return; // dont show
        
        d.width = Math.max(d.width, OAJfcUtil.getCharWidth(29));
        
        d.width += 7;  // include popup borders
        d.height += 10; // include popup borders
        
        popup.setPopupSize(d);
        
        Point pt = txt.getCaret().getMagicCaretPosition();

        offset -= currentMatchString.length();
        Rectangle r = getLocation(offset);

        txt.moveCaretPosition(txt.getCaretPosition());
        //was:  dont select, since user can continue to type characters
        //txt.select(offset, offset+currentMatchString.length());
        
        popup.show(txt, r.x-3, r.y+r.height); 
        txt.requestFocusInWindow(); 
    } 

    private Rectangle getLocation(int offset) {
        Rectangle r = null;
        try {
            TextUI ui = txt.getUI();
            Shape s = ui.modelToView(txt, offset, Position.Bias.Forward);
            r = (s instanceof Rectangle) ? (Rectangle) s : s.getBounds();
        } 
        catch (BadLocationException ble) {
           
        }
        return r;
    }    
    

    /**
     * This should be overwritten to supply the words that start with the string in text.
     */
    protected abstract String[] getMatches(String text);

    /**
     * This should be overwritten to supply the words that are Soundex the text.
     */
    protected abstract String[] getSoundexMatches(String text);
    
}


