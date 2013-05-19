/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc..

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jfc;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.hub.HubSelectDelegate;
import com.viaoa.jfc.dnd.OATransferable;
import com.viaoa.jfc.table.OATableCellEditor;
import com.viaoa.jfc.table.OATableCellRenderer;
import com.viaoa.jfc.table.OATableColumn;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.jfc.table.OATableListener;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAConv;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;


/**
    Used for building a Table of columns/rows listing Objects.  All columns are
    created by adding an OATableComponent as a column to the Table.
    Current components that support the OATableComponent interface,that can be used/added as table columns include:
    OACheckBox, OAComboBox, OALabel, OAPasswordField, OARadioButton, OATextField.
    <p>
    Full support for Drag and Drop (DND), and options to control how it works.
    <p>
    OATable supports a multi selection list, by supplying a second Hub that is used
    to contain the selected objects.
    <p>
    OATable allows for creating custom renderers.
    Each component can have its own renderer, and OATable has its own own renderer that
    is called for each cell.<br>
    Also see OATable.getRenderer(...) to be able to customize any cell.
    <p>
    &nbsp;&nbsp;&nbsp;<img src="doc-files/table.gif">
    <p>
    Example:<br>
    <p>
    Create an OATable that will display a list (Hub) of Employees
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    Hub hubDepartment = new Hub(Department.class);
    hubDepartment.setLink(hubEmployee);

    OATable table = new OATable();
    OALabel lbl = new OALabel(hubEmployee, "Id");
    table.addColumn("Id", 14, lbl);
    OATextField txt = new OATextField(hubEmployee, "firstName");
    table.addColumn("First Name", 14, txt);
    OAComboBox cbo = new OAComboBox(hubDepartment, "name");
    table.addColumn("Department", 22, cbo);
    OACheckBox chk = new OACheckBox(hubEmployee, "fullTime");
    table.addColumn("FT", 6, chk);
    table.setPreferredSize(6, 3, true); // 6 rows, 3 columns, plus width of scrollbar
    table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    table.sizeColumnsToFit(JTable.AUTO_RESIZE_LAST_COLUMN);
    panel.add(new JScrollPane(table));
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OATableComponent
*/
public class OATable extends JTable implements DragGestureListener, DropTargetListener {
    protected int prefCols=1, prefRows=5;
    protected Hub hub;
    protected Hub hubSelect;
    protected OATableModel oaTableModel;
    protected Vector columns = new Vector(5,5);
    protected MyHubAdapter hubAdapter;
    protected boolean includeScrollBar; // used by setPreferredSize
    protected boolean bAllowDrag = false;
    protected boolean bAllowDrop = false;
    protected boolean bRemoveDragObject;
    protected DropTarget dropTarget;
    protected DragSource dragSource = DragSource.getDefaultDragSource();
    final static DragSourceListener dragSourceListener = new MyDragSourceListener();
    protected ButtonHeaderRenderer headerRenderer; // 2006/10/13
    protected JPopupMenu popupMenu;  // used for alignment options
    protected boolean bAllowSorting;
    protected static Icon[] iconDesc;
    protected static Icon[] iconAsc;
    private boolean bEnableEditors=true;
    
    static {
        iconAsc = new Icon[4];
        iconDesc = new Icon[4];

        URL url = OAButton.class.getResource("icons/sortAsc.gif");
        if (url != null) iconAsc[0] = new ImageIcon(url);
        url = OAButton.class.getResource("icons/sortDesc.gif");
        if (url != null) iconDesc[0] = new ImageIcon(url);

        url = OAButton.class.getResource("icons/sortAsc1.gif");
        if (url != null) iconAsc[1] = new ImageIcon(url);
        url = OAButton.class.getResource("icons/sortDesc1.gif");
        if (url != null) iconDesc[1] = new ImageIcon(url);

        url = OAButton.class.getResource("icons/sortAsc2.gif");
        if (url != null) iconAsc[2] = new ImageIcon(url);
        url = OAButton.class.getResource("icons/sortDesc2.gif");
        if (url != null) iconDesc[2] = new ImageIcon(url);

        url = OAButton.class.getResource("icons/sortAsc3.gif");
        if (url != null) iconAsc[3] = new ImageIcon(url);
        url = OAButton.class.getResource("icons/sortDesc3.gif");
        if (url != null) iconDesc[3] = new ImageIcon(url);
    }
    
    
    /** Create a new Table. */
    public OATable() {
        this(true);
    }

    /** Create a new Table that is bound to a Hub. */
    public OATable(Hub hub) {
        this(hub, true);
    }

    /**
        Create a new Table that is bound to a Hub.
        @param bAddHack if true (default) then register [Enter] key and consume
    */
    public OATable(Hub hub, boolean bAddHack) {
        this(bAddHack);
        setHub(hub);
    }
    
    
    /**
        @param bAddHack if true (default) then register [Enter] key and consume
    */
    protected OATable(boolean bAddHack) {
        JTableHeader head = getTableHeader();
        head.setUpdateTableInRealTime(false);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // AUTO_RESIZE_LAST_COLUMN
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setDoubleBuffered(true);
        setPreferredScrollableViewportSize(new Dimension(150,100));
        setAutoCreateColumnsFromModel(false);
        setColumnSelectionAllowed(false);
        if (bAddHack) addHack();
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        Boolean b = new Boolean(true);
        Class c = b.getClass();
        setSurrendersFocusOnKeystroke(true);

        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,this);
        dropTarget = new DropTarget(this,this);
        
