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

/***************** NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE ***********************
        Changes need be made to both:  OATextField and OAPasswordField
***************** NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE NOTE ***********************/

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
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

/**
    Used for binding a JTextField component to a property in an Object or Hub.
    <p>
    Example:<br>
    This will create a JTextField that will automatically display the LastName property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OATextField txt = new OATextField(hubEmployee, "LastName", 30);
    </pre>
    <br>
    Example:<br>
    This will create a JTextField that will automatically display the Department Name property of the
    Department object for the active Employee object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OATextField txt = new OATextField(hubEmployee, "Department.name", 30);
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see Hub2TextField
    @see OAPasswordField
    @see OATextArea
*/
public class OATextField_ extends JTextField implements OAJFCComponent, OATableComponent {
    Hub2TextField htf;
    OATable table;
    String heading = "";

    /**
        Create an unbound TextField.
    */
    public OATextField() {
        htf = new Hub2TextField(this);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextField(Hub hub, String propertyPath) {
        htf = new Hub2TextField(hub,this,propertyPath);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OATextField(Hub hub, String propertyPath, int cols) {
        super(cols);
        htf = new Hub2TextField(hub,this,propertyPath);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public OATextField(OAObject hubObject, String propertyPath) {
        htf = new Hub2TextField(hubObject,this,propertyPath);
    }

    /**
        Create TextArea that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property.
        @param cols is the width
    */
    public OATextField(OAObject hubObject, String propertyPath, int cols) {
        super(cols);
        htf = new Hub2TextField(hubObject,this,propertyPath);
    }


    /**
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public void setFormat(String fmt) {
        htf.setFormat(fmt);
    }

    /**
        Format used to display this property.  Used to format Date, Times and Numbers.
        @see OADate#OADate
        @see OAConverterNumber#OAConverterNumber
    */
    public String getFormat() {
        return htf.getFormat();
    }

    
    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    String isValid(Object object, Object value) {
        return null;
    }
    
    /** might want to use (and test) this later
    boolean addNotifyFlag;
    public void addNotify() {
        super.addNotify();
        if (!addNotifyFlag) {
            addNotifyFlag = true;
            htf.initialize(this);
        }
    }
    ***/

    public void addNotify() {
        super.addNotify();
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

    public Dimension getMinimumSize() {
        Dimension d = super.getPreferredSize();
        //09/15/99   Dimension d = super.getMinimumSize();
        return d;
    }

    /**
        Flag to set TextField to be read only.
    */
    public void setReadOnly(boolean b) {
        htf.setReadOnly(b);
    }
    /**
        Flag to set TextField to be read only.
    */
    public boolean getReadOnly() {
        return htf.getReadOnly();
    }

    public void setText(String s) {
        super.setText(s);
        if (htf != null) htf.saveChanges();
    }

    public void setText(String s, boolean bSaveChanges) {
        super.setText(s);
        if (htf != null && bSaveChanges) htf.saveChanges();
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (hasFocus) {
        	// renderer.setBorder(new LineBorder(UIManager.getColor("Table.selectionBackground"), 1));
        	renderer.setBorder(UIManager.getBorder("Table.focusCellHighlightBorder") );
        }
        else renderer.setBorder(null);

        if (hasFocus) {
            renderer.setForeground( UIManager.getColor("Table.focusCellForeground") );
            renderer.setBackground( UIManager.getColor("Table.focusCellBackground") );
        }
        else if (isSelected) {
            renderer.setForeground( UIManager.getColor("Table.selectionForeground") );
            renderer.setBackground( UIManager.getColor("Table.selectionBackground") );
        }
        else {
            renderer.setForeground( UIManager.getColor(table.getForeground()) );
            renderer.setBackground( UIManager.getColor(table.getBackground()) );
        }

        return renderer;
    }


    OATextFieldTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OATextFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }

    // 2004/08/04
    /**
        Used to manually enable/disable.
    */
    public void setEnabled(boolean b) {
        setEnabled(b, false);
    }
    // @param bOnce if this is to resume normal enable afterwards
    public void setEnabled(boolean b, boolean bOnce) {
        // overwritten to find out if button is being manually enabled
        super.setEnabled(b);
        if (htf != null && !bOnce) {
            htf.setEnabled(b);
        }
    }

    /*
    public Hub2TextField getHub2TextField() {
        return htf;
    }
    */

}



