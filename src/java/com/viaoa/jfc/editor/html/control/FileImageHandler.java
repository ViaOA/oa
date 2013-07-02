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
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.viaoa.jfc.image.OAImageUtil;

/**
 * Used by OAHTMLTextPane for manage file images.
 * Note: this is not needed, as the OAHTMLTextPane will perform this same
 * functionality by default.
 * @author vvia
 */
public class FileImageHandler implements ImageHandlerInterface {
    
    @Override
    public Image loadImage(String srcName) {
        if (srcName == null) return null;
        File file = new File(srcName);
        if (!file.exists()) return null;
        try {
            return ImageIO.read(file);
        }
        catch (Exception e) {
        }
        return null;
    }

    @Override
    public String onInsertImage(String srcName, Image img) {
        return srcName;
    }
    
    @Override
    public void onUpdateImage(String srcName, Image img) {
        if (srcName == null) return;
        try {
            BufferedImage bi = OAImageUtil.convertToBufferedImage(img);
            int pos = srcName.indexOf('.');
            String s;
            if (pos >= 0 && pos < srcName.length()) s = srcName.substring(pos);
            else s = "jpg";
            ImageIO.write(bi, s, new File(srcName));
        }
        catch (Exception e) {
        }
    }

    @Override
    public void onDeleteImage(String srcName, Image img) {
        // todo: qqqqqqqqqq
    }
}
