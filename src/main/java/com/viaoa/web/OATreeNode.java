package com.viaoa.web;

import java.awt.event.MouseEvent;

import javax.swing.Icon;

import com.viaoa.hub.Hub;
import com.viaoa.web.swing.*;


/**
 * @author vvia
 *
 */
public class OATreeNode {

    public OATreeNode(String path) {
    }
    /** @param hub is either the Hub or UpdateHub, depending if this is a top level node.*/
    public OATreeNode(String path, Hub hub) {
    }
    public OATreeNode(String path, Hub hub, Hub updateHub) {
    }
    
    public OATreeNode(String path, OATableComponent editor) {
    }
    public OATreeNode(OATableComponent editor) {
    }
    
    public OATreeNode(OATreeNode originalNode) {
    }
    
    public OATree getTree() {
        return null;
    }

    public void objectSelected(Object obj) {
    }

    public ComponentInterface getTreeCellRendererComponent(ComponentInterface comp,
        JTree tree, Object value, boolean selected,
        boolean expanded, boolean leaf, int row, boolean hasFocus)
    {
        return null;
    }
    public void setSelectedHub(Hub multiSelectHub) {
        // TODO Auto-generated method stub
        
    }
    public void onDoubleClick(Object obj, MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
    public JPopupMenu getPopupMenu() {
        // TODO Auto-generated method stub
        return null;
    }
    public void setPopupMenu(JPopupMenu pm) {
        
    }
    public void setIcon(Icon icon) {
        // TODO Auto-generated method stub
    }
    public void setForegroundColorProperty(String ppForecolor) {
        // TODO Auto-generated method stub
        
    }
    public void setBackgroundColorProperty(String ppBackcolor) {
        // TODO Auto-generated method stub
        
    }
    public void setImageProperty(String ppIcon) {
        // TODO Auto-generated method stub
        
    }
    public void setAllowDnD(boolean b) {
        // TODO Auto-generated method stub
        
    }
    public void setAllowDrop(boolean b) {
        // TODO Auto-generated method stub
        
    }
    public void setAllowDrag(boolean b) {
        // TODO Auto-generated method stub
        
    }
    public void setFontProperty(String ppFont) {
        // TODO Auto-generated method stub
        
    }
    public void setSuffix(Object object) {
        // TODO Auto-generated method stub
        
    }
    public void setMaxImageHeight(int i) {
        // TODO Auto-generated method stub
        
    }
    public void setMaxImageWidth(int i) {
        // TODO Auto-generated method stub
        
    }
    public void add(OATreeNode node, String pEmployers) {
        // TODO Auto-generated method stub
        
    }
    public Object getHub() {
        // TODO Auto-generated method stub
        return null;
    }
    public String getPropertyPath() {
        // TODO Auto-generated method stub
        return null;
    }
    public void add(OATreeNode node) {
        // TODO Auto-generated method stub
        
    }
    public Component getTreeCellRendererComponent(Component comp, JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
}
