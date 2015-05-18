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
package com.viaoa.jfc.editor.image.view;

import java.awt.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.viaoa.jfc.editor.image.control.BrightnessPanelController;

/**
 * Panel used to enter information for changing an image scale.
 * @author vincevia
 * @see ScalePanelController controller for this panel.
 */
public abstract class ZoomPanel extends JPanel {
    private NumberTextField txtScalePercent, txtScaleWidth, txtScaleHeight;
    private JLabel lblCurentDescription;
    private JSlider slider;
    private JButton cmdZoomUp;
    private JButton cmdZoomDown;
    private JButton cmdZoom100Percent;
    private JButton cmdZoomFitWidth;
    private JButton cmdZoomFitHeight;
    private JButton cmdZoomFitBoth;

    public ZoomPanel() {
        super(new GridBagLayout());
        setup();
    }
    
    private void setup() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 1, 1, 2);

        
        gc.anchor = gc.EAST;
        add(new JLabel("Current Image:"), gc);
        gc.anchor = gc.WEST;
        
        gc.gridwidth = gc.REMAINDER;
        gc.fill = gc.BOTH;
        add(getCurrentDescriptionLabel(), gc);
        gc.gridwidth = 1;
        gc.fill = gc.NONE;
        
        
//qqqqqq buttons        
        Box box = new Box(BoxLayout.X_AXIS);

        box.add(getZoomDownCommand());
        box.add(getZoomUpCommand());
        box.add(getZoom100PercentCommand());
        box.add(getZoomFitWidthCommand());
        box.add(getZoomFitHeightCommand());
        box.add(getZoomFitBothCommand());

        
        
        gc.gridwidth = gc.REMAINDER;
        add(box, gc);
        gc.gridwidth = 1;
        
        
        
        
        
        
        // Zoom %, w, h
        box = new Box(BoxLayout.X_AXIS);

        box.add(new JLabel("Zoom: "));
        box.add(getPercentTextField());
        box.add(new JLabel("%"));
        box.add(Box.createHorizontalStrut(6));

        box.add(new JLabel("Width:"));
        box.add(getWidthTextField());
        box.add(Box.createHorizontalStrut(5));

        box.add(new JLabel("Height:"));
        box.add(getHeightTextField());

        gc.gridwidth = gc.REMAINDER;
        add(box, gc);
        gc.gridwidth = 1;
        

        gc.gridwidth = gc.REMAINDER;
        gc.fill = gc.HORIZONTAL;
        add(getSlider(), gc);
        gc.gridwidth = 1;
        gc.fill = gc.NONE;
    }

    
    
    
    public JSlider getSlider() {
        if (slider == null) {
            slider = new JSlider(1, 30);
            slider.setSnapToTicks(false);
            slider.setMajorTickSpacing(10);
            slider.setMinorTickSpacing(5);
            slider.setPaintTicks(true);
            //slider.setLabelTable(slider.createStandardLabels(25));
            //slider.setPaintLabels(true);
            slider.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int x = slider.getValue();
                    ZoomPanel.this.onScalePercentChange(x*10);
                }
            });
        }
        return slider;
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
                        ZoomPanel.this.onScalePercentChange(x);
                    }
                }
            });
            txtScalePercent.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    String s = txtScalePercent.getText();
                    if (s != null && s.length() > 0) {
                        int x = Integer.valueOf(s);
                        ZoomPanel.this.onScalePercentChange(x);
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
                    ZoomPanel.this.onWidthChange(w);
                }
            });
            txtScaleWidth.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    int w = Integer.valueOf(txtScaleWidth.getText()).intValue();
                    ZoomPanel.this.onWidthChange(w);
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
                    ZoomPanel.this.onHeightChange(h);
                }
            });
            txtScaleHeight.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    int h = Integer.valueOf(txtScaleHeight.getText()).intValue();
                    ZoomPanel.this.onHeightChange(h);
                }
                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }
        return txtScaleHeight;
    }
    
    
    public JLabel getCurrentDescriptionLabel() {
        if (lblCurentDescription == null) {
            lblCurentDescription = new JLabel(" ");
            lblCurentDescription.setOpaque(false);
        }
        return lblCurentDescription;
    }

    public JButton getZoomUpCommand() {
        if (cmdZoomUp == null) {
            cmdZoomUp = new JButton();
            cmdZoomUp = new JButton();
            cmdZoomUp.setIcon(new ImageIcon(ZoomPanel.class.getResource("image/zoomUp.gif")));
            cmdZoomUp.setToolTipText("Zoom up");
            cmdZoomUp.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ZoomPanel.this.onZoomUpCommand();
                }
            });
            String cmdName = "cmdZoomUp";
            cmdZoomUp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK, false), cmdName);
            cmdZoomUp.getActionMap().put(cmdName, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onZoomUpCommand();
                }
            });
        }
        return cmdZoomUp;
    }
    
    public JButton getZoomDownCommand() {
        if (cmdZoomDown == null) {
            cmdZoomDown = new JButton();
            cmdZoomDown.setIcon(new ImageIcon(ZoomPanel.class.getResource("image/zoomDown.gif")));
            cmdZoomDown.setToolTipText("Zoom down");
            cmdZoomDown.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ZoomPanel.this.onZoomDownCommand();
                }
            });
            String cmdName = "cmdZoomDown";
            cmdZoomDown.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK, false), cmdName);
            cmdZoomDown.getActionMap().put(cmdName, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onZoomDownCommand();
                }
            });
        }
        return cmdZoomDown;
    }

    public JButton getZoom100PercentCommand() {
        if (cmdZoom100Percent == null) {
            cmdZoom100Percent = new JButton("100%");
            cmdZoom100Percent.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ZoomPanel.this.onZoom100PercentCommand();
                }
            });
        }
        return cmdZoom100Percent;
    }

    public JButton getZoomFitWidthCommand() {
        if (cmdZoomFitWidth == null) {
            cmdZoomFitWidth = new JButton();
            cmdZoomFitWidth.setIcon(new ImageIcon(ZoomPanel.class.getResource("image/zoomWidth.gif")));
            cmdZoomFitWidth.setToolTipText("Zoom to fix width");
            cmdZoomFitWidth.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ZoomPanel.this.onZoomFitWidthCommand();
                }
            });
        }
        return cmdZoomFitWidth;
    }
    public JButton getZoomFitHeightCommand() {
        if (cmdZoomFitHeight == null) {
            cmdZoomFitHeight = new JButton();
            cmdZoomFitHeight.setIcon(new ImageIcon(ZoomPanel.class.getResource("image/zoomHeight.gif")));
            cmdZoomFitHeight.setToolTipText("Zoom to fix height");
            cmdZoomFitHeight.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ZoomPanel.this.onZoomFitHeightCommand();
                }
            });
        }
        return cmdZoomFitHeight;
    }

    public JButton getZoomFitBothCommand() {
        if (cmdZoomFitBoth == null) {
            cmdZoomFitBoth = new JButton();
            cmdZoomFitBoth.setIcon(new ImageIcon(ZoomPanel.class.getResource("image/zoomBoth.gif")));
            cmdZoomFitBoth.setToolTipText("Zoom to fix height and height");
            cmdZoomFitBoth.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    ZoomPanel.this.onZoomFitBothCommand();
                }
            });
        }
        return cmdZoomFitBoth;
    }
    
    /**
     * called when one of the NumberTextFields are changed.
     */ 
    protected abstract void onScalePercentChange(int zoom);
    protected abstract void onWidthChange(int w);
    protected abstract void onHeightChange(int h);
    protected abstract void onZoomUpCommand();
    protected abstract void onZoomDownCommand();
    protected abstract void onZoom100PercentCommand();
    protected abstract void onZoomFitWidthCommand();
    protected abstract void onZoomFitHeightCommand();
    protected abstract void onZoomFitBothCommand();
        
}





