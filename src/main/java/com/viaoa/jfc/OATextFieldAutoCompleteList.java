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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.text.autocomplete.AutoCompleteList;
import com.viaoa.jfc.table.*;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.OAConv;

public abstract class OATextFieldAutoCompleteList extends JTextField implements OATableComponent {
    OATable table;
    String heading = "";
    Hub<?> hub;
    String displayPropertyPath;
    String updatePropertyPath;
    JList jlist;
    AutoCompleteList autoCompleteList;  // this makes the magic happen
    private boolean bSettingText;
    
    public OATextFieldAutoCompleteList(Hub<?> hub, String displayPropertyPath, String updatePropertyPath) {
        this.hub = hub;
        this.displayPropertyPath = displayPropertyPath;
        this.updatePropertyPath = updatePropertyPath;
        setup();
    }

    public OATextFieldAutoCompleteList() {
        setup();
    }
    

    protected void setup() {
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (editObject == null) return;
                String t = getText();
                Object val = getPropertyValueForText(t);
                OAObjectReflectDelegate.setProperty(editObject, updatePropertyPath, val, null);
            }
        });
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editObject != null) {
                    String t = getText();
                    Object val = getPropertyValueForText(t);
                    OAObjectReflectDelegate.setProperty(editObject, updatePropertyPath, val, null);
                }
            }
        });
        
        jlist = new JList();
        autoCompleteList = new AutoCompleteList(this, jlist, true) {
            @Override
            protected String getClosestMatch(String value) {
                if (bSettingText) return value;
                String s = OATextFieldAutoCompleteList.this.getClosestMatch(value);
                return s;
            }
            @Override
            protected String[] getSearchData(String text, int offset) {
                if (bSettingText) return new String[] {text};
                return OATextFieldAutoCompleteList.this.getSearchData(text, offset);
            }
            protected void onValueSelected(int pos, String value) {
                value = getTextForSelectedValue(pos, value);
                super.onValueSelected(pos, value);
                OATextFieldAutoCompleteList.this.onValueSelected(pos, value);
            }
            @Override
            protected String getTextForSelectedValue(int pos, String value) {
                return OATextFieldAutoCompleteList.this.getTextForSelectedValue(pos, value);
            }
        };
        
        if (hub != null) {
            hub.addHubListener(new HubListenerAdapter() {
                
                @Override
                public void afterChangeActiveObject(HubEvent evt) {
                    if (OATextFieldAutoCompleteList.this.hasFocus()) {
                        if (editObject != null) {
                            String t = getText();
                            Object val = getPropertyValueForText(t);
                            OAObjectReflectDelegate.setProperty(editObject, updatePropertyPath, val, null);
                        }
                    }

                    String s = null;
                    OAObject oaObj = (OAObject) hub.getAO();
                    if (oaObj != null) {
                        s = OAConv.toString(OAObjectReflectDelegate.getProperty(oaObj, displayPropertyPath));
                    }
                    if (s == null) s = "";
                    bSettingText = true;
                    setText(s);
                    bSettingText = false;
                    editObject = oaObj;
                }
            });
        }        
    }
    private OAObject editObject;

    public void setShowOne(boolean b) {
        this.autoCompleteList.setShowOne(b);
    }
    public boolean getShowOne() {
        return this.autoCompleteList.getShowOne();
    }
    
    
    @Override
    public void setText(String t) {
        super.setText(t);
        if (hub == null) return;
        if (bSettingText) return;
        
        OAObject oaObj = (OAObject) hub.getAO();
        if (oaObj != null) {
            Object val = getPropertyValueForText(t);
            OAObjectReflectDelegate.setProperty(oaObj, updatePropertyPath, val, null);
        }
        editObject = oaObj;
    }  
    
    
    
    
    // ----- OATableComponent Interface methods -----------------------
    public Hub getHub() {
        return this.hub;
    }
    @Override
    public void setHub(Hub hub) {
        this.hub = hub;
        
        
        
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
        return this.displayPropertyPath;
    }
    public void setPropertyPath(String path) {
        this.displayPropertyPath = path;
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
        //09/15/99   Dimension d = super.getMinimumSize();
        return d;
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (hasFocus) {
            // renderer.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
            renderer.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder") );
        }
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
    public String getFormat() {
        return null;
    }    

    @Override
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
        return defaultValue;
    }
    
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }
    
    
    OATextFieldAutoCompleteListTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATextFieldAutoCompleteListTableCellEditor(this);
        }
        return tableCellEditor;
    }


    protected void onValueSelected(int pos, String value) {
    }
    protected String getTextForSelectedValue(int pos, String value) {
        return value;
    }
    
    
    protected abstract String[] getSearchData(String text, int offset);
    protected abstract String getClosestMatch(String value);
    
    /**
     * Called when text has been set, to get the object that represents the selected (String) value. 
     * So that the correct OAObject property can be updated
     * @param code
     */
    protected abstract Object getPropertyValueForText(String code);
    
    

}



