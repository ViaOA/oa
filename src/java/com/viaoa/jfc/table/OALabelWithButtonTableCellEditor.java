package com.viaoa.jfc.table;

import java.awt.event.KeyEvent;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.viaoa.jfc.OALabelWithButton;
import com.viaoa.jfc.OATextField;

/**
 * Label+Button table editor component. 
 * @author vvia
 *
 */
public class OALabelWithButtonTableCellEditor extends OATableCellEditor {

    OALabelWithButton lbl;

    public OALabelWithButtonTableCellEditor(OALabelWithButton lbl) {
        super(lbl);
        this.lbl = lbl;
    }

    public Object getCellEditorValue() {
        return lbl.getText();
	}


}

