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
package com.viaoa.jfc.control;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;

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
import com.viaoa.object.OALinkInfo;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.web.OATreeNode;
import com.viaoa.hub.*;
import com.viaoa.hub.HubListener.InsertLocation;

/**
 * Creates a Tree to use as a column in a Table.
 * It also populates a Hub with all of the visible tree nodes.
 * 
 * Note: this should only be used for a recursive treeNode
 *
 * See the paint method to see how it is used to render a single column cell.
 *
        Hub&lt;User&gt; hub = new Hub&lt;User&gt;(User.class);
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
    private Hub hubRoot;
    private Hub hubTable;
    private Hub hubFlattened;

    /**
     * @param hub Hub that will be populated with all of the objects that are visible in the tree.
     */
    public OATreeTableController(Hub hubRoot, Hub hub) {
        this(hubRoot, null, hub);
    }
    public OATreeTableController(Hub hubRoot, Hub hubFlattened, Hub hub) {
        super(8, 14, false);
        this.hubRoot = hubRoot;
        this.hubTable = hub;
        this.hubFlattened = hubFlattened;
        setup();
    }
    
    
    
    public OATreeTableController(Hub hub) {
        super(8, 14, false);
        this.hubRoot = null;
        this.hubTable = hub;
        setup();
    }

    public Hub getRootHub() {
        return hubRoot;
    }
    
    protected int visibleRow;

    public void setBounds(int x, int y, int w, int h) {
        super.setBounds(x, 0, w, table.getHeight());
    }

    public void paint(Graphics g) {
        //int h = getRowHeight();
        int h = table.getRowHeight();
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
    public String getToolTipText(JTable table, int row, int col, String defaultValue) {
        return defaultValue;
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
        return OATreeTableController.this.hubTable;
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
        super.addNotify(false);
    }

    private volatile boolean bIgnoreFlag;
    private Object lastRemoved;
    
    
    protected void refreshHub() {
        if (bIgnoreFlag) return;
        if (OAThreadLocalDelegate.isLoading()) {
            if (hubTable.size() > 0) return;
        }
        
        try {
            bIgnoreFlag = true;
            OAThreadLocalDelegate.setLoading(true);  // 20171214
            _doRefreshHub();
        }
        finally {
            OAThreadLocalDelegate.setLoading(false);
            bIgnoreFlag = false;
        }
    }
    private boolean bDoRefreshHub;

    private void _doRefreshHub() {
        int row = 0;
        boolean bValid = hubTable.isValid();
        for ( ;; row++) {
            TreePath tp2 = OATreeTableController.this.getPathForRow(row);
            if (tp2 == null) break;
            Object[] objs = tp2.getPath();
            if (objs.length < 1) break;
            OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
            Object objx = tnd.getObject();
            
            if (hubTable.getAt(row) == objx) continue;
            hubTable.removeAt(row);
            if (bValid) {
                if (row >= hubTable.size()) {
                    hubTable.add(objx);
                }
                else {
                    hubTable.insert(objx, row);
                }
            }
        }
        for (;;) {
            if (hubTable.getAt(row) == null) break;
            hubTable.removeAt(row);
        }
//qqqqqqqqqqqqqqqq        
//if (bDEBUG) System.out.println("refresh => "+hubTable.size());        
        
    }
//public boolean bDEBUG = false;

    private OALinkInfo liOne, liMany;
    public OALinkInfo getOneLinkInfo() {
        return liOne;
    }
    public OALinkInfo getManyLinkInfo() {
        return liMany;
    }
    
    @Override
    protected void finalize() throws Throwable {
        if (hl != null && hubTable != null) hubTable.removeHubListener(hl);
        if (hl2 != null && hubRoot != null) hubRoot.removeHubListener(hl2);
        super.finalize();
    }
    
    private HubListener hl;
    private HubListener hl2;
    
    void setup() {
        if (hubTable == null) {
            return;
        }
        
        OAObjectInfo oi = hubTable.getOAObjectInfo();
        liMany = oi.getRecursiveLinkInfo(OALinkInfo.MANY);
        liOne = oi.getRecursiveLinkInfo(OALinkInfo.ONE);
        
        hl = new HubListenerAdapter() {
            OAObject activeObject;
            OAObject prevActiveObject;

            @Override
            public void afterChangeActiveObject(HubEvent e) {
                prevActiveObject = activeObject;
                activeObject = (OAObject) hubTable.getAO();
            }
            
            @Override
            public void afterRemove(HubEvent e) {
                // removed, since another treeTableController could be removing during a refresh
                // refreshHub();
            }
            @Override
            public void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            @Override
            public void afterAdd(HubEvent e) {
                if (bIgnoreFlag) return;
                
                OAObject ao = activeObject;
                if (ao == e.getObject()) ao = prevActiveObject;
                
                if (ao != null) {
                    if (liOne.getValue(e.getObject()) == null) {
                        Hub hub = (Hub) liMany.getValue(ao);
                        hub.add(e.getObject());
                        
                        int row = hubTable.getPos(ao);
                        if (!OATreeTableController.this.isExpanded(row)) {
                            bIgnoreFlag = true;
                            try {
                                ArrayList al = new ArrayList();
                                for (Object obj=ao; obj!=null; ) {
                                    al.add(obj);
                                    obj = liOne.getValue(obj);
                                }
                                Collections.reverse(al);
                                Object[] objs = al.toArray();
                                OATreeTableController.this.expand(objs);
                            }
                            finally {
                                bIgnoreFlag = false;
                            }
                        }
                    }
                }
                else {
                    if (hubRoot != null) hubRoot.add(e.getObject());
                }
                refreshHub();
            }
        };
        hl.setLocation(InsertLocation.LAST);
        hubTable.addHubListener(hl);

        if (hubRoot != null) {
            hl2 = new HubListenerAdapter() {
                @Override
                public void onNewList(HubEvent e) {
                    hubTable.clear();
                    try {
                        OAThreadLocalDelegate.setLoading(true);
                        refreshHub();
                    }
                    finally {
                        OAThreadLocalDelegate.setLoading(false);
                    }
                }
            };        
            hubRoot.addHubListener(hl2);
        }        
        
        OATreeModel model = (OATreeModel) this.getModel();

        model.addTreeModelListener(new TreeModelListener() {
            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                refreshHub();
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                refreshHub();
            }

            /* was
            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                if (bIgnoreFlag) {
                    return;
                }
                try {
                    bIgnoreFlag = true;
                    _treeNodesRemoved(e);
                }
                finally {
                    bIgnoreFlag = false;
                }
            }
            void _treeNodesRemoved(TreeModelEvent e) {
                TreePath tp = e.getTreePath();
                int row = OATreeTableController.this.getRowForPath(tp);
                
                if (!OATreeTableController.this.isExpanded(row)) return;

                int[] ints = e.getChildIndices();
                if (ints == null) return;
                for (int i = (ints.length - 1); i >= 0; i--) {
                    hub.remove(row + ints[i] + 1);
                }
            }
            */

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                refreshHub();
            }
            
            /*was
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
            */

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                refreshHub();
            }
        });

        
        
        OATreeTableController.this.addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                refreshHub();
