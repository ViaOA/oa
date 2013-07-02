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

import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
    Popup List that is bound to a property in a Hub.
    A button can be setup that will automatically popup the OAList.
    <p>
    Example:<br>
    OAPopupList lst = new OAPopupList(hubEmployee, "fullName", 30);
    JButton cmd = new JButton("Set Date");
    dc.setButton(cmd);
    -- or --
    OACommand cmd = new OACommand(hubEmployee);
    dc.setButton(cmd);
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OAPopup
    @see OAList
*/
public class OAPopupList extends OAList {
    protected OAPopup popup;

    /**
        Create a new Popup List that is bound to a Hub.
        @param visibleRowCount number of rows to visually display.
        @param cols is width of list using character width size.
    */
    public OAPopupList(Hub hub, String propertyPath, int visibleRowCount, int columns) {
        super(hub, propertyPath, visibleRowCount, columns);
        popup = new OAPopup(new JScrollPane(this));
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

    /** called by Hub2List when item is selected */
    public void valueChanged() {
        super.valueChanged();
        popup.setVisible(false);
    }
}


