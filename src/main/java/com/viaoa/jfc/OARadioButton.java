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
package com.viaoa.jfc;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;
import com.viaoa.object.OAObject;

public class OARadioButton extends JRadioButton implements OATableComponent, OAJfcComponent {
    OARadioButtonController control;
    int columns;
    int width;
    OATable table;
    String heading;

/* 20150927 removed, not sure why this was here    
    static {
        Color c = UIManager.getColor("RadioButton.foreground");
        if (c == null) c = Color.black;
        
        UIManager.put("RadioButton.disabledText", c);
    }
*/    
    /**
        Create an OARadioButton that is not bound to a Hub.
    */  
    public OARadioButton() {
        control = new OARadioButtonController();
        initialize();
    }
    
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, Object value) {
        control = new OARadioButtonController(hub, propertyPath, value);
        initialize();
    }
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, boolean value) {
        control = new OARadioButtonController(hub, propertyPath, value);
        initialize();
    }
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, int value) {
        control = new OARadioButtonController(hub, propertyPath, new Integer(value));
        initialize();
    }
    /**
        Create an OARadioButton that is bound to an Object.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(OAObject obj, String propertyPath, Object objOn, Object objOff) {
        control = new OARadioButtonController(obj, propertyPath, objOn, objOff);
        initialize();
    }
    
    /**
        Create an OARadioButton that is bound to a Hub.
        @param onValue value to set property to when radio is selected.
        @param offValue value to set property to when radio is not selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, Object onValue, Object offValue) {
        control = new OARadioButtonController(hub, propertyPath,onValue, offValue);
        initialize();
    }

    /**
        Create an OARadioButton that is bound to a Hub.
        @param onValue value to set property to when radio is selected.
        @param offValue value to set property to when radio is not selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, boolean onValue, boolean offValue) {
        control = new OARadioButtonController(hub, propertyPath, new Boolean(onValue), new Boolean(offValue));
        initialize();
    }
    
    
    /**
        Create an OARadioButton that is bound to a Hub.
    */  
    public OARadioButton(Hub hub, String propertyPath) {
        control = new OARadioButtonController(hub, propertyPath);
        initialize();
    }

    @Override
    public void initialize() {
    }
    
    
    @Override
    public OAJfcController getController() {
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
        /*
        if (bRemoved) {
            control.resetHubOrProperty();
            bRemoved = false;
        }
        */
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
    /*
    public void setHub(Hub hub) {
        control.setHub(hub);
    }
    */
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
    /*
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) table.resetColumn(this);
    }
    */
    @Override
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
        // control.resetHubOrProperty();
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
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
        Object obj = ((OATable) table).getObjectAt(row, col);
        getToolTipText(obj, row, defaultValue);
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
        Object obj = ((OATable) table).getObjectAt(row, column);
        customizeRenderer(lbl, obj, value, isSelected, hasFocus, row, wasChanged, wasMouseOver);
    }
    
    @Override
	public String getFormat() {
		return control.getFormat();
	}	
    
    public void addEnabledCheck(Hub hub) {
        control.getEnabledChangeListener().add(hub);
    }
    public void addEnabledCheck(Hub hub, String propPath) {
        control.getEnabledChangeListener().add(hub, propPath);
    }
    public void addEnabledCheck(Hub hub, String propPath, Object compareValue) {
        control.getEnabledChangeListener().add(hub, propPath, compareValue);
    }
    protected boolean isEnabled(boolean defaultValue) {
        return defaultValue;
    }
    public void addVisibleCheck(Hub hub) {
        control.getVisibleChangeListener().add(hub);
    }
    public void addVisibleCheck(Hub hub, String propPath) {
        control.getVisibleChangeListener().add(hub, propPath);
    }
    public void addVisibleCheck(Hub hub, String propPath, Object compareValue) {
        control.getVisibleChangeListener().add(hub, propPath, compareValue);
    }
    protected boolean isVisible(boolean defaultValue) {
        return defaultValue;
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
        public String isValid(Object object, Object value) {
            String msg = OARadioButton.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
    }


    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    /*
    public JLabel getLabel() {
        return getController().getLabel();
    }
    */
}


