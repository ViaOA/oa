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
package com.viaoa.jfc.text;


import java.awt.Color;


import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.undo.*;

import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.text.autocomplete.AutoComplete;
import com.viaoa.jfc.text.spellcheck.SpellChecker;
import com.viaoa.jfc.text.view.FindDialog;


/**
 * Adds popup menu to any text component.
 * 
 * @see #main(String[]) for an example.
 */
public class OATextController {

    protected JTextComponent editor;

    protected JPopupMenu popupMenu;
    
    protected AutoComplete autoComplete;
    protected FindDialog dlgFind;
    protected SpellChecker spellChecker;
    protected UndoManager undoManager;
    protected boolean bAddUndoSupport;

    
    /**
     *  
     */
    public OATextController(JTextComponent txt, SpellChecker spellChecker, boolean bAddUndoSupport) {
        this.editor = txt;
        this.spellChecker = spellChecker;
        this.bAddUndoSupport = bAddUndoSupport;

        this.autoComplete = new AutoComplete(txt) {
            @Override
            protected String[] getMatches(String text) {
                if (OATextController.this.spellChecker == null) return null;
                return OATextController.this.spellChecker.getMatches(text);
            }
            @Override
            protected String[] getSoundexMatches(String text) {
                if (OATextController.this.spellChecker == null) return null;
                return OATextController.this.spellChecker.getSoundexMatches(text);
            }
        };
        
        if (bAddUndoSupport) {
            // UndoManager
            editor.getDocument().addUndoableEditListener(getUndoManager());
        }        
        editor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    onRightMouse(e);
                }
            }
        });
    
        
        editor.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt == null) return;
                String s = evt.getPropertyName();
                if (s.equalsIgnoreCase("enabled") || s.equalsIgnoreCase("editable")) {
                    updateEnabled();
                }
                else if (OATextController.this.bAddUndoSupport && s.equalsIgnoreCase("document")) {
                    Document doc = (Document) evt.getOldValue();
                    if (doc != null) {
                        doc.removeUndoableEditListener(getUndoManager());
                    }
                    doc = (Document) evt.getNewValue();
                    if (doc != null) {
                        doc.addUndoableEditListener(getUndoManager());
                    }
                }
            }
        });
        
        getPopupMenu(); // initialize accelerator keys
        updateEnabled();
    }
    
    public void setSpellChecker(SpellChecker spellChecker) {
        this.spellChecker = spellChecker;
    }
    public SpellChecker getSpellChecker() {
        return this.spellChecker;
    }
    
    protected void updateEnabled() {
        boolean b = editor != null && editor.isEnabled() && editor.isEditable();
        
        // update all components
        if (miFind != null) miFind.setEnabled(true);
        if (pmiFind != null) pmiFind.setEnabled(true);
        if (cmdFind != null) cmdFind.setEnabled(true);
        
        if (miReplace != null) miReplace.setEnabled(b);
        if (pmiReplace != null) pmiReplace.setEnabled(b);

        if (miSpellCheck != null) miSpellCheck.setEnabled(b);
        if (pmiSpellCheck != null) pmiSpellCheck.setEnabled(b);
        if (cmdSpellCheck != null) cmdSpellCheck.setEnabled(b);

        
        int p = editor.getCaretPosition();
        int p2 = editor.getCaret().getMark();

        if (cmdCut != null) cmdCut.setEnabled((p != p2) && b);
        if (cmdCopy != null) cmdCopy.setEnabled((p != p2));
        
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        Object objx;
        try {
            objx = cb.getContents(null);
        }
        catch (Throwable e) {
            objx = null;
        }

        if (cmdCopy != null) cmdCopy.setEnabled(b && objx != null);
        
        if (miSelectAll != null) miSelectAll.setEnabled(true);
        if (pmiSelectAll != null) pmiSelectAll.setEnabled(true);
        if (cmdSelectAll != null) cmdSelectAll.setEnabled(true);
        
        if (miUnselect != null) miUnselect.setEnabled(true);
        if (pmiUnselect != null) pmiUnselect.setEnabled(true);
        if (cmdUnselect != null) cmdUnselect.setEnabled(true);
        
        if (miAutoComplete != null) miAutoComplete.setEnabled(b);
        if (pmiAutoComplete != null) pmiAutoComplete.setEnabled(b);
        if (cmdAutoComplete != null) cmdAutoComplete.setEnabled(b);

        
        
        if (!b && dlgFind==null) {
            Window w = getWindow();
            if (w != null) dlgFind = new FindDialog(w, editor);
        }
        if (dlgFind != null) {
            dlgFind.getTabbedPane().setEnabledAt(1, b);    
            dlgFind.getTabbedPane().setSelectedIndex(0);    
        }
        
        updateUndoable();
        
    }
    
    
