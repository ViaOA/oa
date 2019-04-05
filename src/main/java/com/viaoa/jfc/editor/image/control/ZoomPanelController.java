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
package com.viaoa.jfc.editor.image.control;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.viaoa.jfc.editor.image.view.ScalePanel;
import com.viaoa.jfc.editor.image.view.ZoomPanel;

/**
 * Used to manage a ScalePanel.
 * @author vincevia
 *
 */
public abstract class ZoomPanelController {

    private ZoomPanel panZoom;
    private JPopupMenu popupZoom;
    private boolean updatingComponents;
    

    public ZoomPanelController() {
    }

    public ZoomPanel getZoomPanel() {
        if (panZoom == null) {
            panZoom = new ZoomPanel() {
                @Override
                protected void onHeightChange(int newHeight) {
                    if (updatingComponents) return;
                    if (newHeight > 0) {
                        double h = getImageHeight();
                        onPerformZoom( newHeight/h );
                        updateComponents();
                    }
                }
                @Override
                protected void onWidthChange(int newWidth) {
                    if (updatingComponents) return;
                    if (newWidth > 0) {
                        double w = getImageWidth();
                        onPerformZoom( newWidth/w );
                        updateComponents();
                    }
                }
                @Override
                protected void onScalePercentChange(int scale) {
                    if (updatingComponents) return;
                    if (scale > 0) {
                        onPerformZoom(scale/100.0);
                        updateComponents();
                    }
                }
                @Override
                protected void onZoomDownCommand() {
                    double d = getZoomScale();
                    if (d <= .01) {
                        d = .01;
                    }
                    else if (d <= .1) {
                        d *= 100;
                        d -= 1;
                        d /= 100;
                    }
                    else if (d <= 1) {
                        d *= 100;
                        if (d%10==0) {
                            d -= 10;
                        }
                        int x = (int) (d / 10);
                        d = x * 10;
                        d /= 100;
                    }
                    else if (d <= 2) {
                        d *= 100;
                        if (d%25==0) {
                            d -= 25;
                        }
                        int x = (int) (d / 25);
                        d = x * 25;
                        d /= 100;
                    }
                    else {
                        d *= 100;
                        if (d%50==0) {
                            d -= 50;
                        }
                        int x = (int) (d / 50);
                        d = x * 50;
                        d /= 100;
                    }
                    onPerformZoom(Math.max(.01, d));
                    updateComponents();
                }
                @Override
                protected void onZoomUpCommand() {
                    double d = getZoomScale();
                    if (d < .01) {
                        d = .01;
                    }
                    else if (d < .1) {
                        d *= 100;
                        d += 1;
                        d /= 100;
                    }
                    else if (d < 1) {
                        d *= 100;
                        d += 10;
                        int x = (int) (d / 10);
                        d = x * 10;
                        d /= 100;
                    }
                    else if (d < 2) {
                        d *= 100;
                        d += 25;
                        int x = (int) (d / 25);
                        d = x * 25;
                        d /= 100;
                    }
                    else {
                        d *= 100;
                        d += 50;
                        int x = (int) (d / 50);
                        d = x * 50;
                        d /= 100;
                    }

                    onPerformZoom(d);
                    updateComponents();
                }
                @Override
                protected void onZoom100PercentCommand() {
                    onPerformZoom(1.0);
                    updateComponents();
                }
                @Override
                protected void onZoomFitWidthCommand() {
                    double w = getImageWidth();
                    double w2 = getContainerSize().width;
                    onPerformZoom( w2/w );
                    updateComponents();
                }
                @Override
                protected void onZoomFitHeightCommand() {
                    double h = getImageHeight();
                    double h2 = getContainerSize().height;
                    onPerformZoom( h2/h );
                    updateComponents();
                }
                @Override
                protected void onZoomFitBothCommand() {
                    setZoomFitBoth();
                }
            };
            
            
        }
        return panZoom;
    }

    public void setZoomFitBoth() {
        double w = getImageWidth();
        double h = getImageHeight();
        Dimension d = getContainerSize();
        onPerformZoom( Math.min(d.width/w, d.height/h) );
        updateComponents();
    }
    
    
    public void setZoomScale(double x) {
        updateComponents();
    }
    
    /** 
     * Popup used to display panel.
     * @return
     */
    protected JPopupMenu getZoomPopup() {
        if (popupZoom != null) return popupZoom;
        
        popupZoom = new JPopupMenu();
        popupZoom.setBorder(BorderFactory.createLineBorder(Color.lightGray));
        popupZoom.setLayout(new BorderLayout());
        JPanel pan = getZoomPanel();
        Border border = new CompoundBorder(new LineBorder(Color.lightGray), new EmptyBorder(2,2,2,2));
        pan.setBorder(border);
        popupZoom.add(pan);
        
        return popupZoom;
    }
  
    
    protected void updateComponents() {
        if (updatingComponents) return;
        updatingComponents = true;
        
        double scale = getZoomScale();
        int iScale = (int) (scale * 100); 
        
        int w = getImageWidth();
        int h = getImageHeight();

        String s;
        if (w == 0 || h == 0) s = "no image";
        else {
            s = "width: "+w + ", height: " + h;
        }
        getZoomPanel().getCurrentDescriptionLabel().setText(s);
        
        
        w = (int) (w * scale);
        h = (int) (h * scale);
        
        getZoomPanel().getPercentTextField().setText(iScale+ "");
        getZoomPanel().getWidthTextField().setText(w + "");
        getZoomPanel().getHeightTextField().setText(h + "");
        
        getZoomPanel().getSlider().setValue(iScale/10);

        updatingComponents = false;
    }
    
    
    
    
    /** re-zoom image */
    protected abstract void onPerformZoom(double scale);
    
    /** Used to get width of image, which must be supplied by controller. */
    protected abstract int getImageWidth();
    /** Used to get height of image, which must be supplied by controller. */
    protected abstract int getImageHeight();
    
    protected abstract double getZoomScale();
    
    // used for determining zoom to fit values
    protected abstract Dimension getContainerSize(); 
}
