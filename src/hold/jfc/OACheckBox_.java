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

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;


/** 
    JCheckBox subclass that binds to a property for an Object or Hub.
    Button is automatically enabled based on active object in Hub.  
    On/Off values can be set to bind to a proeperty in the active object.
    Also, can be used to add/remove an objects to/from another Hub.
    <p>
    OACheckBox can used as a column in an OATable, where custom renderer and editor are
    automatically supplied.
    <p>
    Example:<br>
    <pre>
        Hub hubEmployee = new Hub(Employee.class);
        hubEmployee.select();
        OACheckBox chk = new OACheckBox(hubEmp, "dept.manager.fullTime");
        chk.setText("Full Time Manager?");
        
        // use checkbox in column of an OATable to select Employees 
        // for a second list.
        Hub hubEmployeeSelect = new Hub(Employee.class);
        OACheckBox chk = new OACheckBox(hubEmployee, hubEmployeeSelect);
        chkBox.setTableHeading("Selected");  // if used in a OATable column
        chkBox.setColumns(8);  // column width if used in a OATable column
    </pre>    
    @see Hub2ToggleButton
    @see OACheckBoxTableCellEditor
*/
public class OACheckBox_ extends JCheckBox implements OAJFCComponent, OATableComponent {
    Hub2ToggleButton hcb;
    int columns;
    int width;
    OATable table;
    String heading;

    /**
        Create an unbound CheckBox.
    */
    public OACheckBox() {
        createHub2ToggleButton(null, null, null, null);
    }
    
    private void createHub2ToggleButton(Hub hub, String propertyPath, Object onValue, Object offValue) {
        if (onValue == null) onValue = new Boolean(true);
        if (offValue == null) offValue = new Boolean(false);
        hcb = new Hub2ToggleButton(hub,this, propertyPath, onValue, offValue) {
            public void itemStateChanged(ItemEvent evt) {
                if (!onItemStateChanged(evt)) super.itemStateChanged(evt);
            }
        };
    }
    
    
    /**
        Create CheckBox that is bound to a property for the active object in a Hub.
        @param cols is width of list using character width size.
    */
    public OACheckBox(Hub hub, String propertyPath, int cols) {
        this(hub,propertyPath);
        setColumns(cols);
    }

    /**
        Create CheckBox that is bound to a property for the active object in a Hub.
    */
    public OACheckBox(Hub hub, String propertyPath) {
        createHub2ToggleButton(hub, propertyPath, null, null);
    }
    /**
        Create CheckBox that is bound to a property for the active object in a Hub.
        @param onValue value if value is considered true.
        @param offValue value if value is considered false.
    */
    public OACheckBox(Hub hub, String propertyPath, Object onValue, Object offValue) {
        createHub2ToggleButton(hub, propertyPath, onValue, offValue);
    }

    /**
        Bind a button to have it add/remove objects with another Hub.  
        @param hub that has active object that is added/removed from hubSelect
        @param hubSelect the active object from hub will be added/removed from this Hub.
    */
    public OACheckBox(Hub hub, Hub hubSelect) {
        hcb = new Hub2ToggleButton(hub, hubSelect, this) {
            public void itemStateChanged(ItemEvent evt) {
                if (!onItemStateChanged(evt)) super.itemStateChanged(evt);
            }
        };
    }

