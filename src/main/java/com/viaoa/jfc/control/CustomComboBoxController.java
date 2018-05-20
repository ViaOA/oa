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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.JComboBox.*;

import com.viaoa.hub.*;
import com.viaoa.object.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;


/**
 * Functionality for binding custom JComboBox to OA.
 * @author vvia
 */
public class CustomComboBoxController extends JFCController {
    JComboBox comboBox;
    public boolean bDisplayPropertyOnly; // 2007/05/25 used by OATreeComboBox so that setSelectedItem() does not try to update property
    protected boolean bTypeEditProperty; // if true, then this will edit hub.property, else it updates hub.ao
    
    public CustomComboBoxController(Hub hub, JComboBox cb, String propertyPath, boolean bTypeEditProperty) {
        super(hub, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    public CustomComboBoxController(Object obj, JComboBox cb, String propertyPath, boolean bTypeEditProperty) {
        super(obj, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    protected void create(JComboBox cb) {
        this.comboBox = cb;

        // 20110116 so all combos will have a renderer for calculating width
        comboBox.setRenderer(new MyListCellRenderer());

        Hub h = getHub();
        if (h != null) {
            if (comboBox != null) {
                comboBox.setModel(new MyComboBoxModel());
                // was: comboBox.setRenderer(new MyListCellRenderer());

                HubEvent e = new HubEvent(getHub(),getHub().getActiveObject());
                this.afterChangeActiveObject(e);  // this will set selectedPos in JComboBox
            }
        }
        createEnabledController();
    }
    
    protected void createEnabledController() {
        Hub h = getHub();
        if (h == null) return;

        if (bTypeEditProperty) {
            getEnabledController().add(h, null, OANotNullObject.instance); //was: OAAnyValueObject.instance); // so that Hub.isValid will be the only check
        }
        else {
            Hub hx = HubLinkDelegate.getHubWithLink(h, true);
            if (hx != null) {
                getEnabledController().add(h.getLinkHub(), null, OANotNullObject.instance); // so that it will verify that hub is valid
            }
            if (h.getMasterHub() != null) {
                getEnabledController().add(h.getMasterHub(), null, OANotNullObject.instance);//was: OAAnyValueObject.instance); // so that Hub.isValid will be the only check
            }
        }
    }

    protected void resetHubOrProperty() {  // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (comboBox != null) create(comboBox);
    }

    
    /**
        Changing active object in Hub will set the select item in ComboBox.
        ComboBox is enabled based on if Hub is valid.
    */
    public @Override void afterChangeActiveObject(HubEvent evt) {
        OAObject oaObject = (OAObject) getActualHub().getActiveObject(); // use hub instead of actualHub

        if (comboBox == null) return;
        comboBox.hidePopup();  // this is for CustomComboBoxes that will change AO, so that it will auto close
        
        Object value;
        if (oaObject == null) value = null;
        else value = getPropertyPathValue(oaObject);
        // was: else value = oaObject.getProperty(getPropertyName());
        
        if (evt != null) {
            comboBox.setSelectedItem(value);
        }
        update();
        comboBox.repaint();
    }

    /**
        Used to change selected item if property name matches property used by ComboBox.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        if (comboBox != null && e.getPropertyName().equalsIgnoreCase(getHubListenerPropertyName()) ) {
            afterChangeActiveObject(e);
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
    

    /**
        Called when item is selected in ComboBox to update the property for active object
        in the Hub.
    */
    public void updatePropertyValue(Object value) {
        if (bDisplayPropertyOnly) return; // 2007/05/25

    	Hub h = getHub();
        if (getEnableUndo() && h != null) {
            OAObject obj = (OAObject) h.getAO();
	        if (obj != null) {
                Object prev = getPropertyPathValue(obj);
	            // was; Object prev = obj.getProperty(getPropertyName());
	            if (value != prev && (value == null || !value.equals(prev))) {
	                if (getEnableUndo()) {
	                    OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, obj, getPropertyPathFromActualHub(), prev, value) );
	                }
                    setPropertyPathValue(obj, value);
	                // was: obj.setProperty(getPropertyName(), value);
	            }
	        }
	    }
    }

    
    /** 2006/12/11 copied from Hub2ComboBox
    Default cell renderer.
	*/
	class MyListCellRenderer extends JLabel implements ListCellRenderer {
	    public MyListCellRenderer() {
	        setOpaque(true);
	    }
	
	    /** will either call OAComboBox.getRenderer() or Hub2ComboBox.getRenderer() */
	    public Component getListCellRendererComponent(JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
	        Component comp = CustomComboBoxController.this.getRenderer(this, list, value, index, isSelected, cellHasFocus);
	        return comp;
	    }
	}
 
    public Component getRenderer(Component renderer, JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        String s;
        if (value == null || value instanceof OANullObject) s = "   ";
        else if (value instanceof OANullObject) {
        	s = nullDescription;
        }
        else if (value instanceof String) {  // from prototype setting
        	s = (String) value;
        }
        else {
            Object obj = getPropertyPathValue(value);
            // was: Object obj = OAReflect.getPropertyValue(value, getGetMethods());
            s = OAConv.toString(obj, getFormat());
            if (s.length() == 0) s = " ";  // if length == 0 then Jlist wont show any
        }
        if (renderer instanceof JLabel) {
            JLabel lbl = (JLabel) renderer;

            if (!isSelected) {
            	lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }

            lbl.setText(s);
            update(lbl, value);

            if (isSelected) {
                lbl.setBackground(list.getSelectionBackground());
                lbl.setForeground(list.getSelectionForeground());
            } 
        }
        return renderer;
    }
    
    // 2006/12/11
    //==============================================================================
    // note: these need to be ran in the AWT Thread, use SwingUtilities.invokeLater() to call these
    class MyComboBoxModel extends DefaultListModel implements ComboBoxModel {
         OANullObject empty = OANullObject.instance;
         public synchronized void setSelectedItem(Object obj) {
             if (comboBox instanceof OACustomComboBox) { // 20120508 hake to make sure that propertyChange does not happen more then once
                 if ( ((OACustomComboBox) comboBox).bSetting) return;
             }
        	 updatePropertyValue(obj);
         }
         public Object getSelectedItem() {
            Object obj = getHub().getActiveObject();
            if (obj == null) obj = empty;
            return obj;
         }
         public Object getElementAt(int index) {
            if (getHub() == null) return empty;
            Object obj = getHub().elementAt(index);
            if (obj == null) obj = empty;
            return obj;
         }
         public int getSize() {
            return 1;
         }
    }
    
}



