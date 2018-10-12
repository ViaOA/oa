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
public abstract class HTMLTextPaneController extends OAJfcController implements FocusListener {
    private OAHTMLTextPane editor;
    private Object activeObject;
    private Object focusActiveObject;
    private String prevText;
    private boolean bSettingText;
    private boolean bValueChangedWhileEditing;
    private int imageChangeCount;

    public HTMLTextPaneController(Hub hub, OAHTMLTextPane tf, String propertyPath) {
        super(hub, null, propertyPath, tf, HubChangeListener.Type.AoNotNull, false, true); // this will add hub listener
        create(tf);
    }

    private void create(OAHTMLTextPane ed) {
        if (editor != null) editor.removeFocusListener(this);
        editor = ed;

        if (editor == null) return; 
            
        editor.addFocusListener(this);
        afterChangeActiveObject();
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
        
        Method method = OAReflect.getMethod(c, "get"+endPropertyName+"TemplateRoot");
        if (method == null) {
            method = OAReflect.getMethod(c, "get"+endPropertyName+"FieldClass");
        }
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
    
    public String[] getCustomFields() {
        Hub h = getHub();
        if (h == null) return null;
        Object obj = h.getAO();
        if (obj == null) return null;
        Class c = h.getObjectClass();
        Method method = OAReflect.getMethod(c, "get"+endPropertyName+"TemplateFields");
        
        if (method == null) return null;
        
        Class[] cs = method.getParameterTypes();
        if (cs != null && cs.length > 0) return null;

        try {
            obj = method.invoke(obj, null); 
        }
        catch (Exception e) {
        }

        if (obj instanceof String[]) {
            return (String[]) obj;
        }
        
        return new String[0];
    }
    
    public void close() {
        if (editor != null) editor.removeFocusListener(this);
        super.close();  // this will call hub.removeHubListener()
    }

    
    @Override
    public void afterPropertyChange() {
        if (bSettingText) return;
        
        if (focusActiveObject != null) {
            bValueChangedWhileEditing = true;
        }
        else {
            update();
        }
    }
    
    public @Override void afterChangeActiveObject() {
        boolean b = (focusActiveObject != null && focusActiveObject == activeObject);
        if (b) onFocusLost();
        
        Hub h = hub;
        if (h != null) activeObject = hub.getActiveObject();
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
        
        if (hub != null) {
            // 20110224 getText from property, since the text in the editor could have been
            //   changed by previous focus owner (ex: replace, or color chooser)
            prevText = null;
            if (activeObject != null) {
                prevText = OAConv.toString(getValue(activeObject));
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
                    String msg = isValid(activeObject, newText);
                    if (msg != null) {
                        JOptionPane.showMessageDialog(SwingUtilities.getRoot(editor), 
                                "Invalid Entry\n"+msg,
                                "Invalid Entry", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    if (!confirmPropertyChange(activeObject, newText)) return false;
                }

                bChange = true;
                boolean bSettext = false;
                if (bValueChangedWhileEditing) {
                    String currentValue = getValueAsString(activeObject, getFormat());
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
                    if (getEnableUndo()) OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, endPropertyName, hold, newText) );
                    setValue(activeObject, newText);
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
     * @param currentPropertyValue current property value
     * @param newValueFromThisUser new value entered by this user
     * @return returns the newValue
     */
    protected String getValueToUse(String origValue, String currentPropertyValue, String newValueFromThisUser) {
        return newValueFromThisUser;
    }

    @Override
    public void update() {
        if (editor == null) return;
        if (focusActiveObject != null) return;

        String text = null;
        if (activeObject != null) {
            text = getValueAsString(activeObject, getFormat());
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
   
        super.update();
        super.update(editor, activeObject, true);
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
