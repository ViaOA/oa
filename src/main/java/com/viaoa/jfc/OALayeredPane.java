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

