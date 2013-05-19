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

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;


import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
    ComboBox with drop down tree.
    <p>

    <pre>
    OATree tree = new OATree();
    OATreeNode node = new OATreeNode("name", hubObjectDef);
    node.setShowAll(false);
    tree.add(node);

    OATreeNode node2 = new OATreeNode("propertyDefs.name", hub);
    node.add(node2);

    tree.setVisibleRowCount(15);
    tree.setColumns(22);
    tree.allowDnD(false);
    OATreeComboBox tcbo = new OATreeComboBox(tree, hub, "name");
    tcbo.setColumns(12);
    </pre>
*/
public class OATreeComboBox extends OACustomComboBox {
    protected OATree tree;
    private MyTreePopup myTreePopup;

    /**
    */
    public OATreeComboBox() {
    	control.bDisplayPropertyOnly = true;
    }

    
    /**
        @param hub is hub to use for displaying the current value.
        @param displayProperty property path to display
    */
    public OATreeComboBox(OATree tree, Hub hub, String displayProperty) {
        super(hub, displayProperty);
    	control.bDisplayPropertyOnly = true;
        setTree(tree);
        tree.updateUI();
        
        // 20110801
        if (hub != null) {
            if (hub.getLinkHub() != null) {
                setEnabled(hub.getLinkHub(), null);
            }
            if (hub.getMasterHub() != null) {
                setEnabled(hub.getMasterHub(), null);
            }
        }
    }

    /**
        @param hub is hub to use for displaying the current value.
        @param displayProperty property path to display
    */
    public OATreeComboBox(Hub hub, String displayProperty) {
        super(hub, displayProperty);
    	control.bDisplayPropertyOnly = true;
    }


    public OATree getTree() {
        return tree;
    }
    public void setTree(OATree tree) {
        this.tree = tree;
    }

    // 20110731 allow 
    @Override
    protected boolean isEnabled(boolean bIsCurrentlyEnabled) {
        if (!bIsCurrentlyEnabled) {
            Hub h = getHub();
            if (h != null) {
                if (h.getLinkHub() != null) {
                    bIsCurrentlyEnabled = (h.getLinkHub().getAO() != null);
                }
                if (!bIsCurrentlyEnabled) {
                    if (h.getMasterHub() != null) {
                        bIsCurrentlyEnabled = (h.getMasterHub().getAO() != null);
                    }
                }
            }
        }
        return super.isEnabled(bIsCurrentlyEnabled);
    }
    
    /**
        override to create popup calendar
    */
    public void updateUI() {
	    ComboBoxUI cui = (ComboBoxUI) UIManager.getUI(this);
	    if (cui instanceof MotifComboBoxUI) {
	        cui = new MotifComboBoxUI() {
	            protected ComboPopup createPopup() {
	                myTreePopup = new MyTreePopup( comboBox, OATreeComboBox.this );
	                return myTreePopup;
	            }
	        };
	    }
	    else if (cui instanceof WindowsComboBoxUI) {
	        cui = new WindowsComboBoxUI() {
	            protected ComboPopup createPopup() {
	                myTreePopup = new MyTreePopup( comboBox, OATreeComboBox.this );
	                return myTreePopup;
	            }
	        };
	    }
	    else cui = new MetalComboBoxUI() {
	        protected ComboPopup createPopup() {
	            myTreePopup = new MyTreePopup( comboBox, OATreeComboBox.this );
	            return myTreePopup;
	        }
	    };
        setUI(cui);
// 20100320        
        if (myTreePopup != null) {
            myTreePopup.popup.updateUI();
        }
        if (tree != null) tree.updateUI();
    }

    /**
        Called when popup.show is called.  Can be overwritten to add custom behaviors, ex: tree.expandRow(0)
    */
    public void onShow() {
    }
    
    public void hidePopup() {
        if (myTreePopup != null) {
            myTreePopup.hide();        
        }
    }

    @Override
    public String getToolTipText(int row, int col, String defaultValue) {
        return defaultValue;
    }
}


class MyTreePopup implements ComboPopup, MouseMotionListener, MouseListener, KeyListener, PopupMenuListener {

	protected JComboBox comboBox;
	protected JPopupMenu popup;
	protected OATreeComboBox cboTree;
	protected JPanel panCommands;

	public MyTreePopup(JComboBox comboBox, OATreeComboBox cboTree) {
	    this.comboBox = comboBox;
	    this.cboTree = cboTree;
	    popup = new JPopupMenu();
	    popup.setBorder(BorderFactory.createLineBorder(Color.black));
	    popup.setLayout(new BorderLayout());
	    popup.addPopupMenuListener(this);
	}

