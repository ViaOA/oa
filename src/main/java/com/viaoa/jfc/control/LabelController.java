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

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import com.viaoa.annotation.OACalculatedProperty;
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
    private boolean bIsPassword;
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

    public void setPassword(boolean b) {
        this.bIsPassword = b;
        update();
    }
    public boolean isPassword() {
        return bIsPassword;
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
        Hub h = getActualHub();
        if (h != null && h != hub) {
            getEnabledController().add(h);
        }
        
        String spp = getPropertyPath();
        if (OAString.isNotEmpty(spp)) {
            OAPropertyPath pp = new OAPropertyPath(this.hub.getObjectClass(), spp);
            OALinkInfo[] lis = pp.getLinkInfos();
            
            boolean bUsed = false;
            if (lis != null && lis.length > 1) bUsed = true;
            else if (pp.getOACalculatedPropertyAnnotation() != null) bUsed = true;
            
            if (bUsed) {
                siblingHelper = new OASiblingHelper(this.hub);
                siblingHelper.add(spp);
            }
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
        Component comp = super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return comp;
    }

    
    private OASiblingHelper siblingHelper;
    @Override
    protected void update() {
        boolean bx = (siblingHelper != null) && OAThreadLocalDelegate.addSiblingHelper(siblingHelper);
        try {
            _update();
        }
        finally {
            if (bx) OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        }
    }
    
    protected void _update() {
        if (label == null) return;

        if (getActualHub() != null) {
            Object obj = getActualHub().getActiveObject();
            String text = null;
            if (obj != null || bIsHubCalc) {
                if (getPropertyPath() != null) {
                    text = getPropertyPathValueAsString(obj, getFormat());
                    // Object value = getPropertyPathValueAsString(obj, getFormat());
                    // if (value == null || value instanceof OANullObject) text = null;
                    //else text = OAConv.toString(value, getFormat());
                    
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
            
            if (bIsPassword) text = "*****";
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



