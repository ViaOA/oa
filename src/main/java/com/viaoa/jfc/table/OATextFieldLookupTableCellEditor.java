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
package com.viaoa.jfc.table;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import com.viaoa.jfc.*;

/**
 * TextField lookup table editor component. 
 * @author vvia
 *
 */
public abstract class OATextFieldLookupTableCellEditor extends OATextFieldTableCellEditor {

    private JPanel pan;

    public OATextFieldLookupTableCellEditor(OATextFieldLookup tf, JButton cmd) {
        super(tf);
        pan = new JPanel(new BorderLayout(0,0)) {
            @Override
            public void requestFocus() {
                vtf.requestFocus();
            }
            @Override
            protected void processKeyEvent(KeyEvent e) {
                doProcessKeyEvent(e);
            }
            
            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,int condition, boolean pressed) {
                return doProcessKeyBinding(ks, e, condition, pressed);
            }
        };
        pan.setBorder(null);
        pan.add(tf);
        pan.add(cmd, BorderLayout.EAST);
    }

    @Override
    public void focusGained(FocusEvent e) {
        // give focus to Txt
        vtf.requestFocus();
    }
    
    public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column) {
        // Component comp = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        this.table = (OATable) table;
        return pan;
    }

    protected abstract boolean doProcessKeyBinding(KeyStroke ks, KeyEvent e,int condition, boolean pressed);
    protected abstract void doProcessKeyEvent(KeyEvent e);
    
}
