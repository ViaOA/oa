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


public class OAComboBox extends JComboBox implements OATableComponent, OAJFCComponent {
    private OAComboBoxController control;
    private OATextField vtf;
    private OATable table;
    private String heading = "";
    public int iDebug;  // used to help with debugging a specific ComboBox

    static {
        // UIManager.put( "ComboBox.disabledBackground", new Color(212,212,210) );
        UIManager.put( "ComboBox.disabledForeground", Color.BLACK );        
    }
    
    /**
        Create an unbound ComboBox.
    */
    public OAComboBox() {
        this(null, null);
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
        control = new OAComboBoxController(hub, propertyPath) {
            @Override
            public void onItemSelected(int row) {
                 OAComboBox.this.onItemSelected(row);
            }
        };
        Color c = UIManager.getColor("ComboBox.foreground");
        if (c == null) c = Color.black;
        //20151002 removed: UIManager.put("ComboBox.disabledForeground", c);
    }

    
    /**
        Flag to enable undo, default is true.
    */
    public void setEnableUndo(boolean b) {
        control.setEnableUndo(b);
    }
    public boolean getEnableUndo() {
        return control.getEnableUndo();
    }
    
    
    public void setUndoDescription(String s) {
        control.setUndoDescription(s);
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return control.getUndoDescription();
    }
    

    @Override
    public ComboBoxController getController() {
    	return control;
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (control != null) {
            control.getEnabledController().directlySet(true, enabled);
        }
        super.setEnabled(enabled);
    }
    
    /**
     * Other Hub/Property used to determine if component is enabled.
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
    
    
    // 20170515    
    @Override
    public void setVisible(boolean aFlag) {
        if (control != null) {
            control.getVisibleController().directlySet(true, aFlag);
        }
        super.setVisible(aFlag);
    }
    
/* was:    
    private boolean bManualVisible;
    private boolean bManualVisibleValue;
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        bManualVisible = true;
        bManualVisibleValue = b;
    }
*/    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
/*        
        if (bManualVisible) {
            return bManualVisibleValue;
        }
*/        
        return bIsCurrentlyVisible;
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
if (true || cols > 0) return; //qqqqqqqqqqqqqqq

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
	    			if (sp == null) return;
                    sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
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
     	Bind this comboBox to a Hub and property.
	*/
	public void bind(Hub hub, String propertyPath) {
	    setHub(hub);
	    setPropertyPath(propertyPath);
	}
    
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
    */
    public void setFormat(String fmt) {
        control.setFormat(fmt);
    }
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
    */
    public String getFormat() {
        return control.getFormat();
    }

    /**
        Property used to get file name of image icon.
    */
    public void setImageProperty(String prop) {
        control.setImageProperty(prop);
    }
    /**
        Property used to get file name of image icon.
    */
    public String getImageProperty() {
        return control.getImageProperty();
    }

    /**
        Root directory where images are stored.
    */
    public void setImagePath(String path) {
        control.setImagePath(path);
    }

    /**
        Root directory where images are stored.
        @see #getImageProperty
    */
    public String getImagePath() {
        return control.getImagePath();
    }

