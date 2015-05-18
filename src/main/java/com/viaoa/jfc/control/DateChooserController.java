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

import java.beans.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/** 
    Works directly with OADateChooser for binding an calendar component to an object property.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OADateChooser
    @see OADateComboBox
*/
public class DateChooserController extends JFCController implements PropertyChangeListener {
    private OADateChooser dateChooser;
    private String prevValue;

    public DateChooserController(Hub hub, OADateChooser dc, String propertyPath) {
        super(hub, propertyPath, dc); // this will add hub listener
        create(dc);
    }
    protected void create(OADateChooser dc) {
        if (dateChooser != null) {
            dateChooser.removePropertyChangeListener(this);
        }
        dateChooser = dc;

        if (dateChooser != null) {
            dateChooser.addPropertyChangeListener(this);
        }
        // set initial value of textField
        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
        }
        afterChangeActiveObject(null);
        if (getHub() != null) {
            getEnabledController().add(getHub());
        }
    }

    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (dateChooser != null) create(dateChooser);
    }

    public void close() {
        if (dateChooser != null) {
            dateChooser.removePropertyChangeListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }

    
    private boolean bAO;
    /**
        Used to update the selected date for the value of property in the active object.
    */
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (getActualHub() == null) return;
        Object oaObject = getActualHub().getActiveObject();
        OADate d = null;
        if (oaObject != null) {
             Object obj = getPropertyPathValue(oaObject);
             if (obj instanceof OADate) d = (OADate) obj;
        }
        bAO = true;
        dateChooser.setDate(d);
        bAO = false;
        update();
    }

    /**
        Used to update the selected date for the value of property in the active object.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        if (getActualHub() == null) return;
        if (e.getObject() == getActualHub().getActiveObject() && e.getPropertyName().equalsIgnoreCase(this.getHubListenerPropertyName()) ) {
            afterChangeActiveObject(e); // could be calculated property
        }
    }

    /**
        Used to update the selected date for the value of property in the active object.
    */
    public void propertyChange(PropertyChangeEvent evt) {
        if (bAO) return;
        Hub h = getActualHub();
        if (h == null) return;
        Object obj = h.getActiveObject();
        if (obj != null) {
            Object prev = getPropertyPathValue(obj);
            OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription,obj, getPropertyPathFromActualHub(), prev, dateChooser.getDate()));
            setPropertyPathValue(obj, dateChooser.getDate());
            // OAReflect.setPropertyValue(obj, getSetMethod(), dateChooser.getDate());
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

}