        // 20101031 have table fill the scrollpane viewport area, instead of just the rows area
        setFillsViewportHeight(true);
    }

    /** 2006/10/12    
     * If you would like to allow for sorting on a clicked column heading.  The
     * user can use [ctrl] to click on multiple headings.  Re-clicking on a heading
     * will changed from ascending to descending order.
     * @param b
     */
    public void setAllowSorting(boolean b) {
        bAllowSorting = b;
        /*
        if (headerRenderer == null) {
            headerRenderer = new ButtonHeaderRenderer(this);
            Enumeration e = columnModel.getColumns();
            for ( ;e.hasMoreElements(); ) {
                TableColumn col = (TableColumn) e.nextElement();
                col.setHeaderRenderer(headerRenderer);
            }
        }
        */
    }
    public boolean getAllowSorting() {
        return bAllowSorting;
    }    
    
    // 2006/12/29 called by superclass, this is overwritten from JTable
    protected JTableHeader createDefaultTableHeader() {
        headerRenderer = new ButtonHeaderRenderer(this);
        
        JTableHeader th = new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int pos = columnModel.getColumnIndexAtX(p.x);
                if (pos < 0) return null;
                pos = columnModel.getColumn(pos).getModelIndex();
                String tt = null;
                if (pos >= 0 && pos < columns.size()) {
                    OATableColumn tc = (OATableColumn) columns.elementAt(pos);
                    tt = OATable.this.getToolTipText(tc);
                }
                return tt;
            }
        };
        th.addMouseListener(new MouseAdapter() {
            Point pt;
            public void mousePressed(MouseEvent e) {
                pt = e.getPoint();
            }
            public void mouseReleased(MouseEvent e) {
                if (pt != null) onHeadingMouseReleased(e, pt);
                pt = null;
            }
        });
        return th;
    }
    
    // 2006/10/12    
    protected void performSort() {
        if (!bAllowSorting) return;
        String s = null;
        int x = columns.size();
        for (int i=1; ; i++) {
            boolean b = false;
            for (int j=0; j<x; j++) {
                OATableColumn col = (OATableColumn) columns.elementAt(j);
                col.getMethods(this.hub);  // make sure that path has been set up correctly, to match to table.hub
                if (col.sortOrder == i) {
                    if (s == null) s = col.path;
                    else s += ", " + col.path;
                    if (col.sortDesc) s += " DESC";
                    b = true;
                }
            }
            if (!b) break;
        }
        if (s != null) {
            hub.sort(s);
        }
    }
    
    /**
        Returns Hub that is bound to Table.
    */
    public Hub getHub() {
        return hub;
    }
    /**
        Sets Hub that is bound to Table.
    */
    public void setHub(Hub h) {
        this.hub = h;

        int x = columns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            tc.setMethods(null);
        }
        oaTableModel = new OATableModel(hub);
        setModel(oaTableModel);
        hubAdapter = new MyHubAdapter(hub, this);
    }


    // START Drag&Drop
    static class MyDragSourceListener implements DragSourceListener {
        public void dragEnter(DragSourceDragEvent e) {}
        public void dragOver(DragSourceDragEvent e) { }
        public void dropActionChanged(DragSourceDragEvent e) {}
        public void dragExit(DragSourceEvent e) {}
        public void dragDropEnd(DragSourceDropEvent e) {
        }
    }

    /**
        Flag to enable Drag and Drop. default=true
    */
    public void setAllowDnd(boolean b) {
        setAllowDrop(b);
        setAllowDrag(b);
    }
    /**
        Flag to enable Drag and Drop. default=true
    */
    public void setAllowDnD(boolean b) {
        setAllowDnd(b);
    }

    /**
        Flag to enable Drag and Drop. default=true
    */
    public void allowDnd(boolean b) {
        setAllowDnd(b);
    }
    /**
        Flag to enable Drag and Drop. default=true
    */
    public void allowDnD(boolean b) {
        setAllowDnd(b);
    }

    /**
        Flag to enable Dropping. default=true
    */
    public boolean getAllowDrop() {
        return bAllowDrop;
    }
    /**
        Flag to enable Dropping. default=true
    */
    public void setAllowDrop(boolean b) {
        bAllowDrop = b;
    }

    /**
        Flag to enable Dragging. default=true
    */
    public boolean getAllowDrag() {
        return bAllowDrag;
    }
    /**
        Flag to enable Dragging. default=true
    */
    public void setAllowDrag(boolean b) {
        bAllowDrag = b;
    }

    /**
        Flag to have a Dragged object removed from Hub. default=true
    */
    public void setRemoveDragObject(boolean b) {
        bRemoveDragObject = b;
    }
    /**
        Flag to have a Dragged object removed from Hub. default=true
    */
    public boolean getRemoveDragObject() {
        return bRemoveDragObject;
    }

    /** Used to support drag and drop (DND). */
    public void dragGestureRecognized(DragGestureEvent e) {
        if (!getAllowDrag()) return;
        if (hub != null) {
            Object obj = hub.getActiveObject();
            if (obj != null) {
                OATransferable t = new OATransferable(hub,obj);
                dragSource.startDrag(e, null, t, dragSourceListener);
            }
        }
    }

    /** Used to support drag and drop (DND). */
    public void dragEnter(DropTargetDragEvent e) {
        if (getAllowDrop() && e.isDataFlavorSupported(OATransferable.HUB_FLAVOR)) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
        else {
            e.rejectDrag();
        }
    }
    /** Used to support drag and drop (DND). */
    public void dragOver(DropTargetDragEvent e) {
    }
    /** Used to support drag and drop (DND). */
    public void dropActionChanged(DropTargetDragEvent e) {
    }
    /** Used to support drag and drop (DND). */
    public void dragExit(DropTargetEvent e) {
    }

    /** Used to support drag and drop (DND). */
    public void drop(DropTargetDropEvent e) {
        try {
            if (!e.getTransferable().isDataFlavorSupported(OATransferable.HUB_FLAVOR)) {
                return;
            }
            if (!getAllowDrop()) {
                return;
            }
            if (hub == null) {
                return;
            }
            // get object to move/copy
            Hub dragHub = (Hub) e.getTransferable().getTransferData(OATransferable.HUB_FLAVOR);
            Object dragObject = (Object) e.getTransferable().getTransferData(OATransferable.OAOBJECT_FLAVOR);

            Point pt = e.getLocation();
            int row = rowAtPoint(pt);
            if (row < 0) row = hub.getSize();

            Rectangle rect = getCellRect(row, 0, true);

            if (rect != null && pt.y > (rect.y + (rect.height/2))) row++;

            if ( hub.getObjectClass().isAssignableFrom(dragObject.getClass()) ) {
                int pos = hub.getPos(dragObject);
                if (pos >= 0 && !hub.isSorted()) {
                    // move
                    if (pos < row) row--;
                    hub.move(pos, row);
                    // 20091214
                    OAUndoManager.add(OAUndoableEdit.createUndoableMove(null, hub, pos, row));
                }
                else {
                    if (hub.isSorted()) {
                        hub.add(dragObject);
                        // 20091214
                        OAUndoManager.add(OAUndoableEdit.createUndoableAdd(null, hub, dragObject));
                    }
                    else {
                        hub.insert(dragObject, row);
                        // 20091214
                        OAUndoManager.add(OAUndoableEdit.createUndoableInsert(null, hub, dragObject, row));
                    }
                    if (getRemoveDragObject()) {
                        dragHub.remove(dragObject);
                    }
                }
                hub.setActiveObject(dragObject);
            }
            e.dropComplete(true);
        }
        catch (Exception ex) {
        }
    }
    //END Drag&Drop


    /**
        Seperate Hub that can contain selected objects.
        This will allow for a multi-select table.
    */
    public void setSelectHub(Hub hub) {
        this.hubSelect = hub;
        getSelectionModel().setSelectionMode( (hub==null)?ListSelectionModel.SINGLE_SELECTION:ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        hubAdapter.setSelectHub(hubSelect);
    }
    /**
        Seperate Hub that can contain selected objects.
        This will allow for a multi-select table.
    */
    public Hub getSelectHub() {
        return hubSelect;
    }





    /****
    public void setEditingRow(int aRow) {
        if (Hub.DEBUG) System.out.println("OATable.setEditingRow()");
        super.setEditingRow(aRow);
        if (aRow >= 0 && hub != null) {
            if (hub.getPos(hub.getActiveObject()) != aRow) {
                hub.setActiveObject(aRow);
                int pos = hub.getPos();
                if (pos != aRow) {
                    getCellEditor().stopCellEditing();
                    setRowSelectionInterval(pos,pos);
                }

            }
        }
    }
    ****/


    /**
        Number of columns that should be visible when determinng the preferred size of the Table.
    */
    public int getPreferredColumns() {
        return prefCols;
    }
    /**
        Number of columns that should be visible when determinng the preferred size of the Table.
    */
    public void setPreferredColumns(int cols) {
        setPreferredSize(cols,prefRows);
    }

    /**
        Number of rows that should be visible when determinng the preferred size of the Table.
    */
    public int getPreferredRows() {
        return prefRows;
    }
    /**
        Number of rows that should be visible when determinng the preferred size of the Table.
    */
    public void setPreferredRows(int rows) {
        setPreferredSize(prefCols,rows);
    }

    /**
        Number of columns and rows that should be visible when determinng the preferred size of the Table.
    */
    public void setPreferredSize(int rows, int cols) {
        this.setPreferredSize(rows,cols,true);
    }
    /**
        Number of columns and rows that should be visible when determinng the preferred size of the Table.
        @param includeScrollBar add the width of a scrollbar to the preferred width of the Table.
    */
    public void setPreferredSize(int rows, int cols, boolean includeScrollBar) {
        prefCols = ((cols > 0) ? cols : 1 );
        prefRows = ((rows > 0) ? rows : 1 );
        this.includeScrollBar = includeScrollBar;
        calcPreferredSize();
    }


    /**
        Used to have JTable.getCellRenderer(row, column) call OATable.getRenderer()
    */
    class MyTableCellRenderer implements TableCellRenderer {
        TableCellRenderer rend;
        OATable table;
        public MyTableCellRenderer(OATable t) {
            this.table = t;
        }
        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
            if (rend == null) return null;
            // 20080905 was: Component comp =  rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return getRenderer(table, value, isSelected, hasFocus, row, column);
        }
    }
    private MyTableCellRenderer myRend;

    
    
    /**
        JTable method used to get the renderer for a cell.  This is set up to
        automatically call getRenderer(), which is much easier to use.
        @see #getRenderer
    */
    public TableCellRenderer getCellRenderer(int row, int column) {
        // This will call MyTableCellRenderer.getTableCellRendererComponent(),
        //  which will then call OATable.getRenderer()
        if (myRend == null) myRend = new MyTableCellRenderer(this);
        myRend.rend = super.getCellRenderer(row, column);
        return myRend;
    }

    /**
        Can be overwritten to customize the component used to renderer a Table cell.
    */
    public Component getRenderer(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
        if (myRend != null) return myRend.rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        return null;
    }
    

    // listeners for customizing Renderers
    Vector vecListener;

    /**
        Add a listener that is called to customize the rendering component for a cell.
    */
    public void addListener(OATableListener l) {
        if (vecListener == null) vecListener = new Vector(2,2);
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }
    /**
        Remove a listener that is called to customize the rendering component for a cell.
    */
    public void removeListener(OATableListener l) {
        vecListener.removeElement(l);
    }
    public OATableListener[] getListeners() {
        if (vecListener == null) return null;
        OATableListener[] l = new OATableListener[vecListener.size()];
        vecListener.copyInto(l);
        return l;
    }


    /**
        Determine preferred size based on number of preferred number of columns and rows.
    */
    protected void calcPreferredSize() {
        int w = 0;
        int cols = prefCols;
        if (cols > columns.size()) cols = columns.size();

        for (int i=0; i<cols ;i++) {
            w += getColumnModel().getColumn(i).getWidth();
        }
        w += cols * getIntercellSpacing().width;
        w += 2;
        //was: int h = getIntercellSpacing().height + getRowHeight();
        // 20101027 rowHeight includes spacing
        int h = getRowHeight();
        h *= prefRows;
        if (w < 20) w = 20;
        if (h < 20) h = 20;

        if (includeScrollBar) w += 18; // scrollbar

        setPreferredScrollableViewportSize(new Dimension(w, h));

        // have table resized in layoutManager
        this.invalidate();
        Component c = this.getParent();
        if (c instanceof JViewport) {
            c = c.getParent();
            if (c != null && c instanceof JScrollPane) c = c.getParent();
        }
        if (c != null && c.getParent() != null) c = c.getParent();
        if (c != null) {
            c.invalidate();
            c.validate();
        }
    }

    /**
        Returns the column position for an OATableComponent.
    */
    public int getColumnIndex(OATableComponent c) {
        int x = columns.size();
        for (int i = 0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            if (tc.getOATableComponent() == c) return i;
        }
        return -1;
    }

    /**
        Change the heading for a column number.  First column is at postion 0.
    */
    public void setColumnHeading(int col, String heading) {
        if (col < columns.size() && col >= 0) {
            getColumnModel().getColumn(col).setHeaderValue(heading);
            invalidate();
            this.getParent().validate();
        }
    }

    /**
        Change the width for a column number, based on character width.  First column is at postion 0.
    */
    public void setColumnWidth(int col, int w) {
        if (col < columns.size() && col >= 0) {
            getColumnModel().getColumn(col).setWidth(w);
            calcPreferredSize();
        }
    }

    /**
        Set the property path used to display values for a column.
        This could be necessary when it can not be determined by the columns OATableComponent.
    */
    public void setColumnPropertyPath(int col, String propertyPath) {
        if (col < columns.size() && col >= 0) {
            OATableColumn tc = (OATableColumn) columns.elementAt(col);
            tc.setMethods(null);
            tc.path = propertyPath;
            tc.bIgnoreLink = true;
            if (oaTableModel != null) oaTableModel.fireTableStructureChanged();
        }
    }

    /**
        Create a new column using an OATableComponent.
    */
    public OATableColumn addColumn(OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(comp.getTableHeading(), -1, comp.getPropertyPath(), comp, c, -1, null);
    }
    /**
        Create a new column using an OATableComponent.
    */
    public OATableColumn add(OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(comp.getTableHeading(), -1, comp.getPropertyPath(), comp, c, -1, null);
    }


    /**
        Create a new column using an OATableComponent.
        @param heading column heading
        @param width of column based on average character width.
    */
    public OATableColumn addColumn(String heading, int width, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, width, comp.getPropertyPath(), comp, c, -1, null);
    }

    /**
        Create a new column using an OATableComponent.
        @param heading column heading
        @param width of column based on average character width.
    */
    public OATableColumn add(String heading, int width, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, width, comp.getPropertyPath(), comp, c, -1, null);
    }

    /**
        Create a new column using an OATableComponent.
        @param heading column heading
        @param width of column based on average character width.
        @param path  Set the property path used to display values for a column.
        This could be necessary when it can not be determined by the columns OATableComponent.
    */
    public OATableColumn addColumn(String heading, int width, String path, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        OATableColumn tc = this.addColumnMain(heading, width, path, comp, c, -1, null);
        tc.bIgnoreLink = true;
        return tc;
    }

    /**
        Create a new column using an OATableComponent.
        @param heading column heading
        @param width of column based on average character width.
        @param path  Set the property path used to display values for a column.
        This could be necessary when it can not be determined by the columns OATableComponent.
    */
    public OATableColumn add(String heading, int width, String path, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        OATableColumn tc = this.addColumnMain(heading, width, path, comp, c, -1, null);
        tc.bIgnoreLink = true;
        return tc;
    }

    /**
        Create a new column using an OATableComponent.
        @param heading column heading
        @param width of column based on average character width.
        @param path  Set the property path used to display values for a column.
        This could be necessary when it can not be determined by the columns OATableComponent.
        @param index column number, -1 to append to existing columns
    */
    public OATableColumn addColumn(String heading, int width, String path, OATableComponent comp, int index) {
        TableCellEditor c = comp.getTableCellEditor();
        OATableColumn tc = this.addColumnMain(heading, width, path, comp, c, index, comp.getFormat());
        tc.bIgnoreLink = true;
        return tc;
    }

    
    /**
        Create a new column using an OATableComponent.
        @param heading column heading
        @param width of column based on average character width.
        @param path  Set the property path used to display values for a column.
        This could be necessary when it can not be determined by the columns OATableComponent.
        @param index column number, -1 to append to existing columns
    */
    public OATableColumn add(String heading, int width, String path, OATableComponent comp, int index) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, width, path, comp, c, index, null);
    }

    /**
        Create a new column using a path.
        @param heading column heading
        @param width of column based on average character width.
        @param path  Set the property path used to display values for a column.
    */
    public OATableColumn addColumn(String heading, int width, String path) {
        return this.addColumnMain(heading,width,path,null,(TableCellEditor)null,-1,null);
    }
    /**
    Create a new column using a path.
    @param heading column heading
    @param width of column based on average character width.
    @param path  Set the property path used to display values for a column.
    */
    public OATableColumn addColumn(String heading, int width, String path, String fmt) {
        return this.addColumnMain(heading,width,path,null,(TableCellEditor)null,-1,fmt);
    }
    /**
        Create a new column using a path.
        @param heading column heading
        @param width of column based on average character width.
        @param path  Set the property path used to display values for a column.
    */
    public OATableColumn add(String heading, int width, String path) {
        return this.addColumnMain(heading,width,path,null,(TableCellEditor)null,-1,null);
    }
    /**
    Create a new column using a path.
    @param heading column heading
    @param width of column based on average character width.
    @param path  Set the property path used to display values for a column.
    */
    public OATableColumn add(String heading, int width, String path, String fmt) {
        return this.addColumnMain(heading,width,path,null,(TableCellEditor)null,-1,fmt);
    }

    static int averageCharWidth = 0;
    static int averageCharHeight = 0;
    static int lastFontSize = 0;
    /**
        Used to determine the pixel width based on the average width of a character.
    */
    public static int getCharWidth(Component comp, Font font, int x) {
        if (averageCharWidth == 0 || (font != null && font.getSize() != lastFontSize)) {
            if (font == null) {
System.out.println("OATable.getCharWidth=null, will use average=12 as default");
Exception e = new Exception("OATable.getCharWidth=null, will use average=12 as default");
e.printStackTrace();
                return (12 * x);
            }
            lastFontSize = font.getSize();
            FontMetrics fm = comp.getFontMetrics(font);
            averageCharWidth = (int) (fm.stringWidth("9XYZ") / 4);
        }
        return (averageCharWidth * x);
    }
    public static int getCharHeight(Component comp, Font font) {
        if (averageCharHeight == 0 || (font != null && font.getSize() != lastFontSize)) {
            lastFontSize = font.getSize();
            FontMetrics fm = comp.getFontMetrics(font);
            averageCharHeight = (int) fm.getHeight();
        }
        return (averageCharHeight);
    }

