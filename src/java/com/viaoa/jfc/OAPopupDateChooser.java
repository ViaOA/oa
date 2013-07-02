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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.beans.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;


/**
    Popup calendar that is bound to a property in a Hub.
    A button can be setup that will automatically popup the OADateChooser.
    <p>
    Example:<br>
    OAPopupDateChooser dc = new OAPopupDateChooser(hubEmployee, "hireDate");
    JButton cmd = new JButton("Set Date");
    dc.setButton(cmd);
    -- or --
    OACommand cmd = new OACommand(hubEmployee);
    dc.setButton(cmd);
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OAPopup
    @see OADateChooser
*/
public class OAPopupDateChooser extends OADateChooser {
    protected OAPopup popup;

    /**
        Create a new Popup DateChooser that is bound to a property in the active object of a Hub.
    */
    public OAPopupDateChooser(Hub hub, String propertyPath) {
        super(hub, propertyPath);
        // setIcon( new ImageIcon(getClass().getResource("images/date.gif")) );

        popup = new OAPopup(this);
    }

    /** changing/selecting a date causes the popup to disappear. */
    protected void firePropertyChange(String propertyName,Object oldValue,Object newValue) {
        if (popup.isVisible()) popup.setVisible(false);
    }

    /** 
        Component used to set the popup to be visible.
    */
    public void setController(JComponent comp) {
        popup.setupListener(comp);
    }

    /**
        Flag to have the popup displayed only when the right mouse button is clicked.
    */
    public void setRightClickOnly(boolean b) {
        popup.setRightClickOnly(b);
    }

}


