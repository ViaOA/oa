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

import java.awt.*;
import java.awt.event.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.util.Vector;
import java.beans.*;
import java.io.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.dnd.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;

/**
    Used for binding a List component to a property in a Hub.  An icon property can also be
    set to display an image for each row.
    <p>
    OAList has options to allow for key bindings that will insert/remove/delete/move rows (default=disabled).
    <p>
    A List can be created as a multi selection list, by supplying a second Hub that is used
    to contain the selected objects.
    <p>
    Full support for Drag and Drop (DND), and options to control how it works.  Default is to 
    have full support turned on.
    <p>
    Example:<br>
    Create a OAList that will display a list (Hub) of Employees full name, 8 rows are visible with a
    column width of 28 characters.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OAList lst = new OAList(hubEmployee, "fullName", 8, 28);

    // create a multi-select List by using a second Hub that will 
    //   contain the objects that are selected 
    Hub hubSelectEmployee = new Hub(Employee.class);
    lst.setSelectionHub(hubSelectEmployee);
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OAScrollList.OAScrollList
    @see Hub2List
*/
public class OAList extends JList implements OAJFCComponent, OATableComponent, DragGestureListener, DropTargetListener {
    protected OATable table;
    protected Hub2List hubList;
    protected int columns = 16;

    // 10/16/99 Drag&Drop  
    boolean bAllowDrag = false;
    boolean bAllowDrop = false;
    boolean bRemoveDragObject;
    DropTarget dropTarget;
    DragSource dragSource = DragSource.getDefaultDragSource();
    final static DragSourceListener dragSourceListener = new MyDragSourceListener();
    
    
    /**
        Create an unbound list.
    */
    public OAList() {
        this(null, null, 10, 5);
    }
    
    /**
        Create List that is bound to a Hub.
    */
    public OAList(Hub hub) {
        this(hub, null, 10, 5);
    }

    /**
        Create List that is bound to a property for the active object in a Hub.
    */
    public OAList(Hub hub, String propertyPath) {
        this(hub,propertyPath, 10,5);
    }

