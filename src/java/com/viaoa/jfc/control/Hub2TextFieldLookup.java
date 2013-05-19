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

package com.viaoa.jfc.control;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;


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
public class Hub2TextFieldLookup extends TextFieldController {
    boolean useQuotes;
    String wildCard = "";
    boolean showActiveObject = true;
    
    public Hub2TextFieldLookup(JTextField tf) {
        super(tf);
    }
    public Hub2TextFieldLookup(Hub hub, JTextField tf, String propertyPath) {
        super(hub, tf, propertyPath); 
    }
    public Hub2TextFieldLookup(OAObject oaObject, JTextField tf, String propertyPath) {
        super(oaObject, tf, propertyPath);
    }

    protected void create(JTextField tf) {
        super.create(tf);
        String s = getPropertyPath();
        if (s != null && getHub() != null) {
            Method[] m = OAReflect.getMethods(getHub().getObjectClass(), s);
            Class c = m[m.length-1].getReturnType();
            useQuotes = ( !c.isPrimitive() && !(Number.class.isAssignableFrom(c)) );
        }
    }

    public @Override void afterPropertyChange(HubEvent e) {
        if (e.getPropertyName().equalsIgnoreCase("changed")) {
            boolean tf = true;// && isParentEnabled(textField);
            if (tf) {
                Object obj = e.getObject();
                if (obj != null && obj instanceof OAObject && ((OAObject)obj).getChanged()) tf = false;
            }
            textField.setEnabled(tf);
        }
    }

    
    /** 
        Display the property value of the active object.  
        If false, then the search text that was entered is always displayed.
        Default is true
    */
    public void setShowActiveObject(boolean b) {
        this.showActiveObject = b;
    }

    /** 
        Display the property value of the active object.  
        If false, then the search text that was entered is always displayed.
        Default is true
    */
    public boolean getShowActiveObject() {
        return this.showActiveObject;
    }

    
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (showActiveObject) super.afterChangeActiveObject(e);
        textField.setEnabled(true && isParentEnabled(textField));
    }

    /** 
        Set any characters that need to be treated as wildcard chars.  They will
        be converted to the SQL '%' wildcard.  Default is "*"
    */
    public void setWildCard(String wildCard) {
        if (wildCard != null) this.wildCard = wildCard;
    }
    /** 
        Set any characters that need to be treated as wildcard chars.  They will
        be converted to the SQL '%' wildcard.  Default is "*"
    */
    public String getWildCard() {
        return wildCard;
    }

    public void actionPerformed(ActionEvent e) {  // over-ride
        saveChanges();
    }

    public void saveChanges() {  // over-ride
        if (getActualHub() == null) return;
        String s = textField.getText();
        if (s.equals(prevText)) return;
        prevText = s;

        ((OATextFieldLookup)textField).performSelect();
    }

    public void performSelect() {
        String s = textField.getText();
        int x = s.length();
        char c = '*';
        if (wildCard != null && wildCard.length() > 0) c = wildCard.charAt(0);
        s = s.replace(c, '%');
        
        Hub h = getHub();
        if (s.length() == 0) {
            h.setActiveObject(-1);
            return;
        }
        String op = " = ";
        if (useQuotes) {
            s = "'" + s + "'";
            op = " LIKE ";
        }
        s = getPropertyPath() + op + s;
        // dont update links until after select
        Object hold = h.getActiveObject();

        h.select(s, "");
        
        if (hold != null) {
        	if (!h.contains(hold)) h.add(hold);
        }
        if (showActiveObject) h.setAO(hold); 
        else h.setAO(null); 
    }

    public void keyTyped(KeyEvent e) {
        // override Hub2TextField
    }
}
