/*
2003/10/21 added support for Undo

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
package com.viaoa.jfc.control;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.lang.reflect.*;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.table.*;

/**
    Class for binding JButton to Object or Hub, with built-in command support, and default icons.
    Can also be used to call a method in the active object, see {@link #setMethod(String)}.
    <p>
    Example:<br>
    This will create a JButton that will automatically save the active object in the hubEmployee.
    If hubEmployee active object is null, then the button will be disabled.
    <pre>
    Hub hubEmployee = new Hub(Employee.class);
    JButton cmd = new JButton();
    Hub2Command hc = new Hub2Command(hubEmployee, cmd, OACommand.SAVE);

    Hub2Command hc = new Hub2Command(hubEmployee, cmd, OACommand.NEW);
    hc.setFocusComponent(txtField); // set focus when button is clicked
    </pre>
    <p>

    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
    @see OACommand
*/
public class Hub2Command extends Hub2Gui implements ActionListener {
    AbstractButton button;
    int command = -1;

    boolean bMasterControl = true;
    boolean bAnyTime, bManual;
    String confirmMessage;
    JComponent focusComponent; // comp to get focus after click
    String methodName;
    Object updateObject, updateValue;
    Method updateMethod;
    protected String undoDescription;

    /**
        Used to bind an AbstractButton to a Hub, with built in support for a command.
        <p>
    */
    public Hub2Command(Hub hub, AbstractButton button, int command) {
        super(hub, button);
        create(button, command);
        
        if (command == OACommand.NEW || command == OACommand.SAVE || command == OACommand.ADD) {
        	setAnyTime( hub != null );
    	}
    }

