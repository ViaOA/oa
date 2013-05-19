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