/*qqqqqqq later ...    
    public void addColumn(String heading, int width, OATableColumn oatc) {
        int pos = columns.size();
        columns.insertElementAt(oatc, pos);

        Font font = ((JComponent)oatc.oaComp).getFont();
        if (width < 0 && oatc.oaComp != null) {
            width = ((JComponent)oatc.oaComp).getPreferredSize().width;
            width /= getCharWidth((JComponent)oatc.oaComp, font, 1);
        }
        int w = OATable.getCharWidth(this,font,width);
        w += 8; // borders, etc.
        
        TableColumn tc = new TableColumn(pos);
        tc.setPreferredWidth(oatc.defaultWidth);
        tc.setWidth(w);
        tc.setCellEditor(oatc.comp);

        tc.setCellRenderer( new OATableCellRenderer(oatc) );
        tc.setHeaderValue(heading);
        tc.sizeWidthToFit();
        getColumnModel().addColumn(tc);

        column.tc = oatc;
        if (headerRenderer != null) tc.setHeaderRenderer(headerRenderer); // 2006/10/13
        
        tc.setHeaderRenderer(headerRenderer);  // 2006/12/29
        
        calcPreferredSize();
    }
**/
    
    /**
        Main method for adding a new Table Column.
    */
    protected OATableColumn addColumnMain(String heading, int width, String path, OATableComponent oaComp, TableCellEditor editComp, int index, String fmt) {
        Font font;
        if (oaComp != null) font = ((Component)oaComp).getFont();
        else font = getFont();

        if (width < 0 && oaComp != null) {
            width = ((JComponent)oaComp).getPreferredSize().width;
            width /= getCharWidth((JComponent)oaComp, font, 1);
        }
        int w = OATable.getCharWidth(this,font,width);
        w += 8; // borders, etc.

        TableCellRenderer rend = null;

        OATableColumn column = new OATableColumn(this, path, editComp, rend, oaComp, fmt);
        column.defaultWidth = width;
        if (oaComp != null) oaComp.setTable(this);

        int col = index;
        if (index == -1) col = columns.size();

        columns.insertElementAt( column, col );
        if (oaTableModel != null) oaTableModel.fireTableStructureChanged();

        TableColumn tc = new TableColumn(col);
        tc.setPreferredWidth(w);
        tc.setWidth(w);
        tc.setCellEditor(editComp);

        tc.setCellRenderer( new OATableCellRenderer(column) );
        tc.setHeaderValue(heading);
        tc.sizeWidthToFit();  // 2006/12/26
        getColumnModel().addColumn(tc);

        column.tc = tc; // 2006/10/12
        if (headerRenderer != null) tc.setHeaderRenderer(headerRenderer); // 2006/10/13
        
        tc.setHeaderRenderer(headerRenderer);  // 2006/12/29
        
        calcPreferredSize();
        return column;
    }

    /**
        Remove a column from Table.
    */
    public void removeColumn(int pos) {
        if (pos < 0) return;
        if (pos >= columns.size()) return;

        columns.removeElementAt(pos);
        getColumnModel().removeColumn(getColumnModel().getColumn(pos));
    }

    /*****
    protected void addImpl(Component comp,Object constraints,int index) {
        if (comp instanceof OATableComponent) {
            OATableComponent oacomp = (OATableComponent) comp;
            int w = oacomp.getColumns();
            if (w < 0) w = 8;
            addColumn(oacomp.getTableHeading(), w, oacomp.getPropertyPath(), oacomp, index);
        }
        else super.addImpl(comp,constraints,index);
    }

    public Component getComponent(int n) {
        if (!bDesignTime) return super.getComponent(n);
        int x = columns.size();
        for (int i=0,j=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            if (tc.oaComp != null) {
                if (j == n) return (Component) tc.oaComp;
                j++;
            }
        }
        return null;
    }
    public int getComponentCount() {
        if (!bDesignTime) return super.getComponentCount();
        int x = columns.size();
        int cnt=0;
        for (int i=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            if (tc.oaComp != null) cnt++;
        }
        return cnt;
    }
    public Component[] getComponents() {
        if (!bDesignTime) return super.getComponents();
        Component[] comps = new Component[getComponentCount()];
        int x = columns.size();
        for (int i=0,j=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            if (tc.oaComp != null) {
                comps[j] = (Component) tc.oaComp;
                j++;
            }
        }
        return comps;
    }

    public void remove(Component comp) {
        if (!bDesignTime) return super.remove(comp);
        int x = columns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            if (tc.comp == comp) removeColumn(i);
        }
    }

    public void removeAll() {
        int x = columns.size();
        for (int i=0; i<x; i++) {
            removeColumn(0);
        }
    }
    ******/


    /**
        Table Model using a Hub.
    */
    class OATableModel extends DefaultTableModel {
        Hub hub;
        public OATableModel(Hub hub) {
            this.hub = hub;
        }
        public int getColumnCount() {
            return columns.size();
        }

        public int getRowCount() {
            if (hub == null) return 0;
            return Math.abs( hub.getSize() );
        }

        public void fireTableStructureChanged() {
           super.fireTableStructureChanged();
        }

        public void fireTableRowsUpdated(int pos1, int pos2) {
           super.fireTableRowsUpdated(pos1, pos2);
        }

        public void fireTableRowsInserted(int firstRow, int lastRow) {
            super.fireTableRowsInserted(firstRow,lastRow);
        }
        public void fireTableRowsDeleted(int firstRow, int lastRow) {
            super.fireTableRowsDeleted(firstRow,lastRow);
        }

        public Class getColumnClass(int c) {
            Method[] ms = ((OATableColumn)columns.elementAt(c)).getMethods(hub);

            int i = ms.length;
            if (i == 0) return hub.getObjectClass();
            Method m = ms[i-1];
            Class cl = m.getReturnType();

            return OAReflect.getPrimitiveClassWrapper(cl);
        }

        public boolean isCellEditable(int rowIndex,int columnIndex) {
            boolean b = ( ((OATableColumn)columns.elementAt(columnIndex)).getTableCellEditor() != null );
            return b;
        }
        public void setValueAt(Object obj, int row, int col) {
            //no!   if (hub.getActiveObject() != hub.elementAt(row)) hub.setActiveObject(row);
            // do nothing, the editor component is object aware
        }

        boolean loadMoreFlag;
        boolean loadingMoreFlag;
        public Object getValueAt(int row, int col) {
            if (hub == null) return "";
            Object obj;
            int cnt = hub.getSize();


            if (hub.isMoreData()) {
                if ( row+5 >= cnt) {
                    if (!loadMoreFlag && !loadingMoreFlag) {
                        loadMoreFlag = true;
                        loadingMoreFlag = true;

                        if (isEditing()) getCellEditor().stopCellEditing(); //instead of "removeEditor();"
                        obj = hub.elementAt(row);
                        hub.elementAt(row+5);
                        hubAdapter.onNewList(null);

                        // make sure cell is visible
                        int pos = hub.getPos(obj);
                        if (pos < 0) pos = 0;
                        Rectangle cellRect;
                        cellRect = getCellRect(pos,col,true);
                        scrollRectToVisible(cellRect);
                        repaint();


                        pos = hub.getPos(hub.getActiveObject());
                        if (pos < 0) getSelectionModel().clearSelection();
                        else setRowSelectionInterval(pos,pos);

                        loadingMoreFlag = false;

                    }
                }
                else loadMoreFlag = false;
            }

            obj = hub.elementAt(row);
            if (obj == null) return "";//qqqqqq


            OATableColumn tc = (OATableColumn) columns.elementAt(col);
// 2006/02/09
obj = tc.getValue(hub, obj);
/* was:
            Method[] m = tc.getMethods(hub);
            if (tc.bLinkOnPos) {
                Method[] m2 = tc.methodsIntValue;
                obj = ClassModifier.getPropertyValue( obj, m2 );
                if (obj instanceof Number) {
                    obj = tc.oaComp.getHub().elementAt( ((Number)obj).intValue() );
                    obj = ClassModifier.getPropertyValue( obj, m );
                }
                else obj = "Invalid";
            }
            else obj = ClassModifier.getPropertyValue( obj, m );
*/
            return obj;
        }
    }

    //******************************** H A C K S ********************************************
    //******************************** H A C K S ********************************************
    //******************************** H A C K S ********************************************
    // Hack: this should to be called within the constructor
    private void addHack() {
        // this is needed so that other components that have called registerKeyboardAction()
        // wont get <enter> key
        registerKeyboardAction( new ActionListener() {
                public void actionPerformed(ActionEvent e) {   
                }
            },  KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction( new ActionListener() {
                public void actionPerformed(ActionEvent e) {  
                }
            },  KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        //10/18/99 jdk1.2  The new BasicTableUI ignores the [Enter], but we want to have it setFocus
        registerKeyboardAction( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    int row = getSelectedRow();
                    int col = getSelectedColumn();
                    editCellAt(row, col, e);
                    requestFocus();
                }
            },  KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);
        registerKeyboardAction( new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                }
            },  KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
    }

    protected AbstractButton cmdDoubleClick;

    /**
        Button to perform a doClick() when table clickCount == 2
    */
    public void setDoubleClickButton(AbstractButton cmd) {
        cmdDoubleClick = cmd;
    }
    /**
        Button to perform a doClick() when table clickCount == 2
    */
    public AbstractButton getDoubleClickButton() {
        return cmdDoubleClick;
    }

    /**
        Capture double click and call double click button.
        @see #getDoubleClickButton
    */
    @Override
    protected void processMouseEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            if (e.getClickCount() == 2) {
                if (cmdDoubleClick != null) cmdDoubleClick.doClick();
                onDoubleClick();
                return;
            }
        }
        if (e.getID() == MouseEvent.MOUSE_EXITED) {
            onMouseOver(-1,-1, e);
        }
        super.processMouseEvent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_MOVED) {
            int row = rowAtPoint(e.getPoint());
            int col = columnAtPoint(e.getPoint());
            onMouseOver(row, col, e);
        }
        super.processMouseMotionEvent(e);
    }
    
    public void onMouseOver(int row, int column, MouseEvent e) {
        
    }
    
    /**
        Method that is called whenever mouse click count = 2.
        Note: the activeObject of the clicked row will be the active object in the OATables Hub.
    */
    public void onDoubleClick() {

    }

    public void setEnableEditors(boolean b) {
        bEnableEditors = b;
    }
    public boolean getEnableEditors() {
        return bEnableEditors;
    }
    
    /** 
        Overwritten to set active object in Hub.
    */
    @Override
    public boolean editCellAt(int row, int column, java.util.EventObject e){
        // hack: editCellAt() will not hide the current cell editor if the new "column"
        //       does not have an editorComponent.  If this happens, then it will return
        //       false.

        
        if (hub.getPos() != row) {
            hub.setPos(row);
        }
        
        if (hubSelect != null) {
        }
        else {
            try {
                // Note: 20100529 calling setRowSelectionInterval(row,row), which will call valueChanged(evt) 
                //    will have e.getValueIsAdjusting() = true
                // was: setRowSelectionInterval(row,row); // this will not call setActiveObject(), since e.getValueIsAdjusting() will be true
            }
            catch (RuntimeException ex) {}
            if (hub.getPos() != row) {
                return false;  // cant change activeObject
            }
        }
    
        if (!bEnableEditors) return false;
        
        boolean b = super.editCellAt(row,column,e);
        
        // hack: if editCellAt() returned false and the old column had an editor, then
        //       we must stop it now.  Calling stopCellEditor() has no side effects, other
        //       then removing the editorComponent
    
    
        if (!b && isEditing()) {
            getCellEditor().stopCellEditing();
        }
        else {
            if (getCellEditor() instanceof OATableCellEditor) {
                ((OATableCellEditor)getCellEditor()).startCellEditing(e);
            }
            requestFocus();  // make sure component gets input
        }
        
        return b;
    }
    
    
    // hack: called by OATableCellEditor because  JTable.removeEditor() sets isEditing to false
    //       after it removes the component from the Table
    boolean checkFocusFlag = true;
    public void setCheckFocus(boolean b) {
        checkFocusFlag = b;
    }


    /**
        Overwritten to resume edit mode when focus is regained.
    */
    @Override
    public void requestFocus() {
        // hack: set focus to editorComponent if it is showing.
        if (checkFocusFlag && isEditing() && bEnableEditors) {
            getEditorComponent().requestFocus();
        }
        else {
            super.requestFocus();
        }
    }

