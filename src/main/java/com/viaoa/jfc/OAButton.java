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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.viaoa.jfc.control.*;
import com.viaoa.jfc.dialog.OAPasswordDialog;
import com.viaoa.jfc.table.OAButtonTableCellEditor;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.object.OAObject;
import com.viaoa.util.OANotNullObject;
import com.viaoa.util.OAString;
import com.viaoa.hub.*;


public class OAButton extends JButton implements OATableComponent, OAJFCComponent {
    public boolean DEBUG;
    private OAButtonController control;
    
    public static ButtonCommand OTHER = ButtonCommand.Other;
    public static ButtonCommand UP = ButtonCommand.Up;
    public static ButtonCommand DOWN = ButtonCommand.Down;
    public static ButtonCommand SAVE = ButtonCommand.Save;
    public static ButtonCommand CANCEL = ButtonCommand.Cancel;
    public static ButtonCommand FIRST = ButtonCommand.First;
    public static ButtonCommand LAST = ButtonCommand.Last;
    public static ButtonCommand NEXT = ButtonCommand.Next;
    public static ButtonCommand PREVIOUS = ButtonCommand.Previous;
    public static ButtonCommand DELETE = ButtonCommand.Delete;
    public static ButtonCommand REMOVE = ButtonCommand.Remove;
    public static ButtonCommand NEW = ButtonCommand.New;
    public static ButtonCommand INSERT = ButtonCommand.Insert;
    public static ButtonCommand Add = ButtonCommand.Add;
    public static ButtonCommand CUT = ButtonCommand.Cut;
    public static ButtonCommand COPY = ButtonCommand.Copy;
    public static ButtonCommand PASTE = ButtonCommand.Paste;
    public static ButtonCommand NEW_MANUAL = ButtonCommand.NewManual;
    public static ButtonCommand ADD_MANUAL = ButtonCommand.AddManual;
    public static ButtonCommand CLEARAO = ButtonCommand.ClearAO;
    
    public enum ButtonCommand {
        Other, Up, Down, Save, Cancel, First, Last, 
        Next, Previous, Delete, Remove, New, Insert, Add, Cut, Copy, Paste,
        NewManual, AddManual, ClearAO
    }
    
    public static ButtonEnabledMode ALWAYS = ButtonEnabledMode.Always;
    public enum ButtonEnabledMode {
        UsesIsEnabled,
        Always,
        ActiveObjectNotNull,
        ActiveObjectNull,
        HubIsValid,
        HubIsNotEmpty,
        HubIsEmpty,
        AOPropertyIsNotEmpty,
        AOPropertyIsEmpty,
        SelectHubIsNotEmpty,
        SelectHubIsEmpty,
    }
    public static ButtonEnabledMode UsesIsEnabled = ButtonEnabledMode.UsesIsEnabled;
    public static ButtonEnabledMode Always = ButtonEnabledMode.Always;
    public static ButtonEnabledMode ActiveObjectNotNull = ButtonEnabledMode.ActiveObjectNotNull;
    public static ButtonEnabledMode ActiveObjectNull = ButtonEnabledMode.ActiveObjectNull;
    public static ButtonEnabledMode HubIsValid = ButtonEnabledMode.HubIsValid;
    public static ButtonEnabledMode HubIsNotEmpty = ButtonEnabledMode.HubIsNotEmpty;
    public static ButtonEnabledMode HubIsEmpty = ButtonEnabledMode.HubIsEmpty;
    public static ButtonEnabledMode AOPropertyIsNotEmpty = ButtonEnabledMode.AOPropertyIsNotEmpty;
    public static ButtonEnabledMode AOPropertyIsEmpty = ButtonEnabledMode.AOPropertyIsEmpty;
    public static ButtonEnabledMode SelectHubIsNotEmpty = ButtonEnabledMode.SelectHubIsNotEmpty;
    public static ButtonEnabledMode SelectHubIsEmpty = ButtonEnabledMode.SelectHubIsEmpty;
    
