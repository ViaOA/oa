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


import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubAODelegate;
import com.viaoa.hub.HubDataDelegate;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubFilter;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.hub.HubSelectDelegate;
import com.viaoa.jfc.border.CustomLineBorder;
import com.viaoa.jfc.control.JFCController;
import com.viaoa.jfc.dnd.OATransferable;
import com.viaoa.jfc.table.OATableCellEditor;
import com.viaoa.jfc.table.OATableCellRenderer;
import com.viaoa.jfc.table.OATableColumn;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.jfc.table.OATableFilterComponent;
import com.viaoa.jfc.table.OATableListener;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.object.OAObject;
import com.viaoa.util.OACompare;
import com.viaoa.util.OAConv;
import com.viaoa.util.OANullObject;
import com.viaoa.util.OAProperties;
import com.viaoa.util.OAReflect;
import com.viaoa.util.OAString;

/**
 * Used for building a Table of columns/rows listing Objects. All columns are created by adding an
 * OATableComponent as a column to the Table. Current components that support the OATableComponent
 * interface,that can be used/added as table columns include: OACheckBox, OAComboBox, OALabel,
 * OAPasswordField, OARadioButton, OATextField.
 * <p>
 * Full support for Drag and Drop (DND), and options to control how it works.
 * <p>
 * OATable supports a multi selection list, by supplying a second Hub that is used to contain the
 * selected objects.
 * <p>
 * OATable allows for creating custom renderers. Each component can have its own renderer, and OATable
 * has its own own renderer that is called for each cell.<br>
 * Also see OATable.getRenderer(...) to be able to customize any cell.
 * <p>
 * &nbsp;&nbsp;&nbsp;<img src="doc-files/table.gif">
 * <p>
 * Example:<br>
 * <p>
 * Create an OATable that will display a list (Hub) of Employees
 * 
 * <pre>
 * Hub hubEmployee = new Hub(Employee.class);
 * Hub hubDepartment = new Hub(Department.class);
 * hubDepartment.setLink(hubEmployee);
 * 
 * OATable table = new OATable();
 * OALabel lbl = new OALabel(hubEmployee, &quot;Id&quot;);
 * table.addColumn(&quot;Id&quot;, 14, lbl);
 * OATextField txt = new OATextField(hubEmployee, &quot;firstName&quot;);
 * table.addColumn(&quot;First Name&quot;, 14, txt);
 * OAComboBox cbo = new OAComboBox(hubDepartment, &quot;name&quot;);
 * table.addColumn(&quot;Department&quot;, 22, cbo);
 * OACheckBox chk = new OACheckBox(hubEmployee, &quot;fullTime&quot;);
 * table.addColumn(&quot;FT&quot;, 6, chk);
 * table.setPreferredSize(6, 3, true); // 6 rows, 3 columns, plus width of scrollbar
 * table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 * panel.add(new JScrollPane(table));
 * 
 * table.getTableHeader().setReorderingAllowed(false);
 * 
 * Note: use this to get the original column position
 *  col = columnModel.getColumn(col).getModelIndex();
 * </pre>
 * <p>
 * For more information about this package, see <a
 * href="package-summary.html#package_description">documentation</a>.
 * 
 * @see OATableComponent
 */
public class OATable extends JTable implements DragGestureListener, DropTargetListener {
    private static Logger LOG = Logger.getLogger(OATable.class.getName());
    
    protected int prefCols = 1, prefRows = 5;
    protected Hub hub;
    protected HubFilter hubFilter;
    protected Hub hubSelect;
    protected OATableModel oaTableModel;
    protected Vector<OATableColumn> columns = new Vector<OATableColumn>(5, 5);
    protected MyHubAdapter hubAdapter;
    protected boolean includeScrollBar; // used by setPreferredSize
    protected boolean bAllowDrag = false;
    protected boolean bAllowDrop = false;
    protected boolean bRemoveDragObject;
    protected DropTarget dropTarget;
    protected DragSource dragSource = DragSource.getDefaultDragSource();
    final static DragSourceListener dragSourceListener = new MyDragSourceListener();
    protected PanelHeaderRenderer headerRenderer; // 2006/10/13
    protected JPopupMenu popupMenu; // used for alignment options
    protected boolean bAllowSorting;
    protected static Icon[] iconDesc;
    protected static Icon[] iconAsc;
    private boolean bEnableEditors = true;
    public boolean bDEBUG;

    public static final Color COLOR_Odd = UIManager.getColor("Table.background");
    public static final Color COLOR_Even = new Color(249, 255, 255);
    public static final Color COLOR_Focus = UIManager.getColor("Table.foreground");
    public static final Color COLOR_MouseOver = new Color(0, 0, 110);

    public static final Color COLOR_Change_Foreground = Color.yellow;
    public static final Color COLOR_Change_Background = new Color(0, 0, 105);
    public static final Border BORDER_Change = new LineBorder(COLOR_Change_Background, 1);

    public static final Color COLOR_Focus_Forground = UIManager.getColor("Table.background");
    public static final Border BORDER_Focus = new LineBorder(Color.white, 1);
    
    
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
     * Create a new Table that is bound to a Hub.
     * 
     * @param bAddHack
     *            if true (default) then register [Enter] key and consume
     */
    public OATable(Hub hub, boolean bAddHack) {
        this(bAddHack);
        setHub(hub);
    }

    /**
     * @param bAddHack
     *            if true (default) then register [Enter] key and consume
     */
    protected OATable(boolean bAddHack) {
        JTableHeader head = getTableHeader();
        head.setUpdateTableInRealTime(false);

        setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // AUTO_RESIZE_LAST_COLUMN
        getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setDoubleBuffered(true);
        setPreferredScrollableViewportSize(new Dimension(150, 100));
        setAutoCreateColumnsFromModel(false);
        setColumnSelectionAllowed(false);
        if (bAddHack) addHack();
        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        Boolean b = new Boolean(true);
        Class c = b.getClass();
        setSurrendersFocusOnKeystroke(true);

        setIntercellSpacing(new Dimension(4, 1));

        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
        dropTarget = new DropTarget(this, this);

        // 20101031 have table fill the scrollpane viewport area, instead of just the rows area
        setFillsViewportHeight(true);
    }

    /**
     * 2006/10/12 If you would like to allow for sorting on a clicked column heading. The user can use
     * [ctrl] to click on multiple headings. Re-clicking on a heading will changed from ascending to
     * descending order.
     * 
     * @param b
     */
    public void setAllowSorting(boolean b) {
        bAllowSorting = b;
        /*
         * if (headerRenderer == null) { headerRenderer = new ButtonHeaderRenderer(this); Enumeration e
         * = columnModel.getColumns(); for ( ;e.hasMoreElements(); ) { TableColumn col = (TableColumn)
         * e.nextElement(); col.setHeaderRenderer(headerRenderer); } }
         */
    }

    public boolean getAllowSorting() {
        return bAllowSorting;
    }

    /**
     * Columns will be resized to be at least the size of the heading text.
     */
    public void resizeColumnsToFitHeading() {
    
        FontMetrics fm = this.getFontMetrics(getFont());
        
        for (OATableColumn tc : getAllTableColumns()) {
            int w = tc.tc.getWidth();
            String s = (String) tc.tc.getHeaderValue();
            int w2 = fm.stringWidth(s);
            
            if (w < w2+8) {
                tc.tc.setPreferredWidth(w2 + 8);
                tc.tc.setWidth(w2+8);
                tc.defaultWidth = w2 + 8;
            }
        }
    }
    
    
    @Override
    public String getToolTipText(MouseEvent event) {
        String s = super.getToolTipText(event);
        if (event != null) {
            Point pt = event.getPoint();

            int col = columnAtPoint(pt);
            int row = rowAtPoint(pt);

            OATable t = getRightTable();
            if (t != null) {
                s = t.getToolTipText1(row, col, s);
            }
            else {
                t = getLeftTable();
                if (t != null) {
                    col += t.getColumnCount();
                }
                s = getToolTipText1(row, col, s);
            }
        }
        return s;
    }

    private int cntTT;

    private String getToolTipText1(int row, int col, String defaultValue) {
        OATableColumn[] tcs = getAllTableColumns();
        if (col >= 0 && col < tcs.length) {
            OATableColumn tc = (OATableColumn) tcs[col];
            defaultValue = tc.getToolTipText(row, col, defaultValue);
        }
        defaultValue = getToolTipText(row, col, defaultValue);
        if (!OAString.isEmpty(OAString.trim(defaultValue))) {
            if (cntTT++ % 2 == 0) defaultValue += " "; // so that it is changed and will show by mouse
        }
        return defaultValue;
    }

    public String getToolTipText(int row, int col, String defaultValue) {
        OATableColumn[] tcs = getAllTableColumns();

        if (col >= 0 && col < tcs.length) {
            OATableColumn tc = (OATableColumn) tcs[col];
            defaultValue = tc.getToolTipText(row, col, defaultValue);
        }
        return defaultValue;
    }

    /**
     * Clear all of the filter values.
     */
    public void resetFilters() {
        try {
            hubAdapter.aiIgnoreValueChanged.incrementAndGet();
            _resetFilters();
        }
        finally {
            hubAdapter.aiIgnoreValueChanged.decrementAndGet();
        }
    }
    private void _resetFilters() {
        for (OATableColumn tc :  getAllTableColumns()) {
            if (tc.getFilterComponent() != null) {
                tc.getFilterComponent().reset();
            }
        }
        if (hubFilter != null) hubFilter.refresh();
        else if (tableRight != null && tableRight.hubFilter != null) {
            tableRight.hubFilter.refresh();
        }
        Container cont = getParent();
        for (int i=0; i<3 && cont!=null; i++) {
            cont.repaint();
            cont = cont.getParent();
        }
    }
    
