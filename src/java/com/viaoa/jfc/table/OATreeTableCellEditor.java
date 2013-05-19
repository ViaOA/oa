/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.jfc.table;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

import javax.swing.*;
import com.viaoa.jfc.*;

/**
 * Tree table editor component. 
 * @author vvia
 *
 */
public class OATreeTableCellEditor extends OATableCellEditor {
    OATree tree;
   
    public OATreeTableCellEditor(OATree tree) {
        super(tree);
        this.tree = tree;
    }
    @Override
    public Object getCellEditorValue() {
        return null;
    }
    
    public boolean isCellEditable(EventObject anEvent) {
        if (!(anEvent instanceof MouseEvent)) return false;
        MouseEvent e = (MouseEvent) anEvent;
        if (e.getID() != MouseEvent.MOUSE_PRESSED) return false;

        Object src = anEvent.getSource();
        if (!(src instanceof OATable)) return false;
        
        OATable t = (OATable) src;
        
        int row = t.getHub().getPos();
        Rectangle rec = t.getCellRect(row, 0, true);
        
        if (tree.isExpanded(row)) {
            tree.collapseRow(row);
        }
        else {
            tree.expandRow(row);
        }
        
        return false;
    }
    
//qqqqqqqqqqqqq    
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    
    @Override
    public void startCellEditing(EventObject e) {
        if (!(e instanceof MouseEvent)) return;
        
        //mouse
    }
    
}

