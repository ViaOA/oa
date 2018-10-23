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
public class DateChooserController extends OAJfcController implements PropertyChangeListener {
    private OADateChooser dateChooser;
    private String prevValue;

    
    public DateChooserController(Hub hub, OADateChooser dc, String propertyPath) {
        super(hub, null, propertyPath, dc, HubChangeListener.Type.AoNotNull, false, true);
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

        this.afterChangeActiveObject();
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
    public @Override void afterChangeActiveObject() {
        Object oaObject = hub.getAO();
        OADate d = null;
        if (oaObject != null) {
             Object obj = getValue(oaObject);
             if (obj instanceof OADate) d = (OADate) obj;
        }
        bAO = true;
        dateChooser.setDate(d);
        bAO = false;
        super.afterChangeActiveObject();
    }

    /**
        Used to update the selected date for the value of property in the active object.
    */
    public @Override void afterPropertyChange() {
        afterChangeActiveObject(); // could be calculated property
    }

    /**
        Used to update the selected date for the value of property in the active object.
    */
    public void propertyChange(PropertyChangeEvent evt) {
        if (bAO) return;
        Object obj = hub.getActiveObject();
        if (obj != null) {
            Object prev = getValue(obj);
            if (getEnableUndo()) OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription,obj, endPropertyName, prev, dateChooser.getDate()));
            setValue(obj, dateChooser.getDate());
        }
    }


}


