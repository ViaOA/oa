/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jfc;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;
import com.viaoa.object.OAObject;

public class OARadioButton extends JRadioButton implements OATableComponent, OAJFCComponent {
    OARadioButtonController control;
    int columns;
    int width;
    OATable table;
    String heading;

    /**
        Create an OARadioButton that is not bound to a Hub.
    */  
    public OARadioButton() {
        control = new OARadioButtonController();
    }
    
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, Object value) {
        control = new OARadioButtonController(hub, propertyPath, value);
    }
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, boolean value) {
        control = new OARadioButtonController(hub, propertyPath, value);
    }
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, int value) {
        control = new OARadioButtonController(hub, propertyPath, new Integer(value));
    }
    /**
        Create an OARadioButton that is bound to an Object.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(OAObject obj, String propertyPath, Object objOn, Object objOff) {
        control = new OARadioButtonController(obj, propertyPath, objOn, objOff);
    }
    
    /**
        Create an OARadioButton that is bound to a Hub.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, Object onValue, Object offValue) {
        control = new OARadioButtonController(hub, propertyPath,onValue, offValue);
    }

    /**
        Create an OARadioButton that is bound to a Hub.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, boolean onValue, boolean offValue) {
        control = new OARadioButtonController(hub, propertyPath, new Boolean(onValue), new Boolean(offValue));
    }
    
    
    /**
        Create an OARadioButton that is bound to a Hub.
    */  
    public OARadioButton(Hub hub, String propertyPath) {
        control = new OARadioButtonController(hub, propertyPath);
    }

    @Override
    public JFCController getController() {
        return control;
    }
    
    /** 
        The value to use if the property is null.  
        Useful when using primitive types that might be set to null.
        <p>
        Example:<br>
        A boolean property can be true,false or null.  Might want to have a null value 
        treated as false.<br>
        setNullValue(false);
    */
    public void setNullValue(Object obj) {
        control.setNullValue(obj);
    }
    /** 
        The value to use if the property is null.  
        @see #setNullValue(Object)
    */
    public void setUseNull(boolean b) {
        control.setNullValue(b?control.valueOn:null);
    }

    boolean bRemoved;
    public void addNotify() {
        super.addNotify();
        if (bRemoved) {
            control.resetHubOrProperty();
            bRemoved = false;
        }
    }
    /* 2005/02/07 need to manually call close instead
    public void removeNotify() {
        super.removeNotify();
        bRemoved = true;
        control.close();
    }
    */
    public void close() {
        bRemoved = true;
        control.close();
    }
    
    // ----- OATableComponent Interface methods -----------------------
    public void setHub(Hub hub) {
        control.setHub(hub);
    }
    public Hub getHub() {
        return control.getHub();
    }
    public void setTable(OATable table) {
        this.table = table;
        if (table != null) table.resetColumn(this);
    }
    public OATable getTable() {
        return table;
    }

    public int getColumns() {
        return columns;            
    }
    public void setColumns(int x) {
        columns = x;
        this.width = OATable.getCharWidth(this,getFont(),x);
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),width);
        Dimension d = getPreferredSize();
        d.width = this.width;
        setPreferredSize(d);
    }
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) table.resetColumn(this);
    }
    public String getPropertyPath() {
        return control.getPropertyPath();
    }
    public String getTableHeading() { //zzzzz
        return heading;   
    }
    public void setTableHeading(String heading) { 
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    /** value property will be set to when selected */
    public Object getValue() {
        return control.valueOn;
    }
    public void setValue(Object value) {
        control.valueOn = value;
        control.resetHubOrProperty();
    }
    
    public JComponent getComponent() {
        return this;   
    }
// not done 
//    OARadioButtonTableCellEditor tableCellEditor;

    public TableCellEditor getTableCellEditor() {
/**** qqqqqqqqqqqqqqqqqq not done
        if (tableCellEditor == null) {
            tableCellEditor = new OARadioButtonTableCellEditor(this);
            this.setHorizontalAlignment(JLabel.CENTER);
            this.setOpaque(true);
            this.setBackground( UIManager.getColor("Table.focusCellBackground") );
        }
        return tableCellEditor;
***/
        return null;
    }

    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderer;
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }
    
    @Override
	public String getFormat() {
		return control.getFormat();
	}	

    /**
     * Other Hub/Property used to determine if component is enabled.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setEnabled(Hub hub) {
        control.getEnabledController().add(hub);
    }
    public void setEnabled(Hub hub, String prop) {
        control.getEnabledController().add(hub, prop);
    }
    public void setEnabled(Hub hub, String prop, Object compareValue) {
        control.getEnabledController().add(hub, prop, compareValue);
    }
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }
    
    /** removed, to "not use" the enabledController, need to call it directly - since it has 2 params now, and will need 
     * to be turned on and off   
    @Override
    public void setEnabled(boolean b) {
        if (control != null) {
            b = control.getEnabledController().directSetEnabledCalled(b);
        }
        super.setEnabled(b);
    }
    */
    
    /**
     * Other Hub/Property used to determine if component is visible.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setVisible(Hub hub) {
        control.getVisibleController().add(hub);
    }    
    public void setVisible(Hub hub, String prop) {
        control.getVisibleController().add(hub, prop);
    }    
    public void setVisible(Hub hub, String prop, Object compareValue) {
        control.getVisibleController().add(hub, prop, compareValue);
    }    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }

    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    protected String isValid(Object object, Object value) {
        return null;
    }
    

    
    
    
    class OARadioButtonController extends ToggleButtonController {
        public OARadioButtonController() {
            super(OARadioButton.this);
        }    
        public OARadioButtonController(Hub hub, String propertyPath) {
            super(hub, OARadioButton.this, propertyPath);
        }
        public OARadioButtonController(Hub hub, String propertyPath, Object onValue, Object offValue) {
            super(hub, OARadioButton.this, propertyPath, onValue, offValue);
        }
        public OARadioButtonController(Hub hub, String propertyPath, Object value) {
            super(hub, OARadioButton.this, propertyPath, value);
        }
        public OARadioButtonController(OAObject hubObject, String propertyPath) {
            super(hubObject, OARadioButton.this, propertyPath);
        }        
                
        public OARadioButtonController(OAObject hubObject, String propertyPath, Object onValue, Object offValue) {
            super(hubObject, OARadioButton.this, propertyPath, onValue, offValue);
        }        
        public OARadioButtonController(Hub hub, Hub hubSelect) {
            super(hub, hubSelect, OARadioButton.this);
        }
        
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OARadioButton.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OARadioButton.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OARadioButton.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
    }

}


