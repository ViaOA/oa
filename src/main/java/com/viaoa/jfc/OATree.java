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
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.dnd.OATransferable;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.tree.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;


/**
    JTree subclass that binds objects and collections to tree nodes.
    Trees are built using Hubs for root nodes, and property paths for children nodes.  The
    property paths are based on the parent node.  A title node can be used to act as a heading
    for children nodes.
    <p>
    Nodes for trees are built using OANode objects that are then added to the tree as root nodes.
    Nodes can then be added to other nodes to create children nodes.  Recursive nodes can be built
    by adding a node back to the tree.
    <p>
    All nodes work directly with Hubs and objects.  The display value for the node is based on the property
    path defined in the node.
    <p>
    Allows for multiple root nodes.
    <p>
    Trees can be customized by using renderer methods.
    <ol>
    <li>Uses OATreeNode icon, font, background, foreground.
    <li>Calls the select OATreeNode getTreeCellRendererComponent(...)
    <li>Calls OATreeNode listeners getTreeCellRendererComponent(...)
    <li>Calls OATree getTreeCellRendererComponent(...)
    <li>Calls OATree listeners getTreeCellRendererComponent(...)
    </ol>    
    Navigation events/methods can be used to "know" when a node is acted upon.
    <ol>
    <li>nodeSelected()
    <li>objectSelected()
    <li>onDoubleClick()
    </ol>
    Sequence of events when a node is selected
    <ol>
    <li>valueChanged(TreeSelectionEvent) is called
    <li>nodeSelected(TreeSelectionEvent)
    <li>valueChangedImpl(TreeSelectionEvent) depreicated
    <li>nodeSelected(OATreeNodeData) is called for tree
    <li>nodeSelected(OATreeNodeData) is called for selected OATreeNode
    <li>nodeSelected(OATreeNodeData) is called for OATreeListeners for the Tree
    <li>nodeSelected(OATreeNodeData) is called for OATreeListeners for the selected OATreeNode 
    <li>objectSelected(Object) is called for tree
    <li>objectSelected(Object) is called for selected OATreeNode
    <li>objectSelected(Object) is called for OATreeListeners for the selected OATreeNode 
    <li>objectSelected(Object) is called for OATreeListeners for the Tree
    </ol>
    <p>
    Nodes can be set up to have editors that are other OA components, ex: OATextField.
    <p>
    Popup menus can be added to nodes that allow for right click functionality.
    <p>
    Note: when adding recursive nodes (ex: node.add(node2)), make sure nodes are
        completely setup before adding, since the node is cloned if it is already being used.
    <p>
    Example:<br>
    Simple tree showing for listing Employees.  Other examples are listed in the methods and also
    in OATreeNode
    <pre>

    OATree tree = new OATree();
    OATreeTitleNode nodeTitle = new OATreeTitleNode("Employees");
    icon = Application.getIcon("employee.gif");
    nodeTitle.setIcon(new ImageIcon("employee.gif");
    nodeTitle.setPopupMenu(getEmployeesMenu());
    add(nodeTitle);

    node = new OATreeNode("fullName", hubEmployee);
    node.setFont(getFont().deriveFont(font.ITALIC));
    node.setAllowDrop(false);
    node.setIconPropertyPath("employeeGifFileName");
    node.setPopupMenu(getEmployeeMenu());
    nodeTitle.add(node);

    tree.setRowHeight(30);
    tree.setColumns(25);

    
    // setting an active node based on oaObjects
    ItemCategory ic = getItemCategories().getAO();
    Object[] objs = null;
    for ( ;ic!= null; ic=ic.getParentItemCategory()) {
        objs = (Object[]) OAArray.insert(Object.class, objs, ic, 0);
    }
    objs = (Object[]) OAArray.add(Object.class, objs, item);
    OATree t = getTitleTreeNode().getTree();
    t.setSelectedNode(objs);
    
    
    </pre>

    @see OATreeNode
    @see OATreeTitleNode
    @see Hub2TreeNode
    @see OATreeNodeData
    @see #nodeSelected
*/
public class OATree extends JTree implements TreeExpansionListener, TreeSelectionListener, DragGestureListener, DropTargetListener {
    
    private static Logger LOG = Logger.getLogger(OATree.class.getName());
    
    protected OATreeNode root;
    protected OATreeModel model;
    protected int rows, columns, miniRows, miniColumns;
    protected int widthColumn, miniWidthColumn;
    protected OAObject hubObject;
    protected Font fontDefault;
    protected boolean bIsSelectingNode; // used to flag that valueChanged() event is updating hubs
    protected boolean bIsSettingNode; // used to flag that valueChanged() called by hub update, also used by Hub2TreeNode
    protected Object[] lastSelection = new Object[0];
    protected boolean bAllowDrop = false;
    protected boolean bAllowDrag = false;
    protected DropTarget dropTarget;
    protected DragSource dragSource;
    protected boolean bUseIcon=true;
    private int onAddNotifyExpandRow = -1;
    private Object[] onAddNotifySelectNode; // if set before addNotify
    private boolean bAddNotify;
    private boolean bWaitOnAddNotify;
    private  boolean bStructureChanged;
    private boolean onAddNotifyExpandAll;
    private TreeCellRenderer oldRenderer;
    private Vector vecListener;
    private boolean bValueChangedCalled;  // used to track mouseEvents, to know if valueChanged() was called when super.processMouse is called.

    private Hub dragToHub;
    private Object dragToObject;
    private boolean dragAfter;
    private OATreeNode dragToNode;
    
    
    // used for DND support.
    final static DragSourceListener dragSourceListener = new MyDragSourceListener();

    // START Drag&Drop
    static class MyDragSourceListener implements DragSourceListener {
        public void dragEnter(DragSourceDragEvent e) {}
        public void dragOver(DragSourceDragEvent e) { }
        public void dropActionChanged(DragSourceDragEvent e) {}
        public void dragExit(DragSourceEvent e) {}
        public void dragDropEnd(DragSourceDropEvent e) {}
    }

    
    
    public OATree() {
        this(0, 0);
    }
    
    public String getToolTipText() {
    	return super.getToolTipText();
    }
    
    public OATree(int columns) {
    	this(0, columns);
    }