    /**
        Used to bind an AbstractButton to a Hub.
        <p>
        Note: setAnyTime(false) is used.
    */
    public Hub2Command(Hub hub, AbstractButton button) {
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
    public boolean getManual(){
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
        change(null);
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
            updateMethod = OAReflect.getMethod(object.getClass(), property);
        }
        else updateMethod = null;
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


    private void create(AbstractButton but, int command) {
        this.button = but;
        button.addActionListener(this);
        setCommand(command);  // this will call change()
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
        change(null);
    }

    @Override
    public void setReadOnly(boolean b) {
        super.setReadOnly(b);
        change(null);
    }
    
    boolean flag;
    public void change(HubEvent e) {
        Object obj = null;
        Hub hub = getActualHub();

        if (hub != null) obj = hub.getActiveObject();
        OAObject oaObj;
        if (obj instanceof OAObject) oaObj = (OAObject) obj;
        else oaObj = null;


        flag = bAnyTime;

        if (hub != null) {
            switch (command) {
                case OACommand.NEXT:
                    int pos = hub.getPos();
                    flag = hub.elementAt(pos+1) != null;
                    if (flag) {
                        if (oaObj != null && !bMasterControl && !bAnyTime && oaObj.getChanged() ) flag = false;
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
                    break;
                case OACommand.DELETE:
                    //was: flag = (obj != null && hub.getAllowDelete());
                    flag = obj != null;
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
                    break;
                case OACommand.SELECT_MANUAL:
                case OACommand.SELECT:
                    if ( hub.getMasterHub() == null) {
                        if (obj == null || !(obj instanceof OAObject) || bAnyTime || !((OAObject)obj).getChanged() ) flag = true;
                    }
                    else flag = false;
                    break;
                case OACommand.UP:
                    if (obj != null && hub.getPos() > 0) flag = true;
                    break;
                case OACommand.DOWN:
                    if (obj != null && (hub.isMoreData() || hub.getPos() < (hub.getSize()-1))  ) flag = true;
                    break;
                default:
                    if (hub.getPos() >= 0) flag = true;
            }
            if (!HubDelegate.isValid(hub)) flag = false;
        }


        if (button instanceof OAButton) {
        	flag = ((OAButton) button).isEnabled(flag);
        }
        else if (button instanceof OAMenuItem) {
            flag = ((OAMenuItem) button).isEnabled(flag);
        }
        
        if (readOnly) {
            flag = false;
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            this.setInternalEnabled(flag && isParentEnabled(button));
        }
        else {
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    Hub2Command.this.setInternalEnabled(flag && isParentEnabled(button));
                }
            });
        }
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
        super.close();
    }

    /**
        Hub event used to change status of button.
    */
    public @Override void afterChangeActiveObject(HubEvent e) {
        change(e);
    }

    /**
        Hub event used to change status of button.
    */
    public @Override void afterPropertyChange(HubEvent e) {
        /* was: 20100920
        String s = e.getPropertyName();
        if (s.equalsIgnoreCase("changed")) {
            change(e);
        }
        */
        change(e);
    }
    /**
        Hub event used to change status of button.
    */
    public @Override void afterInsert(HubEvent e) {
        change(e);
    }
    /**
        Hub event used to change status of button.
    */
    public @Override void afterAdd(HubEvent e) {
        change(e);
    }
    /**
        Hub event used to change status of button.
    */
    public @Override void afterRemove(HubEvent e) {
        change(e);
    }
    /**
        Hub event used to change status of button.
    */
    public @Override void onNewList(HubEvent e) {
        change(e);
    }
    /**
        Hub event used to change status of button.
    */
    public void hubOptionChange(HubEvent e) {
        change(e);
    }


    /**
        Click event handler.  Shows confirm message dialog if confirmMessage is not null, then
        processes command based on command value.
    */
    public void actionPerformed(ActionEvent e) {
        if (button instanceof OAButton) {
            if (!((OAButton) button).onConfirm(confirmMessage)) return;
        }
        else if (button instanceof OAMenuItem) {
            if (!((OAMenuItem) button).onConfirm(confirmMessage)) return;
        }
        if (button instanceof OAButton) {
        	((OAButton) button).onActionPerformed();
        }
        else if (button instanceof OAMenuItem) {
            ((OAMenuItem) button).onActionPerformed();
        }
        else {
        	onActionPerformed();
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
    
    public void onActionPerformed() {
        Object ho = null;
        Hub hub = getActualHub();
        if (hub == null) return;
        if (hub != null) ho = hub.getActiveObject();;

        /*was:
        if (confirmMessage != null) {
            if (!confirm()) return;
            if (hub != null && ho != hub.getActiveObject()) return;
        }
        */
        Object currentAO = hub.getAO();
        if (hub != null) {
            if (!bManual) {
                int pos = hub.getPos();
            	switch (command) {
                    case OACommand.NEXT:
                    	hub.setPos(pos+1);
                        if (currentAO != hub.getAO() && bEnableUndo) {
                            OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoText("next"), hub, currentAO, hub.getAO()));
                        }
                        break;
                    case OACommand.PREVIOUS:
                    	hub.setPos(pos-1);
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
                            ((OAObject)ho).save();
                        }
                        break;
                    case OACommand.SAVE_MANUAL:
                        break;
                    case OACommand.DELETE:
                        if (ho != null) {
                            if (bEnableUndo) {
                                OAUndoManager.startCompoundEdit(getUndoText("delete"));
                            }
                            if (ho instanceof OAObject && (hub == null || !hub.isOwned()) ) {
                            	/*was
                                String msg = ((OAObject)ho).getCantDeleteMessage();
                                if (msg != null) {
                                    JOptionPane.showMessageDialog(null, msg, "", JOptionPane.ERROR_MESSAGE,null);
                                    break;
                                }
                                ((OAObject)ho).delete();
                                */
                                if (bEnableUndo) {
                                    OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("delete"), hub, ho, hub.getPos()));
                                }
                                ((OAObject)ho).delete();
                            }
                            else {
                                if (hub != null) {
                                    if (bEnableUndo) {
                                        OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("remove"), hub, ho, hub.getPos()));
                                    }
                                    hub.remove(ho);
                                }
                            }
                            if (bEnableUndo) {
                                OAUndoManager.endCompoundEdit();
                            }
                        }
                        break;
                    case OACommand.REMOVE:
                        if (ho != null) {
                            if (bEnableUndo) {
                                OAUndoManager.startCompoundEdit(getUndoText("remove"));
                                OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoText("remove"), hub, ho, hub.getPos()));
                            }
                            hub.remove(ho);
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

                    case OACommand.SELECT_MANUAL: break; // user must respond
                    case OACommand.SELECT:
                        if (hub != null) hub.select();
                        break;
                    case OACommand.UP:
                        ho = hub.getActiveObject();
                        pos = hub.getPos();
                        if (ho != null && pos > 0) {
                            hub.move(pos,pos-1);
                            if (bEnableUndo) {
                                OAUndoManager.add(OAUndoableEdit.createUndoableMove(getUndoText("move up"), hub, pos, pos-1));
                            }
                            HubAODelegate.setActiveObjectForce(hub, ho);
                        }
                        break;
                    case OACommand.DOWN:
                        ho = hub.getActiveObject();
                        pos = hub.getPos();
                        if (ho != null && hub.elementAt(pos+1) != null) {
                            hub.move(pos,pos+1);
                            if (bEnableUndo) {
                                OAUndoManager.add(OAUndoableEdit.createUndoableMove(getUndoText("move down"), hub, pos, pos+1));
                            }
                            HubAODelegate.setActiveObjectForce(hub, ho);
                        }
                        break;

                }
            }
            if (methodName != null) OAReflect.executeMethod(hub.getAO(), methodName);
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
                        for (int i=0; i < x; i++) {
                            TableColumn tcol = mod.getColumn(i);
                            TableCellEditor editor = tcol.getCellEditor();
                            if (oac.getTableCellEditor() == editor) {
                                col = i;
                                bFlag = true;
                                break;
                            }
                        }
                        if (bFlag) {
                            irow = hub.getPos();
                            icol = col;
                            SwingUtilities.invokeLater( new Runnable() {
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
                for (int i=0; i < x; i++) {
                    if (mod.getColumn(i).getCellEditor() != null) {
                        irow = hub.getPos();
                        icol = i;
                        SwingUtilities.invokeLater( new Runnable() {
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
                OAReflect.setPropertyValue(updateObject, updateMethod, updateValue);
            }
            catch (Exception ex) {
                throw new RuntimeException("Hub2Command update method exception invoking method="+ updateMethod.getName()+" "+ex);
            }
        }
    }
    private int irow, icol;

    protected void createNew(boolean insertFlag) {
        Object obj;
        Hub hub = getActualHub();
        Class c = hub.getObjectClass();
        if (c == null) return;

        obj = OAObjectReflectDelegate.createNewObject(c);

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
            int x = JOptionPane.showOptionDialog(SwingUtilities.getWindowAncestor(button), confirmMessage, "Confirmation", 0, JOptionPane.QUESTION_MESSAGE,null, new String[] {"Yes","No"}, "Yes");
            return (x == 0);
        }
        else return true;
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
}
