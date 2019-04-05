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
package com.viaoa.jfc.tree;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.*;

public class OATreeCellEditor implements TreeCellEditor {
    Vector listeners = new Vector(3,3);
    OATree tree;

    public OATreeCellEditor(OATree oatree) {
        this.tree = oatree;
    }

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
        
        ChangeEvent evt = new ChangeEvent(tree);
        for (int i=0; i < l.length; i++) {
            l[i].editingStopped(evt);
        }
    }


    public void cancelCellEditing() {
	    fireEditingStopped();
    }
    
    public Object getCellEditorValue() {
        return ""; //qqqqqqqqqqqqqqq
    }

    public boolean isCellEditable(EventObject anEvent) {
	    if (anEvent instanceof MouseEvent) {
	        MouseEvent me = (MouseEvent) anEvent;
	        if (me.getClickCount() > 1) return false;
	        // never true: if (me.getID() != MouseEvent.MOUSE_RELEASED) return false;
	        
	        TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
	        if (tp == null) return false;
            
            OATreeNodeData tnd = (OATreeNodeData) tp.getLastPathComponent();
            
            if (tnd.node.getTableEditor() != null) {
                if (tree.getLastSelection().length > 0) {
                    if (tnd == tree.getLastSelection()[tree.getLastSelection().length - 1] ) return true;
                }
            }
	    }
	    else if (anEvent instanceof KeyEvent) {
	    }
	    
	    return false;
	}
    public boolean shouldSelectCell(EventObject anEvent) {
        return true;
    }

    public boolean stopCellEditing() {
        fireEditingStopped();
		return true;
    }

    public Component getTreeCellEditorComponent(JTree tree,Object value,boolean isSelected,boolean expanded,boolean leaf,int row) {
        OATreeNode tn = ((OATreeNodeData)value).node;
        return tn.getEditorComponent();
    }

}