//  ================== 2006/12/29 :) =============================    
    
    private OATableColumn popupTableColumn;
    private JMenuItem miAdd, miFind;
    private JMenu menuAddColumn, menuLoad, menuSave;
    private JCheckBoxMenuItem[] menuCheckBoxes;
    private JRadioButtonMenuItem[] menuSaveRadios;
    private JRadioButtonMenuItem[] menuLoadRadios;
    private OAProperties columnProperties;
    private String columnPrefix;
    private boolean bColumnPropertiesLoaded;
    private OATableColumn[][] tcColumnSetup = new OATableColumn[3][];
    private int[][] intColumnSetup = new int[3][];

    /**
     * The properties used to store the settings.
     * @param props 
     * @param name prefix used for storing properties, unique name for this table.
     */
    public void setColumnProperties(OAProperties props, String name) {
        columnProperties = props;
        columnPrefix = name;
        bColumnPropertiesLoaded = false;
    }

    protected void loadColumnProperties() {
        if (bColumnPropertiesLoaded || columnProperties == null || columnPrefix == null) return;
        bColumnPropertiesLoaded = true;
        for (int i=0; i<3; i++) {
            String line1 = columnProperties.getProperty(columnPrefix + ".setup" + (i+1) + ".columns");
            if (line1 == null) continue;
            String line2 = columnProperties.getProperty(columnPrefix + ".setup" + (i+1) + ".widths");
            if (line2 == null) continue;
            int x = OAString.dcount(line1, ",");

            tcColumnSetup[i] = new OATableColumn[x];
            intColumnSetup[i] = new int[x];
            
            for (int j=1; j<=x ;j++) {
                String w1 = OAString.field(line1, ',', j);
                if (w1 == null) break;
                w1 = w1.trim();
                String w2 = OAString.field(line2, ',', j);
                if (w2 == null) continue;
                int w = OAConv.toInt(w2);
                if (w <= 0) continue;
                intColumnSetup[i][j-1] = w;
                
                int kx = columns.size();
                for (int k=0; k<kx; k++) {
                    OATableColumn tc = (OATableColumn) columns.elementAt(k);
                    if (!w1.equalsIgnoreCase(tc.origPath)) continue;
                    tcColumnSetup[i][j-1] = tc;
                    break;
                }
            }
        }
    }

    protected void saveColumnProperties() {
        loadColumnProperties();
        if (!bColumnPropertiesLoaded) return;
        for (int i=0; i<3; i++) {
            if (tcColumnSetup[i] == null) continue;
            if (intColumnSetup[i] == null) continue;
            String line1 = "";
            String line2 = "";
            for (int j=0; j<tcColumnSetup[i].length; j++) {
                if (j > 0) {
                    line1 += ",";
                    line2 += ",";
                }
                if (tcColumnSetup[i][j] != null) {
                    line1 += tcColumnSetup[i][j].path;
                    line2 += intColumnSetup[i][j];
                }
            }
            columnProperties.put(columnPrefix + ".setup" + (i+1) + ".columns", line1);
            columnProperties.put(columnPrefix + ".setup" + (i+1) + ".widths", line2);
        }
        columnProperties.save();
    }
    
    protected void displayPopupMenu(OATableColumn tc, Point pt) {
        if (columnProperties == null) return;
        getPopupMenu();
        loadColumnProperties();
        
        // list of columns
        int x = columns.size();
        menuCheckBoxes = new JCheckBoxMenuItem[x];
        menuAddColumn.removeAll();
        for (int i=0; i<x; i++) {
            final OATableColumn tcx = (OATableColumn) columns.elementAt(i);
            menuCheckBoxes[i] = new JCheckBoxMenuItem(tcx.tc.getHeaderValue()+"", tcx.bVisible);
            menuCheckBoxes[i].addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    JCheckBoxMenuItem chk = (JCheckBoxMenuItem) e.getSource();
                    if (chk.isSelected()) onAddColumn(tcx);
                    else onRemoveColumn(tcx);
                }
            });
            OATableComponent tce = tcx.getOATableComponent();
            if (tce instanceof JComponent) {
                String s = ((JComponent) tce).getToolTipText();
                if (s != null && s.trim().length() > 0) menuCheckBoxes[i].setToolTipText(s);
            }
            menuAddColumn.add(menuCheckBoxes[i]);
        }
        
        // list of Save As
        if (!bColumnPropertiesLoaded) {
            menuSave.setVisible(false);
        }
        else {
            menuSave.setVisible(true);
            menuSaveRadios = new JRadioButtonMenuItem[3];
            menuSave.removeAll();
            ButtonGroup grp = new ButtonGroup();
            for (int i=0; i<3; i++) {
                menuSaveRadios[i] = new JRadioButtonMenuItem("Setup #" + (i+1), false);
                grp.add(menuSaveRadios[i]);
                menuSaveRadios[i].addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        JRadioButtonMenuItem rad = (JRadioButtonMenuItem) e.getSource();
                        if (rad.isSelected()) {
                            for (int i=0; i<menuSaveRadios.length; i++) {
                                if (menuSaveRadios[i] == rad) {
                                    saveColumnSetup(i);
                                    break;
                                }
                            }
                        }
                    }
                });
                menuSave.add(menuSaveRadios[i]);
            }
        }
        
        // list of Load
        menuLoadRadios = new JRadioButtonMenuItem[4];
        menuLoad.removeAll();
        ButtonGroup grp = new ButtonGroup();
        for (int i=0; i<4; i++) {
            if (i == 0) menuLoadRadios[i] = new JRadioButtonMenuItem("Default", false);
            else {
                menuLoadRadios[i] = new JRadioButtonMenuItem("Setup #" + i, false);
                menuLoadRadios[i].setEnabled(tcColumnSetup[i-1] != null);
            }
            grp.add(menuLoadRadios[i]);
            menuLoadRadios[i].addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    JRadioButtonMenuItem rad = (JRadioButtonMenuItem) e.getSource();
                    if (rad.isSelected()) {
                        if (rad == menuLoadRadios[0]) {
                            onLoadDefault();
                        }
                        else {
                            for (int i=0; i<menuLoadRadios.length; i++) {
                                if (menuLoadRadios[i] == rad) {
                                    loadColumnSetup(i-1);
                                    break;
                                }
                            }
                        }
                    }
                }
            });
            menuLoad.add(menuLoadRadios[i]);
            if (!bColumnPropertiesLoaded) break;
        }
        
        this.popupTableColumn = tc;
        getPopupMenu().show(OATable.this, pt.x, pt.y);
    }

    protected void saveColumnSetup(int pos) {
        if (pos > 2 || pos < 0) return;
        
        int x = columnModel.getColumnCount();
        tcColumnSetup[pos] = new OATableColumn[x];
        intColumnSetup[pos] = new int[x];
        
        OATableColumn[] tcs = new OATableColumn[x];
        for (int i=0; i<x; i++) {
            TableColumn tc = columnModel.getColumn(i);
            int x2 = columns.size();
            for (int i2=0; i2<x2; i2++) {
                OATableColumn tcx = (OATableColumn) columns.elementAt(i2);
                if (tcx.tc == tc) {
                    tcx.currentWidth = tcx.tc.getWidth();
                    tcColumnSetup[pos][i] = tcx;
                    intColumnSetup[pos][i] = tcx.currentWidth;
                }
            }
        }
        saveColumnProperties();     
    }

    protected void loadColumnSetup(int pos) {
        if (pos > 2 || pos < 0) return;
        if (tcColumnSetup[pos] == null) return;
        for ( ;columnModel.getColumnCount()>0; ) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }
        int x = columns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tcx = (OATableColumn) columns.elementAt(i);
            tcx.bVisible = false;
        }
        x = tcColumnSetup[pos].length;
        for (int i=0; i<x; i++) {
            OATableColumn tcx = tcColumnSetup[pos][i];
            tcx.bVisible = true;
            tcx.tc.setWidth(intColumnSetup[pos][i]);
            columnModel.addColumn(tcx.tc);
        }
    }
    
    protected void onAddColumn(OATableColumn tc) {
        if (tc != null) {
            tc.bVisible = true;

            int x = columns.size();
            int pos = 0;
            columnModel.addColumn(tc.tc);
            for (int i=0; i<x; i++) {
                OATableColumn tcx = (OATableColumn) columns.elementAt(i);
                if (tcx == tc) {
                    int cnt = columnModel.getColumnCount();
                    if (pos != cnt-1) {
                        columnModel.moveColumn(cnt-1, pos);
                    }
                    return;
                }
                if (tcx.bVisible) pos++;
            }
        }
    }

    protected void onRemoveColumn(OATableColumn tc) {
        if (tc != null) {
            tc.bVisible = false;
            columnModel.removeColumn(tc.tc);
        }
    }
    
    protected void onLoadDefault() {
        for ( ;columnModel.getColumnCount()>0; ) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }
        int x = columns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tcx = (OATableColumn) columns.elementAt(i);
            tcx.bVisible = tcx.bDefault;
            tcx.tc.setWidth(tcx.defaultWidth);
            if (tcx.bVisible) columnModel.addColumn(tcx.tc);
        }
    }
    
    protected void onFind() {
    }

    protected JPopupMenu getPopupMenu() {
        if (popupMenu != null) return popupMenu;
        popupMenu = new JPopupMenu("Options");
        JMenu menu;

        menuAddColumn = new JMenu("Select Columns");
        popupMenu.add(menuAddColumn);
/*
        miFind = new JMenuItem("Find ...");
        miFind.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onFind();
            }
        });
        popupMenu.add(miFind);
*/        
        popupMenu.addSeparator();
        
        menuLoad = new JMenu("Load");
        popupMenu.add(menuLoad);

        menuSave = new JMenu("Save As");
        popupMenu.add(menuSave);
        
        return popupMenu;
    }

    public void setSortColumn(OATableComponent oaComp, boolean bAsc, int pos) {
        OATableColumn tc;
        for (int i=0; i<columns.size(); i++) {
            tc = (OATableColumn) columns.elementAt(i);
            if (tc.getOATableComponent() == oaComp) {
                tc.sortOrder = pos;
                tc.sortDesc = !bAsc;
                break;
            }
        }
        getTableHeader().repaint();
    }
    
    public void removeSort() {
        OATableColumn tc;
        for (int i=0; i<columns.size(); i++) {
            tc = (OATableColumn) columns.elementAt(i);
            tc.sortOrder = 0;
        }
        getTableHeader().repaint();
    }
    
    public int getDisplayedColumnCount() {
        return columnModel.getColumnCount();
    }

    public OATableComponent getDisplayedColumnComponent(int pos) {
        int x = columnModel.getColumnCount();
        if (pos >= x) return null;
        TableColumn tc = columnModel.getColumn(pos);
        x = columns.size();
        for (int i=0; i<x; i++) {
            OATableColumn tcx = (OATableColumn) columns.elementAt(i);
            if (tcx.tc == tc) return tcx.getOATableComponent();
        }
        return null;
    }

    public int getDisplayedColumnWidth(int pos) {
        int x = columnModel.getColumnCount();
        if (pos >= x) return 0;
        TableColumn tc = columnModel.getColumn(pos);
        return tc.getWidth();
    }

    protected String getToolTipText(OATableColumn tc) {
        String s = null;
        if (tc != null && tc.getOATableComponent() instanceof JComponent) {
            s = ((JComponent) tc.getOATableComponent()).getToolTipText();
            s = getToolTipText(tc.getOATableComponent(), s);
            if (s == null || s.length() == 0) {
                if (tc.tc.getHeaderValue() != null) s = tc.tc.getHeaderValue().toString();
            }
        }
        return s;
    }

    protected String getToolTipText(OATableComponent comp, String tt) {
        return tt;
    }

    protected void onHeadingRightClick(OATableColumn tc, Point pt) {
        if (columnProperties != null) {
            displayPopupMenu(tc, pt);
        }
    }

    protected void onHeadingClick(OATableColumn tc, MouseEvent e, Point pt) {
        if (!bAllowSorting) return;
        if (tc == null) return;
        tc.setupTableColumn();
        
        if (e.isControlDown() || e.isShiftDown()) {
            if (tc.sortOrder == 0) {
                int max = 0;
                for (int i=0; i<columns.size(); i++) {
                    OATableColumn col = (OATableColumn) columns.elementAt(i);
                    tc.sortOrder = Math.max(tc.sortOrder, col.sortOrder);
                }
                tc.sortOrder++;
            }
            else tc.sortDesc = !tc.sortDesc;
        }
        else {
            if (tc.sortOrder > 0) {
                boolean b = false;
                for (int i=0; !b && i<columns.size(); i++) {
                    OATableColumn col = (OATableColumn) columns.elementAt(i);
                    if (col != tc & col.sortOrder > 0) b = true;
                }
                if (!b) tc.sortDesc = !tc.sortDesc;
            }
            tc.sortOrder = 1;
            for (int i=0; i<columns.size(); i++) {
                OATableColumn col = (OATableColumn) columns.elementAt(i);
                if (col != tc) {
                    col.sortOrder = 0;
                    col.sortDesc = false;
                }
            }
        }
        OATable.this.performSort();
        OATable.this.getTableHeader().repaint();
    }

    protected void onHeadingMouseReleased(MouseEvent e, Point pt) {
        if (pt != null && !e.isPopupTrigger()) {
            Rectangle rec = new Rectangle(pt.x-3, pt.y-3, 6, 6);
            Point pt2 = e.getPoint();
            if (!rec.contains(pt2)) return;
            // if (pt2 != null && (pt.x != pt2.x || pt.y != pt2.y)) return;
        }

        int pos = columnModel.getColumnIndexAtX(e.getX());
        if (pos < 0) return;
        pos = columnModel.getColumn(pos).getModelIndex();
        OATableColumn tc = null;
        if (pos >= 0 && pos < columns.size()) {
            tc = (OATableColumn) columns.elementAt(pos);
        }
        if (tc == null) return;
        if ((e.getModifiers() & Event.META_MASK) !=0) {
            if (!e.isPopupTrigger()) return;
        }
        if (e.isPopupTrigger()) {
            if (tc != null) onHeadingRightClick(tc, pt);
            return;
        }
        if (tc != null) onHeadingClick(tc, e, pt);
    }

