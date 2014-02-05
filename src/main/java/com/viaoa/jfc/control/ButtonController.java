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

import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.table.*;
import java.lang.reflect.*;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.dnd.OATransferable;
import com.viaoa.jfc.table.*;

/**
 * Functionality for binding JButton to OA.
 * @author vvia
 */
public class ButtonController extends JFCController implements ActionListener {
    private static Logger LOG = Logger.getLogger(ButtonController.class.getName());
    private AbstractButton button;
    private int command = -1;

    private boolean bMasterControl = true;
    private boolean bAnyTime, bManual;
    private String confirmMessage;
    private String completedMessage;
    private JComponent focusComponent; // comp to get focus after click
    private String methodName;
    private Object updateObject, updateValue;
    private Method updateMethod;
    private String undoDescription;
    private boolean bUseSwingWorker;
    public String processingTitle, processingMessage;

    /**
        Used to bind an AbstractButton to a Hub, with built in support for a command.
        <p>
    */
    public ButtonController(Hub hub, AbstractButton button, int command) {
        super(hub, button);
        create(button, command);

        if (command == OACommand.NEW || command == OACommand.SAVE || command == OACommand.ADD) {
            setAnyTime(hub != null);
        }
    }

    /**
        Used to bind an AbstractButton to a Hub.
        <p>
        Note: setAnyTime(false) is used.
    */
    public ButtonController(Hub hub, AbstractButton button) {
        this(hub, button, -1);
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

    /**
        If the hub for this command has a masterHub, then it can control this button if
        this is set to true.  Default = true
    */
    public void setMasterControl(boolean b) {
        bMasterControl = b;
    }

    public boolean getMasterControl() {
        return bMasterControl;
    }

    /**
        If false (default) then command will be ran when button is pressed,
        else if true then no code will run when button is pressed.  The button will be
        enabled based on status of Hub and value of command.
    */
    public boolean getManual() {
        return bManual;
    }

    /**
        If false (default) then command will be ran when button is pressed,
        else if true then no code will run when button is pressed.  The button will be
        enabled based on status of Hub and value of command.
    */
    public void setManual(boolean b) {
        bManual = b;
    }

    /**
        If true and Hub is valid, then button will always be enabled, else button will be disabled
        based on value of command.  If true and command is NEW, then button will be disabled if object
        needs to be saved.
        <p>
        Note: default value is true.
     */
    public void setAnyTime(boolean b) {
        bAnyTime = b;
        update();
    }

    /**
        If true and Hub is valid, then button will always be enabled, else button will be disabled
        based on value of command.
     */
    public boolean getAnyTime() {
        return bAnyTime;
    }

    /**
       Object to update whenever button is clicked.
    */
    public void setUpdateObject(Object object, String property, Object newValue) {
        if (object != null && property != null) {
            updateMethod = OAReflect.getMethod(object.getClass(), "set" + property);
            updateValue = newValue;
        }
        else updateMethod = null;
    }

    /**
        Update active object whenever button is clicked.
    */
    public void setUpdateObject(String property, Object newValue) {
        if (property == null) {
            updateMethod = null;
            updateValue = null;
        }
        else {
            if (getHub() == null) return;
            updateMethod = OAReflect.getMethod(getHub().getObjectClass(), "set" + property);
            updateValue = newValue;
        }
    }

    /**
        Popup message used to confirm button click before running code.
    */
    public void setConfirmMessage(String msg) {
        confirmMessage = msg;
    }
    /**
        Popup message used to confirm button click before running code.
    */
    public String getConfirmMessage() {
        return confirmMessage;
    }

    public void setCompletedMessage(String msg) {
        completedMessage = msg;
    }
    public String getCompletedMessage() {
        return completedMessage;
    }
    
    
    private void create(AbstractButton but, int command) {
        this.button = but;
        button.addActionListener(this);
        setCommand(command); // this will call change()
        if (hub != null) {
            getEnabledController().add(hub, null, OAAnyValueObject.instance); // so that Hub.isValid will be the only check
        }
    }

    @Override
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        if (bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = getDefaultEnabled();
        }
        return bIsCurrentlyEnabled;
    }

