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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.editor.html.protocol.oaproperty.Handler;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.object.OAObject;
import com.viaoa.object.OAObjectReflectDelegate;
import com.viaoa.util.OAConv;


/**
 * Used by OAHTMLTextPane to use OAObjects to store images in an OAObject.
 * 
 * This will use an Image src protocol/scheme of "oaproperty://className.../prop?id"
 * and will also work when setting the HTML src attribute to the value of the object Id
 * or by finding the src in the objects sourceName. 
 * 
 * Note: this is not needed for readonly/reports, only for Editors; since the "oaproperty://" url Handler
 * will be able to find the image in the object property.
 * 
 */
public class Class2ImageHandler implements ImageHandlerInterface {
    private static Logger LOG = Logger.getLogger(Class2ImageHandler.class.getName());
    
    
    private OAHTMLTextPane txtHtml;
    private Hub hub;
    private String byteArrayPropertyName;
    private String sourceNamePropertyName;
    private String idPropertyName;    
    
    
    static {
        // make sure that the "oaproperty://" url handler is installed.
        //   it is used to automatically load URL images from OAObject property.
        com.viaoa.jfc.editor.html.protocol.classpath.Handler.register();
    }
    
    /**
     * 
     * @param hub Hub that has all images in it.
     * @param byteArrayPropertyName name of property that is for a byte[] to store "raw"
     * @param sourceNamePropertyName property to use to store the source of the image (ex: file name) 
     * @param idPropertyName unique property value to use for the html img src tag. If null, then sourceNamePropertyName will be used.
     */
    public Class2ImageHandler(OAHTMLTextPane txtHtml, Class<? extends OAObject> clazz, String byteArrayPropertyName, String sourceNamePropertyName, String idPropertyName) {
        this.txtHtml = txtHtml;
        this.hub = new Hub(clazz);
        this.byteArrayPropertyName = byteArrayPropertyName;
        this.sourceNamePropertyName = sourceNamePropertyName;
        this.idPropertyName = idPropertyName;
        
        txtHtml.setImageHandler(this);
    }


    /**
     * Finds the object that is storing the image, using the html image src attribute.
     * This will take 3 approaches to find the object:
     * 1: if using oaproperty:// scheme, it will use the value after "?" as the object id.
     * 2: it will use the src as the object Id.
     * 3: if will find object based on the sourceName property in the object.
     * @param srcName URL of image.
     * @return object that is storing the image
     * @see com.viaoa.jfc.editor.html.protocol.oaproperty.Handler used by HTMLTextPane to directly get image from OAObject property
     */
    protected OAObject getObject(String srcName) {
        // url ex: "oaproperty://com.vetplan.oa.ImageStore/bytes?12.abc"
        OAObject obj = null;
        if (srcName == null) return obj;
        int pos = srcName.indexOf('?');
        if (pos < 0) return null;

        String id = null;
        
        // 20130217 get Id from oaprperty scheme
        id = srcName.substring(pos+1);    
        
        /* was
        String query = srcName;
        String[] params = query.split("&");

        for (int i=0; i<params.length; i++) {
            String s = params[i];
            pos = s.indexOf("=");
            if (pos < 0) {
                if (id == null) id = s;
                continue;
            }
            s = s.substring(0, pos);
            String s2 = params[i].substring(pos+1); 
            if (s.equalsIgnoreCase("id")) {
                id = s.substring(pos+1);
            }
        }
        */
        hub.select(idPropertyName + " = ?", new Object[] {id});
        obj = (OAObject) hub.getAt(0);
        return obj;
    }
    
    
    /**
     * Called by editor to get/load an image.
     */
    @Override
    public Image loadImage(String srcName) {
        if (srcName == null) return null;

        OAObject obj = getObject(srcName);
        
        if (obj == null) {
            // LOG.warning("cant find image for src="+srcName);
            // url handler will be called
        }
        else {
            byte[] bs = (byte[]) obj.getProperty(byteArrayPropertyName);
            if (bs != null) {
                try {
                    return OAImageUtil.convertToBufferedImage(bs);
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "cant convert bytes to image for "+srcName, e);
                }
            }
        }
        return null;
    }
    
    /**
     * Called by editor when an image is inserted in a document.
     * @param srcName name of image
     */
    @Override
    public String onInsertImage(String srcName, Image img) throws Exception {
        // will save as jpg encoded bytes
        BufferedImage bi = OAImageUtil.convertToBufferedImage(img);
        
        OAObject obj = (OAObject) OAObjectReflectDelegate.createNewObject(hub.getObjectClass());
        obj.setProperty(byteArrayPropertyName, OAImageUtil.convertToBytes(bi));
        obj.setProperty(sourceNamePropertyName, srcName);
        
        obj.save();
        srcName = "oaproperty://" + hub.getObjectClass().getName() + "/" + byteArrayPropertyName + "?" + obj.getPropertyAsString(idPropertyName);
        return srcName;
    }

    /**
     * Called by editor when an image has been changed.
     */
    @Override
    public void onUpdateImage(String srcName, Image img) {
        OAObject obj = getObject(srcName);
        if (obj == null) {
            LOG.warning("cant find image for src="+srcName);
        }
        else {
            try {
                byte[] bs = OAImageUtil.convertToBytes(OAImageUtil.convertToBufferedImage(img));
                obj.setProperty(byteArrayPropertyName, bs);
                obj.save();
            }
            catch (Exception e) {
                LOG.log(Level.WARNING, "Error reading image file "+srcName, e);
            }
        }
    }

    @Override
    public void onDeleteImage(String srcName, Image img) {
        OAObject obj = getObject(srcName);
        if (obj == null) {
            LOG.warning("cant find image for src="+srcName);
        }
        else {
            obj.delete();
        }
    }
}




