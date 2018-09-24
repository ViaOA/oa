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

import java.lang.reflect.*;
import java.util.logging.Logger;

import javax.swing.*;
import com.viaoa.annotation.OAEditQuery;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAObjectReflectDelegate;


//qqqqqqqqqqqqqqqqqq not used ... can be deleted 20190920

/**
    Base controller class for OA JFC/Swing components.

    Implements the HubListener and provides most of the methods required for creating
    controller Classes (Model/View/Controller) for UI components.  
*/
public class OAJfcController2 {
    private static Logger LOG = Logger.getLogger(OAJfcController2.class.getName());
    
    public boolean DEBUG;  // used for debugging a single component. ex: ((OALabel)lbl).setDebug(true)    

    protected final JComponent component;
    
    protected Hub hubEdit;
    protected String editProperty;
    protected HubListener hlEdit;
    
    protected Hub hubDisplay;
    protected String displayPropertyPath;
    protected HubListener hlDisplay;
    
    protected HubChangeListener.Type changeListenerType;

    private HubChangeListener changeListener; // listens for any/all hub+propPaths needed for component
    private HubChangeListener changeListenerEnabled;
    private HubChangeListener changeListenerVisible;

    private String displayListenerName;
    

    public OAJfcController2(JComponent comp, Hub hubEdit, String editProperty, Hub hubDisplay, String displayPropertyPath, HubChangeListener.Type type) {
        this.component = comp;
        this.hubEdit = hubEdit;
        this.editProperty = editProperty;
        this.hubDisplay = hubDisplay;
        this.displayPropertyPath = displayPropertyPath;
        this.changeListenerType = type;

        if (hubEdit != null) {
            if (editProperty == null) editProperty = "";
            else if (editProperty.indexOf('.') >= 0) throw new IllegalArgumentException("editProperty can not be a propertyPath, prop="+editProperty);
            
            hlEdit = new HubListenerAdapter() {
                @Override
                public void afterChangeActiveObject(HubEvent e) {
                    // update();
                }
                //qqqqqq more
            };
            hubEdit.addHubListener(hlEdit, true);
        }
 
        if (hubDisplay != null) {
            hlDisplay = new HubListenerAdapter() {
                @Override
                public void afterChangeActiveObject(HubEvent e) {
                    // if (hubEdit != hubDisplay) update();
                }
                //qqqqqq more
            };
            
            if (displayPropertyPath != null) {
                if (displayPropertyPath.indexOf('.') >= 0) displayListenerName = OAString.convert(displayPropertyPath, ".", "_");
            }
            if (displayListenerName == null) hubDisplay.addHubListener(hlDisplay, true);
            else hubDisplay.addHubListener(hlDisplay, displayListenerName, new String[] {displayPropertyPath}, true);
        }
        
        if (changeListenerType != HubChangeListener.Type.HubValid) {
            if (hubEdit != null) getEnabledChangeListener().add(hubEdit, changeListenerType);
            if (hubDisplay != null && hubDisplay != hubEdit) getEnabledChangeListener().add(hubDisplay, changeListenerType);
        }
        
        loadEditQuery(hubEdit, editProperty);
        if (hubEdit != hubDisplay || OAString.isEqual(editProperty, displayPropertyPath)) {
            loadDisplayEditQuery();
        }
    }

    protected void loadDisplayEditQuery() {
        if (hubDisplay == null || OAString.isEmpty(displayPropertyPath)) return;
        
        Class cz = hubDisplay.getObjectClass();
        OAPropertyPath oaPropertyPath = new OAPropertyPath(cz, displayPropertyPath); 
        
        final String[] properties = oaPropertyPath.getProperties();
        String ppPrefix = "";
        int cnt = 0;
        for (String prop : properties) {
            loadEditQuery(cz, hubDisplay, prop, ppPrefix);
            ppPrefix += prop + ".";
            cz = oaPropertyPath.getClasses()[cnt++];
        }        
    }

