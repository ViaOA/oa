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
import javax.swing.border.*;
import javax.swing.table.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OAPasswordField extends JPasswordField implements OATableComponent, OAJFCComponent {
    private OAPasswordFieldController control;
    private OATable table;
    private String heading = "";

    /**
        Create a PasswordField that is not bound to a Hub.
    */  
    public OAPasswordField() {
        control = new OAPasswordFieldController();
    }
    /**
        Bind a PasswordField to a property path in the active object of a Hub.
    */  
    public OAPasswordField(Hub hub, String propertyPath) {
        control = new OAPasswordFieldController(hub, propertyPath);
    }
    /**
        Bind a PasswordField to a property path in the active object of a Hub.
    */  
    public OAPasswordField(Hub hub, String propertyPath, int cols) {
        super(cols);
        control = new OAPasswordFieldController(hub, propertyPath);
    }
    /**
        Bind a component to a property path for an object.
    */  
    public OAPasswordField(OAObject oaObject, String propertyPath) {
        control = new OAPasswordFieldController(oaObject, propertyPath);
    }
    /**
        Bind a component to a property path for an object.
    */  
    public OAPasswordField(OAObject oaObject, String propertyPath, int cols) {
        super(cols);
        control = new OAPasswordFieldController(oaObject, propertyPath);
    }

    @Override
    public OAPasswordFieldController getController() {
        return control;
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

    private OAPasswordFieldTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAPasswordFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }

    /**
     * Same as textfield, default is 'P'assword
     */
    public void setConversion(char conv) {
        getController().setConversion(conv);
    }
    public char getConversion() {
        return getController().getConversion();
    }

    
    
    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String s = renderer.getText();
        int x = s.length();
        StringBuffer sb = new StringBuffer(x);
        for (int i=0; i<8; i++) sb.append('*');
        renderer.setText(new String(sb));

        
        if (hasFocus) renderer.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
        else renderer.setBorder(null);

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
	
	
    class OAPasswordFieldController extends TextFieldController {
        public OAPasswordFieldController() {
            super(OAPasswordField.this);
//qqqqqqqqq passwords are not encrypted yet        
//            setConversion('P');
        }    
        public OAPasswordFieldController(Hub hub, String propertyPath) {
            super(hub, OAPasswordField.this, propertyPath);
//qqqqqqqqq passwords are not encrypted yet        
//            setConversion('P');
        }
        public OAPasswordFieldController(OAObject hubObject, String propertyPath) {
            super(hubObject, OAPasswordField.this, propertyPath);
//qqqqqqqqq passwords are not encrypted yet        
//            setConversion('P');
        }        
        
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OAPasswordField.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OAPasswordField.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OAPasswordField.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
    }

}



