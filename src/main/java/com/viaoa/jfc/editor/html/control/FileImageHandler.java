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
