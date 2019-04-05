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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubEvent;
import com.viaoa.hub.HubListener;
import com.viaoa.hub.HubListenerAdapter;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAString;

/**
 * Used to have a toolbar with a buttons for the first (max) amount of objects in a hub.
 * The onButtonClick will be called when button is clicked.
 * The blink method can be called for any of the buttons.
 * @author vvia
 *
 */
public class OAButtonToolBar extends JToolBar {
    private Hub hub;
    String propertyPath;
    private JPanel pan;
    private JButton[] buttons;
    private int max;

    /**
     * @param hub
     * @param propertyPath text to show as the button text, will be formated to 12L.
     * @param max max numbers of objects to display
     */
    public OAButtonToolBar(Hub hub, String propertyPath, int max) {
        this.hub = hub;
        this.propertyPath = propertyPath;
        this.max = max;
        setup();
        if (hub != null && propertyPath != null) {
            setupHub();
        }
    }
    
    private static int cntStatic;
    private String ppListen;
    protected void setupHub() {
        HubListener hl = new HubListenerAdapter() {
            @Override
            public void afterPropertyChange(HubEvent e) {
                if (!ppListen.equalsIgnoreCase(e.getPropertyName())) return;

                Object obj = e.getObject();
                int pos = hub.getPos(obj);
                if (pos >= max) return;
                
                update(obj, pos);
            }
            @Override
            public void afterAdd(HubEvent e) {
                Object obj = e.getObject();
                int pos = hub.getPos(obj);
                if (pos >= max) return;
                update(obj, pos);
            }
            
            @Override
            public void afterInsert(HubEvent e) {
                int pos = e.getPos();
                if (pos >= max) return;
                
                for (int i=pos; i<max; i++) {
                    update(hub.getAt(i), i);
                }
            }
            
            @Override
            public void afterRemove(HubEvent e) {
                int pos = e.getPos();
                if (pos >= max) return;
                
                for (int i=pos; i<max; i++) {
                    update(hub.getAt(i), i);
                }
            }
        };
        ppListen = propertyPath;
        String[] pps = null;
        if (ppListen.indexOf('.') > 0) {
            ppListen = "calcOAButtonToolBar"+(cntStatic++);
            pps = new String[] {propertyPath};
        }
        hub.addHubListener(hl, ppListen, pps);
    }

    protected void update(Object obj, int pos) {
        if (obj == null) {
            String s = "";
            s = OAString.format(s, "12L.");
            buttons[pos].setText(s);
            buttons[pos].setVisible(false);
        }
        else {
            buttons[pos].setVisible(true);
            if (obj instanceof OAObject) {
                String s = ((OAObject) obj).getPropertyAsString(propertyPath);
                s = OAString.format(s, "12L.");
                buttons[pos].setText(s);
            }
            else {
                String s = OAString.format(obj+"", "12L.");
                buttons[pos].setText(s);
            }
        }
    }
    
    protected void setup() {
        add(Box.createHorizontalStrut(5));

        pan = new JPanel(new GridLayout(1, 2));

        buttons = new JButton[10];
        for (int i=0; i<buttons.length; i++) {
            
            buttons[i] = new JButton(" ");
            OAButton.setup(buttons[i]);
            buttons[i].setFocusable(false);
            buttons[i].setFocusPainted(false);
            pan.add(buttons[i]);

            final int pos = i;
            buttons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    onButtonClick(pos);
                }
            });
            
            Object obj;
            if (hub != null) obj = hub.getAt(i);
            else obj = null;
            update(obj, i);
        }
        
        add(new OAScroller(pan));
        
        
        add(Box.createHorizontalStrut(5));
        add(Box.createGlue());

        JButton but = new JButton("X");
        OAButton.setup(but);
        but.setFocusable(false);
        but.setFocusPainted(false);
        but.setToolTipText("hide");
        //but.setBorder(null);
        
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAButtonToolBar.this.setVisible(false);
            }
        });
            
        add(but);
        add(Box.createHorizontalStrut(5));
    }
    
    /**
     * Called when a button is clicked.
     * default is to set the hub AO.
     * @param pos
     */
    public void onButtonClick(int pos) {
        if (hub != null) {
            hub.setPos(pos);
        }
    }
    

    private Color color;
    /**
     * this can be used to have a button blink on/off for about one second.
     * @param pos position of the object in the hub, which is the position of the button in the toolbar.
     */
    public void blink(final int pos) {
        blink(pos, null);
    }
    public void blink(final int pos, Color c) {
        if (pos < 0 || pos >= buttons.length) return;
        if (color == null) color = buttons[pos].getBackground();
        final String txtHold = buttons[pos].getText();
        
        final Color colorBlink = c == null ? Color.white : c; 
        
        Timer timer = new Timer(100, new ActionListener() {
            int cnt;
            public void actionPerformed(ActionEvent e) {
                ++cnt;
                if (cnt % 2 == 0) {
                    buttons[pos].setOpaque(false);
                    buttons[pos].setBackground(color);
                    if (cnt > 10) {
                        buttons[pos].setText(txtHold);
                        ((Timer) e.getSource()).stop();
                    }
                    else buttons[pos].setText(" ");
                }
                else {
                    buttons[pos].setText(txtHold);
                    buttons[pos].setOpaque(true);
                    buttons[pos].setBackground(colorBlink);
                }
            }
        });
        timer.setRepeats(true);
        timer.start();
    }
    
    
}
