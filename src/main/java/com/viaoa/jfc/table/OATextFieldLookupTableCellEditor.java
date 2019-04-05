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
