/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
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
    }    
    public EnabledController(JComponent comp, Hub hub) {
        super(hub);
        this.component = comp;
    }
    public EnabledController(JComponent comp, Hub hub, String propertyName) {
        super(hub, propertyName);
        this.component = comp;
    }
    public EnabledController(JComponent comp, Hub hub, String propertyName, Object compareValue) {
        super(hub, propertyName, compareValue);
        this.component = comp;
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
