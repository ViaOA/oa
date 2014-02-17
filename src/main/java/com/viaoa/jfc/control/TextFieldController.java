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
package com.viaoa.jfc.control;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;

import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.jfc.*;

/**
 * Controller for binding OA to JTextField.
 * @author vvia
 *
 */
public class TextFieldController extends JFCController implements FocusListener, ActionListener, KeyListener, MouseListener {
    protected JTextField textField;
    protected String prevText;
    private boolean bSettingText;
    private Object activeObject;
    private Object focusActiveObject;
    private int dataSourceMax=-2;
    private int max=-1;
    private OAPlainDocument document;
    protected char conversion;  // 'U'pper, 'L'ower, 'T'itle, 'P'assword
    
    /**
        Create an unbound TextField.
    */
    public TextFieldController(JTextField tf) {
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextFieldController(Hub hub, JTextField tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextFieldController(Object object, JTextField tf, String propertyPath) {
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
        if (actualHub == null) return; 
        
        Class c = OAReflect.getClass(getLastMethod());
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
            getEnabledController().add(getActualHub());
        }
        else {
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
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(TextFieldController.this.textField), msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
            @Override
            public void insertString(int offset, String str, AttributeSet attr)
                    throws BadLocationException {
                super.insertString(offset, str, attr);
            }
            @Override
            protected void insertUpdate(DefaultDocumentEvent chng,
                    AttributeSet attr) {
                super.insertUpdate(chng, attr);
            }
        };
        document.setMaxLength(getMax());
        textField.setDocument(document);
        
        
        
        c = OAReflect.getClass(getLastMethod());
        if (OAReflect.isNumber(c)) {
            
            final boolean bFloat = !OAReflect.isInteger(c);
            if (bFloat) {
                document.setValidChars("0123456789-. ");
            }
            else {
                document.setValidChars("0123456789- ");
            }

            
            // 20121101
            document.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    String s = "";
                    for (int i=0; i<string.length(); i++) {
                        char ch = string.charAt(i);
                        if (Character.isDigit(ch) || ch == '-' || (bFloat && ch == '.')) {
                            s += string.charAt(i);
                        }
                    }
                    if (s.length() > 0) fb.insertString(offset, s, attr);
                }
                @Override
                public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                    super.remove(fb, offset, length);
                }
                @Override
                public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                    String s = "";
                    for (int i=0; i<text.length(); i++) {
                        char ch = text.charAt(i);
                        if (Character.isDigit(ch) || ch == '-' || (bFloat && ch == '.')) {
                            s += text.charAt(i);
                        }
                    }
                    fb.replace(offset, length, s, attrs);
                }
            });
        }
    }



    public int getDataSourceMax() {
        if (dataSourceMax == -2) {
            if (hub != null) {
            	dataSourceMax = -1;
                OADataSource ds = OADataSource.getDataSource(actualHub.getObjectClass());
                if (ds != null) {
                    dataSourceMax = ds.getMaxLength(actualHub.getObjectClass(), getPropertyPathFromActualHub());
                    Method method = getLastMethod();
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
        getDataSourceMax();
        if (max < 0) {
            if (dataSourceMax >= 0) return dataSourceMax;
        }
        if (dataSourceMax > 0 && max > dataSourceMax) return dataSourceMax; 
        return max;
    }
    /** max length of text.  If -1 (default) then unlimited.  
    */
    public void setMax(int x) {
        max = x;
        max = getMax();  // verify with Datasource
    	if (document != null) document.setMaxLength(getMax());
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
            textField.removeMouseListener(this);
        }
        super.close();  // this will call hub.removeHubListener()
    }


    public @Override void afterPropertyChange(HubEvent e) {
        if (activeObject != null && e.getObject() == activeObject) {
            if (e.getPropertyName().equalsIgnoreCase(this.getHubListenerPropertyName()) ) {
                Object value = getPropertyValue(activeObject);
                if (value == null) prevText = "";
                else prevText = OAConv.toString(value, null);
                update();
            }
        }
    }

    public @Override void afterChangeActiveObject(HubEvent e) {
/*qqqqqqqqqq
if (textField instanceof OATextField && ((OATextField)textField).bTest) {
    int xx = 4;
    xx++;
}
*/
        boolean b = (focusActiveObject != null && focusActiveObject == activeObject);
        if (b) onFocusLost();
        
        Hub h = getActualHub();
        if (h != null) activeObject = getActualHub().getActiveObject();
        else activeObject = null;
        
        Object value = getPropertyValue(activeObject);
        if (value == null) prevText = "";
        else prevText = OAConv.toString(value, null);
        
        update(); 
        
        if (b) onFocusGained();
    }

    /**
     * 'U'pper, 'L'ower, 'T'itle, 'P'assword
     */
    public void setConversion(char conv) {
        conversion = conv;
    }
    public char getConversion() {
        return conversion;
    }
    
    
    @Override
    public void focusGained(FocusEvent e) {
        onFocusGained();

        if (textField instanceof OATextField) {
            OATextField tf = (OATextField) textField;
            if (tf.getTable() != null) return;  // OATextFieldTableCellEditor will handle this based on how the focus is gained - could be on keystroke from Table cell
            if (tf.getParent() instanceof JTable) return; 
        }
        
        if (!bMousePressed) {
            textField.selectAll();
        }
    }    
    protected void onFocusGained() {
        focusActiveObject = activeObject;

    	if (activeObject != null && !OAString.isEmpty(getFormat())) {
            // need to settext, without the formatting
    	    
            Object value = getPropertyValue(activeObject);

            // 201106076 make sure that text was not changed by keystroke from OATable
            //       compare the "last known" value set with the current textField.getText
            String ftext = OAConv.toString(value, getFormat());
            if (ftext == null) {
                ftext = getNullDescription();
                if (ftext == null) ftext = " ";
            }
            if (ftext.equals(textField.getText())) {  // otherwise, it was changed by OATable keystroke
                String text;
                if (value == null) text = "";
                else text = OAConv.toString(value, null); // dont include format, need raw/edit version

                if (text == null) {
                    text = getNullDescription();
                    if (text == null) text = "";
                }
                bSettingText = true;
                
                // 20110605 see if select all is current done
                int p1 = textField.getSelectionStart();
                int p2 = textField.getSelectionEnd();
                boolean b = p1 == 0 && text != null && p2 == text.length(); 
    
                textField.setText(text);
                
                if (b) {
                    textField.selectAll();
                }
                bSettingText = false;
            }
    	}
    	
    }
    
    @Override
    public void focusLost(FocusEvent e) {
        onFocusLost();
    }
    /**
        Saves changes to property in active object of Hub.
    */
    public void onFocusLost() {
        if (focusActiveObject != null && focusActiveObject == activeObject) {
            saveText();
        }
        focusActiveObject = null;
        update(); // will settext with formatting
    }

    
    // called when [Enter] is used
    @Override
    public void actionPerformed(ActionEvent e) {
        saveText();
        textField.selectAll(); // 20110606
    }

    private boolean bSaving; // only used by saveChanges(), calling setText generates actionPerformed()
    public void saveText() {
        if (bSettingText) return;
        if (bSaving) return;
        try {
            bSaving = true;
            _saveText();
        }
        finally {
            bSaving = false;
        }
    }

    private void _saveText() {
        /* 20101121  not valid when used in table, and arrow key to next column is used
        // 2006/06/14 bug with password always failing this while being used for a table
        if (!textField.isValid()) {
    	   if (!(textField instanceof OAPasswordField)) return;
        }
        */
        
        if (activeObject == null) return;
        String text = textField.getText();
        if (text.equals(prevText)) return;
        
        if (text != null && conversion != 0) {
            String hold = text;
            if (conversion == 'U' || conversion == 'u') {
                text = text.toUpperCase();
            }
            else if (conversion == 'L' || conversion == 'l') {
                text = text.toLowerCase();
            }
            else if (conversion == 'T' || conversion == 't') {
                if (text.toLowerCase().equals(text) || text.toUpperCase().equals(text)) {
                    text = OAString.toTitleCase(text);
                }
            }
            else if (conversion == 'P' || conversion == 'p') {
                text = OAString.getSHAHash(text);
            }
            if (hold != text) textField.setText(text);
        }
        
        try {
            Object convertedValue = getConvertedValue(text, null); // dont include format - it is for display only
            // Object convertedValue = OAReflect.convertParameterFromString(getSetMethod(), text, null); // dont include format - it is for display only
            
            if (convertedValue == null && text.length() > 0) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                        "Invalid Entry \""+text+"\"", 
                        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String msg = null;
            OAEditMessage em = new OAEditMessage();
            boolean b = isValid(activeObject, convertedValue, em);
            if (!b) {
                msg = em.getMessage();
                if (msg == null) msg = "";
                if (em.getThrowable() != null) {
                    if (msg.length() > 0) msg += "\nError: ";
                    msg += em.getThrowable().toString();
                }
            }
            
            if (msg != null) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                        "Invalid Entry \""+text+"\"\n"+msg,
                        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }

            prevText = text;
            Object prevValue = getPropertyPathValue(activeObject);
            
            String prop = getHubListenerPropertyName();
            if (prop == null || prop.length() == 0) {  // use object.  (ex: String.class)
                Object oldObj = activeObject;
                Hub h = getActualHub();
                Object newObj = OAReflect.convertParameterFromString(h.getObjectClass(), text);
                if (newObj != null) {
                    int posx = h.getPos(oldObj);
                    h.remove(posx);
                    h.insert(newObj, posx);
                }
            }
            else {
                setPropertyPathValue(activeObject, convertedValue);
                // OAReflect.setPropertyValue(activeObject, getSetMethod(), convertedValue);
                if (text == null || text.length() == 0) {
                    Class c = getLastMethod().getReturnType();
                    if (OAReflect.isNumber(c) && activeObject instanceof OAObject) {
                    	OAObjectReflectDelegate.setProperty((OAObject)activeObject, getHubListenerPropertyName(), null, null);  // was: setNull(prop)
                    }
                }
            }
            OAUndoableEdit ue = OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, getPropertyPathFromActualHub(), prevValue, getPropertyPathValue(activeObject) );
            OAUndoManager.add(ue);
        }
        catch (Throwable t) {
            System.out.println("Error in TextFieldController, "+t);
            t.printStackTrace();
            String msg = t.getMessage();
            for (;;) {
                t = t.getCause();
                if (t == null) break;
                msg = t.getMessage();
            }
            
            
        	JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
        	        "Invalid Entry \""+msg+"\"", 
        	        "Invalid Entry", JOptionPane.ERROR_MESSAGE);
        }
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
            if (!textField.getText().equals(prevText)) {
        		bConsumeEsc = true;
            	e.consume();
	    		textField.setText(prevText);
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
    
    private boolean bAllowChangesWhileFocused;
    public void setAllowChangesWhileFocused() {
        // set by OAComboBox.setEditor(OATextField) so that user can select combo item while textField is focused
        bAllowChangesWhileFocused = true;
    }
    
    @Override
    protected void update() {
        if (textField == null) return;
        if (focusActiveObject == null || bAllowChangesWhileFocused) {
            if (getActualHub() != null) {
                String text = null;
                if (activeObject != null) {
                    Object value = getPropertyValue(activeObject);
                    if (value == null) text = "";
                    else text = OAConv.toString(value, getFormat());
                }
                if (text == null) {
                    text = getNullDescription();
                    if (text == null) text = " ";
                }
                boolean bHold = bSettingText;
                bSettingText = true;

                // 20110605 see if select all is currently done
                int p1 = textField.getSelectionStart();
                int p2 = textField.getSelectionEnd();
                boolean b = p1 == 0 && text != null && p2 == text.length(); 
                
                textField.setText(text);
                prevText = text; // 20110112 to fix bug found while testing undo
                
                if (b) {
                    textField.selectAll();
                }
                
                bSettingText = bHold;
            }   
        }        
        super.update();
        super.update(textField, activeObject);
    }
    
    protected Object getPropertyValue(Object obj) {
        if (obj == null) return null;
        if (getPropertyPath() == null) return null;
        Object value = getPropertyPathValue(obj);
        if (value instanceof OANullObject) value = null;
        
        if (value != null && obj instanceof OAObject) {
            if (isPropertyPathValueNull(obj)) value = null;
            //was: String ss = getPropertyName();
            //was: if (OAObjectReflectDelegate.getPrimitiveNull((OAObject)obj, ss) ) value = null;
        }
        
        return value;
    }
    
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return label;
    }
    
}



