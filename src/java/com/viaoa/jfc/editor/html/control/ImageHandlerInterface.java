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
