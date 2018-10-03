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
import java.awt.event.*;
import java.util.logging.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.OAPlainDocument;
import com.viaoa.jfc.OATextArea;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;


/**
 * Controller for binding OA to JTextArea.
 * @author vvia
 *
 */
public class TextAreaController extends OAJfcController implements FocusListener, MouseListener {
    private static Logger LOG = Logger.getLogger(TextAreaController.class.getName());    
    
    private JTextArea textArea;
    private String prevText;
    private boolean bSettingText;
    private Object activeObject;
    private Object focusActiveObject;
    private int dataSourceMax=-2;
    private int max=-1;
    private OAPlainDocument document;
    
    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextAreaController(Hub hub, JTextArea ta, String propertyPath) {
        super(hub, propertyPath, ta, HubChangeListener.Type.AoNotNull); // this will add hub listener
        create(ta);
    }

    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextAreaController(Object object, JTextArea ta, String propertyPath) {
        super(object, propertyPath, ta, HubChangeListener.Type.AoNotNull); // this will add hub listener
        create(ta);
    }


    
    protected void create(JTextArea tf) {
        if (textArea != null) {
            textArea.removeFocusListener(this);
            //textArea.removeKeyListener(this);
            textArea.removeMouseListener(this);
        }
        textArea = tf;
        textArea.setBorder(new EmptyBorder(2,2,2,2));

        if (textArea != null) {
            textArea.addFocusListener(this);
            //textArea.addKeyListener(this);
            textArea.addMouseListener(this);
        }
        
        document = new OAPlainDocument() {
            public void handleError(int errorType) {
                super.handleError(errorType);
                if (!TextAreaController.this.textArea.hasFocus()) return;
                String msg = "";
                switch (errorType) {
                case OAPlainDocument.ERROR_MAX_LENGTH:
                    int max = getMaximumColumns();
                    if (max <= 0) max = getPropertyInfoMaxColumns();
                    msg = "Maximum input exceeded, currently set to " + max;
                    if (textArea instanceof OATextArea) {
                        msg += " for " + TextAreaController.this.getEndPropertyName();
                        Hub h = ((OATextArea)textArea).getHub();
                        if (h != null) msg += ", in "+OAString.getDisplayName(h.getObjectClass().getSimpleName());
                    }
                    
                    break;
                case OAPlainDocument.ERROR_INVALID_CHAR:
                    return;
                }
                if (!OAString.isEmpty(msg)) LOG.warning(msg);
                if (textArea != null && textArea.hasFocus()) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(TextAreaController.this.textArea), msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            @Override
            public void insertString(int offset, String str, AttributeSet attr)
                    throws BadLocationException {
                super.insertString(offset, str, attr);
            }
            @Override
            protected void insertUpdate(DefaultDocumentEvent chng,
                    AttributeSet attr) {
                super.insertUpdate(chng, attr);
            }
        };
        
        if (max > 0) document.setMaxLength(max);
        textArea.setDocument(document);
    }

    public void close() {
        if (textArea != null) {
            textArea.removeFocusListener(this);
            //textArea.removeKeyListener(this);
            textArea.removeMouseListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }

    public @Override void afterPropertyChange() {
        update();
    }

    public @Override void afterChangeActiveObject() {
        boolean b = (focusActiveObject != null && focusActiveObject == activeObject);
        if (b) onFocusLost();
        
        if (hub != null) activeObject = hub.getActiveObject();
        else activeObject = null;
        
        update();
        
        if (b) onFocusGained();
        super.afterChangeActiveObject();
    }

    @Override
    public void focusGained(FocusEvent e) {
        onFocusGained();
    }    
    protected void onFocusGained() {
        focusActiveObject = activeObject;
    	prevText = textArea.getText();
    }
    
    @Override
    public void focusLost(FocusEvent e) {
        onFocusLost();
    }
    /**
        Saves changes to property in active object of Hub.
    */
    public void onFocusLost() {
        if (focusActiveObject != null && focusActiveObject == activeObject) {
            saveText();
        }
        update();
        focusActiveObject = null;
        //was: update(); 
    }

    @Override
    public void setMaximumColumns(int x) {
        super.setMaximumColumns(x);
        if (document != null) {
            document.setMaxLength(x);
        }
    }

    private boolean bSaving; // only used by saveChanges(), calling setText generates actionPerformed()
    public void saveText() {
        if (bSettingText) return;
        if (bSaving) return;
        try {
            bSaving = true;
            _saveText();
        }
        finally {
            bSaving = false;
        }
    }

    private void _saveText() {
        if (activeObject == null) return;
        String text = textArea.getText();
        if (text.equals(prevText)) return;
        
        try {
            Object convertedValue = getConvertedValue(text, null); 
            
            if (convertedValue == null && text.length() > 0) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textArea), 
                        "Invalid Entry \""+text+"\"", 
                        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String msg = isValid(activeObject, convertedValue);
            
            if (msg != null) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textArea), 
                        "Invalid Entry \""+text+"\"\n"+msg,
                        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!confirm(activeObject, convertedValue)) return;
            
            prevText = text;
            Object prevValue = getValue(activeObject);
            
            String prop = endPropertyName;
            if (prop == null || prop.length() == 0) {  // use object.  (ex: String.class)
                Object oldObj = activeObject;
                Hub h = hub;
                Object newObj = OAReflect.convertParameterFromString(h.getObjectClass(), text);
                if (newObj != null) {
                    int posx = h.getPos(oldObj);
                    h.remove(posx);
                    h.insert(newObj, posx);
                }
            }
            else {
                setValue(activeObject, convertedValue);
                // OAReflect.setPropertyValue(activeObject, getSetMethod(), convertedValue);
                if (text == null || text.length() == 0) {
                    if (OAReflect.isNumber(endPropertyClass) && activeObject instanceof OAObject) {
                    	OAObjectReflectDelegate.setProperty((OAObject)activeObject, endPropertyName, null, null);  // was: setNull(prop)
                    }
                }
            }
            if (getEnableUndo()) {
                OAUndoManager.add( OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, endPropertyName, prevValue, getValue(activeObject)) );
            }
        }
        catch (Exception e) {
        	JOptionPane.showMessageDialog(SwingUtilities.getRoot(textArea), 
        	        "Invalid Entry \""+e.getMessage()+"\"", 
        	        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    public void mouseClicked(MouseEvent e) {
    	//bMousePressed = true;
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mousePressed(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return label;
    }

    @Override
    public void update() {
        try {
            _update();
        }
        catch (Exception e) {
        }
        super.update();
    }    
    public void _update() {
        if (textArea == null) return;
        if (focusActiveObject == null) {
            if (hub != null) {
                String text = null;
                if (activeObject != null) {
                    Object value = getValue(activeObject);
                    if (value == null) text = "";
                    else text = OAConv.toString(value, null);  // dont format
                }
                if (text == null) {
                    text = getNullDescription();
                    if (text == null) text = " ";
                }
                boolean bHold = bSettingText;
                bSettingText = true;
                textArea.setText(text);
                prevText = text; // 20110112 to fix bug found while testing undo
                textArea.setCaretPosition(0);
                bSettingText = bHold;
            }   
        }        
        super.update();
    }
}
