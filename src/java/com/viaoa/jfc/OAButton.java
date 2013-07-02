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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import com.viaoa.jfc.control.*;
import com.viaoa.jfc.table.OAButtonTableCellEditor;
import com.viaoa.jfc.table.OATableComponent;
import com.viaoa.object.OAObject;
import com.viaoa.util.OANotNullObject;
import com.viaoa.util.OAString;

import com.viaoa.hub.*;


public class OAButton extends JButton implements OATableComponent, OAJFCComponent {
    private OAButtonController control;
    public boolean DEBUG;
public boolean XXX;
    /** Save the active object in the Hub. 
        @see OAObject#save
    */
    public static final int SAVE = 0; 

    /** Cancel the active object in Hub. 
        @see OAObject#cancel
    */
    public static final int CANCEL = 1;

    /** Set active object to first object in Hub.
        @see Hub#setPos(int)
    */
    public static final int FIRST = 2;
    
    /** Set active object to last object in Hub.
        @see Hub#setPos(int)
    */
    public static final int LAST = 3;

    /** Set active object to next object in Hub.
        @see Hub#next
    */
    public static final int NEXT = 4;

    /** Set active object to previous object in Hub.
        @see Hub#previous
    */
    public static final int PREVIOUS = 5;

    /** Delete the active object in Hub. 
        @see OAObject#delete
    */
    public static final int DELETE = 6;


    /** 
        Creates a new object, adds it to the Hub, and makes it the active object.
        Calls OAObject.createNewObject.
        @see OAObject#createNewObject
        @see Hub#add
    */
    public static final int NEW = 7;

    /** 
        Remove the active object from the Hub.
        @see Hub#remove
    */
    public static final int REMOVE = 8;

    /** 
        Creates a new object, insert at current Hub position, and makes it the active object.
        Calls OAObject.createNewObject.
        @see OAObject#createNewObject
        @see Hub#insert
    */
    public static final int INSERT = 9;

    /** 
        Calls select for Hub. 
        @see Hub#select
    */
    public static final int SELECT = 10;

    /** 
        Same as SELECT, without a call to hub.select(). Must use an ActionEvent listener.
    */
    public static final int SELECT_MANUAL = 11;

    /** 
        Same as NEW, without creating new object.  Must use an ActionEvent listener.
    */
    public static final int NEW_MANUAL = 12;

    /** 
        Same as INSERT, without creating new object.  Must use an ActionEvent listener.
    */
    public static final int INSERT_MANUAL = 13;

    /** 
        Move the active object from the Hub up one position.
        @see Hub#move
    */
    public static final int UP = 14;
    /** 
        Move the active object from the Hub down one position.
        @see Hub#move
    */
    public static final int DOWN = 15;

    /** 
        Same as SAVE, without saving object.  Must use an ActionEvent listener.
    */
    public static final int SAVE_MANUAL = 16; 

    /** 
        Same as ADD, without creating new object.  Must use an ActionEvent listener.
    */
    public static final int ADD_MANUAL = 17;
    /** 
        Creates a new object, adds it to the Hub, and makes it the active object.
        Calls OAObject.createNewObject.
        @see OAObject#createNewObject
        @see Hub#add
    */
    public static final int ADD = 18;

    public static final int CUT = 19;
    public static final int COPY = 20;
    public static final int PASTE = 21;

    public static final int CLEARAO = 22;  // set hub AO = null

    static final int MAX = 23; //  <--------


    public static final String[] commandNames = new String[] { "Save", "Cancel", "First", "Last", "Next", "Previous", "Delete", "New",
        "Remove", "Insert", "Select", "Select", "New", "Insert", "Up", "Down", "Save", "Add ...", "Add ...",
        "Cut", "Copy", "Paste", "Clear"
    };

    
    /**
        Create a new OAButton that is not bound to a Hub, and is without a defined command.
    */
    public OAButton() {
        this(null, -1, null, null);
    }

    
    /**
       Create an unbound Button.
    */
    public OAButton(String text) {
        this(null, -1, text, null);
    }
    /**
       Create an unbound Button.
    */
    public OAButton(String text, Icon icon) {
        this(null, -1, text, icon);
    }
    /**
       Create an unbound Button.
    */
    public OAButton(Icon icon) {
        this(null, -1, null, icon);
    }

