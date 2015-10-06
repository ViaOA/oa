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

import com.viaoa.hub.Hub;
import com.viaoa.jfc.control.EnabledController;


/**
 * Creates a single JButton that internally has two buttons: a main button, and a button with a down arrow.
 * The SplitButton will manage the settings and events for mainButton, and the application will need to
 * create an actionListener to the dropDownButton.
 * 
 * This is a subclass of JButton, since toolbars have methods that work with buttons. ex: toolbar.setRollOver()
 *    
 * @author vvia
 */
public class OASplitButton extends JButton {
    private static final long serialVersionUID = 1L;
    protected JButton mainButton;   // this is wrapped by splitButton
    protected JButton dropDownButton;  // this is used to popup, etc.
    private boolean bIsInSuperPaint;

    public OASplitButton() {
        this.mainButton = new JButton();
        mainButton.setMargin(new Insets(0,0,0,-3));
        setupMainButtonListener();
        
        this.dropDownButton  = new JButton(new ImageIcon(this.getClass().getResource("icons/downCombobox.gif")));
        
        dropDownButton.setRequestFocusEnabled(false);
        dropDownButton.setFocusPainted(false);
        dropDownButton.setMargin(new Insets(0,2,0,2));

        
        this.setLayout(new BorderLayout(0, 0));
        this.setMargin(new Insets(-3, -3, -3, -3));
        
        // 20110810 this works with OAButton.setup(..) 
        setBorderPainted(false);
        setContentAreaFilled(false);
        
        this.add(dropDownButton, BorderLayout.EAST);
        this.add(mainButton, BorderLayout.CENTER);
    }

    protected void setupMainButtonListener() {
        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OASplitButton.this.fireActionPerformed(e);
            }
        });
    }    
    
    //@Override
    protected void paintComponent(Graphics g) {
        bIsInSuperPaint = true;
        super.paintComponent(g);
        bIsInSuperPaint = false;
    }
    
    public Dimension getPreferredSize() {
        if (mainButton == null) return super.getPreferredSize();
        Dimension d = mainButton.getPreferredSize();
        Dimension d2 = dropDownButton.getPreferredSize();
        d.width += d2.width + 5;
        d.height = Math.max(d.height, d2.height);
        return d;
    }
    
    public Dimension getMaximumSize() {
        if (mainButton == null) return super.getMaximumSize();
        return getPreferredSize();
    }
    
    public Dimension getMinimumSize() {
        if (mainButton == null) return super.getMinimumSize();
        return getPreferredSize();
    }    
 
    public void setText(String s) {
        if (mainButton != null) mainButton.setText(s);
    }
    public String getText() {
        if (mainButton == null || bIsInSuperPaint) return null;
        return mainButton.getText();
    }

    public void setIcon(Icon icon) {
        if (mainButton == null) return;      
        mainButton.setIcon(icon);
    }
    public Icon getIcon() {
        if (mainButton == null || bIsInSuperPaint) return null;
        return mainButton.getIcon();
    }

    public void setToolTipText(String s) {
        if (mainButton == null) return;      
        mainButton.setToolTipText(s);
    }
    public String setToolTipText() {
        if (mainButton == null) return null;
        return mainButton.getToolTipText();
    }
    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        mainButton.setFocusable(focusable);
    }
    @Override
    public void setFocusPainted(boolean b) {
        super.setFocusPainted(b);
        mainButton.setFocusPainted(b);
    }
    @Override
    public void setRequestFocusEnabled(boolean b) {
        super.setRequestFocusEnabled(b);
        mainButton.setRequestFocusEnabled(b);
    }
    
    @Override
    public void setEnabled(boolean b) {
        mainButton.setEnabled(b);
        dropDownButton.setEnabled(b);
        super.setEnabled(b);
    }

    /**
     * gets the drop down button (with the arrow)
     * @return JButton
     */
    public JButton getDropDownButton() {
        return dropDownButton;
    }

    /**
     * Mainbutton that has all of the splitButtons methods forward to - ex: actionListener, enable, etc 
     * @return
     */
    public JButton getMainButton() {
        return mainButton;
    }

    
}


