package com.viaoa.web;

import com.viaoa.hub.Hub;
import com.viaoa.web.swing.JLabel;
import com.viaoa.web.swing.JTable;

public class OAComboBox extends OACombo {

    public OAComboBox(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath);
    }
    
    public OAComboBox(String id, Hub hub, String propertyPath, int columns) {
        super(id, hub, propertyPath, columns);
    }
    public OAComboBox(String id, Hub hub, String propertyPath, int columns, int rows) {
        super(id, hub, propertyPath, columns, rows);
    }

    public OAComboBox(Hub hub, String cpp, int cols) {
        // super(hub, cpp, i);
        super("xx", hub, cpp, cols, 12);
    }

    public String getToolTipText(int row, int col, String defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setPopupColumns(int i) {
        // TODO Auto-generated method stub
        
    }

    public void setMaximumRowCount(int i) {
        // TODO Auto-generated method stub
        
    }

    public void setIconColorProperty(String ppBackcolor) {
        // TODO Auto-generated method stub
        
    }

    public void setImageProperty(String ppIcon) {
        // TODO Auto-generated method stub
        
    }

    public void setMaxImageHeight(int i) {
        // TODO Auto-generated method stub
        
    }

    public void setMaxImageWidth(int i) {
        // TODO Auto-generated method stub
        
    }

    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,
            boolean wasChanged, boolean wasMouseOver) {
        // TODO Auto-generated method stub
        
    }
    
    
}
