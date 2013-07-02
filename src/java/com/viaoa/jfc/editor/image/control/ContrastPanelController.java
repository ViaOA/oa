/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.jfc.editor.image.control;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.viaoa.jfc.editor.image.view.ContrastPanel;
import com.viaoa.jfc.editor.image.view.ScalePanel;

/**
 * Used to manage a ContrastPanel.
 */
public abstract class ContrastPanelController {

    private ContrastPanel panContrast;
    private JPopupMenu popupContrast;
    

    public ContrastPanelController() {
    }

    public ContrastPanel getContrastPanel() {
        if (panContrast == null) {
            panContrast = new ContrastPanel() {
                @Override
                public void onOkCommand() {
                    int x = panContrast.getContrastSlider().getValue();
                    popupContrast.setVisible(false);
                    ContrastPanelController.this.onOkCommand(x);
                }
            };
            
            panContrast.getContrastSlider().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int x = panContrast.getContrastSlider().getValue();
                    panContrast.getLabel().setText(""+x);
                    ContrastPanelController.this.onSlideChange(x);
                }
            });
        }
        return panContrast;
    }

    /** 
     * Popup used to display panel.
     */
    protected JPopupMenu getContrastPopup() {
        if (popupContrast != null) return popupContrast;
        
        popupContrast = new JPopupMenu();
        
        popupContrast.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                ContrastPanelController.this.onStart();
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                ContrastPanelController.this.onEnd();
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        
        popupContrast.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        popupContrast.setLayout(new BorderLayout());
        JPanel pan = getContrastPanel();
        Border border = new CompoundBorder(new LineBorder(Color.lightGray), new EmptyBorder(2,2,2,2));
        pan.setBorder(border);
        popupContrast.add(pan);
        
        return popupContrast;
    }
  
    public int getContrast() {
        return getContrastPanel().getContrastSlider().getValue();
    }
    public void setContrast(int x) {
        getContrastPanel().getContrastSlider().setValue(x);
    }
    
    protected abstract void onSlideChange(int x);
    protected abstract void onOkCommand(int x);
    protected abstract void onStart();
    protected abstract void onEnd();
    
}
