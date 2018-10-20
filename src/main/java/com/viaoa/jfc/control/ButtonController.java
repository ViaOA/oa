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

import java.awt.BorderLayout;
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
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import java.io.File;
import java.lang.reflect.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.jfc.undo.OAUndoableEdit;
import com.viaoa.util.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.OAButton.ButtonCommand;
import com.viaoa.jfc.OAButton.ButtonEnabledMode;
import com.viaoa.jfc.dialog.OAConfirmDialog;
import com.viaoa.jfc.dialog.OAPasswordDialog;
import com.viaoa.jfc.dnd.OATransferable;
import com.viaoa.jfc.table.*;

/**
 * Functionality for binding JButton to OA.
 * 
 * 
    Note:  order of tasks for actionPerformed event:  
    actionPerformed,
        [password dialog]
        beforeActionPerformed   -- pretask, or cancel
        [confirmActionPerformed]  -- user confirm or cancel
        [getFile Save/Open] 
        {runActionPerformed}  -- sets up/uses swingWorker
            onActionPerformed  -- where actual event is handled
        afterActionPerformed  -- show completed message
           or
        afterActionPerformedFailure  - if error
 * 
 * @author vvia
 */
public class ButtonController extends OAJfcController implements ActionListener {
    private static Logger LOG = Logger.getLogger(ButtonController.class.getName());
    private AbstractButton button;
    
    private OAButton.ButtonEnabledMode enabledMode;
    protected OAButton.ButtonCommand command;

    private boolean bMasterControl = true;
    private String completedMessage;
    private String returnMessage;
    private String consoleProperty;
    private JComponent focusComponent; // comp to get focus after click
    private String methodName;

    private JFileChooser fileChooserOpen;
    private JFileChooser fileChooserSave;
    
    private String updateProperty;
    private OAObject updateObject;
    private Object updateValue;
    
    private boolean bUseSwingWorker;
    public String processingTitle, processingMessage;

    /*qqqqqq    
    public static ButtonController createHubValid(AbstractButton button, Hub hub) {
        ButtonController bc = new ButtonController(hub, button, OAButton.ButtonEnabledMode.ActiveObjectNotNull, null);
        return bc;
    }
    public static ButtonController createAoNotNull(AbstractButton button, Hub hub) {
        ButtonController bc = new ButtonController(hub, button, OAButton.ButtonEnabledMode.HubIsValid, null);
        return bc;
    }
    public static ButtonController createHubNotEmpty(AbstractButton button, Hub hub) {
        ButtonController bc = new ButtonController(hub, button, OAButton.ButtonEnabledMode.HubIsNotEmpty, null);
        return bc;
    }
    public static ButtonController createAlwaysTrue(AbstractButton button, Hub hub) {
        ButtonController bc = new ButtonController(hub, button, OAButton.ButtonEnabledMode.Always, null);
        return bc;
    }
    */    
    

// 20181010    
//qqqqqqqqqqqq NEW: to match OAJfcController    
// (Hub hub, Object object, String propertyPath, JComponent comp, HubChangeListener.Type type, final boolean bDirectlySetsAO, final boolean bIncludeExtendedChecks)    
    
    public ButtonController(Hub hub, AbstractButton button, OAButton.ButtonEnabledMode enabledMode, OAButton.ButtonCommand command, HubChangeListener.Type type, boolean bDirectlySetsAO, boolean bIncludeExtendedChecks) {
        super(hub, null, null, button, 
            type, 
            bDirectlySetsAO,
            bIncludeExtendedChecks
        );
        create(button, enabledMode, command);
    }
    
    
    
    /**
        Used to bind an AbstractButton to a Hub, with built in support for a command.
        <p>
    */
    public ButtonController(Hub hub, AbstractButton button, OAButton.ButtonEnabledMode enabledMode, OAButton.ButtonCommand command) {
        super(hub, null, null, button, 
            enabledMode.getHubChangeListenerType(), 
            ((command != null && hub != null && hub.getLinkHub() != null) ? command.getSetsAO() : false),
            true
            //was:  (((enabledMode == ButtonEnabledMode.ActiveObjectNotNull) && (command == ButtonCommand.Other)) ? false : true)
        );
        
        
//        super(hub, null, button, enabledMode.getHubChangeListenerType(), (command != null ? command.getSetsAO() : false));
        
//new OAJfcController(hub, null, null, comp, HubChangeListener.Type.AoNotNull, false, false);
        
        
//was:        super(hub, button, enabledMode.getHubChangeListenerType() );
        
        create(button, enabledMode, command);
    }

    
    
    /**
        Used to bind an AbstractButton to a Hub.
        <p>
        Note: setAnyTime(false) is used.
    */
    public ButtonController(Hub hub, AbstractButton button) {
        this(hub, button, OAButton.ButtonEnabledMode.ActiveObjectNotNull, null);
        create(button, enabledMode, command);
    }

    private void create(AbstractButton but, OAButton.ButtonEnabledMode enabledMode, OAButton.ButtonCommand command) {
        this.button = but;
        button.addActionListener(this);
        if (command == null) command = OAButton.ButtonCommand.Other;        
        this.command = command;
        this.enabledMode = enabledMode;
        update();
    }
    
    public void setCommand(OAButton.ButtonCommand command) {
        this.command = command;
        update();
    }
    
    
    /**
        If the hub for this command has a masterHub, then it can control this button if
        this is set to true.  Default = true
    */
    public void setMasterControl(boolean b) {
        bMasterControl = b;
        update();
    }

