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
package com.viaoa.jfc.table;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.OATable;

/** 
    Interface that defines the behavior for  OATableComponents to work with an OATable.
    Any component that implements this interface can be used as a Table Column.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public interface OATableComponent {
    /**
        Hub that this component is bound to.
    */
    public Hub getHub();
    /**
        Hub that this component is bound to.
    */
    // public void setHub(Hub hub);

    /**
        A dot (".") separated list of property names.
    */
    public String getPropertyPath();
    /**
        A dot (".") separated list of property names.
    */
    // public void setPropertyPath(String path);

    /**
        Width of component, based on average width of the font's character.
    */
    public int getColumns();
    /**
        Width of component, based on average width of the font's character.
    */
    public void setColumns(int x);
    
    /**
        Column heading when this component is used as a column in an OATable.
    */
    public String getTableHeading();
    /**
        Column heading when this component is used as a column in an OATable.
    */
    public void setTableHeading(String heading);
    
    /**
        Editor used when this component is used as a column in an OATable.
    */
    public TableCellEditor getTableCellEditor();
    
    /**
        Set by OATable when this component is used as a column.
    */
    public void setTable(OATable table);
    /**
        Set by OATable when this component is used as a column.
    */
    public OATable getTable();

    public String getFormat();
    
    /**
        Get the Renderer for this component when it is used as a column in an OATable.
        @see #customizeTableRenderer(JLabel, JTable, Object, boolean, boolean, int, int, boolean, boolean)
    */
    public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);
    
    /**
     * This will be called after the default OATable settings are set for the cell, and before the
     * OATable.customizeRenderer is called.
     */
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver);

    public String getTableToolTipText(JTable table, int row, int col, String defaultValue);
    
    default public Object getValue(JTable table, int row, int col, Object defaultValue) {
        return defaultValue;
    }
    
    default public String getTablePropertyPath(OATable table) {
        return null;
    }
}
