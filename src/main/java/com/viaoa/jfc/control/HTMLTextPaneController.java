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


import java.awt.event.*;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.editor.html.OAHTMLParser;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.object.OAObject;

/** class for binding Editor to Object or Hub. 
 * 
 *  NOTE: Use OAHTMLTextPaneController.bind(hub,prop) for more features.
 *  
 * */
public abstract class HTMLTextPaneController extends JFCController implements FocusListener {
    private OAHTMLTextPane editor;
    private Object activeObject;
    private Object focusActiveObject;
    private String undoDescription;
    private String prevText;
    private boolean bSettingText;
    private boolean bValueChangedWhileEditing;
    private int imageChangeCount;

    public HTMLTextPaneController(Hub hub, OAHTMLTextPane tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    
    private void create(OAHTMLTextPane ed) {
        if (editor != null) editor.removeFocusListener(this);
        editor = ed;

        if (editor == null) return; 
            
        editor.addFocusListener(this);
        afterChangeActiveObject(null);
        if (getActualHub() != null) {
            getEnabledController().add(getActualHub());
        }
    }

    private Class fieldClass;
    /**
     * Base/root class to use when inserting Field tags.
     * Set to non-OAObject class to to have it disabled.
     */
    public void setFieldClass(Class c) {
        this.fieldClass = c;
    }
    
    /**
     * Get the class to use for the list of Fields that a user can insert into a document.
     * If fieldClass was not set by calling setFieldClass(..), then it will look for 
     * a method named "get"+propertyPath+"FieldClass" from the current Hub's class.
     * If not found then it will use the Hub's class.
     * @return
     */
    public Class getFieldClass() {
        if (fieldClass != null) {
            if (OAObject.class.isAssignableFrom(fieldClass)) return fieldClass;
            return null; // not enabled
        }
        Hub h = getHub();
        if (h == null) return null;
        Object obj = h.getAO();
        if (obj == null) return null;
        Class c = h.getObjectClass();
        Method method = OAReflect.getMethod(c, "get"+propertyPath+"FieldClass");
        if (method != null) {
            Class[] cs = method.getParameterTypes();
            if (cs != null && cs.length > 0) return null;
            try {
                c = (Class) method.invoke(obj, null); 
            }
            catch (Exception e) {
            }
        }
        if (c == null) c = h.getObjectClass();
        if (!OAObject.class.isAssignableFrom(c)) return null;
        
        return c;
    }
    
    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (editor != null) create(editor);
    }
    
    public void close() {
        if (editor != null) editor.removeFocusListener(this);
        super.close();  // this will call hub.removeHubListener()
    }

    
    @Override
    public void afterPropertyChange(HubEvent e) {
        if (bSettingText || getActualHub() == null) return;
        
        if (e.getObject() == getActualHub().getActiveObject() && e.getPropertyName().equalsIgnoreCase(this.getHubListenerPropertyName()) ) {
            if (focusActiveObject != null) {
                bValueChangedWhileEditing = true;
            }
            else {
                update();
            }
        }
    }
    
    public @Override void afterChangeActiveObject(HubEvent e) {
        boolean b = (focusActiveObject != null && focusActiveObject == activeObject);
        if (b) onFocusLost();
        
        Hub h = getActualHub();
        if (h != null) activeObject = getActualHub().getActiveObject();
        else activeObject = null;
        imageChangeCount = editor.getImageChangeCount();
        
        update(); 
        
        if (b) onFocusGained();
    }
    @Override
    public void focusGained(FocusEvent e) {
        onFocusGained();
    }    
    protected void onFocusGained() {
        focusActiveObject = activeObject;
        bValueChangedWhileEditing = false;
        
        if (getActualHub() != null) {
            // 20110224 getText from property, since the text in the editor could have been
            //   changed by previous focus owner (ex: replace, or color chooser)
            prevText = null;
            if (activeObject != null) {
                prevText = OAConv.toString(getPropertyPathValue(activeObject));
                //was: prevText = ClassModifier.getPropertyValueAsString(activeObject, getGetMethod());
                if (prevText == null) prevText = getNullDescription();
            }
            if (prevText == null) {
                prevText = " ";
            }
        }        
        else {
            prevText = editor.getText();
            prevText = OAHTMLParser.removeBody(prevText); 
        }
    }
    @Override
    public void focusLost(FocusEvent e) {
        onFocusLost();
    }
    protected void onFocusLost() {
        if (focusActiveObject != null && focusActiveObject == activeObject) {
            saveText();
        }
        focusActiveObject = null;
    }
    


    
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
    
    
    boolean bSaving; // only used by saveChanges(), calling setText generates actionPerformed()
    public boolean saveText() {
        if (bSettingText) return true;
        if (bSaving) return true;
        boolean bResult = true;
        try {
            bSaving = true;
            bResult = _saveText();
        }
        finally {
            bSaving = false;
        }
        return bResult;
    }

