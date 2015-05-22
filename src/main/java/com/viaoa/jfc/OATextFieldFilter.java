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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellEditor;

import com.viaoa.hub.*;
import com.viaoa.jfc.table.OATableCellEditor;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.jfc.table.OATableFilterComponent;
import com.viaoa.jfc.table.OATextFieldTableCellEditor;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;

/** 
*/
public class OATextFieldFilter<T extends OAObject> extends JTextField implements KeyListener, OATableFilterComponent {
    private String propertyPath;

    public OATextFieldFilter(String propertyPath) {
        this.propertyPath = propertyPath;
        addKeyListener(this);
    }

    @Override
    public Hub getHub() {
        return null;
    }

    @Override
    public void setHub(Hub hub) {
    }
    @Override
    public String getPropertyPath() {
        return null;
    }

    @Override
    public void setPropertyPath(String path) {
        this.propertyPath = path;
    }

    @Override
    public String getTableHeading() {
        return null;
    }
    @Override
    public void setTableHeading(String heading) {
    }

    private OATableCellEditor tableCellEditor;
    @Override
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATableCellEditor(this) {
                @Override
                public Object getCellEditorValue() {
                    return getText();
                }
            };
        }
        return tableCellEditor;
    }
    
    private OATable table;
    @Override
    public void setTable(OATable table) {
        this.table = table;
    }

    @Override
    public OATable getTable() {
        return this.table;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        lbl.setText(getText());
        if (this.table == null && table instanceof OATable) setTable((OATable) table);
        return lbl;
    }

    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver) {
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return null;
    }


    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        if (table != null) {
            table.refreshFilter();
        }
    }

    @Override
    public boolean isUsed(Object obj) {
        if (!(obj instanceof OAObject)) return false;

        String txt = getText();
        if (txt == null || txt.length() == 0) return true;
        txt = txt.toLowerCase();

        String val = ((OAObject)obj).getPropertyAsString(propertyPath);
        if (OAString.isEmpty(val)) return false;
        return (val.toLowerCase().indexOf(txt) >= 0);
    }
    
    @Override
    public void reset() {
        setText("");
    }
}

