package com.viaoa.web;

import com.viaoa.hub.Hub;
import com.viaoa.object.OAObject;
import com.viaoa.web.swing.ComponentInterface;
import com.viaoa.web.swing.JLabel;
import com.viaoa.web.swing.JTable;

public class OALabel extends OAHtmlElement implements ComponentInterface {

    public OALabel(String id, Hub hub, String propertyPath, int width) {
        super(id, hub, propertyPath, width);
    }
    public OALabel(String id, Hub hub, String propertyPath) {
        super(id, hub, propertyPath);
    }
    public OALabel(String id, Hub hub, String propertyPath, int width, int minWidth, int maxRows) {
        super(id, hub, propertyPath, width, minWidth, maxRows);
    }

    public OALabel(Hub hub, String cpp, int i) {
        // TODO Auto-generated constructor stub
    }
    @Override
    public void setMaxWidth(String val) {
        super.setMaxWidth(val);
        setOverflow("hidden");  // oahtmlelement will then add al.add("'text-overflow':'ellipsis'");
    }

    @Override
    public String getEditorHtml(OAObject obj) {
        return getRenderHtml(obj);  // no real editor
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
    public String getToolTipText(int row, int col, String defaultValue) {
        // TODO Auto-generated method stub
        return null;
    }
    public void customizeTableRenderer(JLabel lbl, JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column,
            boolean wasChanged, boolean wasMouseOver) {
        // TODO Auto-generated method stub
        
    }
    
}
