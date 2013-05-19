package com.viaoa.jfc.control;

import java.awt.event.*;

import javax.swing.*;

import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;

public class Hub2FormattedTextField extends Hub2Gui implements FocusListener, KeyListener, MouseListener {
    OAFormattedTextField textField;
    String prevValue;
    private boolean bSettingText;
    private Object activeObject;
    
    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public Hub2FormattedTextField(Hub hub, OAFormattedTextField tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public Hub2FormattedTextField(Object object, OAFormattedTextField tf, String propertyPath) {
        super(object, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    protected void create(OAFormattedTextField tf) {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeMouseListener(this);
        }
        textField = tf;
        if (actualHub == null) return;

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
            textField.addMouseListener(this);
        }
        // set initial value of textField
        // this needs to run before listeners are added
        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.afterChangeActiveObject(e);
        }
    }

    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (textField != null) create(textField);
    }

    public void close() {
        if (textField != null) {
            textField.removeKeyListener(this);
            textField.removeFocusListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }

    
    // HUB Events
    public @Override void afterChangeActiveObject(HubEvent e) {
        if (textField.hasFocus()) {
            try {
                textField.commitEdit();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("Exception: "+ex);
            }
            saveChanges(textField.getValue().toString());
        }
        if (getActualHub() == null) return;
        
        activeObject = getActualHub().getActiveObject();

        String hcas;
        if (activeObject != null) {
            setInternalEnabled(isEnabled(textField, activeObject)); 
            hcas = OAReflect.getPropertyValueAsString(activeObject, getGetMethod(), getFormat(), "");
            if ((activeObject instanceof OAObject) && OAObjectReflectDelegate.getPrimitiveNull((OAObject)activeObject, getPropertyName()) ) hcas = "";
        }
        else {
            hcas = "";
            setInternalEnabled(false);
        }

        prevValue = hcas;
        bSettingText = true;

        super.updateComponent(textField, activeObject, hcas);

        bSettingText = false;
        if (!getReadOnly())  hcas = "";
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
    	
		if (textField.getTable() != null) return;  // OATextFieldTableCellEditor will handle this based on how the focus is gained - could be on keystroke from Table cell
		if (textField.getParent() instanceof JTable) return;  // ""

		if (!bMousePressed) {
			textField.selectAll();
		}
    }

    /**
        Saves changes to property in active object of Hub.
    */
    public void focusLost(FocusEvent e) {
        //qqq not needed?  saveChanges();
    }

    @Override
    public void setReadOnly(boolean b) {
        super.setReadOnly(b);
        setEnabled(!b);
        afterChangeActiveObject(null);
    }

    public void actionPerformed(ActionEvent e) {
        //qq not needed? if (textField.hasFocus()) saveChanges();
    }

    boolean bChanging; // only used by saveChanges(), calling setText generates actionPerformed()
    public void saveChanges(final String text) {
        if (bSettingText) return;
        if (bChanging) return;
        try {
            bChanging = true;
            _saveChanges(text);
        }
        finally {
            bChanging = false;
        }
    }
    private void _saveChanges(String text) {
        if (activeObject == null) return;

        int i,x;
        String s = text;

        if (s == null || !s.equals(prevValue)) {
            if (!getReadOnly()) {
                try {
                    Object prev = OAReflect.getPropertyValue(activeObject, getGetMethod());

                    Object convertedValue = OAReflect.convertParameterFromString(getSetMethod(), s, getFormat());

                    Method m = getValidMethod();
                    if (m != null) {
                        OAEditMessage oamsg = new OAEditMessage();
                        Object[] objs = new Object[] { convertedValue, oamsg };
                        Boolean B = (Boolean) m.invoke(activeObject, objs);
                        if (B != null && !B.booleanValue()) {
                            String msg = oamsg.getMessage();
                            if (msg == null || msg.length() == 0) s = "Invalid entry \""+s+"\"for "+getPropertyName();
                            JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), msg, "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                            textField.setValue(prevValue);
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
                    OAUndoManager.add( OAUndoableEdit.createUndoablePropertyChange("change "+getPropertyName(), activeObject, getPropertyName(), prev, OAReflect.getPropertyValue(activeObject, getGetMethod())) );
                }
                catch (Exception e) {
                	JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(textField), "Invalid Entry \""+e.getMessage()+"\"", "", JOptionPane.ERROR_MESSAGE);
                    // throw new OAException("Hub2TextField saveChanges() exception invoking method="+ getSetMethod().getName()+" class="+this.getActualHub().getObjectClass().getName()+" "+e );
                }

                String s2 = OAReflect.getPropertyValueAsString(activeObject, getGetMethod(), getFormat(), "");

                if (!s.equals(s2)) {
                    s = s2;
                    bSettingText = true;
                    textField.setValue(s);
                    bSettingText = false;
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
                    textField.setValue(prevValue); // should not happen since it will be disabled
                    bSettingText = false;
                }
            }
            prevValue = s;
        }
    }


    // Key Events
    private boolean bConsumeEsc;
    @Override
    public void keyPressed(KeyEvent e) {
    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        	bConsumeEsc = false;
            if (!textField.getValue().equals(prevValue)) {
        		bConsumeEsc = true;
            	e.consume();
	    		textField.setValue(prevValue);
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