    // 2006/12/29 called by superclass, this is overwritten from JTable
    protected JTableHeader createDefaultTableHeader() {
        headerRenderer = new PanelHeaderRenderer(this);

        JTableHeader th = new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int pos = columnModel.getColumnIndexAtX(p.x);
                if (pos < 0) return null;
                pos = columnModel.getColumn(pos).getModelIndex();
                String tt = null;
                if (pos >= 0 && pos < columns.size()) {
                    OATableColumn tc = (OATableColumn) columns.elementAt(pos);
                    tt = OATable.this.getColumnHeaderToolTipText(tc, p);
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

        OATableColumn[] allColumns = getAllTableColumns();

        int x = allColumns.length;
        OATableColumn colHubSelect = null;

        final ArrayList<OATableColumn> alSelectedColumns = new ArrayList<OATableColumn>();

        for (int i = 1;; i++) {
            boolean b = false;
            for (int j = 0; j < x; j++) {
                OATableColumn col = allColumns[j];
                col.getMethods(this.hub); // make sure that path has been set up correctly, to match to
                                          // table.hub
                if (col.sortOrder == i) {
                    alSelectedColumns.add(col);
                    if (OAString.isEmpty(col.path)) {
                        if (!OAString.isEmpty(col.pathIntValue)) {
                            if (s == null) s = col.pathIntValue;
                            else s += ", " + col.pathIntValue;
                            if (col.sortDesc) s += " DESC";
                        }
                        else {
                            if (hubSelect != null) {
                                colHubSelect = col;
                            }
                        }
                    }
                    else {
                        if (s == null) s = col.path;
                        else s += ", " + col.path;
                        if (col.sortDesc) s += " DESC";
                    }
                    b = true;
                }
            }
            if (!b) break;
        }

        final Object[] objs = new Object[hubSelect == null ? 0 : hubSelect.getSize()];
        if (hubSelect != null) {
            hubSelect.copyInto(objs);
        }

        if (colHubSelect != null) {
            final OATableColumn tcSelect = colHubSelect;
            hub.sort(new Comparator() {
                @Override
                public int compare(Object o1, Object o2) {
                    if (o1 == o2) return 0;
                    int x = 0;
                    for (OATableColumn col : alSelectedColumns) {

                        Object z1 = col.getValue(hub, o1);
                        Object z2 = col.getValue(hub, o2);

                        x = OACompare.compare(z1, z2);
                        if (x != 0) {
                            if (col.sortDesc) x *= -1;
                            if (col == tcSelect) x *= -1;
                            break;
                        }
                    }
                    return x;
                }
            });
        }
        else if (s != null) {
            hub.sort(s);
        }
        else {
            hub.cancelSort();
        }
// 20150810 dont keep sorted
if (!getKeepSorted()) hub.cancelSort();

        if (hubSelect != null) {
            hubAdapter.rebuildListSelectionModel();
            /*qqqqqq
            hubSelect.clear();
            for (Object obj : objs) {
                hubSelect.add(obj);
            }
            */
        }

        // reset AO
        // 20111008 add if
        if (hub.getAO() != null && hubSelect == null) {
            HubAODelegate.setActiveObjectForce(hub, hub.getAO());
        }
    }

    /**
     * Returns Hub that is bound to Table.
     */
    public Hub getHub() {
        return hub;
    }

    private boolean bKeepSorted;
    public void setKeepSorted(boolean b) {
        bKeepSorted = b;
    }
    public boolean getKeepSorted() {
        return bKeepSorted;
    }
    
    /**
     * Sets Hub that is bound to Table.
     */
    public void setHub(Hub h) {
        this.hub = h;

        int x = columns.size();
        for (int i = 0; i < x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            tc.setMethods(null);
        }
        oaTableModel = new OATableModel(hub);
        setModel(oaTableModel);
        hubAdapter = new MyHubAdapter(hub, this);
    }

    // START Drag&Drop
    static class MyDragSourceListener implements DragSourceListener {
        public void dragEnter(DragSourceDragEvent e) {
        }

        public void dragOver(DragSourceDragEvent e) {
        }

        public void dropActionChanged(DragSourceDragEvent e) {
        }

        public void dragExit(DragSourceEvent e) {
        }

        public void dragDropEnd(DragSourceDropEvent e) {
        }
    }

    /**
     * Flag to enable Drag and Drop. default=true
     */
    public void setAllowDnd(boolean b) {
        setAllowDrop(b);
        setAllowDrag(b);
        OATable t = getLeftTable();
        if (t != null) {
            t.setAllowDnd(b);
        }
    }

    /**
     * Flag to enable Drag and Drop. default=true
     */
    public void setAllowDnD(boolean b) {
        setAllowDnd(b);
    }

    /**
     * Flag to enable Drag and Drop. default=true
     */
    public void allowDnd(boolean b) {
        setAllowDnd(b);
    }

    /**
     * Flag to enable Drag and Drop. default=true
     */
    public void allowDnD(boolean b) {
        setAllowDnd(b);
    }

    /**
     * Flag to enable Dropping. default=true
     */
    public boolean getAllowDrop() {
        return bAllowDrop;
    }

    /**
     * Flag to enable Dropping. default=true
     */
    public void setAllowDrop(boolean b) {
        bAllowDrop = b;
        OATable t = getLeftTable();
        if (t != null) {
            t.bAllowDrop = b;
        }
    }

    /**
     * Flag to enable Dragging. default=true
     */
    public boolean getAllowDrag() {
        OATable t = getRightTable();
        if (t != null) {
            return t.bAllowDrag;
        }
        return bAllowDrag;
    }

    /**
     * Flag to enable Dragging. default=true
     */
    public void setAllowDrag(boolean b) {
        bAllowDrag = b;
        OATable t = getLeftTable();
        if (t != null) {
            t.bAllowDrag = b;
        }
    }

    /**
     * Flag to have a Dragged object removed from Hub. default=true
     */
    public void setRemoveDragObject(boolean b) {
        bRemoveDragObject = b;
        OATable t = getLeftTable();
        if (t != null) {
            t.bRemoveDragObject = b;
        }
    }

    /**
     * Flag to have a Dragged object removed from Hub. default=true
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
                OATransferable t = new OATransferable(hub, obj);
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
        Point pt = e.getLocation();
        autoscroll(pt);
    }

    private static Insets autoScrollInsets = new Insets(20, 20, 20, 20);

    private void autoscroll(Point cursorLocation) {
        Rectangle rectOuter = getVisibleRect();
        Rectangle rectInner = new Rectangle(rectOuter.x + autoScrollInsets.left, rectOuter.y + autoScrollInsets.top, rectOuter.width - (autoScrollInsets.left + autoScrollInsets.right), rectOuter.height - (autoScrollInsets.top + autoScrollInsets.bottom));
        if (!rectInner.contains(cursorLocation)) {
            Rectangle rect = new Rectangle(cursorLocation.x - autoScrollInsets.left, cursorLocation.y - autoScrollInsets.top, autoScrollInsets.left + autoScrollInsets.right, autoScrollInsets.top + autoScrollInsets.bottom);
            scrollRectToVisible(rect);
        }
    }

    /** Used to support drag and drop (DND). */
    public void dropActionChanged(DropTargetDragEvent e) {
    }

    /** Used to support drag and drop (DND). */
    public void dragExit(DropTargetEvent e) {
    }

    public boolean getAllowDrop(Hub hubDrag, Object objectDrag, Hub hubDrop) {
        return true;
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

            if (rect != null && pt.y > (rect.y + (rect.height / 2))) row++;

            if (hub.getObjectClass().isAssignableFrom(dragObject.getClass())) {
                if (!getAllowDrop(dragHub, dragObject, hub)) {
                    return;
                }
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
                        if (dragHub != hub) {
                            dragHub.remove(dragObject);
                        }
                    }
                }
                hub.setActiveObject(dragObject);
            }
            e.dropComplete(true);
        }
        catch (Exception ex) {
        }
    }

    // END Drag&Drop

    /**
     * Separate Hub that can contain selected objects. This will allow for a multi-select table.
     */
    public void setSelectHub(Hub hub) {
        this.hubSelect = hub;
        int x = (hub == null) ? ListSelectionModel.SINGLE_SELECTION : ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
        getSelectionModel().setSelectionMode(x);
        hubAdapter.setSelectHub(hubSelect);
    }

    /**
     * Separate Hub that can contain selected objects. This will allow for a multi-select table.
     */
    public Hub getSelectHub() {
        return hubSelect;
    }

    /****
     * public void setEditingRow(int aRow) { if (Hub.DEBUG)
     * System.out.println("OATable.setEditingRow()"); super.setEditingRow(aRow); if (aRow >= 0 && hub !=
     * null) { if (hub.getPos(hub.getActiveObject()) != aRow) { hub.setActiveObject(aRow); int pos =
     * hub.getPos(); if (pos != aRow) { getCellEditor().stopCellEditing();
     * setRowSelectionInterval(pos,pos); }
     * 
     * } } }
     ****/

    /**
     * Number of columns that should be visible when determinng the preferred size of the Table.
     */
    public int getPreferredColumns() {
        return prefCols;
    }

    /**
     * Number of columns that should be visible when determinng the preferred size of the Table.
     */
    public void setPreferredColumns(int cols) {
        setPreferredSize(prefRows, cols);
    }

    /**
     * Number of rows that should be visible when determinng the preferred size of the Table.
     */
    public int getPreferredRows() {
        return prefRows;
    }

    /**
     * Number of rows that should be visible when determinng the preferred size of the Table.
     */
    public void setPreferredRows(int rows) {
        setPreferredSize(rows, prefCols);
    }

    /**
     * Number of columns and rows that should be visible when determinng the preferred size of the
     * Table.
     */
    public void setPreferredSize(int rows, int cols) {
        this.setPreferredSize(rows, cols, true);
    }

    /**
     * Number of columns and rows that should be visible when determinng the preferred size of the
     * Table.
     * 
     * @param includeScrollBar
     *            add the width of a scrollbar to the preferred width of the Table.
     */
    public void setPreferredSize(int rows, int cols, boolean includeScrollBar) {
        prefCols = ((cols > 0) ? cols : 1);
        prefRows = ((rows > 0) ? rows : 1);
        this.includeScrollBar = includeScrollBar;
        calcPreferredSize();
    }

    /**
     * Used to have JTable.getCellRenderer(row, column) call OATable.getRenderer()
     */
    class MyTableCellRenderer implements TableCellRenderer {
        TableCellRenderer rend;
        OATable table;

        public MyTableCellRenderer(OATable t) {
            this.table = t;
        }

        private int cntError;
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = null;
            try {
                // 201512225
                hasFocus = hasFocus && row >= 0 && hub.getPos() == row;
                comp = _getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            catch (Exception e) {
                if (cntError++ < 25 || (cntError % 250) == 0) {
                    LOG.log(Level.WARNING, "error with column="+column, e);
                }
            }
            return comp;
        }
        private Component _getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        
            if (rend == null) return null;

            column = convertColumnIndexToModel(column);
            boolean bMouseOver = (row == mouseOverRow && column == mouseOverColumn);

            // see: OATableCellRenderer
            Component comp = rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            Component compOrig = comp;

            OATable t = getRightTable();
            if (t != null) {
                comp = t.getRenderer(comp, table, value, isSelected, hasFocus, row, column, wasChanged(row, column), bMouseOver);
            }
            else {
                t = getLeftTable();
                if (t != null) {
                    column += t.getColumnCount();
                }
                comp = getRenderer(comp, table, value, isSelected, hasFocus, row, column, wasChanged(row, column), bMouseOver);
            }
            if (comp == null) comp = compOrig;
            return comp;
        }
    }

    private MyTableCellRenderer myRend;

    /**
     * JTable method used to get the renderer for a cell. This is set up to automatically call
     * getRenderer() from the column's component. Dont overwrite this method, since OATable could be
     * made up of 2 tables.
     * 
     * @see #getRenderer This needs to be used instead of overwriting this method - especially with
     *      OATableScrollPane.
     * @see #customizeRenderer(JLabel, JTable, Object, boolean, boolean, int, int, boolean, boolean)
     */
    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        // This will call MyTableCellRenderer.getTableCellRendererComponent(),
        // which will then call OATable.getRenderer()
        if (myRend == null) myRend = new MyTableCellRenderer(this);

        // this will set the default renderer, ex:
        myRend.rend = super.getCellRenderer(row, column);
        return myRend;
    }

    // 20150315
    protected ConcurrentHashMap<String, Long> hmRowColumnChanged;
    private boolean bShowChanges;
    private Timer timerShowChanges;
    private final Object lockShowChanges = new Object();

    /**
     * Flag to track changes to row,col (cells).
     */
    public boolean getShowChanges() {
        return bShowChanges;
    }

    public void setShowChanges(final boolean b) {
        synchronized (lockShowChanges) {
            bShowChanges = b;
            if (tableRight != null) return;
            if (!b) {
                hmRowColumnChanged = null;
                if (timerShowChanges != null) {
                    timerShowChanges.stop();
                    timerShowChanges = null;
                    hmRowColumnValue = null;
                }
            }
            else if (b && hmRowColumnChanged == null) {
                hmRowColumnChanged = new ConcurrentHashMap<String, Long>();

                timerShowChanges = new Timer(25, new ActionListener() {
                    int emptyCount;

                    public void actionPerformed(ActionEvent e) {
                        long tsNow = 0;
                        for (Map.Entry<String, Long> entry : hmRowColumnChanged.entrySet()) {
                            if (tsNow == 0) {
                                tsNow = System.currentTimeMillis();
                            }
                            if (tsNow > entry.getValue().longValue() + 500) {
                                hmRowColumnChanged.remove(entry.getKey());
                                if (hmRowColumnValue != null) hmRowColumnValue.remove(entry.getKey());
                            }
                        }
                        if (tsNow > 0) {
                            OATable.this.repaint(250);
                            if (tableLeft != null) tableLeft.repaint(250);
                        }
                        else {
                            emptyCount++;
                            if (emptyCount > 10) {
                                emptyCount = 0;
                                synchronized (OATable.this.lockShowChanges) {
                                    if (hmRowColumnChanged.size() == 0) timerShowChanges.stop();
                                }
                            }
                        }
                    }
                });
                timerShowChanges.setRepeats(true);
            }
        }
        if (tableLeft != null) tableLeft.setShowChanges(b);
    }

    public void setChanged(int row, int col) {
        OATable t = getRightTable();
        if (t != null) {
            t.setChanged(row, col);
            return;
        }
        if (!bShowChanges) return;

        // if (!OARemoteThreadDelegate.isRemoteThread()) return;
        synchronized (lockShowChanges) {
            hmRowColumnChanged.put(row + "." + col, System.currentTimeMillis());
            if (!timerShowChanges.isRunning()) {
                timerShowChanges.start();
            }
        }
    }

    private ConcurrentHashMap<String, Object> hmRowColumnValue;

    public void setChanged(int row, int col, Object newValue) {
        OATable t = getRightTable();
        if (t != null) {
            t.setChanged(row, col, newValue);
            return;
        }

        if (!bShowChanges) return;
        if (hmRowColumnValue == null) {
            hmRowColumnValue = new ConcurrentHashMap<String, Object>();
        }
        String k = row + "." + col;
        if (newValue == null) newValue = OANullObject.instance;
        Object old = hmRowColumnValue.get(k);
        if (!OACompare.isEqual(old, newValue)) {
            if (old != null) setChanged(row, col);
            hmRowColumnValue.put(k, newValue);
        }
    }

    public boolean wasChanged(int row, int col) {
        OATable t = getRightTable();
        if (t != null) {
            return t.wasChanged(row, col);
        }
        if (!bShowChanges) return false;

        if (hmRowColumnChanged == null) return false;
        Long longx = hmRowColumnChanged.get(row + "." + col);
        return (longx != null);
    }

    @Override
    public void setRowHeight(int rowHeight) {
        super.setRowHeight(rowHeight);
        OATable t = getLeftTable();
        if (t != null) {
            t.setRowHeight(rowHeight);
        }
    }

    /**
     * Can be overwritten to customize the component used to renderer a Table cell.
     * 
     * @see #getRenderer(JComponent, JTable, Object, boolean, boolean, int, int) to customize the
     *      component
     * @see #customizeRenderer(JLabel, JTable, Object, boolean, boolean, int, int) Preferred way
     */
    public Component getRenderer_OLD(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component comp = null;
        if (myRend != null) {
            comp = myRend.rend.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        }
        if (comp instanceof JLabel) {
            OATable t = getRightTable();
            if (t != null) {
                // qq t.customizeRenderer((JLabel) comp, table, value, isSelected, hasFocus, row,
                // column);
            }
            else {
                t = getLeftTable();
                if (t != null) {
                    column += t.getColumnCount();
                }
                // qq customizeRenderer((JLabel) comp, table, value, isSelected, hasFocus, row, column);
            }
        }
        return comp;
    }

    // listeners for customizing Renderers
    Vector vecListener;

    /**
     * Add a listener that is called to customize the rendering component for a cell.
     */
    public void addListener(OATableListener l) {
        if (vecListener == null) vecListener = new Vector(2, 2);
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }

    /**
     * Remove a listener that is called to customize the rendering component for a cell.
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

    public boolean debug;

    /**
     * Determine preferred size based on number of preferred number of columns and rows.
     */
    protected void calcPreferredSize() {
        int w = 0;
        int cols = prefCols;

        OATableColumn[] tcs = getAllTableColumns();

        int i=0;
        for (OATableColumn tc : getAllTableColumns()) {
            if (i++ == cols) break;
            w += tc.tc.getWidth();
        }

        w += cols * getIntercellSpacing().width;
        w += 2;
        // was: int h = getIntercellSpacing().height + getRowHeight();
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
     * Returns the column position for an OATableComponent.
     */
    public int getColumnIndex(OATableComponent c) {
        if (tableLeft != null) {
            int x = tableLeft.getColumnIndex(c);
            if (x >= 0) return x;
        }

        int x = columns.size();
        for (int i = 0; i < x; i++) {
            OATableColumn tc = (OATableColumn) columns.elementAt(i);
            if (tc.getOATableComponent() == c) {
                if (tableLeft != null) {
                    i += tableLeft.columns.size();
                }
                return i;
            }
        }
        return -1;
    }

    /**
     * Change the heading for a column number. First column is at postion 0.
     */
    public void setColumnHeading(int col, String heading) {
        if (col < columns.size() && col >= 0) {
            getColumnModel().getColumn(col).setHeaderValue(heading);
            invalidate();
            this.getParent().validate();
        }
    }

    /**
     * Change the width for a column number, based on character width. First column is at postion 0.
     */
    public void setColumnWidth(int col, int w) {
        if (col < columns.size() && col >= 0) {
            getColumnModel().getColumn(col).setWidth(w);
            calcPreferredSize();
        }
    }

    /**
     * Set the property path used to display values for a column. This could be necessary when it can
     * not be determined by the columns OATableComponent.
     */
    public void setColumnPropertyPath(int col, String propertyPath) {
        if (col < columns.size() && col >= 0) {
            OATableColumn tc = (OATableColumn) columns.elementAt(col);
            tc.path = propertyPath;
            tc.bIsAlreadyExpanded = true;
            tc.setMethods(null);
            if (oaTableModel != null) {
                boolean b = false;
                try {
                    if (hubAdapter != null) {
                        b = true;
                        hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                    }
                    oaTableModel.fireTableStructureChanged();
                }
                finally {
                    if (b && hubAdapter != null) hubAdapter.aiIgnoreValueChanged.decrementAndGet();
                }
            }
        }
    }

    public void resetColumn(OATableComponent comp) {
        int col = getColumnIndex(comp);
        if (col < columns.size() && col >= 0) {
            OATableColumn tc = (OATableColumn) columns.elementAt(col);
            tc.path = comp.getPropertyPath();
            tc.setMethods(null);
            if (oaTableModel != null) {
                boolean b = false;
                try {
                    if (hubAdapter != null) {
                        b = true;
                        hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                    }
                    oaTableModel.fireTableStructureChanged();
                }
                finally {
                    if (b && hubAdapter != null) hubAdapter.aiIgnoreValueChanged.decrementAndGet();
                }
            }
        }
    }

    // 20150428
    /**
     * Add a column that will that will use checkboxes to show selected rows.
     * 
     * @param hubSelect
     * @param heading
     * @param width
     */
    public OATableColumn addCounterColumn() {
        return addCounterColumn("#", 3);
    }

    public OATableColumn addCounterColumn(String heading, int width) {
        OALabel lbl = new OALabel(getHub(), "") {
            @Override
            public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver) {
                lbl.setText("" + (row + 1) + " ");
                lbl.setHorizontalAlignment(SwingConstants.RIGHT);
                if (!isSelected) lbl.setForeground(Color.gray);
            }

            @Override
            public String getToolTipText(int row, int col, String defaultValue) {
                defaultValue = super.getToolTipText(row, col, defaultValue);
                if (OAString.isEmpty(defaultValue)) {
                    defaultValue = (row + 1) + " of " + getHub().getSize();
                }
                return defaultValue;
            }
        };
        tcCount = addColumn(heading, width, lbl);
        tcCount.setAllowSorting(false);
        return tcCount;
    }
    protected OATableColumn tcCount; 
    
    // 20150423
    /**
     * Add a column that will that will use checkboxes to show selected rows.
     * 
     * @param hubSelect
     * @param heading
     * @param width
     */
    public void addSelectionColumn(Hub hubSelect, String heading, int width) {
        if (hubSelect == null) return;
        setSelectHub(hubSelect);
        chkSelection = new OACheckBox(hub, hubSelect) {
            @Override
            public String getToolTipText(int row, int col, String defaultValue) {
                Object obj = hub.getAt(row);
                if (obj == null || OATable.this.hubSelect == null) {
                    return super.getToolTipText(row, col, defaultValue);
                }
                int pos = OATable.this.hubSelect.getPos(obj);
                if (pos < 0) return OATable.this.hubSelect.getSize() + " selected";
                return (pos + 1) + " of " + OATable.this.hubSelect.getSize() + " selected";
            }
            @Override
            public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableRenderer(lbl, table, value, isSelected, hasFocus, row, column);
                if (row == -1) {
                    // heading
                    //need to set checked=true if all selected
                    boolean b = isAllSelected();
                    chkRenderer.setSelected(b);
                    setHalfChecked(!b && isAnySelected());
                }
                else setHalfChecked(false);
                return comp;
            }
        };
        chkSelection.setToolTipText(" ");
        chkSelection.setTableHeading(heading);
        OATableColumn tc = addColumn(heading, width, chkSelection);
    }

    protected boolean isAllSelected() {
        Hub h = getSelectHub();
        if (h == null) return false;
        int x = h.getSize();
        if (x == 0) return false;
        if (getHub().getSize() > x) return false;
        
        for (Object obj : getHub()) {
            if (!h.contains(obj)) return false;
        }
        return true;
    }
    protected boolean isAnySelected() {
        Hub h = getSelectHub();
        if (h == null) return false;
        if (h.getSize() == 0) return false;
        if (getHub().getSize() == 0) return false;
        for (Object obj : h) {
            if (getHub().contains(obj)) return true;
        }
        return false;
    }
    
    protected OACheckBox chkSelection;

    // 20150511
    @Override
    public void changeSelection(int rowIndex, int columnIndex, boolean toggleUsingControlKey, boolean extendUsingShiftKey) {
        // extendUsingShiftKey=true if its a mouse drag

        if (chkSelection == null && tableRight != null) {
            chkSelection = tableRight.chkSelection;
        }

        if (chkSelection != null && hubSelect != null) {
            if (extendUsingShiftKey) {
                if (bIsMouseDragging) {
                    if (lastMouseDragRow < 0) {
                        lastMouseDragRow = rowIndex;
                        return; // ignore right now
                    }
                    if (rowIndex == lastMouseDragRow) return;
                    getSelectionModel().addSelectionInterval(lastMouseDragRow, lastMouseDragRow);
                }
            }

            int addColumns = 0;
            if (tableLeft != null) {
                addColumns = tableLeft.getColumnCount();
            }

            if ((columnIndex + addColumns) == getColumnIndex(chkSelection)) {
                if (!extendUsingShiftKey) {
                    if (!bIsProcessKeyBinding) {
                        toggleUsingControlKey = true;
                    }
                }
            }
        }
        try {
            super.changeSelection(rowIndex, columnIndex, toggleUsingControlKey, extendUsingShiftKey);
        }
        catch (Exception e) {
        }
    }

    // 20150423
    // was: qqqqqqq
    public void changeSelection_OLD(int rowIndex, int columnIndex, boolean toggleUsingControlKey, boolean extendUsingShiftKey) {
        // extendUsingShiftKey=true if its a mouse drag

        if (chkSelection == null && tableRight != null) {
            chkSelection = tableRight.chkSelection;
        }

        if (chkSelection != null && hubSelect != null) {

            if (extendUsingShiftKey) {
                if (bIsMouseDragging) {
                    if (lastMouseDragRow < 0) {
                        lastMouseDragRow = rowIndex;
                        return; // ignore right now
                    }
                    if (rowIndex == lastMouseDragRow) return;
                    getSelectionModel().addSelectionInterval(lastMouseDragRow, lastMouseDragRow);
                }
            }

            if (columnIndex == getColumnIndex(chkSelection)) {
                if (!extendUsingShiftKey) toggleUsingControlKey = true;
            }
            else {
                if (!extendUsingShiftKey && hubSelect.getSize() > 0 && !toggleUsingControlKey) {
                    toggleUsingControlKey = true;
                }
            }
        }
        super.changeSelection(rowIndex, columnIndex, toggleUsingControlKey, extendUsingShiftKey);
    }

    /**
     * Create a new column using an OATableComponent.
     */
    public OATableColumn addColumn(OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(comp.getTableHeading(), -1, comp.getPropertyPath(), comp, c, -1, null);
    }

    /**
     * Create a new column using an OATableComponent.
     */
    public OATableColumn add(OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(comp.getTableHeading(), -1, comp.getPropertyPath(), comp, c, -1, null);
    }

    /**
     * Create a new column using an OATableComponent.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     */
    public OATableColumn addColumn(String heading, int width, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, width, comp.getPropertyPath(), comp, c, -1, null);
    }

    public OATableColumn addColumn(String heading, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, -1, comp.getPropertyPath(), comp, c, -1, null);
    }

    /**
     * Create a new column using an OATableComponent.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     */
    public OATableColumn add(String heading, int width, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, width, comp.getPropertyPath(), comp, c, -1, null);
    }

    /**
     * Create a new column using an OATableComponent.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column. This could be necessary
     *            when it can not be determined by the columns OATableComponent.
     */
    public OATableColumn addColumn(String heading, int width, String path, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        OATableColumn tc = this.addColumnMain(heading, width, path, comp, c, -1, null);
        return tc;
    }

    /**
     * Create a new column using an OATableComponent.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column. This could be necessary
     *            when it can not be determined by the columns OATableComponent.
     */
    public OATableColumn add(String heading, int width, String path, OATableComponent comp) {
        TableCellEditor c = comp.getTableCellEditor();
        OATableColumn tc = this.addColumnMain(heading, width, path, comp, c, -1, null);
        return tc;
    }

    /**
     * Create a new column using an OATableComponent.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column. This could be necessary
     *            when it can not be determined by the columns OATableComponent.
     * @param index
     *            column number, -1 to append to existing columns
     */
    public OATableColumn addColumn(String heading, int width, String path, OATableComponent comp, int index) {
        TableCellEditor c = comp.getTableCellEditor();
        OATableColumn tc = this.addColumnMain(heading, width, path, comp, c, index, comp.getFormat());
        return tc;
    }

    /**
     * Create a new column using an OATableComponent.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column. This could be necessary
     *            when it can not be determined by the columns OATableComponent.
     * @param index
     *            column number, -1 to append to existing columns
     */
    public OATableColumn add(String heading, int width, String path, OATableComponent comp, int index) {
        TableCellEditor c = comp.getTableCellEditor();
        return this.addColumnMain(heading, width, path, comp, c, index, null);
    }

    /**
     * Create a new column using a path.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column.
     */
    public OATableColumn addColumn(String heading, int width, String path) {
        return this.addColumnMain(heading, width, path, null, (TableCellEditor) null, -1, null);
    }

    /**
     * Create a new column using a path.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column.
     */
    public OATableColumn addColumn(String heading, int width, String path, String fmt) {
        return this.addColumnMain(heading, width, path, null, (TableCellEditor) null, -1, fmt);
    }

    /**
     * Create a new column using a path.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column.
     */
    public OATableColumn add(String heading, int width, String path) {
        return this.addColumnMain(heading, width, path, null, (TableCellEditor) null, -1, null);
    }

    /**
     * Create a new column using a path.
     * 
     * @param heading
     *            column heading
     * @param width
     *            of column based on average character width.
     * @param path
     *            Set the property path used to display values for a column.
     */
    public OATableColumn add(String heading, int width, String path, String fmt) {
        return this.addColumnMain(heading, width, path, null, (TableCellEditor) null, -1, fmt);
    }

    static int averageCharWidth = 0;
    static int averageCharHeight = 0;
    static int lastFontSize = 0;

    /**
     * Used to determine the pixel width based on the average width of a character 'X'.
     */
    public static int getCharWidth(Component comp, int columns) {
        if (comp == null) return 0;
        return getCharWidth(comp, comp.getFont(), columns);
    }
    public static int getCharWidth(Component comp, Font font, int columns) {
        if (comp == null) return 0;
        if (averageCharWidth == 0 || (font != null && font.getSize() != lastFontSize)) {
            if (font == null) {
                System.out.println("OATable.getCharWidth=null, will use average=12 as default");
                Exception e = new Exception("OATable.getCharWidth=null, will use average=12 as default");
                e.printStackTrace();
                return (11 * columns);
            }
            lastFontSize = font.getSize();
            FontMetrics fm = comp.getFontMetrics(font);
            //averageCharWidth = (int) (fm.stringWidth("9XYma") / 5);
            averageCharWidth = fm.charWidth('m');  // same used by JTextField.getColumnWidth
        }
        
        return (averageCharWidth * columns);
    }

    public static int getCharHeight(Component comp, Font font) {
        if (averageCharHeight == 0 || (font != null && font.getSize() != lastFontSize)) {
            lastFontSize = font.getSize();
            FontMetrics fm = comp.getFontMetrics(font);
            averageCharHeight = (int) fm.getHeight();
        }
        return (averageCharHeight);
    }

    /*
     * later ... public void addColumn(String heading, int width, OATableColumn oatc) { int pos =
     * columns.size(); columns.insertElementAt(oatc, pos);
     * 
     * Font font = ((JComponent)oatc.oaComp).getFont(); if (width < 0 && oatc.oaComp != null) { width =
     * ((JComponent)oatc.oaComp).getPreferredSize().width; width /=
     * getCharWidth((JComponent)oatc.oaComp, font, 1); } int w = OATable.getCharWidth(this,font,width);
     * w += 8; // borders, etc.
     * 
     * TableColumn tc = new TableColumn(pos); tc.setPreferredWidth(oatc.defaultWidth); tc.setWidth(w);
     * tc.setCellEditor(oatc.comp);
     * 
     * tc.setCellRenderer( new OATableCellRenderer(oatc) ); tc.setHeaderValue(heading);
     * tc.sizeWidthToFit(); getColumnModel().addColumn(tc);
     * 
     * column.tc = oatc; if (headerRenderer != null) tc.setHeaderRenderer(headerRenderer); // 2006/10/13
     * 
     * tc.setHeaderRenderer(headerRenderer); // 2006/12/29
     * 
     * calcPreferredSize(); }
     */

    /**
     * Main method for adding a new Table Column.
     */
    protected OATableColumn addColumnMain(String heading, int width, String path, OATableComponent oaComp, final TableCellEditor editComp, int index, String fmt) {
        Font font;

        Component comp = null;
        if (oaComp instanceof JComponent) {
            comp = (Component) oaComp;
        }
        else if (oaComp != null) {
            TableCellEditor tce = oaComp.getTableCellEditor();
            if (tce != null) {
                comp = tce.getTableCellEditorComponent(this, null, false, -1, -1);
            }
        }

        if (comp != null) {
            font = comp.getFont();

            // 20151226 stop table editor on focuslost 
            FocusListener fl = (new FocusListener() {
                int focusRow;
                @Override
                public void focusLost(FocusEvent e) {
                    TableCellEditor ed = getCellEditor();
                    if (ed != editComp) return;
                    if (focusRow != getHub().getPos()) return;
                    ed.stopCellEditing();
                }
                @Override
                public void focusGained(FocusEvent e) {
                    focusRow = getHub().getPos();
                }
            });
            comp.addFocusListener(fl);
        }
        else font = getFont();

        if (width < 0 && comp != null) {
            if (comp instanceof JTextField) {
                width = ((JTextField) comp).getColumns();
            }
            else {
                width = comp.getPreferredSize().width;
                width /= getCharWidth(comp, font, 1);
            }
        }
        int w = OATable.getCharWidth(this, font, width);
        w += 6; // borders, etc.

        TableCellRenderer rend = null;

        OATableColumn column = new OATableColumn(this, path, editComp, rend, oaComp, fmt);
        column.defaultWidth = w;  // 20150927 was: width
        if (oaComp != null) oaComp.setTable(this);

        int col = index;
        if (index == -1) col = columns.size();

        columns.insertElementAt(column, col);
        if (oaTableModel != null) {
            boolean b = false;
            try {
                if (hubAdapter != null) {
                    b = true;
                    hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                }
                oaTableModel.fireTableStructureChanged();
            }
            finally {
                if (b && hubAdapter != null) hubAdapter.aiIgnoreValueChanged.decrementAndGet();
            }
        }

        TableColumn tc = new TableColumn(col);
        tc.setPreferredWidth(w);
        tc.setWidth(w);
        tc.setCellEditor(editComp);

        tc.setCellRenderer(new OATableCellRenderer(column));
        tc.setHeaderValue(heading);
// 20150927 removed, not positive what this does, but it apprears to set the width based on component. I want it based on columns, so I'm removing it        
//        tc.sizeWidthToFit(); // 2006/12/26
        getColumnModel().addColumn(tc);

        column.headerRenderer = null;
        column.tc = tc; // 2006/10/12
        tc.setHeaderRenderer(headerRenderer); 

        calcPreferredSize();
        return column;
    }

    /**
     * Remove a column from Table.
     */
    public void removeColumn(int pos) {
        if (pos < 0) return;
        if (pos >= columns.size()) return;

        columns.removeElementAt(pos);
        getColumnModel().removeColumn(getColumnModel().getColumn(pos));
    }

    /*****
     * protected void addImpl(Component comp,Object constraints,int index) { if (comp instanceof
     * OATableComponent) { OATableComponent oacomp = (OATableComponent) comp; int w =
     * oacomp.getColumns(); if (w < 0) w = 8; addColumn(oacomp.getTableHeading(), w,
     * oacomp.getPropertyPath(), oacomp, index); } else super.addImpl(comp,constraints,index); }
     * 
     * public Component getComponent(int n) { if (!bDesignTime) return super.getComponent(n); int x =
     * columns.size(); for (int i=0,j=0; i<x; i++) { OATableColumn tc = (OATableColumn)
     * columns.elementAt(i); if (tc.oaComp != null) { if (j == n) return (Component) tc.oaComp; j++; } }
     * return null; } public int getComponentCount() { if (!bDesignTime) return
     * super.getComponentCount(); int x = columns.size(); int cnt=0; for (int i=0; i<x; i++) {
     * OATableColumn tc = (OATableColumn) columns.elementAt(i); if (tc.oaComp != null) cnt++; } return
     * cnt; } public Component[] getComponents() { if (!bDesignTime) return super.getComponents();
     * Component[] comps = new Component[getComponentCount()]; int x = columns.size(); for (int i=0,j=0;
     * i<x; i++) { OATableColumn tc = (OATableColumn) columns.elementAt(i); if (tc.oaComp != null) {
     * comps[j] = (Component) tc.oaComp; j++; } } return comps; }
     * 
     * public void remove(Component comp) { if (!bDesignTime) return super.remove(comp); int x =
     * columns.size(); for (int i=0; i<x; i++) { OATableColumn tc = (OATableColumn)
     * columns.elementAt(i); if (tc.comp == comp) removeColumn(i); } }
     * 
     * public void removeAll() { int x = columns.size(); for (int i=0; i<x; i++) { removeColumn(0); } }
     ******/

    /**
     * Table Model using a Hub.
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
            return Math.abs(hub.getSize());
        }

        public void fireTableStructureChanged() {
            for (int i=0; i<3; i++) {
                boolean b = false;
                try {
                    if (hubAdapter != null) {
                        b  = true;
                        hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                    }
                    _fireTableStructureChanged();
                    break;
                }
                catch (Exception e) {
                    // ignore
                }
                finally {
                    if (b && hubAdapter != null) hubAdapter.aiIgnoreValueChanged.decrementAndGet();
                }
            }
        }
        
        private void _fireTableStructureChanged() {
            // need to retain the selected objects
            super.fireTableStructureChanged();

            getSelectionModel().clearSelection();
            if (hubSelect != null) {
                for (Object obj : hubSelect) {
                    int x = hub.getPos(obj);
                    if (x >= 0) addRowSelectionInterval(x, x);
                }
            }
            OATable t = OATable.this;
            if (t != null) {
                t.repaint();
                JTableHeader th = t.getTableHeader();
                if (th != null) th.repaint();
                OATable tx = t.getLeftTable();
                if (tx != null) {
                    th = tx.getTableHeader();
                    if (th != null) th.repaint();
                }
                tx = t.getRightTable();
                if (tx != null) {
                    th = tx.getTableHeader();
                    if (th != null) th.repaint();
                }
            }
        }

        public void fireTableRowsUpdated(int pos1, int pos2) {
            super.fireTableRowsUpdated(pos1, pos2);
        }

        public void fireTableRowsInserted(int firstRow, int lastRow) {
            super.fireTableRowsInserted(firstRow, lastRow);
        }

        public void fireTableRowsDeleted(int firstRow, int lastRow) {
            super.fireTableRowsDeleted(firstRow, lastRow);
        }

        public Class getColumnClass(int c) {
            Method[] ms = ((OATableColumn) columns.elementAt(c)).getMethods(hub);

            int i = ms.length;
            if (i == 0) return hub.getObjectClass();
            Method m = ms[i - 1];
            Class cl = m.getReturnType();

            return OAReflect.getPrimitiveClassWrapper(cl);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            boolean b = (((OATableColumn) columns.elementAt(columnIndex)).getTableCellEditor() != null);
            return b;
        }

        public void setValueAt(Object obj, int row, int col) {
            // dont do this: if (hub.getActiveObject() != hub.elementAt(row)) hub.setActiveObject(row);
            // do nothing, the editor component is object aware
        }

        boolean loadMoreFlag;
        boolean loadingMoreFlag;

        public Object getValueAt(int row, int col) {
            if (hub == null) return "";
            Object obj;
            int cnt = hub.getSize();

            if (hub.isMoreData()) {
                if (row + 5 >= cnt) {
                    if (!loadMoreFlag && !loadingMoreFlag) {
                        loadMoreFlag = true;
                        loadingMoreFlag = true;

                        if (isEditing()) getCellEditor().stopCellEditing(); // instead of
                                                                            // "removeEditor();"
                        obj = hub.elementAt(row);
                        hub.elementAt(row + 5);
                        hubAdapter.onNewList(null);

                        // make sure cell is visible
                        int pos = hub.getPos(obj);
                        if (pos < 0) pos = 0;
                        Rectangle cellRect;
                        cellRect = getCellRect(pos, col, true);
                        scrollRectToVisible(cellRect);
                        repaint();

                        pos = hub.getPos(hub.getActiveObject());
                        if (pos < 0) getSelectionModel().clearSelection();
                        else setRowSelectionInterval(pos, pos);

                        loadingMoreFlag = false;

                    }
                }
                else loadMoreFlag = false;
            }

            obj = hub.elementAt(row);
            if (obj == null) return "";

            OATableColumn tc = (OATableColumn) columns.elementAt(col);
            obj = tc.getValue(hub, obj);
            return obj;
        }
    }

    // ******************************** H A C K S ********************************************
    // ******************************** H A C K S ********************************************
    // ******************************** H A C K S ********************************************
    // Hack: this should to be called within the constructor
    private void addHack() {
        // this is needed so that other components that have called registerKeyboardAction()
        // wont get <enter> key
        registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 10/18/99 jdk1.2 The new BasicTableUI ignores the [Enter], but we want to have it setFocus
        registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = getSelectedRow();
                int col = getSelectedColumn();
                editCellAt(row, col, e);
                requestFocus();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_FOCUSED);
        registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED);
    }

    protected AbstractButton cmdDoubleClick;

    /**
     * Button to perform a doClick() when table clickCount == 2
     */
    public void setDoubleClickButton(AbstractButton cmd) {
        cmdDoubleClick = cmd;
        OATable t = getLeftTable();
        if (t != null) {
            t.cmdDoubleClick = cmd;
        }
    }

    /**
     * Button to perform a doClick() when table clickCount == 2
     */
    public AbstractButton getDoubleClickButton() {
        return cmdDoubleClick;
    }

    private JPopupMenu compPopupMenu;

    @Override
    public void setComponentPopupMenu(JPopupMenu popup) {
        // super.setComponentPopupMenu(popup);
        this.compPopupMenu = popup;
        OATable t = getLeftTable();
        if (t != null) {
            t.compPopupMenu = popup;
        }
    }

    /*
     * dont include this, since it will then be used by JFC, which wont then use special code in
     * processMouseEvent to show the popupMenu public JPopupMenu getComponentPopupMenu() { return
     * this.compPopupMenu; }
     */
    public JPopupMenu getMyComponentPopupMenu() {
        return this.compPopupMenu;
    }

    // similar to private in jtable
    protected void myClearSelectionAndLeadAnchor() {
        for (int i=0; i<3; i++) {
            try {
                selectionModel.setValueIsAdjusting(true);
                columnModel.getSelectionModel().setValueIsAdjusting(true);

                _myClearSelectionAndLeadAnchor();
                break;
            }
            catch (Exception e) {
                // no-op
            }
            finally {
                selectionModel.setValueIsAdjusting(false);
                columnModel.getSelectionModel().setValueIsAdjusting(false);
            }
        }
    }
    protected void _myClearSelectionAndLeadAnchor() {
        clearSelection();

        selectionModel.setAnchorSelectionIndex(-1);
        selectionModel.setLeadSelectionIndex(-1);
        columnModel.getSelectionModel().setAnchorSelectionIndex(-1);
        columnModel.getSelectionModel().setLeadSelectionIndex(-1);
    }

    // 20150424
    private int lastMouseDragRow = -1;
    private boolean bIsMouseDragging;

    /**
     * Capture double click and call double click button.
     * 
     * @see #getDoubleClickButton
     */
    @Override
    protected void processMouseEvent(MouseEvent e) {

        if (e.getID() == MouseEvent.MOUSE_RELEASED) {
            lastMouseDragRow = -1;
            bIsMouseDragging = false;
        }

        // 20150511 popup trigger to work for windows and Mac

        if (compPopupMenu != null) {
            if (e.getID() == MouseEvent.MOUSE_RELEASED || e.getID() == MouseEvent.MOUSE_PRESSED) {
                if (e.isPopupTrigger()) {
                    Point pt = e.getPoint();
                    int row = rowAtPoint(pt);

                    hub.setPos(row);
                    compPopupMenu.show(this, pt.x, pt.y);
                }
            }
        }

        /*
         * was if (compPopupMenu != null) { if (e.getID() == MouseEvent.MOUSE_RELEASED) { if (
         * (e.getModifiers() & Event.META_MASK) != 0) { if (e.isPopupTrigger()) { Point pt =
         * e.getPoint(); int row = rowAtPoint(pt);
         * 
         * hub.setPos(row); / * ListSelectionModel lsm = getSelectionModel(); if
         * (!lsm.isSelectedIndex(row)) { getSelectionModel().setSelectionInterval(row, row); } /
         * compPopupMenu.show(this, pt.x, pt.y); } } } }
         */

        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            Point pt = e.getPoint();
            int row = rowAtPoint(pt);
            if (row < 0) { // 20150428
                hub.setPos(-1);
            }
            if (e.getClickCount() == 2) {
                if (hub.getPos() == row && row >= 0) {
                    onDoubleClick();
                }
                return;
            }
        }
        else if (e.getID() == MouseEvent.MOUSE_EXITED) {
            onMouseOver(-1, -1, e);
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
        else if (e.getID() == MouseEvent.MOUSE_DRAGGED) {
            bIsMouseDragging = true;
        }

        super.processMouseMotionEvent(e);
    }

    /**
     * Method that is called whenever mouse click count = 2. Note: the activeObject of the clicked row
     * will be the active object in the OATables Hub.
     * 
     * Default behaviour is to call doubleClick Command, if it is enabled.
     */
    public void onDoubleClick() {
        OATable t = getRightTable();
        if (t != null) {
            t.onDoubleClick();
            return;
        }

        if (cmdDoubleClick != null) {
            if (cmdDoubleClick.isEnabled()) {
                cmdDoubleClick.doClick();
            }
        }
    }
    public void setEnableEditors(boolean b) {
        bEnableEditors = b;
    }
    public boolean getEnableEditors() {
        return bEnableEditors;
    }

    public void setFilterMasterHub(Hub hubFilterMaster) {
        setMasterFilterHub(hubFilterMaster);
    }
    Hub hubFilterMaster;
    public void setMasterFilterHub(Hub hubFilterMaster) {
        this.hubFilterMaster = hubFilterMaster;
        if (headerRenderer != null) {
            if (hubFilterMaster == null) headerRenderer.remove(headerRenderer.label);
            else headerRenderer.add(headerRenderer.label, BorderLayout.CENTER);
            repaint();
        }
        if (hubFilter != null) {
            hubFilter.close();
            hubFilter = null;
        }
        if (hubFilterMaster == null) return;

        hubFilter = new HubFilter(hubFilterMaster, getHub(), true) {
            @Override
            public boolean isUsed(Object obj) {
                for (OATableColumn tc : getAllTableColumns()) {
                    OATableFilterComponent tfc = tc.getFilterComponent();
                    if (tfc != null && !tfc.isUsed(obj)) {
                        return false;
                    }
                }
                return true;
            }
        };
        oaTableModel.fireTableStructureChanged();
    }

    public void refreshFilter() {
        final HubFilter hf = (hubFilter != null || tableRight == null) ? hubFilter : tableRight.hubFilter;
        
        if (hf == null) return;
        // final int cnt = aiRefreshFilter.incrementAndGet();
        
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            Dimension dim;
            @Override
            protected Void doInBackground() throws Exception {
                boolean b = false;
                try {
                    if (hubAdapter != null) {
                        b = true;
                        hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                    }
                    hf.refresh();
                }
                finally {
                    if (b && hubAdapter != null) hubAdapter.aiIgnoreValueChanged.decrementAndGet();
                }
                oaTableModel.fireTableStructureChanged();
                return null;
            }
            @Override
            protected void done() {
            }
        };
        sw.execute();
    }
    
    

    /**
     * Overwritten to set active object in Hub.
     */
    @Override
    public boolean editCellAt(int row, int column, java.util.EventObject e) {
        // hack: editCellAt() will not hide the current cell editor if the new "column"
        // does not have an editorComponent. If this happens, then it will return
        // false.

        if (hub.getPos() != row) {
            try {
                hubAdapter._bIsRunningValueChanged = true; // 20131113
                hub.setPos(row);
            }
            finally {
                hubAdapter._bIsRunningValueChanged = false;
            }
        }

        // 20151225 checkbox selection column does not need to have cellEdit, so that drag many can work
        if (chkSelection == null && tableRight != null) {
            chkSelection = tableRight.chkSelection;
        }
        if (chkSelection != null) {
            int addColumns = 0;
            if (tableLeft != null) {
                addColumns = tableLeft.getColumnCount();
            }
            if ((column + addColumns) == getColumnIndex(chkSelection)) {
                return false;
            }
        }        
        
        
        if (hubSelect != null) {
            /* 20151225
            int x = hubSelect.getSize();
            if (x > 0) {
                if (x > 1) return false;
                if (hubSelect.getAt(0) != hub.getAO()) return false;
            }
            */
        }
        else {
            try {
                // Note: 20100529 calling setRowSelectionInterval(row,row), which will call
                // valueChanged(evt)
                // will have e.getValueIsAdjusting() = true
                // was: setRowSelectionInterval(row,row); // this will not call setActiveObject(), since
                // e.getValueIsAdjusting() will be true
            }
            catch (RuntimeException ex) {
            }

            if (hub.getPos() != row) {
                return false; // cant change activeObject
            }
        }

        if (!bEnableEditors) return false;

        boolean b = super.editCellAt(row, column, e);

        // hack: if editCellAt() returned false and the old column had an editor, then
        // we must stop it now. Calling stopCellEditor() has no side effects, other
        // then removing the editorComponent

        if (!b && isEditing()) {
            getCellEditor().stopCellEditing();
        }
        else {
            if (getCellEditor() instanceof OATableCellEditor) {
                ((OATableCellEditor) getCellEditor()).startCellEditing(e);
            }
            requestFocus(); // make sure component gets input
        }

        return b;
    }

    // hack: called by OATableCellEditor because JTable.removeEditor() sets isEditing to false
    // after it removes the component from the Table
    boolean checkFocusFlag = true;

    public void setCheckFocus(boolean b) {
        checkFocusFlag = b;
    }

    /**
     * Overwritten to resume edit mode when focus is regained.
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

    // ================== 2006/12/29 :) =============================

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
     * 
     * @param props
     * @param name
     *            prefix used for storing properties, unique name for this table.
     */
    public void setColumnProperties(OAProperties props, String name) {
        columnProperties = props;
        columnPrefix = name;
        bColumnPropertiesLoaded = false;
    }

    protected void loadColumnProperties() {
        if (bColumnPropertiesLoaded || columnProperties == null || columnPrefix == null) return;
        bColumnPropertiesLoaded = true;
        for (int i = 0; i < 3; i++) {
            String line1 = columnProperties.getProperty(columnPrefix + ".setup" + (i + 1) + ".columns");
            if (line1 == null) continue;
            String line2 = columnProperties.getProperty(columnPrefix + ".setup" + (i + 1) + ".widths");
            if (line2 == null) continue;
            int x = OAString.dcount(line1, ",");

            tcColumnSetup[i] = new OATableColumn[x];
            intColumnSetup[i] = new int[x];

            for (int j = 1; j <= x; j++) {
                String w1 = OAString.field(line1, ',', j);
                if (w1 == null) break;
                w1 = w1.trim();
                String w2 = OAString.field(line2, ',', j);
                if (w2 == null) continue;
                int w = OAConv.toInt(w2);
                if (w <= 0) continue;
                intColumnSetup[i][j - 1] = w;

                int kx = columns.size();
                for (int k = 0; k < kx; k++) {
                    OATableColumn tc = (OATableColumn) columns.elementAt(k);
                    if (!w1.equalsIgnoreCase(tc.origPath)) continue;
                    tcColumnSetup[i][j - 1] = tc;
                    break;
                }
            }
        }
    }

    protected void saveColumnProperties() {
        loadColumnProperties();
        if (!bColumnPropertiesLoaded) return;
        for (int i = 0; i < 3; i++) {
            if (tcColumnSetup[i] == null) continue;
            if (intColumnSetup[i] == null) continue;
            String line1 = "";
            String line2 = "";
            for (int j = 0; j < tcColumnSetup[i].length; j++) {
                if (j > 0) {
                    line1 += ",";
                    line2 += ",";
                }
                if (tcColumnSetup[i][j] != null) {
                    line1 += tcColumnSetup[i][j].path;
                    line2 += intColumnSetup[i][j];
                }
            }
            columnProperties.put(columnPrefix + ".setup" + (i + 1) + ".columns", line1);
            columnProperties.put(columnPrefix + ".setup" + (i + 1) + ".widths", line2);
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
        for (int i = 0; i < x; i++) {
            final OATableColumn tcx = (OATableColumn) columns.elementAt(i);
            menuCheckBoxes[i] = new JCheckBoxMenuItem(tcx.tc.getHeaderValue() + "", tcx.bVisible);
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
            for (int i = 0; i < 3; i++) {
                menuSaveRadios[i] = new JRadioButtonMenuItem("Setup #" + (i + 1), false);
                grp.add(menuSaveRadios[i]);
                menuSaveRadios[i].addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        JRadioButtonMenuItem rad = (JRadioButtonMenuItem) e.getSource();
                        if (rad.isSelected()) {
                            for (int i = 0; i < menuSaveRadios.length; i++) {
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
        for (int i = 0; i < 4; i++) {
            if (i == 0) menuLoadRadios[i] = new JRadioButtonMenuItem("Default", false);
            else {
                menuLoadRadios[i] = new JRadioButtonMenuItem("Setup #" + i, false);
                menuLoadRadios[i].setEnabled(tcColumnSetup[i - 1] != null);
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
                            for (int i = 0; i < menuLoadRadios.length; i++) {
                                if (menuLoadRadios[i] == rad) {
                                    loadColumnSetup(i - 1);
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
        getPopupMenu().show(OATable.this.getTableHeader(), pt.x, pt.y);
    }

    protected void saveColumnSetup(int pos) {
        if (pos > 2 || pos < 0) return;

        int x = columnModel.getColumnCount();
        tcColumnSetup[pos] = new OATableColumn[x];
        intColumnSetup[pos] = new int[x];

        OATableColumn[] tcs = new OATableColumn[x];
        for (int i = 0; i < x; i++) {
            TableColumn tc = columnModel.getColumn(i);
            int x2 = columns.size();
            for (int i2 = 0; i2 < x2; i2++) {
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
        for (; columnModel.getColumnCount() > 0;) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }
        int x = columns.size();
        for (int i = 0; i < x; i++) {
            OATableColumn tcx = (OATableColumn) columns.elementAt(i);
            tcx.bVisible = false;
        }
        x = tcColumnSetup[pos].length;
        for (int i = 0; i < x; i++) {
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
            for (int i = 0; i < x; i++) {
                OATableColumn tcx = (OATableColumn) columns.elementAt(i);
                if (tcx == tc) {
                    int cnt = columnModel.getColumnCount();
                    if (pos != cnt - 1) {
                        columnModel.moveColumn(cnt - 1, pos);
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
        for (; columnModel.getColumnCount() > 0;) {
            columnModel.removeColumn(columnModel.getColumn(0));
        }
        int x = columns.size();
        for (int i = 0; i < x; i++) {
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
         * miFind = new JMenuItem("Find ..."); miFind.addActionListener(new ActionListener() { public
         * void actionPerformed(ActionEvent e) { onFind(); } }); popupMenu.add(miFind);
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
        for (int i = 0; i < columns.size(); i++) {
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
        for (int i = 0; i < columns.size(); i++) {
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
        for (int i = 0; i < x; i++) {
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

    protected String getColumnHeaderToolTipText(OATableColumn tc, Point pt) {
        String s = null;
        
        if (pt != null) {
            if (tc.getOATableComponent() == chkSelection) {
                return "selected rows";
            }
            if (tableRight!=null && tc.getOATableComponent() == tableRight.chkSelection) {
                return "selected rows";
            }
            if (tc == tcCount) {
                if (pt.y > headerRenderer.buttonHeight) {
                    return "reset filters";
                }
            }
            if (tableRight!=null && tc == tableRight.tcCount) {
                if (pt.y > tableRight.headerRenderer.buttonHeight) {
                    return "reset filters";
                }
            }
        }
        
        if (tc != null && tc.getOATableComponent() instanceof JComponent) {
            s = ((JComponent) tc.getOATableComponent()).getToolTipText();
            s = getColumnHeaderToolTipText(tc.getOATableComponent(), s);
            if (s == null || s.length() == 0) {
                if (tc.tc.getHeaderValue() != null) s = tc.tc.getHeaderValue().toString();
            }
        }
        
        if (tc != null && tc.compFilter != null) {
            if (headerRenderer.buttonHeight > 0 && pt.y > headerRenderer.buttonHeight) {
                s = "enter value to filter by "+s;
            }
            else if (tableRight != null && pt.y > tableRight.headerRenderer.buttonHeight) {
                s = "enter value to filter by "+s;
            }
        }
        else {
            if (headerRenderer.buttonHeight > 0 && pt.y > headerRenderer.buttonHeight) {
                s = "no filter for "+s;
            }
            else if (tableRight != null && pt.y > tableRight.headerRenderer.buttonHeight) {
                s = "no filter for "+s;
            }
        }
        
        return s;
    }

    protected String getColumnHeaderToolTipText(OATableComponent comp, String tt) {
        return tt;
    }

    protected void onHeadingRightClick(OATableColumn tc, Point pt) {
        if (columnProperties != null) {
            displayPopupMenu(tc, pt);
        }
    }

    // 20101229 - set by OATableScrollPane
    protected OATable tableLeft;
    protected OATable tableRight;

    protected void setLeftTable(OATable table) {
        this.tableLeft = table;
    }

    public OATable getLeftTable() {
        return tableLeft;
    }

    protected void setRightTable(OATable table) {
        this.tableRight = table;

        setSelectionModel(table.getSelectionModel());
        setSelectHub(table.hubSelect);
        this.chkSelection = table.chkSelection;

        setAllowDrag(table.getAllowDrag());
        setAllowDrop(table.getAllowDrop());
        setAllowSorting(table.getAllowSorting());
        setDoubleClickButton(table.getDoubleClickButton());

        setComponentPopupMenu(table.getMyComponentPopupMenu());

        setIntercellSpacing(table.getIntercellSpacing());
        setRowHeight(table.getRowHeight());
        setShowChanges(table.getShowChanges());

        /*
         * getTableHeader().setResizingAllowed(false); getTableHeader().setReorderingAllowed(false);
         */
    }

    public OATable getRightTable() {
        return tableRight;
    }

    // includes joinedTable from OATableScrollPane
    protected OATableColumn[] getAllTableColumns() {

        int tot = columns.size();
        if (tableLeft != null) tot += tableLeft.columns.size();
        if (tableRight != null) tot += tableRight.columns.size();

        OATableColumn[] allColumns = new OATableColumn[tot];
        int pos = 0;
        for (int z = 0; z < 2; z++) {
            OATable t;
            if (z == 0) {
                if (this.tableLeft != null) t = tableLeft;
                else t = this;
            }
            else {
                if (this.tableRight != null) t = tableRight;
                else t = this;
            }
            for (int i = 0; i < t.columns.size(); i++) {
                OATableColumn col = (OATableColumn) t.columns.elementAt(i);
                allColumns[pos++] = col;
            }
            if (tableLeft == null && tableRight == null) break;
        }
        return allColumns;
    }

    protected void onHeadingClick(OATableColumn tc, MouseEvent e, Point pt) {
        if (!bAllowSorting) return;
        if (tc == null) return;
        if (!tc.getAllowSorting()) return;
        tc.setupTableColumn();

        // 20101229 setup to be able to remove the sort order on a column

        OATableColumn[] allColumns = getAllTableColumns();

        if (e.isControlDown() || e.isShiftDown()) {
            if (tc.sortOrder == 0) {
                int max = 0;
                for (int i = 0; i < allColumns.length; i++) {
                    OATableColumn col = allColumns[i];
                    tc.sortOrder = Math.max(tc.sortOrder, col.sortOrder);
                }
                tc.sortOrder++;
            }
            else {
                boolean bTurnOff = true;
                for (int i = 0; i < allColumns.length; i++) {
                    OATableColumn col = allColumns[i];
                    if (col != tc && col.sortOrder > 0) {
                        bTurnOff = tc.sortDesc;
                        break;
                    }
                }
                if (bTurnOff) {
                    tc.sortOrder = 0;
                    tc.sortDesc = false;
                }
                else tc.sortDesc = !tc.sortDesc;
            }
        }
        else {
            boolean bTurnOff = false;
            if (tc.sortOrder > 0) {
                boolean b = false;
                for (int i = 0; !b && i < allColumns.length; i++) {
                    OATableColumn col = allColumns[i];
                    if (col != tc & col.sortOrder > 0) b = true;
                }
                if (!b) {
                    if (tc.sortDesc) bTurnOff = true;
                    else tc.sortDesc = !tc.sortDesc;
                }
            }
            tc.sortOrder = 1;
            for (int i = 0; i < allColumns.length; i++) {
                OATableColumn col = allColumns[i];
                if (col != tc || bTurnOff) {
                    col.sortOrder = 0;
                    col.sortDesc = false;
                }
            }
        }
        OATable.this.performSort();
        OATable.this.getTableHeader().repaint();
        if (OATable.this.tableLeft != null) {
            OATable.this.tableLeft.getTableHeader().repaint();
        }
        else if (OATable.this.tableRight != null) {
            OATable.this.tableRight.getTableHeader().repaint();
        }
    }

    protected void onHeadingMouseReleased(MouseEvent e, Point pt) {
        if (pt != null && !e.isPopupTrigger()) {
            Rectangle rec = new Rectangle(pt.x - 3, pt.y - 3, 6, 6);
            Point pt2 = e.getPoint();
            if (!rec.contains(pt2)) return;
            // if (pt2 != null && (pt.x != pt2.x || pt.y != pt2.y)) return;
        }

        int column = columnModel.getColumnIndexAtX(e.getX());
        if (column < 0) return;
        int myColumn = columnModel.getColumn(column).getModelIndex();
        OATableColumn tc = null;
        if (myColumn >= 0 && myColumn < columns.size()) {
            tc = (OATableColumn) columns.elementAt(myColumn);
        }
        if (tc == null) return;

        PanelHeaderRenderer bhr = headerRenderer;
        if (bhr == null || tableRight != null) bhr = tableRight.headerRenderer;
        if ( (hubFilter != null || (tableRight!=null && tableRight.hubFilter != null)) && bhr != null && (bhr.buttonHeight > 0 || bhr.getPreferredSize() != null) && pt.y > bhr.buttonHeight) {
            // header editor

            // stop table editor
            TableCellEditor ed = getCellEditor();
            if (ed != null) ed.stopCellEditing();
            if (tableRight != null) {
                ed = tableRight.getCellEditor();
                if (ed != null) ed.stopCellEditing();
            }
            if (tableLeft != null) {
                ed = tableLeft.getCellEditor();
                if (ed != null) ed.stopCellEditing();
            }

            if (tc == tcCount || (tableRight!=null && tc == tableRight.tcCount)) {
                resetFilters();
                return;
            }
            
            // 20150810
            if (tc.getOATableComponent() == this.chkSelection) {
                if (isAnySelected()) {
                    getSelectHub().clear();
                    getSelectionModel().clearSelection();
                }
                else {
                    Hub h = getHub();
                    getSelectionModel().setSelectionInterval(0, h.getSize()-1);
                }
                return;
            }
            
            if (headerRenderer != null) {
                headerRenderer.setupEditor(column);
            }
            return;
        }

        if ((e.getModifiers() & Event.META_MASK) != 0) {
            if (!e.isPopupTrigger()) return;
        }
        if (e.isPopupTrigger()) {
            if (tc != null) onHeadingRightClick(tc, pt);
            return;
        }
        if (tc != null) onHeadingClick(tc, e, pt);
    }

    
    // END END END END END END ===== 2006/12/29 CONSTRUCTION ZONE :) ===== END END END END END END

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        configureEnclosingScrollPane();
    }
    
    // 20101031 improve the look when table does not take up all of viewport
    protected void configureEnclosingScrollPane() {
        super.configureEnclosingScrollPane();
        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane) gp;
                scrollPane.setBackground(getBackground());

                JTableHeader th = getTableHeader();
                if (th != null) th.setBackground(getBackground());
                
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                viewport.setBackground(getBackground());

                /*
                 * JPanel pan = new JPanel(new BorderLayout()); pan.add(getTableHeader(),
                 * BorderLayout.WEST);
                 * 
                 * JButton cmd = new JButton(""); pan.add(cmd, BorderLayout.CENTER);
                 * 
                 * pan.setBackground(getBackground()); scrollPane.setColumnHeaderView(pan);
                 */
            }
        }
    }
    // 20151022 called by removeNotify. Fixed problem when using table in popup, and it calls calls removeNotity
    //   which removes the columnHeaderView, and it's not used for getting the preferred size
    //   this is copied from JTable
    protected void unconfigureEnclosingScrollPane() {
        // this replaces 
        Container p = getParent();
        if (p instanceof JViewport) {
            Container gp = p.getParent();
            if (gp instanceof JScrollPane) {
                JScrollPane scrollPane = (JScrollPane)gp;
                // Make certain we are the viewPort's view and not, for
                // example, the rowHeaderView of the scrollPane -
                // an implementor of fixed columns might do this.
                JViewport viewport = scrollPane.getViewport();
                if (viewport == null || viewport.getView() != this) {
                    return;
                }
                // 20151022 removed this line
                // scrollPane.setColumnHeaderView(null);
                // remove ScrollPane corner if one was added by the LAF
                Component corner =
                        scrollPane.getCorner(JScrollPane.UPPER_TRAILING_CORNER);
                if (corner instanceof UIResource){
                    scrollPane.setCorner(JScrollPane.UPPER_TRAILING_CORNER, 
                            null);
                }
            }
        }
    }

    private boolean bIsProcessKeyBinding;

    // 20101229 add this to be able to left/right arrow between joined tables
    @Override
    protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {

        Hub hub = getHub();
        final int rowBefore = (hub == null) ? 0 : hub.getPos();
        int code = e.getKeyCode();
        int colBefore = getSelectedColumn();

        bIsProcessKeyBinding = true;
        boolean bWasUsed = super.processKeyBinding(ks, e, condition, pressed);
        bIsProcessKeyBinding = false;

        if (!bWasUsed) return false; // only want to know when a key was actually used
        final int rowAfter = (hub == null) ? 0 : hub.getPos();

        if (tableLeft == null && tableRight == null) {
        }
        else {
            if (colBefore == getSelectedColumn()) { // column change was not able to be made
                if (code == KeyEvent.VK_LEFT) {
                    if (colBefore != 0 || tableLeft == null) return true;
                    // goto left table, last column
                    int col = tableLeft.getColumnCount() - 1;
                    tableLeft.setColumnSelectionInterval(col, col);
                    int row = this.getSelectedRow();
                    tableLeft.setRowSelectionInterval(row, row);
                    tableLeft.requestFocus();
                }
                else if (code == KeyEvent.VK_RIGHT) {
                    if (tableRight == null) return true;
                    if (colBefore != this.getColumnCount() - 1) return true;
                    // goto first column in right table
                    tableRight.setColumnSelectionInterval(0, 0);
                    int row = this.getSelectedRow();
                    tableRight.setRowSelectionInterval(row, row);
                    tableRight.requestFocus();
                }
            }
        }

        // 20150512
        if (code == KeyEvent.VK_UP) {
            if (rowAfter == rowBefore && rowBefore != 0) {
                if (hub != null) {
                    int pos = hub.getPos() - 1;
                    // if (pos >= 0) hub.setPos(pos);
                }
            }
        }
        else if (code == KeyEvent.VK_DOWN) {
            if (rowAfter == rowBefore) {
                if (hub != null) {
                    int pos = hub.getPos() + 1;
                    // if (pos < hub.getSize()) hub.setPos(pos);
                }
            }
        }
        else if (code == KeyEvent.VK_HOME) {
            if ((e.getModifiers() & Event.CTRL_MASK) != 0) {
                if (rowAfter == rowBefore) {
                    // if (hub != null) hub.setPos(0);
                }
            }
            else {
                if (tableLeft != null) {
                    tableLeft.setColumnSelectionInterval(0, 0);
                    int row = this.getSelectedRow();
                    tableLeft.setRowSelectionInterval(row, row);
                    tableLeft.requestFocus();
                }
            }
        }
        else if (code == KeyEvent.VK_END) {
            if ((e.getModifiers() & Event.CTRL_MASK) != 0) {
                if (rowAfter == rowBefore) {
                    if (hub != null) {
                        int pos = hub.getSize() - 1;
                        // if (pos >= 0) hub.setPos(pos);
                    }
                }
            }
            else {
                if (tableRight != null) {
                    int col = tableRight.getColumnCount() - 1;
                    tableRight.setColumnSelectionInterval(col, col);
                    int row = this.getSelectedRow();
                    tableRight.setRowSelectionInterval(row, row);
                    tableRight.requestFocus();
                }
            }
        }

        return true;
    }

    @Override
    public TableCellEditor getCellEditor(int row, int column) {
        return super.getCellEditor(row, column);
    }

    // 20150426 adding features from CustomTable
    private int tttCnt;

    @Override
    public void setToolTipText(String text) {
        if (text == null) text = "";
        if (text.length() > 0) text += ((tttCnt++) % 2 == 0) ? "" : " "; // make unique
        super.setToolTipText(text);
    }

    protected int mouseOverRow = -1, mouseOverColumn;
    private Rectangle rectMouseOver;

    public void onMouseOver(int row, int column, MouseEvent evt) {
        super.setToolTipText("");
        if (mouseOverRow == row && mouseOverColumn == column) return;
        mouseOverRow = row;
        mouseOverColumn = column;
        repaint();
        /* 20160203 change to repaint, since treetable was not refreshing correctly on treenode column and mouseOver of the selected row
        if (rectMouseOver != null) {
            repaint(rectMouseOver);
            OATable tx = getLeftTable();
            if (tx != null) {
                tx.repaint();
            }
            tx = getRightTable();
            if (tx != null) {
                tx.repaint();
            }
        }
        if (row < 0) rectMouseOver = null;
        else {
            rectMouseOver = getCellRect(row, column, true);
            repaint(rectMouseOver);
        }
        */
    }


    private JLabel lblDummy;
    private Border borderDummy;

    
    
    //qqqqqqq add more doc here qqqqqqqqq 
    /**
     * Called by getCellRender to customize the renderer.
     * 
     * @param comp
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @param wasChanged
     * @param wasMouseOver
     * @return
     * @see #customizeRenderer(JLabel, JTable, Object, boolean, boolean, int, int, boolean, boolean)
     *      which is called by this method after it sets the defaults.
     */
    public Component getRenderer(Component comp, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver) {
        JLabel lbl = null;
        // 1of3: set default settings
        if (!(comp instanceof JLabel)) {
            if (lblDummy == null) lblDummy = new JLabel();
            lbl = lblDummy;
            lbl.setBackground(Color.cyan);
            lbl.setForeground(Color.cyan);
            if (borderDummy == null) borderDummy = new LineBorder(Color.red);
            lbl.setBorder(borderDummy);
        }
        else lbl = (JLabel) comp;

        if (hub.getAt(row) != null) {
            if (!isSelected && !hasFocus) {
                lbl.setForeground(Color.BLACK);
                if (row % 2 == 0) lbl.setBackground(COLOR_Even);
                else lbl.setBackground(COLOR_Odd);
            }
        }

        if (wasChanged) {
            lbl.setForeground(COLOR_Change_Foreground);
            lbl.setBackground(COLOR_Change_Background);
            lbl.setBorder(BORDER_Change);
            if (isSelected) {
                // lbl.setBorder(??); // use selected background color
            }
        }
        else {
            if (hasFocus && row >= 0 && hub.getPos() == row) {
                lbl.setForeground(Color.white);
                lbl.setBackground(COLOR_Focus);
            }

            if (wasMouseOver) {
                lbl.setForeground(Color.white);
                lbl.setBackground(COLOR_MouseOver);
                lbl.setBorder(BORDER_Focus);
            }
            else lbl.setBorder(null);
        }

        // have the component customize
        OATableComponent oacomp = null;
        if (tableLeft != null && column < tableLeft.columns.size()) {
            OATableColumn tc = (OATableColumn) tableLeft.columns.elementAt(column);
            oacomp = tc.getOATableComponent();

        }
        else if (column >= 0 && column < columns.size()) {
            int x = (tableLeft == null) ? 0 : tableLeft.columns.size();
            OATableColumn tc = (OATableColumn) columns.elementAt(column-x);
            oacomp = tc.getOATableComponent();
        }

        // 1of3: is done, defaults are set

        // 2of3: allow component to customize
        if (oacomp != null) {
            oacomp.customizeTableRenderer(lbl, table, value, isSelected, hasFocus, row, column, wasChanged, wasMouseOver);
        }

        // 3of3: allow App to customize
        customizeRenderer(lbl, table, value, isSelected, hasFocus, row, column, wasChanged, wasMouseOver);

        if (lbl == lblDummy && comp != null) {
            Color c = lblDummy.getBackground();
            if (!Color.cyan.equals(c)) comp.setBackground(c);
            c = lblDummy.getForeground();
            if (!Color.cyan.equals(c)) comp.setForeground(c);
            if (lbl.getBorder() != borderDummy) {
                if (comp instanceof JComponent) {
                    ((JComponent) comp).setBorder(lbl.getBorder());
                }
            }
        }
        
        return comp;
    }

    /**
     * This is called by getRenderer(..) after the default settings have been set.
     */
    public void customizeRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column, boolean wasChanged, boolean wasMouseOver) {
        // to be overwritten
    }
}

/**
 * Class used to bind Table to a Hub.
 */
class MyHubAdapter extends JFCController implements ListSelectionListener {
    OATable table;
    Hub hubSelect;
    private HubListenerAdapter hlSelect;

    AtomicInteger aiIgnoreValueChanged = new AtomicInteger();  // used to ignore calls to valueChanged(...)
    volatile boolean _bIsRunningValueChanged; // flag set when valueChanged is running

    public MyHubAdapter(Hub hub, OATable table) {
        setHub(hub);
        this.table = table;
        table.getSelectionModel().addListSelectionListener(this);
        getHub().addHubListener(this);
        afterChangeActiveObject(null);
    }

    protected boolean getIgnoreValueChanged() {
        if (table.tableLeft != null) {
            if (table.tableLeft.hubAdapter.aiIgnoreValueChanged.get() > 0) return true;
        }
        else if (table.tableRight != null) {
            if (table.tableRight.hubAdapter.aiIgnoreValueChanged.get() > 0) return true;
        }
        return aiIgnoreValueChanged.get() > 0;
    }

    protected boolean getRunningValueChanged() {
        if (table.tableLeft != null) {
            if (table.tableLeft.hubAdapter._bIsRunningValueChanged) return true;
        }
        else if (table.tableRight != null) {
            if (table.tableRight.hubAdapter._bIsRunningValueChanged) return true;
        }
        return _bIsRunningValueChanged;
    }

    protected void setSelectHub(Hub hubSelect) {
        if (this.hubSelect == hubSelect) return;

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
                // 20150424
                if (table.chkSelection != null) table.repaint();
                if (getRunningValueChanged()) return;
                int pos = hub.getPos(obj);

                try {
                    aiIgnoreValueChanged.incrementAndGet();
                    hub.setPos(pos);
                    if (pos >= 0) {
                        ListSelectionModel lsm = table.getSelectionModel();
                        lsm.addSelectionInterval(pos, pos);
                    }
                }
                finally {
                    aiIgnoreValueChanged.decrementAndGet();
                }

                Container cont = table.getParent();
                for (int i=0; i<3 && cont!=null; i++) {
                    cont.repaint();
                    cont = cont.getParent();
                }
            }

            public @Override void afterInsert(HubEvent e) {
                afterAdd(e);
            }

            public @Override void afterRemove(HubEvent e) {
                if (table.chkSelection != null) table.repaint();
                if (getRunningValueChanged()) return;
                int pos = HubDataDelegate.getPos(hub, e.getObject(), false, false);
                // int pos = hub.getPos(e.getObject());
                if (pos >= 0) {
                    try {
                        aiIgnoreValueChanged.incrementAndGet();
                        ListSelectionModel lsm = table.getSelectionModel();
                        lsm.removeSelectionInterval(pos, pos);
                    }
                    catch (Exception ex) {
                        // no-op
                    }
                    finally {
                        aiIgnoreValueChanged.decrementAndGet();
                    }
                }
                Container cont = table.getParent();
                for (int i=0; i<3 && cont!=null; i++) {
                    cont.repaint();
                    cont = cont.getParent();
                }
            }

            public @Override void onNewList(HubEvent e) {
                if (getRunningValueChanged()) return;

                table.myClearSelectionAndLeadAnchor();
                rebuildListSelectionModel();
                if (table.chkSelection != null) table.repaint();
            }
        };
        hubSelect.addHubListener(hlSelect);
        rebuildListSelectionModel();
    }

    protected void rebuildListSelectionModel() {
        for (int i=0; i<5; i++) {
            try {
                aiIgnoreValueChanged.incrementAndGet();
                _rebuildListSelectionModel();
                break;
            }
            catch (Exception e) { 
                // retry again 
            }
            finally {
                aiIgnoreValueChanged.decrementAndGet();
            }
        }
    }
    private void _rebuildListSelectionModel() {
        ListSelectionModel lsm = table.getSelectionModel();
        lsm.clearSelection();

        if (hubSelect == null) {
            int x = hub.getPos();
            if (x >= 0) lsm.addSelectionInterval(x, x);
            return;
        }

        // update hubSelect, to see if objects are in table.hub
        for (int i = 0;; i++) {
            Object obj = hubSelect.getAt(i);
            if (obj == null) {
                break;
            }
            int pos = hub.indexOf(obj); // dont use hub.getPos(), since it will adjust "linkage"
            if (pos < 0) {
                // only remove if it is not in the hubFilterMaster (if used)
                Hub h = table.hubFilterMaster;
                if (h == null && table.tableRight != null) h = table.tableRight.hubFilterMaster;
                if (h != null) pos = h.indexOf(obj);
                if (pos < 0) {
                    hubSelect.removeAt(i);
                    i--;
                }
            }
            else {
                lsm.addSelectionInterval(pos, pos);
            }
        }
    }

    public synchronized void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            return;
        }

        if (_bIsRunningValueChanged) {
            return;
        }

        if (getIgnoreValueChanged()) {
            return;
        }

        int row1 = e.getFirstIndex();
        int row2 = e.getLastIndex();
        if (row2 < 0) row2 = row1;

        _bIsRunningValueChanged = true;

        if (hubSelect != null) {
            ListSelectionModel lsm = table.getSelectionModel();

            int newAoPos = -1;

            for (int i = row1;;) {
                Object obj = table.hub.elementAt(i);
                if (obj != null) {
                    if (lsm.isSelectedIndex(i)) {
                        if (!hubSelect.contains(obj)) {
                            hubSelect.add(obj);
                            newAoPos = i;
                        }
                        else if (newAoPos < 0) newAoPos = i;
                    }
                }
                if (row2 > row1) {
                    i++;
                    if (i > row2) break;
                }
                else {
                    i--;
                    if (i < row2) break;
                }
            }
            for (Object obj : hubSelect) {
                int pos = table.hub.getPos(obj);
                if (pos >= 0 && !lsm.isSelectedIndex(pos)) {
                    hubSelect.remove(obj);
                }
            }

            if (newAoPos < 0) {
                newAoPos = getHub().getPos(hubSelect.getAt(0));
            }

            // newAoPos = table.getEditingRow();
            // if (newAoPos < 0) newAoPos = table.getSelectedRow();
            newAoPos = table.getSelectionModel().getLeadSelectionIndex();

            getHub().setAO(newAoPos);
        }
        else {
            int row = table.getSelectedRow();
            getHub().setPos(row);
            int pos = getHub().getPos();
            if (pos != row) { // if the hub.pos is not the same, set it back
                _bIsRunningValueChanged = false;
                if (pos >= 0) table.setRowSelectionInterval(pos, pos);
                else table.clearSelection();
            }
        }
        _bIsRunningValueChanged = false;
        Container cont = table.getParent();
        for (int i=0; i<3 && cont!=null; i++) {
            cont.repaint();
            cont = cont.getParent();
        }
    }

    public @Override void onNewList(HubEvent e) {

        boolean b = false;
        try {
            if (table.hubAdapter != null) {
                b = true;
                table.hubAdapter.aiIgnoreValueChanged.incrementAndGet();
            }
            table.oaTableModel.fireTableStructureChanged();
        }
        finally {
            if (b && table.hubAdapter != null) table.hubAdapter.aiIgnoreValueChanged.decrementAndGet();
        }
        
        int x = getHub().getPos();
        if (x >= 0) setSelectedRow(x);
        else {
            Rectangle cellRect = new Rectangle(0, 0, 10, 10);
            table.scrollRectToVisible(cellRect);
            // table.repaint();
        }

        // 20101229 new list needs to be resorted
        table.performSort();

        // update hubSelect, to see if objects are in table.hub
        rebuildListSelectionModel();
    }

    public @Override void afterSort(HubEvent e) {
        table.oaTableModel.fireTableStructureChanged();

        int x = getHub().getPos();
        if (x >= 0) setSelectedRow(x);
        else {
            Rectangle cellRect = new Rectangle(0, 0, 10, 10);
            table.scrollRectToVisible(cellRect);
            // table.repaint();
        }
        // table.repaint();

        rebuildListSelectionModel();
    }

    public @Override void afterMove(HubEvent e) {

        // 20110616
        if (table.tableLeft != null || table.tableRight != null) {
            table.repaint();
        }

        table.oaTableModel.fireTableRowsUpdated(e.getPos(), e.getToPos());
        afterChangeActiveObject(e);
        rebuildListSelectionModel();

        final Rectangle cellRect = table.getCellRect(e.getToPos(), 0, true);

        if (SwingUtilities.isEventDispatchThread()) {
            table.scrollRectToVisible(cellRect);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    table.scrollRectToVisible(cellRect);
                    rebuildListSelectionModel();
                }
            });
        }

    }

    public @Override void afterChangeActiveObject(HubEvent e) {
        if (getRunningValueChanged()) return; // 20131113
        if (getIgnoreValueChanged()) return; // 20160127
        
        int row = getHub().getPos();
        if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing();

        // 20131113
        if (table.hubSelect == null) {
            if (row < 0) {
                table.myClearSelectionAndLeadAnchor();
            }
            setSelectedRow(row);
            rebuildListSelectionModel();
        }
        else {
            // 20151225
            table.hubSelect.add(getHub().getAO());
            for (Object obj : table.hubSelect) {
                if (obj != getHub().getAO()) table.hubSelect.remove(obj);
            }
            rebuildListSelectionModel();
        }
    }

    protected void setSelectedRow(final int row) {
        if (getRunningValueChanged()) return;
        if (SwingUtilities.isEventDispatchThread()) {
            _setSelectRow(row);
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    _setSelectRow(row);
                }
            });
        }
    }

    private void _setSelectRow(int row) {
        try {
            aiIgnoreValueChanged.incrementAndGet();
            _setSelectRow2(row);
        }   
        finally {
            aiIgnoreValueChanged.decrementAndGet();
        }
    }
    private void _setSelectRow2(int row) {
        if (table.getCellEditor() != null) table.getCellEditor().stopCellEditing();
        if (row < 0) {
            table.getSelectionModel().clearSelection();
        }
        else {
            row = getHub().getPos();
            try {
                // 20110408 need to allow for selecting multiple lines
                ListSelectionModel lsm = table.getSelectionModel();
                if (!lsm.getValueIsAdjusting() && !lsm.isSelectedIndex(row)) {
                    table.setRowSelectionInterval(row, row);
                }
                // was: table.setRowSelectionInterval(row,row);
            }
            catch (Exception e) { // IllegalArgument: row index out of range. Happens when Hub is
                                  // changed
                return;
            }

            // 20101029 this would scroll to leftmost AO
            Rectangle cellRect;
            if (row < 0) cellRect = new Rectangle(0, 0, 10, 10);
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
    }

    public @Override void afterPropertyChange(HubEvent e) {
        if (!(e.getObject() instanceof OAObject)) return;

        // was: if ( ((OAObject)e.getObject()).isProperty(e.getPropertyName())) {
        table.repaint();
    }

    protected void removeInvoker(final int pos) {
        // 20110616
        if (table.tableRight != null) {
            rebuildListSelectionModel();
            table.repaint();
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            try {
                table.hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                table.oaTableModel.fireTableRowsDeleted(pos, pos);
            }
            finally {
                table.hubAdapter.aiIgnoreValueChanged.decrementAndGet();
            }
            rebuildListSelectionModel();
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        table.hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                        try {
                            table.oaTableModel.fireTableRowsDeleted(pos, pos);
                        }
                        catch (Exception e) {
                        }
                    }
                    finally {
                        table.hubAdapter.aiIgnoreValueChanged.decrementAndGet();
                    }
                    rebuildListSelectionModel();
                }
            });
        }
    }

    /**
     * 20090702, replaced with beforeRemove(), since activeObject is changed before afterRemove is
     * called public @Override void afterRemove(HubEvent e) { removeInvoker(e.getPos()); }
     */
    public @Override void beforeRemove(HubEvent e) {
        removeInvoker(e.getPos());
    }

    @Override
    public void afterRemove(HubEvent e) {
        rebuildListSelectionModel();
        // 20101229 need to reset the activeRow
        int row = getHub().getPos();

        if (table.hubSelect == null) {
            setSelectedRow(row);
        }
    }

    protected void insertInvoker(final int pos) {

        // 20110616
        if (table.tableRight != null) {
            // need to make sure that selectionModel is not changed
            table.repaint();
            rebuildListSelectionModel();
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            try {
                table.hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                table.oaTableModel.fireTableRowsInserted(pos, pos);
            }
            finally {
                table.hubAdapter.aiIgnoreValueChanged.decrementAndGet();
            }
            rebuildListSelectionModel();
        }
        else {
            rebuildListSelectionModel();
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        table.hubAdapter.aiIgnoreValueChanged.incrementAndGet();
                        table.oaTableModel.fireTableRowsInserted(pos, pos);
                    }
                    finally {
                        table.hubAdapter.aiIgnoreValueChanged.decrementAndGet();
                    }
                    rebuildListSelectionModel();
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
            table.setChanged(e.getPos(), -1);
        }
    }

    public @Override void afterFetchMore(HubEvent e) {
        onNewList(e);
    }
}