    /**
        Returns the icon to use for an active object.
    */
    public Icon getIcon() {
        return control.getIcon();
    }
    /**
        Returns the icon to use for an object.
    */
    public Icon getIcon(Object obj) {
        return control.getIcon(obj);
    }

    
    public void setIconColorProperty(String s) {
    	control.setIconColorProperty(s);
    }
    public String getIconColorProperty() {
    	return control.getIconColorProperty();
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


    // ----- OATableComponent Interface methods -----------------------zzzzzzz

    /** 
        Hub that this component is bound to.
    */
    public Hub getHub() {
        return control.getHub();
    }
    /** 
        Hub that this component is bound to.
    */
    public void setHub(Hub hub) {
        control.setHub(hub);
        if (table != null) {
            table.resetColumn(this);
        }
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
        Width of component, based on average width of the font's character.
    */
    public int getColumns() {
        return control.getColumns();            
    }

    
    /**
        Width of ComboBox, based on average width of the font's character.
    */
    public void setColumns(int x) {
        control.setColumns(x);

        int w = OATable.getCharWidth(this, getFont(), x);
        Border b = this.getBorder();
        if (b != null) {
        	Insets ins = b.getBorderInsets(this);
        	if (ins != null) w += ins.left + ins.right;
        }
        if (table != null) { 
        	table.setColumnWidth(table.getColumnIndex(this), w);
        }
        String str = "w";
        for (int i=1; i<x; i++) str += "w";
        super.setPrototypeDisplayValue(str);
    }

    public void setMaximumColumns(int x) {
        control.setMaximumColumns(x);
        invalidate();
    }
    public int getMaxColumns() {
        return control.getMaximumColumns();            
    }

    public void setMinimumColumns(int x) {
        control.setMinimumColumns(x);
        invalidate();
    }
    public int getMinimumColumns() {
        return control.getMinimumColumns();            
    }
    
    
    /**
        Property path used to retrieve/set value for this component.

    */
    public String getPropertyPath() {
        return control.getPropertyPath();
    }
    /**
        Property path used to retrieve/set value for this component.

    */
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) table.resetColumn(this);
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
            vtf.getController().setAllowChangesWhileFocused();
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
    	Hub h = control.getHub();
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
    			if (obj instanceof OAObject) s = ((OAObject) obj).getPropertyAsString(control.getPropertyPath());
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
        Called by ComboBoxController.MyListCellRenderer.getListCellRendererComponent to get renderer.
    */
    public Component getRenderer(Component renderer, JList list,Object value,int index,boolean isSelected,boolean cellHasFocus) {
		return renderer;
    }

    /**
     * This is called by getRenderer(..) after the default settings have been set.
     */
    public void customizeRenderer(JLabel label, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // to be overwritten
    }
    
    /** 
        Used to supply the renderer when this component is used in the column of an OATable.
        Can be overwritten to customize the rendering.
    */
    @Override
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
            // 20110116 when using linkFromProperty, dont get prop value. Ex: Breed.name linked to Pet.breed (string)
            if (h2.getLinkHub() != null && HubLinkDelegate.getLinkFromProperty(h2) == null) {
            // was: if (h2.getLinkHub() != null) {
            	try {  // 20081010 add catch, in case the propertyPath for tableColumn is being used instead of using the link value
	            	obj = HubLinkDelegate.getPropertyValueInLinkedToHub(h2, h.elementAt(row));
	            	s = control.getPropertyPathValueAsString(obj, control.getFormat());
	                //was: s = OAReflect.getPropertyValueAsString(obj, control.getGetMethods());
            	}
            	catch (Exception e) {
                    s = OAConv.toString(value);
            	}
            }     
            else {
                s = OAConv.toString(value);
            }
            control.update(renderer, obj);
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
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    
    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    public String isValid(Object object, Object value) {
        return null;
    }



    class OAComboBoxController extends ComboBoxController {
        public OAComboBoxController() {
            super(OAComboBox.this);
        }    
        public OAComboBoxController(Hub hub, String propertyPath) {
            super(hub, OAComboBox.this, propertyPath);
        }
        

        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OAComboBox.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OAComboBox.this.isVisible(bIsCurrentlyVisible);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OAComboBox.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }
        
        
        EmptyBorder emptyBorder = new EmptyBorder(0,2,0,0);

        @Override
        protected Component getRenderer(Component renderer, JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            renderer = super.getRenderer(renderer, list, value, index, isSelected, cellHasFocus);
            renderer = OAComboBox.this.getRenderer(renderer, list, value, index, isSelected, cellHasFocus);

            if (renderer instanceof JLabel) {
                ((JLabel)renderer).setBorder(emptyBorder);
                OAComboBox.this.customizeRenderer((JLabel)renderer, list, value, index, isSelected, cellHasFocus);
            }
            return renderer;
        }
    }
    
    
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) return d;
        
        int cols = getMaxColumns();
        if (cols <= 0) {
            cols = getColumns() * 2;
            if (cols <= 0) return d;
        }
        
        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;

        d.width = OATable.getCharWidth(this, getFont(), cols) + inx; 
        
        return d;
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) return d;
        int cols = getMinimumColumns();
        if (cols < 1) return d;
        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;

        d.width = OATable.getCharWidth(this, getFont(), cols) + inx; 
        return d;
    }
    
    /** 
        Called when item is selected 
    */
    public void onItemSelected(int row) {
    }
    
    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLabel() {
        return getController().getLabel();
    }
}


