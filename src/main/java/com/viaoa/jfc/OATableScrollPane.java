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
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.jfc.border.CustomLineBorder;
import com.viaoa.jfc.table.OATableColumn;
import com.viaoa.jfc.OATable;

/*
    Creates a scrollPane for OATable, with an option to have some of the left columns fixed,
    so that they do not scroll.
 */
public class OATableScrollPane extends JScrollPane implements ChangeListener, PropertyChangeListener {
    private OATable mainTable;
    private OATable fixedTable;

/*    
    @Override
    public Dimension getPreferredSize() {
        Dimension dim1 = super.getPreferredSize();
        Dimension dim = mainTable.getPreferredSize();
        return dim;
    }
*/
    
    public OATableScrollPane(OATable table, int fixedColumns)
    {
        super(table);
        
        mainTable = table; // ((OATable)scrollPane.getViewport().getView());
        // mainTable.addPropertyChangeListener(this);

        mainTable.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
            }
            @Override
            public void focusGained(FocusEvent e) {
                if (fixedTable == null) return;
                TableCellEditor tced = fixedTable.getCellEditor();
                if (tced != null) tced.stopCellEditing();
                
            }
        });
        
        
        if (fixedColumns < 1) return;
        
        int totalColumns = mainTable.getColumnCount();

        fixedTable = createFixedTable(mainTable);
        fixedTable.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
            }
            @Override
            public void focusGained(FocusEvent e) {
                if (mainTable == null) return;
                TableCellEditor tced = mainTable.getCellEditor();
                if (tced != null) tced.stopCellEditing();
                
            }
        });

        if (mainTable.getHub() != fixedTable.getHub()) {
            throw new RuntimeException("must use the same Hub for both main and fixed tables");
        }
        
        
        // 20101229 need to have both tables aware of each other - for column sorting, arrow keys, etc.
        fixedTable.setJoinedTable(mainTable, true);
        mainTable.setJoinedTable(fixedTable, false);

        
        
        for (int i = 0; i < fixedColumns; i++) {
            OATableColumn tc = (OATableColumn) mainTable.columns.get(0);
            mainTable.columns.remove(0);

            fixedTable.columns.add(tc); // needs to be done first
            tc.setTable(fixedTable);
            if (tc.getOATableComponent() != null) {
                tc.getOATableComponent().setTable(fixedTable);
            }

            TableColumnModel columnModel = mainTable.getColumnModel();
            TableColumn column = columnModel.getColumn( 0 );
            columnModel.removeColumn(column);
            
            fixedTable.getColumnModel().addColumn( column );
        }

        // renumber the column indexes
        TableColumnModel columnModel = mainTable.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            columnModel.getColumn(i).setModelIndex(i);
        }
        
        
        //  Add the fixed table to the scroll pane
        
        Color color = UIManager.getLookAndFeelDefaults().getColor("Table.gridColor");
        if (color == null) color = Color.lightGray;
        Border b = new CustomLineBorder(0, 0, 0, 3, color);
        b = new CompoundBorder(new CustomLineBorder(0, 0, 0, 1, color.brighter()), b);
        b = new CompoundBorder(b, new EmptyBorder(0, 0, 0, 2));

        fixedTable.setBorder(b);        
        fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize());

        fixedTable.getTableHeader().setBorder(b);
        this.setRowHeaderView(fixedTable);
        
        this.setCorner(JScrollPane.UPPER_LEFT_CORNER, fixedTable.getTableHeader());

        // 20140412
        fixedTable.getHub().addHubListener(new HubListenerAdapter() {
            @Override
            public void afterAdd(HubEvent e) {
                OATableScrollPane.this.fixedTable.invalidate();
            }
            public void afterInsert(HubEvent e) {
                OATableScrollPane.this.fixedTable.invalidate();
            }
        });
        
        JLabel lbl = new JLabel("");
        lbl.setBorder(b);
        this.setCorner(JScrollPane.LOWER_LEFT_CORNER, lbl);

        this.getRowHeader().addChangeListener( this );
        
        fixedTable.oaTableModel.fireTableStructureChanged();
        mainTable.oaTableModel.fireTableStructureChanged();
        
        setupMouseListener();
    }

    public void stateChanged(ChangeEvent e) {
        JViewport viewport = (JViewport) e.getSource();
        this.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
    }

    protected OATable createFixedTable(OATable mainTable) {
        OATable t = new OATable(mainTable.getHub());
        /*
            public Component getRenderer(JTable table,Object value, boolean isSelected, boolean hasFocus,int row, int column) {
                return OATableScrollPane.this.mainTable.getRenderer(table, value, isSelected, hasFocus, row, column);
            }
        };
        */
        return t;
    }
    
    
    // not being used - since both tables are set up to share same model and selectionModel
    public void propertyChange(PropertyChangeEvent e) {
        //  Keep the fixed table in sync with the main table
        if ("selectionModel".equals(e.getPropertyName())){
            fixedTable.setSelectionModel(mainTable.getSelectionModel());
        }
        if ("model".equals(e.getPropertyName())){
            fixedTable.setModel(mainTable.getModel());
        }
    }


    protected void setupMouseListener() {
        MouseAdapter ma = new MouseAdapter() { 
            TableColumn column; 
            int columnWidth; 
            int pressedX; 
         
            public void mousePressed(MouseEvent e) 
            { 
                JTableHeader header = (JTableHeader)e.getComponent(); 
                TableColumnModel tcm = header.getColumnModel(); 
                int columnIndex = tcm.getColumnIndexAtX( e.getX() ); 
                Cursor cursor = header.getCursor(); 
                if (cursor == Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)) { 
                    column = tcm.getColumn(columnIndex); 
                    columnWidth = column.getWidth(); 
                    pressedX = e.getX(); 
                } 
            } 
         
            public void mouseReleased(MouseEvent e) {
                if (column == null) return;
                int width = columnWidth + (e.getX() - pressedX);
                column.setPreferredWidth(width); 
                fixedTable.setPreferredScrollableViewportSize(fixedTable.getPreferredSize()); 

                column = null;
                fixedTable.revalidate();
                mainTable.revalidate();

                OATableScrollPane.this.revalidate();
            }
        }; 
         
        fixedTable.getTableHeader().addMouseListener( ma ); 
        
    }
}