    protected void loadEditQuery(Hub hub, String prop) {
        if (hub == null) return;
        loadEditQuery(hub.getObjectClass(), hub, prop, "");
        loadEditQueryMaster(hub);
    }
    
    protected void loadEditQuery(Class cz, Hub hub, String prop, String prefix) {
        if (hub == null) return;
        
        OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(cz);
        String s = oi.getEnabledProperty();
        boolean b = oi.getEnabledValue();
        if (OAString.isNotEmpty(s)) getEnabledChangeListener().add(hub, prefix+s, b);
        s = oi.getVisibleProperty();
        b = oi.getVisibleValue();
        if (OAString.isNotEmpty(s)) getVisibleChangeListener().add(hub, prefix+s, b);
        
        Method m = oi.getEditQueryMethod(prop);
        if (m != null) {
            OAEditQuery oaq = (OAEditQuery) m.getAnnotation(OAEditQuery.class);
            if (oaq != null) {
                s = oaq.enabledProperty();
                b = oaq.enabledValue();
                if (OAString.isNotEmpty(s)) getEnabledChangeListener().add(hub, prefix+s, b);
                s = oaq.visibleProperty();
                b = oaq.visibleValue();
                if (OAString.isNotEmpty(s)) getVisibleChangeListener().add(hub, prefix+s, b);
            }
        }        
    }
    protected void loadEditQueryMaster(Hub hub) {
        if (hub == null) return;
        Hub hx = hub.getMasterHub();
        if (hx != null) {
            final String propx = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
            loadEditQuery(hx, propx);
            loadEditQueryMaster(hx);
        }
    }
    

    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        if (changeListener != null) {
            changeListener.close();
            changeListener = null;
        }
        if (changeListenerEnabled != null) {
            changeListenerEnabled.close();
            changeListenerEnabled = null;
        }
        if (changeListenerVisible != null) {
            changeListenerVisible.close();
            changeListenerVisible = null;
        }
        //qqqqqqqq remove hub listeners
    }

    
    /**
     * This will find the real object in this hub use, in cases where a comp is added to
     * a table, and the table.hub is different then the comp.hub, which could be
     * a detail or link type relationship to the table.hub
     */
    private Class fromParentClass;
    private String fromParentPropertyPath;
    protected Object getRealObjectForDisplay(Object fromObject) {
        if (fromObject == null) return fromObject;
        Hub h = hubEdit == null ? hubEdit : hubDisplay; 
        if (h == null) return fromObject;
        Class c = h.getObjectClass();
        if (c == null || c.isAssignableFrom(fromObject.getClass())) return fromObject;
        if (!(fromObject instanceof OAObject)) return fromObject;
        
        if (fromParentClass == null || !fromParentClass.equals(fromObject.getClass())) {
            fromParentClass = fromObject.getClass();
            fromParentPropertyPath = OAObjectReflectDelegate.getPropertyPathFromMaster((OAObject)fromObject, hubDisplay);
        }
        return OAObjectReflectDelegate.getProperty((OAObject)fromObject, fromParentPropertyPath);
    }
    

    
    public HubChangeListener getChangeListener() {
        if (changeListener != null) return changeListener;
        changeListener = new HubChangeListener() {
            @Override
            protected void onChange() {
//qqq                OAJfcController.this.update();
            }
        };
        return changeListener;
    }
    
    public HubChangeListener getEnabledChangeListener() {
        if (changeListenerEnabled != null) return changeListenerEnabled;
        changeListenerEnabled = new HubChangeListener() {
            @Override
            protected void onChange() {
//qqq                OAJfcController.this.updateEnabled();
            }
        };
        return changeListenerEnabled;
    }

    
    public HubChangeListener getVisibleChangeListener() {
        if (changeListenerVisible != null) return changeListenerVisible;
        changeListenerVisible = new HubChangeListener() {
            @Override
            protected void onChange() {
//qqqq                OAJfcController.this.updateVisible();
            }
        };
        return changeListenerVisible;
    }
    
}

