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

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import com.viaoa.jfc.*;

import com.viaoa.hub.*;

/**  
    Implemented by OATableComponents
    abstract, must include your own:
        public abstract Object getCellEditorValue();
*/
public abstract class OATableCellEditor implements TableCellEditor, FocusListener, KeyListener {
    protected Vector listeners = new Vector();
    protected JComponent vcomp;
    protected OATable table;
    
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;
    protected int editArrowKeys; // arrows keys that can be used while in edit mode
    protected int disabledArrowKeys; // arrows keys that are used by component and need to be ignored
    
    public OATableCellEditor(JComponent c) {
        this(c,0,0);
    }

    /** 
        @see #setEditArrowkeys        
        @see #setDisabledArrowKeys
    */
    public OATableCellEditor(JComponent c, int editArrowKeys) {
        this(c,editArrowKeys,0);
    }

    /** 
        @see #setEditArrowkeys        
        @see #setDisabledArrowKeys
    */
    public OATableCellEditor(JComponent c, int editArrowKeys, int disabledArrowKeys) {
        this.vcomp = c;
        this.editArrowKeys = editArrowKeys;
        this.disabledArrowKeys = disabledArrowKeys;
        vcomp.addFocusListener(this);
        vcomp.addKeyListener(this);
    }

    
    /** 
        Defines the arrow keys that will be used by the editor for editing.  Others
        that are not defined will allow for navigating from cell to cell. <br>
        Need to OR "|" : UP, DOWN, LEFT, RIGHT.
    */
    public void setEditArrowKeys(int x) {
        editArrowKeys = x;
    }

    /** 
        Defines the arrow keys that are to be ignored. <br>
        Need to OR, UP, DOWN, LEFT, RIGHT.
        @deprecated Use setEditArrowKeys to define keys that are permitted
    */
    public void setDisabledArrowKeys(int x) {
        disabledArrowKeys = x;
    }

    // hack: for JTable to use arrow keys
    // note: when JTable has focus and the user hits a key, JTable will get the keyPress
    //       and then send focus here and this will get keyReleased key only
    //       By knowing that, I can find out if focus was obtained using keystroke instead of mouse

    private boolean editMode;
    
    /** 
        Capture key released to know if key should be used by this component or sent to table.
    */
    public void keyReleased(KeyEvent e) {
        int col = 0;
        int row = 0;

        int kc = e.getKeyCode();
	    
	    switch (kc) {
            case KeyEvent.VK_ESCAPE:
            case KeyEvent.VK_ENTER:
                if (!editMode) return;
                break;
	        case KeyEvent.VK_LEFT:
                if (editMode && (editArrowKeys & LEFT) != 0) return;
	            if ((disabledArrowKeys & LEFT) != 0) return;
	            col--;
	            break;
            case KeyEvent.VK_RIGHT:
                if (editMode && (editArrowKeys & RIGHT) != 0) return;
	            if ((disabledArrowKeys & RIGHT) != 0) return;
                col++;
                break;
	        case KeyEvent.VK_UP:
                if (editMode && (editArrowKeys & UP) != 0) return;
	            if ((disabledArrowKeys & UP) != 0) return;
	            row--;
	            break;
	        case KeyEvent.VK_DOWN:
                if (editMode && (editArrowKeys & DOWN) != 0) return;
	            if ((disabledArrowKeys & DOWN) != 0) return;
	            row++;
                break;
            default: 
                return;
        }

        col += table.getEditingColumn();
        row += table.getEditingRow();
        fireEditingStopped();

        
        // 20101229
        OATable tableLeft = table.getLeftTable();
        OATable tableRight = table.getRightTable();
        
        OATable useTable = table;
        
        if ( row == table.getRowCount() ) row--;
        if ( row < 0 ) row = 0;

        if ( col == table.getColumnCount() ) {
            if (table == tableLeft && tableRight != null) {
                useTable = tableRight;
                col = 0;
            }
            else {
                col = table.getColumnCount() - 1;
            }
        }
        else if ( col < 0 ) {
            col = 0;
            if (table == tableRight) {
                col = tableLeft.getColumnCount() - 1;
                useTable = tableLeft;
            }
        }

        useTable.setColumnSelectionInterval(col,col);
        useTable.setRowSelectionInterval(row,row);
        useTable.requestFocus();

        // hack: make sure cell is visible
	    Rectangle cellRect = useTable.getCellRect(row,col, true);
        if (cellRect != null) useTable.scrollRectToVisible(cellRect);
    }



    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}


    public synchronized void addCellEditorListener(CellEditorListener l) {
        if (!listeners.contains(l)) listeners.addElement(l);
    }
    public synchronized void removeCellEditorListener(CellEditorListener l) {
        listeners.removeElement(l);
    }

    public void fireEditingStopped() {
        CellEditorListener[] l;
        synchronized(listeners) {
            l = new CellEditorListener[listeners.size()];
            listeners.copyInto(l);
        }
        
        if (table == null) throw new RuntimeException("OATableCellEditor component error: "+this.vcomp);
        table.setCheckFocus(false); // hack: see OATable setCheckFocus(), this will set focus to OATable
        ChangeEvent evt = new ChangeEvent(vcomp);
        for (int i=0; i < l.length; i++) {
            l[i].editingStopped(evt);
            // JTable gets these messages and calls container.remove() to get rid of editor component
        }
        table.setCheckFocus(true);
    }

    public void focusGained(FocusEvent e) { }

    public void focusLost(FocusEvent e) {
        Container c = vcomp.getParent();
        while (c != null) {
            if (c instanceof Window) {
                if ( ((Window)c).getFocusOwner() == null ) return;
                    // focus was lost to another window, dont respond
                    // the "other" window could be the popdown list from comboBox
            }
            c = c.getParent();
        }
        fireEditingStopped();
    }



    public void cancelCellEditing() {
	    fireEditingStopped();
    }
    
    public abstract Object getCellEditorValue();

    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean isCellEditable(EventObject anEvent) {
        return true;
    }


    public boolean stopCellEditing() { // fired by JTable when another cell is mouse selected
        editMode = false;
        fireEditingStopped();
		return true;
    }

    public Component getTableCellEditorComponent(JTable table,Object value,boolean isSelected,int row,int column) {
        this.table = (OATable) table;
        return vcomp;
    }

    public void startCellEditing(java.util.EventObject e) {
        editMode = true;
    }


}
