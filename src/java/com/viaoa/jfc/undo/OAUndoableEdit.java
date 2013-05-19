/* 
2003/10/21 created
2003/10/24 added setPresentationName()
2003/10/24 added get/setAllowReplace() so that an event can replace a previous matching event
2003/10/24 allowRedo() support

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
package com.viaoa.jfc.undo;

import java.util.*;
import javax.swing.undo.*;

import com.viaoa.object.*;
import com.viaoa.util.OAString;
import com.viaoa.hub.*;

/**
    Undoable for OA changes.

*/
public class OAUndoableEdit implements UndoableEdit {

    int type;
    Hub hub;
    String propertyName;
    Object prevValue, newValue;
    Object object;
    String presentationName;
    boolean bCanUndo = true;
    int prevPos, newPos;
    boolean bAllowReplace;
    boolean bAllowRedo=true;

    public static final int ADD = 0;
    public static final int REMOVE = 1;
    public static final int MOVE = 2;
    public static final int INSERT = 3;
    public static final int CHANGEAO = 4;
    public static final int PROPCHANGE = 5;
    public static final int PROPERTYCHANGE = 5;

    private OAUndoableEdit() {
    }

    
    public static OAUndoableEdit createUndoableAdd(String presentationName, Hub hub, Object obj) {
        OAUndoableEdit oe = new OAUndoableEdit();
        oe.type = ADD;
        oe.hub = hub;
        oe.object = obj;
        if (presentationName == null) {
            presentationName = "Add " + oe.getClassName();
        }
        oe.presentationName = presentationName;
        return oe;
    }
    
    public static OAUndoableEdit createUndoableChangeAO(String presentationName, Hub hub, Object prevObject, Object newObject) {
        OAUndoableEdit oe = new OAUndoableEdit();
        oe.type = CHANGEAO;
        oe.hub = hub;
        oe.newValue = newObject;
        oe.prevValue = prevObject;
        if (presentationName == null) {
            Class c = hub.getObjectClass();
            String s = OAString.convertHungarian(c.getSimpleName());
            
            Hub h2 = hub.getLinkHub();
            if (h2 != null) {
                c = h2.getObjectClass();
                s = OAString.convertHungarian(c.getSimpleName());
                String s2 = HubLinkDelegate.getLinkToProperty(hub);
                presentationName = "change to " + s + " " + s2;
            }
            else {
                presentationName = "change selected " + s;
            }
        }
        oe.presentationName = presentationName;
        return oe;
    }

    
    public static OAUndoableEdit createUndoableInsert(String presentationName, Hub hub, Object obj, int pos) {
        OAUndoableEdit oe = new OAUndoableEdit();
        oe.type = INSERT;
        oe.hub = hub;
        oe.object = obj;
        oe.newPos = pos;
        if (presentationName == null) {
            presentationName = "Insert " + oe.getClassName();
        }
        oe.presentationName = presentationName;
        return oe;
    }

    public static OAUndoableEdit createUndoableRemove(String presentationName, Hub hub, Object obj, int pos) {
        OAUndoableEdit oe = new OAUndoableEdit();
        oe.type = REMOVE;
        oe.hub = hub;
        oe.object = obj;
        oe.prevPos = pos;
        if (presentationName == null) {
            presentationName = "Remove " + oe.getClassName();
        }
        oe.presentationName = presentationName;
        return oe;
    }
    public static OAUndoableEdit createUndoableMove(String presentationName, Hub hub, int prevPos, int newPos) {
        OAUndoableEdit oe = new OAUndoableEdit();
        oe.type = MOVE;
        oe.hub = hub;
        oe.prevPos = prevPos;
        oe.newPos = newPos;
        if (presentationName == null) {
            presentationName = "Move " + oe.getClassName();
        }
        oe.presentationName = presentationName;
        return oe;
    }

    public static OAUndoableEdit createUndoablePropertyChange(String presentationName, Object obj, String prop, Object prevValue, Object newValue) {
        OAUndoableEdit oe = new OAUndoableEdit();
        oe.type = PROPCHANGE;
        oe.object = obj;
        oe.propertyName = prop;
        oe.prevValue = prevValue;
        oe.newValue = newValue;
        if (presentationName == null) {
            String s = oe.getClassName();
            s += " " + OAString.convertHungarian(prop);
            presentationName = "Change to " + s;
        }
        oe.presentationName = presentationName;
        return oe;
    }

    private String getClassName() {
        Class c = null;
        String s = null;
        if (object != null) c = object.getClass();
        else if (hub != null) c = hub.getObjectClass();
        if (c != null) {
            s = OAString.getClassName(c);
            s = OAString.convertHungarian(s);
        }
        return s;
    }
    

    public void setName(String name) {
        presentationName = name;
    }
    public String getName() {
        return presentationName;
    }
    public void setPresentationName(String name) {
        presentationName = name;
    }
    public String getPresentationName() {
        return presentationName;
    }

    public boolean canUndo() {
        return bCanUndo;
    }


    public void undo() throws CannotUndoException {
        bCanUndo = false;
        switch (type) {
            case ADD:  
                hub.remove(object);
                break;
            case REMOVE:
                hub.insert(object, prevPos);
                break;
            case MOVE:
                hub.move(newPos, prevPos);
                break;
            case INSERT:
                hub.remove(object);
                break;
            case CHANGEAO: 
                hub.setAO(prevValue);
                break;
            case PROPCHANGE:
                ((OAObject)object).setProperty(propertyName, prevValue);
                break;
        }
    }

    
    public void redo() throws CannotRedoException {
        bCanUndo = true;
        switch (type) {
            case ADD:  
                hub.add(object);
                break;
            case REMOVE:
                hub.remove(object);
                break;
            case MOVE:
                hub.move(prevPos, newPos);
                break;
            case INSERT:
                hub.insert(object, newPos);
                break;
            case CHANGEAO: 
                hub.setAO(newValue);
                break;
            case PROPCHANGE:
                ((OAObject)object).setProperty(propertyName, newValue);
                break;
        }
    }
    public boolean canRedo() {
        return !bCanUndo && bAllowRedo;
    }

    public String getUndoPresentationName() {
        return "Undo "+presentationName;
    }
    public String getRedoPresentationName() {
        return "Redo "+presentationName;
    }

    public boolean isSignificant() {
        return true;
    }

    public boolean addEdit(UndoableEdit anEdit) {
        return false;
    }
    public void die() {
    }
    public boolean replaceEdit(UndoableEdit anEdit) {
        return (anEdit != null && (anEdit instanceof OAUndoableEdit) && ((OAUndoableEdit)anEdit).bAllowReplace && this.equals(anEdit));
    }
    
    /** if true, an event can replace a previous matching event that is equals() */
    public void setAllowReplace(boolean b) {
        bAllowReplace = b;
    }
    public boolean getAllowReplace() {
        return bAllowReplace;
    }

    public void setAllowRedo(boolean b) {
        bAllowRedo = b;
    }
    public boolean getAllowRedo() {
        return bAllowRedo;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof OAUndoableEdit)) return false;
        OAUndoableEdit ue = (OAUndoableEdit) obj;
        if (this.type != ue.type) return false;
        if (object != ue.object) return false;
        /*
        if (this.presentationName != ue.presentationName) {
            if (this.presentationName == null || !this.presentationName.equals(ue.presentationName)) return false;
        }
        */
        if (this.propertyName != ue.propertyName) {
            if (this.propertyName == null || !this.propertyName.equals(ue.propertyName)) return false;
        }
        return true;
    }
}

