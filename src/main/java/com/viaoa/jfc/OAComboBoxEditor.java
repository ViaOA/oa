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
