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

package com.viaoa.jfc;

import java.lang.reflect.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;


/** 
    JRadioButton subclass that binds to object/Hub.
    <p>
    Example:<br>
    <pre>
        OARadioButton rad = new OARadioButton(hubEmp, "dept.manager.fullTime", true);
        rad.setText("Full Time Manager?");
        rad.setHorizontalAlignment(JLabel.CENTER);
        rad.setColumns(14);
        rad.setTableHeading("full time manager?");
    </pre>    
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OAToggleButton
*/
public class OARadioButton_ extends JRadioButton implements OAJFCComponent, OATableComponent {
    Hub2ToggleButton hcb;
    int columns;
    int width;
    OATable table;
    String heading;

    /**
        Create an OARadioButton that is not bound to a Hub.
    */  
    public OARadioButton() {
        hcb = new Hub2ToggleButton(this);
    }
    
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, Object value) {
        hcb = new Hub2ToggleButton(hub,this,propertyPath,value);
    }
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, boolean value) {
        hcb = new Hub2ToggleButton(hub,this,propertyPath,value);
    }
    /**
        Create an OARadioButton that is bound to a Hub.
        @param value is value to set property to when radio is selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, int value) {
        hcb = new Hub2ToggleButton(hub,this,propertyPath,new Integer(value));
    }
    /**
        Create an OARadioButton that is bound to an Object.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(Object obj, String propertyPath, Object objOn, Object objOff) {
        hcb = new Hub2ToggleButton(obj,this,propertyPath, objOn, objOff);
    }
    
    /**
        Create an OARadioButton that is bound to a Hub.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, Object onValue, Object offValue) {
        hcb = new Hub2ToggleButton(hub,this,propertyPath,onValue, offValue);
    }

    /**
        Create an OARadioButton that is bound to a Hub.
        @param objOn value to set property to when radio is selected.
        @param objOff value to set property to when radio is not selected.
    */  
    public OARadioButton(Hub hub, String propertyPath, boolean onValue, boolean offValue) {
        hcb = new Hub2ToggleButton(hub,this,propertyPath, new Boolean(onValue), new Boolean(offValue));
    }
    
    
    /**
        Create an OARadioButton that is bound to a Hub.
    */  
    public OARadioButton(Hub hub, String propertyPath) {
        hcb = new Hub2ToggleButton(hub,this,propertyPath);
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
        hcb.setNullValue(obj);
    }
    /** 
        The value to use if the property is null.  
        @see #setNullValue(Object)
    */
    public void setUseNull(boolean b) {
        hcb.setNullValue(b?hcb.valueOn:null);
    }

    boolean bRemoved;
    public void addNotify() {
        super.addNotify();
        if (bRemoved) {
            hcb.resetHubOrProperty();
            bRemoved = false;
        }
        hcb.updateEnabled();
    }
    /* 2005/02/07 need to manually call close instead
    public void removeNotify() {
        super.removeNotify();
        bRemoved = true;
        hcb.close();
    }
    */
    public void close() {
        bRemoved = true;
        hcb.close();
    }
    
    // ----- OATableComponent Interface methods -----------------------
    public void setHub(Hub hub) {
        hcb.setHub(hub);
    }
    public Hub getHub() {
        return hcb.getHub();
    }
    public void setTable(OATable table) {
        this.table = table;
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),getPropertyPath());
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
        hcb.setPropertyPath(path);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),path);
    }
    public String getPropertyPath() {
        return hcb.getPropertyPath();
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
        return hcb.valueOn;
    }
    public void setValue(Object value) {
        hcb.valueOn = value;
        hcb.resetHubOrProperty();
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

    // 2004/08/04
    /**
        Used to manually enable/disable.
    */
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (hcb != null) {
            hcb.setEnabled(b);
        }
    }
    public void setReadOnly(boolean b) {
        hcb.setReadOnly(b);
    }
    public boolean getReadOnly() {
        return hcb.getReadOnly();
    }

    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }
    
    @Override
	public String getFormat() {
		return hcb.getFormat();
	}	

}


