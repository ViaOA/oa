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
package com.viaoa.jfc.toolbar;

import javax.swing.*; 
import javax.swing.event.PopupMenuListener; 
import javax.swing.event.PopupMenuEvent; 
import java.awt.event.ActionEvent; 
import java.awt.event.ActionListener; 
import java.awt.event.ComponentAdapter; 
import java.awt.event.ComponentEvent; 

import java.awt.*; 
 
// 20101031 not yet used, for demo see: http://jroller.com/santhosh/category/Swing?page=1

/** 
 * MySwing: Advanced Swing Utilites 
 * Copyright (C) 2005  Santhosh Kumar T 
 * <p/> 
 * This library is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version. 
 * <p/> 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 * Lesser General Public License for more details. 
 */ 
 
/** 
 * to use this feature replace: 
 *   frame.getContentPane().add(toolbar, BorderLayout.NORTH); 
 * with 
 *   frame.getContentPane().add(MoreButton.wrapToolBar(toolBar), BorderLayout.NORTH); 
 * 
 * @author Santhosh Kumar T 
 * @email  santhosh@in.fiorano.com 
 */ 

public class ToolBarUtil extends JToggleButton implements ActionListener{ 
    JToolBar toolbar; 
 
    private ToolBarUtil(final JToolBar toolbar){ 
        super("More>"); 
        this.toolbar = toolbar; 
        addActionListener(this); 
        setFocusPainted(false); 
 
        // hide & seek 
        toolbar.addComponentListener(new ComponentAdapter(){ 
            public void componentResized(ComponentEvent e){ 
                setVisible(!isVisible(toolbar.getComponent(toolbar.getComponentCount()-1), null)); 
            } 
        }); 
    } 
 
    // check visibility 
    // partially visible is treated as not visible 
    private boolean isVisible(Component comp, Rectangle rect){ 
        if(rect==null) 
            rect = toolbar.getVisibleRect(); 
        return comp.getLocation().x+comp.getWidth()<=rect.getWidth(); 
    } 
 
    public void actionPerformed(ActionEvent e){ 
        Component[] comp = toolbar.getComponents(); 
        Rectangle visibleRect = toolbar.getVisibleRect(); 
        for(int i = 0; i<comp.length; i++){ 
            if (!isVisible(comp[i], visibleRect)){ 
                JPopupMenu popup = new JPopupMenu(); 
                for( ; i<comp.length; i++){ 
                    if (comp[i] instanceof AbstractButton) { 
                        AbstractButton button = (AbstractButton)comp[i]; 
                        if(button.getAction() != null) popup.add(button.getAction()); 
                    } 
                    else if (comp[i] instanceof JSeparator) { 
                        popup.addSeparator(); 
                    }
                } 
 
                //on popup close make more-button unselected 
                popup.addPopupMenuListener(new PopupMenuListener(){ 
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e){ 
                        setSelected(false); 
                    } 
                    public void popupMenuCanceled(PopupMenuEvent e){} 
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e){} 
                }); 
                popup.show(this, 0, getHeight()); 
            } 
        } 
    } 
 
    public static Component wrapToolBar(JToolBar toolbar){ 
        JToolBar moreToolbar = new JToolBar(); 
        moreToolbar.setRollover(true); 
        moreToolbar.setFloatable(false); 
        moreToolbar.add(new ToolBarUtil(toolbar)); 
 
        JPanel panel = new JPanel(new BorderLayout()); 
        panel.add(toolbar, BorderLayout.CENTER); 
        panel.add(moreToolbar, BorderLayout.EAST); 
 
        return panel; 
    } 
} 

 
