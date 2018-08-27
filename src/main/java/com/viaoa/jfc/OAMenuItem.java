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

import com.viaoa.jfc.OAButton.ButtonCommand;
import com.viaoa.jfc.OAButton.ButtonEnabledMode;
import com.viaoa.jfc.OAButton.OAButtonController;
import com.viaoa.jfc.control.*;
import com.viaoa.object.OAObject;
import com.viaoa.hub.*;

// See OAButton - this is a copy of the same code
public class OAMenuItem extends JMenuItem implements OAJfcComponent {
    private OAMenuItemController control;

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
     * Create a new OAMenuItem that is bound to a Hub and command.
     */
    public OAMenuItem(Hub hub, String text, Icon icon, ButtonEnabledMode enabledMode, ButtonCommand command) {
        if (text != null) setText(text);
        if (icon != null) setIcon(icon);
        
        if (command == null) command = ButtonCommand.Other;
        
        if (enabledMode == null) enabledMode = getDefaultEnabledMode(hub, command);
        
        control = new OAMenuItemController(hub, enabledMode, command) {
        };
        
        setup();
        initialize();
    }

    public static ButtonEnabledMode getDefaultEnabledMode(Hub hub, ButtonCommand command) {
        ButtonEnabledMode enabledMode = ButtonEnabledMode.HubIsValid;
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
        case Paste:
            enabledMode = ButtonEnabledMode.HubIsValid;
            break;
        default:
            enabledMode = ButtonEnabledMode.ActiveObjectNotNull;
            break;
        }
        return enabledMode;
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

    public OAMenuItem(Hub hub, ButtonCommand command) {
        this(hub, null, null, null, command);
    }

    public OAMenuItem(Hub hub, ButtonEnabledMode enabledMode) {
        this(hub, null, null, enabledMode, null);
    }

    public OAMenuItem(Hub hub, String text) {
        this(hub, text, null, null, null);
    }

    public OAMenuItem(Hub hub, String text, ButtonCommand command) {
        this(hub, text, null, null, command);
    }
    public OAMenuItem(Hub hub, ButtonCommand command, String text) {
        this(hub, text, null, null, command);
    }

    public OAMenuItem(Hub hub, String text, ButtonEnabledMode enabledMode) {
        this(hub, text, null, enabledMode, null);
    }

    public OAMenuItem(Hub hub, Icon icon) {
        this(hub, null, icon, null, null);
    }

    public OAMenuItem(Hub hub, Icon icon, ButtonCommand command) {
        this(hub, null, icon, null, command);
    }

    public OAMenuItem(Hub hub, Icon icon, ButtonEnabledMode enabledMode) {
        this(hub, null, icon, enabledMode, null);
    }

    public OAMenuItem(Hub hub, String text, Icon icon) {
        this(hub, text, icon, null, null);
    }

    public OAMenuItem(Hub hub, String text, Icon icon, ButtonCommand command) {
        this(hub, text, icon, null, command);
    }

    public OAMenuItem(Hub hub, String text, Icon icon, ButtonEnabledMode enabledMode) {
        this(hub, text, icon, enabledMode, null);
    }

    @Override
    public void initialize() {
    }
    
    @Override
    public ButtonController getController() {
        return control;
    }

    public void bind(Hub hub, ButtonCommand buttonCommad) {
        setHub(hub);
        getController().setCommand(buttonCommad);
    }
    public void bind(Hub hub) {
        setHub(hub);
    }
    
    /**
     * Built in command. Set command value and set button text, tooltip, and icon.
     *
    public void setCommand(ButtonCommand command) {
        if (command == ButtonCommand.NewManual) {
            control.setCommand(ButtonCommand.Add);
            setManual(true);
        }
        control.setCommand(command);
    }
    */

    /**
     * Built in command.
     */
    public ButtonCommand getCommand() {
        return control.getCommand();
    }

    /*
    public void setEnabledMode(ButtonEnabledMode mode) {
        control.setEnabledMode(mode);
    }
    */

    
    public void setManual(boolean b) {
        control.setManual(b);
    }
    public boolean getManual() {
        return control.getManual();
    }
    
    public ButtonEnabledMode getEnabledMode() {
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
    

    public void setDefaultIcon() {
        ButtonCommand cmd = getCommand();
        if (cmd == null) setIcon(null);
        else setIcon(getDefaultIcon(cmd));
    }
    /**
     * Retrieve an Icon from the viaoa.gui.icons directory.
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
     * Sets the default icon, and tooltip (if they are not already set) based on the value of command.
     * Also calls setup(this). Note: does not set default Text.
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
     * @param bIcon
     *            if true, calls getIcon(command) to set icon
     * @param bText
     *            if true, set to command name
     * @param bToolTip if true, set to command name plus name of object in Hub
     */
    public void setup(boolean bIcon, boolean bText, boolean bToolTip) {
        ButtonCommand cmd = getCommand();
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
            if (cmd == ButtonCommand.Other) s = "";
            else if (s.indexOf("Manual") > 0) {
                s = s.substring(0, s.length()-6);
            }
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
     *
    public void bind(Hub hub, ButtonCommand command) {
        setHub(hub);
        setCommand(command);
    }
    */

    /**
     * Bind menuItem to automatically work with a Hub. This will setAnyTime(false);
     *
    public void bind(Hub hub) {
        setHub(hub);
        setCommand(null);
    }
    */

    /**
     * Description to use for Undo and Redo presentation names.
     */
    public void setUndoDescription(String s) {
        control.setUndoDescription(s);
    }

    /**
     * Description to use for Undo and Redo presentation names.
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
        control.setSelectHub(hubMultiSelect);
    }

    public Hub getHubMultiSelect() {
        if (control == null) return null;
        return control.getSelectHub();
    }

    public Hub getMultiSelectHub() {
        if (control == null) return null;
        return control.getSelectHub();
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
        return control.getConfirmMessage();
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
     */
    public void setEnabled(Hub hub) {
        control.getEnabledChangeListener().add(hub);
    }

    public void setEnabled(Hub hub, String prop) {
        control.getEnabledChangeListener().add(hub, prop);
    }

    public void setEnabled(Hub hub, String prop, Object compareValue) {
        control.getEnabledChangeListener().add(hub, prop, compareValue);
    }

    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }

    /**
     * Other Hub/Property used to determine if component is visible.
     */
    public void setVisible(Hub hub) {
        control.getVisibleChangeListener().add(hub);
    }

    public void setVisible(Hub hub, String prop) {
        control.getVisibleChangeListener().add(hub, prop);
    }

    public void setVisible(Hub hub, String prop, Object compareValue) {
        control.getVisibleChangeListener().add(hub, prop, compareValue);
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

    public void afterActionPerformed() {
        control.default_afterActionPerformed();
    }

    public void afterActionPerformedFailure(String msg, Exception e) {
        control.default_afterActionPerformedFailure(msg, e);
    }

    class OAMenuItemController extends ButtonController {
        public OAMenuItemController(Hub hub, ButtonEnabledMode enabledMode, ButtonCommand command) {
            super(hub, OAMenuItem.this, enabledMode, command);
        }

        @Override
        public String isValid(Object object, Object value) {
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
        public void afterActionPerformed() {
            OAMenuItem.this.afterActionPerformed();
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

    public void setDisplayComponent(JComponent comp) {
        control.setDisplayComponent(comp);
    }
    public JComponent getDisplayComponent() {
        return control.getDisplayComponent();
    }

    public void setConfirmComponent(JComponent comp) {
        control.setDisplayComponent(comp);
    }
    public JComponent getConfirmComponent() {
        return control.getDisplayComponent();
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
    }
    
}
