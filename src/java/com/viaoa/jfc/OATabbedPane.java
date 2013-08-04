package com.viaoa.jfc;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

/**
 * This is to allow left pane Tabs to have the tab title left aligned.
 * @author vvia
 */
public class OATabbedPane extends JTabbedPane {
    private Dimension dimMaxSize;
    
    public OATabbedPane(int tabPlacement) {
        super(tabPlacement);
    }
    public OATabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
    }
    
    @Override
    public void setTabPlacement(int tabPlacement) {
        dimMaxSize = null;
        super.setTabPlacement(tabPlacement);
    }
    
    @Override
    public void addTab(String title, Component component) {
        this.addTab(title, null, component, null);
    }

    @Override
    public void addTab(String title, Icon icon, Component component) {
        this.addTab(title, icon, component, null);
    }
    
    @Override
    public void addTab(String title, Icon icon, Component component, String tip) {
        dimMaxSize = null;
        super.addTab(title, icon, component, tip);
    }
    
    @Override
    public void doLayout() {
        if (getTabPlacement() == LEFT && dimMaxSize == null) {
            int tabCount = getTabCount();
            for (int i = 0; i < tabCount; i++) {
                JComponent oomp = (JComponent) getTabComponentAt(i);
                
                if (oomp == null) {
                    oomp = new JLabel(getTitleAt(i), getIconAt(i), JLabel.LEFT);
                    setTabComponentAt(i, oomp);
                }
                
                Dimension d = oomp.getPreferredSize();
                if (dimMaxSize == null) dimMaxSize = d;
                else {
                    dimMaxSize.width = Math.max(dimMaxSize.width, d.width);
                    dimMaxSize.height = Math.max(dimMaxSize.height, d.height);
                }
            }
            for (int i = 0; i < tabCount && dimMaxSize != null; i++) {
                JComponent comp = (JComponent) getTabComponentAt(i);
                if (comp != null) {
                    comp.setPreferredSize(dimMaxSize);
                }
            }
        }
        super.doLayout();
    }

}
