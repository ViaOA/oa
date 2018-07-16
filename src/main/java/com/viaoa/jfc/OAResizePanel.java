package com.viaoa.jfc;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;


/**
 * Panel that "lets" a UI component grow a percentage of the available space.
 * This is useful when using a gridbagLayout, and gc.fill = gc.HORIZONTAL.
 * 
<code> 
        gc.gridwidth = gc.REMAINDER;
        gc.fill = gc.HORIZONTAL;
        panel.add(new OAResizePanel(cbo, 20), gc);
        gc.gridwidth = 1;
        gc.fill = gc.NONE;
</code> 
 
 * @author vvia
 *
 *
 *  Note: it will allow it to grow up to comp.maxSize
 *  
 *  example:  
 
        txt = new OATextField(hubCalcPropertyDef, "toolTip", 14);
        txt.setMaximumColumns(80);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = GridBagConstraints.REMAINDER;
        pan.add(new OAResizePanel(txt, 80), gc);
 
 */
public class OAResizePanel extends JPanel {

    public static boolean DEBUG = false;
    
    public OAResizePanel(JComponent comp) {
        setup(comp, 50, false);
    }
    public OAResizePanel(JComponent comp, int percentage) {
        setup(comp, percentage, false);
    }

    public OAResizePanel(JComponent comp, int percentage, boolean bBoth) {
        setup(comp, percentage, bBoth);
    }
    
    private void setup(JComponent comp, int percentage, boolean bBoth) {
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        setBorder(null);
        
        GridBagConstraints gcx = new GridBagConstraints();
        gcx.insets = new Insets(0,0,0,0);
        gcx.anchor = GridBagConstraints.NORTHWEST;
        
        if (bBoth) gcx.fill = gcx.BOTH;
        else gcx.fill = gcx.HORIZONTAL;

        // 20161129 this will allow for using preferred, and max sizing
        JPanel panComp = new JPanel();
        BoxLayout box = new BoxLayout(panComp, BoxLayout.X_AXIS);
        panComp.setLayout(box);
        panComp.add(comp);
        
        gcx.weightx = gcx.weighty = ((double)percentage)/100.0d;
        gcx.gridwidth = 1;
        add(panComp, gcx);
        
        gcx.gridwidth = GridBagConstraints.REMAINDER;
        gcx.weightx = gcx.weighty = (100.0d-percentage)/100.0d;
        
        JLabel lbl = new JLabel("");
        if (DEBUG) {        
            lbl.setText("<<");        
            lbl.setOpaque(true);        
            lbl.setBackground(Color.lightGray);
            setBorder(new LineBorder(Color.yellow, 3));
        }
        add(lbl, gcx);
        
        if (bBoth) {
            lbl = new JLabel("");
            add(lbl, gcx);
        }
    }
    
    public OAResizePanel(JComponent comp, JComponent comp2, int percentage, boolean bBoth) {
        JPanel panComp = new JPanel();
        BoxLayout box = new BoxLayout(panComp, BoxLayout.X_AXIS);
        panComp.setLayout(box);
        panComp.add(comp);
        if (comp2 != null) panComp.add(comp2);
        setup(panComp, percentage, bBoth);        
    }
    public OAResizePanel(JComponent comp, JComponent comp2, int percentage) {
        this(comp, comp2, percentage, false);
    }
    public OAResizePanel(JComponent comp, JComponent comp2) {
        this(comp, comp2, 50, false);
    }

}
