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
package com.viaoa.jfc.scroller;



//http://www.javadocexamples.com/java_source/frost/gui/ScrollableBarUI.java.html


import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.ViewportLayout;

import javax.swing.plaf.ComponentUI;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.viaoa.jfc.OAScroller;

/**
 * Vertical scroller, used for scrolling toolbar
 * @author vvia
 *
 */
public class OAScrollerUI extends ComponentUI 
implements SwingConstants, 
MouseListener,
ChangeListener, 
PropertyChangeListener {

    private OAScroller sb;
    private JViewport scroll;
    private JButton scrollF, scrollB;
    private boolean pressed = false;
    private int inc;

    public static ComponentUI createUI(JComponent c) {
        return new OAScrollerUI();
    }

    public void installUI(JComponent c) {

        sb = (OAScroller) c;
        sb.removeAll();

        inc = sb.getIncrement();
        boolean small = sb.isSmallArrows();

        // Create the Buttons
        int sbSize = 18;
        Integer i = (Integer) UIManager.get("ScrollBar.width");
        if (i != null) sbSize = i.intValue();

        scrollB = createButton(sb.isHorizontal()?WEST:NORTH, sbSize, small);
        scrollB.setVisible(false);
        scrollB.addMouseListener(this);

        scrollF = createButton(sb.isHorizontal()?EAST:SOUTH, sbSize, small);
        scrollF.setVisible(false);
        scrollF.addMouseListener(this);

        int axis = sb.isHorizontal()?BoxLayout.X_AXIS:BoxLayout.Y_AXIS;
        sb.setLayout(new BoxLayout(sb, axis));

        scroll = new JViewport() {
            // Create a customized layout manager
            protected LayoutManager createLayoutManager() {
                return new ViewportLayout() {
                    public Dimension minimumLayoutSize(Container parent) {
                        Component view = ((JViewport)parent).getView();
                        if (view == null) {
                            return new Dimension(4, 4);
                        }
                        else {
                            Dimension d = view.getPreferredSize();
                            if (sb.isHorizontal()) {
                                return new Dimension(4, (int)d.getHeight());
                            }
                            else {
                                return new Dimension((int)d.getWidth(), 4);
                            }
                        }  
                    }
                };
            }
        };

        Component box = sb.getComponent();
        scroll.setView(box);

        sb.add(scrollB);
        sb.add(scroll);
        sb.add(scrollF);

        // Install the change listeners
        scroll.addChangeListener(this);
        sb.addPropertyChangeListener(this);
    }

    public void uninstallUI(JComponent c) {
        // Remove the change listeners
        scroll.removeChangeListener(this);
        sb.removePropertyChangeListener(this);
    }

    protected JButton createButton(int direction, int width, boolean small) {
        JButton button = new ScrollButton(direction, width, small);
        button.setRequestFocusEnabled(false);
        button.setAlignmentX(0.5f);
        button.setAlignmentY(0.5f);
        return button;
    }

    // PropertyChangeListner methods.

    public void propertyChange(PropertyChangeEvent evt) {
        if ("increment".equals(evt.getPropertyName())) {
            inc = ((Integer)evt.getNewValue()).intValue();
        }
        else if ("smallArrows".equals(evt.getPropertyName())) {
            boolean small = ((Boolean)evt.getNewValue()).booleanValue();
            ((ScrollButton)scrollB).setSmallArrows(small);
            ((ScrollButton)scrollF).setSmallArrows(small);
        }
        else if ("component".equals(evt.getPropertyName())) {
            scroll.setView((Component)evt.getNewValue());
        }
    }

    // ChangeListner methods.

    public void stateChanged(ChangeEvent e) {
        boolean cond = sb.isHorizontal() ?
                sb.getWidth() < scroll.getViewSize().width:
                    sb.getHeight() < scroll.getViewSize().height;
                if (cond) {
                    scrollB.setVisible(true);
                    scrollF.setVisible(true);
                }
                else {
                    scrollB.setVisible(false);
                    scrollF.setVisible(false);
                    sb.doLayout();
                }
    }

    // MouseListener methods.

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
        pressed = false;
    }

    public void mouseReleased(MouseEvent e) {
        pressed = false;
    }

    public void mousePressed(MouseEvent e) {
        pressed = true;
        final Object o = e.getSource();
        Thread scroller = new Thread(new Runnable() {
            public void run() {
                int accl = 500;
                while (pressed) {
                    Point p = scroll.getViewPosition();
                    //  "Compute new view position"
                    if (sb.isHorizontal()) {
                        if (o == scrollB) {
                            p.x -= inc;
                            if (p.x < 0) {
                                p.x = 0;
                                scroll.setViewPosition(p);
                                return;
                            }
                        }
                        else {
                            if (scroll.getViewSize().width - p.x - scroll.getExtentSize().width > inc) {
                                p.x += inc;
                            }
                            else {
                                p.x = scroll.getViewSize().width - 
                                scroll.getExtentSize().width;
                                scroll.setViewPosition(p);
                                return;
                            }
                        }
                    }

                    else {
                        if (o == scrollB) {
                            p.y -= inc;
                            if (p.y < 0) {
                                p.y = 0;
                                scroll.setViewPosition(p);
                                return;
                            }
                        }
                        else {
                            if (scroll.getViewSize().height - p.y - 
                                    scroll.getExtentSize().height > inc) {
                                p.y += inc;
                            }
                            else {
                                p.y = scroll.getViewSize().height - 
                                scroll.getExtentSize().height;
                                scroll.setViewPosition(p);
                                return;
                            }
                        }
                    }
                    // ...
                    scroll.setViewPosition(p);
                    try {
                        Thread.sleep(accl);
                        if (accl <= 10) accl = 10;
                        else accl /= 2;
                    } catch (InterruptedException ie) {}
                }
            }
        });
        scroller.start();
    }

}