// 20150518 add header editor, for entering filter data
class PanelHeaderRenderer extends JPanel implements TableCellRenderer {
    OATable table;
    JButton button;
    JLabel label;
    int buttonHeight, labelHeight;

    public PanelHeaderRenderer(OATable t) {
        this.table = t;

        setLayout(new BorderLayout());

        Color c = UIManager.getColor("Table.gridColor");
        if (c == null) c = Color.black;
        Border border = new CustomLineBorder(0, 0, 2, 0, c);
        setBorder(border);
        
        button = new JButton() {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                if (buttonHeight == 0) {
                    buttonHeight = dim.height;
                }
                else dim.height = buttonHeight;
                return dim;
            }
        };
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        add(button, BorderLayout.NORTH);

        label = new JLabel(" ") {
            @Override
            public Dimension getPreferredSize() {
                Dimension dim = super.getPreferredSize();
                if (labelHeight == 0) {
                    labelHeight = dim.height+4;
                }
                else dim.height = labelHeight;
                return dim;
            }
        };
        label.setOpaque(true);
        if (table.hubFilter != null || (table.tableRight != null && table.tableRight.hubFilter != null)) {
            add(label, BorderLayout.CENTER);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        if (labelHeight > 0 & buttonHeight > 0);
        else if (table != null && table.tableRight != null) {
            if (table.tableRight.headerRenderer.labelHeight > 0 & table.tableRight.headerRenderer.buttonHeight > 0) {
                this.labelHeight = table.tableRight.headerRenderer.labelHeight;
                this.buttonHeight = table.tableRight.headerRenderer.buttonHeight;
            }
        }
        else if (table != null && table.tableLeft != null) {
            if (table.tableLeft.headerRenderer.labelHeight > 0 & table.tableLeft.headerRenderer.buttonHeight > 0) {
                this.labelHeight = table.tableLeft.headerRenderer.labelHeight;
                this.buttonHeight = table.tableLeft.headerRenderer.buttonHeight;
            }
        }
        dim.height = labelHeight + buttonHeight;
        return dim;
    }
    
