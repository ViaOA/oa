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
    public void setHub(Hub hub);

    /**
        A dot (".") separated list of property names.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getPropertyPath();
    /**
        A dot (".") separated list of property names.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path);

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
        Renderer for this component when it is used as a column in an OATable.
    */
    public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column);

    public String getToolTipText(int row, int col, String defaultValue);
}


