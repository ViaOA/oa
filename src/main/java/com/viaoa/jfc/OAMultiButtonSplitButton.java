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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.viaoa.util.OAArray;

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
    private boolean bShowSelectedButton = true;
    private GridBagConstraints gc;
    private boolean bIsPopupVisible;

    private JButton[] buttons = new JButton[0];
    private PropertyChangeListener propertyChangeListener;
    private boolean bFirst=true;
    private JPanel panHidden; // so that each comp/button will have a parent 
    
    public JPopupMenu getPopupMenu() {
        return popup;
    }
    
    public OAMultiButtonSplitButton() {
        popup = new JPopupMenu() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(d.width, OAMultiButtonSplitButton.this.mainButton.getPreferredSize().width);
                return d;
            }
        };
        
        panHidden = new JPanel();
        Dimension d = new Dimension(0,0);
        panHidden.setMaximumSize(d);
        panHidden.setPreferredSize(d);
        panHidden.setMinimumSize(d);
        super.add(panHidden, BorderLayout.WEST);
        
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
                    if (evt.getSource() == cmdSelected) {
                        mainButton.setEnabled(b);
                    }
                }
            }
        };
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        if (b && cmdSelected != null) {
            mainButton.setEnabled(cmdSelected.isEnabled());
        }
    }
    
    
    public void setShowSelectedButton(boolean b) {
        bShowSelectedButton = b;
    }
    public boolean getShowSelectedButton() {
        return bShowSelectedButton;
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
                if (bShowSelectedButton) {
                    if (cmdSelected != null) {
                        cmdSelected.doClick();
                    }
                }
                else {
                    // show popup
                    dropDownButton.doClick();
                }
            }
        });
    }
    
    public void addButton(final JButton cmd) {
        addButton(cmd, false);
    }
  
    
    
    public int getButtonCount() {
        return buttons==null?0:buttons.length;
    }
    

    public JButton[] getButtons() {
        return buttons;
    }
    
    public void addButton(final JButton cmd, boolean bDefault) {
        // cmd.setAlignmentX(LEFT_ALIGNMENT);
        cmd.setHorizontalAlignment(SwingConstants.LEFT);  // Sets the horizontal alignment of the icon and text.

        buttons = (JButton[]) OAArray.add(JButton.class, buttons, cmd);

        panHidden.add(cmd);
        
        boolean bAdd = true;
        if (bDefault || bFirst) {
            bFirst = false;
            setSelected(cmd);
            if (!bAllowChangeMasterButton) {
                if (bShowSelectedButton) {
                    bAdd = false;
                }
            }
        }

        if (bAdd) popup.add(cmd, gc);
        
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
        if (!bShowSelectedButton) return;
        cmdSelected = cmd;
        if (bShowTextInSelectedButton) {
            mainButton.setText(cmdSelected.getText());
            mainButton.setFont(cmdSelected.getFont());
        }
        mainButton.setIcon(cmdSelected.getIcon());
        mainButton.setToolTipText(cmdSelected.getToolTipText());
        mainButton.setEnabled(cmdSelected.isEnabled());
        cmdSelected.addPropertyChangeListener(propertyChangeListener);
    }
    
    public Dimension getPreferredSize() {
        if (mainButton == null) return super.getPreferredSize();
        
        Dimension d = mainButton.getPreferredSize();
    
        for (JButton b : buttons) {
            Dimension d2 = b.getPreferredSize();
            d.width = Math.max(d.width, d2.width);
        }
        
        
        Dimension d2 = dropDownButton.getPreferredSize();
        d.width += d2.width + 5;
        d.height = Math.max(d.height, d2.height);
        return d;
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
