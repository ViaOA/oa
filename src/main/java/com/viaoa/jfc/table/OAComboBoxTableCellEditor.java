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
        
        // 20181024 removed        
        //was: cb.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
        
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
    
    public boolean getIgnorePopup()  {
        if (lastMouseEvent == null) return false;
        lastMouseEvent = null;
        return true;
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