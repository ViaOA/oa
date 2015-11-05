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

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OATextField extends JTextField implements OATableComponent, OAJFCComponent {
    protected TextFieldController control; // 20110408 was OATextFieldController (internally defined)
    private OATable table;
    private String heading = "";

    public boolean bTest;
    /**
        Create an unbound TextField.
    */
    public OATextField() {
        control = new OATextFieldController();
    }
    public OATextField(TextFieldController control) {
        this.control = control;
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextField(Hub hub, String propertyPath) {
        control = new OATextFieldController(hub, propertyPath);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OATextField(Hub hub, String propertyPath, int cols) {
        super(cols);
        control = new OATextFieldController(hub, propertyPath);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextField(OAObject hubObject, String propertyPath) {
        control = new OATextFieldController(hubObject, propertyPath);
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OATextField(OAObject hubObject, String propertyPath, int cols) {
        super(cols);
        control = new OATextFieldController(hubObject, propertyPath);
    }

    public TextFieldController getController() {
        return control;
    }

    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLabel() {
        return getController().getLabel();
    }
    
    
    /**
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        control.setFormat(fmt);
    }

    /**
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        return control.getFormat();
    }


    /** might want to use (and test) this later
    boolean addNotifyFlag;
    public void addNotify() {
        super.addNotify();
        if (!addNotifyFlag) {
            addNotifyFlag = true;
            control.initialize(this);
        }
    }
    ***/

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
        if (table != null) {
            int w = OATable.getCharWidth(this,getFont(),x+1);
            table.setColumnWidth(table.getColumnIndex(this),w);
        }
    }

    public String getPropertyPath() {
        return control.getPropertyPath();
    }
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) {
            if (table != null) table.resetColumn(this);
        }
    }
    public String getTableHeading() { //zzzzz
        return heading;
    }
    public void setTableHeading(String heading) { //zzzzz
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    public void setText(String s) {
        try {
            super.setText(s);
            if (control != null) control.saveText();
            invalidate();
        }
        catch (Exception e) {
        }
    }

    public void setText(String s, boolean bSaveChanges) {
        super.setText(s);
        if (control != null && bSaveChanges) {
            control.saveText();
        }
        invalidate();
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (control != null) {
            control.getTableRenderer(renderer, table, value, isSelected, hasFocus, row, column);
            if (row == -1 && renderer != null) { // header
                renderer.setText(getText());
            }
        }
        return renderer;
    }
    


    OATextFieldTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATextFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (control != null) {
            control.getEnabledController().directlySet(true, enabled);
        }
        super.setEnabled(enabled);
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

    class OATextFieldController extends TextFieldController {
        public OATextFieldController() {
            super(OATextField.this);
        }    
        public OATextFieldController(Hub hub, String propertyPath) {
            super(hub, OATextField.this, propertyPath);
        }
        public OATextFieldController(OAObject hubObject, String propertyPath) {
            super(hubObject, OATextField.this, propertyPath);
        }        
        
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OATextField.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OATextField.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OATextField.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
    }
    
    
    /**
     * 'U'pper, 'L'ower, 'T'itle, 'P'assword/encrypt
     */
    public void setConversion(char conv) {
        getController().setConversion(conv);
    }
    public char getConversion() {
        return getController().getConversion();
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }

    
    public void setMaximumColumns(int x) {
        control.setMaximumColumns(x);
        invalidate();
    }
    public int getMaximumColumns() {
        return control.getMaximumColumns();
    }
    public void setMinimumColumns(int x) {
        control.setMinimumColumns(x);
        invalidate();
    }
    public int getMinimumColumns() {
        return control.getMinimumColumns();
    }

    
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) return d;
        
        int cols = getMaximumColumns();
        if (cols < 1)  {
            //cols = control.getDataSourceMaxColumns();
            //if (cols < 1) {
                cols = control.getPropertyInfoMaxColumns();
                if (cols < 1) {
                    cols = getColumns() * 2; 
                }
            //}
        }
        if (cols > 0) d.width = OATable.getCharWidth(this, getFont(), cols);
        
        // also check size of text
        String s = getText();
        if (s == null) s = " ";

        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;
        
        FontMetrics fm = getFontMetrics(getFont());
        d.width = Math.min(d.width, fm.stringWidth(s+"www")+inx+2);
        
        // dont size under pref size
        Dimension dx = getPreferredSize();
        d.width = Math.max(d.width, dx.width);

        return d;
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) return d;
        int cols = getMinimumColumns();
        if (cols < 1) return d;
        d.width = OATable.getCharWidth(this, getFont(), cols+1);
        return d;
    }
    
    
}