    /**
        Return command value.
        @see OACommand
    */
    public int getCommand() {
        return command;
    }

    /**
        @see OAButton#setCommand
    */
    public void setCommand(int command) {
        this.command = command;
        getEnabledController().update();
        getVisibleController().update();
        update();
    }

    protected void resetHubOrProperty() { // called when Hub or PropertyName is changed
        super.resetHubOrProperty();
        if (button != null) create(button, command);
    }

    /**
        Return actionListener and close.
    */
    public void close() {
        if (button != null) button.removeActionListener(this);

        // 20110111
        if (flavorListener != null) {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            cb.removeFlavorListener(flavorListener);
            flavorListener = null;
        }
        super.close();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override
    void afterChangeActiveObject(HubEvent e) {
        update();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override
    void afterPropertyChange(HubEvent e) {
        /* was: 20100920
        String s = e.getPropertyName();
        if (s.equalsIgnoreCase("changed")) {
            change(e);
        }
        */
        update();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override
    void afterInsert(HubEvent e) {
        update();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override
    void afterAdd(HubEvent e) {
        update();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override
    void afterRemove(HubEvent e) {
        update();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override
    void onNewList(HubEvent e) {
        //qqqqqqqqq        
        if (button instanceof OAButton) {
            if (((OAButton) button).XXX) {
                int xxx = 4;
                xxx++;
            }
        }

        update();
    }

    /**
        Hub event used to change status of button.
    */
    public void hubOptionChange(HubEvent e) {
        update();
    }

    // Note:  order of action:  actionPerformed, confirm, onActionPerformed, _onActionPerformed
    /**
        Click event handler.  Shows confirm message dialog if confirmMessage is not null, then
        processes command based on command value.
    */
    public void actionPerformed(ActionEvent e) {
        if (button == null || !button.isEnabled()) return;
        if (confirmMessage != null) {
            if (!confirm(confirmMessage)) return;
        }
        onActionPerformed();
        if (completedMessage != null) {
            afterCompleted(completedMessage);
        }
    }

    private String getUndoText(String cmd) {
        if (undoDescription != null && undoDescription.length() > 0) return undoDescription;
        if (hub != null) {
            Class c = hub.getObjectClass();
            cmd += " " + OAString.convertHungarian(c.getSimpleName());
        }
        return cmd;
    }

    public void setUseSwingWorker(boolean b) {
        this.bUseSwingWorker = b;
    }

    public boolean getUseSwingWorker() {
        return this.bUseSwingWorker;
    }

    public void setProcessingText(String title, String msg) {
        processingTitle = title;
        processingMessage = msg;
    }

    protected void onActionPerformed() {
        Window window = OAJFCUtil.getWindow(button);
        try {
            if (window != null) {
                window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            onActionPerformed2();
        }
        finally {
            if (window != null) {
                window.setCursor(Cursor.getDefaultCursor());
            }
        }
    }
    
    protected void onActionPerformed2() {
        Hub mhub = getMultiSelectHub();
        if (command == OACommand.DELETE) {
            OAObject currentAO = (OAObject) hub.getAO();
            if (currentAO != null) {
                OALinkInfo[] lis = OAObjectDeleteDelegate.getMustBeEmptyBeforeDelete(currentAO);

                if (mhub != null && (lis == null || lis.length == 0)) {
                    Object[] objs = mhub.toArray();
                    for (Object obj : objs) {
                        if (obj instanceof OAObject) {
                            lis = OAObjectDeleteDelegate.getMustBeEmptyBeforeDelete((OAObject) obj);
                            if (lis != null && lis.length > 0) break;
                        }
                    }
                }

                if (lis != null && lis.length > 0) {
                    String msg = null;
                    for (OALinkInfo li : lis) {
                        if (msg == null) msg = li.getName();
                        else msg += ", " + li.getName();
                    }
                    msg = "Can not delete while the following are not empty\n" + msg;
                    JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(component), msg, "Can not delete", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        if (!bUseSwingWorker) {
            _onActionPerformed();
            return;
        }

        final Window window = OAJFCUtil.getWindow(button);
        final OAWaitDialog dlg = new OAWaitDialog(window, false);

        String s = processingTitle;
        if (s == null) s = "Processing";
        dlg.setTitle(s);

        s = processingMessage;
        if (s == null) {
            s = button.getText();
            if (s == null) s = "";
            else s = " \"" + s + "\"";
            s = "Please wait ... processing request" + s;
        }
        dlg.setStatus(s);

        SwingWorker<Boolean, Void> sw = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                _onActionPerformed();
                return true;
            }

            @Override
            protected void done() {
                synchronized (Lock) {
                    if (dlg.isVisible()) {
                        dlg.setVisible(false);
                    }
                }
            }
        };
        sw.execute();
        synchronized (Lock) {
            if (sw.getState() != StateValue.DONE) {
                dlg.setVisible(true);
            }
        }
        try {
            sw.get();
        }
        catch (Exception ex) {
            LOG.log(Level.WARNING, "error while performing command action", ex);
            for (int i=0 ; i<10; i++) {
                Throwable t = ex.getCause();
                if (t == null || t == ex || !(t instanceof Exception)) { 
                    break;
                }
                ex = (Exception) t;
            }
            
            JOptionPane.showMessageDialog(OAJFCUtil.getWindow(button), 
                    "Error: "+OAString.fmt(ex.getMessage(), "40L."), 
                    "Command Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private final Object Lock = new Object();

    protected void _onActionPerformed() {
        Object ho = null;
        Hub hub = getActualHub();
        if (hub == null) return;
        if (hub != null) ho = hub.getActiveObject();

        /*was:
        if (confirmMessage != null) {
            if (!confirm()) return;
            if (hub != null && ho != hub.getActiveObject()) return;
        }
        */
        Object currentAO = hub.getAO();
        Hub mhub = getMultiSelectHub();

        if (hub != null) {
            if (!bManual) {
                OAObject oaObj;
                int pos = hub.getPos();
                switch (command) {
                case OACommand.NEXT:
                    hub.setPos(pos + 1);
                    if (currentAO != hub.getAO() && bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoText("next"), hub, currentAO, hub.getAO()));
                    }
                    break;
                case OACommand.PREVIOUS:
                    hub.setPos(pos - 1);
                    if (currentAO != hub.getAO() && bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoText("previous"), hub, currentAO, hub.getAO()));
                    }
                    break;
                case OACommand.FIRST:
                    hub.setPos(0);
                    if (currentAO != hub.getAO() && bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoText("goto first"), hub, currentAO, hub.getAO()));
                    }
                    break;
                case OACommand.LAST:
                    if (hub.isMoreData()) hub.loadAllData();
                    hub.setPos(hub.getSize() - 1);
                    if (currentAO != hub.getAO() && bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoText("goto last"), hub, currentAO, hub.getAO()));
                    }
                    break;

                case OACommand.SAVE:
                    if (ho == null) break;
                    if (ho instanceof OAObject) {
                        /*was
                        if (hub == null || (!hub.isMasterNew() && !hub.isOwned()) ) {
                            String msg = ((OAObject)ho).getCantSaveMessage();
                            if (msg != null) {
                                JOptionPane.showMessageDialog(null, msg, "", JOptionPane.ERROR_MESSAGE,null);
                                break;
                            }
                            ((OAObject)ho).save();
                        }
                        */
                        String msg = null;
                        try {
                            ((OAObject) ho).save();
                        }
                        catch (Exception e) {
                            msg = "Error while saving\n" + e;
                        }
                        if (msg != null) {
                            JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE,null);
                            break;
                        }
                        
                    }
                    if (mhub != null) {
                        Object[] objs = mhub.toArray();
                        for (Object obj : objs) {
                            if (obj instanceof OAObject) {
                                String msg = null;
                                try {
                                    ((OAObject) obj).save();
                                }
                                catch (Exception e) {
                                    msg = "Error while saving\n" + e;
                                }
                            }
                        }
                    }
                    break;
                case OACommand.SAVE_MANUAL:
                    break;
                case OACommand.DELETE:
                    if (ho == null) break;

                    if (bEnableUndo) {
                        OAUndoManager.startCompoundEdit(getUndoText("delete"));
                    }
                    try {
                        if (mhub != null && mhub.getSize() > 0) {
                            Object[] objs = mhub.toArray();
                            for (Object obj : objs) {
                                if (obj instanceof OAObject) {
                                    if (HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                                        getHub().remove(obj); // keep "noise" down
                                    }
                                    if (bEnableUndo) {
                                        int posx = hub.getPos(obj);
                                        OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("delete"), hub, ho, posx));
                                    }
                                    String msg = null;
                                    try {
                                        ((OAObject) obj).delete();
                                    }
                                    catch (Exception e) {
                                        msg = "Error while deleting\n" + e;
                                    }
                                }
                            }
                            ho = null;
                        }
                        else {
                            if (ho instanceof OAObject) {
                                oaObj = (OAObject) ho;
                            }
                            else oaObj = null;

                            // 20131220
                            if (oaObj != null) {
                            //was: if (oaObj != null && (hub == null || (!hub.isOwned() || OAObjectHubDelegate.getHubReferenceCount(oaObj) > 1))) {
                                if (bEnableUndo) {
                                    OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("delete"), hub, ho, hub.getPos()));
                                }
                                if (HubAddRemoveDelegate.isAllowAddRemove(getHub())) { // 20120720
                                    getHub().remove(ho); // 20110215 remove first, so that cascading deletes are not so "noisy"
                                }
                                // else it can only be removed when delete is called (ex: a detail hub that is from a linkOne)
                                String msg = null;
                                try {
                                    ((OAObject) ho).delete();
                                }
                                catch (Exception e) {
                                    msg = "Error while deleting\n" + e;
                                }
                            }
                            else {
                                if (hub != null) {
                                    if (bEnableUndo) {
                                        OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("remove"), hub, ho, hub.getPos()));
                                    }
                                    hub.remove(ho);
                                }
                            }
                        }
                    }
                    finally {
                        if (bEnableUndo) {
                            OAUndoManager.endCompoundEdit();
                        }
                    }
                    break;
                case OACommand.REMOVE:
                    if (bEnableUndo) {
                        OAUndoManager.startCompoundEdit(getUndoText("remove"));
                    }
                    try {
                        if (mhub != null && mhub.getSize() > 0) {
                            Object[] objs = mhub.toArray();
                            for (Object obj : objs) {
                                if (obj instanceof OAObject) {
                                    if (HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                                        int posx = hub.getPos(obj);
                                        OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("remove"), hub, obj, posx));
                                        getHub().remove(obj);
                                    }
                                }
                            }
                        }
                        else if (ho != null) {
                            if (bEnableUndo) {
                                OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("remove"), hub, ho, hub.getPos()));
                            }
                            hub.remove(ho);
                        }
                    }
                    finally {
                        if (bEnableUndo) {
                            OAUndoManager.endCompoundEdit();
                        }
                    }
                    break;
                case OACommand.CANCEL:
                    /* was
                    if (ho != null && ho instanceof OAObject) {
                        OAObject obj = (OAObject) ho;
                        obj.cancel();
                        if (obj.isNew()) obj.removeAll();
                    }
                    */
                    break;

                case OACommand.ADD:
                case OACommand.NEW:
                    createNew(false);
                    break;
                case OACommand.ADD_MANUAL:
                case OACommand.NEW_MANUAL:
                    break;// user must respond

                case OACommand.INSERT:
                    createNew(true);
                    break;
                case OACommand.INSERT_MANUAL:
                    break; // user must respond

                case OACommand.SELECT_MANUAL:
                    break; // user must respond
                case OACommand.SELECT:
                    if (hub != null) hub.select();
                    break;
                case OACommand.UP:
                    ho = hub.getActiveObject();
                    pos = hub.getPos();
                    if (ho != null && pos > 0) {
                        hub.move(pos, pos - 1);
                        if (bEnableUndo) {
                            OAUndoManager.add(OAUndoableEdit.createUndoableMove(getUndoText("move up"), hub, pos, pos - 1));
                        }
                        HubAODelegate.setActiveObjectForce(hub, ho);
                    }
                    break;
                case OACommand.DOWN:
                    ho = hub.getActiveObject();
                    pos = hub.getPos();
                    if (ho != null && hub.elementAt(pos + 1) != null) {
                        hub.move(pos, pos + 1);
                        if (bEnableUndo) {
                            OAUndoManager.add(OAUndoableEdit.createUndoableMove(getUndoText("move down"), hub, pos, pos + 1));
                        }
                        HubAODelegate.setActiveObjectForce(hub, ho);
                    }
                    break;
                case OACommand.CLEARAO:
                    ho = hub.getActiveObject();
                    if (ho != null) {
                        hub.setAO(null);
                        if (bEnableUndo) {
                            OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO("Set active object to null", hub, ho, null));
                        }
                    }
                    break;
                case OACommand.CUT:
                    ho = hub.getActiveObject();
                    if (ho instanceof OAObject) addToClipboard((OAObject) ho);
                    break;
                case OACommand.COPY:
                    if (mhub != null && mhub.getSize() > 0) {
                        addToClipboard(mhub);
                    }
                    else {
                        ho = hub.getActiveObject();
                        if (ho instanceof OAObject) {
                            oaObj = createCopy((OAObject) ho);
                            if (oaObj != null) addToClipboard(oaObj);
                        }
                    }
                    break;
                case OACommand.PASTE:
                    OAObject obj = getClipboardObject();
                    if (obj != null) {
                        if (!hub.contains(obj)) {
                            hub.add(obj);
                        }
                        hub.setAO(obj);
                    }
                    else {
                        Hub hx = getClipboardHub();
                        if (hx != null) {
                            for (Object objx : hx) {
                                if (!hub.contains(objx)) {
                                    hub.add(objx);
                                }
                            }
                        }
                    }
                    break;
                }
            }
            if (methodName != null) {
                Method[] method = OAReflect.getMethods(hub.getObjectClass(), methodName);

                String msg = null;
                if (mhub != null && mhub.getSize() > 0) {
                    Object[] objs = mhub.toArray();
                    for (Object obj : objs) {
                        if (obj instanceof OAObject) {
                            Object objx = OAReflect.executeMethod(obj, methodName);
                            if (msg != null && objx instanceof String) {
                                msg = (String) objx;
                                msg = msg + " (total " + objs.length + ")";
                            }
                        }
                    }
                    if (msg == null) {
                        if (objs.length > 1) msg = "processed " + objs.length;
                    }
                }
                else {
                    Object objx = OAReflect.executeMethod(hub.getAO(), methodName);
                    if (objx instanceof String) {
                        msg = (String) objx;
                    }
                }
                if (msg != null) {
                    JOptionPane.showMessageDialog(OAJFCUtil.getWindow(button), msg, "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
        if (focusComponent != null) {
            boolean bFlag = false;
            if (focusComponent instanceof OATableComponent && hub != null && (focusComponent.getParent() == null || focusComponent.getParent() instanceof OATable)) {
                OATableComponent oac = (OATableComponent) focusComponent;
                if (oac.getTableCellEditor() != null) {
                    // this component is a tableCellEditor
                    OATable table = oac.getTable();
                    if (table != null) {
                        table.requestFocus();
                        TableColumnModel mod = table.getColumnModel();
                        int x = mod.getColumnCount();
                        int col = 0;
                        for (int i = 0; i < x; i++) {
                            TableColumn tcol = mod.getColumn(i);
                            TableCellEditor editor = tcol.getCellEditor();
                            if (oac.getTableCellEditor() == editor) {
                                col = i;
                                bFlag = true;
                                break;
                            }
                        }
                        if (bFlag) {
                            final int irow = table.getHub().getPos();
                            final int icol = col;
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    ((OATableComponent) focusComponent).getTable().editCellAt(irow, icol);
                                }
                            });
                            // table.editCellAt(hub.getPos(), col);
                        }
                        else bFlag = true;
                    }
                }
            }

            if (!bFlag) focusComponent.requestFocus();

            if (focusComponent instanceof JTable) {
                JTable table = (JTable) focusComponent;
                TableColumnModel mod = table.getColumnModel();
                int x = mod.getColumnCount();
                for (int i = 0; i < x; i++) {
                    if (mod.getColumn(i).getCellEditor() != null) {
                        final int irow = hub.getPos();
                        final int icol = i;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                ((JTable) focusComponent).editCellAt(irow, icol);
                            }
                        });
                        // table.editCellAt(hub.getPos(), i);
                        break;
                    }
                }
            }
        }
        if (updateMethod != null) {
            try {
                if (updateObject != null) OAReflect.setPropertyValue(updateObject, updateMethod, updateValue);
                else {
                    if (getHub() != null) {
                        OAReflect.setPropertyValue(getHub().getAO(), updateMethod, updateValue);
                    }
                }
            }
            catch (Exception ex) {
                throw new RuntimeException("ButtonController update method exception invoking method=" + updateMethod.getName() + " " + ex);
            }
        }
        if (button instanceof OAButton) {
            ((OAButton) button).performAction();
        }
        else if (button instanceof OAMenuItem) {
            ((OAMenuItem) button).performAction();
        }
    }

    protected void createNew(boolean insertFlag) {
        Object obj;
        Hub hub = getActualHub();
        Class c = hub.getObjectClass();
        if (c == null) return;

        obj = OAObjectReflectDelegate.createNewObject(c);
        if (hub.contains(obj)) return; // 20110925: the createNew added it to the hub, since it was a selectAll hub

        if (insertFlag) {
            int pos = hub.getPos();
            if (pos < 0) pos = 0;
            hub.insert(obj, pos);
            if (bEnableUndo) {
                OAUndoManager.add(OAUndoableEdit.createUndoableInsert(getUndoText("create new"), hub, obj, pos));
            }
        }
        else {
            hub.addElement(obj);
            if (bEnableUndo) {
                OAUndoManager.add(OAUndoableEdit.createUndoableAdd(getUndoText("create new"), hub, obj));
            }
        }
        hub.setActiveObject(obj);
    }

    /** returns true if command is allowed */
    public boolean confirm(String confirmMessage) {
        if (!OAString.isEmpty(confirmMessage)) {
            int x = JOptionPane.showOptionDialog(OAJFCUtil.getWindow(button), confirmMessage, "Confirmation", 0, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No" }, "Yes");
            return (x == 0);
        }
        else return true;
    }

    public void afterCompleted(String completedMessage) {
        if (!OAString.isEmpty(completedMessage)) {
            JOptionPane.showMessageDialog(
                OAJFCUtil.getWindow(button), 
                completedMessage, "Command completed", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
        Returns the component that will receive focus when this button is clicked.
    */
    public JComponent getFocusComponent() {
        return focusComponent;
    }

    /**
        Set the component that will receive focus when this button is clicked.
    */
    public void setFocusComponent(JComponent focusComponent) {
        this.focusComponent = focusComponent;
    }

    /**
        Method in object to execute on active object in hub.
    */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
        Method in object to execute on active object in hub.
    */
    public String getMethodName() {
        return methodName;
    }

    protected boolean getDefaultEnabled() {
        if (button == null) return false;

        Object obj = null;
        Hub hub = getActualHub();

        //qqqqqqqqq        
        if (button instanceof OAButton) {
            if (((OAButton) button).XXX) {
                int xxx = 4;
                xxx++;
            }
        }
        if (hub != null) obj = hub.getActiveObject();
        OAObject oaObj;
        if (obj instanceof OAObject) oaObj = (OAObject) obj;
        else oaObj = null;

        // 20110420
        boolean flag = bAnyTime || (hub == null);
        // was: boolean flag = bAnyTime;

        if (hub != null) {
            switch (command) {
            case OACommand.NEXT:
                int pos = hub.getPos();
                flag = hub.elementAt(pos + 1) != null;
                if (flag) {
                    if (oaObj != null && !bMasterControl && !bAnyTime && oaObj.getChanged()) flag = false;
                }
                break;
            case OACommand.PREVIOUS:
                pos = hub.getPos();
                flag = pos >= 0;
                if (flag) {
                    if (oaObj != null && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                }
                break;
            case OACommand.FIRST:
                flag = hub.getPos() != 0 && hub.getCurrentSize() > 0;
                if (oaObj != null && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                break;
            case OACommand.LAST:
                flag = hub.getSize() != 0;
                if (flag) {
                    if (oaObj != null && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                    else flag = hub.getPos() != hub.getSize() - 1;
                }
                break;
            case OACommand.SAVE:
            case OACommand.SAVE_MANUAL:
                flag = (obj != null);
                if (oaObj != null) {
                    flag = bAnyTime || oaObj.getChanged();
                }
                break;
            case OACommand.CANCEL:
                if (obj == null) flag = false;
                else if (oaObj != null && (bAnyTime || oaObj.getChanged() || oaObj.getNew())) flag = true;
                break;
            case OACommand.REMOVE:
                flag = obj != null;
                if (flag && !HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                    flag = false;
                }
                break;
            case OACommand.CLEARAO:
                flag = obj != null;
                break;
            case OACommand.DELETE:
                //was: flag = (obj != null && hub.getAllowDelete());
                flag = (obj != null);
                break;
            case OACommand.INSERT:
            case OACommand.INSERT_MANUAL:
            case OACommand.NEW:
            case OACommand.NEW_MANUAL:
            case OACommand.ADD:
            case OACommand.ADD_MANUAL:
                //was: flag = hub.getAllowNew();
                flag = true;
                if (hub != null) flag = hub.isValid();
                if (flag && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                if (flag && !HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                    flag = (hub.getSize() == 0);
                    break;
                }
                break;
            case OACommand.SELECT_MANUAL:
            case OACommand.SELECT:
                if (hub.getMasterHub() == null) {
                    if (obj == null || !(obj instanceof OAObject) || bAnyTime || !((OAObject) obj).getChanged()) flag = true;
                }
                else flag = false;
                break;
            case OACommand.UP:
                if (obj != null && hub.getPos() > 0) flag = true;
                break;
            case OACommand.DOWN:
                if (obj != null && (hub.isMoreData() || hub.getPos() < (hub.getSize() - 1))) flag = true;
                break;
            case OACommand.CUT: // 20100111
            case OACommand.COPY:
                flag = (obj != null);
                break;
            case OACommand.PASTE:
                flag = false;
                OAObject objx = getClipboardObject();
                if (hub != null && objx != null && objx.getClass().equals(hub.getObjectClass())) {
                    if (!hub.contains(objx)) flag = true;
                }
                if (flag && !HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                    flag = (hub.getSize() == 0);
                    break;
                }
                break;
            default:
                if (hub.getPos() >= 0) flag = true;
            }
            if (!HubDelegate.isValid(hub)) flag = false;
        }
        return flag;
    }

    // 20110111 used for Paste
    private FlavorListener flavorListener;

    protected void setupPasteCommand() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        flavorListener = new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                ButtonController.this.update();
            }
        };
        cb.addFlavorListener(flavorListener);
    }

    // this can be overwritten to customize an object copy.
    protected OAObject createCopy(OAObject obj) {
        OAObject objx = obj.createCopy();
        return objx;
    }

    protected OAObject getClipboardObject() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        OAObject oaObj;
        try {
            Object objx = cb.getData(OATransferable.OAOBJECT_FLAVOR);
            if (objx instanceof OAObject) oaObj = (OAObject) objx;
            else oaObj = null;
        }
        catch (Exception e) {
            oaObj = null;
        }
        return oaObj;
    }

    protected Hub getClipboardHub() {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        Hub hub;
        try {
            Object objx = cb.getData(OATransferable.HUB_FLAVOR);
            if (objx instanceof Hub) hub = (Hub) objx;
            else hub = null;
        }
        catch (Exception e) {
            hub = null;
        }
        return hub;
    }

    protected void addToClipboard(OAObject obj) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        OATransferable t = new OATransferable(getHub(), obj);
        cb.setContents(t, new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
                ButtonController.this.update();
            }
        });
    }

    protected void addToClipboard(Hub hub) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        OATransferable t = new OATransferable(getHub(), hub);
        cb.setContents(t, new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
                ButtonController.this.update();
            }
        });
    }

    @Override
    protected void update() {
        Object obj;
        Hub hub = getActualHub();
        if (hub != null) obj = hub.getActiveObject();
        else obj = null;

        super.update(button, obj);
    }
}