    /**
	    Create a new OATree that has a width based on character size.
	    @param columns default width of tree based on average size of character.
	    @param rows used to call setVisibleRowCount()
	*/
    public OATree(int rows, int columns) {
        this(rows, columns, true);
    }
    public OATree(int rows, int columns, boolean bWaitOnAddNotify) {
        super(new Vector());

        this.bWaitOnAddNotify = bWaitOnAddNotify;
        if (!bWaitOnAddNotify) bAddNotify = true;
        
        this.enableEvents(AWTEvent.FOCUS_EVENT_MASK);

        
        setRootVisible(false);
        root = new OATreeNode("");
        root.def.tree = this;

        model = new OATreeModel(this);
        setModel(model);
        setLargeModel(true);
        // setDragEnabled(true);
        setAutoscrolls(true);  // not working, since OATree does it's own DND

        setupRenderer();
        setupEditor();

        setPreferredSize(rows, columns);
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        dragSource = DragSource.getDefaultDragSource();
        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,this);
        dropTarget = new DropTarget(this,this);

        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false), "ToggleNode");
        getActionMap().put("ToggleNode", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				int[] rows = getSelectionRows();
				if (rows == null || rows.length == 0) return;
				if (isExpanded(rows[0])) collapseRow(rows[0]);
				else expandRow(rows[0]);
			}
		});
        
        ToolTipManager.sharedInstance().registerComponent(this);
        setExpandsSelectedPaths(true);
        
        setBorder(new EmptyBorder(2,2,2,2));
    }

    public @Override void setDragEnabled(boolean b) {
    	super.setDragEnabled(false); // this is auto handled wihtin the class
    }
    
    /**
     * Flag to use the folder icon. Default is true
     * @param b
     */
    public void setUseIcon(boolean b) {
    	this.bUseIcon = b;
    }
    public boolean getUseIcon() {
    	return bUseIcon;
    }

    public @Override void setLargeModel(boolean newValue) {
    	// must be true, since OATree is keeping track of nodes and nodeDatas
    	super.setLargeModel(true);
    }
    public @Override void setRootVisible(boolean b) {
    	super.setRootVisible(false); // needs to be false
    }
    

    /**
        Flag that allows for drag &amp; drop support, default=true.
    */
    public void setAllowDnD(boolean b) {
        setAllowDrop(b);
        setAllowDrag(b);
    }
    public void setAllowDnd(boolean b) {
        setAllowDrop(b);
        setAllowDrag(b);
    }

    /**
        Flag that allows for dropping (DND) support, default=true.
    */
    public boolean getAllowDrop() {
        return bAllowDrop;
    }
    /**
        Flag that allows for dropping (DND) support, default=true.
    */
    public void setAllowDrop(boolean b) {
        bAllowDrop = b;
    }

    /**
        Flag that allows for dragging (DND) support, default=true.
    */
    public boolean getAllowDrag() {
        return bAllowDrag;
    }
    /**
        Flag that allows for dragging (DND) support, default=true.
    */
    public void setAllowDrag(boolean b) {
        bAllowDrag = b;
    }


    /**
        Overloaded from JTree to always return true.  (vv Not sure why)
    */
    public boolean hasBeenExpanded(TreePath path) {
        return true;
    }
    
    
    public boolean isSelectingNode() {
    	return bIsSelectingNode;
    }
    public boolean isSettingNode() {
    	return bIsSettingNode;
    }
    public void setSettingNode(Hub2TreeNode htn, boolean tf) {
    	bIsSettingNode = tf;
    }

    public OATreeNodeData getSelectedTreeNodeData() {
        TreePath tp = getSelectionPath();
        if (tp == null) return null;
        Object[] objs = tp.getPath();
        if (objs.length < 2) return null;

        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        return tnd;
    }
    
    public OATreeNode getSelectedTreeNode() {
        TreePath tp = getSelectionPath();
        if (tp == null) return null;
        Object[] objs = tp.getPath();
        if (objs.length < 2) return null;

        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        OATreeNode node = tnd.node;
        return node;
    }
    
    public Object getSelectedObject() {
        TreePath tp = getSelectionPath();
        if (tp == null) return null;
        Object[] objs = tp.getPath();
        if (objs.length < 2) return null;

        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        OATreeNode node = tnd.node;
        if (node.titleFlag) return null;

        return tnd.object;
    }
    public Hub getSelectedHub() {
        TreePath tp = getSelectionPath();
        if (tp == null) return null;
        Object[] objs = tp.getPath();
        if (objs.length < 2) return null;

        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        OATreeNode node = tnd.node;
        if (node.titleFlag) return null;

        return  tnd.getHub();
    }
    
    
    private OATreeNodeData tndDragging;
    /** Drag and Drop support. */
    public void dragGestureRecognized(DragGestureEvent e) {
        if (!bAllowDrag) return;

        Point pt = e.getDragOrigin();
        int row = getRowForLocation(pt.x, pt.y);
        if (row < 0) return;

        TreePath tp = getPathForRow(row);
        Object[] objs = tp.getPath();
        if (objs.length < 2) return;

        tndDragging = (OATreeNodeData) objs[objs.length-1];
        OATreeNode node = tndDragging.node;
        if (node.titleFlag) return;
        if (!node.getAllowDrag()) return;

        Hub h = tndDragging.getHub();
        if (h == null) return;

        OATransferable t = new OATransferable(h, tndDragging.object);
        dragSource.startDrag(e, null, t, dragSourceListener);
    }

    /** Drag and Drop support. */
    public void dragEnter(DropTargetDragEvent e) {
        if (e.isDataFlavorSupported(OATransferable.HUB_FLAVOR)) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
        else {
            e.rejectDrag();
        }
    }

    
    public void dragOver(DropTargetDragEvent e) {
        dragToHub = null;
        dragToObject = null;
        dragAfter = false;
        dragToNode = null;

        Point pt = e.getLocation();
       	dragOverNode(pt);
        autoscroll(pt);
        
        _dragOver(e);
        
        if (dragToHub != null) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
        else {
            e.rejectDrag();
        }
    }    

    private int lastDragOverRow;
    private long lastDragOverTime;
    // this will expand a row.
    private void dragOverNode(Point location) {
        TreePath path = getClosestPathForLocation(location.x, location.y);
        
        long holdLastTime = lastDragOverTime;
        int holdLastRow = lastDragOverRow;
        lastDragOverTime = 0;
        lastDragOverRow = -1;

        if (path == null) return;
    	if (isExpanded(path)) return;
        
        // setSelectionPath(path);
        lastDragOverRow = getRowForPath(path);
        
    	long t = System.currentTimeMillis();
        if (holdLastRow != lastDragOverRow || holdLastTime == 0) {
        	 lastDragOverTime = t;
        	 return;
        }
        lastDragOverTime = holdLastTime;
        if (holdLastTime + 470 > t) return;

        Object[] objs = path.getPath();
        if (objs.length > 0) {
        	OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        	if (tnd == tndDragging) return;  // dont expand the node that is being dragged
        }
        
		expandPath(path);
    }
    
    private static Insets autoScrollInsets = new Insets(20, 20, 20, 20);
    private void autoscroll(Point cursorLocation) {
        Rectangle rectOuter = getVisibleRect();
        Rectangle rectInner = new Rectangle(
        		rectOuter.x + autoScrollInsets.left,
        		rectOuter.y + autoScrollInsets.top,
        		rectOuter.width - (autoScrollInsets.left + autoScrollInsets.right),
        		rectOuter.height - (autoScrollInsets.top + autoScrollInsets.bottom));
        if (!rectInner.contains(cursorLocation)) {
            Rectangle rect = new Rectangle(
                    cursorLocation.x - autoScrollInsets.left,
                    cursorLocation.y - autoScrollInsets.top,
                    autoScrollInsets.left + autoScrollInsets.right,
                    autoScrollInsets.top + autoScrollInsets.bottom);
            scrollRectToVisible(rect);
        }
    }
    
    
    
    /**
        Drag and Drop support.
    */
    private void _dragOver(DropTargetDragEvent e) {
        if (!getAllowDrop()) return;
        if (!e.isDataFlavorSupported(OATransferable.HUB_FLAVOR)) return;

    	Point pt = e.getLocation();
        int row = getRowForLocation(pt.x, pt.y);

        Hub dragHub = null;
        Object dragObject = null;
        DropTargetDropEvent temp = null;
        try {
        	Transferable t = e.getTransferable();
        	dragObject = t.getTransferData(OATransferable.OAOBJECT_FLAVOR);
        	dragHub = (Hub) t.getTransferData(OATransferable.HUB_FLAVOR);
        }
        catch (Exception ex) {
            System.out.println("OATree.dragOver "+ex);
            ex.printStackTrace();
        }

        if (row < 0) return;
        if (dragObject == null) return;
        
        TreePath tp = getPathForRow(row);
        Object[] objs = tp.getPath();
        if (objs.length < 1) return;

        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        OATreeNode node = tnd.node;

        Rectangle rect = getRowBounds(row);
        int position = 0;  // 0:above  1:below  2:middle
        if (node.def.treeNodeChildren.length == 0) {
            if (pt.y < (rect.y + (rect.height/2))) position = 0;
            else position = 1;
        }
        else {
            if ( (pt.y <= (rect.y + 4)) ) position = 0;
            else if (pt.y >= (rect.y + rect.height - 4) ) position = 1;
            else position = 2;
        }
        if (position == 1) {
            TreePath tp2 = getPathForRow(row+1);
            if (tp2 == null) position = 2;
        }

        if ( position == 0 ) {
            if (row > 0) {
                // check node above
                TreePath tp2 = getPathForRow(row-1);
                Object[] objs2 = tp2.getPath();
                if ( (node.titleFlag || !isExpanded(tp2)) && (objs2.length > 1) ) {
                    OATreeNodeData tnd2 = (OATreeNodeData) objs2[objs2.length-1];
                    OATreeNode node2 = tnd2.node;
                    if (node2.getAllowDrop() && (tnd2.getHub() != dragHub)) {
                        Object obj = tnd2.getObject();
                        if ( node2.getAllowDrop(dragHub, dragObject, tnd2.getHub()) ) {
                            // add to end of node above
                            dragToHub = tnd2.getHub();
                            dragToObject = tnd2.getObject();
                            dragAfter = true;
                            dragToNode = node2;
                        }
                    }
                }
            }
            if (dragToHub == null) {
                // check for insert above of this node
                Object obj = tnd.getObject();
                if (obj != null) {
                    if (node.getAllowDrop() || tnd.getHub() == dragHub) {
                        if ( node.getAllowDrop(dragHub, dragObject, tnd.getHub()) ) {
                            dragToHub = tnd.getHub();
                            dragToObject = obj;
                            dragToNode = node;
                            // insert in this node
                        }
                    }
                }
            }
        }
        else if (position == 1) {
            TreePath tp2 = getPathForRow(row+1);
            Object[] objs2 = tp2.getPath();
            OATreeNodeData tnd2 = null;
            OATreeNode node2 = null;
            if (dragObject != null && objs2.length > 1) {
                tnd2 = (OATreeNodeData) objs2[objs2.length-1];
                node2 = tnd2.node;
            }

            if (!isExpanded(tp) || tp2 == null) {
                // drop after this node
                Object obj = tnd.getObject();
                Hub h = tnd.getHub();
                if (obj != null) {
                    // same class
                    if (node.getAllowDrop() || (h == dragHub)) {
                        if ( node.getAllowDrop(dragHub, dragObject, tnd.getHub()) ) {
                            dragToHub = h;
                            dragToObject = obj;
                            dragAfter = true;
                            dragToNode = node;
                        }
                    }
                }
                if (dragToHub == null && node2 != null && !node2.titleFlag && node2.getAllowDrop()) {
                    if ( node2.getAllowDrop(dragHub, dragObject, tnd2.getHub()) ) {
                        obj = tnd2.getObject();
                        if (obj != null) {
                            // insert before first expanded child node
                            dragToHub = tnd2.getHub();
                            dragToObject = tnd2.getObject();
                            dragToNode = node2;
                        }
                    }
                }
            }
            else {
                // check first expanded child to insert before
                if (dragObject != null && node2 != null) {
                    if (node2.getAllowDrop() || (tnd2.getHub() == dragHub)) {
                        if ( node2.getAllowDrop(dragHub, dragObject, tnd2.getHub()) ) {
                            Object obj = tnd2.getObject();
                            if (obj != null) {
                                // insert before first expanded child node
                                dragToHub = tnd2.getHub();
                                dragToObject = tnd2.getObject();
                                dragToNode = node2;
                            }
                        }
                    }
                }
            }
        }
        else {
            // drop "inside" of node
            // check to see if it can be added to a child node
            for (int i=0; (dragToHub == null) && i<node.def.treeNodeChildren.length; i++) {
                OATreeNode tn = node.def.treeNodeChildren[i];
                if (tn.titleFlag) continue;

                Hub h2 = null;
                if (node.titleFlag && tn.hub != null) {
                    h2 = tn.hub;
                }
                else if (tn.methodsToHub != null) {
                   // Temp Hack: 20101209 changed so that it will always get methods, since a treeNode could be reused (ex: recursive) and the methods could be wrong
                   //   this will be fixed later, when the method path is in the TreeNodeChild        
                   if (tn.methodsToHub != null) {
                       tn.methodsToHub = null;
                       tn.def.methodsToProperty = null;
                   }

                    if (tn.methodsToHub == null && tn.def.methodsToProperty == null) {
                        tn.findMethods(tnd.object.getClass(), true);
                    }
                    if (tn.methodsToHub != null) {
                        h2 = (Hub) OAReflect.getPropertyValue(tnd.object, tn.methodsToHub);
                    }
                }
                if (h2 != null) {
                    if (tn.getAllowDrop() || h2 == dragHub) {
                        if ( tn.getAllowDrop(dragHub, dragObject, h2) ) {
                            dragToHub = h2;
                            dragToNode = tn;
                        }
                    }
                }
            }
        }
    	if (dragToHub != null && !HubAddRemoveDelegate.canAdd(dragToHub, dragObject)) dragToHub = null;  // 2008/04/18
    }

    /** Drag and Drop support. */
    public void dropActionChanged(DropTargetDragEvent e) {
    }
    /** Drag and Drop support. */
    public void dragExit(DropTargetEvent e) {
    }


    /** Drag and Drop support. */
    public void drop(DropTargetDropEvent e) {
        try {
            if (!e.getTransferable().isDataFlavorSupported(OATransferable.HUB_FLAVOR)) return;
            if (dragToHub == null) return;
            bIsSelectingNode = true;
            
            Object dragObject = (Object) e.getTransferable().getTransferData(OATransferable.OAOBJECT_FLAVOR);
            if (dragObject == null) return;
            Hub dragHub = (Hub) e.getTransferable().getTransferData(OATransferable.HUB_FLAVOR);

            if (dragHub == dragToHub) {
                int pos1 = dragHub.getPos(dragObject);
                int pos2;
                if (dragToObject == null) pos2 = dragHub.getSize()-1;
                else {
                    pos2 = dragHub.getPos(dragToObject);
                    if (dragAfter) pos2++;
                    if (pos2 > pos1) pos2--;
                }

                if (pos2 < 0) pos2 = dragHub.getSize()-1;
                dragHub.move(pos1, pos2);
                // 20091214
                OAUndoManager.add(OAUndoableEdit.createUndoableMove(null, dragHub, pos1, pos2));
            }
            else {
            	if (HubAddRemoveDelegate.canAdd(dragToHub, dragObject)) {  // 2008/04/18
                    int x = JOptionPane.showOptionDialog(OAJfcUtil.getWindow(OATree.this), "Ok to move?", "Confirmation", 0, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No" }, "Yes");
                    if (x != 0) return;
            	    
	            	if (HubRootDelegate.getRootHub(dragHub) != null && dragHub.getAO() == dragObject) {  // 2008/04/18
						// this will make sure that a recursive root hub will not be changed to share a child hub.  Ex: Model -> ObjectGraphs (root) -> ObjectGraphs  <== updateHub used for which ever is the active Hub
						dragHub.setActiveObject(null); 
					}
	            	if (dragToObject != null) {
	                    int pos1 = dragToHub.getPos(dragToObject);
	                    if (dragAfter) pos1++;
	                    dragToHub.insert(dragObject, pos1);
	                    // 20091214
	                    OAUndoManager.add(OAUndoableEdit.createUndoableInsert(null, dragHub, dragObject, pos1));
	                }
	                else {
	                	if (!dragToHub.contains(dragObject)) {
	                	    dragToHub.add(dragObject);
	                        OAUndoManager.add(OAUndoableEdit.createUndoableAdd(null, dragHub, dragObject));
	                	}
	                }
            	}
            }
            bIsSelectingNode = false;
            e.dropComplete(true);
            dragToHub.setActiveObject(dragObject);
        }
        catch (UnsupportedFlavorException ex) {
            System.out.println("OATree.drop "+ex);
            ex.printStackTrace();
        }
        catch (IOException ex) {
            System.out.println("OATree.drop "+ex);
            ex.printStackTrace();
        }
        finally {
        	bIsSelectingNode = false;
        }
    }
    //END Drag&Drop


    /**
        Overwritten to know what rows have been manually expanded.  This is used by
        addNotify, in cases where the tree is not configured so that the row is not
        available to be expanded.
    */
    public void expandRow(int x) {
        super.expandRow(x);
        if (!bAddNotify) onAddNotifyExpandRow = x;
    }

    
    private boolean bSettingSelectedNode;
    public boolean isSettingSelectedNode() {
        return bSettingSelectedNode;
    }
    
    
    /**
     * This will select a node, given an object or object path.
     * If the objects are not from the top of the tree, then the tree will be scanned to find the 
     * given object(s)
     *
     * @see #getTreePath(Object...)
     */
    public void setSelectedNode(Object... objects) {
        try {
            bSettingSelectedNode = true;
            _setSelectedNode(objects);
        }
        finally {
            bSettingSelectedNode = false;
        }
    }
    private void _setSelectedNode(Object... objects) {
        if (objects == null || objects.length == 0) {
            return; // might want to deselect all
        }
        onAddNotifySelectNode = null;
        if (!bAddNotify) {
            onAddNotifySelectNode = objects;
            return;
        }
        TreePath tp = getTreePath(objects);
        if (tp == null) return;
        
        expandPath(tp.getParentPath());

        setSelectionPath(tp);
        scrollPathToVisible(tp);
    }

    /**
     * Creates a TreePath from the given object(s).  Objects can be a complete
     * or partial list from the root of tree, and can include OATreeNode or value of node object
     */
    public TreePath getTreePath(Object... objects) {
        OATreeNodeData nodeData = (OATreeNodeData) getModel().getRoot();  // the "invisible" root

        if (objects == null) return new TreePath(nodeData);

        ArrayList<OATreeNodeData> al = new ArrayList<OATreeNodeData>();

        int x = objects.length;
        if (objects[x-1] instanceof OATreeNodeData) {
            OATreeNodeData tnd = (OATreeNodeData) objects[x-1];
            for ( ;tnd != null; ) {
                al.add(0, tnd);
                tnd = tnd.getParent();
            }
            TreePath tp = new TreePath(al.toArray());
            return tp;
        }

        al.add(nodeData);
        
        for (int i=0; i<objects.length; i++) {
            boolean bFound = false;

            OATreeNode tn = null;
            Object objFind = objects[i];

            if (objFind instanceof OATreeNode) {
                tn = (OATreeNode) objFind;
                if (tn.def.updateHub != null) {
                    objFind = tn.def.updateHub.getAO();
                }
                else objFind = null;
                if (objFind == null && tn.hub != null) {
                    Object obj = tn.hub.getAO();
                    if (obj != null) objFind = obj;
                }
            }
            
            bFound = findNode(al, nodeData, tn,  objFind);
            if (!bFound) {
                //System.out.println("setSelection failed, did not find path");
                return null;
            }
            nodeData = al.get(al.size()-1);

        }        
        TreePath tp = new TreePath(al.toArray());
        return tp;
    }
    
    protected boolean findNode(ArrayList<OATreeNodeData> al, OATreeNodeData fromNodeData, OATreeNode nodeFind, Object objFind) {
        return _findNode(al, fromNodeData, nodeFind, objFind, 0);
    }
    protected boolean _findNode(ArrayList<OATreeNodeData> al, OATreeNodeData fromNodeData, OATreeNode nodeFind, Object objFind, final int counter) {
        if (counter > 20) {
            LOG.warning("counter > 20, will return false");
            return false;
        }
        boolean bFound = false;
        for (int j=0; !bFound && j < fromNodeData.getChildCount(); j++) {
            OATreeNodeData tnd = fromNodeData.getChild(j);
            if (tnd.node == nodeFind && tnd.object == objFind) {
                al.add(tnd);
                bFound = true;
            }
            else if (nodeFind == null && tnd.object == objFind) {
                al.add(tnd);
                bFound = true;
            }
        }
        for (int j=0; !bFound && j < fromNodeData.getChildCount(); j++) {
            OATreeNodeData tnd = fromNodeData.getChild(j);
            // try others - this is in case the select objects[] are not from the top of tree
            int pos = al.size();
            bFound = _findNode(al, tnd, nodeFind, objFind, counter+1);
            if (bFound) {
                al.add(pos, tnd);
            }
        }
        return bFound;
    }
    
    
    public void expandAll() {
        if (!bAddNotify) onAddNotifyExpandAll = true;
        else expandAll(new TreePath(model.getRoot()), true, false);
    }
    public void collapseAll() {
    	expandAll(new TreePath(model.getRoot()), false, true);
    }
    private void expandAll(TreePath parent, boolean expand, boolean bChildrenOnly) {
        OATreeNodeData node = (OATreeNodeData) parent.getLastPathComponent();
        if (!expand) {
            if (!node.areChildrenLoaded()) return;
        }
        for (int i=0; i < node.getChildCount(); i++) {
            TreePath path = parent.pathByAddingChild(node.getChild(i));
            expandAll(path, expand, false);
        }
        
        if (!bChildrenOnly) {
	        // Expansion or collapse must be done bottom-up
	        if (expand) {
	            expandPath(parent);
	        } else {
	    		collapsePath(parent);
	        }
        }
    }

    // 20110106
    /**
     * @see #getTreePath(Object...)
     */
    public void expandOnly(Object... objects) {
        TreePath tp = getTreePath(objects);
        collapseAll();
        if (tp != null) {
            expandPath(tp.getParentPath());
            setSelectionPath(tp);
        }
    }
    public void expandOnlySelectedTreeNode() {
        expandOnly(getSelectedTreeNodeData());
    }
    
    /**
     * @see #getTreePath(Object...)
     */
    public void expand(Object... objects) {
        TreePath tp = getTreePath(objects);
        if (tp != null) expandPath(tp);
    }
    /**
     * @see #getTreePath(Object...)
     */
    public void collapse(Object... objects) {
        TreePath tp = getTreePath(objects);
        if (tp != null) collapsePath(tp);
    }

    /**
     * Internally used to know if structure has changed before the tree is added.
     */
    public boolean isReady(boolean bStructureChange) {
    	if (bStructureChange) {
    	    this.bStructureChanged = true;
    	}
    	return bAddNotify;
    }

    /**
        Overwritten to allow for handling rows that have been manually expanded.
        Sets up listeners for tree expansion events (TreeExpansionListener).
    */
    public void addNotify() {
        addNotify(true);
    }
    public void addNotify(boolean bExpandedVersion) {
        if (bExpandedVersion) {
            bAddNotify = true;
            addTreeExpansionListener(this);
            addTreeSelectionListener(this);
        }        
        super.addNotify();
        
        if (!bExpandedVersion) return;
        
        if (bStructureChanged) {
            if (SwingUtilities.isEventDispatchThread()) {
                model.fireTreeStructureChanged();
                bStructureChanged = false;
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        model.fireTreeStructureChanged();
                        bStructureChanged = false;
                    }
                });
            }
        }

        
        if (onAddNotifyExpandRow >= 0) {
            if (SwingUtilities.isEventDispatchThread()) doExpandRow();
            else {
                SwingUtilities.invokeLater(new Runnable() {
                	public void run() { 
                		doExpandRow(); 
                	}
                });
            }
        }
        if (onAddNotifyExpandAll) {
            onAddNotifyExpandAll = false;
            if (SwingUtilities.isEventDispatchThread()) expandAll();
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        expandAll();
                    }
                });
            }
        }
        
        if (onAddNotifySelectNode != null) {
            if (SwingUtilities.isEventDispatchThread()) doSelectObject();
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        doSelectObject();
                    }
                });
            }
        }
