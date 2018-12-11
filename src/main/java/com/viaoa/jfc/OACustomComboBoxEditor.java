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
        // 20181211 removed, so that combo will have a border around it.  Ex: DateCombo          
        // editor.setBorder(null);
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
            if (editor.getController().getHub() == null) return null;
            obj = (OAObject) editor.getController().getHub().getActiveObject();
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
