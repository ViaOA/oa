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

import java.awt.event.*;
import java.net.*;
import javax.swing.*;
import com.viaoa.jfc.OAButton.Command;
import com.viaoa.jfc.OAButton.EnabledMode;
import com.viaoa.jfc.control.*;
import com.viaoa.object.OAObject;
import com.viaoa.hub.*;

public class OAMenuItem extends JMenuItem implements OAJFCComponent {
    private OAMenuItemController control;

    public static Command REMOVE = Command.Remove;
    public static Command NEW = Command.Add;
    public static Command NEW_MANUAL = Command.NewManual;
    public static Command ADD_MANUAL = Command.NewManual;
    public static Command CUT = Command.Cut;
    public static Command COPY = Command.Copy;
    public static Command PASTE = Command.Paste;
    public static Command DELETE = Command.Delete;
    public static Command CLEARAO = Command.Clear;
    
    /**
     * Create a new OAMenuItem that is bound to a Hub and command.
     */
    public OAMenuItem(Hub hub, String text, Icon icon, EnabledMode enabledMode, Command command) {
        if (text != null) setText(text);
        if (icon != null) setIcon(icon);

        if (command == null) command = Command.Other;

        if (enabledMode == null) {
            // get default enabledMode
            switch (command) {
            case Other:
                enabledMode = EnabledMode.UsesIsEnabled;
                break;
            default:
                enabledMode = EnabledMode.HubIsValid;
                break;
            }
        }

        control = new OAMenuItemController(hub, enabledMode, command) {
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

    public OAMenuItem(Hub hub, EnabledMode enabledMode) {
        this(hub, null, null, enabledMode, null);
    }

    public OAMenuItem(Hub hub, String text) {
        this(hub, text, null, null, null);
    }

    public OAMenuItem(Hub hub, String text, Command command) {
        this(hub, text, null, null, command);
    }
    public OAMenuItem(Hub hub, Command command, String text) {
        this(hub, text, null, null, command);
    }

    public OAMenuItem(Hub hub, String text, EnabledMode enabledMode) {
        this(hub, text, null, enabledMode, null);
    }

    public OAMenuItem(Hub hub, Icon icon) {
        this(hub, null, icon, null, null);
    }

    public OAMenuItem(Hub hub, Icon icon, Command command) {
        this(hub, null, icon, null, command);
    }

    public OAMenuItem(Hub hub, Icon icon, EnabledMode enabledMode) {
        this(hub, null, icon, enabledMode, null);
    }

    public OAMenuItem(Hub hub, String text, Icon icon) {
        this(hub, text, icon, null, null);
    }

    public OAMenuItem(Hub hub, String text, Icon icon, Command command) {
        this(hub, text, icon, null, command);
    }

    public OAMenuItem(Hub hub, String text, Icon icon, EnabledMode enabledMode) {
        this(hub, text, icon, enabledMode, null);
    }

    @Override
    public ButtonController getController() {
        return control;
    }

    /**
     * Built in command. Set command value and set button text, tooltip, and icon.
     */
    public void setCommand(Command command) {
        if (command == Command.NewManual) {
            control.setCommand(Command.Add);
            setManual(true);
        }
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

    
    public void setManual(boolean b) {
        control.setManual(b);
    }
    public boolean getManual() {
        return control.getManual();
    }
    
    public EnabledMode getEnabledMode() {
        return control.getEnabledMode();
    }

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

        setup(bIcon, bText, bTtt);
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
    public void setup(boolean bIcon, boolean bText, boolean bToolTip) {
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
     * Popup message used to confirm button click before running code.
     */
    public void setConfirmMessage(String msg) {
        control.setConfirmMessage(msg);
    }

    /**
     * Popup message used to confirm button click before running code.
     */
    public String getConfirmMessage() {
        return control.default_getConfirmMessage();
    }

    /**
     * Popup message when command is completed
     */
    public void setCompletedMessage(String msg) {
        control.setCompletedMessage(msg);
    }

    public String getCompletedMessage() {
        return control.default_getCompletedMessage();
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

    public void afterActionPerformedSuccessful() {
        control.default_afterActionPerformedSuccessful();
    }

    public void afterActionPerformedFailure(String msg, Exception e) {
        control.default_afterActionPerformedFailure(msg, e);
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
        protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
            bIsCurrentlyEnabled = super.isEnabled(bIsCurrentlyEnabled);
            return OAMenuItem.this.isEnabled(bIsCurrentlyEnabled);
        }

        @Override
        protected boolean isVisible(boolean bIsCurrentlyVisible) {
            return OAMenuItem.this.isVisible(bIsCurrentlyVisible);
        }

        // ActionPerformed

        @Override
        public boolean beforeActionPerformed() {
            return OAMenuItem.this.beforeActionPerformed();
        }
        @Override
        public String getConfirmMessage() {
            return OAMenuItem.this.getConfirmMessage();
        }

        @Override
        public boolean confirmActionPerformed() {
            return OAMenuItem.this.confirmActionPerformed();
        }

        @Override
        protected boolean onActionPerformed() {
            return OAMenuItem.this.onActionPerformed();
        }

        @Override
        public void afterActionPerformedSuccessful() {
            OAMenuItem.this.afterActionPerformedSuccessful();
        }

        @Override
        public void afterActionPerformedFailure(String msg, Exception e) {
            OAMenuItem.this.afterActionPerformedFailure(msg, e);
        }

        @Override
        public String getCompletedMessage() {
            return OAMenuItem.this.getCompletedMessage();
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

    public void setAnytime(boolean b) {
        control.setAnytime(b);
    }
    public void setAnyTime(boolean b) {
        control.setAnytime(b);
    }
}