/****************************    
>>   COMMAND: Find ^F
*****************************/    
    protected KeyStroke getFindKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK, false);
    }
    private Action actionFind;
    protected Action getFindAction() {
        if (actionFind == null) {
            actionFind = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onFind();
                }
            };
            String cmd = "find";
            editor.getInputMap(JComponent.WHEN_FOCUSED).put(getFindKeyStroke(), cmd);
            editor.getActionMap().put(cmd, actionFind);
        }
        return actionFind;
    }
    private JMenuItem miFind, pmiFind;
    public JMenuItem getFindMenuItem() {
        if (miFind == null) {
            miFind = createFindMenuItem();
        }
        return miFind;
    }
    public JMenuItem getPopupFindMenuItem() {
        if (pmiFind == null) {
            pmiFind = createFindMenuItem();
        }
        return pmiFind;
    }
    protected JMenuItem createFindMenuItem() {
        JMenuItem miFind = new JMenuItem("Find ...");
        miFind.setMnemonic('F');
        miFind.setIcon(new ImageIcon(OATextController.class.getResource("view/image/find.png")));
        miFind.addActionListener(getFindAction());
        miFind.setAccelerator(getFindKeyStroke());
        return miFind;
    }
    private JButton cmdFind;
    public JButton getFindButton() {
        if (cmdFind == null) {
            cmdFind = new JButton();
            cmdFind.setToolTipText("Find text ^F and/or Replace Text ^R");
            cmdFind.setRequestFocusEnabled(false);
            cmdFind.setFocusPainted(false);
            OAButton.setup(cmdFind);
            cmdFind.setIcon(new ImageIcon(OATextController.class.getResource("view/image/find.png")));
            cmdFind.addActionListener(getFindAction());
        }
        return cmdFind;
    }    
    
    
    
/****************************    
>>   COMMAND: Replace ^R
*****************************/    
    protected KeyStroke getReplaceKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK, false);
    }
    private Action actionReplace;
    protected Action getReplaceAction() {
        if (actionReplace == null) {
            actionReplace = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onReplace();
                }
            };
            String cmd = "replace";
            editor.getInputMap(JComponent.WHEN_FOCUSED).put(getReplaceKeyStroke(), cmd);
            editor.getActionMap().put(cmd, actionReplace);
        }
        return actionReplace;
    }
    protected JMenuItem createReplaceMenuItem() {
        JMenuItem miReplace = new JMenuItem("Replace ...");
        miReplace.setMnemonic('R');
        miReplace.addActionListener(getReplaceAction());
        miReplace.setAccelerator(getReplaceKeyStroke());
        return miReplace;
    }
    private JMenuItem miReplace, pmiReplace;
    public JMenuItem getReplaceMenuItem() {
        if (miReplace == null) {
            miReplace = createReplaceMenuItem();
        }
        return miReplace;
    }
    public JMenuItem getPopupReplaceMenuItem() {
        if (pmiReplace == null) {
            pmiReplace = createReplaceMenuItem();
        }
        return pmiReplace;
    }
    
    
