/* 
2003/11/11 took out "setLineWrap(true)"


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
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;


/**
    Example:<br>
    This will create a JTextArea that will automatically display the Notes property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OATextArea txta = new OATextArea(hubEmployee, "notes", 8,30); // 8 rows, 30 columns
    txta.setLineWrap(true);
    txta.setWrapStyleWord(true);
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OATextArea
*/
public class OATextArea_ extends JTextArea implements OAJFCComponent, OATableComponent {
    Hub2TextArea htf;
    OATable table;
    String heading = "";


    /**
        Create an unbound TextArea.
    */
    public OATextArea() {
        htf = new Hub2TextArea(this);
    }

    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextArea(Hub hub, String propertyPath) {
        htf = new Hub2TextArea(hub,this,propertyPath);
    }

    /**
        Create TextArea that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param rows number of rows to visually display.
        @param cols is the width
    */
    public OATextArea(Hub hub, String propertyPath, int rows, int cols) {
        super(rows, cols);
        htf = new Hub2TextArea(hub,this,propertyPath);
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextArea(OAObject hubObject, String propertyPath) {
        htf = new Hub2TextArea(hubObject,this,propertyPath);
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
        @param rows number of rows to visually display.
        @param cols is the width
    */
    public OATextArea(OAObject hubObject, String propertyPath, int rows, int cols) {
        super(rows, cols);
        htf = new Hub2TextArea(hubObject,this,propertyPath);
    }


	private boolean bLineWrap;
    @Override
    public void setLineWrap(boolean wrap) {
    	bLineWrap = wrap;
    	super.setLineWrap(wrap);
    }
    
    /**
        update with active object.
    */
    public void addNotify() {
        super.addNotify();
        setWrapStyleWord(true);
        setLineWrap(bLineWrap);
        htf.afterChangeActiveObject(null); 
    }

    // ----- OATableComponent Interface methods -----------------------
    public void setHub(Hub hub) {
        htf.setHub(hub);        
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),getPropertyPath());
    }
    public Hub getHub() {
        return htf.getHub();
    }
    public void setTable(OATable table) {
        this.table = table;
    }
    public OATable getTable() {
        return table;
    }
    public void setColumns(int x) {
        super.setColumns(x);
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),super.getPreferredSize().width);
    }

    public String getPropertyPath() {
        return htf.getPropertyPath();
    }
    public void setPropertyPath(String path) {
        htf.setPropertyPath(path);
        if (table != null) table.setColumnPropertyPath(table.getColumnIndex(this),path);
    }
    public String getTableHeading() { //zzzzz
        return heading;   
    }
    public void setTableHeading(String heading) { //zzzzz
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return renderer;
    }

    OATextAreaTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATextAreaTableCellEditor(this);
        }
        return tableCellEditor;
    }

    // 2004/08/04
    /**
        Used to manually enable/disable.
    */
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (htf != null) {
            htf.setEnabled(b);
        }
    }

    public void setReadOnly(boolean b) {
        htf.setReadOnly(b);
    }
    public boolean getReadOnly() {
        return htf.getReadOnly();
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
	public String getFormat() {
		return htf.getFormat();
	}	
}

class OATextAreaTableCellEditor extends OATableCellEditor {
    OATextArea vtf;
   
    public OATextAreaTableCellEditor(OATextArea tf) {
        super(tf, (OATableCellEditor.LEFT | OATableCellEditor.RIGHT) );
        this.vtf = tf;
    }
    public void focusGained(FocusEvent e) {
        super.focusGained(e);
        vtf.selectAll();
    }
    public Object getCellEditorValue() {
        return vtf.getText();
	}

}