    private Color bgColor;
    private Border border;
    private ImageIcon iconFilter;
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        button.setText((value == null) ? "" : value.toString());
        
        int myColumn = table.getColumnModel().getColumn(column).getModelIndex();
        //OATableColumn tc = (OATableColumn) this.table.columns.elementAt(column);
        final OATableColumn tc = (OATableColumn) ((OATable)table).columns.elementAt(myColumn);

        Component comp = null;
        
        if (this.table.hubFilter != null || (this.table.tableRight != null && this.table.tableRight.hubFilter != null)) {
            OATableComponent tcFilter = tc.getFilterComponent();

            if (border == null) {
                Color c = UIManager.getColor("Table.gridColor");
                if (c == null) c = Color.black;
                //was:  border = new CustomLineBorder(1, 1, 3, 1, c);
                border = new CustomLineBorder(1, 1, 0, 1, c);
                border = new CompoundBorder(border, new EmptyBorder(0,2,0,1));
            }

            label.setHorizontalTextPosition(SwingConstants.RIGHT);
            
            Icon icon = null;
            if (tcFilter != null) {
                label.setBackground(Color.white);

                if (iconFilter == null) {
                    URL url = OAButton.class.getResource("icons/filter16.png");
                    iconFilter = new ImageIcon(url);
                }
                icon = iconFilter;
                
                comp = tcFilter.getTableRenderer(label, table, value, false, false, -1, column);
            }
       
            if (tc.getOATableComponent() == this.table.chkSelection) {
                comp = this.table.chkSelection.getTableRenderer(label, table, value, false, false, -1, column);
            }
            else if (comp == null) {
                comp = label;
                label.setText(" ");
                if (bgColor == null) bgColor = new Color(230,230,230);//bgColor = UIManager.getColor("Table.gridColor");
                Color color = bgColor;
                if (color == null) color = Color.white;
                label.setBackground(color);
            }
            String s = label.getText();
            if (OAString.isEmpty(s)) label.setText(" ");

            if (comp instanceof JComponent) {
                ((JComponent)comp).setBorder(border);
            }
            
            if (tc == this.table.tcCount || (this.table.tableRight != null && tc == this.table.tableRight.tcCount)) {
                if (iconResetFilter == null) {
                    URL url = OAButton.class.getResource("icons/reset.gif");
                    if (url == null) return null;
                    iconResetFilter = new ImageIcon(url);
                }
                label.setText("");
                label.setBorder(null);
                icon = iconResetFilter;
                label.setBackground(Color.white);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
                p.setBorder(null);
                p.setOpaque(true);
                p.setBackground(Color.white);
                p.add(comp);
                comp = p;
            }
            label.setIcon(icon);

            PanelHeaderRenderer.this.removeAll();
            PanelHeaderRenderer.this.setLayout(new BorderLayout());
            PanelHeaderRenderer.this.add(button, BorderLayout.NORTH);
            PanelHeaderRenderer.this.add(comp, BorderLayout.CENTER);
        }

