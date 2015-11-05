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
import java.lang.reflect.Method;

import javax.swing.*;
import javax.swing.table.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

public class OALabel extends JLabel implements OATableComponent, OAJFCComponent {
    private OALabelController control;
    private OATable table;
    private String heading = "";

    /**
        Create an unbound label.
    */
    public OALabel() {
        control = new OALabelController();
    }

    /**
        Create label that is bound to a property for the active object in a Hub.
    */
    public OALabel(Hub hub, String propertyPath) {
        control = new OALabelController(hub, propertyPath);
    }
    /**
        Create label that is bound to a property for the active object in a Hub.
        @param cols width of label.
    */
    public OALabel(Hub hub, String propertyPath, int cols) {
        control = new OALabelController(hub, propertyPath);
        setColumns(cols);
    }

    /**
        Create label that is bound to a property for an object.
    */
    public OALabel(OAObject hubObject, String propertyPath) {
        control = new OALabelController(hubObject, propertyPath);
    }

    /**
        Create label that is bound to a property for an object.
        @param cols width of label.
    */
    public OALabel(OAObject hubObject, String propertyPath, int cols) {
        control = new OALabelController(hubObject, propertyPath);
        setColumns(cols);
        // setText(" ");  //<-- this screws preferredSize if before setColumns()
    }

    /** Used with imageProperty, imagePath to display icon */
    public OALabel(Hub hub) {
        control = new OALabelController(hub);
    }

    public OALabelController getController() {
    	return control;
    }

    public void setPassword(boolean b) {
        getController().setPassword(b);
    }
    public boolean isPassword() {
        return getController().isPassword();
    }
    
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        control.setFormat(fmt);
    }
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        return control.getFormat();
    }

    private boolean bRemoved;
    public void addNotify() {
        if (bRemoved) {
            control.setHub(control.getHub());
            bRemoved = false;
        }
        else {
            if (getColumns() > 0) setColumns(getColumns());
        }
        super.addNotify();
    }

    /* 2005/02/07 need to manually call close instead
    public void removeNotify() {
        super.removeNotify();
        bRemoved = true;
        label.close();
    }
    */
    public void close() {
        bRemoved = true;
        control.close();
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
        Hub this this component is bound to.
    */
    public void setHub(Hub hub) {
        control.setHub(hub);        
    }
    /**
        Hub this this component is bound to.
    */
    public Hub getHub() {
        if (control == null) return null;
        return control.getHub();
    }
    /**
        Used to get the <i>actual</i> Hub being used.  This is used when a property path
        includes a another Hub within the path.
        <p>
        Example:<br>
        The property path from an Employee Class "department.manger.employess", where the Hub for "employees"
        would be the actutal Hub.
    */
    public Hub getActualHub() {
        return control.getActualHub();
    }

    /**
        Returns the single object that is bound to this component.
    */
    public Object getObject() {
        return control.getObject();
    }

    /**
        Set by OATable when this component is used as a column.
    */
    public void setTable(OATable table) {
        this.table = table;
        if (table != null) table.resetColumn(this);
    }


    /**
        Set by OATable when this component is used as a column.
    */
    public OATable getTable() {
        return table;
    }

    /**
        Width of label, based on average width of the font's character 'w'.
    */
    public int getColumns() {
        return control.getColumns();            
    }
    /**
        Width of label, based on average width of the font's character.
    */
    public void setColumns(int x) {
        control.setColumns(x);
        invalidate();
        if (table != null) {
            int w = OATable.getCharWidth(this,getFont(),x+1);
            table.setColumnWidth(table.getColumnIndex(this),w);
        }
    }
    public void setMaximumColumns(int x) {
        control.setMaximumColumns(x);
        invalidate();
    }
    public int getMaxColumns() {
        return control.getMaximumColumns();
    }
    public void setMiniColumns(int x) {
        control.setMinimumColumns(x);
        invalidate();
    }
    public int getMiniColumns() {
        return control.getMinimumColumns();
    }

    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (isPreferredSizeSet()) return d;
        int cols = getColumns();
        if (cols <= 0) {
            cols = control.getPropertyInfoDisplayColumns();
        }
        
        if (cols > 0) {
            Insets ins = getInsets();
            int inx = ins == null ? 0 : ins.left + ins.right;
            d.width = OATable.getCharWidth(this,getFont(),cols)+inx+2;
        }
        return d;
    }
    
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (isMaximumSizeSet()) return d;
        int cols = getMaxColumns();
        if (cols < 1)  {
            //maxCols = control.getDataSourceMaxColumns();
            //if (maxCols < 1) {
            cols = control.getPropertyInfoMaxColumns();
            
            if (cols < 1) {
                cols = getColumns() * 2; 
            }
        }
        if (cols > 0) d.width = OATable.getCharWidth(this, getFont(), cols);
        
        // also check size of text, so that label is not bigger then the text it needs to display
        String s = getText();
        if (s == null) s = " ";

        Insets ins = getInsets();
        int inx = ins == null ? 0 : ins.left + ins.right;
        
        FontMetrics fm = getFontMetrics(getFont());
        d.width = Math.min(d.width, fm.stringWidth(s)+inx+2);
        
        // dont size under pref size
        Dimension dx = getPreferredSize();
        d.width = Math.max(d.width, dx.width);

        return d;
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (isMinimumSizeSet()) return d;
        int cols = getMiniColumns();
        if (cols < 1) return d;
        d.width = OATable.getCharWidth(this, getFont(), cols+1);
        return d;
    }
        


    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path) {
        control.setPropertyPath(path);
        if (table != null) table.resetColumn(this);
    }
    /**
        Property path used to retrieve/set value for this component.
    */
    public String getPropertyPath() {
         if (control == null) return null;
         return control.getPropertyPath();
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
    public TableCellEditor getTableCellEditor() {
        return null;
    }

    // OATableComponent Interface method
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (control != null) {
            control.getTableRenderer(renderer, table, value, isSelected, hasFocus, row, column);
        }
        return renderer;
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
    public void setVisible(Hub hub) {
        control.getVisibleController().add(hub);
    }    
    public void setVisible(Hub hub, String prop) {
        control.getVisibleController().add(hub, prop);
    }    
    public void setVisible(Hub hub, String prop, Object compareValue) {
        control.getVisibleController().add(hub, prop, compareValue);
    }    

    /**
     * This is a callback method that can be overwritten to determine if the button should be visible or not.
     */
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }

    class OALabelController extends LabelController {
        public OALabelController() {
            super(OALabel.this);
        }    
        public OALabelController(Hub hub) {
            super(hub, OALabel.this);
        }
        public OALabelController(Hub hub, String propertyPath) {
            super(hub, OALabel.this, propertyPath);
        }
        public OALabelController(OAObject hubObject, String propertyPath) {
            super(hubObject, OALabel.this, propertyPath);
        }        
        
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            bIsCurrentlyVisible = super.isVisible(bIsCurrentlyVisible);
            return OALabel.this.isVisible(bIsCurrentlyVisible);
        }
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }

    public void setEnabled(Hub hub) {
        control.getEnabledController().add(hub);
    }
    public void setEnabled(Hub hub, String prop) {
    }
    public void setEnabled(Hub hub, String prop, Object compareValue) {
    }

    public void setLabel(JLabel lbl) {
        getController().setLabel(lbl);
    }
    public JLabel getLabel() {
        return getController().getLabel();
    }

}