//  END END END END END END ===== 2006/12/29 CONSTRUCTION ZONE :) ===== END END END END END END
//  END END END END END END ===== 2006/12/29 CONSTRUCTION ZONE :) ===== END END END END END END
//  END END END END END END ===== 2006/12/29 CONSTRUCTION ZONE :) ===== END END END END END END
//  END END END END END END ===== 2006/12/29 CONSTRUCTION ZONE :) ===== END END END END END END

    
    // 20101031 improve the look when table does not take up all of viewport
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();
        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                viewport.setBackground(getBackground());

                /*
                JPanel pan = new JPanel(new BorderLayout());                
                pan.add(getTableHeader(), BorderLayout.WEST);
    
                JButton cmd = new JButton("");
                pan.add(cmd, BorderLayout.CENTER);
                
                pan.setBackground(getBackground());
                scrollPane.setColumnHeaderView(pan);
                */
            }
        }
    }


}

/**
    Class used to bind Table to a Hub.
*/
class MyHubAdapter extends Hub2Gui implements ListSelectionListener {
    OATable table;
    Hub hubSelect;
    private HubListenerAdapter hlSelect;

    boolean bIgnoreValueChanged;  // used to ignore calls to valueChanged(...) 
    boolean bRunningValueChanged; // flag set when valueChanged is running

