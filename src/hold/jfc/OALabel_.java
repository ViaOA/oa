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

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

/**
    Used for binding a Label component to a property in an Object or Hub.  An icon property can also be
    set to display an image with the label.
    <p>
    Example:<br>
    This will create a Label that will automatically display the FullName property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OALable lbl = new OALabel(hubEmployee, "fullName", 30);
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see Hub2Label
*/
public class OALabel_ extends JLabel implements OAJFCComponent, OATableComponent {
    Hub2Label label;
    int columns;
    int oaWidth;
    OATable table;
    String heading = "";


    /**
        Create an unbound label.
    */
    public OALabel() {
        label = new Hub2Label(this);
        //   setText(" ");  <-- this screws up preferredSize
    }

    /**
        Create label that is bound to a property for the active object in a Hub.
    */
    public OALabel(Hub hub, String propertyPath) {
        label = new Hub2Label(hub,this,propertyPath);
    }
    /**
        Create label that is bound to a property for the active object in a Hub.
        @param cols width of label.
    */
    public OALabel(Hub hub, String propertyPath, int cols) {
        label = new Hub2Label(hub,this,propertyPath);
        setColumns(cols);
    }

    /**
        Create label that is bound to a property for an object.
    */
    public OALabel(OAObject hubObject, String propertyPath) {
        label = new Hub2Label(hubObject,this,propertyPath);
    }

    /**
        Create label that is bound to a property for an object.
        @param cols width of label.
    */
    public OALabel(OAObject hubObject, String propertyPath, int cols) {
        label = new Hub2Label(hubObject,this,propertyPath);
        setColumns(cols);
        // setText(" ");  //<-- this screws preferredSize if before setColumns()
    }

    /** Used with imageProperty, imagePath to display icon */
    public OALabel(Hub hub) {
        label = new Hub2Label(hub,this);
    }

    public Hub2Label getHub2Label() {
    	return label;
    }
    
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        label.setFormat(fmt);
    }
    /** 
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        return label.getFormat();
    }

    private boolean bRemoved;
    public void addNotify() {
        if (bRemoved) {
            label.setHub(label.getHub());
            bRemoved = false;
        }
        else {
            if (columns > 0) setColumns(columns);
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
        label.close();
    }
    
    
    /**
        Get the property name used for displaying an image with component.
    */
    public void setImageProperty(String prop) {
        label.setImageProperty(prop);
    }
    /**
        Get the property name used for displaying an image with component.
    */
    public String getImageProperty() {
        return label.getImageProperty();
    }

    /**
        Root directory path where images are stored.
    */
    public void setImagePath(String path) {
        label.setImagePath(path);
    }
    /**
        Root directory path where images are stored.
    */
    public String getImagePath() {
        return label.getImagePath();
    }

    
    public int getMaxImageHeight() {
    	return label.getMaxImageHeight();
	}
	public void setMaxImageHeight(int maxImageHeight) {
		label.setMaxImageHeight(maxImageHeight);
	}

	public int getMaxImageWidth() {
		return label.getMaxImageWidth();
	}
	public void setMaxImageWidth(int maxImageWidth) {
		label.setMaxImageWidth(maxImageWidth);
	}
    
    
    /**
        Hub this this component is bound to.
    */
    public void setHub(Hub hub) {
        label.setHub(hub);        
    }
    /**
        Hub this this component is bound to.
    */
    public Hub getHub() {
        if (label == null) return null;
        return label.getHub();
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
        return label.getActualHub();
    }

    /**
        Returns the single object that is bound to this component.
    */
    public Object getObject() {
        return label.getObject();
    }

    /**
        Set by OATable when this component is used as a column.
    */
    public void setTable(OATable table) {
        this.table = table;
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),getPropertyPath());
    }


    /**
        Set by OATable when this component is used as a column.
    */
    public OATable getTable() {
        return table;
    }

    /**
        Width of label, based on average width of the font's character.
    */
    public int getColumns() {
        return columns;            
    }

    /**
        Width of label, based on average width of the font's character.
    */
    public void setColumns(int x) {
        columns = x;
        this.oaWidth = OATable.getCharWidth(this,getFont(),x+1);  // 2008/05/18 added 1 for border
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),oaWidth);
    }

    /**
    public Dimension getSize() {
        Dimension d = super.getSize();
        if (oaWidth > 0) d.width = this.oaWidth;
        return d;
    }
    */
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (oaWidth > 0) d.width = this.oaWidth;
        return d;
    }
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (d.width < oaWidth) d.width = this.oaWidth;
        return d;
    }
    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (oaWidth > 0) d.width = this.oaWidth;
        return d;
    }
        


    /**
        Property path used to retrieve/set value for this component.
        @see HubGuiAdapter#setPropertyPath(String)
    */
    public void setPropertyPath(String path) {
        label.setPropertyPath(path);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),path);
    }
    /**
        Property path used to retrieve/set value for this component.
    */
    public String getPropertyPath() {
         if (label == null) return null;
         return label.getPropertyPath();
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

    private static Border borderFocus;
    // OATableComponent Interface method
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        if (!isSelected && !hasFocus) {
            renderer.setForeground( UIManager.getColor(table.getForeground()) );
            renderer.setBackground( UIManager.getColor(table.getBackground()) );
        }
    	
    	if (getHub() != null) {
            renderer.setHorizontalTextPosition(this.getHorizontalTextPosition());
            renderer.setIcon( label.getIcon(getHub().elementAt(row)) );

            Hub h = label.getHub();  // could be a link hub
            if (table instanceof OATable) {
            	h = ((OATable) table).getHub();
            }
            Object obj = h.elementAt(row);
            label.updateComponent(renderer, obj, renderer.getText());
        }
        
/*
    	if (hasFocus) renderer.setBorder( UIManager.getBorder("Table.focusCellHighlightBorder") );
        else renderer.setBorder(null);
*/
        if (hasFocus) {
        	if (borderFocus == null) {
        		borderFocus = new CompoundBorder(UIManager.getBorder("Table.focusCellHighlightBorder"), new LineBorder(UIManager.getColor("Table.focusCellBackground"),1));
        	}
        	renderer.setBorder( borderFocus );
        }
        else renderer.setBorder(null);

        if (isSelected || hasFocus) {
            renderer.setForeground( UIManager.getColor("Table.selectionForeground") );
            renderer.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        return renderer;
    }

    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (label != null) {
            label.setEnabled(b);
        }
    }
    public void setReadOnly(boolean b) {
        label.setReadOnly(b);
    }
    public boolean getReadOnly() {
        return label.getReadOnly();
    }
    
    
    public void setIconColorProperty(String s) {
    	label.setIconColorProperty(s);
    }
    public String getIconColorProperty() {
    	return label.getIconColorProperty();
    }
    
    public void setBackgroundColorProperty(String s) {
    	label.setBackgroundColorProperty(s);
    }
    public String getBackgroundColorProperty() {
    	return label.getBackgroundColorProperty();
    }

    public void setDebug(boolean bDebug) {
    	label.bDebug = bDebug;
    }
    
}

