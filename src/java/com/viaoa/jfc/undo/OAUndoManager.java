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
package com.viaoa.jfc.undo;

import java.util.*;
import java.util.logging.Logger;

import javax.swing.undo.*;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;


/** Undo Support for OA.gui components.
   @see OAUndoableEvent
   @see #createUndoManager
   @see UndoableController
*/
public class OAUndoManager extends UndoManager {

    private static Logger LOG = Logger.getLogger(OAUndoManager.class.getName());
    
    protected static Hashtable hash = new Hashtable();  // key=thread
    protected static OAUndoManager undoManager;
    protected static boolean bVerbose;
    protected static boolean bIgnoreAll;
    private static CompoundEdit compoundEdit;
    private static UndoableEdit lastEdit;

    /** 
        @see OAUndoManager#createUndoManager
    */
    protected OAUndoManager() {
        
    }

    public static OAUndoManager createUndoManager() {
        if (undoManager == null) undoManager = new OAUndoManager();
        return undoManager;
    }

    public static OAUndoManager getUndoManager() {
        return undoManager;
    }

    public static void setVerbose(boolean b) {
        bVerbose = b;
    }
    public static boolean getVerbose() {
        return bVerbose;
    }


    /**
        Used to group more then one undoable edit into one undoable edit.
        @param presentationName name to display for compound edit.
    */
    public static void startCompoundEdit() {
        startCompoundEdit("", true);
    }
    public static void startCompoundEdit(final String presentationName) {
        startCompoundEdit(presentationName, true);
    }
    
    /**
     * All OAObject property changes will be captured into an Undoable.
     * This creates a compoundEdit, calls oaThreadLocalDelegate.setCreateUndoablePropertyChanges,
     * which is used by OAObject.firePropertyChange to add undoableEdits using UndoableEdit.createUndoablePropertyChange
     */
    public static void startCompoundEditForPropertyChanges(final String presentationName) {
        startCompoundEdit(presentationName);
        OAThreadLocalDelegate.setCreateUndoablePropertyChanges(true);
    }    
    public static void endCompoundEditForPropertyChanges() {
        endCompoundEdit();
        OAThreadLocalDelegate.setCreateUndoablePropertyChanges(false);
    }    
    
    public static void startCompoundEdit(final String presentationName, final boolean bCanRedoThis) {
        if (getIgnore()) return;
        if (undoManager == null) throw new RuntimeException("createUndoManager() must be called first");
        if (compoundEdit != null) {
            LOG.warning("compoundEdit is not null, presentationName="+compoundEdit.getPresentationName()+", will end before starting this new compoundEdit="+presentationName);
            endCompoundEdit();
        }
        
        compoundEdit = new CompoundEdit() {
            public String getPresentationName() {
                return presentationName;
            }
            @Override
            public String getUndoPresentationName() {
                return "Undo " + presentationName;
            }
            @Override
            public String getRedoPresentationName() {
                return "Redo " + presentationName;
            }
            @Override
            public boolean canRedo() {
                return bCanRedoThis;
            }
        };
    }
    public static void endCompoundEdit() {
        if (getIgnore()) return;
        if (undoManager == null || compoundEdit == null) return;
        compoundEdit.end();
        undoManager.addEdit(compoundEdit);
        compoundEdit = null;
    }
    public static boolean isInCompoundEdit() {
        if (getIgnore()) return false;
        return (undoManager != null && compoundEdit != null);
    }

    public static void cancelCompoundEdit() {
        compoundEdit = null;
    }

    public static void add(UndoableEdit anEdit) {
        if (anEdit == null || undoManager == null) return;
        undoManager.addEdit(anEdit);
    }    

/**qqqqqqq 20100124 not used?    
    public static void add(UndoableEdit anEdit, boolean bIgnoreDuplicate) {
        if (anEdit == null || undoManager == null) return;
        if (bIgnoreDuplicate && anEdit.equals(lastEdit)) return;
        lastEdit = anEdit;
        undoManager.addEdit(anEdit);
    }    
**/
    
    public static void add(UndoableEdit[] anEdits) {
        if (getIgnore()) return;
        if (anEdits != null && undoManager != null && anEdits.length > 0) {
            if (compoundEdit != null) {
                for (int i=0; i<anEdits.length; i++) {
                    undoManager.compoundEdit.addEdit(anEdits[i]);
                }
            }
            else {
                CompoundEdit ce = new CompoundEdit();
                for (int i=0; i<anEdits.length; i++) {
                    ce.addEdit(anEdits[i]);
                }
                ce.end();
                undoManager.addEdit(ce);
            }
        }
    }    
    
    public synchronized boolean addEdit(UndoableEdit anEdit) {
        if (getIgnore()) return false;
        if (bVerbose) System.out.println("OAUndoManager.addEdit "+anEdit.getPresentationName());

        if (compoundEdit != null && anEdit != compoundEdit) {
            compoundEdit.addEdit(anEdit);
            return true;
        }
        return super.addEdit(anEdit);
    }


    /**
        Increment/Deincrement ignore counter for current thread
    */
    public static void setIgnore(boolean b) {
        setIgnore(b, false);
    }
    /**
        @param bResetToZero reset counter to zero before performing setting ignore counter
    */
    public static void setIgnore(boolean b, boolean bResetToZero) {
        if (undoManager != null) {
            int i = 0;
            Thread t = Thread.currentThread();
            if (!bResetToZero) {
                Integer ii = (Integer) hash.get(t);
                if (ii != null) i = ii.intValue();
            }            
            if (b) i++;
            else i--;
            
            if (i>0) hash.put(t, new Integer(i));
            else hash.remove(t);
        }
    }
    public static void ignore() {
        setIgnore(true);
    }

    /** same as calling setIgnore(true)
    */
    public static void ignore(boolean b) {
        setIgnore(b);
    }

    /** 
        @returns true if counter for current thread > 0, or if OAUndoManager is null or thread is OAClient.isClientThread()
    */
    public static boolean getIgnore() {
        if (undoManager == null) return true;
        if (bIgnoreAll) return true;
        
        int i = 0;
        Thread t = Thread.currentThread();
        Integer ii = (Integer) hash.get(t);
        if (ii != null) i = ii.intValue();
        if (i > 0) return true;

        if (!OASyncDelegate.isSingleUser()) {
            if (OARemoteThreadDelegate.isRemoteThread()) return true;
        }
        return false;
    }


    /**
       Ignore all events 
    */
    public static void setIgnoreAll(boolean b) {
        bIgnoreAll = b;
    }

    @Override
    public synchronized void undo() throws CannotUndoException {
        try {
            bIgnoreAll = true;
            super.undo();
        }
        finally {
            bIgnoreAll = false;
        }
    }
}