    private boolean _saveText() {
        if (activeObject == null) return true;

        try {
            String newText = editor.getText();
            if (newText == null) newText = "";
            newText = OAHTMLParser.removeBody(newText);  // store inner (clean) html only
            
            boolean bChange = OAString.compare(newText, prevText) != 0 || (imageChangeCount != editor.getImageChangeCount()); 
            
            if (bValueChangedWhileEditing || bChange ) {

                if (bChange) {
                    String msg = validateNewValue(activeObject, newText);
                    if (msg != null) {
                        JOptionPane.showMessageDialog(SwingUtilities.getRoot(editor), 
                                "Invalid Entry\n"+msg,
                                "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
                
                bChange = true;
                boolean bSettext = false;
                if (bValueChangedWhileEditing) {
                    String currentValue = getPropertyPathValueAsString(activeObject, getFormat());
                    //was: String currentValue = ((OAObject)activeObject).getPropertyAsString(getPropertyName());
                    if (OAString.compare(prevText, currentValue) != 0) {
                        String hold = newText;
                        newText = getValueToUse(prevText, currentValue, newText);
                        newText = OAHTMLParser.removeBody(newText);
                        if (OAString.compare(newText, currentValue) == 0) {
                            bChange = false;
                            bSettext = true;
                        }
                        else {
                            bSettext = (OAString.compare(hold, newText) != 0);
                        }
                    }
                }
                if (bChange) {
                    String hold = prevText;
                    prevText = newText;
                    bSettingText = true;
                    OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, getPropertyPathFromActualHub(), hold, newText) );
                    setPropertyPathValue(activeObject, newText);
                    // ((OAObject)activeObject).setProperty(getPropertyName(), newText);
                }
                if (bSettext) editor.setText(newText);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(""+ex);
            return false;
        }
        finally {
            bSettingText = false;
            bValueChangedWhileEditing = false;            
        }
        return true;
    }
    
    // ?? not used qqqqqqqqqqq
    protected void changeEnabled(boolean b) {
        if (editor != null) editor.setEditable(b);
    }
    
    /**
     * This is called before the text is saved to a property, in cases where the data was changed by
     * another user while this user was editing.  This is not called if the user did not make any changes from the original value.
     * Note: if a property is being edited (hasFocus), then propertyChanges are ignored.
     * 
     * @param origValue value of the data when the editing started.
     * @param currentValue current property value
     * @param newValue new value entered by this user
     * @return returns the newValue
     */
    protected String getValueToUse(String origValue, String currentPropertyValue, String newValueFromThisUser) {
        return newValueFromThisUser;
    }


    @Override
    protected void update() {
        if (editor == null) return;
        if (focusActiveObject != null) return;

        if (getActualHub() != null) {
            String text = null;
            if (activeObject != null) {
                text = OAConv.toString(getPropertyPathValue(activeObject));
                // was: text = ClassModifier.getPropertyValueAsString(activeObject, getGetMethod());
                if (text == null)  text = getNullDescription();
            }
            if (text == null) {
                text = " ";
            }

            if (!SwingUtilities.isEventDispatchThread()) {
                final String _text = text;
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        _update(_text);
                    }
                });
            }
            else {
                _update(text);
            }
        }   
        super.update();
        super.update(editor, activeObject);
    }
    
    private void _update(String text) {
        boolean bHold = bSettingText;
        bSettingText = true;
        editor.setText(text);
        editor.setCaretPosition(0);
        prevText = text; // 20110112 to fix bug found while testing undo
        bSettingText = bHold;
    }
    
}
