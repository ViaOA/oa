/* 
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

package com.viaoa.jfc;

import java.lang.reflect.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import com.viaoa.hub.*;
import com.viaoa.jfc.control.*;

/** 
    JMenuItem subclass that binds an object or Hub with built-in command.
    JMenuItem will be enabled based on status of active object in Hub.
    <p>
    Example:<br>
    <pre>
        OAMenuItem mi = new OAMenuItem(hubEmployee, OACommandButton.NEW);
        mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, java.awt.Event.ALT_MASK));
        menu.add(oami);
    </pre>
    @see Hub2Command
*/
public class OAMenuItem_ extends JMenuItem implements OAJFCComponent {
    Hub2Command hubButton;


    /**
        Create a new OAMenuItem that is not bound to a Hub, and is without a defined command.
    */
    public OAMenuItem() {
        this(null, -1, null, null);
        setup();
    }

    /**
       Create an unbound MenuItem.
    */
    public OAMenuItem(String text) {
        this(null, -1, text, null);
        setup();
    }
    /**
       Create an unbound MenuItem.
    */
    public OAMenuItem(String text, Icon icon) {
        this(null, -1, text, icon);
        setup();
    }
    /**
       Create an unbound MenuItem.
    */
    public OAMenuItem(Icon icon) {
        this(null, -1, null, icon);
        setup();
    }

    /**
        Create a new OAMenuItem that is bound to a Hub and command.
    */
    public OAMenuItem(Hub hub, String text, Icon icon) {
        this(hub, -1, text, icon);
        setup();
    }

    /**
        Create a new OAMenuItem that is bound to a Hub.
    */
    public OAMenuItem(Hub hub, String text) {
        this(hub, -1, text, null);
        setup();
    }


    /**
        Create a new OAMenuItem that is bound to a Hub and command.
    */
    public OAMenuItem(Hub hub, int command) {
        hubButton = new Hub2Command(hub, this, command);
        setup();
    }

    /**
        Create a new OAMenuItem that is bound to a Hub and command.
    */
    public OAMenuItem(Hub hub, int command, String text) {
        hubButton = new Hub2Command(hub, this, command);
        setText(text);
        setup();
    }

    /**
        Create a new OAMenuItem that is bound to a Hub and command.
    */
    public OAMenuItem(Hub hub, int command, String text, Icon icon) {
        hubButton = new Hub2Command(hub, this, command);
        setText(text);
        setIcon(icon);
        setup();
    }

    /** 
        Text to display for MenuItem.
    */
    public void setText(String s) {
        super.setText( (s==null)?"":s );
    }

    /** 
        Popup message used to confirm before performing action.
    */
    public void setConfirmMessage(String msg) {
        hubButton.setConfirmMessage(msg);
    }
    /** 
        Popup message used to confirm before performing action.
    */
    public String getConfirmMessage() {
        return hubButton.getConfirmMessage();
    }

    //*******  BeanInfo Properties ************************************


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

    
    /** if the hub for this command has a masterHub, then it can control this button if
	    this is set to true.  Default = true
	*/
	public void setMasterControl(boolean b) {
	    hubButton.setMasterControl(b);
	}
	public boolean getMasterControl() {
	    return hubButton.getMasterControl();
	}
    
    
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public void setUndoDescription(String s) {
        hubButton.setUndoDescription(s);
    }
    /**
        Description to use for Undo and Redo presentation names.
        @see OAUndoableEdit#setPresentationName
    */
    public String getUndoDescription() {
        return hubButton.getUndoDescription();
    }


    /**
        Flag to enable undo, default is true.
    */
    public void setEnableUndo(boolean b) {
        hubButton.setEnableUndo(b);
    }
    /**
        Flag to enable undo, default is true.
    */
    public boolean getEnableUndo() {
        return hubButton.getEnableUndo();
    }

    /**
        Hub this this component is bound to.
    */
    public void setHub(Hub hub) {
        hubButton.setHub(hub);        
    }
    /**
        Hub this this component is bound to.
    */
    public Hub getHub() {
        return hubButton == null ? null : hubButton.getHub();
    }

    /**
        Built-in command to execute when selected.
        @see OACommand
    */
    public void setCommand(int command) {
        hubButton.setCommand(command);
    }
    /**
        Built-in command to execute when selected.
        @see OACommand
    */
    public int getCommand() {
        return hubButton.getCommand();
    }

    /**
       Object to update whenever button is clicked.
    */
    public void setUpdateObject(Object object, String property, Object newValue) {
        hubButton.setUpdateObject(object, property, newValue);
    }

    /**
        If true, then MenuItem will always be active, unless active object is null.
    */
    public void setAnytime(boolean b) {
        setAnyTime(b);
    }
    /**
        If true, then MenuItem will always be active, unless active object is null.
    */
    public void setAnyTime(boolean b) {
        hubButton.setAnyTime(b);
    }
    /**
        If true, then MenuItem will always be active, unless active object is null.
    */
    public boolean getAnyTime() {
        return hubButton.getAnyTime();
    }
    /**
        If true, then MenuItem will always be active, unless active object is null.
    */
    public boolean getAnytime() {
        return hubButton.getAnyTime();
    }
    
    // 2004/08/04
    /**
        Used to manually enable/disable MenuItem.
    */
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (hubButton != null) {
            hubButton.setEnabled(b);
        }
    }
    public void setReadOnly(boolean b) {
        hubButton.setReadOnly(b);
    }
    public boolean getReadOnly() {
        return hubButton.getReadOnly();
    }

    // 20101108
    private EnableController controlEnable;
    public void setEnabled(Hub hub, String prop) {
        if (controlEnable != null) {
            controlEnable.close();
        }
        controlEnable = new EnableController(hub, this, prop);
    }
    

    /**
        Sets the default icon, text, and tooltip (if they are not already set) based on the value of command.
        @see #setup(boolean,boolean,boolean)
    */
    public void setup() {
        boolean bIcon = (getIcon() == null);
        boolean bText = (getText() == null || getText().length() == 0);
        boolean bTtt = (getToolTipText() == null || getToolTipText().length() == 0);
        setup(bIcon, bText, bTtt);
    }
    
    /**
        Sets the default icon, text, and tooltip based on the value of command. 
        @param bIcon if true, calls getIcon(command) to set icon
        @param bText if true, set to command name
        @parma bToolTip if true, set to command name plus name of object in Hub
    */
    public void setup(boolean bIcon, boolean bText, boolean bToolTip) {
        int command = getCommand();
        if (command < 0 || command > OAButton.MAX) return;
        if (bIcon) setIcon(OAButton.getDefaultIcon(command));
        if (bText) setText(OAButton.commandNames[command]);
        if (bToolTip) {
            String s = OAButton.commandNames[command];
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
     * This is a callback method that can be overwritten to determine if the button should be enabled or not.
     */
    public boolean isEnabled(boolean bIsCurrentlyEnabled) {
        return bIsCurrentlyEnabled;
    }
    
    /**
     * This is a callback method that can be overwritten to determine action to perform when button is clicked.
     * @return
     */
    public void onActionPerformed() {
        if (hubButton != null) hubButton.onActionPerformed();
    }
    public boolean onConfirm(String message) {
        if (hubButton != null) {
            return hubButton.confirm(message);
        }
        else return true;
    }

}
