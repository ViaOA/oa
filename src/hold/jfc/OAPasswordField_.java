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
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.*;

/** 
    JPasswordField subclass that is bound to a property in an Object or Hub.  
    <p>
    Example:<br>
    This will create a PasswordField that will automatically display the Password property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    OAPasswordField pass = new OAPasswordField(hubEmployee, "password", 12);
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see Hub2TextField
*/
public class OAPasswordField_ extends JPasswordField implements OAJFCComponent, OATableComponent {
    Hub2TextField htf;
    OATable table;
    String heading = "";

    /**
        Create a PasswordField that is not bound to a Hub.
    */  
    public OAPasswordField() {
        htf = new Hub2TextField(this);
    }
    /**
        Bind a PasswordField to a property path in the active object of a Hub.
    */  
    public OAPasswordField(Hub hub, String propertyPath) {
        htf = new Hub2TextField(hub,this,propertyPath);
    }
    /**
        Bind a PasswordField to a property path in the active object of a Hub.
    */  
    public OAPasswordField(Hub hub, String propertyPath, int cols) {
        super(cols);
        htf = new Hub2TextField(hub,this,propertyPath);
    }
    /**
        Bind a component to a property path for an object.
    */  
    public OAPasswordField(OAObject oaObject, String propertyPath) {
        htf = new Hub2TextField(oaObject,this,propertyPath);
    }
    /**
        Bind a component to a property path for an object.
    */  
    public OAPasswordField(OAObject oaObject, String propertyPath, int cols) {
        super(cols);
        htf = new Hub2TextField(oaObject,this,propertyPath);
    }

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
    public String getTableHeading() { 
        return heading;   
    }
    public void setTableHeading(String heading) {
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    OAPasswordFieldTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAPasswordFieldTableCellEditor(this);
        }
        return tableCellEditor;
    }


    /** called by getTableCellRendererComponent */
    public Component getTableRenderer(JLabel renderer, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String s = renderer.getText();
        int x = s.length();
        StringBuffer sb = new StringBuffer(x);
        for (int i=0; i<8; i++) sb.append('*');
        renderer.setText(new String(sb));

        
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
        else {
            renderer.setForeground( UIManager.getColor(table.getForeground()) );
            renderer.setBackground( UIManager.getColor(table.getBackground()) );
        }
        
        return renderer;
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



