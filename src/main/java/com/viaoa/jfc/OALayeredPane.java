package com.viaoa.jfc;

import java.awt.*;

import javax.swing.*;

import com.viaoa.hub.*;

/**
 * Not quite ready:  the comp still sometimes shows on top of the top panel.
 *    It appears to be a painting issue.
 * 
 * This is to allow painting a semi-transparent panel if the hub.AO = null.
 * Components can still be painted without calling the panels paint method.
 * 
 * @author vvia
 *
 */
public class OALayeredPane extends JPanel {
    private Hub hub;
    private JComponent comp;
    private JPanel panel; // glass pane
    
    public OALayeredPane(Hub h, JComponent comp) {
        this.comp = comp;
        
        add(comp, JLayeredPane.DEFAULT_LAYER);

        panel = new JPanel();
        // panel.setOpaque(true);
        Color c = getBackground();
        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 200);
        panel.setBackground(c);
        add(panel, JLayeredPane.PALETTE_LAYER);
        
        setHub(h);
    }

    @Override
    public void doLayout() {
        // TODO Auto-generated method stub
        super.doLayout();
        update();
    }
    
    public void setHub(Hub h) {
        this.hub = h;
        if (h == null) return;
        
        hub.addHubListener(new HubListenerAdapter() {
            @Override
            public void afterChangeActiveObject(HubEvent e) {
                update();
            }
        });
        update();
    }
    
    public void update() {
        if (comp == null) return;
        Dimension d = this.getSize();
        comp.setBounds(0, 0, d.width, d.height);
        panel.setBounds(0, 0, d.width, d.height);
        
        boolean b = (hub == null || hub.getAO() == null);
b = true;        
        panel.setVisible(b);
    }
}

