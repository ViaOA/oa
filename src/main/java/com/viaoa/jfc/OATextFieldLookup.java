/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
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
    Used for binding a JTextField component to perform a select on a Hub.
    The query will be based on the property name and the value entered.
    Users can use specified wildcard (default "*") character that is used with 
    the "LIKE" operator.
    <p>
    Example:<br>
    This will create a JTextField that will automatically select Employees with the LastName property
    matching the value entered.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    JTextField txt = new JTextField(30);
    Hub2TextFieldLookup tfl = new Hub2TextFieldLookup(hubEmployee, txt, "LastName");
    tfl.setWildCard("*");   // will be converted to '%' and used with LIKE operator
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OATextFieldLookup
*/
public class OATextFieldLookup extends OATextField {
    OATable table;
    String heading = "";
    
    
    /**
        Create an unbound TextFieldLookup.
    */
    public OATextFieldLookup() {
        control = new Hub2TextFieldLookup(this);
    }

    /**
        Create TextFieldLookup that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property to build query for.
    */
    public OATextFieldLookup(Hub hub, String propertyPath) {
        control = new Hub2TextFieldLookup(hub,this,propertyPath);
    }

    /**
        Create TextFieldLookup that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property to build query for.
        @param cols is the width
    */
    public OATextFieldLookup(Hub hub, String propertyPath, int cols) {
        control = new Hub2TextFieldLookup(hub,this,propertyPath);
        setColumns(cols);
    }

    /**
        Create TextFieldLookup that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property to build query for.
        @param cols is the width
    */
    public OATextFieldLookup(OAObject hubObject, String propertyPath) {
        control = new Hub2TextFieldLookup(hubObject,this,propertyPath);
    }

    
    
    
    /**
        Create TextFieldLookup that is bound to a property path in an Object.
        @param propertyPath path from Hub, used to find bound property to build query for.
        @param cols is the width
    */
    public OATextFieldLookup(OAObject hubObject, String propertyPath, int cols) {
        control = new Hub2TextFieldLookup(hubObject,this,propertyPath);
        setColumns(cols);
    }

    /** 
        Set any characters that need to be treated as wildcard chars.  They will
        be converted to the SQL '%' wildcard.  Default is "*"
    */
    public void setWildCard(String wildCard) {
        ((Hub2TextFieldLookup)control).setWildCard(wildCard);
    }
    /** 
        Set any characters that need to be treated as wildcard chars.  They will
        be converted to the SQL '%' wildcard.  Default is "*"
    */
    public String getWildCard() {
        return ((Hub2TextFieldLookup)control).getWildCard();
    }
    
    /** 
        Display the property value of the active object.  
        If false, then the search text that was entered is always displayed.
        Default is true
    */
    public void setShowActiveObject(boolean b) {
        ((Hub2TextFieldLookup)control).setShowActiveObject(b);
    }
    /** 
        Display the property value of the active object.  
        If false, then the search text that was entered is always displayed.
        Default is true
    */
    public boolean getShowActiveObject() {
        return ((Hub2TextFieldLookup)control).getShowActiveObject();
    }

    
    /**
        Used to manually enable/disable.
    
    public void setEnabled(boolean b) {
        // overwritten to find out if it is being manually enabled
        super.setEnabled(b);
        if (control != null) control.setEnabled(b);
    }
    */
    public void performSelect() {
        ((Hub2TextFieldLookup)control).performSelect();
    }

    
    
    
}

