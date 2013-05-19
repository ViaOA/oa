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
//qqqqqq KeySelectionManager

package com.viaoa.jfc;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;
import com.viaoa.jfc.undo.OAUndoableEdit;


/** 
    JComboBox subclass that binds to a Hub.  Display property value and image
    can be set for display each object in a dropdown.
    A blank row is used to be used as a null value.  This null row can be customized
    to display text or even to be removed.
    <p>
    An OATextField can be used to allow editing of the property value.
    <p>
    OAComboBox can used as a column in an OATable, where custom renderer and editor are
    automatically supplied.
    <p>
    Renderer can be customized/overwritter.
    <p>
    Example:<br>
    <pre>
        Hub hubDepartment = new Hub(Department.class);
        hubDepartment.select();

        Hub hubEmployee = new Hub(Employee.class);
        hubEmployee.select();
        
        hubDepartment.setLink(hubEmployee); // link Dept with Emps (see Hub.setLink())

        OAComboBox cbo = new OAComboBox(hubDepartment, "name", 15);
        cbo.setNullDescription("Select a Department");
        cbo.setMaximumRowCount(15);
        // for using as a column in OATable
        cbo.setTableHeading("Department");
        cbo.setColumns(18);  // column width based on size of average character
        cbo.setMaxImageHeight(20);
        cbo.setImageProperty("gifFileName");
        cbo.setImagePath("images");
    </pre>
    @see Hub2ComboBox
    @see com.viaoa.hub.Hub#setLink
*/
public class OAComboBox_ extends JComboBox implements OAJFCComponent, OATableComponent {
    private Hub2ComboBox hcb;
    private OATextField vtf;
    private int columns;
    private int width;
    private OATable table;
    private String heading = "";
    public int iDebug;  // used to help with debugging a specific ComboBox
    /**
        Create an unbound ComboBox.
    */
    public OAComboBox() {
        hcb = new Hub2ComboBox(this);
    }

    /**
        Create ComboBox that is bound to a Hub.
    */
    public OAComboBox(Hub hub) {
        this(hub, "");
    }

