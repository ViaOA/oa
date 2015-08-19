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

import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.beans.*;
import java.net.URL;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.text.JTextComponent;
import javax.swing.*;

import com.viaoa.jfc.dnd.OATransferable;
import com.viaoa.jfc.tree.*;
import com.viaoa.jfc.*;
import com.viaoa.jfc.OAButton.Command;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;
import com.viaoa.hub.*;


/**
 * Creates system wide cut/copy/paste (c/c/p) components,
 * that listen to focus component for c/c/p changes.
 * 
 * Currently works OATree, OATable - that have a popup menu that
 * have OAMenuItem with commands cut,copy or paste.
 * 
 * Also, has an Hub that can be set manually, that is used when the focus component 
 * does not have c/c/p menuitems. 
 * 
 */
public class CutCopyPasteController {

    private Component focusComponent;
    
    // main menu C/C/P
    private JMenu menuEdit;
    private JMenuItem miCut, miCopy, miPaste;

    
    // focusComponent menuItems for C/C/P
    private OAMenuItem omiCut, omiCopy, omiPaste;
    
    private OATree focusTree;
    private OATreeListener treeListener;
    private OATable focusTable;
    private HubListener tableListener;

    private PropertyChangeListener focusListener; 

    private Hub hubManual;
    private boolean bManualCut, bManualCopy, bManualPaste;
    
    
    public CutCopyPasteController() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener(getFocusListener());
    }
    
    public PropertyChangeListener getFocusListener() {
        if (focusListener == null) {
            focusListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getPropertyName().equals("permanentFocusOwner")) {
                        setFocusComponent((Component) e.getNewValue());
                    }
                }
            };
        }
        return focusListener;
    }
    
    protected void setFocusComponent(Component comp) {
        focusComponent = comp;
        update();
    }
    
    /**
     * Used to manually set the Hub that is used for c/c/p.
     * This is used when the focus component is null or not OATree or OATable.
     */
    public void setManualHub(Hub h) {
        this.hubManual = h;
        update();
    }
    
    public JMenuItem getCutMenuItem() {
        if (miCut == null) {
            URL url = OAButton.class.getResource("icons/cut.gif");
            miCut = new JMenuItem() {
                @Override
                public void addNotify() {
                    super.addNotify();
                    onAddNotify(miCut);
                }
            };
            miCut.setIcon(new ImageIcon(url));
            miCut.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCut();
                }
            });
            /*
            miCut.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_X, java.awt.Event.CTRL_MASK) );
                                                
            String cmd = "cut";
            miCut.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.SHIFT_DOWN_MASK, false), cmd);
            miCut.getActionMap().put(cmd,
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            onCut();
                        }
                    });
            */
        }        
        return miCut;
    }
    public JMenuItem getCopyMenuItem() {
        if (miCopy == null) {
            URL url = OAButton.class.getResource("icons/copy.gif");
            miCopy = new JMenuItem() {
                @Override
                public void addNotify() {
                    super.addNotify();
                    onAddNotify(miCopy);
                }
            };
            miCopy.setIcon(new ImageIcon(url));
            miCopy.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onCopy();
                }
            });
            /*
            miCopy.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_C, java.awt.Event.CTRL_MASK) );
            String cmd = "copy";
            miCopy.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK, false), cmd);
            miCopy.getActionMap().put(cmd,
                    new AbstractAction() {
                        public void actionPerformed(ActionEvent e) {
                            onCopy();
                        }
                    });
            */
        }        
        return miCopy;
    }
    public JMenuItem getPasteMenuItem() {
        if (miPaste == null) {
            URL url = OAButton.class.getResource("icons/paste.gif");
            miPaste = new JMenuItem() {
                @Override
                public void addNotify() {
                    super.addNotify();
                    onAddNotify(miPaste);
                }
            };
            miPaste.setIcon(new ImageIcon(url));

            miPaste.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onPaste();
                }
            });
            /*
            miPaste.setAccelerator( javax.swing.KeyStroke.getKeyStroke(KeyEvent.VK_V, java.awt.Event.CTRL_MASK) );

            String cmd = "paste";
            miPaste.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK, false), cmd);
            miPaste.getActionMap().put(cmd,
                new AbstractAction() {
                    public void actionPerformed(ActionEvent e) {
                        onPaste();
                    }
                }
            );
            */
        }        
        return miPaste;
    }
   
    protected void onAddNotify(JMenuItem mi) {
        if (menuEdit == null) { 
            Component comp = mi.getParent();
            if (!(comp instanceof JPopupMenu)) return;
            comp = ((JPopupMenu) comp).getInvoker();
            if (comp instanceof JMenu) {
                setMenu((JMenu) comp);
            }
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
    
    
    public void update() {
        omiCut = omiCopy = omiPaste = null;
        bManualCut = bManualCopy = bManualPaste = false;    

        if (tableListener != null && focusTable != null) {
            focusTable.getHub().removeHubListener(tableListener);
            tableListener = null;
            focusTable = null;
        }
        if (treeListener != null && focusTree != null) {
            focusTree.removeListener(treeListener);
            treeListener = null;
            focusTree = null;
        }
        
        if (focusComponent instanceof OATable) {
            focusTable = (OATable) focusComponent;
            Hub hub = focusTable.getHub();
            if (hub != null) {
                JPopupMenu pm = focusTable.getMyComponentPopupMenu();
                if (pm != null) findMenuItems(pm);
                tableListener = new HubListenerAdapter() {
                    @Override
                    public void afterChangeActiveObject(HubEvent e) {
                        update();
                    }
                };
                hub.addHubListener(tableListener);
            }
        }
        else if (focusComponent instanceof OATree) {
            focusTree = (OATree) focusComponent;
            OATreeNode tn = focusTree.getSelectedTreeNode();
            if (tn != null) {
                JPopupMenu pm = tn.getPopupMenu();
                if (pm != null) findMenuItems(pm);
            }
            treeListener = new OATreeListener() {
                @Override
                public void onDoubleClick(OATreeNode node, Object object, MouseEvent e) {
                }
                @Override
                public void objectSelected(Object obj) {
                }
                @Override
                public void nodeSelected(OATreeNodeData tnd) {
                    focusComponent = focusTree;
                    update();
                }
                @Override
                public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                    return comp;
                }
            };
            focusTree.addListener(treeListener);
        }
        else if (focusComponent instanceof JTextComponent) {
            //qqqqqqqqqqqqqqqqqqq
            
        }
        if (omiCut == null && omiCopy == null && omiPaste == null) {
            updateUsingManualHub();
        }
        
        // update Menuitem enabled
        getCutMenuItem().setEnabled(bManualCut || (omiCut != null && omiCut.isEnabled()) );
        getCopyMenuItem().setEnabled(bManualCopy || (omiCopy != null && omiCopy.isEnabled()) );
        
        if (omiPaste != null) {
            omiPaste.getController().update();
        }
        getPasteMenuItem().setEnabled(bManualPaste || (omiPaste != null && omiPaste.isEnabled()) );

        
        // update MenuItem text
        String s = "Cut";
        Hub h = null;
        if (omiCut != null) h = omiCut.getHub();
        if (h == null) h = hubManual;
        if (h != null) s = "Cut " + OAString.convertHungarian(OAString.getClassName(h.getObjectClass()));
        getCutMenuItem().setText(s);
    
        s = "Copy";
        h = null;
        if (omiCopy != null) h = omiCopy.getHub();
        if (h == null) h = hubManual;
        if (h != null) s = "Copy " + OAString.convertHungarian(OAString.getClassName(h.getObjectClass()));
        getCopyMenuItem().setText(s);
        
        s = "Paste";
        h = null;
        if (omiPaste != null) h = omiPaste.getHub();
        if (h == null) h = hubManual;
        if (h != null) s = "Paste " + OAString.convertHungarian(OAString.getClassName(h.getObjectClass()));
        getPasteMenuItem().setText(s);
    
    }
    
    
    protected void updateUsingManualHub() {
        if (hubManual == null) return;

        bManualCut = bManualCopy = bManualPaste = false;    
        bManualCut = bManualCopy = (hubManual.getAO() != null);
        
        OAObject obj = getClipboardObject();
        bManualPaste = obj != null && obj.getClass().equals(hubManual.getObjectClass());
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
    
    
    
    protected void findMenuItems(JPopupMenu pm) {
        if (pm == null) return;
        int x = pm.getComponentCount();
        for (int i=0; i<x; i++) {
            Component c = pm.getComponent(i);
            if (c instanceof OAMenuItem) {
                OAButton.Command cmd = ((OAMenuItem) c).getCommand();
                if (cmd == Command.Cut) {
                    omiCut = (OAMenuItem) c;
                }
                else if (cmd == Command.Copy) {
                    omiCopy = (OAMenuItem) c;
                }
                else if (cmd == Command.Paste) {
                    omiPaste = (OAMenuItem) c;
                }
            }
        }
    }
    
    protected void onCut() {
        if (omiCut != null) {
            omiCut.doClick();
        }
        else if (hubManual != null) {
            Object ho = hubManual.getActiveObject();
            if (ho instanceof OAObject) addToClipboard(hubManual, (OAObject)ho);
        }
        update();
    }
    protected void onCopy() {
        if (omiCopy != null) {
            omiCopy.doClick();
        }
        else if (hubManual != null) {
            Object ho = hubManual.getActiveObject();
            if (ho instanceof OAObject) {
                OAObject oaObj = ((OAObject)ho).createCopy();
                addToClipboard(hubManual, oaObj);
            }
        }
        update();
    }
    protected void onPaste() {
        if (omiPaste != null) {
            omiPaste.doClick();
        }
        else if (hubManual != null) {
            OAObject obj = getClipboardObject();
            if (!hubManual.contains(obj)) {
                hubManual.add(obj);
            }
            hubManual.setAO(obj);
        }
        update();
    }

    protected void addToClipboard(Hub hub, OAObject obj) {
        Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        OATransferable t = new OATransferable(hub, obj);
        cb.setContents(t, new ClipboardOwner() {
            @Override
            public void lostOwnership(Clipboard clipboard, Transferable contents) {
                update();
            }
        });
    }
    
}
