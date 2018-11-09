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


import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;

import com.viaoa.hub.Hub;
import com.viaoa.hub.HubChangeListener;
import com.viaoa.jfc.OAButton;
import com.viaoa.jfc.OAJfcUtil;
import com.viaoa.jfc.OAMultiButtonSplitButton;
import com.viaoa.jfc.control.OAJfcController;
import com.viaoa.jfc.editor.image.view.*;
import com.viaoa.jfc.editor.image.OAImageEditor;
import com.viaoa.jfc.editor.image.OAImagePanel;
import com.viaoa.jfc.image.*;
import com.viaoa.object.OAObject;
import com.viaoa.util.OAArray;
import com.viaoa.util.OAString;

/**
 * Controller for OAImagePanel and image components.
 * Adds toolbar commands, etc. 
 * @author vincevia
 */
public class OAImagePanelController extends OAJfcController {
    private static Logger LOG = Logger.getLogger(OAImagePanelController.class.getName());
    private ImageComponents comps;
    private ScalePanelController controlScalePanel;
    private ZoomPanelController controlZoomPanel;
    private BrightnessPanelController controlBrightnessPanel;
    private ContrastPanelController controlContrastPanel;
    
    private OAImageEditor editor;
    private JToolBar toolBar;
    private OAImagePanel panImage;
    private static ImageFileChooserController controlImageFileChooser;

    private File file;
    private JButton[] cmdOpens = new JButton[0];  // open buttons to add to open - creating a splitbutton dropdown

    public OAImagePanelController(Hub hub, OAImageEditor editor, String property) {
        super(hub, null, property, editor, HubChangeListener.Type.AoNotNull, false, true);
        editor = this.editor;
        comps = new ImageComponents();
        setup();
        enableVisibleListener(true);
        updateCommands();
    }

    private Object lastActiveObject;
    private boolean bCallingUpdate;
    
    @Override
    public void update(JComponent comp, Object object, boolean bIncudeToolTip) {
        super.update(comp, object, bIncudeToolTip);
        if (hub == null) return;
        Object objao = hub.getAO();
        
        if (lastActiveObject == objao) {
            return;        
        }
        lastActiveObject = objao;        
        byte[] bs = null; 
        if (objao instanceof OAObject) {
            Object objx = ((OAObject) objao).getProperty(propertyPath);
            if (objx instanceof byte[]) { 
                bs = (byte[]) objx;
            }
        }
        
        Image img = null;
        try {
            img = OAImageUtil.convertToBufferedImage(bs);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error while creating image from bytes", e);
        }
        try {
            bCallingUpdate = true;
            setImage(img);
        }
        finally {
            bCallingUpdate = false;            
        }
    }

    public void addOpenButton(JButton cmd) {
        if (cmd != null) {
            cmdOpens = (JButton[]) OAArray.add(JButton.class, cmdOpens, cmd);
        }
    }
    
    private void updateUndoButton() {
        int x = panImage.getUndoableCount();
        comps.getUndoButton().setEnabled(x > 0 && (panImage != null) && (panImage.getImage() != null));      
        String msg = panImage.getUndoableEventMessage();
        comps.getUndoButton().setToolTipText(msg);        
    }
    private void updateCropButton() {
        boolean b = panImage.getShowRectangle();
        comps.getCropButton().setEnabled(b && (panImage != null) && (panImage.getImage() != null));      
    }

    public static ImageFileChooserController getImageFileChooserController() {
        if (controlImageFileChooser == null) {
            controlImageFileChooser = new ImageFileChooserController();
        }
        return controlImageFileChooser;
    }

    /**
     * Called whenever the current image has changed.
     */
    protected void onImageChanged() {
        updateUndoButton();
        // 20181109
        updateHubProperty();
    }
    