/****************************    
>>   COMMAND: SpellCheck ^F7
*****************************/    
    protected KeyStroke getSpellCheckKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK, false);
    }
    private Action actionSpellCheck;
    protected Action getSpellCheckAction() {
        if (actionSpellCheck == null) {
            actionSpellCheck = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onSpellCheck();
                }
            };
            String cmd = "SpellCheck";
            editor.getInputMap(JComponent.WHEN_FOCUSED).put(getSpellCheckKeyStroke(), cmd);
            editor.getActionMap().put(cmd, actionSpellCheck);
        }
        return actionSpellCheck;
    }
    protected JMenuItem createSpellCheckMenuItem() {
        JMenuItem miSpellCheck = new JMenuItem("Spell Check ...");
        //miSpellCheck.setMnemonic('F');
        miSpellCheck.setIcon(new ImageIcon(OATextController.class.getResource("view/image/spell.gif")));
        miSpellCheck.addActionListener(getSpellCheckAction());
        miSpellCheck.setAccelerator(getSpellCheckKeyStroke());
        return miSpellCheck;
    }
    private JMenuItem miSpellCheck, pmiSpellCheck;
    public JMenuItem getSpellCheckMenuItem() {
        if (miSpellCheck == null) {
            miSpellCheck = createSpellCheckMenuItem();
        }
        return miSpellCheck;
    }
    public JMenuItem getPopupSpellCheckMenuItem() {
        if (pmiSpellCheck == null) {
            pmiSpellCheck = createSpellCheckMenuItem();
        }
        return pmiSpellCheck;
    }
    private JButton cmdSpellCheck;
    public JButton getSpellCheckButton() {
        if (cmdSpellCheck == null) {
            cmdSpellCheck = new JButton();
            cmdSpellCheck = new JButton();
            cmdSpellCheck.setToolTipText("Spell Check");
            cmdSpellCheck.setRequestFocusEnabled(false);
            cmdSpellCheck.setFocusPainted(false);
            OAButton.setup(cmdSpellCheck);
            cmdSpellCheck.setIcon(new ImageIcon(OATextController.class.getResource("view/image/spell.gif")));
            cmdSpellCheck.addActionListener(getSpellCheckAction());
        }
        return cmdSpellCheck;
    }    

    
/****************************    
>>   COMMAND: Undo ^Z
*****************************/    
    protected KeyStroke getUndoKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK, false);
    }
    private Action actionUndo;
    protected Action getUndoAction() {
        if (actionUndo == null) {
            actionUndo = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onUndoPerformed();
                }
            };
            String cmd = "UndoZ";
            editor.getInputMap(JComponent.WHEN_FOCUSED).put(getUndoKeyStroke(), cmd);
            editor.getActionMap().put(cmd, actionUndo);
        }
        return actionUndo;
    }
    protected JMenuItem createUndoMenuItem() {
        JMenuItem miUndo = new JMenuItem("Undo");
        miUndo.setIcon(new ImageIcon(OATextController.class.getResource("view/image/undo.png")));
        miUndo.addActionListener(getUndoAction());
        miUndo.setAccelerator(getUndoKeyStroke());
        return miUndo;
    }
    private JMenuItem miUndo, pmiUndo;
    public JMenuItem getUndoMenuItem() {
        if (miUndo == null) {
            miUndo = createUndoMenuItem();
        }
        return miUndo;
    }
    public JMenuItem getPopupUndoMenuItem() {
        if (pmiUndo == null) {
            pmiUndo = createUndoMenuItem();
        }
        return pmiUndo;
    }
    private JButton cmdUndo;
    protected JButton getUndoButton() {
        if (cmdUndo == null) {
            cmdUndo = new JButton();
            cmdUndo.setToolTipText("Undo");
            cmdUndo.setRequestFocusEnabled(false);
            cmdUndo.setFocusPainted(false);
            OAButton.setupButton(cmdUndo);
            cmdUndo.setIcon(new ImageIcon(OATextController.class.getResource("view/image/undo.png")));
            cmdUndo.addActionListener(getUndoAction());
        }
        return cmdUndo;
    }    
    