    /**
        Create a new OAButton that is bound to a Hub.
    */
    public OAButton(Hub hub) {
        this(hub, -1, null, null);
        setup(false, true, true, true);
    }
    /**
        Create a new OAButton that is bound to a Hub and command.
    */
    public OAButton(Hub hub, int command) {
        this(hub, command, null, null);
        setup(false, true, true, true);
    }

    /**
        Create a new OAButton that is bound to a Hub and command.
    */
    public OAButton(Hub hub, int command, String text) {
        this(hub, command, text, null);
        setup(false, true, false, true);
    }

    /**
        Create a new OAButton that is bound to a Hub and command.
    */
    public OAButton(Hub hub, int command, Icon icon) {
        this(hub, command, null, icon);
        setup(false, false, true, true);
    }

    
    /**
        Create a new OAButton that is bound to a Hub.
    */
    public OAButton(Hub hub, String text, Icon icon) {
        this(hub, -1, text, icon);
        setup(false, false, false, true);
    }

    /**
        Create a new OAButton that is bound to a Hub.
    */
    public OAButton(Hub hub, String text) {
        this(hub, -1, text, null);
        setup(false, true, false, true);
    }

    /**
        Create a new OAButton that is bound to a Hub.
    */
    public OAButton(Hub hub, Icon icon) {
        this(hub, -1, null, icon);
        setup(false, false, true, true);
    }
    
