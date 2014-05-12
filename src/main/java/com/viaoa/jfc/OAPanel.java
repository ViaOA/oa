package com.viaoa.jfc;

import java.awt.*;

import javax.swing.*;
import com.viaoa.hub.*;

/**
 * 20140509 not ready.  This is to allow painting a semi-transparent panel if the hub.AO = null.
 * Components can still be painted without calling the panels paint method.
 * 
 * @author vvia
 *
 */
public class OAPanel extends JPanel {
    private Hub hub;
    
    public OAPanel(Hub h, LayoutManager lm) {
        super(lm);
        this.hub = h;
    }

    public OAPanel(Hub h) {
        this.hub = h;
    }

    @Override
    public void paintAll(Graphics g) {
        // TODO Auto-generated method stub
        super.paintAll(g);
        System.out.println("paintAll");
    }
    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        System.out.println("paintChildren");
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        // TODO Auto-generated method stub
        super.paintComponent(g);
        System.out.println("paintComponent");
    }
    @Override
    public void paintComponents(Graphics g) {
        // TODO Auto-generated method stub
        super.paintComponents(g);
        System.out.println("paintComponents");
    }
    
    
    @Override
    public void paint(Graphics gr) {
        super.paint(gr);

//        if (hub == null && hub.getAO() != null) return;
        
        Graphics2D g = (Graphics2D) gr;
        Dimension d = getSize();
        Color c = getBackground();
        c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 200);
        //c = Color.red;
        g.setColor(c);
        g.fillRect(0, 0, d.width, d.height);
    }

}

