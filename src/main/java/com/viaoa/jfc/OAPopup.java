/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/

package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;


/**
    PopupMenu that will bind button(s) to control open and close of a component that is within the popup.
    Used to add a popup/dropdown to a button.
    <p>
    Example:<br>
    OAPopup pop = new OAPopup(tree, cmdOpen, cmdClose);
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAPopup extends JPopupMenu {

    protected int align;
    protected boolean bRightClick;
    public int widthAdd;
    protected JComponent popupComponent, cmdOpen;

    /** 
        Create new Popup without a controlling component that causes it to popup.
    */
    public OAPopup(JComponent popupComponent) {
        this(popupComponent, null, null, 0);
    }
    
    /** 
        Create new Popup with a component that wiil cause it to popup on mouse click.
        @param popupComponent component to display
        @param cmdOpen component that causes popup to be displayed.  Popup will be display under
        this component.
    */
    public OAPopup(JComponent popupComponent, JComponent cmdOpen) {
        this(popupComponent, cmdOpen, null, 0);
    }
    
    /** 
        Create new Popup with a component that wiil cause it to popup on mouse click.
        @param popupComponent component to display
        @param cmdOpen component that causes popup to be displayed.  Popup will be display under
        this component.
        @param align SwingConstants.LEFT, RIGHT, CENTER
    */
    public OAPopup(JComponent popupComponent, JComponent cmdOpen, int align) {
        this(popupComponent, cmdOpen, null, align);
    }

    /** 
        Create new Popup with components that wiil cause it to popup/hide on mouse click.
        @param popupComponent component to display
        @param cmdOpen component that causes popup to be displayed.  Popup will be display under
        this component.
        @param cmdClose component that causes popup to be closed.
    */
    public OAPopup(JComponent popupComponent, JComponent cmdOpen, JComponent cmdClose) {
        this(popupComponent, cmdOpen, cmdClose, 0);
    }

    /** 
        Create new Popup with components that wiil cause it to popup/hide on mouse click.
        @param popupComponent component to display
        @param cmdOpen component that causes popup to be displayed.  Popup will be display under
        this component.
        @param cmdClose component that causes popup to be closed.
        @param align SwingConstants.LEFT, RIGHT, CENTER
    */
    public OAPopup(JComponent popupComponent, JComponent cmdOpen, JComponent cmdClose, int align) {
        // was: 070202: this.add(new JScrollPane(popupComponent));
        this.popupComponent = popupComponent;
        this.add(popupComponent);
        this.cmdOpen = cmdOpen;
        setupListener(cmdOpen);
        setupCloseListener(cmdClose);
        setAlign(align);
        // if (popupComponent != null) this.add(popupComponent);
    }

    /**
        Flag to have the popup displayed only when the right mouse button is clicked.
    */
    public void setRightClickOnly(boolean b) {
        bRightClick = b;
    }

    /**
        Set alignment SwingConstants.LEFT, RIGHT, CENTER.  
        <p>
        Note: This is not currently implemented.
    */
    public void setAlign(int i) {
        align = i;
    }

    /**
        Used to set a component to automatically close the popup.
    */
    public void setupCloseListener(JComponent component) {
        if (component == null) return;
        component.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                OAPopup.this.setVisible(false);
            }
        });
    }

    /**
        Overwritten to add widthAdd to the preferred width of popup.
        @see #setAddWidth
    */
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width += widthAdd;
        return d;
    }

    /**
        Number of pixels to add to the width of the default preferred size.
    */
    public void setAddWidth(int x) {
        widthAdd = x;    
    }
    /**
        Number of pixels to add to the width of the default preferred size.
    */
    public int getAddWidth() {
        return widthAdd;
    }

    private boolean bShowing;

    /**
        Sets a component to have it open the popup.
    */
    public void setupListener(JComponent component) {
        if (component == null) return;
        /*
        if (component == cmdOpen && cmdOpen instanceof JToggleButton) {
            this.addPopupMenuListener(new PopupMenuListener() {
                public void popupMenuCanceled(PopupMenuEvent e) {
                }
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                }
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    if ( ((JToggleButton) OAPopup.this.cmdOpen).isSelected() ) {
                        ((JToggleButton) OAPopup.this.cmdOpen).setSelected(false);
                    }
                }
            });
           
            
            ((JToggleButton) component).addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent evt) {
                    if (evt.getStateChange() == ItemEvent.SELECTED) {
                        showPopup(OAPopup.this.cmdOpen);
                    }
                    else {
                        OAPopup.this.setVisible(false);
                    }
                }
            });            
            return;
        }
        */
        component.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                bShowing = OAPopup.this.isVisible();
            }
            public void mouseReleased(MouseEvent e) {
                if (!bShowing) {
                    if (bRightClick && !e.isPopupTrigger()) return;

                    Component comp = (Component) e.getSource();
                    if (comp.isEnabled()) {
                        showPopup(comp);
                    }
                }
                else OAPopup.this.setVisible(false);
            }
        });
    }

    protected void showPopup(Component comp) {

        Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
        //qqqq  needs to use multi monitor screen size qqq 
        Dimension dComp = comp.getSize();
        Point ptComp = new Point(0,0);
        SwingUtilities.convertPointToScreen(ptComp, comp);

        Dimension dPopup = this.getSize();
        if (dPopup.width < 5) {
            show(comp, 0,0);
            dPopup = this.getSize();
        }
        
        
        boolean bUp = false;
        // see if it should pop UP or DOWN
        if (ptComp.y + dComp.height + dPopup.height > dScreen.height) {
            bUp = true;
            // pop UP
            if (ptComp.y < dPopup.height) {
                // not enough room to pop UP
                if (ptComp.y > (dScreen.height/2)) {
                    // pop UP, but shrink size
                    dPopup.height = ptComp.y;
                }
                else {
                    bUp = false;
                    dPopup.height = dScreen.height - (ptComp.y + dComp.height);
                }
                setPopupSize(dPopup);
            }
        }
        
        boolean bRight = false;  // alignment
        if (ptComp.x + dPopup.width > dScreen.width) {
            bRight = true;
            if (ptComp.x + dComp.width < dPopup.width) {
                if (ptComp.x > (dScreen.height/2)) {
                    dPopup.width = ptComp.x + dComp.width;
                }
                else {
                    bRight = false;
                    dPopup.width = dScreen.width - ptComp.x;
                }
                setPopupSize(dPopup);
            }
        }
        
        int x,y;
        
        if (bRight) {
            x = dComp.width - dPopup.width;
        }
        else {
            x = 0;
        }
        
        if (bUp) {
            y = -dPopup.height;
        }
        else {
            y = dComp.height;
        }
        show(comp, x, y);
    }


        
}


