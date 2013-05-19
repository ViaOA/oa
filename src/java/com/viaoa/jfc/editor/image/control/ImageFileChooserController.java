package com.viaoa.jfc.editor.image.control;

import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;


/**
 * Controls open file and save as file JFileChoosers, that are able to work
 * with valid image files.
 */
public class ImageFileChooserController {

    private JFileChooser fileChooserOpenImage, fileChooserSaveAsImage;
    private String[] openFileNameExtensions;
    
    
    public JFileChooser getOpenImageFileChooser() {
        if (fileChooserOpenImage == null) {
            openFileNameExtensions = ImageIO.getReaderFileSuffixes();
            if (openFileNameExtensions == null) openFileNameExtensions = new String[0];
            for (int i=0; i<openFileNameExtensions.length; i++) {
                openFileNameExtensions[i] = openFileNameExtensions[i].toLowerCase(); 
            }
            
            fileChooserOpenImage = new JFileChooser();
            
            for (int i=0; i<openFileNameExtensions.length; i++) {
                openFileNameExtensions[i] = openFileNameExtensions[i].toLowerCase(); 
                final String fileNameExt = openFileNameExtensions[i];
                
                javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {  // controls files that can be selected
                    public boolean accept(File f) {
                        if ( f.isDirectory() ) return true;
                        if ( f.getName().toLowerCase().endsWith("."+fileNameExt) ) {
                            return true;
                        }
                        return false;
                    }
                    public String getDescription() {
                        return "*."+fileNameExt;
                    }
                };
                fileChooserOpenImage.addChoosableFileFilter(filter);
            }
            

            javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {  // controls files that can be selected
                public boolean accept(File f) {
                    if ( f.isDirectory() ) return true;
                    for (String s : openFileNameExtensions) {
                        if ( f.getName().toLowerCase().endsWith("."+s) ) {
                            return true;
                        }
                    }
                    return false;
                }
                public String getDescription() {
                    return "All image files";
                }
            };
            fileChooserOpenImage.addChoosableFileFilter(filter);
            
            
            
            fileChooserOpenImage.setDialogTitle("Open image file");
            fileChooserOpenImage.setDialogType(JFileChooser.OPEN_DIALOG);
            fileChooserOpenImage.setFileHidingEnabled(false);
            fileChooserOpenImage.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooserOpenImage.setMultiSelectionEnabled(false);
            fileChooserOpenImage.setAcceptAllFileFilterUsed(false);
        }
        return fileChooserOpenImage;
    }

    public JFileChooser getSaveAsImageFileChooser() {
        if (fileChooserSaveAsImage == null) {
            
            fileChooserSaveAsImage = new JFileChooser() {
                public @Override void approveSelection() {
                    File f = getSelectedFile();
                    File fx = convertToValidImageFile(f, fileChooserSaveAsImage);

                    boolean bDone = true;
                    
                    if (getDialogType() == SAVE_DIALOG && fx != null && f != fx) {
                        int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                                "The selected file \""+f.getName()+"\" does not have a file name extension. " +
                                "\nWould you like to use file name \"" + fx.getName() + "\"?",
                                "No file extension",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        switch(result)  {
                        case JOptionPane.YES_OPTION:
                            setSelectedFile(fx);
                            bDone = false;
                            break;
                        case JOptionPane.NO_OPTION:
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                        }
                        
                    }
                    else if (getDialogType() == SAVE_DIALOG && fx == null) {
                        int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                                "The selected file \""+f.getName()+"\" cant be saved as an Image. " +
                                "\nPlease select a file name extension using the dropdown list",
                                "Unknown image file type",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.INFORMATION_MESSAGE);
                        switch(result)  {
                        case JOptionPane.OK_OPTION:
                            bDone = false;
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            return;
                        }
                    }
                    else if (getDialogType() == SAVE_DIALOG && fx.exists() ) {
                        int result = JOptionPane.showConfirmDialog(getTopLevelAncestor(),
                                "The selected file \""+fx.getName()+"\" already exists. " +
                                "Do you want to overwrite it?",
                                "The file already exists",
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE);
                        switch(result)  {
                        case JOptionPane.YES_OPTION:
                            break;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                        }
                    }
                    if (bDone) super.approveSelection();
                }
            };
            
            getValidSaveAsImageExtensions();
            for (int i=0; i<saveAsFileNameExtensions.length; i++) {
                saveAsFileNameExtensions[i] = saveAsFileNameExtensions[i].toLowerCase(); 
                final String fileNameExt = saveAsFileNameExtensions[i];
                javax.swing.filechooser.FileFilter filter = new javax.swing.filechooser.FileFilter() {  // controls files that can be selected
                    public boolean accept(File f) {
                        if ( f.getName().toLowerCase().endsWith("."+fileNameExt) ) {
                            return true;
                        }
                        if ( f.isDirectory() ) return true;
                        return false;
                    }
                    public String getDescription() {
                        return "*."+fileNameExt;
                    }
                };
                fileChooserSaveAsImage.addChoosableFileFilter(filter);
            }
            
            
            fileChooserSaveAsImage.setDialogTitle("Save image to file");
            fileChooserSaveAsImage.setDialogType(JFileChooser.SAVE_DIALOG);
            fileChooserSaveAsImage.setFileHidingEnabled(false);
            fileChooserSaveAsImage.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooserSaveAsImage.setMultiSelectionEnabled(false);
            fileChooserSaveAsImage.setAcceptAllFileFilterUsed(false);
        }
        return fileChooserSaveAsImage;
    }
    
    
    
    
    
    // adds correct file extension to filename.
    private File convertToValidImageFile(File file, JFileChooser fileChooser) {
        if (fileChooser == null || file == null) return file;
        
        String fname = file.getName();
        int pos = fname.lastIndexOf('.');
        String ext;
        if (pos < 0) {
            ext  = fileChooser.getFileFilter().getDescription();
            if (ext != null && ext.length() > 2) {  // ex: "*.jpg"
                ext = ext.substring(2).toLowerCase();
                fname = file.getPath() + "." + ext;
                file = new File(fname);
            }
        }
        else {
            ext = fname.substring(pos+1);
        }
        
        // verify file extension as valid image format
        getValidSaveAsImageExtensions();
        boolean bValid = false;
        for (int i=0; i<saveAsFileNameExtensions.length; i++) {
            if (ext.equalsIgnoreCase(saveAsFileNameExtensions[i])) {
                bValid = true;
                break;
            }
        }
        if (!bValid) return null; 
        
        return file;
    }
    
    
    private String[] saveAsFileNameExtensions;
    protected String[] getValidSaveAsImageExtensions() {
        if (saveAsFileNameExtensions == null) {
            saveAsFileNameExtensions = ImageIO.getWriterFileSuffixes();
            if (saveAsFileNameExtensions == null) saveAsFileNameExtensions = new String[0];
            for (int i=0; i<saveAsFileNameExtensions.length; i++) {
                saveAsFileNameExtensions[i] = saveAsFileNameExtensions[i].toLowerCase(); 
            }
        }
        return saveAsFileNameExtensions;
    }
    
    
    
}