    public MyHubAdapter(Hub hub, OATable table) {
        setHub(hub);
        this.table = table;
        table.getSelectionModel().addListSelectionListener(this);
        getHub().addHubListener(this);
        afterChangeActiveObject(null);
    }


    protected void setSelectHub(Hub hubSelect) {
        if (this.hubSelect != null && hlSelect != null) {
            this.hubSelect.removeHubListener(hlSelect);
            hlSelect = null;
        }
        this.hubSelect = hubSelect;
        if (hubSelect == null) return;
        hlSelect = new HubListenerAdapter() {
            public @Override void afterAdd(HubEvent e) {
                Object obj = e.getObject();
                if (obj == null || hub == null) return;
                if (bRunningValueChanged) return;
                int pos = hub.getPos(obj);
                if (pos >= 0) {
                    bIgnoreValueChanged = true;
                    ListSelectionModel lsm = table.getSelectionModel();
                    lsm.addSelectionInterval(pos, pos);
                    bIgnoreValueChanged = false;
                }
            }
            public @Override void afterInsert(HubEvent e) {
                afterAdd(e);
            }
            public @Override void afterRemove(HubEvent e) {
                if (bRunningValueChanged) return;
                int pos = e.getPos();
                if (pos >= 0) {
                    bIgnoreValueChanged = true;
                    ListSelectionModel lsm = table.getSelectionModel();
                    lsm.removeSelectionInterval(pos, pos);
                    bIgnoreValueChanged = false;
                }
            }
            public @Override void onNewList(HubEvent e) {
                rebuildListSelectionModel();
            }
        };
        hubSelect.addHubListener(hlSelect);
        rebuildListSelectionModel();
    }
    private void rebuildListSelectionModel() {
        // update hubSelect, to see if objects are in table.hub
        if (hubSelect == null) return;

        ListSelectionModel lsm = table.getSelectionModel();
        bIgnoreValueChanged = true;
        lsm.clearSelection();
        for (int i=0;  ;i++) {
            Object obj = hubSelect.getAt(i);
            if (obj == null) break;
            int pos = hub.indexOf(obj);  // dont use hub.getPos(), since it will adjust "linkage"
            if (pos < 0) {
                hubSelect.removeAt(i);
                i--;
            }
            else {
                lsm.addSelectionInterval(i, i);
            }
        }
        bIgnoreValueChanged = false;
    }


