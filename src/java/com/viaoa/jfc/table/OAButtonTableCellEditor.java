package com.viaoa.jfc.table;

import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.viaoa.jfc.OATextField;

/**
 * Button table editor components. 
 * @author vvia
 *
 */
public class OAButtonTableCellEditor extends OATableCellEditor {

    OATextField vtf;

    public OAButtonTableCellEditor(JButton cmd) {
        super(cmd);
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }
	
}