/****************************    
>>   COMMAND: Redo ^Y
*****************************/    
    protected KeyStroke getRedoKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK, false);
    }
    private Action actionRedo;
    protected Action getRedoAction() {
        if (actionRedo == null) {
            actionRedo = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onRedoPerformed();
                }
            };
            String cmd = "RedoY";
            editor.getInputMap(JComponent.WHEN_FOCUSED).put(getRedoKeyStroke(), cmd);
            editor.getActionMap().put(cmd, actionRedo);
        }
        return actionRedo;
    }
    protected JMenuItem createRedoMenuItem() {
        JMenuItem miRedo = new JMenuItem("Redo");
        miRedo.setIcon(new ImageIcon(OATextController.class.getResource("view/image/redo.png")));
        miRedo.addActionListener(getRedoAction());
        miRedo.setAccelerator(getRedoKeyStroke());
        return miRedo;
    }
    private JMenuItem miRedo, pmiRedo;
    public JMenuItem getRedoMenuItem() {
        if (miRedo == null) {
            miRedo = createRedoMenuItem();
        }
        return miRedo;
    }
    public JMenuItem getPopupRedoMenuItem() {
        if (pmiRedo == null) {
            pmiRedo = createRedoMenuItem();
        }
        return pmiRedo;
    }
    private JButton cmdRedo;
    protected JButton getRedoButton() {
        if (cmdRedo == null) {
            cmdRedo = new JButton();
            cmdRedo.setToolTipText("Redo");
            cmdRedo.setRequestFocusEnabled(false);
            cmdRedo.setFocusPainted(false);
            OAButton.setupButton(cmdRedo);
            cmdRedo.setIcon(new ImageIcon(OATextController.class.getResource("view/image/redo.png")));
            cmdRedo.addActionListener(getRedoAction());
        }
        return cmdRedo;
    }    

/****************************    
>>   COMMAND: Cut
*****************************/    
    protected KeyStroke getCutKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    private Action actionCut;
    protected Action getCutAction() {
        if (actionCut == null) {
            actionCut = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onCutPerformed();
                }
            };
            String cmd = "Cut";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getCutKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionCut);
        }
        return actionCut;
    }
    protected JMenuItem createCutMenuItem() {
        JMenuItem miCut = new JMenuItem("Cut");
        miCut.setIcon(new ImageIcon(OATextController.class.getResource("view/image/cut.gif")));
        miCut.addActionListener(getCutAction());
        //miCut.setAccelerator(getCutKeyStroke());
        return miCut;
    }
    private JMenuItem miCut, pmiCut;
    protected JMenuItem getCutMenuItem() {
        if (miCut == null) {
            miCut = createCutMenuItem();
        }
        return miCut;
    }
    protected JMenuItem getPopupCutMenuItem() {
        if (pmiCut == null) {
            pmiCut = createCutMenuItem();
        }
        return pmiCut;
    }
    private JButton cmdCut;
    public JButton getCutButton() {
        if (cmdCut == null) {
            cmdCut = new JButton();
            cmdCut.setToolTipText("Cut");
            cmdCut.setRequestFocusEnabled(false);
            cmdCut.setFocusPainted(false);
            OAButton.setupButton(cmdCut);
            cmdCut.setIcon(new ImageIcon(OATextController.class.getResource("view/image/cut.gif")));
            cmdCut.addActionListener(getCutAction());
        }
        return cmdCut;
    }
    
    
