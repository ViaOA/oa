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
import com.viaoa.util.OADate;
import com.viaoa.util.OAString;
import com.viaoa.util.converter.OAConverterNumber;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OATextField extends JTextField implements OATableComponent, OAJfcComponent {
    protected TextFieldController control; // 20110408 was OATextFieldController (internally defined)
    private OATable table;
    private String heading = "";

    public OATextField() {
        control = new OATextFieldController();
        setDisabledTextColor(Color.gray);
        initialize();
    }
    
    
    public OATextField(TextFieldController control) {
        this.control = control;
        setDisabledTextColor(Color.gray);
        initialize();
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextField(Hub hub, String propertyPath) {
        control = new OATextFieldController(hub, propertyPath);
        setDisabledTextColor(Color.gray);
        initialize();
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OATextField(Hub hub, String propertyPath, int cols) {
        control = new OATextFieldController(hub, propertyPath);
        setColumns(cols);
        setDisabledTextColor(Color.gray);
        initialize();
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextField(OAObject hubObject, String propertyPath) {
        control = new OATextFieldController(hubObject, propertyPath);
        setDisabledTextColor(Color.gray);
        initialize();
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OATextField(OAObject hubObject, String propertyPath, int cols) {
        control = new OATextFieldController(hubObject, propertyPath);
        setColumns(cols);
        setDisabledTextColor(Color.gray);
        initialize();
    }

    @Override
    public void initialize() {
    }
    
    public TextFieldController getController() {
        return control;
    }

    public void setLabel(JLabel lbl, boolean bAlwaysMatchEnabled) {
        getController().setLabel(lbl, bAlwaysMatchEnabled);
    }
    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLabel() {
        if (getController() == null) return null;
        return getController().getLabel();
    }
    
    /**
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate
        @see OAConverterNumber
    */
    public void setFormat(String fmt) {
        control.setFormat(fmt);
    }

    /**
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate
        @see OAConverterNumber
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
        control.onAddNotify();
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
        getController().setColumns(x);
        invalidate();
    }

    public String getPropertyPath() {
        return control.getPropertyPath();
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
            revalidate();
        }
        catch (Exception e) {
        }
    }

    public void setText(String s, boolean bSaveChanges) {
        super.setText(s);
        if (control != null && bSaveChanges) {
            control.saveText();
        }
        revalidate();
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (control == null) return renderer;
        control.getTableRenderer(renderer, table, value, isSelected, hasFocus, row, column);
        if (row == -1 && renderer != null) { // header
            renderer.setText(getText());
        }
        return renderer;
    }
    
    protected OATextFieldTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATextFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }

    @Override
    public boolean allowEdit() {
        return isEnabled();
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
    protected String isValidCallback(Object object, Object value) {
        return null;
    }

    
    /**
     * 'U'ppercase, 
     * 'L'owercase, 
     * 'T'itle, 
     * 'J'ava identifier
     * 'E'ncrpted password/encrypt
     * 'S'HA password
     */
    public void setConversion(char conv) {
        getController().setConversion(conv);
    }
    public char getConversion() {
        return getController().getConversion();
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

    @Override
    protected int getColumnWidth() {
        return OAJfcUtil.getCharWidth();
    }

    /**
     * Max columns to be displayed, used when calculating max size.
     */
    public void setMaxInput(int x) {
        control.setMaxInput(x);
    }
    public int getMaxInput() {
        return control.getCalcMaxInput();
    }
    
    
    /**
     * Max columns to be displayed, used when calculating max size.
     */
    public void setMaxCols(int x) {
        setMaximumColumns(x);
    }
    public void setMaxColumns(int x) {
        setMaximumColumns(x);
    }
    public void setMaximumColumns(int x) {
        control.setMaximumColumns(x);
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
        invalidate();
    }
    public int getMinimumColumns() {
        return control.getMinimumColumns();
    }
    public void setMinColumns(int x) {
        control.setMinimumColumns(x);
        invalidate();
    }
    public int getMinColumns() {
        return control.getMinimumColumns();
    }
 
    
    @Override
    public void setSize(Dimension d) {
        super.setSize(d);
    }
        
    
    // 20181120
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (isPreferredSizeSet()) return d;

        String text = getText();
        if (text == null) text = "";
        final int textLen = text.length();
        
        // resize based on size of text        
        int cols = getController().getCalcColumns();
        int maxCols = getMaximumColumns();
        if (cols < 1 && maxCols < 1) return d;
        if (maxCols < 1) maxCols = cols;
        else if (cols < 1) cols = 0;
        
        if (textLen >= cols && textLen > 0) {
            FontMetrics fm = getFontMetrics(getFont());
            if (textLen > maxCols) text = text.substring(0, maxCols);
            d.width = fm.stringWidth(text) + 8;
        }
        else {
            d.width = OAJfcUtil.getCharWidth(cols);
        }

        Insets ins = getInsets();
        if (ins != null) d.width += ins.left + ins.right;
        return d;
    }
    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) return d;

        String text = getText();
        if (text == null) text = "";
        final int textLen = text.length();
        
        // resize based on size of text        
        int cols = getController().getCalcColumns();
        int maxCols = getMaximumColumns();
        if (cols < 1 && maxCols < 1) return d;
        if (maxCols < 1) maxCols = cols;
        else if (cols < 1) cols = 0;

        if (textLen >= cols && textLen > 0) {
            FontMetrics fm = getFontMetrics(getFont());
            if (textLen > maxCols) text = text.substring(0, maxCols);
            d.width = fm.stringWidth(text) + 8;
        }
        else d.width = OAJfcUtil.getCharWidth(cols);
        
        Insets ins = getInsets();
        if (ins != null) d.width += ins.left + ins.right;
        return d;
    }
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) return d;
        int cols = getMinimumColumns();

        if (cols < 1) return d;
        d.width = OAJfcUtil.getCharWidth(cols);
        return d;
    }

    class OATextFieldController extends TextFieldController {
        public OATextFieldController() {
            super(OATextField.this);
            setColumns(OATextField.this.getColumns());
        }
        public OATextFieldController(Hub hub, String propertyPath) {
            super(hub, OATextField.this, propertyPath);
            setColumns(OATextField.this.getColumns());
        }
        public OATextFieldController(OAObject hubObject, String propertyPath) {
            super(hubObject, OATextField.this, propertyPath);
            setColumns(OATextField.this.getColumns());
        }        
        
        @Override
        public void afterPropertyChange() {
            super.afterPropertyChange();
            revalidate();
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
        public String isValid(Object object, Object value) {
            String msg = OATextField.this.isValidCallback(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
        @Override
        public void update(JComponent comp, Object object, boolean bIncudeToolTip) {
            OATextField.this.beforeUpdate();
            super.update(comp, object, bIncudeToolTip);
            OATextField.this.afterUpdate();
        }
    }
    
    public void beforeUpdate() {
    }
    public void afterUpdate() {
    }
    
    public void setConfirmMessage(String msg) {
        getController().setConfirmMessage(msg);
    }
    public String getConfirmMessage() {
        return getController().getConfirmMessage();
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

