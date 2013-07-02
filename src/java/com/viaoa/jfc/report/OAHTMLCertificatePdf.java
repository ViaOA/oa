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
package com.viaoa.jfc.report;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterException;
import java.net.URL;
import java.util.Map;

import javax.swing.ImageIcon;

import com.viaoa.jfc.editor.html.OAHTMLTextPane;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.jfc.print.OAPrintUtil;
import com.viaoa.jfc.print.OAPrintable;
import com.viaoa.util.OAString;

/**
   This uses HTML that has a (hi-res) background image to create a certificate 
   the size of the background image.
   
   This is used with OAPdf to create the pdf.

   This is based on the fact that OAHTMLTextPane was used to create a <DIV> with a background image:
   <pre>
   <div style="height:840; background-repeat:no-repeat; background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/Bytes?23342&h=840&w=1140); width:1140">
   </pre> 
 */
public class OAHTMLCertificatePdf implements OAPrintable {

    private PageFormat pf;
    private int width, height;
    private Image image;
    private String html;
    private OAHTMLTextPane textPane;
    
    public void setHtml(String html) {
        this.html = html;
        prepareForPdf();
    }
    
    public Image getImage() {
        return image;
    }
    public void setImage(Image img) {
        this.image = img;
    }
    public PageFormat getPageFormat() {
        return pf;
    }

    /** create pdf, using the background image as a hi-res PDF background image */
    protected void prepareForPdf() {
        width = 0;
        height = 0;
        
//<div style="background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/Bytes?23422&h=360&w=540); width:540; height:360; background-repeat:no-repeat">
        
//background-image:url(oaproperty://com.tmgsc.hifive.model.oa.ImageStore/Bytes?23342&h=840&w=1140); width:1140">
        
        String find = "background-image:url(";
        int pos = html.toLowerCase().indexOf(find.toLowerCase());

        
        image = null;
        if (pos >= 0) {
            // get the width and height of the div style
            int posDivBegin = html.lastIndexOf("<div", pos);
            if (posDivBegin >= 0) {
                int posDivEnd = html.indexOf(">", posDivBegin);
                if (posDivEnd > 0) {
                    String s = OAString.substring(html, posDivBegin, posDivEnd+1).toLowerCase();
                    Map<String, String> map = OAString.getHTMLAttributeMap(s);
                    String style = map.get("style");
                    if (style != null) {
                        map = OAString.getCSSMap(style);
                        s = map.get("width");
                        if (s != null && OAString.isNumber(s)) width = Integer.valueOf(s);
                        s = map.get("height");
                        if (s != null && OAString.isNumber(s)) height = Integer.valueOf(s);
                    }
                }
            }

            // get image
            int posBegin = pos;
            pos = html.indexOf('(', pos);
            int posEnd = html.indexOf(")", pos);
            int pos2 = html.indexOf("&", pos);
            if (pos2 < 0 || pos2 > posEnd) pos2 = posEnd;

            if (pos2 > 0) {
                String strUrl = html.substring(pos+1, pos2);
                try {
                    URL url = new URL(strUrl);
                    ImageIcon ii = new ImageIcon(url);
                    image = ii.getImage();
                    OAImageUtil.loadImage(image);
                }
                catch (Exception e) {
                }
                
                // remove backgrd image from html, since it will be put directly into PDF document
                html = html.substring(0, posBegin) + html.substring(posEnd+1); 
            }
        }
        
        if (image != null) {
            if (width < 1) width = image.getWidth(null);
            if (height < 1) height = image.getHeight(null);
        }
        if (width < 1) width = 800;
        if (height < 1) height = 800;

        // need to convert from pixels to point sizes for printing/paper
        width = (int) OAPrintUtil.convertPixelsToPoints(width);
        height = (int) OAPrintUtil.convertPixelsToPoints(height);
        
        // paper/page setup
        pf = new PageFormat();
        Paper paper = pf.getPaper();
        
        
        // html
        textPane = new OAHTMLTextPane();
        textPane.setSize(width, Integer.MAX_VALUE);
        textPane.setText(html);
        
        paper.setImageableArea(0, 0, width, height);
        paper.setSize(width, height);
        pf.setPaper(paper);
    }

    
    @Override
    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
        return textPane.print(graphics, pageFormat, pageIndex);
    }
    @Override
    public int preview(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        return textPane.preview(graphics, pageFormat, pageIndex);
    }

    @Override
    public Dimension getPrintSize(int pageIndex, PageFormat pageFormat, int width) {
        return textPane.getPrintSize(pageIndex, pageFormat, width);
    }

    @Override
    public void beforePrint(PageFormat pageFormat) {
        textPane.beforePrint(pageFormat);
    }

    @Override
    public void afterPrint() {
        textPane.afterPrint();
    }

    @Override
    public void beforePreview(PageFormat pageFormat) {
        textPane.beforePreview(pageFormat);
    }

    @Override
    public void afterPreview() {
        textPane.afterPreview();
    }
    

    public static void main(String[] args) throws Exception {
        OAHTMLCertificatePdf hpc = new OAHTMLCertificatePdf();

        String s = "<div style=\"height:672; width:912; background-repeat:no-repeat; background-image:url(file:/temp/xx.png);\">";
        //<img src='file:/temp/test.png'>        
        hpc.setHtml("<html><body>"+s+"<br><br><br><b>abcdefGHIJKlmno</b><h1>test 1234</h1></div></body></html>");
        
        OAPdf op = new OAPdf();
        op.setAuthor("author name here");
        op.setSubject("subject here");
        op.setBackgroundImage(hpc.getImage());
        op.setTitle("title here");
        op.saveToFile(hpc, hpc.getPageFormat(), "c:/temp/test.pdf");
        
        System.out.println("done");
    }
    
}


