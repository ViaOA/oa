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
package com.viaoa.jfc.control;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.viaoa.jfc.*;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.jfc.table.OATreeTableCellEditor;
import com.viaoa.jfc.tree.OATreeModel;
import com.viaoa.jfc.tree.OATreeNodeData;
import com.viaoa.hub.*;

/**
 * Creates a Tree to use as a column in a Table.
 * It also populates a Hub with all of the visible tree nodes.
 * 
 * Note: this should only be used for a recursive treeNode
 *
 * See the paint method to see how it is used to render a single column cell.
 *
        Hub<User> hub = new Hub<User>(User.class);
        final OATreeTableController tree = new OATreeTableController(hub);
        tree.setPreferredSize(15, 33);

        OATreeNode node = new OATreeNode(PP_Display, getRootHub(), getHub());
        tree.add(node);
        node.add(node, OAString.cpp(User.P_Users)); // make recursive
        OATable table = new CustomTable(hub);
        table.addColumn("Users", 12, tree);        
        ...
 */
public class OATreeTableController extends OATree implements OATableComponent {
    private Hub hub;

    /**
     * @param hub Hub that will be popuplated with all of the objects that are visible in the tree.
     */
    public OATreeTableController(Hub hub) {
        super(8, 14, false);
        this.hub = hub;
        setup();
    }

    protected int visibleRow;

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, 0, w, table.getHeight());
    }

    public void paint(Graphics g) {
        int h = getRowHeight();
        h = table.getRowHeight();
        g.translate(0, (-visibleRow * h) - 3);
        super.paint(g);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        visibleRow = row;
        return this;
    }

    @Override
    public void setTableHeading(String heading) {
    }

    OATable table;

    @Override
    public void setRowHeight(int rowHeight) {
        if (table == null) {
            super.setRowHeight(rowHeight);
        }
        // else use the rowHeight of the table
    }
    
    @Override
    public void setTable(OATable table) {
        this.table = table;
        int h = table.getRowHeight();
        super.setRowHeight(h);
    }

    @Override
    public void setPropertyPath(String path) {
    }

    @Override
    public void setHub(Hub hub) {
    }

    @Override
    public void setColumns(int x) {
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return null;
    }

    JLabel lbl = new JLabel();

    @Override
    public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return OATreeTableController.this.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }

    @Override
    public String getTableHeading() {
        return "tree column";
    }

    private OATreeTableCellEditor tableCellEditor;

    @Override
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATreeTableCellEditor(this);
        }
        return tableCellEditor;
    }

    @Override
    public OATable getTable() {
        return OATreeTableController.this.table;
    }

    @Override
    public String getPropertyPath() {
        return "id";
    }

    @Override
    public Hub getHub() {
        return OATreeTableController.this.hub;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public int getColumns() {
        return 10;
    }

    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }

    
    @Override
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        setup();
    }
    
    
    @Override
    public void addNotify() {
        super.addNotify();
    }
    
    void setup() {
        if (hub == null) {
            return;
        }
        OATreeModel model = (OATreeModel) this.getModel();
        model.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                hub.clear();
                for (int row = 0;; row++) {
                    TreePath tp2 = OATreeTableController.this.getPathForRow(row);
                    if (tp2 == null) break;
                    Object[] objs = tp2.getPath();
                    if (objs.length < 1) break;
                    OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
                    Object objx = tnd.getObject();
                    hub.add(objx);
                }
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                TreePath tp = e.getTreePath();
                int row = OATreeTableController.this.getRowForPath(tp);
                if (row >= 0) {
                    if (!OATreeTableController.this.isExpanded(row)) return;
                }
                else row = 0;

                int[] ints = e.getChildIndices();
                if (ints == null) return;
                for (int i = (ints.length - 1); i >= 0; i--) {
                    hub.remove(row + ints[i] + 1);
                }
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                TreePath tp = e.getTreePath();
                int row = OATreeTableController.this.getRowForPath(tp);
                if (row >= 0) {
                    if (!OATreeTableController.this.isExpanded(row)) {
                        table.repaint();
                        return;
                    }
                }
                else row = 0;

                Object[] objs = e.getChildren();
                int[] ints = e.getChildIndices();
                for (int i = 0; objs != null && i < objs.length; i++) {
                    OATreeNodeData tnd = (OATreeNodeData) objs[i];
                    Object objx = tnd.getObject();
                    hub.insert(objx, row + ints[i] + 1);
                }
            }

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
            }
        });

        OATreeTableController.this.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                TreePath tp = event.getPath();
                if (tp == null) return;

                int row = OATreeTableController.this.getRowForPath(tp);

                TreePath tp2 = OATreeTableController.this.getPathForRow(row + 1);
                
                if (tp2 == null) {
                    row++;
                    for ( ;; ) {
                        if (hub.getAt(row) == null) break;
                        hub.removeAt(row);
                    }
                    return;
                }
                
                Object[] objs = tp2.getPath();
                if (objs.length < 1) return;
                OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
                
                Object objx = tnd.getObject();
                int row2 = hub.getPos(objx);
                if (row2 < 0) return;
                for (int i = row2 - 1; i > row; i--) {
                    hub.remove(i);
                }
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                TreePath tp = event.getPath();
                if (tp == null) return;

                int row = OATreeTableController.this.getRowForPath(tp);

                for (row++;; row++) {
                    TreePath tp2 = OATreeTableController.this.getPathForRow(row);
                    if (tp2 == null) break;
                    Object[] objs = tp2.getPath();
                    if (objs.length < 1) break;
                    OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
                    Object objx = tnd.getObject();
                    if (hub.contains(objx)) break;
                    hub.insert(objx, row);
                }
            }
        });
    }

}
