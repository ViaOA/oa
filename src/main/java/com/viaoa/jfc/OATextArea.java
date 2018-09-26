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
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.util.OAString;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;


/**
    Example:<br>
    This will create a JTextArea that will automatically display the Notes property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OATextArea txta = new OATextArea(hubEmployee, "notes", 8,30); // 8 rows, 30 columns
    txta.setLineWrap(true);
    txta.setWrapStyleWord(true);
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OATextArea
*/
public class OATextArea extends JTextArea implements OATableComponent, OAJfcComponent {
    private OATextAreaController control;
    private OATable table;
    private String heading = "";

    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextArea(Hub hub, String propertyPath) {
        control = new OATextAreaController(hub, propertyPath);
        initialize();
    }

    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param rows number of rows to visually display.
        @param cols is the width
    */
    public OATextArea(Hub hub, String propertyPath, int rows, int cols) {
        super(rows, cols);
        control = new OATextAreaController(hub, propertyPath);
        initialize();
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextArea(OAObject hubObject, String propertyPath) {
        control = new OATextAreaController(hubObject, propertyPath);
        initialize();
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
        @param rows number of rows to visually display.
        @param cols is the width
    */
    public OATextArea(OAObject hubObject, String propertyPath, int rows, int cols) {
        super(rows, cols);
        control = new OATextAreaController(hubObject, propertyPath);
        initialize();
    }

    @Override
    public void initialize() {
    }
    
    
    @Override
    public TextAreaController getController() {
        return control;
    }

	private boolean bLineWrap;
    @Override
    public void setLineWrap(boolean wrap) {
    	bLineWrap = wrap;
    	super.setLineWrap(wrap);
    }
    
    /**
        update with active object.
    */
    public void addNotify() {
        super.addNotify();
        setWrapStyleWord(true);
        setLineWrap(bLineWrap);
        control.afterChangeActiveObject(); 
    }

    // ----- OATableComponent Interface methods -----------------------
    public Hub getHub() {
        return control.getHub();
    }
    public void setTable(OATable table) {
        this.table = table;
    }
    public OATable getTable() {
        return table;
    }
    public void setColumns(int x) {
        super.setColumns(x);
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),super.getPreferredSize().width);
    }

    public String getPropertyPath() {
        if (control == null) return null;
        return control.getPropertyPath();
    }
    public String getEndPropertyName() {
        if (control == null) return null;
        return control.getEndPropertyName();
    }
    public String getTableHeading() { //zzzzz
        return heading;   
    }
    public void setTableHeading(String heading) { //zzzzz
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderer;
    }

    OATextAreaTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATextAreaTableCellEditor(this);
        }
        return tableCellEditor;
    }

    @Override
	public String getFormat() {
		return control.getFormat();
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

    
    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    public String isValidCallback(Object object, Object value) {
        return null;
    }

    class OATextAreaController extends TextAreaController {
        public OATextAreaController(Hub hub, String propertyPath) {
            super(hub, OATextArea.this, propertyPath);
        }
        public OATextAreaController(OAObject hubObject, String propertyPath) {
            super(hubObject, OATextArea.this, propertyPath);
        }        

        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OATextArea.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OATextArea.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        public String isValid(Object object, Object value) {
            String msg = OATextArea.this.isValidCallback(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
    }

    
    @Override
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
        Object obj = ((OATable) table).getObjectAt(row, col);
        defaultValue = getToolTipText(obj, row, defaultValue);
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
        Object obj = ((OATable) table).getObjectAt(row, column);
        customizeRenderer(lbl, obj, value, isSelected, hasFocus, row, wasChanged, wasMouseOver);
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
}

class OATextAreaTableCellEditor extends OATableCellEditor {
    OATextArea vtf;
   
    public OATextAreaTableCellEditor(OATextArea tf) {
        super(tf, (OATableCellEditor.LEFT | OATableCellEditor.RIGHT) );
        this.vtf = tf;
    }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        vtf.selectAll();
    }
    public Object getCellEditorValue() {
        return vtf.getText();
	}

}

