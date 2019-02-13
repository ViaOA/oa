package com.viaoa.jfc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;

import com.viaoa.util.OAArray;


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
    protected JComponent comp1;
    protected Component[] comps;

    public static boolean DEBUG = false;
    
    public OAResizePanel(JComponent comp) {
        comp1 = comp;
        setup(comp, 100, false);
    }
    public OAResizePanel(JComponent comp, int percentage) {
        comp1 = comp;
        setup(comp, percentage, false);
    }

    public OAResizePanel(JComponent comp, int percentage, boolean bBoth) {
        comp1 = comp;
        setup(comp, percentage, bBoth);
    }
    
    public OAResizePanel(JComponent comp, JComponent comp2, int percentage, boolean bBoth) {
        this(null, comp, comp2, percentage, bBoth);
    }

    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent comp2, int percentage) {
        this(icon, comp, comp2, percentage, false);
    }
    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent comp2) {
        this(icon, comp, comp2, 100, false);
    }
    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent comp2, JComponent comp3, int percentage) {
        this(icon, comp, new JComponent[] {comp2, comp3}, percentage, false);
    }

    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent comp2, int percentage, boolean bBoth) {
        this(icon, new JComponent[] {comp, comp2}, percentage, bBoth);
    }

    public OAResizePanel(ImageIcon icon, JComponent comp) {
        this(icon, new JComponent[] {comp}, 100, false);
    }
    
    public OAResizePanel(JComponent comp, JComponent comp1, JComponent comp2, int percentage) {
        this(null, comp, new JComponent[] {comp1, comp2}, percentage, false);
    }
    
    
    public OAResizePanel(ImageIcon icon, JComponent[] comps) {
        this(icon, comps, 100, false);
    }
    public OAResizePanel(ImageIcon icon, JComponent[] comps, int percentage) {
        this(icon, comps, percentage, false);
    }
    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent[] comps) {
        this(icon, (JComponent[]) OAArray.insert(JComponent.class, comps, comp, 0), 100, false);
    }
    
    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent[] comps, int percentage, boolean bBoth) {
        this(icon, (JComponent[]) OAArray.insert(JComponent.class, comps, comp, 0), percentage, bBoth);
    }

    
    public OAResizePanel(JComponent comp, JComponent[] comps) {
        this(null, (JComponent[]) OAArray.insert(JComponent.class, comps, comp, 0), 100, false);
    }
    public OAResizePanel(JComponent comp, JComponent[] comps, int percentage) {
        this(null, (JComponent[]) OAArray.insert(JComponent.class, comps, comp, 0), percentage, false);
    }

    private JLabel lblIcon;
    public JLabel getIconLabel() {
        return lblIcon;
    }
    
    public OAResizePanel(ImageIcon icon, JComponent[] comps, int percentage, boolean bBoth) {
        this.comps = comps;
        comp1 = (comps != null && comps.length > 0) ? comps[0] : null;

        final JPanel panel = new JPanel();

        GridBagLayout gb = new GridBagLayout();
        panel.setLayout(gb);
        panel.setBorder(null);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = gc.NONE;
        gc.weightx = gc.weighty = 0.0; 
        
        if (icon != null) {
            lblIcon = new JLabel(icon);
            lblIcon.setOpaque(true);
            gc.insets = new Insets(0, 0, 0, 5);
            panel.add(lblIcon, gc);
            lblIcon.setLabelFor(comp1);
        }
        gc.insets = new Insets(0, 0, 0, 0);
        if (bBoth) {
            gc.fill = gc.BOTH;
            gc.weightx = gc.weighty = 1.0; 
        }
        else {
            gc.fill = gc.HORIZONTAL;
            gc.weightx = 1.0; 
        }

        // this will allow for using preferred, and max sizing
        JPanel panComp = new JPanel();
        BoxLayout box = new BoxLayout(panComp, BoxLayout.X_AXIS);
        panComp.setLayout(box);
        panComp.add(comp1);
        
        for (int i=1; comps != null && i<comps.length; i++) {
            if (comps[i] == null) continue;
            panComp.add(Box.createHorizontalStrut(2));
            panComp.add(comps[i]);
        }
        
        panel.add(panComp, gc);
        
        setup(panel, percentage, bBoth);        
    }

    
