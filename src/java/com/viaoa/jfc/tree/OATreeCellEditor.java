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
