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
    protected JComponent popupComponent, cmdOpen, cmdDisplayFrom;

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

    public OAPopup(JComponent popupComponent, JComponent cmdOpen, JComponent cmdClose, int align) {
        this(popupComponent, cmdOpen, cmdClose, cmdOpen, align);
    }

    /**
        Create new Popup with components that wiil cause it to popup/hide on mouse click.
        @param popupComponent component to display
        @param cmdOpen component that causes popup to be displayed.  Popup will be display under
        this component.
        @param cmdClose component that causes popup to be closed.
        @param align SwingConstants.LEFT, RIGHT, CENTER
        @param cmdDisplayFrom component used to display the popup in reference to, default is cmdOpen.
     */
    public OAPopup(JComponent popupComponent, JComponent cmdOpen, JComponent cmdClose, JComponent cmdDisplayFrom, int align) {
        // was: 070202: this.add(new JScrollPane(popupComponent));
        this.popupComponent = popupComponent;
        this.add(popupComponent);
        this.cmdOpen = cmdOpen;
        this.cmdDisplayFrom = cmdDisplayFrom;
        setupListener(cmdOpen);
        setupCloseListener(cmdClose);
        setAlign(align);
        // if (popupComponent != null) this.add(popupComponent);
    }
    
    public JComponent getOpenCommand() {
        return cmdOpen;
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

    @Override
    public void setPreferredSize(Dimension preferredSize) {
        // TODO Auto-generated method stub
        super.setPreferredSize(preferredSize);
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

        if (GraphicsEnvironment.isHeadless()) return;
        
        if (cmdDisplayFrom != null) comp = cmdDisplayFrom;
        
        // make sure that comp is on window 20140826
        Component c = comp;
        for ( ; c != null; ) {
            if (c instanceof OAPopup) { 
                OAPopup pop = (OAPopup) c;
                comp = c = pop.getOpenCommand();
            }
            else if (c instanceof JPopupMenu) {
                JPopupMenu pop = (JPopupMenu) c;
                comp = c = pop.getInvoker();
            }
            c = c.getParent();
        }

        Point ptComp = new Point(0,0);
        SwingUtilities.convertPointToScreen(ptComp, comp);
        
        Rectangle rectScreen = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; i++) {
            if (gd[i].getType() == GraphicsDevice.TYPE_RASTER_SCREEN) {
                GraphicsConfiguration dgc = gd[i].getDefaultConfiguration();
                if (dgc.getBounds().contains(ptComp)) {
                    rectScreen = dgc.getBounds();
                    break;
                }
            }
        }
        if (rectScreen == null) {
            rectScreen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        }

        Dimension dScreen = new Dimension(rectScreen.x + rectScreen.width, rectScreen.height);
        Dimension dComp = comp.getSize();
        
        
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