/*qq                
                TreePath tp = event.getPath();
                if (tp == null) return;

                int row = OATreeTableController.this.getRowForPath(tp);

                TreePath tp2 = OATreeTableController.this.getPathForRow(row + 1);
                
                if (tp2 == null) {
                    row++;
                    for ( ;; ) {
                        if (hubTable.getAt(row) == null) break;
                        hubTable.removeAt(row);
                    }
                    return;
                }
                
                Object[] objs = tp2.getPath();
                if (objs.length < 1) return;
                OATreeNodeData tnd = (OATreeNodeData) objs[objs.length - 1];
                
                Object objx = tnd.getObject();
                int row2 = hubTable.getPos(objx);
                if (row2 < 0) return;
                for (int i = row2 - 1; i > row; i--) {
                    hubTable.remove(i);
                }
*/                
            }

            @Override
            public void treeExpanded(TreeExpansionEvent event) {
refreshHub();
/*
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
                    if (hubTable.contains(objx)) break;
                    hubTable.insert(objx, row);
                }
*/                
            }
        });

    
        if (hubFlattened == null) return;

        
        // need to update tree table Hub.AO when hubFlattened.AO changes
        hubFlattened.addHubListener(new HLA() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                Object obj = hubFlattened.getAO();
                if (obj == null) return;
                expandTo(obj, true);
            }
            
            void expandTo(Object obj, boolean bBottom) {
                if (obj == null) return;
                int pos = hubTable.getPos(obj);
                if (pos >= 0) {
                    if (bBottom) {
                        OATreeTableController.this.setSelectionRow(pos);
                        OATreeTableController.this.hubTable.setPos(pos);
                    }
                    else OATreeTableController.this.expandRow(pos);
                    return;
                }
                Object objx = getOneLinkInfo().getValue(obj);
                expandTo(objx, false);
                if (bBottom) {
                    pos = hubTable.getPos(obj);
                    if (pos >= 0) {
                        OATreeTableController.this.setSelectionRow(pos);
                        OATreeTableController.this.hubTable.setPos(pos);
                    }
                }
            }
        });

        hubTable.addHubListener(new HLA() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                hubFlattened.setAO(e.getObject());                
            }
        });
    }
    public void add(OATreeNode node) {
        // TODO Auto-generated method stub
        
    }
}