        Icon icon = null;
        if (tc.sortOrder > 0) {
            int pos = tc.sortOrder;
            if (tc.sortOrder == 1) {
                pos = 0;
                int x = this.table.columns.size();
                for (int i = 0; i < x; i++) {
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
        button.setIcon(icon);

        return this;
    }
    
    private Icon iconResetFilter;
    private FocusListener focusListener;
    private Component compFilter;
    
    public void setupEditor(int column) {
        OATableColumn tc = null;
        int myColumn = table.getColumnModel().getColumn(column).getModelIndex();
        if (column >= 0 && column < table.columns.size()) {
            tc = (OATableColumn) table.columns.elementAt(myColumn);
        }
        if (tc == null) return;
        
        Component comp = null;
        OATableComponent tcFilter = tc.getFilterComponent();
        if (tcFilter != null) {
            comp = tcFilter.getTableCellEditor().getTableCellEditorComponent(table, null, false, -1, column);
        }
        
        JTableHeader th = table.getTableHeader();

        if (compFilter != null) {
            compFilter.removeFocusListener(focusListener);
            th.remove(compFilter);
            compFilter = null; 
            focusListener = null;
        }
        if (comp == null) return;
        compFilter = comp;

        focusListener = (new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                Component comp = (Component) e.getSource();
                JTableHeader th = table.getTableHeader();
                comp.removeFocusListener(this);
                th.remove(comp);
                if (compFilter == comp) {
                    compFilter = null;
                    focusListener = null;
                }
                table.getTableHeader().repaint();
                if (table.getLeftTable() != null) table.getLeftTable().getTableHeader().repaint();
                if (table.getRightTable() != null) table.getRightTable().getTableHeader().repaint();
            }
            @Override
            public void focusGained(FocusEvent e) {
            }
        });
        compFilter.addFocusListener(focusListener);
        
        if (comp.getParent() != table.getTableHeader()) {
            table.getTableHeader().add(comp);
        }
        
        Rectangle rect = table.getTableHeader().getHeaderRect(column);
        if (buttonHeight == 0) getPreferredSize();
        rect.y += buttonHeight + 1;
        rect.height -= (buttonHeight + 3);
        rect.x += 2;
        rect.width -= 3;
            
        compFilter.setBounds(rect);
        compFilter.requestFocusInWindow();
        this.table.repaint();
        table.getTableHeader().repaint();
        if (table.getLeftTable() != null) table.getLeftTable().getTableHeader().repaint();
        if (table.getRightTable() != null) table.getRightTable().getTableHeader().repaint();
    }
}
