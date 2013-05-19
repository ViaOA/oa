/*

2003/10/21 added support for Undo

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

import java.awt.event.*;

import javax.swing.*;
import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.jfc.*;

/**
    Used for binding a JTextField component to a property in an Object or Hub.
    <p>
    Example:<br>
    This will create a JTextField that will automatically display the LastName property of the
    active object in a Hub of Employee objects.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    JTextField txt = new JTextField(30);
    new Hub2TextField(hubEmployee, txt, "LastName");
    </pre>
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OATextField
    @see OAPasswordField
*/
public class Hub2TextField extends Hub2Gui implements FocusListener, ActionListener, KeyListener, MouseListener {
    JTextField textField;
    String prevValue;
    private boolean bSettingText;
    private Object activeObject;
    
    /**
        Create an unbound TextField.
    */
    public Hub2TextField(JTextField tf) {
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public Hub2TextField(Hub hub, JTextField tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public Hub2TextField(Object object, JTextField tf, String propertyPath) {
        super(object, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    protected void create(JTextField tf) {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeActionListener(this);
            textField.removeMouseListener(this);
        }
        textField = tf;
        if (actualHub == null) return; // 20090607
        
        Class c = OAReflect.getClass(getGetMethod());
        if (OAReflect.isNumber(c)) {
            textField.setHorizontalAlignment(JTextField.RIGHT);
        }
        else {
            textField.setHorizontalAlignment(JTextField.LEFT);
        }

        if (textField != null) {
            textField.addFocusListener(this);
            textField.addKeyListener(this);
            textField.addActionListener(this);
            textField.addMouseListener(this);
        }
        // set initial value of textField
        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
        }
        else {
            //qqqq textField.setEnabled(false);
            bSettingText = true;
            if (document != null) document.setAllowAll(true);
            if (tf != null) {
                if (tf instanceof OATextField) ((OATextField)tf).setText("", false);
                else tf.setText("");
            }
            if (document != null) document.setAllowAll(false);
            bSettingText = false;
        }

        document = new OAPlainDocument() {
            public void handleError(int errorType) {
            	super.handleError(errorType);
            	String msg = "";
            	switch (errorType) {
            	case OAPlainDocument.ERROR_MAX_LENGTH:
            		msg = "Maximum input exceeded, currently set to " + getMax(); 
            		break;
            	case OAPlainDocument.ERROR_INVALID_CHAR:
            		return;
            	}
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(Hub2TextField.this.textField), msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
        };
        document.setMaxLength(getMax());
        textField.setDocument(document);
        
        c = OAReflect.getClass(getGetMethod());
        if (OAReflect.isNumber(c)) {
            if (OAReflect.isInteger(c)) {
            	document.setValidChars("0123456789- ");
            }
            else {
            	document.setValidChars("0123456789-. ");
            }
        }        
    }


    protected int dataSourceMax=-2;
    protected int max=-1;
    private OAPlainDocument document;
    public int getDataSourceMax() {
        if (dataSourceMax == -2) {
            if (hub != null) {
            	dataSourceMax = -1;
                OADataSource ds = OADataSource.getDataSource(actualHub.getObjectClass());
                if (ds != null) {
                    dataSourceMax = ds.getMaxLength(actualHub.getObjectClass(), propertyName);
                    Method method = getGetMethod();
                    if (method != null) {
                        if (method.getReturnType().equals(String.class)) {
                            if (dataSourceMax > 254) dataSourceMax = -1;
                        }
                        else dataSourceMax = -1;
                    }
                }
            }
        }
        return dataSourceMax;
    }
    public int getMax() {
        if (max > getDataSourceMax() || max < 0) {
            if (dataSourceMax >= 0) return dataSourceMax;
        }
        return max;
    }
    /** max length of text.  If -1 (default) then unlimited.  
    */
    public void setMax(int x) {
        max = x;
    	if (document != null) document.setMaxLength(getMax());  // 2006/12/01
    }
    
    
    
    
    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (textField != null) create(textField);
    }

    public void close() {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeActionListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }

    
    // HUB Events
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (textField.hasFocus()) {
            saveChanges();
        }
        if (getActualHub() == null) return;
        
        activeObject = getActualHub().getActiveObject();

        String hcas;
        if (activeObject != null) {
            setInternalEnabled(isEnabled(textField, activeObject)); 

            hcas = OAReflect.getPropertyValueAsString(activeObject, getGetMethod(), getFormat(), "");

            // hcas = ClassModifier.getPropertyValueAsString(oaObject, getGetMethod());
            if ((activeObject instanceof OAObject) && OAObjectReflectDelegate.getPrimitiveNull((OAObject)activeObject, getPropertyName()) ) hcas = "";
        }
        else {
            hcas = "";
            setInternalEnabled(false);
        }

        prevValue = hcas;
        if (document != null) document.setAllowAll(true);
        bSettingText = true;

        super.updateComponent(textField, activeObject, hcas);

        bSettingText = false;
        if (document != null) document.setAllowAll(false);
        if (!getReadOnly())  hcas = "";
        // textField.setToolTipText(hcas);
    }

    public @Override void afterPropertyChange(HubEvent e) {
        if (activeObject != null && e.getObject() == activeObject) {
        	if (e.getPropertyName().equalsIgnoreCase(this.getPropertyName()) ) {
        		afterChangeActiveObject(e); // could be calculated property
        	}
        	else {
        		setInternalEnabled(isEnabled(textField, e.getObject()));
        	}
        }
    }

    // Focus Events
    public void focusGained(FocusEvent e) {
    	Hub h = getActualHub();
    	if (h != null) activeObject = getActualHub().getActiveObject();
    	else activeObject = null;
    	
    	if (textField instanceof OATextField) {
    	    OATextField tf = (OATextField) textField;
    		if (tf.getTable() != null) return;  // OATextFieldTableCellEditor will handle this based on how the focus is gained - could be on keystroke from Table cell
    		if (tf.getParent() instanceof JTable) return;  // ""
    	}
		if (!bMousePressed) {
			textField.selectAll();
		}
    }

    /**
        Saves changes to property in active object of Hub.
    */
    public void focusLost(FocusEvent e) {
        saveChanges();
    }

    @Override
    public void setReadOnly(boolean b) {
        super.setReadOnly(b);
        setEnabled(!b);
        afterChangeActiveObject(null);
    }

    public void actionPerformed(ActionEvent e) {
        if (textField.hasFocus()) saveChanges();
    }

    boolean bChanging; // only used by saveChanges(), calling setText generates actionPerformed()
    public void saveChanges() {
        if (bSettingText) return;
        if (bChanging) return;
        try {
            bChanging = true;
            _saveChanges();
        }
        finally {
            bChanging = false;
        }
    }
    private void _saveChanges() {
    	// 2006/06/14 bug with password always failing this while being used for a table
       if (!textField.isValid()) {
    	   if (!(textField instanceof OAPasswordField)) return;
       }
       // was; if (!textField.isValid()) return;
       
        if (activeObject == null) return;

        int i,x;
        String s = null;
        s = textField.getText();

        if (!s.equals(prevValue)) {
            if (!getReadOnly()) {
                try {
                    Object prev = OAReflect.getPropertyValue(activeObject, getGetMethod());

                    Object convertedValue = OAReflect.convertParameterFromString(getSetMethod(), s, getFormat());
                    if (convertedValue == null && s.length() > 0) {
                        JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), "Invalid Entry \""+s+"\"", "", JOptionPane.ERROR_MESSAGE);
                        s = prevValue;
                        if (document != null) document.setAllowAll(true);
                        bSettingText = true;
                        textField.setText(prevValue);
                        bSettingText = false;
                        if (document != null) document.setAllowAll(false);
                        return;
                    }

                    Method m = getValidMethod();
                    if (m != null) {
                        OAEditMessage oamsg = new OAEditMessage();
                        Object[] objs = new Object[] { convertedValue, oamsg };
                        Boolean B = (Boolean) m.invoke(activeObject, objs);
                        if (B != null && !B.booleanValue()) {
                            String msg = oamsg.getMessage();
                            if (msg == null || msg.length() == 0) s = "Invalid entry \""+s+"\"for "+getPropertyName();
                            JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), msg, "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                            textField.setText(prevValue);
                            textField.requestFocus();
                            return;
                        }
                    }

                    OAReflect.setPropertyValue(activeObject, getSetMethod(), convertedValue);

                    if (s == null || s.length() == 0) {
                        Class c = getGetMethod().getReturnType();
                        if (OAReflect.isNumber(c) && activeObject instanceof OAObject) {
                        	OAObjectReflectDelegate.setProperty((OAObject)activeObject, getPropertyName(), null, null);  // was: setNull(prop)
                        }
                    }
                    OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, getPropertyName(), prev, OAReflect.getPropertyValue(activeObject, getGetMethod())) );
                }
                catch (Exception e) {
                	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(textField), "Invalid Entry \""+e.getMessage()+"\"", "", JOptionPane.ERROR_MESSAGE);
                    // throw new OAException("Hub2TextField saveChanges() exception invoking method="+ getSetMethod().getName()+" class="+this.getActualHub().getObjectClass().getName()+" "+e );
                }

                String s2 = OAReflect.getPropertyValueAsString(activeObject, getGetMethod(), getFormat(), "");

                if (!s.equals(s2)) {
                    s = s2;
                    bSettingText = true;
                    if (document != null) document.setAllowAll(true);
                    textField.setText(s);
                    bSettingText = false;
                    if (document != null) document.setAllowAll(false);
                }
            }
            else {
                String prop = getPropertyName();
                if (prop == null || prop.length() == 0) {  // use object.  (ex: String.class)
                    Object oldObj = activeObject;
                    Hub h = getActualHub();
                    Object newObj = OAReflect.convertParameterFromString(h.getObjectClass(), s);
                    int posx = h.getPos(oldObj);
                    h.remove(posx);
                    h.insert(newObj, posx);
                }
                else {
                    s = prevValue;
                    bSettingText = true;
                    if (document != null) document.setAllowAll(true);
                    textField.setText(prevValue); // should not happen since it will be disabled
                    bSettingText = false;
                    if (document != null) document.setAllowAll(false);
                }
            }
            prevValue = s;
        }
        /* was
        else {
            if (tempChangedFlag) {
                ((OAObject)activeObject).setChanged(false);
            }
        }
        tempChangedFlag = false;
        */
    }

    private String undoDescription;
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public void setUndoDescription(String s) {
        undoDescription = s;
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return undoDescription;
    }
    

    // Key Events
    private boolean bConsumeEsc;
    @Override
    public void keyPressed(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        	bConsumeEsc = false;
            if (!textField.getText().equals(prevValue)) {
        		bConsumeEsc = true;
            	e.consume();
	    		textField.setText(prevValue);
	    		afterChangeActiveObject(null);
            }
        	textField.selectAll();
    	}
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE && bConsumeEsc) {
            e.consume();
    	}
    }
    @Override
    public void keyTyped(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE && bConsumeEsc) {
            e.consume();
            return;
    	}
    	
    	/* was
    	private boolean tempChangedFlag;  // used by keylistener to determine if the setChange() has been called
 
 
    	Hub h = getActualHub();
        if (h != null) {
            Object obj = h.getActiveObject();
            if (obj instanceof OAObject) {
                if (textField.getText().equals(prevValue)) {
                    if (tempChangedFlag) ((OAObject)obj).setChanged(false);
                    tempChangedFlag = false;
                }
                else {
                    if (!tempChangedFlag) {
                        if ( !((OAObject)obj).getChanged() ) {
                            tempChangedFlag = true;
                            ((OAObject)obj).setChanged(true);
                        }
                    }
                }
            }
        }
        */
    }

    
    private boolean bMousePressed;
    @Override
    public void mouseClicked(MouseEvent e) {
    	//bMousePressed = true;
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    	bMousePressed = false;
    }
    @Override
    public void mousePressed(MouseEvent e) {
    	bMousePressed = true;
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    	bMousePressed = false;
    }
}