    /**
        Create ComboBox that is bound to a property for the active object in a Hub.
        @param columns is width of list using character width size.
    */
    public OAComboBox(Hub hub, String propertyPath, int columns) {
        this(hub,propertyPath);
        setColumns(columns);
    }
    /**
        Create ComboBox that is bound to a property for the active object in a Hub.
    */
    public OAComboBox(Hub hub, String propertyPath) {
        hcb = new Hub2ComboBox(hub,this,propertyPath);
    }

    
    /**
        Flag to enable undo, default is true.
    */
    public void setEnableUndo(boolean b) {
        hcb.setEnableUndo(b);
    }
    public boolean getEnableUndo() {
        return hcb.getEnableUndo();
    }
    
    
    public void setUndoDescription(String s) {
        hcb.setUndoDescription(s);
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return hcb.getUndoDescription();
    }
    
    
    /**
        Create ComboBox that is bound to a property for an object.
    */
    public OAComboBox(OAObject oaObject, String propertyPath) {
        hcb = new Hub2ComboBox(oaObject,this,propertyPath);
    }

    
    public Hub2ComboBox getHub2ComboBox() {
    	return hcb;
    }

    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }
    
    
    @Override
    public Dimension getSize() {
        Dimension d = super.getSize();
        if (popupColumns > 0 && !bDoLayout) {
            int w = OATable.getCharWidth(OAComboBox.this, OAComboBox.this.getFont(), popupColumns);;
            d.width = Math.max(d.width, w);
        }
        return d;
    }
    private boolean bDoLayout;
    @Override
    public void doLayout() {
        try {
            bDoLayout = true;
            super.doLayout();
        }
        finally {
            bDoLayout = false;            
        }
    }
    
    private int popupColumns;
    private PopupMenuListener listenPopupMenu;
    public void setPopupColumns(int cols) {
    	if (cols < 1) return;

    	this.popupColumns = cols;
// 20101031 replaced wih code found at http://jroller.com/santhosh/category/Swing?page=1
// now uses bDoLayout
if (cols > 0) return; //qqqqqqqqqqqqqqq

        if (listenPopupMenu == null) this.removePopupMenuListener(listenPopupMenu);
    	
    	/*  This is some "hacking" to change the width of the popup/dropdown
    	 *  By default, it will have the same width as the comboBox.
    	 *  This will "try" to find the JList that is used, change the layout, the scrollPane and the JMenuPopup size.
    	 */
    	listenPopupMenu = (new PopupMenuListener() {
    		JScrollPane getScrollPane(Container cont) {
		        Component[] comps = cont.getComponents();
		        for (int i=0; comps != null && i < comps.length; i++) {
		        	if (comps[i] instanceof JScrollPane) return (JScrollPane) comps[i];
		        	if (comps[i] instanceof Container) {
		        		JScrollPane sp = getScrollPane((Container) comps[i]);
		        		if (sp != null) return sp;
		        	}
		        }
		        return null;
    		}

    		public @Override void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    			Object obj = getUI().getAccessibleChild(OAComboBox.this, 0);
    			if (!(obj instanceof JPopupMenu)) return;
    			JPopupMenu pop = (JPopupMenu) obj;
    			if (!(pop.getLayout() instanceof BorderLayout)) {
	    			JScrollPane sp = getScrollPane(pop);
	    			sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    			if (sp == null) return;
	    			pop.setLayout(new BorderLayout());
	    			pop.add(sp, BorderLayout.CENTER);
	    			pop.pack();
    			}
		        Dimension d = new Dimension();
		        d.width = OATable.getCharWidth(OAComboBox.this, OAComboBox.this.getFont(), popupColumns);;
		        d.width = Math.max(d.width, OAComboBox.this.getWidth());

		        d.height = OATable.getCharHeight(OAComboBox.this, OAComboBox.this.getFont());
		        int rows = getModel().getSize();
		        int max = getMaximumRowCount();
		        if (max < 1) max = 30;
		        rows = Math.min(rows, max);
		        
				if (rows < 1) rows = 1;
		        d.height *= rows;
		        if (exht == 0) {
		        	exht = getExtraHeight(pop);
	    			JScrollPane sp = getScrollPane(pop);
	    			Object comp = sp.getViewport().getView();
	    			if (comp instanceof JComponent) exht += getExtraHeight((JComponent) comp);
		        }
		        d.height += exht;
		        pop.setSize(d);
		        pop.setPreferredSize(d);
		        pop.setMinimumSize(d);
    		}
    		int exht = 0;
    		int getExtraHeight(JComponent comp) {
    			int result = 0;
    			Border b = comp.getBorder();
    			if (b != null) {
    				Insets ins = b.getBorderInsets(comp);
    				if (ins != null) result += ins.top + ins.bottom;
    			}
    			return result;
    		}
    		
    		
    		@Override
    		public void popupMenuCanceled(PopupMenuEvent e) {
    		}
    		@Override
    		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    		}
    	});
    	
    	this.addPopupMenuListener(listenPopupMenu);
    }
    
    
    public int getMaxImageHeight() {
    	return hcb.getMaxImageHeight();
	}
	public void setMaxImageHeight(int maxImageHeight) {
		hcb.setMaxImageHeight(maxImageHeight);
	}

	public int getMaxImageWidth() {
		return hcb.getMaxImageWidth();
	}
	public void setMaxImageWidth(int maxImageWidth) {
		hcb.setMaxImageWidth(maxImageWidth);
	}
    
    
    /**
     	Bind this comboBox to a Hub and property.
	*/
	public void bind(Hub hub, String propertyPath) {
	    setHub(hub);
	    setPropertyPath(propertyPath);
	}
    
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        hcb.setFormat(fmt);
    }
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        return hcb.getFormat();
    }

    /**
        Property used to get file name of image icon.
    */
    public void setImageProperty(String prop) {
        hcb.setImageProperty(prop);
    }
    /**
        Property used to get file name of image icon.
    */
    public String getImageProperty() {
        return hcb.getImageProperty();
    }

    /**
        Root directory where images are stored.
        @see #setImageProperty
    */
    public void setImagePath(String path) {
        hcb.setImagePath(path);
    }

    /**
        Root directory where images are stored.
        @see #getImageProperty
    */
    public String getImagePath() {
        return hcb.getImagePath();
    }

    /**
        Returns the icon to use for an active object.
    */
    public Icon getIcon() {
        return hcb.getIcon();
    }
    /**
        Returns the icon to use for an object.
    */
    public Icon getIcon(Object obj) {
        return hcb.getIcon(obj);
    }

    
    public void setIconColorProperty(String s) {
    	hcb.setIconColorProperty(s);
    }
    public String getIconColorProperty() {
    	return hcb.getIconColorProperty();
    }
    
    
    /** 
        The "word(s)" to use for the empty row (null value).  Default=""  <br>
        Example: "none of the above".  
        <p>
        Note: Set to null if none should be used
    */
    public String getNullDescription() {
        return hcb.getNullDescription();
    }
    /** 
        The "word(s)" to use for the empty row (null value).  Default=""  <br>
        Example: "none of the above".  
        <p>
        Note: Set to null if none should be used
    */
    public void setNullDescription(String s) {
        hcb.setNullDescription(s);
    }


    // ----- OATableComponent Interface methods -----------------------zzzzzzz

    /** 
        Hub that this component is bound to.
    */
    public Hub getHub() {
        return hcb.getHub();
    }
    /** 
        Hub that this component is bound to.
    */
    public void setHub(Hub hub) {
        hcb.setHub(hub);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),getPropertyPath());
    }
    /**
        Set by OATable when this component is used as a column.  
    */
    public void setTable(OATable table) {
    	if (table != null) setBorder(null);
        this.table = table;
    }
    /**
        Set by OATable when this component is used as a column.  
    */
    public OATable getTable() {
        return table;
    }

    
    /**
        Overwritten to know if ComboBox was manually disabled.
    */  
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (hcb != null) {
            hcb.setEnabled(b);
        }
    }
    public void setReadOnly(boolean b) {
        hcb.setReadOnly(b);
    }
    public boolean getReadOnly() {
        return hcb.getReadOnly();
    }

    /**
        Width of component, based on average width of the font's character.
    */
    public int getColumns() {
        return columns;            
    }

    
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
        if (table != null) { 
            int w = OATable.getCharWidth(this,getFont(),x);
            Border b = this.getBorder();
            if (b != null) {
            	Insets ins = b.getBorderInsets(this);
            	if (ins != null) w += ins.left + ins.right;
            }
        	table.setColumnWidth(table.getColumnIndex(this), w);
        }
        super.setPrototypeDisplayValue(str);
    }

    
    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public String getPropertyPath() {
        return hcb.getPropertyPath();
    }
    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path) {
        hcb.setPropertyPath(path);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),path);
    }

    /**
        Column heading when this component is used as a column in an OATable.
    */
    public String getTableHeading() {
        return heading;   
    }
    /**
        Column heading when this component is used as a column in an OATable.
    */
    public void setTableHeading(String heading) {
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    /**
        Editor used when this component is used as a column in an OATable.
    */
    public void setEditor(OATextField vtf) {
        this.vtf = vtf;
        if (vtf == null) super.setEditor(null);
        else {
            OAComboBoxEditor ed = new OAComboBoxEditor(this, vtf);
            super.setEditor(ed);
            setEditable(true);
        }
    }



    OAComboBoxTableCellEditor tableCellEditor;
    /**
        Editor used when this component is used as a column in an OATable.
    */
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAComboBoxTableCellEditor(this);
        }
        return tableCellEditor;
    }



    // hack: JComboBox could be container, so set focus to first good component
    JComponent focusComp;
    /**
        Overwritten, to setup editor component.
    */
    public void requestFocus() {
        if (getEditor() != null) {
            focusComp = (JComponent) getEditor().getEditorComponent();
            if ( !(focusComp instanceof OATextField) ) focusComp = null; // dont use default editor
        }
        if (focusComp == null) {
            Component[] comps = getComponents();
            for (int i=0; i<comps.length; i++) {
                if (comps[i] instanceof JComponent) {
                    focusComp = (JComponent) comps[i];
                    if (focusComp.isRequestFocusEnabled()) break;
                    focusComp = null;
                }
            }
            if (focusComp == null) focusComp = this;
        }
        if (focusComp != this) {
            focusComp.requestFocus();
            if (focusComp instanceof OATextField) ((OATextField)focusComp).selectAll();
        }
        else super.requestFocus();
    }

    /**
        Overwritten, to add key handlers that will drop down the list.
    */
    public void processKeyEvent(KeyEvent e) {
        boolean b = true;
        if (e.getID() == KeyEvent.KEY_RELEASED) {
            if ((e.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK)) != 0 && e.getKeyCode() == KeyEvent.VK_DOWN) {
                b = false;
            }
        }
        else if (e.getID() == KeyEvent.KEY_PRESSED) {
            if ((e.getModifiers() & (KeyEvent.CTRL_MASK | KeyEvent.ALT_MASK)) != 0 && e.getKeyCode() == KeyEvent.VK_DOWN) {
                b = false;
                showPopup();
            }
            else {
            	b = processChar(e.getKeyChar());
            }
        }
        if (b) super.processKeyEvent(e);
    }

    /* 2006/12/11
     * return false if char should be ignored, or true if char should be processed.
     */
    public boolean processChar(char ch) {
    	Hub h = hcb.getHub();
    	if (h == null) return true;
    	ch = Character.toUpperCase(ch);
    	int start = h.getPos() + 1;
    	for (int i=start; ;i++) {
    		Object obj = h.elementAt(i);
    		if (obj == null) {
    			if (start == 0) break;
    			start = 0;
    			i = -1;
    			continue;
    		}
    		String s = null;
    		if (obj instanceof String) s = (String) obj;
    		else {
    			if (obj instanceof OAObject) s = ((OAObject) obj).getPropertyAsString(hcb.getPropertyPath());
    		}
    		if (s != null && s.length() > 0) {
    			if (ch == Character.toUpperCase(s.charAt(0))) {
    				h.setPos(i);
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    

    /** 
        Used to supply the renderer for the rows/objects in the list.
        Called by Hub2ComboBox.MyListCellRenderer.getListCellRendererComponent to get renderer.
    */
    public Component getRenderer(Component renderer, JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
        if (hcb == null) return renderer;
        
        renderer = hcb.getRenderer(renderer, list, value, index, isSelected, cellHasFocus);
		return renderer;
    }

    /** 
        Used to supply the renderer when this component is used in the column of an OATable.
        Can be overwritten to customize the rendering.
    */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    	Hub h = ((OATable) table).getHub();
        Hub h2 = this.getHub();

        if (!hasFocus && !isSelected) {
            renderer.setForeground( UIManager.getColor(table.getForeground()) );
            renderer.setBackground( UIManager.getColor(table.getBackground()) );
        }
        
        if (h2 != null && h != null) {
            // 2006/10/05
            String s;
            Object obj = value;
            if (h2.getLinkHub() != null) {
            	try {  // 20081010 add catch, in case the propertyPath for tableColumn is being used instead of using the link value
	            	obj = HubLinkDelegate.getPropertyValueInLinkedToHub(h2, h.elementAt(row));
	                s = OAReflect.getPropertyValueAsString(obj, hcb.getGetMethods());
            	}
            	catch (Exception e) {
                    s = OAConv.toString(value);
            	}
            }     
            else {
                s = OAConv.toString(value);
            }
            hcb.updateComponent(renderer, obj, s);
            renderer.setText(s);
            renderer.setEnabled(true);
        }

        if (hasFocus) renderer.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
        else renderer.setBorder(null);

        if (hasFocus) {
            renderer.setForeground( UIManager.getColor("Table.focusCellForeground") );
            renderer.setBackground( UIManager.getColor("Table.focusCellBackground") );
        }
        else if (isSelected) {
            renderer.setForeground( UIManager.getColor("Table.selectionForeground") );
            renderer.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        
        return renderer;
    }

}


