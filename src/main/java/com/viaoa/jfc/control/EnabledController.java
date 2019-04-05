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

import java.awt.Component;
import java.awt.Container;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.text.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.OAJfcComponent;
import com.viaoa.jfc.OASplitButton;
import com.viaoa.jfc.OATextField;

/**
 * Used to bind a components enabled/disabled value to a one or more Hub/Property value
 * @author vincevia
 * @deprecated  use OAObjectChangeListener instead
 */
public class EnabledController extends HubPropController {
    protected JComponent component;

    public EnabledController(JComponent comp) {
        super();
        this.component = comp;
        // 20170515 can cause loop
        // update();
    }    
    public EnabledController(JComponent comp, Hub hub) {
        super(hub);
        this.component = comp;
        update();
    }
    public EnabledController(JComponent comp, Hub hub, String propertyName) {
        super(hub, propertyName);
        this.component = comp;
        update();
    }
    public EnabledController(JComponent comp, Hub hub, String propertyName, Object compareValue) {
        super(hub, propertyName, compareValue);
        this.component = comp;
        update();
    }
    
    @Override
    public void update() {
        if (SwingUtilities.isEventDispatchThread()) {
            super.update();
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                EnabledController.this.update();
            }
        });
    }
    

    @Override
    protected void onUpdate(boolean bValid) {
        if (this.component != null) {
            onUpdate(this.component, bValid);
        }
    }

    
    private void onUpdate(final Component comp, final boolean bEnabled) {
        _onUpdate(comp, bEnabled);
    }
    
    
    // note: recursive
    private void _onUpdate(Component comp, boolean bEnabled) {
        if (comp == null) return;
        if (comp instanceof JTextComponent) {
            JTextComponent txt = (JTextComponent) comp;
            if (!bEnabled) {
                boolean b = true;
                // need to see if it should call setEditable(b) instead
                for (HubProp hp : hubProps) {
                    Object obj = hp.hub.getAO();
                    if (obj == null) {
                        b = false;
                        break;
                    }
                }
                if (b) {
                    txt.setEditable(false);
                    bEnabled = true;
                }
            }
            else {
                if (!txt.isEditable()) txt.setEditable(true);
            }
        }
        if (comp.isEnabled() != bEnabled) {
            comp.setEnabled(bEnabled);
        }
        
        if (comp instanceof OAJfcComponent) {
            OAJfcController jc = ((OAJfcComponent) comp).getController();
            if (jc != null) {
                JLabel lbl = jc.getLabel();
                if (lbl != null) lbl.setEnabled(bEnabled);
            }
        }
    }
}
