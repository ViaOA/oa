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

import javax.swing.*;

import javax.swing.event.PopupMenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.metal.MetalComboBoxUI;

import com.sun.java.swing.plaf.motif.MotifComboBoxUI;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;


import com.viaoa.hub.*;

/**
    ComboBox with drop down table.
*/
public class OATableComboBox extends OACustomComboBox {
    protected OATable comboTable;
    private MyTablePopup myTablePopup;

    /**
    */
    public OATableComboBox() {
    	control.bDisplayPropertyOnly = true;
    }

    
    /**
        @param hub is hub to use for displaying the current value.
        @param displayProperty property path to display
    */
    public OATableComboBox(OATable table, Hub hub, String displayProperty) {
        super(hub, displayProperty, false);
    	control.bDisplayPropertyOnly = true;
        setComboTable(table);
        table.updateUI();
    }

    /**
        @param hub is hub to use for displaying the current value.
        @param displayProperty property path to display
    */
    public OATableComboBox(Hub hub, String displayProperty) {
        super(hub, displayProperty, false);
    	control.bDisplayPropertyOnly = true;
    }


    public OATable getComboTable() {
        return comboTable;
    }
    public void setComboTable(OATable table) {
        this.comboTable = table;
    }

    
    /**
        override to create popup calendar
    */
    public void updateUI() {
	    ComboBoxUI cui = (ComboBoxUI) UIManager.getUI(this);
	    if (cui instanceof MotifComboBoxUI) {
	        cui = new MotifComboBoxUI() {
	            protected ComboPopup createPopup() {
	                myTablePopup = new MyTablePopup( comboBox, OATableComboBox.this );
	                return myTablePopup;
	            }
	        };
	    }
	    else if (cui instanceof WindowsComboBoxUI) {
	        cui = new WindowsComboBoxUI() {
	            protected ComboPopup createPopup() {
	                myTablePopup = new MyTablePopup( comboBox, OATableComboBox.this );
	                return myTablePopup;
	            }
	        };
	    }
	    else cui = new MetalComboBoxUI() {
	        protected ComboPopup createPopup() {
	            myTablePopup = new MyTablePopup( comboBox, OATableComboBox.this );
	            return myTablePopup;
	        }
	    };
        setUI(cui);
// 20100320        
        if (myTablePopup != null) {
            myTablePopup.popup.updateUI();
        }
        if (comboTable != null) comboTable.updateUI();
    }

    /**
        Called when popup.show is called.  Can be overwritten to add custom behaviors, ex: tree.expandRow(0)
    */
    public void onShow() {
    }
    
    public void hidePopup() {
        if (myTablePopup != null) myTablePopup.hide();        
    }
    
    @Override
    public String getTableToolTipText(JTable table, int row, int col, String defaultValue) {
        return defaultValue;
    }
}


class MyTablePopup implements ComboPopup, MouseMotionListener, MouseListener, KeyListener, PopupMenuListener {

	protected JComboBox comboBox;
	protected JPopupMenu popup;
	protected OATableComboBox cboTable;
	protected JPanel panCommands;

	public MyTablePopup(JComboBox comboBox, OATableComboBox cboTable) {
	    this.comboBox = comboBox;
	    this.cboTable = cboTable;
	    popup = new JPopupMenu();
	    popup.setBorder(BorderFactory.createLineBorder(Color.black));
	    popup.setLayout(new BorderLayout(0,0));
	    popup.addPopupMenuListener(this);
	}

	//========================================
	// begin ComboPopup method implementations
	private boolean bInit;
    public void show() {
        if (cboTable != null && cboTable.comboTable != null) {
            if (!bInit) {
                // hack: this cant be created in the constructor
                bInit = true;
                popup.add(new JScrollPane(cboTable.comboTable), BorderLayout.CENTER);
                popup.add(getButtonCommands(), BorderLayout.SOUTH);
            }
            cboTable.onShow();
        }
        
        Dimension d = popup.getSize();
        if (d == null || d.height == 0) {
            d = popup.getPreferredSize();
            d.height += 18; // does not include the table column heading
            popup.setPopupSize(d);
        }
        Rectangle rec = computePopupBounds(0, comboBox.getSize().height, d.width, d.height);

        for (int i=0; i<3; i++) {
            try {
                popup.show(comboBox, 0, rec.y);
                break;
            }
            catch (Exception e) {
            }
        }
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

    	if (cboTable.getAllowClearButton()) {
	    	cmd = new JButton("clear");
	    	cmd.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e) {
	    			cboTable.onClear();
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


