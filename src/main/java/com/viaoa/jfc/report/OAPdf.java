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
package com.viaoa.jfc.report;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.DefaultFontMapper.BaseFontParameters;
import com.viaoa.jfc.image.OAImageUtil;
import com.viaoa.jfc.print.OAPrintable;

/**
 * Generate pdf documents, using printable.
 * 
 * @author vvia
 */
public class OAPdf {
    private static Logger LOG = Logger.getLogger(OAPdf.class.getName());

    private Document document;
    private PdfWriter writer;
    private PdfContentByte contentByte;
    private PageFormat pageFormat;
    private OutputStream outputStream;
    private float width, height;
    private static DefaultFontMapper mapper;
    private String author;
    private String title;
    private String subject;
    private byte[] bsBackgroundImage;
    private int beginPage = -1, endPage = -1;

    public void setAuthor(String s) {
        this.author = s;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public void setSubject(String s) {
        this.subject = s;
    }

    public void setBeginPage(int x) {
        this.beginPage = x;
    }

    public void setEndPage(int x) {
        this.endPage = x;
    }

    /** set the background image to use. It will be scaled to fit the page width/height */
    public void setBackgroundImage(java.awt.Image img) throws Exception {
        if (img == null) {
            this.bsBackgroundImage = null;
        }
        else {
            byte[] bs = OAImageUtil.convertToJPG(img);
            this.bsBackgroundImage = bs;
        }
    }

    public void setBackgroundImage(byte[] bs) throws Exception {
        this.bsBackgroundImage = bs;
    }

    public byte[] saveAsBytes(Printable printable, PageFormat pageFormat) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(12000);
        createPdf(printable, pageFormat, baos);
        return baos.toByteArray();
    }

    public void saveToFile(Printable printable, PageFormat pageFormat, String fileName) throws Exception {
        FileOutputStream fos = new FileOutputStream(fileName);
        createPdf(printable, pageFormat, fos);
    }

    public void createPdf(Printable printable, PageFormat pageFormat, OutputStream outputStream) throws Exception {
        setup(pageFormat, outputStream);
        createPdf(printable);
    }

    protected void setup(PageFormat pageFormat, OutputStream outputStream) throws Exception {
        this.pageFormat = pageFormat;
        this.outputStream = outputStream;

        this.width = (float) pageFormat.getWidth();
        this.height = (float) pageFormat.getHeight();

        document = new Document();

        document.addAuthor(author);
        document.addCreator("ViaOA");
        document.addCreationDate();
        document.addTitle(title);
        document.addSubject(subject);
        // document.addProducer(); // itext + version

        document.setPageSize(new com.lowagie.text.Rectangle(width, height)); // float margin L,R,TB
                                                                             // qqqqqqq might need to
                                                                             // add the margins using
                                                                             // PageFormat

        writer = PdfWriter.getInstance(document, outputStream);

        document.open();

        contentByte = writer.getDirectContent();

        if (bsBackgroundImage != null) {
            // add image
            // http://thinktibits.blogspot.com/2011/04/java-itext-pdf-add-watermark-example.html#.UJcTmMXA8WE
            Image backgroundImage = Image.getInstance(bsBackgroundImage);
            // Image backgroundImage = Image.getInstance("cert150dpi.jpg");
            backgroundImage.setAbsolutePosition(0, 0); // co-ords are diff then Java - bottom/left is
                                                       // 0,0
            backgroundImage.scaleToFit(width, height);
            contentByte.addImage(backgroundImage);
        }
    }

    
    public static DefaultFontMapper getFontMapper() {
        if (mapper != null) return mapper;
        mapper = new DefaultFontMapper() {
            @Override
            public BaseFontParameters getBaseFontParameters(String name) {
                BaseFontParameters bfp = super.getBaseFontParameters(name);
                if (bfp != null) bfp.encoding = BaseFont.IDENTITY_H; // utf
                return bfp;
            }
        };
        mapper.insertDirectory("c:/windows/fonts/");
        return mapper;
    }
    
    protected void createPdf(Printable printable) throws Exception {
        if (printable instanceof OAPrintable) {
            ((OAPrintable) printable).beforePrint(pageFormat);
        }

        BufferedImage bi = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        Graphics gTemp = bi.getGraphics();


        for (int p = 0;; p++) {
            if (beginPage >= 0 && p < beginPage) continue;
            if (endPage >= 0 && p > endPage) break;

            if (p > 0) {
                int x = printable.print(gTemp, pageFormat, p);
                if (x == Printable.NO_SUCH_PAGE) break;
                document.newPage();
            }

            Graphics2D g2 = contentByte.createGraphics(width, height, getFontMapper());

            int x = printable.print(g2, pageFormat, p);
            g2.dispose();
            if (x == Printable.NO_SUCH_PAGE) break;
        }
        printable.print(null, null, -1); // this is used so that printable will know that the printjob
                                         // is done.

        document.close();
        outputStream.flush();
        outputStream.close();

        if (printable instanceof OAPrintable) {
            ((OAPrintable) printable).afterPrint();
        }
    }
}
