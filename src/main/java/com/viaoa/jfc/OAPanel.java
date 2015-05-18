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