    public synchronized void valueChanged(ListSelectionEvent e)  {
        if (bIgnoreValueChanged) return;
        if (e.getValueIsAdjusting()) return;

        int row = table.getSelectedRow();
        if (row < 0 || !table.getSelectionModel().isSelectedIndex(row)) return;

        bRunningValueChanged = true;
        getHub().setActiveObject(row);

        if (hubSelect != null) {
            int p1 = e.getFirstIndex();
            int p2 = e.getLastIndex();
            ListSelectionModel lsm = table.getSelectionModel();
            int beginAdd = hubSelect.getSize();
            for (int i=p1; i<=p2; i++) {
                if (!lsm.isSelectedIndex(i)) continue;
                Object obj = table.hub.elementAt(i);
                if (obj == null) continue;
                if (!hubSelect.contains(obj)) {
                    hubSelect.add(obj);
                }
            }
            // remove all objects in hubSelect that are no longer selected
            for (int i=0; i<beginAdd ;i++) {
                Object obj = hubSelect.getAt(i);
                if (obj == null) break;
                int pos = hub.getPos(obj);
                if (lsm.isSelectedIndex(pos)) continue;
                hubSelect.removeAt(i);
                i--;
            }
        }
        else {
            int pos = getHub().getPos();
            if (pos != row) {  // if the hub.pos is not the same, set it back
                bRunningValueChanged = false;
                table.setRowSelectionInterval(pos,pos);
            }
        }
        bRunningValueChanged = false;
    }

