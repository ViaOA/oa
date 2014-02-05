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
import java.awt.event.*;
import java.util.*;
import javax.swing.border.*;
import javax.swing.*;
import com.viaoa.jfc.*;

/**
    Used by OAComboBox when used as a column in an OATable.
    @see OAComboBox
*/
public class OAComboBoxTableCellEditor extends OATableCellEditor {
    JComboBox vcb;
    Component[] components;
    
    public OAComboBoxTableCellEditor(JComboBox cb) {
        super(cb, (OATableCellEditor.UP | OATableCellEditor.DOWN), (OATableCellEditor.UP | OATableCellEditor.DOWN) );
        // cb.setBorder(null);
        //cb.setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
        cb.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
        
        this.vcb = cb;

        // 07/11/02 setClickCount(1);
        // 07/11/02 setShouldSelectCell(false);  //ffffffffff
        // since JComboBox could be a container, listen to all components
        components = vcb.getComponents();
        for (int i=0; i<components.length; i++) myComponentAdded(components[i]);
    }

    protected void myComponentAdded(Component c) {
        if (c == null) return;
        c.addFocusListener(this);
        c.addKeyListener(this);
    }
    
    public void focusGained(FocusEvent e) {
        if (e.getSource() instanceof JTextField) {
            setEditArrowKeys( (OATableCellEditor.LEFT | OATableCellEditor.RIGHT) | (OATableCellEditor.UP | OATableCellEditor.DOWN));
            setDisabledArrowKeys(0);
        }
        else {
            setEditArrowKeys( (OATableCellEditor.UP | OATableCellEditor.DOWN) );
            setDisabledArrowKeys( (OATableCellEditor.UP | OATableCellEditor.DOWN) );
        }
        super.focusGained(e);
    }


    public Object getCellEditorValue() {
        return vcb.getSelectedItem();    
	}

    /** this is a way to keep the comboBox from popping up
    */
    public boolean shouldSelectCell(EventObject anEvent) {
	    if (anEvent instanceof MouseEvent) {
//            ((MouseEvent) anEvent).consume();
//            vcb.hidePopup();
	    }
return true; // 2005/02/07
//was:        return super.shouldSelectCell(anEvent);
    }
}

/***
System.out.println("LOST");//qqqqqqqqzzzzzzzzzvvvvvvvvv
if (e != null) return;//zzzzzzzzzzzz
        Container c = vcb.getParent();
        while (c != null) {
            if (c instanceof Window) {
                if ( ((Window)c).getFocusOwner() == null ) {
                    // focus was lost to another window, dont respond
                    // the "another" window could be the popdown list
                    skipNextFocus = true;
                    return;
                }
            }
        }


**/