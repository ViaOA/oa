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
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.*;

/**
 * Controller for binding OA to AbstratButton.
 * @author vvia
 *
 */
public class ToggleButtonController extends OAJfcController implements ItemListener {
    AbstractButton button;
    public Object valueOn = Boolean.TRUE;
    public Object valueOff = Boolean.FALSE;  // set to OANullObject to ignore unselect (ex: RadioButton)
    protected Hub hub2;  // another hub that this button relies on
    protected int xorValue;
    
    protected Object valueNull;

    /**
        Bind a button to a property path to the active object for a Hub.
    */
    public ToggleButtonController(AbstractButton button) {
        super(null, null, null, button, HubChangeListener.Type.Unknown, false, true);
        create(button);
    }
    
    /**
        Bind a button to a property path to the active object for a Hub.
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath) {
        super(hub, null, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        create(button);
    }

    /**
        Bind a button to have it add/remove objects from a Hub.  
        @param hub that has active object that is added/removed from hubMultiSelect
        @param hubMultiSelect the active object from hub will be added/removed from this Hub.
    */
    public ToggleButtonController(Hub hub, Hub hubMultiSelect, AbstractButton button) {
        super(hub, null, "", button, HubChangeListener.Type.AoNotNull, false, true);
        setSelectHub(hubMultiSelect);
        create(button);
    }
    
    /**
        Bind a button to a property path to the active object for a Hub.
        Button wil be enabled based on both active object and propertyPath in Hub not being null.
    */
    public ToggleButtonController(Object object, AbstractButton button, String propertyPath) {
        super(null, object, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        create(button);
    }

    /**
        Bind a button to a property path for an object.
        @param valueOn value to use for property when button is selected
        @param valueOff value to use for property when button is not selected
    */
    public ToggleButtonController(Object object, AbstractButton button, String propertyPath, Object valueOn, Object valueOff) {
        super(null, object, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        this.valueOn = valueOn;
        this.valueOff = valueOff;
        create(button);
    }

    /**
        Bind a button to a property path for an object.
        @param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Object object, AbstractButton button, String propertyPath, Object valueOn) {
        super(null, object, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        this.valueOn = valueOn;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, int value) {
        this(hub, button, propertyPath, new Integer(value));
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, boolean value) {
        super(hub, null, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        this.valueOn = new Boolean(value);
        this.valueOff = OANullObject.instance;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button will be enabled based on active object in Hub not being null.
        param valueOn value to use for property when button is selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, Object value) {
        super(hub, null, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        this.valueOn = value;
        this.valueOff = OANullObject.instance;
        create(button);
    }

    /**
        Bind a button to a property path to the active object for a Hub.
        Button wil be enabled based on active object in Hub not being null.
        @param valueOn value to use for property when button is selected
        @param valueOff value to use for property when button is not selected
    */
    public ToggleButtonController(Hub hub, AbstractButton button, String propertyPath, Object valueOn, Object valueOff) {
        super(hub, null, propertyPath, button, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
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

    private void create(AbstractButton but) {
        if (button != null) button.removeItemListener(this);
        button = but;
        if (button != null) button.addItemListener(this);

        if (getHub() == null) return;
        // this needs to run before listeners are added
        HubEvent e = new HubEvent(hub, hub.getActiveObject());
        this.afterChangeActiveObject();
    }

    public void close() {
        if (button != null) button.removeItemListener(this);
        super.close();  // this will call hub.removeHubListener()
    }

    @Override
    protected void afterPropertyChange() {
        afterChangeActiveObject();
    }
    
    // HUB Events
    public @Override void afterChangeActiveObject() {
        try {
            if (bFlag) return;
            _afterChangeActiveObject();
            bFlag = true;
        }
        finally {
            bFlag = false;
        }
    }
    protected void _afterChangeActiveObject() {
        boolean b = false;

        Object oaObject = hub.getActiveObject();

        if (hubSelect != null) {
            b = (oaObject != null && hubSelect.getObject(oaObject) != null);
        }
        else {
            Object obj = null;
            if (oaObject != null) {
                obj = getValue(oaObject);
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
    
    public boolean isChanging() {
        return bFlag;
    }
    
    private volatile boolean bFlag;
    
    public void itemStateChanged(ItemEvent evt) {
        try {
            if (bFlag) return;
            bFlag = true;
            _itemStateChanged(evt);
        }
        finally {
            bFlag = false;
        }
    }    
    protected void _itemStateChanged(ItemEvent evt) {
        Object value;

        if ( (hubSelect != null) || hub != null) {
            Object obj = hub.getActiveObject();
            if (obj != null) {
                
                int type = evt.getStateChange();   
                if (type == ItemEvent.SELECTED) value = valueOn;
                else value = valueOff;
                if (value instanceof OANullObject) {
                    return;
                }
                
                if (hubSelect != null) {
                    if (type == ItemEvent.SELECTED) {
                        if (hubSelect.getObject(obj) == null) hubSelect.add(obj);
                    }
                    else hubSelect.remove(obj);
                }
                else {
                    
                    Method method = null;
                    try {
                        Object prev = getValue(obj);
                        if (xorValue > 0) {
                            int x = 0;
                            if (prev instanceof Number) x = ((Number) prev).intValue();
                            if (type == ItemEvent.SELECTED) x = (x | xorValue);
                            else x = (x ^ xorValue);
                            value = new Integer(x);
                        }

                        if (OACompare.compare(prev, value) == 0) {
                            return;
                        }
                        
                        boolean bValid = true;
                        String msg = isValid(obj, value);
                        if (msg != null) {
                            JOptionPane.showMessageDialog(SwingUtilities.getRoot(component), 
                                "Invalid Entry "+msg,
                                "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                            bValid = false;
                        }
                        else if (!confirmPropertyChange(obj, value)) bValid = false;
                        // else if (!confirm()) bValid = false;
                        if (!bValid) {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    bFlag = true;
                                    afterChangeActiveObject();
                                    bFlag = false;
                                }
                            });
                            return;
                        }
                        
                        setValue(obj, value);
                        /* was
                        method = getSetMethod();
                        if (method == null) throw new RuntimeException("Hub2ToggleButton.itemStateChanged() - cant find setMethod for property \""+getPropertyName()+"\"");
                        method.invoke(obj, new Object[] { value } );
                        */
                        if (getEnableUndo()) {
                            OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, obj, endPropertyName, prev, value));
                        }

                        Object objx = hub.getActiveObject();
                        if (obj == objx) { // 20130919, object could have been removed
                            afterChangeActiveObject();  // check to make sure value "took"
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
                            afterChangeActiveObject(); // reset
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
    
    public void setXORValue(int xor) {
        this.xorValue = xor;
    }

    public int getXORValue() {
        return this.xorValue;
    }

}

