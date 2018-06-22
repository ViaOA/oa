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

import java.lang.reflect.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.util.OANotNullObject;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OACheckBox extends JCheckBox implements OATableComponent, OAJFCComponent {
    OACheckBoxController control;
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
    public OACheckBox(String txt) {
        createHub2ToggleButton(null, null, null, null);
        setText(txt);
    }

    public void bind(Hub hub, String propertyPath) {
        control = new OACheckBoxController(hub, propertyPath, true, false);
    }
    
    private void createHub2ToggleButton(Hub hub, String propertyPath, Object onValue, Object offValue) {
        if (onValue == null) onValue = new Boolean(true);
        if (offValue == null) offValue = new Boolean(false);
        control = new OACheckBoxController(hub, propertyPath, onValue, offValue);
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
        control = new OACheckBoxController(hub, hubSelect);
    }

    private boolean onItemStateChanged(ItemEvent evt) {
        if (control == null) return false;
        if (control.isChanging()) return false;
        if (!OACheckBox.this.confirmChange(evt.getStateChange()==ItemEvent.SELECTED)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    control.afterChangeActiveObject(null);
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
        control = new OACheckBoxController(oaObject, propertyPath);
    }
    /**
        Create CheckBox that is bound to a property for an object.
        @param onValue value if value is considered true.
        @param offValue value if value is considered false.
    */
    public OACheckBox(OAObject oaObject, String propertyPath, Object onValue, Object offValue) {
        control = new OACheckBoxController(oaObject, propertyPath, onValue, offValue);
    }

    @Override
    public ToggleButtonController getController() {
    	return control;
    }
    
    /**
        Bind a button to have it add/remove objects a Hub.  
        @param hubSelect the active object from hub will be added/removed from this Hub.
    */
    public void setSelectHub(Hub hubSelect) {
        control.setSelectHub(hubSelect);
    }
    /**
        Returns the Hub that maintains the selected objects.
        @see setSelectHub(Hub)
    */
    public Hub getSelectHub() {
        return control.getSelectHub();
    }

    
    public void setXORValue(int xor) {
        control.setXORValue(xor);
    }

    public int getXORValue() {
        return control.getXORValue();
    }


    // ----- OATableComponent Interface methods -----------------------
    public void setHub(Hub hub) {
        control.setHub(hub);
    }
    public Hub getHub() {
        if (control == null) return null;
        return control.getHub();
    }
    public void setTable(OATable table) {
        this.table = table;
        if (table != null) table.resetColumn(this);
    }
    public OATable getTable() {
        return table;
    }

    /** value property will be set to when selected. Default: TRUE */
    public Object getOnValue() {
        return control.valueOn;
    }
    public void setOnValue(Object value) {
        control.valueOn = value;
        control.resetHubOrProperty();
    }
    /** value property will be set to when deselected. Default: FALSE */
    public Object getOffValue() {
        return control.valueOff;
    }
    public void setOffValue(Object value) {
        control.valueOff = value;
        control.resetHubOrProperty();
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
    */
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) table.resetColumn(this);
    }
    /**
        Property path used to retrieve/set value for this component.
    */
    public String getPropertyPath() {
        return control.getPropertyPath();
    }

    
    /**
        Column heading when this component is used as a column in an OATable.
    */
    public String getTableHeading() {
        return heading;   
    }
    /**
        Column heading when this component is used as a column in an OATable.
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
            control.resetHubOrProperty();
            bRemoved = false;
        }
    }
    
    /* 2005/02/07 need to manually call close instead
    public void removeNotify() {
        super.removeNotify();
        close();
    }
    */
    public void close() {
        bRemoved = true;
        control.close();
    }

    JCheckBox chkRenderer;
    /** 
        Called by getTableCellRendererComponent to display this component.
    */
    @Override
    public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (chkRenderer == null) {
            chkRenderer = new JCheckBox() {
                @Override
                public void paint(Graphics g) {
                    super.paint(g);
                    if (!bHalfChecked) return;
                    g.setColor(Color.gray);
                    Dimension d = getSize();
                    int w = d.width/2;
                    int h = d.height/2;
                    g.fillRect(w-2, h-1, 5, 3);
                }
            };
            chkRenderer.setOpaque(true);
            chkRenderer.setHorizontalAlignment(JLabel.CENTER);
        }
        chkRenderer.setEnabled(true);
        boolean tf = false;
        Object obj = control.getHub().elementAt(row);
        if (control.hubSelect != null) {
            // Object obj = control.getActualHub().elementAt(row);
            if (obj != null && control.hubSelect.getObject(obj) != null) tf = true;
        }
        else {
        	if (value != null && value instanceof Boolean) tf = ((Boolean)value).booleanValue();
        }

        if (!isSelected && !hasFocus) {
            chkRenderer.setForeground( UIManager.getColor(table.getForeground()) );
            chkRenderer.setBackground( UIManager.getColor(table.getBackground()) );
        }
        
        chkRenderer.setText(null);
        control.update(chkRenderer, obj);
        
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
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }

    private boolean bHalfChecked;
    public void setHalfChecked(boolean b) {
        bHalfChecked = b;
    }
    public boolean isHalfChecked() {
        return bHalfChecked;
    }
    
    @Override
    public String getToolTipText(JTable table, int row, int col, String defaultValue) {
        return defaultValue;
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
    		if (isEnabled()) { // 20101220 added isEnabled check
        	    if (bMousePressed && (bSelected == isSelected())) {
        			setSelected(!bSelected);
        		}
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
        control.setConfirmMessage(msg);
    }
    /**
        Popup message used to confirm button click before running code.
    */
    public String getConfirmMessage() {
        return control.getConfirmMessage();
    }

    /** returns true if command is allowed */
    protected boolean confirmChange(boolean bSelected) {
        return true;
    }


    /**
     * Other Hub/Property used to determine if component is enabled.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setEnabled(Hub hub, String prop) {
        control.getEnabledController().add(hub, prop);
    }
    public void setEnabled(Hub hub) {
        control.getEnabledController().add(hub);
    }
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (control != null) {
           control.getEnabledController().directlySet(true, enabled);
        }
        super.setEnabled(enabled);
    }

    
    /**
     * 
     */
    public void setEnabled(Hub hub, String prop, Object compareValue) {
        control.getEnabledController().add(hub, prop, compareValue);
    }
    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }
    
    public void setVisible(Hub hub) {
        control.getVisibleController().add(hub);
    }    
    public void setVisible(Hub hub, String prop) {
        control.getVisibleController().add(hub, prop);
    }    
    public void setVisible(Hub hub, String prop, Object trueValue) {
        control.getVisibleController().add(hub, prop, trueValue);
    }    

    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    protected String isValid(Object object, Object value) {
        return null;
    }
    
    
    class OACheckBoxController extends ToggleButtonController {
        public OACheckBoxController() {
            super(OACheckBox.this);
        }    
        public OACheckBoxController(Hub hub, String propertyPath) {
            super(hub, OACheckBox.this, propertyPath);
        }
        public OACheckBoxController(Hub hub, String propertyPath, Object onValue, Object offValue) {
            super(hub, OACheckBox.this, propertyPath, onValue, offValue);
        }
        public OACheckBoxController(OAObject hubObject, String propertyPath) {
            super(hubObject, OACheckBox.this, propertyPath);
        }        
                
        public OACheckBoxController(OAObject hubObject, String propertyPath, Object onValue, Object offValue) {
            super(hubObject, OACheckBox.this, propertyPath, onValue, offValue);
        }        
        public OACheckBoxController(Hub hub, Hub hubSelect) {
            super(hub, hubSelect, OACheckBox.this);
        }
        
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OACheckBox.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OACheckBox.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OACheckBox.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
        @Override
        public void itemStateChanged(ItemEvent evt) {
            if (!onItemStateChanged(evt)) super.itemStateChanged(evt);
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




