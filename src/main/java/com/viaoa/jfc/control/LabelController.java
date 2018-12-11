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
import com.viaoa.annotation.OAProperty;
import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/**
 * Controller for binding OA to JLabel.
 * @author vvia
 *
 */
public class LabelController extends OAJfcController {
    protected JLabel thisLabel;
    protected boolean bIsPassword;
    protected OASiblingHelper siblingHelper;

    
    /**
        Bind a label to a property for the active object in a Hub.
    */
    public LabelController(Hub hub, JLabel lab, String propertyPath) {
        super(hub, null, propertyPath, true, lab, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        init(lab);
    }

    /**
        Bind a label to the active object in a Hub.  Can be used to only
        display an icon for the active object.

    */
    public LabelController(Hub hub, JLabel lab) { 
        super(hub, null, null, lab, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        init(lab);
    }

    protected LabelController(Hub hub, JLabel lab, String propertyPath, HubChangeListener.Type type) { 
        super(hub, null, propertyPath, lab, type, false, true); // this will add hub listener
        init(lab);
    }
    protected LabelController(Hub hub, JLabel lab, String propertyPath, HubChangeListener.Type type, boolean bAoOnly) { 
        super(hub, null, propertyPath, bAoOnly, lab, type, false, true); // this will add hub listener
        init(lab);
    }
    
    /**
        Bind a label to a property for an object.
    */
    public LabelController(Object object, JLabel lab, String propertyPath) {
        super(null, object, propertyPath, lab, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        init(lab);
    }

    public void setPassword(boolean b) {
        this.bIsPassword = b;
        callUpdate();
    }
    public boolean isPassword() {
        return bIsPassword;
    }
    
    
    /**
        Bind a label.
    */
    protected void init(JLabel lab) {
        thisLabel = lab;
		// if (label != null) label.setFont(label.getFont().deriveFont(Font.BOLD));
        if (thisLabel != null) {
            thisLabel.setBorder(new CompoundBorder(new LineBorder(Color.lightGray, 1), new EmptyBorder(0,2,0,2)));
        }
        
        OALinkInfo[] lis = oaPropertyPath.getLinkInfos();
        
        boolean bUsed = (oaPropertyPath.getOACalculatedPropertyAnnotation() != null);
        
        if (bUsed) {
            siblingHelper = new OASiblingHelper(this.hub);
            siblingHelper.add(this.propertyPath);
        }
        
        // default to right align for numeric
        if (OAReflect.isNumber(getEndPropertyClass())) {
            thisLabel.setHorizontalAlignment(JLabel.RIGHT);
        }
        
        OAProperty pa = oaPropertyPath.getOAPropertyAnnotation();
        if (pa != null && pa.isHtml()) {
            setHtml(true);
        }
        else {
            OACalculatedProperty ca = oaPropertyPath.getOACalculatedPropertyAnnotation();
            if (ca != null && ca.isHtml()) setHtml(true);
        }
        
        callUpdate();
    }

    /**
        Used to display property value in JLabel.
    */
    public @Override void afterPropertyChange() {
    	callUpdate();
    	if (!(thisLabel instanceof OALabel)) return; 
        // label could be in a table
        OATable t = ((OALabel)thisLabel).getTable();
        if (t != null) {
            t.repaint();
        }
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return comp;
    }

    @Override
    public void callUpdate() {
        boolean bx = (siblingHelper != null) && OAThreadLocalDelegate.addSiblingHelper(siblingHelper);
        try {
            super.callUpdate();
            _update();
        }
        finally {
            if (bx) OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        }
    }
    
    protected void _update() {
        if (thisLabel == null) return;

        /*was:  20181004
        Object obj = hub.getAO();
        String text = null;
        if (obj != null || bIsHubCalc) {
            text = getValueAsString(obj, getFormat());
        }
        if (text == null) {
            text = getNullDescription();
            if (text == null) text = " ";
        }
        if (text.length() == 0) text = " "; // so that default size is not 0,0

        OATemplate temp = getTemplateForDisplay();
        if (temp != null) {
            Object objx = getHub().getAO();
            if (objx instanceof OAObject) {
                text = temp.process((OAObject) objx);
                if (text != null && text.indexOf('<') >=0 && text.toLowerCase().indexOf("<html>") < 0) text = "<html>" + text; 
            }
        }
        if (bIsPassword) text = "*****";
        thisLabel.setText(text);
        */
        
        if (bIsPassword) {
            if (OAString.isNotEmpty(thisLabel.getText())) thisLabel.setText("******");
        }
    }
    
}
