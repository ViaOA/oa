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
