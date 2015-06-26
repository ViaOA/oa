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

import javax.swing.*;
import javax.swing.text.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.OAJFCComponent;

/**
 * Used to bind a components enabled/disabled value to a one or more Hub/Property value
 * @author vincevia
 */
public class EnabledController extends HubPropController {
    protected JComponent component;
    
    public EnabledController(JComponent comp) {
        super();
        this.component = comp;
        update();
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
    protected void onUpdate(boolean bValid) {
        if (this.component != null) {
            onUpdate(this.component, bValid);
        }
    }

    // note: recursive
    private void onUpdate(Component comp, boolean bEnabled) {
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
        else {
            if (comp instanceof Container && !(comp instanceof OAJFCComponent)) {
                Component[] comps = ((Container)comp).getComponents();
                for (int i=0; comps != null && i<comps.length; i++) {
                    Component compx = comps[i];
                    if (compx instanceof JComponent) {
                        onUpdate(compx, bEnabled);
                    }
                }
            }
        }        
        if (comp instanceof JTabbedPane) {
        }
        else if (comp instanceof JLabel) {
        }
        else if (comp.isEnabled() != bEnabled) {
            comp.setEnabled(bEnabled);
        }
    }
}
