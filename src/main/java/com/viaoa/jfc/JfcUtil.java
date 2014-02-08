package com.viaoa.jfc;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class JfcUtil {

    /**
     * Create a panel that will resize a percentage of the remaining space. 
     * 
     * ex: JfcUtil.createFilledPanel(txt, 20);
     */
    public static JComponent createFilledPanel(JComponent comp, int percentage) {
        GridBagLayout gb = new GridBagLayout();
        JPanel pan = new JPanel(gb);
        pan.setBorder(null);
        
        GridBagConstraints gcx = new GridBagConstraints();
        gcx.insets = new Insets(0,0,0,0);
        gcx.anchor = GridBagConstraints.WEST;
        gcx.fill = gcx.HORIZONTAL;
        gcx.weightx = gcx.weighty = ((double)percentage)/100.0d;
        gcx.gridwidth = 1;
        pan.add(comp, gcx);
        gcx.gridwidth = GridBagConstraints.REMAINDER;
        gcx.weightx = gcx.weighty =  (100.0d-percentage)/100.0d;
        pan.add(new JLabel(""), gcx);
        return pan;
    }
}
