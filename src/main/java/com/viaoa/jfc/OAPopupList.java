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
        super.onItemSelected(getSelectedIndex());
        popup.setVisible(false);
    }
}