	//========================================
	// begin ComboPopup method implementations
	private boolean bInit;
    public void show() {
        if (cboTree != null && cboTree.tree != null) {
            if (!bInit) {
                // hack: this cant be created in the constructor
                bInit = true;
                popup.add(new JScrollPane(cboTree.tree), BorderLayout.CENTER);
                popup.add(getButtonCommands(), BorderLayout.SOUTH);
            }
            cboTree.onShow();
        }
        
        Dimension d = popup.getSize();
        if (d == null || d.height == 0) {
            d = popup.getPreferredSize();
            popup.setPopupSize(d);
        }
        
        Rectangle rec = computePopupBounds(0, comboBox.getSize().height, d.width, d.height);
	    popup.show(comboBox, 0, rec.y);
    }

    
    protected Rectangle computePopupBounds(int px,int py,int pw,int ph) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Rectangle screenBounds;

        // Calculate the desktop dimensions relative to the combo box.
        GraphicsConfiguration gc = comboBox.getGraphicsConfiguration();
        Point p = new Point();
        SwingUtilities.convertPointFromScreen(p, comboBox);
        if (gc != null) {
            Insets screenInsets = toolkit.getScreenInsets(gc);
            screenBounds = gc.getBounds();
            screenBounds.width -= (screenInsets.left + screenInsets.right);
            screenBounds.height -= (screenInsets.top + screenInsets.bottom);
            screenBounds.x += (p.x + screenInsets.left);
            screenBounds.y += (p.y + screenInsets.top);
        }
        else {
            screenBounds = new Rectangle(p, toolkit.getScreenSize());
        }

        Rectangle rect = new Rectangle(px,py,pw,ph);
        if (py+ph > screenBounds.y+screenBounds.height && ph < screenBounds.height) {
            rect.y = -rect.height;
        }
        return rect;
    }


    
    private JPanel getButtonCommands() {
        if (panCommands != null) return panCommands;
        panCommands = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    	JButton cmd = new JButton("close");
    	cmd.addActionListener(new ActionListener() {
    		public void actionPerformed(ActionEvent e) {
    			hide();
    		}
    	});
    	OACommand.setup(cmd);
    	panCommands.add(cmd);

    	if (cboTree.getAllowClearButton()) {
	    	cmd = new JButton("clear");
	    	cmd.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			cboTree.onClear();
	    			hide();
	    		}
	    	});
	    	OACommand.setup(cmd);
	    	// cmd.setToolTipText("remove current selected value and close.");
	    	panCommands.add(cmd);
    	}
    	return panCommands;
    }
    
	public void hide() {
	    popup.setVisible(false);
	}

	protected JList list = new JList();
	public JList getList() {
	    return list;
	}

	public MouseListener getMouseListener() {
	    return this;
	}

	public MouseMotionListener getMouseMotionListener() {
	    return this;
	}

	public KeyListener getKeyListener() {
	    return this;
	}

	public boolean isVisible() {
	    return popup.isVisible();
	}

	
	
	public void uninstallingUI() {
	    popup.removePopupMenuListener(this);
	}

	//
	// end ComboPopup method implementations
	//======================================



	//===================================================================
	// begin Event Listeners
	//

	// MouseListener

	// MouseListener
	public void mousePressed( MouseEvent e ) {
		doPopup(e); // 20080515
	}
    public void mouseReleased( MouseEvent e ) {}
    

	// something else registered for MousePressed
	public void mouseClicked(MouseEvent e) {
		// 20080515 was: doPopup(e);
	}
	protected void doPopup(MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if (!comboBox.isEnabled()) return;

	    if (comboBox.isEditable() ) { 
	    	comboBox.getEditor().getEditorComponent().requestFocus();
	    } 
	    else {
	    	comboBox.requestFocus();
	    }
	    togglePopup();
	}

	protected boolean mouseInside = false;
	public void mouseEntered(MouseEvent e) {
	    mouseInside = true;
	}
	public void mouseExited(MouseEvent e) {
	    mouseInside = false;
	}

	// MouseMotionListener
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	// KeyListener
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyReleased( KeyEvent e ) {
	    if ( e.getKeyCode() == KeyEvent.VK_SPACE ||
		 e.getKeyCode() == KeyEvent.VK_ENTER ) {
		togglePopup();
	    }
	}

	/**
	 * Variables hideNext and mouseInside are used to
	 * hide the popupMenu by clicking the mouse in the JComboBox
	 */
	public void popupMenuCanceled(PopupMenuEvent e) {}
	protected boolean hideNext = false;
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//System.out.println("popupMenuWillBecomeInvisible");//qqqq
	    hideNext = mouseInside;
	}
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	//
	// end Event Listeners
	//=================================================================

	//===================================================================
	// begin Utility methods
	//

	protected void togglePopup() {
		//20080515 was:	    if ( isVisible() || hideNext ) {
	    if ( isVisible() ) {

		hide();
	    } else {

		show();
	    }
	    hideNext = false;
	}

}


