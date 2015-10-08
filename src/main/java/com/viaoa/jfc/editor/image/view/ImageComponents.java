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
package com.viaoa.jfc.editor.image.view;

import java.awt.Image;
import java.awt.Insets;

import javax.swing.*;

import com.viaoa.jfc.editor.image.OAImagePanel;
import com.viaoa.jfc.image.MultiImageIcon;

/**
 * Components used for ImagePanel, controlled by ImagePanelController.
 * @author vincevia
 */
public class ImageComponents {

    private JButton cmdCrop, cmdRotateCW, cmdRotateCCW, cmdBrighter, cmdDarker, cmdSharpen, cmdBlur;
    private JButton cmdScale, cmdUndo, cmdMoreContrast, cmdLessContrast, cmdBrightness, cmdContrast, cmdZoom;
    private JButton cmdOpen, cmdSave, cmdSaveAs, cmdDelete;
    private JButton cmdTwain, cmdSignature;
    
    public JButton getCropButton() {
        if (cmdCrop == null) {
            cmdCrop = new JButton();
            cmdCrop.setIcon(new ImageIcon(ImageComponents.class.getResource("image/crop.gif")));
            cmdCrop.setToolTipText("crop the image");
            cmdCrop.setRequestFocusEnabled(false);
            cmdCrop.setFocusPainted(false);
//            cmdCrop.setBorderPainted(false);
//            cmdCrop.setMargin(new Insets(1,1,1,1));
            
        }
        return cmdCrop;
    }
    
