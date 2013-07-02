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
package com.viaoa.jfc;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * Manages multiple buttons to be choosen from a dropdown, displaying an active button.
 * 
 * Note: there is no action event for this button.  The buttons that are added will
 * get the action event by having the main button call the current active buttons doClick method.
 * @author vvia
 *
 */
public class OAMultiButtonSplitButton extends OASplitButton {
    private static final long serialVersionUID = 1L;

    private JPopupMenu popup;
    private JButton cmdSelected;
    private boolean bShowTextInSelectedButton = true;
    private GridBagConstraints gc;
    private boolean bIsPopupVisible;
    
    public JPopupMenu getPopupMenu() {
        return popup;
    }
    
    public OAMultiButtonSplitButton() {
        popup = new JPopupMenu();
        
        popup.setInvoker(this);
        
        
//        BoxLayout lay = new BoxLayout(popup, BoxLayout.Y_AXIS);
//        popup.setLayout(lay);
        
        gc = new GridBagConstraints();
        Insets ins = new Insets(1, 3, 1, 3);
        gc.insets = ins;
        gc.anchor = gc.NORTHWEST;
        gc.gridwidth = gc.REMAINDER;
        gc.fill = gc.BOTH;
        popup.setLayout(new GridBagLayout());
        
        
        dropDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAMultiButtonSplitButton sb = OAMultiButtonSplitButton.this;                
                popup.show(sb, 0, sb.getHeight());
            }
        });
        
        // 20110810
        propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("enabled".equalsIgnoreCase(evt.getPropertyName())) {
                    Object val= evt.getNewValue();
                    boolean b = (val instanceof Boolean) && ((Boolean) val).booleanValue();
                    mainButton.setEnabled(b);
                }
            }
        };
        
    }
    
    public void setShowTextInSelectedButton(boolean b) {
        bShowTextInSelectedButton = b;
    }

    // can a dropdown button become the main button?
    private boolean bAllowChangeMasterButton = true;
    public void setAllowChangeMasterButton(boolean b) {
        bAllowChangeMasterButton = b;
    }
    
    @Override
    protected void setupMainButtonListener() {
        mainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cmdSelected != null) {
                    cmdSelected.doClick();
                }
            }
        });
    }
    
    public void addButton(final JButton cmd) {
        addButton(cmd, false);
    }
  
    private PropertyChangeListener propertyChangeListener;
    
    private boolean bFirst=true;
    public void addButton(final JButton cmd, boolean bDefault) {
        
        // cmd.setAlignmentX(LEFT_ALIGNMENT);
        cmd.setHorizontalAlignment(SwingConstants.LEFT);  // Sets the horizontal alignment of the icon and text.
        
        popup.add(cmd, gc);
        if (bDefault || bFirst) {
            bFirst = false;
            setSelected(cmd);
        }
        cmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (bAllowChangeMasterButton) {
                    setSelected(cmd);
                }
                popup.setVisible(false);
            }
        });
    }

    public void setSelected(JButton cmd) {
        cmdSelected = cmd;
        if (bShowTextInSelectedButton) mainButton.setText(cmdSelected.getText());
        mainButton.setIcon(cmdSelected.getIcon());
        mainButton.setToolTipText(cmdSelected.getToolTipText());
        mainButton.setEnabled(cmdSelected.isEnabled());
        cmdSelected.addPropertyChangeListener(propertyChangeListener);
    }
    
    public static void main(String[] args) {
        final JFrame frm = new JFrame();
        frm.setLayout(new FlowLayout());
        frm.setDefaultCloseOperation(frm.EXIT_ON_CLOSE);

        OAMultiButtonSplitButton cmd = new OAMultiButtonSplitButton();
        cmd.setText("This is the button text");
        
        JButton but = new JButton("hey1");
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("1");
            }
        });
        cmd.addButton(but, true);

        but = new JButton("another drop down button");
        but.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("2");
            }
        });
        cmd.addButton(but, false);
        
        
        cmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });

        frm.add(cmd);

        frm.pack();
        Dimension d = frm.getSize();
        d.width *= 2;
        d.height *= 2;
        frm.setSize(d);
        frm.setLocation(1600, 500);
        frm.setVisible(true);
    }
    
    
}