    public boolean getMasterControl() {
        return bMasterControl;
    }

    /**
       Object to update whenever button is clicked.
    */
    public void setUpdateObject(OAObject object, String property, Object newValue) {
        this.updateObject = object;
        this.updateProperty = property;
        this.updateValue = newValue;
        update();
    }

    /**
        Update active object whenever button is clicked.
    */
    public void setUpdateObject(String property, Object newValue) {
        this.updateObject = null;
        this.updateProperty = property;
        this.updateValue = newValue;
        
        // 20181009
        addEnabledCheck(getHub(), HubChangeListener.Type.AoNotNull);
        addEnabledEditQueryCheck(getHub(), property);
        addVisibleEditQueryCheck(getHub(), property);
        
        update();
    }

    public void setCompletedMessage(String msg) {
        completedMessage = msg;
    }
    public String getCompletedMessage() {
        return completedMessage;
    }
    public String default_getCompletedMessage() {
        return completedMessage;
    }

    public void setReturnMessage(String msg) {
        returnMessage = msg;
    }
    public String getReturnMessage() {
        return returnMessage;
    }
    
    public void setConsoleProperty(String prop) {
        consoleProperty = prop;
    }
    public String getConsoleProperty() {
        return consoleProperty;
    }
    
    public void setOpenFileChooser(JFileChooser fc) {
        this.fileChooserOpen = fc;
    }
    public JFileChooser getOpenFileChooser() {
        return fileChooserOpen;
    }