/*was    
    public OAResizePanel(ImageIcon icon, JComponent comp, JComponent comp2, int percentage, boolean bBoth) {
        JPanel panComp = new JPanel();
        BoxLayout box = new BoxLayout(panComp, BoxLayout.X_AXIS);
        panComp.setLayout(box);
        if (icon != null) {
            JLabel lbl = new JLabel(icon);
            lbl.setOpaque(true);
            panComp.add(lbl);
            panComp.add(Box.createHorizontalStrut(4));
            lbl.setLabelFor(comp);
        }
        panComp.add(comp);
        if (comp2 != null) panComp.add(comp2);
        setup(panComp, percentage, bBoth);        
    }
*/
    public OAResizePanel(JComponent comp, JComponent comp2, int percentage) {
        this(comp, comp2, percentage, false);
    }
    public OAResizePanel(JComponent comp, JComponent comp2) {
        this(comp, comp2, 100, false);
    }

    
    private void setup(JComponent comp, int percentage, boolean bBoth) {
        if (comp instanceof JScrollPane) {
            JScrollPane jsp = (JScrollPane) comp;
            Component compx = ((JScrollPane) comp).getViewport().getView();
            if (compx instanceof OAList) {
                final OAList list = (OAList) compx;
                
                JPanel panx = new JPanel(new BorderLayout()) {
                    @Override
                    public Dimension getPreferredSize() {
                        Dimension d = super.getPreferredSize();
                        Dimension dx =  list.getPreferredSize();
                        d.width = dx.width;
                        return d;
                    }
                    @Override
                    public Dimension getMaximumSize() {
                        Dimension d = super.getMaximumSize();
                        Dimension dx =  list.getMaximumSize();
                        d.width = dx.width;
                        return d;
                    }
                    @Override
                    public Dimension getMinimumSize() {
                        Dimension d = super.getMinimumSize();
                        Dimension dx =  list.getMinimumSize();
                        d.width = dx.width;
                        return d;
                    }
                };
                if (DEBUG) panx.setBorder(new LineBorder(Color.GREEN, 4));
                panx.add(comp, BorderLayout.CENTER);
                comp = panx;
            }
        }
        
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        setBorder(null);
        
        GridBagConstraints gcx = new GridBagConstraints();
        gcx.insets = new Insets(0,0,0,0);
        gcx.anchor = GridBagConstraints.WEST;  // 20181006
        //was: gcx.anchor = GridBagConstraints.NORTHWEST;
        
        if (bBoth) gcx.fill = gcx.BOTH;
        else gcx.fill = gcx.HORIZONTAL;

        // boxlayout will allow for using preferred, and max sizing
        JPanel panComp = new JPanel();
        BoxLayout box = new BoxLayout(panComp, BoxLayout.X_AXIS);
        panComp.setLayout(box);
        panComp.add(comp);
        
        gcx.weightx = ((double)percentage)/100.0d;
        if (bBoth) gcx.weighty = gcx.weightx;
        gcx.gridwidth = 1;
        add(panComp, gcx);
        
        gcx.gridwidth = GridBagConstraints.REMAINDER;
        gcx.weightx = (100.0d-percentage)/100.0d;
        if (bBoth) gcx.weighty = gcx.weightx;
        
        JLabel lbl = new JLabel("");
        if (DEBUG) {
            lbl.setOpaque(true);        
            lbl.setBackground(Color.lightGray);
            setBorder(new LineBorder(Color.yellow, 2));
            if (comp1 instanceof OAJfcComponent) {
                int c1 = ((OAJfcComponent) comp1).getController().getColumns();
                int c2 = ((OAJfcComponent) comp1).getController().getMaximumColumns();
                if (c1 < c2) lbl.setText("+-");        
                else {
                    lbl.setText("::");
                    lbl.setBackground(Color.RED);
                }
                lbl.setToolTipText("cols="+c1+", max="+c2);
            }
            else lbl.setText("<");        
        }
        add(lbl, gcx);
        
        if (bBoth) {
            lbl = new JLabel("");
            add(lbl, gcx);
        }
    }
    

//qqqqqqqqqqqqqq    
//qqqqqqqqqqq revisit this, used by OATemplate apps    
    
    /**
     * Used when Window.pack is called so that preferred size is used.
     * @see OAJfcUtil#pack(Window)
     */
    public static void setPacking(Window window) {
        windowPack = window;
    }
    private static Window windowPack;

    
    
    // JScrollPane will only go down in size to preferred size.
    //   this will allow it to be 3/4 between preferred and minimum
    //@Override
    public Dimension getPreferredSizeXXX() {
        Dimension d = super.getPreferredSize();
        if (windowPack == null) {
            return d;
        }
        boolean bFoundWindow = false;;
        boolean bHasScrollPane = false; 
        
        Component comp = this.getParent();
        for ( ; comp != null; comp = comp.getParent()) {
            if (comp instanceof JScrollPane) {
                bHasScrollPane = true;
            }
            if (comp == windowPack) {
                bFoundWindow = true;
            }                
        }
        
        if (bFoundWindow && bHasScrollPane) {
            Dimension dx = super.getMinimumSize();
            int x = d.width - dx.width;
            if (x > 0) {
                x = (int) (x * .60);
                d.width = dx.width + x;
            }
        }
        return d;
    }
    
//qqqqqqqqqqqqqqqqq
    // 20190102 this fixed issue with "find:" autocomplete on toolbar taking up all available space
    @Override
    public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        d.width = super.getPreferredSize().width;
        return d;
    }

}
