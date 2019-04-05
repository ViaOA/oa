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

import java.awt.event.KeyEvent;

import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import com.viaoa.jfc.*;

/**
 * TextField w/ autocomplete table editor component. 
 * @author vvia
 *
 */
public class OATextFieldAutoCompleteListTableCellEditor extends OATableCellEditor {

    OATextFieldAutoCompleteList vtf;

    public OATextFieldAutoCompleteListTableCellEditor(OATextFieldAutoCompleteList tf) {
        super(tf, (OATableCellEditor.LEFT | OATableCellEditor.RIGHT) );
        this.vtf = tf;
        vtf.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
    }

    public Object getCellEditorValue() {
        return vtf.getText();
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
        editArrowKeys |= (OATableCellEditor.UP | OATableCellEditor.DOWN);
        super.keyReleased(e);
    }
	
}









