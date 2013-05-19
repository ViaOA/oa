package com.dispatcher.editor;


import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;

import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.control.Hub2Gui;

/** class for binding Editor to Object or Hub. */
public class Hub2Editor extends Hub2Gui implements FocusListener {
    Editor editor;

    public Hub2Editor(Hub hub, Editor tf, String propertyPath) {
        super(hub, propertyPath, tf); // this will add hub listener
        create(tf);
    }


    private void create(Editor ed) {
        if (editor != null) editor.removeFocusListener(this);
        editor = ed;

        if (editor != null) editor.addFocusListener(this);

        if (getActualHub() != null) {
            HubEvent e = new HubEvent(getActualHub(),getActualHub().getActiveObject());
            this.hubChangeActiveObject(e);
        }
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
        super.afterChangeActiveObject(e);
        if (editor.hasFocus()) {
            focusLost(null);
        }
    }
 
 
    // HUB Events
    public void hubChangeActiveObject(HubEvent e) {
        if (getActualHub() == null) return;
        Object oaObject = getActualHub().getActiveObject();
        if (oaObject != null) {
            String s = ClassModifier.getPropertyValueAsString(oaObject, getGetMethod());

            editor.setText(s);
            editor.setCaretPosition(0);
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
    }

    public void hubPropertyChange(HubEvent e) {
        if (bUpdating || getActualHub() == null) return;
        if (e.getObject() == getActualHub().getActiveObject() && e.getPropertyName().equalsIgnoreCase(this.getPropertyName()) ) {

// 2004/07/16 this next line was commented out, with the 2nd line giving the reason.
//            I added bUpdating to handle this.
            hubChangeActiveObject(e);
// dont do this - focusLost gets called when color/font popup dialog are used and this would call setText(), and set cursor to end of document
        }
    }
    public void hubReplace(HubEvent e) {
        if (e.getObject() == getActualHub().getActiveObject()) {
            hubChangeActiveObject(e);
        }
    }
    public void hubAfterCancel(HubEvent e) {
        if (e.getObject() == getActualHub().getActiveObject()) {
            hubChangeActiveObject(e);
        }
    }
    public void hubInsert(HubEvent e) {
    }
    public void hubBeforeAdd(HubEvent e) {
    }
    public void hubAdd(HubEvent e) {
    }
    public void hubRemove(HubEvent e) {
    }
    public void hubNewList(HubEvent e) {
    }

    // Focus Events
    public void focusGained(FocusEvent e) {
    }

    private boolean bUpdating;
    public void focusLost(FocusEvent e) {
        if (getActualHub() == null) return;
        Object obj = getActualHub().getActiveObject();
        if (obj == null) return;

        try {
            /*
	            StringWriter sw = new StringWriter();
	            editor.editorKit.write(sw, editor.editorDocument, 0, editor.editorDocument.getLength());
	            sw.close();
	            String s = sw.toString();
            */
            String s = editor.getText();            
            s = HTMLParser.removeBody(s);
            bUpdating = true;
            ClassModifier.setPropertyValue(obj, getSetMethod(), s);
        }
		catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(""+ex);
		}
		bUpdating = false;
    }
}
