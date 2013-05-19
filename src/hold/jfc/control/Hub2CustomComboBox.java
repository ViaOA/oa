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
    Class for binding JComboBox with custom popup to Object or Hub. 
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class Hub2CustomComboBox extends Hub2Gui {
    JComboBox comboBox;
    public boolean bDisplayPropertyOnly; // 2007/05/25 used to OATreeComboBox so that setSelectedItem() does not try to update property
    
    public Hub2CustomComboBox(Hub hub, JComboBox cb, String propertyPath) {
        super(hub, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    public Hub2CustomComboBox(Object obj, JComboBox cb, String propertyPath) {
        super(obj, propertyPath, cb); // this will add hub listener
        create(cb);
    }

    protected void create(JComboBox cb) {
        this.comboBox = cb;

        Hub h = getHub();
        if (h != null) {
            if (comboBox != null) {
                comboBox.setModel(new MyComboBoxModel());
                comboBox.setRenderer(new MyListCellRenderer());

                HubEvent e = new HubEvent(getHub(),getHub().getActiveObject());
                this.afterChangeActiveObject(e);  // this will set selectedPos in JComboBox
            }
        }
    }

    protected void resetHubOrProperty() {  // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (comboBox != null) create(comboBox);
    }

    
    @Override
    public void setReadOnly(boolean b) {
        super.setReadOnly(b);
        setEnabled(!b);
        afterChangeActiveObject(null);
    }
    
    /**
        Changing active object in Hub will set the select item in ComboBox.
        ComboBox is enabled based on if Hub is valid.
    */
    public @Override void afterChangeActiveObject(HubEvent evt) {
        OAObject oaObject = (OAObject) getHub().getActiveObject(); // use hub instead of actualHub

        if (comboBox == null) return;
        comboBox.hidePopup();  // this is for CustomComboBoxes that will change AO, so that it will auto close
        
        Object value;
        if (oaObject == null) value = null;
        else value = oaObject.getProperty(getPropertyName());
        
        if (evt != null) {
            comboBox.setSelectedItem(value);
        }
         
        boolean b = true;
        if (b) {
            Hub h = getHub();
            if (h == null) b = false;
            else {
            	if (!HubDelegate.isValid(h)) b = false;
            	else b = h.getAO() != null;
            }
        }
        if (b) {
            b = isParentEnabled(comboBox);
            if (b && comboBox instanceof OACustomComboBox) {
//                b = ((OACustomComboBox)comboBox).isEnabled(oaObject);
            }
        }
        this.setInternalEnabled(b);
    }

    /**
        Used to change selected item if property name matches property used by ComboBox.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        if (comboBox != null && e.getPropertyName().equalsIgnoreCase(getPropertyName()) ) {
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
        if (h != null) {
            OAObject obj = (OAObject) h.getAO();
	        if (obj != null) {
	            Object prev = obj.getProperty(getPropertyName());
	            if (value != prev && (value == null || !value.equals(prev))) {
	                OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, obj, getPropertyName(), prev, value) );
	                obj.setProperty(getPropertyName(), value);
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
	        Component comp = Hub2CustomComboBox.this.getRenderer(this, list, value, index, isSelected, cellHasFocus);
	        return comp;
	    }
	}
 
    public Component getRenderer(Component renderer, JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        String s;
        if (value == null) s = "   ";
        else if (value instanceof OANullObject) {
        	s = nullDescription;
        }
        else if (value instanceof String) {  // from prototype setting
        	s = (String) value;
        }
        else {
            Object obj = OAReflect.getPropertyValue(value, getGetMethods());
            s = OAConv.toString(obj, getFormat());
            if (s.length() == 0) s = " ";  // if length == 0 then Jlist wont show any
        }
        if (renderer instanceof JLabel) {
            JLabel lbl = (JLabel) renderer;

            if (!isSelected) {
            	lbl.setBackground(list.getBackground());
                lbl.setForeground(list.getForeground());
            }
            
            updateComponent(lbl, value, s);

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
         OANullObject empty = OANullObject.nullObject;
         public synchronized void setSelectedItem(Object obj) {
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



