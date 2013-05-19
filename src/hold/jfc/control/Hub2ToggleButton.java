/* 

2003/10/21 added support for undo

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
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/** 
    Used for binding JToggleButton, JCheckBox, JRadioButton, JMenuItem to a property value.
    Button is automatically enabled based on active object in Hub.  
    On/Off values can be set to bind to a proeperty in the active object.
    Button can also be set up to add/remove object to a second Hub.
    <p>
    Example:<br>
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    JCheckBox chk = new JCheckBox("Retired");
    Hub2ToggleButton htb = new Hub2ToggleButton(hub, chk, "retired", true, false);
    htb.setNullValue(false); // substitute null for a false

    JRadioButton rad1 = new JRadioButton("Yes");
    new Hub2ToggleButton(hub, rad, "retired", true);
    JRadioButton rad2 = new JRadioButton("No");
    Hub2ToggleButton htb = new Hub2ToggleButton(hub, rad, "retired", false);
    htb.setNullValue(false); // substitute null for a false
    
    
    </pre>
    @see OACheckBox
    @see OARadioButton
    @see OAMenuItem
*/
public class Hub2ToggleButton extends Hub2Gui implements ItemListener {
    JToggleButton button;
    public Object valueOn = Boolean.TRUE;
    public Object valueOff = Boolean.FALSE;  // set to OANullObject to ignore unselect (ex: RadioButton)
    protected Hub hub2;  // another hub that this button relies on
    public Hub hubSelect;
    protected int xorValue;
    String confirmMessage;
    
    Object valueNull;

    /**
        Bind a button, need to set Hub and property path.
    */
    public Hub2ToggleButton(JToggleButton button) {
        create(button);   
    }
    
    /**
        Bind a button to a property path to the active object for a Hub.
    */
    public Hub2ToggleButton(Hub hub, JToggleButton button, String propertyPath) {
        super(hub, propertyPath, button); // this will add hub listener
        create(button);
    }


    /**
        Bind a button to have it add/remove objects a Hub.  
        @param hub that has active object that is added/removed from hubSelect
        @param hubSelect the active object from hub will be added/removed from this Hub.
    */
    public Hub2ToggleButton(Hub hub, Hub hubSelect, JToggleButton button) {
        super(hub, "", button);
        setSelectHub(hubSelect);
        create(button);
    }
    
    /**
        Bind a button to a property path to the active object for a Hub.
        Button wil be enabled based on active object in Hub not being null.
    */
    public Hub2ToggleButton(Object object, JToggleButton button, String propertyPath) {
        super(object, propertyPath, button); // this will add hub listener
        create(button);
    }

    /**
        Bind a button to a property path for an object.
        @param valueOn value to use for property when button is selected
        @param valueOFf value to use for property when button is not selected
    */
    public Hub2ToggleButton(Object object, JToggleButton button, String propertyPath, Object valueOn, Object valueOff) {
        super(object, propertyPath, button); // this will add hub listener
        this.valueOn = valueOn;
        this.valueOff = valueOff;
        create(button);
    }

