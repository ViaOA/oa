package com.viaoa.jfc.table;

import java.awt.event.KeyEvent;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.viaoa.jfc.OAPasswordField;

/**
 * Password table editor component. 
 * @author vvia
 *
 */
public class OAPasswordFieldTableCellEditor extends OATableCellEditor {
    OAPasswordField vtf;
    
    public OAPasswordFieldTableCellEditor(OAPasswordField tf) {
        super(tf, (OATableCellEditor.LEFT | OATableCellEditor.RIGHT) );
        tf.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
        //tf.setBorder(null);
        this.vtf = tf;
    }
    public Object getCellEditorValue() {
        return vtf.getPassword();
	}

    public void startCellEditing(java.util.EventObject e) {
        super.startCellEditing(e);
        vtf.selectAll();
    }

    int pos1, pos2;
    public void keyPressed(KeyEvent e) {
        pos1 = vtf.getSelectionStart();
        pos2 = vtf.getSelectionEnd();
    }
    
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        editArrowKeys = (OATableCellEditor.LEFT | OATableCellEditor.RIGHT);
        if (pos1 == pos2) {
            if (key == KeyEvent.VK_LEFT) {
                if (pos1 == 0) editArrowKeys = 0;
            }
            if (key == KeyEvent.VK_RIGHT) {
                int x = vtf.getText().length();
                if (pos2 == x) editArrowKeys = 0;
            }
        }
        super.keyReleased(e);
    }


}
