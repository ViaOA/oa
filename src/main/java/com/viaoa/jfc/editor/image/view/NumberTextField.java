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
package com.viaoa.jfc.editor.image.view;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

/**
 * TextField that only accepts digits, and is right justified. 
 * @author vincevia
 * @see NumberTextField#onChange() called when textfield document is changed.
 */
public class NumberTextField extends JTextField {

    public NumberTextField(int columns) {
        super(columns);
        setHorizontalAlignment(JTextField.RIGHT);
        
        AbstractDocument doc = (AbstractDocument) getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                String s = "";
                for (int i=0; i<string.length(); i++) {
                    if (Character.isDigit(string.charAt(i))) s += string.charAt(i); 
                }
                if (s.length() > 0) fb.insertString(offset, s, attr);
            }
            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
            }
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                String s = "";
                for (int i=0; i<text.length(); i++) {
                    if (Character.isDigit(text.charAt(i))) s += text.charAt(i); 
                }
                fb.replace(offset, length, s, attrs);
            }
        });
        doc.addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                NumberTextField.this.onChange();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                NumberTextField.this.onChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                NumberTextField.this.onChange();
            }
        });
    }
    
    // called by document listener when there is a change.
    public void onChange() {
        
    }
}
