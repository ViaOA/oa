package com.viaoa.jfc.control;


import java.awt.event.*;

import javax.swing.SwingUtilities;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.control.Hub2Gui;
import com.viaoa.jfc.editor.html.OAHTMLParser;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.object.OAObject;

/** class for binding Editor to Object or Hub. */
public class Hub2HTMLTextPane extends Hub2Gui implements FocusListener {
    private OAHTMLTextPane editor;
    private Object activeObject;
    private String undoDescription;

    public Hub2HTMLTextPane(Hub hub, OAHTMLTextPane tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }

    private void create(OAHTMLTextPane ed) {
        if (editor != null) editor.removeFocusListener(this);
        editor = ed;

        if (editor == null) return; 
            
        editor.addFocusListener(this);
        display();
    }
    
    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (editor != null) create(editor);
    }
    
    public void close() {
        if (editor != null) editor.removeFocusListener(this);
        super.close();  // this will call hub.removeHubListener()
    }

    public @Override void afterChangeActiveObject(HubEvent e) {
        if (SwingUtilities.isEventDispatchThread()) {
            saveChanges();
            display();
        }
        else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        saveChanges();
                        display();
                    }
                });
            }
            catch (Exception ex) {}
        }
    }
    
    // make sure that this is caled in swingEDT
    protected void display() {
        activeObject = null;
        if (getActualHub() != null) {
            activeObject = getActualHub().getActiveObject();
        }

        if (activeObject != null) {
            String s = ClassModifier.getPropertyValueAsString(activeObject, getGetMethod());

            editor.setText(s);
            editor.setCaretPosition(0);
            editor.setEditable(true);
            

            /*****
			StringReader sr = new StringReader(s);
			editor.editorDocument = (EditorDocument) editor.editorKit.createDocument();
            try {
    			editor.editorKit.read(sr, editor.editorDocument, 0);
				editor.setDocument(editor.editorDocument);

                File file = new File("XXX");
                s = file.getAbsolutePath();
                file = new File(s);
                file = new File(file.getParent());
                URL url = file.toURL();
                editor.editorDocument.setBase(url);
			}
			catch (Exception ex) {
			    ex.printStackTrace();
			    System.out.println(""+ex);
			}
			sr.close();
            *****/       
        }
        else {
            editor.setText("");
            editor.setEditable(false);
        }
    }
    
    private boolean bValueChangedWhileEditing;
    
    @Override
    public void afterPropertyChange(HubEvent e) {
        if (bUpdating || getActualHub() == null) return;
        
        
        if (editor.hasFocus()) {
            // 20101015
            bValueChangedWhileEditing = true;
            onValueChangedWhileEditing();
            return; // dont change while user is editing.
        }
        
        if (e.getObject() == getActualHub().getActiveObject() && e.getPropertyName().equalsIgnoreCase(this.getPropertyName()) ) {
            if (SwingUtilities.isEventDispatchThread()) {
                display();
            }
            else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        display();
                    }
                });
            }
        }
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
    
    private String prevValue;
    private boolean bHadFocus = false;
    // Focus Events
    public void focusGained(FocusEvent e) {
        bHadFocus = true;
        bValueChangedWhileEditing = false;
        prevValue = editor.getText();
    }

    private boolean bUpdating;
    public void focusLost(FocusEvent e) {
        saveChanges();
        bHadFocus = false; 
    }
    
    protected void saveChanges() {
        if (activeObject == null) return;
        if (!bHadFocus) return;
        bHadFocus = false;
        try {
            prevValue = OAHTMLParser.removeBody(prevValue);
            
            String s = editor.getText();
            if (s == null) s = "";
            s = OAHTMLParser.removeBody(s);  // store inner html only
/*qqqqqqqqqqqqqqqqqq Test only            
System.out.println("Hub2HTMLTextPane.saveChanges() has test code in it qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq");            
bValueChangedWhileEditing=true;
prevValue += "QQ";
*/
            if (OAString.compare(s, prevValue) != 0) {
                boolean bChange = true;
                boolean bSettext = false;
                if (bValueChangedWhileEditing) {
                    Object current = ((OAObject)activeObject).getPropertyAsString(getPropertyName());
                    if (current instanceof String && OAString.compare(prevValue, (String)current) != 0) {
                        String hold = s;
                        s = getValueToUse(prevValue, (String) current, s);
                        s = OAHTMLParser.removeBody(s);
                        if (OAString.compare(s, (String) current) == 0) {
                            bChange = false;
                            bSettext = true;
                        }
                        else {
                            bSettext = (OAString.compare(hold, s) != 0);
                        }
                    }
                }
                if (bChange) {
                    bUpdating = true;
                    OAUndoManager.add(OAUndoableEdit.createUndoablePropertyChange(undoDescription, activeObject, getPropertyName(), prevValue, s) );
                    ((OAObject)activeObject).setProperty(getPropertyName(), s);
                }
                if (bSettext) editor.setText(s);
            }
            //ClassModifier.setPropertyValue(activeObject, getSetMethod(), s);
        }
        catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(""+ex);
        }
        finally {
            bUpdating = false;
            bValueChangedWhileEditing = false;            
        }
    }

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
    /**
     * Called by afterChangeProperty, if another user has changed the value that this user is currently editing (hasFocus)
     */
    protected void onValueChangedWhileEditing() {
    }

}
