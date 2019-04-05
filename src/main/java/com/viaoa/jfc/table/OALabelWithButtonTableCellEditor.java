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

