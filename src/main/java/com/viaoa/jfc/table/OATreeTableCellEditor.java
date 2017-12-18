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
import java.util.EventObject;

import javax.swing.*;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;

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

        // 20171215
        if (anEvent instanceof AWTEvent) {
            tree.dispatchEvent((AWTEvent) anEvent);
        }
        /**was:        
        if (tree.isExpanded(row)) {
            tree.collapseRow(row);
        }
        else {
            tree.expandRow(row);
        }
        */        
        return false;
    }
    
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }
    
    @Override
    public void startCellEditing(EventObject e) {
        if (!(e instanceof MouseEvent)) return;
        
        //mouse
    }
    
}