    /**
        Create List that is bound to a Hub.
        @param visibleRowCount number of rows to visually display.
        @param cols is width of list using character width size.
    */
    public OAList(Hub hub, String propertyPath, int visibleRowCount, int cols) {
        if (hub == null) hubList = new Hub2List(this);
        else hubList = new Hub2List(hub, this, propertyPath, visibleRowCount);
        
        if (cols > 0) setColumns(cols);
        else setPrototypeCellValue("0123456789ABCD");

        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,this);
        dropTarget = new DropTarget(this,this);
    }

    boolean bRemoved;     
    public void addNotify() {
        super.addNotify();
        if (bRemoved) hubList.setHub(hubList.getHub());
    }
    /* 2005/02/07 need to manually call close instead
    public void removeNotify() {
        super.removeNotify();
        bRemoved = true;
        hubList.close();
    }
    */
    public void close() {
        bRemoved = true;
        hubList.close();
    }
    

    /** 
        Called by Hub2List when item is selected 
    */
    public void valueChanged() {
    }

    /** 
    The "word(s)" to use for the empty row (null value).  Default=""  <br>
    Example: "none of the above".  
    <p>
    Note: Set to null if none should be used
	*/
	public String getNullDescription() {
	    return hubList.getNullDescription();
	}
	/** 
	    The "word(s)" to use for the empty row (null value).  Default=""  <br>
	    Example: "none of the above".  
	    <p>
	    Note: Set to null if none should be used
	*/
	public void setNullDescription(String s) {
	    hubList.setNullDescription(s);
	}
    
    
    /**
        Flag to know if a row/object can be removed from the Hub by using the [Delete] key.
    */
    public void setAllowRemove(boolean b) {
        hubList.setAllowRemove(b);
    }
    /**
        Flag to know if a row/object can be removed from the Hub by using the [Delete] key.
    */
    public boolean getAllowRemove() {
        return hubList.getAllowRemove();
    }
    
    /**
        Flag to know if a row/object can be deleted by using the [Delete] key.
    */
    public void setAllowDelete(boolean b) {
        hubList.setAllowDelete(b);
    }
    /**
        Flag to know if a row/object can be deleted by using the [Delete] key.
    */
    public boolean getAllowDelete() {
        return hubList.getAllowDelete();
    }

    /**
        Flag to know if a new row/object can be inserted by using the [Insert] key.
    */
    public void setAllowInsert(boolean b) {
        hubList.setAllowInsert(b);
    }
    /**
        Flag to know if a new row/object can be inserted by using the [Insert] key.
    */
    public boolean getAllowInsert() {
        return hubList.getAllowInsert();
    }

    /**
        Get the property name used for displaying an image with component.
    */
    public void setImageProperty(String prop) {
        hubList.setImageProperty(prop);
    }
    /**
        Get the property name used for displaying an image with component.
    */
    public String getImageProperty() {
        return hubList.getImageProperty();
    }

    public int getMaxImageHeight() {
    	return hubList.getMaxImageHeight();
	}
	public void setMaxImageHeight(int maxImageHeight) {
		hubList.setMaxImageHeight(maxImageHeight);
	}

	public int getMaxImageWidth() {
		return hubList.getMaxImageWidth();
	}
	public void setMaxImageWidth(int maxImageWidth) {
		hubList.setMaxImageWidth(maxImageWidth);
	}
    
    /**
        Root directory path where images are stored.
    */
    public void setImagePath(String path) {
        hubList.setImagePath(path);
    }
    /**
        Root directory path where images are stored.
    */
    public String getImagePath() {
        return hubList.getImagePath();
    }

    /**
        Get the image to display for the current object.
    */
    public Icon getIcon() {
        return hubList.getIcon();
    }
    /**
        Get the image to display for an object.
    */
    public Icon getIcon(Object obj) {
        return hubList.getIcon(obj);
    }
    
    
    
    // ----- OATableComponent Interface methods -----------------------
    // some of these are only included so that the ProperyPathCustomerEditor can be used
    
    
    /**
        Hub this this component is bound to.
    */
    public void setHub(Hub h) {
        hubList.setHub(h);
    }
    /**
        Hub that this component is bound to.
    */
    public Hub getHub() {
        return hubList.getHub();
    }


    /**
        Set by OATable when this component is used as a column.  
        Ignored, OAList can not be used as a column in an OATable.
    */
    public void setTable(OATable table) {
    }
    /**
        Set by OATable when this component is used as a column.  
        Ignored, OAList can not be used as a column in an OATable.
    */
    public OATable getTable() {
        return null;
    }

    /**
        Width of component, based on average width of the font's character.
    */
    public int getColumns() {
        return columns;
    }
    /** was: 20080515
        Width of component, based on average width of the font's character.
    / 
    public void setColumns(int cols) {
        columns = cols;
        int w = OATable.getCharWidth(this,getFont(),cols);
        setFixedCellWidth(w);
    }
    */
    
    /**
	    Width of ComboBox, based on average width of the font's character.
	*/
	public void setColumns(int x) {
	    columns = x;
		String str = null;
		for (int i=0; i<x; i++) {
			if (str == null) str = "X";
			else str += "X"; 
		}
	    super.setPrototypeCellValue(str);
	}

    
    
    /**
        Returns the amount of rows that are visible.
        Calls super.getVisibleRowCount()
    */
    public int getVisibleRows() {
        return super.getVisibleRowCount();
    }
    /**
        Sets the amount of rows that are visible.
        Calls super.setVisibleRowCount()
    */
    public void setVisibleRows(int rows) {
        super.setVisibleRowCount(rows);
    }

    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getPropertyPath() {
        return hubList.getPropertyPath();
    }
    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path) {
        hubList.setPropertyPath(path);
    }


    /** 
        Seperate Hub that can contain selected objects. 
        This will allow for a multiselect list.
    */
    public void setSelectionHub(Hub hub) {
        hubList.setSelectionHub(hub);
    }

    /** 
        Popup message that will be used to confirm an insert/remove/delete/new.
    */
    public void setConfirmMessage(String msg) {
        hubList.setConfirmMessage(msg);
    }
    /** 
        Popup message that will be used to confirm an insert/remove/delete/new.
    */
    public String getConfirmMessage() {
        return hubList.getConfirmMessage();
    }
    
    
    /**
        Column heading when this component is used as a column in an OATable.
        Ignored, OAList can not be used as a column in an OATable.
    */
    public String getTableHeading() { 
        return "";
    }
    /**
        Column heading when this component is used as a column in an OATable.
        Ignored, OAList can not be used as a column in an OATable.
    */
    public void setTableHeading(String heading) {
    }

    /**
        Editor used when this component is used as a column in an OATable.
        Ignored, OAList can not be used as a column in an OATable.
    */
    public TableCellEditor getTableCellEditor() {
        return null;
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
    /** Flag to allow drag & drop. default=true */
    public void setAllowDnD(boolean b) {
        setAllowDrop(b);
        setAllowDrag(b);
    }

    
    /** Flag to allow drop. default=true */
    public boolean getAllowDrop() {
        return bAllowDrop;
    }
    /** Flag to allow drop. default=true */
    public void setAllowDrop(boolean b) {
        bAllowDrop = b;
    }

    /** Flag to allow drag. default=true */
    public boolean getAllowDrag() {
        return bAllowDrag;
    }
    /** Flag to allow drag. default=true */
    public void setAllowDrag(boolean b) {
        bAllowDrag = b;
    }

    /** 
        Flag to allow know if a dragged object should be removed from list.
    */
    public void setRemoveDragObject(boolean b) {
        bRemoveDragObject = b;
    }
    /** 
        Flag to allow know if a dragged object should be removed from list.
    */
    public boolean getRemoveDragObject() {
        return bRemoveDragObject;
    }

    /** Used to support drag and drop (DND). */
    public void dragGestureRecognized(DragGestureEvent e) {
        if (!getAllowDrag()) return;
        Hub h = hubList.getHub();
        if (h != null) {
            Object obj = h.getActiveObject();
            if (obj != null) {
                OATransferable t = new OATransferable(h,obj);
                dragSource.startDrag(e, null, t, dragSourceListener);
            }
        }
    }

    /** Used to support drag and drop (DND). */
    public void dragEnter(DropTargetDragEvent e) {
        if (getAllowDrop() && e.isDataFlavorSupported(OATransferable.HUB_FLAVOR)) {
            e.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
        }
        else e.rejectDrag();
    }

    private long timeDragOver;
    /** Used to support drag and drop (DND). */
    public void dragOver(DropTargetDragEvent e) { 
        Point pt = e.getLocation();
        Container cont = this.getParent();
        Point p = this.getLocation();
        pt.translate(p.x, p.y);
        for (;cont != null; ) {
            if (cont instanceof JPanel) break;//qqqqqqqqqqq 20101030
            if (cont instanceof JScrollPane) {
                long ltime = (new java.util.Date()).getTime();
                if (ltime < (timeDragOver + 200)) break;
                JScrollPane sp = (JScrollPane) cont;
                JViewport viewport = sp.getViewport();

                Point ptViewPosition = (Point) viewport.getViewPosition().clone(); 
                Component comp = viewport.getView();
                

                // if (qqq == 0) System.out.println("viewportSize="+viewport.getSize()+"   ScrollPaneSize="+sp.getSize()+"  CompSize="+comp.getSize());
                // System.out.println((qqq++)+") "+ptViewPosition);
                boolean b = false;
                if (pt.y < 20) {
                    if (ptViewPosition.y != 0) {
                        ptViewPosition.y -= 20;
                        if (ptViewPosition.y < 0) ptViewPosition.y = 0;
                        b = true;;
                    }
                }
                else {
                    Dimension dimViewPort = viewport.getExtentSize();
                    
                    if (pt.y > dimViewPort.height - 20) {
                        Dimension dimComp = comp.getSize();
                        if ((ptViewPosition.y + dimViewPort.height) != dimComp.height) {
                            ptViewPosition.y += 20;
                            if ((ptViewPosition.y + dimViewPort.height) > dimComp.height) ptViewPosition.y = (dimComp.height - dimViewPort.height);
                            b = true;
                        }
                    }                    
                }

                if (pt.x < 20) {
                    if (ptViewPosition.x != 0) {
                        ptViewPosition.x -= 20;
                        if (ptViewPosition.x < 0) ptViewPosition.x = 0;
                        b = true;;
                    }
                }
                else {
                    Dimension dimViewPort = viewport.getExtentSize();
                    
                    if (pt.x > dimViewPort.width - 20) {
                        Dimension dimComp = comp.getSize();
                        if ((ptViewPosition.x + dimViewPort.width) != dimComp.width) {
                            ptViewPosition.x += 20;
                            if ((ptViewPosition.x + dimViewPort.width) > dimComp.height) ptViewPosition.x = (dimComp.width - dimViewPort.width);
                            b = true;
                        }
                    }                    
                }

                if (b) {
                    scrollDrag(viewport, ptViewPosition);
                    timeDragOver = ltime;
                }
                break;
            }
            p = cont.getLocation();
            pt.translate(p.x, p.y);
            cont = cont.getParent();
        }
    }
    
    /** Used to support drag and drop (DND). */
    protected void scrollDrag(final JViewport viewport, final Point ptViewPosition) {
        viewport.setViewPosition(ptViewPosition);
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
            if (!e.getTransferable().isDataFlavorSupported(OATransferable.HUB_FLAVOR)) return;
            if (!getAllowDrop()) return;

            // get object to move/copy
            Hub dragHub = (Hub) e.getTransferable().getTransferData(OATransferable.HUB_FLAVOR);
            Object dragObject = (Object) e.getTransferable().getTransferData(OATransferable.OAOBJECT_FLAVOR);
 
            Point pt = e.getLocation();
            
            int row = locationToIndex(pt);
            
            
            if (row < 0) row = getModel().getSize();

            Rectangle rect = getCellBounds(row,row);
            if (rect != null && pt.y > (rect.y + (rect.height/2))) row++;

            Hub newHub = getDropHub();
            if (newHub == null) return;
            Object toObject = newHub.elementAt(row);

            if ( newHub.getObjectClass().isAssignableFrom(dragObject.getClass()) ) {
                if (dragObject != toObject) {
                    
                	int pos = HubDataDelegate.getPos(newHub, dragObject, false, false);
                    if (pos >= 0) {
                        // move
                        if (!newHub.isSorted()) {
                            if (pos < row) row--;
                            newHub.move(pos, row);
                            // 20091214
                            OAUndoManager.add(OAUndoableEdit.createUndoableMove(null, newHub, pos, row));
                            hubList.afterChangeActiveObject(null);
                        }
                    }
                    else {
                        if (newHub.isSorted()) {
                            newHub.add(dragObject);
                            // 20091214
                            OAUndoManager.add(OAUndoableEdit.createUndoableAdd(null, newHub, dragObject));
                        }
                        else {
                            newHub.insert(dragObject, row);
                            // 20091214
                            OAUndoManager.add(OAUndoableEdit.createUndoableInsert(null, newHub, dragObject, row));
                        }
                        
                        if (getRemoveDragObject()) {
                            dragHub.remove(dragObject);   
                        }
                    }
                    newHub.setActiveObject(dragObject);
                }
            }
            e.dropComplete(true);
        }
        catch (UnsupportedFlavorException ex) {
        }
        catch (IOException ex) {
        }
    }
    /** 
     * This is used to work with MergedHubs, where the List hub is different then the source hub.
     * Default is to use this.getHub() 
     * 20080730
     */
    protected Hub getDropHub() {
    	return hubList.getHub();
    }
    //END Drag&Drop

    /** 
        Renderer used to display each row.  Can be overwritten to create custom rendering.
        Called by Hub2List.MyListCellRenderer.getListCellRendererComponent to get renderer. 
    */
    public Component getRenderer(Component renderer, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if (hubList == null) return renderer;
        return hubList.getRenderer(renderer, list, value, index, isSelected, cellHasFocus);
    }

    /** 
        Used to supply the renderer when this component is used in the column of an OATable.
        Can be overwritten to customize the rendering.
        <p>
        Note: Not used, OAList can not be used as a column for an OATable.    
    */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderer;
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (hubList != null) {
            hubList.setEnabled(b);
        }
    }
    public void setReadOnly(boolean b) {
        hubList.setReadOnly(b);
    }
    public boolean getReadOnly() {
        return hubList.getReadOnly();
    }

    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }
    
    
    public void setIconColorProperty(String s) {
    	hubList.setIconColorProperty(s);
    }
    public String getIconColorProperty() {
    	return hubList.getIconColorProperty();
    }
    public void setBackgroundColorProperty(String s) {
    	hubList.setBackgroundColorProperty(s);
    }
    public String getBackgroundColorProperty() {
    	return hubList.getBackgroundColorProperty();
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
	protected void processMouseEvent(MouseEvent e) {
	    super.processMouseEvent(e);
	    if (e.getID() == MouseEvent.MOUSE_PRESSED) {
	        if (e.getClickCount() == 2) {
	            if (cmdDoubleClick != null) cmdDoubleClick.doClick();
	            onDoubleClick();
	        }
	    }
	}

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (e.getID() == MouseEvent.MOUSE_MOVED) {
            int row = locationToIndex(e.getPoint());
            onMouseOver(row, e);
        }
        super.processMouseMotionEvent(e);
    }
	
	
	protected void onMouseOver(int row, MouseEvent e) {
	    
	}
	
	/**
	    Method that is called whenever mouse click count = 2.
	    Note: the activeObject of the clicked row will be the active object in the OATables Hub.
	*/
	public void onDoubleClick() {
	
	}

	@Override
	public String getFormat() {
		return hubList.getFormat();
	}	
}

