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
    private JLabel label;
    private boolean bIsPassword;
    private OASiblingHelper siblingHelper;
    
    /**
        Bind a label to a property for the active object in a Hub.
    */
    public LabelController(Hub hub, JLabel lab, String propertyName) {
        super(hub, propertyName, lab, HubChangeListener.Type.AoNotNull); // this will add hub listener
        init(lab);
    }

    /**
        Bind a label to the active object in a Hub.  Can be used to only
        display an icon for the active object.

    */
    public LabelController(Hub hub, JLabel lab) { 
        super(hub, lab, HubChangeListener.Type.AoNotNull); // this will add hub listener
        init(lab);
    }

    /**
        Bind a label to a property for an object.
    */
    public LabelController(Object object, JLabel lab, String propertyPath) {
        super(object, propertyPath, lab, HubChangeListener.Type.AoNotNull); // this will add hub listener
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
        label = lab;
		// if (label != null) label.setFont(label.getFont().deriveFont(Font.BOLD));
        if (label != null) {
        	label.setBorder(new CompoundBorder(new LineBorder(Color.lightGray, 1), new EmptyBorder(0,2,0,2)));
        }
        
        OALinkInfo[] lis = oaPropertyPath.getLinkInfos();
        
        boolean bUsed = (oaPropertyPath.getOACalculatedPropertyAnnotation() != null);
        
        if (bUsed) {
            siblingHelper = new OASiblingHelper(this.hub);
            siblingHelper.add(endPropertyName);
        }
        update();
    }

    /**
        Used to display property value in JLabel.
    */
    public @Override void afterPropertyChange() {
    	update();
        // label could be in a table
        OATable t = ((OALabel)label).getTable();
        if (t != null) {
            t.repaint();
        }
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return comp;
    }

    @Override
    public void update() {
        boolean bx = (siblingHelper != null) && OAThreadLocalDelegate.addSiblingHelper(siblingHelper);
        try {
            _update();
        }
        finally {
            if (bx) OAThreadLocalDelegate.removeSiblingHelper(siblingHelper);
        }
        super.update();
    }
    
    protected void _update() {
        if (label == null) return;

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
        
        if (bIsPassword) text = "*****";
        label.setText(text);
   
        super.update(label, obj);  // will update icon, font, colors, etc
    }
}