    /**
        Create a new OAButton that is bound to a Hub and command.
    */
    public OAButton(Hub hub, int command, String text, Icon icon) {
        control = new OAButtonController(hub, command) {
            @Override
            protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
                bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
                return OAButton.this.isEnabled(bIsCurrentlyEnabled);
            }
            @Override
            protected boolean isVisible(boolean bIsCurrentlyVisible) {
                return OAButton.this.isVisible(bIsCurrentlyVisible);
            }
        };
        if (text != null) setText(text);
        if (icon != null) setIcon(icon);
        setup(false, false, false, true);
    }

    @Override
    public ButtonController getController() {
        return control;
    }

    /**
        Retrieve an Icon from the viaoa.gui.icons directory.
        @param name name of file in icons directory.
    */
    public static Icon getIcon(String name) {
        URL url = OAButton.class.getResource("icons/"+name);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
        Retrieve an Icon from the viaoa.gui.icons directory.
        @param name name of file in icons directory.
    */
    public static Icon getDefaultIcon(int x) {
        if (x < 0 || x > commandNames.length) return null;
        String s = commandNames[x];
        s = OAString.convert(s,".", "");
        s = OAString.convert(s," ", "");
        s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
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
        boolean bText = false; // (getText() == null || getText().length() == 0);
        boolean bTtt = (getToolTipText() == null || getToolTipText().length() == 0);

        setup(true, bIcon, bText, bTtt);
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
        int command = getCommand();
        if (command < 0 || command > MAX) return;
        if (bIcon) setIcon(getDefaultIcon(command));
        if (bText) setText(commandNames[command]);
        if (bToolTip) {
            String s = commandNames[command];
            s = OAString.convert(s,".", "");
            s = OAString.convert(s," ", "");
            if (getHub() != null) {
                String s2 = getHub().getObjectClass().getName();
                int x = s2.lastIndexOf(".");
                if (x > 0) s2 = s2.substring(x+1);
                s2 = com.viaoa.util.OAString.convertHungarian(s2);
                s += " "+s2;
            }
            setToolTipText(s);
        }
    }


    /**
        Bind menuItem to automatically work with a Hub and command.
    */
    public void bind(Hub hub, int command) {
        setHub(hub);
        setCommand(command);
    }

    /**
        Bind menuItem to automatically work with a Hub.
        This will setAnyTime(false);
    */
    public void bind(Hub hub) {
        setHub(hub);
        setAnyTime(false);
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

    /** 
        If true and Hub is valid, then button will always be enabled, else button will be disabled
        based on value of command.
     */
    public void setAnytime(boolean b) {
        setAnyTime(b);
    }
    /** 
        If true and Hub is valid, then button will always be enabled, else button will be disabled
        based on value of command.
     */
    public void setAnyTime(boolean b) {
        control.setAnyTime(b);
    }
    /** 
        If true and Hub is valid, then button will always be enabled, else button will be disabled
        based on value of command.
     */
    public boolean getAnyTime() {
        return control.getAnyTime();
    }
    /** 
        If true and Hub is valid, then button will always be enabled, else button will be disabled
        based on value of command.
     */
    public boolean getAnytime() {
        return control.getAnyTime();
    }
    
    
    
    //*******  BeanInfo Properties ************************************
    // property "hub"
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
        Built in command.
        Set command value and set button text, tooltip, and icon.
    */
    public void setCommand(int command) {
        control.setCommand(command);
    }
    /**
        Built in command.
    */
    public int getCommand() {
        return control.getCommand();
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



    /** if the hub for this command has a masterHub, then it can control this button if
        this is set to true.  Default = true
    */
    public void setMasterControl(boolean b) {
        control.setMasterControl(b);
    }
    public boolean getMasterControl() {
        return control.getMasterControl();
    }
    
    /****
    private void checkSize(Dimension d) {
        if (d.width > 24) d.width = 22;
        if (d.height > 24) d.height = 22;
    }
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        if (control.iconType >= NAVIGATION_ICON_ONLY) checkSize(d);
        return d;
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getMinimumSize();
        if (control.iconType >= NAVIGATION_ICON_ONLY) checkSize(d);
        return d;
    }
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        if (control.iconType >= NAVIGATION_ICON_ONLY) checkSize(d);
        return d;
    }
    */
    
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
        If true, then command is not executed when button is clicked, else default command action is 
        executed.
    */
    public void setManual(boolean b) {
        control.setManual(b);
    }
    /**
        If true, then command is not executed when button is clicked, else default command action is 
        executed.
    */
    public boolean getManual() {
        return control.getManual();
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
        return control.getConfirmMessage();
    }

    /**
       Object to update whenever button is clicked.
    */
    public void setUpdateObject(Object object, String property, Object newValue) {
        control.setUpdateObject(object, property, newValue);
    }
    /**
        Update active object whenever button is clicked.
    */
    public void setUpdateObject(String property, Object newValue) {
        control.setUpdateObject(property, newValue);
    }

    
    /**
     * This is a callback method that can be overwritten to replace the default action when button is clicked.
     * By default, this will call the button controller, which will also call performAction().
     * @see #performAction to create a custom action.
     */
    public void onActionPerformed() {
        if (control != null) control.onActionPerformed();
    }

    /**
     * Called to perform custom action event.
     */
    public void performAction() {
    }
    
    public boolean onConfirm(String message) {
        if (control != null) {
            return control.confirm(message);
        }
        return true;
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
    public String getTableHeading() { //zzzzz
        return heading;
    }
    public void setTableHeading(String heading) { //zzzzz
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
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
    @Override
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
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
    
/** removed, to "not use" the enabledController, need to call it directly - since it has 2 params now, and will need 
 * to be turned on and off   
    
    @Override
    public void setEnabled(boolean b) {
        if (control != null) {
            b = control.getEnabledController().directSetEnabledCalled(b);
        }
        super.setEnabled(b);
    }
*/
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
    

    class OAButtonController extends ButtonController {
        public OAButtonController(Hub hub, int command) {
            super(hub, OAButton.this, command);
        }
        @Override
        protected String isValid(Object object, Object value) {
            String msg = OAButton.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!OAButton.this.isEnabled()) return;
            if (!OAButton.this.onConfirm(getConfirmMessage())) return;
            OAButton.this.onActionPerformed();  // default will then call this.onActionPerformed()
        }
        @Override
        protected void onActionPerformed() {
            super.onActionPerformed();
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

}




