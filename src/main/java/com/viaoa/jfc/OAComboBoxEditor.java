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

import javax.swing.*;

/**
    Used by OAComboBox to set an editor component.
    @see OAComboBox#setEditor
*/
public class OAComboBoxEditor implements ComboBoxEditor,FocusListener, java.io.Serializable {
    protected OATextField editor;
    protected OAComboBox cbo;

    public OAComboBoxEditor(OAComboBox cbo, OATextField editor) {
        this.cbo = cbo;
        this.editor = editor;
        editor.setBorder(null);
        editor.addFocusListener(this);
    }

    public Component getEditorComponent() {
        return editor;
    }

    public void setItem(Object anObject) {
        // OATextField will do this
    }

    public Object getItem() {
        if (cbo != null && editor.getHub() != cbo.getHub()) {
            return cbo.getHub().getActiveObject();
        }

        if (editor.getController().getActualHub() == null) return null;
        return editor.getController().getActualHub().getActiveObject();
    }

    public void selectAll() {
        editor.selectAll();
        editor.requestFocus();
    }

    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {
        editor.postActionEvent();
    }

    public void addActionListener(ActionListener l) {
        editor.addActionListener(l);
    }
    public void removeActionListener(ActionListener l) {
        editor.removeActionListener(l);
    }
}
