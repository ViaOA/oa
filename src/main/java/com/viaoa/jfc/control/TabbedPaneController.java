package com.viaoa.jfc.control;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubChangeListener;
import com.viaoa.jfc.OATable;

/**
 * creates an EnabledController, Keylistener (ctrl+down) to toggle tab layout policy, popup menu to
 * select tab layout policy.
 * 
 * @author vvia
 *
 */
public class TabbedPaneController {

    private Hub hub;
    private JTabbedPane tabbedPane;
    private JRadioButton radScroll;
    private JRadioButton radWrap;
    private JRadioButton radTop, radLeft, radBottom, radRight;

    public TabbedPaneController(Hub hub, JTabbedPane tp) {
        this.hub = hub;
        this.tabbedPane = tp;

        if (tp != null && hub != null) {
            new OAJfcController(hub, tp, HubChangeListener.Type.AoNotNull);
            //was: new EnabledController(tp, hub);
        }

        if (tp != null) setup();
    }

    private void setup() {
        final JPopupMenu pmenu = new JPopupMenu();

        pmenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                if (tabbedPane == null) return;
                if (tabbedPane.getTabLayoutPolicy() == JTabbedPane.SCROLL_TAB_LAYOUT) {
                    radScroll.setSelected(true);
                }
                else radWrap.setSelected(true);

                switch (tabbedPane.getTabPlacement()) {
                case JTabbedPane.TOP:
                    radTop.setSelected(true);
                    break;
                case JTabbedPane.LEFT:
                    radLeft.setSelected(true);
                    break;
                case JTabbedPane.BOTTOM:
                    radBottom.setSelected(true);
                    break;
                case JTabbedPane.RIGHT:
                    radRight.setSelected(true);
                    break;
                }

            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });

        tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, false),
                "tab");
        tabbedPane.getActionMap().put("tab", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int x = tabbedPane.getTabLayoutPolicy();
                tabbedPane.setTabLayoutPolicy(x == JTabbedPane.WRAP_TAB_LAYOUT ? JTabbedPane.SCROLL_TAB_LAYOUT
                        : JTabbedPane.WRAP_TAB_LAYOUT);
            }
        });
        // shows up on pane, instead of just the tabs
        //tabbedPane.setToolTipText("right click or [ctrl]+[down] to change tab layout");

        ButtonGroup bg = new ButtonGroup();
        JRadioButton rad = new JRadioButton();
        rad.setText("Scrolling tabs");
        rad.setSelected(true);
        rad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT); // WRAP_TAB_LAYOUT
                pmenu.setVisible(false);
            }
        });
        bg.add(rad);
        radScroll = rad;
        pmenu.add(rad);

        rad = new JRadioButton();
        rad.setText("Wrapping tabs");
        rad.setSelected(true);
        rad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
                pmenu.setVisible(false);
            }
        });
        bg.add(rad);
        radWrap = rad;
        pmenu.add(rad);

        pmenu.addSeparator();

        bg = new ButtonGroup();
        rad = new JRadioButton();
        rad.setText("Tabs on top");
        rad.setSelected(true);
        rad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setTabPlacement(JTabbedPane.TOP);
                pmenu.setVisible(false);
            }
        });
        bg.add(rad);
        radTop = rad;
        pmenu.add(rad);

        rad = new JRadioButton();
        rad.setText("Tabs on left");
        rad.setSelected(true);
        rad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setTabPlacement(JTabbedPane.LEFT);
                pmenu.setVisible(false);
            }
        });
        bg.add(rad);
        radLeft = rad;
        pmenu.add(rad);

        rad = new JRadioButton();
        rad.setText("Tabs on bottom");
        rad.setSelected(true);
        rad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);
                pmenu.setVisible(false);
            }
        });
        bg.add(rad);
        radBottom = rad;
        pmenu.add(rad);

        rad = new JRadioButton();
        rad.setText("Tabs on right");
        rad.setSelected(true);
        rad.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tabbedPane.setTabPlacement(JTabbedPane.RIGHT);
                pmenu.setVisible(false);
            }
        });
        bg.add(rad);
        radRight = rad;
        pmenu.add(rad);

        tabbedPane.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point pt = e.getPoint();
                    pmenu.show(tabbedPane, pt.x, pt.y);

                }
                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    Point pt = e.getPoint();
                    pmenu.show(tabbedPane, pt.x, pt.y);
                }
                super.mouseReleased(e);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (lblMouseMoveTab != null) {
                    lblMouseMoveTab.setBackground(bgColor);
                    lblMouseMoveTab.setForeground(fgColor);
                    lblMouseMoveTab.setOpaque(false);
                    lblMouseMoveTab = null;
                }
            }
        });

        
        tabbedPane.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getBoundsAt(i).contains(e.getPoint())) {
                        JComponent comp = (JComponent) tabbedPane.getTabComponentAt(i);
                        if (comp instanceof JLabel) {
                            if (lblMouseMoveTab != null) {
                                lblMouseMoveTab.setBackground(bgColor);
                                lblMouseMoveTab.setForeground(fgColor);
                                lblMouseMoveTab.setOpaque(false);
                            }
                            if (!tabbedPane.isEnabled()) return;

                            JLabel lbl = (JLabel) comp;
                            if (fgColor == null) {
                                fgColor = lbl.getForeground();
                                bgColor = lbl.getBackground();
                            }

                            lbl.setBackground(OATable.COLOR_MouseOver);
                            lbl.setForeground(Color.white);
                            lbl.setOpaque(true);
                            
                            lblMouseMoveTab = lbl;
                            
                            break;
                        }
                    }
                }
            }
        });

        tabbedPane.addPropertyChangeListener("enabled", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean b = tabbedPane.isEnabled();
                for (int i=0; i<tabbedPane.getTabCount(); i++) {
                    JComponent comp = (JComponent) tabbedPane.getTabComponentAt(i);
                    if (comp instanceof JLabel) {
                        ((JLabel) comp).setEnabled(b);
                    }
                }                
            }
        });
        
        
        tabbedPane.addContainerListener(new ContainerListener() {
            @Override
            public void componentRemoved(ContainerEvent e) {
            }
            @Override
            public void componentAdded(ContainerEvent e) {
                updateTabs();
            }
        });
        updateTabs();
    }

    private JLabel lblMouseMoveTab;
    private Color fgColor, bgColor;
    
    protected void updateTabs() {
        boolean b = tabbedPane.isEnabled();
        for (int i=0; i<tabbedPane.getTabCount(); i++) {
            JComponent comp = (JComponent) tabbedPane.getTabComponentAt(i);
            if (comp != null) continue;
            String s = tabbedPane.getTitleAt(i);
            JLabel lbl = new JLabel(s);
            lbl.setEnabled(b);
            lbl.setIcon(tabbedPane.getIconAt(i));
            tabbedPane.setTabComponentAt(i, lbl);
        }
    }
    
}
