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

import javax.swing.*;

import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.jfc.*;


/**
 * Functionality for binding OAFormattedTextField to OA.
 * @author vvia
 */
public class FormattedTextFieldController extends OAJfcController implements FocusListener, KeyListener, MouseListener {
    private OAFormattedTextField textField;
    private String prevText;
    private boolean bSettingValue;
    private Object activeObject;
    private Object focusActiveObject;
    private int dataSourceMax=-2;
    private int max=-1;

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public FormattedTextFieldController(Hub hub, OAFormattedTextField tf, String propertyPath) {
        super(hub, propertyPath, tf, HubChangeListener.Type.AoNotNull); // this will add hub listener
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public FormattedTextFieldController(Object object, OAFormattedTextField tf, String propertyPath) {
        super(object, propertyPath, tf, HubChangeListener.Type.AoNotNull); // this will add hub listener
        create(tf);
    }

    
    protected void create(OAFormattedTextField tf) {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeMouseListener(this);
        }
        textField = tf;
        
        if (OAReflect.isNumber(endPropertyClass)) {
            textField.setHorizontalAlignment(JTextField.RIGHT);
        }
        else {
            textField.setHorizontalAlignment(JTextField.LEFT);
        }

        if (textField != null) {
            textField.addFocusListener(this);
            textField.addKeyListener(this);
            textField.addMouseListener(this);
        }
        this.afterChangeActiveObject();
    }

    public void close() {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeMouseListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }


    public @Override void afterPropertyChange() {
        update();
    }

    public @Override void afterChangeActiveObject() {
        boolean b = (focusActiveObject != null && focusActiveObject == activeObject);
        if (b) {
            try {
                textField.commitEdit();  // this will setValue, which will then call saveText()
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Exception: "+ex);
            }
            focusActiveObject = null;
        }
        
        Hub h = getHub();
        if (h != null) activeObject = getHub().getActiveObject();
        else activeObject = null;
        
        update(); 
        
        if (b) onFocusGained();
    }

    @Override
    public void focusGained(FocusEvent e) {
        onFocusGained();
    }    
    protected void onFocusGained() {
        focusActiveObject = activeObject;
    	prevText = textField.getValue().toString();
    }
    
    @Override
    public void focusLost(FocusEvent e) {
        // do nothing, BaseFormattedTextField will handle, and saveText(String) will be called
        focusActiveObject = null;
    }

    
    private boolean bSaving; // only used by saveChanges(), calling setText generates actionPerformed()
    public void saveText() {
        saveText(textField.getValue().toString());
    }
    public void saveText(String text) {
        if (bSettingValue) return;
        if (bSaving) return;
        try {
            bSaving = true;
            _saveText(text);
        }
        finally {
            bSaving = false;
        }
    }

    private void _saveText(String text) {
        if (activeObject == null) return;
        if (text.equals(prevText)) return;
        
        try {
            Object convertedValue = getConvertedValue(text, null); // dont include format - it is for display only
            // Object convertedValue = OAReflect.convertParameterFromString(getSetMethod(), text, null); // dont include format - it is for display only
            
            if (convertedValue == null && text.length() > 0) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                        "Invalid Entry \""+text+"\"", 
                        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String msg = isValid(activeObject, convertedValue);
            if (msg != null) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                    "Invalid Entry \""+text+"\"\n"+msg,
                    "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!confirmPropertyChange(activeObject, convertedValue)) return;

            prevText = text;
            Object prevValue = getValue(activeObject);
            
            String prop = endPropertyName;
            if (prop == null || prop.length() == 0) {  // use object.  (ex: String.class)
                Object oldObj = activeObject;
                Hub h = getHub();
                Object newObj = getConvertedValue(text, getFormat());
                // was: Object newObj = OAReflect.convertParameterFromString(h.getObjectClass(), text);
                if (newObj != null) {
                    int posx = h.getPos(oldObj);
                    h.remove(posx);
                    h.insert(newObj, posx);
                }
            }
            else {
                setValue(activeObject, convertedValue);
            }
            if (getEnableUndo()) OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, endPropertyName, prevValue, getValue(activeObject)) );
        }
        catch (Exception e) {
        	JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
        	        "Invalid Entry \""+e.getMessage()+"\"", 
        	        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
    }


    // Key Events
    private boolean bConsumeEsc;
    @Override
    public void keyPressed(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        	bConsumeEsc = false;
            if (!textField.getText().equals(prevText)) {
        		bConsumeEsc = true;
            	e.consume();
	    		textField.setValue(prevText);
            }
        	textField.selectAll();
    	}
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE && bConsumeEsc) {
            e.consume();
    	}
    }
    @Override
    public void keyTyped(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE && bConsumeEsc) {
            e.consume();
            return;
    	}
    }

    
    private boolean bMousePressed;
    @Override
    public void mouseClicked(MouseEvent e) {
    	//bMousePressed = true;
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    	bMousePressed = false;
    }
    @Override
    public void mousePressed(MouseEvent e) {
    	bMousePressed = true;
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    	bMousePressed = false;
    }
    
    @Override
    public void update() {
        if (textField == null) return;
        if (focusActiveObject == null) {
            String text = null;
            if (activeObject != null) {
                Object value = getPropertyValue(activeObject);
                if (value == null) text = "";
                else text = OAConv.toString(value, null);  // dont use formatting, since mask is being used
            }
            if (text == null) {
                text = getNullDescription();
                if (text == null) text = " ";
            }
            boolean bHold = bSettingValue;
            bSettingValue = true;
            textField.setValue(text);
            bSettingValue = bHold;
        }        
        super.update();
    }
    
    protected Object getPropertyValue(Object obj) {
        if (obj == null) return null;
        if (endPropertyName == null) return null;
        Object value = getValue(obj);
        if (value instanceof OANullObject) value = null;
        return value;
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return label;
    }
}

