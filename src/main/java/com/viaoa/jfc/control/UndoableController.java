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

import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OATextField;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.util.OAString;

/**
 * Works with OAUndoManager, supplies UI components with undo/redo functionality.
 * 
 * @author vincevia
 *
 */
public class UndoableController {

    private JPopupMenu pmenuEdit;
    private JMenu menuEdit;
    private JMenuItem miUndo;
    private JMenuItem miRedo; 

    public UndoableController() {
        OAUndoManager man = OAUndoManager.getUndoManager();
        if (man == null) OAUndoManager.createUndoManager();
    }

    public JMenuItem getUndoMenuItem() {
        if (miUndo == null) {
            miUndo = new JMenuItem("Undo") {
                @Override
                public void addNotify() {
                    super.addNotify();
                    onAddNotify(miUndo);
                }
            };
            //miUndo.setMnemonic('C');
            URL url = OAButton.class.getResource("icons/undo.gif");
            if (url != null) miUndo.setIcon(new ImageIcon(url));
            // miUndo.setToolTipText("");
            miUndo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onUndo();
                }
            });
            miUndo.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_Z, java.awt.Event.CTRL_MASK) );
        }
        return miUndo;
    }    

    public JMenuItem getRedoMenuItem() {
        if (miRedo == null) {
            miRedo = new JMenuItem("Redo") {
                @Override
                public void addNotify() {
                    super.addNotify();
                    onAddNotify(miRedo);
                }
            };
            //miRedo.setMnemonic('C');
            URL url = OAButton.class.getResource("icons/redo.gif");
            if (url != null) miRedo.setIcon(new ImageIcon(url));
            // miRedo.setToolTipText("");
            miRedo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onRedo();
                }
            });
            miRedo.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_Y, java.awt.Event.CTRL_MASK) );
        }
        return miRedo;
    }    

    protected void onAddNotify(JMenuItem mi) {
        if (menuEdit == null) { 
            Component comp = mi.getParent();
            if (!(comp instanceof JPopupMenu)) return;
            comp = ((JPopupMenu) comp).getInvoker();
            if (comp instanceof JMenu) setMenu((JMenu) comp);
        }
        update();
    }
    
    // set the Menu that Undo/Redo menuItems will be added to
    protected void setMenu(JMenu menu) {
        menuEdit = menu;
        menuEdit.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                update();
            }
            @Override
            public void menuDeselected(MenuEvent e) {
            }
            @Override
            public void menuCanceled(MenuEvent e) {
            }
        });
    }
    
    protected void onUndo() {
        OAUndoManager undoManager = OAUndoManager.getUndoManager();
        if (undoManager == null || !undoManager.canUndo()) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
        else {
            Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//qqqqqqq txtFld does not undo if it has focus qqqqqqqqqqqq            
            if (comp instanceof OATextField) {
                OATextField txt = (OATextField) comp;
//                txt.getController().
            }
            undoManager.undo();
        }
    }
    protected void onRedo() {
        OAUndoManager undoManager = OAUndoManager.getUndoManager();
        if (undoManager == null || !undoManager.canRedo()) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
        else undoManager.redo();
    }

    public void update() {
        OAUndoManager man = OAUndoManager.getUndoManager();
        boolean b = false;
        String s = "Undo";
        JMenuItem mi = getUndoMenuItem();
        if (man != null) {
            if (man.canUndo()) {
                b = true;
                s = man.getUndoPresentationName();
            }
        }
        mi.setEnabled(b);
        mi.setText(OAString.fmt(s, "22L."));
        mi.setToolTipText("<html>"+OAString.lineBreak(s, 34, "<br>", 3));


        b = false;
        s = "Redo";
        mi = getRedoMenuItem();
        if (man != null) {
            if (man.canRedo()) {
                b = true;
                s = man.getRedoPresentationName();
            }
        }
        mi.setEnabled(b);
        mi.setText(OAString.fmt(s, "22L."));
        mi.setToolTipText("<html>"+OAString.lineBreak(s, 34, "<br>", 3));
    }
    
}
