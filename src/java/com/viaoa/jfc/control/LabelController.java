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

import javax.swing.*;
import javax.swing.border.*;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;


/**
 * Controller for binding OA to JLabel.
 * @author vvia
 *
 */
public class LabelController extends JFCController {
    private JLabel label;
    
    /**
        Create an unbound label.
    */
    public LabelController(JLabel lab) {
        super(lab);
        init(lab);
    }
    
    /**
        Bind a label to a property for the active object in a Hub.
    */
    public LabelController(Hub hub, JLabel lab, String propertyPath) {
        super(hub, propertyPath, lab); // this will add hub listener
        init(lab);
    }

    /**
        Bind a label to the active object in a Hub.  Can be used to only
        display an icon for the active object.
        @see HubGuiAdapter#setImageProperty
    */
    public LabelController(Hub hub, JLabel lab) { 
        super(hub, lab); // this will add hub listener
        init(lab);
    }

    /**
        Bind a label to a property for an object.
    */
    public LabelController(Object object, JLabel lab, String propertyPath) {
        super(object, propertyPath, lab); // this will add hub listener
        init(lab);
    }

    /**
        Bind a label.
    */
    protected void init(JLabel lab) {
        if (hub != null) setEnabled(hub, null);
        label = lab;
		// if (label != null) label.setFont(label.getFont().deriveFont(Font.BOLD));
        if (label != null) {
        	label.setBorder(new CompoundBorder(new LineBorder(Color.lightGray, 1), new EmptyBorder(0,2,0,2)));
        }
        if (getActualHub() != null) {
            getEnabledController().add(getActualHub());
        }
        update();
    }

    
    /**
        Used to display property value in JLabel.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        //was: if (this.propertyName == null) return;
        String prop = e.getPropertyName();
        if (prop == null) return;
        boolean b = prop.equalsIgnoreCase(this.getHubListenerPropertyName());
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
            	update();
            }
        }
        else {
            if (label instanceof OALabel) {
                // label could be in a table
                OATable t = ((OALabel)label).getTable();
                if (t != null) {
                    if (e.getPropertyName().equalsIgnoreCase(this.getHubListenerPropertyName()) || e.getPropertyName().equalsIgnoreCase(getImageProperty()) ) {
                        t.repaint();
                    }
                }
            }
        }
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return label;
    }


    @Override
    protected void update() {
        if (label == null) return;

        if (getActualHub() != null) {
            Object obj = getActualHub().getActiveObject();
            String text = null;
            if (obj != null) {
                if (getPropertyPath() != null) {
                    Object value = getPropertyPathValue(obj);
                    if (value == null || value instanceof OANullObject) text = null;
                    else text = OAConv.toString(value, getFormat());
                    
                    if (text != null && obj instanceof OAObject) {
                        if (isPropertyPathValueNull(obj)) text = null;
                        //was:String ss = getPropertyName();
                        //was:if (OAObjectReflectDelegate.getPrimitiveNull((OAObject)obj, ss) ) text = null;
                    }
                }
                else text = null;
            }
            if (text == null) {
                text = getNullDescription();
                if (text == null) text = " ";
            }
            if (text.length() == 0) text = " "; // so that default size is not 0,0
            label.setText(text);
        }   
        
        Object obj = null;
        if (getHub() != null) {
            obj = getHub().getAO();
        }
        //super.updateVisible();
        super.update(label, obj);  // will update icon, font, colors, etc
    }

}