// 20180526 replaced with OASplitPane        
//        addNotify2();
    }

    // 20120711 need to set divider if in a splitpane
    public void addNotify2() {
// 20180526 not called, replaced with OASplitPane        
        Dimension d = this.getPreferredSize();
        if (d == null) return;
        
        Container containerLast = null;
        for (Container c = this.getParent(); c!=null ; c=c.getParent()) {
            if (d == null || !(c instanceof JSplitPane)) {
                containerLast = c;
                continue;
            }
            JSplitPane split = (JSplitPane) c;
            
            if (split.getLeftComponent() != containerLast) {
                break;
            }
            
            int loc = split.getDividerLocation();
            if (split.getOrientation() == JSplitPane.HORIZONTAL_SPLIT) {
                if (loc < d.width) {
                    split.setDividerLocation(d.width);
                }
            }            
            else {
                if (split.getDividerLocation() < d.height) {
                    split.setDividerLocation(d.height);
                }
            }
            //split.resetToPreferredSizes();
            break;
        }
        
    }

    
    private void doSelectObject() {
        try {
            setSelectedNode(onAddNotifySelectNode);
        }
        catch (Exception ex) {
            System.out.println("OATree.addNotify() "+ex);
        }
    }

    
    private void doExpandRow() {
        try {
            expandRow(onAddNotifyExpandRow);
            onAddNotifyExpandRow = -1;
        }
        catch (Exception ex) {
            System.out.println("OATree.addNotify() "+ex);
        }
    }


    /**
        Removes listeners used for expanding rows.
    */
    public void removeNotify() {
        if (bWaitOnAddNotify) {
            bAddNotify = false;
        }
        removeTreeExpansionListener(this);
        removeTreeSelectionListener(this);
        super.removeNotify();
    }

    public int getMouseOverRow() {
        return rowLastMouse2;
    }
    
    protected void setupRenderer() {
        oldRenderer = getCellRenderer();
        setCellRenderer( new TreeCellRenderer() {
            private int lastCntUpdateUI;
            public Component getTreeCellRendererComponent(JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus) {

                if (oldRenderer == null) {
                    oldRenderer = getCellRenderer();
                    if (oldRenderer == null) {
                        return null;
                    }
                }

                JLabel lbl = (JLabel)oldRenderer.getTreeCellRendererComponent(tree,value,selected,expanded,leaf,row, hasFocus);

                if (OATree.this.cntUpdateUI != lastCntUpdateUI) {
                    lbl.updateUI();
                    lastCntUpdateUI = OATree.this.cntUpdateUI; 
                }
                lbl.setHorizontalTextPosition(JLabel.RIGHT);
                lbl.setOpaque(false);
                
                if (value == null) return lbl;
                OATreeNode tn = ((OATreeNodeData)value).node;

                if (!OATree.this.bUseIcon || !tn.def.bUseIcon) lbl.setIcon(null);
                if (tn.def.icon != null) lbl.setIcon(tn.def.icon);

                if (tn.def.font != null) {
                    lbl.setFont(tn.def.font);
                }
                else {
                   Font f = lbl.getFont();
                   if (fontDefault == null) fontDefault = f;
                   if (fontDefault != null) {
                       if (f == null || !f.equals(fontDefault)) lbl.setFont(fontDefault);
                   }
                }
                
                if (tn.def.colorBackground != null) lbl.setBackground(tn.def.colorBackground);
                if (tn.def.colorForeground != null) lbl.setForeground(tn.def.colorForeground);

                Component comp = lbl;
                comp = tn.getTreeCellRendererComponent(comp,tree,value,selected,expanded,leaf,row,hasFocus);

                OATreeListener[] l = tn.getListeners();
                if (l != null) {
                    for (int i=0; i<l.length; i++) {
                        comp = l[i].getTreeCellRendererComponent(comp,tree,value,selected,expanded,leaf,row,hasFocus);
                    }
                }

                comp = OATree.this.getTreeCellRendererComponent(comp,tree,value,selected,expanded,leaf,row,hasFocus);
                l = getListeners();
                if (l != null) {
                    for (int i=0; i<l.length; i++) {
                        comp = l[i].getTreeCellRendererComponent(comp,tree,value,selected,expanded,leaf,row,hasFocus);
                    }
                }

                // mouseover effect                
                if (row == rowLastMouse2 && rowLastMouse2 >= 0) {
                    if (!selected) {
                        lbl.setBackground(OATable.COLOR_MouseOver);
                        lbl.setForeground(Color.white);
                        lbl.setOpaque(true);
                    }
                }
                
                return comp;
            }
        });
    }

    public void addListener(OATreeListener l) {
        if (vecListener == null) vecListener = new Vector(2,2);
        if (!vecListener.contains(l)) vecListener.addElement(l);
    }
    public void removeListener(OATreeListener l) {
        vecListener.removeElement(l);
    }
    protected OATreeListener[] getListeners() {
        if (vecListener == null) return null;
        OATreeListener[] l = new OATreeListener[vecListener.size()];
        vecListener.copyInto(l);
        return l;
    }

    void setupEditor() {
        setCellEditor( new OATreeCellEditor(this) );
        setEditable(true);
    }


    /**
        Returns the <i>invisible</i> root node.  The children of this node are the <i>real</i> root nodes
        for the tree.
    */
    public OATreeNode getRoot() {
        return root;
    }

    /**
        Add a top Root Node to this tree. Multiple roots nodes can be added.
        All root nodes that have objects to display need to have a Hub assigned.
        <p>
        See OATreeNode for examples.

        @see #getRoot
        @see OATreeNode#add(OATreeNode)
    */
    public void add(OATreeNode node) {
        if (node.hub == null && !node.titleFlag) {
            throw new RuntimeException("OATree.add() node is not a TitleNode and does not have a Hub assigned");
        }

        root.add(node);
        model.fireTreeStructureChanged(root);
    }

    /**
        Same as calling add() to create a root node.
        @see #add(OATreeNode)
    */
    public void setRoot(OATreeNode node) {
        add(node);
    }

    /** Tree listener support */
    public void treeExpanded(TreeExpansionEvent e) {
    }
    /** Tree listener support */
    public void treeCollapsed(TreeExpansionEvent e) {
    }

    /**
        Used to find out if a leaf node is currently selected.
    */
    public boolean isLeafSelected() {
        int i = lastSelection.length;
        if (i == 0) return false;
        return getModel().isLeaf(lastSelection[i-1]) || getModel().getChildCount(lastSelection[i-1]) == 0;
    }

    public Object[] getLastSelection() {
    	return lastSelection;
    }
    
    /**
        Get a string array of treePath of selected node.
    */
    public String[] getSelectionAsString() {
        Vector vec = new Vector(3,3);
        for (int i=1; i < lastSelection.length; i++) {
            OATreeNodeData d = (OATreeNodeData) lastSelection[i];
            if (!d.node.titleFlag) vec.addElement(d.toString());
        }
        String[] s = new String[vec.size()];
        vec.copyInto(s);
        return s;
    }


    protected boolean isValidSelection(TreeSelectionEvent e) {
        return true;
    }
    
    private volatile int valueChangeCounter;
    /**
        Calls nodeSelected(e) method, nodeSelected(node) and objectSelected()
        <p>
        Note: do not overwrite this method, use "nodeSelected(e)" instead.
        @see #nodeSelected(TreeSelectionEvent)
        @see #nodeSelected(OATreeNode)
        @see #objectSelected(Object)
    */
    public @Override void valueChanged(final TreeSelectionEvent e) {
        if (e == null) return;

        
        if (bIsSettingNode) return;
        if (bSettingSelectedNode) return;
        
        
        final int cnt = ++valueChangeCounter;
        if (!isValidSelection(e)) return;
        
        bValueChangedCalled = true; // flag used by mouseEvent to know if valueChanged is called.

        // 20170805 does not need to run in swingworker, since the beforeObjectSelected method could be doing UI work
        //   and any server call to get data is now handled in background thread
        TreePath tp = e.getNewLeadSelectionPath();
        if (tp != null) {
            Object[] objs = tp.getPath();
            OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
            tnd.node.beforeObjectSelected(tnd.object);
        }
        try {
            _valueChanged(e);
        }
        catch (Exception t) {
            System.out.println("OATree.valueChanged.done exception="+t+" ... will ignore");
            t.printStackTrace();
        }
        
        /*was
        // 20110131 add swingworker
        SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    _doInBackground();
                }
                catch (Exception t) {
                    System.out.println("OATree.valueChanged.doInBackground exception="+t);
                    throw t;
                }
                return null;
            }
            protected Void _doInBackground() throws Exception {
                if (cnt != valueChangeCounter) return null;

                TreePath tp = e.getNewLeadSelectionPath();
                if (tp != null) {
                    Object[] objs = tp.getPath();
                    OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
                    tnd.node.beforeObjectSelected(tnd.object);
                }
                return null;
            }
            @Override
            protected void done() {
                if (cnt == valueChangeCounter) {  // this does not need to be sync, since AWT thread is used in both places that check counter
                    try {
                        _valueChanged(e);
                    }
                    catch (Exception t) {
                        System.out.println("OATree.valueChanged.done exception="+t+" ... will ignore");
                        t.printStackTrace();
                    }
                }
            }
        };
        sw.execute();
        */
    }
    private void _valueChanged(TreeSelectionEvent e) {    
        TreePath tp = e.getNewLeadSelectionPath();
        Object[] objs = null;
        OATreeNodeData tnd = null;
        OATreeNode tn = null;
        if (tp != null) {
            objs = tp.getPath();
            tnd = (OATreeNodeData) objs[objs.length-1];
            tn = tnd.node;
        }

        try {
        	bIsSelectingNode = true;  // block Hub2TreeNode from having hubAO change generating a tree event
            nodeSelected(e);  // updates node hubs and activeObjects
        }
        finally {
        	bIsSelectingNode = false;
        }

        if (tp == null) {
            nodeSelected((OATreeNodeData)null);
        }
        else {
            nodeSelected(tnd);
            tn.nodeSelected(tnd);
    
            OATreeListener[] l;
            
            l = getListeners();
            if (l != null) {
                for (int i=0; i<l.length; i++) {
                    l[i].nodeSelected(tnd);
                }
            }
            l = tn.getListeners();
            if (l != null) {
                for (int i=0; i<l.length; i++) {
                    l[i].nodeSelected(tnd);
                }
            }
            
            tn.objectSelected(tnd.getObject());
            objectSelected(tnd.getObject());
    
            l = getListeners();
            if (l != null) {
                for (int i=0; i<l.length; i++) {
                    l[i].objectSelected(tnd.getObject());
                }
            }
            l = tn.getListeners();
            if (l != null) {
                for (int i=0; i<l.length; i++) {
                    l[i].objectSelected(tnd.getObject());
                }
            }
        }
    }

    
    
    /**
        This will get called when a node is selected/reselected directly from tree.
        This can be overwritten to "know" when a node is selected.
        <p>
        This is called by valueChanged()
        @see #valueChanged(TreeSelectionEvent)
        @see #objectSelected
        @see #nodeSelected(TreeSelectionEvent)
    */
    public Component getTreeCellRendererComponent(Component comp, JTree tree,Object value,boolean selected,boolean expanded,boolean leaf,int row,boolean hasFocus) {
        return comp;
    }

    
    /**
        This will get called when a node is selected/reselected directly from tree.
        This can be overwritten to "know" when a node is selected.
        <p>
        This is called by valueChanged()
        @see #valueChanged(TreeSelectionEvent)
        @see #objectSelected
        @see #nodeSelected(TreeSelectionEvent)
        @see #add
    */
    public void nodeSelected(OATreeNodeData tnd) {
    	if (tnd == null) nodeSelected((OATreeNode)null);
    	else nodeSelected(tnd.node);
    }

    /**
	    This will get called when a node is selected/reselected directly from tree.
	    This can be overwritten to "know" when a node is selected.
	    <p>
	    This is called by nodeSelected(OATreeNodeData)
	*/
	public void nodeSelected(OATreeNode node) {
	}
    

    /**
        This will get called when a node is selected/reselected directly from tree.
        This can be overwritten to "know" when a node is selected.
        <p>
        This is called by valueChanged()
        @see #valueChanged(TreeSelectionEvent)
        @see #objectSelected
        @see #nodeSelected(OATreeNodeData)
    */
    public void objectSelected(Object obj) {
    }

    /**
        This will get called when a node double clicked
        This can be overwritten to "know" when a node is double clicked.
        <p>
        This is called by valueChanged()
        @see #valueChanged(TreeSelectionEvent)
        @see #objectSelected
        @see #nodeSelected(OATreeNodeData)
    */
    public void onDoubleClick(OATreeNode node, Object object, MouseEvent e) {
    }

    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
    }

    
    protected @Override void processMouseMotionEvent(MouseEvent e) {
        this._processMouseMotionEvent(e);
        
        Point pt = e.getPoint();
        int row = this.getRowForLocation(pt.x, pt.y);
        
        if (row == rowLastMouse2) return;
        
        rowLastMouse2 = row;
        if (recLastMouse2 != null) this.repaint(recLastMouse2);

        TreePath tp = getPathForRow(row);
        if (tp != null) {
            Object[] objs = tp.getPath();
            OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
            OATreeNode node = tnd.node;
            
            recLastMouse2 = getRowBounds(row);
            rowLastMouse2 = row;
            
            if (recLastMouse2 != null) {
                this.repaint(recLastMouse2);
            }
        }        
        else {
            recLastMouse2 = null;
            row = -1;
        }
    }
    
    protected void _processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);
        downLastMouse = false;
        updateCheckBox(e.getPoint());        
    }
    
    
    
    private int rowLastMouse2 = -1;
    private Rectangle recLastMouse2;
    
    @Override
    protected void processMouseEvent(MouseEvent e) {
        this._processMouseEvent(e);
        if (e.getID() == MouseEvent.MOUSE_EXITED) {
            if (recLastMouse2 != null) {
                rowLastMouse2 = -1;
                this.repaint(recLastMouse2);
                recLastMouse2 = null;
            }
        }
    }
    
    /**
        Handles popup menus for TreeNodes.
    */
    protected void _processMouseEvent(MouseEvent mouseEvent) {
        if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED) {
            bValueChangedCalled = false;  // this is used with valueChanged() to know if super.processMouse() calls valueChanged()
            downLastMouse = true;
            OATreeNodeData tnd = updateCheckBox(mouseEvent.getPoint());
            if (tnd != null) { // checkbox was clicked
            	tnd.node.checkBoxClicked(tnd);
            	mouseEvent.consume();
            	return;        
            }
        }
        else downLastMouse = false;

        boolean bDoubleClicked = false;
        
        // 20101230 need to triple-click node that is not a leaf
        if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED && !bValueChangedCalled) {
            if (!mouseEvent.isPopupTrigger()) {
                int cc = mouseEvent.getClickCount();
                if (cc > 1) {
                    int[] rows = getSelectionRows();
                    if (rows != null && rows.length != 0) {
                        if (isLeafSelected()) {
                            bDoubleClicked = (cc == 2);
                        }
                        else {
                            bDoubleClicked = (cc == 3);
                        }
                    }
                }
            }
        }
        
        try {
            super.processMouseEvent(mouseEvent);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "", e);
        }
        
        
        TreePath tp = getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
        if (mouseEvent.getID() == MouseEvent.MOUSE_PRESSED && !bValueChangedCalled) {
            OATree.this.setSelectionRow(getRowForPath(tp));
            if (tp != null) {
                TreeSelectionEvent tse = new TreeSelectionEvent(OATree.this, tp, false, tp, tp);
                valueChanged(tse);

                if (bDoubleClicked) {
                    if (tp != null) {
                        Object[] objs = tp.getPath();
                        if (objs != null && objs.length > 0) {
                            OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
                            tnd.node.onDoubleClick(tnd.getObject(), mouseEvent);
                            this.onDoubleClick(tnd.node, tnd.getObject(), mouseEvent);

                            OATreeListener[] l = tnd.node.getListeners();
                            if (l != null) {
                                for (int i=0; i<l.length; i++) {
                                    l[i].onDoubleClick(tnd.node, tnd.getObject(), mouseEvent);
                                }
                            }
                            l = getListeners();
                            if (l != null) {
                                for (int i=0; i<l.length; i++) {
                                    l[i].onDoubleClick(tnd.node, tnd.getObject(), mouseEvent);
                                }
                            }
                            
                        }
                    }
                }
            }        
        }
        else if (mouseEvent.getID() == MouseEvent.MOUSE_RELEASED) {
            if (mouseEvent.isPopupTrigger()) {
                if (tp != null) {
                    Object[] objs = tp.getPath();
                    super.setSelectionPath(tp); // make sure node is selected
                    if (objs != null && objs.length > 0) {
                        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
                        tnd.node.popup(mouseEvent.getPoint());
                    }
                }
            }
        }
    }

    protected int rowLastMouse;
    protected int xLastMouse;  // within row editor
    protected Rectangle recLastMouse;
    protected boolean downLastMouse;
    
    protected OATreeNodeData updateCheckBox(Point pt) {
        // 20101215 added try/catch - getRowForLocation is throw npe 
        for (int i=0; i<3; i++) {
            try {
                return _updateCheckBox(pt);  
            }
            catch (Exception e) {
                System.err.println("OATree.updateCheckBox - caught exception, will retry 3 times.  ex="+e);
                e.printStackTrace();
            }
        }
        return null;
    }
    // 2008/04/23
    private OATreeNodeData _updateCheckBox(Point pt) {
    	if (pt == null) return null;
    	
    	int rowHold = rowLastMouse;
    	Rectangle recHold = recLastMouse;
  
    	
	    rowLastMouse = this.getRowForLocation(pt.x, pt.y);

        // check the node
        TreePath tp = getPathForRow(rowLastMouse);
        if (tp == null) {
            if (recHold != null) this.repaint(recHold);
        	return null;
        }
        
        Object[] objs = tp.getPath();
        OATreeNodeData tnd = (OATreeNodeData) objs[objs.length-1];
        OATreeNode node = tnd.node;
        
    	downLastMouse = false;
        if (rowHold != rowLastMouse && recHold != null) this.repaint(recHold);

        if (!node.needsCheckBox()) return null;
        
        
        if (rowLastMouse < 0) return null;
        
        recLastMouse = getRowBounds(rowLastMouse);
        if (recLastMouse != null) {
        	xLastMouse = pt.x - recLastMouse.x;
        	this.repaint(recLastMouse);
        	
        	int w = node.checkIcon == null ? 20 : node.checkIcon.getIconWidth();
        	
            if (xLastMouse > 0 && xLastMouse < w) return tnd;
        }
        return null;
    }
    
    
    
    /**
        This will get called when a node is selected/reselected directly from tree.
        This can be overwritten to "know" when a node is selected.
        <p>
        This is called by valueChanged()
        Also can be done by using the OATreeNode.addListener()
        <p>
        Example:<br>
        <pre>
        public void nodeSelected(TreeSelectionEvent e) {
            super.nodeSelected(e);
            TreePath tp = e.getNewLeadSelectionPath();
            if (tp == null) return;

            Object[] objects = tp.getPath();  // OATreeNodeData objects
            if (objects.length == 0) return;

            OATreeNodeData tnd = (OATreeNodeData) objects[objects.length-1];
            Hub h = tnd.getHub();
            if (h != null) {
                Class c = h.getObjectClass();
                if (c.equals(User.class)) // do something
                else if (c.equals(Species.class)) // do something
            }
            else {
                OATreeNode tn = tnd.getNode();
                if (tn instanceof OATreeTitleNode) {
                    OATreeTitleNode n = (OATreeTitleNode) tn;
                    String title = n.getTitle();

                    if (title.equals(USER_TABNAME)) // do something
                    else if (title.equals(SPECIES_TABNAME)) // do something
                }
            }
        }
        </pre>
        @see #valueChanged(TreeSelectionEvent)
        @see #nodeSelected(OATreeNodeData)
        @see #objectSelected(Object)
    */
    public void nodeSelected(TreeSelectionEvent e) {
        if (e == null) return;
        TreePath tp = e.getNewLeadSelectionPath();
        if (tp == null) return;
        
        int row = getRowForPath(tp);
        if (row < 0) return;
        int[] rows = this.getSelectionRows();
        if (rows == null) return; 
        if (rows.length != 1 || rows[0] != row) this.setSelectionRow(row);
        
        lastSelection = tp.getPath();
        if (lastSelection == null) return;

        HashSet<Hub> hsUpdateHub = new HashSet<Hub>(); // hubs that get updated
        
        for (int i = 1; i<lastSelection.length; i++) {
            final OATreeNodeData tnd = (OATreeNodeData) lastSelection[i];
            final boolean bLastNode = (i == lastSelection.length - 1);
            
            // 20120228 if titleNode is selected, then find first child node to display
            OATreeNode tnUse = tnd.node;
            OATreeNodeData tndUse = tnd;
            Hub hubAdditonalUpdate = null; // 20171106
            
            if (bLastNode && tnd.node instanceof OATreeTitleNode) {
                OATreeNode[] tns = tnd.node.getChildrenTreeNodes();
                if (tns != null) {
                    for (OATreeNode tn : tns) {
                        if (tn.def.updateHub != null) {
                            tnUse = tn;
                            break;
                        }
                    }
                }
                int x = tnd.getChildCount();
                if (x == 0) { // 20171106
                    hubAdditonalUpdate = tnUse.def.updateHub;
                }
                for (int ii=0; tnUse != null && ii<x; ii++) {
                    OATreeNodeData tndx = tnd.getChild(ii);
                    if (tndx != null && tndx.node == tnUse) {
                        tndUse = tndx;
                        break;
                    }
                }
            }
            
            if (tndUse.node.def.updateHub != null) {
                Hub hubNode = tndUse.getHub();
                if (hubNode != null) {
                    if (tndUse.node.def.updateHub != hubNode && tndUse.node.def.updateHub.getSharedHub() != hubNode) {
                        if (hubNode.getSharedHub() != tndUse.node.def.updateHub) {
                            // 20140421,20170823 dont change if hub is from a type=ONE.  OAObject.setAO will handle it.
                            OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(tndUse.node.def.updateHub);
                            OALinkInfo liRev;
                            boolean b = true;
                            
                            if (li != null) {
                                liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
                            }
                            else {
                                liRev = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(tndUse.node.def.updateHub);
                            }
                            if (liRev != null && liRev.getType() == li.ONE) {
                                b = false;
                            }

                            if (b) {
                                tndUse.node.def.updateHub.setSharedHub(hubNode, false);
                            }
                            // was:
                            //tndUse.node.def.updateHub.setSharedHub(hubNode, false);
                        }
                    }
                }
                
                if (bLastNode || tndUse.node.def.updateHub.getActiveObject() != tndUse.object) {
                    // 20120228 if selected treeNodeTitle, then tnd.object will be null - set AO=null
                    if (bLastNode) {
                        if (tnd.node instanceof OATreeTitleNode) HubAODelegate.setActiveObjectForce(tndUse.node.def.updateHub, null); 
                        else HubAODelegate.setActiveObjectForce(tndUse.node.def.updateHub, tndUse.object);
                    }
                    else {
                        // update this node, only if none of the other nodes are using the same updateHub
                        for (int ix = i+1; ; ix++) {
                            if (ix  == lastSelection.length) {
                                tndUse.node.def.updateHub.setActiveObject(tndUse.object);
                                break;
                            }
                            OATreeNodeData tndx = (OATreeNodeData) lastSelection[ix];
                            if (tndx.node.def.updateHub == tndUse.node.def.updateHub) {
                                break;
                            }
                        }
                    }
                }
                hsUpdateHub.add(tndUse.node.def.updateHub);
            }
            if (tndUse.node.hub != null) {
                if (bLastNode || tndUse.node.hub.getActiveObject() != tnd.object) {
                    // 20120228 if selected treeNodeTitle, then tnd.object will be null - set AO=null
                    if (bLastNode) {
                        HubAODelegate.setActiveObjectForce(tndUse.node.hub, tnd.object);
                    }
                    else {
                        tndUse.node.hub.setActiveObject(tnd.object);
                    }
                    hsUpdateHub.add(tndUse.node.def.updateHub);
                }
            }

            if (hubAdditonalUpdate != null) {
                HubAODelegate.setActiveObjectForce(hubAdditonalUpdate, null);
            }
        }

        
        // need to unselect Hub.ActiveObject for all nodes under the selected node
        OATreeNodeData tnd = (OATreeNodeData) lastSelection[lastSelection.length-1];

        setChildSharedHub(tnd, hsUpdateHub);
        clearChildNodeAO(tnd.node, null, hsUpdateHub);
    }

    // 20120228 update child nodes - this will allow a titleNode to be selected and have it's child node hubs updated
    protected void setChildSharedHub(OATreeNodeData tnd, HashSet<Hub> hsHub) {
        if (tnd == null || !tnd.getAreChildrenLoaded()) return;
        int x = tnd.getChildCount();
        for (int ii=0; ii<x; ii++) {
            OATreeNodeData tndx = tnd.getChild(ii);
            Hub h = tndx.getHub();
            if (tndx != null && h != null) {
                if (tndx.node.def.updateHub != null) {
                    Hub hx = tndx.node.def.updateHub;
                    if (!hsHub.contains(hx)) {
                        hsHub.add(hx);
                        
                        // 20140421,20170823 dont change if hub is from a type=ONE.  OAObject.setAO will handle it.
                        OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(hx);
                        OALinkInfo liRev;
                        boolean b = true;
                        
                        if (li != null) {
                            liRev = OAObjectInfoDelegate.getReverseLinkInfo(li);
                        }
                        else {
                            liRev = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(hx);
                        }
                        if (liRev != null && liRev.getType() == li.ONE) {
                            b = false;
                        }

                        if (b && hx != h && hx.getSharedHub() != h) {
                            hx.setSharedHub(h, false);
                        }
                    }
                }
            }
            setChildSharedHub(tndx, hsHub);
        }
    }
    
    protected void clearChildNodeAO(OATreeNode tn, HashSet<OATreeNode> hsTreeNode, HashSet<Hub> hsHub) {
		if (hsTreeNode == null) hsTreeNode = new HashSet<OATreeNode>(7);
  		hsTreeNode.add(tn);

		Hub h = tn.def.updateHub;
		if (h == null) h = tn.hub;
  		
    	for (int i=0; tn.def.treeNodeChildren != null && i < tn.def.treeNodeChildren.length; i++) {
    		if (hsTreeNode.contains(tn.def.treeNodeChildren[i])) continue;

    		h = tn.def.treeNodeChildren[i].def.updateHub;
    		if (h == null) h = tn.def.treeNodeChildren[i].hub;

    		if (h != null && h.getAO() != null && !hsHub.contains(h)) {
    			hsHub.add(h);
    			
    			// 20111008 dont set to null if it is a Hub used by a LinkOne detail
    			OALinkInfo li = HubDetailDelegate.getLinkInfoFromDetailToMaster(h);
    			if (li != null) {
    			    li = OAObjectInfoDelegate.getReverseLinkInfo(li);
    			}
                if (li == null || li.getType() == OALinkInfo.MANY) {
                    h.setActiveObject(null);
                }
    		}

    		if (!hsTreeNode.contains(tn.def.treeNodeChildren[i])) {
	    		clearChildNodeAO(tn.def.treeNodeChildren[i], hsTreeNode, hsHub);
    		}
    	}
    }

    public void setPreferredSize(int rows, int cols) {
        this.rows = rows;
        this.columns = cols;

        if (this.rows > 0) setVisibleRowCount(this.rows);

        if (this.columns > 0) {
            Dimension d = super.getPreferredSize();
        	this.widthColumn = OAJfcUtil.getCharWidth(cols);
        }
    	invalidate();
    }
    
    public void setMinimumSize(int rows, int cols) {
        this.miniRows = rows;
        this.miniColumns = cols;

        if (this.miniColumns > 0) {
            Dimension d = super.getMinimumSize();
            this.miniWidthColumn = OAJfcUtil.getCharWidth(cols);
        }
        invalidate();
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (miniWidthColumn > 0) {
            d.width = miniWidthColumn;
        }
        if (d.height == 0) {
            d.height = 10;
        }
        return d;
    }
    
    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        return d;
    }

    private Dimension dimLastPerferredSize;
    public Dimension getPreferredSize() {
        Dimension d = null; 
    
        try {
            d = super.getPreferredSize();
        }
        catch (Exception e) {
            d = null;
        }
        if (d == null) {
            if (dimLastPerferredSize == null) dimLastPerferredSize = new Dimension(20,20);
            d = dimLastPerferredSize;
        }
        else {
            if (widthColumn > 0) {
                d.width = widthColumn;
            }
            if (d.height == 0) {
                d.height = 10;
            }
            dimLastPerferredSize = d;
        }
    	return d;
    }

    @Override
    public void paint(Graphics g) {
        try {
            super.paint(g);
        }
        catch (Exception e) {
            
        }
    }
    
    /**
        Preferred width of tree, based on average width of the font's character.
    */
    public void setColumns(int cols) {
    	setPreferredSize(this.rows, cols);
    }
    /**
        Preferred width of tree, based on average width of the font's character.
    */
    public int getColumns() {
        return columns;
    }

    public void setRows(int rows) {
		setPreferredSize(rows, this.columns);
	}
	public int getRows() {
	    return rows;
	}
    
    /**
        Refreshes tree, collapsing all nodes.
    */
    public void refresh() {
        model.fireTreeStructureChanged();
    }

    private int cntUpdateUI;
    public @Override void updateUI() {
    	super.updateUI();
    	if (root != null) root.updateUICalled();
    	cntUpdateUI++;
    }
    
    // 20180303 hack for JTree.setExpandedState
    //   so that it does not unselect descendents and select this node
    private boolean bIgnoreCollapse;
    @Override
    public void fireTreeCollapsed(TreePath path) {
        bIgnoreCollapse = true;
        super.fireTreeCollapsed(path);
    }
    @Override
    protected boolean removeDescendantSelectedPaths(TreePath path, boolean includePath) {
        if (bIgnoreCollapse) {
            bIgnoreCollapse = false;
            return false;
        }
        return super.removeDescendantSelectedPaths(path, includePath);
    }
    
    // 20180601
    public void preload() {
        try {
            preload(root, null, 0, "");
        }
        catch (Exception e) {
            System.out.println("preload exception: "+e);
            e.printStackTrace();
        }
    }
    protected void preload(final OATreeNode treeNode, final Hub hub, final int amt, final String ppPrefix) {
        if (amt > 5) return;
        if (treeNode == null) return;
        if (treeNode.getChildrenTreeNodes() == null) return;
        
        for (OATreeNode tn : treeNode.getChildrenTreeNodes()) {
            if (tn == treeNode) continue; 
            if (hub != null) {
                String pp = tn.fullPath;
                if (OAString.isNotEmpty(pp) && pp.indexOf('.') > 0) {
                    pp = OAString.field(pp, '.', 1);
                    try {
                        OAFinder finder = new OAFinder(hub, ppPrefix+pp);
                        finder.find();
                        preload(tn, hub, amt+1, ppPrefix+pp+".");
                    }
                    catch (Exception e) {
                    }
                }
            }
            else {
                preload(tn, tn.hub, amt+1, ppPrefix);
            }
        }
    }
    
}
