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
package com.viaoa.jfc.undo;

import java.util.*;
import java.util.logging.Logger;

import javax.swing.undo.*;

import com.viaoa.object.*;
import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.*;


/** Undo Support for OA.gui components.

   @see #createUndoManager
   see UndoableController
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
        OAThreadLocalDelegate.startUndoable(presentationName);
        //startCompoundEdit(presentationName);
        //OAThreadLocalDelegate.setCreateUndoablePropertyChanges(true);
    }    
    public static void endCompoundEditForPropertyChanges() {
        OAThreadLocalDelegate.endUndoable();
        //endCompoundEdit();
        //OAThreadLocalDelegate.setCreateUndoablePropertyChanges(false);
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

/* *qqqqqqq 20100124 not used?    
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
        @return true if counter for current thread &gt; 0, or if OAUndoManager is null or thread is OAClient.isClientThread()
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


