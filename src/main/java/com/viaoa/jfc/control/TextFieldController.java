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
package com.viaoa.jfc.control;

import java.awt.Component;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import com.viaoa.hub.*;
import com.viaoa.jfc.undo.*;
import com.viaoa.object.OAObjectInfo;
import com.viaoa.object.OAObjectInfoDelegate;
import com.viaoa.object.OAPropertyInfo;
import com.viaoa.util.*;
import com.viaoa.jfc.*;

/**
 * Controller for binding OA to JTextField.
 * @author vvia
 *
 */
public class TextFieldController extends OAJfcController implements FocusListener, ActionListener, KeyListener, MouseListener {
    private static Logger LOG = Logger.getLogger(TextFieldController.class.getName());
    protected JTextField textField;
    protected volatile String prevText;
    private final AtomicInteger aiIgnoreSetText = new AtomicInteger();
    private Object activeObject;
    private Object focusActiveObject;
    private OAPlainDocument document;
    private boolean bConsumeEsc;
    
    /**
     * 'U'ppercase, 
     * 'L'owercase, 
     * 'T'itle, 
     * 'J'ava identifier
     * 'E'ncrpted password/encrypt
     * 'S'HA password
     */
    protected char conversion;

    public TextFieldController(JTextField tf) {
        super(null, null, tf, HubChangeListener.Type.Unknown); // this will add hub listener
        create(tf);
    }
    
    
    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextFieldController(Hub hub, JTextField tf, String propertyPath) {
        super(hub, propertyPath, tf, HubChangeListener.Type.AoNotNull); // this will add hub listener
        create(tf);
    }

    /**
        Create TextField that is bound to a property path in a Hub.
        @param propertyPath path from Hub, used to find bound property.
    */
    public TextFieldController(Object object, JTextField tf, String propertyPath) {
        super(object, propertyPath, tf, HubChangeListener.Type.AoNotNull); // this will add hub listener
        create(tf);
    }
    
    @Override
    protected void reset() {
        super.reset();
        if (textField != null) create(textField);
    }
    