    /**
        Bind a button to a property path for an object.
        @param valueOn value to use for property when button is selected
    */
    public Hub2ToggleButton(Object object, JToggleButton button, String propertyPath, Object valueOn) {
        super(object, propertyPath, button); // this will add hub listener
        this.valueOn = valueOn;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
    */
    public Hub2ToggleButton(Hub hub, JToggleButton button, String propertyPath, int value) {
        this(hub, button, propertyPath, new Integer(value));
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
    */
    public Hub2ToggleButton(Hub hub, JToggleButton button, String propertyPath, boolean value) {
        super(hub, propertyPath, button); // this will add hub listener
        this.valueOn = new Boolean(value);
        this.valueOff = OANullObject.nullObject;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
    */
    public Hub2ToggleButton(Hub hub, JToggleButton button, String propertyPath, Object value) {
        super(hub, propertyPath, button); // this will add hub listener
        this.valueOn = value;
        this.valueOff = OANullObject.nullObject;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button wil be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
        @param valueOFf value to use for property when button is not selected
    */
    public Hub2ToggleButton(Hub hub, JToggleButton button, String propertyPath, Object valueOn, Object valueOff) {
        super(hub, propertyPath, button); // this will add hub listener
        this.valueOn = valueOn;
        this.valueOff = valueOff;
        create(button);
    }

    
    /**
        Second Hub that is used to determine if this button is enabled.
        Button is enabled based on the active object not being null.
    */
    public void setDependentHub(Hub hub2) {
        this.hub2 = hub2;
        if (hub2 != null) {
            hub2.addHubListener(new HubListenerAdapter() {
                public @Override void afterChangeActiveObject(HubEvent e) {
                    updateEnabled();
                }
            });
        }
        updateEnabled();
    }

    /**
        Second Hub that is used to determine if this button is enabled.
        Button is enabled based on the active object not being null.
    */
    public Hub getDependentHub() {
        return this.hub2;
    }
    

    /** 
        The value to use if the property is null.  
        Useful when using primitive types that might be set to null.
        <p>
        Example:<br>
        A boolean property can be true,false or null.  Might want to have a null value 
        treated as false.<br>
        setNullValue(false);
    */
    public void setNullValue(Object value) {
        valueNull = value;
    }
    /** 
        The value to use if the property is null. 
        @see #setNullValue(Object)
    */
    public void setNullValue(boolean b) {
        setNullValue(new Boolean(b));
    }
    /** 
        The value to use if the property is null. 
        @see #setNullValue(Object)
    */
    public Object getNullValue() {
        return valueNull;
    }

    /** 
        Hub used to add/remove objects from
    */
    public void setSelectHub(Hub hub) {
        hubSelect = hub;
        setPropertyPath("");
    }
    /** 
        Hub used to add/remove objects from
    */
    public Hub getSelectHub() {
        return hubSelect;
    }

    private void create(JToggleButton but) {
        if (button != null) button.removeItemListener(this);
        button = but;
        if (button != null) button.addItemListener(this);

        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
        }
    }

    public void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (button != null) create(button);
    }

    public void close() {
        if (button != null) button.removeItemListener(this);
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
        if (bFlag) return;
        if (getActualHub() == null) return;
        bFlag = true;
        boolean b = false;

        
        Object oaObject = getActualHub().getActiveObject();

        if (hubSelect != null) {
            b = (oaObject != null && hubSelect.getObject(oaObject) != null);
        }
        else {
            Object obj = null;
            if (oaObject != null) {
                obj = OAReflect.getPropertyValue(oaObject, getGetMethod());
            }
            if (obj == null) obj = valueNull;
            if (obj == null && valueOn == null) b = true;
            else if (obj == null || valueOn == null) b = false;
            else {
            	if (valueOff == null) b = true;  // 2006/10/26 if off is set to null, then any non-null is true
            	else b = obj.equals(valueOn);
            }
            if (xorValue > 0) {
                int x = 0;
                if (obj instanceof Number) x = ((Number) obj).intValue();
                b = (x & xorValue) > 0;
            }
        }
        b = isSelected(oaObject, b);
        
        if (button.isSelected() != b) button.setSelected(b);
        bFlag = false;
        updateEnabled();
    }
    
    /**
     * Called by Hub2Button to hook into whether button is checked for a specific object.
     * @param obj
     * @param b default value already set by Hub2Button.
     * @return
     */
    public boolean isSelected(Object obj, boolean b) {
    	return b;
    }
    
    public void updateEnabled() {
        boolean b = getActualHub().getAO() != null;
        if (b && hub2 != null) b = hub2.getAO() != null;
        setInternalEnabled(b && isParentEnabled(button));
    }

    public @Override void afterPropertyChange(HubEvent e) {
        if (e.getObject() instanceof Hub) return;
        if ( e.getPropertyName().equalsIgnoreCase(getPropertyName()) ) {
            this.afterChangeActiveObject(e);
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
    
    
    public boolean isChanging() {
        return bFlag;
    }
    private boolean bFlag;
    public void itemStateChanged(ItemEvent evt) {
        if (bFlag) return;
        bFlag = true;
        try {
            if (confirmMessage != null && !confirm()) {
                bFlag = false;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        afterChangeActiveObject(null);
                    }
                });
                return;
            }
            int type = evt.getStateChange();   

            Object value;
            if (type == ItemEvent.SELECTED) value = valueOn;
            else value = valueOff;
            if (value instanceof OANullObject) return;

            if ( (hubSelect != null) || (!getReadOnly() && getActualHub() != null)) {
                Object obj = getActualHub().getActiveObject();
                if (obj != null) {

                    if (hubSelect != null) {
                        if (type == ItemEvent.SELECTED) {
                            if (hubSelect.getObject(obj) == null) hubSelect.add(obj);
                        }
                        else hubSelect.remove(obj);
                    }
                    else {
                        Method method = null;
                        try {
                            Object prev = OAReflect.getPropertyValue(obj, getGetMethod());
                            
                            if (xorValue > 0) {
                                int x = 0;
                                if (prev instanceof Number) x = ((Number) prev).intValue();
                                if (type == ItemEvent.SELECTED) x = (x | xorValue);
                                else x = (x ^ xorValue);
                                value = new Integer(x);
                            }
                            
                            method = getSetMethod();
                            if (method == null) throw new RuntimeException("Hub2ToggleButton.itemStateChanged() - cant find setMethod for property \""+getPropertyName()+"\"");
                            method.invoke(obj, new Object[] { value } );

                            OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, obj, getPropertyName(), prev, OAReflect.getPropertyValue(obj, getGetMethod())) );
                            
                            bFlag = false;
                            afterChangeActiveObject(null);  // check to make sure value "took"
                            // 09/03/2000 was:
                            // if (method != null && (method.getParameterTypes())[0].equals(boolean.class)) method.invoke(obj, new Object[] { new Boolean(value) } );
                        }
                        catch (InvocationTargetException e) {
                            bFlag = false;
                            /*
                            hubChangeActiveObject(null); // set back
                            Throwable t = e.getTargetException();
                            if (t instanceof OAException) {
                                throw ((OAException) t);
                            }
                            else {
                                throw new OAException("Hub2ToggleButton.itemStateChanged() exception invoking method="+ method.getName()+" class="+this.getActualHub().getObjectClass().getName()+" "+t,t);
                            }
                            */
                        }
                        catch(IllegalAccessException e) {
                            // throw new OAException("Hub2ToggleButton.itemStateChanged() exception invoking method="+ method.getName()+" class="+this.getActualHub().getObjectClass().getName()+" Exception="+e.getMessage() );
                        }
                    }
                }                               
            }
        }
        finally {
            bFlag = false;
        }
    }
    
    public void setXORValue(int xor) {
        this.xorValue = xor;
    }

    public int getXORValue() {
        return this.xorValue;
    }

    /*
    Popup message used to confirm button click before running code.
    */
    public void setConfirmMessage(String msg) {
        confirmMessage = msg;
    }
    /**
        Popup message used to confirm button click before running code.
    */
    public String getConfirmMessage() {
        return confirmMessage;
    }
    
    /** returns true if command is allowed */
    protected boolean confirm() {
        int x = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(button), confirmMessage, "Confirmation", 0, JOptionPane.QUESTION_MESSAGE,null, new String[] {"Yes","No"}, "Yes");
        return (x == 0);
    }
    
}

