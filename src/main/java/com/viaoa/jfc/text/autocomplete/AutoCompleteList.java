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
package com.viaoa.jfc.text.autocomplete;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;

import com.viaoa.jfc.control.ListController;

/**
 * Autocomplete that uses a JList for the popup component. This is used by
 * OATextFieldAutoCompletList.java
 * 
 * Note: abstract methods are used to supply data.
 * 
 * 
 * @author vincevia
 * @see AutoCompleteList#onValueSelected(int, String) to get selected value.
 */
public abstract class AutoCompleteList extends AutoCompleteBase {
    private JList list;
    private ListCellRenderer origListCellRenderer;

    public AutoCompleteList(JTextField txt, final JList list, boolean bExactMatchOnly) {
        super(txt, list, bExactMatchOnly);
        this.list = list;

        list.setFocusable(false);
        list.setRequestFocusEnabled(false);
        Border border = list.getBorder();
        if (border == null) border = BorderFactory.createEmptyBorder(1, 5, 1, 16);
        else border = new CompoundBorder(BorderFactory.createEmptyBorder(1, 5, 1, 16), border); // extra
                                                                                                // space
                                                                                                // is
                                                                                                // needed
        list.setBorder(border);

        origListCellRenderer = list.getCellRenderer();
        list.setCellRenderer(new MyListCellRenderer());

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AutoCompleteList completer = AutoCompleteList.this;
                completer.popup.setVisible(false);

                int row = list.locationToIndex(e.getPoint());
                String s1 = (String) list.getModel().getElementAt(row);
                String s2 = completer.getTextForSelectedValue(row, s1);
                completer.onValueSelected(row, s2);
            }
        });

        txt.addActionListener(new ActionListener() {
            // note: this is never called, since the keyEvent <enter> is used
            @Override
            public void actionPerformed(ActionEvent e) {
                // only send if one is in list, otherwise not sure which value was selected.
                String s = textComp.getText();
                String[] ss = getSearchData(s, s.length());
                if (ss != null && ss.length == 1) {
                    onValueSelected(0, ss[0]);
                }
            }
        });
    }

    @Override
    protected Dimension updateSelectionList(String text, int offset) {
        String[] ss = getSearchData(textComp.getText(), offset);
        if (ss == null) return null;
        int size = ss.length;
        if (size == 0) return null;
        if (bExactMatchOnly && ss.length == 1) {
            if (!getShowOne()) return null;
        }

        list.setListData(ss);
        list.setVisibleRowCount(size < 15 ? size : 15);

        Dimension d = list.getPreferredScrollableViewportSize();
        return d;
    }

    @Override
    protected void onDownArrow() {
        int si = list.getSelectedIndex();
        if (si < list.getModel().getSize() - 1) {
            list.setSelectedIndex(si + 1);
            list.ensureIndexIsVisible(si + 1);
        }
    }

    @Override
    protected void onUpArrow() {
        int si = list.getSelectedIndex();
        if (si > 0) {
            list.setSelectedIndex(--si);
            list.ensureIndexIsVisible(si);
        }
    }

    // called when the popup isVisible and [enter]
    @Override
    protected void onSelection() {
        // called when popup is visible and [Enter]
        Object obj = list.getSelectedValue();
        if (obj == null) return; // nothing selected, only [enter]

        int pos = list.getSelectedIndex();
        String s = getTextForSelectedValue(pos, (String) obj);
        if (s == null) s = "";

        onValueSelected(pos, (String) obj);
        textComp.setText(s);
    }

    @Override
    protected void onPageDown() {
        int si = list.getSelectedIndex();
        int rc = list.getVisibleRowCount();
        si += rc;
        int max = list.getModel().getSize();
        if (si >= max) si = max - 1;
        if (si < 0) si = 0;
        list.setSelectedIndex(si);
        list.ensureIndexIsVisible(si);
    }

    @Override
    protected void onPageUp() {
        int si = list.getSelectedIndex();
        int rc = list.getVisibleRowCount();
        si -= rc;
        if (si < 0) si = 0;
        list.setSelectedIndex(si);
        list.ensureIndexIsVisible(si);
    }

    /**
     * Data used to populate listBox
     * 
     * @param text
     *            value of textField
     * @param offset
     *            caret position of textfield
     * @return
     */
    protected abstract String[] getSearchData(String text, int offset);

    /**
     * This can be used to auto-fill the remainder of value. Example: if user enters 'abc' and there is
     * a match for 'abcdef', the 'def' can also be filled in. Returning value should be the least that
     * is done.
     */
    protected abstract String getClosestMatch(String value); // from super class

    /*
     * This can be overwritten to replace the call to textfield.setText(value) when the item is selected
     * in the jlist
     */
    protected String getTextForSelectedValue(int pos, String value) {
        return value;
    }

    /**
     * Main method to override. Called when an item from Jlist is [clicked], [enter] on, or textField
     * [enter] and there is only one item in Jlist.
     * 
     * @param pos
     *            in searchData
     * @param value
     *            value returned from searchData
     */
    protected void onValueSelected(int pos, final String value) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textComp.setText(value);
            }
        });
    }

    protected String getToolTipText(int pos) {
        return null;
    }

    class MyListCellRenderer extends JLabel implements ListCellRenderer {
        public MyListCellRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s;
            try {
                s = AutoCompleteList.this.getToolTipText(index);
            }
            catch (Exception e) {
                s = "";
            }
            list.setToolTipText(s);
            return origListCellRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }
}
