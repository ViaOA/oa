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
import javax.swing.event.*;
import com.viaoa.jfc.OAButton.Command;
import com.viaoa.jfc.OAButton.EnabledMode;
import com.viaoa.jfc.control.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;
import com.viaoa.hub.*;

public class OAMenuItem extends JMenuItem implements OAJFCComponent {
    private OAMenuItemController control;

    /**
     * Create a new OAMenuItem that is bound to a Hub and command.
     */
    public OAMenuItem(Hub hub, String text, Icon icon, EnabledMode enabledMode, Command command) {
        if (text != null) setText(text);
        if (icon != null) setIcon(icon);

        if (enabledMode == null) enabledMode = EnabledMode.ActiveObjectNotNull;
        if (command == null) command = Command.Other;

        control = new OAMenuItemController(hub, enabledMode, command) {
            @Override
            protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
                bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
                return OAMenuItem.this.isEnabled(bIsCurrentlyEnabled);
            }

            @Override
            protected boolean isVisible(boolean bIsCurrentlyVisible) {
                return OAMenuItem.this.isVisible(bIsCurrentlyVisible);
            }

            @Override
            public String getCompletedMessage() {
                return OAMenuItem.this.getCompletedMessage();
            }
        };

        setup();
    }

    public OAMenuItem() {
        this(null, null, null, null, null);
    }

    public OAMenuItem(String text) {
        this(null, text, null, null, null);
    }

    public OAMenuItem(String text, Icon icon) {
        this(null, text, icon, null, null);
    }

    public OAMenuItem(Icon icon) {
        this(null, null, icon, null, null);
    }

    public OAMenuItem(Hub hub) {
        this(hub, null, null, null, null);
    }

    public OAMenuItem(Hub hub, Command command) {
        this(hub, null, null, null, command);
    }

    public OAMenuItem(Hub hub, String text) {
        this(hub, text, null, null, null);
    }

    public OAMenuItem(Hub hub, String text, Command command) {
        this(hub, text, null, null, command);
    }

    public OAMenuItem(Hub hub, Icon icon) {
        this(hub, null, icon, null, null);
    }

    public OAMenuItem(Hub hub, Icon icon, Command command) {
        this(hub, null, icon, null, command);
    }

    public OAMenuItem(Hub hub, String text, Icon icon) {
        this(hub, text, icon, null, null);
    }

    @Override
    public ButtonController getController() {
        return control;
    }

    /**
     * Built in command. Set command value and set button text, tooltip, and icon.
     */
    public void setCommand(Command command) {
        control.setCommand(command);
    }

    /**
     * Built in command.
     */
    public Command getCommand() {
        return control.getCommand();
    }

    public void setEnabledMode(EnabledMode mode) {
        control.setEnabledMode(mode);
    }

    public EnabledMode getEnabledMode() {
        return control.getEnabledMode();
    }

    // qqqqqqqqqqq

    /**
     * Retrieve an Icon from the viaoa.gui.icons directory.
     * 
     * @param name
     *            name of file in icons directory.
     */
    public static Icon getIcon(String name) {
        URL url = OAMenuItem.class.getResource("icons/" + name);
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
     * Retrieve an Icon from the viaoa.gui.icons directory.
     * 
     * @param name
     *            name of file in icons directory.
     */
    public static Icon getDefaultIcon(Command cmd) {
        if (cmd == null) return null;
        int x = cmd.ordinal();
        String s = cmd.name();
        s = Character.toLowerCase(s.charAt(0)) + s.substring(1);
        URL url = OAMenuItem.class.getResource("icons/" + s + ".gif");
        if (url == null) return null;
        return new ImageIcon(url);
    }

    /**
     * Sets the default icon, and tooltip (if they are not already set) based on the value of command.
     * Also calls setup(this). Note: does not set default Text.
     * 
     * @see #setup(boolean,boolean,boolean,boolean)
     */
    public void setup() {
        boolean bIcon = (getIcon() == null);
        boolean bText = false; // (getText() == null || getText().length() == 0);
        boolean bTtt = (getToolTipText() == null || getToolTipText().length() == 0);

        setup(true, bIcon, bText, bTtt);
    }

    /**
     * Sets the default icon, text, and tooltip based on the value of command.
     * 
     * @param bSetup
     *            if true, calls setup(this) to set border, mouseover.
     * @param bIcon
     *            if true, calls getIcon(command) to set icon
     * @param bText
     *            if true, set to command name
     * @parma bToolTip if true, set to command name plus name of object in Hub
     */
    public void setup(boolean bSetup, boolean bIcon, boolean bText, boolean bToolTip) {
        if (bSetup) this.setup(this);
        Command cmd = getCommand();
        if (cmd == null) {
            if (bIcon) setIcon(null);
            if (bText) setText(null);
            if (bToolTip) setToolTipText(null);
            return;
        }
        if (bIcon) setIcon(getDefaultIcon(cmd));

        if (bText) setText(cmd.name());
        if (bToolTip) {
            String s = cmd.name();
            if (getHub() != null) {
                String s2 = getHub().getObjectClass().getSimpleName();
                s2 = com.viaoa.util.OAString.convertHungarian(s2);
                s += " " + s2;
            }
            setToolTipText(s);
        }
    }

    /**
     * Bind menuItem to automatically work with a Hub and command.
     */
    public void bind(Hub hub, Command command) {
        setHub(hub);
        setCommand(command);
    }

    /**
     * Bind menuItem to automatically work with a Hub. This will setAnyTime(false);
     */
    public void bind(Hub hub) {
        setHub(hub);
        setCommand(null);
    }

    /**
     * Description to use for Undo and Redo presentation names.
     * 
     * @see OAUndoableEdit#setPresentationName
     */
    public void setUndoDescription(String s) {
        control.setUndoDescription(s);
    }

    /**
     * Description to use for Undo and Redo presentation names.
     * 
     * @see OAUndoableEdit#setPresentationName
     */
    public String getUndoDescription() {
        return control.getUndoDescription();
    }

    /**
     * Flag to enable undo, default is true.
     */
    public void setEnableUndo(boolean b) {
        control.setEnableUndo(b);
    }

    /**
     * Flag to enable undo, default is true.
     */
    public boolean getEnableUndo() {
        return control.getEnableUndo();
    }

    public void setText(String s) {
        super.setText((s == null) ? "" : s);
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
     * Method in object to execute on active object in hub.
     */
    public void setMethodName(String methodName) {
        control.setMethodName(methodName);
    }

    /**
     * Method in object to execute on active object in hub.
     */
    public String getMethodName() {
        return control.getMethodName();
    }

    private JFileChooser fileChooserOpen;

    public void setOpenFileChooser(JFileChooser fc) {
        this.fileChooserOpen = fc;
    }

    public JFileChooser getOpenFileChooser() {
        return fileChooserOpen;
    }

    private JFileChooser fileChooserSave;

    public void setSaveFileChooser(JFileChooser fc) {
        this.fileChooserSave = fc;
    }

    public JFileChooser getSaveFileChooser() {
        return fileChooserSave;
    }

    public void setConsoleProperty(String prop) {
        control.setConsoleProperty(prop);
    }

    public String getConsoleProperty() {
        return control.getConsoleProperty();
    }

    /**
     * if the hub for this command has a masterHub, then it can control this button if this is set to
     * true. Default = true
     */
    public void setMasterControl(boolean b) {
        control.setMasterControl(b);
    }

    public boolean getMasterControl() {
        return control.getMasterControl();
    }

    /**
     * Returns the component that will receive focus when this button is clicked.
     */
    public JComponent getFocusComponent() {
        return control.getFocusComponent();
    }

    /**
     * Set the component that will receive focus when this button is clicked.
     */
    public void setFocusComponent(JComponent focusComponent) {
        control.setFocusComponent(focusComponent);
        if (focusComponent != null) setFocusPainted(false);
    }

    /**
     * Same as setupButton.
     * 
     * @see #setupButton
     */
    public static void setup(AbstractButton cmd) {
        setupButton(cmd);
    }

    public static void setup(OASplitButton cmd) {
        setupButton(cmd);
    }

    /**
     * Called automatically for all new OAMenuItems if AutoSetup is true (default).<br>
     * Does the following:
     * <ul>
     * <li>setFocusPainted(false)
     * <li>setBorderPainted(false)
     * <li>setMargin(new Insets(1,1,1,1))
     * <li>public void mouseEntered(MouseEvent e) { setBorderPainted(true) }
     * <li>public void mouseExited(MouseEvent e) { setBorderPainted(false) }
     * </ul>
     * 
     * @see #setAutoSetup
     * @see JComponent#setRequestFocusEnabled
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
        // 2004/11/10 removed. If focus is not called, then previous focus component will not get focus
        // lost event
        // cmd.setRequestFocusEnabled(false); // 2003/6/3
        cmd.setBorderPainted(false);
        cmd.setContentAreaFilled(false);
        cmd.setMargin(new Insets(1, 1, 1, 1));

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
                    if (((JToggleButton) cmd).isSelected()) b = true;
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
     * Popup message used to confirm button click before running code.
     */
    public void setConfirmMessage(String msg) {
        control.setConfirmMessage(msg);
    }

    /**
     * Popup message used to confirm button click before running code.
     */
    public String getConfirmMessage() {
        return control.getConfirmMessage();
    }

    /**
     * Popup message when command is completed
     */
    public void setCompletedMessage(String msg) {
        control.setCompletedMessage(msg);
    }

    public String getCompletedMessage() {
        return control.getCompletedMessage();
    }

    /**
     * Object to update whenever button is clicked.
     */
    public void setUpdateObject(OAObject object, String property, Object newValue) {
        control.setUpdateObject(object, property, newValue);
    }

    /**
     * Update active object whenever button is clicked. If there is a multiSelectHub, then each object
     * in it will also be updated.
     */
    public void setUpdateObject(String property, Object newValue) {
        control.setUpdateObject(property, newValue);
    }

    /**
     * This is a callback method that can be overwritten to replace the default action when button is
     * clicked. By default, this will call the button controller, which will also call performAction().
     * 
     * @see #performAction to create a custom action.
     */
    public boolean onActionPerformed() {
        if (control != null) return control.onActionPerformed();
        return false;
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
     * @param keyStroke
     *            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_MASK, false)
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

    public String getPropertyPath() {
        return null;
    }

    public void setPropertyPath(String path) {
    }

    public Dimension getMinimumSize() {
        Dimension d = super.getPreferredSize();
        return d;
    }

    /**
     * Other Hub/Property used to determine if component is enabled.
     * 
     * @param hub
     * @param prop
     *            if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
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
     * 
     * @param hub
     * @param prop
     *            if null, then only checks hub.AO, otherwise will use OAConv.toBoolean to determine.
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
     * This is a callback method that can be overwritten to determine if the component should be visible
     * or not.
     * 
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

    class OAMenuItemController extends ButtonController {
        public OAMenuItemController(Hub hub, EnabledMode enabledMode, Command command) {
            super(hub, OAMenuItem.this, enabledMode, command);
        }

        @Override
        protected String isValid(Object object, Object value) {
            String msg = OAMenuItem.this.isValid(object, value);
            if (msg == null) msg = super.isValid(object, value);
            return msg;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (!OAMenuItem.this.isEnabled()) {
                return;
            }

            String fileName = null;
            JFileChooser fc = getSaveFileChooser();
            if (fc != null) {
                int i = fc.showSaveDialog(SwingUtilities.getWindowAncestor(OAMenuItem.this));
                if (i != JFileChooser.APPROVE_OPTION) return;

                File file = fc.getSelectedFile();
                if (file == null) return;

                fileName = file.getPath();
            }
            else {
                fc = getOpenFileChooser();
                if (fc != null) {
                    int i = fc.showOpenDialog(SwingUtilities.getWindowAncestor(OAMenuItem.this));
                    if (i != JFileChooser.APPROVE_OPTION) return;

                    File file = fc.getSelectedFile();
                    if (file == null) return;

                    fileName = file.getPath();
                }
            }

            if (!OAMenuItem.this.onConfirm(getConfirmMessage())) {
                return;
            }

            if (OAMenuItem.this.onActionPerformed()) { // default will then call
                                                       // this.onActionPerformed()
                afterCompleted(getCompletedMessage());
            }
            else {
                String s = getCompletedMessage();
                if (!OAString.isEmpty(s)) {
                    afterFailure("Command was not completed");
                }
            }
        }

        @Override
        protected boolean onActionPerformed() {
            return super.onActionPerformed();
        }

        @Override
        protected OAObject createCopy(OAObject obj) {
            obj = OAMenuItem.this.createCopy(obj);
            return obj;
        }

        protected OAObject _createCopy(OAObject obj) {
            return super.createCopy(obj);
        }
    }

}