/****************************    
>>   COMMAND: Copy
*****************************/    
    protected KeyStroke getCopyKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    private Action actionCopy;
    protected Action getCopyAction() {
        if (actionCopy == null) {
            actionCopy = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onCopyPerformed();
                }
            };
            String cmd = "Copy";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getCopyKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionCopy);
        }
        return actionCopy;
    }
    protected JMenuItem createCopyMenuItem() {
        JMenuItem miCopy = new JMenuItem("Copy");
        miCopy.setIcon(new ImageIcon(OATextController.class.getResource("view/image/copy.gif")));
        miCopy.addActionListener(getCopyAction());
        //miCopy.setAccelerator(getCopyKeyStroke());
        return miCopy;
    }
    private JMenuItem miCopy, pmiCopy;
    protected JMenuItem getCopyMenuItem() {
        if (miCopy == null) {
            miCopy = createCopyMenuItem();
        }
        return miCopy;
    }
    protected JMenuItem getPopupCopyMenuItem() {
        if (pmiCopy == null) {
            pmiCopy = createCopyMenuItem();
        }
        return pmiCopy;
    }
    private JButton cmdCopy;
    public JButton getCopyButton() {
        if (cmdCopy == null) {
            cmdCopy = new JButton();
            cmdCopy.setToolTipText("Copy");
            cmdCopy.setRequestFocusEnabled(false);
            cmdCopy.setFocusPainted(false);
            OAButton.setupButton(cmdCopy);
            cmdCopy.setIcon(new ImageIcon(OATextController.class.getResource("view/image/copy.gif")));
            cmdCopy.addActionListener(getCopyAction());
        }
        return cmdCopy;
    }    
    
    
/****************************    
>>   COMMAND: Paste
*****************************/    
    protected KeyStroke getPasteKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    private Action actionPaste;
    protected Action getPasteAction() {
        if (actionPaste == null) {
            actionPaste = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onPastePerformed();
                }
            };
            String cmd = "Paste";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getPasteKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionPaste);
        }
        return actionPaste;
    }
    protected JMenuItem createPasteMenuItem() {
        JMenuItem miPaste = new JMenuItem("Paste");
        miPaste.setIcon(new ImageIcon(OATextController.class.getResource("view/image/paste.gif")));
        miPaste.addActionListener(getPasteAction());
        //miPaste.setAccelerator(getPasteKeyStroke());
        return miPaste;
    }
    private JMenuItem miPaste, pmiPaste;
    public JMenuItem getPasteMenuItem() {
        if (miPaste == null) {
            miPaste = createPasteMenuItem();
        }
        return miPaste;
    }
    public JMenuItem getPopupPasteMenuItem() {
        if (pmiPaste == null) {
            pmiPaste = createPasteMenuItem();
        }
        return pmiPaste;
    }
    private JButton cmdPaste;
    public JButton getPasteButton() {
        if (cmdPaste == null) {
            cmdPaste = new JButton();
            cmdPaste.setToolTipText("Paste");
            cmdPaste.setRequestFocusEnabled(false);
            cmdPaste.setFocusPainted(false);
            OAButton.setupButton(cmdPaste);
            cmdPaste.setIcon(new ImageIcon(OATextController.class.getResource("view/image/paste.gif")));
            cmdPaste.addActionListener(getPasteAction());
        }
        return cmdPaste;
    }    
    