    private boolean onItemStateChanged(ItemEvent evt) {
        if (hcb == null) return false;
        if (hcb.isChanging()) return false;
        if (!OACheckBox.this.confirmChange(evt.getStateChange()==ItemEvent.SELECTED)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    hcb.afterChangeActiveObject(null);
                }
            });
            return true;
        }
        return false;
    }
    
    
    /**
        Create CheckBox that is bound to a property for an object.
    */
    public OACheckBox(OAObject oaObject, String propertyPath) {
        hcb = new Hub2ToggleButton(oaObject,this,propertyPath) {
            public void itemStateChanged(ItemEvent evt) {
                if (!onItemStateChanged(evt)) super.itemStateChanged(evt);
            }
        };
    }
    /**
        Create CheckBox that is bound to a property for an object.
        @param onValue value if value is considered true.
        @param offValue value if value is considered false.
    */
    public OACheckBox(OAObject oaObject, String propertyPath, Object onValue, Object offValue) {
        hcb = new Hub2ToggleButton(oaObject,this,propertyPath, onValue, offValue) {
            public void itemStateChanged(ItemEvent evt) {
                if (!onItemStateChanged(evt)) super.itemStateChanged(evt);
            }
        };
    }

    public Hub2ToggleButton getHub2ToggleButton() {
    	return hcb;
    }
    
    /**
        Bind a button to have it add/remove objects a Hub.  
        @param hubSelect the active object from hub will be added/removed from this Hub.
    */
    public void setSelectHub(Hub hubSelect) {
        hcb.setSelectHub(hubSelect);
    }
    /**
        Returns the Hub that maintains the selected objects.
        @see setSelectHub(Hub)
    */
    public Hub getSelectHub() {
        return hcb.getSelectHub();
    }

    
    public void setXORValue(int xor) {
        hcb.setXORValue(xor);
    }

    public int getXORValue() {
        return hcb.getXORValue();
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

    /** value property will be set to when selected. Default: TRUE */
    public Object getOnValue() {
        return hcb.valueOn;
    }
    public void setOnValue(Object value) {
        hcb.valueOn = value;
        hcb.resetHubOrProperty();
    }
    /** value property will be set to when deselected. Default: FALSE */
    public Object getOffValue() {
        return hcb.valueOff;
    }
    public void setOffValue(Object value) {
        hcb.valueOff = value;
        hcb.resetHubOrProperty();
    }
    
    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }



    /**
        Width of component, based on average width of the font's character.
    */
    public int getColumns() {
        return columns;            
    }
    /**
        Width of component, based on average width of the font's character.
    */
    public void setColumns(int x) {
        columns = x;
        this.width = OATable.getCharWidth(this,getFont(),x);
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),width);
        Dimension d = getPreferredSize();
        d.width = this.width;
        setPreferredSize(d);
    }

    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path) {
        hcb.setPropertyPath(path);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),path);
    }
    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getPropertyPath() {
        return hcb.getPropertyPath();
    }

    
    /**
        Column heading when this component is used as a column in an OATable.
        @see OATable#add
    */
    public String getTableHeading() {
        return heading;   
    }
    /**
        Column heading when this component is used as a column in an OATable.
        @see OATable#add
    */
    public void setTableHeading(String heading) { //zzzzz
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    
    public JComponent getComponent() {
        return this;   
    }
    OACheckBoxTableCellEditor tableCellEditor;

    /**
        Editor used when this component is used as a column in an OATable.
    */
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OACheckBoxTableCellEditor(this);
            this.setHorizontalAlignment(JLabel.CENTER);
            this.setOpaque(true);
            this.setBackground( UIManager.getColor("Table.selectionBackground") );
            // this.setBackground( UIManager.getColor("Table.focusCellBackground") );
            this.setBorderPainted(true);
            this.setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
        }
        return tableCellEditor;
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
        close();
    }
    */
    public void close() {
        bRemoved = true;
        hcb.close();
    }

    JCheckBox chkRenderer;
    /** 
        Called by getTableCellRendererComponent to display this component.
    */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (chkRenderer == null) {
            chkRenderer = new JCheckBox();
            chkRenderer.setOpaque(true);
            chkRenderer.setHorizontalAlignment(JLabel.CENTER);
        }
        boolean tf = false;
        Object obj = hcb.getHub().elementAt(row);
        if (hcb.hubSelect != null) {
            // Object obj = hcb.getActualHub().elementAt(row);
            if (obj != null && hcb.hubSelect.getObject(obj) != null) tf = true;
        }
        else {
        	if (value != null && value instanceof Boolean) tf = ((Boolean)value).booleanValue();
        }

        if (!isSelected && !hasFocus) {
            chkRenderer.setForeground( UIManager.getColor(table.getForeground()) );
            chkRenderer.setBackground( UIManager.getColor(table.getBackground()) );
        }
        
        hcb.updateComponent(chkRenderer, obj, null);
        
        chkRenderer.setSelected(tf);

        if (hasFocus) {
            chkRenderer.setBorderPainted(true);
            chkRenderer.setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
        }
        else chkRenderer.setBorder(null);

        if (isSelected || hasFocus) {
            chkRenderer.setForeground( UIManager.getColor("Table.selectionForeground") );
            chkRenderer.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        
        return chkRenderer;
    }

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

    
    // 200804/27 Hack: to work with OATable, to have mouse click check the box.
    // If you have a chk in a column and then click another row in same column, then the mouse pressed is not sent to checkbox
    private boolean bMousePressed;
    private boolean bSelected;
    @Override
    protected void processMouseEvent(MouseEvent e) {
    	super.processMouseEvent(e);
    	if (getTable() == null) return;
    	int id = e.getID();
    	if (id == MouseEvent.MOUSE_PRESSED) {
    		bSelected = isSelected();	
    		bMousePressed = true;
    	}
    	else if (id == MouseEvent.MOUSE_RELEASED) {
    		if (bMousePressed && (bSelected == isSelected())) {
    			setSelected(!bSelected);
    		}
    		bMousePressed = false;
    	}
    }
    public String getFormat() {
    	return null;
    }

    /*
    Popup message used to confirm button click before running code.
    */
    public void setConfirmMessage(String msg) {
        hcb.setConfirmMessage(msg);
    }
    /**
        Popup message used to confirm button click before running code.
    */
    public String getConfirmMessage() {
        return hcb.getConfirmMessage();
    }

    /** returns true if command is allowed */
    protected boolean confirmChange(boolean bSelected) {
        return true;
    }

    
}