    public JButton getRotateCWButton() {
        if (cmdRotateCW == null) {
            cmdRotateCW = new JButton();
            cmdRotateCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCW.gif")));
            cmdRotateCW.setToolTipText("rotate image clockwise");
            cmdRotateCW.setRequestFocusEnabled(false);
            cmdRotateCW.setFocusPainted(false);
//            cmdRotateCW.setBorderPainted(false);
//            cmdRotateCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdRotateCW;
    }

    public JButton getRotateCCWButton() {
        if (cmdRotateCCW == null) {
            cmdRotateCCW = new JButton();
            cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdRotateCCW.setToolTipText("rotate image counter clockwise");
            cmdRotateCCW.setRequestFocusEnabled(false);
            cmdRotateCCW.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdRotateCCW;
    }

    public JButton getScaleButton() {
        if (cmdScale == null) {
            Image img = new ImageIcon(ImageComponents.class.getResource("image/scale.gif")).getImage();
            Image img2 = new ImageIcon(ImageComponents.class.getResource("image/down.gif")).getImage();
            MultiImageIcon mii = new MultiImageIcon(new Image[] {img, img2}, 2);
            cmdScale = new JButton();
            cmdScale.setToolTipText("scale the image up/down");
            cmdScale.setIcon(mii);
            cmdScale.setRequestFocusEnabled(false);
            cmdScale.setFocusPainted(false);
            cmdScale.setToolTipText("Scale");
        }
        return cmdScale;
    }

    public JButton getZoomButton() {
        if (cmdZoom == null) {
            Image img = new ImageIcon(ImageComponents.class.getResource("image/zoom.gif")).getImage();
            Image img2 = new ImageIcon(ImageComponents.class.getResource("image/down.gif")).getImage();
            MultiImageIcon mii = new MultiImageIcon(new Image[] {img, img2}, 2);
            cmdZoom = new JButton();
            cmdZoom.setToolTipText("Zoom in/out");
            cmdZoom.setIcon(mii);
            cmdZoom.setRequestFocusEnabled(false);
            cmdZoom.setFocusPainted(false);
            cmdZoom.setToolTipText("Zoom");
        }
        return cmdZoom;
    }
    
    public JButton getBrightnessButton() {
        if (cmdBrightness == null) {
            Image img = new ImageIcon(ImageComponents.class.getResource("image/brightness.gif")).getImage();
            Image img2 = new ImageIcon(ImageComponents.class.getResource("image/down.gif")).getImage();
            MultiImageIcon mii = new MultiImageIcon(new Image[] {img, img2}, 2);
            cmdBrightness = new JButton();
            cmdBrightness.setToolTipText("Adjust Brightness");
            cmdBrightness.setIcon(mii);
            cmdBrightness.setRequestFocusEnabled(false);
            cmdBrightness.setFocusPainted(false);
        }
        return cmdBrightness;
    }
    public JButton getContrastButton() {
        if (cmdContrast == null) {
            Image img = new ImageIcon(ImageComponents.class.getResource("image/contrast.gif")).getImage();
            Image img2 = new ImageIcon(ImageComponents.class.getResource("image/down.gif")).getImage();
            MultiImageIcon mii = new MultiImageIcon(new Image[] {img, img2}, 2);
            cmdContrast = new JButton();
            cmdContrast.setToolTipText("Adjust Contrast");
            cmdContrast.setIcon(mii);
            cmdContrast.setRequestFocusEnabled(false);
            cmdContrast.setFocusPainted(false);
        }
        return cmdContrast;
    }
    
    public JButton getBrighterButton() {
        if (cmdBrighter == null) {
            cmdBrighter = new JButton("Brighter");
            // cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdBrighter.setToolTipText("make image brighter");
            cmdBrighter.setRequestFocusEnabled(false);
            cmdBrighter.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdBrighter;
    }
    public JButton getDarkerButton() {
        if (cmdDarker == null) {
            cmdDarker = new JButton("Darker");
            // cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdDarker.setToolTipText("make image darker");
            cmdDarker.setRequestFocusEnabled(false);
            cmdDarker.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdDarker;
    }

    public JButton getMoreContrastButton() {
        if (cmdMoreContrast == null) {
            cmdMoreContrast = new JButton("More Contrast");
            // cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdMoreContrast.setRequestFocusEnabled(false);
            cmdMoreContrast.setToolTipText("more contrast");
            cmdMoreContrast.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdMoreContrast;
    }
    public JButton getLessContrastButton() {
        if (cmdLessContrast == null) {
            cmdLessContrast = new JButton("Less Contrast");
            // cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdLessContrast.setToolTipText("less contrast");
            cmdLessContrast.setRequestFocusEnabled(false);
            cmdLessContrast.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdLessContrast;
    }
    
    public JButton getSharpenButton() {
        if (cmdSharpen == null) {
            cmdSharpen = new JButton("Sharpen");
            cmdSharpen.setToolTipText("sharpen the image");
            // cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdSharpen.setRequestFocusEnabled(false);
            cmdSharpen.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdSharpen;
    }
    
    public JButton getBlurButton() {
        if (cmdBlur == null) {
            cmdBlur = new JButton("Blur");
            // cmdRotateCCW.setIcon(new ImageIcon(ImageComponents.class.getResource("image/rotateCCW.gif")));
            cmdBlur.setToolTipText("blur the image");
            cmdBlur.setRequestFocusEnabled(false);
            cmdBlur.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdBlur;
    }
    
    public JButton getUndoButton() {
        if (cmdUndo == null) {
            cmdUndo = new JButton();
            cmdUndo.setIcon(new ImageIcon(ImageComponents.class.getResource("image/undo.png")));
            cmdUndo.setRequestFocusEnabled(false);
            cmdUndo.setToolTipText("undo the last change");
            cmdUndo.setFocusPainted(false);
//            cmdRotateCCW.setBorderPainted(false);
//            cmdRotateCCW.setMargin(new Insets(1,1,1,1));
        }
        return cmdUndo;
    }

    public JButton getOpenButton() {
        if (cmdOpen == null) {
            cmdOpen = new JButton();
            cmdOpen.setIcon(new ImageIcon(ImageComponents.class.getResource("image/open.gif")));
            cmdOpen.setToolTipText("open an image from file system.");
            cmdOpen.setRequestFocusEnabled(false);
            cmdOpen.setFocusPainted(false);
        }
        return cmdOpen;
    }

    public JButton getDeleteButton() {
        if (cmdDelete == null) {
            cmdDelete = new JButton();
            cmdDelete.setIcon(new ImageIcon(ImageComponents.class.getResource("image/delete.gif")));
            cmdDelete.setRequestFocusEnabled(false);
            cmdDelete.setToolTipText("delete this image.");
            cmdDelete.setFocusPainted(false);
        }
        return cmdDelete;
    }

    public JButton getSaveButton() {
        if (cmdSave == null) {
            cmdSave = new JButton();
            cmdSave.setIcon(new ImageIcon(ImageComponents.class.getResource("image/save.gif")));
            cmdSave.setToolTipText("save the image.");
            cmdSave.setRequestFocusEnabled(false);
            cmdSave.setFocusPainted(false);
        }
        return cmdSave;
    }
    
    public JButton getSaveAsButton() {
        if (cmdSaveAs == null) {
            cmdSaveAs = new JButton();
            cmdSaveAs.setIcon(new ImageIcon(ImageComponents.class.getResource("image/saveAs.gif")));
            cmdSaveAs.setToolTipText("save this image to another file.");
            cmdSaveAs.setRequestFocusEnabled(false);
            cmdSaveAs.setFocusPainted(false);
        }
        return cmdSaveAs;
    }

    public JButton getTwainButton() {
        if (cmdTwain == null) {
            cmdTwain = new JButton();
            cmdTwain.setIcon(new ImageIcon(ImageComponents.class.getResource("image/twain.gif")));
            cmdTwain.setToolTipText("get an image from a camera or scanner.");
            cmdTwain.setRequestFocusEnabled(false);
            cmdTwain.setFocusPainted(false);
        }
        return cmdTwain;
    }

    public JButton getSignatureButton() {
        if (cmdSignature == null) {
            cmdSignature = new JButton();
            cmdSignature.setIcon(new ImageIcon(ImageComponents.class.getResource("image/signature.gif")));
            cmdSignature.setToolTipText("get an image from signature pad");
            cmdSignature.setRequestFocusEnabled(false);
            cmdSignature.setFocusPainted(false);
        }
        return cmdSignature;
    }
    
}


/*

slider = new JSlider(0, 200) {
@Override
public Dimension getMaximumSize() {
  return super.getPreferredSize();
}
};
slider.setValue(100);
slider.setMinorTickSpacing(10);
slider.setMajorTickSpacing(50);
slider.setPaintLabels(true);
slider.setPaintTicks(true);
slider.setSnapToTicks(true);
tool.add(slider);

*/  