    /**
        Create a new OAButton that is bound to a Hub and command.
    */
    public OAButton(Hub hub, String text, Icon icon, ButtonEnabledMode enabledMode, ButtonCommand command) {
        this(hub, text, icon, enabledMode, command, true);
    }
    public OAButton(Hub hub, String text, Icon icon, ButtonEnabledMode enabledMode, ButtonCommand command, boolean bCallSetup) {
        if (text != null) setText(text);
        if (icon != null) setIcon(icon);
        
        if (command == null) command = ButtonCommand.Other;
        
        if (enabledMode == null) {
            
            // first, last, new,insert,add,nwe_manual, add_manual            
            // get default enabledMode
            switch (command) {
            case Other:
                if (hub != null) {
                    enabledMode = ButtonEnabledMode.ActiveObjectNotNull;
                }
                else enabledMode = ButtonEnabledMode.UsesIsEnabled;
                break;
            case First:
            case Last:
            case New:
            case Insert:
            case Add:
            case NewManual:
            case AddManual:
                enabledMode = ButtonEnabledMode.HubIsValid;
                break;
            default:
                enabledMode = ButtonEnabledMode.ActiveObjectNotNull;
                break;
            }
        }
        
        control = new OAButtonController(hub, enabledMode, command) {
        };
        
        if (bCallSetup) setup();
    }
    
    public OAButton() {
        this(null, null, null, null, null);
    }
    public OAButton(String text) {
        this(null, text, null, null, null);
    }
    public OAButton(String text, Icon icon) {
        this(null, text, icon, null, null);
    }
    public OAButton(Icon icon) {
        this(null, null, icon, null, null);
    }
    public OAButton(Icon icon, boolean bCallSetup) {
        this(null, null, icon, null, null, bCallSetup);
    }
    public OAButton(Hub hub) {
        this(hub, null, null, null, null);
    }
    public OAButton(Hub hub, ButtonCommand command) {
        this(hub, null, null, null, command);
    }
    public OAButton(Hub hub, ButtonEnabledMode enabledMode) {
        this(hub, null, null, enabledMode, null);
    }
    
    public OAButton(Hub hub, String text) {
        this(hub, text, null, null, null);
    }
    public OAButton(Hub hub, String text, ButtonCommand command) {
        this(hub, text, null, null, command);
    }
    public OAButton(Hub hub, ButtonCommand command, String text) {
        this(hub, text, null, null, command);
    }
    public OAButton(Hub hub, String text, ButtonEnabledMode enabledMode) {
        this(hub, text, null, enabledMode, null);
    }

    public OAButton(Hub hub, Icon icon) {
        this(hub, null, icon, null, null);
    }
    public OAButton(Hub hub, Icon icon, ButtonCommand command) {
        this(hub, null, icon, null, command);
    }
    public OAButton(Hub hub, Icon icon, ButtonEnabledMode enabledMode) {
        this(hub, null, icon, enabledMode, null);
    }
    
    public OAButton(Hub hub, String text, Icon icon) {
        this(hub, text, icon, null, null);
    }
    public OAButton(Hub hub, String text, Icon icon, ButtonCommand command) {
        this(hub, text, icon, null, command);
    }
    public OAButton(Hub hub, String text, Icon icon, ButtonEnabledMode enabledMode) {
        this(hub, text, icon, enabledMode, null);
    }

    
    @Override
    public ButtonController getController() {
        return control;
    }
    
    
    /**
        Built in command.
        Set command value and set button text, tooltip, and icon.
    */
    public void setCommand(ButtonCommand command) {
        if (command == ButtonCommand.NewManual) {
            control.setCommand(ButtonCommand.New);
            setManual(true);
        }
        else if (command == ButtonCommand.AddManual) {
            control.setCommand(ButtonCommand.Add);
            setManual(true);
        }
        control.setCommand(command);
    }
    /**
        Built in command.
    */
    public ButtonCommand getCommand() {
        return control.getCommand();
    }
    
    public void setManual(boolean b) {
        control.setManual(b);
    }
    public boolean getManual() {
        return control.getManual();
    }
    
    
    public void setEnabledMode(ButtonEnabledMode mode) {
        control.setEnabledMode(mode);
    }
    public ButtonEnabledMode getEnabledMode() {
        return control.getEnabledMode();
    }

    
    
//qqqqqqqqqqq    