    public void setSaveFileChooser(JFileChooser fc) {
        this.fileChooserSave = fc;
    }
    public JFileChooser getSaveFileChooser() {
        return fileChooserSave;
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
    public OAButton.ButtonCommand getCommand() {
        return command;
    }

    public OAButton.ButtonEnabledMode getEnabledMode() {
        return enabledMode;
    }
    
    /**
        Return actionListener and close.
    */
    public void close() {
        if (button != null) {
            button.removeActionListener(this);
        }

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
    public @Override void afterChangeActiveObject(HubEvent e) {
        update();
    }

    @Override
    public String getUndoDescription() {
        String s = super.getUndoDescription();
        if (s != null && s.length() > 0) return s;
        if (hub != null) {
            OAObjectInfo oi = OAObjectInfoDelegate.getObjectInfo(hub.getObjectClass());
            s = command.name() + " " + oi.getDisplayName();
        }
        return s;
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
    
    
    public Object getSearchObject() {
        return null;
    }
    
    public boolean beforeActionPerformed() {
        return true;
    }
    public boolean default_beforeActionPerformed() {
        return true;
    }
    public boolean confirmActionPerformed() {
        return default_confirmActionPerformed();
    }
    public boolean default_confirmActionPerformed() {
        OAObject obj = updateObject;
        if (obj == null && hub != null) obj = (OAObject) hub.getAO();

        String msg = getConfirmMessage();
        String title = "Confirm";
        
        OAObjectEditQuery eq = null;
                
        if (obj != null) {
            switch (command) {
            case ClearAO:
                if (obj != null) {
                    if (hub.getLinkHub() != null) {
                        eq = OAObjectEditQueryDelegate.getConfirmPropertyChangeEditQuery( (OAObject) hub.getLinkHub().getAO(), hub.getLinkPath(), null, msg, title);
                    }
                }
                break;
            case Delete:
                eq = OAObjectEditQueryDelegate.getConfirmDeleteEditQuery(obj, msg, title);
                // also have remove checked
            case Remove:
                OAObjectEditQuery eqHold = eq;
                eq = OAObjectEditQueryDelegate.getVerifyRemoveEditQuery(getHub(), obj);
                if (!eq.getAllowed()) {
                    String s = eq.getResponse();
                    if (s == null) s = "";
                    else s = ", " + s;
                    JOptionPane.showMessageDialog(button, s, "Warning", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                if (eqHold != null) eq = eqHold;
                else eq = OAObjectEditQueryDelegate.getConfirmRemoveEditQuery(getHub(), obj, msg, title);
                break;
            case Add:
            case Insert:
            case New:
                eq = OAObjectEditQueryDelegate.getVerifyAddEditQuery(getHub(), null);
                if (!eq.getAllowed()) {
                    String s = eq.getResponse();
                    if (s == null) s = "";
                    else s = ", " + s;
                    JOptionPane.showMessageDialog(button, s, "Warning", JOptionPane.WARNING_MESSAGE);
                    return false;
                }
                eq = OAObjectEditQueryDelegate.getConfirmAddEditQuery(getHub(), null, msg, title);
                break;
            case Search:
                Hub hubx = hub.getLinkHub();
                String propx = null;
                if (hubx != null) propx = hub.getLinkPath();
                else {
                    hubx = hub.getMasterHub();
                    if (hubx != null) {
                        propx = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
                    }
                }
                if (hubx == null || propx == null) break;
                Object objx = hubx.getAO();
                if (!(objx instanceof OAObject)) break;
                eq = OAObjectEditQueryDelegate.getConfirmPropertyChangeEditQuery( (OAObject) objx, propx, objSearch, msg, title);
                break;
            }            
        }
        
        if (OAString.isNotEmpty(getMethodName())) {
            eq = OAObjectEditQueryDelegate.getVerifyPropertyChangeEditQuery(obj, getMethodName(), null, updateValue);
            if (!eq.getAllowed()) {
                String s = eq.getResponse();
                if (s == null) s = "";
                else s = ", " + s;
                JOptionPane.showMessageDialog(button, s, "Warning", JOptionPane.WARNING_MESSAGE);
                return false;
            }
            eq = OAObjectEditQueryDelegate.getConfirmPropertyChangeEditQuery(obj, getMethodName(), updateValue, msg, title);
        }
        
        if (eq != null) {
            msg = eq.getConfirmMessage();
            title = eq.getConfirmTitle();
        }
        
        if (OAString.isEmpty(msg) && compConfirm == null) return true;
        
        if (compConfirm == null) {
            int x = JOptionPane.showOptionDialog(OAJfcUtil.getWindow(button), msg, title, 0, JOptionPane.QUESTION_MESSAGE, null, new String[] { "Yes", "No" }, "Yes");
            return (x == 0);
        }
        getConfirmDialog().setVisible(true);
        return !getConfirmDialog().wasCancelled();
    }
    
    
    
//qqqqqqqqqqqqqqqq    
    @Override
    public String isValid(Object obj, Object newValue) {
        OAObjectEditQuery em = _isValid(obj);
        String result = null;
        if (em != null) {
            if (!em.getAllowed()) {
                result = em.getResponse();
                Throwable t = em.getThrowable();
                if (OAString.isEmpty(result) && t != null) {
                    for (; t!=null; t=t.getCause()) {
                        result = t.getMessage();
                        if (OAString.isNotEmpty(result)) break;
                    }
                    if (OAString.isEmpty(result)) result = em.getThrowable().toString();
                }
                else result = "invalid value";
            }
        }
        return result;
    }
    protected OAObjectEditQuery _isValid(Object obj) {
        OAObjectEditQuery eq = null;
   
        switch (command) {
        case ClearAO:
            if (hub.getLinkHub() != null) {
                eq = OAObjectEditQueryDelegate.getVerifyPropertyChangeEditQuery((OAObject) hub.getLinkHub().getAO(), hub.getLinkPath(), obj, null);
            }
            break;
        case Delete:
            if (obj instanceof OAObject) {
                eq = OAObjectEditQueryDelegate.getVerifyDeleteEditQuery((OAObject)obj);
            }
            // needs to also check remove
        case Remove:
            OAObjectEditQuery eqHold = eq;
            if (obj instanceof OAObject) {
                eq = OAObjectEditQueryDelegate.getVerifyRemoveEditQuery(getHub(), (OAObject) obj);
            }
            if (eqHold != null) eq = eqHold;
            break;
        case Add:
        case Insert:
        case New:
            if (obj instanceof OAObject) {
                eq = OAObjectEditQueryDelegate.getVerifyAddEditQuery(getHub(), (OAObject) obj);
            }
        case Search:
            Hub hubx = hub.getLinkHub();
            String propx = null;
            if (hubx != null) propx = hub.getLinkPath();
            else {
                hubx = hub.getMasterHub();
                if (hubx != null) {
                    propx = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
                }
            }
            if (hubx == null || propx == null) return null;
            Object objx = hubx.getAO();
            if (!(objx instanceof OAObject)) return null;
            eq = OAObjectEditQueryDelegate.getVerifyPropertyChangeEditQuery((OAObject) objx, propx, null, obj);
            break;
        }            
        
        if (OAString.isNotEmpty(getMethodName()) && (obj instanceof OAObject)) {
            eq = OAObjectEditQueryDelegate.getVerifyPropertyChangeEditQuery((OAObject)obj, getMethodName(), null, updateValue);
        }
        return eq;
    }
    
    
    private Object objSearch;
    public void actionPerformed(ActionEvent e) {
        default_actionPerformed(e);
    }
    public void default_actionPerformed(ActionEvent e) {
        
        if (!beforeActionPerformed()) return;
        if (button == null || !button.isEnabled()) return;
        
        OAObject obj = updateObject;
        if (obj == null && hub != null) obj = (OAObject) hub.getAO();
        

        if (command != null && command == OAButton.SEARCH) {
            objSearch = getSearchObject();
            if (!(objSearch instanceof OAObject)) {
                objSearch = null;
                return;
            }
            obj = (OAObject) objSearch;
        }        
        
        String s = isValid(obj, null); 
        if (OAString.isNotEmpty(s)) {
            JOptionPane.showMessageDialog(button, s, "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        OAPasswordDialog dlgPw; 
        if (button instanceof OAButton) {
            dlgPw = ((OAButton) button).getPasswordDialog();
        }
        else {
            dlgPw = getPasswordDialog();
        }
        if (dlgPw != null) {
            dlgPw.setVisible(true);
            if (dlgPw.wasCancelled()) return;
        }
        
        if (!confirmActionPerformed()) {
            objSearch = null;
            return;
        }
        
        JFileChooser fc = getSaveFileChooser();
        if (fc != null) {
            int i = fc.showSaveDialog(SwingUtilities.getWindowAncestor(ButtonController.this.button));
            if (i != JFileChooser.APPROVE_OPTION) return;
            File file = fc.getSelectedFile();
            if (file == null) return;
            // fileName = file.getPath();
        }
        else {
            fc = getOpenFileChooser();
            if (fc != null) {
                int i = fc.showOpenDialog(SwingUtilities.getWindowAncestor(ButtonController.this.button));
                if (i != JFileChooser.APPROVE_OPTION) return;
                File file = fc.getSelectedFile();
                if (file == null) return;
            }
        }

        try {
            boolean b = runActionPerformed();
            if (!bUseSwingWorker) reportActionCompleted(b, null);
        }
        catch (Exception ex) {
            reportActionCompleted(false, ex);
        }
        finally {
            objSearch = null;
        }
    }
    
    public void reportActionCompleted(boolean b, Exception ex) {
        if (ex != null) {
            LOG.log(Level.WARNING, "error while performing command action", ex);
            for (int i=0 ; i<10; i++) {
                Throwable t = ex.getCause();
                if (t == null || t == ex || !(t instanceof Exception)) { 
                    break;
                }
                ex = (Exception) t;
            }
            afterActionPerformedFailure("Command error: "+OAString.fmt(ex.getMessage(), "100L.").trim(), ex);
        }
        else {
            if (b) afterActionPerformed();
            else {
                afterActionPerformedFailure("Action was not completed", null);
            }
        }
    }
    
    public void afterActionPerformed() {
        default_afterActionPerformed();
    }
    public void default_afterActionPerformed() {
        String completedMessage = getCompletedMessage();
        String returnMessage = getReturnMessage();
        String displayMessage = "";

        boolean bUsedCompletedMsg = false;
        if (completedMessage != null) {
            Hub h = getHub();
            if (h != null) {
                Object obj = h.getAO();
                if (completedMessage != null && completedMessage.indexOf("<%=") >= 0 && obj instanceof OAObject) {
                    OATemplate temp = new OATemplate(completedMessage);
                    temp.setProperty("returnMessage", returnMessage);  // used by <%=$returnMessage%>
                    completedMessage = temp.process((OAObject) obj);
                    bUsedCompletedMsg = true;
                    if (completedMessage != null && completedMessage.indexOf('<') >=0 && completedMessage.toLowerCase().indexOf("<html>") < 0) completedMessage = "<html>" + completedMessage; 
                }
            }            
            displayMessage = completedMessage;
        }
        
        if (!bUsedCompletedMsg && returnMessage != null) {
            if (displayMessage.length() > 0) displayMessage += " ";
            displayMessage += returnMessage;
        }
        
        if (!OAString.isEmpty(displayMessage) && OAString.isEmpty(getConsoleProperty()) && compDisplay == null) {
            String s = OAString.lineBreak(displayMessage, 85, "\n", 20);
            JOptionPane.showMessageDialog(
                OAJfcUtil.getWindow(button), 
                s, "Command completed", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
        if (focusComponent == null) return;
        
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
    public void afterActionPerformedFailure(String msg, Exception e) {
        default_afterActionPerformedFailure(msg, e);
    }
    public void default_afterActionPerformedFailure(String msg, Exception e) {
        if (!OAString.isEmpty(msg) || e != null) {
            if (msg == null) msg = "";
            System.out.println(msg+", exception="+e);
            if (e != null) e.printStackTrace();

            String s = OAString.lineBreak(msg, 85, "\n", 20);
            JOptionPane.showMessageDialog(
                OAJfcUtil.getWindow(button), 
                s, "Command failed", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    

    protected boolean runActionPerformed() throws Exception {
        Window window = OAJfcUtil.getWindow(button);
        boolean b = false;
        try {
            if (window != null) {
                window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
            b = runActionPerformed2();
        }
        finally {
            if (window != null) {
                window.setCursor(Cursor.getDefaultCursor());
            }
        }
        return b;
    }
    
    private OAWaitDialog dlgWait;
    
    protected boolean runActionPerformed2() throws Exception {
        Hub mhub = getSelectHub();
        if (command == OAButton.ButtonCommand.Delete) {
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
                    return false;
                }
            }
        }

        boolean bResult = false;
        if (!bUseSwingWorker && compDisplay == null) {
            bResult = onActionPerformed();
            return bResult;
        }

        final Window window = OAJfcUtil.getWindow(button);
        if (dlgWait == null) {
            dlgWait = new OAWaitDialog(window, true);  // allowCancel, was false
        }
        
        
        dlgWait.getCancelButton().setText("Run in background");
        dlgWait.getCancelButton().setToolTipText("use this to close the dialog, and allow the the procss to run in the background");
        
        String s = processingTitle;
        if (s == null) s = "Processing";
        dlgWait.setTitle(s);

        s = processingMessage;
        if (s == null) {
            s = button.getText();
            if (s == null) s = "";
            else s = " \"" + s + "\"";
            s = "Please wait ... processing request" + s;
        }
        dlgWait.setStatus(s);

        s = getConsoleProperty();
        OAConsole con = null;
        if (!OAString.isEmpty(s)) {
            con = new OAConsole(getHub(), s, 45);
            con.setPreferredSize(14, 1, true);
            dlgWait.setConsole(con);
        }
        
        if (compDisplay != null) {
            dlgWait.setDisplayComponent(compDisplay);
            if (con != null) con.setPreferredSize(6, 1, true);
        }
        
        
        final AtomicInteger aiCompleted = new AtomicInteger(); 
        final OAConsole console = con;
        SwingWorker<Boolean, String> sw = new SwingWorker<Boolean, String>() {
            Exception exception;
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("");
                boolean b;
                try {
                    b = onActionPerformed();
                }
                catch (Exception e) {
                    b = false;
                    this.exception = e;
                }
                finally {
                    aiCompleted.incrementAndGet();
                }
                return b;
            }
            @Override
            protected void process(List<String> chunks) {
            }

            @Override
            protected void done() {
                
                synchronized (Lock) {
                    if (!dlgWait.wasCancelled() && console == null && compDisplay == null) {
                        if (dlgWait.isVisible()) {
                            dlgWait.setVisible(false);
                        }
                    }
                    else {
                        dlgWait.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        dlgWait.done();//hack
                        if (console != null) console.close();
                        JButton cmd = dlgWait.getCancelButton();
                        cmd.setText("Close");
                        cmd.setToolTipText("the command has completed, click to close window.");
                        
                        cmd.registerKeyboardAction(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                dlgWait.setVisible(false);
                            }
                        }, "xx", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), JComponent.WHEN_IN_FOCUSED_WINDOW);
                        cmd.registerKeyboardAction(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                dlgWait.setVisible(false);
                            }
                        }, "zz", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_IN_FOCUSED_WINDOW);
                        
                        
                        dlgWait.getProgressBar().setIndeterminate(false);
                        dlgWait.getProgressBar().setMaximum(100);
                        dlgWait.getProgressBar().setValue(100);
                    }   
                    
                    try {
                        if (!get() && exception == null) return;
                    }
                    catch (Exception e) {
                        exception = e;
                    }
                    
                    String msg = "";
                    msg = OAString.append(msg, getCompletedMessage(), ", ");
                    if (exception != null) {
                        OAString.append(msg, "Command had an exception, "+exception.getMessage()); 
                    }
                    msg = OAString.append(msg, getReturnMessage(), ", ");
                    
                    msg = OAString.trunc(msg, 300);
                    dlgWait.setStatus(msg);
                    dlgWait.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                    if (dlgWait.wasCancelled()) {
                        dlgWait.setVisible(true, false);
                    }
                    
                    reportActionCompleted(true, exception);
                }
            }
        };
        sw.execute();

        synchronized (Lock) {
            if (sw.getState() != StateValue.DONE && aiCompleted.get() == 0) {
                dlgWait.setVisible(true);  // the thread will wait until the dialog is closed
            }
        }

        if (aiCompleted.get() == 0) {
            // run in background
            // sw.cancel(true);  //qqqq need to test to see how it affects the thread.isInterrupted flag
        }
        else {
            sw.get(); // even though dlg.setVisible is modal, we need to check for an exception, if it was not cancelled
        }
        bResult = true;//aiCompleted.get() > 0;
        return bResult;
    }

    private final Object Lock = new Object();

    private boolean bManual;
    public void setManual(boolean b) {
        bManual = b;
    }
    public boolean getManual() {
        return bManual;
    }
    
    /**
     * This is where the actual action is handled.
     */
    protected boolean onActionPerformed() {
        return default_onActionPerformed();
    }
    public boolean default_onActionPerformed() {
        boolean b = false;
        b = _default_onActionPerformed();
        return b;
    }
    private boolean _default_onActionPerformed() {
        Object ho = null;
        Hub hub = getHub();
        if (hub == null) return true;
        ho = hub.getActiveObject();
        if (bManual) return true;

        /*was:
        if (confirmMessage != null) {
            if (!confirm()) return;
            if (hub != null && ho != hub.getActiveObject()) return;
        }
        */
        Object currentAO = hub.getAO();
        Hub mhub = getSelectHub();

        if (hub != null) {
            OAObject oaObj;
            int pos = hub.getPos();
            switch (command) {
            case Other:
                break;
            case Next:
                hub.setPos(pos + 1);
                if (currentAO != hub.getAO() && bEnableUndo) {
                    OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoDescription(), hub, currentAO, hub.getAO()));
                }
                break;
            case Previous:
                hub.setPos(pos - 1);
                if (currentAO != hub.getAO() && bEnableUndo) {
                    OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoDescription(), hub, currentAO, hub.getAO()));
                }
                break;
            case First:
                hub.setPos(0);
                if (currentAO != hub.getAO() && bEnableUndo) {
                    OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoDescription(), hub, currentAO, hub.getAO()));
                }
                break;
            case Last:
                if (hub.isMoreData()) hub.loadAllData();
                hub.setPos(hub.getSize() - 1);
                if (currentAO != hub.getAO() && bEnableUndo) {
                    OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO(getUndoDescription(), hub, currentAO, hub.getAO()));
                }
                break;

            case Save:
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
                        update(); // 20181006
                    }
                    catch (Exception e) {
                        msg = "Error while saving\n" + e;
                    }
                    if (msg != null) {
                        JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(button), msg, "Error", JOptionPane.ERROR_MESSAGE,null);
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
            case Delete:
                if (ho == null && mhub == null) break;

                if (bEnableUndo) {
                    OAUndoManager.startCompoundEdit(getUndoDescription());
                }
                try {
                    if (mhub != null && mhub.getSize() > 0) {
                        Object[] objs = mhub.toArray();
                        for (Object obj : objs) {
                            if (obj instanceof OAObject) {
                                int posx = hub.getPos(obj);
                                if (HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                                    getHub().remove(obj); // keep "noise" down
                                }
                                if (bEnableUndo) {
                                    OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoDescription(), hub, ho, posx));
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
                                OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoDescription(), hub, ho, hub.getPos()));
                            }
                            if (HubAddRemoveDelegate.isAllowAddRemove(getHub())) { // 20120720
                                getHub().remove(ho); // 20110215 remove first, so that cascading deletes are not so "noisy"
                            }
                            // else it can only be removed when delete is called (ex: a detail hub that is from a linkOne)
                            ((OAObject) ho).delete();
                        }
                        else {
                            if (hub != null) {
                                if (bEnableUndo) {
                                    OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoDescription(), hub, ho, hub.getPos()));
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
            case Remove:
                if (bEnableUndo) {
                    OAUndoManager.startCompoundEdit(getUndoDescription());
                }
                try {
                    if (mhub != null && mhub.getSize() > 0) {
                        Object[] objs = mhub.toArray();
                        for (Object obj : objs) {
                            if (obj instanceof OAObject) {
                                if (HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                                    int posx = hub.getPos(obj);
                                    OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoDescription(), hub, obj, posx));
                                    getHub().remove(obj);
                                }
                            }
                        }
                    }
                    else if (ho != null) {
                        if (bEnableUndo) {
                            OAUndoManager.add(OAUndoableEdit.createUndoableRemove(getUndoDescription(), hub, ho, hub.getPos()));
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
            case Cancel:
                /* was
                if (ho != null && ho instanceof OAObject) {
                    OAObject obj = (OAObject) ho;
                    obj.cancel();
                    if (obj.isNew()) obj.removeAll();
                }
                */
                break;
            case New:
            case Add:
                createNew(false);
                break;
            case Insert:
                createNew(true);
                break;
            case Up:
                ho = hub.getActiveObject();
                pos = hub.getPos();
                if (ho != null && pos > 0) {
                    hub.move(pos, pos - 1);
                    if (bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableMove(getUndoDescription(), hub, pos, pos - 1));
                    }
                    HubAODelegate.setActiveObjectForce(hub, ho);
                }
                break;
            case Down:
                ho = hub.getActiveObject();
                pos = hub.getPos();
                if (ho != null && hub.elementAt(pos + 1) != null) {
                    hub.move(pos, pos + 1);
                    if (bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableMove(getUndoDescription(), hub, pos, pos + 1));
                    }
                    HubAODelegate.setActiveObjectForce(hub, ho);
                }
                break;
            case ClearAO:
                ho = hub.getActiveObject();
                if (ho != null) {
                    hub.setAO(null);
                    if (bEnableUndo) {
                        OAUndoManager.add(OAUndoableEdit.createUndoableChangeAO("Set active object to null", hub, ho, null));
                    }
                }
                break;
            case Cut:
                ho = hub.getActiveObject();
                if (ho instanceof OAObject) addToClipboard((OAObject) ho);
                break;
            case Copy:
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
            case Paste:
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
            case Search:
                if (objSearch == null) break;
                Hub hubx = hub.getLinkHub();
                String propx = null;
                if (hubx != null) propx = hub.getLinkPath();
                else {
                    hubx = hub.getMasterHub();
                    if (hubx != null) {
                        propx = HubDetailDelegate.getPropertyFromMasterToDetail(hub);
                    }
                }
                if (hubx == null || propx == null) break;
                Object objx = hubx.getAO();
                if (!(objx instanceof OAObject)) break;
                ((OAObject)objx).setProperty(propx, objSearch);
                break;
            }
            
            
            if (methodName != null) {
                // Method[] method = OAReflect.getMethods(hub.getObjectClass(), methodName);
                
                String msg = null;
                if (mhub != null && mhub.getSize() > 0) {
                    
                    // see if there is a static method for the mhub
                    Method method = OAReflect.getMethod(hub.getObjectClass(), methodName, new Object[] {mhub});
                    if (method != null) {
                        try {
                            Object objx = method.invoke(null, mhub);
                        }
                        catch (Exception e) {
                            String msgx = "Error calling Method "+method+", using hub="+mhub;
                            throw new RuntimeException(msgx, e);
                        }
                        if (msg == null) {
                            msg = "processed " + mhub.getSize();
                        }
                    }
                    else {
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
                }
                else {
                    Object objx = OAReflect.executeMethod(hub.getAO(), methodName);
                    if (objx instanceof String) {
                        msg = (String) objx;
                    }
                }
                if (msg != null) {
                //    JOptionPane.showMessageDialog(OAJFCUtil.getWindow(button), msg, "Information", JOptionPane.INFORMATION_MESSAGE);
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
        if (updateProperty != null) {
            try {
                if (updateObject != null) {
                    updateObject.setProperty(updateProperty, updateValue);
                }
                else {
                    if (hubSelect != null) {
                        for (Object obj : hubSelect) {
                            if (obj instanceof OAObject) {
                                ((OAObject)obj).setProperty(updateProperty, updateValue);
                            }
                        }
                    }
                    if (getHub() != null) {
                        Object obj = getHub().getAO();
                        if (obj instanceof OAObject) {
                            ((OAObject)obj).setProperty(updateProperty, updateValue);
                        }
                    }
                }
            }
            catch (Exception ex) {
                throw new RuntimeException("ButtonController update property=" + updateProperty, ex);
            }
        }
        return true;
    }

    protected void createNew(boolean insertFlag) {
        Object obj;
        Class c = hub.getObjectClass();
        if (c == null) return;

        obj = OAObjectReflectDelegate.createNewObject(c);
        if (hub.contains(obj)) return; // 20110925: the createNew added it to the hub, since it was a selectAll hub

        if (insertFlag) {
            int pos = hub.getPos();
            if (pos < 0) pos = 0;
            hub.insert(obj, pos);
            if (bEnableUndo) {
                OAUndoManager.add(OAUndoableEdit.createUndoableInsert(getUndoDescription(), hub, obj, pos));
            }
        }
        else {
            hub.addElement(obj);
            if (bEnableUndo) {
                OAUndoManager.add(OAUndoableEdit.createUndoableAdd(getUndoDescription(), hub, obj));
            }
        }
        hub.setActiveObject(obj);
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
        
        // 20181015
        addEnabledCheck(getHub(), HubChangeListener.Type.AoNotNull);
        addEnabledEditQueryCheck(getHub(), methodName);
        addVisibleEditQueryCheck(getHub(), methodName);
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

        if (hub != null) obj = hub.getActiveObject();
        OAObject oaObj;
        if (obj instanceof OAObject) oaObj = (OAObject) obj;
        else oaObj = null;

        boolean flag = (hub != null && hub.isValid());
        boolean bAnyTime = false;
        
        if (enabledMode != null) {
            switch (enabledMode) {
            case UsesIsEnabled:
                flag = true;
                break;
            case Always:
                bAnyTime = true;
                flag = true;
                break;
            case ActiveObjectNotNull:
                if (!flag) break;
                if (hub != null) flag = hub.getAO() != null;
                break;
            case ActiveObjectNull:
                if (!flag) break;
                if (hub != null) flag = hub.getAO() == null;
                break;
            case HubIsValid:
                break;
            case HubIsNotEmpty:
                if (!flag) break;
                if (hub != null) flag = hub.getSize() > 0;
                break;
            case HubIsEmpty:
                if (!flag) break;
                if (hub != null) flag = hub.getSize() == 0;
                break;
            case AOPropertyIsNotEmpty:
                if (!flag) break;
                if (updateObject != null) {
                    if (updateObject instanceof OAObject) {
                        obj = ((OAObject) updateObject).getProperty(updateProperty);
                        flag = !OACompare.isEmpty(obj);
                    }
                }
                else if (oaObj != null) {
                    obj = oaObj.getProperty(updateProperty);
                    flag = !OACompare.isEmpty(obj);
                }
                break;
            case AOPropertyIsEmpty:
                if (!flag) break;
                if (updateObject != null) {
                    if (updateObject instanceof OAObject) {
                        obj = ((OAObject) updateObject).getProperty(updateProperty);
                        flag = OACompare.isEmpty(obj);
                    }
                }
                else if (oaObj != null) {
                    obj = oaObj.getProperty(updateProperty);
                    flag = OACompare.isEmpty(obj);
                }
                break;
            case SelectHubIsNotEmpty:
                if (!flag) break;
                flag = (hubSelect != null && hubSelect.getSize() > 0);
                break;
            case SelectHubIsEmpty:
                if (!flag) break;
                flag = (hubSelect != null && hubSelect.getSize() == 0);
                break;
            }
        }        
       
        if (flag && command != null && hub != null) {
            switch (command) {
            case Next:
                if (hub == null) {
                    flag = true; 
                    break;
                }
                int pos = hub.getPos();
                flag = hub.elementAt(pos + 1) != null;
                if (flag) {
                    if (oaObj != null && !bMasterControl && !bAnyTime &&  oaObj.getChanged()) flag = false;
                }
                break;
            case Previous:
                if (hub == null) {
                    flag = false; 
                    break;
                }
                pos = hub.getPos();
                flag = pos >= 0;
                if (flag) {
                    if (oaObj != null && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                }
                break;
            case First:
                flag = hub.getPos() != 0 && hub.getCurrentSize() > 0;
                if (oaObj != null && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                break;
            case Last:
                flag = hub.getSize() != 0;
                if (flag) {
                    if (oaObj != null && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                    else flag = hub.getPos() != hub.getSize() - 1;
                }
                break;
            case Save:
                flag = (obj != null);
                if (oaObj != null) {
                    flag = bAnyTime || oaObj.getChanged();
                    if (flag && hub != null && !bAnyTime && oaObj.isNew()) {  // 20180429 dont use save button if master is new and owns child hub 
                        Object objx = hub.getMasterObject();
                        if (objx instanceof OAObject) {
                            if ( ((OAObject) objx).isNew()) {
                                OALinkInfo li = HubDetailDelegate.getLinkInfoFromMasterHubToDetail(hub);
                                if (li != null && li.getOwner()) {
                                    flag = false;
                                }
                            }
                        }
                    }
                }
                break;
            case Cancel:
                if (obj == null) flag = false;
                else {
                    flag = (oaObj != null && (bAnyTime || oaObj.getChanged() || oaObj.getNew()));
                }
                break;
            case Remove:
                flag = (obj != null) || (hubSelect != null && hubSelect.size() > 0);
                if (flag && !HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                    flag = false;
                }
                break;
            case ClearAO:
                flag = obj != null;
                if (oaObj != null) {
                    if (hub.getLinkHub() != null) {
                       // flag = OAObjectEditQueryDelegate.getVerifyPropertyChange((OAObject)hub.getLinkHub().getAO(), hub.getLinkPath(), oaObj, null); 
                    }
                }
                break;
            case Delete:
                //was: flag = (obj != null && hub.getAllowDelete());
                flag = (obj != null);
                break;
            case New:
            case Insert:
            case Add:
                if (hub != null) flag = hub.isValid();
                if (flag && !bAnyTime && !bMasterControl && oaObj.getChanged()) flag = false;
                if (flag && !HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                    flag = (hub.getSize() == 0);
                    break;
                }
                break;
            case Up:
                flag = (obj != null && hub.getPos() > 0);
                break;
            case Down:
                flag = (obj != null && (hub.isMoreData() || hub.getPos() < (hub.getSize() - 1)));
                break;
            case Cut: // 20100111
            case Copy:
                flag = (obj != null);
                break;
            case Paste:
                flag = false;
                if (hub != null) {
                    OAObject objx = getClipboardObject();
                    if (objx != null && objx.getClass().equals(hub.getObjectClass())) {
                        if (!hub.contains(objx)) flag = true;
                    }
                }
                if (flag && !HubAddRemoveDelegate.isAllowAddRemove(getHub())) {
                    flag = (hub.getSize() == 0);
                    break;
                }
                break;
            default:
            }
            if (flag && !HubDelegate.isValid(hub)) flag = false;
        }
        
        
        if (flag) {
            OAObjectEditQuery eq;
            switch (command) {
            case Delete:
                if (oaObj != null) {
                    flag = OAObjectEditQueryDelegate.getAllowDelete(oaObj); 
                    flag = flag && oaObj.canDelete();
                    flag = flag && hub.canRemove(oaObj);
                }
                break;
            case Remove:
                if (oaObj != null) {
                    flag = OAObjectEditQueryDelegate.getAllowRemove(hub); 
                    flag = flag && hub.canRemove(oaObj);
                }
                break;
            case Add:
            case Insert:
            case New:
                if (hub != null) {
                    flag = hub.canAdd();
                    flag = flag && OAObjectEditQueryDelegate.getAllowAdd(hub); 
                    flag = flag && hub.canAdd();
                }
            }            
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

    private JComponent compDisplay;
    public void setDisplayComponent(JComponent comp) {
        this.compDisplay = comp;
    }
    public JComponent getDisplayComponent() {
        return compDisplay;
    }
    
    private JComponent compConfirm;
    public void setConfirmComponent(JComponent comp) {
        this.compConfirm = comp;
    }
    public JComponent getConfirmComponent() {
        return compConfirm;
    }

    private OAPasswordDialog dlgPassword;
    /**
     * Used to set the password that enables the user to run the command.
     */
    public void setPasswordDialog(OAPasswordDialog dlg) {
        this.dlgPassword = dlg;
        bCreatedPasswordDialog = false;
        if (dlgPassword != null) {
            if (button instanceof OAButton) {
                ((OAButton) button).setPasswordProtected(true);
            }
            else setPasswordProtected(true);
        }
    }
    public OAPasswordDialog getPasswordDialog() {
        if (this.dlgPassword != null) return this.dlgPassword;; 

        if (button instanceof OAButton) {
            OAButton ob = (OAButton) button;
            if (!ob.getPasswordProtected()) return null;
        }
        else {
            if (!getPasswordProtected()) return null;
        }
            

        bCreatedPasswordDialog = true;
        dlgPassword = new OAPasswordDialog(SwingUtilities.getWindowAncestor(this.button), "Enter Password") {
            @Override
            protected boolean isValidPassword(String pw) {
                if (pw == null) return false;
                
                String s;
                if (button instanceof OAButton) {
                    s = ((OAButton) button).getSHAHashPassword();
                }
                else {
                    s = getSHAHashPassword();
                }
                return pw.equals(s);
            }
        };
        
        return this.dlgPassword;
    }

    private boolean bPasswordProtected;
    private String password;
    private boolean bCreatedPasswordDialog;
    
    /**
     * @param pw encrypted password use SHAHash
     * @see OAString#getSHAHash(String) 
     */
    public void setSHAHashPassword(String pw) {
        this.password = pw;
        if (pw == null && bCreatedPasswordDialog) {
            this.dlgPassword = null;
            bCreatedPasswordDialog = false;
        }

        if (pw != null) {
            if (button instanceof OAButton) {
                ((OAButton) button).setPasswordProtected(true);
            }
            else setPasswordProtected(true);
        }
    }
    public String getSHAHashPassword() {
        return password;
    }

    public void setPasswordProtected(boolean b) {
        bPasswordProtected = b;
        if (!bPasswordProtected && bCreatedPasswordDialog) {
            this.dlgPassword = null;
            bCreatedPasswordDialog = false;
        }
    }
    public boolean getPasswordProtected() {
        return bPasswordProtected;
    }

    private OAConfirmDialog dlgConfirm;
    public OAConfirmDialog getConfirmDialog() {
        if (this.dlgConfirm != null) return this.dlgConfirm;; 

        dlgConfirm = new OAConfirmDialog(SwingUtilities.getWindowAncestor(this.button), button.getText(), getConfirmMessage());
        
        JPanel pan = new JPanel(new BorderLayout());
        pan.setBorder(new EmptyBorder(5, 5, 5, 5));
        pan.add(getConfirmComponent());
        dlgConfirm.add(pan, BorderLayout.CENTER); 
        dlgConfirm.resize();
        
        return this.dlgConfirm;
    }
    
}
