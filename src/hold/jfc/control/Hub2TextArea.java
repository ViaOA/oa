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


import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/**
    Used for binding a JTextArea component to a property in an Object or Hub.
    <p>
    Example:<br>
    This will create a JTextArea that will automatically display the Notes property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    JTextArea txta = new JTextArea(8,30);
    txta.setLineWrap(true);
    txta.setWrapStyleWord(true);
    new Hub2TextArea(hubEmployee, txta, "notes");
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see Hub2TextArea
*/
public class Hub2TextArea extends Hub2Gui implements FocusListener, KeyListener {
    JTextArea textField;
    String prevValue;
    private Object activeObject;

    /**
        Create an unbound TextArea.
    */
    public Hub2TextArea(JTextArea tf) {
        create(tf);
    }

    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public Hub2TextArea(Hub hub, JTextArea tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }
    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
    */
    public Hub2TextArea(Object object, JTextArea tf, String propertyPath) {
        super(object, propertyPath, tf); // this will add hub listener
        create(tf);
    }


    private void create(JTextArea tf) {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
        }
        textField = tf;
        tf.setBorder(new EmptyBorder(2,2,2,2));

        if (textField != null) {
            textField.addFocusListener(this);
            textField.addKeyListener(this);
        }
        // set initial value of textField
        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
        }
        else {
            if (tf != null) {
                bSettingText = true;
                tf.setText("");
                bSettingText = false;
            }
        }
    }

    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (textField != null) create(textField);
    }

    public void close() {
        if (textField != null) {
            textField.removeFocusListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }

    @Override
    public void setReadOnly(boolean b) {
        super.setReadOnly(b);
        setEnabled(!b);
        afterChangeActiveObject(null);
    }
    
    // HUB Events
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (textField.hasFocus()) {
            bChanging = false;
            bSettingText = false;
            saveChanges();
        }
        if (getActualHub() == null) return;

        activeObject = getActualHub().getActiveObject();
        
        String s = "";
        if (activeObject != null) {
            s = OAReflect.getPropertyValueAsString(activeObject, getGetMethod());
            if ((activeObject instanceof OAObject) && OAObjectReflectDelegate.getPrimitiveNull((OAObject)activeObject, getPropertyName()) ) s = "";
            setInternalEnabled(true && isParentEnabled(textField));
        }
        else {
            setInternalEnabled(false);
        }
        bSettingText = true;
        updateComponent(textField, activeObject, s);
        textField.setCaretPosition(0);
        bSettingText = false;
    }
    private String hpcs;

    /**
        Updates TextArea on new value for bound property.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        if (getActualHub() == null) return;
        if (activeObject == null) return;
        if (e.getObject() != activeObject) return;
        if (bChanging) return;
        
        if ( !OAString.equalsIgnoreCase(e.getPropertyName(), this.getPropertyName()) ) return;
        
        if (textField.hasFocus()) {
            // 20101015
            onValueChangedWhileEditing();
        }
        else {
            afterChangeActiveObject(null);
        }
    }

    /**

    */
    public void focusGained(FocusEvent e) {
    	Hub h = getActualHub();
    	if (h != null) activeObject = getActualHub().getActiveObject();
    	else activeObject = null;
    	
        int i,x;
        if (getReadOnly()) this.setInternalEnabled(false);
        prevValue = textField.getText();
    }

    /**
        Updates property value with textarea text.  If needed, converts to correct data type.
    */
    public void focusLost(FocusEvent e) {
        saveChanges();
    }

    private boolean bChanging; // only used by saveChanges(), calling setText generates actionPerformed()
    private boolean bSettingText;
    protected void saveChanges() {
        if (bChanging) return;
        if (bSettingText) return;
        if (getActualHub() == null) return;
        if (activeObject == null) return;
        
        try {
            bChanging = true;
            int i,x;
            String s = null;

            s = textField.getText();
            if (OAString.compare(s, prevValue) != 0) {
                if (!getReadOnly()) {
                    Object current = OAReflect.getPropertyValue(activeObject, getGetMethod());
                    
                    // 20101015 make sure that property has not changed by another user while editing
                    boolean bChange = true;
                    if (current instanceof String && OAString.compare(prevValue, (String)current) != 0) {
                        s = getValueToUse(prevValue, (String) current, s);
                        if (OAString.compare(s, (String) current) == 0) bChange = false;
                    }
                    if (bChange) {
                        OAReflect.setPropertyValue(activeObject, getSetMethod(), s);
                        OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, getPropertyName(), current, OAReflect.getPropertyValue(activeObject, getGetMethod())) );
                    }
                }
                else {
                    String prop = getPropertyName();
                    if (prop == null || prop.length() == 0) {  // use object.  (ex: String.class)
                        Object oldObj = activeObject;
                        Object newObj = OAReflect.convertParameterFromString(getActualHub().getObjectClass(), s);
                        //was: getActualHub().replace(oldObj,newObj);
                        Hub h = getActualHub();
                        int pos = h.getPos(oldObj);
                        if (pos >= 0) {
                        	h.remove(pos);
                        	h.insert(newObj, pos);
                        }
                    }
                    else {
                        textField.setText(prevValue); // cant change value (redo later... dont allow change to begin with)
                    }
                }
                prevValue = s;
            }
            else {
            	/* 2008/05/09 took out to simplify
                if (tempChangedFlag) {
                    ((OAObject)activeObject).setChanged(false);
                }
                */
            }
        }
        finally {
            bChanging = false;
            // tempChangedFlag = false;
        }
    }

    // 2008/05/09 took out: boolean tempChangedFlag;  // used by keylistener to determine if the setChange() has been called
    // Key Events
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}

    
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

    /**
        Checks to see if value is changed.
    */
    public void keyTyped(KeyEvent e) {
/* 2008/05/09 took out to simplify    	
        Hub h = getActualHub();
        if (h != null) {
            Object obj = h.getActiveObject();
            if (obj instanceof OAObject) {
                if (textField.getText().equals(prevValue)) {
                    if (tempChangedFlag) ((OAObject)obj).setChanged(false);
                    tempChangedFlag = false;
                }
                else {
                    if (!tempChangedFlag) {
                        if ( !((OAObject)obj).getChanged() ) {
                            tempChangedFlag = true;
                            ((OAObject)obj).setChanged(true);
                        }
                    }
                }
            }
        }
*/        
    }
    
    /**
     * This is called before the text is saved to a property, in cases where the data was changed by
     * another user while this user was editing.  This is not called if the user did not make any changes from the original value.
     * Note: if a property is being edited (hasFocus), then propertyChanges are ignored.
     * 
     * @param origValue value of the data when the editing started.
     * @param currentValue current property value
     * @param newValue new value entered by this user
     * @return returns the newValue
     */
    protected String getValueToUse(String origValue, String currentPropertyValue, String newValueFromThisUser) {
        return newValueFromThisUser;
    }
    /**
     * Called by afterChangeProperty, if another user has changed the value that this user is currently editing (hasFocus)
     */
    protected void onValueChangedWhileEditing() {
    }
}
