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
package com.viaoa.jfc.control;

import java.awt.*;
import javax.swing.*;
import com.viaoa.hub.*;

/**
 * Controller OAAutoComplete.
 * @author vvia
 *
 */
public class AutoCompleteTextFieldController extends OAJfcController {
    private final JTextField txt;
    
    public AutoCompleteTextFieldController(Hub hub, JTextField txt, String propertyPath) {
        super(hub, propertyPath, txt, HubChangeListener.Type.HubValid);
        this.txt = txt;
    }

    public AutoCompleteTextFieldController(Hub hub, JTextField txt) { 
        super(hub, txt, HubChangeListener.Type.HubValid);
        this.txt = txt;
    }

    protected void init(JLabel lab) {
    }

    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return comp;
    }
    

    @Override
    public void update() {
        try {
            _update();
        }
        finally {
        }
        super.update();
    }
    
    protected void _update() {
        if (txt == null) return;

        Object obj = hub.getAO();
        String text = null;
        if (obj != null || bIsHubCalc) {
            text = getValueAsString(obj, getFormat());
        }
        if (text == null) {
            text = getNullDescription();
            if (text == null) text = "";
        }
        txt.setText(text);
    }
    
}