    protected void setup() {
        panImage = new OAImagePanel() {
            @Override
            protected void onImageChanged() {
                super.onImageChanged();
                OAImagePanelController.this.onImageChanged();
            }
            @Override
            public void setShowRectangle(boolean b) {
                super.setShowRectangle(b);
                updateCropButton();
            }
        };
        
        comps.getUndoButton().setEnabled(false);
        comps.getCropButton().setEnabled(false);
        
        comps.getCropButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onCrop();
            }
        });
      
        comps.getRotateCWButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onRotateCW();
            }
        });
        comps.getRotateCCWButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onRotateCCW();
            }
        });
        comps.getScaleButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.getScalePanelController().updateComponents();
                JComponent cmd = comps.getScaleButton();
                getScalePanelController().setScale(1.0);                
                OAImagePanelController.this.getScalePanelController().getScalePopup().show(cmd, 0, cmd.getHeight());
            }
        });
        
        comps.getBrightnessButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComponent cmd = comps.getBrightnessButton();
                int x = OAImagePanelController.this.panImage.getBrightness();
                OAImagePanelController.this.getBrightnessPanelController().setBrightness(x);
                OAImagePanelController.this.getBrightnessPanelController().getBrightnessPopup().show(cmd, 0, cmd.getHeight());
                OAImagePanelController.this.getBrightnessPanelController().getBrightnessPanel().getBrightnessSlider().requestFocus();
            }
        });
        comps.getContrastButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComponent cmd = comps.getContrastButton();
                int x = OAImagePanelController.this.panImage.getContrast();
                OAImagePanelController.this.getContrastPanelController().setContrast(x);
                OAImagePanelController.this.getContrastPanelController().getContrastPopup().show(cmd, 0, cmd.getHeight());
                OAImagePanelController.this.getContrastPanelController().getContrastPanel().getContrastSlider().requestFocus();
            }
        });
        
        comps.getZoomButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComponent cmd = comps.getZoomButton();
                double x = OAImagePanelController.this.panImage.getZoomScale();
                OAImagePanelController.this.getZoomPanelController().setZoomScale(x);
                OAImagePanelController.this.getZoomPanelController().getZoomPopup().show(cmd, 0, cmd.getHeight());
            }
        });

        
        
        comps.getBrighterButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.brighter();
            }
        });
        comps.getDarkerButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.darker();
            }
        });
        comps.getMoreContrastButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.moreContrast();
            }
        });
        comps.getLessContrastButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.lessContrast();
            }
        });
        comps.getSharpenButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.sharpen();
            }
        });
        comps.getBlurButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.blur();
            }
        });
        comps.getUndoButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.panImage.undo();
            }
        });
        
        
        comps.getOpenButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onOpen();
            }
        });
        comps.getUrlButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onOpenUrl();
            }
        });
        comps.getDeleteButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onDelete();
            }
        });
        comps.getSaveButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onSave();
            }
        });
        comps.getSaveAsButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onSaveAs();
            }
        });
        comps.getTwainButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onTwain();
            }
        });
        comps.getSignatureButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OAImagePanelController.this.onSignature();
            }
        });

    }

    protected void onOpen() {
        JFileChooser fc = getImageFileChooserController().getOpenImageFileChooser();
        
        int x = fc.showOpenDialog(OAJfcUtil.getWindow(panImage));
        if (x != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (file.length() > (4 * 1024 * 1000)) {
            JOptionPane.showMessageDialog(OAJfcUtil.getWindow(panImage), "Image file over 4mb, please use smaller size", "Open image", JOptionPane.WARNING_MESSAGE);
        }
        String fileName = file.getPath();
        try {
            BufferedImage bi = OAImageUtil.loadImage(file);
            // 20120327
            //was:  BufferedImage bi = ImageIO.read(file);
            setImage(fileName, bi);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(OAJfcUtil.getWindow(panImage), "Error reading file \"" + fileName+"\"\nError: " + e.getMessage(), "Open image", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    protected void onOpenUrl() {
        getUrlDialog().setVisible(true);
    }
    protected UrlDialog dlgUrl;
    public UrlDialog getUrlDialog() {
        if (dlgUrl != null) return dlgUrl;
        dlgUrl = new UrlDialog(OAJfcUtil.getWindow(panImage)) {
            @Override
            protected void onOk() {
                String s = getTextField().getText();
                setVisible(false);
                onOpenUrl(s);
            }
        };
        return dlgUrl;
    }
    protected void onOpenUrl(String urlLocation) {
        try {
            Image img = OAImageUtil.loadWebImage(urlLocation);
            setImage(img);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(OAJfcUtil.getWindow(panImage), "Error downloading file \"" + urlLocation+"\"\nError: " + e.getMessage(), "Download image from web", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    
    protected void onDelete() {
        setImage(null);
    }

    public void setFile(File file) {
        this.file = file;
    }
    public File getFile() {
        return file;
    }
    
    public boolean updateHubProperty() {
        if (bCallingUpdate) return true;
        if (getHub() != null && OAString.isNotEmpty(getPropertyPath())) {
            Object objx = getHub().getAO();
            if (objx instanceof OAObject) {
                Image image = getBufferedImage();
                byte[] bs = null;
                if (image != null) {
                    try {
                        bs = OAImageUtil.convertToBytes(image);
                    }
                    catch (Exception e) {}
                }
                ((OAObject) objx).setProperty(getPropertyPath(), bs);
                return true;
            }                
        }
        return false;
    }
    
    public void onSave() {
        if (panImage == null || panImage.getImage() == null) return;
        
        boolean bUpdatedObject = updateHubProperty();
        
        if (file == null) {
            if (bUpdatedObject) return;
            onSaveAs();
            return;
        }
        
        String ext = file.getName();
        int x = ext.lastIndexOf('.');
        if (x < 0 || (x == ext.length()-1)) ext = "jpg";
        else ext = ext.substring(x+1);

        try {
            RenderedImage imgx = panImage.getBufferedImage();
            FileOutputStream fos = new FileOutputStream(file);
            ImageIO.write(imgx, ext, fos);
            fos.close();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(OAJfcUtil.getWindow(panImage), "Error saving file \"" + file.getName()+"\"\nError: " + e.getMessage(), "Save image", JOptionPane.WARNING_MESSAGE);
        }
        if (bUpdatedObject) {
            file = null;
        }
    }
    
    
    
    protected void onSaveAs() {
        if (panImage == null || panImage.getImage() == null) return;
        JFileChooser fc = getImageFileChooserController().getSaveAsImageFileChooser();
        int x = fc.showSaveDialog(OAJfcUtil.getWindow(panImage));
        if (x != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        String fileName = file.getPath();
        int pos = fileName.lastIndexOf('.');
        
        String ext;
        if (pos >=0 && pos < fileName.length()-1) {
            ext = fileName.substring(pos+1).toLowerCase();
        }
        else ext = "jpg";
        
        try {
            Image img = panImage.getBufferedImage();
            FileOutputStream fos = new FileOutputStream(file);
            ImageIO.write((RenderedImage)img, ext, fos);
            fos.close();
            setFile(file);
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(OAJfcUtil.getWindow(panImage), "Error saving file \"" + fileName+"\"\nError: " + e.getMessage(), "Save image", JOptionPane.WARNING_MESSAGE);
        }
    }
    protected void onTwain() {
        //qqqqq
    }
    protected void onSignature() {
        //qqqqq
    }

    
    public OAImagePanel getOAImagePanel() {
        return panImage;
    }
    
    public ZoomPanelController getZoomPanelController() {
        if (controlZoomPanel == null) {
            controlZoomPanel = new ZoomPanelController() {
                @Override
                protected int getImageHeight() {
                    BufferedImage bi = OAImagePanelController.this.panImage.getBufferedImage();
                    if (bi == null) return 0;
                    return bi.getHeight(null);
                }
                @Override
                protected int getImageWidth() {
                    BufferedImage bi = OAImagePanelController.this.panImage.getBufferedImage();
                    if (bi == null) return 0;
                    return bi.getWidth(null);
                }
                @Override
                protected void onPerformZoom(double scale) {
                    OAImagePanelController.this.panImage.setZoomScale(scale);
                }
                @Override
                protected double getZoomScale() {
                    return OAImagePanelController.this.panImage.getZoomScale();
                }
                @Override
                protected Dimension getContainerSize() {
                    Dimension d = panImage.getSize();
                    Container c = panImage.getParent();
                    if (c != null) {
                        d = c.getSize();
                    }
                    return d;
                }
            };
        }
        return controlZoomPanel;
    }
    
    public ScalePanelController getScalePanelController() {
        if (controlScalePanel == null) {
            controlScalePanel = new ScalePanelController() {
                @Override
                protected int getImageHeight() {
                    BufferedImage bi = OAImagePanelController.this.panImage.getBufferedImage();
                    if (bi == null) return 0;
                    return bi.getHeight(null);
                }
                @Override
                protected int getImageWidth() {
                    BufferedImage bi = OAImagePanelController.this.panImage.getBufferedImage();
                    if (bi == null) return 0;
                    int x = bi.getWidth(null);
                    return x;
                }
                @Override
                protected void onPerformScale(double scale) {
                    OAImagePanelController.this.onPerformScale(scale);
                }
                @Override
                protected void onUseZoomScale() {
                    OAImagePanelController.this.onUseZoomScale();
                }
            };
        }
        return controlScalePanel;
    }

   
    public BrightnessPanelController getBrightnessPanelController() {
        if (controlBrightnessPanel == null) {
            controlBrightnessPanel = new BrightnessPanelController() {
                @Override
                protected void onStart() {
                    panImage.setDemoBrightness(true);
                }
                @Override
                protected void onEnd() {
                    panImage.setDemoBrightness(false);
                }
                @Override
                protected void onOkCommand(int x) {
                    panImage.setDemoBrightness(false);
                    panImage.setBrightness(x);
                }
                @Override
                protected void onSlideChange(int x) {
                    panImage.setBrightness(x);
                }
            };
        }
        return controlBrightnessPanel;
    }

    public ContrastPanelController getContrastPanelController() {
        if (controlContrastPanel == null) {
            controlContrastPanel = new ContrastPanelController() {
                @Override
                protected void onStart() {
                    panImage.setDemoContrast(true);
                }
                @Override
                protected void onEnd() {
                    panImage.setDemoContrast(false);
                }
                @Override
                protected void onOkCommand(int x) {
                    panImage.setDemoContrast(false);
                    panImage.setContrast(x);
                }
                @Override
                protected void onSlideChange(int x) {
                    panImage.setContrast(x);
                }
            };
        }
        return controlContrastPanel;
    }
    
    void onCrop() {
        panImage.crop();
    }
    
    
    
    void onPerformScale(double scale) {
        if (panImage == null || panImage.getImage() == null) return;
        if (scale > 0.0) {
            panImage.scale(scale);
            OAImagePanelController.this.panImage.setZoomScale(1.0);
        }
        getScalePanelController().getScalePopup().setVisible(false);
    }

    void onUseZoomScale() {
        if (panImage == null || panImage.getImage() == null) return;
        double scale = panImage.getZoomScale();
        
        if (scale > 0.0 && scale != 1.0) {
            getScalePanelController().setScale(scale);
        }
    }
    
    void onRotateCCW() {
        panImage.rotateCCW();
    }
    void onRotateCW() {
        panImage.rotateCW();
    }
    
    private int maxHeight;
    public void setMaxHeight(int x) {
        maxHeight = x;
    }
    private int maxWidth;
    public void setMaxWidth(int x) {
        maxWidth = x;
    }
    
    private static int staticMaxHeight;
    public static void setStaticMaxHeight(int x) {
        staticMaxHeight = x;
    }
    private static int staticMaxWidth;
    public static void setStaticMaxWidth(int x) {
        staticMaxWidth = x;
    }
    
    
    
    public void setImage(Image image) {
        _setImage(image);
    }
    public void setImage(String imageName, Image image) {
        _setImage(image);
    }
    
    /**
     * Note: this does not include changes made to buffered image that
     * have not been saved to image. 
     */
    public Image getImage() {
        if (panImage == null) return null;
        return panImage.getImage();
    }
    
    /**
     * Returns the current buffered image, which is he original image
     * plus changes made to it.  The changes are not yet part of the original image.
     * 
     * @see #isImageChanged()
     */
    public Image getBufferedImage() {
        if (panImage == null) return null;
        return panImage.getBufferedImage();
    }

    /**
     * Returns true if the buffered image has changes that are not saved to image.
     * @return
     */
    public boolean isImageChanged() {
        if (panImage == null) return false;
        return panImage.isChanged();
    }
    
    
    /**
     * This is called by both setImage methods.
     */
    protected void _setImage(Image image) {
        if (panImage == null) return;
        
        int wx = Math.max(maxWidth, staticMaxWidth); 
        int hx = Math.max(maxHeight, staticMaxHeight); 
        
        if (image != null && (wx > 0 || hx > 0) ) {
            image = OAImageUtil.scaleDownToSize(image, wx, hx);
        }
        panImage.setImage(image);
        
        if (updateHubProperty()) file = null;

        updateCommands();
    }
    
    private boolean bEnabled = true;
    public void setEnabled(boolean b) {
        bEnabled = b;
        updateCommands();
    }
    public boolean getEnabled() {
        return bEnabled;
    }
    public void updateCommands() {
        boolean b = (panImage != null && panImage.getImage() != null && bEnabled);
        
        b = b && (hub == null || hub.getAO() != null);
        
        comps.getCropButton().setEnabled(b && panImage.getShowRectangle());
        
        comps.getRotateCWButton().setEnabled(b);
        comps.getRotateCCWButton().setEnabled(b);
        comps.getZoomButton().setEnabled(b);
        comps.getScaleButton().setEnabled(b);
        comps.getBrightnessButton().setEnabled(b);
        comps.getContrastButton().setEnabled(b);
        
        updateUndoButton();
        
        comps.getOpenButton().setEnabled(bEnabled && (hub == null || hub.getAO() != null));
        for (int i=0; cmdOpens != null && i < cmdOpens.length; i++) {
            cmdOpens[i].setEnabled(bEnabled);
        }
        
        comps.getTwainButton().setEnabled(bEnabled);
        comps.getSignatureButton().setEnabled(bEnabled);

        comps.getDeleteButton().setEnabled(b);
        comps.getSaveButton().setEnabled(b);
        comps.getSaveAsButton().setEnabled(b);
        if (splitButtonSave != null) splitButtonSave.setEnabled(b);
        
        panImage.setEnabled(b);
    }
    
    private OAMultiButtonSplitButton splitButtonSave; 
    private OAMultiButtonSplitButton splitButtonOpen; 

    public JToolBar getToolBar() {
        if (toolBar != null) return toolBar;
        
        toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        
        // 20181109
        addOpenButton(comps.getUrlButton());
        comps.getUrlButton().setText("Web Download ...");
        
        if (cmdOpens != null && cmdOpens.length > 0) {
            splitButtonOpen = new OAMultiButtonSplitButton();
            splitButtonOpen.setAllowChangeMasterButton(false);
            splitButtonOpen.setShowTextInSelectedButton(false);
            OAButton.setup(splitButtonOpen);

            comps.getOpenButton().setText("Open ...");
            OAButton.setup(comps.getOpenButton());
            splitButtonOpen.addButton(comps.getOpenButton(), true);
            
            for (int i=0; i<cmdOpens.length; i++) {
                splitButtonOpen.addButton(cmdOpens[i]);
            }
            toolBar.add(splitButtonOpen);
        }
        else {
            toolBar.add(comps.getOpenButton());
        }
        /*qqq
        toolBar.add(comps.getTwainButton());
        toolBar.add(comps.getSignatureButton());
        */
        
        splitButtonSave = new OAMultiButtonSplitButton();
        splitButtonSave.setAllowChangeMasterButton(false);
        splitButtonSave.setShowTextInSelectedButton(false);
        OAButton.setup(splitButtonSave);
        
        comps.getSaveButton().setText("Save");
        OAButton.setup(comps.getSaveButton());
        splitButtonSave.addButton(comps.getSaveButton(), true);
        comps.getSaveAsButton().setText("Save as ...");
        OAButton.setup(comps.getSaveAsButton());

        splitButtonSave.addButton(comps.getSaveAsButton());
        splitButtonSave.setRequestFocusEnabled(false);
        splitButtonSave.setFocusPainted(false);
        
        
        toolBar.add(splitButtonSave);
        toolBar.addSeparator();

        
        toolBar.add(comps.getDeleteButton());
        toolBar.add(comps.getUndoButton());

        toolBar.addSeparator();
        toolBar.add(comps.getCropButton());
        toolBar.addSeparator();
        toolBar.add(comps.getRotateCWButton());
        toolBar.add(comps.getRotateCCWButton());
        toolBar.addSeparator();
        toolBar.add(comps.getZoomButton());
        toolBar.add(comps.getScaleButton());
        toolBar.addSeparator();
        toolBar.add(comps.getBrightnessButton());
        toolBar.add(comps.getContrastButton());
        /*
        toolBar.add(comps.getSharpenButton());
        toolBar.add(comps.getBlurButton());
        */
//qqq add redo        
        return toolBar;
    }

    
    
    
    
    public static void Xmain(String[] args) throws Exception {

        System.out.println("Hello World");

        JFrame kyle = new JFrame();
        
        kyle.setBounds(500, 200, 600, 400);
       
        kyle.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        kyle.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton xxx = new JButton("Hello");
        // xxx.setEnabled(false);
        
        kyle.add(xxx);
        
        
        kyle.setVisible(true);
    }
    
    
/**    
    public static void main(String[] args) throws Exception {
        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
        }

        JFrame frm = new JFrame();
        frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frm.setLayout(new BorderLayout());
        OAImagePanelController cont = new OAImagePanelController() {
            int qqq;
            @Override
            public void onImageChanged() {
                System.out.println("onImageChnnage "+(qqq++));                
            }
        };

        JToolBar tool = cont.getToolBar();
        tool.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0,false), "esc");
        tool.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0,false), "esc");
        tool.getActionMap().put("esc", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                System.out.println("Very Cool!");
                System.exit(0);
            }
        });

        frm.add(cont.getToolBar(), BorderLayout.NORTH);
        cont.getOAImagePanel().setBorder(new LineBorder(Color.lightGray, 1));
        frm.add(new JScrollPane(cont.getOAImagePanel()), BorderLayout.CENTER);
        frm.setSize(500, 500);
        frm.setVisible(true);
        File file = new File("c:\\test.gif");
        cont.setFile(file);
        try {
        cont.setImage(ImageIO.read(file));
        }
        catch (Exception e) {}
    }
*/    
}

