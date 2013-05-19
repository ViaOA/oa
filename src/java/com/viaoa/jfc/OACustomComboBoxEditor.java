/* 
This software and documentation is the confidential and proprietary 
information of ViaOA, Inc. ("Confidential Information").  
You shall not disclose such Confidential Information and shall use 
it only in accordance with the terms of the license agreement you 
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.
 
Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/ 
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import com.viaoa.object.*;

/**
    Used by OACustomComboBox for creating custom editor.
    @see OACustomComboBox#setEditor
*/
class OACustomComboBoxEditor implements ComboBoxEditor,FocusListener, java.io.Serializable {
    protected OATextField editor;
    protected OACustomComboBox cbo;

    public OACustomComboBoxEditor(OACustomComboBox cbo, OATextField editor) {
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
        OAObject obj = null;
        if (cbo != null && editor.getHub() != cbo.getHub()) {
            obj = (OAObject) cbo.getHub().getActiveObject();
        }
        else {
            if (editor.getController().getActualHub() == null) return null;
            obj = (OAObject) editor.getController().getActualHub().getActiveObject();
        }
        if (obj == null) return null;
        return obj.getProperty(cbo.getController().getPropertyPath());
    }

    public void selectAll() {
        editor.selectAll();
        editor.requestFocus();
    }

    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {
        editor.getController().saveText();
        //was:  editor.postActionEvent();  this causes popup to be hidden
    }

    public void addActionListener(ActionListener l) {
        editor.addActionListener(l);
    }
    public void removeActionListener(ActionListener l) {
        editor.removeActionListener(l);
    }
}