/****************************    
>>   COMMAND: SelectAll ^A
*****************************/    
    protected KeyStroke getSelectAllKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    private Action actionSelectAll;
    protected Action getSelectAllAction() {
        if (actionSelectAll == null) {
            actionSelectAll = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onSelectAllPerformed();
                }
            };
            String cmd = "SelectAll";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getSelectAllKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionSelectAll);
        }
        return actionSelectAll;
    }
    protected JMenuItem createSelectAllMenuItem() {
        JMenuItem miSelectAll = new JMenuItem("Select All");
        miSelectAll.setIcon(new ImageIcon(OATextController.class.getResource("view/image/selectAll.gif")));
        miSelectAll.addActionListener(getSelectAllAction());
        //miSelectAll.setAccelerator(getSelectAllKeyStroke());
        return miSelectAll;
    }
    private JMenuItem miSelectAll, pmiSelectAll;
    public JMenuItem getSelectAllMenuItem() {
        if (miSelectAll == null) {
            miSelectAll = createSelectAllMenuItem();
        }
        return miSelectAll;
    }
    public JMenuItem getPopupSelectAllMenuItem() {
        if (pmiSelectAll == null) {
            pmiSelectAll = createSelectAllMenuItem();
        }
        return pmiSelectAll;
    }
    private JButton cmdSelectAll;
    public JButton createSelectAllButton() {
        if (cmdSelectAll == null) {
            cmdSelectAll = new JButton();
            cmdSelectAll.setToolTipText("Select all text");
            cmdSelectAll.setRequestFocusEnabled(false);
            cmdSelectAll.setFocusPainted(false);
            OAButton.setupButton(cmdSelectAll);
            cmdSelectAll.setIcon(new ImageIcon(OATextController.class.getResource("view/image/selectAll.gif")));
            cmdSelectAll.addActionListener(getSelectAllAction());
        }
        return cmdSelectAll;
    }    
    
    
/****************************    
>>   COMMAND: Unselect
*****************************/    
    protected KeyStroke getUnselectKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK, false);
    }
    private Action actionUnselect;
    protected Action getUnselectAction() {
        if (actionUnselect == null) {
            actionUnselect = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onUnselectPerformed();
                }
            };
            String cmd = "Unselect";
            // editor.getInputMap(JComponent.WHEN_FOCUSED).put(getUnselectKeyStroke(), cmd);
            // editor.getActionMap().put(cmd, actionUnselect);
        }
        return actionUnselect;
    }
    protected JMenuItem createUnselectMenuItem() {
        JMenuItem miUnselect = new JMenuItem("Unselect");
        miUnselect.setIcon(new ImageIcon(OATextController.class.getResource("view/image/unselect.gif")));
        miUnselect.addActionListener(getUnselectAction());
        //miUnselect.setAccelerator(getUnselectKeyStroke());
        return miUnselect;
    }
    private JMenuItem miUnselect, pmiUnselect;
    public JMenuItem getUnselectMenuItem() {
        if (miUnselect == null) {
            miUnselect = createUnselectMenuItem();
        }
        return miUnselect;
    }
    public JMenuItem getPopupUnselectMenuItem() {
        if (pmiUnselect == null) {
            pmiUnselect = createUnselectMenuItem();
        }
        return pmiUnselect;
    }
    private JButton cmdUnselect ;
    public JButton createUnselectButton() {
        if (cmdUnselect == null) {
            cmdUnselect = new JButton();
            cmdUnselect.setToolTipText("Unselect text");
            cmdUnselect.setRequestFocusEnabled(false);
            cmdUnselect.setFocusPainted(false);
            OAButton.setupButton(cmdUnselect);
            cmdUnselect.setIcon(new ImageIcon(OATextController.class.getResource("view/image/unselect.gif")));
            cmdUnselect.addActionListener(getUnselectAction());
        }
        return cmdUnselect;
    }    
        
    

