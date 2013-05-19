package com.viaoa.jfc.editor.html.control;

import java.awt.Image;


/**
 * Used by OAHTMLTextPane to manage images.
 * @author vvia
 */
public interface ImageHandlerInterface {
    
    /**
     * Load the image.
     * @param name name of image
     */
    public Image loadImage(String srcName);

    /**
     * Insert an image
     * @param srcName name of source to load
     * @param img the image to use
     * @return the new name for source
     */
    public String onInsertImage(String srcName, Image img) throws Exception;
    
    /**
     * Update an image, after it has been changed.
     * @param srcName
     * @param img
     */
    public void onUpdateImage(String srcName, Image img);
    

    public void onDeleteImage(String srcName, Image img) throws Exception;
    
}
