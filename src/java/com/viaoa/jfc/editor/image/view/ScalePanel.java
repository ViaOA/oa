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
package com.viaoa.jfc.editor.image.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

/**
 * Panel used to enter information for changing an image scale.
 * @author vincevia
 * @see ScalePanelController controller for this panel.
 */
public abstract class ScalePanel extends JPanel {
    private NumberTextField txtScalePercent, txtScaleWidth, txtScaleHeight;
    private JLabel lblScaleDescription, lblScaleNewDescription;
    private JButton cmdPerformScale;
    private JButton cmdUseZoomScale;

    public ScalePanel() {
        super(new GridBagLayout());
       
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 1, 1, 2);

        gc.anchor = gc.EAST;
        add(new JLabel("Current Image:"), gc);
        gc.anchor = gc.WEST;
        
        gc.gridwidth = gc.REMAINDER;
        gc.fill = gc.BOTH;
        add(getScaleDescriptionLabel(), gc);
        gc.gridwidth = 1;
        gc.fill = gc.NONE;
        
        
        Box box = new Box(BoxLayout.X_AXIS);
        
        box.add(new JLabel("Scale:"));
        box.add(getPercentTextField());
        box.add(new JLabel("%"));
        box.add(Box.createHorizontalStrut(6));

        box.add(new JLabel("Width:"));
        box.add(getWidthTextField());
        box.add(Box.createHorizontalStrut(5));

        box.add(new JLabel("Height:"));
        box.add(getHeightTextField());

        box.add(Box.createHorizontalStrut(5));
        box.add(getUseZoomScaleCommand());
        
        
        box.add(Box.createHorizontalStrut(5));
        box.add(getScaleCommand());

        
        box.add(Box.createHorizontalStrut(10));
        
        gc.gridwidth = gc.REMAINDER;
        add(box, gc);
        gc.gridwidth = 1;
        
        
    }
    
    public NumberTextField getPercentTextField() {
        if (txtScalePercent == null) {
            txtScalePercent = new NumberTextField(3);
            txtScalePercent.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String s = txtScalePercent.getText();
                    if (s != null && s.length() > 0) {
                        int x = Integer.valueOf(s);
                        ScalePanel.this.onScalePercentChange(x);
                    }
                }
            });
            txtScalePercent.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    String s = txtScalePercent.getText();
                    if (s != null && s.length() > 0) {
                        int x = Integer.valueOf(s);
                        ScalePanel.this.onScalePercentChange(x);
                    }
                }
                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }
        return txtScalePercent;
    }

    public NumberTextField getWidthTextField() {
        if (txtScaleWidth == null) {
            txtScaleWidth = new NumberTextField(3);
            txtScaleWidth.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int w = Integer.valueOf(txtScaleWidth.getText()).intValue();
                    ScalePanel.this.onWidthChange(w);
                }
            });
            txtScaleWidth.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    int w = Integer.valueOf(txtScaleWidth.getText()).intValue();
                    ScalePanel.this.onWidthChange(w);
                }
                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }
        return txtScaleWidth;
    }

    public NumberTextField getHeightTextField() {
        if (txtScaleHeight == null) {
            txtScaleHeight = new NumberTextField(3);
            txtScaleHeight.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int h = Integer.valueOf(txtScaleHeight.getText()).intValue();
                    ScalePanel.this.onHeightChange(h);
                }
            });
            txtScaleHeight.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    int h = Integer.valueOf(txtScaleHeight.getText()).intValue();
                    ScalePanel.this.onHeightChange(h);
                }
                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }
        return txtScaleHeight;
    }
    
    public JButton getScaleCommand() {
        if (cmdPerformScale == null) {
            cmdPerformScale = new JButton("Ok");
            cmdPerformScale.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ScalePanel.this.onScaleCommand();
                }
            });
            String cmdName = "onClick";
            cmdPerformScale.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false), cmdName);
            cmdPerformScale.getActionMap().put(cmdName, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onScaleCommand();
                }
            });
        }
        return cmdPerformScale;
    }

    public JButton getUseZoomScaleCommand() {
        if (cmdUseZoomScale == null) {
            cmdUseZoomScale = new JButton("Zoom Size");
            cmdUseZoomScale.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ScalePanel.this.onUseZoomScaleCommand();
                }
            });
            cmdUseZoomScale.setToolTipText("use the currect zoom size as the new scale");
        }
        return cmdUseZoomScale;
    }
    
    
    public JLabel getScaleDescriptionLabel() {
        if (lblScaleDescription == null) {
            lblScaleDescription = new JLabel(" ");
            lblScaleDescription.setOpaque(false);
        }
        return lblScaleDescription;
    }
    public JLabel getScaleNewDescriptionLabel() {
        if (lblScaleNewDescription == null) {
            lblScaleNewDescription = new JLabel(" ");
            lblScaleNewDescription.setOpaque(false);
        }
        return lblScaleNewDescription;
    }

    
    /**
     * called when one of the NumberTextFields are changed.
     */ 
    
    protected abstract void onScaleCommand();
    protected abstract void onUseZoomScaleCommand();

    protected abstract void onScalePercentChange(int zoom);
    protected abstract void onWidthChange(int newWidth);
    protected abstract void onHeightChange(int newHeight);
    
}