    protected void create(JTextField tf) {
        if (textField != null) {
            textField.removeFocusListener(this);
            textField.removeKeyListener(this);
            textField.removeActionListener(this);
            textField.removeMouseListener(this);
        }
        textField = tf;
        if (hub == null) return; 
        
        if (OAReflect.isNumber(endPropertyClass)) {
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
        if (hub != null) {
            this.afterChangeActiveObject();
        }
        else {
            aiIgnoreSetText.incrementAndGet();
            if (document != null) document.setAllowAll(true);
            if (tf != null) {
                if (tf instanceof OATextField) ((OATextField)tf).setText("", false);
                else tf.setText("");
            }
            if (document != null) document.setAllowAll(false);
            aiIgnoreSetText.decrementAndGet();
        }

        document = new OAPlainDocument() {
            public void handleError(int errorType) {
            	super.handleError(errorType);
            	if (!TextFieldController.this.textField.hasFocus()) return;
            	String msg = "";
            	switch (errorType) {
            	case OAPlainDocument.ERROR_MAX_LENGTH:
        	        int max = getMaximumColumns();
        	        if (max <= 0) max = getPropertyInfoMaxColumns();
            		msg = "Maximum input exceeded, currently set to " + max;

            		if (textField instanceof OATextField) {
            		    msg += " for " + ((OATextField)textField).getEndPropertyName();
            		    Hub h = ((OATextField)textField).getHub();
            		    if (h != null) msg += ", in "+OAString.getDisplayName(h.getObjectClass().getSimpleName());
            		}
            		
            		break;
            	case OAPlainDocument.ERROR_INVALID_CHAR:
            		return;
            	}
            	if (!OAString.isEmpty(msg)) LOG.warning(msg);
                if (textField != null && textField.hasFocus()) {
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(TextFieldController.this.textField), msg, "Error", JOptionPane.ERROR_MESSAGE);
                }
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
        
        int max = getPropertyInfoMaxColumns();
        // if (max <= 0) max = getDataSourceMaxColumns();  // use getOAColumn max instead 
        
        if (max > 0) document.setMaxLength(max);
        textField.setDocument(document);
        
        if (OAReflect.isNumber(endPropertyClass)) {
            final boolean bFloat = !OAReflect.isInteger(endPropertyClass);
            
            OAObjectInfo oi = OAObjectInfoDelegate.getOAObjectInfo(getHub().getObjectClass());
            OAPropertyInfo pi = oi.getPropertyInfo(endPropertyName);
            
            final boolean isCurrency = pi != null && pi.isCurrency(); 
            String s = "";
            if (isCurrency) s = "$"; 
            
            if (bFloat) {
                document.setValidChars("0123456789-. "+s);
            }
            else {
                document.setValidChars("0123456789- "+s);
            }

            document.setDocumentFilter(new DocumentFilter() {
                @Override
                public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                    String s = "";
                    for (int i=0; i<string.length(); i++) {
                        char ch = string.charAt(i);
                        if (Character.isDigit(ch) || ch == '-' || (bFloat && ch == '.') || ch == ',') {
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
                        if (Character.isDigit(ch) || ch == '-' || (bFloat && ch == '.') || ch == ',' || (isCurrency && (ch == '$' || ch == ' '))) {
                            s += text.charAt(i);
                        }
                    }
                    fb.replace(offset, length, s, attrs);
                }
            });
        }
    }

    public void setMax(int x) {
        setMaximumColumns(x);
    }
    public void setMaximumColumns(int x) {
        super.setMaximumColumns(x);
        if (document != null) {
            document.setMaxLength(x);
        }
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


    public @Override void afterPropertyChange() {
        update();  // will update prevText if it is activeObject
    }

    public void onAddNotify() {
        focusActiveObject = null;
        afterChangeActiveObject();
    }
    
    public @Override void afterChangeActiveObject() {
        boolean bHasFocus = (focusActiveObject != null && focusActiveObject == activeObject);
        if (bHasFocus) onFocusLost();
        
        if (hub != null) activeObject = hub.getActiveObject();
        else activeObject = null;
        
        if (!bHasFocus) {
            prevText = getFormattedTextValue(activeObject);
            try {
                aiIgnoreSetText.incrementAndGet();
                textField.setText(prevText);
            }
            finally {
                aiIgnoreSetText.decrementAndGet();
            }
        }
        
        if (bHasFocus) onFocusGained();
        super.afterChangeActiveObject();
    }

    /**
     * 'U'ppercase, 
     * 'L'owercase, 
     * 'T'itle, 
     * 'J'ava identifier
     * 'E'ncrpted password/encrypt
     * 'S'HA password  (one way hash)
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
    	if (activeObject == null) return;
    	if (OAString.isEmpty(getFormat())) return;
    	    
        Object value = getValue(activeObject);

        // make sure that text was not changed by keystroke from OATable
        //       compare the "last known" value set with the current textField.getText
        String ftext = getFormattedTextValue(value);
        
        if (ftext.equals(textField.getText())) {  // otherwise, it was changed by OATable keystroke
            String text = ftext; 
            if (value == null) text = "";

            aiIgnoreSetText.incrementAndGet();
            
            // see if select all is currently done
            int p1 = textField.getSelectionStart();
            int p2 = textField.getSelectionEnd();
            boolean b = p1 == 0 && text != null && p2 == text.length(); 

            textField.setText(text);
            
            if (b) {
                textField.selectAll();
            }
            aiIgnoreSetText.decrementAndGet();
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
        update();
    }

    
    // called when [Enter] is used
    @Override
    public void actionPerformed(ActionEvent e) {
        saveText();
        textField.selectAll();
    }

    private boolean bSaving; // only used by saveChanges(), calling setText generates actionPerformed()
    public void saveText() {
        if (aiIgnoreSetText.get() > 0) return;
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
            else if (conversion == 'J' || conversion == 'j') {
                text = OAString.makeJavaIndentifier(text);
            }
            else if (conversion == 'S' || conversion == 's') {
                text = OAString.getSHAHash(text);
            }
            else if (conversion == 'P' || conversion == 'p') {
                text = OAString.getSHAHash(text);
            }
            else if (conversion == 'E' || conversion == 'e') {
                try {
                    text = OAEncryption.encrypt(text);
                }
                catch (Exception e) {
                    throw new RuntimeException("encryption failed", e);
                }
            }

            if (text.equals(prevText)) return;
            if (hold != text) textField.setText(text);
        }
        
        try {

            Object convertedValue;
            if (OAString.isEmpty(text) && endPropertyClass != null && endPropertyClass.isPrimitive()) {
                convertedValue = null;
            }
            else {
                convertedValue = getConvertedValue(text, null);
                if (convertedValue == null && text.length() > 0) {
                    JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                            "Invalid Entry \""+text+"\"", 
                            "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            String msg = isValid(activeObject, convertedValue);
            if (msg != null) {
                JOptionPane.showMessageDialog(SwingUtilities.getRoot(textField), 
                    "Invalid Entry \""+text+"\"\n"+msg,
                    "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!confirm(activeObject, convertedValue)) return;

            prevText = text;
            Object prevValue = getValue(activeObject);
            
            if (endPropertyName == null || endPropertyName.length() == 0) {  // use object.  (ex: String.class)
                Object oldObj = activeObject;
                Object newObj = OAReflect.convertParameterFromString(hub.getObjectClass(), text);
                if (newObj != null) {
                    int posx = hub.getPos(oldObj);
                    hub.remove(posx);
                    hub.insert(newObj, posx);
                }
            }
            else {
                setValue(activeObject, convertedValue, null);
            }

            if (getEnableUndo()) {
                OAUndoableEdit ue = OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, endPropertyName, prevValue, getValue(activeObject) );
                OAUndoManager.add(ue);
            }
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

    
    // Key Events
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
    public void update() {
        if (textField == null) return;
        if (focusActiveObject == null || bAllowChangesWhileFocused) {
            Object obj;
            if (hub != null) obj = hub.getActiveObject();
            else obj = null;
            
            String text = getFormattedTextValue(obj);
            aiIgnoreSetText.incrementAndGet();
            try {
                // see if select all is currently done
                int p1 = textField.getSelectionStart();
                int p2 = textField.getSelectionEnd();
                boolean b = p1 == 0 && text != null && p2 == text.length(); 
                
                textField.setText(text);
                prevText = text; // 20110112 to fix bug found while testing undo
                
                if (b) {
                    textField.selectAll();
                }
            }
            finally {
                aiIgnoreSetText.decrementAndGet();
            }
        }        
        super.update();
    }
    
    protected String getFormattedTextValue(Object obj) {
        String text = null;
        if (obj != null) {
            Object value = getValue(obj);
            if (value == null) text = null;
            else text = OAConv.toString(value, getFormat());
        }
        if (text == null) {
            text = getNullDescription();
            if (text == null) text = " ";
        }
        return text;
    }
    
    @Override
    public Component getTableRenderer(JLabel label, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableRenderer(label, table, value, isSelected, hasFocus, row, column);
        return label;
    }
}

