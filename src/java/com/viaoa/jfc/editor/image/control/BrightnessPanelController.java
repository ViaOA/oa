package com.viaoa.jfc.editor.image.control;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.viaoa.jfc.editor.image.view.BrightnessPanel;
import com.viaoa.jfc.editor.image.view.ScalePanel;

/**
 * Used to manage a BrightnessPanel.
 */
public abstract class BrightnessPanelController {

    private BrightnessPanel panBrightness;
    private JPopupMenu popupBrightness;
    

    public BrightnessPanelController() {
    }

    public BrightnessPanel getBrightnessPanel() {
        if (panBrightness == null) {
            panBrightness = new BrightnessPanel() {
                @Override
                public void onOkCommand() {
                    int x = panBrightness.getBrightnessSlider().getValue();
                    popupBrightness.setVisible(false);
                    BrightnessPanelController.this.onOkCommand(x);
                }
            };
            
            panBrightness.getBrightnessSlider().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    int x = panBrightness.getBrightnessSlider().getValue();
                    panBrightness.getLabel().setText(""+x);
                    BrightnessPanelController.this.onSlideChange(x);
                }
            });
        }
        return panBrightness;
    }

    /** 
     * Popup used to display panel.
     */
    protected JPopupMenu getBrightnessPopup() {
        if (popupBrightness != null) return popupBrightness;
        
        popupBrightness = new JPopupMenu();
        
        popupBrightness.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                BrightnessPanelController.this.onStart();
            }
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                BrightnessPanelController.this.onEnd();
            }
            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        
        popupBrightness.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        popupBrightness.setLayout(new BorderLayout());
        JPanel pan = getBrightnessPanel();
        Border border = new CompoundBorder(new LineBorder(Color.lightGray), new EmptyBorder(2,2,2,2));
        pan.setBorder(border);
        popupBrightness.add(pan);
        
        return popupBrightness;
    }
  
    public int getBrightness() {
        return getBrightnessPanel().getBrightnessSlider().getValue();
    }
    public void setBrightness(int x) {
        getBrightnessPanel().getBrightnessSlider().setValue(x);
    }
    
    protected abstract void onSlideChange(int x);
    protected abstract void onOkCommand(int x);
    protected abstract void onStart();
    protected abstract void onEnd();
    
}
