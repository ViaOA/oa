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
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public abstract class OACustomComboBox extends JComboBox implements OATableComponent, OAJfcComponent {
    OACustomComboBoxController control;
    OATextField vtf;
    OATable table;
    String heading = "";
    String format;
    boolean bTypeEditProperty;
    private boolean bAllowClear;
    public boolean bSetting;


    public OACustomComboBox() {
        this((Hub)null, (String) null, 7);
    }
    
    /**
        Create a ComboBox that is bound to a property for the active object in a Hub.
        @param columns is width of list using character width size.
    */
    public OACustomComboBox(Hub hub, String propertyPath, int columns) {
        this(hub, propertyPath);
        setColumns(columns);
    }

    /**
        Create a ComboBox that is bound to a property for the active object in a Hub.
    */
    public OACustomComboBox(Hub hub, String propertyPath) {
        control = new OACustomComboBoxController(hub, propertyPath);
        initialize();
    }
    
    public OACustomComboBox(Hub hub, String propertyPath, Hub hubDirect, String directProperty) {
        control = new OACustomComboBoxController(hub, propertyPath, hubDirect, directProperty);
        initialize();
    }
    
    /**
        Create a ComboBox that is bound to a property for the active object in a Hub.
        @param columns is width of list using character width size.
    */
    public OACustomComboBox(Object obj, String propertyPath, int columns) {
        this(obj, propertyPath);
        setColumns(columns);
    }

    /**
        Create a ComboBox that is bound to a property for an object.
    */
    public OACustomComboBox(Object obj, String propertyPath) {
        control = new OACustomComboBoxController(obj, propertyPath);
        initialize();
    }

    @Override
    public void initialize() {
    }
    
    /*
     *  2006/12/13
     *  Can be overwritten to clear selected value.  Called when clicking the clear button
     *  on the bottom of the combo popup.
     */
    public void onClear() {
    	// this needs to be overwritten.
    }

    public void allowClearButton(boolean b) {
    	this.bAllowClear = b;
    }
    public boolean getAllowClearButton() {
    	return bAllowClear;
    }
    
    

    /** 
        Directly called by popup to set property value.
        This can be overwritten to provide custom features for
        setting property.
    */

    
    public void setSelectedItem(Object item) {
        if (bSetting) return;
        try {
            bSetting = true;
	        // 2006/12/11 commented out, caused exception:  
            // removeAllItems(); // hides the popup if visible
	        // if (item != null) addItem(item);
            if (control != null) control.updatePropertyValue(item);
	        super.setSelectedItem(item);
        }
        finally {
            bSetting = false;
        }
    }

    /**
        Format string used for displaying value.
    */
    public void setFormat(String fmt) {
        if (control != null) control.setFormat(fmt);
        else {
            format = fmt;
        }
    }
    /**
        Format string used for displaying value.
    */
    public String getFormat() {
        if (control != null) return control.getFormat();
        return format;
    }

    /**
        Property used for image name, that is used to get image to
        display with rows.
    */
    public void setImageProperty(String propPath) {
        control.setImagePropertyPath(propPath);
    }
    /**
        Property used for image name, that is used to get image to
        display with rows.
    */
    public String getImageProperty() {
        return control.getImagePropertyPath();
    }

    /**
        Root directory path where images are stored.
    */
    public void setImagePropertyPath(String path) {
        control.setImagePropertyPath(path);
    }
    /**
        Root directory path where images are stored.
    */
    public String getImagePropertyPath() {
        return control.getImagePropertyPath();
    }

    /**
        Returns icon used for active object.
    */
    public Icon getIcon() {
        return control.getIcon();
    }
    /**
        Returns icon used for an object.
    */
    public Icon getIcon(Object obj) {
        return control.getIcon(obj);
    }

    
    public void setIconColorProperty(String s) {
    	control.setIconColorPropertyPath(s);
    }
    public String getIconColorProperty() {
    	return control.getIconColorPropertyPath();
    }
    

    // ----- OATableComponent Interface methods -----------------------zzzzzzz

    /**
        Hub that this component is bound to.
    */
    public Hub getHub() {
    	if (control == null) return null;
        return control.getHub();
    }

    /**
        Set by OATable when this component is used as a column.  
    */
    public void setTable(OATable table) {
        this.table = table;
    }
    /**
        Set by OATable when this component is used as a column.  
    */
    public OATable getTable() {
        return table;
    }

    /**
        Used to manually disable/enable this component.
    */
    public void setDisable(boolean b) {
        this.setEnabled(!b);
    }
    /**
        Used to manually disable/enable this component.
    */
    public boolean getDisable() {
        return !this.isEnabled();
    }
    

    /* 09/13/99 works with jdk1.1  but this does not work in jdk2
    JList list = null;
    public void addNotify() {
        super.addNotify();   

        if (list == null) {
            list = getUI().getList();
            list.setPrototypeCellValue("1234567890");
            list.indexToLocation(0);  // force update
        }
    }
    */  
    
    /**
        Used to determine the default width, based on average character width 
        of font.
    */
    public int getColumns() {
        return control.getColumns();            
    }
    
    /**
    	Width of ComboBox, based on average width of the font's character.
	*/
    public void setColumns(int x) {
        control.setColumns(x);

        int w = OATable.getCharWidth(this, getFont(), x);
        Border b = this.getBorder();
        if (b != null) {
            Insets ins = b.getBorderInsets(this);
            if (ins != null) w += ins.left + ins.right;
        }
        if (table != null) { 
            table.setColumnWidth(table.getColumnIndex(this), w);
        }
        String str = "w";
        for (int i=1; i<x; i++) str += "w";
        super.setPrototypeDisplayValue(str);
        
        if (vtf != null) {
            int c = this.getColumns();
            if (c > 0) vtf.setColumns(c);
            else {
                c = vtf.getColumns();
                if (c > 0) this.setColumns(c);
            }
        }        
    }
	

    public void setMaximumColumns(int x) {
        control.setMaximumColumns(x);
        if (vtf != null) {
            int c = this.getMaximumColumns();
            if (c > 0) vtf.setMaximumColumns(c);
            else {
                c = vtf.getMaximumColumns();
                if (c > 0) this.setMaximumColumns(c);
            }
        }        
        invalidate();
    }
    public int getMaxColumns() {
        return control.getMaximumColumns();            
    }
    public int getMaximumColumns() {
        return control.getMaximumColumns();            
    }

    public void setMinimumColumns(int x) {
        control.setMinimumColumns(x);
        if (vtf != null) {
            int c = this.getMinimumColumns();
            if (c > 0) vtf.setMinimumColumns(c);
            else {
                c = vtf.getMinimumColumns();
                if (c > 0) this.setMinimumColumns(c);
            }
        }        
        invalidate();
    }
    public int getMinimumColumns() {
        return control.getMinimumColumns();            
    }
    
	public CustomComboBoxController getController() {
	    return control;
	}
    
    /**
        Property path used for displaying rows.
    */
    @Override
    public String getPropertyPath() {
        return control.getPropertyPath();
    }
/*    
    public String getEndPropertyName() {
        return control.getEndPropertyName();
    }
*/
    /**
        Column heading when used as a column in an OATable.
    */
    public String getTableHeading() {
        return heading;   
    }
    /**
        Column heading when used as a column in an OATable.
    */
    public void setTableHeading(String heading) {
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    /**
        OATextField used to directly edit value.
    */
    public void setEditor(OATextField vtf) {
        this.vtf = vtf;
        if (vtf == null) super.setEditor(null);
        else {
            int c = this.getColumns();
            if (c > 0) vtf.setColumns(c);
            else {
                c = vtf.getColumns();
                if (c > 0) this.setColumns(c);
            }
            
            c = this.getMaximumColumns();
            if (c > 0) vtf.setMaximumColumns(c);
            
            c = this.getMinimumColumns();
            if (c > 0) vtf.setMinimumColumns(c);
            
            OACustomComboBoxEditor ed = new OACustomComboBoxEditor(this, vtf);
            super.setEditor(ed);
            setEditable(true);
        }
        focusComp = null;
    }
    public OATextField getTextEditor() {
        return vtf;
    }


    protected OAComboBoxTableCellEditor tableCellEditor;
    /** 
        Used by OATable to set this component as an OATable column editor.
    */
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAComboBoxTableCellEditor(this);
        }
        return tableCellEditor;
    }

    // hack: JComboBox could be container, so set focus to first good component
    JComponent focusComp;
    protected JComponent getFocusComponent() {
        if (focusComp != null) return focusComp;

        if (getEditor() != null) {
            focusComp = (JComponent) getEditor().getEditorComponent();
            if ( !(focusComp instanceof OATextField) ) focusComp = null; // dont use default editor
        }
        
        if (focusComp == null) {
            Component[] comps = getComponents();
            for (int i=0; i<comps.length; i++) {
                if (comps[i] instanceof JComponent) {
                    focusComp = (JComponent) comps[i];
                    if (focusComp.isRequestFocusEnabled()) break;
                    focusComp = null;
                }
            }
            if (focusComp == null) focusComp = this;
        }
//qqqqqqqqqqqqqqqqqqqqqqqqqqqqq    20160401 needs to use this, ex: oacomboDate in a table will not allow dropdown to be clicked, since the txtfld would lose focus and then the table focusListener would stop editing    
focusComp = this;
//qqqqqqqq needs to be tested with other custom combos, ex: color combo
        
        return focusComp;
    }

    /**
        Overwritten, to setup editor component.
    */
    public void requestFocus() {
        ComboBoxEditor cbe = getEditor(); 
        if (cbe != null) {
            JComponent jc = (JComponent) cbe.getEditorComponent();
            if (jc instanceof OATextField) {
                ((OATextField)jc).requestFocus();
                ((OATextField)jc).selectAll();
                return;
            }
        }
        super.requestFocus();
        /* was 20160402
        getFocusComponent();
        if (focusComp != this) {
            focusComp.requestFocus();
            if (focusComp instanceof OATextField) ((OATextField)focusComp).selectAll();
        }
        else super.requestFocus();
        */
    }
    @Override
    public synchronized void addFocusListener(FocusListener l) {
        getFocusComponent();
        if (focusComp != this) {
            focusComp.addFocusListener(l);
        }
        else { // 20160402
            super.addFocusListener(l);
        }
    }
    
    
    
    /**
        Overwritten, to add key handlers that will drop down the list.
    */
    public void processKeyEvent(KeyEvent e) {
        boolean b = true;
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            if ((e.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK)) != 0 && e.getKeyCode() == KeyEvent.VK_DOWN) {
                b = false;
            }
        }
        else if (e.getID() == KeyEvent.KEY_PRESSED) {
            if ((e.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK)) != 0 && e.getKeyCode() == KeyEvent.VK_DOWN) {
                b = false;
                showPopup();
            }
        }
        if (b) {
        	try {
        		super.processKeyEvent(e);
        	}
        	catch (Exception ex) {
        	}
        }
    }


    /** 
        Used to supply the renderer when this component is used in the column of an OATable.
        Can be overwritten to customize the rendering.
    */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Hub h = ((OATable) table).getHub();
        if (h != null) {
            Object obj = h.elementAt(row);
            if (h == this.getHub()) {
                String s = control.getValueAsString(obj, control.getFormat());
                //was: String s = OAReflect.getPropertyValueAsString(obj, control.getGetMethods(), control.getFormat());
                renderer.setText(s);
                renderer.setIcon(this.getIcon(obj));
            }
        }

        if (hasFocus) renderer.setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
        else renderer.setBorder(null);
        // 20080906 was: if (hasFocus) renderer.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));

        if (hasFocus) {
            renderer.setForeground( UIManager.getColor("Table.focusCellForeground") );
            renderer.setBackground( UIManager.getColor("Table.focusCellBackground") );
        }
        else if (isSelected) {
            renderer.setForeground( UIManager.getColor("Table.selectionForeground") );
            renderer.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        else {
            renderer.setForeground( UIManager.getColor(table.getForeground()) );
            renderer.setBackground( UIManager.getColor(table.getBackground()) );
        }
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
    
    
    public void addEnabledCheck(Hub hub) {
        control.getEnabledChangeListener().add(hub);
    }
    public void addEnabledCheck(Hub hub, String propPath) {
        control.getEnabledChangeListener().addPropertyNotNull(hub, propPath);
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
        control.getVisibleChangeListener().addPropertyNotNull(hub, propPath);
    }
    public void addVisibleCheck(Hub hub, String propPath, Object compareValue) {
        control.getVisibleChangeListener().add(hub, propPath, compareValue);
    }
    protected boolean isVisible(boolean defaultValue) {
        return defaultValue;
    }

    
    class OACustomComboBoxController extends CustomComboBoxController {
        public OACustomComboBoxController(Hub hub, String propertyPath, Hub hubDirect, String directProperty) {
            super(hub, OACustomComboBox.this, propertyPath, hubDirect, directProperty);
        }
        public OACustomComboBoxController(Hub hub, String propertyPath) {
            super(hub, OACustomComboBox.this, propertyPath);
        }
        public OACustomComboBoxController(Object obj, String propertyPath) {
            super(obj, OACustomComboBox.this, propertyPath);
        }
        
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OACustomComboBox.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OACustomComboBox.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        public void update(JComponent comp, Object object, boolean bIncudeToolTip) {
            OACustomComboBox.this.beforeUpdate();
            super.update(comp, object, bIncudeToolTip);
            OACustomComboBox.this.afterUpdate();
        }
    }

    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) return d;
        
        int cols = getMaxColumns();
        if (cols <= 0) {
            cols = getColumns() * 2;
            if (cols < 0) return d;
        }
        
        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;

        d.width = OATable.getCharWidth(this, getFont(), cols) + inx; 
        
        return d;
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) return d;
        int cols = getMinimumColumns();
        if (cols < 1) return d;
        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;

        d.width = OATable.getCharWidth(this, getFont(), cols) + inx; 
        return d;
    }

    
    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLabel() {
        if (getController() == null) return null;
        return getController().getLabel();
    }

    
    public void setConfirmMessage(String msg) {
        getController().setConfirmMessage(msg);
    }
    public String getConfirmMessage() {
        return getController().getConfirmMessage();
    }

    public void beforeUpdate() {
    }
    public void afterUpdate() {
    }


    public void setDisplayTemplate(String s) {
        this.control.setDisplayTemplate(s);
    }
    public String getDisplayTemplate() {
        return this.control.getDisplayTemplate();
    }
    public void setToolTipTextTemplate(String s) {
        this.control.setToolTipTextTemplate(s);
    }
    public String getToolTipTextTemplate() {
        return this.control.getToolTipTextTemplate();
    }
}



