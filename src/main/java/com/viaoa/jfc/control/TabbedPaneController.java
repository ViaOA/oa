package com.viaoa.jfc.control;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import com.viaoa.hub.Hub;

/**
 * creates an EnabledController,
 * Keylistener (ctrl+down) to toggle tab layout policy,
 * popup menu to select tab layout policy.
 * @author vvia
 *
 */
public class TabbedPaneController {

    private Hub hub;
    private JTabbedPane tabbedPane;
    
    public TabbedPaneController(Hub hub, JTabbedPane tp) {
        this.hub = hub;
        this.tabbedPane = tp;

        if (tp != null && hub != null) new EnabledController(tp, hub);
        
        if (tp != null) setup();
    }
    
    private void setup() {
        final JPopupMenu pmenu = new JPopupMenu();
        tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, false), "tab");
        tabbedPane.getActionMap().put("tab", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int x = tabbedPane.getTabLayoutPolicy();
                tabbedPane.setTabLayoutPolicy(x == JTabbedPane.WRAP_TAB_LAYOUT ? JTabbedPane.SCROLL_TAB_LAYOUT : JTabbedPane.WRAP_TAB_LAYOUT);
            }
        });
        tabbedPane.setToolTipText("right click or [ctrl]+[down] to change tab layout");
        
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
        });
        
    }
    
}
