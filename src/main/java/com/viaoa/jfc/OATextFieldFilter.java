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
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.TableCellEditor;

import com.viaoa.ds.query.OAQueryTokenizer;
import com.viaoa.hub.*;
import com.viaoa.jfc.table.OATableCellEditor;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.jfc.table.OATableFilterComponent;
import com.viaoa.jfc.table.OATextFieldTableCellEditor;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAFilter;
import com.viaoa.util.OAString;
import com.viaoa.util.filter.OAGreaterFilter;
import com.viaoa.util.filter.OAGreaterOrEqualFilter;
import com.viaoa.util.filter.OALessFilter;
import com.viaoa.util.filter.OALessOrEqualFilter;
import com.viaoa.util.filter.OALikeFilter;
import com.viaoa.util.filter.OANotLikeFilter;
import com.viaoa.util.filter.OAQueryFilter;

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
    public String getPropertyPath() {
        return this.propertyPath;
    }

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
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
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

    
    private String prevText;
    private OAFilter filter;
    
    // fix* && !gsmr || 
    protected OAFilter getFilter() {
        String text = getText();
        if (filter != null && prevText != null) {
            if (prevText.equals(text)) return filter;
        }
        prevText = text;

        
        if (text.length() == 0) {
            filter = new OAFilter() {
                @Override
                public boolean isUsed(Object obj) {
                    return true;
                }
            };
            return filter;
        };
        
        final char c1 = text.charAt(0);
        final char c2 = text.length() == 1 ? 0 : text.charAt(1);

        if (c1 == '!') {
            if (text.indexOf('*') < 0) filter = new OANotLikeFilter(propertyPath, "*"+text.substring(1)+"*");
            else filter = new OANotLikeFilter(propertyPath, text.substring(1));
        }
        else if (c1 == '>') {
            if (c2 != '=') filter = new OAGreaterFilter(propertyPath, text.substring(1));
            else filter = new OAGreaterOrEqualFilter(propertyPath, text.substring(2));
        }
        else if (c1 == '<') {
            if (c2 != '=') filter = new OALessFilter(propertyPath, text.substring(1));
            else filter = new OALessOrEqualFilter(propertyPath, text.substring(2));
        }
        else if (text.indexOf('*') < 0) {
            filter = new OALikeFilter(propertyPath, "*"+text+"*");
        }
        else {
            filter = new OALikeFilter(propertyPath, text);
        }
        
        return filter;
    }

    
    
    
    @Override
    public boolean isUsed(Object obj) {
        if (!(obj instanceof OAObject)) return false;

        OAFilter f = getFilter();
        if (f != null) {
            return f.isUsed(obj);
        }
        
        String txt = getText();
        if (txt == null || txt.length() == 0) return true;

        
        boolean bNot = txt.charAt(0) == '!';
        if (bNot) {
            txt = txt.substring(1);
            if (txt.length() == 0) return true;
        }
        
        txt = txt.toLowerCase();

        String val = ((OAObject)obj).getPropertyAsString(propertyPath);
        if (OAString.isEmpty(val)) return false;
        
        
        boolean b = (val.toLowerCase().indexOf(txt) >= 0);
        if (bNot) b = !b;
        return b;
    }
    
    @Override
    public void reset() {
        setText("");
    }
    @Override
    public boolean isBeingUsed() {
        String text = getText();
        return (text != null && text.length() > 0);
    }
}

