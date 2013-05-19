package com.viaoa.jfc.editor.image.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.ChangeListener;

public class BrightnessPanel extends JPanel {
    private JButton cmdOk;
    private JSlider slider;
    private JLabel lbl;
    
    public BrightnessPanel() {
        super(new FlowLayout());
       
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2, 1, 1, 2);
    
        
        slider = new JSlider(-100, 100);
        slider.setValue(0);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(10);
        //slider.setSnapToTicks(true);  // wont allow increments
        slider.setPaintTicks(true);
        slider.setExtent(10);
        slider.setLabelTable(slider.createStandardLabels(75));
        slider.setPaintLabels(true);
        
        
        add(slider);
        add(getLabel());
        
        add(getOkCommand());
    }
    
    public JSlider getBrightnessSlider() {
        return slider;
    }
    
    public JButton getOkCommand() {
        if (cmdOk == null) {
            cmdOk = new JButton("Ok");
            cmdOk.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    BrightnessPanel.this.onOkCommand();
                }
            });
            String cmdName = "onClick";
            cmdOk.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0,false), cmdName);
            cmdOk.getActionMap().put(cmdName, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    onOkCommand();
                }
            });
        }
        return cmdOk;
    }
    
    public JLabel getLabel() {
        if (lbl == null) {
            lbl = new JLabel("     ") {
                Dimension d;
                @Override
                public Dimension getPreferredSize() {
                    if (d == null) {
                        d = super.getPreferredSize();
                        d.width += 10;
                    }
                    return d;
                }
            };
            lbl.setOpaque(false);
        }
        return lbl;
    }
    
    
    protected void onOkCommand() {
    }
}