    /**
        Retrieve an Icon from the viaoa.gui.icons directory.
        @param name name of file in icons directory.
    */
    public static Icon getIcon(String name) {
        URL url = OAButton.class.getResource("icons/"+name);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    public void setDefaultIcon() {
        ButtonCommand cmd = getCommand();
        if (cmd == null) setIcon(null);
        else setIcon(getDefaultIcon(cmd));
    }
    /**
        Retrieve an Icon from the viaoa.gui.icons directory.
        @param name name of file in icons directory.
    */
    public static Icon getDefaultIcon(ButtonCommand cmd) {
        if (cmd == null) return null;
        int x = cmd.ordinal();
        String s = cmd.name();
        s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
        if (s.endsWith("Manual")) s = s.substring(0, s.length()-6);
        URL url = OAButton.class.getResource("icons/"+s+".gif");
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
        Sets the default icon, and tooltip (if they are not already set) based on the value of command.
        Also calls setup(this).   Note: does not set default Text.
        @see #setup(boolean,boolean,boolean,boolean)
    */
    public void setup() {
        boolean bIcon = (getIcon() == null);
        
        // use setup(b,b,b,b) if it needs to be set, or setDefaultText
        boolean bText = false;// (getText() == null || getText().length() == 0);  
        boolean bTtt;
        if (control != null) {
            bTtt = (control.getCommand() != ButtonCommand.Other) && (getToolTipText() == null || getToolTipText().length() == 0);
        }
        else bTtt = false;

        setup(true, bIcon, bText, bTtt);
    }
    public static String getDefaultText(ButtonCommand cmd) {
        if (cmd == null) return "";
        String s = cmd.name();
        if (s.indexOf("Manual") > 0) {
            s = s.substring(0, s.length()-6);
        }
        return s;
    }
    public void setDefaultText() {
        ButtonCommand cmd = getCommand();
        setText(getDefaultText(cmd));
    }
    
    /**
        Sets the default icon, text, and tooltip based on the value of command. 
        @param bSetup if true, calls setup(this) to set border, mouseover.
        @param bIcon if true, calls getIcon(command) to set icon
        @param bText if true, set to command name
        @parma bToolTip if true, set to command name plus name of object in Hub
    */
    public void setup(boolean bSetup, boolean bIcon, boolean bText, boolean bToolTip) {
        if (bSetup) this.setup(this);
        ButtonCommand cmd = getCommand();
        if (cmd == null) {
            if (bIcon) setIcon(null);
            if (bText) setText(null);
            if (bToolTip) setToolTipText(null);
            return;
        }
        if (bIcon) setIcon(getDefaultIcon(cmd));
        
        if (bText) {
            setDefaultText();
        }
        if (bToolTip) {
            String s = cmd.name();
            if (s.indexOf("Manual") > 0) {
                s = s.substring(0, s.length()-6);
            }
            if (getHub() != null) {
                String s2 = getHub().getObjectClass().getSimpleName();
                s2 = com.viaoa.util.OAString.convertHungarian(s2);
                s += " "+s2;
            }
            setToolTipText(s);
        }
    }


    /**
        Bind menuItem to automatically work with a Hub and command.
    */
    public void bind(Hub hub, ButtonCommand command) {
        setHub(hub);
        setCommand(command);
    }

    /**
        Bind menuItem to automatically work with a Hub.
        This will setAnyTime(false);
    */
    public void bind(Hub hub) {
        setHub(hub);
        setCommand(null);
    }

    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public void setUndoDescription(String s) {
        control.setUndoDescription(s);
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return control.getUndoDescription();
    }
    
    /**
        Flag to enable undo, default is true.
    */
    public void setEnableUndo(boolean b) {
        control.setEnableUndo(b);
    }
    /**
        Flag to enable undo, default is true.
    */
    public boolean getEnableUndo() {
        return control.getEnableUndo();
    }

    public void setText(String s) {
        super.setText( (s==null)?"":s );
    }

    public void setHub(Hub hub) {
        control.setHub(hub);        
    }
    public Hub getHub() {
        if (control == null) return null;
        return control.getHub();
    }

    public void setMultiSelectHub(Hub hubMultiSelect) {
        if (control == null) return;
        control.setMultiSelectHub(hubMultiSelect);        
    }
    public Hub getHubMultiSelect() {
        if (control == null) return null;
        return control.getMultiSelectHub();
    }
    public Hub getMultiSelectHub() {
        if (control == null) return null;
        return control.getMultiSelectHub();
    }
    

    /**
        Method in object to execute on active object in hub. 
    */
    public void setMethodName(String methodName) {
        control.setMethodName(methodName);
    }
    /**
        Method in object to execute on active object in hub. 
    */
    public String getMethodName() {
        return control.getMethodName();
    }

    public void setOpenFileChooser(JFileChooser fc) {
        control.setOpenFileChooser(fc);
    }
    public JFileChooser getOpenFileChooser() {
        return control.getOpenFileChooser();
    }
    public void setSaveFileChooser(JFileChooser fc) {
        control.setSaveFileChooser(fc);
    }
    public JFileChooser getSaveFileChooser() {
        return control.getSaveFileChooser();
    }
    
    public void setConsoleProperty(String prop) {
        control.setConsoleProperty(prop);
    }
    public String getConsoleProperty() {
        return control.getConsoleProperty();
    }

    

    /** if the hub for this command has a masterHub, then it can control this button if
        this is set to true.  Default = true
    */
    public void setMasterControl(boolean b) {
        control.setMasterControl(b);
    }
    public boolean getMasterControl() {
        return control.getMasterControl();
    }
    
    /** 
        Returns the component that will receive focus when this button is clicked. 
    */
    public JComponent getFocusComponent() {
        return control.getFocusComponent();
    }
    /** 
        Set the component that will receive focus when this button is clicked. 
    */
    public void setFocusComponent(JComponent focusComponent) {
        control.setFocusComponent(focusComponent);
        if (focusComponent != null) setFocusPainted(false);
    }

    
    /**
        Same as setupButton.
        @see #setupButton
    */
    public static void setup(AbstractButton cmd) {
        setupButton(cmd);
    }
    public static void setup(OASplitButton cmd) {
        setupButton(cmd);
    }
    /**
        Called automatically for all new OAButtons if AutoSetup is true (default).<br>
        Does the following:
        <ul>
        <li>setFocusPainted(false)
        <li>setBorderPainted(false)
        <li>setMargin(new Insets(1,1,1,1))
        <li>public void mouseEntered(MouseEvent e) { setBorderPainted(true) }
        <li>public void mouseExited(MouseEvent e)  { setBorderPainted(false) }
        </ul>
        @see #setAutoSetup
        @see JComponent#setRequestFocusEnabled
    */
    public static void setupButton(AbstractButton cmd) {
        _setupButton(cmd);
    }
  
    
    public static void setupButton(final OASplitButton sb) {
        final AbstractButton cmd1 = sb.getMainButton();
        final AbstractButton cmd2 = sb.getDropDownButton();
        
        cmd1.setBorderPainted(false);
        cmd1.setContentAreaFilled(false);
        // cmd1.setMargin(new Insets(1,1,1,1));
        
        cmd2.setBorderPainted(false);
        cmd2.setContentAreaFilled(false);
        // cmd2.setMargin(new Insets(1,1,1,1));

        
        JPopupMenu popup = null;
        if (sb instanceof OAMultiButtonSplitButton) {
            OAMultiButtonSplitButton msb = (OAMultiButtonSplitButton) sb;
            popup = msb.getPopupMenu();
        }
        if (popup != null) {
            popup.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    
                }
                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    cmd1.setContentAreaFilled(false);
                    cmd2.setContentAreaFilled(false);
                    cmd1.setBorderPainted(false);
                    cmd2.setBorderPainted(false);
                }
                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
            });
        }
        
        final JPopupMenu pm = popup;
        
        MouseAdapter ma = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (sb.isEnabled()) {
                    cmd1.setBorderPainted(true);
                    if (cmd1.isEnabled()) {
                        if (e.getComponent() == cmd1) {
                            cmd1.setContentAreaFilled(true);
                        }
                    }
                    cmd2.setBorderPainted(true);
                    if (e.getComponent() == cmd2) {
                        cmd2.setContentAreaFilled(true);
                    }
                }
            }
            public void mouseExited(MouseEvent e) {
                cmd1.setContentAreaFilled(false);
                if (pm == null || !pm.isVisible()) {
                    cmd2.setContentAreaFilled(false);
                    cmd1.setBorderPainted(false);
                    cmd2.setBorderPainted(false);
                }
            }
        };
        cmd1.addMouseListener(ma);
        cmd2.addMouseListener(ma);
    }    

    private static void _setupButton(final AbstractButton cmd) {
        cmd.setFocusPainted(false);
        // 2004/11/10 removed. If focus is not called, then previous focus component will not get focus lost event
        // cmd.setRequestFocusEnabled(false); // 2003/6/3
        cmd.setBorderPainted(false);
        cmd.setContentAreaFilled(false);
        cmd.setMargin(new Insets(1,1,1,1));
        
        cmd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (cmd.isEnabled()) {
                    cmd.setContentAreaFilled(true);
                    cmd.setBorderPainted(true);
                }
            }
            public void mouseExited(MouseEvent e) {
                boolean b = false;
                if (cmd instanceof JToggleButton) {
                    if ( ((JToggleButton)cmd).isSelected() ) b = true;
                }
                cmd.setBorderPainted(b);
                cmd.setContentAreaFilled(b);
            }
        });

        if (cmd instanceof JToggleButton) {
            cmd.addChangeListener(new ChangeListener() {
                public void stateChanged(ChangeEvent e) {
                    if (cmd.isSelected()) {
                        if (cmd.isEnabled()) {
                            cmd.setContentAreaFilled(true);
                            cmd.setBorderPainted(true);
                        }
                    }
                    else {
                        cmd.setBorderPainted(false);
                        cmd.setContentAreaFilled(false);
                    }
                }
            });
        }
        
    }

    /**
        Popup message used to confirm button click before running code.
    */
    public void setConfirmMessage(String msg) {
        control.setConfirmMessage(msg);
    }
    /**
        Popup message used to confirm button click before running code.
    */
    public String getConfirmMessage() {
        return control.default_getConfirmMessage();
    }

    /**
        Popup message when command is completed
    */
    public void setCompletedMessage(String msg) {
        control.setCompletedMessage(msg);
    }
    public String getCompletedMessage() {
        return control.default_getCompletedMessage();
    }
    
    /**
       Object to update whenever button is clicked.
    */
    public void setUpdateObject(OAObject object, String property, Object newValue) {
        control.setUpdateObject(object, property, newValue);
    }
    /**
        Update active object whenever button is clicked.
        If there is a multiSelectHub, then each object in it will also be updated.
    */
    public void setUpdateObject(String property, Object newValue) {
        control.setUpdateObject(property, newValue);
    }

    
    /**
     * 
     * @param keyStroke KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK, false)
     */
    public void registerKeyStroke(KeyStroke keyStroke) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "oabutton");
        getActionMap().put("oabutton", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (control != null) control.actionPerformed(e);
            }
        });        
    }    
    public void registerKeyStroke(KeyStroke keyStroke, JComponent focusComponent) {
        if (focusComponent == null) return;
        focusComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyStroke, "oabutton");
        getActionMap().put("oabutton", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (control != null) control.actionPerformed(e);
            }
        });        
    }    
    
    
    
    // ----- OATableComponent Interface methods -----------------------
    private OATable table;
    private String heading;
    public void setTable(OATable table) {
        this.table = table;
    }
    public OATable getTable() {
        return table;
    }
    public void setColumns(int x) {
        this.columns = x;
        if (table != null) table.setColumnWidth(table.getColumnIndex(this),super.getPreferredSize().width);
    }
    private int columns;
    @Override
    public int getColumns() {
        return columns;
    }    

    public String getPropertyPath() {
        return null;
    }
    public void setPropertyPath(String path) {
    }
    public String getTableHeading() { 
        return heading;
    }
    public void setTableHeading(String heading) { 
        this.heading = heading;
        if (table != null) table.setColumnHeading(table.getColumnIndex(this),heading);
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getPreferredSize();
        return d;
    }

    
    /** called by getTableCellRendererComponent */
    @Override
    public Component getTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return this;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,boolean wasChanged, boolean wasMouseOver) {
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }

    
    private OAButtonTableCellEditor tableCellEditor;
    public TableCellEditor getTableCellEditor() {
        if (tableCellEditor == null) {
            tableCellEditor = new OAButtonTableCellEditor(this);
        }
        return tableCellEditor;
    }
    
    @Override
    public String getFormat() {
        return null;
    }

    /**
     * Other Hub/Property used to determine if component is enabled.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setEnabled(Hub hub) {
        control.getEnabledController().add(hub);
    }
    public void setEnabled(Hub hub, String prop) {
        control.getEnabledController().add(hub, prop);
    }
    public void setEnabled(Hub hub, String prop, Object compareValue) {
        control.getEnabledController().add(hub, prop, compareValue);
    }
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }
    
    /**
     * Other Hub/Property used to determine if component is visible.
     * @param hub 
     * @param prop if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
     */
    public void setVisible(Hub hub) {
        control.getVisibleController().add(hub);
    }    
    public void setVisible(Hub hub, String prop) {
        control.getVisibleController().add(hub, prop);
    }    
    public void setVisible(Hub hub, String prop, Object compareValue) {
        control.getVisibleController().add(hub, prop, compareValue);
    }    
    protected boolean isVisible(boolean bIsCurrentlyVisible) {
        return bIsCurrentlyVisible;
    }


    /**
     * This is a callback method that can be overwritten to determine if the component should be visible or not.
     * @return null if no errors, else error message
     */
    protected String isValid(Object object, Object value) {
        return null;
    }

    public void setUseSwingWorker(boolean b) {
        if (control == null) return;
        control.setUseSwingWorker(b);
    }
    public boolean getUseSwingWorker() {
        if (control == null) return false;
        return control.getUseSwingWorker();
    }
    public void setProcessingText(String title, String msg) {
        if (control == null) return;
        control.setProcessingText(title, msg);
    }

    // this can be overwritten to customize an object copy.
    protected OAObject createCopy(OAObject obj) {
        return control._createCopy(obj);
    }
    

//qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq

    // ActionPerformed methods
    public boolean beforeActionPerformed() {
        if (control == null) return false;
        return control.default_beforeActionPerformed();
    }    
    public boolean confirmActionPerformed() {
        return control.default_confirmActionPerformed();
    }
    /** This is where the "real" work is done when actionPerformed is called. */ 
    protected boolean onActionPerformed() {
        if (control == null) return false;
        return control.default_onActionPerformed();
    }
    public void afterActionPerformed() {
        control.default_afterActionPerformed();
    }
    public void afterActionPerformedFailure(String msg, Exception e) {
        control.default_afterActionPerformedFailure(msg, e);
    }
    
    
    class OAButtonController extends ButtonController {
        public OAButtonController(Hub hub, ButtonEnabledMode enabledMode, ButtonCommand command) {
            super(hub, OAButton.this, enabledMode, command);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OAButton.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }

        @Override
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OAButton.this.isEnabled(bIsCurrentlyEnabled);
        }
        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            return OAButton.this.isVisible(bIsCurrentlyVisible);
        }
        
        
        // ActionPerformed
        @Override
        public boolean beforeActionPerformed() {
            return OAButton.this.beforeActionPerformed();
        }
        @Override
        public String getConfirmMessage() {
            return OAButton.this.getConfirmMessage();
        }
        @Override
        public boolean confirmActionPerformed() {
            return OAButton.this.confirmActionPerformed();
        }
        @Override
        protected boolean onActionPerformed() {
            return OAButton.this.onActionPerformed();
        }
        @Override
        public void afterActionPerformed() {
            OAButton.this.afterActionPerformed();
        }
        @Override
        public void afterActionPerformedFailure(String msg, Exception e) {
            OAButton.this.afterActionPerformedFailure(msg, e);
        }
        @Override
        public String getCompletedMessage() {
            return OAButton.this.getCompletedMessage();
        }
        
        @Override
        protected OAObject createCopy(OAObject obj) {
            obj = OAButton.this.createCopy(obj);
            return obj;
        }
        protected OAObject _createCopy(OAObject obj) {
            return super.createCopy(obj);
        }
   }

    public void setAnytime(boolean b) {
        control.setAnytime(b);
    }
    public void setAnyTime(boolean b) {
        control.setAnytime(b);
    }
    
    /**
     * Component to display in the processing window 
     */
    public void setDisplayComponent(JComponent comp) {
        control.setDisplayComponent(comp);
    }
    public JComponent getDisplayComponent() {
        return control.getDisplayComponent();
    }

    public void setPasswordDialog(OAPasswordDialog dlg) {
        control.setPasswordDialog(dlg);
    }
    public OAPasswordDialog getPasswordDialog() {
        return control.getPasswordDialog();
    }

    /**
     * Used to password protect the command. 
     * Note: this can be overwritten, to be called when the user input pw needs to be verified.  
     * To do this, then call setPasswordProtected(true)
     * 
     * @param pw SHAHash encryted password
     * @see OAString#getSHAHash(String)
     * @see #setPasswordProtected
     */
    public void setSHAHashPassword(String pw) {
        control.setSHAHashPassword(pw);
    }
    public String getSHAHashPassword() {
        return control.getSHAHashPassword();
    }

    public void setPasswordProtected(boolean b) {
        control.setPasswordProtected(b);
    }
    public boolean getPasswordProtected() {
        return control.getPasswordProtected();
    }
    
}
