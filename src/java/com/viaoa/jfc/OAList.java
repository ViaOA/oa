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
import java.io.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.dnd.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;


public class OAList extends JList implements OATableComponent, DragGestureListener, DropTargetListener, OAJFCComponent {
    protected OATable table;
    protected OAListController control;
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
        if (hub == null) control = new OAListController();
        else control = new OAListController(hub, propertyPath, visibleRowCount);
        
        if (cols > 0) setColumns(cols);
        else setPrototypeCellValue("0123456789ABCD");

        dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE,this);
        dropTarget = new DropTarget(this,this);
        
        setBorder(new EmptyBorder(2,2,2,2));
    }

    
    @Override
    public JFCController getController() {
        return control;
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (d == null || d.width == 0) {
            d = new Dimension(
                    OATable.getCharWidth(this, getFont(), columns), 
                    OATable.getCharHeight(this, getFont()) * getVisibleRows() 
            );
        }
        return d;
    }
    
    boolean bRemoved;     
    public void addNotify() {
        super.addNotify();
        if (bRemoved) control.setHub(control.getHub());
        
        // 20120116 need to set divider if in a splitpane, since JList.getPreferredSize returns 0,0 if no rows in model
        Dimension d = this.getPreferredSize();
        if (d == null) return;
        
        for (Container c = this.getParent(); c!=null ; c=c.getParent()) {
            if (d == null || !(c instanceof JSplitPane)) continue;
            JSplitPane split = (JSplitPane) c;

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
    
    /* 2005/02/07 need to manually call close instead
    public void removeNotify() {
        super.removeNotify();
        bRemoved = true;
        control.close();
    }
    */
    public void close() {
        bRemoved = true;
        control.close();
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
	    return control.getNullDescription();
	}
	/** 
	    The "word(s)" to use for the empty row (null value).  Default=""  <br>
	    Example: "none of the above".  
	    <p>
	    Note: Set to null if none should be used
	*/
	public void setNullDescription(String s) {
	    control.setNullDescription(s);
	}
    
    
    /**
        Flag to know if a row/object can be removed from the Hub by using the [Delete] key.
    */
    public void setAllowRemove(boolean b) {
        control.setAllowRemove(b);
    }
    /**
        Flag to know if a row/object can be removed from the Hub by using the [Delete] key.
    */
    public boolean getAllowRemove() {
        return control.getAllowRemove();
    }
    
    /**
        Flag to know if a row/object can be deleted by using the [Delete] key.
    */
    public void setAllowDelete(boolean b) {
        control.setAllowDelete(b);
    }
    /**
        Flag to know if a row/object can be deleted by using the [Delete] key.
    */
    public boolean getAllowDelete() {
        return control.getAllowDelete();
    }

    /**
        Flag to know if a new row/object can be inserted by using the [Insert] key.
    */
    public void setAllowInsert(boolean b) {
        control.setAllowInsert(b);
    }
    /**
        Flag to know if a new row/object can be inserted by using the [Insert] key.
    */
    public boolean getAllowInsert() {
        return control.getAllowInsert();
    }

    /**
        Get the property name used for displaying an image with component.
    */
    public void setImageProperty(String prop) {
        control.setImageProperty(prop);
    }
    /**
        Get the property name used for displaying an image with component.
    */
    public String getImageProperty() {
        return control.getImageProperty();
    }

    public int getMaxImageHeight() {
    	return control.getMaxImageHeight();
	}
	public void setMaxImageHeight(int maxImageHeight) {
		control.setMaxImageHeight(maxImageHeight);
	}

	public int getMaxImageWidth() {
		return control.getMaxImageWidth();
	}
	public void setMaxImageWidth(int maxImageWidth) {
		control.setMaxImageWidth(maxImageWidth);
	}
    
    /**
        Root directory path where images are stored.
    */
    public void setImagePath(String path) {
        control.setImagePath(path);
    }
    /**
        Root directory path where images are stored.
    */
    public String getImagePath() {
        return control.getImagePath();
    }

    /**
        Get the image to display for the current object.
    */
    public Icon getIcon() {
        return control.getIcon();
    }
    /**
        Get the image to display for an object.
    */
    public Icon getIcon(Object obj) {
        return control.getIcon(obj);
    }
    
    
    
    // ----- OATableComponent Interface methods -----------------------
    // some of these are only included so that the ProperyPathCustomerEditor can be used
    
    
    /**
        Hub this this component is bound to.
    */
    public void setHub(Hub h) {
        control.setHub(h);
    }
    /**
        Hub that this component is bound to.
    */
    public Hub getHub() {
        if (control == null) return null;
        return control.getHub();
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
        return control.getPropertyPath();
    }
    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
    }


    /** 
        Seperate Hub that can contain selected objects. 
        This will allow for a multiselect list.
    */
    public void setSelectionHub(Hub hub) {
        control.setSelectionHub(hub);
    }

    /** 
        Popup message that will be used to confirm an insert/remove/delete/new.
    */
    public void setConfirmMessage(String msg) {
        control.setConfirmMessage(msg);
    }
    /** 
        Popup message that will be used to confirm an insert/remove/delete/new.
    */
    public String getConfirmMessage() {
        return control.getConfirmMessage();
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
        Hub h = control.getHub();
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
                            control.afterChangeActiveObject(null);
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
    	return control.getHub();
    }
    //END Drag&Drop

    /** 
        Renderer used to display each row.  Can be overwritten to create custom rendering.
        Called by Hub2List.MyListCellRenderer.getListCellRendererComponent to get renderer. 
    */
    public Component getRenderer(Component renderer, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return renderer;
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

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    }

    public void setIconColorProperty(String s) {
    	control.setIconColorProperty(s);
    }
    public String getIconColorProperty() {
    	return control.getIconColorProperty();
    }
    public void setBackgroundColorProperty(String s) {
    	control.setBackgroundColorProperty(s);
    }
    public String getBackgroundColorProperty() {
    	return control.getBackgroundColorProperty();
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
		return control.getFormat();
	}	

    /**
     * Other Hub/Property used to determine if component is enabled.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setEnabled(Hub hub) {
        control.getEnabledController().add(hub);
    }
    public void setEnabled(Hub hub, String prop) {
        control.getEnabledController().add(hub, prop);
    }
    public void setEnabled(Hub hub, String prop, Object compareValue) {
        control.getEnabledController().add(hub, prop, compareValue);
    }
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }
    
    /** removed, to "not use" the enabledController, need to call it directly - since it has 2 params now, and will need 
     * to be turned on and off   
    @Override
    public void setEnabled(boolean b) {
        if (control != null) {
            b = control.getEnabledController().directSetEnabledCalled(b);
        }
        super.setEnabled(b);
    }
    */
    /**
     * Other Hub/Property used to determine if component is visible.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setVisible(Hub hub) {
        control.getVisibleController().add(hub);
    }    
    public void setVisible(Hub hub, String prop) {
        control.getVisibleController().add(hub, prop);
    }    
    public void setVisible(Hub hub, String prop, Object compareValue) {
        control.getVisibleController().add(hub, prop, compareValue);
    }    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }

    
    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    public String isValid(Object object, Object value) {
        return null;
    }


    public String getToolTipText(Object object, String defalutValue) {
        return defalutValue;
    }
    
    
    class OAListController extends ListController {
        public OAListController() {
            super(OAList.this);
        }    
        public OAListController(Hub hub, String propertyPath, int visibleRow) {
            super(hub, OAList.this, propertyPath, visibleRow);
        }
        
        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OAList.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OAList.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OAList.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
        
        @Override
        public synchronized void valueChanged(ListSelectionEvent e) {
            super.valueChanged(e);
            OAList.this.valueChanged();
        }
        @Override
        public Component getRenderer(Component renderer, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            renderer = super.getRenderer(renderer, list, value, index, isSelected, cellHasFocus);
            String tt = getToolTipText(value);
            tt = OAList.this.getToolTipText(value, tt);
            ((JComponent)renderer).setToolTipText(tt);
            return OAList.this.getRenderer(renderer, list, value, index, isSelected, cellHasFocus);
        }
    
    }


}

