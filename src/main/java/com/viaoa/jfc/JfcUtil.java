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
        return createFilledPanel(comp, percentage, false);
    }
    /**
     * Create a wrapper component that will resize a percentage of the available space. 
     * @param comp
     * @param percentage number from 0 to 100
     * @param bBoth if true then Hort & Vert, else only Hort
     * @return
     */
    public static JComponent createFilledPanel(JComponent comp, int percentage, boolean bBoth) {
        GridBagLayout gb = new GridBagLayout();
        JPanel pan = new JPanel(gb);
        pan.setBorder(null);
        
        GridBagConstraints gcx = new GridBagConstraints();
        gcx.insets = new Insets(0,0,0,0);
        gcx.anchor = GridBagConstraints.WEST;
        
        if (bBoth) gcx.fill = gcx.BOTH;
        else gcx.fill = gcx.HORIZONTAL;
        
        gcx.weightx = gcx.weighty = ((double)percentage)/100.0d;
        gcx.gridwidth = 1;
        pan.add(comp, gcx);
        gcx.gridwidth = GridBagConstraints.REMAINDER;
        gcx.weightx = gcx.weighty =  (100.0d-percentage)/100.0d;
        pan.add(new JLabel(""), gcx);
        return pan;
    }

    public static JComponent createFilledPanel(JComponent comp, JComponent comp2, int percentage) {
        return createFilledPanel(comp, comp2, percentage, false);
    }

    /**
     * Create a wrapper component that will be resized a percentage of the available space,
     * and have anoher component on it's right that does not resize. 
     * @param comp
     * @param comp2
     * @param percentage
     * @param bBoth
     * @return
     */
    public static JComponent createFilledPanel(JComponent comp, JComponent comp2, int percentage, boolean bBoth) {
        GridBagLayout gb = new GridBagLayout();
        JPanel pan = new JPanel(gb);
        pan.setBorder(null);
        
        GridBagConstraints gcx = new GridBagConstraints();
        gcx.insets = new Insets(0,0,0,0);
        gcx.anchor = GridBagConstraints.WEST;
        
        if (bBoth) gcx.fill = gcx.BOTH;
        else gcx.fill = gcx.HORIZONTAL;
        gcx.weightx = gcx.weighty = ((double)percentage)/100.0d;
        gcx.gridwidth = 1;
        pan.add(comp, gcx);
        
        gcx.fill = gcx.NONE;
        gcx.weightx = gcx.weighty = 0;
        gcx.insets = new Insets(0,10,0,0);
        pan.add(comp2, gcx);
        
        gcx.gridwidth = GridBagConstraints.REMAINDER;
        gcx.weightx = gcx.weighty =  (100.0d-percentage)/100.0d;
        pan.add(new JLabel(""), gcx);
        return pan;
    }
}



