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

import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;

/**
 * Controller for binding OA to AbstratButton.
 * @author vvia
 *
 */
public class ToggleButtonController extends JFCController implements ItemListener {
    AbstractButton button;
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
    public ToggleButtonController(AbstractButton button) {
        create(button);   
    }
    
    /**
        Bind a button to a property path to the active object for a Hub.
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath) {
        super(hub, propertyPath, button); // this will add hub listener
        create(button);
    }


    /**
        Bind a button to have it add/remove objects from a Hub.  
        @param hub that has active object that is added/removed from hubSelect
        @param hubSelect the active object from hub will be added/removed from this Hub.
    */
    public ToggleButtonController(Hub hub, Hub hubSelect, AbstractButton button) {
        super(hub, "", button);
        setSelectHub(hubSelect);
        create(button);
    }
    
    /**
        Bind a button to a property path to the active object for a Hub.
        Button wil be enabled based on active object in Hub not being null.
    */
    public ToggleButtonController(Object object, AbstractButton button, String propertyPath) {
        super(object, propertyPath, button); // this will add hub listener
        create(button);
    }

    /**
        Bind a button to a property path for an object.
        @param valueOn value to use for property when button is selected
        @param valueOFf value to use for property when button is not selected
    */
    public ToggleButtonController(Object object, AbstractButton button, String propertyPath, Object valueOn, Object valueOff) {
        super(object, propertyPath, button); // this will add hub listener
        this.valueOn = valueOn;
        this.valueOff = valueOff;
        create(button);
    }

    /**
        Bind a button to a property path for an object.
        @param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Object object, AbstractButton button, String propertyPath, Object valueOn) {
        super(object, propertyPath, button); // this will add hub listener
        this.valueOn = valueOn;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, int value) {
        this(hub, button, propertyPath, new Integer(value));
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, boolean value) {
        super(hub, propertyPath, button); // this will add hub listener
        this.valueOn = new Boolean(value);
        this.valueOff = OANullObject.instance;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, Object value) {
        super(hub, propertyPath, button); // this will add hub listener
        this.valueOn = value;
        this.valueOff = OANullObject.instance;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button wil be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
        @param valueOFf value to use for property when button is not selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, Object valueOn, Object valueOff) {
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
                    update();
                }
            });
        }
        update();
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

    private void create(AbstractButton but) {
        if (button != null) button.removeItemListener(this);
        button = but;
        if (button != null) button.addItemListener(this);

        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
            getEnabledController().add(getActualHub());
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
                obj = getPropertyPathValue(oaObject);
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
        
        if (button.isSelected() != b) {
            button.setSelected(b);
        }
        bFlag = false;
        update();
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
    
    public @Override void afterPropertyChange(HubEvent e) {
        if (e.getObject() instanceof Hub) return;
        if ( e.getPropertyName().equalsIgnoreCase(getHubListenerPropertyName()) ) {
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
    private volatile boolean bFlag;
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

            if ( (hubSelect != null) || getActualHub() != null) {
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
                            Object prev = getPropertyPathValue(obj);
                            
                            if (xorValue > 0) {
                                int x = 0;
                                if (prev instanceof Number) x = ((Number) prev).intValue();
                                if (type == ItemEvent.SELECTED) x = (x | xorValue);
                                else x = (x ^ xorValue);
                                value = new Integer(x);
                            }

                            setPropertyPathValue(obj, value);
                            /* was
                            method = getSetMethod();
                            if (method == null) throw new RuntimeException("Hub2ToggleButton.itemStateChanged() - cant find setMethod for property \""+getPropertyName()+"\"");
                            method.invoke(obj, new Object[] { value } );
                            */
                            OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, obj, getPropertyPathFromActualHub(), prev, getPropertyPathValue(obj)) );
                            
                            bFlag = false;
                            Object objx = getActualHub().getActiveObject();
                            if (obj == objx) { // 20130919, object could have been removed
                                afterChangeActiveObject(null);  // check to make sure value "took"
                            }
                            // 09/03/2000 was:
                            // if (method != null && (method.getParameterTypes())[0].equals(boolean.class)) method.invoke(obj, new Object[] { new Boolean(value) } );
                        }
                        catch (Exception e) {
                            // this needs a better solution
                            System.out.println("ToggleButtonController exception: "+e);
                            e.printStackTrace();
                            bFlag = false;
                            try {
                                afterChangeActiveObject(null); // reset
                            }
                            catch (Exception ex) {}
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

    
    @Override
    protected void update() {
        if (button == null) return;
        
        Object obj = null;
        if (getHub() != null) obj = getHub().getAO();
        super.update();
        super.update(button, obj);
    }
    
}

