/* 
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
// check to see if current object is changed before allowing movement
package com.viaoa.jfc.control;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/** 
    Used for binding a JLabel component to a property in an Object or Hub.  An icon property can also be
    set to display an image with the label.

    <p>
    Example:<br>
    This will create a JLabel that will automatically display the FullName property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    new Hub2Label(hubEmployee, "fullName");
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OALabel
*/
public class Hub2Label extends Hub2Gui {
    JLabel label;
    String prevValue;
    String origData;

    
    /**
        Create an unbound label.
    */
    public Hub2Label(JLabel lab) {
        create(lab);
    }
    
    /**
        Bind a label to a property for the active object in a Hub.
    */
    public Hub2Label(Hub hub, JLabel lab, String propertyPath) {
        super(hub, propertyPath, lab); // this will add hub listener
        create(lab);
    }

    /**
        Bind a label to the active object in a Hub.  Can be used to only
        display an icon for the active object.
        @see HubGuiAdapter#setImageProperty
    */
    public Hub2Label(Hub hub, JLabel lab) { 
        super(hub, lab); // this will add hub listener
        create(lab);
    }

    /**
        Bind a label to a property for an object.
    */
    public Hub2Label(Object object, JLabel lab, String propertyPath) {
        super(object, propertyPath, lab); // this will add hub listener
        create(lab);
    }

    /**
        Bind a label.
    */
    private void create(JLabel lab) {
        label = lab;
		// if (label != null) label.setFont(label.getFont().deriveFont(Font.BOLD));
        if (label != null) {
        	label.setBorder(new CompoundBorder(new LineBorder(Color.lightGray, 1), new EmptyBorder(0,2,0,2)));
        }
        
        
        origData = lab.getText();
        // set initial value of label
        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
        }
        else {
            if (lab != null) invokeSetText(origData, null, null);
        }
    }

    void invokeSetText(final String s, final Object obj, final Object objOrig) {
        // obj = from actualHub
        // objOrig = from hub.AO - this is needed to be able to use the other propertyPaths for a Label. ex: iconColorProperty, etc.
// 20080520 changed to not use invokeLater
// 20080520 changed to use objOrig for updateComponent, since the property paths for colors/icons/etc are from
// 20080520 changed to use repaint instead of invalidate (since invalidate is for layout management)    	
    	   //  the original obj (in orig Hub) 
        if (true || SwingUtilities.isEventDispatchThread()) {
            updateComponent(label, objOrig, s);
            // label.invalidate();  
            label.repaint(); // this is needed to refresh color icon, (maybe other changes also)
        }
        else {
            try {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateComponent(label, objOrig, s);
                        label.invalidate();
                    }
                });
            }
            catch (Exception e) {}
        }
    }

    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (label != null) create(label);
    }

    /**
        Changing active object in Hub will display property value in JLabel.
    */
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (getActualHub() == null) return;
        Object oaObject = getActualHub().getActiveObject();
        String hcas;
        if (oaObject != null) {
            if (getPropertyPath() != null) {
                Object obj = OAReflect.getPropertyValue(oaObject, getGetMethod());
                hcas = OAConv.toString(obj, getFormat());
                
                // hcas = ClassModifier.getPropertyValueAsString(oaObject, getGetMethod());
                if (hcas == null || hcas.length() == 0) hcas = origData;   
                if (oaObject instanceof OAObject) {
                    String ss = getPropertyName();
                    if ( OAObjectReflectDelegate.getPrimitiveNull((OAObject)oaObject, ss) ) hcas = "";
                }
            }
            else hcas = "";
        }
        else hcas = origData;
        if (hcas == null || hcas.length() == 0) hcas = " ";
        invokeSetText(hcas, oaObject, getHub().getAO());
    }

    /**
        Used to display property value in JLabel.
    */
    public @Override void afterPropertyChange(HubEvent e) {
if (bDebug) {
	int xx = 4;
}
        String prop = e.getPropertyName();
        boolean b = prop.equalsIgnoreCase(this.getPropertyName());
        if (b || (getActualHub() != null && e.getObject() == getActualHub().getActiveObject())) {
        	if (!b) {
        		String s = getImageProperty();
        		if (s != null) b = s.toLowerCase().indexOf(prop.toLowerCase()) >= 0;
            	if (!b) {
            		s = getIconColorProperty();
            		if (s != null) b = s.toLowerCase().indexOf(prop.toLowerCase()) >= 0;
            	}
        	}
            if (b) { 
            	afterChangeActiveObject(e); // could be calculated property
            }
        }
        else {
            if (label instanceof OALabel) {
                OATable t = ((OALabel)label).getTable();
                if (t != null) {
                    if (e.getPropertyName().equalsIgnoreCase(this.getPropertyName()) || e.getPropertyName().equalsIgnoreCase(getImageProperty()) ) {
                        t.repaint();
                    }
                }
            }
        }
    }

    // 2004/08/04
    public void setEnabled(boolean b) {
        if (!bInternallyCallingEnabled) {
            bManuallyDisabled = !b;
            afterChangeActiveObject(null);
        }
    }
}
