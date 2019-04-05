package com.viaoa.web;

import javax.swing.ImageIcon;

import com.viaoa.hub.Hub;

import com.viaoa.web.OAButton.ButtonCommand;
import com.viaoa.web.OAButton.ButtonEnabledMode;
import com.viaoa.web.swing.ComponentInterface;
import com.viaoa.web.swing.JMenuItem;

public class OAMenuItem extends JMenuItem {

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
    
    
    
    public OAMenuItem(Hub hub, ButtonCommand cmd) {
        // TODO Auto-generated constructor stub
        super(null, null);
    }

    public OAMenuItem(Hub hub, ButtonCommand cmd, String string) {
        // TODO Auto-generated constructor stub
    }

    public OAMenuItem(Hub hub, String string, ImageIcon jarIcon) {
        // TODO Auto-generated constructor stub
    }

    public boolean onActionPerformed() {
        // TODO Auto-generated method stub
        return false;
    }

    public void setText(String string) {
    }

    public void setToolTipText(String string) {
        // TODO Auto-generated method stub
    }

    public void afterActionPerformed() {
        // TODO Auto-generated method stub
        
    }

    public void setMnemonic(int vkN) {
        // TODO Auto-generated method stub
        
    }

    public void setFocusComponent(ComponentInterface comp) {
        // TODO Auto-generated method stub
        
    }

    public void setConfirmMessage(String string) {
        // TODO Auto-generated method stub
        
    }

    public void setUseSwingWorker(boolean b) {
        // TODO Auto-generated method stub
        
    }

    public void setProcessingText(String string, String string2) {
        // TODO Auto-generated method stub
        
    }

}
