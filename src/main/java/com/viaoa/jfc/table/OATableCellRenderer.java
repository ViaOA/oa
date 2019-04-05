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

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;

import com.viaoa.util.*;
import com.viaoa.jfc.*;


/** 
    Internally used by OATable to wrap a column component and will call the components getTableRenderer()
    Will also call OATable listeners getTableCellRendererComponent().
*/
public class OATableCellRenderer implements TableCellRenderer {

    OATableColumn tableColumn;
    private JLabel lblRenderer;
    static Border noFocusBorder = new EmptyBorder(0, 2, 0, 2);
    private boolean bWasAligned;
    
    public OATableCellRenderer(OATableColumn column) {
        this.tableColumn = column;;
    }
                
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                          boolean isSelected, boolean hasFocus, int row, int column) {

        TableCellRenderer rend = tableColumn.renderer;
        
        Object origValue = value;
        Component comp;
        
        if (rend != null) {
            comp = rend.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
        }
        else if (tableColumn.getOATableComponent() != null) {
            String s = tableColumn.fmt;
            if ((s == null || s.length() == 0) && value != null) {
                s = tableColumn.getOATableComponent().getFormat();
                if ((s == null || s.length() == 0) && value != null) {
                	s = com.viaoa.util.OAConv.getFormat(value.getClass());
                }
            }
            s = com.viaoa.util.OAConv.toString(value,s);
        
            if (lblRenderer == null) {
                lblRenderer = new JLabel();
                lblRenderer.setOpaque(true);
            }
            lblRenderer.setBorder(noFocusBorder);

            if (!bWasAligned && value != null) {
                bWasAligned = true;
                int align;
                if (OAReflect.isNumber(value.getClass()) ) align = JLabel.RIGHT;
                else align = JLabel.LEFT;
                lblRenderer.setHorizontalAlignment( align );
            }            
            lblRenderer.setText(s);
          	comp = tableColumn.getOATableComponent().getTableRenderer(lblRenderer, table, value, isSelected, hasFocus, row, column);
        }
        else  {
            rend = table.getDefaultRenderer(table.getModel().getColumnClass(column));
            comp = rend.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
            if (comp instanceof JLabel) {
                String s = tableColumn.fmt;
                if ((s == null || s.length() == 0) && value != null) {
                    if (tableColumn.getOATableComponent() != null) s = tableColumn.getOATableComponent().getFormat();
                    if ((s == null || s.length() == 0) && value != null) {
                    	s = com.viaoa.util.OAConv.getFormat(value.getClass());
                    }
                }
                value = OAConverter.toString(value, s);
                
                if (tableColumn.getTableCellEditor() instanceof OAPasswordFieldTableCellEditor) value = "******";
                ((JLabel)comp).setText((String)value);
                
                // 20120809 reset alignment
                int align;
                if (value != null && OAReflect.isNumber(value.getClass()) ) align = JLabel.RIGHT;
                else align = JLabel.LEFT;
                ((JLabel)comp).setHorizontalAlignment( align );
            }
        }

        if (comp == null) comp = lblRenderer;
/***
if (tableColumn.oaComp != null) {
    if (hasFocus) {
        if (comp instanceof AbstractButton) ((AbstractButton)comp).setBorderPainted(true);
        ((JComponent)comp).setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
    }
    else ((JComponent)comp).setBorder(noFocusBorder);

    if (hasFocus) {
        comp.setForeground( UIManager.getColor("Table.focusCellForeground") );
        comp.setBackground( UIManager.getColor("Table.focusCellBackground") );
    }
    else if (isSelected) {
        comp.setForeground( UIManager.getColor("Table.selectionForeground") );
        comp.setBackground( UIManager.getColor("Table.selectionBackground") );
    }
    else {
        comp.setForeground( UIManager.getColor(table.getForeground()) );
        comp.setBackground( UIManager.getColor(table.getBackground()) );
    }
}
else {
    if (hasFocus) {
        ((JComponent)comp).setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
    }
    else ((JComponent)comp).setBorder(noFocusBorder);

    if (isSelected || hasFocus) {
        comp.setForeground( UIManager.getColor("Table.selectionForeground") );
        comp.setBackground( UIManager.getColor("Table.selectionBackground") );
    }
    else {
        comp.setForeground( UIManager.getColor(table.getForeground()) );
        comp.setBackground( UIManager.getColor(table.getBackground()) );
    }
}
***/

        if (((JComponent)comp).getBorder() == null) ((JComponent)comp).setBorder(noFocusBorder);
        // System.out.println("("+row+","+column+") isSelected="+isSelected+" "+((JComponent)comp).getBorder());//qqqqfff

        if (table instanceof OATable) {
            OATableListener[] listeners = ((OATable) table).getListeners();
            for (int i=0; listeners != null && i < listeners.length; i++) {
                comp = listeners[i].getTableCellRendererComponent(comp, (OATable) table, origValue, isSelected, hasFocus, row, column);
            }
        }
        return comp;    
    }

}
