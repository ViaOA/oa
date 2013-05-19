/*

2003/10/21 added support for Undo

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

package com.viaoa.jfc.control;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.jfc.*;


/**
 * Controller for binding OA to JTextArea.
 * @author vvia
 *
 */
public class TextAreaController extends JFCController implements FocusListener, KeyListener, MouseListener {
    private JTextArea textField;
    private String prevText;
    private boolean bSettingText;
    private Object activeObject;
    private Object focusActiveObject;
    private int dataSourceMax=-2;
    private int max=-1;
    private OAPlainDocument document;
    
    /**
        Create an unbound TextField.
    */
    public TextAreaController(JTextArea tf) {
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextAreaController(Hub hub, JTextArea tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextAreaController(Object object, JTextArea tf, String propertyPath) {
        super(object, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    
    
    
    protected void create(JTextArea tf) {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeMouseListener(this);
        }
        textField = tf;
        textField.setBorder(new EmptyBorder(2,2,2,2));

        if (actualHub == null) return; 

        if (textField != null) {
            textField.addFocusListener(this);
            textField.addKeyListener(this);
            textField.addMouseListener(this);
        }
        // set initial value of textField
        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
            getEnabledController().add(getActualHub());
        }
    }



    public int getDataSourceMax() {
        if (dataSourceMax == -2) {
            if (hub != null) {
            	dataSourceMax = -1;
                OADataSource ds = OADataSource.getDataSource(actualHub.getObjectClass());
                if (ds != null) {
                    dataSourceMax = ds.getMaxLength(actualHub.getObjectClass(), getHubListenerPropertyName());
                    Method method = getLastMethod();
                    if (method != null) {
                        if (method.getReturnType().equals(String.class)) {
                            if (dataSourceMax > 254) dataSourceMax = -1;
                        }
                        else dataSourceMax = -1;
                    }
                }
            }
        }
        return dataSourceMax;
    }
    public int getMax() {
        if (max > getDataSourceMax() || max < 0) {
            if (dataSourceMax >= 0) return dataSourceMax;
        }
        return max;
    }
    /** max length of text.  If -1 (default) then unlimited.  
    */
    public void setMax(int x) {
        max = x;
    	if (document != null) document.setMaxLength(getMax());
    }
    
    
    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (textField != null) create(textField);
    }

    public void close() {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeMouseListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }


    public @Override void afterPropertyChange(HubEvent e) {
        if (activeObject != null && e.getObject() == activeObject) {
            if (e.getPropertyName().equalsIgnoreCase(this.getHubListenerPropertyName()) ) {
                update();
            }
        }
    }

    public @Override void afterChangeActiveObject(HubEvent e) {
        boolean b = (focusActiveObject != null && focusActiveObject == activeObject);
        if (b) onFocusLost();
        
        Hub h = getActualHub();
        if (h != null) activeObject = getActualHub().getActiveObject();
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
    	prevText = textField.getText();
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
        String text = textField.getText();
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
            
            String msg = validateNewValue(activeObject, convertedValue);
            if (msg == null) {
                msg = isValid(activeObject, convertedValue);
            }
            
            if (msg != null) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                        "Invalid Entry \""+text+"\"\n"+msg,
                        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            prevText = text;
            Object prevValue = getPropertyPathValue(activeObject);
            
            String prop = getHubListenerPropertyName();
            if (prop == null || prop.length() == 0) {  // use object.  (ex: String.class)
                Object oldObj = activeObject;
                Hub h = getActualHub();
                Object newObj = OAReflect.convertParameterFromString(h.getObjectClass(), text);
                if (newObj != null) {
                    int posx = h.getPos(oldObj);
                    h.remove(posx);
                    h.insert(newObj, posx);
                }
            }
            else {
                setPropertyPathValue(activeObject, convertedValue);
                // OAReflect.setPropertyValue(activeObject, getSetMethod(), convertedValue);
                if (text == null || text.length() == 0) {
                    Class c = getLastMethod().getReturnType();
                    if (OAReflect.isNumber(c) && activeObject instanceof OAObject) {
                    	OAObjectReflectDelegate.setProperty((OAObject)activeObject, getPropertyPathFromActualHub(), null, null);  // was: setNull(prop)
                    }
                }
            }
            OAUndoManager.add( OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, getPropertyPathFromActualHub(), prevValue, getPropertyPathValue(activeObject)) );
        }
        catch (Exception e) {
        	JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
        	        "Invalid Entry \""+e.getMessage()+"\"", 
        	        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String undoDescription;
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public void setUndoDescription(String s) {
        undoDescription = s;
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return undoDescription;
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
	    		textField.setText(prevText);
	    		textField.setCaretPosition(0);
            }
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
    
    
    protected Object getPropertyValue(Object obj) {
        if (obj == null) return null;
        if (getPropertyPath() == null) return null;
        Object value = getPropertyPathValue(obj);
        if (value instanceof OANullObject) value = null;
        
        if (value != null && obj instanceof OAObject) {
            String ss = getPropertyPathFromActualHub();
            //was: String ss = getPropertyName();
            if (OAObjectReflectDelegate.getPrimitiveNull((OAObject)obj, ss) ) value = null;
        }
        
        return value;
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return label;
    }


    @Override
    public void update() {
        if (textField == null) return;
        if (focusActiveObject == null) {
            if (getActualHub() != null) {
                String text = null;
                if (activeObject != null) {
                    Object value = getPropertyValue(activeObject);
                    if (value == null) text = "";
                    else text = OAConv.toString(value, null);  // dont format
                }
                if (text == null) {
                    text = getNullDescription();
                    if (text == null) text = " ";
                }
                boolean bHold = bSettingText;
                bSettingText = true;
                textField.setText(text);
                prevText = text; // 20110112 to fix bug found while testing undo
                textField.setCaretPosition(0);
                bSettingText = bHold;
            }   
        }        
        super.update();
        super.update(textField, activeObject);
    }
    
    
}