/*********************************
>>   COMMAND: AutoComplete ^Space
**********************************/    
    protected KeyStroke getAutoCompleteKeyStroke() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK, false);
    }
    private Action actionAutoComplete;
    protected Action getAutoCompleteAction() {
        if (actionAutoComplete == null) {
            actionAutoComplete = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onAutoComplete();
                }
            };
            String cmd = "AutoComplete";
            editor.getInputMap(JComponent.WHEN_FOCUSED).put(getAutoCompleteKeyStroke(), cmd);
            editor.getActionMap().put(cmd, actionAutoComplete);
        }
        return actionAutoComplete;
    }
    protected JMenuItem createAutoCompleteMenuItem() {
        JMenuItem miAutoComplete = new JMenuItem("Auto Complete ...");
        //miAutoComplete.setMnemonic('F');
        miAutoComplete.setIcon(new ImageIcon(OATextController.class.getResource("view/image/autoComplete.gif")));
        miAutoComplete.addActionListener(getAutoCompleteAction());
        miAutoComplete.setAccelerator(getAutoCompleteKeyStroke());
        return miAutoComplete;
    }
    private JMenuItem miAutoComplete, pmiAutoComplete;
    protected JMenuItem getAutoCompleteMenuItem() {
        if (miAutoComplete == null) {
            miAutoComplete = createAutoCompleteMenuItem();
        }
        return miAutoComplete;
    }    
    protected JMenuItem getPopupAutoCompleteMenuItem() {
        if (pmiAutoComplete == null) {
            pmiAutoComplete = createAutoCompleteMenuItem();
        }
        return pmiAutoComplete;
    }    
    private JButton cmdAutoComplete;
    public JButton getAutoCompleteButton() {
        if (cmdAutoComplete == null) {
            cmdAutoComplete = new JButton();
            cmdAutoComplete = new JButton();
            cmdAutoComplete.setToolTipText("Auto Complete");
            cmdAutoComplete.setRequestFocusEnabled(false);
            cmdAutoComplete.setFocusPainted(false);
            OAButton.setup(cmdAutoComplete);
            cmdAutoComplete.setIcon(new ImageIcon(OATextController.class.getResource("view/image/autoComplete.gif")));
            cmdAutoComplete.addActionListener(getAutoCompleteAction());
        }
        return cmdAutoComplete;
    }    
    
    
    protected void onRightMouse(MouseEvent e) {
        if (editor == null) return;
        
        int p = editor.getCaretPosition();
        int p2 = editor.getCaret().getMark();
        boolean b = (p != p2) && editor.isEnabled();
        
        getPopupUnselectMenuItem().setEnabled(b);
        
        getPopupCutMenuItem().setEnabled(b);
        getPopupCopyMenuItem().setEnabled(b);

        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        Object objx = null;
        try {
            objx = cb.getContents(null);
        }
        catch (Exception ex) {
            System.out.println("OATextController.onRightMouse clipboard.getContents exception, ex="+ex);
        }
        
        getPopupPasteMenuItem().setEnabled(objx != null && editor.isEnabled());

        getPopupMenu().show(editor, e.getPoint().x, e.getPoint().y);
    }
    
    protected JPopupMenu getPopupMenu() {
        if (popupMenu != null) return popupMenu;
        
        popupMenu = new JPopupMenu();
        
        JMenu menu;
        
        popupMenu.add(getPopupUndoMenuItem());
        popupMenu.add(getPopupRedoMenuItem());

        popupMenu.addSeparator();
        popupMenu.add(getPopupCutMenuItem());
        popupMenu.add(getPopupCopyMenuItem());
        popupMenu.add(getPopupPasteMenuItem());
        
        popupMenu.addSeparator();
        popupMenu.add(getPopupFindMenuItem());
        popupMenu.add(getPopupReplaceMenuItem());

        popupMenu.addSeparator();

        boolean b = false;
        if (spellChecker != null) {
            popupMenu.add(getPopupSpellCheckMenuItem());
            b = true;
        }
        if (autoComplete != null) {
            popupMenu.add(getPopupAutoCompleteMenuItem());
            b = true;
        }

        if (b) popupMenu.addSeparator();
        popupMenu.add(getPopupSelectAllMenuItem());
        popupMenu.add(getPopupUnselectMenuItem());

        return popupMenu;
    }

    
    protected Window getWindow() {
        return SwingUtilities.getWindowAncestor(editor);
    }
    
    public void onFind() {
        if (dlgFind==null) dlgFind = new FindDialog(getWindow(), editor);
        dlgFind.setSelectedIndex(0);
        dlgFind.setVisible(true);
    }

    public void onReplace() {
        if (dlgFind==null) dlgFind = new FindDialog(getWindow(), editor);
        dlgFind.setSelectedIndex(1);
        dlgFind.setVisible(true);
        editor.requestFocus();  // so that lostFocus will be called, and text will be saved
    }

    public void onSpellCheck() {
        if (spellChecker == null) {
            JOptionPane.showMessageDialog(getWindow(),  "SpellChecker has not been set - see programming");
        }
        else {
            if (editor != null) spellChecker.spellCheck(editor);
        }
        editor.requestFocus();  // so that lostFocus will be called, and text will be saved
    }
    
    /**
     * create a self contained Undomanger for this document only.
     * @return
     */
    public UndoManager getUndoManager() {
        if (undoManager == null) {
            undoManager = new UndoManager() {
                @Override
                public synchronized boolean addEdit(UndoableEdit anEdit) {
                    boolean b = super.addEdit(anEdit);
                    updateUndoable();
                    return b;
                }
                @Override
                public synchronized void undo() throws CannotUndoException {
                    super.undo();
                    updateUndoable();
                }
                @Override
                public synchronized void discardAllEdits() {
                    super.discardAllEdits();
                    updateUndoable();
                }
                @Override
                protected void redoTo(UndoableEdit edit) throws CannotRedoException {
                    super.redoTo(edit);
                    updateUndoable();
                }
            };
            updateUndoable();
        }
        return undoManager;
    }
    /**
     * This can be used to know when the undoManager has changed.
     */
    protected void updateUndoable() {
        boolean bUndo = false;
        boolean bRedo = false;
        
        if (undoManager != null && editor.isEnabled()) {
            bUndo = undoManager.canUndo();
            bRedo = undoManager.canRedo();
        }
        
        getPopupUndoMenuItem().setEnabled(bUndo && editor.isEnabled());
        getPopupRedoMenuItem().setEnabled(bRedo && editor.isEnabled());

        getUndoButton().setEnabled(bUndo && editor.isEnabled());
        getRedoButton().setEnabled(bRedo && editor.isEnabled());
    }
    protected void onUndoPerformed() {
        if (undoManager != null && undoManager.canUndo()) {
            undoManager.undo();
            updateUndoable();
        }
    }
    protected void onRedoPerformed() {
        if (undoManager != null && undoManager.canRedo()) {
            undoManager.redo();
            updateUndoable();
        }
    }
    
    protected void onCutPerformed() {
        editor.cut();
    }
    protected void onCopyPerformed() {
        editor.copy();
    }
    protected void onPastePerformed() {
        editor.paste();        
    }
    protected void onSelectAllPerformed() {
        editor.selectAll();        
    }
    protected void onUnselectPerformed() {
        int pos = editor.getCaretPosition();
        editor.select(pos, pos);        
    }
  
    public void onAutoComplete() {
        if (autoComplete != null) {
            autoComplete.showAutoComplete();
        }
    }
    
    
    
    
    public static void main(String[] args) {
        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextArea ta = new JTextArea(12, 60);

        ta.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "CMD_Esc");
        ta.getActionMap().put("CMD_Esc", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        ta.setBorder(new javax.swing.border.EmptyBorder(2,3,2,2));
        
        AutoComplete acsc = new AutoComplete(ta) {
            @Override
            protected String[] getMatches(String text) {
                String[] ss = new String[8];
                for (int i=0; i<ss.length; i++) ss[i] = text+"_"+i;
                return ss;
            }
            
            @Override
            protected String[] getSoundexMatches(String text) {
                String[] ss = new String[12];
                for (int i=0; i<ss.length; i++) ss[i] = text+"ABCxyz"+i;
                return ss;
            }
        };        

        SpellChecker sc = new SpellChecker() {
            @Override
            public boolean isWordFound(String word) {
                return false;
            }
            @Override
            public String[] getMatches(String text) {
                return null;
            }
            @Override
            public String[] getSoundexMatches(String text) {
                return null;
            }
            @Override
            public void addNewWord(String word) {
            }
        }; 

        OATextController tc = new OATextController(ta, sc, true);
        frm.getContentPane().add(new JScrollPane(ta));
        frm.pack();
        frm.setVisible(true);
    }
    
}