    public @Override void onNewList(HubEvent e) {
        table.oaTableModel.fireTableStructureChanged();
        int x = getHub().getPos();
        if (x >= 0) setSelectedRow(x);
        else {
            Rectangle cellRect = new Rectangle(0,0,10,10);
            table.scrollRectToVisible(cellRect);
            // table.repaint();
        }

        // update hubSelect, to see if objects are in table.hub
        rebuildListSelectionModel();
    }

    
    public @Override void afterSort(HubEvent e) {
        table.oaTableModel.fireTableStructureChanged();

        int x = getHub().getPos();
        if (x >= 0) setSelectedRow(x);
        else {
            Rectangle cellRect = new Rectangle(0,0,10,10);
            table.scrollRectToVisible(cellRect);
            // table.repaint();
        }
        // table.repaint();
        
        rebuildListSelectionModel();
    }

    public @Override void afterMove(HubEvent e) {
        table.oaTableModel.fireTableRowsUpdated(e.getPos(), e.getToPos());
        table.repaint();
        afterChangeActiveObject(e);
        rebuildListSelectionModel();
    }


    public @Override synchronized void afterChangeActiveObject(HubEvent e) {
        int row = getHub().getPos();
        if (table.hubSelect == null) {
            setSelectedRow(row);
        }
    }

    protected void setSelectedRow(final int row) {
        if (bRunningValueChanged) return;
        if (SwingUtilities.isEventDispatchThread()) {
            _setSelectRow(row);
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    _setSelectRow(row);
                }
            });
        }
    }

    private void _setSelectRow(int row) {
        bIgnoreValueChanged = true;
        if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing();
        if (row < 0) {
            table.getSelectionModel().clearSelection();
        }
        else {
            row = getHub().getPos();
            try {
                table.setRowSelectionInterval(row,row);
            }
            catch (Exception e) {  // IllegalArgument: row index out of range.  Happens when Hub is changed
                return;
            }


            // 20101029 this would scroll to leftmost AO
            
            Rectangle cellRect;
            if (row < 0) cellRect = new Rectangle(0,0,10,10);
            else {
                Container cont = table.getParent();
                cellRect = table.getCellRect(row, 0, true);
                if (cont instanceof JViewport) {
                    cellRect.x = ((JViewport) cont).getViewPosition().x;
                    cellRect.width = 5;
                }
            }
            
            
            if (cellRect != null) table.scrollRectToVisible(cellRect);
            table.repaint();
            
        }
        bIgnoreValueChanged = false;

    }

    public @Override void afterPropertyChange(HubEvent e) {
        // if (Hub.DEBUG) System.out.println("OATable.hubpropertyChange()");//qqqqq
        if (!(e.getObject() instanceof OAObject)) return;
        
        //was: if ( ((OAObject)e.getObject()).isProperty(e.getPropertyName())) {
        table.repaint();
    }

    protected void removeInvoker(final int pos) {
        if (SwingUtilities.isEventDispatchThread()) {
            bIgnoreValueChanged = true;
            table.oaTableModel.fireTableRowsDeleted(pos,pos);
            bIgnoreValueChanged = false;
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    bIgnoreValueChanged = true;
                    table.oaTableModel.fireTableRowsDeleted(pos,pos);
                    bIgnoreValueChanged = false;
                }
            });
        }
    }
    /** 20090702, replaced with beforeRemove(), since activeObject is changed before afterRemove is called
    public @Override void afterRemove(HubEvent e) {
        removeInvoker(e.getPos());
    }
    */
    public @Override void beforeRemove(HubEvent e) {
        removeInvoker(e.getPos());
    }

    @Override
    public void afterRemove(HubEvent e) {
        rebuildListSelectionModel();
    }

    protected void insertInvoker(final int pos) {
        if (SwingUtilities.isEventDispatchThread()) {
            bIgnoreValueChanged = true;
            table.oaTableModel.fireTableRowsInserted(pos,pos);
            bIgnoreValueChanged = false;
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    bIgnoreValueChanged = true;
                    table.oaTableModel.fireTableRowsInserted(pos,pos);
                    bIgnoreValueChanged = false;
                }
            });
        }
    }

    public @Override void afterInsert(HubEvent e) {
        insertInvoker(e.getPos());
    }

    public @Override void afterAdd(HubEvent e) {
        if (getHub() != null && !HubSelectDelegate.isFetching(getHub())) {
            insertInvoker(e.getPos());
        }
    }
    
    public @Override void afterFetchMore(HubEvent e) {
        onNewList(e);
    }

}

// 2006/10/13
class ButtonHeaderRenderer extends JButton implements TableCellRenderer {
    OATable table;
    public ButtonHeaderRenderer(OATable t) {
        this.table = t;
        setMargin(new Insets(0,0,0,0));
        this.setHorizontalTextPosition(SwingConstants.LEFT);
    }
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setText((value ==null) ? "" : value.toString());

        TableColumn jtc = table.getTableHeader().getColumnModel().getColumn(column);
        
        OATableColumn tc = (OATableColumn) this.table.columns.elementAt(column);
        // 2006/11/28
        if (tc.tc != jtc) {
            int x = this.table.columns.size();
            for (int i=0; i<x; i++) {
                tc = (OATableColumn) this.table.columns.elementAt(i);
                if (tc.tc == jtc) break;
            }
        }
        if (tc == null) {  // should not happen
            tc = (OATableColumn) this.table.columns.elementAt(column);
        }
        
        Icon icon = null;
        if (tc.sortOrder > 0) {
            int pos = tc.sortOrder;
            if (tc.sortOrder == 1) {
                pos = 0;
                int x = this.table.columns.size();
                for (int i=0; i<x;i++) {
                    OATableColumn tcx = (OATableColumn) this.table.columns.elementAt(i);
                    if (tcx.sortOrder > 0 && tcx != tc) {
                        pos = 1;
                        break;
                    }
                }
            }
            else if (pos > 3) pos = 0;
            if (tc.sortDesc) icon = this.table.iconDesc[pos];
            else icon = this.table.iconAsc[pos];
        }
        setIcon(icon);
        return this;
    }

}






