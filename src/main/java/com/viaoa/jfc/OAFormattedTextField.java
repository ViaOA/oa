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

public class OAFormattedTextField extends BaseFormattedTextField implements OATableComponent, OAJFCComponent {
    private FormattedTextFieldController control;
    private OATable table;
    private String heading = "";
    private OAFormattedTextFieldTableCellEditor tableCellEditor;

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OAFormattedTextField(Hub hub, String propertyPath, int cols, String mask, String validChars, boolean bRightJustified, boolean bAllowSpaces) {
        super(mask, validChars, bRightJustified, bAllowSpaces);
        setColumns(cols);
        control = new FormattedTextFieldController(hub,this,propertyPath) {
            @Override
            protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
                return OAFormattedTextField.this.isEnabled(bIsCurrentlyEnabled);
            }
            @Override
            protected boolean isVisible(boolean bIsCurrentlyVisible) {
                return OAFormattedTextField.this.isVisible(bIsCurrentlyVisible);
            }
        };
    }

    public void addNotify() {
        super.addNotify();
        control.afterChangeActiveObject(null);
    }

    // ----- OATableComponent Interface methods -----------------------
    public void setHub(Hub hub) {
        control.setHub(hub);
        if (table != null) table.resetColumn(this);
    }
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
    
    @Override
    public JFCController getController() {
        return control;
    }
    
    
    @Override
    public String getFormat() {
        return null;
    }

    public String getPropertyPath() {
        return control.getPropertyPath();
    }
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) table.resetColumn(this);
    }
    public String getTableHeading() {
        return heading;
    }
    public void setTableHeading(String heading) {
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getPreferredSize();
        return d;
    }



    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (control != null) {
            control.getTableRenderer(renderer, table, value, isSelected, hasFocus, row, column);
        }
        return renderer;
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }

    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAFormattedTextFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }

    
    
    @Override
    protected void onValueChange(String value) {
        if (control != null) {
            control.saveText(value);
        }
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
    public String isValid(Object object, Object value) {
        return null;
    }
    
}



